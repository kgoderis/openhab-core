package org.openhab.core.ai.stub;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
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
    private @Nullable MetricsService metricsService;

    /**
     * Record request handling using the new monitoring framework
     */
    public void recordRequest(String serviceName, long processingTimeMs, boolean success) {
        try {
            // Use centralized metrics service
            if (metricsService != null) {
                metricsService.recordOperation("stub-service", serviceName, success,
                        java.time.Duration.ofMillis(processingTimeMs));
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
            // Use centralized metrics service
            if (metricsService != null) {
                metricsService.recordOperation("stub-service", serviceName + "-error", false, java.time.Duration.ZERO);
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

        if (metricsService != null) {
            try {
                // Get statistics from metrics service for stub services
                var snapshot = metricsService.getDomainAggregatedSnapshot("stub-service");

                statistics.put("requestCount", snapshot.totalOperations());
                statistics.put("successCount", snapshot.successfulOperations());
                statistics.put("errorCount", snapshot.failedOperations());
                statistics.put("totalProcessingTimeMs", snapshot.totalDurationNanos() / 1_000_000); // Convert from
                                                                                                    // nanoseconds
                statistics.put("averageProcessingTimeMs",
                        snapshot.totalOperations() > 0
                                ? snapshot.totalDurationNanos() / (snapshot.totalOperations() * 1_000_000)
                                : 0);
                statistics.put("successRate", snapshot.getSuccessRate());
                statistics.put("lastUpdated", System.currentTimeMillis());
            } catch (Exception e) {
                logger.debug("Failed to get stub service statistics: {}", e.getMessage());
            }
        }

        return statistics;
    }

    /**
     * Get the total number of requests handled.
     * 
     * @return Request count
     */
    public long getRequestCount() {
        if (metricsService != null) {
            return metricsService.executionSnapshot().total();
        }
        return 0;
    }

    /**
     * Get the number of successful requests.
     * 
     * @return Success count
     */
    public long getSuccessCount() {
        if (metricsService != null) {
            return metricsService.executionSnapshot().success();
        }
        return 0;
    }

    /**
     * Get the number of failed requests.
     * 
     * @return Error count
     */
    public long getErrorCount() {
        if (metricsService != null) {
            return metricsService.executionSnapshot().failure();
        }
        return 0;
    }

    /**
     * Get the total processing time in milliseconds.
     * 
     * @return Total processing time
     */
    public long getTotalProcessingTimeMs() {
        if (metricsService != null) {
            return metricsService.executionSnapshot().totalDurationNanos() / 1_000_000; // Convert from nanoseconds
        }
        return 0;
    }

    /**
     * Get the average processing time in milliseconds.
     * 
     * @return Average processing time
     */
    public long getAverageProcessingTimeMs() {
        if (metricsService != null) {
            var snapshot = metricsService.executionSnapshot();
            return snapshot.totalDurationNanos() / Math.max(1, snapshot.total()) / 1_000_000; // Convert from
                                                                                              // nanoseconds
        }
        return 0;
    }

    /**
     * Reset statistics (clears the monitoring registry data for stub services)
     */
    public void resetStatistics() {
        if (metricsService != null) {
            metricsService.reset();
            logger.info("Stub service statistics reset");
        }
    }

    /**
     * Reset statistics - alias for resetStatistics() for backward compatibility
     */
    public void reset() {
        resetStatistics();
    }

    /**
     * Set request count - for backward compatibility (no-op since we use monitoring registry)
     */
    public void setRequestCount(long count) {
        // No-op since we use the monitoring registry for statistics
        logger.debug("setRequestCount called with {} - using monitoring registry instead", count);
    }
}
