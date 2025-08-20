package org.openhab.core.ai.common.statistics;

import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.model.api.ModelProviderType;
import org.openhab.core.ai.tool.services.ProviderMetrics;
import org.openhab.core.ai.tool.services.ToolMetrics;

/**
 * Hybrid service metrics for tracking execution performance and statistics.
 * 
 * This class provides comprehensive metrics for hybrid tool execution services including
 * total executions, success rates, execution times, costs, and breakdowns by provider and tool.
 * This unified class replaces the duplicate HybridServiceMetrics implementations.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class HybridServiceMetrics extends BaseStatistics {
    private final long totalExecutions;
    private final long successfulExecutions;
    private final long failedExecutions;
    private final long fallbackExecutions;
    private final long totalExecutionTime;
    private final long totalCost;
    private final Map<ModelProviderType, ProviderMetrics> providerMetrics;
    private final Map<String, ToolMetrics> toolMetrics;

    /**
     * Constructor for HybridServiceMetrics.
     * 
     * @param totalExecutions total number of executions
     * @param successfulExecutions number of successful executions
     * @param failedExecutions number of failed executions
     * @param fallbackExecutions number of fallback executions
     * @param totalExecutionTime total execution time in milliseconds
     * @param totalCost total cost of executions
     * @param providerMetrics metrics by provider type
     * @param toolMetrics metrics by tool
     */
    public HybridServiceMetrics(long totalExecutions, long successfulExecutions, long failedExecutions,
            long fallbackExecutions, long totalExecutionTime, long totalCost,
            Map<ModelProviderType, ProviderMetrics> providerMetrics, Map<String, ToolMetrics> toolMetrics) {
        super("hybrid-service-metrics", StatisticsType.EXECUTION);
        this.totalExecutions = totalExecutions;
        this.successfulExecutions = successfulExecutions;
        this.failedExecutions = failedExecutions;
        this.fallbackExecutions = fallbackExecutions;
        this.totalExecutionTime = totalExecutionTime;
        this.totalCost = totalCost;
        this.providerMetrics = Objects.requireNonNull(providerMetrics, "providerMetrics");
        this.toolMetrics = Objects.requireNonNull(toolMetrics, "toolMetrics");

        // Add metrics to the base class
        addMetric("totalExecutions", totalExecutions);
        addMetric("successfulExecutions", successfulExecutions);
        addMetric("failedExecutions", failedExecutions);
        addMetric("fallbackExecutions", fallbackExecutions);
        addMetric("totalExecutionTime", totalExecutionTime);
        addMetric("totalCost", totalCost);
        addMetric("providerMetrics", providerMetrics);
        addMetric("toolMetrics", toolMetrics);
    }

    /**
     * Get total number of executions.
     * 
     * @return total executions
     */
    public long getTotalExecutions() {
        return totalExecutions;
    }

    /**
     * Get number of successful executions.
     * 
     * @return successful executions
     */
    public long getSuccessfulExecutions() {
        return successfulExecutions;
    }

    /**
     * Get number of failed executions.
     * 
     * @return failed executions
     */
    public long getFailedExecutions() {
        return failedExecutions;
    }

    /**
     * Get number of fallback executions.
     * 
     * @return fallback executions
     */
    public long getFallbackExecutions() {
        return fallbackExecutions;
    }

    /**
     * Get total execution time in milliseconds.
     * 
     * @return total execution time
     */
    public long getTotalExecutionTime() {
        return totalExecutionTime;
    }

    /**
     * Get total cost of executions.
     * 
     * @return total cost
     */
    public long getTotalCost() {
        return totalCost;
    }

    /**
     * Get metrics by provider type.
     * 
     * @return provider metrics
     */
    public Map<ModelProviderType, ProviderMetrics> getProviderMetrics() {
        return providerMetrics;
    }

    /**
     * Get metrics by tool.
     * 
     * @return tool metrics
     */
    public Map<String, ToolMetrics> getToolMetrics() {
        return toolMetrics;
    }

    /**
     * Calculate success rate.
     * 
     * @return success rate as a percentage (0.0 to 1.0)
     */
    public double getSuccessRate() {
        return totalExecutions > 0 ? (double) successfulExecutions / totalExecutions : 0.0;
    }

    /**
     * Calculate failure rate.
     * 
     * @return failure rate as a percentage (0.0 to 1.0)
     */
    public double getFailureRate() {
        return totalExecutions > 0 ? (double) failedExecutions / totalExecutions : 0.0;
    }

    /**
     * Calculate fallback rate.
     * 
     * @return fallback rate as a percentage (0.0 to 1.0)
     */
    public double getFallbackRate() {
        return totalExecutions > 0 ? (double) fallbackExecutions / totalExecutions : 0.0;
    }

    /**
     * Calculate average execution time.
     * 
     * @return average execution time in milliseconds
     */
    public double getAverageExecutionTime() {
        return totalExecutions > 0 ? (double) totalExecutionTime / totalExecutions : 0.0;
    }

    /**
     * Calculate average cost per execution.
     * 
     * @return average cost per execution
     */
    public double getAverageCost() {
        return totalExecutions > 0 ? (double) totalCost / totalExecutions : 0.0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        HybridServiceMetrics other = (HybridServiceMetrics) obj;
        return totalExecutions == other.totalExecutions && successfulExecutions == other.successfulExecutions
                && failedExecutions == other.failedExecutions && fallbackExecutions == other.fallbackExecutions
                && totalExecutionTime == other.totalExecutionTime && totalCost == other.totalCost
                && Objects.equals(providerMetrics, other.providerMetrics)
                && Objects.equals(toolMetrics, other.toolMetrics);
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalExecutions, successfulExecutions, failedExecutions, fallbackExecutions,
                totalExecutionTime, totalCost, providerMetrics, toolMetrics);
    }

    @Override
    public String toString() {
        return "HybridServiceMetrics{" + "totalExecutions=" + totalExecutions + ", successfulExecutions="
                + successfulExecutions + ", failedExecutions=" + failedExecutions + ", fallbackExecutions="
                + fallbackExecutions + ", totalExecutionTime=" + totalExecutionTime + ", totalCost=" + totalCost
                + ", providerMetrics=" + providerMetrics + ", toolMetrics=" + toolMetrics + '}';
    }
}
