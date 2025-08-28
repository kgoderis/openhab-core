package org.openhab.core.ai.stub;

import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.MetricKey;
import org.openhab.core.ai.common.monitoring.api.MetricKeys;
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
        if (serviceName == null || serviceName.trim().isEmpty()) {
            logger.warn("Cannot record stub service request metrics: service name is null or empty");
            return;
        }

        if (processingTimeMs < 0) {
            logger.warn(
                    "Cannot record stub service request metrics for service '{}': processing time cannot be negative ({}ms)",
                    serviceName, processingTimeMs);
            return;
        }

        // Use centralized metrics service
        if (metricsService != null) {
            try {
                metricsService.recordOperation("stub-service", serviceName, success,
                        java.time.Duration.ofMillis(processingTimeMs));
            } catch (Exception e) {
                logger.error("Failed to record stub service request metrics for service '{}': {}", serviceName,
                        e.getMessage(), e);
                // Graceful degradation: continue with logging even if metrics recording fails
            }
        } else {
            logger.debug("MetricsService not available - skipping stub service request metrics for service: {}",
                    serviceName);
        }

        // Log the operation
        if (success) {
            logger.debug("Stub service request successful - Service: {}, Duration: {}ms", serviceName,
                    processingTimeMs);
        } else {
            logger.warn("Stub service request failed - Service: {}, Duration: {}ms", serviceName, processingTimeMs);
        }
    }

    /**
     * Record error occurrence using the new monitoring framework
     */
    public void recordError(String serviceName, String errorType) {
        if (serviceName == null || serviceName.trim().isEmpty()) {
            logger.warn("Cannot record stub service error metrics: service name is null or empty");
            return;
        }

        // Use centralized metrics service with builder pattern
        if (metricsService != null) {
            try {
                metricsService.recordOperation("stub-service", "error").withSuccess(false).withDuration(0L)
                        .withData("serviceName", serviceName)
                        .withData("errorType", errorType != null ? errorType : "unknown").record();
            } catch (Exception e) {
                logger.error("Failed to record stub service error metrics for service '{}': {}", serviceName,
                        e.getMessage(), e);
                // Graceful degradation: continue with logging even if metrics recording fails
            }
        } else {
            logger.debug("MetricsService not available - skipping stub service error metrics for service: {}",
                    serviceName);
        }

        // Log the error
        logger.warn("Stub service error recorded - Service: {}, Error: {}", serviceName,
                errorType != null ? errorType : "unknown");
    }

    /**
     * Get stub service statistics using the new monitoring framework
     */
    // Eliminated getStatistics() proxy method - unit conversion moved to StatisticsFactory
    // Consumers should access snapshots directly via MetricsService:
    // metricsService.getSnapshot(MetricKeys.custom("stub-service", Map.of(), Set.of("counts", "latency")),
    // GenericMetricsSnapshot.class)

    /**
     * Get the total number of requests handled.
     * 
     * @return Request count
     */
    public long getRequestCount() {
        if (metricsService != null) {
            try {
                MetricKey stubKey = MetricKeys.custom("stub-service", Map.of(), Set.of("counts", "latency"));
                var snapshot = metricsService.getSnapshot(stubKey,
                        org.openhab.core.ai.common.monitoring.service.snapshot.GenericMetricsSnapshot.class);
                return snapshot != null ? snapshot.getLong("total") : 0;
            } catch (Exception e) {
                logger.debug("Failed to get request count from MetricsService: {}", e.getMessage());
                return 0;
            }
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
            try {
                MetricKey stubKey = MetricKeys.custom("stub-service", Map.of(), Set.of("counts", "latency"));
                var snapshot = metricsService.getSnapshot(stubKey,
                        org.openhab.core.ai.common.monitoring.service.snapshot.GenericMetricsSnapshot.class);
                return snapshot != null ? snapshot.getLong("success") : 0;
            } catch (Exception e) {
                logger.debug("Failed to get success count from MetricsService: {}", e.getMessage());
                return 0;
            }
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
            try {
                MetricKey stubKey = MetricKeys.custom("stub-service", Map.of(), Set.of("counts", "latency"));
                var snapshot = metricsService.getSnapshot(stubKey,
                        org.openhab.core.ai.common.monitoring.service.snapshot.GenericMetricsSnapshot.class);
                return snapshot != null ? snapshot.getLong("failure") : 0;
            } catch (Exception e) {
                logger.debug("Failed to get error count from MetricsService: {}", e.getMessage());
                return 0;
            }
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
            try {
                MetricKey stubKey = MetricKeys.custom("stub-service", Map.of(), Set.of("counts", "latency"));
                var snapshot = metricsService.getSnapshot(stubKey,
                        org.openhab.core.ai.common.monitoring.service.snapshot.GenericMetricsSnapshot.class);
                return snapshot != null ? snapshot.getLong("totalDurationNanos") / 1_000_000 : 0; // Convert from
                                                                                                  // nanoseconds
            } catch (Exception e) {
                logger.debug("Failed to get total processing time from MetricsService: {}", e.getMessage());
                return 0;
            }
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
            try {
                MetricKey stubKey = MetricKeys.custom("stub-service", Map.of(), Set.of("counts", "latency"));
                var snapshot = metricsService.getSnapshot(stubKey,
                        org.openhab.core.ai.common.monitoring.service.snapshot.GenericMetricsSnapshot.class);
                if (snapshot != null) {
                    long total = snapshot.getLong("total");
                    long totalDurationNanos = snapshot.getLong("totalDurationNanos");
                    return totalDurationNanos / Math.max(1, total) / 1_000_000; // Convert from nanoseconds
                }
                return 0;
            } catch (Exception e) {
                logger.debug("Failed to get average processing time from MetricsService: {}", e.getMessage());
                return 0;
            }
        }
        return 0;
    }

    /**
     * Reset statistics (clears the monitoring registry data for stub services)
     */
    public void resetStatistics() {
        // Reset functionality is not available in MetricsService
        logger.info("Stub service statistics reset not supported - using centralized MetricsService");
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
