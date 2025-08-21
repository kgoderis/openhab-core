package org.openhab.core.ai.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.openhab.core.ai.common.monitoring.api.MetricKeys;
import org.openhab.core.ai.common.monitoring.collector.ExecutionMetricsCollector;
import org.openhab.core.ai.common.monitoring.collector.ProviderHealthCollector;
import org.openhab.core.ai.common.monitoring.export.RESTMetricsExporter;
import org.openhab.core.ai.common.monitoring.health.MetricsCircuitBreaker;
import org.openhab.core.ai.common.monitoring.health.MetricsHealthMonitor;
import org.openhab.core.ai.common.monitoring.registry.DefaultMonitoringRegistry;
import org.openhab.core.ai.common.monitoring.snapshot.ExecutionMetricsSnapshot;
import org.openhab.core.ai.common.monitoring.snapshot.ProviderHealthSnapshot;
import org.openhab.core.ai.tool.services.HybridToolExecutionService;

/**
 * Comprehensive integration test for the monitoring system.
 * 
 * This test validates the end-to-end functionality of the monitoring system,
 * including collectors, snapshots, registry, exporters, and health monitoring.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public class MonitoringSystemIntegrationTest {

    private DefaultMonitoringRegistry registry;
    private RESTMetricsExporter exporter;
    private MetricsHealthMonitor healthMonitor;
    private MetricsCircuitBreaker circuitBreaker;
    private HybridToolExecutionService toolService;

    @BeforeEach
    void setUp() {
        registry = new DefaultMonitoringRegistry();
        exporter = new RESTMetricsExporter(registry);
        healthMonitor = new MetricsHealthMonitor(registry);
        circuitBreaker = MetricsCircuitBreaker.builder().failureThreshold(3).timeoutMs(1000).build();
        toolService = new HybridToolExecutionService();
        toolService.setMonitoringRegistry(registry);
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testEndToEndMonitoringWorkflow() {
        // 1. Record metrics using collectors
        ExecutionMetricsCollector executionCollector = registry.executionCollector(MetricKeys.action("test-action"));
        ProviderHealthCollector healthCollector = registry.healthCollector(MetricKeys.provider("test-provider"));

        // Record some execution metrics
        executionCollector.recordExecution(true, 100_000_000L); // 100ms in nanoseconds
        executionCollector.recordExecution(false, 50_000_000L); // 50ms in nanoseconds
        executionCollector.recordExecution(true, 200_000_000L); // 200ms in nanoseconds

        // Record some health metrics
        healthCollector.recordHealthCheck(true, 10_000_000L); // 10ms in nanoseconds
        healthCollector.recordHealthCheck(false, 5_000_000L); // 5ms in nanoseconds

        // 2. Retrieve snapshots
        List<ExecutionMetricsSnapshot> executionSnapshots = registry.getExecutionSnapshots();
        List<ProviderHealthSnapshot> healthSnapshots = registry.getHealthSnapshots();

        // 3. Validate snapshots
        assertFalse(executionSnapshots.isEmpty(), "Should have execution snapshots");
        assertFalse(healthSnapshots.isEmpty(), "Should have health snapshots");

        ExecutionMetricsSnapshot executionSnapshot = executionSnapshots.get(0);
        assertEquals(3, executionSnapshot.total(), "Should have 3 total executions");
        assertEquals(2, executionSnapshot.success(), "Should have 2 successful executions");
        assertEquals(1, executionSnapshot.failure(), "Should have 1 failed execution");
        assertEquals(350_000_000L, executionSnapshot.totalDurationNanos(), "Should have correct total duration");
        assertEquals(116.67, executionSnapshot.averageMs(), 0.01, "Should have correct average duration");

        ProviderHealthSnapshot healthSnapshot = healthSnapshots.get(0);
        assertEquals(2, healthSnapshot.totalChecks(), "Should have 2 health checks");
        assertEquals(1, healthSnapshot.healthyChecks(), "Should have 1 healthy check");
        assertEquals(1, healthSnapshot.unhealthyChecks(), "Should have 1 unhealthy check");
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testRESTExporterIntegration() {
        // Record some metrics
        ExecutionMetricsCollector collector = registry.executionCollector(MetricKeys.action("export-test"));
        collector.recordExecution(true, 100_000_000L);
        collector.recordExecution(true, 200_000_000L);

        // Test REST exporter methods
        Map<String, Object> metricsSummary = exporter.getMetricsSummary();
        assertNotNull(metricsSummary, "Metrics summary should not be null");
        assertTrue(metricsSummary.containsKey("totalActions"), "Should contain total actions");
        assertTrue(metricsSummary.containsKey("totalExecutions"), "Should contain total executions");

        Map<String, Object> performanceMetrics = exporter.getPerformanceMetrics();
        assertNotNull(performanceMetrics, "Performance metrics should not be null");
        assertTrue(performanceMetrics.containsKey("executionMetrics"), "Should contain execution metrics");

        Map<String, Object> healthData = exporter.getHealthData();
        assertNotNull(healthData, "Health data should not be null");
        assertTrue(healthData.containsKey("healthStatus"), "Should contain health status");
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testHealthMonitorIntegration() {
        // Test health monitoring
        MetricsHealthMonitor.HealthStatus healthStatus = healthMonitor.getHealthStatus();
        assertNotNull(healthStatus, "Health status should not be null");

        List<MetricsHealthMonitor.Alert> activeAlerts = healthMonitor.getActiveAlerts();
        assertNotNull(activeAlerts, "Active alerts should not be null");

        // Perform health check
        healthMonitor.performHealthCheck();

        // Verify health check was performed
        assertNotNull(healthMonitor.getHealthStatistics(), "Health statistics should be available");
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testCircuitBreakerIntegration() {
        // Test circuit breaker with successful operations
        String result = circuitBreaker.execute(() -> "success");
        assertEquals("success", result, "Should execute successfully");

        // Test circuit breaker with failing operations
        assertThrows(RuntimeException.class, () -> {
            circuitBreaker.execute(() -> {
                throw new RuntimeException("Test failure");
            });
        }, "Should throw exception for failed operation");

        // Verify circuit breaker state
        assertEquals(MetricsCircuitBreaker.State.CLOSED, circuitBreaker.getState(), "Should be in CLOSED state");
        assertEquals(1, circuitBreaker.getFailureCount(), "Should have 1 failure");
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testToolServiceIntegration() {
        // Test tool service integration with monitoring
        toolService.updateMetrics("test-provider", "test-action", true, 100L);
        toolService.updateMetrics("test-provider", "test-action", false, 50L);

        // Verify metrics are recorded
        List<ExecutionMetricsSnapshot> snapshots = registry.getExecutionSnapshots();
        assertFalse(snapshots.isEmpty(), "Should have recorded metrics");

        // Test metrics retrieval
        Map<String, Object> metrics = toolService.getMetrics();
        assertNotNull(metrics, "Metrics should not be null");
        assertTrue(metrics.containsKey("executionMetrics"), "Should contain execution metrics");
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testConcurrentAccess() throws Exception {
        // Test concurrent access to monitoring system
        ExecutionMetricsCollector collector = registry.executionCollector(MetricKeys.action("concurrent-test"));

        // Create multiple threads recording metrics
        CompletableFuture<Void>[] futures = new CompletableFuture[10];
        for (int i = 0; i < 10; i++) {
            final int threadId = i;
            futures[i] = CompletableFuture.runAsync(() -> {
                for (int j = 0; j < 100; j++) {
                    collector.recordExecution(threadId % 2 == 0, 100_000_000L);
                }
            });
        }

        // Wait for all threads to complete
        CompletableFuture.allOf(futures).get(5, TimeUnit.SECONDS);

        // Verify all metrics were recorded correctly
        List<ExecutionMetricsSnapshot> snapshots = registry.getExecutionSnapshots();
        assertFalse(snapshots.isEmpty(), "Should have snapshots after concurrent access");

        ExecutionMetricsSnapshot snapshot = snapshots.get(0);
        assertEquals(1000, snapshot.total(), "Should have 1000 total executions");
        assertEquals(500, snapshot.success(), "Should have 500 successful executions");
        assertEquals(500, snapshot.failure(), "Should have 500 failed executions");
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testRegistryLifecycle() {
        // Test registry lifecycle
        assertNotNull(registry.keys(), "Should have keys");
        assertTrue(registry.keys().isEmpty(), "Should start with empty keys");

        // Add some collectors
        registry.executionCollector(MetricKeys.action("lifecycle-test"));
        registry.healthCollector(MetricKeys.provider("lifecycle-provider"));

        // Verify keys are added
        assertEquals(2, registry.keys().size(), "Should have 2 keys");

        // Test reset functionality
        registry.reset(MetricKeys.action("lifecycle-test"));

        // Verify reset worked
        List<ExecutionMetricsSnapshot> snapshots = registry.getExecutionSnapshots();
        assertTrue(snapshots.isEmpty() || snapshots.get(0).total() == 0, "Should have reset metrics");
    }
}
