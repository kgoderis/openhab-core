package org.openhab.core.ai.model.clients;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
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
import org.openhab.core.ai.model.configuration.LMStudioConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * LM Studio LLM Client implementation using HTTP client and OpenAI-compatible API.
 * 
 * LM Studio provides an OpenAI-compatible REST API that can be used with standard HTTP clients.
 * This implementation uses Jackson for JSON processing and Java's built-in HTTP client.
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public class LMStudioClient implements ModelClient {

    private final Logger logger = LoggerFactory.getLogger(LMStudioClient.class);
    private final LMStudioConfiguration config;
    private final @Nullable ActionRegistry actionRegistry;
    private final ExecutorService executorService;
    private final ModelClientInfo providerInfo;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public LMStudioClient(LMStudioConfiguration config, @Nullable ActionRegistry actionRegistry) {
        this.config = config;
        this.actionRegistry = actionRegistry;
        this.executorService = Executors.newCachedThreadPool();
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(config.getTimeoutMs())).build();
        this.objectMapper = new ObjectMapper();

        this.providerInfo = new ModelClientInfo(ModelProviderType.LMSTUDIO, config.getModelName(), true, // supportsFunctionCalling
                true, // supportsStreaming
                true, // supportsMultimodal
                config.getMaxTokens(), 0.0); // Local provider, no cost
    }

    @Override
    public CompletableFuture<ModelResponse> complete(String prompt, ModelParameters params) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Build request payload (OpenAI-compatible format)
                ObjectNode requestBody = objectMapper.createObjectNode();
                requestBody.put("model", config.getModelName());
                requestBody.put("max_tokens", params.getMaxTokens());
                requestBody.put("temperature", params.getTemperature());
                requestBody.put("stream", false);

                // Build messages array
                ArrayNode messages = requestBody.putArray("messages");
                ObjectNode message = messages.addObject();
                message.put("role", "user");
                message.put("content", prompt);

                // Add system prompt if configured
                if (config.getSystemPrompt() != null && !config.getSystemPrompt().isEmpty()) {
                    ObjectNode systemMessage = messages.addObject();
                    systemMessage.put("role", "system");
                    systemMessage.put("content", config.getSystemPrompt());
                }

                String requestJson = objectMapper.writeValueAsString(requestBody);
                logger.debug("LM Studio request: model={}, maxTokens={}, temperature={}", config.getModelName(),
                        params.getMaxTokens(), params.getTemperature());

                // Build HTTP request
                String url = config.getBaseUrl() + "/v1/chat/completions";
                HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                        .timeout(Duration.ofMillis(config.getTimeoutMs())).build();

                // Send request
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    throw new RuntimeException(
                            "LM Studio API error: " + response.statusCode() + " - " + response.body());
                }

                // Parse response
                JsonNode responseJson = objectMapper.readTree(response.body());
                String responseContent = "";

                if (responseJson.has("choices") && responseJson.get("choices").isArray()) {
                    JsonNode choices = responseJson.get("choices");
                    if (choices.size() > 0) {
                        JsonNode choice = choices.get(0);
                        if (choice.has("message") && choice.get("message").has("content")) {
                            responseContent = choice.get("message").get("content").asText();
                        }
                    }
                }

                return ModelResponse.builder().content(responseContent).modelName(config.getModelName())
                        .providerType(ModelProviderType.LMSTUDIO.name()).build();

            } catch (Exception e) {
                logger.error("Error completing LM Studio request", e);
                throw new RuntimeException("LM Studio completion failed", e);
            }
        }, executorService);
    }

    @Override
    public CompletableFuture<ModelResponse> completeWithStreaming(String prompt, ModelParameters params,
            ModelStreamHandler handler) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Build request payload (OpenAI-compatible format)
                ObjectNode requestBody = objectMapper.createObjectNode();
                requestBody.put("model", config.getModelName());
                requestBody.put("max_tokens", params.getMaxTokens());
                requestBody.put("temperature", params.getTemperature());
                requestBody.put("stream", true);

                // Build messages array
                ArrayNode messages = requestBody.putArray("messages");
                ObjectNode message = messages.addObject();
                message.put("role", "user");
                message.put("content", prompt);

                // Add system prompt if configured
                if (config.getSystemPrompt() != null && !config.getSystemPrompt().isEmpty()) {
                    ObjectNode systemMessage = messages.addObject();
                    systemMessage.put("role", "system");
                    systemMessage.put("content", config.getSystemPrompt());
                }

                String requestJson = objectMapper.writeValueAsString(requestBody);
                logger.debug("LM Studio streaming request: model={}, maxTokens={}, temperature={}",
                        config.getModelName(), params.getMaxTokens(), params.getTemperature());

                // Build HTTP request
                String url = config.getBaseUrl() + "/v1/chat/completions";
                HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                        .timeout(Duration.ofMillis(config.getTimeoutMs())).build();

                // Send streaming request
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    throw new RuntimeException(
                            "LM Studio API error: " + response.statusCode() + " - " + response.body());
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
                            JsonNode event = objectMapper.readTree(data);
                            if (event.has("choices") && event.get("choices").isArray()) {
                                JsonNode choices = event.get("choices");
                                if (choices.size() > 0) {
                                    JsonNode choice = choices.get(0);
                                    if (choice.has("delta") && choice.get("delta").has("content")) {
                                        String chunk = choice.get("delta").get("content").asText();
                                        contentBuilder.append(chunk);
                                        handler.onChunk(chunk);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            logger.debug("Error parsing streaming chunk: {}", e.getMessage());
                        }
                    }
                }

                String finalContent = contentBuilder.toString();
                ModelResponse llmResponse = ModelResponse.builder().content(finalContent)
                        .modelName(config.getModelName()).providerType(ModelProviderType.LMSTUDIO.name()).build();

                handler.onComplete(llmResponse);
                return llmResponse;

            } catch (Exception e) {
                logger.error("Error completing LM Studio streaming request", e);
                handler.onError(e);
                throw new RuntimeException("LM Studio streaming completion failed", e);
            }
        }, executorService);
    }

    @Override
    public boolean isAvailable() {
        try {
            // Simple health check by testing connection
            return testConnection().get();
        } catch (Exception e) {
            logger.debug("LM Studio client not available", e);
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
            Instant now = Instant.now();
            return new ModelHealthStatus(available, now, available ? 100L : 0L, available ? 1.0 : 0.0, 0, null, null);
        } catch (Exception e) {
            Instant now = Instant.now();
            return new ModelHealthStatus(false, now, 0L, 0.0, 1, e.getMessage(), now);
        }
    }

    @Override
    public ModelProviderType getProviderType() {
        return ModelProviderType.LMSTUDIO;
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
                ObjectNode requestBody = objectMapper.createObjectNode();
                requestBody.put("model", config.getModelName());
                requestBody.put("max_tokens", 5);
                requestBody.put("stream", false);

                ArrayNode messages = requestBody.putArray("messages");
                ObjectNode message = messages.addObject();
                message.put("role", "user");
                message.put("content", "Hello");

                String requestJson = objectMapper.writeValueAsString(requestBody);

                String url = config.getBaseUrl() + "/v1/chat/completions";
                HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestJson)).timeout(Duration.ofMillis(10000)) // Shorter
                                                                                                                  // timeout
                                                                                                                  // for
                                                                                                                  // test
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                return response.statusCode() == 200;
            } catch (Exception e) {
                logger.debug("LM Studio connection test failed", e);
                return false;
            }
        }, executorService);
    }

    @Override
    public double estimateCost(String prompt, ModelParameters params) {
        // Local provider, no cost
        return 0.0;
    }

    @Override
    public int getMaxTokens() {
        return config.getMaxTokens();
    }

    @Override
    public double getCostPer1kTokens() {
        // Local provider, no cost
        return 0.0;
    }

    @Override
    public boolean supportsFunctionCalling() {
        // TODO: Check if LM Studio supports function calling
        return false;
    }

    @Override
    public boolean supportsStreaming() {
        return true;
    }

    @Override
    public boolean supportsMultimodal() {
        // TODO: Check if LM Studio supports multimodal input
        return false;
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
