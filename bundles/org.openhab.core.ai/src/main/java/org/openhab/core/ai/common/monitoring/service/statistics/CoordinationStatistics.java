package org.openhab.core.ai.common.monitoring.service.statistics;

import java.time.Duration;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.CoordinationMetrics;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.api.PercentileMetrics;
import org.openhab.core.ai.common.monitoring.api.TrendMetrics;
import org.openhab.core.ai.common.monitoring.service.snapshot.CoordinationSnapshot;
import org.osgi.service.component.annotations.Reference;

/**
 * Statistics class for coordination metrics.
 * 
 * <p>
 * This class provides comprehensive coordination statistics including
 * trend analysis, percentile calculations, and coordination metrics.
 * It aggregates multiple CoordinationSnapshot instances to provide
 * historical and statistical analysis of coordination patterns.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record CoordinationStatistics(List<CoordinationSnapshot> snapshots, Duration timeRange,
        long timestampMs) implements StatisticsSnapshot, TrendMetrics, PercentileMetrics, CoordinationMetrics {

    @Reference
    private static @Nullable MetricsService metricsService;

    // TrendMetrics implementation
    @Override
    public double trendPercentage() {
        if (snapshots.size() < 2) {
            return 0.0;
        }
        // Calculate percentage change from first to last snapshot
        double firstValue = snapshots.get(0).coordinationEfficiency();
        double lastValue = snapshots.get(snapshots.size() - 1).coordinationEfficiency();
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

        List<Double> values = snapshots.stream().mapToDouble(s -> s.coordinationEfficiency()).sorted().boxed().toList();

        int index = (int) Math.ceil((percentile / 100.0) * values.size()) - 1;
        index = Math.max(0, Math.min(index, values.size() - 1));
        return values.get(index);
    }

    // CoordinationMetrics implementation
    @Override
    public double coordinationEfficiency() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.coordinationEfficiency()).average().orElse(0.0);
    }

    @Override
    public double taskSynchronizationRate() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.taskSynchronizationRate()).average().orElse(0.0);
    }

    @Override
    public double resourceCoordinationRate() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.resourceCoordinationRate()).average().orElse(0.0);
    }

    @Override
    public double coordinationSuccessRate() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.coordinationSuccessRate()).average().orElse(0.0);
    }

    @Override
    public double coordinationLatency() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.coordinationLatency()).average().orElse(0.0);
    }

    @Override
    public double coordinationThroughput() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.coordinationThroughput()).average().orElse(0.0);
    }

    @Override
    public double coordinationErrorRate() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.coordinationErrorRate()).average().orElse(0.0);
    }

    @Override
    public double coordinationConfidence() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.coordinationConfidence()).average().orElse(0.0);
    }

    @Override
    public double coordinationCoverage() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.coordinationCoverage()).average().orElse(0.0);
    }

    @Override
    public double coordinationImpact() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.coordinationImpact()).average().orElse(0.0);
    }

    /**
     * Create statistics from a list of snapshots.
     * 
     * @param snapshots the list of snapshots to aggregate
     * @param timeRange the time range for the statistics
     * @return coordination statistics
     */
    public static CoordinationStatistics fromSnapshots(List<CoordinationSnapshot> snapshots, Duration timeRange) {
        return new CoordinationStatistics(snapshots, timeRange, System.currentTimeMillis());
    }

    /**
     * Record coordination statistics using MetricsService.
     */
    public static void recordCoordinationStatistics(boolean success, long durationMs) {
        MetricsService metrics = metricsService;
        if (metrics != null) {
            metrics.recordOperation("coordination", "statistics", success, java.time.Duration.ofMillis(durationMs));
        }
    }
}
