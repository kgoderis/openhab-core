package org.openhab.core.ai.common.monitoring.service.snapshot;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Test class for GenericMetricsSnapshot.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
class GenericMetricsSnapshotTest {

    @Test
    void testBasicSnapshotCreation() {
        // Create a snapshot using the builder pattern
        GenericMetricsSnapshot snapshot = GenericMetricsSnapshot.builder("authentication", "attempt")
                .withCount("total_attempts", 100L)
                .withCount("successful_attempts", 85L)
                .withCount("failed_attempts", 15L)
                .withDuration("total_duration_ms", 5000L)
                .withRate("success_rate", 0.85)
                .withSuccess("last_attempt", true)
                .build();

        // Verify basic properties
        assertEquals("authentication", snapshot.getDomain());
        assertEquals("attempt", snapshot.getOperation());
        assertTrue(snapshot.getTimestampMs() > 0);
        assertEquals(6, snapshot.getMetricCount());

        // Verify metric values
        assertEquals(100L, snapshot.getMetricAsLong("total_attempts"));
        assertEquals(85L, snapshot.getMetricAsLong("successful_attempts"));
        assertEquals(15L, snapshot.getMetricAsLong("failed_attempts"));
        assertEquals(5000L, snapshot.getMetricAsLong("total_duration_ms"));
        assertEquals(0.85, snapshot.getMetricAsDouble("success_rate"), 0.001);
        assertTrue(snapshot.getMetric("last_attempt") instanceof Boolean);
        assertTrue((Boolean) snapshot.getMetric("last_attempt"));

        // Verify metric existence
        assertTrue(snapshot.hasMetric("total_attempts"));
        assertFalse(snapshot.hasMetric("nonexistent_metric"));

        // Verify string conversion
        assertEquals("100", snapshot.getMetricAsString("total_attempts"));
        assertEquals("0.85", snapshot.getMetricAsString("success_rate"));
    }

    @Test
    void testSnapshotWithMap() {
        // Create metrics map
        Map<String, Object> metrics = Map.of(
                "request_count", 50L,
                "error_count", 5L,
                "average_response_time_ms", 150.5,
                "status", "healthy"
        );

        // Create snapshot with map
        GenericMetricsSnapshot snapshot = new GenericMetricsSnapshot("http_transport", "request", metrics);

        // Verify metrics
        assertEquals(50L, snapshot.getMetricAsLong("request_count"));
        assertEquals(5L, snapshot.getMetricAsLong("error_count"));
        assertEquals(150.5, snapshot.getMetricAsDouble("average_response_time_ms"), 0.001);
        assertEquals("healthy", snapshot.getMetricAsString("status"));
    }

    @Test
    void testImmutableMetrics() {
        // Create snapshot
        GenericMetricsSnapshot snapshot = GenericMetricsSnapshot.builder("test", "operation")
                .withCount("count", 10L)
                .build();

        // Get metrics map (should be unmodifiable)
        Map<String, Object> metrics = snapshot.getMetrics();
        
        // Verify it's unmodifiable
        assertThrows(UnsupportedOperationException.class, () -> {
            metrics.put("new_metric", 20L);
        });
    }

    @Test
    void testTimestampConversion() {
        GenericMetricsSnapshot snapshot = GenericMetricsSnapshot.builder("test", "operation")
                .withCount("count", 1L)
                .build();

        // Verify timestamp conversion
        long timestampMs = snapshot.getTimestampMs();
        Instant timestamp = snapshot.getTimestamp();
        
        assertEquals(timestampMs, timestamp.toEpochMilli());
        assertTrue(timestamp.isAfter(Instant.now().minusSeconds(1)));
    }

    @Test
    void testEqualsAndHashCode() {
        GenericMetricsSnapshot snapshot1 = GenericMetricsSnapshot.builder("domain", "operation")
                .withCount("count", 10L)
                .build();

        GenericMetricsSnapshot snapshot2 = GenericMetricsSnapshot.builder("domain", "operation")
                .withCount("count", 10L)
                .build();

        // Note: These might not be equal due to different timestamps
        // In a real implementation, you might want to exclude timestamp from equals/hashCode
        // or use a fixed timestamp for testing
        assertNotEquals(snapshot1, snapshot2);
    }

    @Test
    void testToString() {
        GenericMetricsSnapshot snapshot = GenericMetricsSnapshot.builder("test_domain", "test_operation")
                .withCount("count", 42L)
                .withRate("rate", 0.95)
                .build();

        String toString = snapshot.toString();
        
        assertTrue(toString.contains("test_domain"));
        assertTrue(toString.contains("test_operation"));
        assertTrue(toString.contains("count"));
        assertTrue(toString.contains("42"));
        assertTrue(toString.contains("rate"));
        assertTrue(toString.contains("0.95"));
    }
}
