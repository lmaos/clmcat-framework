package com.clmcat.basics.commons.snowflake;

@FunctionalInterface
public interface SnowflakeValueStrategy {
    long next();

    default long min() {
        throw new UnsupportedOperationException("Snowflake value strategy does not support min()");
    }

    default long max() {
        throw new UnsupportedOperationException("Snowflake value strategy does not support max()");
    }
}
