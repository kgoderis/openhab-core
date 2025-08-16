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
import org.openhab.core.ai.model.ModelClientInfo;
import org.openhab.core.ai.model.ModelHealthStatus;
import org.openhab.core.ai.model.ModelParameters;
import org.openhab.core.ai.model.ModelRateLimitInfo;
import org.openhab.core.ai.model.ModelResponse;
import org.openhab.core.ai.model.api.ModelClient;
import org.openhab.core.ai.model.api.ModelProviderType;
import org.openhab.core.ai.model.api.ModelStreamHandler;
import org.openhab.core.ai.model.configuration.AnthropicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.MessageParam;

/**
 * Anthropic Claude client implementation.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public class AnthropicClient implements ModelClient {

    private final Logger logger = LoggerFactory.getLogger(AnthropicClient.class);
    private final AnthropicConfiguration config;
    private final @Nullable ActionRegistry actionRegistry;
    private final ExecutorService executorService;
    private final ModelClientInfo providerInfo;
    private final com.anthropic.client.AnthropicClient anthropicClient;

    // Metrics tracking fields
    private final AtomicLong totalResponseTime = new AtomicLong(0);
    private final AtomicInteger totalRequests = new AtomicInteger(0);
    private final AtomicInteger successfulRequests = new AtomicInteger(0);
    private final AtomicInteger errorCount = new AtomicInteger(0);
    private final AtomicReference<String> lastError = new AtomicReference<>();
    private final AtomicReference<Instant> lastErrorTime = new AtomicReference<>();
    private final AtomicLong minResponseTime = new AtomicLong(Long.MAX_VALUE);
    private final AtomicLong maxResponseTime = new AtomicLong(0);

    public AnthropicClient(AnthropicConfiguration config, @Nullable ActionRegistry actionRegistry) {
        this.config = config;
        this.actionRegistry = actionRegistry;
        this.executorService = Executors.newCachedThreadPool();

        // Initialize Anthropic client with API key
        this.anthropicClient = new AnthropicOkHttpClient.Builder().apiKey(config.getApiKey()).build();

        this.providerInfo = new ModelClientInfo(ModelProviderType.ANTHROPIC, config.getModelName(), true, // supportsFunctionCalling
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
                List<MessageParam> messages = buildMessages(prompt);

                // Build request
                MessageCreateParams request = MessageCreateParams.builder().model(config.getModelName())
                        .maxTokens(params.getMaxTokens()).temperature(params.getTemperature()).messages(messages)
                        .build();

                logger.debug("Anthropic request: model={}, maxTokens={}, temperature={}", config.getModelName(),
                        params.getMaxTokens(), params.getTemperature());

                // Send request
                Message response = anthropicClient.messages().create(request);

                // Extract response content
                String responseContent = "";
                if (response.content() != null && !response.content().isEmpty()) {
                    // Get the first content block and extract text
                    var firstBlock = response.content().get(0);
                    if (firstBlock.isText()) {
                        responseContent = firstBlock.asText().text();
                    }
                }

                // Track success metrics
                long responseTime = System.currentTimeMillis() - startTime;
                trackMetrics(responseTime, true, null);

                return ModelResponse.builder().content(responseContent).modelName(config.getModelName())
                        .providerType(ModelProviderType.ANTHROPIC.name()).build();

            } catch (Exception e) {
                // Track error metrics
                long responseTime = System.currentTimeMillis() - startTime;
                trackMetrics(responseTime, false, e.getMessage() != null ? e.getMessage() : "Unknown error");

                logger.error("Error completing Anthropic request", e);
                throw new RuntimeException("Anthropic completion failed", e);
            }
        }, executorService);
    }

    private List<MessageParam> buildMessages(String prompt) {
        List<MessageParam> messages = new ArrayList<>();

        // Add system message if configured
        if (config.getSystemPrompt() != null && !config.getSystemPrompt().isEmpty()) {
            messages.add(MessageParam.builder().role(MessageParam.Role.USER)
                    .content("System: " + config.getSystemPrompt()).build());
        }

        // Add user message
        messages.add(MessageParam.builder().role(MessageParam.Role.USER).content(prompt).build());

        return messages;
    }

    @Override
    public CompletableFuture<ModelResponse> completeWithStreaming(String prompt, ModelParameters params,
            ModelStreamHandler handler) {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            try {
                // Build messages
                List<MessageParam> messages = buildMessages(prompt);

                // Create request
                MessageCreateParams request = MessageCreateParams.builder().model(config.getModelName())
                        .maxTokens(params.getMaxTokens()).temperature(params.getTemperature()).messages(messages)
                        .build();

                logger.debug("Anthropic streaming request: model={}, maxTokens={}, temperature={}",
                        config.getModelName(), params.getMaxTokens(), params.getTemperature());

                // Send streaming request
                var stream = anthropicClient.messages().createStreaming(request);

                StringBuilder responseContent = new StringBuilder();
                AtomicReference<String> modelNameRef = new AtomicReference<>(config.getModelName());

                // Process streaming response
                stream.stream().forEach(event -> {
                    if (event.isContentBlockDelta()) {
                        var delta = event.asContentBlockDelta();
                        var contentDelta = delta.delta();
                        if (contentDelta.isText()) {
                            String text = contentDelta.asText().text();
                            responseContent.append(text);
                            handler.onChunk(text);
                        }
                    } else if (event.isMessageStart()) {
                        var start = event.asMessageStart();
                        modelNameRef.set(start.message().model().toString());
                    } else if (event.isMessageStop()) {
                        handler.onComplete(ModelResponse.builder().content(responseContent.toString())
                                .modelName(modelNameRef.get()).providerType(ModelProviderType.ANTHROPIC.name())
                                .build());
                    }
                });

                // Track success metrics
                long responseTime = System.currentTimeMillis() - startTime;
                trackMetrics(responseTime, true, null);

                return ModelResponse.builder().content(responseContent.toString()).modelName(modelNameRef.get())
                        .providerType(ModelProviderType.ANTHROPIC.name()).build();

            } catch (Exception e) {
                // Track error metrics
                long responseTime = System.currentTimeMillis() - startTime;
                trackMetrics(responseTime, false, e.getMessage() != null ? e.getMessage() : "Unknown error");

                logger.error("Error completing Anthropic streaming request", e);
                handler.onError(e);
                throw new RuntimeException("Anthropic streaming completion failed", e);
            }
        }, executorService);
    }

    @Override
    public boolean isAvailable() {
        try {
            // Simple availability check
            return config.isEnabled() && config.getApiKey() != null && !config.getApiKey().isEmpty();
        } catch (Exception e) {
            logger.debug("Anthropic availability check failed", e);
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

            return new ModelHealthStatus(available, Instant.now(), avgResponseTime, successRate, errorCount.get(),
                    lastError.get(), lastErrorTime.get());
        } catch (Exception e) {
            return new ModelHealthStatus(false, Instant.now(), -1, 0.0, errorCount.get() + 1,
                    e.getMessage() != null ? e.getMessage() : "Unknown error", Instant.now());
        }
    }

    @Override
    public ModelProviderType getProviderType() {
        return ModelProviderType.ANTHROPIC;
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
                List<MessageParam> messages = List
                        .of(MessageParam.builder().role(MessageParam.Role.USER).content("Hello").build());

                MessageCreateParams request = MessageCreateParams.builder().model(config.getModelName()).maxTokens(5)
                        .messages(messages).build();

                Message response = anthropicClient.messages().create(request);
                return response != null;
            } catch (Exception e) {
                logger.debug("Anthropic connection test failed", e);
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
        // For now, return null as the Anthropic SDK doesn't expose rate limit headers directly
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
