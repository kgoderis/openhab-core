package org.openhab.core.ai.common.monitoring.service.statistics;

import java.time.Duration;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.AutonomousMetrics;
import org.openhab.core.ai.common.monitoring.api.PercentileMetrics;
import org.openhab.core.ai.common.monitoring.api.TrendMetrics;
import org.openhab.core.ai.common.monitoring.service.snapshot.AutonomousBehaviorSnapshot;

/**
 * Statistics class for autonomous behavior metrics.
 * 
 * <p>
 * This class provides comprehensive autonomous behavior statistics including
 * trend analysis, percentile calculations, and autonomous behavior metrics.
 * It aggregates multiple AutonomousBehaviorSnapshot instances to provide
 * historical and statistical analysis of autonomous behavior patterns.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record AutonomousBehaviorStatistics(List<AutonomousBehaviorSnapshot> snapshots, Duration timeRange,
        long timestampMs) implements StatisticsSnapshot, TrendMetrics, PercentileMetrics, AutonomousMetrics {

    // TrendMetrics implementation
    @Override
    public double trendPercentage() {
        if (snapshots.size() < 2) {
            return 0.0;
        }
        // Calculate percentage change from first to last snapshot
        double firstValue = snapshots.get(0).decisionAccuracy();
        double lastValue = snapshots.get(snapshots.size() - 1).decisionAccuracy();
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

        List<Double> values = snapshots.stream().mapToDouble(s -> s.decisionAccuracy()).sorted().boxed().toList();

        int index = (int) Math.ceil((percentile / 100.0) * values.size()) - 1;
        index = Math.max(0, Math.min(index, values.size() - 1));
        return values.get(index);
    }

    // AutonomousMetrics implementation
    @Override
    public double decisionAccuracy() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.decisionAccuracy()).average().orElse(0.0);
    }

    @Override
    public double actionSuccessRate() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.actionSuccessRate()).average().orElse(0.0);
    }

    @Override
    public double learningProgress() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.learningProgress()).average().orElse(0.0);
    }

    @Override
    public double adaptationRate() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.adaptationRate()).average().orElse(0.0);
    }

    @Override
    public double behaviorEfficiency() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.behaviorEfficiency()).average().orElse(0.0);
    }

    @Override
    public double decisionLatency() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.decisionLatency()).average().orElse(0.0);
    }

    @Override
    public double actionThroughput() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.actionThroughput()).average().orElse(0.0);
    }

    @Override
    public double autonomousErrorRate() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.autonomousErrorRate()).average().orElse(0.0);
    }

    @Override
    public double confidenceLevel() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.confidenceLevel()).average().orElse(0.0);
    }

    @Override
    public double explorationRate() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.explorationRate()).average().orElse(0.0);
    }
}
