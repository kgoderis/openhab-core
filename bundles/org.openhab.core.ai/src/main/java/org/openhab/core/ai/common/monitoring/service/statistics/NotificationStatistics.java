package org.openhab.core.ai.common.monitoring.service.statistics;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.PercentileMetrics;
import org.openhab.core.ai.common.monitoring.api.TrendMetrics;
import org.openhab.core.ai.common.monitoring.service.snapshot.NotificationSnapshot;

/**
 * Statistics class for notification service metrics.
 * 
 * <p>
 * This class provides comprehensive notification statistics including
 * delivery performance, event processing efficiency, listener management,
 * and notification type analysis. It aggregates multiple NotificationSnapshot
 * instances to provide historical and statistical analysis of notification
 * service behavior.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record NotificationStatistics(List<NotificationSnapshot> snapshots, Duration timeRange, long timestampMs)
        implements
            StatisticsSnapshot,
            CountsMetrics,
            LatencyMetrics,
            TrendMetrics,
            PercentileMetrics {

    // CountsMetrics implementation (aggregated across all snapshots)
    @Override
    public long total() {
        return snapshots.stream().mapToLong(NotificationSnapshot::total).sum();
    }

    @Override
    public long success() {
        return snapshots.stream().mapToLong(NotificationSnapshot::success).sum();
    }

    @Override
    public long failure() {
        return snapshots.stream().mapToLong(NotificationSnapshot::failure).sum();
    }

    // LatencyMetrics implementation
    @Override
    public long totalDurationNanos() {
        return snapshots.stream().mapToLong(NotificationSnapshot::totalDurationNanos).sum();
    }

    // TrendMetrics implementation
    @Override
    public double trendPercentage() {
        if (snapshots.size() < 2) {
            return 0.0;
        }
        // Calculate reliability trend from first to last snapshot
        double firstReliability = snapshots.get(0).reliabilityScore();
        double lastReliability = snapshots.get(snapshots.size() - 1).reliabilityScore();
        return firstReliability > 0 ? ((lastReliability - firstReliability) / firstReliability) * 100.0 : 0.0;
    }

    @Override
    public String trendDirection() {
        double percentage = trendPercentage();
        if (percentage > 5.0) {
            return "improving";
        } else if (percentage < -5.0) {
            return "declining";
        } else {
            return "stable";
        }
    }

    @Override
    public double changeRate() {
        if (snapshots.size() < 2) {
            return 0.0;
        }
        // Calculate change rate in notifications per second
        long totalNotifications = total();
        long timeSpanMs = snapshots.get(snapshots.size() - 1).timestampMs() - snapshots.get(0).timestampMs();
        return timeSpanMs > 0 ? (double) totalNotifications / (timeSpanMs / 1000.0) : 0.0;
    }

    // PercentileMetrics implementation
    @Override
    public double percentile50() {
        return calculatePercentile(0.5);
    }

    @Override
    public double percentile90() {
        return calculatePercentile(0.9);
    }

    @Override
    public double percentile95() {
        return calculatePercentile(0.95);
    }

    @Override
    public double percentile99() {
        return calculatePercentile(0.99);
    }

    private double calculatePercentile(double percentile) {
        if (snapshots.isEmpty()) {
            return 0.0;
        }

        List<Double> deliveryTimes = snapshots.stream().mapToDouble(s -> s.averageDeliveryTimeMs()).sorted().boxed()
                .toList();

        if (deliveryTimes.isEmpty()) {
            return 0.0;
        }

        int index = (int) Math.ceil(percentile * deliveryTimes.size()) - 1;
        index = Math.max(0, Math.min(index, deliveryTimes.size() - 1));
        return deliveryTimes.get(index);
    }

    /**
     * Get total delivered notifications across all snapshots.
     * 
     * @return total delivered notifications
     */
    public long totalDeliveredNotifications() {
        return snapshots.stream().mapToLong(NotificationSnapshot::deliveredNotifications).sum();
    }

    /**
     * Get total pending notifications across all snapshots.
     * 
     * @return total pending notifications
     */
    public long totalPendingNotifications() {
        return snapshots.stream().mapToLong(NotificationSnapshot::pendingNotifications).sum();
    }

    /**
     * Get total queued notifications across all snapshots.
     * 
     * @return total queued notifications
     */
    public long totalQueuedNotifications() {
        return snapshots.stream().mapToLong(NotificationSnapshot::queuedNotifications).sum();
    }

    /**
     * Get total listeners across all snapshots.
     * 
     * @return total listeners
     */
    public long totalListeners() {
        return snapshots.stream().mapToLong(NotificationSnapshot::totalListeners).sum();
    }

    /**
     * Get total active listeners across all snapshots.
     * 
     * @return total active listeners
     */
    public long totalActiveListeners() {
        return snapshots.stream().mapToLong(NotificationSnapshot::activeListeners).sum();
    }

    /**
     * Get total events across all snapshots.
     * 
     * @return total events
     */
    public long totalEvents() {
        return snapshots.stream().mapToLong(NotificationSnapshot::totalEvents).sum();
    }

    /**
     * Get total processed events across all snapshots.
     * 
     * @return total processed events
     */
    public long totalProcessedEvents() {
        return snapshots.stream().mapToLong(NotificationSnapshot::processedEvents).sum();
    }

    /**
     * Get total dropped events across all snapshots.
     * 
     * @return total dropped events
     */
    public long totalDroppedEvents() {
        return snapshots.stream().mapToLong(NotificationSnapshot::droppedEvents).sum();
    }

    /**
     * Get average delivery success rate across all snapshots.
     * 
     * @return average delivery success rate
     */
    public double averageDeliverySuccessRate() {
        return snapshots.stream().mapToDouble(NotificationSnapshot::deliverySuccessRate).average().orElse(0.0);
    }

    /**
     * Get average delivery rate across all snapshots.
     * 
     * @return average delivery rate
     */
    public double averageDeliveryRate() {
        return snapshots.stream().mapToDouble(NotificationSnapshot::deliveryRate).average().orElse(0.0);
    }

    /**
     * Get average event processing success rate across all snapshots.
     * 
     * @return average event processing success rate
     */
    public double averageEventProcessingSuccessRate() {
        return snapshots.stream().mapToDouble(NotificationSnapshot::eventProcessingSuccessRate).average().orElse(0.0);
    }

    /**
     * Get average event drop rate across all snapshots.
     * 
     * @return average event drop rate
     */
    public double averageEventDropRate() {
        return snapshots.stream().mapToDouble(NotificationSnapshot::eventDropRate).average().orElse(0.0);
    }

    /**
     * Get average listener utilization rate across all snapshots.
     * 
     * @return average listener utilization rate
     */
    public double averageListenerUtilizationRate() {
        return snapshots.stream().mapToDouble(NotificationSnapshot::listenerUtilizationRate).average().orElse(0.0);
    }

    /**
     * Get average queue utilization rate across all snapshots.
     * 
     * @return average queue utilization rate
     */
    public double averageQueueUtilizationRate() {
        return snapshots.stream().mapToDouble(NotificationSnapshot::queueUtilizationRate).average().orElse(0.0);
    }

    /**
     * Get average delivery time across all snapshots.
     * 
     * @return average delivery time in milliseconds
     */
    public double averageDeliveryTime() {
        return snapshots.stream().mapToDouble(NotificationSnapshot::averageDeliveryTimeMs).average().orElse(0.0);
    }

    /**
     * Get average processing time across all snapshots.
     * 
     * @return average processing time in milliseconds
     */
    public double averageProcessingTime() {
        return snapshots.stream().mapToDouble(NotificationSnapshot::averageProcessingTimeMs).average().orElse(0.0);
    }

    /**
     * Get average notification throughput across all snapshots.
     * 
     * @return average notification throughput
     */
    public double averageNotificationThroughput() {
        return snapshots.stream().mapToDouble(NotificationSnapshot::notificationThroughput).average().orElse(0.0);
    }

    /**
     * Get average event throughput across all snapshots.
     * 
     * @return average event throughput
     */
    public double averageEventThroughput() {
        return snapshots.stream().mapToDouble(NotificationSnapshot::eventThroughput).average().orElse(0.0);
    }

    /**
     * Get average reliability score across all snapshots.
     * 
     * @return average reliability score
     */
    public double averageReliabilityScore() {
        return snapshots.stream().mapToDouble(NotificationSnapshot::reliabilityScore).average().orElse(0.0);
    }

    /**
     * Get notification type distribution.
     * 
     * @return map of notification types to their counts
     */
    public Map<String, Long> notificationTypeDistribution() {
        return snapshots.stream().collect(Collectors.groupingBy(NotificationSnapshot::notificationType,
                Collectors.summingLong(NotificationSnapshot::total)));
    }

    /**
     * Get event category distribution.
     * 
     * @return map of event categories to their counts
     */
    public Map<String, Long> eventCategoryDistribution() {
        return snapshots.stream().collect(Collectors.groupingBy(NotificationSnapshot::eventCategory,
                Collectors.summingLong(NotificationSnapshot::totalEvents)));
    }

    /**
     * Get service status distribution.
     * 
     * @return map of service statuses to their counts
     */
    public Map<String, Long> serviceStatusDistribution() {
        return snapshots.stream()
                .collect(Collectors.groupingBy(NotificationSnapshot::serviceStatus, Collectors.counting()));
    }

    /**
     * Get backlog status distribution.
     * 
     * @return map of backlog statuses to their counts
     */
    public Map<String, Long> backlogStatusDistribution() {
        return snapshots.stream()
                .collect(Collectors.groupingBy(NotificationSnapshot::backlogStatus, Collectors.counting()));
    }

    /**
     * Get count of healthy notification services.
     * 
     * @return number of healthy services
     */
    public long healthyServiceCount() {
        return snapshots.stream().mapToLong(s -> s.isHealthy() ? 1 : 0).sum();
    }

    /**
     * Get count of stressed notification services.
     * 
     * @return number of stressed services
     */
    public long stressedServiceCount() {
        return snapshots.stream().mapToLong(s -> s.isStressed() ? 1 : 0).sum();
    }

    /**
     * Get count of overloaded notification services.
     * 
     * @return number of overloaded services
     */
    public long overloadedServiceCount() {
        return snapshots.stream().mapToLong(s -> s.isOverloaded() ? 1 : 0).sum();
    }

    /**
     * Get count of enabled notification services.
     * 
     * @return number of enabled services
     */
    public long enabledServiceCount() {
        return snapshots.stream().mapToLong(s -> s.serviceEnabled() ? 1 : 0).sum();
    }

    /**
     * Get unique notification type count.
     * 
     * @return number of unique notification types
     */
    public long uniqueNotificationTypeCount() {
        return snapshots.stream().map(NotificationSnapshot::notificationType).distinct().count();
    }

    /**
     * Check if overall notification system is healthy.
     * 
     * @return true if average reliability > 0.8 and most services are healthy
     */
    public boolean isOverallHealthy() {
        double healthyRatio = snapshots.size() > 0 ? (double) healthyServiceCount() / snapshots.size() : 0.0;
        return averageReliabilityScore() > 0.8 && healthyRatio > 0.8;
    }

    /**
     * Get most active notification type.
     * 
     * @return notification type with highest count
     */
    public String mostActiveNotificationType() {
        return notificationTypeDistribution().entrySet().stream().max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).orElse("unknown");
    }

    /**
     * Get most common event category.
     * 
     * @return event category with highest count
     */
    public String mostCommonEventCategory() {
        return eventCategoryDistribution().entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey)
                .orElse("unknown");
    }

    /**
     * Get notification system performance level.
     * 
     * @return performance level string
     */
    public String performanceLevel() {
        double deliveryRate = averageDeliverySuccessRate();
        double processingRate = averageEventProcessingSuccessRate();

        if (deliveryRate > 0.95 && processingRate > 0.95) {
            return "excellent";
        } else if (deliveryRate > 0.90 && processingRate > 0.90) {
            return "good";
        } else if (deliveryRate > 0.80 && processingRate > 0.80) {
            return "fair";
        } else {
            return "poor";
        }
    }

    /**
     * Get notification system utilization rate.
     * 
     * @return utilization rate as percentage of services that are enabled
     */
    public double systemUtilizationRate() {
        return snapshots.size() > 0 ? (double) enabledServiceCount() / snapshots.size() * 100.0 : 0.0;
    }

    /**
     * Get total queue size across all services.
     * 
     * @return total queue size
     */
    public long totalQueueSize() {
        return snapshots.stream().mapToLong(NotificationSnapshot::queueSize).sum();
    }

    /**
     * Get total maximum queue size across all services.
     * 
     * @return total maximum queue size
     */
    public long totalMaxQueueSize() {
        return snapshots.stream().mapToLong(NotificationSnapshot::maxQueueSize).sum();
    }
}
