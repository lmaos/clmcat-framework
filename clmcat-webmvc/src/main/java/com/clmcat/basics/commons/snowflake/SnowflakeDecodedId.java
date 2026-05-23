package com.clmcat.basics.commons.snowflake;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SnowflakeDecodedId {
    private final long id;
    private final int totalBits;
    private final List<Segment> segments;
    private final Map<String, Long> namedValues;

    public SnowflakeDecodedId(long id, int totalBits, List<Segment> segments) {
        this.id = id;
        this.totalBits = totalBits;
        this.segments = List.copyOf(segments);
        Map<String, Long> map = new LinkedHashMap<>();
        for (Segment segment : segments) {
            if (segment.getName() != null) {
                map.put(segment.getName(), segment.getValue());
            }
        }
        this.namedValues = Map.copyOf(map);
    }

    public long getId() {
        return id;
    }

    public int getTotalBits() {
        return totalBits;
    }

    public List<Segment> getSegments() {
        return segments;
    }

    public Long getValue(String name) {
        return namedValues.get(name);
    }

    public long getValue(int index) {
        return segments.get(index).getValue();
    }

    public static final class Segment {
        private final String name;
        private final long value;
        private final int bits;
        private final int highBit;
        private final int lowBit;

        public Segment(String name, long value, int bits, int highBit, int lowBit) {
            this.name = name;
            this.value = value;
            this.bits = bits;
            this.highBit = highBit;
            this.lowBit = lowBit;
        }

        public String getName() {
            return name;
        }

        public long getValue() {
            return value;
        }

        public int getBits() {
            return bits;
        }

        public int getHighBit() {
            return highBit;
        }

        public int getLowBit() {
            return lowBit;
        }
    }
}
