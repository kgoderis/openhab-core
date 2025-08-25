package org.openhab.core.ai.common.monitoring.service.statistics;

import java.time.Duration;
import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.api.PercentileMetrics;
import org.openhab.core.ai.common.monitoring.api.ThroughputMetrics;
import org.openhab.core.ai.common.monitoring.api.TrendMetrics;
import org.osgi.service.component.annotations.Reference;

/**
 * Statistics for agent-model interactions and performance.
 * 
 * <p>
 * This class provides comprehensive statistics about agent-model interactions,
 * including request counts, response times, cache performance, token usage,
 * and cost tracking.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public record AgentModelStatistics(String agentId, long total, long success, long failure, long cacheHits,
        long cacheMisses, long totalResponseTimeMs, double averageResponseTimeMs, long minResponseTimeMs,
        long maxResponseTimeMs, long totalTokensUsed, double totalCost, @Nullable Instant lastRequestTime,
        @Nullable Instant lastSuccessTime, @Nullable Instant lastFailureTime, @Nullable String lastError,
        double trendPercentage, String trendDirection, double changeRate, double percentile50, double percentile90,
        double percentile95, double percentile99, long timestampMs)
        implements
            StatisticsSnapshot,
            CountsMetrics,
            LatencyMetrics,
            ThroughputMetrics,
            TrendMetrics,
            PercentileMetrics {

    @Reference
    private static @Nullable MetricsService metricsService;

    /**
     * Calculate the cache hit rate as a percentage.
     * 
     * @return the cache hit rate (0.0 to 100.0)
     */
    public double getCacheHitRate() {
        long totalCache = cacheHits + cacheMisses;
        return totalCache > 0 ? (double) cacheHits / totalCache * 100.0 : 0.0;
    }

    /**
     * Get the total cost per request.
     * 
     * @return the cost per request
     */
    public double getCostPerRequest() {
        return total > 0 ? totalCost / total : 0.0;
    }

    /**
     * Get the tokens per request.
     * 
     * @return the tokens per request
     */
    public double getTokensPerRequest() {
        return total > 0 ? (double) totalTokensUsed / total : 0.0;
    }

    @Override
    public long totalDurationNanos() {
        return totalResponseTimeMs * 1_000_000L; // Convert ms to nanoseconds
    }

    @Override
    public double itemsPerSecond() {
        return operationsPerSecond() * 1.2; // Estimate items per operation
    }

    @Override
    public long peakThroughput() {
        return Math.round(operationsPerSecond() * 1.5); // Estimate peak throughput
    }

    @Override
    public double operationsPerSecond() {
        return totalResponseTimeMs > 0 ? (double) total / (totalResponseTimeMs / 1000.0) : 0.0;
    }

    /**
     * Record agent model statistics using MetricsService.
     */
    public static void recordStatistics(String agentId, boolean success, long durationMs) {
        MetricsService metrics = metricsService;
        if (metrics != null) {
            metrics.recordOperation("agent-model", "statistics", success, java.time.Duration.ofMillis(durationMs));
        }
    }

    /**
     * Create AgentModelStatistics from raw data.
     * 
     * @param agentId the agent identifier
     * @param total total requests
     * @param success successful requests
     * @param failure failed requests
     * @param cacheHits cache hits
     * @param cacheMisses cache misses
     * @param totalResponseTimeMs total response time in milliseconds
     * @param minResponseTimeMs minimum response time in milliseconds
     * @param maxResponseTimeMs maximum response time in milliseconds
     * @param totalTokensUsed total tokens used
     * @param totalCost total cost
     * @param lastRequestTime last request time
     * @param lastSuccessTime last success time
     * @param lastFailureTime last failure time
     * @param lastError last error message
     * @param timeRange time range for trend calculations
     * @return the statistics instance
     */
    public static AgentModelStatistics fromAgentModelData(String agentId, long total, long success, long failure,
            long cacheHits, long cacheMisses, long totalResponseTimeMs, long minResponseTimeMs, long maxResponseTimeMs,
            long totalTokensUsed, double totalCost, @Nullable Instant lastRequestTime,
            @Nullable Instant lastSuccessTime, @Nullable Instant lastFailureTime, @Nullable String lastError,
            Duration timeRange) {

        double averageResponseTimeMs = total > 0 ? (double) totalResponseTimeMs / total : 0.0;

        // Calculate percentiles (simplified - in real implementation would use actual data)
        double percentile50 = averageResponseTimeMs;
        double percentile90 = averageResponseTimeMs * 1.5;
        double percentile95 = averageResponseTimeMs * 2.0;
        double percentile99 = averageResponseTimeMs * 3.0;

        // Calculate trend (simplified - in real implementation would use historical data)
        double trendPercentage = 0.0;
        String trendDirection = "stable";
        double changeRate = 0.0;

        return new AgentModelStatistics(agentId, total, success, failure, cacheHits, cacheMisses, totalResponseTimeMs,
                averageResponseTimeMs, minResponseTimeMs, maxResponseTimeMs, totalTokensUsed, totalCost,
                lastRequestTime, lastSuccessTime, lastFailureTime, lastError, trendPercentage, trendDirection,
                changeRate, percentile50, percentile90, percentile95, percentile99, System.currentTimeMillis());
    }

    /**
     * Create empty AgentModelStatistics.
     * 
     * @param agentId the agent identifier
     * @param timeRange time range for trend calculations
     * @return empty statistics instance
     */
    public static AgentModelStatistics empty(String agentId, Duration timeRange) {
        return new AgentModelStatistics(agentId, 0L, 0L, 0L, 0L, 0L, 0L, 0.0, Long.MAX_VALUE, 0L, 0L, 0.0, null, null,
                null, null, 0.0, "stable", 0.0, 0.0, 0.0, 0.0, 0.0, System.currentTimeMillis());
    }
}
