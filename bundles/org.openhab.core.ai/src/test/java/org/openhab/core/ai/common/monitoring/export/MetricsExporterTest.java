package org.openhab.core.ai.common.monitoring.export;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.ai.common.monitoring.api.Health;
import org.openhab.core.ai.common.monitoring.api.Metrics;
import org.openhab.core.ai.common.monitoring.api.MonitoringType;
import org.openhab.core.ai.common.monitoring.api.Statistics;
import org.openhab.core.ai.common.monitoring.registry.MetricsRegistry;

/**
 * Comprehensive unit tests for RESTMetricsExporter.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class MetricsExporterTest {

    @Mock
    private MetricsRegistry monitoringRegistry;

    @Mock
    private Metrics mockMetrics;

    @Mock
    private Statistics mockStatistics;

    @Mock
    private Health mockHealth;

    private RESTMetricsExporter exporter;

    @BeforeEach
    void setUp() {
        exporter = new RESTMetricsExporter();
        // Use reflection to inject the mock registry
        try {
            var field = RESTMetricsExporter.class.getDeclaredField("monitoringRegistry");
            field.setAccessible(true);
            field.set(exporter, monitoringRegistry);
        } catch (Exception e) {
            fail("Failed to inject mock registry: " + e.getMessage());
        }
    }

    @Test
    void testGetPerformanceMetricsWithValidRegistry() {
        // Given
        when(mockMetrics.getId()).thenReturn("test-metrics-1");
        when(mockMetrics.getTimestamp()).thenReturn(Instant.now());
        when(mockMetrics.getType()).thenReturn(MonitoringType.PERFORMANCE);
        when(mockMetrics.getDomain()).thenReturn("test-domain");
        when(mockMetrics.getSource()).thenReturn("test-source");
        when(mockMetrics.getTotalOperations()).thenReturn(100L);
        when(mockMetrics.getSuccessfulOperations()).thenReturn(90L);
        when(mockMetrics.getFailedOperations()).thenReturn(10L);
        when(mockMetrics.getTotalProcessingTime()).thenReturn(5000000000L);
        when(mockMetrics.getAverageResponseTime()).thenReturn(50.0);
        when(mockMetrics.getLastOperationTime()).thenReturn(Instant.now());
        when(mockMetrics.getData()).thenReturn(Map.of("key", "value"));

        when(monitoringRegistry.getMetricsSnapshots()).thenReturn(List.of(mockMetrics));

        // When
        Map<String, Object> result = exporter.getPerformanceMetrics();

        // Then
        assertNotNull(result);
        assertEquals("timestamp", result.keySet().toArray()[0]);
        assertEquals("count", result.keySet().toArray()[1]);
        assertEquals("metrics", result.keySet().toArray()[2]);
        assertEquals(1, result.get("count"));
        assertTrue(result.get("timestamp") instanceof Long);
        assertTrue(result.get("metrics") instanceof List);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> metricsList = (List<Map<String, Object>>) result.get("metrics");
        assertEquals(1, metricsList.size());
        assertEquals("test-metrics-1", metricsList.get(0).get("id"));
        assertEquals("test-domain", metricsList.get(0).get("domain"));
        assertEquals("test-source", metricsList.get(0).get("source"));
        assertEquals(100L, metricsList.get(0).get("totalOperations"));
        assertEquals(90L, metricsList.get(0).get("successfulOperations"));
        assertEquals(10L, metricsList.get(0).get("failedOperations"));
        assertEquals(5000000000L, metricsList.get(0).get("totalProcessingTime"));
        assertEquals(50.0, metricsList.get(0).get("averageResponseTime"));
        assertEquals(Map.of("key", "value"), metricsList.get(0).get("data"));
    }

    @Test
    void testGetPerformanceMetricsWithNullRegistry() {
        // Given
        RESTMetricsExporter nullExporter = new RESTMetricsExporter();

        // When
        Map<String, Object> result = nullExporter.getPerformanceMetrics();

        // Then
        assertNotNull(result);
        assertEquals("error", result.keySet().toArray()[0]);
        assertEquals("Monitoring registry not available", result.get("error"));
    }

    @Test
    void testGetPerformanceMetricsWithEmptyList() {
        // Given
        when(monitoringRegistry.getMetricsSnapshots()).thenReturn(List.of());

        // When
        Map<String, Object> result = exporter.getPerformanceMetrics();

        // Then
        assertNotNull(result);
        assertEquals(0, result.get("count"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> metricsList = (List<Map<String, Object>>) result.get("metrics");
        assertTrue(metricsList.isEmpty());
    }

    @Test
    void testGetStatisticsDataWithValidRegistry() {
        // Given
        when(mockStatistics.getId()).thenReturn("test-stats-1");
        when(mockStatistics.getTimestamp()).thenReturn(Instant.now());
        when(mockStatistics.getType()).thenReturn(MonitoringType.STATISTICS);
        when(mockStatistics.getDomain()).thenReturn("test-domain");
        when(mockStatistics.getSource()).thenReturn("test-source");
        when(mockStatistics.getTotalCount()).thenReturn(200L);
        when(mockStatistics.getSuccessCount()).thenReturn(180L);
        when(mockStatistics.getFailureCount()).thenReturn(20L);
        when(mockStatistics.getSuccessRate()).thenReturn(90.0);
        when(mockStatistics.getData()).thenReturn(Map.of("key", "value"));

        when(monitoringRegistry.getStatisticsSnapshots()).thenReturn(List.of(mockStatistics));

        // When
        Map<String, Object> result = exporter.getStatisticsData();

        // Then
        assertNotNull(result);
        assertEquals("timestamp", result.keySet().toArray()[0]);
        assertEquals("count", result.keySet().toArray()[1]);
        assertEquals("statistics", result.keySet().toArray()[2]);
        assertEquals(1, result.get("count"));
        assertTrue(result.get("timestamp") instanceof Long);
        assertTrue(result.get("statistics") instanceof List);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> statsList = (List<Map<String, Object>>) result.get("statistics");
        assertEquals(1, statsList.size());
        assertEquals("test-stats-1", statsList.get(0).get("id"));
        assertEquals("test-domain", statsList.get(0).get("domain"));
        assertEquals("test-source", statsList.get(0).get("source"));
        assertEquals(200L, statsList.get(0).get("totalCount"));
        assertEquals(180L, statsList.get(0).get("successCount"));
        assertEquals(20L, statsList.get(0).get("failureCount"));
        assertEquals(90.0, statsList.get(0).get("successRate"));
        assertEquals(Map.of("key", "value"), statsList.get(0).get("data"));
    }

    @Test
    void testGetStatisticsDataWithNullRegistry() {
        // Given
        RESTMetricsExporter nullExporter = new RESTMetricsExporter();

        // When
        Map<String, Object> result = nullExporter.getStatisticsData();

        // Then
        assertNotNull(result);
        assertEquals("error", result.keySet().toArray()[0]);
        assertEquals("Monitoring registry not available", result.get("error"));
    }

    @Test
    void testGetHealthDataWithValidRegistry() {
        // Given
        when(mockHealth.getId()).thenReturn("test-health-1");
        when(mockHealth.getTimestamp()).thenReturn(Instant.now());
        when(mockHealth.getType()).thenReturn(MonitoringType.HEALTH);
        when(mockHealth.getDomain()).thenReturn("test-domain");
        when(mockHealth.getSource()).thenReturn("test-source");
        when(mockHealth.getStatus()).thenReturn(Health.HealthStatus.HEALTHY);
        when(mockHealth.getStatusMessage()).thenReturn("All systems operational");
        when(mockHealth.getHealthIndicators()).thenReturn(Map.of("cpu", 0.5, "memory", 0.3));
        when(mockHealth.getData()).thenReturn(Map.of("key", "value"));

        when(monitoringRegistry.getHealthSnapshots()).thenReturn(List.of(mockHealth));

        // When
        Map<String, Object> result = exporter.getHealthData();

        // Then
        assertNotNull(result);
        assertEquals("timestamp", result.keySet().toArray()[0]);
        assertEquals("count", result.keySet().toArray()[1]);
        assertEquals("health", result.keySet().toArray()[2]);
        assertEquals(1, result.get("count"));
        assertTrue(result.get("timestamp") instanceof Long);
        assertTrue(result.get("health") instanceof List);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> healthList = (List<Map<String, Object>>) result.get("health");
        assertEquals(1, healthList.size());
        assertEquals("test-health-1", healthList.get(0).get("id"));
        assertEquals("test-domain", healthList.get(0).get("domain"));
        assertEquals("test-source", healthList.get(0).get("source"));
        assertEquals("HEALTHY", healthList.get(0).get("status"));
        assertEquals("All systems operational", healthList.get(0).get("statusMessage"));
        assertEquals(Map.of("cpu", 0.5, "memory", 0.3), healthList.get(0).get("healthIndicators"));
        assertEquals(Map.of("key", "value"), healthList.get(0).get("data"));
    }

    @Test
    void testGetHealthDataWithNullRegistry() {
        // Given
        RESTMetricsExporter nullExporter = new RESTMetricsExporter();

        // When
        Map<String, Object> result = nullExporter.getHealthData();

        // Then
        assertNotNull(result);
        assertEquals("error", result.keySet().toArray()[0]);
        assertEquals("Monitoring registry not available", result.get("error"));
    }

    @Test
    void testGetMetricsSummaryWithValidRegistry() {
        // Given
        when(mockMetrics.getId()).thenReturn("test-metrics-1");
        when(mockMetrics.getTimestamp()).thenReturn(Instant.now());
        when(mockMetrics.getType()).thenReturn(MonitoringType.PERFORMANCE);
        when(mockMetrics.getDomain()).thenReturn("test-domain");
        when(mockMetrics.getSource()).thenReturn("test-source");
        when(mockMetrics.getTotalOperations()).thenReturn(100L);
        when(mockMetrics.getSuccessfulOperations()).thenReturn(90L);
        when(mockMetrics.getFailedOperations()).thenReturn(10L);
        when(mockMetrics.getTotalProcessingTime()).thenReturn(5000000000L);
        when(mockMetrics.getAverageResponseTime()).thenReturn(50.0);
        when(mockMetrics.getLastOperationTime()).thenReturn(Instant.now());
        when(mockMetrics.getData()).thenReturn(Map.of("key", "value"));

        when(mockStatistics.getId()).thenReturn("test-stats-1");
        when(mockStatistics.getTimestamp()).thenReturn(Instant.now());
        when(mockStatistics.getType()).thenReturn(MonitoringType.STATISTICS);
        when(mockStatistics.getDomain()).thenReturn("test-domain");
        when(mockStatistics.getSource()).thenReturn("test-source");
        when(mockStatistics.getTotalCount()).thenReturn(200L);
        when(mockStatistics.getSuccessCount()).thenReturn(180L);
        when(mockStatistics.getFailureCount()).thenReturn(20L);
        when(mockStatistics.getSuccessRate()).thenReturn(90.0);
        when(mockStatistics.getData()).thenReturn(Map.of("key", "value"));

        when(mockHealth.getId()).thenReturn("test-health-1");
        when(mockHealth.getTimestamp()).thenReturn(Instant.now());
        when(mockHealth.getType()).thenReturn(MonitoringType.HEALTH);
        when(mockHealth.getDomain()).thenReturn("test-domain");
        when(mockHealth.getSource()).thenReturn("test-source");
        when(mockHealth.getStatus()).thenReturn(Health.HealthStatus.HEALTHY);
        when(mockHealth.getStatusMessage()).thenReturn("All systems operational");
        when(mockHealth.getHealthIndicators()).thenReturn(Map.of("cpu", 0.5, "memory", 0.3));
        when(mockHealth.getData()).thenReturn(Map.of("key", "value"));

        when(monitoringRegistry.getMetricsSnapshots()).thenReturn(List.of(mockMetrics));
        when(monitoringRegistry.getStatisticsSnapshots()).thenReturn(List.of(mockStatistics));
        when(monitoringRegistry.getHealthSnapshots()).thenReturn(List.of(mockHealth));
        when(monitoringRegistry.getAllKeys()).thenReturn(List.of());

        // When
        Map<String, Object> result = exporter.getMetricsSummary();

        // Then
        assertNotNull(result);
        assertEquals("timestamp", result.keySet().toArray()[0]);
        assertEquals("totalMetrics", result.keySet().toArray()[1]);
        assertEquals("totalStatistics", result.keySet().toArray()[2]);
        assertEquals("totalHealth", result.keySet().toArray()[3]);
        assertEquals("activeCollectors", result.keySet().toArray()[4]);
        assertEquals("systemStatus", result.keySet().toArray()[5]);
        assertEquals(1, result.get("totalMetrics"));
        assertEquals(1, result.get("totalStatistics"));
        assertEquals(1, result.get("totalHealth"));
        assertEquals(0, result.get("activeCollectors"));
        assertEquals("HEALTHY", result.get("systemStatus"));
        assertTrue(result.get("timestamp") instanceof Long);
    }

    @Test
    void testGetMetricsSummaryWithNullRegistry() {
        // Given
        RESTMetricsExporter nullExporter = new RESTMetricsExporter();

        // When
        Map<String, Object> result = nullExporter.getMetricsSummary();

        // Then
        assertNotNull(result);
        assertEquals("error", result.keySet().toArray()[0]);
        assertEquals("Monitoring registry not available", result.get("error"));
    }

    @Test
    void testGetMetricsSummaryWithEmptyData() {
        // Given
        when(monitoringRegistry.getMetricsSnapshots()).thenReturn(List.of());
        when(monitoringRegistry.getStatisticsSnapshots()).thenReturn(List.of());
        when(monitoringRegistry.getHealthSnapshots()).thenReturn(List.of());
        when(monitoringRegistry.getAllKeys()).thenReturn(List.of());

        // When
        Map<String, Object> result = exporter.getMetricsSummary();

        // Then
        assertNotNull(result);
        assertEquals(0, result.get("totalMetrics"));
        assertEquals(0, result.get("totalStatistics"));
        assertEquals(0, result.get("totalHealth"));
        assertEquals(0, result.get("activeCollectors"));
        assertEquals("UNKNOWN", result.get("systemStatus"));
    }

    @Test
    void testGetMetricsByDomainWithValidRegistry() {
        // Given
        when(mockMetrics.getId()).thenReturn("test-metrics-1");
        when(mockMetrics.getTimestamp()).thenReturn(Instant.now());
        when(mockMetrics.getType()).thenReturn(MonitoringType.PERFORMANCE);
        when(mockMetrics.getDomain()).thenReturn("test-domain");
        when(mockMetrics.getSource()).thenReturn("test-source");
        when(mockMetrics.getTotalOperations()).thenReturn(100L);
        when(mockMetrics.getSuccessfulOperations()).thenReturn(90L);
        when(mockMetrics.getFailedOperations()).thenReturn(10L);
        when(mockMetrics.getTotalProcessingTime()).thenReturn(5000000000L);
        when(mockMetrics.getAverageResponseTime()).thenReturn(50.0);
        when(mockMetrics.getLastOperationTime()).thenReturn(Instant.now());
        when(mockMetrics.getData()).thenReturn(Map.of("key", "value"));

        when(monitoringRegistry.getMetricsSnapshots()).thenReturn(List.of(mockMetrics));

        // When
        Map<String, Object> result = exporter.getMetricsByDomain("test-domain");

        // Then
        assertNotNull(result);
        assertEquals("timestamp", result.keySet().toArray()[0]);
        assertEquals("domain", result.keySet().toArray()[1]);
        assertEquals("count", result.keySet().toArray()[2]);
        assertEquals("metrics", result.keySet().toArray()[3]);
        assertEquals("test-domain", result.get("domain"));
        assertEquals(1, result.get("count"));
        assertTrue(result.get("timestamp") instanceof Long);
        assertTrue(result.get("metrics") instanceof List);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> metricsList = (List<Map<String, Object>>) result.get("metrics");
        assertEquals(1, metricsList.size());
        assertEquals("test-metrics-1", metricsList.get(0).get("id"));
    }

    @Test
    void testGetMetricsByDomainWithNoMatch() {
        // Given
        when(mockMetrics.getId()).thenReturn("test-metrics-1");
        when(mockMetrics.getDomain()).thenReturn("other-domain");
        when(mockMetrics.getTimestamp()).thenReturn(Instant.now());
        when(mockMetrics.getType()).thenReturn(MonitoringType.PERFORMANCE);
        when(mockMetrics.getSource()).thenReturn("test-source");
        when(mockMetrics.getTotalOperations()).thenReturn(100L);
        when(mockMetrics.getSuccessfulOperations()).thenReturn(90L);
        when(mockMetrics.getFailedOperations()).thenReturn(10L);
        when(mockMetrics.getTotalProcessingTime()).thenReturn(5000000000L);
        when(mockMetrics.getAverageResponseTime()).thenReturn(50.0);
        when(mockMetrics.getLastOperationTime()).thenReturn(Instant.now());
        when(mockMetrics.getData()).thenReturn(Map.of("key", "value"));

        when(monitoringRegistry.getMetricsSnapshots()).thenReturn(List.of(mockMetrics));

        // When
        Map<String, Object> result = exporter.getMetricsByDomain("test-domain");

        // Then
        assertNotNull(result);
        assertEquals("test-domain", result.get("domain"));
        assertEquals(0, result.get("count"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> metricsList = (List<Map<String, Object>>) result.get("metrics");
        assertTrue(metricsList.isEmpty());
    }

    @Test
    void testGetMetricsByDomainWithNullRegistry() {
        // Given
        RESTMetricsExporter nullExporter = new RESTMetricsExporter();

        // When
        Map<String, Object> result = nullExporter.getMetricsByDomain("test-domain");

        // Then
        assertNotNull(result);
        assertEquals("error", result.keySet().toArray()[0]);
        assertEquals("Monitoring registry not available", result.get("error"));
    }

    @Test
    void testGetPaginatedMetricsWithValidRegistry() {
        // Given
        when(mockMetrics.getId()).thenReturn("test-metrics-1");
        when(mockMetrics.getTimestamp()).thenReturn(Instant.now());
        when(mockMetrics.getType()).thenReturn(MonitoringType.PERFORMANCE);
        when(mockMetrics.getDomain()).thenReturn("test-domain");
        when(mockMetrics.getSource()).thenReturn("test-source");
        when(mockMetrics.getTotalOperations()).thenReturn(100L);
        when(mockMetrics.getSuccessfulOperations()).thenReturn(90L);
        when(mockMetrics.getFailedOperations()).thenReturn(10L);
        when(mockMetrics.getTotalProcessingTime()).thenReturn(5000000000L);
        when(mockMetrics.getAverageResponseTime()).thenReturn(50.0);
        when(mockMetrics.getLastOperationTime()).thenReturn(Instant.now());
        when(mockMetrics.getData()).thenReturn(Map.of("key", "value"));

        when(monitoringRegistry.getMetricsSnapshots()).thenReturn(List.of(mockMetrics));

        // When
        Map<String, Object> result = exporter.getPaginatedMetrics(0, 10);

        // Then
        assertNotNull(result);
        assertEquals("timestamp", result.keySet().toArray()[0]);
        assertEquals("page", result.keySet().toArray()[1]);
        assertEquals("size", result.keySet().toArray()[2]);
        assertEquals("total", result.keySet().toArray()[3]);
        assertEquals("totalPages", result.keySet().toArray()[4]);
        assertEquals("hasNext", result.keySet().toArray()[5]);
        assertEquals("hasPrevious", result.keySet().toArray()[6]);
        assertEquals("metrics", result.keySet().toArray()[7]);
        assertEquals(0, result.get("page"));
        assertEquals(10, result.get("size"));
        assertEquals(1, result.get("total"));
        assertEquals(1, result.get("totalPages"));
        assertEquals(false, result.get("hasNext"));
        assertEquals(false, result.get("hasPrevious"));
        assertTrue(result.get("timestamp") instanceof Long);
        assertTrue(result.get("metrics") instanceof List);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> metricsList = (List<Map<String, Object>>) result.get("metrics");
        assertEquals(1, metricsList.size());
        assertEquals("test-metrics-1", metricsList.get(0).get("id"));
    }

    @Test
    void testGetPaginatedMetricsWithMultiplePages() {
        // Given
        when(mockMetrics.getId()).thenReturn("test-metrics-1");
        when(mockMetrics.getTimestamp()).thenReturn(Instant.now());
        when(mockMetrics.getType()).thenReturn(MonitoringType.PERFORMANCE);
        when(mockMetrics.getDomain()).thenReturn("test-domain");
        when(mockMetrics.getSource()).thenReturn("test-source");
        when(mockMetrics.getTotalOperations()).thenReturn(100L);
        when(mockMetrics.getSuccessfulOperations()).thenReturn(90L);
        when(mockMetrics.getFailedOperations()).thenReturn(10L);
        when(mockMetrics.getTotalProcessingTime()).thenReturn(5000000000L);
        when(mockMetrics.getAverageResponseTime()).thenReturn(50.0);
        when(mockMetrics.getLastOperationTime()).thenReturn(Instant.now());
        when(mockMetrics.getData()).thenReturn(Map.of("key", "value"));

        when(monitoringRegistry.getMetricsSnapshots()).thenReturn(List.of(mockMetrics, mockMetrics, mockMetrics));

        // When
        Map<String, Object> result = exporter.getPaginatedMetrics(0, 2);

        // Then
        assertNotNull(result);
        assertEquals(0, result.get("page"));
        assertEquals(2, result.get("size"));
        assertEquals(3, result.get("total"));
        assertEquals(2, result.get("totalPages"));
        assertEquals(true, result.get("hasNext"));
        assertEquals(false, result.get("hasPrevious"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> metricsList = (List<Map<String, Object>>) result.get("metrics");
        assertEquals(2, metricsList.size());
    }

    @Test
    void testGetPaginatedMetricsWithNullRegistry() {
        // Given
        RESTMetricsExporter nullExporter = new RESTMetricsExporter();

        // When
        Map<String, Object> result = nullExporter.getPaginatedMetrics(0, 10);

        // Then
        assertNotNull(result);
        assertEquals("error", result.keySet().toArray()[0]);
        assertEquals("Monitoring registry not available", result.get("error"));
    }

    @Test
    void testCalculateSystemStatusWithHealthyData() {
        // Given
        when(mockHealth.getStatus()).thenReturn(Health.HealthStatus.HEALTHY);
        when(monitoringRegistry.getHealthSnapshots()).thenReturn(List.of(mockHealth, mockHealth, mockHealth));

        // When
        Map<String, Object> result = exporter.getMetricsSummary();

        // Then
        assertEquals("HEALTHY", result.get("systemStatus"));
    }

    @Test
    void testCalculateSystemStatusWithDegradedData() {
        // Given
        when(mockHealth.getStatus()).thenReturn(Health.HealthStatus.DEGRADED);
        when(monitoringRegistry.getHealthSnapshots()).thenReturn(List.of(mockHealth, mockHealth, mockHealth));

        // When
        Map<String, Object> result = exporter.getMetricsSummary();

        // Then
        assertEquals("DEGRADED", result.get("systemStatus"));
    }

    @Test
    void testCalculateSystemStatusWithMixedData() {
        // Given
        when(mockHealth.getStatus()).thenReturn(Health.HealthStatus.HEALTHY);
        when(monitoringRegistry.getHealthSnapshots()).thenReturn(List.of(mockHealth, mockHealth, mockHealth, mockHealth, mockHealth));

        // When
        Map<String, Object> result = exporter.getMetricsSummary();

        // Then
        assertEquals("HEALTHY", result.get("systemStatus"));
    }

    @Test
    void testCalculateSystemStatusWithEmptyData() {
        // Given
        when(monitoringRegistry.getHealthSnapshots()).thenReturn(List.of());

        // When
        Map<String, Object> result = exporter.getMetricsSummary();

        // Then
        assertEquals("UNKNOWN", result.get("systemStatus"));
    }

    @Test
    void testClearCache() {
        // Given
        when(mockMetrics.getId()).thenReturn("test-metrics-1");
        when(mockMetrics.getTimestamp()).thenReturn(Instant.now());
        when(mockMetrics.getType()).thenReturn(MonitoringType.PERFORMANCE);
        when(mockMetrics.getDomain()).thenReturn("test-domain");
        when(mockMetrics.getSource()).thenReturn("test-source");
        when(mockMetrics.getTotalOperations()).thenReturn(100L);
        when(mockMetrics.getSuccessfulOperations()).thenReturn(90L);
        when(mockMetrics.getFailedOperations()).thenReturn(10L);
        when(mockMetrics.getTotalProcessingTime()).thenReturn(5000000000L);
        when(mockMetrics.getAverageResponseTime()).thenReturn(50.0);
        when(mockMetrics.getLastOperationTime()).thenReturn(Instant.now());
        when(mockMetrics.getData()).thenReturn(Map.of("key", "value"));

        when(mockStatistics.getId()).thenReturn("test-stats-1");
        when(mockStatistics.getTimestamp()).thenReturn(Instant.now());
        when(mockStatistics.getType()).thenReturn(MonitoringType.STATISTICS);
        when(mockStatistics.getDomain()).thenReturn("test-domain");
        when(mockStatistics.getSource()).thenReturn("test-source");
        when(mockStatistics.getTotalCount()).thenReturn(200L);
        when(mockStatistics.getSuccessCount()).thenReturn(180L);
        when(mockStatistics.getFailureCount()).thenReturn(20L);
        when(mockStatistics.getSuccessRate()).thenReturn(90.0);
        when(mockStatistics.getData()).thenReturn(Map.of("key", "value"));

        when(mockHealth.getId()).thenReturn("test-health-1");
        when(mockHealth.getTimestamp()).thenReturn(Instant.now());
        when(mockHealth.getType()).thenReturn(MonitoringType.HEALTH);
        when(mockHealth.getDomain()).thenReturn("test-domain");
        when(mockHealth.getSource()).thenReturn("test-source");
        when(mockHealth.getStatus()).thenReturn(Health.HealthStatus.HEALTHY);
        when(mockHealth.getStatusMessage()).thenReturn("All systems operational");
        when(mockHealth.getHealthIndicators()).thenReturn(Map.of("cpu", 0.5, "memory", 0.3));
        when(mockHealth.getData()).thenReturn(Map.of("key", "value"));

        when(monitoringRegistry.getMetricsSnapshots()).thenReturn(List.of(mockMetrics));
        when(monitoringRegistry.getStatisticsSnapshots()).thenReturn(List.of(mockStatistics));
        when(monitoringRegistry.getHealthSnapshots()).thenReturn(List.of(mockHealth));
        when(monitoringRegistry.getAllKeys()).thenReturn(List.of());

        // When - First call to populate cache
        Map<String, Object> result1 = exporter.getMetricsSummary();
        assertNotNull(result1);

        // Clear cache
        exporter.clearCache();

        // Second call should work the same
        Map<String, Object> result2 = exporter.getMetricsSummary();
        assertNotNull(result2);

        // Then - Both results should be identical
        assertEquals(result1, result2);
    }

    @Test
    void testThreadSafety() {
        // Given
        when(mockMetrics.getId()).thenReturn("test-metrics-1");
        when(mockMetrics.getTimestamp()).thenReturn(Instant.now());
        when(mockMetrics.getType()).thenReturn(MonitoringType.PERFORMANCE);
        when(mockMetrics.getDomain()).thenReturn("test-domain");
        when(mockMetrics.getSource()).thenReturn("test-source");
        when(mockMetrics.getTotalOperations()).thenReturn(100L);
        when(mockMetrics.getSuccessfulOperations()).thenReturn(90L);
        when(mockMetrics.getFailedOperations()).thenReturn(10L);
        when(mockMetrics.getTotalProcessingTime()).thenReturn(5000000000L);
        when(mockMetrics.getAverageResponseTime()).thenReturn(50.0);
        when(mockMetrics.getLastOperationTime()).thenReturn(Instant.now());
        when(mockMetrics.getData()).thenReturn(Map.of("key", "value"));

        when(monitoringRegistry.getMetricsSnapshots()).thenReturn(List.of(mockMetrics));
        when(monitoringRegistry.getStatisticsSnapshots()).thenReturn(List.of());
        when(monitoringRegistry.getHealthSnapshots()).thenReturn(List.of());
        when(monitoringRegistry.getAllKeys()).thenReturn(List.of());

        // When & Then - Should be thread-safe
        assertDoesNotThrow(() -> {
            Thread[] threads = new Thread[10];
            for (int i = 0; i < threads.length; i++) {
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < 100; j++) {
                        exporter.getPerformanceMetrics();
                        exporter.getStatisticsData();
                        exporter.getHealthData();
                        exporter.getMetricsSummary();
                        exporter.getMetricsByDomain("test-domain");
                        exporter.getPaginatedMetrics(0, 10);
                    }
                });
            }

            for (Thread thread : threads) {
                thread.start();
            }

            for (Thread thread : threads) {
                thread.join();
            }
        });
    }

    @Test
    void testEdgeCases() {
        // Given
        when(mockMetrics.getId()).thenReturn("test-metrics-1");
        when(mockMetrics.getTimestamp()).thenReturn(Instant.now());
        when(mockMetrics.getType()).thenReturn(MonitoringType.PERFORMANCE);
        when(mockMetrics.getDomain()).thenReturn("test-domain");
        when(mockMetrics.getSource()).thenReturn("test-source");
        when(mockMetrics.getTotalOperations()).thenReturn(0L);
        when(mockMetrics.getSuccessfulOperations()).thenReturn(0L);
        when(mockMetrics.getFailedOperations()).thenReturn(0L);
        when(mockMetrics.getTotalProcessingTime()).thenReturn(0L);
        when(mockMetrics.getAverageResponseTime()).thenReturn(0.0);
        when(mockMetrics.getLastOperationTime()).thenReturn(Instant.now());
        when(mockMetrics.getData()).thenReturn(Map.of());

        when(monitoringRegistry.getMetricsSnapshots()).thenReturn(List.of(mockMetrics));

        // When
        Map<String, Object> result = exporter.getPerformanceMetrics();

        // Then
        assertNotNull(result);
        assertEquals(1, result.get("count"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> metricsList = (List<Map<String, Object>>) result.get("metrics");
        assertEquals(1, metricsList.size());
        assertEquals(0L, metricsList.get(0).get("totalOperations"));
        assertEquals(0L, metricsList.get(0).get("successfulOperations"));
        assertEquals(0L, metricsList.get(0).get("failedOperations"));
        assertEquals(0L, metricsList.get(0).get("totalProcessingTime"));
        assertEquals(0.0, metricsList.get(0).get("averageResponseTime"));
        assertEquals(Map.of(), metricsList.get(0).get("data"));
    }

    @Test
    void testNullHandling() {
        // Given
        when(mockMetrics.getId()).thenReturn(null);
        when(mockMetrics.getTimestamp()).thenReturn(null);
        when(mockMetrics.getType()).thenReturn(null);
        when(mockMetrics.getDomain()).thenReturn(null);
        when(mockMetrics.getSource()).thenReturn(null);
        when(mockMetrics.getTotalOperations()).thenReturn(100L);
        when(mockMetrics.getSuccessfulOperations()).thenReturn(90L);
        when(mockMetrics.getFailedOperations()).thenReturn(10L);
        when(mockMetrics.getTotalProcessingTime()).thenReturn(5000000000L);
        when(mockMetrics.getAverageResponseTime()).thenReturn(50.0);
        when(mockMetrics.getLastOperationTime()).thenReturn(null);
        when(mockMetrics.getData()).thenReturn(null);

        when(monitoringRegistry.getMetricsSnapshots()).thenReturn(List.of(mockMetrics));

        // When
        Map<String, Object> result = exporter.getPerformanceMetrics();

        // Then
        assertNotNull(result);
        assertEquals(1, result.get("count"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> metricsList = (List<Map<String, Object>>) result.get("metrics");
        assertEquals(1, metricsList.size());
        assertNull(metricsList.get(0).get("id"));
        assertNull(metricsList.get(0).get("domain"));
        assertNull(metricsList.get(0).get("source"));
        assertNull(metricsList.get(0).get("lastOperationTime"));
        assertNull(metricsList.get(0).get("data"));
    }
}
