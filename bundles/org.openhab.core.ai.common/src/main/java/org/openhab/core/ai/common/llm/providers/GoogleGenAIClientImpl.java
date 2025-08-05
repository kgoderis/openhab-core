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
import org.openhab.core.ai.common.llm.configuration.GoogleGenAIConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;

/**
 * Google GenAI LLM Client implementation using official Google GenAI Java SDK.
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public class GoogleGenAIClientImpl implements LLMClient {

    private final Logger logger = LoggerFactory.getLogger(GoogleGenAIClientImpl.class);
    private final GoogleGenAIConfiguration config;
    private final @Nullable AIActionRegistry actionRegistry;
    private final ExecutorService executorService;
    private final LLMProviderInfo providerInfo;
    private final Client genaiClient;

    public GoogleGenAIClientImpl(GoogleGenAIConfiguration config, @Nullable AIActionRegistry actionRegistry) {
        this.config = config;
        this.actionRegistry = actionRegistry;
        this.executorService = Executors.newCachedThreadPool();

        // Initialize Google GenAI client
        this.genaiClient = Client.builder().apiKey(config.getApiKey()).build();

        this.providerInfo = new LLMProviderInfo(LLMProviderType.GOOGLE, config.getModelName(), true, // supportsFunctionCalling
                true, // supportsStreaming
                true, // supportsMultimodal
                config.getMaxTokens(), config.getCostPer1kTokens());
    }

    @Override
    public CompletableFuture<LLMResponse> complete(String prompt, LLMParameters params) {
        return CompletableFuture.supplyAsync(() -> {
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

                return LLMResponse.builder().content(responseContent).modelName(config.getModelName())
                        .providerType(LLMProviderType.GOOGLE.name()).build();

            } catch (Exception e) {
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
    public CompletableFuture<LLMResponse> completeWithStreaming(String prompt, LLMParameters params,
            LLMStreamHandler handler) {
        return CompletableFuture.supplyAsync(() -> {
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

                LLMResponse llmResponse = LLMResponse.builder().content(responseContent.toString())
                        .modelName(this.config.getModelName()).providerType(LLMProviderType.GOOGLE.name()).build();

                handler.onComplete(llmResponse);
                return llmResponse;

            } catch (Exception e) {
                logger.error("Error completing Google GenAI streaming request", e);
                handler.onError(e);
                throw new RuntimeException("Google GenAI streaming completion failed", e);
            }
        }, executorService);
    }

    @Override
    public boolean isAvailable() {
        try {
            // Simple health check by testing connection
            return testConnection().get();
        } catch (Exception e) {
            logger.debug("Google GenAI client not available", e);
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
        return LLMProviderType.GOOGLE;
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
