package org.openhab.core.ai.common.monitoring.service.statistics;

import java.time.Duration;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.ConflictMetrics;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.api.PercentileMetrics;
import org.openhab.core.ai.common.monitoring.api.TrendMetrics;
import org.openhab.core.ai.common.monitoring.service.snapshot.ConflictResolutionSnapshot;
import org.osgi.service.component.annotations.Reference;

/**
 * Statistics class for conflict resolution metrics.
 * 
 * <p>
 * This class provides comprehensive conflict resolution statistics including
 * trend analysis, percentile calculations, and conflict metrics.
 * It aggregates multiple ConflictResolutionSnapshot instances to provide
 * historical and statistical analysis of conflict resolution patterns.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record ConflictResolutionStatistics(List<ConflictResolutionSnapshot> snapshots, Duration timeRange,
        long timestampMs) implements StatisticsSnapshot, TrendMetrics, PercentileMetrics, ConflictMetrics {

    @Reference
    private static @Nullable MetricsService metricsService;

    // TrendMetrics implementation
    @Override
    public double trendPercentage() {
        if (snapshots.size() < 2) {
            return 0.0;
        }
        // Calculate percentage change from first to last snapshot
        double firstValue = snapshots.get(0).conflictResolutionSuccessRate();
        double lastValue = snapshots.get(snapshots.size() - 1).conflictResolutionSuccessRate();
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

        List<Double> values = snapshots.stream().mapToDouble(s -> s.conflictResolutionSuccessRate()).sorted().boxed()
                .toList();

        int index = (int) Math.ceil((percentile / 100.0) * values.size()) - 1;
        index = Math.max(0, Math.min(index, values.size() - 1));
        return values.get(index);
    }

    // ConflictMetrics implementation
    @Override
    public double conflictDetectionRate() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.conflictDetectionRate()).average().orElse(0.0);
    }

    @Override
    public double conflictResolutionSuccessRate() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.conflictResolutionSuccessRate()).average().orElse(0.0);
    }

    @Override
    public double conflictComplexity() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.conflictComplexity()).average().orElse(0.0);
    }

    @Override
    public double conflictResolutionEfficiency() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.conflictResolutionEfficiency()).average().orElse(0.0);
    }

    @Override
    public double conflictResolutionLatency() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.conflictResolutionLatency()).average().orElse(0.0);
    }

    @Override
    public double conflictFrequency() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.conflictFrequency()).average().orElse(0.0);
    }

    @Override
    public double conflictEscalationRate() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.conflictEscalationRate()).average().orElse(0.0);
    }

    @Override
    public double conflictResolutionConfidence() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.conflictResolutionConfidence()).average().orElse(0.0);
    }

    @Override
    public double conflictPreventionRate() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.conflictPreventionRate()).average().orElse(0.0);
    }

    @Override
    public double conflictImpact() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.conflictImpact()).average().orElse(0.0);
    }

    /**
     * Create ConflictResolutionStatistics from a list of snapshots.
     * 
     * @param snapshots the list of conflict resolution snapshots
     * @param timeRange the time range for the statistics
     * @return conflict resolution statistics
     */
    public static ConflictResolutionStatistics fromSnapshots(List<ConflictResolutionSnapshot> snapshots,
            Duration timeRange) {
        return new ConflictResolutionStatistics(snapshots, timeRange, System.currentTimeMillis());
    }

    /**
     * Record conflict resolution statistics using MetricsService.
     */
    public static void recordConflictResolutionStatistics(boolean success, long durationMs) {
        MetricsService metrics = metricsService;
        if (metrics != null) {
            metrics.recordOperation("conflict-resolution", "statistics", success,
                    java.time.Duration.ofMillis(durationMs));
        }
    }
}
