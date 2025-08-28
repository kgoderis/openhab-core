package org.openhab.core.ai.tool.compliance;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.PercentileMetrics;
import org.openhab.core.ai.common.monitoring.api.TrendMetrics;
import org.openhab.core.ai.common.monitoring.api.ValidationMetrics;
import org.openhab.core.ai.common.monitoring.service.statistics.StatisticsSnapshot;

/**
 * Statistical analysis of compliance test performance over time.
 * 
 * <p>
 * This class provides advanced statistical analysis including trends,
 * percentiles, and historical performance data for compliance test operations.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record ComplianceTestStatistics(List<ComplianceTestSnapshot> snapshots, Duration timeRange,
        long timestampMs) implements StatisticsSnapshot, TrendMetrics, PercentileMetrics, ValidationMetrics {

    /**
     * Factory method to create statistics from historical data.
     * 
     * @param snapshots list of compliance test snapshots
     * @param timeRange time range for statistics
     * @return new ComplianceTestStatistics instance
     */
    public static ComplianceTestStatistics of(List<ComplianceTestSnapshot> snapshots, Duration timeRange) {
        return new ComplianceTestStatistics(List.copyOf(snapshots), timeRange, System.currentTimeMillis());
    }

    /**
     * Factory method to create empty statistics.
     * 
     * @return empty ComplianceTestStatistics instance
     */
    public static ComplianceTestStatistics empty() {
        return new ComplianceTestStatistics(List.of(), Duration.ZERO, System.currentTimeMillis());
    }

    // TrendMetrics implementation
    @Override
    public double trendPercentage() {
        if (snapshots.size() < 2) {
            return 0.0;
        }
        // Calculate percentage change from first to last snapshot
        double firstValue = snapshots.get(0).validationSuccessRate();
        double lastValue = snapshots.get(snapshots.size() - 1).validationSuccessRate();
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

        List<Double> values = snapshots.stream().mapToDouble(s -> s.validationSuccessRate()).sorted().boxed()
                .collect(Collectors.toList());

        int index = (int) Math.ceil((percentile / 100.0) * values.size()) - 1;
        index = Math.max(0, Math.min(index, values.size() - 1));
        return values.get(index);
    }

    // ValidationMetrics implementation
    @Override
    public double validationSuccessRate() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.validationSuccessRate()).average().orElse(0.0);
    }

    @Override
    public double validationAccuracy() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.validationAccuracy()).average().orElse(0.0);
    }

    @Override
    public double validationThroughput() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.validationThroughput()).average().orElse(0.0);
    }

    @Override
    public double validationEfficiency() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.validationEfficiency()).average().orElse(0.0);
    }

    @Override
    public double validationErrorRate() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.validationErrorRate()).average().orElse(0.0);
    }

    @Override
    public double validationLatency() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.validationLatency()).average().orElse(0.0);
    }

    @Override
    public double validationCoverage() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.validationCoverage()).average().orElse(0.0);
    }

    @Override
    public double validationConfidence() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.validationConfidence()).average().orElse(0.0);
    }

    @Override
    public double validationRejectionRate() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.validationRejectionRate()).average().orElse(0.0);
    }

    @Override
    public double validationComplianceRate() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.validationComplianceRate()).average().orElse(0.0);
    }

    /**
     * Create a summary string with key statistics.
     * 
     * @return summary string
     */
    public String toSummary() {
        return String.format(
                "ComplianceTestStatistics{trend=%s, successRate=%.1f%%, p95Latency=%.2fms, confidence=%.1f%%}",
                trendDirection(), validationSuccessRate(), percentile95(), validationConfidence());
    }
}

