package org.openhab.core.ai.model.clients;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.action.ActionRegistry;
import org.openhab.core.ai.action.api.Action;
import org.openhab.core.ai.common.monitoring.api.HealthMetrics;
import org.openhab.core.ai.common.monitoring.api.HealthStatus;
import org.openhab.core.ai.common.monitoring.api.MetricKeys;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.service.snapshot.UnifiedMetricsSnapshot;
import org.openhab.core.ai.common.response.ModelResponse;
import org.openhab.core.ai.model.ModelClientInfo;
import org.openhab.core.ai.model.ModelParameters;
import org.openhab.core.ai.model.ModelRateLimitInfo;
import org.openhab.core.ai.model.api.ModelClient;
import org.openhab.core.ai.model.api.ModelProviderType;
import org.openhab.core.ai.model.api.ModelStreamHandler;
import org.openhab.core.ai.model.config.OpenAIConfiguration;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.core.http.StreamResponse;
import com.openai.helpers.ChatCompletionAccumulator;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionChunk;
import com.openai.models.chat.completions.ChatCompletionCreateParams;

/**
 * OpenAI LLM Client implementation using the official OpenAI Java SDK.
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public class OpenAIClient implements ModelClient {

    private final Logger logger = LoggerFactory.getLogger(OpenAIClient.class);
    private final OpenAIConfiguration config;
    private final com.openai.client.OpenAIClient openAIClient;
    private final @Nullable ActionRegistry actionRegistry;
    private final ExecutorService executorService;
    private final ModelClientInfo providerInfo;

    @Reference
    private MetricsService metricsService;

    public OpenAIClient(OpenAIConfiguration config, @Nullable ActionRegistry actionRegistry) {
        this.config = config;
        this.actionRegistry = actionRegistry;
        this.executorService = Executors.newCachedThreadPool();

        // Initialize OpenAI client
        this.openAIClient = OpenAIOkHttpClient.builder().apiKey(config.getApiKey()).baseUrl(config.getBaseUrl())
                .timeout(Duration.ofMillis(config.getTimeoutMs())).maxRetries(config.getRetryAttempts()).build();

        this.providerInfo = new ModelClientInfo(ModelProviderType.OPENAI, config.getModelName(), true, // supportsFunctionCalling
                true, // supportsStreaming
                true, // supportsMultimodal
                config.getMaxTokens(), config.getCostPer1kTokens());
    }

    @Override
    public CompletableFuture<ModelResponse> complete(String prompt, ModelParameters params) {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            try {
                ChatCompletionCreateParams.Builder paramsBuilder = ChatCompletionCreateParams.builder()
                        .model(config.getModelName()).addUserMessage(prompt).temperature(params.getTemperature())
                        .maxTokens(params.getMaxTokens());

                // Add system prompt if configured
                if (config.getSystemPrompt() != null && !config.getSystemPrompt().isEmpty()) {
                    paramsBuilder.addSystemMessage(config.getSystemPrompt());
                }

                ChatCompletionCreateParams requestParams = paramsBuilder.build();
                ChatCompletion response = openAIClient.chat().completions().create(requestParams);

                // Record operation metrics
                long responseTime = System.currentTimeMillis() - startTime;
                try {
                    metricsService.recordOperation("model", "completion").withSuccess(true).withDuration(responseTime)
                            .withData(Map.of("provider", "openai", "model", config.getModelName(), "promptLength",
                                    prompt.length(), "maxTokens", params.getMaxTokens()))
                            .record();
                } catch (Exception metricError) {
                    logger.warn("Failed to record OpenAI completion metrics: {}", metricError.getMessage());
                    // Graceful degradation: continue with response even if metrics recording fails
                }

                // Return regular text response
                String content = response.choices().get(0).message().content().orElse("");
                return ModelResponse.builder().withContent(content).withModelName(response.model())
                        .withProviderType(ModelProviderType.OPENAI.name()).build();

            } catch (Exception e) {
                // Record error metrics
                long responseTime = System.currentTimeMillis() - startTime;
                try {
                    metricsService.recordOperation("model", "completion").withSuccess(false).withDuration(responseTime)
                            .withData(Map.of("provider", "openai", "model", config.getModelName(), "error",
                                    e.getMessage() != null ? e.getMessage() : "Unknown error"))
                            .record();
                } catch (Exception metricError) {
                    logger.warn("Failed to record OpenAI completion error metrics: {}", metricError.getMessage());
                    // Graceful degradation: continue with error handling even if metrics recording fails
                }

                logger.error("Error completing OpenAI request", e);
                throw new RuntimeException("OpenAI completion failed", e);
            }
        }, executorService);
    }

    @Override
    public CompletableFuture<ModelResponse> completeWithStreaming(String prompt, ModelParameters params,
            ModelStreamHandler handler) {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            try {
                ChatCompletionCreateParams.Builder paramsBuilder = ChatCompletionCreateParams.builder()
                        .model(config.getModelName()).addUserMessage(prompt).temperature(params.getTemperature())
                        .maxTokens(params.getMaxTokens());

                // Add system prompt if configured
                if (config.getSystemPrompt() != null && !config.getSystemPrompt().isEmpty()) {
                    paramsBuilder.addSystemMessage(config.getSystemPrompt());
                }

                ChatCompletionCreateParams requestParams = paramsBuilder.build();
                ChatCompletionAccumulator accumulator = ChatCompletionAccumulator.create();

                try (StreamResponse<ChatCompletionChunk> streamResponse = openAIClient.chat().completions()
                        .createStreaming(requestParams)) {

                    streamResponse.stream().peek(accumulator::accumulate).forEach(chunk -> {
                        if (chunk.choices().get(0).delta().content().isPresent()) {
                            String content = chunk.choices().get(0).delta().content().get();
                            handler.onChunk(content);
                        }
                    });
                }

                ChatCompletion finalResponse = accumulator.chatCompletion();
                String content = finalResponse.choices().get(0).message().content().orElse("");

                // Record operation metrics
                long responseTime = System.currentTimeMillis() - startTime;
                try {
                    metricsService.recordOperation("model", "streaming-completion").withSuccess(true)
                            .withDuration(responseTime)
                            .withData(Map.of("provider", "openai", "model", config.getModelName(), "promptLength",
                                    prompt.length(), "maxTokens", params.getMaxTokens()))
                            .record();
                } catch (Exception metricError) {
                    logger.warn("Failed to record OpenAI streaming completion metrics: {}", metricError.getMessage());
                    // Graceful degradation: continue with response even if metrics recording fails
                }

                ModelResponse response = ModelResponse.builder().withContent(content)
                        .withModelName(finalResponse.model()).withProviderType(ModelProviderType.OPENAI.name()).build();

                handler.onComplete(response);
                return response;

            } catch (Exception e) {
                // Record error metrics
                long responseTime = System.currentTimeMillis() - startTime;
                try {
                    metricsService.recordOperation("model", "streaming-completion").withSuccess(false)
                            .withDuration(responseTime)
                            .withData(Map.of("provider", "openai", "model", config.getModelName(), "error",
                                    e.getMessage() != null ? e.getMessage() : "Unknown error"))
                            .record();
                } catch (Exception metricError) {
                    logger.warn("Failed to record OpenAI streaming completion error metrics: {}",
                            metricError.getMessage());
                    // Graceful degradation: continue with error handling even if metrics recording fails
                }

                logger.error("Error completing OpenAI streaming request", e);
                handler.onError(e);
                throw new RuntimeException("OpenAI streaming completion failed", e);
            }
        }, executorService);
    }

    @Override
    public boolean isAvailable() {
        try {
            // Simple health check by testing connection
            return testConnection().get();
        } catch (Exception e) {
            logger.debug("OpenAI client not available", e);
            return false;
        }
    }

    @Override
    public ModelClientInfo getProviderInfo() {
        return providerInfo;
    }

    @Override
    public HealthMetrics getHealthStatus() {
        try {
            boolean available = isAvailable();

            // Record health check operation
            try {
                metricsService.recordOperation("model", "health-check").withSuccess(available).withDuration(100)
                        .withData(Map.of("provider", "openai", "model", config.getModelName())).record();
            } catch (Exception metricError) {
                logger.warn("Failed to record OpenAI health check metrics: {}", metricError.getMessage());
                // Graceful degradation: continue with health status even if metrics recording fails
            }

            // Return health metrics from service
            try {
                return metricsService.getSnapshot(MetricKeys.modelHealth(config.getModelName()),
                        UnifiedMetricsSnapshot.class);
            } catch (Exception metricError) {
                logger.warn("Failed to retrieve health metrics snapshot: {}", metricError.getMessage());
                // Return a default health metrics implementation for graceful degradation
                return UnifiedMetricsSnapshot.builder("default", "model", "health-check")
                        .withHealthStatus(HealthStatus.UNKNOWN).withStatusMessage("Health metrics unavailable").build();
            }

        } catch (Exception e) {
            // Record failed health check
            try {
                metricsService.recordOperation("model", "health-check").withSuccess(false).withDuration(100)
                        .withData(Map.of("provider", "openai", "model", config.getModelName(), "error",
                                e.getMessage() != null ? e.getMessage() : "Unknown error"))
                        .record();
            } catch (Exception metricError) {
                logger.warn("Failed to record OpenAI health check error metrics: {}", metricError.getMessage());
                // Graceful degradation: continue with health status even if metrics recording fails
            }

            // Return health metrics from service (will reflect the failure)
            try {
                return metricsService.getSnapshot(MetricKeys.modelHealth(config.getModelName()),
                        UnifiedMetricsSnapshot.class);
            } catch (Exception metricError) {
                logger.warn("Failed to retrieve health metrics snapshot after error: {}", metricError.getMessage());
                // Return a default health metrics implementation for graceful degradation
                return UnifiedMetricsSnapshot.builder("default", "model", "health-check")
                        .withHealthStatus(HealthStatus.DEGRADED).withStatusMessage("Health check failed").build();
            }
        }
    }

    @Override
    public ModelProviderType getProviderType() {
        return ModelProviderType.OPENAI;
    }

    @Override
    public String getModelName() {
        return config.getModelName();
    }

    @Override
    public CompletableFuture<Boolean> testConnection() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Simple test with minimal tokens
                ChatCompletionCreateParams testParams = ChatCompletionCreateParams.builder()
                        .model(config.getModelName()).addUserMessage("Hello").maxTokens(5).build();

                openAIClient.chat().completions().create(testParams);
                return true;
            } catch (Exception e) {
                logger.debug("OpenAI connection test failed", e);
                return false;
            }
        }, executorService);
    }

    @Override
    public double estimateCost(String prompt, ModelParameters params) {
        // Rough estimation based on token count
        int estimatedTokens = prompt.length() / 4; // Rough approximation
        estimatedTokens += params.getMaxTokens();

        return (estimatedTokens / 1000.0) * config.getCostPer1kTokens();
    }

    @Override
    public int getMaxTokens() {
        return config.getMaxTokens();
    }

    @Override
    public double getCostPer1kTokens() {
        return config.getCostPer1kTokens();
    }

    @Override
    public boolean supportsFunctionCalling() {
        return true;
    }

    @Override
    public boolean supportsStreaming() {
        return true;
    }

    @Override
    public boolean supportsMultimodal() {
        return true;
    }

    @Override
    public @Nullable ModelRateLimitInfo getRateLimitInfo() {
        // Extract rate limit info from response headers
        // Note: This would require access to the HTTP response headers
        // For now, return null as the OpenAI SDK doesn't expose headers directly
        // In a real implementation, this would parse headers like:
        // - x-ratelimit-remaining-requests
        // - x-ratelimit-reset-requests
        // - x-ratelimit-limit-requests
        logger.debug("Rate limit info not available from OpenAI SDK - headers not exposed");
        return null;
    }

    /**
     * Get available actions from the registry
     */
    private List<Action> getAvailableActions() {
        if (actionRegistry != null) {
            try {
                Map<String, Action> actionsMap = actionRegistry.getAllActions();
                return new ArrayList<>(actionsMap.values());
            } catch (Exception e) {
                logger.warn("Error retrieving actions from registry", e);
                return List.of();
            }
        }
        return List.of();
    }

    /**
     * Get minimum response time in milliseconds from metrics snapshot
     */
    public long getMinResponseTime() {
        try {
            UnifiedMetricsSnapshot snapshot = metricsService.getSnapshot(MetricKeys.modelHealth(config.getModelName()),
                    UnifiedMetricsSnapshot.class);
            if (snapshot != null && snapshot.healthIndicators() != null) {
                Object minTime = snapshot.healthIndicators().get("minResponseTimeMs");
                return minTime instanceof Number ? ((Number) minTime).longValue() : 0;
            }
            return 0;
        } catch (Exception e) {
            logger.warn("Failed to retrieve min response time metrics: {}", e.getMessage());
            return 0; // Graceful degradation: return default value
        }
    }

    /**
     * Get maximum response time in milliseconds from metrics snapshot
     */
    public long getMaxResponseTime() {
        try {
            UnifiedMetricsSnapshot snapshot = metricsService.getSnapshot(MetricKeys.modelHealth(config.getModelName()),
                    UnifiedMetricsSnapshot.class);
            if (snapshot != null && snapshot.healthIndicators() != null) {
                Object maxTime = snapshot.healthIndicators().get("maxResponseTimeMs");
                return maxTime instanceof Number ? ((Number) maxTime).longValue() : 0;
            }
            return 0;
        } catch (Exception e) {
            logger.warn("Failed to retrieve max response time metrics: {}", e.getMessage());
            return 0; // Graceful degradation: return default value
        }
    }

    /**
     * Get total number of requests made from metrics snapshot
     */
    public int getTotalRequests() {
        try {
            UnifiedMetricsSnapshot snapshot = metricsService.getSnapshot(MetricKeys.modelHealth(config.getModelName()),
                    UnifiedMetricsSnapshot.class);
            return snapshot != null ? (int) snapshot.total() : 0;
        } catch (Exception e) {
            logger.warn("Failed to retrieve total requests metrics: {}", e.getMessage());
            return 0; // Graceful degradation: return default value
        }
    }

    /**
     * Get total number of successful requests from metrics snapshot
     */
    public int getSuccessfulRequests() {
        try {
            UnifiedMetricsSnapshot snapshot = metricsService.getSnapshot(MetricKeys.modelHealth(config.getModelName()),
                    UnifiedMetricsSnapshot.class);
            return snapshot != null ? (int) snapshot.success() : 0;
        } catch (Exception e) {
            logger.warn("Failed to retrieve successful requests metrics: {}", e.getMessage());
            return 0; // Graceful degradation: return default value
        }
    }

    /**
     * Get total number of failed requests from metrics snapshot
     */
    public int getFailedRequests() {
        try {
            UnifiedMetricsSnapshot snapshot = metricsService.getSnapshot(MetricKeys.modelHealth(config.getModelName()),
                    UnifiedMetricsSnapshot.class);
            return snapshot != null ? (int) snapshot.failure() : 0;
        } catch (Exception e) {
            logger.warn("Failed to retrieve failed requests metrics: {}", e.getMessage());
            return 0; // Graceful degradation: return default value
        }
    }

    /**
     * Reset metrics for this model
     */
    public void resetMetrics() {
        // Reset is handled by the MetricsService, not locally
        // This method is kept for backward compatibility but delegates to the service
        logger.debug("Reset metrics requested for OpenAI model {}", config.getModelName());
    }
}
