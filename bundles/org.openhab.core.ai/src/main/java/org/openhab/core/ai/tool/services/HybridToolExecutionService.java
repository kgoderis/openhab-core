package org.openhab.core.ai.tool.services;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.action.ActionContext;
import org.openhab.core.ai.action.ActionError;
import org.openhab.core.ai.action.ActionResult;
import org.openhab.core.ai.model.api.ModelProviderType;
import org.openhab.core.ai.tool.monitoring.DefaultSystemHealthMonitor;
import org.openhab.core.ai.tool.registry.ToolRegistry;
import org.openhab.core.ai.tool.resources.ResourceManager;
import org.openhab.core.ai.tool.services.api.ToolExecutionService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hybrid Tool Execution Service - Provides intelligent tool execution with fallback, load balancing, and optimization
 * 
 * <p>
 * This service provides:
 * - Fallback mechanism for tool execution failures
 * - Load balancing logic across multiple tool providers
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
@Component(service = ToolExecutionService.class)
@NonNullByDefault
public class HybridToolExecutionService implements ToolExecutionService {

    private static final Logger logger = LoggerFactory.getLogger(HybridToolExecutionService.class);

    // Performance monitoring
    private final AtomicLong totalToolExecutions = new AtomicLong(0);
    private final AtomicLong successfulToolExecutions = new AtomicLong(0);
    private final AtomicLong failedToolExecutions = new AtomicLong(0);
    private final AtomicLong fallbackExecutions = new AtomicLong(0);
    private final AtomicLong totalExecutionTime = new AtomicLong(0);
    private final AtomicLong totalCost = new AtomicLong(0);

    // Provider performance tracking
    private final ConcurrentHashMap<ModelProviderType, ProviderMetrics> providerMetrics = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ToolMetrics> toolMetrics = new ConcurrentHashMap<>();

    // Configuration
    private final AtomicReference<Boolean> enableFallback = new AtomicReference<>(true);
    private final AtomicReference<Boolean> enableLoadBalancing = new AtomicReference<>(true);
    private final AtomicReference<Boolean> enableCostOptimization = new AtomicReference<>(true);
    private final AtomicReference<Boolean> enablePrivacyRouting = new AtomicReference<>(true);
    private final AtomicReference<Integer> maxFallbackAttempts = new AtomicReference<>(3);
    private final AtomicReference<Duration> fallbackDelay = new AtomicReference<>(Duration.ofSeconds(1));

    // Load balancing state
    private final ConcurrentHashMap<ModelProviderType, AtomicLong> providerLoadCounters = new ConcurrentHashMap<>();
    private final AtomicReference<LoadBalancingStrategy> loadBalancingStrategy = new AtomicReference<>(
            LoadBalancingStrategy.ROUND_ROBIN);

    @Reference
    private @Nullable ToolRegistry toolRegistry;

    @Reference
    private @Nullable DefaultSystemHealthMonitor healthMonitor;

    @Reference
    private @Nullable ResourceManager resourceManager;

    @Override
    public CompletableFuture<ActionResult> executeTool(ActionContext actionContext,
            List<ModelProviderType> availableProviders) {
        totalToolExecutions.incrementAndGet();
        Instant startTime = Instant.now();

        try {
            // 1. Privacy-aware routing check
            if (enablePrivacyRouting.get() && isPrivacySensitive(actionContext)) {
                return executeWithPrivacyRouting(actionContext, availableProviders);
            }

            // 2. Provider selection with load balancing
            ModelProviderType selectedProvider = selectProvider(availableProviders, actionContext);

            // 3. Cost optimization check
            if (enableCostOptimization.get()) {
                selectedProvider = optimizeForCost(selectedProvider, availableProviders, actionContext);
            }

            // 4. Execute with fallback mechanism
            return executeWithFallback(actionContext, selectedProvider, availableProviders, startTime);

        } catch (Exception e) {
            failedToolExecutions.incrementAndGet();
            logger.error("Tool execution failed", e);
            return CompletableFuture.completedFuture(ActionResult.error("Tool execution failed",
                    new ActionError("EXECUTION_ERROR", e.getMessage(), "EXECUTION_ERROR", e),
                    Duration.between(startTime, Instant.now()).toMillis()));
        }
    }

