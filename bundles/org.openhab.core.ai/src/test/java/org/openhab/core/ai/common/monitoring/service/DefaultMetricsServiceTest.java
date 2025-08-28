package org.openhab.core.ai.common.monitoring.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.ai.common.monitoring.api.MetricKey;
import org.openhab.core.ai.common.monitoring.collector.MetricsCollector;
import org.openhab.core.ai.common.monitoring.registry.MetricsRegistry;
import org.openhab.core.ai.common.monitoring.service.snapshot.DomainAggregatedSnapshot;
import org.openhab.core.ai.common.monitoring.service.snapshot.GenericMetricsSnapshot;
import org.openhab.core.ai.common.monitoring.snapshot.ExecutionMetricsSnapshot;

/**
 * Test class for DefaultMetricsService.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class DefaultMetricsServiceTest {

    @Mock
    private MetricsRegistry monitoringRegistry;

    @Mock
    private MetricsCollector metricsCollector;

    @Mock
    private ExecutionMetricsSnapshot executionSnapshot;

    private DefaultMetricsService metricsService;

    @BeforeEach
    void setUp() {
        metricsService = new DefaultMetricsService();
        // Use reflection to set the private field for testing
        try {
            java.lang.reflect.Field field = DefaultMetricsService.class.getDeclaredField("monitoringRegistry");
            field.setAccessible(true);
            field.set(metricsService, monitoringRegistry);
        } catch (Exception e) {
            fail("Failed to set monitoring registry for testing: " + e.getMessage());
        }
    }

    @Test
    void testRecordOperation() {
        // Given
        when(monitoringRegistry.getCollector(any(MetricKey.class))).thenReturn(metricsCollector);

        // When
        metricsService.recordOperation("test_domain", "test_operation", true, Duration.ofMillis(100));

        // Then
        verify(monitoringRegistry).getCollector(any(MetricKey.class));
        verify(metricsCollector).recordExecution(true, 100_000_000L); // 100ms in nanoseconds
    }

    @Test
    void testGetSnapshotWithValidData() {
        // Given
        when(monitoringRegistry.getCollector(any(MetricKey.class))).thenReturn(metricsCollector);
        when(metricsCollector.executionSnapshot()).thenReturn(executionSnapshot);
        when(executionSnapshot.total()).thenReturn(100L);
        when(executionSnapshot.success()).thenReturn(85L);
        when(executionSnapshot.failure()).thenReturn(15L);
        when(executionSnapshot.totalDurationNanos()).thenReturn(5000_000_000L); // 5 seconds in nanoseconds

        // When
        GenericMetricsSnapshot snapshot = metricsService.getSnapshot("test_domain", "test_operation");

        // Then
        assertNotNull(snapshot);
        assertEquals("test_domain", snapshot.getDomain());
        assertEquals("test_operation", snapshot.getOperation());
        assertEquals(100L, snapshot.getMetricAsLong("total_count"));
        assertEquals(85L, snapshot.getMetricAsLong("success_count"));
        assertEquals(15L, snapshot.getMetricAsLong("failure_count"));
        assertEquals(5000L, snapshot.getMetricAsLong("total_duration_ms")); // 5 seconds in milliseconds
        assertEquals(50L, snapshot.getMetricAsLong("average_duration_ms")); // 5000ms / 100 operations
        assertEquals(0.85, snapshot.getMetricAsDouble("success_rate"), 0.001);
        assertEquals(0.15, snapshot.getMetricAsDouble("failure_rate"), 0.001);
    }

    @Test
    void testGetSnapshotWithNoData() {
        // Given
        when(monitoringRegistry.getCollector(any(MetricKey.class))).thenReturn(metricsCollector);
        when(metricsCollector.executionSnapshot()).thenReturn(executionSnapshot);
        when(executionSnapshot.total()).thenReturn(0L);
        when(executionSnapshot.success()).thenReturn(0L);
        when(executionSnapshot.failure()).thenReturn(0L);
        when(executionSnapshot.totalDurationNanos()).thenReturn(0L);

        // When
        GenericMetricsSnapshot snapshot = metricsService.getSnapshot("test_domain", "test_operation");

        // Then
        assertNotNull(snapshot);
        assertEquals("test_domain", snapshot.getDomain());
        assertEquals("test_operation", snapshot.getOperation());
        assertEquals(0L, snapshot.getMetricAsLong("total_count"));
        assertEquals(0L, snapshot.getMetricAsLong("success_count"));
        assertEquals(0L, snapshot.getMetricAsLong("failure_count"));
        assertEquals(0L, snapshot.getMetricAsLong("total_duration_ms"));
        assertEquals(0L, snapshot.getMetricAsLong("average_duration_ms"));
        assertEquals(0.0, snapshot.getMetricAsDouble("success_rate"), 0.001);
        assertEquals(0.0, snapshot.getMetricAsDouble("failure_rate"), 0.001);
    }

    @Test
    void testGetSnapshotWithRegistryNull() {
        // Given
        DefaultMetricsService serviceWithoutRegistry = new DefaultMetricsService();
        // Don't set the monitoring registry

        // When
        GenericMetricsSnapshot snapshot = serviceWithoutRegistry.getSnapshot("test_domain", "test_operation");

        // Then
        assertNotNull(snapshot);
        assertEquals("test_domain", snapshot.getDomain());
        assertEquals("test_operation", snapshot.getOperation());
        assertEquals(0L, snapshot.getMetricAsLong("total_count"));
        assertEquals(0L, snapshot.getMetricAsLong("success_count"));
        assertEquals(0L, snapshot.getMetricAsLong("failure_count"));
        assertEquals(0L, snapshot.getMetricAsLong("total_duration_ms"));
        assertEquals(0.0, snapshot.getMetricAsDouble("success_rate"), 0.001);
    }

    @Test
    void testGetSnapshotWithException() {
        // Given
        when(monitoringRegistry.getCollector(any(MetricKey.class))).thenThrow(new RuntimeException("Test exception"));

        // When
        GenericMetricsSnapshot snapshot = metricsService.getSnapshot("test_domain", "test_operation");

        // Then
        assertNotNull(snapshot);
        assertEquals("test_domain", snapshot.getDomain());
        assertEquals("test_operation", snapshot.getOperation());
        assertEquals(0L, snapshot.getMetricAsLong("total_count"));
        assertEquals(0L, snapshot.getMetricAsLong("success_count"));
        assertEquals(0L, snapshot.getMetricAsLong("failure_count"));
        assertEquals(0L, snapshot.getMetricAsLong("total_duration_ms"));
        assertEquals(0.0, snapshot.getMetricAsDouble("success_rate"), 0.001);
    }

    @Test
    void testRecordOperationWithData() {
        // Given
        when(monitoringRegistry.getCollector(any(MetricKey.class))).thenReturn(metricsCollector);
        Map<String, Object> data = Map.of("key1", "value1", "key2", 42L);

        // When
        metricsService.recordOperationWithData("test_domain", "test_operation", true, Duration.ofMillis(200), data);

        // Then
        verify(monitoringRegistry).getCollector(any(MetricKey.class));
        verify(metricsCollector).recordExecution(true, 200_000_000L); // 200ms in nanoseconds
        // Note: The current implementation doesn't store the additional data, but logs it
    }

    @Test
    void testGetSnapshotMetricKeyCreation() {
        // Given
        when(monitoringRegistry.getCollector(any(MetricKey.class))).thenReturn(metricsCollector);
        when(metricsCollector.executionSnapshot()).thenReturn(executionSnapshot);
        when(executionSnapshot.total()).thenReturn(10L);
        when(executionSnapshot.success()).thenReturn(8L);
        when(executionSnapshot.failure()).thenReturn(2L);
        when(executionSnapshot.totalDurationNanos()).thenReturn(1000_000_000L); // 1 second

        // When
        metricsService.getSnapshot("auth", "login");

        // Then
        verify(monitoringRegistry).getCollector(argThat(
                metricKey -> metricKey.kind().equals("auth.login") && metricKey.labels().get("domain").equals("auth")
                        && metricKey.labels().get("operation").equals("login")));
    }

    @Test
    void testGetSnapshotWithRecordedData() {
        // Given
        when(monitoringRegistry.getCollector(any(MetricKey.class))).thenReturn(metricsCollector);
        when(metricsCollector.executionSnapshot()).thenReturn(executionSnapshot);
        when(executionSnapshot.total()).thenReturn(50L);
        when(executionSnapshot.success()).thenReturn(45L);
        when(executionSnapshot.failure()).thenReturn(5L);
        when(executionSnapshot.totalDurationNanos()).thenReturn(2500_000_000L); // 2.5 seconds
        when(executionSnapshot.healthStatus())
                .thenReturn(org.openhab.core.ai.common.monitoring.api.Health.HealthStatus.HEALTHY);
        when(executionSnapshot.statusMessage()).thenReturn("Operations successful");
        when(executionSnapshot.consecutiveFailures()).thenReturn(0L);
        when(executionSnapshot.lastFailureTime()).thenReturn(0L);
        when(executionSnapshot.lastSuccessTime()).thenReturn(System.currentTimeMillis());
        when(executionSnapshot.lastError()).thenReturn("");

        // Mock recorded data from the collector
        Map<String, Object> recordedData = Map.of("input_tokens", 1500, "output_tokens", 500, "model_cost", 0.0025,
                "cache_hit", true, "response_time_ms", 1250L);
        when(metricsCollector.getData()).thenReturn(recordedData);

        // When
        GenericMetricsSnapshot snapshot = metricsService.getSnapshot("model", "completion");

        // Then
        assertNotNull(snapshot);
        assertEquals("model", snapshot.getDomain());
        assertEquals("completion", snapshot.getOperation());

        // Verify basic metrics
        assertEquals(50L, snapshot.getMetricAsLong("total_count"));
        assertEquals(45L, snapshot.getMetricAsLong("success_count"));
        assertEquals(5L, snapshot.getMetricAsLong("failure_count"));
        assertEquals(2500L, snapshot.getMetricAsLong("total_duration_ms"));
        assertEquals(50L, snapshot.getMetricAsLong("average_duration_ms")); // 2500ms / 50 operations
        assertEquals(0.9, snapshot.getMetricAsDouble("success_rate"), 0.001);
        assertEquals(0.1, snapshot.getMetricAsDouble("failure_rate"), 0.001);

        // Verify health metrics
        assertEquals("HEALTHY", snapshot.getMetricAsString("health_status"));
        assertEquals("Operations successful", snapshot.getMetricAsString("status_message"));
        assertEquals(0L, snapshot.getMetricAsLong("consecutive_failures"));

        // Verify recorded data is included
        assertEquals(1500, snapshot.getMetricAsLong("input_tokens"));
        assertEquals(500, snapshot.getMetricAsLong("output_tokens"));
        assertEquals(0.0025, snapshot.getMetricAsDouble("model_cost"), 0.0001);
        assertTrue(snapshot.getMetric("cache_hit") instanceof Boolean);
        assertTrue((Boolean) snapshot.getMetric("cache_hit"));
        assertEquals(1250L, snapshot.getMetricAsLong("response_time_ms"));
    }

    @Test
    void testGetSnapshotWithNoRecordedData() {
        // Given
        when(monitoringRegistry.getCollector(any(MetricKey.class))).thenReturn(metricsCollector);
        when(metricsCollector.executionSnapshot()).thenReturn(executionSnapshot);
        when(executionSnapshot.total()).thenReturn(10L);
        when(executionSnapshot.success()).thenReturn(10L);
        when(executionSnapshot.failure()).thenReturn(0L);
        when(executionSnapshot.totalDurationNanos()).thenReturn(500_000_000L); // 0.5 seconds
        when(executionSnapshot.healthStatus())
                .thenReturn(org.openhab.core.ai.common.monitoring.api.Health.HealthStatus.HEALTHY);
        when(executionSnapshot.statusMessage()).thenReturn("All operations successful");
        when(executionSnapshot.consecutiveFailures()).thenReturn(0L);
        when(executionSnapshot.lastFailureTime()).thenReturn(0L);
        when(executionSnapshot.lastSuccessTime()).thenReturn(System.currentTimeMillis());
        when(executionSnapshot.lastError()).thenReturn("");

        // Mock empty recorded data
        when(metricsCollector.getData()).thenReturn(Map.of());

        // When
        GenericMetricsSnapshot snapshot = metricsService.getSnapshot("tool", "file_read");

        // Then
        assertNotNull(snapshot);
        assertEquals("tool", snapshot.getDomain());
        assertEquals("file_read", snapshot.getOperation());

        // Verify basic metrics are present
        assertEquals(10L, snapshot.getMetricAsLong("total_count"));
        assertEquals(10L, snapshot.getMetricAsLong("success_count"));
        assertEquals(0L, snapshot.getMetricAsLong("failure_count"));
        assertEquals(500L, snapshot.getMetricAsLong("total_duration_ms"));
        assertEquals(1.0, snapshot.getMetricAsDouble("success_rate"), 0.001);

        // Verify health metrics are present
        assertEquals("HEALTHY", snapshot.getMetricAsString("health_status"));
        assertEquals("All operations successful", snapshot.getMetricAsString("status_message"));

        // Verify no recorded data metrics are present
        assertFalse(snapshot.hasMetric("input_tokens"));
        assertFalse(snapshot.hasMetric("output_tokens"));
        assertFalse(snapshot.hasMetric("model_cost"));
    }

    // ===== Phase 4: Advanced Features - Aggregation Tests =====

    @Test
    void testGetAggregatedSnapshots() {
        // Given
        MetricKey key1 = mock(MetricKey.class);
        MetricKey key2 = mock(MetricKey.class);
        when(monitoringRegistry.getKeys()).thenReturn(List.of(key1, key2));
        when(key1.kind()).thenReturn("test_domain");
        when(key2.kind()).thenReturn("test_domain");
        when(monitoringRegistry.getCollector(key1)).thenReturn(metricsCollector);
        when(monitoringRegistry.getCollector(key2)).thenReturn(metricsCollector);
        when(metricsCollector.executionSnapshot()).thenReturn(executionSnapshot);
        when(executionSnapshot.total()).thenReturn(50L);
        when(executionSnapshot.success()).thenReturn(45L);
        when(executionSnapshot.failure()).thenReturn(5L);
        when(executionSnapshot.totalDurationNanos()).thenReturn(1000_000_000L);

        // When
        List<GenericMetricsSnapshot> snapshots = metricsService.getAggregatedSnapshots("test_domain",
                GenericMetricsSnapshot.class);

        // Then
        assertNotNull(snapshots);
        assertEquals(2, snapshots.size());
        for (GenericMetricsSnapshot snapshot : snapshots) {
            assertEquals("test_domain", snapshot.getDomain());
            assertEquals(50L, snapshot.getMetricAsLong("total_count"));
            assertEquals(45L, snapshot.getMetricAsLong("success_count"));
            assertEquals(5L, snapshot.getMetricAsLong("failure_count"));
        }
    }

    @Test
    void testGetSnapshotsInRange() {
        // Given
        Instant start = Instant.now().minusSeconds(3600); // 1 hour ago
        Instant end = Instant.now();
        MetricKey key = mock(MetricKey.class);
        when(monitoringRegistry.getKeys()).thenReturn(List.of(key));
        when(key.kind()).thenReturn("test_domain");
        when(monitoringRegistry.getCollector(key)).thenReturn(metricsCollector);
        when(metricsCollector.executionSnapshot()).thenReturn(executionSnapshot);
        when(executionSnapshot.total()).thenReturn(25L);
        when(executionSnapshot.success()).thenReturn(20L);
        when(executionSnapshot.failure()).thenReturn(5L);
        when(executionSnapshot.totalDurationNanos()).thenReturn(500_000_000L);

        // When
        List<GenericMetricsSnapshot> snapshots = metricsService.getSnapshotsInRange(start, end,
                GenericMetricsSnapshot.class);

        // Then
        assertNotNull(snapshots);
        assertEquals(1, snapshots.size());
        GenericMetricsSnapshot snapshot = snapshots.get(0);
        assertEquals("test_domain", snapshot.getDomain());
        assertEquals(25L, snapshot.getMetricAsLong("total_count"));
        assertEquals(20L, snapshot.getMetricAsLong("success_count"));
        assertEquals(5L, snapshot.getMetricAsLong("failure_count"));
    }

    @Test
    void testGetDomainAggregatedSnapshot() {
        // Given
        MetricKey key1 = mock(MetricKey.class);
        MetricKey key2 = mock(MetricKey.class);
        when(monitoringRegistry.getKeys()).thenReturn(List.of(key1, key2));
        when(key1.kind()).thenReturn("test_domain");
        when(key2.kind()).thenReturn("test_domain");
        when(monitoringRegistry.getCollector(key1)).thenReturn(metricsCollector);
        when(monitoringRegistry.getCollector(key2)).thenReturn(metricsCollector);
        when(metricsCollector.executionSnapshot()).thenReturn(executionSnapshot);
        when(executionSnapshot.total()).thenReturn(30L);
        when(executionSnapshot.success()).thenReturn(25L);
        when(executionSnapshot.failure()).thenReturn(5L);
        when(executionSnapshot.totalDurationNanos()).thenReturn(1500_000_000L);

        // When
        DomainAggregatedSnapshot snapshot = metricsService.getDomainAggregatedSnapshot("test_domain");

        // Then
        assertNotNull(snapshot);
        assertEquals("test_domain", snapshot.getDomain());
        assertEquals(60L, snapshot.getTotalOperations()); // 30 + 30
        assertEquals(50L, snapshot.getSuccessfulOperations()); // 25 + 25
        assertEquals(10L, snapshot.getFailedOperations()); // 5 + 5
        assertEquals(3000L, snapshot.getTotalDurationMs()); // 1500 + 1500 in ms
        assertEquals(0.833, snapshot.getSuccessRate(), 0.001); // 50/60
    }

    @Test
    void testGetAggregatedSnapshotsWithNoRegistry() {
        // Given
        DefaultMetricsService serviceWithoutRegistry = new DefaultMetricsService();
        // Don't set the monitoring registry

        // When
        List<GenericMetricsSnapshot> snapshots = serviceWithoutRegistry.getAggregatedSnapshots("test_domain",
                GenericMetricsSnapshot.class);

        // Then
        assertNotNull(snapshots);
        assertTrue(snapshots.isEmpty());
    }

    @Test
    void testGetSnapshotsInRangeWithNoRegistry() {
        // Given
        DefaultMetricsService serviceWithoutRegistry = new DefaultMetricsService();
        Instant start = Instant.now().minusSeconds(3600);
        Instant end = Instant.now();

        // When
        List<GenericMetricsSnapshot> snapshots = serviceWithoutRegistry.getSnapshotsInRange(start, end,
                GenericMetricsSnapshot.class);

        // Then
        assertNotNull(snapshots);
        assertTrue(snapshots.isEmpty());
    }

    @Test
    void testGetDomainAggregatedSnapshotWithNoRegistry() {
        // Given
        DefaultMetricsService serviceWithoutRegistry = new DefaultMetricsService();

        // When
        DomainAggregatedSnapshot snapshot = serviceWithoutRegistry.getDomainAggregatedSnapshot("test_domain");

        // Then
        assertNotNull(snapshot);
        assertEquals("test_domain", snapshot.getDomain());
        assertEquals(0L, snapshot.getTotalOperations());
        assertEquals(0L, snapshot.getSuccessfulOperations());
        assertEquals(0L, snapshot.getFailedOperations());
        assertEquals(0L, snapshot.getTotalDurationMs());
        assertEquals(0.0, snapshot.getSuccessRate(), 0.001);
    }
}
