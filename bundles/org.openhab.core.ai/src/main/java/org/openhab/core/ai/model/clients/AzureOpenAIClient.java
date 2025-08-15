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
import org.openhab.core.ai.model.ModelClientInfo;
import org.openhab.core.ai.model.ModelHealthStatus;
import org.openhab.core.ai.model.ModelParameters;
import org.openhab.core.ai.model.ModelRateLimitInfo;
import org.openhab.core.ai.model.ModelResponse;
import org.openhab.core.ai.model.api.ModelClient;
import org.openhab.core.ai.model.api.ModelProviderType;
import org.openhab.core.ai.model.api.ModelStreamHandler;
import org.openhab.core.ai.model.configuration.AzureOpenAIConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.ChatCompletions;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.openai.models.ChatRequestUserMessage;
import com.azure.core.credential.AzureKeyCredential;

/**
 * Azure OpenAI client implementation.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public class AzureOpenAIClient implements ModelClient {

    private final Logger logger = LoggerFactory.getLogger(AzureOpenAIClient.class);
    private final AzureOpenAIConfiguration config;
    private final @Nullable ActionRegistry actionRegistry;
    private final ExecutorService executorService;
    private final ModelClientInfo providerInfo;
    private final OpenAIClient openAIClient;

    // Metrics tracking fields
    private final AtomicLong totalResponseTime = new AtomicLong(0);
    private final AtomicInteger totalRequests = new AtomicInteger(0);
    private final AtomicInteger successfulRequests = new AtomicInteger(0);
    private final AtomicInteger errorCount = new AtomicInteger(0);
    private final AtomicReference<String> lastError = new AtomicReference<>();
    private final AtomicReference<java.time.Instant> lastErrorTime = new AtomicReference<>();
    private final AtomicLong minResponseTime = new AtomicLong(Long.MAX_VALUE);
    private final AtomicLong maxResponseTime = new AtomicLong(0);

    public AzureOpenAIClient(AzureOpenAIConfiguration config, @Nullable ActionRegistry actionRegistry) {
        this.config = config;
        this.actionRegistry = actionRegistry;
        this.executorService = Executors.newCachedThreadPool();

        // Initialize Azure OpenAI client
        this.openAIClient = new OpenAIClientBuilder().endpoint(config.getEndpoint())
                .credential(new AzureKeyCredential(config.getApiKey())).buildClient();

        this.providerInfo = new ModelClientInfo(ModelProviderType.AZURE, config.getModelName(), true, // supportsFunctionCalling
                true, // supportsStreaming
                true, // supportsMultimodal
                config.getMaxTokens(), config.getCostPer1kTokens());
    }

    @Override
    public CompletableFuture<ModelResponse> complete(String prompt, ModelParameters params) {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            try {
                // Build messages
                ChatRequestUserMessage userMessage = new ChatRequestUserMessage(prompt);
                List<ChatRequestUserMessage> messages = List.of(userMessage);

                // Build options
                ChatCompletionsOptions options = new ChatCompletionsOptions(
                        messages.stream().map(msg -> (com.azure.ai.openai.models.ChatRequestMessage) msg)
                                .collect(java.util.stream.Collectors.toList()));
                options.setMaxTokens(params.getMaxTokens());
                options.setTemperature(params.getTemperature());

                logger.debug("Azure OpenAI request: model={}, maxTokens={}, temperature={}", config.getDeploymentName(),
                        params.getMaxTokens(), params.getTemperature());

                // Send request
                ChatCompletions response = openAIClient.getChatCompletions(config.getDeploymentName(), options);

                // Extract response content
                String responseContent = "";
                if (response.getChoices() != null && !response.getChoices().isEmpty()) {
                    var choice = response.getChoices().get(0);
                    if (choice.getMessage() != null) {
                        responseContent = choice.getMessage().getContent();
                    }
                }

                // Track success metrics
                long responseTime = System.currentTimeMillis() - startTime;
                trackMetrics(responseTime, true, null);

                return ModelResponse.builder().content(responseContent).modelName(config.getModelName())
                        .providerType(ModelProviderType.AZURE.name()).build();

            } catch (Exception e) {
                // Track error metrics
                long responseTime = System.currentTimeMillis() - startTime;
                trackMetrics(responseTime, false, e.getMessage() != null ? e.getMessage() : "Unknown error");

                logger.error("Error completing Azure OpenAI request", e);
                throw new RuntimeException("Azure OpenAI completion failed", e);
            }
        }, executorService);
    }

    @Override
    public CompletableFuture<ModelResponse> completeWithStreaming(String prompt, ModelParameters params,
            ModelStreamHandler handler) {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            try {
                // Build messages
                ChatRequestUserMessage userMessage = new ChatRequestUserMessage(prompt);
                List<ChatRequestUserMessage> messages = List.of(userMessage);

                // Build options
                ChatCompletionsOptions options = new ChatCompletionsOptions(
                        messages.stream().map(msg -> (com.azure.ai.openai.models.ChatRequestMessage) msg)
                                .collect(java.util.stream.Collectors.toList()));
                options.setMaxTokens(params.getMaxTokens());
                options.setTemperature(params.getTemperature());
                options.setStream(true);

                logger.debug("Azure OpenAI streaming request: model={}, maxTokens={}, temperature={}",
                        config.getDeploymentName(), params.getMaxTokens(), params.getTemperature());

                // Send streaming request
                var stream = openAIClient.getChatCompletionsStream(config.getDeploymentName(), options);

                StringBuilder responseContent = new StringBuilder();

                // Process streaming response
                for (ChatCompletions response : stream) {
                    if (response.getChoices() != null && !response.getChoices().isEmpty()) {
                        var choice = response.getChoices().get(0);
                        if (choice.getDelta() != null && choice.getDelta().getContent() != null) {
                            String chunk = choice.getDelta().getContent();
                            responseContent.append(chunk);
                            handler.onChunk(chunk);
                        }
                    }
                }

                ModelResponse llmResponse = ModelResponse.builder().content(responseContent.toString())
                        .modelName(config.getModelName()).providerType(ModelProviderType.AZURE.name()).build();

                // Track success metrics
                long responseTime = System.currentTimeMillis() - startTime;
                trackMetrics(responseTime, true, null);

                handler.onComplete(llmResponse);
                return llmResponse;

            } catch (Exception e) {
                // Track error metrics
                long responseTime = System.currentTimeMillis() - startTime;
                trackMetrics(responseTime, false, e.getMessage() != null ? e.getMessage() : "Unknown error");

                logger.error("Error completing Azure OpenAI streaming request", e);
                handler.onError(e);
                throw new RuntimeException("Azure OpenAI streaming completion failed", e);
            }
        }, executorService);
    }

    @Override
    public boolean isAvailable() {
        try {
            // Simple availability check
            return config.isEnabled() && config.getApiKey() != null && !config.getApiKey().isEmpty();
        } catch (Exception e) {
            logger.debug("Azure OpenAI availability check failed", e);
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

            return new ModelHealthStatus(available, java.time.Instant.now(), avgResponseTime, successRate,
                    errorCount.get(), lastError.get(), lastErrorTime.get());
        } catch (Exception e) {
            return new ModelHealthStatus(false, java.time.Instant.now(), -1, 0.0, errorCount.get() + 1,
                    e.getMessage() != null ? e.getMessage() : "Unknown error", java.time.Instant.now());
        }
    }

    @Override
    public ModelProviderType getProviderType() {
        return ModelProviderType.AZURE;
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
                ChatRequestUserMessage userMessage = new ChatRequestUserMessage("Hello");
                List<ChatRequestUserMessage> messages = List.of(userMessage);

                ChatCompletionsOptions options = new ChatCompletionsOptions(
                        messages.stream().map(msg -> (com.azure.ai.openai.models.ChatRequestMessage) msg)
                                .collect(java.util.stream.Collectors.toList()));
                options.setMaxTokens(5);

                ChatCompletions response = openAIClient.getChatCompletions(config.getDeploymentName(), options);
                return response != null;
            } catch (Exception e) {
                logger.debug("Azure OpenAI connection test failed", e);
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
        // For now, return null as the Azure OpenAI SDK doesn't expose rate limit headers directly
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
                lastErrorTime.set(java.time.Instant.now());
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
