package com.clmcat.basics.commons.snowflake;

public interface SnowflakeBitAwareStrategy {
    void initialize(int bits, long maxValue);
}
