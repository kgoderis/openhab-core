package org.openhab.core.ai.common.monitoring.service.statistics;

import java.time.Duration;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.ContextMetrics;
import org.openhab.core.ai.common.monitoring.api.PercentileMetrics;
import org.openhab.core.ai.common.monitoring.api.TrendMetrics;
import org.openhab.core.ai.common.monitoring.service.snapshot.ContextProcessingSnapshot;

/**
 * Statistics class for context processing metrics.
 * 
 * <p>
 * This class provides comprehensive context processing statistics including
 * trend analysis, percentile calculations, and context metrics.
 * It aggregates multiple ContextProcessingSnapshot instances to provide
 * historical and statistical analysis of context processing patterns.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record ContextProcessingStatistics(List<ContextProcessingSnapshot> snapshots, Duration timeRange,
        long timestampMs) implements StatisticsSnapshot, TrendMetrics, PercentileMetrics, ContextMetrics {

    // TrendMetrics implementation
    @Override
    public double trendPercentage() {
        if (snapshots.size() < 2) {
            return 0.0;
        }
        // Calculate percentage change from first to last snapshot
        double firstValue = snapshots.get(0).contextProcessingEfficiency();
        double lastValue = snapshots.get(snapshots.size() - 1).contextProcessingEfficiency();
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

        List<Double> values = snapshots.stream().mapToDouble(s -> s.contextProcessingEfficiency()).sorted().boxed()
                .toList();

        int index = (int) Math.ceil((percentile / 100.0) * values.size()) - 1;
        index = Math.max(0, Math.min(index, values.size() - 1));
        return values.get(index);
    }

    // ContextMetrics implementation
    @Override
    public double contextRelevanceScore() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.contextRelevanceScore()).average().orElse(0.0);
    }

    @Override
    public double contextProcessingEfficiency() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.contextProcessingEfficiency()).average().orElse(0.0);
    }

    @Override
    public double contextAccuracy() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.contextAccuracy()).average().orElse(0.0);
    }

    @Override
    public double contextAdaptationRate() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.contextAdaptationRate()).average().orElse(0.0);
    }

    @Override
    public double contextProcessingLatency() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.contextProcessingLatency()).average().orElse(0.0);
    }

    @Override
    public double contextThroughput() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.contextThroughput()).average().orElse(0.0);
    }

    @Override
    public double contextErrorRate() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.contextErrorRate()).average().orElse(0.0);
    }

    @Override
    public double contextConsistencyRate() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.contextConsistencyRate()).average().orElse(0.0);
    }

    @Override
    public double contextCompleteness() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.contextCompleteness()).average().orElse(0.0);
    }

    @Override
    public double contextConfidence() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.contextConfidence()).average().orElse(0.0);
    }
}
