package com.clmcat.basics.commons.snowflake;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SnowflakeCustomBuilder {
    private final List<ComponentDefinition> definitions = new ArrayList<>();

    public static SnowflakeCustomBuilder create() {
        return new SnowflakeCustomBuilder();
    }

    public static SnowflakeCustomBuilder builder() {
        return create();
    }

    public SnowflakeCustomBuilder add(int bitValue) {
        return add(null, bitValue);
    }

    public SnowflakeCustomBuilder add(String name, int bitValue) {
        if (bitValue != 0 && bitValue != 1) {
            throw new IllegalArgumentException("Snowflake fixed bit value must be 0 or 1");
        }
        definitions.add(ComponentDefinition.fixedValue(name, 1, bitValue));
        return this;
    }

    public SnowflakeCustomBuilder addFixed(int bits, long value) {
        return addFixed(null, bits, value);
    }

    public SnowflakeCustomBuilder addFixed(String name, int bits, long value) {
        definitions.add(ComponentDefinition.fixedValue(name, bits, value));
        return this;
    }

    public SnowflakeCustomBuilder add(int bits, SnowflakeValueStrategy strategy) {
        return add(null, bits, strategy);
    }

    public SnowflakeCustomBuilder add(String name, int bits, SnowflakeValueStrategy strategy) {
        definitions.add(ComponentDefinition.fixed(name, bits, strategy));
        return this;
    }

    public SnowflakeCustomBuilder add(int bits, SnowflakeDependentValueStrategy strategy, String dependencyName) {
        return add(null, bits, strategy, dependencyName);
    }

    public SnowflakeCustomBuilder add(String name, int bits, SnowflakeDependentValueStrategy strategy, String dependencyName) {
        definitions.add(ComponentDefinition.dependent(name, bits, strategy, dependencyName));
        return this;
    }

    public int getTotalBits() {
        return resolveDefinitions().totalBits;
    }

    public SnowflakeDescription describe() {
        Resolution resolution = resolveDefinitions();
        return createDescription(resolution.totalBits, resolution.definitions);
    }

    public CustomSnowflake build() {
        Resolution resolution = resolveDefinitions();
        List<CustomSnowflake.Component> components = new ArrayList<>(resolution.definitions.size());
        for (ResolvedDefinition definition : resolution.definitions) {
            initializeStrategy(definition.valueStrategy, definition.dependentValueStrategy, definition.bits, definition.maxValue);
            components.add(new CustomSnowflake.Component(definition.name, definition.bits, definition.maxValue, definition.valueStrategy,
                    definition.dependentValueStrategy, definition.dependencyIndex, definition.dependencyName, definition.fixedValue));
        }
        return new CustomSnowflake(resolution.totalBits, List.copyOf(components), createDescription(resolution.totalBits, resolution.definitions));
    }

    public static long maxValueForBits(int bits) {
        validateBits(bits);
        if (bits == Long.SIZE) {
            return Long.MAX_VALUE;
        }
        return (1L << bits) - 1;
    }

    private static void initializeStrategy(SnowflakeValueStrategy valueStrategy,
            SnowflakeDependentValueStrategy dependentValueStrategy, int bits, long maxValue) {
        if (valueStrategy instanceof SnowflakeBitAwareStrategy awareStrategy) {
            awareStrategy.initialize(bits, maxValue);
        }
        if (dependentValueStrategy instanceof SnowflakeBitAwareStrategy awareStrategy) {
            awareStrategy.initialize(bits, maxValue);
        }
    }

    private Resolution resolveDefinitions() {
        if (definitions.isEmpty()) {
            throw new IllegalStateException("Snowflake components must not be empty");
        }
        int totalBits = 0;
        Map<String, Integer> nameIndexMap = new HashMap<>();
        List<ResolvedDefinition> resolvedDefinitions = new ArrayList<>(definitions.size());
        for (int i = 0; i < definitions.size(); i++) {
            ComponentDefinition definition = definitions.get(i);
            validateBits(definition.bits);
            totalBits += definition.bits;
            if (totalBits > 64) {
                throw new IllegalStateException("Snowflake total bits must be <= 64");
            }
            if (definition.name != null && nameIndexMap.containsKey(definition.name)) {
                throw new IllegalStateException("Snowflake component name duplicated: " + definition.name);
            }
            Integer dependencyIndex = null;
            if (definition.dependencyName != null) {
                dependencyIndex = nameIndexMap.get(definition.dependencyName);
                if (dependencyIndex == null) {
                    throw new IllegalStateException(
                            "Snowflake dependency must reference an earlier named component: " + definition.dependencyName);
                }
            }
            long maxValue = maxValueForBits(definition.bits);
            if (definition.fixedValue != null) {
                validateFixedValue(definition.bits, definition.fixedValue, maxValue);
            }
            resolvedDefinitions.add(new ResolvedDefinition(definition.name, definition.bits, maxValue, definition.valueStrategy,
                    definition.dependentValueStrategy, dependencyIndex, definition.dependencyName, definition.fixedValue));
            if (definition.name != null) {
                nameIndexMap.put(definition.name, i);
            }
        }
        return new Resolution(totalBits, resolvedDefinitions);
    }

    private static SnowflakeDescription createDescription(int totalBits, List<ResolvedDefinition> definitions) {
        List<SnowflakeDescription.Segment> segments = new ArrayList<>(definitions.size());
        int remainingBits = totalBits;
        for (ResolvedDefinition definition : definitions) {
            int highBit = remainingBits - 1;
            int lowBit = remainingBits - definition.bits;
            remainingBits -= definition.bits;
            segments.add(new SnowflakeDescription.Segment(definition.name, definition.bits, definition.maxValue,
                    definition.dependencyName, definition.fixedValue, highBit, lowBit));
        }
        return new SnowflakeDescription(totalBits, segments);
    }

    private static void validateFixedValue(int bits, long value, long maxValue) {
        validateBits(bits);
        if (value < 0) {
            throw new IllegalArgumentException("Snowflake fixed value must be >= 0");
        }
        if (value > maxValue) {
            throw new IllegalArgumentException(
                    "Snowflake fixed value " + value + " exceeds max " + maxValue + " for " + bits + " bits");
        }
    }

    private static void validateBits(int bits) {
        if (bits <= 0 || bits > 64) {
            throw new IllegalArgumentException("Snowflake bits must be between 1 and 64");
        }
    }

    private static final class ComponentDefinition {
        private final String name;
        private final int bits;
        private final SnowflakeValueStrategy valueStrategy;
        private final SnowflakeDependentValueStrategy dependentValueStrategy;
        private final String dependencyName;
        private final Long fixedValue;

        private ComponentDefinition(String name, int bits, SnowflakeValueStrategy valueStrategy,
                SnowflakeDependentValueStrategy dependentValueStrategy, String dependencyName, Long fixedValue) {
            this.name = normalizeName(name);
            this.bits = bits;
            this.valueStrategy = valueStrategy;
            this.dependentValueStrategy = dependentValueStrategy;
            this.dependencyName = normalizeName(dependencyName);
            this.fixedValue = fixedValue;
        }

        private static ComponentDefinition fixed(String name, int bits, SnowflakeValueStrategy strategy) {
            if (strategy == null) {
                throw new IllegalArgumentException("Snowflake value strategy must not be null");
            }
            return new ComponentDefinition(name, bits, strategy, null, null, null);
        }

        private static ComponentDefinition fixedValue(String name, int bits, long fixedValue) {
            validateFixedValue(bits, fixedValue, maxValueForBits(bits));
            return new ComponentDefinition(name, bits, () -> fixedValue, null, null, fixedValue);
        }

        private static ComponentDefinition dependent(String name, int bits, SnowflakeDependentValueStrategy strategy,
                String dependencyName) {
            if (strategy == null) {
                throw new IllegalArgumentException("Snowflake dependent strategy must not be null");
            }
            if (normalizeName(dependencyName) == null) {
                throw new IllegalArgumentException("Snowflake dependency name must not be blank");
            }
            return new ComponentDefinition(name, bits, null, strategy, dependencyName, null);
        }

        private static String normalizeName(String name) {
            if (name == null) {
                return null;
            }
            String trimmed = name.trim();
            return trimmed.isEmpty() ? null : trimmed;
        }
    }

    private static final class ResolvedDefinition {
        private final String name;
        private final int bits;
        private final long maxValue;
        private final SnowflakeValueStrategy valueStrategy;
        private final SnowflakeDependentValueStrategy dependentValueStrategy;
        private final Integer dependencyIndex;
        private final String dependencyName;
        private final Long fixedValue;

        private ResolvedDefinition(String name, int bits, long maxValue, SnowflakeValueStrategy valueStrategy,
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
    }

    private static final class Resolution {
        private final int totalBits;
        private final List<ResolvedDefinition> definitions;

        private Resolution(int totalBits, List<ResolvedDefinition> definitions) {
            this.totalBits = totalBits;
            this.definitions = definitions;
        }
    }
}
