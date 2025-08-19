package org.openhab.core.ai.common.builder;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the MetricsBuilder class.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
class MetricsBuilderTest {

    private TestMetricsBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new TestMetricsBuilder();
    }

    @Test
    void testBasicMetricsSetters() {
        // Given
        Instant now = Instant.now();

        // When
        TestMetrics result = builder.withTotalCount(100).withSuccessCount(80).withFailureCount(20)
                .withTotalDurationMs(5000).withMinDurationMs(10).withMaxDurationMs(200).withFirstExecution(now)
                .withLastExecution(now.plusSeconds(60)).build();

        // Then
        assertNotNull(result);
        assertEquals(100, result.getTotalCount());
        assertEquals(80, result.getSuccessCount());
        assertEquals(20, result.getFailureCount());
        assertEquals(5000, result.getTotalDurationMs());
        assertEquals(10, result.getMinDurationMs());
        assertEquals(200, result.getMaxDurationMs());
        assertEquals(now, result.getFirstExecution());
        assertEquals(now.plusSeconds(60), result.getLastExecution());
    }

    @Test
    void testCustomCounters() {
        // Given
        Map<String, Long> counters = new HashMap<>();
        counters.put("api_calls", 150L);
        counters.put("cache_hits", 75L);

        // When
        TestMetrics result = builder.withCustomCounter("requests", 200L).withCustomCounters(counters).build();

        // Then
        Map<String, Long> resultCounters = result.getCustomCounters();
        assertEquals(200L, resultCounters.get("requests"));
        assertEquals(150L, resultCounters.get("api_calls"));
        assertEquals(75L, resultCounters.get("cache_hits"));
    }

    @Test
    void testCustomDurations() {
        // Given
        Duration duration1 = Duration.ofSeconds(5);
        Duration duration2 = Duration.ofMillis(500);

        // When
        TestMetrics result = builder.withCustomDuration("processing_time", duration1)
                .withCustomDuration("response_time", duration2).build();

        // Then
        Map<String, Duration> resultDurations = result.getCustomDurations();
        assertEquals(duration1, resultDurations.get("processing_time"));
        assertEquals(duration2, resultDurations.get("response_time"));
    }

    @Test
    void testCustomMetrics() {
        // Given
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("cpu_usage", 75.5);
        metrics.put("memory_usage", 1024L);

        // When
        TestMetrics result = builder.withCustomMetric("error_rate", 0.05).withCustomMetrics(metrics).build();

        // Then
        Map<String, Object> resultMetrics = result.getCustomMetrics();
        assertEquals(0.05, resultMetrics.get("error_rate"));
        assertEquals(75.5, resultMetrics.get("cpu_usage"));
        assertEquals(1024L, resultMetrics.get("memory_usage"));
    }

    @Test
    void testCalculatedSuccessRate() {
        // When
        TestMetrics result = builder.withTotalCount(100).withSuccessCount(85).withCalculatedSuccessRate().build();

        // Then
        Map<String, Object> metrics = result.getCustomMetrics();
        assertEquals(85.0, metrics.get("successRate"));
    }

    @Test
    void testCalculatedAverageDuration() {
        // When
        TestMetrics result = builder.withTotalCount(10).withTotalDurationMs(5000).withCalculatedAverageDuration()
                .build();

        // Then
        Map<String, Object> metrics = result.getCustomMetrics();
        assertEquals(500.0, metrics.get("averageDurationMs"));
    }

    @Test
    void testValidationWithNegativeValues() {
        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            builder.withTotalCount(-1).build();
        });

        assertThrows(IllegalStateException.class, () -> {
            builder.withSuccessCount(-1).build();
        });

        assertThrows(IllegalStateException.class, () -> {
            builder.withFailureCount(-1).build();
        });

        assertThrows(IllegalStateException.class, () -> {
            builder.withTotalDurationMs(-1).build();
        });
    }

    @Test
    void testValidationWithInvalidCounts() {
        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            builder.withTotalCount(100).withSuccessCount(80).withFailureCount(30) // 80 + 30 = 110 > 100
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
            builder.withFirstExecution(later).withLastExecution(now) // first after last
                    .build();
        });
    }

    @Test
    void testReset() {
        // Given
        builder.withTotalCount(100).withSuccessCount(80).withCustomCounter("test", 50L);

        // When
        builder.reset();

        // Then
        assertEquals(0, builder.getTotalCount());
        assertEquals(0, builder.getSuccessCount());
        assertTrue(builder.getCustomCounters().isEmpty());
    }

    @Test
    void testNullCustomCounterName() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            builder.withCustomCounter(null, 100L);
        });
    }

    @Test
    void testNullCustomDurationName() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            builder.withCustomDuration(null, Duration.ofSeconds(1));
        });
    }

    @Test
    void testNullCustomDurationValue() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            builder.withCustomDuration("test", null);
        });
    }

    @Test
    void testNullCustomMetricName() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            builder.withCustomMetric(null, "value");
        });
    }

    // Test implementation class
    private static class TestMetrics {
        private final long totalCount;
        private final long successCount;
        private final long failureCount;
        private final long totalDurationMs;
        private final long minDurationMs;
        private final long maxDurationMs;
        private final Instant firstExecution;
        private final Instant lastExecution;
        private final Map<String, Long> customCounters;
        private final Map<String, Duration> customDurations;
        private final Map<String, Object> customMetrics;

        TestMetrics(TestMetricsBuilder builder) {
            this.totalCount = builder.totalCount;
            this.successCount = builder.successCount;
            this.failureCount = builder.failureCount;
            this.totalDurationMs = builder.totalDurationMs;
            this.minDurationMs = builder.minDurationMs;
            this.maxDurationMs = builder.maxDurationMs;
            this.firstExecution = builder.firstExecution;
            this.lastExecution = builder.lastExecution;
            this.customCounters = new HashMap<>(builder.customCounters);
            this.customDurations = new HashMap<>(builder.customDurations);
            this.customMetrics = new HashMap<>(builder.customMetrics);
        }

        public long getTotalCount() {
            return totalCount;
        }

        public long getSuccessCount() {
            return successCount;
        }

        public long getFailureCount() {
            return failureCount;
        }

        public long getTotalDurationMs() {
            return totalDurationMs;
        }

        public long getMinDurationMs() {
            return minDurationMs;
        }

        public long getMaxDurationMs() {
            return maxDurationMs;
        }

        public Instant getFirstExecution() {
            return firstExecution;
        }

        public Instant getLastExecution() {
            return lastExecution;
        }

        public Map<String, Long> getCustomCounters() {
            return customCounters;
        }

        public Map<String, Duration> getCustomDurations() {
            return customDurations;
        }

        public Map<String, Object> getCustomMetrics() {
            return customMetrics;
        }
    }

    private static class TestMetricsBuilder extends MetricsBuilder<TestMetrics> {
        @Override
        public TestMetrics build() {
            validate();
            return new TestMetrics(this);
        }
    }
}
