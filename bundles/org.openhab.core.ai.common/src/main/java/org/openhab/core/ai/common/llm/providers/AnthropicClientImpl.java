package org.openhab.core.ai.common.llm.providers;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
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
import org.openhab.core.ai.common.llm.configuration.AnthropicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Anthropic LLM Client implementation using HTTP client.
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public class AnthropicClientImpl implements LLMClient {

    private final Logger logger = LoggerFactory.getLogger(AnthropicClientImpl.class);
    private final AnthropicConfiguration config;
    private final @Nullable AIActionRegistry actionRegistry;
    private final ExecutorService executorService;
    private final LLMProviderInfo providerInfo;
    private final HttpClient httpClient;
    private final Gson gson;

    public AnthropicClientImpl(AnthropicConfiguration config, @Nullable AIActionRegistry actionRegistry) {
        this.config = config;
        this.actionRegistry = actionRegistry;
        this.executorService = Executors.newCachedThreadPool();
        this.gson = new Gson();

        // Initialize HTTP client
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(config.getTimeoutMs())).build();

        this.providerInfo = new LLMProviderInfo(LLMProviderType.ANTHROPIC, config.getModelName(), true, // supportsFunctionCalling
                true, // supportsStreaming
                true, // supportsMultimodal
                config.getMaxTokens(), config.getCostPer1kTokens());
    }

    @Override
    public CompletableFuture<LLMResponse> complete(String prompt, LLMParameters params) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Build request payload
                JsonObject requestBody = new JsonObject();
                requestBody.addProperty("model", config.getModelName());
                requestBody.addProperty("max_tokens", params.getMaxTokens());
                requestBody.addProperty("temperature", params.getTemperature());

                JsonArray messages = new JsonArray();

                // Add system message first if configured
                if (config.getSystemPrompt() != null && !config.getSystemPrompt().isEmpty()) {
                    JsonObject systemMessage = new JsonObject();
                    systemMessage.addProperty("role", "system");
                    systemMessage.addProperty("content", config.getSystemPrompt());
                    messages.add(systemMessage);
                }

                // Add user message
                JsonObject message = new JsonObject();
                message.addProperty("role", "user");
                message.addProperty("content", prompt);
                messages.add(message);
                requestBody.add("messages", messages);

                String requestJson = gson.toJson(requestBody);
                logger.debug("Anthropic request: {}", requestJson);

                // Build HTTP request
                HttpRequest request = HttpRequest.newBuilder().uri(URI.create(config.getBaseUrl() + "/v1/messages"))
                        .header("Content-Type", "application/json").header("x-api-key", config.getApiKey())
                        .header("anthropic-version", "2023-06-01")
                        .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                        .timeout(Duration.ofMillis(config.getTimeoutMs())).build();

                // Send request
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    throw new RuntimeException(
                            "Anthropic API error: " + response.statusCode() + " - " + response.body());
                }

                // Parse response
                JsonObject responseJson = JsonParser.parseString(response.body()).getAsJsonObject();
                JsonArray content = responseJson.getAsJsonArray("content");
                String model = responseJson.get("model").getAsString();

                String responseContent = "";
                if (content.size() > 0) {
                    JsonObject firstContent = content.get(0).getAsJsonObject();
                    if (firstContent.has("text")) {
                        responseContent = firstContent.get("text").getAsString();
                    }
                }

                return LLMResponse.builder().content(responseContent).modelName(model)
                        .providerType(LLMProviderType.ANTHROPIC.name()).build();

            } catch (Exception e) {
                logger.error("Error completing Anthropic request", e);
                throw new RuntimeException("Anthropic completion failed", e);
            }
        }, executorService);
    }

    @Override
    public CompletableFuture<LLMResponse> completeWithStreaming(String prompt, LLMParameters params,
            LLMStreamHandler handler) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Build request payload
                JsonObject requestBody = new JsonObject();
                requestBody.addProperty("model", config.getModelName());
                requestBody.addProperty("max_tokens", params.getMaxTokens());
                requestBody.addProperty("temperature", params.getTemperature());
                requestBody.addProperty("stream", true);

                JsonArray messages = new JsonArray();

                // Add system message first if configured
                if (config.getSystemPrompt() != null && !config.getSystemPrompt().isEmpty()) {
                    JsonObject systemMessage = new JsonObject();
                    systemMessage.addProperty("role", "system");
                    systemMessage.addProperty("content", config.getSystemPrompt());
                    messages.add(systemMessage);
                }

                // Add user message
                JsonObject message = new JsonObject();
                message.addProperty("role", "user");
                message.addProperty("content", prompt);
                messages.add(message);
                requestBody.add("messages", messages);

                String requestJson = gson.toJson(requestBody);
                logger.debug("Anthropic streaming request: {}", requestJson);

                // Build HTTP request
                HttpRequest request = HttpRequest.newBuilder().uri(URI.create(config.getBaseUrl() + "/v1/messages"))
                        .header("Content-Type", "application/json").header("x-api-key", config.getApiKey())
                        .header("anthropic-version", "2023-06-01")
                        .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                        .timeout(Duration.ofMillis(config.getTimeoutMs())).build();

                // Send streaming request
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    throw new RuntimeException(
                            "Anthropic API error: " + response.statusCode() + " - " + response.body());
                }

                // Parse streaming response
                StringBuilder contentBuilder = new StringBuilder();
                String[] lines = response.body().split("\n");

                for (String line : lines) {
                    if (line.startsWith("data: ")) {
                        String data = line.substring(6);
                        if (data.equals("[DONE]")) {
                            break;
                        }

                        try {
                            JsonObject event = JsonParser.parseString(data).getAsJsonObject();
                            if (event.has("type") && event.get("type").getAsString().equals("content_block_delta")) {
                                JsonObject delta = event.getAsJsonObject("delta");
                                if (delta.has("text")) {
                                    String chunk = delta.get("text").getAsString();
                                    contentBuilder.append(chunk);
                                    handler.onChunk(chunk);
                                }
                            }
                        } catch (Exception e) {
                            logger.debug("Error parsing streaming chunk: {}", e.getMessage());
                        }
                    }
                }

                String content = contentBuilder.toString();
                LLMResponse llmResponse = LLMResponse.builder().content(content).modelName(config.getModelName())
                        .providerType(LLMProviderType.ANTHROPIC.name()).build();

                handler.onComplete(llmResponse);
                return llmResponse;

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
        return LLMProviderType.ANTHROPIC;
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
                JsonObject requestBody = new JsonObject();
                requestBody.addProperty("model", config.getModelName());
                requestBody.addProperty("max_tokens", 5);

                JsonArray messages = new JsonArray();
                JsonObject message = new JsonObject();
                message.addProperty("role", "user");
                message.addProperty("content", "Hello");
                messages.add(message);
                requestBody.add("messages", messages);

                String requestJson = gson.toJson(requestBody);

                HttpRequest request = HttpRequest.newBuilder().uri(URI.create(config.getBaseUrl() + "/v1/messages"))
                        .header("Content-Type", "application/json").header("x-api-key", config.getApiKey())
                        .header("anthropic-version", "2023-06-01")
                        .POST(HttpRequest.BodyPublishers.ofString(requestJson)).timeout(Duration.ofMillis(10000)) // Shorter
                                                                                                                  // timeout
                                                                                                                  // for
                                                                                                                  // test
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                return response.statusCode() == 200;
            } catch (Exception e) {
                logger.debug("Anthropic connection test failed", e);
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
