package com.clmcat.basics.commons.snowflake.strategy;

import com.clmcat.basics.commons.snowflake.SnowflakeBitAwareStrategy;
import com.clmcat.basics.commons.snowflake.SnowflakeDependentValueStrategy;

public class SequenceStrategy implements SnowflakeDependentValueStrategy, SnowflakeBitAwareStrategy {
    private volatile long maxValue = Long.MAX_VALUE;
    private long lastDependency = Long.MIN_VALUE;
    private long currentValue = -1L;

    public static SequenceStrategy create() {
        return new SequenceStrategy();
    }

    public static SequenceStrategy resetOnDependencyChange() {
        return create();
    }

    @Override
    public synchronized void initialize(int bits, long maxValue) {
        this.maxValue = maxValue < 0 ? Long.MAX_VALUE : maxValue;
        this.lastDependency = Long.MIN_VALUE;
        this.currentValue = -1L;
    }

    @Override
    public synchronized long next(long dependencyValue) {
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
}
