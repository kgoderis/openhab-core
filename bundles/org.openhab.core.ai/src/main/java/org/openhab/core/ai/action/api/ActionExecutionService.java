package org.openhab.core.ai.action.api;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.model.api.ModelProviderType;

/**
 * Action Execution Service Interface
 * 
 * <p>
 * This interface defines the contract for action execution services that provide:
 * - Provider-agnostic action execution abstraction
 * - Action execution for both local and remote LLMs
 * - Agent-based action delegation for local LLMs
 * - Unified error handling for all action types
 * - Action execution performance monitoring
 * - Action execution retry mechanisms
 * - Action result caching and optimization
 * - Action execution security and validation
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface ActionExecutionService {

    /**
     * Execute an action with provider-agnostic abstraction
     * 
     * @param actionContext the action context containing execution details
     * @param providerType the LLM provider type
     * @return CompletableFuture with the action result
     */
    CompletableFuture<ActionResult> executeAction(ActionContext actionContext, ModelProviderType providerType);

    /**
     * Execute an action with retry mechanism
     * 
     * @param actionContext the action context containing execution details
     * @param providerType the LLM provider type
     * @param maxRetries the maximum number of retry attempts
     * @return CompletableFuture with the action result
     */
    CompletableFuture<ActionResult> executeActionWithRetry(ActionContext actionContext, ModelProviderType providerType,
            int maxRetries);

    /**
     * Execute multiple actions in parallel
     * 
     * @param actionContexts the list of action contexts
     * @param providerType the LLM provider type
     * @return CompletableFuture with the list of action results
     */
    CompletableFuture<List<ActionResult>> executeActionsParallel(List<ActionContext> actionContexts,
            ModelProviderType providerType);

    /**
     * Execute multiple actions sequentially
     * 
     * @param actionContexts the list of action contexts
     * @param providerType the LLM provider type
     * @return CompletableFuture with the list of action results
     */
    CompletableFuture<List<ActionResult>> executeActionsSequential(List<ActionContext> actionContexts,
            ModelProviderType providerType);

    /**
     * Get action execution statistics
     * 
     * @return map of execution statistics
     */
    Map<String, Object> getExecutionStatistics();

    /**
     * Get action execution performance metrics
     * 
     * @return map of performance metrics
     */
    Map<String, Object> getPerformanceMetrics();

    /**
     * Clear action result cache
     * 
     * @return true if cache was cleared successfully
     */
    boolean clearCache();

    /**
     * Set cache expiration duration
     * 
     * @param expiration the cache expiration duration
     */
    void setCacheExpiration(Duration expiration);

    /**
     * Get cache expiration duration
     * 
     * @return the cache expiration duration
     */
    Duration getCacheExpiration();

    /**
     * Set maximum retry attempts
     * 
     * @param maxRetries the maximum number of retry attempts
     */
    void setMaxRetryAttempts(int maxRetries);

    /**
     * Get maximum retry attempts
     * 
     * @return the maximum number of retry attempts
     */
    int getMaxRetryAttempts();

    /**
     * Set retry delay duration
     * 
     * @param retryDelay the retry delay duration
     */
    void setRetryDelay(Duration retryDelay);

    /**
     * Get retry delay duration
     * 
     * @return the retry delay duration
     */
    Duration getRetryDelay();

    /**
     * Enable or disable caching
     * 
     * @param enable true to enable caching, false to disable
     */
    void setCachingEnabled(boolean enable);

    /**
     * Check if caching is enabled
     * 
     * @return true if caching is enabled
     */
    boolean isCachingEnabled();

    /**
     * Enable or disable security validation
     * 
     * @param enable true to enable security validation, false to disable
     */
    void setSecurityValidationEnabled(boolean enable);

    /**
     * Check if security validation is enabled
     * 
     * @return true if security validation is enabled
     */
    boolean isSecurityValidationEnabled();
}
