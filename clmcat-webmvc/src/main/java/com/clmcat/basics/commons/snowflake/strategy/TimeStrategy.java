package com.clmcat.basics.commons.snowflake.strategy;

import com.clmcat.basics.commons.snowflake.SnowflakeValueStrategy;

import java.util.Objects;
import java.util.function.LongSupplier;

public final class TimeStrategy {
    private TimeStrategy() {
    }

    /**
     * 创建毫秒级单调时间策略。
     *
     * @param baseTimeMillis 基准时间戳，生成结果为 {@code 当前时间 - 基准时间} 的毫秒差值
     * @return 毫秒级单调时间策略；当系统时钟回拨时，不会返回更小的时间值
     */
    public static SnowflakeValueStrategy millisecond(long baseTimeMillis) {
        return monotonic(baseTimeMillis, 1L);
    }

    /**
     * 创建毫秒级单调时间策略。
     *
     * @param baseTimeMillis 基准时间戳，生成结果为 {@code 当前时间 - 基准时间} 的毫秒差值
     * @param currentTimeSupplier 当前时间提供器，适合测试或自定义时间源
     * @return 毫秒级单调时间策略；当时间源回拨时，内部会保持单调不减
     */
    public static SnowflakeValueStrategy millisecond(long baseTimeMillis, LongSupplier currentTimeSupplier) {
        return monotonic(baseTimeMillis, 1L, currentTimeSupplier);
    }

    /**
     * 创建秒级单调时间策略。
     *
     * @param baseTimeMillis 基准时间戳，生成结果为 {@code (当前时间 - 基准时间) / 1000} 的秒差值
     * @return 秒级单调时间策略；当系统时钟回拨时，不会返回更小的时间值
     */
    public static SnowflakeValueStrategy second(long baseTimeMillis) {
        return monotonic(baseTimeMillis, 1000L);
    }

    /**
     * 创建秒级单调时间策略。
     *
     * @param baseTimeMillis 基准时间戳，生成结果为 {@code (当前时间 - 基准时间) / 1000} 的秒差值
     * @param currentTimeSupplier 当前时间提供器，适合测试或自定义时间源
     * @return 秒级单调时间策略；当时间源回拨时，内部会保持单调不减
     */
    public static SnowflakeValueStrategy second(long baseTimeMillis, LongSupplier currentTimeSupplier) {
        return monotonic(baseTimeMillis, 1000L, currentTimeSupplier);
    }

    /**
     * 创建通用单调时间策略。
     *
     * @param baseTimeMillis 基准时间戳，生成结果为 {@code (当前时间 - 基准时间) / tickMillis}
     * @param tickMillis 时间步长，单位毫秒；例如 1 表示毫秒级，1000 表示秒级，5000 表示 5 秒窗口
     * @return 通用单调时间策略；时间回拨时默认保持上一次时间值
     */
    public static SnowflakeValueStrategy monotonic(long baseTimeMillis, long tickMillis) {
        return monotonic(baseTimeMillis, tickMillis, System::currentTimeMillis);
    }

    /**
     * 创建通用单调时间策略。
     *
     * @param baseTimeMillis 基准时间戳，生成结果为 {@code (当前时间 - 基准时间) / tickMillis}
     * @param tickMillis 时间步长，单位毫秒；适合扩展为秒、分钟、5秒窗口等自定义时间分片
     * @param currentTimeSupplier 当前时间提供器，适合测试或自定义时间源
     * @return 通用单调时间策略；时间回拨时默认保持上一次时间值
     */
    public static SnowflakeValueStrategy monotonic(long baseTimeMillis, long tickMillis, LongSupplier currentTimeSupplier) {
        return monotonic(baseTimeMillis, tickMillis, currentTimeSupplier, RollbackPolicy.HOLD_LAST);
    }

    /**
     * 创建通用单调时间策略。
     *
     * @param baseTimeMillis 基准时间戳，生成结果为 {@code (当前时间 - 基准时间) / tickMillis}
     * @param tickMillis 时间步长，单位毫秒，必须大于 0
     * @param currentTimeSupplier 当前时间提供器，适合测试或自定义时间源
     * @param rollbackPolicy 回拨策略；{@link RollbackPolicy#HOLD_LAST} 表示回拨时保持上次值，
     *                       {@link RollbackPolicy#THROW} 表示直接抛异常
     * @return 通用单调时间策略
     */
    public static SnowflakeValueStrategy monotonic(long baseTimeMillis, long tickMillis, LongSupplier currentTimeSupplier,
            RollbackPolicy rollbackPolicy) {
        Objects.requireNonNull(currentTimeSupplier, "currentTimeSupplier");
        Objects.requireNonNull(rollbackPolicy, "rollbackPolicy");
        if (tickMillis <= 0) {
            throw new IllegalArgumentException("Snowflake tickMillis must be > 0");
        }
        return new MonotonicTimeValueStrategy(baseTimeMillis, currentTimeSupplier, tickMillis, rollbackPolicy);
    }

