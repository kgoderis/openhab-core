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
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.action.ActionRegistry;
import org.openhab.core.ai.action.api.Action;
import org.openhab.core.ai.common.monitoring.api.HealthStatus;
import org.openhab.core.ai.common.monitoring.api.MetricKeys;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.service.snapshot.UnifiedMetricsSnapshot;
import org.openhab.core.ai.common.response.ModelResponse;
import org.osgi.service.component.annotations.Reference;
import org.openhab.core.ai.model.ModelClientInfo;
import org.openhab.core.ai.model.ModelParameters;
import org.openhab.core.ai.model.ModelRateLimitInfo;
import org.openhab.core.ai.model.api.ModelClient;
import org.openhab.core.ai.model.api.ModelProviderType;
import org.openhab.core.ai.model.api.ModelStreamHandler;
import org.openhab.core.ai.model.config.AzureOpenAIConfiguration;
import org.openhab.core.ai.common.monitoring.api.HealthMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.ChatCompletions;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.openai.models.ChatRequestMessage;
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

    @Reference
    private MetricsService metricsService;

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
                        messages.stream().map(msg -> (ChatRequestMessage) msg).collect(Collectors.toList()));
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

                // Record operation metrics
                long responseTime = System.currentTimeMillis() - startTime;
                metricsService.recordOperation("model", "completion")
                    .withSuccess(true)
                    .withDuration(responseTime)
                    .withData(Map.of(
                        "provider", "azure",
                        "model", config.getModelName(),
                        "promptLength", prompt.length(),
                        "maxTokens", params.getMaxTokens()
                    ))
                    .record();

                return ModelResponse.builder().withContent(responseContent).withModelName(config.getModelName())
                        .withProviderType(ModelProviderType.AZURE.name()).build();

            } catch (Exception e) {
                // Record error metrics
                long responseTime = System.currentTimeMillis() - startTime;
                metricsService.recordOperation("model", "completion")
                    .withSuccess(false)
                    .withDuration(responseTime)
                    .withData(Map.of(
                        "provider", "azure",
                        "model", config.getModelName(),
                        "error", e.getMessage() != null ? e.getMessage() : "Unknown error"
                    ))
                    .record();

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
                        messages.stream().map(msg -> (ChatRequestMessage) msg).collect(Collectors.toList()));
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

                ModelResponse llmResponse = ModelResponse.builder().withContent(responseContent.toString())
                        .withModelName(config.getModelName()).withProviderType(ModelProviderType.AZURE.name()).build();

                // Record operation metrics
                long responseTime = System.currentTimeMillis() - startTime;
                metricsService.recordOperation("model", "streaming-completion")
                    .withSuccess(true)
                    .withDuration(responseTime)
                    .withData(Map.of(
                        "provider", "azure",
                        "model", config.getModelName(),
                        "promptLength", prompt.length(),
                        "maxTokens", params.getMaxTokens()
                    ))
                    .record();

                handler.onComplete(llmResponse);
                return llmResponse;

            } catch (Exception e) {
                // Record error metrics
                long responseTime = System.currentTimeMillis() - startTime;
                metricsService.recordOperation("model", "streaming-completion")
                    .withSuccess(false)
                    .withDuration(responseTime)
                    .withData(Map.of(
                        "provider", "azure",
                        "model", config.getModelName(),
                        "error", e.getMessage() != null ? e.getMessage() : "Unknown error"
                    ))
                    .record();

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
    public HealthMetrics getHealthStatus() {
        try {
            boolean available = isAvailable();

            // Record health check operation
            metricsService.recordOperation("model", "health-check")
                .withSuccess(available)
                .withDuration(100)
                .withData(Map.of(
                    "provider", "azure",
                    "model", config.getModelName()
                ))
                .record();

            // Return health metrics from service
            return metricsService.getSnapshot(MetricKeys.modelHealth(config.getModelName()), UnifiedMetricsSnapshot.class);
            
        } catch (Exception e) {
            // Record failed health check
            metricsService.recordOperation("model", "health-check")
                .withSuccess(false)
                .withDuration(100)
                .withData(Map.of(
                    "provider", "azure",
                    "model", config.getModelName(),
                    "error", e.getMessage() != null ? e.getMessage() : "Unknown error"
                ))
                .record();

            // Return health metrics from service (will reflect the failure)
            return metricsService.getSnapshot(MetricKeys.modelHealth(config.getModelName()), UnifiedMetricsSnapshot.class);
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
                        messages.stream().map(msg -> (ChatRequestMessage) msg).collect(Collectors.toList()));
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
