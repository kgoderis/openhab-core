package org.openhab.core.ai.action;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.action.api.ActionContext;
import org.openhab.core.ai.action.api.ActionError;
import org.openhab.core.ai.action.api.ActionExecutionService;
import org.openhab.core.ai.action.api.ActionResult;
import org.openhab.core.ai.action.api.ActionSecurityValidator;
import org.openhab.core.ai.agent.delegation.api.AgentActionDelegationService;
import org.openhab.core.ai.model.api.ModelProviderType;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default Action Execution Service - Provides provider-agnostic action execution
 * 
 * <p>
 * This service provides:
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
 * <h3>Execution Flow</h3>
 * 
 * <pre>{@code
 * LLM Request → DefaultActionExecutionService → AgentActionDelegationService → Action Execution
 *                                    ↓
 *                              Performance Monitoring
 *                                    ↓
 *                              Result Caching
 *                                    ↓
 *                              Security Validation
 * }</pre>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@Component(service = ActionExecutionService.class)
@NonNullByDefault
public class DefaultActionExecutionService implements ActionExecutionService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultActionExecutionService.class);

    // Performance monitoring
    private final AtomicLong totalActionExecutions = new AtomicLong(0);
    private final AtomicLong successfulActionExecutions = new AtomicLong(0);
    private final AtomicLong failedActionExecutions = new AtomicLong(0);
    private final AtomicLong totalExecutionTime = new AtomicLong(0);
    private final AtomicLong totalRetryAttempts = new AtomicLong(0);

    // Enhanced caching using ActionCacheEntry
    private final ConcurrentHashMap<String, ActionCacheEntry> actionResultCache = new ConcurrentHashMap<>();
    private final AtomicReference<Duration> cacheExpiration = new AtomicReference<>(Duration.ofMinutes(5));

    // Configuration
    private final AtomicReference<Integer> maxRetryAttempts = new AtomicReference<>(3);
    private final AtomicReference<Duration> retryDelay = new AtomicReference<>(Duration.ofSeconds(1));
    private final AtomicReference<Boolean> enableCaching = new AtomicReference<>(true);
    private final AtomicReference<Boolean> enableSecurityValidation = new AtomicReference<>(true);

    @Reference
    private @Nullable AgentActionDelegationService agentDelegationService;

    @Reference
    private @Nullable ActionRegistry actionRegistry;

    @Reference
    private @Nullable ActionSecurityValidator securityValidator;

    /**
     * Execute an action with provider-agnostic abstraction
     * 
     * @param actionContext the action context containing execution details
     * @param providerType the LLM provider type
     * @return CompletableFuture with the action result
     */
    public CompletableFuture<ActionResult> executeAction(ActionContext actionContext, ModelProviderType providerType) {
        totalActionExecutions.incrementAndGet();
        Instant startTime = Instant.now();

        // Add debug logging
        Map<String, Object> protocolContext = actionContext.getProtocolContext();
        String actionName = (String) protocolContext.get("action");
        logger.debug("executeAction called for action: {}", actionName);

        try {
            // Validate provider type
            if (providerType == null) {
                logger.debug("Provider type validation failed for action: {}", actionName);
                ActionResult validationError = ActionResult.error("Action execution failed",
                        new ActionError("VALIDATION_ERROR", "Provider type cannot be null"),
                        Duration.between(startTime, Instant.now()).toMillis());
                failedActionExecutions.incrementAndGet();
                return CompletableFuture.completedFuture(validationError);
            }

            // Validate action name
            if (actionName == null || actionName.isEmpty()) {
                logger.debug("Action name validation failed for action: {}", actionName);
                ActionResult validationError = ActionResult.error("Action execution failed",
                        new ActionError("VALIDATION_ERROR", "Action name cannot be null or empty"),
                        Duration.between(startTime, Instant.now()).toMillis());
                failedActionExecutions.incrementAndGet();
                return CompletableFuture.completedFuture(validationError);
            }

            // Security validation
            if (enableSecurityValidation.get() && !validateActionSecurity(actionContext)) {
                logger.debug("Security validation failed for action: {}", actionName);
                ActionResult securityError = ActionResult.error("Action execution blocked by security validation",
                        new ActionError("SECURITY_VIOLATION", "Action failed security validation"),
                        Duration.between(startTime, Instant.now()).toMillis());
                failedActionExecutions.incrementAndGet();
                return CompletableFuture.completedFuture(securityError);
            }

            // Check cache first
            String cacheKey = generateCacheKey(actionContext);
            logger.debug("Cache key for action {}: {}", actionName, cacheKey);
            if (enableCaching.get() && cacheKey != null) {
                ActionCacheEntry cachedResult = actionResultCache.get(cacheKey);
                if (cachedResult != null && !cachedResult.isExpired()) {
                    logger.debug("Returning cached result for action: {}", actionName);
                    successfulActionExecutions.incrementAndGet();
                    // Update access count
                    actionResultCache.put(cacheKey, cachedResult.withAccess());
                    return CompletableFuture.completedFuture((ActionResult) cachedResult.getResult());
                }
            }

            // Execute action based on provider type
            logger.debug("About to call executeActionByProvider for action: {}", actionName);
            CompletableFuture<ActionResult> executionFuture = executeActionByProvider(actionContext, providerType);

            // Add retry logic
            logger.debug("About to add retry logic for action: {}", actionName);
            executionFuture = addRetryLogic(executionFuture, actionContext, providerType);

            // Handle result
            return executionFuture.thenApply(result -> {
                long executionTime = Duration.between(startTime, Instant.now()).toMillis();
                totalExecutionTime.addAndGet(executionTime);

                if (result.isSuccess()) {
                    successfulActionExecutions.incrementAndGet();
                    // Cache successful results
                    if (enableCaching.get() && cacheKey != null) {
                        cacheActionResult(cacheKey, result);
                    }
                } else {
                    failedActionExecutions.incrementAndGet();
                }

                logger.debug("Action execution completed in {} ms: {}", executionTime,
                        actionContext.getCorrelationId());
                return result;
            });

        } catch (Exception e) {
            failedActionExecutions.incrementAndGet();
            logger.error("Error executing action: {}", actionContext.getCorrelationId(), e);
            return CompletableFuture.completedFuture(
                    ActionResult.error("Action execution failed", new ActionError("EXECUTION_ERROR", e.getMessage()),
                            Duration.between(startTime, Instant.now()).toMillis()));
        }
    }

    /**
     * Execute multiple actions in parallel
     * 
     * @param actionContexts list of action contexts
     * @param providerType the LLM provider type
     * @return CompletableFuture with list of action results
     */
    public CompletableFuture<List<ActionResult>> executeActions(List<ActionContext> actionContexts,
            ModelProviderType providerType) {
        List<CompletableFuture<ActionResult>> futures = actionContexts.stream()
                .map(context -> executeAction(context, providerType)).toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream().map(CompletableFuture::join).toList());
    }

    /**
     * Execute action based on provider type
     */
    private CompletableFuture<ActionResult> executeActionByProvider(ActionContext actionContext,
            ModelProviderType providerType) {
        switch (providerType) {
            case OLLAMA:
            case LOCALAI:
            case VLLM:
            case LMSTUDIO:
                // Local LLMs use agent delegation
                return executeViaAgentDelegation(actionContext);

            case OPENAI:
            case ANTHROPIC:
            case GOOGLE:
            case AZURE:
                // Remote LLMs can use direct execution or agent delegation
                return executeViaAgentDelegation(actionContext);

            default:
                throw new IllegalArgumentException("Unsupported provider type: " + providerType);
        }
    }

    /**
     * Execute action via agent delegation
     */
    private CompletableFuture<ActionResult> executeViaAgentDelegation(ActionContext actionContext) {
        AgentActionDelegationService delegationService = agentDelegationService;
        if (delegationService == null) {
            throw new IllegalStateException("AgentActionDelegationService not available");
        }

        return delegationService.delegateAction(actionContext);
    }

    /**
     * Add retry logic to action execution
     */
    private CompletableFuture<ActionResult> addRetryLogic(CompletableFuture<ActionResult> future,
            ActionContext actionContext, ModelProviderType providerType) {
        if (future == null) {
            return CompletableFuture.completedFuture(ActionResult.error("Action execution failed",
                    new ActionError("EXECUTION_ERROR", "Future is null"), 0));
        }

        int maxRetries = maxRetryAttempts.get();
        Duration delay = retryDelay.get();

        CompletableFuture<ActionResult> result = future;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            final int currentAttempt = attempt;
            result = result.handle((actionResult, throwable) -> {
                if (throwable != null || (actionResult != null && !actionResult.isSuccess())) {
                    if (currentAttempt < maxRetries) {
                        totalRetryAttempts.incrementAndGet();
                        logger.debug("Retrying action execution (attempt {}/{}): {}", currentAttempt, maxRetries,
                                actionContext.getCorrelationId());

                        // Wait before retry
                        try {
                            Thread.sleep(delay.toMillis() * currentAttempt); // Exponential backoff
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return ActionResult.error("Action execution interrupted",
                                    new ActionError("INTERRUPTED", "Execution was interrupted during retry"), 0);
                        }

                        // Retry the action
                        return executeActionByProvider(actionContext, providerType).join();
                    }
                }
                return actionResult != null ? actionResult
                        : ActionResult.error("Action execution failed", new ActionError("EXECUTION_ERROR",
                                throwable != null ? throwable.getMessage() : "Unknown error"), 0);
            });
        }

        return result;
    }

    /**
     * Validate action security
     */
    private boolean validateActionSecurity(ActionContext actionContext) {
        ActionSecurityValidator validator = securityValidator;
        if (validator == null || !validator.isAvailable()) {
            logger.warn("Security validator not available, skipping validation");
            return true; // Allow execution if no validator available
        }
        return validator.validateAction(actionContext);
    }

    /**
     * Generate cache key for action context
     */
    private @Nullable String generateCacheKey(ActionContext actionContext) {
        String correlationId = actionContext.getCorrelationId();
        if (correlationId == null) {
            return null;
        }
        return "action_" + correlationId;
    }

    /**
     * Cache action result using enhanced ActionCacheEntry
     */
    private void cacheActionResult(@Nullable String cacheKey, ActionResult result) {
        if (cacheKey == null) {
            return;
        }
        Duration expiration = cacheExpiration.get();
        if (expiration == null) {
            expiration = Duration.ofMinutes(5); // Default fallback
        }
        Instant expirationTime = Instant.now().plus(expiration);
        ActionCacheEntry cachedResult = new ActionCacheEntry(cacheKey, Map.of(), result, Instant.now(), expirationTime);
        actionResultCache.put(cacheKey, cachedResult);
    }

    /**
     * Clear action result cache
     */
    @Override
    public boolean clearCache() {
        try {
            actionResultCache.clear();
            logger.info("Action result cache cleared");
            return true;
        } catch (Exception e) {
            logger.error("Failed to clear action result cache", e);
            return false;
        }
    }

    /**
     * Reset all metrics (for testing purposes)
     */
    public void resetMetrics() {
        totalActionExecutions.set(0);
        successfulActionExecutions.set(0);
        failedActionExecutions.set(0);
        totalExecutionTime.set(0);
        totalRetryAttempts.set(0);
        logger.debug("Performance metrics reset");
    }

    /**
     * Get performance metrics
     */
    @Override
    public Map<String, Object> getPerformanceMetrics() {
        Map<String, Object> metrics = new java.util.HashMap<>();
        ActionExecutionPerformanceMetrics perfMetrics = new ActionExecutionPerformanceMetrics(
                totalActionExecutions.get(), successfulActionExecutions.get(), failedActionExecutions.get(),
                totalExecutionTime.get(), totalRetryAttempts.get(), actionResultCache.size());

        metrics.put("totalExecutions", perfMetrics.getTotalExecutions());
        metrics.put("successfulExecutions", perfMetrics.getSuccessfulExecutions());
        metrics.put("failedExecutions", perfMetrics.getFailedExecutions());
        metrics.put("totalExecutionTime", perfMetrics.getTotalExecutionTime());
        metrics.put("totalRetryAttempts", perfMetrics.getTotalRetryAttempts());
        metrics.put("cacheSize", perfMetrics.getCacheSize());
        metrics.put("successRate", perfMetrics.getSuccessRate());
        metrics.put("averageExecutionTime", perfMetrics.getAverageExecutionTime());

        return metrics;
    }

    /**
     * Update service configuration
     */
    public void updateConfiguration(ActionExecutionConfiguration config) {
        Integer maxRetries = config.maxRetryAttempts;
        if (maxRetries != null) {
            maxRetryAttempts.set(maxRetries);
        }
        Duration delay = config.retryDelay;
        if (delay != null) {
            retryDelay.set(delay);
        }
        Boolean caching = config.enableCaching;
        if (caching != null) {
            enableCaching.set(caching);
        }
        Boolean security = config.enableSecurityValidation;
        if (security != null) {
            enableSecurityValidation.set(security);
        }
        Duration expiration = config.cacheExpiration;
        if (expiration != null) {
            cacheExpiration.set(expiration);
        }

        logger.info("UnifiedActionExecutionService configuration updated");
    }

    /**
     * Configuration class
     */
    // extracted to top-level: ActionExecutionConfiguration

    /**
     * Performance metrics data class
     */
    // extracted to top-level: ActionExecutionPerformanceMetrics

    @Override
    public CompletableFuture<ActionResult> executeActionWithRetry(ActionContext actionContext,
            ModelProviderType providerType, int maxRetries) {
        return executeAction(actionContext, providerType).thenCompose(result -> {
            if (!result.isSuccess() && maxRetries > 0) {
                logger.debug("Retrying action execution, remaining retries: {}", maxRetries);
                return executeActionWithRetry(actionContext, providerType, maxRetries - 1);
            }
            return CompletableFuture.completedFuture(result);
        });
    }

    @Override
    public CompletableFuture<List<ActionResult>> executeActionsParallel(List<ActionContext> actionContexts,
            ModelProviderType providerType) {
        logger.debug("Starting parallel execution of {} actions", actionContexts.size());

        List<CompletableFuture<ActionResult>> futures = actionContexts.stream().map(context -> {
            String actionName = (String) context.getProtocolContext().get("action");
            logger.debug("Creating future for action: {}", actionName);
            return executeAction(context, providerType);
        }).collect(java.util.stream.Collectors.toList());

        logger.debug("Created {} futures for parallel execution", futures.size());

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenApply(v -> {
            logger.debug("All futures completed, collecting results");
            List<ActionResult> results = futures.stream().map(CompletableFuture::join)
                    .collect(java.util.stream.Collectors.toList());
            logger.debug("Collected {} results", results.size());
            return results;
        });
    }

    @Override
    public CompletableFuture<List<ActionResult>> executeActionsSequential(List<ActionContext> actionContexts,
            ModelProviderType providerType) {
        CompletableFuture<List<ActionResult>> result = CompletableFuture.completedFuture(new java.util.ArrayList<>());
        for (ActionContext context : actionContexts) {
            result = result.thenCompose(results -> executeAction(context, providerType).thenApply(actionResult -> {
                results.add(actionResult);
                return results;
            }));
        }
        return result;
    }

    @Override
    public Map<String, Object> getExecutionStatistics() {
        Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalExecutions", totalActionExecutions.get());
        stats.put("successfulExecutions", successfulActionExecutions.get());
        stats.put("failedExecutions", failedActionExecutions.get());
        stats.put("totalExecutionTime", totalExecutionTime.get());
        stats.put("totalRetryAttempts", totalRetryAttempts.get());
        stats.put("cacheSize", actionResultCache.size());
        return stats;
    }

    @Override
    public void setCacheExpiration(Duration expiration) {
        cacheExpiration.set(expiration);
        logger.debug("Cache expiration set to: {}", expiration);
    }

    @Override
    public Duration getCacheExpiration() {
        return cacheExpiration.get();
    }

    @Override
    public void setMaxRetryAttempts(int maxRetries) {
        maxRetryAttempts.set(maxRetries);
        logger.debug("Max retry attempts set to: {}", maxRetries);
    }

    @Override
    public int getMaxRetryAttempts() {
        return maxRetryAttempts.get();
    }

    @Override
    public void setRetryDelay(Duration retryDelay) {
        this.retryDelay.set(retryDelay);
        logger.debug("Retry delay set to: {}", retryDelay);
    }

    @Override
    public Duration getRetryDelay() {
        return retryDelay.get();
    }

    @Override
    public void setCachingEnabled(boolean enable) {
        enableCaching.set(enable);
        logger.debug("Caching enabled set to: {}", enable);
    }

    @Override
    public boolean isCachingEnabled() {
        return enableCaching.get();
    }

    @Override
    public void setSecurityValidationEnabled(boolean enable) {
        enableSecurityValidation.set(enable);
        logger.debug("Security validation enabled set to: {}", enable);
    }

    @Override
    public boolean isSecurityValidationEnabled() {
        return enableSecurityValidation.get();
    }
}
