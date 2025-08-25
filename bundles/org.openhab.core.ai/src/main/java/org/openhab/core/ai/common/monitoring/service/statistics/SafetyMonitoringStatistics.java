package org.openhab.core.ai.common.monitoring.service.statistics;

import java.time.Duration;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.PercentileMetrics;
import org.openhab.core.ai.common.monitoring.api.SafetyMetrics;
import org.openhab.core.ai.common.monitoring.api.TrendMetrics;
import org.openhab.core.ai.common.monitoring.service.snapshot.SafetyMonitoringSnapshot;

/**
 * Statistics class for safety monitoring metrics.
 * 
 * <p>
 * This class provides comprehensive safety monitoring statistics including
 * trend analysis, percentile calculations, and safety metrics.
 * It aggregates multiple SafetyMonitoringSnapshot instances to provide
 * historical and statistical analysis of safety monitoring patterns.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record SafetyMonitoringStatistics(List<SafetyMonitoringSnapshot> snapshots, Duration timeRange,
        long timestampMs) implements StatisticsSnapshot, TrendMetrics, PercentileMetrics, SafetyMetrics {

    // TrendMetrics implementation
    @Override
    public double trendPercentage() {
        if (snapshots.size() < 2) {
            return 0.0;
        }
        // Calculate percentage change from first to last snapshot
        double firstValue = snapshots.get(0).constraintComplianceRate();
        double lastValue = snapshots.get(snapshots.size() - 1).constraintComplianceRate();
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

        List<Double> values = snapshots.stream().mapToDouble(s -> s.constraintComplianceRate()).sorted().boxed()
                .toList();

        int index = (int) Math.ceil((percentile / 100.0) * values.size()) - 1;
        index = Math.max(0, Math.min(index, values.size() - 1));
        return values.get(index);
    }

    // SafetyMetrics implementation
    @Override
    public double safetyViolationRate() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.safetyViolationRate()).average().orElse(0.0);
    }

    @Override
    public double constraintComplianceRate() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.constraintComplianceRate()).average().orElse(0.0);
    }

    @Override
    public double riskAssessmentScore() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.riskAssessmentScore()).average().orElse(0.0);
    }

    @Override
    public double safetyIncidentRate() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.safetyIncidentRate()).average().orElse(0.0);
    }

    @Override
    public double safetyMonitoringCoverage() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.safetyMonitoringCoverage()).average().orElse(0.0);
    }

    @Override
    public double safetyResponseTime() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.safetyResponseTime()).average().orElse(0.0);
    }

    @Override
    public double safetyAlertFrequency() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.safetyAlertFrequency()).average().orElse(0.0);
    }

    @Override
    public double safetySystemAvailability() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.safetySystemAvailability()).average().orElse(0.0);
    }

    @Override
    public double thresholdComplianceRate() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.thresholdComplianceRate()).average().orElse(0.0);
    }

    @Override
    public double safetyConfidenceLevel() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.safetyConfidenceLevel()).average().orElse(0.0);
    }
}
