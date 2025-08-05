package org.openhab.core.ai.common.llm.providers;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.action.AIActionRegistry;
import org.openhab.core.ai.common.api.action.AIAction;
import org.openhab.core.ai.common.api.llm.LLMClient;
import org.openhab.core.ai.common.api.llm.LLMHealthStatus;
import org.openhab.core.ai.common.api.llm.LLMParameters;
import org.openhab.core.ai.common.api.llm.LLMProviderInfo;
import org.openhab.core.ai.common.api.llm.LLMProviderType;
import org.openhab.core.ai.common.api.llm.LLMRateLimitInfo;
import org.openhab.core.ai.common.api.llm.LLMResponse;
import org.openhab.core.ai.common.api.llm.LLMStreamHandler;
import org.openhab.core.ai.common.llm.configuration.AzureOpenAIConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.ChatCompletions;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.openai.models.ChatRequestUserMessage;
import com.azure.core.credential.AzureKeyCredential;

/**
 * Azure OpenAI LLM Client implementation using official Azure OpenAI Java SDK.
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public class AzureOpenAIClientImpl implements LLMClient {

    private final Logger logger = LoggerFactory.getLogger(AzureOpenAIClientImpl.class);
    private final AzureOpenAIConfiguration config;
    private final @Nullable AIActionRegistry actionRegistry;
    private final ExecutorService executorService;
    private final LLMProviderInfo providerInfo;
    private final OpenAIClient openAIClient;

    public AzureOpenAIClientImpl(AzureOpenAIConfiguration config, @Nullable AIActionRegistry actionRegistry) {
        this.config = config;
        this.actionRegistry = actionRegistry;
        this.executorService = Executors.newCachedThreadPool();

        // Initialize Azure OpenAI client
        this.openAIClient = new OpenAIClientBuilder().endpoint(config.getEndpoint())
                .credential(new AzureKeyCredential(config.getApiKey())).buildClient();

        this.providerInfo = new LLMProviderInfo(LLMProviderType.AZURE, config.getModelName(), true, // supportsFunctionCalling
                true, // supportsStreaming
                true, // supportsMultimodal
                config.getMaxTokens(), 0.0); // TODO: Add cost per 1k tokens to config
    }

    @Override
    public CompletableFuture<LLMResponse> complete(String prompt, LLMParameters params) {
        return CompletableFuture.supplyAsync(() -> {
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

                return LLMResponse.builder().content(responseContent).modelName(config.getModelName())
                        .providerType(LLMProviderType.AZURE.name()).build();

            } catch (Exception e) {
                logger.error("Error completing Azure OpenAI request", e);
                throw new RuntimeException("Azure OpenAI completion failed", e);
            }
        }, executorService);
    }

    @Override
    public CompletableFuture<LLMResponse> completeWithStreaming(String prompt, LLMParameters params,
            LLMStreamHandler handler) {
        return CompletableFuture.supplyAsync(() -> {
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

                LLMResponse llmResponse = LLMResponse.builder().content(responseContent.toString())
                        .modelName(config.getModelName()).providerType(LLMProviderType.AZURE.name()).build();

                handler.onComplete(llmResponse);
                return llmResponse;

            } catch (Exception e) {
                logger.error("Error completing Azure OpenAI streaming request", e);
                handler.onError(e);
                throw new RuntimeException("Azure OpenAI streaming completion failed", e);
            }
        }, executorService);
    }

    @Override
    public boolean isAvailable() {
        try {
            // Simple health check by testing connection
            return testConnection().get();
        } catch (Exception e) {
            logger.debug("Azure OpenAI client not available", e);
            return false;
        }
    }

    @Override
    public LLMProviderInfo getProviderInfo() {
        return providerInfo;
    }

    @Override
    public LLMHealthStatus getHealthStatus() {
        try {
            boolean available = isAvailable();
            return new LLMHealthStatus(available, java.time.Instant.now(), available ? 100 : -1, // TODO: Measure actual
                                                                                                 // response time
                    1.0, // TODO: Calculate actual success rate
                    0, // TODO: Track error count
                    null, // TODO: Track last error
                    null // TODO: Track last error time
            );
        } catch (Exception e) {
            return new LLMHealthStatus(false, java.time.Instant.now(), -1, 0.0, 1, e.getMessage(),
                    java.time.Instant.now());
        }
    }

    @Override
    public LLMProviderType getProviderType() {
        return LLMProviderType.AZURE;
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
    public double estimateCost(String prompt, LLMParameters params) {
        // Rough estimation based on token count
        int estimatedTokens = prompt.length() / 4; // Rough approximation
        estimatedTokens += params.getMaxTokens();

        return (estimatedTokens / 1000.0) * 0.03; // Default cost per 1K tokens for Azure OpenAI
    }

    @Override
    public int getMaxTokens() {
        return config.getMaxTokens();
    }

    @Override
    public double getCostPer1kTokens() {
        return 0.03; // Default cost per 1K tokens for Azure OpenAI
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
    public @Nullable LLMRateLimitInfo getRateLimitInfo() {
        // TODO: Extract rate limit info from response headers when SDK is available
        return null;
    }

    /**
     * Get available actions from the registry
     */
    private List<AIAction> getAvailableActions() {
        // TODO: Implement proper action retrieval
        return List.of();
    }
}
