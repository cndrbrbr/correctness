package io.github.cndrbrbr.correctness.punish;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OffenseTrackerTest {

    private static final long HOUR = 3_600_000L;

    private final AtomicLong now = new AtomicLong(1_000_000L);
    private final OffenseTracker tracker = new OffenseTracker(List.of(0, 3, 10), 24, now::get);
    private final UUID player = UUID.randomUUID();

    @Test
    void stepsIncreaseAndStayAtLast() {
        assertEquals(0, tracker.minutesFor(tracker.record(player)));
        assertEquals(3, tracker.minutesFor(tracker.record(player)));
        assertEquals(10, tracker.minutesFor(tracker.record(player)));
        assertEquals(10, tracker.minutesFor(tracker.record(player)));
    }

    @Test
    void offensesExpire() {
        tracker.record(player);
        now.addAndGet(23 * HOUR);
        assertEquals(2, tracker.record(player));
        now.addAndGet(2 * HOUR);
        assertEquals(1, tracker.count(player));
        now.addAndGet(23 * HOUR);
        assertEquals(0, tracker.count(player));
    }

    @Test
    void forgiveResets() {
        tracker.record(player);
        tracker.record(player);
        tracker.forgive(player);
        assertEquals(1, tracker.record(player));
    }

    @Test
    void zeroExpiryNeverForgets() {
        tracker.configure(List.of(0, 5), 0);
        tracker.record(player);
        now.addAndGet(1000 * HOUR);
        assertEquals(2, tracker.record(player));
    }
}
