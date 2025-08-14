package org.openhab.core.ai.tool.services.api;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.model.api.ModelProviderType;

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

    public long getTotalExecutions() { return totalExecutions; }
    public long getSuccessfulExecutions() { return successfulExecutions; }
    public long getFailedExecutions() { return failedExecutions; }
    public long getFallbackExecutions() { return fallbackExecutions; }
    public long getTotalExecutionTime() { return totalExecutionTime; }
    public long getTotalCost() { return totalCost; }
    public Map<ModelProviderType, ProviderMetrics> getProviderMetrics() { return providerMetrics; }
    public Map<String, ToolMetrics> getToolMetrics() { return toolMetrics; }
    public double getSuccessRate() { return totalExecutions > 0 ? (double) successfulExecutions / totalExecutions : 0.0; }
    public double getAverageExecutionTime() { return totalExecutions > 0 ? (double) totalExecutionTime / totalExecutions : 0.0; }
}
