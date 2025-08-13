package org.openhab.core.ai.tool.services;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.model.api.ModelProviderType;

/**
 * Hybrid service metrics for tracking execution performance and statistics.
 * 
 * <p>
 * This class provides comprehensive metrics for hybrid tool execution services including
 * total executions, success rates, execution times, costs, and breakdowns by provider and tool.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class HybridServiceMetrics {
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
        this.totalExecutions = totalExecutions;
        this.successfulExecutions = successfulExecutions;
        this.failedExecutions = failedExecutions;
        this.fallbackExecutions = fallbackExecutions;
        this.totalExecutionTime = totalExecutionTime;
        this.totalCost = totalCost;
        this.providerMetrics = providerMetrics;
        this.toolMetrics = toolMetrics;
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
     * @return success rate as a percentage
     */
    public double getSuccessRate() {
        return totalExecutions > 0 ? (double) successfulExecutions / totalExecutions : 0.0;
    }

    /**
     * Calculate average execution time.
     * 
     * @return average execution time in milliseconds
     */
    public double getAverageExecutionTime() {
        return totalExecutions > 0 ? (double) totalExecutionTime / totalExecutions : 0.0;
    }
}
