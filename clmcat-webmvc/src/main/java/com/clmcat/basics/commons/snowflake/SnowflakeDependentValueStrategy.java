package com.clmcat.basics.commons.snowflake;

@FunctionalInterface
public interface SnowflakeDependentValueStrategy {
    long next(long dependencyValue);
}
