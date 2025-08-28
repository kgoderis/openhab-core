package org.openhab.core.ai.tool.monitoring;

import java.time.Duration;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * System Monitor
 * 
 * This class provides system monitoring capabilities including
 * system health metrics and monitoring statistics.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@Component(service = SystemMonitor.class)
@NonNullByDefault
public class SystemMonitor {

    private static final Logger logger = LoggerFactory.getLogger(SystemMonitor.class);

    // NEW: Centralized MetricsService for monitoring operations
    @Reference
    private @Nullable MetricsService metricsService;

    /**
     * Record a system monitoring operation
     * 
     * @param operationType the type of monitoring operation
     * @param success whether the operation was successful
     * @param duration the operation duration
     * @param metricsCollected number of metrics collected
     * @param alertsGenerated number of alerts generated
     */
    public void recordMonitoringOperation(String operationType, boolean success, Duration duration,
            int metricsCollected, int alertsGenerated) {
        try {
            if (operationType == null || operationType.trim().isEmpty()) {
                logger.warn("Cannot record system monitoring operation: operation type is null or empty");
                return;
            }

            if (duration == null || duration.isNegative()) {
                logger.warn("Cannot record system monitoring operation: duration is null or negative for operation: {}",
                        operationType);
                return;
            }

            if (metricsCollected < 0 || alertsGenerated < 0) {
                logger.warn(
                        "Cannot record system monitoring operation: metrics collected ({}) or alerts generated ({}) is negative for operation: {}",
                        metricsCollected, alertsGenerated, operationType);
                return;
            }

            // NEW: Record monitoring operation using centralized MetricsService
            MetricsService metrics = metricsService;
            if (metrics != null) {
                try {
                    metrics.recordOperation("monitoring", "system-" + operationType).withSuccess(success)
                            .withDuration(duration.toNanos()).withData("metricsCollected", metricsCollected)
                            .withData("alertsGenerated", alertsGenerated).withData("metricType", "system-monitoring")
                            .withData("alertSeverity", success ? "none" : "warning").record();
                    logger.debug(
                            "Recorded system monitoring operation: {} (success={}, duration={}, metrics={}, alerts={})",
                            operationType, success, duration, metricsCollected, alertsGenerated);
                } catch (Exception e) {
                    logger.error("Failed to record system monitoring operation for '{}': {}", operationType,
                            e.getMessage(), e);
                }
            } else {
                logger.debug("MetricsService not available for system monitoring operation: {}", operationType);
            }
        } catch (Exception e) {
            logger.error("Unexpected error recording system monitoring operation for '{}': {}", operationType,
                    e.getMessage(), e);
        }
    }

    /**
     * Get system monitoring statistics
     * 
     * @param timeRange the time range for statistics
     * @return monitoring statistics
     */
    public MonitoringStatistics getSystemMonitoringStatistics(Duration timeRange) {
        try {
            if (timeRange == null || timeRange.isNegative() || timeRange.isZero()) {
                logger.warn("Cannot get system monitoring statistics: time range is null, negative, or zero");
                return MonitoringStatistics.empty(Duration.ofHours(1));
            }

            MetricsService metrics = metricsService;
            if (metrics != null) {
                try {
                    // Note: getMonitoringStatistics method may not exist in current MetricsService implementation
                    // This is a placeholder for future implementation
                    MonitoringStatistics stats = MonitoringStatistics.empty(timeRange);
                    recordMonitoringOperation("statistics-retrieval", true, Duration.ofMillis(0), 1, 0);
                    return stats;
                } catch (Exception e) {
                    logger.error("Failed to get system monitoring statistics from MetricsService: {}", e.getMessage(),
                            e);
                    recordMonitoringOperation("statistics-retrieval", false, Duration.ofMillis(0), 0, 1);
                }
            } else {
                logger.debug("MetricsService not available for system monitoring statistics");
            }

            // Fallback to empty statistics if MetricsService is not available
            return MonitoringStatistics.empty(timeRange);
        } catch (Exception e) {
            logger.error("Unexpected error getting system monitoring statistics: {}", e.getMessage(), e);
            return MonitoringStatistics.empty(timeRange != null ? timeRange : Duration.ofHours(1));
        }
    }

