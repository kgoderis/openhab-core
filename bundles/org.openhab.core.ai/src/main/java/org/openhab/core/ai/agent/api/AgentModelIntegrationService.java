package org.openhab.core.ai.agent.api;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.context.AgentModelContext;
import org.openhab.core.ai.common.monitoring.api.HealthMetrics;
import org.openhab.core.ai.common.response.ModelResponse;
import org.openhab.core.ai.model.ModelParameters;

/**
 * Service for integrating AI models directly into autonomous agents
 * 
 * <p>
 * This service provides:
 * - Shared model brain architecture for resource optimization
 * - Agent-specific model access and context management
 * - Concurrent request handling and resource management
 * - Model session pooling and optimization
 * - Model request queuing and prioritization
 * - Model response caching and optimization
 * - Comprehensive error handling and fallback mechanisms
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public interface AgentModelIntegrationService {

    /**
     * Execute model reasoning for a specific agent
     * 
     * @param agentId the agent ID requesting model access
     * @param prompt the reasoning prompt
     * @param context the agent-specific context
     * @param parameters model parameters (optional, uses defaults if null)
     * @return CompletableFuture with the model response
     */
    CompletableFuture<ModelResponse> reasonAsync(String agentId, String prompt, Map<String, Object> context,
            @Nullable ModelParameters parameters);

    /**
     * Execute model reasoning with agent-specific optimization
     * 
     * @param agentId the agent ID requesting model access
     * @param prompt the reasoning prompt
     * @param context the agent-specific context
     * @param optimizationHints hints for model optimization
     * @param parameters model parameters (optional, uses defaults if null)
     * @return CompletableFuture with the model response
     */
    CompletableFuture<ModelResponse> reasonWithOptimizationAsync(String agentId, String prompt,
            Map<String, Object> context, Map<String, Object> optimizationHints, @Nullable ModelParameters parameters);

    /**
     * Get agent-specific model provider
     * 
     * @param agentId the agent ID
     * @return the agent-specific model provider
     */
    AgentModelProvider getAgentModelProvider(String agentId);

    /**
     * Register an agent for model integration
     * 
     * @param agentId the agent ID
     * @param agentContext the agent context information
     * @return true if registration was successful
     */
    boolean registerAgent(String agentId, AgentModelContext agentContext);

    /**
     * Unregister an agent from model integration
     * 
     * @param agentId the agent ID
     * @return true if unregistration was successful
     */
    boolean unregisterAgent(String agentId);

    /**
     * Get model integration statistics for an agent
     * 
     * @param agentId the agent ID
     * @return the model integration statistics
     */
    Object getAgentStatistics(String agentId);

    /**
     * Get overall model integration statistics
     * 
     * @return the overall model integration statistics
     */
    Object getOverallStatistics();

    /**
     * Check if an agent is registered for model integration
     * 
     * @param agentId the agent ID
     * @return true if the agent is registered
     */
    boolean isAgentRegistered(String agentId);

    /**
     * Get list of registered agent IDs
     * 
     * @return list of registered agent IDs
     */
    List<String> getRegisteredAgentIds();

    /**
     * Update agent context for model integration
     * 
     * @param agentId the agent ID
     * @param context the updated agent context
     * @return true if update was successful
     */
    boolean updateAgentContext(String agentId, AgentModelContext context);

    /**
     * Get agent context for model integration
     * 
     * @param agentId the agent ID
     * @return the agent context, or null if not found
     */
    @Nullable
    AgentModelContext getAgentContext(String agentId);

    /**
     * Clear model cache for a specific agent
     * 
     * @param agentId the agent ID
     * @return true if cache was cleared successfully
     */
    boolean clearAgentCache(String agentId);

    /**
     * Clear all model caches
     * 
     * @return true if all caches were cleared successfully
     */
    boolean clearAllCaches();

    /**
     * Get model health status
     * 
     * @return the model health status
     */
    HealthMetrics getModelHealthMetrics();

    /**
     * Force model fallback for testing or emergency situations
     * 
     * @param agentId the agent ID
     * @param fallbackModel the fallback model to use
     * @return true if fallback was activated successfully
     */
    boolean forceModelFallback(String agentId, String fallbackModel);

    /**
     * Reset model fallback for an agent
     * 
     * @param agentId the agent ID
     * @return true if fallback was reset successfully
     */
    boolean resetModelFallback(String agentId);
}
