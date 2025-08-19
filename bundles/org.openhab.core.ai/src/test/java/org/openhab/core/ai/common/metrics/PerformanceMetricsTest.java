package org.openhab.core.ai.common.metrics;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the PerformanceMetrics class.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
class PerformanceMetricsTest {

    @Test
    void testBasicMetrics() {
        // Given
        Instant now = Instant.now();

        // When
        PerformanceMetrics metrics = PerformanceMetrics.builder().withTotalCount(100).withSuccessCount(85)
                .withFailureCount(15).withTotalDurationMs(5000).withMinDurationMs(10).withMaxDurationMs(200)
                .withFirstExecution(now).withLastExecution(now.plusSeconds(60)).build();

        // Then
        assertEquals(100, metrics.getTotalCount());
        assertEquals(85, metrics.getSuccessCount());
        assertEquals(15, metrics.getFailureCount());
        assertEquals(5000, metrics.getTotalDurationMs());
        assertEquals(10, metrics.getMinDurationMs());
        assertEquals(200, metrics.getMaxDurationMs());
        assertEquals(now, metrics.getFirstExecution());
        assertEquals(now.plusSeconds(60), metrics.getLastExecution());
    }

    @Test
    void testCalculatedMetrics() {
        // When
        PerformanceMetrics metrics = PerformanceMetrics.builder().withTotalCount(100).withSuccessCount(85)
                .withFailureCount(15).withTotalDurationMs(5000).build();

        // Then
        assertEquals(85.0, metrics.getSuccessRate());
        assertEquals(50.0, metrics.getAverageDurationMs());
        assertEquals(Duration.ofMillis(5000), metrics.getTotalDuration());
        assertEquals(Duration.ofMillis(50), metrics.getAverageDuration());
    }

    @Test
    void testCustomMetrics() {
        // Given
        Map<String, Long> counters = new HashMap<>();
        counters.put("api_calls", 150L);
        counters.put("cache_hits", 75L);

        Map<String, Duration> durations = new HashMap<>();
        durations.put("processing_time", Duration.ofSeconds(5));
        durations.put("response_time", Duration.ofMillis(500));

        Map<String, Object> customMetrics = new HashMap<>();
        customMetrics.put("cpu_usage", 75.5);
        customMetrics.put("memory_usage", 1024L);

        // When
        PerformanceMetrics metrics = PerformanceMetrics.builder().withCustomCounters(counters)
                .withCustomDurations(durations).withCustomMetrics(customMetrics).build();

        // Then
        Map<String, Long> resultCounters = metrics.getCustomCounters();
        assertEquals(150L, resultCounters.get("api_calls"));
        assertEquals(75L, resultCounters.get("cache_hits"));

        Map<String, Duration> resultDurations = metrics.getCustomDurations();
        assertEquals(Duration.ofSeconds(5), resultDurations.get("processing_time"));
        assertEquals(Duration.ofMillis(500), resultDurations.get("response_time"));

        Map<String, Object> resultMetrics = metrics.getCustomMetrics();
        assertEquals(75.5, resultMetrics.get("cpu_usage"));
        assertEquals(1024L, resultMetrics.get("memory_usage"));
    }

    @Test
    void testZeroOperations() {
        // When
        PerformanceMetrics metrics = PerformanceMetrics.builder().build();

        // Then
        assertEquals(0.0, metrics.getSuccessRate());
        assertEquals(0.0, metrics.getAverageDurationMs());
        assertFalse(metrics.hasOperations());
        assertFalse(metrics.hasSuccessfulOperations());
        assertFalse(metrics.hasFailedOperations());
    }

    @Test
    void testSuccessRateCalculation() {
        // When
        PerformanceMetrics metrics = PerformanceMetrics.builder().withTotalCount(100).withSuccessCount(100).build();

        // Then
        assertEquals(100.0, metrics.getSuccessRate());
        assertTrue(metrics.hasSuccessfulOperations());
        assertFalse(metrics.hasFailedOperations());
    }

    @Test
    void testFailureRateCalculation() {
        // When
        PerformanceMetrics metrics = PerformanceMetrics.builder().withTotalCount(100).withFailureCount(100).build();

        // Then
        assertEquals(0.0, metrics.getSuccessRate());
        assertFalse(metrics.hasSuccessfulOperations());
        assertTrue(metrics.hasFailedOperations());
    }

