package org.openhab.core.ai.common.monitoring.health;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.Health;
import org.openhab.core.ai.common.monitoring.api.Metrics;
import org.openhab.core.ai.common.monitoring.api.Statistics;
import org.openhab.core.ai.common.monitoring.registry.MetricsRegistry;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Health monitor for the metrics system itself.
 * 
 * This monitor provides health checks for the metrics collection system,
 * alerts for metric collection failures and anomalies, and automatic
 * recovery mechanisms.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
@Component(service = MetricsHealthMonitor.class)
public class MetricsHealthMonitor {

    @Reference
    private @Nullable MetricsRegistry monitoringRegistry;

    private final AtomicBoolean isHealthy = new AtomicBoolean(true);
    private final AtomicLong lastHealthCheck = new AtomicLong(System.currentTimeMillis());
    private final AtomicLong consecutiveFailures = new AtomicLong(0);
    private final AtomicLong totalHealthChecks = new AtomicLong(0);
    private final AtomicLong failedHealthChecks = new AtomicLong(0);
    private final AtomicReference<String> lastError = new AtomicReference<>("");
    private final AtomicReference<HealthStatus> currentStatus = new AtomicReference<>(HealthStatus.HEALTHY);

    private final Map<String, Alert> activeAlerts = new ConcurrentHashMap<>();
    private final Map<String, Long> alertHistory = new ConcurrentHashMap<>();

    private static final long HEALTH_CHECK_INTERVAL_MS = 30_000; // 30 seconds
    private static final long ALERT_CLEANUP_INTERVAL_MS = 300_000; // 5 minutes
    private static final int MAX_CONSECUTIVE_FAILURES = 3;
    private static final double HEALTHY_THRESHOLD = 0.95; // 95% success rate

    /**
     * Health status enumeration.
     */
    public enum HealthStatus {
        HEALTHY,
        DEGRADED,
        WARNING,
        CRITICAL,
        OFFLINE
    }

    /**
     * Alert severity levels.
     */
    public enum AlertSeverity {
        INFO,
        WARNING,
        ERROR,
        CRITICAL
    }

    /**
     * Alert information.
     */
    public static class Alert {
        private final String id;
        private final String message;
        private final AlertSeverity severity;
        private final long timestamp;
        private final Map<String, Object> details;

        public Alert(String id, String message, AlertSeverity severity, Map<String, Object> details) {
            this.id = id;
            this.message = message;
            this.severity = severity;
            this.timestamp = System.currentTimeMillis();
            this.details = details;
        }

        public String getId() {
            return id;
        }

        public String getMessage() {
            return message;
        }

        public AlertSeverity getSeverity() {
            return severity;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public Map<String, Object> getDetails() {
            return details;
        }
    }

