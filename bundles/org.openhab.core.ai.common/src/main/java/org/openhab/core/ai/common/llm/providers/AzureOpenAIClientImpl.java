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
import org.openhab.core.ai.common.llm.configuration.AzureOpenAIConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Azure OpenAI LLM Client implementation using HTTP client.
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
    private final HttpClient httpClient;
    private final Gson gson;

    public AzureOpenAIClientImpl(AzureOpenAIConfiguration config, @Nullable AIActionRegistry actionRegistry) {
        this.config = config;
        this.actionRegistry = actionRegistry;
        this.executorService = Executors.newCachedThreadPool();
        this.gson = new Gson();

        // Initialize HTTP client
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(config.getTimeoutMs())).build();

        this.providerInfo = new LLMProviderInfo(LLMProviderType.AZURE, config.getModelName(), true, // supportsFunctionCalling
                true, // supportsStreaming
                true, // supportsMultimodal
                config.getMaxTokens(), 0.03 // Default cost per 1K tokens for Azure OpenAI
        );
    }

    @Override
    public CompletableFuture<LLMResponse> complete(String prompt, LLMParameters params) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Build request payload
                JsonObject requestBody = new JsonObject();
                requestBody.addProperty("model", config.getDeploymentName());
                requestBody.addProperty("max_tokens", params.getMaxTokens());
                requestBody.addProperty("temperature", params.getTemperature());

                JsonArray messages = new JsonArray();
                JsonObject message = new JsonObject();
                message.addProperty("role", "user");
                message.addProperty("content", prompt);
                messages.add(message);
                requestBody.add("messages", messages);

                String requestJson = gson.toJson(requestBody);
                logger.debug("Azure OpenAI request: {}", requestJson);

                // Build HTTP request
                String url = config.getEndpoint() + "/openai/deployments/" + config.getDeploymentName()
                        + "/chat/completions?api-version=2024-02-15-preview";
                HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url))
                        .header("Content-Type", "application/json").header("api-key", config.getApiKey())
                        .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                        .timeout(Duration.ofMillis(config.getTimeoutMs())).build();

                // Send request
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    throw new RuntimeException(
                            "Azure OpenAI API error: " + response.statusCode() + " - " + response.body());
                }

                // Parse response
                JsonObject responseJson = JsonParser.parseString(response.body()).getAsJsonObject();
                JsonArray choices = responseJson.getAsJsonArray("choices");

                String responseContent = "";
                if (choices.size() > 0) {
                    JsonObject choice = choices.get(0).getAsJsonObject();
                    JsonObject messageResponse = choice.getAsJsonObject("message");
                    responseContent = messageResponse.get("content").getAsString();
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
                // Build request payload
                JsonObject requestBody = new JsonObject();
                requestBody.addProperty("model", config.getDeploymentName());
                requestBody.addProperty("max_tokens", params.getMaxTokens());
                requestBody.addProperty("temperature", params.getTemperature());
                requestBody.addProperty("stream", true);

                JsonArray messages = new JsonArray();
                JsonObject message = new JsonObject();
                message.addProperty("role", "user");
                message.addProperty("content", prompt);
                messages.add(message);
                requestBody.add("messages", messages);

                String requestJson = gson.toJson(requestBody);
                logger.debug("Azure OpenAI streaming request: {}", requestJson);

                // Build HTTP request
                String url = config.getEndpoint() + "/openai/deployments/" + config.getDeploymentName()
                        + "/chat/completions?api-version=2024-02-15-preview";
                HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url))
                        .header("Content-Type", "application/json").header("api-key", config.getApiKey())
                        .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                        .timeout(Duration.ofMillis(config.getTimeoutMs())).build();

                // Send streaming request
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    throw new RuntimeException(
                            "Azure OpenAI API error: " + response.statusCode() + " - " + response.body());
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
                            if (event.has("choices") && event.getAsJsonArray("choices").size() > 0) {
                                JsonObject choice = event.getAsJsonArray("choices").get(0).getAsJsonObject();
                                if (choice.has("delta")) {
                                    JsonObject delta = choice.getAsJsonObject("delta");
                                    if (delta.has("content")) {
                                        String chunk = delta.get("content").getAsString();
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

                String content = contentBuilder.toString();
                LLMResponse llmResponse = LLMResponse.builder().content(content).modelName(config.getModelName())
                        .providerType(LLMProviderType.AZURE.name()).build();

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
                JsonObject requestBody = new JsonObject();
                requestBody.addProperty("model", config.getDeploymentName());
                requestBody.addProperty("max_tokens", 5);

                JsonArray messages = new JsonArray();
                JsonObject message = new JsonObject();
                message.addProperty("role", "user");
                message.addProperty("content", "Hello");
                messages.add(message);
                requestBody.add("messages", messages);

                String requestJson = gson.toJson(requestBody);

                String url = config.getEndpoint() + "/openai/deployments/" + config.getDeploymentName()
                        + "/chat/completions?api-version=2024-02-15-preview";
                HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url))
                        .header("Content-Type", "application/json").header("api-key", config.getApiKey())
                        .POST(HttpRequest.BodyPublishers.ofString(requestJson)).timeout(Duration.ofMillis(10000)) // Shorter
                                                                                                                  // timeout
                                                                                                                  // for
                                                                                                                  // test
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                return response.statusCode() == 200;
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
