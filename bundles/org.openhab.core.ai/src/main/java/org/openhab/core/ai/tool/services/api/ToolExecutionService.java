package org.openhab.core.ai.tool.services.api;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.action.ActionContext;
import org.openhab.core.ai.action.ActionResult;
import org.openhab.core.ai.model.api.ModelProviderType;

/**
 * Tool Execution Service Interface
 * 
 * <p>
 * This interface defines the contract for tool execution services that provide:
 * - Intelligent tool execution with fallback mechanisms
 * - Load balancing across multiple tool providers
 * - Provider selection algorithms based on performance and cost
 * - Cost optimization for tool execution
 * - Privacy-aware routing for sensitive operations
 * - Performance monitoring and metrics collection
 * </p>
 * 
 * <h3>Execution Flow</h3>
 * 
 * <pre>{@code
 * Tool Request → Provider Selection → Load Balancing → Execution → Fallback (if needed)
 *      ↓              ↓                   ↓              ↓              ↓
 * Privacy Check → Cost Analysis → Performance Monitoring → Result Optimization
 * }</pre>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface ToolExecutionService {

    /**
     * Execute a tool with hybrid service capabilities
     * 
     * @param actionContext the action context containing execution details
     * @param availableProviders list of available providers
     * @return CompletableFuture with the action result
     */
    CompletableFuture<ActionResult> executeTool(ActionContext actionContext,
            List<ModelProviderType> availableProviders);

    // Configuration methods
    /**
     * Enable or disable fallback mechanism
     * 
     * @param enable true to enable fallback, false to disable
     */
    void setEnableFallback(boolean enable);

    /**
     * Enable or disable load balancing
     * 
     * @param enable true to enable load balancing, false to disable
     */
    void setEnableLoadBalancing(boolean enable);

    /**
     * Enable or disable cost optimization
     * 
     * @param enable true to enable cost optimization, false to disable
     */
    void setEnableCostOptimization(boolean enable);

    /**
     * Enable or disable privacy routing
     * 
     * @param enable true to enable privacy routing, false to disable
     */
    void setEnablePrivacyRouting(boolean enable);

    /**
     * Set the load balancing strategy
     * 
     * @param strategy the load balancing strategy to use
     */
    void setLoadBalancingStrategy(LoadBalancingStrategy strategy);

    /**
     * Set the maximum number of fallback attempts
     * 
     * @param maxAttempts the maximum number of fallback attempts
     */
    void setMaxFallbackAttempts(int maxAttempts);

    // Metrics methods
    /**
     * Get comprehensive metrics for the hybrid service
     * 
     * @return HybridServiceMetrics containing all service metrics
     */
    HybridServiceMetrics getMetrics();

    /**
     * Reset all metrics to zero
     */
    void resetMetrics();

    // Enums and inner classes
    /**
     * Load balancing strategies for tool execution
     */
    enum LoadBalancingStrategy {
        ROUND_ROBIN,
        LEAST_CONNECTIONS,
        WEIGHTED_RESPONSE_TIME,
        HEALTH_BASED
    }

    /**
     * Comprehensive metrics for the hybrid tool service
     */
    class HybridServiceMetrics {
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

        public long getTotalExecutions() {
            return totalExecutions;
        }

        public long getSuccessfulExecutions() {
            return successfulExecutions;
        }

        public long getFailedExecutions() {
            return failedExecutions;
        }

        public long getFallbackExecutions() {
            return fallbackExecutions;
        }

        public long getTotalExecutionTime() {
            return totalExecutionTime;
        }

        public long getTotalCost() {
            return totalCost;
        }

        public Map<ModelProviderType, ProviderMetrics> getProviderMetrics() {
            return providerMetrics;
        }

        public Map<String, ToolMetrics> getToolMetrics() {
            return toolMetrics;
        }

        public double getSuccessRate() {
            return totalExecutions > 0 ? (double) successfulExecutions / totalExecutions : 0.0;
        }

        public double getAverageExecutionTime() {
            return totalExecutions > 0 ? (double) totalExecutionTime / totalExecutions : 0.0;
        }
    }

    /**
     * Metrics for individual tool providers
     */
    class ProviderMetrics {
        private final AtomicLong totalExecutions = new AtomicLong(0);
        private final AtomicLong successfulExecutions = new AtomicLong(0);
        private final AtomicLong totalExecutionTime = new AtomicLong(0);
        private final AtomicLong totalFailures = new AtomicLong(0);

        public void recordExecution(boolean success, long executionTime) {
            totalExecutions.incrementAndGet();
            totalExecutionTime.addAndGet(executionTime);

            if (success) {
                successfulExecutions.incrementAndGet();
            } else {
                totalFailures.incrementAndGet();
            }
        }

        public long getTotalExecutions() {
            return totalExecutions.get();
        }

        public long getSuccessfulExecutions() {
            return successfulExecutions.get();
        }

        public long getTotalExecutionTime() {
            return totalExecutionTime.get();
        }

        public long getTotalFailures() {
            return totalFailures.get();
        }

        public double getSuccessRate() {
            return totalExecutions.get() > 0 ? (double) successfulExecutions.get() / totalExecutions.get() : 0.0;
        }

        public double getAverageResponseTime() {
            return totalExecutions.get() > 0 ? (double) totalExecutionTime.get() / totalExecutions.get() : 0.0;
        }

        public double getHealthScore() {
            return getSuccessRate() * (1.0 - Math.min(getAverageResponseTime() / 10000.0, 1.0));
        }
    }

    /**
     * Metrics for individual tools
     */
    class ToolMetrics {
        private final AtomicLong totalExecutions = new AtomicLong(0);
        private final AtomicLong successfulExecutions = new AtomicLong(0);
        private final AtomicLong totalExecutionTime = new AtomicLong(0);

        public void recordExecution(boolean success, long executionTime) {
            totalExecutions.incrementAndGet();
            totalExecutionTime.addAndGet(executionTime);

            if (success) {
                successfulExecutions.incrementAndGet();
            }
        }

        public long getTotalExecutions() {
            return totalExecutions.get();
        }

        public long getSuccessfulExecutions() {
            return successfulExecutions.get();
        }

        public long getTotalExecutionTime() {
            return totalExecutionTime.get();
        }

        public double getSuccessRate() {
            return totalExecutions.get() > 0 ? (double) successfulExecutions.get() / totalExecutions.get() : 0.0;
        }

        public double getAverageExecutionTime() {
            return totalExecutions.get() > 0 ? (double) totalExecutionTime.get() / totalExecutions.get() : 0.0;
        }
    }
}
