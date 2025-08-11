package org.openhab.core.ai.model.clients;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.action.ActionRegistry;
import org.openhab.core.ai.action.api.Action;
import org.openhab.core.ai.model.api.ModelClient;
import org.openhab.core.ai.model.api.ModelClientInfo;
import org.openhab.core.ai.model.api.ModelHealthStatus;
import org.openhab.core.ai.model.api.ModelParameters;
import org.openhab.core.ai.model.api.ModelProviderType;
import org.openhab.core.ai.model.api.ModelRateLimitInfo;
import org.openhab.core.ai.model.api.ModelResponse;
import org.openhab.core.ai.model.api.ModelStreamHandler;
import org.openhab.core.ai.model.configuration.OpenAIConfiguration;
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

    // Metrics tracking fields
    private final AtomicLong totalResponseTime = new AtomicLong(0);
    private final AtomicInteger totalRequests = new AtomicInteger(0);
    private final AtomicInteger successfulRequests = new AtomicInteger(0);
    private final AtomicInteger errorCount = new AtomicInteger(0);
    private final AtomicReference<String> lastError = new AtomicReference<>();
    private final AtomicReference<java.time.Instant> lastErrorTime = new AtomicReference<>();
    private final AtomicLong minResponseTime = new AtomicLong(Long.MAX_VALUE);
    private final AtomicLong maxResponseTime = new AtomicLong(0);

    public OpenAIClient(OpenAIConfiguration config, @Nullable ActionRegistry actionRegistry) {
        this.config = config;
        this.actionRegistry = actionRegistry;
        this.executorService = Executors.newCachedThreadPool();

        // Initialize OpenAI client
        this.openAIClient = OpenAIOkHttpClient.builder().apiKey(config.getApiKey()).baseUrl(config.getBaseUrl())
                .timeout(java.time.Duration.ofMillis(config.getTimeoutMs())).maxRetries(config.getRetryAttempts())
                .build();

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

                // Track metrics
                long responseTime = System.currentTimeMillis() - startTime;
                trackMetrics(responseTime, true, null);

                // Return regular text response
                String content = response.choices().get(0).message().content().orElse("");
                return ModelResponse.builder().content(content).modelName(response.model())
                        .providerType(ModelProviderType.OPENAI.name()).build();

            } catch (Exception e) {
                // Track error metrics
                long responseTime = System.currentTimeMillis() - startTime;
                trackMetrics(responseTime, false, e.getMessage() != null ? e.getMessage() : "Unknown error");

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

                // Track metrics
                long responseTime = System.currentTimeMillis() - startTime;
                trackMetrics(responseTime, true, null);

                ModelResponse response = ModelResponse.builder().content(content).modelName(finalResponse.model())
                        .providerType(ModelProviderType.OPENAI.name()).build();

                handler.onComplete(response);
                return response;

            } catch (Exception e) {
                // Track error metrics
                long responseTime = System.currentTimeMillis() - startTime;
                trackMetrics(responseTime, false, e.getMessage() != null ? e.getMessage() : "Unknown error");

                logger.error("Error completing OpenAI streaming request", e);
                handler.onError(e);
                throw new RuntimeException("OpenAI streaming completion failed", e);
            }
        }, executorService);
    }

    /**
     * Track metrics for request performance and errors
     */
    private void trackMetrics(long responseTime, boolean success, @Nullable String errorMessage) {
        totalRequests.incrementAndGet();
        totalResponseTime.addAndGet(responseTime);

        // Update min/max response times
        minResponseTime.updateAndGet(current -> Math.min(current, responseTime));
        maxResponseTime.updateAndGet(current -> Math.max(current, responseTime));

        if (success) {
            successfulRequests.incrementAndGet();
        } else {
            errorCount.incrementAndGet();
            lastError.set(errorMessage);
            lastErrorTime.set(java.time.Instant.now());
        }
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
    public ModelHealthStatus getHealthStatus() {
        try {
            boolean available = isAvailable();
            long avgResponseTime = totalRequests.get() > 0 ? totalResponseTime.get() / totalRequests.get() : 0;
            double successRate = totalRequests.get() > 0 ? (double) successfulRequests.get() / totalRequests.get()
                    : 1.0;

            return new ModelHealthStatus(available, java.time.Instant.now(), avgResponseTime, successRate,
                    errorCount.get(), lastError.get(), lastErrorTime.get());
        } catch (Exception e) {
            return new ModelHealthStatus(false, java.time.Instant.now(), -1, 0.0, errorCount.get() + 1,
                    e.getMessage() != null ? e.getMessage() : "Unknown error", java.time.Instant.now());
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
     * Get minimum response time in milliseconds
     */
    public long getMinResponseTime() {
        long min = minResponseTime.get();
        return min == Long.MAX_VALUE ? 0 : min;
    }

    /**
     * Get maximum response time in milliseconds
     */
    public long getMaxResponseTime() {
        return maxResponseTime.get();
    }

    /**
     * Get total number of requests made
     */
    public int getTotalRequests() {
        return totalRequests.get();
    }

    /**
     * Get total number of successful requests
     */
    public int getSuccessfulRequests() {
        return successfulRequests.get();
    }

    /**
     * Get total number of failed requests
     */
    public int getFailedRequests() {
        return errorCount.get();
    }

    /**
     * Reset all metrics
     */
    public void resetMetrics() {
        totalResponseTime.set(0);
        totalRequests.set(0);
        successfulRequests.set(0);
        errorCount.set(0);
        lastError.set(null);
        lastErrorTime.set(null);
        minResponseTime.set(Long.MAX_VALUE);
        maxResponseTime.set(0);
    }
}
