package org.httpgun.attacker;

import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HttpAttackerStatsTest {
    @Test
    @DisplayName("should get 45 sum")
    void testTimersSum() {
        val stats = createStats();
        Assertions.assertEquals(45L, stats.getTimersSum());
    }

    @Test
    @DisplayName("should get 15 average")
    void testTimersAverage() {
        val stats = createStats();
        val average = stats.getTimersAverage();
        Assertions.assertTrue(average.isPresent());
        Assertions.assertEquals(15L, average.getAsDouble());
    }

    @Test
    @DisplayName("should get 10 for 33%, 15 for 66%, 20 for 99%")
    void testPercentileBy() {
        val stats = createStats();
        Assertions.assertEquals(10L, stats.getPercentileBy(33));
        Assertions.assertEquals(15L, stats.getPercentileBy(66));
        Assertions.assertEquals(20L, stats.getPercentileBy(99));
    }

    private HttpAttackerStats createStats() {
        val stats = new HttpAttackerStats();
        stats.addTimer(10L);
        stats.incrementSuccesses();
        stats.addTimer(15L);
        stats.incrementSuccesses();
        stats.addTimer(20L);
        stats.incrementSuccesses();
        return stats;
    }
}
