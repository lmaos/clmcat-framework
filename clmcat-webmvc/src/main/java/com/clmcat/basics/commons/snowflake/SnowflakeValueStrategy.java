package com.clmcat.basics.commons.snowflake;

@FunctionalInterface
public interface SnowflakeValueStrategy {
    long next();
}
