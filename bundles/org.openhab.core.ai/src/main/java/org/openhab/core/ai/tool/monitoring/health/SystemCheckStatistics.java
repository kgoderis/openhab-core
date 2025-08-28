package org.openhab.core.ai.tool.monitoring.health;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.HealthMetrics;
import org.openhab.core.ai.common.monitoring.api.HealthStatus;
import org.openhab.core.ai.common.monitoring.api.PercentileMetrics;
import org.openhab.core.ai.common.monitoring.api.TrendMetrics;
import org.openhab.core.ai.common.monitoring.service.statistics.StatisticsSnapshot;

/**
 * Statistical analysis of system health check performance over time.
 * 
 * <p>
 * This class provides advanced statistical analysis including trends,
 * percentiles, and historical performance data for system health checks.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record SystemCheckStatistics(double healthTrend, double successTrend, double latencyTrend,
        double dependencyTrend, double p50LatencyMs, double p95LatencyMs, double p99LatencyMs, double maxLatencyMs,
        double minLatencyMs, List<Double> recentSuccessRates, List<Double> recentLatencies,
        List<Double> recentHealthScores, String healthCheckId, String category,
        long timestampMs) implements StatisticsSnapshot, TrendMetrics, PercentileMetrics, HealthMetrics {

    /**
     * Factory method to create statistics from historical data.
     * 
     * @param healthTrend trend in overall health
     * @param successTrend trend in success rate
     * @param latencyTrend trend in latency
     * @param dependencyTrend trend in dependency complexity
     * @param p50LatencyMs 50th percentile latency
     * @param p95LatencyMs 95th percentile latency
     * @param p99LatencyMs 99th percentile latency
     * @param maxLatencyMs maximum latency
     * @param minLatencyMs minimum latency
     * @param recentSuccessRates recent success rates
     * @param recentLatencies recent latencies
     * @param recentHealthScores recent health scores
     * @param healthCheckId the health check identifier
     * @param category the health check category
     * @return new SystemCheckStatistics instance
     */
    public static SystemCheckStatistics of(double healthTrend, double successTrend, double latencyTrend,
            double dependencyTrend, double p50LatencyMs, double p95LatencyMs, double p99LatencyMs, double maxLatencyMs,
            double minLatencyMs, List<Double> recentSuccessRates, List<Double> recentLatencies,
            List<Double> recentHealthScores, String healthCheckId, String category) {
        return new SystemCheckStatistics(healthTrend, successTrend, latencyTrend, dependencyTrend, p50LatencyMs,
                p95LatencyMs, p99LatencyMs, maxLatencyMs, minLatencyMs, List.copyOf(recentSuccessRates),
                List.copyOf(recentLatencies), List.copyOf(recentHealthScores), healthCheckId, category,
                System.currentTimeMillis());
    }

    /**
     * Factory method to create empty statistics.
     * 
     * @param healthCheckId the health check identifier
     * @param category the health check category
     * @return empty SystemCheckStatistics instance
     */
    public static SystemCheckStatistics empty(String healthCheckId, String category) {
        return new SystemCheckStatistics(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, List.of(), List.of(), List.of(),
                healthCheckId, category, System.currentTimeMillis());
    }

    /**
     * Get the timestamp as an Instant.
     * 
     * @return timestamp as Instant
     */
    public Instant timestamp() {
        return Instant.ofEpochMilli(timestampMs);
    }

    /**
     * Calculate current health score from recent data.
     * 
     * @return current health score
     */
    public double currentHealthScore() {
        return recentHealthScores.isEmpty() ? 0.0
                : recentHealthScores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    // TrendMetrics implementation
    @Override
    public double trendPercentage() {
        // Average of all trends
        return (healthTrend + successTrend + latencyTrend + dependencyTrend) / 4.0;
    }

    @Override
    public String trendDirection() {
        double trend = trendPercentage();
        if (trend > 1.0) {
            return "improving";
        } else if (trend < -1.0) {
            return "degrading";
        } else {
            return "stable";
        }
    }

    @Override
    public double changeRate() {
        // Change rate based on overall trend
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

    // HealthMetrics implementation
    @Override
    public HealthStatus healthStatus() {
        double health = currentHealthScore();
        if (health >= 90.0) {
            return HealthStatus.HEALTHY;
        } else if (health >= 70.0) {
            return HealthStatus.HEALTHY;
        } else if (health >= 50.0) {
            return HealthStatus.DEGRADED;
        } else if (health >= 30.0) {
            return HealthStatus.UNHEALTHY;
        } else if (!recentHealthScores.isEmpty()) {
            return HealthStatus.UNHEALTHY;
        } else {
            return HealthStatus.UNKNOWN;
        }
    }

    @Override
    public @Nullable String statusMessage() {
        String direction = trendDirection();
        double health = currentHealthScore();

        if ("improving".equals(direction)) {
            return String.format("Health is improving (%.1f%% score, trending up)", health);
        } else if ("degrading".equals(direction)) {
            return String.format("Health is degrading (%.1f%% score, trending down)", health);
        } else {
            return String.format("Health is stable (%.1f%% score)", health);
        }
    }

    @Override
    public @Nullable Map<String, Object> healthIndicators() {
        return Map.of("currentHealthScore", currentHealthScore(), "trendDirection", trendDirection(), "trendPercentage",
                trendPercentage(), "p50LatencyMs", p50LatencyMs, "p95LatencyMs", p95LatencyMs, "healthCheckId",
                healthCheckId, "category", category, "dataPoints", recentHealthScores.size());
    }

    /**
     * Get the health trend direction with context.
     * 
     * @return trend direction description
     */
    public String getHealthTrendDescription() {
        String direction = trendDirection();
        double trend = healthTrend;

        if ("improving".equals(direction)) {
            return String.format("Health is improving (%.1f%% trend)", trend);
        } else if ("degrading".equals(direction)) {
            return String.format("Health is degrading (%.1f%% trend)", trend);
        } else {
            return "Health is stable";
        }
    }

    /**
     * Get performance insights based on statistics.
     * 
     * @return performance insights
     */
    public String getPerformanceInsight() {
        if (p99LatencyMs > p50LatencyMs * 3) {
            return "High latency variance detected - some checks are significantly slower";
        } else if (latencyTrend > 10.0) {
            return "Latency is increasing - performance degradation detected";
        } else if (successTrend < -5.0) {
            return "Success rate is declining - reliability issues detected";
        } else if (dependencyTrend > 5.0) {
            return "Dependency complexity is increasing - consider simplification";
        } else {
            return "Performance is within normal parameters";
        }
    }

    /**
     * Create a summary string with key statistics.
     * 
     * @return summary string
     */
    public String toSummary() {
        return String.format(
                "SystemCheckStatistics{id=%s, category=%s, trend=%s, health=%.1f%%, p95=%.2fms, insight=%s}",
                healthCheckId, category, trendDirection(), currentHealthScore(), p95LatencyMs, getPerformanceInsight());
    }
}
