package org.openhab.core.ai.model.clients;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.model.ModelClientInfo;
import org.openhab.core.ai.model.ModelHealthStatus;
import org.openhab.core.ai.model.ModelParameters;
import org.openhab.core.ai.model.ModelRateLimitInfo;
import org.openhab.core.ai.model.ModelResponse;
import org.openhab.core.ai.model.api.ModelClient;
import org.openhab.core.ai.model.api.ModelProviderType;
import org.openhab.core.ai.model.api.ModelStreamHandler;

/**
 * Stub implementation of ModelClient for testing and development.
 * 
 * This is a placeholder implementation that can be used during development
 * until the actual provider-specific clients are implemented.
 * 
 * @author openHAB AI Team
 * @since 4.0.0
 */
@NonNullByDefault
public class StubModelClient implements ModelClient {

    private final ModelProviderType providerType;
    private final String modelName;

    public StubModelClient(ModelProviderType providerType, String modelName) {
        this.providerType = providerType;
        this.modelName = modelName;
    }

    @Override
    public CompletableFuture<ModelResponse> complete(String prompt, ModelParameters params) {
        return CompletableFuture.completedFuture(
                ModelResponse.builder().content("This is a stub response from " + providerType + " model " + modelName)
                        .modelName(modelName).providerType(providerType.name()).build());
    }

    @Override
    public CompletableFuture<ModelResponse> completeWithStreaming(String prompt, ModelParameters params,
            ModelStreamHandler handler) {
        // Simulate streaming by calling onChunk and then onComplete
        handler.onChunk("This is a stub streaming response from " + providerType + " model " + modelName);
        ModelResponse response = ModelResponse.builder()
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
    public ModelClientInfo getProviderInfo() {
        return new ModelClientInfo(providerType, modelName, true, true, true, 4000, 0.001);
    }

    @Override
    public ModelHealthStatus getHealthStatus() {
        return new ModelHealthStatus(true, Instant.now(), 0, 1.0, 0, null, null);
    }

    @Override
    public ModelProviderType getProviderType() {
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
    public double estimateCost(String prompt, ModelParameters params) {
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
    public @Nullable ModelRateLimitInfo getRateLimitInfo() {
        return new ModelRateLimitInfo(1000, 1000, java.time.Instant.now().plusSeconds(3600), "per hour");
    }
}
