package org.openhab.core.ai.tool.services.api;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.action.api.ActionResult;
import org.openhab.core.ai.common.context.ExecutionContext;
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
    CompletableFuture<ActionResult> executeTool(ExecutionContext actionContext,
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
}
