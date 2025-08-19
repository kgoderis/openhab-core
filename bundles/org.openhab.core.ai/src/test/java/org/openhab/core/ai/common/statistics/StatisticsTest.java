package org.openhab.core.ai.common.statistics;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the Statistics interface and BaseStatistics class.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
class StatisticsTest {

    @Test
    void testStatisticsInterface() {
        // Test that the interface defines the expected methods
        Statistics stats = new TestStatistics("test-id", StatisticsType.PERFORMANCE);

        assertEquals("test-id", stats.getId());
        assertEquals(StatisticsType.PERFORMANCE, stats.getType());
        assertNotNull(stats.getTimestamp());
        assertTrue(stats.getMetrics().isEmpty());
        assertNull(stats.getMetric("nonexistent"));
        assertFalse(stats.hasMetric("nonexistent"));
    }

    @Test
    void testBaseStatistics() {
        // Given
        String id = "test-stats";
        StatisticsType type = StatisticsType.EXECUTION;
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalCount", 100L);
        metrics.put("successCount", 85L);
        metrics.put("failureCount", 15L);

        // When
        TestStatistics stats = new TestStatistics(id, type, metrics);

        // Then
        assertEquals(id, stats.getId());
        assertEquals(type, stats.getType());
        assertNotNull(stats.getTimestamp());
        assertEquals(3, stats.getMetricCount());
        assertTrue(stats.hasMetrics());
        assertEquals(100L, stats.getMetric("totalCount"));
        assertEquals(85L, stats.getMetric("successCount"));
        assertEquals(15L, stats.getMetric("failureCount"));
    }

    @Test
    void testDefaultMetrics() {
        // Given
        Statistics stats = new TestStatistics("test", StatisticsType.PERFORMANCE);

        // When & Then
        assertEquals(0L, stats.getTotalCount());
        assertEquals(0L, stats.getSuccessCount());
        assertEquals(0L, stats.getFailureCount());
        assertEquals(0.0, stats.getSuccessRate());
        assertFalse(stats.hasOperations());
        assertFalse(stats.hasSuccessfulOperations());
        assertFalse(stats.hasFailedOperations());
    }

    @Test
    void testCalculatedMetrics() {
        // Given
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalCount", 100L);
        metrics.put("successCount", 85L);
        metrics.put("failureCount", 15L);

        Statistics stats = new TestStatistics("test", StatisticsType.PERFORMANCE, metrics);

        // When & Then
        assertEquals(100L, stats.getTotalCount());
        assertEquals(85L, stats.getSuccessCount());
        assertEquals(15L, stats.getFailureCount());
        assertEquals(85.0, stats.getSuccessRate());
        assertTrue(stats.hasOperations());
        assertTrue(stats.hasSuccessfulOperations());
        assertTrue(stats.hasFailedOperations());
    }

    @Test
    void testSuccessRateWithZeroTotal() {
        // Given
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalCount", 0L);
        metrics.put("successCount", 0L);
        metrics.put("failureCount", 0L);

        Statistics stats = new TestStatistics("test", StatisticsType.PERFORMANCE, metrics);

        // When & Then
        assertEquals(0.0, stats.getSuccessRate());
        assertFalse(stats.hasOperations());
    }

    @Test
    void testSuccessRateWithAllSuccess() {
        // Given
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalCount", 100L);
        metrics.put("successCount", 100L);
        metrics.put("failureCount", 0L);

        Statistics stats = new TestStatistics("test", StatisticsType.PERFORMANCE, metrics);

        // When & Then
        assertEquals(100.0, stats.getSuccessRate());
        assertTrue(stats.hasSuccessfulOperations());
        assertFalse(stats.hasFailedOperations());
    }

    @Test
    void testSuccessRateWithAllFailures() {
        // Given
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalCount", 100L);
        metrics.put("successCount", 0L);
        metrics.put("failureCount", 100L);

        Statistics stats = new TestStatistics("test", StatisticsType.PERFORMANCE, metrics);

        // When & Then
        assertEquals(0.0, stats.getSuccessRate());
        assertFalse(stats.hasSuccessfulOperations());
        assertTrue(stats.hasFailedOperations());
    }

