package org.openhab.core.ai.model;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.model.api.ModelProviderType;
import org.openhab.core.ai.common.monitoring.api.MetricsService;

/**
 * Aggregated usage stats per provider across models and agents.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ProviderUsageStats {
    private final ModelProviderType providerType;
    private final Map<String, Integer> modelUsage = new ConcurrentHashMap<>();
    private final Map<String, Integer> agentUsage = new ConcurrentHashMap<>();
    // Performance monitoring - migrated to MetricsService
    // private final AtomicLong totalRequests = new AtomicLong(0);
    // private final AtomicLong totalTokens = new AtomicLong(0);
    // private final AtomicLong totalCost = new AtomicLong(0);
    // private final AtomicLong totalResponseTime = new AtomicLong(0);
    // private final AtomicLong successfulRequests = new AtomicLong(0);
    // private final AtomicLong failedRequests = new AtomicLong(0);

    private MetricsService metricsService;

    public ProviderUsageStats(ModelProviderType providerType) {
        this.providerType = providerType;
    }

    public void setMetricsService(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    public void recordUsage(String agentId, String modelName) {
        modelUsage.merge(modelName, 1, Integer::sum);
        agentUsage.merge(agentId, 1, Integer::sum);
    }

    public void recordRequest(int tokens, double cost, long responseTime, boolean success) {
        if (metricsService != null) {
            try {
                metricsService.recordOperation("provider_usage", "request")
                    .withSuccess(success)
                    .withDuration(Duration.ofMillis(responseTime).toNanos())
                    .withData("providerType", providerType.name())
                    .withData("tokens", tokens)
                    .withData("cost", cost)
                    .withData("responseTimeMs", responseTime)
                    .record();
            } catch (Exception e) {
                // Fallback to local logging if MetricsService fails
                System.err.println("Failed to record provider usage metrics: " + e.getMessage());
            }
        }
    }

    public ModelProviderType getProviderType() {
        return providerType;
    }

    public Map<String, Integer> getModelUsage() {
        return new HashMap<>(modelUsage);
    }

    public Map<String, Integer> getAgentUsage() {
        return new HashMap<>(agentUsage);
    }

    public long getTotalRequests() {
        // Placeholder - would need MetricsService to implement getOperationCount
        return 0L;
    }

    public long getTotalTokens() {
        // Placeholder - would need MetricsService to implement getOperationCount
        return 0L;
    }

    public double getTotalCost() {
        // Placeholder - would need MetricsService to implement getOperationCount
        return 0.0;
    }

    public long getTotalResponseTime() {
        // Placeholder - would need MetricsService to implement getOperationCount
        return 0L;
    }

    public long getSuccessfulRequests() {
        // Placeholder - would need MetricsService to implement getOperationCount
        return 0L;
    }

    public long getFailedRequests() {
        // Placeholder - would need MetricsService to implement getOperationCount
        return 0L;
    }

    public double getSuccessRate() {
        long total = getTotalRequests();
        return total > 0 ? (double) getSuccessfulRequests() / total : 0.0;
    }

    public double getAverageResponseTime() {
        long total = getTotalRequests();
        return total > 0 ? (double) getTotalResponseTime() / total : 0.0;
    }
}
