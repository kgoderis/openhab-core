package org.openhab.core.ai.common.llm.providers;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.api.llm.LLMClient;
import org.openhab.core.ai.common.api.llm.LLMProviderType;
import org.openhab.core.ai.common.llm.LLMHealthStatus;
import org.openhab.core.ai.common.llm.LLMParameters;
import org.openhab.core.ai.common.llm.LLMProviderInfo;
import org.openhab.core.ai.common.llm.LLMRateLimitInfo;
import org.openhab.core.ai.common.llm.LLMResponse;

/**
 * Stub implementation of LLMClient for testing and development.
 * 
 * This is a placeholder implementation that can be used during development
 * until the actual provider-specific clients are implemented.
 * 
 * @author openHAB AI Team
 * @since 4.0.0
 */
@NonNullByDefault
public class StubLLMClient implements LLMClient {

    private final LLMProviderType providerType;
    private final String modelName;

    public StubLLMClient(LLMProviderType providerType, String modelName) {
        this.providerType = providerType;
        this.modelName = modelName;
    }

    @Override
    public CompletableFuture<LLMResponse> complete(String prompt, LLMParameters params) {
        return CompletableFuture.completedFuture(
                LLMResponse.builder().content("This is a stub response from " + providerType + " model " + modelName)
                        .modelName(modelName).providerType(providerType.name()).build());
    }

    @Override
    public CompletableFuture<LLMResponse> completeWithStreaming(String prompt, LLMParameters params,
            org.openhab.core.ai.common.api.llm.LLMStreamHandler handler) {
        // Simulate streaming by calling onChunk and then onComplete
        handler.onChunk("This is a stub streaming response from " + providerType + " model " + modelName);
        LLMResponse response = LLMResponse.builder()
                .content("This is a stub streaming response from " + providerType + " model " + modelName)
                .modelName(modelName).providerType(providerType.name()).build();
        handler.onComplete(response);
        return CompletableFuture.completedFuture(response);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public LLMProviderInfo getProviderInfo() {
        return new LLMProviderInfo(providerType, modelName, true, true, true, 4000, 0.001);
    }

    @Override
    public LLMHealthStatus getHealthStatus() {
        return new LLMHealthStatus(true, Instant.now(), 0, 1.0, 0, null, null);
    }

    @Override
    public LLMProviderType getProviderType() {
        return providerType;
    }

    @Override
    public String getModelName() {
        return modelName;
    }

    @Override
    public CompletableFuture<Boolean> testConnection() {
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public double estimateCost(String prompt, LLMParameters params) {
        return 0.001; // Stub cost
    }

    @Override
    public int getMaxTokens() {
        return 4000;
    }

    @Override
    public double getCostPer1kTokens() {
        return 0.001;
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
        return false;
    }

    @Override
    public @Nullable LLMRateLimitInfo getRateLimitInfo() {
        return new LLMRateLimitInfo(1000, 1000, java.time.Instant.now().plusSeconds(3600), "per hour");
    }
}
