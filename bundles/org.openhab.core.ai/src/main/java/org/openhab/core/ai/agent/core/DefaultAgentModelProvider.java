package org.openhab.core.ai.agent.core;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.action.ActionRegistry;
import org.openhab.core.ai.agent.api.AgentModelIntegrationService;
import org.openhab.core.ai.agent.api.AgentModelProvider;
import org.openhab.core.ai.agent.model.AgentModelConfiguration;
import org.openhab.core.ai.common.context.AgentModelContext;
import org.openhab.core.ai.common.monitoring.api.Health.HealthStatus;
import org.openhab.core.ai.common.monitoring.service.statistics.AgentBehaviorStatistics;
import org.openhab.core.ai.common.response.ModelResponse;
import org.openhab.core.ai.model.ModelParameters;
import org.openhab.core.ai.model.ModelTrackingService;
import org.openhab.core.ai.model.api.ModelClient;
import org.openhab.core.ai.model.api.ModelConfigurationService;
import org.openhab.core.ai.model.api.ModelProviderType;
import org.openhab.core.ai.model.clients.AnthropicClient;
import org.openhab.core.ai.model.clients.GoogleGenAIClient;
import org.openhab.core.ai.model.clients.OpenAIClient;
import org.openhab.core.ai.model.clients.StubModelClient;
import org.openhab.core.ai.model.monitoring.ModelHealthMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of AgentModelProvider providing agent-specific model access
 * 
 * <p>
 * This implementation provides:
 * - Agent-specific model access with shared brain integration
 * - Agent-specific prompt templates and context builders
 * - Agent-specific model selection logic
 * - Agent-specific model parameter optimization
 * - Agent-specific model response processing and validation
 * - Agent-specific model error handling and recovery
 * - Agent-specific model performance monitoring
 * - Agent-specific model security and access controls
 * - LLM client creation and management
 * - Agent-specific tracking and analytics
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public class DefaultAgentModelProvider implements AgentModelProvider {

    private static final Logger logger = LoggerFactory.getLogger(DefaultAgentModelProvider.class);

    private final String agentId;
    private final AgentModelContext context;
    private final AgentModelIntegrationService integrationService;
    private final Map<String, Object> cache = new ConcurrentHashMap<>();
    private final Map<String, String> promptTemplates = new HashMap<>();
    private final Map<String, Object> optimizationSettings = new HashMap<>();
    private final Map<String, Object> securitySettings = new HashMap<>();
    private final Map<String, Object> monitoringSettings = new HashMap<>();

    // LLM Client Management
    private final Map<String, ModelClient> clientCache = new ConcurrentHashMap<>();
    private final Map<ModelProviderType, ModelClient> typedClientCache = new ConcurrentHashMap<>();

    // Dependencies
    private final @Nullable ModelConfigurationService modelConfigurationService;
    private final @Nullable ActionRegistry actionRegistry;
    private final @Nullable ModelTrackingService trackingService;

    // Configuration
    private AgentModelConfiguration configuration;
    private String currentModel;
    private String fallbackModel;
    private boolean fallbackActive = false;

    // Statistics
    private long totalRequests = 0;
    private long successfulRequests = 0;
    private long failedRequests = 0;
    private long cacheHits = 0;
    private long cacheMisses = 0;
    private long totalResponseTimeMs = 0;
    private long minResponseTimeMs = Long.MAX_VALUE;
    private long maxResponseTimeMs = 0;
    private long totalTokensUsed = 0;
    private long totalCost = 0;
    private Instant lastRequestTime = Instant.now();
    private Instant lastSuccessTime = Instant.now();
    private Instant lastFailureTime = Instant.now();
    private String lastError = "";

    public DefaultAgentModelProvider(String agentId, AgentModelContext context,
            AgentModelIntegrationService integrationService,
            @Nullable ModelConfigurationService modelConfigurationService, @Nullable ActionRegistry actionRegistry,
            @Nullable ModelTrackingService trackingService) {
        this.agentId = agentId;
        this.context = context;
        this.integrationService = integrationService;
        this.modelConfigurationService = modelConfigurationService;
        this.actionRegistry = actionRegistry;
        this.trackingService = trackingService;
        this.configuration = createDefaultConfiguration();
        initializeProvider();
    }

    @Override
    public String getAgentId() {
        return agentId;
    }

    @Override
    public CompletableFuture<ModelResponse> reasonAsync(String prompt, Map<String, Object> context,
            @Nullable ModelParameters parameters) {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            totalRequests++;
            lastRequestTime = Instant.now();

            try {
                // Check cache first
                String cacheKey = generateCacheKey(prompt, context);
                if (configuration.isEnableCaching() && cache.containsKey(cacheKey)) {
                    cacheHits++;
                    logger.debug("Cache hit for agent {}: {}", agentId, cacheKey);
                    return (ModelResponse) cache.get(cacheKey);
                }
                cacheMisses++;

                // Build agent-specific context
                Map<String, Object> enhancedContext = buildAgentContext(context);

                // Get optimized parameters
                ModelParameters optimizedParams = getOptimizedParameters(parameters);

                // Execute reasoning through integration service
                ModelResponse response = integrationService
                        .reasonAsync(agentId, prompt, enhancedContext, optimizedParams).get();

                // Process response
                response = processResponse(response);

                // Validate response
                if (!validateResponse(response)) {
                    throw new RuntimeException("Response validation failed for agent: " + agentId);
                }

                // Cache response if enabled
                if (configuration.isEnableCaching()) {
                    cacheResponse(cacheKey, response);
                }

                // Update statistics
                successfulRequests++;
                lastSuccessTime = Instant.now();
                long responseTime = System.currentTimeMillis() - startTime;
                totalResponseTimeMs += responseTime;
                minResponseTimeMs = Math.min(minResponseTimeMs, responseTime);
                maxResponseTimeMs = Math.max(maxResponseTimeMs, responseTime);

                logger.debug("Agent {} reasoning completed successfully in {}ms", agentId, responseTime);
                return response;

            } catch (Exception e) {
                failedRequests++;
                lastFailureTime = Instant.now();
                lastError = e.getMessage();

                logger.error("Agent {} reasoning failed", agentId, e);
                throw new RuntimeException("Reasoning failed for agent " + agentId, e);
            }
        });
    }

    @Override
    public CompletableFuture<ModelResponse> reasonWithOptimizationAsync(String prompt, Map<String, Object> context,
            Map<String, Object> optimizationHints, @Nullable ModelParameters parameters) {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            totalRequests++;
            lastRequestTime = Instant.now();

            try {
                // Apply optimization hints
                Map<String, Object> optimizedContext = applyOptimizationHints(context, optimizationHints);

                // Execute reasoning with optimization
                ModelResponse response = integrationService
                        .reasonWithOptimizationAsync(agentId, prompt, optimizedContext, optimizationHints, parameters)
                        .get();

                // Process response
                response = processResponse(response);

                // Update statistics
                successfulRequests++;
                lastSuccessTime = Instant.now();
                long responseTime = System.currentTimeMillis() - startTime;
                totalResponseTimeMs += responseTime;
                minResponseTimeMs = Math.min(minResponseTimeMs, responseTime);
                maxResponseTimeMs = Math.max(maxResponseTimeMs, responseTime);

                logger.debug("Agent {} optimized reasoning completed successfully in {}ms", agentId, responseTime);
                return response;

            } catch (Exception e) {
                failedRequests++;
                lastFailureTime = Instant.now();
                lastError = e.getMessage();

                logger.error("Agent {} optimized reasoning failed", agentId, e);
                throw new RuntimeException("Optimized reasoning failed for agent " + agentId, e);
            }
        });
    }

    @Override
    public String getPromptTemplate(String templateName, Map<String, Object> variables) {
        String template = promptTemplates.getOrDefault(templateName, "");
        if (template.isEmpty()) {
            logger.warn("Prompt template not found for agent {}: {}", agentId, templateName);
            return "";
        }

        // Simple variable substitution
        String result = template;
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", String.valueOf(entry.getValue()));
        }

        return result;
    }

    @Override
    public Map<String, Object> buildAgentContext(Map<String, Object> baseContext) {
        Map<String, Object> enhancedContext = new HashMap<>(baseContext);

        // Add agent-specific context
        enhancedContext.put("agentId", agentId);
        enhancedContext.put("specialization", context.getSpecialization());
        enhancedContext.put("domain", context.getDomain());
        enhancedContext.put("capabilities", context.getCapabilities());
        enhancedContext.put("constraints", context.getConstraints());
        enhancedContext.put("preferences", context.getPreferences());

        // Add optimization settings
        if (configuration.isEnableOptimization()) {
            enhancedContext.put("optimizationSettings", configuration.getOptimizationSettings());
        }

        // Add security settings
        if (configuration.isEnableSecurity()) {
            enhancedContext.put("securitySettings", configuration.getSecuritySettings());
        }

        return enhancedContext;
    }

    @Override
    public ModelParameters getOptimizedParameters(@Nullable ModelParameters baseParameters) {
        if (baseParameters == null) {
            baseParameters = ModelParameters.builder().build();
        }

        return ModelParameters.builder()
                .withMaxTokens(baseParameters.getMaxTokens() > 0 ? baseParameters.getMaxTokens()
                        : configuration.getMaxTokens())
                .withTemperature(baseParameters.getTemperature() > 0 ? baseParameters.getTemperature()
                        : configuration.getTemperature())
                .withTimeoutMs((int) configuration.getTimeout().toMillis()).build();
    }

    @Override
    public ModelResponse processResponse(ModelResponse response) {
        // Apply agent-specific response processing
        // This could include content filtering, formatting, etc.
        return response;
    }

    @Override
    public boolean validateResponse(ModelResponse response) {
        // Apply agent-specific response validation
        // This could include content safety checks, format validation, etc.
        return response != null && response.getContent() != null;
    }

    @Override
    public AgentBehaviorStatistics getStatistics() {
        // Create a single snapshot with current agent data
        // In a real implementation, you would collect multiple snapshots over time
        // For now, we create a simplified statistics object
        return AgentBehaviorStatistics.fromSnapshots(List.of(), // Empty list for now - would contain AgentTaskSnapshot
                                                                // objects
                Duration.ofDays(1) // Default time range
        );
    }

    @Override
    public ModelHealthMetrics getHealthStatus() {
        double errorRate = totalRequests > 0 ? (double) failedRequests / totalRequests : 0.0;
        double avgResponseTime = calculateAverageResponseTime();

        HealthStatus healthStatus;
        if (errorRate < 0.05 && avgResponseTime < 5000) {
            healthStatus = HealthStatus.HEALTHY;
        } else if (errorRate < 0.15 && avgResponseTime < 10000) {
            healthStatus = HealthStatus.DEGRADED;
        } else {
            healthStatus = HealthStatus.UNHEALTHY;
        }

        return ModelHealthMetrics.builder("agent-" + agentId).withStatus(healthStatus)
                .withStatusMessage("Agent health status")
                .withHealthIndicators(Map.of("errorRate", errorRate, "avgResponseTime", avgResponseTime))
                .withAvailable(!fallbackActive).withAverageResponseTimeMs((long) avgResponseTime)
                .withSuccessRate(1.0 - errorRate).withErrorCount((int) failedRequests).withLastError(lastError)
                .withLastErrorTime(lastFailureTime).build();
    }

    @Override
    public boolean updateConfiguration(AgentModelConfiguration configuration) {
        try {
            this.configuration = configuration;
            logger.debug("Updated configuration for agent: {}", agentId);
            return true;
        } catch (Exception e) {
            logger.error("Failed to update configuration for agent: {}", agentId, e);
            return false;
        }
    }

    @Override
    public AgentModelConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public boolean clearCache() {
        try {
            cache.clear();
            logger.debug("Cleared cache for agent: {}", agentId);
            return true;
        } catch (Exception e) {
            logger.error("Failed to clear cache for agent: {}", agentId, e);
            return false;
        }
    }

    @Override
    public boolean forceFallback(String fallbackModel) {
        try {
            this.fallbackModel = fallbackModel;
            this.fallbackActive = true;
            logger.debug("Forced fallback model {} for agent: {}", fallbackModel, agentId);
            return true;
        } catch (Exception e) {
            logger.error("Failed to force fallback for agent: {}", agentId, e);
            return false;
        }
    }

    @Override
    public boolean resetFallback() {
        try {
            this.fallbackActive = false;
            logger.debug("Reset fallback for agent: {}", agentId);
            return true;
        } catch (Exception e) {
            logger.error("Failed to reset fallback for agent: {}", agentId, e);
            return false;
        }
    }

    // Helper methods
    private void initializeProvider() {
        // Load prompt templates from context
        promptTemplates.putAll(context.getPromptTemplates());

        // Set initial configuration
        currentModel = configuration.getPreferredModel();
        fallbackModel = configuration.getFallbackModel();

        logger.debug("Initialized agent provider for: {}", agentId);
    }

    private AgentModelConfiguration createDefaultConfiguration() {
        return AgentModelConfiguration.builder().withAgentId(agentId).withPreferredModel("gpt-4")
                .withFallbackModel("gpt-3.5-turbo").withTemperature(0.7).withMaxTokens(1000)
                .withTimeout(Duration.ofSeconds(30)).withMaxRetries(3).withRetryDelay(Duration.ofSeconds(5))
                .withEnableCaching(true).withCacheExpiration(Duration.ofMinutes(30)).withMaxCacheSize(1000)
                .withEnableOptimization(true).withEnableSecurity(true).withEnableMonitoring(true).build();
    }

    private String generateCacheKey(String prompt, Map<String, Object> context) {
        // Simple cache key generation
        return agentId + "_" + prompt.hashCode() + "_" + context.hashCode();
    }

    private void cacheResponse(String cacheKey, ModelResponse response) {
        if (cache.size() >= configuration.getMaxCacheSize()) {
            // Simple LRU eviction - remove oldest entry
            String oldestKey = cache.keySet().iterator().next();
            cache.remove(oldestKey);
        }
        cache.put(cacheKey, response);
    }

    private Map<String, Object> applyOptimizationHints(Map<String, Object> context,
            Map<String, Object> optimizationHints) {
        Map<String, Object> optimizedContext = new HashMap<>(context);
        optimizedContext.putAll(optimizationHints);
        return optimizedContext;
    }

    private long calculateAverageResponseTime() {
        return totalRequests > 0 ? totalResponseTimeMs / totalRequests : 0;
    }

    // LLM Client Management Methods

    /**
     * Gets an LLM client for the specified provider type, creating it if necessary.
     * 
     * @param providerType The provider type
     * @return The LLM client
     * @throws IllegalArgumentException if the provider type is not supported
     */
    public ModelClient getOrCreateProvider(ModelProviderType providerType) {
        ModelClient client = typedClientCache.get(providerType);
        if (client == null) {
            client = createProvider(providerType);
            typedClientCache.put(providerType, client);
        }
        return client;
    }

    /**
     * Gets an LLM client for the specified provider type string, creating it if necessary.
     * 
     * @param providerType The provider type string
     * @return The LLM client
     * @throws IllegalArgumentException if the provider type is not supported
     */
    public ModelClient getOrCreateProvider(String providerType) {
        ModelClient client = clientCache.get(providerType.toLowerCase());
        if (client == null) {
            client = createProvider(providerType);
            clientCache.put(providerType.toLowerCase(), client);
        }
        return client;
    }

    /**
     * Gets an LLM client for the specified provider type and records usage for this agent.
     * 
     * @param providerType The provider type
     * @return The LLM client
     * @throws IllegalArgumentException if the provider type is not supported
     */
    public ModelClient getOrCreateProviderForAgent(ModelProviderType providerType) {
        ModelClient client = getOrCreateProvider(providerType);
        recordClientUsage(providerType, client.getModelName(), null);
        return client;
    }

    /**
     * Gets an LLM client for the specified provider type string.
     * 
     * @param providerType The provider type string
     * @return The LLM client
     * @throws IllegalArgumentException if the provider type is not supported
     */
    public ModelClient getOrCreateProviderForAgent(String providerType) {
        return getOrCreateProvider(providerType);
    }

    /**
     * Gets an LLM client for the specified provider type and session.
     * 
     * @param providerType The provider type
     * @param sessionId The session ID for tracking
     * @return The LLM client
     * @throws IllegalArgumentException if the provider type is not supported
     */
    public ModelClient getOrCreateProviderForAgent(ModelProviderType providerType, String sessionId) {
        return getOrCreateProvider(providerType);
    }

    /**
     * Gets an LLM client for the specified provider type string and session.
     * 
     * @param providerType The provider type string
     * @param sessionId The session ID for tracking
     * @return The LLM client
     * @throws IllegalArgumentException if the provider type is not supported
     */
    public ModelClient getOrCreateProviderForAgent(String providerType, String sessionId) {
        return getOrCreateProvider(providerType);
    }

    /**
     * Gets the primary provider for this agent.
     * 
     * @return The primary provider, or null if not configured
     */
    public @Nullable ModelClient getPrimaryProviderForAgent() {
        return getPrimaryProvider();
    }

    /**
     * Gets the fallback provider for this agent.
     * 
     * @return The fallback provider, or null if not configured
     */
    public @Nullable ModelClient getFallbackProviderForAgent() {
        return getFallbackProvider();
    }

    /**
     * Gets the primary provider based on configuration.
     * 
     * @return The primary provider, or null if not configured
     */
    public @Nullable ModelClient getPrimaryProvider() {
        if (modelConfigurationService == null) {
            return null;
        }

        String primaryProvider = modelConfigurationService.getPrimaryProvider();
        if (primaryProvider != null) {
            return getOrCreateProvider(primaryProvider);
        }

        return null;
    }

    /**
     * Gets the fallback provider based on configuration.
     * 
     * @return The fallback provider, or null if not configured
     */
    public @Nullable ModelClient getFallbackProvider() {
        if (modelConfigurationService == null) {
            return null;
        }

        String fallbackProvider = modelConfigurationService.getFallbackProvider();
        if (fallbackProvider != null) {
            return getOrCreateProvider(fallbackProvider);
        }

        return null;
    }

    /**
     * Creates an LLM client for the specified provider type.
     * 
     * @param providerType The provider type
     * @return The created LLM client
     * @throws IllegalArgumentException if the provider type is not supported
     */
    private ModelClient createProvider(ModelProviderType providerType) {
        if (modelConfigurationService == null) {
            throw new IllegalStateException("Configuration service not available");
        }

        switch (providerType) {
            case OPENAI:
                return createOpenAIClient(modelConfigurationService);
            case ANTHROPIC:
                return createAnthropicClient(modelConfigurationService);
            case GOOGLE:
                return createGoogleGenAIClient(modelConfigurationService);
            case AZURE:
                return createAzureOpenAIClient(modelConfigurationService);
            case OLLAMA:
                return createOllamaClient(modelConfigurationService);
            case LOCALAI:
                return createLocalAIClient(modelConfigurationService);
            case VLLM:
                return createVModelClient(modelConfigurationService);
            case LMSTUDIO:
                return createLMStudioClient(modelConfigurationService);
            default:
                throw new IllegalArgumentException("Unsupported LLM provider: " + providerType);
        }
    }

    /**
     * Creates an LLM client for the specified provider type string.
     * 
     * @param providerType The provider type string
     * @return The created LLM client
     * @throws IllegalArgumentException if the provider type is not supported
     */
    private ModelClient createProvider(String providerType) {
        try {
            ModelProviderType type = ModelProviderType.valueOf(providerType.toUpperCase());
            return createProvider(type);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unsupported LLM provider: " + providerType);
        }
    }

    /**
     * Creates an OpenAI client.
     */
    private ModelClient createOpenAIClient(@Nullable ModelConfigurationService config) {
        if (config != null) {
            var openAIConfig = config.getOpenAIConfig();
            if (openAIConfig != null && openAIConfig.isEnabled()) {
                logger.debug("Creating OpenAI client with configuration for agent: {}", agentId);
                return new OpenAIClient(openAIConfig, actionRegistry);
            }
        }
        logger.debug("Creating OpenAI client (stub implementation) for agent: {}", agentId);
        return new StubModelClient(ModelProviderType.OPENAI, "gpt-4o-mini");
    }

    /**
     * Creates an Anthropic client.
     */
    private ModelClient createAnthropicClient(@Nullable ModelConfigurationService config) {
        if (config != null) {
            var anthropicConfig = config.getAnthropicConfig();
            if (anthropicConfig != null && anthropicConfig.isEnabled()) {
                logger.debug("Creating Anthropic client with configuration for agent: {}", agentId);
                return new AnthropicClient(anthropicConfig, actionRegistry);
            }
        }
        logger.debug("Creating Anthropic client (stub implementation) for agent: {}", agentId);
        return new StubModelClient(ModelProviderType.ANTHROPIC, "claude-3-5-sonnet");
    }

    /**
     * Creates a Google GenAI client.
     */
    private ModelClient createGoogleGenAIClient(@Nullable ModelConfigurationService config) {
        if (config != null) {
            var googleConfig = config.getGoogleConfig();
            if (googleConfig != null && googleConfig.isEnabled()) {
                logger.debug("Creating Google GenAI client with configuration for agent: {}", agentId);
                return new GoogleGenAIClient(googleConfig, actionRegistry);
            }
        }
        logger.debug("Creating Google GenAI client (stub implementation) for agent: {}", agentId);
        return new StubModelClient(ModelProviderType.GOOGLE, "gemini-1.5-pro");
    }

    /**
     * Creates an Azure OpenAI client.
     */
    private ModelClient createAzureOpenAIClient(@Nullable ModelConfigurationService config) {
        if (config != null) {
            var azureConfig = config.getAzureConfig();
            if (azureConfig != null && azureConfig.isEnabled()) {
                logger.debug("Creating Azure OpenAI client with configuration for agent: {}", agentId);
                return new StubModelClient(ModelProviderType.AZURE, "gpt-4o-mini");
            }
        }
        logger.debug("Creating Azure OpenAI client (stub implementation) for agent: {}", agentId);
        return new StubModelClient(ModelProviderType.AZURE, "gpt-4o-mini");
    }

    /**
     * Creates an Ollama client.
     */
    private ModelClient createOllamaClient(@Nullable ModelConfigurationService config) {
        if (config != null) {
            var ollamaConfig = config.getOllamaConfig();
            if (ollamaConfig != null && ollamaConfig.isEnabled()) {
                logger.debug("Creating Ollama client with configuration for agent: {}", agentId);
                return new StubModelClient(ModelProviderType.OLLAMA, "llama3.1:8b");
            }
        }
        logger.debug("Creating Ollama client (stub implementation) for agent: {}", agentId);
        return new StubModelClient(ModelProviderType.OLLAMA, "llama3.1:8b");
    }

    /**
     * Creates a LocalAI client.
     */
    private ModelClient createLocalAIClient(@Nullable ModelConfigurationService config) {
        if (config != null) {
            var localAIConfig = config.getLocalAIConfig();
            if (localAIConfig != null && localAIConfig.isEnabled()) {
                logger.debug("Creating LocalAI client with configuration for agent: {}", agentId);
                return new StubModelClient(ModelProviderType.LOCALAI, "llama3.1:8b");
            }
        }
        logger.debug("Creating LocalAI client (stub implementation) for agent: {}", agentId);
        return new StubModelClient(ModelProviderType.LOCALAI, "llama3.1:8b");
    }

    /**
     * Creates a vLLM client.
     */
    private ModelClient createVModelClient(@Nullable ModelConfigurationService config) {
        if (config != null) {
            var vllmConfig = config.getVLLMConfig();
            if (vllmConfig != null && vllmConfig.isEnabled()) {
                logger.debug("Creating vLLM client with configuration for agent: {}", agentId);
                return new StubModelClient(ModelProviderType.VLLM, "llama3.1:8b");
            }
        }
        logger.debug("Creating vLLM client (stub implementation) for agent: {}", agentId);
        return new StubModelClient(ModelProviderType.VLLM, "llama3.1:8b");
    }

    /**
     * Creates an LM Studio client.
     */
    private ModelClient createLMStudioClient(@Nullable ModelConfigurationService config) {
        if (config != null) {
            var lmStudioConfig = config.getLMStudioConfig();
            if (lmStudioConfig != null && lmStudioConfig.isEnabled()) {
                logger.debug("Creating LM Studio client with configuration for agent: {}", agentId);
                return new StubModelClient(ModelProviderType.LMSTUDIO, "llama3.1:8b");
            }
        }
        logger.debug("Creating LM Studio client (stub implementation) for agent: {}", agentId);
        return new StubModelClient(ModelProviderType.LMSTUDIO, "llama3.1:8b");
    }

    /**
     * Records client usage for tracking purposes.
     * 
     * @param providerType The provider type
     * @param modelName The model name
     * @param sessionId The session ID (optional)
     */
    private void recordClientUsage(ModelProviderType providerType, String modelName, @Nullable String sessionId) {
        ModelTrackingService service = trackingService;
        if (service != null) {
            try {
                service.recordClientUsage(agentId, providerType, modelName, sessionId);
                logger.debug("Recorded client usage: agent={}, provider={}, model={}", agentId, providerType,
                        modelName);
            } catch (Exception e) {
                logger.warn("Failed to record client usage for agent: {}", agentId, e);
            }
        }
    }
}
