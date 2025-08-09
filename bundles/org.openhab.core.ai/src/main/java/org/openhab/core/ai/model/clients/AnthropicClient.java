package org.openhab.core.ai.model.clients;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
import org.openhab.core.ai.model.configuration.AnthropicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.MessageParam;

/**
 * Anthropic LLM Client implementation using official Anthropic Java SDK.
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public class AnthropicClient implements ModelClient {

    private final Logger logger = LoggerFactory.getLogger(AnthropicClient.class);
    private final AnthropicConfiguration config;
    private final @Nullable ActionRegistry actionRegistry;
    private final ExecutorService executorService;
    private final ModelClientInfo providerInfo;
    private final com.anthropic.client.AnthropicClient anthropicClient;

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
            try {
                // Build messages array
                List<MessageParam> messages = buildMessages(prompt);

                // Create request
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

                return ModelResponse.builder().content(responseContent).modelName(response.model().toString())
                        .providerType(ModelProviderType.ANTHROPIC.name()).build();

            } catch (Exception e) {
                logger.error("Error completing Anthropic request", e);
                throw new RuntimeException("Anthropic completion failed", e);
            }
        }, executorService);
    }

    /**
     * Build messages array for Anthropic API
     */
    private List<MessageParam> buildMessages(String prompt) {
        List<MessageParam> messages = new java.util.ArrayList<>();

        // Add system message first if configured
        if (config.getSystemPrompt() != null && !config.getSystemPrompt().isEmpty()) {
            messages.add(MessageParam.builder().role(MessageParam.Role.USER)
                    .content("System: " + config.getSystemPrompt() + "\n\nUser: " + prompt).build());
        } else {
            // Add user message
            messages.add(MessageParam.builder().role(MessageParam.Role.USER).content(prompt).build());
        }

        return messages;
    }

    @Override
    public CompletableFuture<ModelResponse> completeWithStreaming(String prompt, ModelParameters params,
            ModelStreamHandler handler) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Build messages array
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
                java.util.concurrent.atomic.AtomicReference<String> modelNameRef = new java.util.concurrent.atomic.AtomicReference<>(
                        config.getModelName());

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

                return ModelResponse.builder().content(responseContent.toString()).modelName(modelNameRef.get())
                        .providerType(ModelProviderType.ANTHROPIC.name()).build();

            } catch (Exception e) {
                logger.error("Error completing Anthropic streaming request", e);
                handler.onError(e);
                throw new RuntimeException("Anthropic streaming completion failed", e);
            }
        }, executorService);
    }

    @Override
    public boolean isAvailable() {
        try {
            return testConnection().get();
        } catch (Exception e) {
            logger.debug("Anthropic client not available", e);
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
            return new ModelHealthStatus(available, java.time.Instant.now(), available ? 100 : -1, // TODO: Measure
                                                                                                   // actual
                                                                                                   // response time
                    1.0, // TODO: Calculate actual success rate
                    0, // TODO: Track error count
                    null, // TODO: Track last error
                    null // TODO: Track last error time
            );
        } catch (Exception e) {
            return new ModelHealthStatus(false, java.time.Instant.now(), -1, 0.0, 1, e.getMessage(),
                    java.time.Instant.now());
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
        // TODO: Extract rate limit info from response headers
        return null;
    }

    /**
     * Get available actions from the registry
     */
    private List<Action> getAvailableActions() {
        // TODO: Implement proper action retrieval
        return List.of();
    }
}
