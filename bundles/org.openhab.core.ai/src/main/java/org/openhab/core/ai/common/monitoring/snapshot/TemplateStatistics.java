package org.openhab.core.ai.common.monitoring.snapshot;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.EfficiencyMetrics;
import org.openhab.core.ai.common.monitoring.api.PercentileMetrics;
import org.openhab.core.ai.common.monitoring.api.TrendMetrics;

/**
 * Immutable statistics for template metrics analysis.
 * 
 * This class provides trend analysis, percentile calculations, and efficiency
 * metrics for template operations over a specified time range.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public record TemplateStatistics(List<TemplateSnapshot> snapshots, Duration timeRange,
        long timestampMs) implements TrendMetrics, PercentileMetrics, EfficiencyMetrics {

    public TemplateStatistics {
        Objects.requireNonNull(snapshots, "snapshots");
        Objects.requireNonNull(timeRange, "timeRange");
    }

    // Helper method for percentile calculations
    private double percentile(double p) {
        if (snapshots.isEmpty()) {
            return 0.0;
        }

        // Calculate percentile of template usage rates
        double[] values = snapshots.stream().mapToDouble(TemplateSnapshot::templateUsageRate).sorted().toArray();

        return calculatePercentile(values, p);
    }

    @Override
    public double percentile50() {
        return percentile(50.0);
    }

    @Override
    public double percentile90() {
        return percentile(90.0);
    }

    @Override
    public double percentile95() {
        return percentile(95.0);
    }

    @Override
    public double percentile99() {
        return percentile(99.0);
    }

    @Override
    public double trendPercentage() {
        if (snapshots.size() < 2) {
            return 0.0;
        }

        // Calculate percentage change from first to last snapshot
        double firstValue = snapshots.get(0).total();
        double lastValue = snapshots.get(snapshots.size() - 1).total();

        if (firstValue == 0) {
            return lastValue > 0 ? 100.0 : 0.0;
        }

        return ((lastValue - firstValue) / firstValue) * 100.0;
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
        if (snapshots.size() < 2 || timeRange.isZero()) {
            return 0.0;
        }

        double firstValue = snapshots.get(0).total();
        double lastValue = snapshots.get(snapshots.size() - 1).total();
        double timeRangeSeconds = timeRange.toSeconds();

        return timeRangeSeconds > 0 ? (lastValue - firstValue) / timeRangeSeconds : 0.0;
    }

    @Override
    public double resourceEfficiency() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }

        // Calculate resource efficiency based on successful operations vs total
        return snapshots.stream()
                .mapToDouble(snapshot -> snapshot.total() > 0 ? (double) snapshot.success() / snapshot.total() : 0.0)
                .average().orElse(0.0);
    }

    @Override
    public double timeEfficiency() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }

        // Calculate time efficiency based on average response time
        double avgResponseTime = snapshots.stream().mapToDouble(TemplateSnapshot::averageMs).average().orElse(0.0);

        // Lower response time = higher efficiency (inverse relationship)
        // Normalize to 0-1 range, assuming 1000ms as baseline
        return avgResponseTime > 0 ? Math.min(1.0, 1000.0 / avgResponseTime) : 0.0;
    }

    @Override
    public double energyEfficiency() {
        // For template operations, energy efficiency is not directly applicable
        // Return a default value based on resource utilization
        return resourceEfficiency();
    }

    // Helper methods for statistical calculations
    private double calculateLinearRegressionSlope(long[] x, double[] y) {
        int n = x.length;
        if (n < 2)
            return 0.0;

        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        for (int i = 0; i < n; i++) {
            sumX += x[i];
            sumY += y[i];
            sumXY += x[i] * y[i];
            sumX2 += x[i] * x[i];
        }

        double denominator = n * sumX2 - sumX * sumX;
        return denominator != 0 ? (n * sumXY - sumX * sumY) / denominator : 0.0;
    }

    private double calculateCorrelationCoefficient(long[] x, double[] y) {
        int n = x.length;
        if (n < 2)
            return 0.0;

        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0, sumY2 = 0;
        for (int i = 0; i < n; i++) {
            sumX += x[i];
            sumY += y[i];
            sumXY += x[i] * y[i];
            sumX2 += x[i] * x[i];
            sumY2 += y[i] * y[i];
        }

        double numerator = n * sumXY - sumX * sumY;
        double denominator = Math.sqrt((n * sumX2 - sumX * sumX) * (n * sumY2 - sumY * sumY));

        return denominator != 0 ? numerator / denominator : 0.0;
    }

    private double calculatePercentile(double[] values, double p) {
        if (values.length == 0)
            return 0.0;

        int n = values.length;
        double index = (p / 100.0) * (n - 1);
        int lowerIndex = (int) Math.floor(index);
        int upperIndex = Math.min(lowerIndex + 1, n - 1);

        if (lowerIndex == upperIndex) {
            return values[lowerIndex];
        }

        double weight = index - lowerIndex;
        return values[lowerIndex] * (1 - weight) + values[upperIndex] * weight;
    }
}
