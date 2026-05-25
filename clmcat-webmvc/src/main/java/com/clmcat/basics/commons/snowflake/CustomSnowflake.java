package com.clmcat.basics.commons.snowflake;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomSnowflake {
    private final int totalBits;
    private final List<Component> components;
    private final SnowflakeDescription description;
    private final Map<String, Integer> nameIndexes;

    CustomSnowflake(int totalBits, List<Component> components, SnowflakeDescription description) {
        this.totalBits = totalBits;
        this.components = components;
        this.description = description;
        Map<String, Integer> indexes = new HashMap<>();
        for (int i = 0; i < components.size(); i++) {
            String name = components.get(i).name;
            if (name != null) {
                indexes.put(name, i);
            }
        }
        this.nameIndexes = Map.copyOf(indexes);
    }

    public synchronized long nextId() {
        return composeId(resolveValues(null, ResolveMode.NEXT));
    }

    /**
     * 按组件顺序指定值来计算一个雪花 ID。
     * <p>
     * 传入的数组下标与 builder 中 {@code add(...)} 的顺序一致；某个位置传入 {@code null}
     * 时，会回退到当前组件自己的策略值。该能力适合手动构造某个时间区间的最小 / 最大 ID。
     *
     * @param specifiedValues 按顺序指定的组件值，允许为 {@code null}
     * @return 组合后的雪花 ID
     */
    public synchronized long computeId(Long... specifiedValues) {
        return composeId(resolveValues(specifiedValues, ResolveMode.NEXT));
    }

    /**
     * 按组件名称指定值来计算一个雪花 ID。
     * <p>
     * 未指定的组件会继续使用当前组件的策略值；适合只覆盖 time / sequence 等少量字段。
     *
     * @param specifiedValues 按名称指定的组件值，value 为 {@code null} 时表示仍使用当前策略值
     * @return 组合后的雪花 ID
     */
    public synchronized long computeId(Map<String, Long> specifiedValues) {
        if (specifiedValues == null || specifiedValues.isEmpty()) {
            return nextId();
        }
        Long[] values = new Long[components.size()];
        for (Map.Entry<String, Long> entry : specifiedValues.entrySet()) {
            Integer index = nameIndexes.get(entry.getKey());
            if (index == null) {
                throw new IllegalArgumentException("Snowflake component not found: " + entry.getKey());
            }
            values[index] = entry.getValue();
        }
        return composeId(resolveValues(values, ResolveMode.NEXT));
    }

    /**
     * 计算当前布局下的最小边界 ID。
     * <p>
     * 传入 {@code null} 的位置不会调用策略的 {@code next()}，而是调用该策略定义的最小值。
     *
     * @param specifiedValues 按组件顺序指定的组件值，允许为 {@code null}
     * @return 最小边界 ID
     */
    public synchronized long minId(Long... specifiedValues) {
        return composeId(resolveValues(specifiedValues, ResolveMode.MIN));
    }

    /**
     * 计算当前布局下的最大边界 ID。
     * <p>
     * 传入 {@code null} 的位置不会调用策略的 {@code next()}，而是调用该策略定义的最大值。
     *
     * @param specifiedValues 按组件顺序指定的组件值，允许为 {@code null}
     * @return 最大边界 ID
     */
    public synchronized long maxId(Long... specifiedValues) {
        return composeId(resolveValues(specifiedValues, ResolveMode.MAX));
    }

    public synchronized long minId(Map<String, Long> specifiedValues) {
        return composeId(resolveNamedValues(specifiedValues, ResolveMode.MIN));
    }

    public synchronized long maxId(Map<String, Long> specifiedValues) {
        return composeId(resolveNamedValues(specifiedValues, ResolveMode.MAX));
    }

    public int getTotalBits() {
        return totalBits;
    }

    public SnowflakeDescription describe() {
        return description;
    }

    public long get(String name, long id) {
        Long value = parse(id).getValue(name);
        if (value == null) {
            throw new IllegalArgumentException("Snowflake component not found: " + name);
        }
        return value;
    }

    public long get(int index, long id) {
        return parse(id).getValue(index);
    }

    public long[] split(long id) {
        SnowflakeDecodedId decodedId = parse(id);
        long[] values = new long[decodedId.getSegments().size()];
        for (int i = 0; i < values.length; i++) {
            values[i] = decodedId.getValue(i);
        }
        return values;
    }

    private long[] resolveNamedValues(Map<String, Long> specifiedValues, ResolveMode resolveMode) {
        if (specifiedValues == null || specifiedValues.isEmpty()) {
            return resolveValues(null, resolveMode);
        }
        Long[] values = new Long[components.size()];
        for (Map.Entry<String, Long> entry : specifiedValues.entrySet()) {
            Integer index = nameIndexes.get(entry.getKey());
            if (index == null) {
                throw new IllegalArgumentException("Snowflake component not found: " + entry.getKey());
            }
            values[index] = entry.getValue();
        }
        return resolveValues(values, resolveMode);
    }

    private long[] resolveValues(Long[] specifiedValues, ResolveMode resolveMode) {
        if (specifiedValues != null && specifiedValues.length > components.size()) {
            throw new IllegalArgumentException("Snowflake specifiedValues length exceeds component size");
        }
        long[] values = new long[components.size()];
        for (int i = 0; i < components.size(); i++) {
            Long specified = specifiedValues != null && i < specifiedValues.length ? specifiedValues[i] : null;
            Component component = components.get(i);
            long value;
            if (specified != null) {
                value = specified;
            } else if (resolveMode == ResolveMode.MIN) {
                value = component.min(values);
            } else if (resolveMode == ResolveMode.MAX) {
                value = component.max(values);
            } else {
                value = component.next(values);
            }
            component.validate(value);
            values[i] = value;
        }
        return values;
    }

    private long composeId(long[] values) {
        if (components.size() == 1 && components.get(0).bits == 64) {
            return values[0];
        }

        long id = 0L;
        for (int i = 0; i < components.size(); i++) {
            id = (id << components.get(i).bits) | values[i];
        }
        return id;
    }

    public SnowflakeDecodedId parse(long id) {
        int remainingBits = totalBits;
        List<SnowflakeDecodedId.Segment> segments = new ArrayList<>(components.size());
        for (Component component : components) {
            remainingBits -= component.bits;
            long value;
            if (component.bits == Long.SIZE) {
                value = id;
            } else {
                value = (id >>> remainingBits) & bitMask(component.bits);
            }
            segments.add(new SnowflakeDecodedId.Segment(component.name, value, component.bits, remainingBits + component.bits - 1,
                    remainingBits));
        }
        return new SnowflakeDecodedId(id, totalBits, segments);
    }

    private long bitMask(int bits) {
        if (bits == Long.SIZE) {
            return -1L;
        }
        return (1L << bits) - 1;
    }

    static final class Component {
        private final String name;
        private final int bits;
        private final long maxValue;
        private final SnowflakeValueStrategy valueStrategy;
        private final SnowflakeDependentValueStrategy dependentValueStrategy;
        private final Integer dependencyIndex;
        private final String dependencyName;
        private final Long fixedValue;

        Component(String name, int bits, long maxValue, SnowflakeValueStrategy valueStrategy,
                SnowflakeDependentValueStrategy dependentValueStrategy, Integer dependencyIndex, String dependencyName,
                Long fixedValue) {
            this.name = name;
            this.bits = bits;
            this.maxValue = maxValue;
            this.valueStrategy = valueStrategy;
            this.dependentValueStrategy = dependentValueStrategy;
            this.dependencyIndex = dependencyIndex;
            this.dependencyName = dependencyName;
            this.fixedValue = fixedValue;
        }

        private long next(long[] values) {
            if (dependentValueStrategy != null) {
                return dependentValueStrategy.next(values[dependencyIndex]);
            }
            return valueStrategy.next();
        }

        private long min(long[] values) {
            if (fixedValue != null) {
                return fixedValue;
            }
            if (dependentValueStrategy != null) {
                return dependentValueStrategy.min(values[dependencyIndex]);
            }
            return valueStrategy.min();
        }

        private long max(long[] values) {
            if (fixedValue != null) {
                return fixedValue;
            }
            if (dependentValueStrategy != null) {
                return dependentValueStrategy.max(values[dependencyIndex]);
            }
            return valueStrategy.max();
        }

        private void validate(long value) {
            if (value < 0) {
                throw new IllegalArgumentException("Snowflake component " + label() + " value must be >= 0");
            }
            if (value > maxValue) {
                if (dependentValueStrategy != null) {
                    throw new IllegalStateException(
                            "Snowflake dependent component " + label() + " overflow, value " + value
                                    + " exceeds max " + maxValue);
                }
                throw new IllegalArgumentException(
                        "Snowflake component " + label() + " value " + value + " exceeds max " + maxValue
                                + " for " + bits + " bits");
            }
        }

        private String label() {
            return name == null || name.isBlank() ? "<anonymous>" : name;
        }
    }

    private enum ResolveMode {
        NEXT,
        MIN,
        MAX
    }
}
