package com.clmcat.basics.commons.snowflake;

import java.util.ArrayList;
import java.util.List;

public class CustomSnowflake {
    private final int totalBits;
    private final List<Component> components;
    private final SnowflakeDescription description;

    CustomSnowflake(int totalBits, List<Component> components, SnowflakeDescription description) {
        this.totalBits = totalBits;
        this.components = components;
        this.description = description;
    }

    public synchronized long nextId() {
        long[] values = new long[components.size()];
        for (int i = 0; i < components.size(); i++) {
            Component component = components.get(i);
            long value = component.next(values);
            component.validate(value);
            values[i] = value;
        }

        if (components.size() == 1 && components.get(0).bits == 64) {
            return values[0];
        }

        long id = 0L;
        for (int i = 0; i < components.size(); i++) {
            id = (id << components.get(i).bits) | values[i];
        }
        return id;
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
}
