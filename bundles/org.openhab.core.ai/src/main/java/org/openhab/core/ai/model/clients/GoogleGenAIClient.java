package org.openhab.core.ai.model.clients;

import java.time.Instant;
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
import org.openhab.core.ai.common.statistics.ModelHealthStatus;
import org.openhab.core.ai.model.ModelClientInfo;
import org.openhab.core.ai.model.ModelParameters;
import org.openhab.core.ai.model.ModelRateLimitInfo;
import org.openhab.core.ai.model.ModelResponse;
import org.openhab.core.ai.model.api.ModelClient;
import org.openhab.core.ai.model.api.ModelProviderType;
import org.openhab.core.ai.model.api.ModelStreamHandler;
import org.openhab.core.ai.model.configuration.GoogleGenAIConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;

/**
 * Google GenAI client implementation.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public class GoogleGenAIClient implements ModelClient {

    private final Logger logger = LoggerFactory.getLogger(GoogleGenAIClient.class);
    private final GoogleGenAIConfiguration config;
    private final @Nullable ActionRegistry actionRegistry;
    private final ExecutorService executorService;
    private final ModelClientInfo providerInfo;
    private final Client genaiClient;

    // Metrics tracking fields
    private final AtomicLong totalResponseTime = new AtomicLong(0);
    private final AtomicInteger totalRequests = new AtomicInteger(0);
    private final AtomicInteger successfulRequests = new AtomicInteger(0);
    private final AtomicInteger errorCount = new AtomicInteger(0);
    private final AtomicReference<String> lastError = new AtomicReference<>();
    private final AtomicReference<Instant> lastErrorTime = new AtomicReference<>();
    private final AtomicLong minResponseTime = new AtomicLong(Long.MAX_VALUE);
    private final AtomicLong maxResponseTime = new AtomicLong(0);

    public GoogleGenAIClient(GoogleGenAIConfiguration config, @Nullable ActionRegistry actionRegistry) {
        this.config = config;
        this.actionRegistry = actionRegistry;
        this.executorService = Executors.newCachedThreadPool();

        // Initialize Google GenAI client
        this.genaiClient = Client.builder().apiKey(config.getApiKey()).build();

        this.providerInfo = new ModelClientInfo(ModelProviderType.GOOGLE, config.getModelName(), true, // supportsFunctionCalling
                true, // supportsStreaming
                true, // supportsMultimodal
                config.getMaxTokens(), config.getCostPer1kTokens());
    }

    @Override
    public CompletableFuture<ModelResponse> complete(String prompt, ModelParameters params) {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            try {
                // Build content
                Content content = buildContent(prompt);

                // Build generation config
                GenerateContentConfig genConfig = GenerateContentConfig.builder()
                        .temperature((float) params.getTemperature()).maxOutputTokens(params.getMaxTokens()).topP(0.8f)
                        .topK(40.0f).build();

                logger.debug("Google GenAI request: model={}, maxTokens={}, temperature={}", this.config.getModelName(),
                        params.getMaxTokens(), params.getTemperature());

                // Send request
                GenerateContentResponse response = genaiClient.models.generateContent(this.config.getModelName(),
                        content, genConfig);

                // Extract response content
                String responseContent = response.text();

                // Track success metrics
                long responseTime = System.currentTimeMillis() - startTime;
                trackMetrics(responseTime, true, null);

                return ModelResponse.builder().withContent(responseContent).withModelName(config.getModelName())
                        .withProviderType(ModelProviderType.GOOGLE.name()).build();

            } catch (Exception e) {
                // Track error metrics
                long responseTime = System.currentTimeMillis() - startTime;
                trackMetrics(responseTime, false, e.getMessage() != null ? e.getMessage() : "Unknown error");

                logger.error("Error completing Google GenAI request", e);
                throw new RuntimeException("Google GenAI completion failed", e);
            }
        }, executorService);
    }

    /**
     * Build content for Google GenAI API
     */
    private Content buildContent(String prompt) {
        Part part = Part.builder().text(prompt).build();
        return Content.builder().parts(part).build();
    }

    @Override
    public CompletableFuture<ModelResponse> completeWithStreaming(String prompt, ModelParameters params,
            ModelStreamHandler handler) {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            try {
                // Build content
                Content content = buildContent(prompt);

                // Build generation config
                GenerateContentConfig genConfig = GenerateContentConfig.builder()
                        .temperature((float) params.getTemperature()).maxOutputTokens(params.getMaxTokens()).topP(0.8f)
                        .topK(40.0f).build();

                logger.debug("Google GenAI streaming request: model={}, maxTokens={}, temperature={}",
                        this.config.getModelName(), params.getMaxTokens(), params.getTemperature());

                // Send streaming request
                var stream = genaiClient.models.generateContentStream(this.config.getModelName(), content, genConfig);

                StringBuilder responseContent = new StringBuilder();

                // Process streaming response
                for (GenerateContentResponse response : stream) {
                    String text = response.text();
                    if (text != null && !text.isEmpty()) {
                        responseContent.append(text);
                        handler.onChunk(text);
                    }
                }

                ModelResponse llmResponse = ModelResponse.builder().withContent(responseContent.toString())
                        .withModelName(this.config.getModelName()).withProviderType(ModelProviderType.GOOGLE.name())
                        .build();

                // Track success metrics
                long responseTime = System.currentTimeMillis() - startTime;
                trackMetrics(responseTime, true, null);

                handler.onComplete(llmResponse);
                return llmResponse;

            } catch (Exception e) {
                // Track error metrics
                long responseTime = System.currentTimeMillis() - startTime;
                trackMetrics(responseTime, false, e.getMessage() != null ? e.getMessage() : "Unknown error");

                logger.error("Error completing Google GenAI streaming request", e);
                handler.onError(e);
                throw new RuntimeException("Google GenAI streaming completion failed", e);
            }
        }, executorService);
    }

    @Override
    public boolean isAvailable() {
        try {
            // Simple availability check
            return config.isEnabled() && config.getApiKey() != null && !config.getApiKey().isEmpty();
        } catch (Exception e) {
            logger.debug("Google GenAI availability check failed", e);
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
            long avgResponseTime = totalRequests.get() > 0 ? totalResponseTime.get() / totalRequests.get() : -1;
            double successRate = totalRequests.get() > 0 ? (double) successfulRequests.get() / totalRequests.get()
                    : 0.0;

            return ModelHealthStatus.builder().withId("google-genai-client")
                    .withOverallHealth(
                            available ? ModelHealthStatus.HealthState.HEALTHY : ModelHealthStatus.HealthState.UNHEALTHY)
                    .withPrimaryModelAvailable(available).withResponseTimeMs(avgResponseTime)
                    .withErrorRate(100.0 - successRate).withTotalRequests(totalRequests.get())
                    .withFailedRequests(errorCount.get()).withLastError(lastError.get())
                    .withLastHealthCheck(Instant.now()).withLastFailedRequest(lastErrorTime.get()).build();
        } catch (Exception e) {
            return ModelHealthStatus.builder().withId("google-genai-client")
                    .withOverallHealth(ModelHealthStatus.HealthState.UNHEALTHY).withPrimaryModelAvailable(false)
                    .withResponseTimeMs(-1).withErrorRate(100.0).withTotalRequests(totalRequests.get())
                    .withFailedRequests(errorCount.get() + 1)
                    .withLastError(e.getMessage() != null ? e.getMessage() : "Unknown error")
                    .withLastHealthCheck(Instant.now()).withLastFailedRequest(Instant.now()).build();
        }
    }

    @Override
    public ModelProviderType getProviderType() {
        return ModelProviderType.GOOGLE;
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
                Content content = Content.builder().parts(Part.builder().text("Hello").build()).build();
                GenerateContentConfig genConfig = GenerateContentConfig.builder().maxOutputTokens(5).build();

                GenerateContentResponse response = genaiClient.models.generateContent(this.config.getModelName(),
                        content, genConfig);
                return response != null;
            } catch (Exception e) {
                logger.debug("Google GenAI connection test failed", e);
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
        // Extract rate limit info from response headers when SDK is available
        // For now, return null as the Google GenAI SDK doesn't expose rate limit headers directly
        // In a real implementation, this would extract from response headers
        return null;
    }

    /**
     * Track metrics for request performance and errors
     */
    private void trackMetrics(long responseTime, boolean success, @Nullable String errorMessage) {
        totalResponseTime.addAndGet(responseTime);
        totalRequests.incrementAndGet();

        if (success) {
            successfulRequests.incrementAndGet();
        } else {
            errorCount.incrementAndGet();
            if (errorMessage != null) {
                lastError.set(errorMessage);
                lastErrorTime.set(Instant.now());
            }
        }

        // Update min/max response times
        minResponseTime.updateAndGet(current -> Math.min(current, responseTime));
        maxResponseTime.updateAndGet(current -> Math.max(current, responseTime));
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
}