    @Test
    void testMetricManagement() {
        // Given
        TestStatistics stats = new TestStatistics("test", StatisticsType.CUSTOM);

        // When
        stats.addMetric("testMetric", "testValue");
        stats.addMetric("numericMetric", 42);

        // Then
        assertTrue(stats.hasMetric("testMetric"));
        assertTrue(stats.hasMetric("numericMetric"));
        assertEquals("testValue", stats.getMetric("testMetric"));
        assertEquals(42, stats.getMetric("numericMetric"));
        assertEquals(2, stats.getMetricCount());

        // When removing
        stats.removeMetric("testMetric");

        // Then
        assertFalse(stats.hasMetric("testMetric"));
        assertTrue(stats.hasMetric("numericMetric"));
        assertEquals(1, stats.getMetricCount());

        // When clearing
        stats.clearMetrics();

        // Then
        assertFalse(stats.hasMetrics());
        assertEquals(0, stats.getMetricCount());
    }

    @Test
    void testEquality() {
        // Given
        Map<String, Object> metrics1 = new HashMap<>();
        metrics1.put("count", 100L);

        Map<String, Object> metrics2 = new HashMap<>();
        metrics2.put("count", 100L);

        TestStatistics stats1 = new TestStatistics("test", StatisticsType.PERFORMANCE, metrics1);
        TestStatistics stats2 = new TestStatistics("test", StatisticsType.PERFORMANCE, metrics2);
        TestStatistics stats3 = new TestStatistics("different", StatisticsType.PERFORMANCE, metrics1);

        // Then
        assertEquals(stats1, stats2);
        assertNotEquals(stats1, stats3);
        assertEquals(stats1.hashCode(), stats2.hashCode());
        assertNotEquals(stats1.hashCode(), stats3.hashCode());
    }

    @Test
    void testToString() {
        // Given
        TestStatistics stats = new TestStatistics("test-id", StatisticsType.MONITORING);

        // When
        String result = stats.toString();

        // Then
        assertTrue(result.contains("test-id"));
        assertTrue(result.contains("MONITORING"));
        assertTrue(result.contains("metricCount=0"));
    }

    @Test
    void testStatisticsTypeValues() {
        // When & Then
        assertEquals("performance", StatisticsType.PERFORMANCE.getValue());
        assertEquals("security", StatisticsType.SECURITY.getValue());
        assertEquals("execution", StatisticsType.EXECUTION.getValue());
        assertEquals("monitoring", StatisticsType.MONITORING.getValue());
        assertEquals("transport", StatisticsType.TRANSPORT.getValue());
        assertEquals("resource", StatisticsType.RESOURCE.getValue());
        assertEquals("communication", StatisticsType.COMMUNICATION.getValue());
        assertEquals("error", StatisticsType.ERROR.getValue());
        assertEquals("system", StatisticsType.SYSTEM.getValue());
        assertEquals("custom", StatisticsType.CUSTOM.getValue());
    }

    @Test
    void testStatisticsTypeToString() {
        // When & Then
        assertEquals("performance", StatisticsType.PERFORMANCE.toString());
        assertEquals("security", StatisticsType.SECURITY.toString());
        assertEquals("execution", StatisticsType.EXECUTION.toString());
    }

    @Test
    void testNullIdThrowsException() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            new TestStatistics(null, StatisticsType.PERFORMANCE);
        });
    }

    @Test
    void testNullTypeThrowsException() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            new TestStatistics("test", null);
        });
    }

    @Test
    void testNullMetricsThrowsException() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            new TestStatistics("test", StatisticsType.PERFORMANCE, null);
        });
    }

    @Test
    void testNullMetricKeyThrowsException() {
        // Given
        TestStatistics stats = new TestStatistics("test", StatisticsType.PERFORMANCE);

        // When & Then
        assertThrows(NullPointerException.class, () -> {
            stats.addMetric(null, "value");
        });
    }

    /**
     * Test implementation of Statistics for testing purposes.
     */
    private static class TestStatistics extends BaseStatistics {

        public TestStatistics(String id, StatisticsType type) {
            super(id, type);
        }

        public TestStatistics(String id, StatisticsType type, Map<String, Object> metrics) {
            super(id, type, metrics);
        }

        public void addMetric(String key, Object value) {
            super.addMetric(key, value);
        }

        public void removeMetric(String key) {
            super.removeMetric(key);
        }

        public void clearMetrics() {
            super.clearMetrics();
        }
    }
}
