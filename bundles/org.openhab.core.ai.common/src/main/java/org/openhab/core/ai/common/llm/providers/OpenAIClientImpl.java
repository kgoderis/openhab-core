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
import org.openhab.core.ai.common.llm.configuration.OpenAIConfiguration;
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
public class OpenAIClientImpl implements LLMClient {

    private final Logger logger = LoggerFactory.getLogger(OpenAIClientImpl.class);
    private final OpenAIConfiguration config;
    private final com.openai.client.OpenAIClient openAIClient;
    private final @Nullable AIActionRegistry actionRegistry;
    private final ExecutorService executorService;
    private final LLMProviderInfo providerInfo;

    public OpenAIClientImpl(OpenAIConfiguration config, @Nullable AIActionRegistry actionRegistry) {
        this.config = config;
        this.actionRegistry = actionRegistry;
        this.executorService = Executors.newCachedThreadPool();

        // Initialize OpenAI client
        this.openAIClient = OpenAIOkHttpClient.builder().apiKey(config.getApiKey()).baseUrl(config.getBaseUrl())
                .timeout(java.time.Duration.ofMillis(config.getTimeoutMs())).maxRetries(config.getRetryAttempts())
                .build();

        this.providerInfo = new LLMProviderInfo(LLMProviderType.OPENAI, config.getModelName(), true, // supportsFunctionCalling
                true, // supportsStreaming
                true, // supportsMultimodal
                config.getMaxTokens(), config.getCostPer1kTokens());
    }

    @Override
    public CompletableFuture<LLMResponse> complete(String prompt, LLMParameters params) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                ChatCompletionCreateParams.Builder paramsBuilder = ChatCompletionCreateParams.builder()
                        .model(config.getModelName()).addUserMessage(prompt).temperature(params.getTemperature())
                        .maxTokens(params.getMaxTokens());

                // Add system prompt if configured
                if (config.getSystemPrompt() != null && !config.getSystemPrompt().isEmpty()) {
                    // TODO: Add system message when ChatMessage import is resolved
                    logger.debug("System prompt configured but not yet implemented: {}", config.getSystemPrompt());
                }

                ChatCompletionCreateParams requestParams = paramsBuilder.build();
                ChatCompletion response = openAIClient.chat().completions().create(requestParams);

                // Return regular text response
                String content = response.choices().get(0).message().content().orElse("");
                return LLMResponse.builder().content(content).modelName(response.model())
                        .providerType(LLMProviderType.OPENAI.name()).build();

            } catch (Exception e) {
                logger.error("Error completing OpenAI request", e);
                throw new RuntimeException("OpenAI completion failed", e);
            }
        }, executorService);
    }

    @Override
    public CompletableFuture<LLMResponse> completeWithStreaming(String prompt, LLMParameters params,
            LLMStreamHandler handler) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                ChatCompletionCreateParams.Builder paramsBuilder = ChatCompletionCreateParams.builder()
                        .model(config.getModelName()).addUserMessage(prompt).temperature(params.getTemperature())
                        .maxTokens(params.getMaxTokens());

                // Add system prompt if configured
                if (config.getSystemPrompt() != null && !config.getSystemPrompt().isEmpty()) {
                    // TODO: Add system message when ChatMessage import is resolved
                    logger.debug("System prompt configured but not yet implemented: {}", config.getSystemPrompt());
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

                LLMResponse response = LLMResponse.builder().content(content).modelName(finalResponse.model())
                        .providerType(LLMProviderType.OPENAI.name()).build();

                handler.onComplete(response);
                return response;

            } catch (Exception e) {
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
        return LLMProviderType.OPENAI;
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
    public double estimateCost(String prompt, LLMParameters params) {
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
    public @Nullable LLMRateLimitInfo getRateLimitInfo() {
        // TODO: Extract rate limit info from response headers
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
