package org.openhab.core.ai.common.monitoring.service.statistics;

import java.time.Duration;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.OptimizationMetrics;
import org.openhab.core.ai.common.monitoring.api.PercentileMetrics;
import org.openhab.core.ai.common.monitoring.api.TrendMetrics;
import org.openhab.core.ai.common.monitoring.service.snapshot.OptimizationSnapshot;

/**
 * Statistics class for optimization metrics.
 * 
 * <p>
 * This class provides comprehensive optimization statistics including
 * trend analysis, percentile calculations, and optimization metrics.
 * It aggregates multiple OptimizationSnapshot instances to provide
 * historical and statistical analysis of optimization patterns.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record OptimizationStatistics(List<OptimizationSnapshot> snapshots, Duration timeRange,
        long timestampMs) implements StatisticsSnapshot, TrendMetrics, PercentileMetrics, OptimizationMetrics {

    // TrendMetrics implementation
    @Override
    public double trendPercentage() {
        if (snapshots.size() < 2) {
            return 0.0;
        }
        // Calculate percentage change from first to last snapshot
        double firstValue = snapshots.get(0).optimizationEfficiency();
        double lastValue = snapshots.get(snapshots.size() - 1).optimizationEfficiency();
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

        List<Double> values = snapshots.stream().mapToDouble(s -> s.optimizationEfficiency()).sorted().boxed().toList();

        int index = (int) Math.ceil((percentile / 100.0) * values.size()) - 1;
        index = Math.max(0, Math.min(index, values.size() - 1));
        return values.get(index);
    }

    // OptimizationMetrics implementation
    @Override
    public double optimizationEfficiency() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.optimizationEfficiency()).average().orElse(0.0);
    }

    @Override
    public double performanceImprovementRate() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.performanceImprovementRate()).average().orElse(0.0);
    }

    @Override
    public double resourceOptimizationRate() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.resourceOptimizationRate()).average().orElse(0.0);
    }

    @Override
    public double optimizationSuccessRate() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.optimizationSuccessRate()).average().orElse(0.0);
    }

    @Override
    public double optimizationLatency() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.optimizationLatency()).average().orElse(0.0);
    }

    @Override
    public double optimizationThroughput() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.optimizationThroughput()).average().orElse(0.0);
    }

    @Override
    public double optimizationErrorRate() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.optimizationErrorRate()).average().orElse(0.0);
    }

    @Override
    public double optimizationConfidence() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.optimizationConfidence()).average().orElse(0.0);
    }

    @Override
    public double optimizationCoverage() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.optimizationCoverage()).average().orElse(0.0);
    }

    @Override
    public double optimizationImpact() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.optimizationImpact()).average().orElse(0.0);
    }

    /**
     * Create statistics from a list of snapshots.
     * 
     * @param snapshots the list of snapshots to aggregate
     * @param timeRange the time range for the statistics
     * @return optimization statistics
     */
    public static OptimizationStatistics fromSnapshots(List<OptimizationSnapshot> snapshots, Duration timeRange) {
        return new OptimizationStatistics(snapshots, timeRange, System.currentTimeMillis());
    }
}
