package org.openhab.core.ai.common.monitoring.service.snapshot;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.MetricsSnapshot;

/**
 * Snapshot record for notification service metrics.
 * 
 * <p>
 * This record provides notification service metrics including
 * notification delivery, event processing, listener management,
 * and notification type distribution. It implements CountsMetrics
 * and LatencyMetrics capability interfaces.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record NotificationSnapshot(String notificationType, String eventCategory, long totalNotifications,
        long successfulNotifications, long failedNotifications, long deliveredNotifications, long pendingNotifications,
        long queuedNotifications, long totalListeners, long activeListeners, long totalEvents, long processedEvents,
        long droppedEvents, long totalDurationNanos, long averageDeliveryTimeNanos, long averageProcessingTimeNanos,
        long maxDeliveryTimeNanos, long maxProcessingTimeNanos, long minDeliveryTimeNanos, long minProcessingTimeNanos,
        long queueSize, long maxQueueSize, boolean serviceEnabled, long serviceUptimeMs,
        long timestampMs) implements MetricsSnapshot, CountsMetrics, LatencyMetrics {

    // CountsMetrics implementation
    @Override
    public long total() {
        return totalNotifications;
    }

    @Override
    public long success() {
        return successfulNotifications;
    }

    @Override
    public long failure() {
        return failedNotifications;
    }

    // LatencyMetrics implementation
    @Override
    public long totalDurationNanos() {
        return totalDurationNanos;
    }

    /**
     * Calculate notification delivery success rate.
     * 
     * @return delivery success rate between 0.0 and 1.0
     */
    public double deliverySuccessRate() {
        return totalNotifications > 0 ? (double) successfulNotifications / totalNotifications : 0.0;
    }

    /**
     * Calculate notification delivery rate.
     * 
     * @return delivery rate between 0.0 and 1.0
     */
    public double deliveryRate() {
        return totalNotifications > 0 ? (double) deliveredNotifications / totalNotifications : 0.0;
    }

    /**
     * Calculate event processing success rate.
     * 
     * @return event processing success rate between 0.0 and 1.0
     */
    public double eventProcessingSuccessRate() {
        return totalEvents > 0 ? (double) processedEvents / totalEvents : 0.0;
    }

    /**
     * Calculate event drop rate.
     * 
     * @return event drop rate between 0.0 and 1.0
     */
    public double eventDropRate() {
        return totalEvents > 0 ? (double) droppedEvents / totalEvents : 0.0;
    }

    /**
     * Calculate listener utilization rate.
     * 
     * @return listener utilization rate between 0.0 and 1.0
     */
    public double listenerUtilizationRate() {
        return totalListeners > 0 ? (double) activeListeners / totalListeners : 0.0;
    }

    /**
     * Calculate queue utilization rate.
     * 
     * @return queue utilization rate between 0.0 and 1.0
     */
    public double queueUtilizationRate() {
        return maxQueueSize > 0 ? (double) queueSize / maxQueueSize : 0.0;
    }

    /**
     * Get average delivery time in milliseconds.
     * 
     * @return average delivery time in ms
     */
    public double averageDeliveryTimeMs() {
        return averageDeliveryTimeNanos / 1_000_000.0;
    }

    /**
     * Get average processing time in milliseconds.
     * 
     * @return average processing time in ms
     */
    public double averageProcessingTimeMs() {
        return averageProcessingTimeNanos / 1_000_000.0;
    }

    /**
     * Get maximum delivery time in milliseconds.
     * 
     * @return maximum delivery time in ms
     */
    public double maxDeliveryTimeMs() {
        return maxDeliveryTimeNanos / 1_000_000.0;
    }

    /**
     * Get maximum processing time in milliseconds.
     * 
     * @return maximum processing time in ms
     */
    public double maxProcessingTimeMs() {
        return maxProcessingTimeNanos / 1_000_000.0;
    }

    /**
     * Get minimum delivery time in milliseconds.
     * 
     * @return minimum delivery time in ms
     */
    public double minDeliveryTimeMs() {
        return minDeliveryTimeNanos / 1_000_000.0;
    }

    /**
     * Get minimum processing time in milliseconds.
     * 
     * @return minimum processing time in ms
     */
    public double minProcessingTimeMs() {
        return minProcessingTimeNanos / 1_000_000.0;
    }

    /**
     * Get service uptime in hours.
     * 
     * @return uptime in hours
     */
    public double uptimeHours() {
        return serviceUptimeMs / (1000.0 * 60.0 * 60.0);
    }

    /**
     * Calculate notification throughput (notifications per second).
     * 
     * @return throughput in notifications per second
     */
    public double notificationThroughput() {
        return serviceUptimeMs > 0 ? (double) totalNotifications / (serviceUptimeMs / 1000.0) : 0.0;
    }

    /**
     * Calculate event throughput (events per second).
     * 
     * @return throughput in events per second
     */
    public double eventThroughput() {
        return serviceUptimeMs > 0 ? (double) totalEvents / (serviceUptimeMs / 1000.0) : 0.0;
    }

    /**
     * Check if notification service is performing well.
     * 
     * @return true if delivery success rate > 95% and service is enabled
     */
    public boolean isHealthy() {
        return deliverySuccessRate() > 0.95 && serviceEnabled && queueUtilizationRate() < 0.8;
    }

    /**
     * Check if notification service is under stress.
     * 
     * @return true if failure rate > 5% or queue utilization > 90%
     */
    public boolean isStressed() {
        double failureRate = totalNotifications > 0 ? (double) failedNotifications / totalNotifications : 0.0;
        return failureRate > 0.05 || queueUtilizationRate() > 0.90;
    }

    /**
     * Check if notification service is overloaded.
     * 
     * @return true if queue is full or processing time is too high
     */
    public boolean isOverloaded() {
        return queueSize >= maxQueueSize || averageProcessingTimeMs() > 5000.0; // 5 second threshold
    }

    /**
     * Get notification service status.
     * 
     * @return service status string
     */
    public String serviceStatus() {
        if (!serviceEnabled) {
            return "disabled";
        } else if (isOverloaded()) {
            return "overloaded";
        } else if (isStressed()) {
            return "stressed";
        } else if (isHealthy()) {
            return "healthy";
        } else {
            return "degraded";
        }
    }

    /**
     * Calculate notification service reliability score.
     * 
     * @return reliability score between 0.0 and 1.0
     */
    public double reliabilityScore() {
        if (!serviceEnabled) {
            return 0.0;
        }

        double deliveryWeight = deliverySuccessRate();
        double processingWeight = eventProcessingSuccessRate();
        double queueWeight = Math.max(0.0, 1.0 - queueUtilizationRate());
        double timeWeight = Math.max(0.0, 1.0 - (averageProcessingTimeMs() / 10000.0)); // 10 second baseline

        return (deliveryWeight * 0.4) + (processingWeight * 0.3) + (queueWeight * 0.2) + (timeWeight * 0.1);
    }

    /**
     * Get notification backlog status.
     * 
     * @return backlog status string
     */
    public String backlogStatus() {
        if (queueSize == 0) {
            return "empty";
        } else if (queueSize < maxQueueSize * 0.5) {
            return "low";
        } else if (queueSize < maxQueueSize * 0.8) {
            return "moderate";
        } else {
            return "high";
        }
    }
}
