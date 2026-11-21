package io.github.cndrbrbr.correctness.punish;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.LongSupplier;

/**
 * Zaehlt Verstoesse pro Spieler. Nur Verstoesse innerhalb des Verfallszeitraums zaehlen;
 * daraus ergibt sich die Strafstufe. Reine Logik ohne Bukkit, gespeichert wird im {@link Punisher}.
 */
public final class OffenseTracker {

    private final Map<UUID, List<Long>> offenses = new HashMap<>();
    private final LongSupplier clock;
    private List<Integer> steps;
    private long expiryMillis;

    /**
     * @param steps        Minuten Gefaengnis je Verstoss (0 = nur Verwarnung); die letzte Stufe
     *                     gilt fuer alle weiteren Verstoesse
     * @param expiryHours  nach so vielen Stunden zaehlt ein Verstoss nicht mehr
     */
    public OffenseTracker(List<Integer> steps, double expiryHours, LongSupplier clock) {
        this.clock = clock;
        configure(steps, expiryHours);
    }

    public void configure(List<Integer> steps, double expiryHours) {
        this.steps = steps.isEmpty() ? List.of(0) : List.copyOf(steps);
        this.expiryMillis = (long) (expiryHours * 3_600_000L);
    }

    /** Verstoss eintragen; liefert die Anzahl der zaehlenden Verstoesse inklusive diesem. */
    public int record(UUID player) {
        long now = clock.getAsLong();
        List<Long> list = offenses.computeIfAbsent(player, id -> new ArrayList<>());
        list.add(now);
        prune(list, now);
        return list.size();
    }

    /** Zaehlende Verstoesse ohne neuen Eintrag. */
    public int count(UUID player) {
        List<Long> list = offenses.get(player);
        if (list == null) {
            return 0;
        }
        prune(list, clock.getAsLong());
        return list.size();
    }

    /** Minuten Gefaengnis fuer den n-ten Verstoss (n ab 1); 0 = Verwarnung. */
    public int minutesFor(int count) {
        return steps.get(Math.max(0, Math.min(count, steps.size()) - 1));
    }

    public void forgive(UUID player) {
        offenses.remove(player);
    }

    public Map<UUID, List<Long>> snapshot() {
        long now = clock.getAsLong();
        Map<UUID, List<Long>> copy = new HashMap<>();
        offenses.forEach((id, list) -> {
            prune(list, now);
            if (!list.isEmpty()) {
                copy.put(id, List.copyOf(list));
            }
        });
        return copy;
    }

    public void load(UUID player, List<Long> times) {
        offenses.put(player, new ArrayList<>(times));
    }

    private void prune(List<Long> list, long now) {
        if (expiryMillis > 0) {
            list.removeIf(time -> now - time >= expiryMillis);
        }
    }
}
