package com.clmcat.basics.commons.snowflake;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SnowflakeDescription {
    private final int totalBits;
    private final List<Segment> segments;
    private final Map<String, Segment> namedSegments;

    public SnowflakeDescription(int totalBits, List<Segment> segments) {
        this.totalBits = totalBits;
        this.segments = List.copyOf(segments);
        Map<String, Segment> map = new LinkedHashMap<>();
        for (Segment segment : segments) {
            if (segment.getName() != null) {
                map.put(segment.getName(), segment);
            }
        }
        this.namedSegments = Map.copyOf(map);
    }

    public int getTotalBits() {
        return totalBits;
    }

    public List<Segment> getSegments() {
        return segments;
    }

    public Segment getSegment(String name) {
        return namedSegments.get(name);
    }

    @Override
    public String toString() {
        List<String> items = new ArrayList<>(segments.size());
        for (Segment segment : segments) {
            items.add(segment.toString());
        }
        return "SnowflakeDescription{totalBits=" + totalBits + ", segments=" + items + "}";
    }

    public static final class Segment {
        private final String name;
        private final int bits;
        private final long maxValue;
        private final String dependencyName;
        private final Long fixedValue;
        private final int highBit;
        private final int lowBit;

        public Segment(String name, int bits, long maxValue, String dependencyName, Long fixedValue, int highBit, int lowBit) {
            this.name = name;
            this.bits = bits;
            this.maxValue = maxValue;
            this.dependencyName = dependencyName;
            this.fixedValue = fixedValue;
            this.highBit = highBit;
            this.lowBit = lowBit;
        }

        public String getName() {
            return name;
        }

        public int getBits() {
            return bits;
        }

        public long getMaxValue() {
            return maxValue;
        }

        public String getDependencyName() {
            return dependencyName;
        }

        public Long getFixedValue() {
            return fixedValue;
        }

        public int getHighBit() {
            return highBit;
        }

        public int getLowBit() {
            return lowBit;
        }

        @Override
        public String toString() {
            String label = name == null ? "<anonymous>" : name;
            StringBuilder builder = new StringBuilder();
            builder.append(label)
                    .append("[bits=").append(bits)
                    .append(", range=").append(highBit).append(':').append(lowBit)
                    .append(", max=").append(maxValue);
            if (dependencyName != null) {
                builder.append(", dependsOn=").append(dependencyName);
            }
            if (fixedValue != null) {
                builder.append(", fixed=").append(fixedValue);
            }
            builder.append(']');
            return builder.toString();
        }
    }
}
