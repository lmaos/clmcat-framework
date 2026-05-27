package com.clmcat.demo.snowflake;

import com.clmcat.basics.commons.snowflake.CustomSnowflake;
import com.clmcat.basics.commons.snowflake.SnowflakeDecodedId;
import com.clmcat.basics.commons.snowflake.SnowflakeDescription;
import com.clmcat.basics.commons.snowflake.SnowflakeCustomBuilder;
import com.clmcat.basics.commons.snowflake.strategy.MachineStrategy;
import com.clmcat.basics.commons.snowflake.strategy.SequenceStrategy;
import com.clmcat.basics.commons.snowflake.strategy.TimeStrategy;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomSnowflakeUnitTest {

    @Test
    void shouldAssembleCustomSnowflakeLayout() {
        CustomSnowflake snowflake = SnowflakeCustomBuilder.builder()
                .add(0)
                .add("time", 32, TimeStrategy.second(0L, () -> 12_000L))
                .add(9, () -> 0L)
                .add("machine", 10, MachineStrategy.manual(7L))
                .add("sequence", 12, SequenceStrategy.grouped(), "time")
                .build();

        long first = snowflake.nextId();
        long second = snowflake.nextId();

        long expectedFirst = (((((0L << 32) | 12L) << 9) | 0L) << 10 | 7L) << 12;
        long expectedSecond = expectedFirst | 1L;
        assertEquals(expectedFirst, first);
        assertEquals(expectedSecond, second);
    }

    @Test
    void shouldResetSequenceWhenDependencyValueChanges() {
        AtomicLong clock = new AtomicLong(10_000L);
        CustomSnowflake snowflake = SnowflakeCustomBuilder.builder()
                .add("time", 32, TimeStrategy.second(0L, clock::get))
                .add(12, SequenceStrategy.grouped(), "time")
                .build();

        long first = snowflake.nextId();
        long second = snowflake.nextId();
        clock.set(11_000L);
        long third = snowflake.nextId();

        assertEquals((10L << 12), first);
        assertEquals((10L << 12) | 1L, second);
        assertEquals((11L << 12), third);
    }

    @Test
    void shouldThrowWhenSequenceOverflowsWithinSameSecond() {
        AtomicLong clock = new AtomicLong(1_000L);
        CustomSnowflake snowflake = SnowflakeCustomBuilder.builder()
                .add("time", 32, TimeStrategy.second(0L, clock::get))
                .add(2, SequenceStrategy.grouped(), "time")
                .build();

        snowflake.nextId();
        snowflake.nextId();
        snowflake.nextId();
        snowflake.nextId();

        assertThrows(IllegalStateException.class, snowflake::nextId);
    }

    @Test
    void shouldSupportMillisecondTimeStrategy() {
        CustomSnowflake snowflake = SnowflakeCustomBuilder.builder()
                .add(20, TimeStrategy.millisecond(5_000L, () -> 5_123L))
                .build();

        assertEquals(123L, snowflake.nextId());
    }

    @Test
    void shouldSupportCustomTimeTickStrategy() {
        CustomSnowflake snowflake = SnowflakeCustomBuilder.builder()
                .add("time", 20, TimeStrategy.monotonic(5_000L, 5_000L, () -> 15_123L))
                .build();

        assertEquals(2L, snowflake.nextId());
        assertEquals(15_000L, TimeStrategy.restore(5_000L, 2L, 5_000L));
    }

    @Test
    void shouldKeepTimeMonotonicWhenClockMovesBackward() {
        AtomicLong clock = new AtomicLong(10_000L);
        CustomSnowflake snowflake = SnowflakeCustomBuilder.builder()
                .add("time", 32, TimeStrategy.second(0L, clock::get))
                .add(12, SequenceStrategy.grouped(), "time")
                .build();

        long first = snowflake.nextId();
        clock.set(9_000L);
        long second = snowflake.nextId();
        clock.set(11_000L);
        long third = snowflake.nextId();

        assertEquals(10L, snowflake.get("time", first));
        assertEquals(10L, snowflake.get("time", second));
        assertEquals(11L, snowflake.get("time", third));
        assertTrue(second > first);
    }

    @Test
    void shouldThrowWhenTimeRollbackPolicyIsThrow() {
        AtomicLong clock = new AtomicLong(10_000L);
        CustomSnowflake snowflake = SnowflakeCustomBuilder.builder()
                .add("time", 32, TimeStrategy.monotonic(0L, 1000L, clock::get, TimeStrategy.RollbackPolicy.THROW))
                .build();

        assertEquals(10L, snowflake.nextId());
        clock.set(9_000L);
        assertThrows(IllegalStateException.class, snowflake::nextId);
    }

    @Test
    void shouldKeepAutoMachineIdInsideConfiguredBits() {
        CustomSnowflake snowflake = SnowflakeCustomBuilder.builder()
                .add(10, MachineStrategy.autoByIp())
                .build();

        long machineId = snowflake.nextId();

        assertTrue(machineId >= 0);
        assertTrue(machineId <= SnowflakeCustomBuilder.maxValueForBits(10));
    }

    @Test
    void shouldKeepAutoMachineIdStableAcrossStrategyInstances() {
        long machineId1 = MachineStrategy.getMachineId();
        long machineId2 = MachineStrategy.getMachineId();

        assertEquals(machineId1, machineId2);
    }

    @Test
    void shouldRejectSnowflakeThatExceedsSixtyFourBits() {
        assertThrows(IllegalStateException.class, () -> SnowflakeCustomBuilder.builder()
                .add(32, () -> 0L)
                .add(33, () -> 0L)
                .build());
    }

    @Test
    void shouldRejectValueThatDoesNotFitBitWidth() {
        CustomSnowflake snowflake = SnowflakeCustomBuilder.builder()
                .add(3, MachineStrategy.manual(9L))
                .build();

        assertThrows(IllegalArgumentException.class, snowflake::nextId);
    }

    @Test
    void shouldSupportFixedValueSegments() {
        CustomSnowflake snowflake = SnowflakeCustomBuilder.builder()
                .addFixed("reserved", 9, 3L)
                .build();

        assertEquals(3L, snowflake.nextId());
    }

    @Test
    void shouldAllowValueAtExactBitWidthBoundary() {
        CustomSnowflake snowflake = SnowflakeCustomBuilder.builder()
                .add(3, MachineStrategy.manual(7L))
                .build();

        assertEquals(7L, snowflake.nextId());
    }

    @Test
    void shouldRejectNegativeComponentValue() {
        CustomSnowflake snowflake = SnowflakeCustomBuilder.builder()
                .add(4, () -> -1L)
                .build();

        assertThrows(IllegalArgumentException.class, snowflake::nextId);
    }

    @Test
    void shouldSortIdsByTimeInAscendingAndDescendingOrder() {
        AtomicLong clock = new AtomicLong();
        CustomSnowflake snowflake = SnowflakeCustomBuilder.builder()
                .add(0)
                .add("time", 32, TimeStrategy.second(0L, clock::get))
                .add("machine", 10, MachineStrategy.manual(7L))
                .add("sequence", 12, SequenceStrategy.grouped(), "time")
                .build();

        clock.set(2_000L);
        long idAt2 = snowflake.nextId();
        clock.set(3_000L);
        long idAt3 = snowflake.nextId();
        clock.set(5_000L);
        long idAt5 = snowflake.nextId();
        clock.set(9_000L);
        long idAt9 = snowflake.nextId();

        List<Long> ids = new ArrayList<>(List.of(idAt5, idAt2, idAt9, idAt3));

        List<Long> ascending = new ArrayList<>(ids);
        ascending.sort(Long::compare);
        assertEquals(List.of(idAt2, idAt3, idAt5, idAt9), ascending);

        List<Long> descending = new ArrayList<>(ids);
        descending.sort(Comparator.reverseOrder());
        assertEquals(List.of(idAt9, idAt5, idAt3, idAt2), descending);
    }

    @Test
    void shouldPassReferencedComponentValueToDependentStrategy() {
        AtomicLong dependencyValue = new AtomicLong(-1L);
        CustomSnowflake snowflake = SnowflakeCustomBuilder.builder()
                .add("time", 8, () -> 21L)
                .add(4, value -> {
                    dependencyValue.set(value);
                    return value % 16;
                }, "time")
                .build();

        long id = snowflake.nextId();

        assertEquals(21L, dependencyValue.get());
        assertEquals((21L << 4) | 5L, id);
    }

    @Test
    void shouldKeepIndependentSequencePerDependencyGroup() {
        SequenceStrategy sequenceStrategy = SequenceStrategy.grouped();
        sequenceStrategy.initialize(4, SnowflakeCustomBuilder.maxValueForBits(4));

        assertEquals(0L, sequenceStrategy.next(10L));
        assertEquals(0L, sequenceStrategy.next(11L));
        assertEquals(1L, sequenceStrategy.next(10L));
        assertEquals(1L, sequenceStrategy.next(11L));
    }

    @Test
    void shouldCleanupSequenceGroupsByMaxGroupLimit() {
        SequenceStrategy sequenceStrategy = SequenceStrategy.grouped(SequenceStrategy.cleanup().maxGroups(2));
        sequenceStrategy.initialize(4, SnowflakeCustomBuilder.maxValueForBits(4));

        assertEquals(0L, sequenceStrategy.next(1L));
        assertEquals(0L, sequenceStrategy.next(2L));
        assertEquals(0L, sequenceStrategy.next(3L));
        assertEquals(0L, sequenceStrategy.next(1L));
    }

    @Test
    void shouldCleanupSequenceGroupsByDependencyWindow() {
        SequenceStrategy sequenceStrategy = SequenceStrategy.groupedWindow(1L);
        sequenceStrategy.initialize(4, SnowflakeCustomBuilder.maxValueForBits(4));

        assertEquals(0L, sequenceStrategy.next(10L));
        assertEquals(0L, sequenceStrategy.next(11L));
        assertEquals(0L, sequenceStrategy.next(12L));
        assertEquals(0L, sequenceStrategy.next(10L));
    }

    @Test
    void shouldResetStandardSequenceWhenDependencySwitchesBack() {
        SequenceStrategy sequenceStrategy = SequenceStrategy.standard();
        sequenceStrategy.initialize(4, SnowflakeCustomBuilder.maxValueForBits(4));

        assertEquals(0L, sequenceStrategy.next(10L));
        assertEquals(0L, sequenceStrategy.next(11L));
        assertEquals(0L, sequenceStrategy.next(10L));
    }

    @Test
    void shouldRejectDependencyThatDoesNotReferenceEarlierNamedComponent() {
        assertThrows(IllegalStateException.class, () -> SnowflakeCustomBuilder.builder()
                .add("sequence", 8, value -> value, "time")
                .add("time", 8, () -> 1L)
                .build());
    }

    @Test
    void shouldSupportFixedBitPlaceholders() {
        CustomSnowflake snowflake = SnowflakeCustomBuilder.builder()
                .add(1)
                .add(0)
                .add(3, () -> 5L)
                .build();

        assertEquals(0b10101L, snowflake.nextId());
    }

    @Test
    void shouldPackSixtyFourBitsWithoutLosingBitPattern() {
        CustomSnowflake snowflake = SnowflakeCustomBuilder.builder()
                .add(1)
                .add(63, () -> 0L)
                .build();

        assertEquals(Long.MIN_VALUE, snowflake.nextId());
    }

    @Test
    void shouldKeepTimeOrderWhenHighestBitIsFixedToOne() {
        AtomicLong clock = new AtomicLong();
        CustomSnowflake snowflake = SnowflakeCustomBuilder.builder()
                .add(1)
                .add("time", 32, TimeStrategy.second(0L, clock::get))
                .add(31, () -> 0L)
                .build();

        clock.set(3_000L);
        long idAt3 = snowflake.nextId();
        clock.set(1_000L);
        long idAt1 = snowflake.nextId();
        clock.set(4_000L);
        long idAt4 = snowflake.nextId();

        List<Long> ascending = new ArrayList<>(List.of(idAt3, idAt1, idAt4));
        ascending.sort(Long::compare);
        assertEquals(List.of(idAt1, idAt3, idAt4), ascending);

        List<Long> descending = new ArrayList<>(ascending);
        descending.sort(Comparator.reverseOrder());
        assertEquals(List.of(idAt4, idAt3, idAt1), descending);
    }

    @Test
    void shouldRejectDuplicateComponentNames() {
        assertThrows(IllegalStateException.class, () -> SnowflakeCustomBuilder.builder()
                .add("time", 8, () -> 1L)
                .add("time", 8, () -> 2L)
                .build());
    }

    @Test
    void shouldExposeBuilderDescriptionAndTotalBits() {
        SnowflakeCustomBuilder builder = SnowflakeCustomBuilder.builder()
                .add(0)
                .add("time", 32, TimeStrategy.second(0L, () -> 1_000L))
                .addFixed("reserved", 9, 0L)
                .add("sequence", 12, SequenceStrategy.grouped(), "time");

        SnowflakeDescription description = builder.describe();

        assertEquals(54, builder.getTotalBits());
        assertEquals(54, description.getTotalBits());
        assertEquals(4, description.getSegments().size());
        assertEquals(53, description.getSegments().get(0).getHighBit());
        assertEquals(53, description.getSegments().get(0).getLowBit());
        assertEquals("time", description.getSegments().get(1).getName());
        assertEquals("time", description.getSegments().get(3).getDependencyName());
        assertEquals(0L, description.getSegment("reserved").getFixedValue());
    }

    @Test
    void shouldParseSnowflakeIdBackToSegmentValues() {
        CustomSnowflake snowflake = SnowflakeCustomBuilder.builder()
                .add(0)
                .add("time", 32, TimeStrategy.second(0L, () -> 12_000L))
                .addFixed("reserved", 9, 3L)
                .add("machine", 10, MachineStrategy.manual(7L))
                .add("sequence", 12, SequenceStrategy.resetOnDependencyChange(), "time")
                .build();

        snowflake.nextId();
        long id = snowflake.nextId();

        SnowflakeDecodedId decoded = snowflake.parse(id);

        assertEquals(64, snowflake.getTotalBits());
        assertEquals(64, decoded.getTotalBits());
        assertEquals(0L, decoded.getValue(0));
        assertEquals(12L, decoded.getValue("time"));
        assertEquals(3L, decoded.getValue("reserved"));
        assertEquals(7L, decoded.getValue("machine"));
        assertEquals(1L, decoded.getValue("sequence"));
        assertEquals(63, decoded.getSegments().get(0).getHighBit());
        assertEquals(31, decoded.getSegments().get(1).getLowBit());
    }

    @Test
    void shouldComputeIdWithSpecifiedValuesByOrder() {
        CustomSnowflake snowflake = SnowflakeCustomBuilder.builder()
                .add(0)
                .add("time", 32, TimeStrategy.second(0L, () -> 20_000L))
                .addFixed("reserved", 9, 3L)
                .add("machine", 10, MachineStrategy.manual(7L))
                .add("sequence", 12, SequenceStrategy.grouped(), "time")
                .build();

        long minId = snowflake.computeId(0L, 10L, 3L, 7L, 0L);
        long maxId = snowflake.computeId(0L, 10L, 3L, 7L, 4095L);
        long mixedId = snowflake.computeId(0L, 10L, null, null, 1L);

        assertEquals(10L, snowflake.get("time", minId));
        assertEquals(0L, snowflake.get("sequence", minId));
        assertEquals(4095L, snowflake.get("sequence", maxId));
        assertEquals(3L, snowflake.get("reserved", mixedId));
        assertEquals(7L, snowflake.get("machine", mixedId));
    }

    @Test
    void shouldComputeMinAndMaxIdByStrategyBounds() {
        CustomSnowflake snowflake = SnowflakeCustomBuilder.builder()
                .add(0)
                .add("time", 32, TimeStrategy.second(0L, () -> 20_000L))
                .addFixed("reserved", 9, 3L)
                .add("machine", 10, MachineStrategy.manual(7L))
                .add("sequence", 12, SequenceStrategy.grouped(), "time")
                .build();

        long minId = snowflake.minId(0L, 10L, null, null, null);
        long maxId = snowflake.maxId(0L, 10L, null, null, null);

        assertEquals(10L, snowflake.get("time", minId));
        assertEquals(3L, snowflake.get("reserved", minId));
        assertEquals(7L, snowflake.get("machine", minId));
        assertEquals(0L, snowflake.get("sequence", minId));
        assertEquals(4095L, snowflake.get("sequence", maxId));
        assertEquals(7L, snowflake.get("machine", maxId));
    }

    @Test
    void shouldComputeMinAndMaxIdByName() {
        CustomSnowflake snowflake = SnowflakeCustomBuilder.builder()
                .add(0)
                .add("time", 32, TimeStrategy.second(0L, () -> 20_000L))
                .addFixed("reserved", 9, 3L)
                .add("machine", 10, MachineStrategy.manual(7L))
                .add("sequence", 12, SequenceStrategy.grouped(), "time")
                .build();

        Map<String, Long> specifiedValues = new HashMap<>();
        specifiedValues.put("time", 12L);

        long minId = snowflake.minId(specifiedValues);
        long maxId = snowflake.maxId(specifiedValues);

        assertEquals(12L, snowflake.get("time", minId));
        assertEquals(0L, snowflake.get("sequence", minId));
        assertEquals(4095L, snowflake.get("sequence", maxId));
        assertEquals(7L, snowflake.get("machine", maxId));
    }

    @Test
    void shouldComputeIdWithSpecifiedValuesByName() {
        CustomSnowflake snowflake = SnowflakeCustomBuilder.builder()
                .add(0)
                .add("time", 32, TimeStrategy.second(0L, () -> 20_000L))
                .addFixed("reserved", 9, 3L)
                .add("machine", 10, MachineStrategy.manual(7L))
                .add("sequence", 12, SequenceStrategy.grouped(), "time")
                .build();

        Map<String, Long> specifiedValues = new HashMap<>();
        specifiedValues.put("time", 12L);
        specifiedValues.put("sequence", 9L);

        long id = snowflake.computeId(specifiedValues);

        assertEquals(12L, snowflake.get("time", id));
        assertEquals(3L, snowflake.get("reserved", id));
        assertEquals(7L, snowflake.get("machine", id));
        assertEquals(9L, snowflake.get("sequence", id));
    }

    @Test
    void shouldConvertElapsedTimeToMillis() {
        assertEquals(10_000L, TimeStrategy.toElapsedMillis(2L, 5_000L));
        assertEquals(15_000L, TimeStrategy.restore(5_000L, 2L, 5_000L));
    }

    @Test
    void shouldReadSegmentValueDirectlyFromSnowflakeId() {
        CustomSnowflake snowflake = SnowflakeCustomBuilder.builder()
                .add(0)
                .add("time", 32, TimeStrategy.second(0L, () -> 12_000L))
                .addFixed("reserved", 9, 3L)
                .add("machine", 10, MachineStrategy.manual(7L))
                .add("sequence", 12, SequenceStrategy.grouped(), "time")
                .build();

        snowflake.nextId();
        long id = snowflake.nextId();

        assertEquals(12L, snowflake.get("time", id));
        assertEquals(3L, snowflake.get("reserved", id));
        assertEquals(7L, snowflake.get("machine", id));
        assertEquals(1L, snowflake.get("sequence", id));
        assertEquals(0L, snowflake.get(0, id));
    }

    @Test
    void shouldSplitSnowflakeIdIntoOrderedValues() {
        CustomSnowflake snowflake = SnowflakeCustomBuilder.builder()
                .add(0)
                .add("time", 32, TimeStrategy.second(0L, () -> 12_000L))
                .addFixed("reserved", 9, 3L)
                .add("machine", 10, MachineStrategy.manual(7L))
                .add("sequence", 12, SequenceStrategy.grouped(), "time")
                .build();

        snowflake.nextId();
        long id = snowflake.nextId();

        assertTrue(Arrays.equals(new long[]{0L, 12L, 3L, 7L, 1L}, snowflake.split(id)));
    }

    @Test
    void shouldRejectUnknownSegmentNameWhenReadingFromId() {
        CustomSnowflake snowflake = SnowflakeCustomBuilder.builder()
                .add("time", 32, TimeStrategy.second(0L, () -> 12_000L))
                .build();

        long id = snowflake.nextId();

        assertThrows(IllegalArgumentException.class, () -> snowflake.get("missing", id));
    }

    @Test
    void shouldRejectFixedValueThatExceedsBitWidth() {
        assertThrows(IllegalArgumentException.class, () -> SnowflakeCustomBuilder.builder()
                .addFixed(3, 8L));
    }
}
