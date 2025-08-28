package org.openhab.core.ai.tool.monitoring;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.MetricKeys;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.service.snapshot.GenericMetricsSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service health state tracker used by the system health monitor.
 * 
 * <p>
 * This class has been migrated to use the centralized MetricsService instead of local
 * AtomicLong counters, following the centralized-only approach for metrics collection.
 * </p>
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
final class ServiceHealthState {
    private static final Logger logger = LoggerFactory.getLogger(ServiceHealthState.class);

    private final String serviceName;
    private final AtomicReference<Instant> lastHealthCheck = new AtomicReference<>();
    private final @Nullable MetricsService metricsService;

    ServiceHealthState(String serviceName, @Nullable MetricsService metricsService) {
        this.serviceName = serviceName;
        this.metricsService = metricsService;
    }

    void recordSuccess(long responseTime) {
        // Record success metrics using centralized MetricsService
        MetricsService metrics = metricsService;
        if (metrics != null) {
            try {
                metrics.recordOperation("service-health", serviceName).withSuccess(true)
                        .withDuration(Duration.ofMillis(responseTime).toNanos())
                        .withData("responseTimeMs", responseTime).record();
            } catch (Exception e) {
                logger.warn("Failed to record success metrics for service {}: {}", serviceName, e.getMessage());
                // Graceful degradation - continue without metrics
            }
        }
    }

    void recordFailure(Exception error) {
        // Record failure metrics using centralized MetricsService
        MetricsService metrics = metricsService;
        if (metrics != null) {
            try {
                metrics.recordOperation("service-health", serviceName).withSuccess(false).withDuration(0) // No duration
                                                                                                          // for
                                                                                                          // failures
                        .withData("errorType", error.getClass().getSimpleName())
                        .withData("errorMessage", error.getMessage() != null ? error.getMessage() : "Unknown error")
                        .record();
            } catch (Exception e) {
                logger.warn("Failed to record failure metrics for service {}: {}", serviceName, e.getMessage());
                // Graceful degradation - continue without metrics
            }
        }
    }

    void updateFromHealthCheck(boolean isHealthy, long responseTime) {
        lastHealthCheck.set(Instant.now());
    }

    void forceRecovery() {
        // Force recovery by logging a recovery event - no local state to reset
        logger.info("Forcing recovery for service {}", serviceName);
        // Optional: Could record a recovery metric
        MetricsService metrics = metricsService;
        if (metrics != null) {
            try {
                metrics.recordOperation("service-health", serviceName).withSuccess(true).withDuration(0)
                        .withData("eventType", "recovery").record();
            } catch (Exception e) {
                logger.warn("Failed to record recovery metrics for service {}: {}", serviceName, e.getMessage());
            }
        }
    }

    boolean isHealthy() {
        double successRate = getSuccessRate();
        double avgResponseTime = getAverageResponseTime();
        return successRate >= 0.8 && avgResponseTime <= 5000;
    }

    double getSuccessRate() {
        // Get success rate from MetricsService - centralized only approach
        GenericMetricsSnapshot snapshot = getMetricsSnapshot();
        if (snapshot != null) {
            long total = snapshot.getTotal();
            long success = snapshot.getSuccess();
            return total > 0 ? (double) success / total : 0.0;
        }
        return 0.0;
    }

    double getAverageResponseTime() {
        // Get average response time from MetricsService - centralized only approach
        GenericMetricsSnapshot snapshot = getMetricsSnapshot();
        if (snapshot != null) {
            long total = snapshot.getTotal();
            long totalDuration = snapshot.getTotalDurationNanos();
            return total > 0 ? (double) totalDuration / total / 1_000_000.0 : 0.0; // Convert nanos to millis
        }
        return 0.0;
    }

    /**
     * Get metrics snapshot from MetricsService for this service.
     * 
     * @return metrics snapshot or null if unavailable
     */
    private @Nullable GenericMetricsSnapshot getMetricsSnapshot() {
        MetricsService metrics = metricsService;
        if (metrics != null) {
            try {
                var metricKey = MetricKeys.custom("service-health", Map.of("serviceName", serviceName),
                        Set.of("counts", "latency"));
                return metrics.getSnapshot(metricKey, GenericMetricsSnapshot.class);
            } catch (Exception e) {
                logger.warn("Failed to retrieve metrics snapshot for service {}: {}", serviceName, e.getMessage());
                // Graceful degradation - return null
            }
        }
        return null;
    }

    Instant getLastHealthCheck() {
        return lastHealthCheck.get();
    }
}
