package org.openhab.core.ai.agent.api;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.configuration.AgentModelConfiguration;
import org.openhab.core.ai.common.response.ModelResponse;
import org.openhab.core.ai.common.statistics.AgentModelStatistics;
import org.openhab.core.ai.common.statistics.ModelHealthStatus;
import org.openhab.core.ai.model.ModelParameters;

/**
 * Agent-specific model provider interface
 * 
 * <p>
 * This interface provides:
 * - Agent-specific model access with shared brain integration
 * - Agent-specific prompt templates and context builders
 * - Agent-specific model selection logic
 * - Agent-specific model parameter optimization
 * - Agent-specific model response processing and validation
 * - Agent-specific model error handling and recovery
 * - Agent-specific model performance monitoring
 * - Agent-specific model security and access controls
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public interface AgentModelProvider {

    /**
     * Get the agent ID this provider serves
     * 
     * @return the agent ID
     */
    String getAgentId();

    /**
     * Execute model reasoning for this agent
     * 
     * @param prompt the reasoning prompt
     * @param context the agent-specific context
     * @param parameters model parameters (optional, uses defaults if null)
     * @return CompletableFuture with the model response
     */
    CompletableFuture<ModelResponse> reasonAsync(String prompt, Map<String, Object> context,
            @Nullable ModelParameters parameters);

    /**
     * Execute model reasoning with optimization hints
     * 
     * @param prompt the reasoning prompt
     * @param context the agent-specific context
     * @param optimizationHints hints for model optimization
     * @param parameters model parameters (optional, uses defaults if null)
     * @return CompletableFuture with the model response
     */
    CompletableFuture<ModelResponse> reasonWithOptimizationAsync(String prompt, Map<String, Object> context,
            Map<String, Object> optimizationHints, @Nullable ModelParameters parameters);

    /**
     * Get agent-specific prompt template
     * 
     * @param templateName the template name
     * @param variables the template variables
     * @return the rendered prompt
     */
    String getPromptTemplate(String templateName, Map<String, Object> variables);

    /**
     * Build agent-specific context
     * 
     * @param baseContext the base context
     * @return the enhanced agent-specific context
     */
    Map<String, Object> buildAgentContext(Map<String, Object> baseContext);

    /**
     * Get agent-specific model parameters
     * 
     * @param baseParameters the base parameters
     * @return the optimized agent-specific parameters
     */
    ModelParameters getOptimizedParameters(@Nullable ModelParameters baseParameters);

    /**
     * Process model response for this agent
     * 
     * @param response the raw model response
     * @return the processed response
     */
    ModelResponse processResponse(ModelResponse response);

    /**
     * Validate model response for this agent
     * 
     * @param response the model response to validate
     * @return true if the response is valid for this agent
     */
    boolean validateResponse(ModelResponse response);

    /**
     * Get agent-specific model statistics
     * 
     * @return the agent-specific model statistics
     */
    AgentModelStatistics getStatistics();

    /**
     * Get agent-specific model health status
     * 
     * @return the agent-specific model health status
     */
    ModelHealthStatus getHealthStatus();

    /**
     * Update agent-specific configuration
     * 
     * @param configuration the new configuration
     * @return true if update was successful
     */
    boolean updateConfiguration(AgentModelConfiguration configuration);

    /**
     * Get agent-specific configuration
     * 
     * @return the agent-specific configuration
     */
    AgentModelConfiguration getConfiguration();

    /**
     * Clear agent-specific cache
     * 
     * @return true if cache was cleared successfully
     */
    boolean clearCache();

    /**
     * Force model fallback for this agent
     * 
     * @param fallbackModel the fallback model to use
     * @return true if fallback was activated successfully
     */
    boolean forceFallback(String fallbackModel);

    /**
     * Reset model fallback for this agent
     * 
     * @return true if fallback was reset successfully
     */
    boolean resetFallback();
}
