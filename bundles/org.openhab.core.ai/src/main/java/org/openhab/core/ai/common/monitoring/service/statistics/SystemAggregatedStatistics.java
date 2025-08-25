package org.openhab.core.ai.common.monitoring.service.statistics;

import java.time.Duration;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.BusinessMetrics;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.EfficiencyMetrics;
import org.openhab.core.ai.common.monitoring.api.PercentileMetrics;
import org.openhab.core.ai.common.monitoring.api.TrendMetrics;

/**
 * Statistics class for system-wide aggregated metrics.
 * 
 * <p>
 * This class provides computed statistics and insights about system-wide
 * performance derived from aggregated metrics data over time ranges. It implements
 * capability interfaces for clean, type-safe statistics access.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record SystemAggregatedStatistics(List<Object> agentStatistics, long totalAgentRequests,
        long totalAgentSuccessfulRequests, long totalAgentFailedRequests, long totalAgentResponseTime,
        long totalAgentTokens, double totalAgentCost, Object trackingStats, int registeredAgentCount,
        Duration timeRange, long timestampMs)
        implements
            StatisticsSnapshot,
            CountsMetrics,
            TrendMetrics,
            PercentileMetrics,
            BusinessMetrics,
            EfficiencyMetrics {

    // CountsMetrics implementation
    @Override
    public long total() {
        return totalAgentRequests;
    }

    @Override
    public long success() {
        return totalAgentSuccessfulRequests;
    }

    @Override
    public long failure() {
        return totalAgentFailedRequests;
    }

    @Override
    public double successRate() {
        if (total() == 0)
            return 0.0;
        return (success() * 100.0) / total();
    }

    // TrendMetrics implementation
    @Override
    public double trendPercentage() {
        // For system-wide statistics, we calculate trend based on success rate
        // This is a simplified implementation - in practice, you'd want historical data
        return 0.0; // Placeholder - would need historical data for proper trend calculation
    }

    @Override
    public String trendDirection() {
        double trend = trendPercentage();
        if (trend > 1.0)
            return "increasing";
        if (trend < -1.0)
            return "decreasing";
        return "stable";
    }

    @Override
    public double changeRate() {
        return trendPercentage() / timeRange.toDays();
    }

    // PercentileMetrics implementation
    @Override
    public double percentile50() {
        // For system-wide statistics, percentiles would be calculated from response times
        // This is a simplified implementation
        return totalAgentResponseTime > 0 ? (double) totalAgentResponseTime / total() : 0.0;
    }

    @Override
    public double percentile90() {
        // Simplified implementation - would need actual response time distribution
        return percentile50() * 1.5;
    }

    @Override
    public double percentile95() {
        // Simplified implementation - would need actual response time distribution
        return percentile50() * 2.0;
    }

    @Override
    public double percentile99() {
        // Simplified implementation - would need actual response time distribution
        return percentile50() * 3.0;
    }

    // BusinessMetrics implementation
    @Override
    public double costEfficiency() {
        if (total() == 0)
            return 0.0;
        return totalAgentCost / total();
    }

    @Override
    public double resourceUtilization() {
        if (total() == 0)
            return 0.0;
        return (double) totalAgentTokens / total();
    }

    @Override
    public double throughputEfficiency() {
        if (timeRange.toHours() == 0)
            return 0.0;
        return (double) total() / timeRange.toHours();
    }

    // EfficiencyMetrics implementation
    @Override
    public double resourceEfficiency() {
        if (total() == 0)
            return 0.0;
        return (double) totalAgentTokens / total();
    }

    @Override
    public double timeEfficiency() {
        if (total() == 0)
            return 0.0;
        return (double) totalAgentResponseTime / total();
    }

    @Override
    public double energyEfficiency() {
        // Simplified implementation - would need actual energy consumption data
        return resourceEfficiency(); // Use resource efficiency as proxy
    }

    /**
     * Create statistics from aggregated system data.
     * 
     * @param agentStatistics list of agent statistics objects
     * @param totalAgentRequests total agent requests
     * @param totalAgentSuccessfulRequests total successful agent requests
     * @param totalAgentFailedRequests total failed agent requests
     * @param totalAgentResponseTime total agent response time
     * @param totalAgentTokens total agent tokens used
     * @param totalAgentCost total agent cost
     * @param trackingStats tracking service statistics
     * @param registeredAgentCount number of registered agents
     * @param timeRange time range for statistics
     * @return system aggregated statistics
     */
    public static SystemAggregatedStatistics fromSystemData(List<Object> agentStatistics, long totalAgentRequests,
            long totalAgentSuccessfulRequests, long totalAgentFailedRequests, long totalAgentResponseTime,
            long totalAgentTokens, double totalAgentCost, Object trackingStats, int registeredAgentCount,
            Duration timeRange) {
        return new SystemAggregatedStatistics(agentStatistics, totalAgentRequests, totalAgentSuccessfulRequests,
                totalAgentFailedRequests, totalAgentResponseTime, totalAgentTokens, totalAgentCost, trackingStats,
                registeredAgentCount, timeRange, System.currentTimeMillis());
    }
}
