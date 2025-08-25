package org.openhab.core.ai.common.monitoring.service.statistics;

import java.time.Duration;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.ConversationMetrics;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.api.PercentileMetrics;
import org.openhab.core.ai.common.monitoring.api.TrendMetrics;
import org.openhab.core.ai.common.monitoring.service.snapshot.ConversationSnapshot;
import org.osgi.service.component.annotations.Reference;

/**
 * Statistics class for conversation metrics.
 * 
 * <p>
 * This class provides comprehensive conversation statistics including
 * trend analysis, percentile calculations, and conversation metrics.
 * It aggregates multiple ConversationSnapshot instances to provide
 * historical and statistical analysis of conversation patterns.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record ConversationStatistics(List<ConversationSnapshot> snapshots, Duration timeRange,
        long timestampMs) implements StatisticsSnapshot, TrendMetrics, PercentileMetrics, ConversationMetrics {

    @Reference
    private static @Nullable MetricsService metricsService;

    // TrendMetrics implementation
    @Override
    public double trendPercentage() {
        if (snapshots.size() < 2) {
            return 0.0;
        }
        // Calculate percentage change from first to last snapshot
        double firstValue = snapshots.get(0).conversationQuality();
        double lastValue = snapshots.get(snapshots.size() - 1).conversationQuality();
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

        List<Double> values = snapshots.stream().mapToDouble(s -> s.conversationQuality()).sorted().boxed().toList();

        int index = (int) Math.ceil((percentile / 100.0) * values.size()) - 1;
        index = Math.max(0, Math.min(index, values.size() - 1));
        return values.get(index);
    }

    // ConversationMetrics implementation
    @Override
    public double conversationQuality() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.conversationQuality()).average().orElse(0.0);
    }

    @Override
    public double responseAccuracy() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.responseAccuracy()).average().orElse(0.0);
    }

    @Override
    public double conversationFlowRate() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.conversationFlowRate()).average().orElse(0.0);
    }

    @Override
    public double conversationSatisfaction() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.conversationSatisfaction()).average().orElse(0.0);
    }

    @Override
    public double conversationLatency() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.conversationLatency()).average().orElse(0.0);
    }

    @Override
    public double conversationThroughput() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.conversationThroughput()).average().orElse(0.0);
    }

    @Override
    public double conversationErrorRate() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.conversationErrorRate()).average().orElse(0.0);
    }

    @Override
    public double conversationConfidence() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.conversationConfidence()).average().orElse(0.0);
    }

    @Override
    public double conversationCoverage() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.conversationCoverage()).average().orElse(0.0);
    }

    @Override
    public double conversationImpact() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.conversationImpact()).average().orElse(0.0);
    }

    /**
     * Create ConversationStatistics from a list of snapshots.
     * 
     * @param snapshots the list of conversation snapshots
     * @param timeRange the time range for the statistics
     * @return conversation statistics
     */
    public static ConversationStatistics fromSnapshots(List<ConversationSnapshot> snapshots, Duration timeRange) {
        return new ConversationStatistics(snapshots, timeRange, System.currentTimeMillis());
    }

    /**
     * Record conversation statistics using MetricsService.
     */
    public static void recordConversationStatistics(boolean success, long durationMs) {
        MetricsService metrics = metricsService;
        if (metrics != null) {
            metrics.recordOperation("conversation", "statistics", success, java.time.Duration.ofMillis(durationMs));
        }
    }
}