    /**
     * Perform a comprehensive health check of the metrics system.
     * 
     * @return true if the system is healthy, false otherwise
     */
    public boolean performHealthCheck() {
        MetricsRegistry registry = monitoringRegistry;
        if (registry == null) {
            recordFailure("Monitoring registry not available");
            return false;
        }

        try {
            totalHealthChecks.incrementAndGet();
            long startTime = System.currentTimeMillis();

            // Check registry availability
            if (!checkRegistryAvailability(registry)) {
                recordFailure("Registry availability check failed");
                return false;
            }

            // Check metrics collection
            if (!checkMetricsCollection(registry)) {
                recordFailure("Metrics collection check failed");
                return false;
            }

            // Check data consistency
            if (!checkDataConsistency(registry)) {
                recordFailure("Data consistency check failed");
                return false;
            }

            // Check performance
            if (!checkPerformance(registry)) {
                recordFailure("Performance check failed");
                return false;
            }

            // Record success
            recordSuccess();
            lastHealthCheck.set(startTime);
            return true;

        } catch (Exception e) {
            recordFailure("Health check exception: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get the current health status.
     * 
     * @return current health status
     */
    public HealthStatus getHealthStatus() {
        return currentStatus.get();
    }

    /**
     * Check if the metrics system is currently healthy.
     * 
     * @return true if healthy, false otherwise
     */
    public boolean isHealthy() {
        return isHealthy.get();
    }

    /**
     * Get health statistics.
     * 
     * @return map containing health statistics
     */
    public Map<String, Object> getHealthStatistics() {
        long total = totalHealthChecks.get();
        long failed = failedHealthChecks.get();
        double successRate = total > 0 ? (double) (total - failed) / total : 0.0;

        return Map.of("timestamp", System.currentTimeMillis(), "status", currentStatus.get().name(), "isHealthy",
                isHealthy.get(), "totalChecks", total, "failedChecks", failed, "successRate", successRate,
                "consecutiveFailures", consecutiveFailures.get(), "lastHealthCheck", lastHealthCheck.get(), "lastError",
                lastError.get(), "activeAlerts", activeAlerts.size());
    }

    /**
     * Get all active alerts.
     * 
     * @return list of active alerts
     */
    public List<Alert> getActiveAlerts() {
        return List.copyOf(activeAlerts.values());
    }

    /**
     * Clear all active alerts.
     */
    public void clearAlerts() {
        activeAlerts.clear();
    }

    /**
     * Get alert history.
     * 
     * @return map of alert history
     */
    public Map<String, Long> getAlertHistory() {
        return Map.copyOf(alertHistory);
    }

    // ===== Private Health Check Methods =====

    /**
     * Check registry availability.
     */
    private boolean checkRegistryAvailability(MetricsRegistry registry) {
        try {
            // Test basic registry operations
            registry.keys();
            registry.getMetricsSnapshots();
            registry.getStatisticsSnapshots();
            registry.getHealthSnapshots();
            return true;
        } catch (Exception e) {
            createAlert("registry_unavailable", "Registry operations failed", AlertSeverity.CRITICAL,
                    Map.of("error", e.getMessage()));
            return false;
        }
    }

    /**
     * Check metrics collection functionality.
     */
    private boolean checkMetricsCollection(MetricsRegistry registry) {
        try {
            List<Metrics> metrics = registry.getMetricsSnapshots();
            List<Statistics> statistics = registry.getStatisticsSnapshots();
            List<Health> healthData = registry.getHealthSnapshots();

            // Check for reasonable data volumes
            if (metrics.size() > 10000) {
                createAlert("high_metrics_volume", "High metrics volume detected", AlertSeverity.WARNING,
                        Map.of("count", metrics.size(), "threshold", 10000));
            }

            if (statistics.size() > 5000) {
                createAlert("high_statistics_volume", "High statistics volume detected", AlertSeverity.WARNING,
                        Map.of("count", statistics.size(), "threshold", 5000));
            }

            return true;
        } catch (Exception e) {
            createAlert("collection_failure", "Metrics collection failed", AlertSeverity.ERROR,
                    Map.of("error", e.getMessage()));
            return false;
        }
    }

    /**
     * Check data consistency.
     */
    private boolean checkDataConsistency(MetricsRegistry registry) {
        try {
            List<Metrics> metrics = registry.getMetricsSnapshots();

            // Check for data consistency issues
            for (Metrics metric : metrics) {
                if (metric.getTotalOperations() < 0) {
                    createAlert("negative_operations", "Negative operations count detected", AlertSeverity.ERROR,
                            Map.of("metricId", metric.getId(), "totalOperations", metric.getTotalOperations()));
                    return false;
                }

                if (metric.getSuccessfulOperations() + metric.getFailedOperations() != metric.getTotalOperations()) {
                    createAlert("inconsistent_operations", "Inconsistent operations count", AlertSeverity.WARNING,
                            Map.of("metricId", metric.getId(), "total", metric.getTotalOperations(), "success",
                                    metric.getSuccessfulOperations(), "failed", metric.getFailedOperations()));
                }
            }

            return true;
        } catch (Exception e) {
            createAlert("consistency_check_failure", "Data consistency check failed", AlertSeverity.ERROR,
                    Map.of("error", e.getMessage()));
            return false;
        }
    }

    /**
     * Check performance characteristics.
     */
    private boolean checkPerformance(MetricsRegistry registry) {
        try {
            long startTime = System.currentTimeMillis();

            // Perform a series of operations to measure performance
            registry.keys();
            registry.getMetricsSnapshots();
            registry.getStatisticsSnapshots();
            registry.getHealthSnapshots();

            long duration = System.currentTimeMillis() - startTime;

            if (duration > 1000) { // More than 1 second
                createAlert("slow_performance", "Slow metrics system performance", AlertSeverity.WARNING,
                        Map.of("durationMs", duration, "threshold", 1000));
            }

            return duration < 5000; // Fail if more than 5 seconds
        } catch (Exception e) {
            createAlert("performance_check_failure", "Performance check failed", AlertSeverity.ERROR,
                    Map.of("error", e.getMessage()));
            return false;
        }
    }

    // ===== Private Helper Methods =====

    /**
     * Record a successful health check.
     */
    private void recordSuccess() {
        consecutiveFailures.set(0);
        isHealthy.set(true);
        currentStatus.set(HealthStatus.HEALTHY);
        lastError.set("");
    }

    /**
     * Record a failed health check.
     */
    private void recordFailure(String error) {
        failedHealthChecks.incrementAndGet();
        consecutiveFailures.incrementAndGet();
        lastError.set(error);

        long consecutive = consecutiveFailures.get();

        if (consecutive >= MAX_CONSECUTIVE_FAILURES) {
            isHealthy.set(false);
            if (consecutive >= MAX_CONSECUTIVE_FAILURES * 2) {
                currentStatus.set(HealthStatus.CRITICAL);
            } else {
                currentStatus.set(HealthStatus.WARNING);
            }
        }
    }

    /**
     * Create an alert.
     */
    private void createAlert(String id, String message, AlertSeverity severity, Map<String, Object> details) {
        Alert alert = new Alert(id, message, severity, details);
        activeAlerts.put(id, alert);
        alertHistory.put(id, System.currentTimeMillis());
    }

    /**
     * Clean up old alerts.
     */
    public void cleanupOldAlerts() {
        long cutoffTime = System.currentTimeMillis() - ALERT_CLEANUP_INTERVAL_MS;

        activeAlerts.entrySet().removeIf(entry -> {
            Alert alert = entry.getValue();
            return alert.getTimestamp() < cutoffTime && alert.getSeverity() != AlertSeverity.CRITICAL;
        });
    }
}