    @Test
    void testDurationCalculations() {
        // When
        PerformanceMetrics metrics = PerformanceMetrics.builder().withTotalCount(10).withTotalDurationMs(1000)
                .withMinDurationMs(50).withMaxDurationMs(150).build();

        // Then
        assertEquals(100.0, metrics.getAverageDurationMs());
        assertEquals(Duration.ofMillis(1000), metrics.getTotalDuration());
        assertEquals(Duration.ofMillis(100), metrics.getAverageDuration());
        assertEquals(Duration.ofMillis(50), metrics.getMinDuration());
        assertEquals(Duration.ofMillis(150), metrics.getMaxDuration());
    }

    @Test
    void testBuilderFromExistingMetrics() {
        // Given
        PerformanceMetrics original = PerformanceMetrics.builder().withTotalCount(100).withSuccessCount(85)
                .withTotalDurationMs(5000).withCustomCounter("test", 50L).build();

        // When
        PerformanceMetrics copy = new PerformanceMetricsBuilder(original).build();

        // Then
        assertEquals(original.getTotalCount(), copy.getTotalCount());
        assertEquals(original.getSuccessCount(), copy.getSuccessCount());
        assertEquals(original.getTotalDurationMs(), copy.getTotalDurationMs());
        assertEquals(original.getCustomCounters(), copy.getCustomCounters());
    }

    @Test
    void testBuilderFromNullSource() {
        // When
        PerformanceMetricsBuilder builder = new PerformanceMetricsBuilder(null);
        PerformanceMetrics metrics = builder.build();

        // Then
        assertNotNull(metrics);
        assertEquals(0, metrics.getTotalCount());
    }

    @Test
    void testEquality() {
        // Given
        PerformanceMetrics metrics1 = PerformanceMetrics.builder().withTotalCount(100).withSuccessCount(85)
                .withTotalDurationMs(5000).build();

        PerformanceMetrics metrics2 = PerformanceMetrics.builder().withTotalCount(100).withSuccessCount(85)
                .withTotalDurationMs(5000).build();

        PerformanceMetrics metrics3 = PerformanceMetrics.builder().withTotalCount(200).withSuccessCount(85)
                .withTotalDurationMs(5000).build();

        // Then
        assertEquals(metrics1, metrics2);
        assertNotEquals(metrics1, metrics3);
        assertEquals(metrics1.hashCode(), metrics2.hashCode());
        assertNotEquals(metrics1.hashCode(), metrics3.hashCode());
    }

    @Test
    void testToString() {
        // When
        PerformanceMetrics metrics = PerformanceMetrics.builder().withTotalCount(100).withSuccessCount(85)
                .withTotalDurationMs(5000).withCustomCounter("test", 50L).build();

        String result = metrics.toString();

        // Then
        assertTrue(result.contains("totalCount=100"));
        assertTrue(result.contains("successCount=85"));
        assertTrue(result.contains("successRate=85.0%"));
        assertTrue(result.contains("averageDurationMs=50.0"));
        assertTrue(result.contains("customCounters=1"));
    }

    @Test
    void testValidationWithNegativeValues() {
        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            PerformanceMetrics.builder().withTotalCount(-1).build();
        });

        assertThrows(IllegalStateException.class, () -> {
            PerformanceMetrics.builder().withSuccessCount(-1).build();
        });

        assertThrows(IllegalStateException.class, () -> {
            PerformanceMetrics.builder().withFailureCount(-1).build();
        });

        assertThrows(IllegalStateException.class, () -> {
            PerformanceMetrics.builder().withTotalDurationMs(-1).build();
        });
    }

    @Test
    void testValidationWithInvalidCounts() {
        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            PerformanceMetrics.builder().withTotalCount(100).withSuccessCount(80).withFailureCount(30) // 80 + 30 = 110
                                                                                                       // > 100
                    .build();
        });
    }

    @Test
    void testValidationWithInvalidTimeOrder() {
        // Given
        Instant now = Instant.now();
        Instant later = now.plusSeconds(60);

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            PerformanceMetrics.builder().withFirstExecution(later).withLastExecution(now) // first after last
                    .build();
        });
    }
}