    /**
     * 根据基准时间和时间差值恢复绝对时间。
     *
     * @param baseTimeMillis 基准时间戳
     * @param elapsedTime 雪花算法中存储的时间差值
     * @param tickMillis 时间步长，单位毫秒
     * @return 恢复后的绝对时间戳
     */
    public static long restore(long baseTimeMillis, long elapsedTime, long tickMillis) {
        if (tickMillis <= 0) {
            throw new IllegalArgumentException("Snowflake tickMillis must be > 0");
        }
        if (elapsedTime < 0) {
            throw new IllegalArgumentException("Snowflake elapsedTime must be >= 0");
        }
        return baseTimeMillis + toElapsedMillis(elapsedTime, tickMillis);
    }

    /**
     * 将雪花算法中的时间差值恢复为毫秒差值。
     *
     * @param elapsedTime 雪花算法中保存的时间差值
     * @param tickMillis 时间步长，单位毫秒
     * @return 对应的毫秒差值；业务侧可自行再加上 baseTime 得到真实时间
     */
    public static long toElapsedMillis(long elapsedTime, long tickMillis) {
        if (tickMillis <= 0) {
            throw new IllegalArgumentException("Snowflake tickMillis must be > 0");
        }
        if (elapsedTime < 0) {
            throw new IllegalArgumentException("Snowflake elapsedTime must be >= 0");
        }
        return elapsedTime * tickMillis;
    }

    /**
     * 根据秒级时间差值恢复绝对时间。
     *
     * @param baseTimeMillis 基准时间戳
     * @param elapsedSecond 雪花算法中存储的秒差值
     * @return 恢复后的绝对时间戳
     */
    public static long restoreSecond(long baseTimeMillis, long elapsedSecond) {
        return restore(baseTimeMillis, elapsedSecond, 1000L);
    }

    /**
     * 根据毫秒级时间差值恢复绝对时间。
     *
     * @param baseTimeMillis 基准时间戳
     * @param elapsedMillisecond 雪花算法中存储的毫秒差值
     * @return 恢复后的绝对时间戳
     */
    public static long restoreMillisecond(long baseTimeMillis, long elapsedMillisecond) {
        return restore(baseTimeMillis, elapsedMillisecond, 1L);
    }

    private static long toElapsedTime(long baseTimeMillis, long currentTimeMillis, long divisor, String unit) {
        long delta = currentTimeMillis - baseTimeMillis;
        if (delta < 0) {
            throw new IllegalStateException("Snowflake " + unit + " time cannot be earlier than base time");
        }
        return delta / divisor;
    }

    public enum RollbackPolicy {
        HOLD_LAST,
        THROW
    }

    private static final class MonotonicTimeValueStrategy implements SnowflakeValueStrategy {
        private final long baseTimeMillis;
        private final LongSupplier currentTimeSupplier;
        private final long tickMillis;
        private final RollbackPolicy rollbackPolicy;
        private long lastElapsedTime = Long.MIN_VALUE;

        private MonotonicTimeValueStrategy(long baseTimeMillis, LongSupplier currentTimeSupplier, long tickMillis,
                RollbackPolicy rollbackPolicy) {
            this.baseTimeMillis = baseTimeMillis;
            this.currentTimeSupplier = currentTimeSupplier;
            this.tickMillis = tickMillis;
            this.rollbackPolicy = rollbackPolicy;
        }

        @Override
        public synchronized long next() {
            long elapsedTime = currentElapsedTime();
            if (lastElapsedTime != Long.MIN_VALUE && elapsedTime < lastElapsedTime) {
                if (rollbackPolicy == RollbackPolicy.THROW) {
                    throw new IllegalStateException(
                            "Snowflake time rolled back from " + lastElapsedTime + " to " + elapsedTime);
                }
                return lastElapsedTime;
            }
            lastElapsedTime = elapsedTime;
            return elapsedTime;
        }

        @Override
        public synchronized long min() {
            return next();
        }

        @Override
        public synchronized long max() {
            return next();
        }

        private long currentElapsedTime() {
            return toElapsedTime(baseTimeMillis, currentTimeSupplier.getAsLong(), tickMillis,
                    tickMillis == 1L ? "millisecond" : tickMillis + "ms");
        }
    }
}
