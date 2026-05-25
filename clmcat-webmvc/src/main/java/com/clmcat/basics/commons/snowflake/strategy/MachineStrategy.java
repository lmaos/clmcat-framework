package com.clmcat.basics.commons.snowflake.strategy;

import com.clmcat.basics.commons.snowflake.SnowflakeBitAwareStrategy;
import com.clmcat.basics.commons.snowflake.SnowflakeCustomBuilder;
import com.clmcat.basics.commons.snowflake.SnowflakeValueStrategy;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.zip.CRC32;

public final class MachineStrategy {
    private static final int DEFAULT_BITS = 10;

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
            if (maxValue < 0) {
                return hashMachineAddress();
            }
            long candidate = hashMachineAddress();
            return candidate % (maxValue + 1);
        }

        private long hashMachineAddress() {
            String machineText = firstNonLoopbackAddress();
            if (machineText == null) {
                machineText = fallbackLocalHostAddress();
            }
            if (machineText == null) {
                machineText = "127.0.0.1";
            }
            CRC32 crc32 = new CRC32();
            crc32.update(machineText.getBytes(StandardCharsets.UTF_8));
            return crc32.getValue() & Long.MAX_VALUE;
        }

        private String firstNonLoopbackAddress() {
            try {
                Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                while (interfaces != null && interfaces.hasMoreElements()) {
                    NetworkInterface networkInterface = interfaces.nextElement();
                    if (!networkInterface.isUp() || networkInterface.isLoopback() || networkInterface.isVirtual()) {
                        continue;
                    }
                    Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        InetAddress address = addresses.nextElement();
                        if (!address.isLoopbackAddress() && !address.isLinkLocalAddress()) {
                            return address.getHostAddress();
                        }
                    }
                }
            } catch (SocketException e) {
                return null;
            }
            return null;
        }

        private String fallbackLocalHostAddress() {
            try {
                return InetAddress.getLocalHost().getHostAddress();
            } catch (Exception e) {
                return null;
            }
        }
    }
}
