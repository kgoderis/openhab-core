package org.openhab.core.ai.common.monitoring.service.statistics;

import java.time.Duration;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.MessagingMetrics;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.api.PercentileMetrics;
import org.openhab.core.ai.common.monitoring.api.TrendMetrics;
import org.openhab.core.ai.common.monitoring.service.snapshot.MessagingSnapshot;
import org.osgi.service.component.annotations.Reference;

/**
 * Statistics class for messaging metrics.
 * 
 * <p>
 * This class provides comprehensive messaging statistics including
 * trend analysis, percentile calculations, and messaging metrics.
 * It aggregates multiple MessagingSnapshot instances to provide
 * historical and statistical analysis of messaging patterns.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record MessagingStatistics(List<MessagingSnapshot> snapshots, Duration timeRange,
        long timestampMs) implements StatisticsSnapshot, TrendMetrics, PercentileMetrics, MessagingMetrics {

    @Reference
    private static @Nullable MetricsService metricsService;

    // TrendMetrics implementation
    @Override
    public double trendPercentage() {
        if (snapshots.size() < 2) {
            return 0.0;
        }
        // Calculate percentage change from first to last snapshot
        double firstValue = snapshots.get(0).messageDeliverySuccessRate();
        double lastValue = snapshots.get(snapshots.size() - 1).messageDeliverySuccessRate();
        return firstValue > 0 ? ((lastValue - firstValue) / firstValue) * 100.0 : 0.0;
    }

    @Override
    public String trendDirection() {
        double percentage = trendPercentage();
        if (percentage > 5.0) {
            return "increasing";
        } else if (percentage < -5.0) {
            return "decreasing";
        } else {
            return "stable";
        }
    }

    @Override
    public double changeRate() {
        if (snapshots.size() < 2) {
            return 0.0;
        }
        // Calculate change rate per time unit
        double totalChange = trendPercentage();
        long timeSpanMs = snapshots.get(snapshots.size() - 1).timestampMs() - snapshots.get(0).timestampMs();
        return timeSpanMs > 0 ? totalChange / (timeSpanMs / 1000.0) : 0.0;
    }

    // PercentileMetrics implementation
    @Override
    public double percentile50() {
        return calculatePercentile(50.0);
    }

    @Override
    public double percentile90() {
        return calculatePercentile(90.0);
    }

    @Override
    public double percentile95() {
        return calculatePercentile(95.0);
    }

    @Override
    public double percentile99() {
        return calculatePercentile(99.0);
    }

    private double calculatePercentile(double percentile) {
        if (snapshots.isEmpty()) {
            return 0.0;
        }

        List<Double> values = snapshots.stream().mapToDouble(s -> s.messageDeliverySuccessRate()).sorted().boxed()
                .toList();

        int index = (int) Math.ceil((percentile / 100.0) * values.size()) - 1;
        index = Math.max(0, Math.min(index, values.size() - 1));
        return values.get(index);
    }

    // MessagingMetrics implementation
    @Override
    public double messageDeliverySuccessRate() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.messageDeliverySuccessRate()).average().orElse(0.0);
    }

    @Override
    public double messagingThroughput() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.messagingThroughput()).average().orElse(0.0);
    }

    @Override
    public double messagingLatency() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.messagingLatency()).average().orElse(0.0);
    }

    @Override
    public double messagingReliability() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.messagingReliability()).average().orElse(0.0);
    }

    @Override
    public long messageQueueDepth() {
        if (snapshots.isEmpty()) {
            return 0L;
        }
        return (long) snapshots.stream().mapToLong(s -> s.messageQueueDepth()).average().orElse(0.0);
    }

    @Override
    public double messagingErrorRate() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.messagingErrorRate()).average().orElse(0.0);
    }

    @Override
    public double messageAcknowledgmentRate() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.messageAcknowledgmentRate()).average().orElse(0.0);
    }

    @Override
    public double messagingRetryRate() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.messagingRetryRate()).average().orElse(0.0);
    }

    @Override
    public double messagingTimeoutRate() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.messagingTimeoutRate()).average().orElse(0.0);
    }

    @Override
    public double messagingConfidence() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.messagingConfidence()).average().orElse(0.0);
    }

    /**
     * Create MessagingStatistics from a list of snapshots.
     * 
     * @param snapshots the list of messaging snapshots
     * @param timeRange the time range for the statistics
     * @return messaging statistics
     */
    public static MessagingStatistics fromSnapshots(List<MessagingSnapshot> snapshots, Duration timeRange) {
        return new MessagingStatistics(snapshots, timeRange, System.currentTimeMillis());
    }

    /**
     * Record messaging statistics using MetricsService.
     */
    public static void recordMessagingStatistics(boolean success, long durationMs) {
        MetricsService metrics = metricsService;
        if (metrics != null) {
            metrics.recordOperation("messaging", "statistics", success, java.time.Duration.ofMillis(durationMs));
        }
    }
}