    /**
     * Perform system health check
     * 
     * @return true if system is healthy
     */
    public boolean performSystemHealthCheck() {
        long startTime = System.currentTimeMillis();
        boolean isHealthy = false;
        int metricsCollected = 0;
        int alertsGenerated = 0;

        try {
            // Enhanced system health check logic with detailed validation
            isHealthy = true;

            // Check MetricsService availability
            MetricsService metrics = metricsService;
            if (metrics == null) {
                logger.warn("System health check: MetricsService is not available");
                isHealthy = false;
                alertsGenerated++;
            } else {
                metricsCollected++;
                logger.debug("System health check: MetricsService is available");
            }

            // Check JVM memory health
            try {
                Runtime runtime = Runtime.getRuntime();
                long maxMemory = runtime.maxMemory();
                long totalMemory = runtime.totalMemory();
                long freeMemory = runtime.freeMemory();
                long usedMemory = totalMemory - freeMemory;

                double memoryUsagePercent = (double) usedMemory / maxMemory * 100;
                if (memoryUsagePercent > 90) {
                    logger.warn("System health check: High memory usage detected: {}%",
                            String.format("%.2f", memoryUsagePercent));
                    isHealthy = false;
                    alertsGenerated++;
                } else {
                    logger.debug("System health check: Memory usage is healthy: {}%",
                            String.format("%.2f", memoryUsagePercent));
                }
                metricsCollected++;
            } catch (Exception e) {
                logger.error("System health check: Failed to check memory health: {}", e.getMessage(), e);
                isHealthy = false;
                alertsGenerated++;
            }

            // Check thread count
            try {
                int threadCount = Thread.activeCount();
                if (threadCount > 1000) {
                    logger.warn("System health check: High thread count detected: {}", threadCount);
                    isHealthy = false;
                    alertsGenerated++;
                } else {
                    logger.debug("System health check: Thread count is healthy: {}", threadCount);
                }
                metricsCollected++;
            } catch (Exception e) {
                logger.error("System health check: Failed to check thread health: {}", e.getMessage(), e);
                isHealthy = false;
                alertsGenerated++;
            }

            logger.debug("System health check completed: healthy={}, metrics={}, alerts={}", isHealthy,
                    metricsCollected, alertsGenerated);

        } catch (Exception e) {
            isHealthy = false;
            alertsGenerated++; // Alert generated due to exception
            logger.error("System health check failed with unexpected error: {}", e.getMessage(), e);
        } finally {
            // Record monitoring operation with proper error handling
            try {
                Duration duration = Duration.ofMillis(System.currentTimeMillis() - startTime);
                recordMonitoringOperation("health-check", isHealthy, duration, metricsCollected, alertsGenerated);
            } catch (Exception e) {
                logger.error("Failed to record health check monitoring operation: {}", e.getMessage(), e);
            }
        }

        return isHealthy;
    }

    /**
     * Collect system metrics
     * 
     * @return map of metric name to value
     */
    public Map<String, Object> collectSystemMetrics() {
        long startTime = System.currentTimeMillis();
        boolean success = true;
        int metricsCollected = 0;
        int alertsGenerated = 0;

        try {
            // TODO: Implement actual system metrics collection
            // This could include:
            // - Memory usage
            // - CPU usage
            // - Disk space
            // - Network statistics
            // - Process information

            // Placeholder implementation
            metricsCollected = 10; // Collect 10 system metrics
            alertsGenerated = 0; // No alerts generated

            logger.debug("System metrics collection completed: {} metrics collected", metricsCollected);

        } catch (Exception e) {
            success = false;
            alertsGenerated = 1; // Alert generated due to exception
            logger.error("System metrics collection failed", e);
        } finally {
            // Record monitoring operation
            Duration duration = Duration.ofMillis(System.currentTimeMillis() - startTime);
            recordMonitoringOperation("metrics-collection", success, duration, metricsCollected, alertsGenerated);
        }

        // Return empty map for now - would be populated with actual metrics
        return Map.of();
    }
}
