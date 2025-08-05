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
import org.openhab.core.ai.api.model.ModelProviderType;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unified Action Execution Service - Provides provider-agnostic action execution
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
 * LLM Request → UnifiedActionExecutionService → AgentActionDelegationService → Action Execution
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
@Component(service = UnifiedActionExecutionService.class)
@NonNullByDefault
public class UnifiedActionExecutionService {

    private static final Logger logger = LoggerFactory.getLogger(UnifiedActionExecutionService.class);

    // Performance monitoring
    private final AtomicLong totalActionExecutions = new AtomicLong(0);
    private final AtomicLong successfulActionExecutions = new AtomicLong(0);
    private final AtomicLong failedActionExecutions = new AtomicLong(0);
    private final AtomicLong totalExecutionTime = new AtomicLong(0);
    private final AtomicLong totalRetryAttempts = new AtomicLong(0);

    // Caching
    private final ConcurrentHashMap<String, CachedActionResult> actionResultCache = new ConcurrentHashMap<>();
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

        try {
            // Validate action name
            Map<String, Object> protocolContext = actionContext.getProtocolContext();
            String actionName = (String) protocolContext.get("action");
            if (actionName == null || actionName.isEmpty()) {
                ActionResult validationError = ActionResult.error("Action execution failed",
                        new ActionError("VALIDATION_ERROR", "Action name cannot be null or empty"),
                        Duration.between(startTime, Instant.now()).toMillis());
                failedActionExecutions.incrementAndGet();
                return CompletableFuture.completedFuture(validationError);
            }

            // Security validation
            if (enableSecurityValidation.get() && !validateActionSecurity(actionContext)) {
                ActionResult securityError = ActionResult.error("Action execution blocked by security validation",
                        new ActionError("SECURITY_VIOLATION", "Action failed security validation"),
                        Duration.between(startTime, Instant.now()).toMillis());
                failedActionExecutions.incrementAndGet();
                return CompletableFuture.completedFuture(securityError);
            }

            // Check cache first
            String cacheKey = generateCacheKey(actionContext);
            if (enableCaching.get() && cacheKey != null) {
                CachedActionResult cachedResult = actionResultCache.get(cacheKey);
                if (cachedResult != null && !cachedResult.isExpired()) {
                    logger.debug("Returning cached result for action: {}", actionContext.getCorrelationId());
                    successfulActionExecutions.incrementAndGet();
                    return CompletableFuture.completedFuture(cachedResult.getResult());
                }
            }

            // Execute action based on provider type
            CompletableFuture<ActionResult> executionFuture = executeActionByProvider(actionContext, providerType);

            // Add retry logic
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
     * Cache action result
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
        actionResultCache.put(cacheKey, new CachedActionResult(result, expirationTime));
    }

    /**
     * Clear action result cache
     */
    public void clearCache() {
        actionResultCache.clear();
        logger.info("Action result cache cleared");
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
    public PerformanceMetrics getPerformanceMetrics() {
        return PerformanceMetrics.builder().totalExecutions(totalActionExecutions.get())
                .successfulExecutions(successfulActionExecutions.get()).failedExecutions(failedActionExecutions.get())
                .totalExecutionTime(totalExecutionTime.get()).totalRetryAttempts(totalRetryAttempts.get())
                .cacheSize(actionResultCache.size()).build();
    }

    /**
     * Update service configuration
     */
    public void updateConfiguration(Configuration config) {
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
    public static class Configuration {
        public @Nullable Integer maxRetryAttempts;
        public @Nullable Duration retryDelay;
        public @Nullable Boolean enableCaching;
        public @Nullable Boolean enableSecurityValidation;
        public @Nullable Duration cacheExpiration;

        public static Configuration builder() {
            return new Configuration();
        }

        public Configuration maxRetryAttempts(int maxRetryAttempts) {
            this.maxRetryAttempts = maxRetryAttempts;
            return this;
        }

        public Configuration retryDelay(Duration retryDelay) {
            this.retryDelay = retryDelay;
            return this;
        }

        public Configuration enableCaching(boolean enableCaching) {
            this.enableCaching = enableCaching;
            return this;
        }

        public Configuration enableSecurityValidation(boolean enableSecurityValidation) {
            this.enableSecurityValidation = enableSecurityValidation;
            return this;
        }

        public Configuration cacheExpiration(Duration cacheExpiration) {
            this.cacheExpiration = cacheExpiration;
            return this;
        }
    }

    /**
     * Cached action result
     */
    private static class CachedActionResult {
        private final ActionResult result;
        private final Instant expirationTime;

        public CachedActionResult(ActionResult result, Instant expirationTime) {
            this.result = result;
            this.expirationTime = expirationTime;
        }

        public ActionResult getResult() {
            return result;
        }

        public boolean isExpired() {
            return Instant.now().isAfter(expirationTime);
        }
    }

    /**
     * Performance metrics data class
     */
    public static class PerformanceMetrics {
        private final long totalExecutions;
        private final long successfulExecutions;
        private final long failedExecutions;
        private final long totalExecutionTime;
        private final long totalRetryAttempts;
        private final int cacheSize;

        private PerformanceMetrics(Builder builder) {
            this.totalExecutions = builder.totalExecutions;
            this.successfulExecutions = builder.successfulExecutions;
            this.failedExecutions = builder.failedExecutions;
            this.totalExecutionTime = builder.totalExecutionTime;
            this.totalRetryAttempts = builder.totalRetryAttempts;
            this.cacheSize = builder.cacheSize;
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

        public long getTotalExecutionTime() {
            return totalExecutionTime;
        }

        public long getTotalRetryAttempts() {
            return totalRetryAttempts;
        }

        public int getCacheSize() {
            return cacheSize;
        }

        public double getSuccessRate() {
            return totalExecutions > 0 ? (double) successfulExecutions / totalExecutions : 0.0;
        }

        public double getAverageExecutionTime() {
            return totalExecutions > 0 ? (double) totalExecutionTime / totalExecutions : 0.0;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private long totalExecutions;
            private long successfulExecutions;
            private long failedExecutions;
            private long totalExecutionTime;
            private long totalRetryAttempts;
            private int cacheSize;

            public Builder totalExecutions(long totalExecutions) {
                this.totalExecutions = totalExecutions;
                return this;
            }

            public Builder successfulExecutions(long successfulExecutions) {
                this.successfulExecutions = successfulExecutions;
                return this;
            }

            public Builder failedExecutions(long failedExecutions) {
                this.failedExecutions = failedExecutions;
                return this;
            }

            public Builder totalExecutionTime(long totalExecutionTime) {
                this.totalExecutionTime = totalExecutionTime;
                return this;
            }

            public Builder totalRetryAttempts(long totalRetryAttempts) {
                this.totalRetryAttempts = totalRetryAttempts;
                return this;
            }

            public Builder cacheSize(int cacheSize) {
                this.cacheSize = cacheSize;
                return this;
            }

            public PerformanceMetrics build() {
                return new PerformanceMetrics(this);
            }
        }
    }
}
