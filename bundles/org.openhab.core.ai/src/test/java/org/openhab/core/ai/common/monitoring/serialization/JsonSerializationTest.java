package org.openhab.core.ai.common.monitoring.serialization;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.ai.common.monitoring.collector.ExecutionMetricsCollector;
import org.openhab.core.ai.common.monitoring.collector.ProviderHealthCollector;
import org.openhab.core.ai.common.monitoring.export.RESTMetricsExporter;
import org.openhab.core.ai.common.monitoring.registry.DefaultMonitoringRegistry;
import org.openhab.core.ai.common.monitoring.snapshot.ExecutionMetricsSnapshot;
import org.openhab.core.ai.common.monitoring.snapshot.ProviderHealthSnapshot;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * JSON serialization tests for the monitoring system.
 * 
 * This test class validates JSON round-trip serialization for all
 * snapshot types and REST export functionality.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public class JsonSerializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testExecutionMetricsSnapshotSerialization() throws Exception {
        ExecutionMetricsCollector collector = new ExecutionMetricsCollector("serialization-test");

        // Add test data
        collector.recordExecution(true, 1000_000_000L);
        collector.recordExecution(false, 2000_000_000L);
        collector.recordExecution(true, 1500_000_000L);

        ExecutionMetricsSnapshot originalSnapshot = collector.snapshot();

        // Serialize to JSON
        String json = objectMapper.writeValueAsString(originalSnapshot);
        assertFalse(json.isEmpty());
        assertTrue(json.contains("\"total\":3"));
        assertTrue(json.contains("\"successful\":2"));
        assertTrue(json.contains("\"failed\":1"));

        System.out.println("ExecutionMetricsSnapshot JSON: " + json);

        // Deserialize from JSON
        ExecutionMetricsSnapshot deserializedSnapshot = objectMapper.readValue(json, ExecutionMetricsSnapshot.class);

        // Verify round-trip integrity
        assertEquals(originalSnapshot.total(), deserializedSnapshot.total());
        assertEquals(originalSnapshot.successful(), deserializedSnapshot.successful());
        assertEquals(originalSnapshot.failed(), deserializedSnapshot.failed());
        assertEquals(originalSnapshot.totalDurationNanos(), deserializedSnapshot.totalDurationNanos());
        assertEquals(originalSnapshot.timestampMs(), deserializedSnapshot.timestampMs());

        // Verify computed values are preserved/recalculated correctly
        assertEquals(originalSnapshot.successRatePercent(), deserializedSnapshot.successRatePercent(), 0.001);
        assertEquals(originalSnapshot.failureRatePercent(), deserializedSnapshot.failureRatePercent(), 0.001);
        assertEquals(originalSnapshot.averageLatencyMs(), deserializedSnapshot.averageLatencyMs(), 0.001);
    }

    @Test
    void testProviderHealthSnapshotSerialization() throws Exception {
        ProviderHealthCollector collector = new ProviderHealthCollector("health-serialization-test");

        // Add test data
        for (int i = 0; i < 10; i++) {
            if (i % 3 == 0) {
                collector.recordFailure("Test error " + i);
            } else {
                collector.recordSuccess();
            }
        }

        ProviderHealthSnapshot originalSnapshot = collector.snapshot();

        // Serialize to JSON
        String json = objectMapper.writeValueAsString(originalSnapshot);
        assertFalse(json.isEmpty());
        assertTrue(json.contains("\"totalOperations\":10"));
        assertTrue(json.contains("\"successfulOperations\":6"));
        assertTrue(json.contains("\"failedOperations\":4"));

        System.out.println("ProviderHealthSnapshot JSON: " + json);

        // Deserialize from JSON
        ProviderHealthSnapshot deserializedSnapshot = objectMapper.readValue(json, ProviderHealthSnapshot.class);

        // Verify round-trip integrity
        assertEquals(originalSnapshot.totalOperations(), deserializedSnapshot.totalOperations());
        assertEquals(originalSnapshot.successfulOperations(), deserializedSnapshot.successfulOperations());
        assertEquals(originalSnapshot.failedOperations(), deserializedSnapshot.failedOperations());
        assertEquals(originalSnapshot.successRate(), deserializedSnapshot.successRate(), 0.001);
        assertEquals(originalSnapshot.timestampMs(), deserializedSnapshot.timestampMs());

        // Verify computed health metrics
        assertEquals(originalSnapshot.uptimePercent(), deserializedSnapshot.uptimePercent(), 0.001);
        assertEquals(originalSnapshot.errorRatePercent(), deserializedSnapshot.errorRatePercent(), 0.001);
        assertEquals(originalSnapshot.getHealthScore(), deserializedSnapshot.getHealthScore(), 0.001);
    }

    @Test
    void testRESTExporterJsonOutput() {
        DefaultMonitoringRegistry registry = new DefaultMonitoringRegistry();
        RESTMetricsExporter exporter = new RESTMetricsExporter();

        // Use reflection to set the registry (since it's normally injected)
        try {
            var field = RESTMetricsExporter.class.getDeclaredField("monitoringRegistry");
            field.setAccessible(true);
            field.set(exporter, registry);
        } catch (Exception e) {
            fail("Could not inject registry for testing: " + e.getMessage());
        }

        // Add test data to registry
        var collector1 = registry
                .executionCollector(org.openhab.core.ai.common.monitoring.api.MetricKeys.provider("openai"));
        collector1.recordExecution(true, 1000_000_000L);
        collector1.recordExecution(false, 2000_000_000L);

        var collector2 = registry
                .executionCollector(org.openhab.core.ai.common.monitoring.api.MetricKeys.tool("weather"));
        collector2.recordExecution(true, 500_000_000L);

        // Test performance metrics export
        Map<String, Object> performanceData = exporter.getPerformanceMetrics();
        assertNotNull(performanceData);
        assertTrue(performanceData.containsKey("timestamp"));
        assertTrue(performanceData.containsKey("count"));
        assertTrue(performanceData.containsKey("metrics"));

        assertEquals(2, performanceData.get("count"));

        @SuppressWarnings("unchecked")
        var metrics = (java.util.List<Map<String, Object>>) performanceData.get("metrics");
        assertEquals(2, metrics.size());

        // Verify each metric has required fields
        for (Map<String, Object> metric : metrics) {
            assertTrue(metric.containsKey("id"));
            assertTrue(metric.containsKey("timestamp"));
            assertTrue(metric.containsKey("type"));
            assertTrue(metric.containsKey("domain"));
            assertTrue(metric.containsKey("source"));
            assertTrue(metric.containsKey("totalOperations"));
            assertTrue(metric.containsKey("successfulOperations"));
            assertTrue(metric.containsKey("failedOperations"));
        }
    }

    @Test
    void testPaginatedMetricsJsonOutput() {
        DefaultMonitoringRegistry registry = new DefaultMonitoringRegistry();
        RESTMetricsExporter exporter = new RESTMetricsExporter();

        // Use reflection to set the registry
        try {
            var field = RESTMetricsExporter.class.getDeclaredField("monitoringRegistry");
            field.setAccessible(true);
            field.set(exporter, registry);
        } catch (Exception e) {
            fail("Could not inject registry for testing: " + e.getMessage());
        }

        // Add multiple metrics for pagination testing
        for (int i = 0; i < 25; i++) {
            var collector = registry
                    .executionCollector(org.openhab.core.ai.common.monitoring.api.MetricKeys.provider("provider-" + i));
            collector.recordExecution(i % 2 == 0, i * 1000_000L);
        }

        // Test pagination
        Map<String, Object> page1 = exporter.getPaginatedMetrics(0, 10);
        Map<String, Object> page2 = exporter.getPaginatedMetrics(1, 10);
        Map<String, Object> page3 = exporter.getPaginatedMetrics(2, 10);

        // Verify pagination metadata
        assertEquals(0, page1.get("page"));
        assertEquals(10, page1.get("size"));
        assertEquals(25, page1.get("total"));
        assertEquals(3, page1.get("totalPages"));
        assertTrue((Boolean) page1.get("hasNext"));
        assertFalse((Boolean) page1.get("hasPrevious"));

        assertEquals(1, page2.get("page"));
        assertTrue((Boolean) page2.get("hasNext"));
        assertTrue((Boolean) page2.get("hasPrevious"));

        assertEquals(2, page3.get("page"));
        assertFalse((Boolean) page3.get("hasNext"));
        assertTrue((Boolean) page3.get("hasPrevious"));

        // Verify page content sizes
        @SuppressWarnings("unchecked")
        var page1Metrics = (java.util.List<Map<String, Object>>) page1.get("metrics");
        @SuppressWarnings("unchecked")
        var page2Metrics = (java.util.List<Map<String, Object>>) page2.get("metrics");
        @SuppressWarnings("unchecked")
        var page3Metrics = (java.util.List<Map<String, Object>>) page3.get("metrics");

        assertEquals(10, page1Metrics.size());
        assertEquals(10, page2Metrics.size());
        assertEquals(5, page3Metrics.size()); // Last page with remaining items
    }

    @Test
    void testMetricsSummaryJsonOutput() {
        DefaultMonitoringRegistry registry = new DefaultMonitoringRegistry();
        RESTMetricsExporter exporter = new RESTMetricsExporter();

        // Use reflection to set the registry
        try {
            var field = RESTMetricsExporter.class.getDeclaredField("monitoringRegistry");
            field.setAccessible(true);
            field.set(exporter, registry);
        } catch (Exception e) {
            fail("Could not inject registry for testing: " + e.getMessage());
        }

        // Add test data
        var collector = registry
                .executionCollector(org.openhab.core.ai.common.monitoring.api.MetricKeys.provider("test"));
        collector.recordExecution(true, 1000_000_000L);

        Map<String, Object> summary = exporter.getMetricsSummary();

        // Verify summary structure
        assertTrue(summary.containsKey("timestamp"));
        assertTrue(summary.containsKey("totalMetrics"));
        assertTrue(summary.containsKey("totalStatistics"));
        assertTrue(summary.containsKey("totalHealth"));
        assertTrue(summary.containsKey("activeCollectors"));
        assertTrue(summary.containsKey("systemStatus"));

        assertEquals(1, summary.get("totalMetrics"));
        assertEquals(0, summary.get("totalStatistics"));
        assertEquals(0, summary.get("totalHealth"));
        assertEquals(1, summary.get("activeCollectors"));
    }

    @Test
    void testJsonStructureConsistency() throws Exception {
        // Test that all exported JSON structures are consistent and parseable
        DefaultMonitoringRegistry registry = new DefaultMonitoringRegistry();
        RESTMetricsExporter exporter = new RESTMetricsExporter();

        // Use reflection to set the registry
        try {
            var field = RESTMetricsExporter.class.getDeclaredField("monitoringRegistry");
            field.setAccessible(true);
            field.set(exporter, registry);
        } catch (Exception e) {
            fail("Could not inject registry for testing: " + e.getMessage());
        }

        // Add test data
        var collector = registry
                .executionCollector(org.openhab.core.ai.common.monitoring.api.MetricKeys.tool("test-tool"));
        collector.recordExecution(true, 1000_000_000L);

        // Test all export methods produce valid JSON
        Map<String, Object> performance = exporter.getPerformanceMetrics();
        Map<String, Object> statistics = exporter.getStatisticsData();
        Map<String, Object> health = exporter.getHealthData();
        Map<String, Object> summary = exporter.getMetricsSummary();
        Map<String, Object> paginated = exporter.getPaginatedMetrics(0, 10);
        Map<String, Object> filtered = exporter.getMetricsByDomain("tool");

        // Convert all to JSON and verify they're parseable
        String[] jsonStrings = { objectMapper.writeValueAsString(performance),
                objectMapper.writeValueAsString(statistics), objectMapper.writeValueAsString(health),
                objectMapper.writeValueAsString(summary), objectMapper.writeValueAsString(paginated),
                objectMapper.writeValueAsString(filtered) };

        for (String json : jsonStrings) {
            assertFalse(json.isEmpty());
            // Verify it's valid JSON by parsing it back
            @SuppressWarnings("unchecked")
            Map<String, Object> parsed = objectMapper.readValue(json, Map.class);
            assertNotNull(parsed);
            assertTrue(parsed.containsKey("timestamp"));
        }
    }

    @Test
    void testEmptyStateJsonSerialization() throws Exception {
        // Test serialization of empty/zero-state objects
        ExecutionMetricsCollector emptyCollector = new ExecutionMetricsCollector("empty-test");
        ExecutionMetricsSnapshot emptySnapshot = emptyCollector.snapshot();

        String json = objectMapper.writeValueAsString(emptySnapshot);
        ExecutionMetricsSnapshot deserializedEmpty = objectMapper.readValue(json, ExecutionMetricsSnapshot.class);

        assertEquals(0, deserializedEmpty.total());
        assertEquals(0, deserializedEmpty.successful());
        assertEquals(0, deserializedEmpty.failed());
        assertEquals(0, deserializedEmpty.totalDurationNanos());

        // Verify computed values handle empty state correctly
        assertEquals(0.0, deserializedEmpty.operationsPerSecond());
        assertEquals(0.0, deserializedEmpty.failureRatePercent());
        assertEquals(100.0, deserializedEmpty.successRatePercent());
        assertEquals(0.0, deserializedEmpty.averageLatencyMs());
    }
}
