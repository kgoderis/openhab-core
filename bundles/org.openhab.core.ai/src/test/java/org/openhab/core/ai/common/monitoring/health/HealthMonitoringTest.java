package org.openhab.core.ai.common.monitoring.health;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.ai.common.monitoring.health.MetricsHealthMonitor.Alert;
import org.openhab.core.ai.common.monitoring.health.MetricsHealthMonitor.HealthStatus;
import org.openhab.core.ai.common.monitoring.registry.DefaultMonitoringRegistry;

/**
 * Unit tests for health monitoring and alerting system.
 * 
 * This test class validates the health monitoring functionality,
 * alert generation, and recovery mechanisms.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
class HealthMonitoringTest {

    private MetricsHealthMonitor healthMonitor;
    private DefaultMonitoringRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new DefaultMonitoringRegistry();
        healthMonitor = new MetricsHealthMonitor();
    }

    @Test
    void testHealthCheckWithHealthySystem() {
        // Add some test data to registry
        var collector = registry
                .executionCollector(org.openhab.core.ai.common.monitoring.api.MetricKeys.provider("test"));
        collector.recordExecution(true, 1000_000_000L);

        var healthStatus = healthMonitor.performHealthCheck();

        assertNotNull(healthStatus);
        assertTrue(healthStatus.isHealthy());
        assertEquals(HealthStatus.HEALTHY, healthStatus.getStatus());
        assertTrue(healthStatus.getAlerts().isEmpty());
    }

    @Test
    void testHealthCheckWithUnhealthySystem() {
        // Simulate unhealthy state by not providing registry
        var healthStatus = healthMonitor.performHealthCheck();

        assertNotNull(healthStatus);
        assertFalse(healthStatus.isHealthy());
        assertEquals(HealthStatus.UNHEALTHY, healthStatus.getStatus());
        assertFalse(healthStatus.getAlerts().isEmpty());
    }

    @Test
    void testAlertGeneration() {
        // Trigger health check to generate alerts
        healthMonitor.performHealthCheck();

        var alerts = healthMonitor.getActiveAlerts();
        assertFalse(alerts.isEmpty());

        // Verify alert structure
        for (Alert alert : alerts) {
            assertNotNull(alert.getId());
            assertNotNull(alert.getMessage());
            assertNotNull(alert.getSeverity());
            assertNotNull(alert.getTimestamp());
            assertTrue(alert.getTimestamp() > 0);
        }
    }

    @Test
    void testAlertSeverityLevels() {
        healthMonitor.performHealthCheck();

        var alerts = healthMonitor.getActiveAlerts();
        boolean hasWarning = false;
        boolean hasError = false;
        boolean hasCritical = false;

        for (Alert alert : alerts) {
            switch (alert.getSeverity()) {
                case WARNING:
                    hasWarning = true;
                    break;
                case ERROR:
                    hasError = true;
                    break;
                case CRITICAL:
                    hasCritical = true;
                    break;
                default:
                    break;
            }
        }

        // Should have at least one alert level
        assertTrue(hasWarning || hasError || hasCritical);
    }

    @Test
    void testAlertHistory() {
        // Perform multiple health checks
        healthMonitor.performHealthCheck();
        healthMonitor.performHealthCheck();

        var history = healthMonitor.getAlertHistory();
        assertNotNull(history);
        assertTrue(history.size() >= 2); // At least 2 health checks
    }

    @Test
    void testAlertCleanup() {
        // Perform health check
        healthMonitor.performHealthCheck();

        var initialAlerts = healthMonitor.getActiveAlerts();
        assertFalse(initialAlerts.isEmpty());

        // Clear alerts
        healthMonitor.clearAlerts();

        var clearedAlerts = healthMonitor.getActiveAlerts();
        assertTrue(clearedAlerts.isEmpty());

        // History should still contain the alerts
        var history = healthMonitor.getAlertHistory();
        assertFalse(history.isEmpty());
    }

    @Test
    void testHealthStatistics() {
        healthMonitor.performHealthCheck();

        var stats = healthMonitor.getHealthStatistics();
        assertNotNull(stats);
        assertTrue(stats.getTotalChecks() > 0);
        assertTrue(stats.getLastCheckTime() > 0);
    }

    @Test
    void testHealthStatusTransitions() {
        // Initial state
        var status1 = healthMonitor.getHealthStatus();
        assertNotNull(status1);

        // After health check
        healthMonitor.performHealthCheck();
        var status2 = healthMonitor.getHealthStatus();
        assertNotNull(status2);

        // Status should be updated
        assertTrue(status2.getLastCheckTime() >= status1.getLastCheckTime());
    }

    @Test
    void testAlertMessageContent() {
        healthMonitor.performHealthCheck();

        var alerts = healthMonitor.getActiveAlerts();
        for (Alert alert : alerts) {
            assertFalse(alert.getMessage().isEmpty());
            assertTrue(alert.getMessage().length() > 10); // Meaningful message
        }
    }

    @Test
    void testAlertIdUniqueness() {
        healthMonitor.performHealthCheck();
        healthMonitor.performHealthCheck();

        var alerts = healthMonitor.getActiveAlerts();
        var alertIds = alerts.stream().map(Alert::getId).toList();

        // All alert IDs should be unique
        assertEquals(alertIds.size(), alertIds.stream().distinct().count());
    }

    @Test
    void testHealthCheckPerformance() {
        long startTime = System.currentTimeMillis();

        healthMonitor.performHealthCheck();

        long duration = System.currentTimeMillis() - startTime;

        // Health check should complete quickly (< 100ms)
        assertTrue(duration < 100, "Health check should be fast, took " + duration + "ms");
    }

    @Test
    void testMultipleConcurrentHealthChecks() throws InterruptedException {
        var threads = new Thread[5];
        var results = new HealthStatus[5];

        for (int i = 0; i < 5; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                results[index] = healthMonitor.performHealthCheck();
            });
        }

        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        // All health checks should complete successfully
        for (HealthStatus result : results) {
            assertNotNull(result);
        }
    }
}
