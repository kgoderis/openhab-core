package org.openhab.core.ai.tool.monitoring;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.model.api.ModelProviderType;
import org.openhab.core.ai.tool.services.ProviderMetrics;
import org.openhab.core.ai.tool.services.ToolMetrics;

/**
 * Metrics for hybrid service operations.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class HybridServiceMetrics {
    private final long totalToolExecutions;
    private final long successfulToolExecutions;
    private final long failedToolExecutions;
    private final long fallbackExecutions;
    private final long totalExecutionTime;
    private final double totalCost;
    private final Map<ModelProviderType, ProviderMetrics> providerMetrics;
    private final Map<String, ToolMetrics> toolMetrics;

    public HybridServiceMetrics(long totalToolExecutions, long successfulToolExecutions, long failedToolExecutions,
            long fallbackExecutions, long totalExecutionTime, double totalCost,
            Map<ModelProviderType, ProviderMetrics> providerMetrics, Map<String, ToolMetrics> toolMetrics) {
        this.totalToolExecutions = totalToolExecutions;
        this.successfulToolExecutions = successfulToolExecutions;
        this.failedToolExecutions = failedToolExecutions;
        this.fallbackExecutions = fallbackExecutions;
        this.totalExecutionTime = totalExecutionTime;
        this.totalCost = totalCost;
        this.providerMetrics = new ConcurrentHashMap<>(providerMetrics);
        this.toolMetrics = new ConcurrentHashMap<>(toolMetrics);
    }

    public long getTotalToolExecutions() {
        return totalToolExecutions;
    }

    public long getSuccessfulToolExecutions() {
        return successfulToolExecutions;
    }

    public long getFailedToolExecutions() {
        return failedToolExecutions;
    }

    public long getFallbackExecutions() {
        return fallbackExecutions;
    }

    public long getTotalExecutionTime() {
        return totalExecutionTime;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public Map<ModelProviderType, ProviderMetrics> getProviderMetrics() {
        return new ConcurrentHashMap<>(providerMetrics);
    }

    public Map<String, ToolMetrics> getToolMetrics() {
        return new ConcurrentHashMap<>(toolMetrics);
    }

    public double getSuccessRate() {
        return totalToolExecutions > 0 ? (double) successfulToolExecutions / totalToolExecutions : 0.0;
    }

    public double getAverageExecutionTime() {
        return totalToolExecutions > 0 ? (double) totalExecutionTime / totalToolExecutions : 0.0;
    }
}
