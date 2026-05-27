package com.clmcat.basics.commons.snowflake.strategy;

import com.clmcat.basics.commons.snowflake.SnowflakeBitAwareStrategy;
import com.clmcat.basics.commons.snowflake.SnowflakeCustomBuilder;
import com.clmcat.basics.commons.snowflake.SnowflakeValueStrategy;
import com.clmcat.basics.commons.util.NetworkUtils;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.CRC32;

public final class MachineStrategy {
    private static final int DEFAULT_BITS = 10;
    private static final String DEFAULT_MACHINE_ADDRESS = resolveMachineAddressText();
    private static final long DEFAULT_MACHINE_HASH = hashMachineAddress(DEFAULT_MACHINE_ADDRESS);

    private MachineStrategy() {
    }

    public static long getMachineId() {
        AutoMachineIdStrategy strategy = new AutoMachineIdStrategy();
        strategy.initialize(DEFAULT_BITS, SnowflakeCustomBuilder.maxValueForBits(DEFAULT_BITS));
        return strategy.next();
    }

    public static SnowflakeValueStrategy autoByIp() {
        return new AutoMachineIdStrategy();
    }

    public static SnowflakeValueStrategy manual(long machineId) {
        return new FixedMachineIdStrategy(machineId);
    }

    private static final class FixedMachineIdStrategy implements SnowflakeValueStrategy {
        private final long machineId;

        private FixedMachineIdStrategy(long machineId) {
            this.machineId = machineId;
        }

        @Override
        public long next() {
            return machineId;
        }

        @Override
        public long min() {
            return machineId;
        }

        @Override
        public long max() {
            return machineId;
        }
    }

    private static final class AutoMachineIdStrategy implements SnowflakeValueStrategy, SnowflakeBitAwareStrategy {
        private volatile long machineId = -1L;
        private volatile long maxValue = SnowflakeCustomBuilder.maxValueForBits(DEFAULT_BITS);

        @Override
        public void initialize(int bits, long maxValue) {
            this.maxValue = maxValue;
            this.machineId = -1L;
        }

        @Override
        public long next() {
            long cached = machineId;
            if (cached >= 0) {
                return cached;
            }
            synchronized (this) {
                if (machineId < 0) {
                    machineId = resolveMachineId(maxValue);
                }
                return machineId;
            }
        }

        @Override
        public long min() {
            return next();
        }

        @Override
        public long max() {
            return next();
        }

        private long resolveMachineId(long maxValue) {
            long candidate = DEFAULT_MACHINE_HASH;
            if (maxValue < 0) {
                return candidate;
            }
            return candidate % (maxValue + 1);
        }

    }

    private static String resolveMachineAddressText() {
        List<String> addresses = NetworkUtils.getLocalIpAddresses();
        if (addresses != null && !addresses.isEmpty()) {
            return addresses.get(0);
        }
        try {
            String localHost = InetAddress.getLocalHost().getHostAddress();
            if (localHost != null && !localHost.isBlank()) {
                return localHost;
            }
        } catch (Exception ignored) {
        }
        return "127.0.0.1";
    }

    private static long hashMachineAddress(String machineText) {
        CRC32 crc32 = new CRC32();
        crc32.update(machineText.getBytes(StandardCharsets.UTF_8));
        return crc32.getValue() & Long.MAX_VALUE;
    }
}
