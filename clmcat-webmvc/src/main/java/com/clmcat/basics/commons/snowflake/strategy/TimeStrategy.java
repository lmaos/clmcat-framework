package com.clmcat.basics.commons.snowflake.strategy;

import com.clmcat.basics.commons.snowflake.SnowflakeValueStrategy;

import java.util.Objects;
import java.util.function.LongSupplier;

public final class TimeStrategy {
    private TimeStrategy() {
    }

    public static SnowflakeValueStrategy millisecond(long baseTimeMillis) {
        return millisecond(baseTimeMillis, System::currentTimeMillis);
    }

    public static SnowflakeValueStrategy millisecond(long baseTimeMillis, LongSupplier currentTimeSupplier) {
        Objects.requireNonNull(currentTimeSupplier, "currentTimeSupplier");
        return () -> toElapsedTime(baseTimeMillis, currentTimeSupplier.getAsLong(), 1L, "millisecond");
    }

    public static SnowflakeValueStrategy second(long baseTimeMillis) {
        return second(baseTimeMillis, System::currentTimeMillis);
    }

    public static SnowflakeValueStrategy second(long baseTimeMillis, LongSupplier currentTimeSupplier) {
        Objects.requireNonNull(currentTimeSupplier, "currentTimeSupplier");
        return () -> toElapsedTime(baseTimeMillis, currentTimeSupplier.getAsLong(), 1000L, "second");
    }

    private static long toElapsedTime(long baseTimeMillis, long currentTimeMillis, long divisor, String unit) {
        long delta = currentTimeMillis - baseTimeMillis;
        if (delta < 0) {
            throw new IllegalStateException("Snowflake " + unit + " time cannot be earlier than base time");
        }
        return delta / divisor;
    }
}
