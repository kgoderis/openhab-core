package org.openhab.core.ai.model.api;

import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.response.ModelResponse;
import org.openhab.core.ai.model.ModelClientInfo;
import org.openhab.core.ai.model.ModelException;
import org.openhab.core.ai.model.ModelParameters;
import org.openhab.core.ai.model.ModelRateLimitInfo;
import org.openhab.core.ai.model.monitoring.ModelHealthMetrics;

/**
 * Core interface for LLM (Large Language Model) clients.
 * 
 * This interface provides a unified abstraction for interacting with various LLM providers,
 * including both cloud-based services (OpenAI, Anthropic, Google) and local models (Ollama, LocalAI).
 * 
 * @author openHAB AI Team
 * @since 4.0.0
 */
@NonNullByDefault
public interface ModelClient {

    /**
     * Generates a completion response for the given prompt.
     * 
     * @param prompt The input prompt for the LLM
     * @param params Configuration parameters for the generation
     * @return A CompletableFuture containing the LLM response
     * @throws ModelException if the request fails
     */
    CompletableFuture<ModelResponse> complete(String prompt, ModelParameters params);

    /**
     * Generates a streaming completion response.
     * 
     * @param prompt The input prompt for the LLM
     * @param params Configuration parameters for the generation
     * @param handler Handler for processing streaming responses
     * @return A CompletableFuture containing the final LLM response
     * @throws ModelException if the request fails
     */
    CompletableFuture<ModelResponse> completeWithStreaming(String prompt, ModelParameters params,
            ModelStreamHandler handler);

    /**
     * Checks if the LLM client is available and ready to process requests.
     * 
     * @return true if the client is available, false otherwise
     */
    boolean isAvailable();

    /**
     * Gets information about the LLM provider and its capabilities.
     * 
     * @return Provider information including model details and capabilities
     */
    ModelClientInfo getProviderInfo();

    /**
     * Gets the current health status of the LLM client.
     * 
     * @return Health status including availability, performance metrics, and error information
     */
    ModelHealthMetrics getHealthStatus();

    /**
     * Gets the provider type for this client.
     * 
     * @return The provider type (e.g., OPENAI, ANTHROPIC, OLLAMA)
     */
    ModelProviderType getProviderType();

    /**
     * Gets the model name being used by this client.
     * 
     * @return The model name (e.g., "gpt-4o-mini", "claude-3-5-sonnet", "llama3.1:8b")
     */
    String getModelName();

    /**
     * Tests the connection to the LLM provider.
     * 
     * @return A CompletableFuture that completes with true if the connection is successful
     */
    CompletableFuture<Boolean> testConnection();

    /**
     * Gets the estimated cost for a completion request.
     * 
     * @param prompt The input prompt
     * @param params Configuration parameters
     * @return Estimated cost in the provider's currency (e.g., USD)
     */
    double estimateCost(String prompt, ModelParameters params);

    /**
     * Gets the maximum number of tokens supported by this model.
     * 
     * @return Maximum token limit
     */
    int getMaxTokens();

    /**
     * Gets the cost per 1K tokens for this model.
     * 
     * @return Cost per 1K tokens in the provider's currency
     */
    double getCostPer1kTokens();

    /**
     * Checks if this provider supports function/tool calling.
     * 
     * @return true if function calling is supported
     */
    boolean supportsFunctionCalling();

    /**
     * Checks if this provider supports streaming responses.
     * 
     * @return true if streaming is supported
     */
    boolean supportsStreaming();

    /**
     * Checks if this provider supports multimodal input (text + images).
     * 
     * @return true if multimodal input is supported
     */
    boolean supportsMultimodal();

    /**
     * Gets the current rate limit status.
     * 
     * @return Rate limit information including remaining requests and reset time
     */
    @Nullable
    ModelRateLimitInfo getRateLimitInfo();
}
