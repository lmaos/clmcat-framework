package com.clmcat.basics.commons.snowflake.strategy;

import com.clmcat.basics.commons.snowflake.SnowflakeBitAwareStrategy;
import com.clmcat.basics.commons.snowflake.SnowflakeDependentValueStrategy;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class SequenceStrategy implements SnowflakeDependentValueStrategy, SnowflakeBitAwareStrategy {
    private final boolean grouped;
    private final GroupCleanup cleanup;
    private volatile long maxValue = Long.MAX_VALUE;
    private long lastDependency = Long.MIN_VALUE;
    private long currentValue = -1L;
    private final LinkedHashMap<Long, GroupState> groups = new LinkedHashMap<>(16, 0.75f, true);

    /**
     * 兼容入口，等价于 {@link #grouped()}。
     *
     * @return 仅分组序号策略；每个 dependencyValue 拥有独立计数器
     */
    public static SequenceStrategy create() {
        return grouped();
    }

    /**
     * 兼容入口，等价于 {@link #grouped(GroupCleanup)}。
     *
     * @param cleanup 分组清理策略，可限制组数量或约束可保留的 dependencyValue 窗口
     * @return 带清理策略的分组序号策略
     */
    public static SequenceStrategy create(GroupCleanup cleanup) {
        return grouped(cleanup);
    }

    /**
     * 创建标准序号策略。
     *
     * @return 标准序号策略；只记录最近一次 dependencyValue，dependency 切换后重新从 0 开始
     */
    public static SequenceStrategy standard() {
        return new SequenceStrategy(false, GroupCleanup.none());
    }

    /**
     * 创建仅分组序号策略。
     *
     * @return 仅分组序号策略；每个 dependencyValue 都有独立序号，不做组清理
     */
    public static SequenceStrategy grouped() {
        return new SequenceStrategy(true, GroupCleanup.none());
    }

    /**
     * 创建带分组清理能力的序号策略。
     *
     * @param cleanup 分组清理策略；可限制最大组数，或按 dependencyValue 的窗口范围清理旧组
     * @return 分组序号策略
     */
    public static SequenceStrategy grouped(GroupCleanup cleanup) {
        return new SequenceStrategy(true, cleanup);
    }

    /**
     * 创建按 dependencyValue 窗口清理旧组的分组序号策略。
     *
     * @param dependencyWindow dependencyValue 可保留的窗口范围；小于 {@code 当前 dependencyValue - dependencyWindow}
     *                         的旧组会被清理，适合时间型 dependency
     * @return 分组 + 窗口清理序号策略
     */
    public static SequenceStrategy groupedWindow(long dependencyWindow) {
        return grouped(cleanup().dependencyWindow(dependencyWindow));
    }

    /**
     * 创建同时限制组数量和 dependencyValue 窗口的分组序号策略。
     *
     * @param maxGroups 最大保留组数；超过后会按最旧组逐步清理
     * @param dependencyWindow dependencyValue 可保留的窗口范围；适合时间型 dependency
     * @return 分组 + 窗口清理序号策略
     */
    public static SequenceStrategy groupedWindow(int maxGroups, long dependencyWindow) {
        return grouped(cleanup().maxGroups(maxGroups).dependencyWindow(dependencyWindow));
    }

    /**
     * 兼容入口，等价于 {@link #standard()}。
     *
     * @return 标准序号策略；dependency 切换时重置序号
     */
    public static SequenceStrategy resetOnDependencyChange() {
        return standard();
    }

    /**
     * 创建分组清理配置对象。
     *
     * @return 空白清理配置，可继续链式调用 {@link GroupCleanup#maxGroups(int)}、
     * {@link GroupCleanup#dependencyWindow(long)}
     */
    public static GroupCleanup cleanup() {
        return GroupCleanup.none();
    }

    private SequenceStrategy(boolean grouped, GroupCleanup cleanup) {
        this.grouped = grouped;
        this.cleanup = cleanup == null ? GroupCleanup.none() : cleanup;
    }

    @Override
    public synchronized void initialize(int bits, long maxValue) {
        this.maxValue = maxValue < 0 ? Long.MAX_VALUE : maxValue;
        this.lastDependency = Long.MIN_VALUE;
        this.currentValue = -1L;
        this.groups.clear();
    }

    @Override
    public synchronized long next(long dependencyValue) {
        if (grouped) {
            return nextGrouped(dependencyValue);
        }
        if (dependencyValue != lastDependency) {
            lastDependency = dependencyValue;
            currentValue = 0L;
            return currentValue;
        }
        if (currentValue >= maxValue) {
            throw new IllegalStateException("Snowflake sequence overflow for dependency value " + dependencyValue);
        }
        currentValue++;
        return currentValue;
    }

    @Override
    public long min(long dependencyValue) {
        return 0L;
    }

    @Override
    public long max(long dependencyValue) {
        return maxValue;
    }

    private long nextGrouped(long dependencyValue) {
        cleanupGroups(dependencyValue);

        GroupState groupState = groups.get(dependencyValue);
        if (groupState == null) {
            groupState = new GroupState();
            groups.put(dependencyValue, groupState);
            cleanupGroups(dependencyValue);
        }

        if (groupState.currentValue >= maxValue) {
            throw new IllegalStateException("Snowflake sequence overflow for dependency value " + dependencyValue);
        }
        groupState.currentValue++;
        return groupState.currentValue;
    }

    private void cleanupGroups(long dependencyValue) {
        if (cleanup.dependencyWindow != null) {
            Iterator<Map.Entry<Long, GroupState>> iterator = groups.entrySet().iterator();
            while (iterator.hasNext()) {
                long key = iterator.next().getKey();
                if (isOutsideDependencyWindow(key, dependencyValue, cleanup.dependencyWindow)) {
                    iterator.remove();
                }
            }
        }

        if (cleanup.maxGroups != null) {
            while (groups.size() > cleanup.maxGroups) {
                Iterator<Map.Entry<Long, GroupState>> iterator = groups.entrySet().iterator();
                if (!iterator.hasNext()) {
                    break;
                }
                iterator.next();
                iterator.remove();
            }
        }
    }

    private boolean isOutsideDependencyWindow(long key, long dependencyValue, long dependencyWindow) {
        return key < dependencyValue - dependencyWindow;
    }

    private static final class GroupState {
        private long currentValue = -1L;
    }

    public static final class GroupCleanup {
        private Integer maxGroups;
        private Long dependencyWindow;

        private GroupCleanup() {
        }

        public static GroupCleanup none() {
            return new GroupCleanup();
        }

        /**
         * 设置最大保留组数。
         *
         * @param maxGroups 最大组数，必须大于 0；超过后会移除最旧的组
         * @return 当前清理配置，便于链式调用
         */
        public GroupCleanup maxGroups(int maxGroups) {
            if (maxGroups <= 0) {
                throw new IllegalArgumentException("Snowflake maxGroups must be > 0");
            }
            this.maxGroups = maxGroups;
            return this;
        }

        /**
         * 设置 dependencyValue 的清理窗口。
         *
         * @param dependencyWindow 窗口大小，必须大于等于 0；当新 dependencyValue 到来时，
         *                         早于 {@code 当前 dependencyValue - dependencyWindow} 的旧组会被清理
         * @return 当前清理配置，便于链式调用
         */
        public GroupCleanup dependencyWindow(long dependencyWindow) {
            if (dependencyWindow < 0) {
                throw new IllegalArgumentException("Snowflake dependencyWindow must be >= 0");
            }
            this.dependencyWindow = dependencyWindow;
            return this;
        }
    }
}