    /**
     * Execute tool with privacy-aware routing
     */
    private CompletableFuture<ActionResult> executeWithPrivacyRouting(ActionContext actionContext,
            List<ModelProviderType> availableProviders) {
        // Select privacy-focused provider (e.g., local providers)
        ModelProviderType privacyProvider = selectPrivacyProvider(availableProviders);

        logger.debug("Using privacy-aware routing for action: {}", actionContext.getProtocolContext().get("action"));

        return executeWithFallback(actionContext, privacyProvider, availableProviders, Instant.now());
    }

    /**
     * Select provider based on load balancing strategy
     */
    private ModelProviderType selectProvider(List<ModelProviderType> availableProviders, ActionContext actionContext) {
        if (!enableLoadBalancing.get() || availableProviders.isEmpty()) {
            return availableProviders.get(0);
        }

        // Filter healthy providers
        List<ModelProviderType> healthyProviders = availableProviders.stream()
                .filter(provider -> isProviderHealthy(provider)).toList();

        if (healthyProviders.isEmpty()) {
            logger.warn("No healthy providers available, using first available provider");
            return availableProviders.get(0);
        }

        switch (loadBalancingStrategy.get()) {
            case ROUND_ROBIN:
                return selectRoundRobin(healthyProviders);
            case LEAST_CONNECTIONS:
                return selectLeastConnections(healthyProviders);
            case WEIGHTED_RESPONSE_TIME:
                return selectWeightedResponseTime(healthyProviders);
            case HEALTH_BASED:
                return selectHealthBased(healthyProviders);
            default:
                return healthyProviders.get(0);
        }
    }

    /**
     * Optimize provider selection for cost
     */
    private ModelProviderType optimizeForCost(ModelProviderType selectedProvider,
            List<ModelProviderType> availableProviders, ActionContext actionContext) {
        // Get cost estimates for all providers
        Map<ModelProviderType, Double> costEstimates = estimateCosts(availableProviders, actionContext);

        // Find the most cost-effective provider within acceptable performance bounds
        ModelProviderType costOptimizedProvider = selectedProvider;
        double minCost = costEstimates.getOrDefault(selectedProvider, Double.MAX_VALUE);

        for (ModelProviderType provider : availableProviders) {
            double cost = costEstimates.getOrDefault(provider, Double.MAX_VALUE);
            if (cost < minCost && isProviderAcceptable(provider, actionContext)) {
                minCost = cost;
                costOptimizedProvider = provider;
            }
        }

        if (!costOptimizedProvider.equals(selectedProvider)) {
            logger.debug("Cost optimization: switched from {} to {} (cost: {} vs {})", selectedProvider,
                    costOptimizedProvider, minCost, costEstimates.get(selectedProvider));
        }

        return costOptimizedProvider;
    }

    /**
     * Execute tool with fallback mechanism
     */
    private CompletableFuture<ActionResult> executeWithFallback(ActionContext actionContext,
            ModelProviderType primaryProvider, List<ModelProviderType> availableProviders, Instant startTime) {

        if (!enableFallback.get()) {
            return executeToolDirectly(actionContext, primaryProvider, startTime);
        }

        return executeToolDirectly(actionContext, primaryProvider, startTime).thenCompose(result -> {
            if (result.isSuccess()) {
                return CompletableFuture.completedFuture(result);
            }

            // Primary provider failed, try fallback providers
            return executeWithFallbackProviders(actionContext, availableProviders, primaryProvider, startTime);
        });
    }

    /**
     * Execute with fallback providers
     */
    private CompletableFuture<ActionResult> executeWithFallbackProviders(ActionContext actionContext,
            List<ModelProviderType> availableProviders, ModelProviderType failedProvider, Instant startTime) {

        fallbackExecutions.incrementAndGet();
        logger.debug("Primary provider {} failed, attempting fallback", failedProvider);

        // Remove failed provider from available list
        List<ModelProviderType> fallbackProviders = availableProviders.stream()
                .filter(provider -> !provider.equals(failedProvider)).toList();

        if (fallbackProviders.isEmpty()) {
            logger.error("No fallback providers available");
            return CompletableFuture.completedFuture(ActionResult.error("No fallback providers available",
                    new ActionError("NO_FALLBACK", "No fallback providers available"),
                    Duration.between(startTime, Instant.now()).toMillis()));
        }

        // Try fallback providers in order
        return tryFallbackProviders(actionContext, fallbackProviders, 0, startTime);
    }

