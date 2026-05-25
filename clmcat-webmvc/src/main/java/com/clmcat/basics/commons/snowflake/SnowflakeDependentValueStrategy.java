package com.clmcat.basics.commons.snowflake;

@FunctionalInterface
public interface SnowflakeDependentValueStrategy {
    long next(long dependencyValue);

    default long min(long dependencyValue) {
        throw new UnsupportedOperationException("Snowflake dependent strategy does not support min()");
    }

    default long max(long dependencyValue) {
        throw new UnsupportedOperationException("Snowflake dependent strategy does not support max()");
    }
}
