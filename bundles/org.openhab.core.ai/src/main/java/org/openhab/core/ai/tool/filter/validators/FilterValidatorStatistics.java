package org.openhab.core.ai.tool.filter.validators;

import java.time.Instant;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.PercentileMetrics;
import org.openhab.core.ai.common.monitoring.api.TrendMetrics;
import org.openhab.core.ai.common.monitoring.api.ValidationMetrics;
import org.openhab.core.ai.common.monitoring.service.statistics.StatisticsSnapshot;

/**
 * Statistical analysis of filter validator performance over time.
 * 
 * <p>
 * This class provides advanced statistical analysis including trends,
 * percentiles, and historical performance data for filter validation operations.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record FilterValidatorStatistics(double validationTrend, double successTrend, double latencyTrend,
        double cacheEfficiencyTrend, double p50LatencyMs, double p95LatencyMs, double p99LatencyMs, double maxLatencyMs,
        double minLatencyMs, List<Double> recentSuccessRates, List<Double> recentLatencies,
        List<Double> recentCacheHitRates,
        long timestampMs) implements StatisticsSnapshot, TrendMetrics, PercentileMetrics, ValidationMetrics {

    /**
     * Factory method to create statistics from historical data.
     * 
     * @param validationTrend trend in validation volume
     * @param successTrend trend in success rate
     * @param latencyTrend trend in latency
     * @param cacheEfficiencyTrend trend in cache efficiency
     * @param p50LatencyMs 50th percentile latency
     * @param p95LatencyMs 95th percentile latency
     * @param p99LatencyMs 99th percentile latency
     * @param maxLatencyMs maximum latency
     * @param minLatencyMs minimum latency
     * @param recentSuccessRates recent success rates
     * @param recentLatencies recent latencies
     * @param recentCacheHitRates recent cache hit rates
     * @return new FilterValidatorStatistics instance
     */
    public static FilterValidatorStatistics of(double validationTrend, double successTrend, double latencyTrend,
            double cacheEfficiencyTrend, double p50LatencyMs, double p95LatencyMs, double p99LatencyMs,
            double maxLatencyMs, double minLatencyMs, List<Double> recentSuccessRates, List<Double> recentLatencies,
            List<Double> recentCacheHitRates) {
        return new FilterValidatorStatistics(validationTrend, successTrend, latencyTrend, cacheEfficiencyTrend,
                p50LatencyMs, p95LatencyMs, p99LatencyMs, maxLatencyMs, minLatencyMs, List.copyOf(recentSuccessRates),
                List.copyOf(recentLatencies), List.copyOf(recentCacheHitRates), System.currentTimeMillis());
    }

    /**
     * Factory method to create empty statistics.
     * 
     * @return empty FilterValidatorStatistics instance
     */
    public static FilterValidatorStatistics empty() {
        return new FilterValidatorStatistics(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, List.of(), List.of(),
                List.of(), System.currentTimeMillis());
    }

    /**
     * Get the timestamp as an Instant.
     * 
     * @return timestamp as Instant
     */
    public Instant timestamp() {
        return Instant.ofEpochMilli(timestampMs);
    }

    // TrendMetrics implementation
    @Override
    public double trendPercentage() {
        // Average of all trends
        return (validationTrend + successTrend + latencyTrend + cacheEfficiencyTrend) / 4.0;
    }

    @Override
    public String trendDirection() {
        double trend = trendPercentage();
        if (trend > 1.0) {
            return "increasing";
        } else if (trend < -1.0) {
            return "decreasing";
        } else {
            return "stable";
        }
    }

    @Override
    public double changeRate() {
        // Simple change rate calculation
        return trendPercentage();
    }

    // PercentileMetrics implementation
    @Override
    public double percentile50() {
        return p50LatencyMs;
    }

    @Override
    public double percentile90() {
        // Interpolate between p50 and p95
        return p50LatencyMs + (p95LatencyMs - p50LatencyMs) * 0.8;
    }

    @Override
    public double percentile95() {
        return p95LatencyMs;
    }

    @Override
    public double percentile99() {
        return p99LatencyMs;
    }

    // ValidationMetrics implementation
    @Override
    public double validationSuccessRate() {
        return recentSuccessRates.isEmpty() ? 0.0
                : recentSuccessRates.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    @Override
    public double validationAccuracy() {
        return validationSuccessRate();
    }

    @Override
    public double validationThroughput() {
        // Estimated throughput based on trends
        return Math.max(0.0, validationTrend);
    }

    @Override
    public double validationEfficiency() {
        double avgCacheHitRate = recentCacheHitRates.isEmpty() ? 0.0
                : recentCacheHitRates.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        return (validationSuccessRate() + avgCacheHitRate) / 2.0;
    }

    @Override
    public double validationErrorRate() {
        return 100.0 - validationSuccessRate();
    }

    @Override
    public double validationLatency() {
        return p50LatencyMs;
    }

    @Override
    public double validationCoverage() {
        // Coverage estimate based on trend stability
        return Math.abs(trendPercentage()) < 5.0 ? 95.0 : 80.0;
    }

    @Override
    public double validationConfidence() {
        // Confidence based on data points and trend stability
        int dataPoints = Math.min(recentSuccessRates.size(),
                Math.min(recentLatencies.size(), recentCacheHitRates.size()));
        double dataConfidence = Math.min(100.0, dataPoints * 10.0);
        double trendConfidence = Math.max(0.0, 100.0 - Math.abs(trendPercentage() * 10.0));
        return (dataConfidence + trendConfidence) / 2.0;
    }

    @Override
    public double validationRejectionRate() {
        return validationErrorRate();
    }

    @Override
    public double validationComplianceRate() {
        return validationSuccessRate();
    }

    /**
     * Create a summary string with key statistics.
     * 
     * @return summary string
     */
    public String toSummary() {
        return String.format(
                "FilterValidatorStatistics{trend=%s, successRate=%.1f%%, p95Latency=%.2fms, confidence=%.1f%%}",
                trendDirection(), validationSuccessRate(), p95LatencyMs, validationConfidence());
    }
}