    /**
     * Try fallback providers recursively
     */
    private CompletableFuture<ActionResult> tryFallbackProviders(ActionContext actionContext,
            List<ModelProviderType> fallbackProviders, int attemptIndex, Instant startTime) {

        if (attemptIndex >= fallbackProviders.size() || attemptIndex >= maxFallbackAttempts.get()) {
            logger.error("All fallback providers failed");
            return CompletableFuture.completedFuture(ActionResult.error("All fallback providers failed",
                    new ActionError("ALL_FALLBACKS_FAILED", "All fallback providers failed"),
                    Duration.between(startTime, Instant.now()).toMillis()));
        }

        ModelProviderType fallbackProvider = fallbackProviders.get(attemptIndex);
        logger.debug("Trying fallback provider {} (attempt {})", fallbackProvider, attemptIndex + 1);

        return executeToolDirectly(actionContext, fallbackProvider, startTime).thenCompose(result -> {
            if (result.isSuccess()) {
                return CompletableFuture.completedFuture(result);
            }

            // This fallback provider also failed, try next one
            return tryFallbackProviders(actionContext, fallbackProviders, attemptIndex + 1, startTime);
        });
    }

    /**
     * Execute tool directly on a specific provider
     */
    private CompletableFuture<ActionResult> executeToolDirectly(ActionContext actionContext, ModelProviderType provider,
            Instant startTime) {
        // Update provider load counter
        providerLoadCounters.computeIfAbsent(provider, p -> new AtomicLong(0)).incrementAndGet();

        // Execute the tool (integrate with the actual tool execution system)
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Integrate with actual tool execution system
                ActionResult result = executeToolOnProvider(actionContext, provider);

                // Update metrics
                updateMetrics(provider, actionContext, result, startTime);

                return result;
            } catch (Exception e) {
                failedToolExecutions.incrementAndGet();
                updateFailureMetrics(provider, actionContext, e, startTime);
                throw e;
            }
        });
    }

    /**
     * Execute tool on specific provider (actual integration)
     */
    private ActionResult executeToolOnProvider(ActionContext actionContext, ModelProviderType provider) {
        // Integrate with UnifiedActionExecutionService or similar
        logger.debug("Executing tool on provider: {}", provider);

        Instant startTime = Instant.now();

        try {
            // Get the action name from context
            Map<String, Object> protocolContext = actionContext.getProtocolContext();
            String actionName = (String) protocolContext.get("action");

            if (actionName == null || actionName.isEmpty()) {
                return ActionResult.error(
                        "Tool execution failed", new ActionError("VALIDATION_ERROR",
                                "Action name cannot be null or empty", "VALIDATION_ERROR", null),
                        Duration.between(startTime, Instant.now()).toMillis());
            }

            // Get tool from registry
            if (toolRegistry == null) {
                return ActionResult.error("Tool execution failed",
                        new ActionError("SERVICE_ERROR", "Tool registry not available", "SERVICE_ERROR", null),
                        Duration.between(startTime, Instant.now()).toMillis());
            }

            // Look up the tool by name
            var tool = toolRegistry.getTool(actionName);
            if (tool == null) {
                return ActionResult.error("Tool execution failed",
                        new ActionError("NOT_FOUND", "Tool not found: " + actionName, "NOT_FOUND", null),
                        Duration.between(startTime, Instant.now()).toMillis());
            }

            // Get tool parameters from context
            @SuppressWarnings("unchecked")
            Map<String, Object> parameters = (Map<String, Object>) protocolContext.get("parameters");
            if (parameters == null) {
                parameters = Map.of();
            }

            // Validate tool parameters
            var validationResult = tool.validateParameters(parameters);
            if (!validationResult.isValid()) {
                return ActionResult.error("Tool execution failed", new ActionError("VALIDATION_ERROR",
                        "Tool parameter validation failed: " + validationResult.getMessage(), "VALIDATION_ERROR", null),
                        Duration.between(startTime, Instant.now()).toMillis());
            }

            // Create tool context
            var toolContext = new org.openhab.core.ai.tool.api.ToolContext();
            toolContext.setProperty("provider", provider.name());
            toolContext.setProperty("requestId", actionContext.getCorrelationId());
            toolContext.setProperty("timestamp", startTime.toEpochMilli());

            // Execute the tool
            var toolResult = tool.execute(parameters, toolContext);

            if (toolResult.isSuccess()) {
                return ActionResult.success("Tool executed successfully on " + provider,
                        Duration.between(startTime, Instant.now()).toMillis());
            } else {
                return ActionResult.error("Tool execution failed",
                        new ActionError("EXECUTION_ERROR", toolResult.getError(), "EXECUTION_ERROR", null),
                        Duration.between(startTime, Instant.now()).toMillis());
            }

        } catch (Exception e) {
            logger.error("Error executing tool on provider: {}", provider, e);
            return ActionResult.error(
                    "Tool execution failed", new ActionError("EXECUTION_ERROR",
                            "Tool execution failed: " + e.getMessage(), "EXECUTION_ERROR", e),
                    Duration.between(startTime, Instant.now()).toMillis());
        }
    }

    // Load balancing strategies
    private ModelProviderType selectRoundRobin(List<ModelProviderType> providers) {
        // Simple round-robin selection
        long currentIndex = totalToolExecutions.get() % providers.size();
        return providers.get((int) currentIndex);
    }

    private ModelProviderType selectLeastConnections(List<ModelProviderType> providers) {
        return providers.stream()
                .min((p1, p2) -> Long.compare(providerLoadCounters.getOrDefault(p1, new AtomicLong(0)).get(),
                        providerLoadCounters.getOrDefault(p2, new AtomicLong(0)).get()))
                .orElse(providers.get(0));
    }

    private ModelProviderType selectWeightedResponseTime(List<ModelProviderType> providers) {
        return providers.stream().min((p1, p2) -> Double.compare(getProviderMetrics(p1).getAverageResponseTime(),
                getProviderMetrics(p2).getAverageResponseTime())).orElse(providers.get(0));
    }

    private ModelProviderType selectHealthBased(List<ModelProviderType> providers) {
        return providers.stream().max((p1, p2) -> Double.compare(getProviderMetrics(p1).getHealthScore(),
                getProviderMetrics(p2).getHealthScore())).orElse(providers.get(0));
    }

    // Privacy and security methods
    private boolean isPrivacySensitive(ActionContext actionContext) {
        // Implement privacy sensitivity detection
        // Check for sensitive data in action context
        Map<String, Object> context = actionContext.getProtocolContext();
        String action = (String) context.get("action");

        if (action == null) {
            return false;
        }

        // Define privacy-sensitive actions
        List<String> privacySensitiveActions = List.of("user_data_access", "personal_info", "security_credentials",
                "private_configuration", "sensitive_logs", "user_profile", "authentication", "authorization",
                "password", "token", "api_key", "secret", "private", "confidential", "personal", "email", "phone",
                "address", "ssn", "credit_card", "bank_account");

        // Check if action name contains privacy-sensitive keywords
        String lowerAction = action.toLowerCase();
        boolean containsSensitiveAction = privacySensitiveActions.stream()
                .anyMatch(sensitive -> lowerAction.contains(sensitive.toLowerCase()));

        if (containsSensitiveAction) {
            logger.debug("Privacy-sensitive action detected: {}", action);
            return true;
        }

        // Check parameters for sensitive data
        @SuppressWarnings("unchecked")
        Map<String, Object> parameters = (Map<String, Object>) context.get("parameters");
        if (parameters != null) {
            boolean hasSensitiveParameters = checkParametersForSensitiveData(parameters);
            if (hasSensitiveParameters) {
                logger.debug("Privacy-sensitive parameters detected in action: {}", action);
                return true;
            }
        }

        // Check authentication context for sensitive information
        var authContext = actionContext.getAuthContext();
        if (authContext != null) {
            // Check if this is a high-privilege session
            Set<String> permissions = authContext.getPermissions();
            if (permissions.contains("admin") || permissions.contains("root") || permissions.contains("superuser")) {
                logger.debug("High-privilege session detected for action: {}", action);
                return true;
            }
        }

        return false;
    }

    /**
     * Check parameters for sensitive data patterns
     */
    private boolean checkParametersForSensitiveData(Map<String, Object> parameters) {
        List<String> sensitiveKeys = List.of("password", "token", "secret", "key", "credential", "auth", "private",
                "confidential", "personal", "email", "phone", "address", "ssn", "credit_card", "bank_account",
                "api_key");

        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            String key = entry.getKey().toLowerCase();

            // Check if parameter name contains sensitive keywords
            if (sensitiveKeys.stream().anyMatch(sensitive -> key.contains(sensitive))) {
                return true;
            }

            // Check parameter value for sensitive patterns
            Object value = entry.getValue();
            if (value instanceof String) {
                String strValue = (String) value;
                if (isSensitiveValue(strValue)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Check if a value contains sensitive data patterns
     */
    private boolean isSensitiveValue(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }

        // Email pattern
        if (value.matches(".*@.*\\..*")) {
            return true;
        }

        // Phone number pattern
        if (value.matches(".*\\d{3}[-.]?\\d{3}[-.]?\\d{4}.*")) {
            return true;
        }

        // Credit card pattern (basic)
        if (value.matches(".*\\d{4}[- ]?\\d{4}[- ]?\\d{4}[- ]?\\d{4}.*")) {
            return true;
        }

        // API key pattern (long alphanumeric strings)
        if (value.matches(".*[a-zA-Z0-9]{32,}.*")) {
            return true;
        }

        // SSN pattern
        if (value.matches(".*\\d{3}-\\d{2}-\\d{4}.*")) {
            return true;
        }

        return false;
    }

    private ModelProviderType selectPrivacyProvider(List<ModelProviderType> availableProviders) {
        // Prefer local providers for privacy-sensitive operations
        return availableProviders.stream().filter(provider -> isLocalProvider(provider)).findFirst()
                .orElse(availableProviders.get(0));
    }

    private boolean isLocalProvider(ModelProviderType provider) {
        return provider == ModelProviderType.OLLAMA || provider == ModelProviderType.LOCALAI
                || provider == ModelProviderType.VLLM;
    }

    // Health and performance methods
    private boolean isProviderHealthy(ModelProviderType provider) {
        if (healthMonitor != null) {
            return healthMonitor.isProviderHealthy(provider);
        }
        return true; // Default to healthy if no health monitor
    }

    private boolean isProviderAcceptable(ModelProviderType provider, ActionContext actionContext) {
        ProviderMetrics metrics = getProviderMetrics(provider);
        return metrics.getSuccessRate() > 0.8 && metrics.getAverageResponseTime() < 5000; // 5 seconds
    }

    private Map<ModelProviderType, Double> estimateCosts(List<ModelProviderType> providers,
            ActionContext actionContext) {
        // Implement cost estimation based on provider pricing and action complexity
        Map<ModelProviderType, Double> costEstimates = new ConcurrentHashMap<>();

        for (ModelProviderType provider : providers) {
            double estimatedCost = estimateProviderCost(provider, actionContext);
            costEstimates.put(provider, estimatedCost);
        }

        return costEstimates;
    }

    private double estimateProviderCost(ModelProviderType provider, ActionContext actionContext) {
        // Implement actual cost estimation
        Map<String, Object> context = actionContext.getProtocolContext();
        String action = (String) context.get("action");

        if (action == null) {
            return 0.0;
        }

        // Base cost per provider (in USD)
        Map<ModelProviderType, Double> baseCosts = Map.of(ModelProviderType.OPENAI, 0.002, // $0.002 per request
                ModelProviderType.ANTHROPIC, 0.003, // $0.003 per request
                ModelProviderType.GOOGLE, 0.001, // $0.001 per request
                ModelProviderType.AZURE, 0.002, // $0.002 per request
                ModelProviderType.OLLAMA, 0.0, // Free (local)
                ModelProviderType.LOCALAI, 0.0, // Free (local)
                ModelProviderType.VLLM, 0.0, // Free (local)
                ModelProviderType.LMSTUDIO, 0.0 // Free (local)
        );

        double baseCost = baseCosts.getOrDefault(provider, 0.001);

        // Complexity multiplier based on action type
        double complexityMultiplier = calculateComplexityMultiplier(action);

        // Parameter complexity multiplier
        double parameterMultiplier = calculateParameterComplexity(actionContext);

        // Authentication complexity multiplier
        double authMultiplier = calculateAuthComplexity(actionContext);

        // Calculate total estimated cost
        double estimatedCost = baseCost * complexityMultiplier * parameterMultiplier * authMultiplier;

        logger.debug("Cost estimate for {} on {}: ${} (base: {}, complexity: {}, params: {}, auth: {})", action,
                provider, estimatedCost, baseCost, complexityMultiplier, parameterMultiplier, authMultiplier);

        return estimatedCost;
    }

    /**
     * Calculate complexity multiplier based on action type
     */
    private double calculateComplexityMultiplier(String action) {
        if (action == null) {
            return 1.0;
        }

        String lowerAction = action.toLowerCase();

        // Simple actions
        if (lowerAction.contains("get") || lowerAction.contains("read") || lowerAction.contains("list")) {
            return 1.0;
        }

        // Medium complexity actions
        if (lowerAction.contains("update") || lowerAction.contains("modify") || lowerAction.contains("change")) {
            return 1.5;
        }

        // High complexity actions
        if (lowerAction.contains("create") || lowerAction.contains("generate") || lowerAction.contains("compute")) {
            return 2.0;
        }

        // Very high complexity actions
        if (lowerAction.contains("analyze") || lowerAction.contains("process") || lowerAction.contains("transform")) {
            return 3.0;
        }

        // AI/ML specific actions
        if (lowerAction.contains("predict") || lowerAction.contains("classify") || lowerAction.contains("recommend")) {
            return 4.0;
        }

        return 1.0;
    }

    /**
     * Calculate parameter complexity multiplier
     */
    private double calculateParameterComplexity(ActionContext actionContext) {
        Map<String, Object> context = actionContext.getProtocolContext();
        @SuppressWarnings("unchecked")
        Map<String, Object> parameters = (Map<String, Object>) context.get("parameters");

        if (parameters == null || parameters.isEmpty()) {
            return 1.0;
        }

        int paramCount = parameters.size();

        // More parameters = higher complexity
        if (paramCount <= 3) {
            return 1.0;
        } else if (paramCount <= 5) {
            return 1.2;
        } else if (paramCount <= 10) {
            return 1.5;
        } else {
            return 2.0;
        }
    }

    /**
     * Calculate authentication complexity multiplier
     */
    private double calculateAuthComplexity(ActionContext actionContext) {
        var authContext = actionContext.getAuthContext();

        if (authContext == null) {
            return 1.0;
        }

        Set<String> permissions = authContext.getPermissions();

        // Higher privileges = higher cost (more security overhead)
        if (permissions.contains("admin") || permissions.contains("root")) {
            return 1.5;
        } else if (permissions.contains("user")) {
            return 1.2;
        } else {
            return 1.0;
        }
    }

    // Metrics management
    private void updateMetrics(ModelProviderType provider, ActionContext actionContext, ActionResult result,
            Instant startTime) {
        long executionTime = Duration.between(startTime, Instant.now()).toMillis();

        // Update provider metrics
        ProviderMetrics providerMetrics = getProviderMetrics(provider);
        providerMetrics.recordExecution(result.isSuccess(), executionTime);

        // Update tool metrics
        String actionName = (String) actionContext.getProtocolContext().get("action");
        if (actionName != null) {
            ToolMetrics toolMetrics = getToolMetrics(actionName);
            toolMetrics.recordExecution(result.isSuccess(), executionTime);
        }

        // Update global metrics
        if (result.isSuccess()) {
            successfulToolExecutions.incrementAndGet();
        } else {
            failedToolExecutions.incrementAndGet();
        }
        totalExecutionTime.addAndGet(executionTime);
    }

    private void updateFailureMetrics(ModelProviderType provider, ActionContext actionContext, Exception error,
            Instant startTime) {
        long executionTime = Duration.between(startTime, Instant.now()).toMillis();

        ProviderMetrics providerMetrics = getProviderMetrics(provider);
        providerMetrics.recordExecution(false, executionTime);

        String actionName = (String) actionContext.getProtocolContext().get("action");
        if (actionName != null) {
            ToolMetrics toolMetrics = getToolMetrics(actionName);
            toolMetrics.recordExecution(false, executionTime);
        }
    }

    private ProviderMetrics getProviderMetrics(ModelProviderType provider) {
        ProviderMetrics metrics = providerMetrics.computeIfAbsent(provider, p -> new ProviderMetrics());
        if (metrics == null) {
            metrics = new ProviderMetrics();
            providerMetrics.put(provider, metrics);
        }
        return metrics;
    }

    private ToolMetrics getToolMetrics(String toolName) {
        ToolMetrics metrics = toolMetrics.computeIfAbsent(toolName, t -> new ToolMetrics());
        if (metrics == null) {
            metrics = new ToolMetrics();
            toolMetrics.put(toolName, metrics);
        }
        return metrics;
    }

    // Configuration methods
    public void setEnableFallback(boolean enable) {
        enableFallback.set(enable);
    }

    public void setEnableLoadBalancing(boolean enable) {
        enableLoadBalancing.set(enable);
    }

    public void setEnableCostOptimization(boolean enable) {
        enableCostOptimization.set(enable);
    }

    public void setEnablePrivacyRouting(boolean enable) {
        enablePrivacyRouting.set(enable);
    }

    @Override
    public void setLoadBalancingStrategy(ToolExecutionService.LoadBalancingStrategy strategy) {
        loadBalancingStrategy.set(LoadBalancingStrategy.valueOf(strategy.name()));
    }

    public void setMaxFallbackAttempts(int maxAttempts) {
        maxFallbackAttempts.set(maxAttempts);
    }

    // Metrics retrieval
    @Override
    public ToolExecutionService.HybridServiceMetrics getMetrics() {
        // Convert provider metrics to interface format
        Map<ModelProviderType, ToolExecutionService.ProviderMetrics> interfaceProviderMetrics = new ConcurrentHashMap<>();
        providerMetrics.forEach((provider, metrics) -> {
            ToolExecutionService.ProviderMetrics interfaceMetrics = new ToolExecutionService.ProviderMetrics();
            // Copy the metrics data
            for (int i = 0; i < metrics.getTotalExecutions(); i++) {
                interfaceMetrics.recordExecution(true, 0);
            }
            for (int i = 0; i < metrics.getTotalFailures(); i++) {
                interfaceMetrics.recordExecution(false, 0);
            }
            interfaceProviderMetrics.put(provider, interfaceMetrics);
        });

        // Convert tool metrics to interface format
        Map<String, ToolExecutionService.ToolMetrics> interfaceToolMetrics = new ConcurrentHashMap<>();
        toolMetrics.forEach((tool, metrics) -> {
            ToolExecutionService.ToolMetrics interfaceMetrics = new ToolExecutionService.ToolMetrics();
            // Copy the metrics data
            for (int i = 0; i < metrics.getTotalExecutions(); i++) {
                interfaceMetrics.recordExecution(true, 0);
            }
            interfaceToolMetrics.put(tool, interfaceMetrics);
        });

        return new ToolExecutionService.HybridServiceMetrics(totalToolExecutions.get(), successfulToolExecutions.get(),
                failedToolExecutions.get(), fallbackExecutions.get(), totalExecutionTime.get(), totalCost.get(),
                interfaceProviderMetrics, interfaceToolMetrics);
    }

    public void resetMetrics() {
        totalToolExecutions.set(0);
        successfulToolExecutions.set(0);
        failedToolExecutions.set(0);
        fallbackExecutions.set(0);
        totalExecutionTime.set(0);
        totalCost.set(0);
        providerMetrics.clear();
        toolMetrics.clear();
        providerLoadCounters.clear();
    }

    // Enums
    public enum LoadBalancingStrategy {
        ROUND_ROBIN,
        LEAST_CONNECTIONS,
        WEIGHTED_RESPONSE_TIME,
        HEALTH_BASED
    }
}
