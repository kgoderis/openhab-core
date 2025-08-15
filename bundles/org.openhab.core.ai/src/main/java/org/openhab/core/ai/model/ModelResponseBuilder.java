package org.openhab.core.ai.model;

import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@NonNullByDefault
public class ModelResponseBuilder {
    String content = "";
    String modelName = "";
    String providerType = "";
    Instant timestamp = Instant.now();
    int promptTokens = 0;
    int completionTokens = 0;
    int totalTokens = 0;
    double cost = 0.0;
    long responseTimeMs = 0;
    Map<String, Object> metadata = Map.of();
    @Nullable
    String finishReason;
    @Nullable
    String errorMessage;

    public ModelResponseBuilder content(String content) {
        this.content = content;
        return this;
    }

    public ModelResponseBuilder modelName(String modelName) {
        this.modelName = modelName;
        return this;
    }

    public ModelResponseBuilder providerType(String providerType) {
        this.providerType = providerType;
        return this;
    }

    public ModelResponseBuilder timestamp(Instant timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public ModelResponseBuilder promptTokens(int promptTokens) {
        this.promptTokens = promptTokens;
        return this;
    }

    public ModelResponseBuilder completionTokens(int completionTokens) {
        this.completionTokens = completionTokens;
        return this;
    }

    public ModelResponseBuilder totalTokens(int totalTokens) {
        this.totalTokens = totalTokens;
        return this;
    }

    public ModelResponseBuilder cost(double cost) {
        this.cost = cost;
        return this;
    }

    public ModelResponseBuilder responseTimeMs(long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
        return this;
    }

    public ModelResponseBuilder metadata(Map<String, Object> metadata) {
        this.metadata = metadata;
        return this;
    }

    public ModelResponseBuilder finishReason(@Nullable String finishReason) {
        this.finishReason = finishReason;
        return this;
    }

    public ModelResponseBuilder errorMessage(@Nullable String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    public ModelResponse build() {
        return new ModelResponse(this);
    }
}
