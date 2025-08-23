package org.openhab.core.ai.stub;

import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.MetricKeys;
import org.openhab.core.ai.common.monitoring.collector.ExecutionMetricsCollector;
import org.openhab.core.ai.common.monitoring.registry.MonitoringRegistry;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Statistics class for stub services using the new monitoring framework.
 * 
 * <p>
 * This class provides comprehensive statistics for stub services:
 * - Request handling statistics
 * - Performance timing and processing metrics
 * - Error tracking and failure analysis
 * - Success rate calculations
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@Component(service = StubServiceStatistics.class)
@NonNullByDefault
public class StubServiceStatistics {

    private static final Logger logger = LoggerFactory.getLogger(StubServiceStatistics.class);

    // NEW: Monitoring registry for centralized metrics collection
    @Reference
    private @Nullable MonitoringRegistry monitoringRegistry;

    /**
     * Record request handling using the new monitoring framework
     */
    public void recordRequest(String serviceName, long processingTimeMs, boolean success) {
        try {
            // Use centralized monitoring registry
            if (monitoringRegistry != null) {
                ExecutionMetricsCollector collector = monitoringRegistry
                        .executionCollector(MetricKeys.action("stub-" + serviceName));
                collector.recordExecution(success, processingTimeMs * 1_000_000L); // Convert to nanoseconds
            }

            // Log the operation
            if (success) {
                logger.debug("Stub service request successful - Service: {}, Duration: {}ms", serviceName,
                        processingTimeMs);
            } else {
                logger.warn("Stub service request failed - Service: {}, Duration: {}ms", serviceName, processingTimeMs);
            }

        } catch (Exception e) {
            logger.error("Error recording stub service request metrics for service: {}", serviceName, e);
        }
    }

    /**
     * Record error occurrence using the new monitoring framework
     */
    public void recordError(String serviceName, String errorType) {
        try {
            // Use centralized monitoring registry
            if (monitoringRegistry != null) {
                ExecutionMetricsCollector collector = monitoringRegistry
                        .executionCollector(MetricKeys.action("stub-" + serviceName));
                collector.recordExecution(false, 0L);
            }

            // Log the error
            logger.warn("Stub service error recorded - Service: {}, Error: {}", serviceName, errorType);

        } catch (Exception e) {
            logger.error("Error recording stub service error metrics for service: {}", serviceName, e);
        }
    }

    /**
     * Get stub service statistics using the new monitoring framework
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> statistics = new java.util.HashMap<>();

        if (monitoringRegistry != null) {
            // Get statistics from monitoring registry for stub services
            ExecutionMetricsCollector collector = monitoringRegistry
                    .executionCollector(MetricKeys.action("stub-service"));
            var snapshot = collector.snapshot();

            statistics.put("requestCount", snapshot.total());
            statistics.put("successCount", snapshot.success());
            statistics.put("errorCount", snapshot.failure());
            statistics.put("totalProcessingTimeMs", snapshot.totalDurationNanos() / 1_000_000); // Convert from
                                                                                                // nanoseconds
            statistics.put("averageProcessingTimeMs",
                    snapshot.totalDurationNanos() / Math.max(1, snapshot.total()) / 1_000_000); // Convert from
                                                                                                // nanoseconds
            statistics.put("timestamp", Instant.now());
        } else {
            // Fallback to basic statistics
            statistics.put("requestCount", 0);
            statistics.put("successCount", 0);
            statistics.put("errorCount", 0);
            statistics.put("totalProcessingTimeMs", 0);
            statistics.put("averageProcessingTimeMs", 0);
            statistics.put("timestamp", Instant.now());
        }

        return statistics;
    }

    /**
     * Get the total number of requests handled.
     * 
     * @return Request count
     */
    public long getRequestCount() {
        if (monitoringRegistry != null) {
            ExecutionMetricsCollector collector = monitoringRegistry
                    .executionCollector(MetricKeys.action("stub-service"));
            return collector.snapshot().total();
        }
        return 0;
    }

    /**
     * Get the number of successful requests.
     * 
     * @return Success count
     */
    public long getSuccessCount() {
        if (monitoringRegistry != null) {
            ExecutionMetricsCollector collector = monitoringRegistry
                    .executionCollector(MetricKeys.action("stub-service"));
            return collector.snapshot().success();
        }
        return 0;
    }

    /**
     * Get the number of failed requests.
     * 
     * @return Error count
     */
    public long getErrorCount() {
        if (monitoringRegistry != null) {
            ExecutionMetricsCollector collector = monitoringRegistry
                    .executionCollector(MetricKeys.action("stub-service"));
            return collector.snapshot().failure();
        }
        return 0;
    }

    /**
     * Get the total processing time in milliseconds.
     * 
     * @return Total processing time
     */
    public long getTotalProcessingTimeMs() {
        if (monitoringRegistry != null) {
            ExecutionMetricsCollector collector = monitoringRegistry
                    .executionCollector(MetricKeys.action("stub-service"));
            return collector.snapshot().totalDurationNanos() / 1_000_000; // Convert from nanoseconds
        }
        return 0;
    }

    /**
     * Get the average processing time in milliseconds.
     * 
     * @return Average processing time
     */
    public long getAverageProcessingTimeMs() {
        if (monitoringRegistry != null) {
            ExecutionMetricsCollector collector = monitoringRegistry
                    .executionCollector(MetricKeys.action("stub-service"));
            var snapshot = collector.snapshot();
            return snapshot.totalDurationNanos() / Math.max(1, snapshot.total()) / 1_000_000; // Convert from
                                                                                              // nanoseconds
        }
        return 0;
    }

    /**
     * Reset statistics (clears the monitoring registry data for stub services)
     */
    public void resetStatistics() {
        if (monitoringRegistry != null) {
            monitoringRegistry.reset(MetricKeys.action("stub-service"));
            logger.info("Stub service statistics reset");
        }
    }
}
