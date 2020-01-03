package org.httpgun.attacker;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class HttpAttackerStats {
    private final List<Long> timers = Collections.synchronizedList(new ArrayList<>());
    private final AtomicInteger successes = new AtomicInteger();
    private final AtomicInteger fails = new AtomicInteger();
    private final AtomicLong totalBytesCount = new AtomicLong();

    void addTimer(long time) {
        timers.add(time);
    }

    void incrementFails() {
        fails.incrementAndGet();
    }

    void incrementSuccesses() {
        successes.incrementAndGet();
    }

    public int getFails() {
        return fails.get();
    }

    public int getSuccesses() {
        return successes.get();
    }

    public long getTimersSum() {
        return timers.stream().mapToLong(Long::longValue).sum();
    }

    public void addBytes(long count) {
        totalBytesCount.addAndGet(count);
    }

    public long getTotalBytesCount() {
        return totalBytesCount.get();
    }

    public OptionalDouble getTimersAverage() {
        return timers.stream().mapToLong(Long::longValue).average();
    }

    public long getPercentileBy(double percentile) {
        timers.sort(Comparator.comparingLong(Long::longValue));
        int index = (int) Math.ceil((percentile / (double) 100) * (double) timers.size());
        return timers.get(index - 1);
    }
}
