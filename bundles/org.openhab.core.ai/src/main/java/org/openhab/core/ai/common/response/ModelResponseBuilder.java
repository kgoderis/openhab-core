package org.openhab.core.ai.common.response;

import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.builder.AbstractBuilder;

/**
 * Builder for {@link ModelResponse} in the unified response hierarchy.
 *
 * <p>
 * Provides a fluent API with validation for constructing {@link ModelResponse}
 * instances.
 * </p>
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class ModelResponseBuilder extends AbstractBuilder<ModelResponse> {

    /**
     * Create a new ModelResponseBuilder instance.
     * 
     * @return a new builder instance
     */
    public static ModelResponseBuilder builder() {
        return new ModelResponseBuilder();
    }

    private @Nullable String id;
    private String content = "";
    private String modelName = "";
    private String providerType = "";
    private long timestamp = System.currentTimeMillis();
    private int promptTokens = 0;
    private int completionTokens = 0;
    private int totalTokens = 0;
    private double cost = 0.0;
    private long responseTimeMs = 0L;
    private Map<String, Object> metadata = Map.of();
    private @Nullable String finishReason;
    private @Nullable String errorMessage;

    public ModelResponseBuilder withId(String id) {
        this.id = Objects.requireNonNull(id, "id");
        return this;
    }

    public ModelResponseBuilder withContent(String content) {
        this.content = Objects.requireNonNull(content, "content");
        return this;
    }

    public ModelResponseBuilder withModelName(String modelName) {
        this.modelName = Objects.requireNonNull(modelName, "modelName");
        return this;
    }

    public ModelResponseBuilder withProviderType(String providerType) {
        this.providerType = Objects.requireNonNull(providerType, "providerType");
        return this;
    }

    public ModelResponseBuilder withTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public ModelResponseBuilder withPromptTokens(int promptTokens) {
        this.promptTokens = promptTokens;
        return this;
    }

    public ModelResponseBuilder withCompletionTokens(int completionTokens) {
        this.completionTokens = completionTokens;
        return this;
    }

    public ModelResponseBuilder withTotalTokens(int totalTokens) {
        this.totalTokens = totalTokens;
        return this;
    }

    public ModelResponseBuilder withCost(double cost) {
        this.cost = cost;
        return this;
    }

    public ModelResponseBuilder withResponseTimeMs(long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
        return this;
    }

    public ModelResponseBuilder withMetadata(Map<String, Object> metadata) {
        this.metadata = Objects.requireNonNull(metadata, "metadata");
        return this;
    }

    public ModelResponseBuilder withFinishReason(@Nullable String finishReason) {
        this.finishReason = finishReason;
        return this;
    }

    public ModelResponseBuilder withErrorMessage(@Nullable String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    @Override
    protected void validate() {
        validateRequiredString(content, "content");
        validateRequiredString(modelName, "modelName");
        validateRequiredString(providerType, "providerType");
        // Non-negative checks
        if (promptTokens < 0) {
            addValidationError("promptTokens must be >= 0");
        }
        if (completionTokens < 0) {
            addValidationError("completionTokens must be >= 0");
        }
        if (totalTokens < 0) {
            addValidationError("totalTokens must be >= 0");
        }
        if (responseTimeMs < 0) {
            addValidationError("responseTimeMs must be >= 0");
        }
        if (cost < 0) {
            addValidationError("cost must be >= 0");
        }
    }

    @Override
    protected void doReset() {
        id = null;
        content = "";
        modelName = "";
        providerType = "";
        timestamp = System.currentTimeMillis();
        promptTokens = 0;
        completionTokens = 0;
        totalTokens = 0;
        cost = 0.0;
        responseTimeMs = 0L;
        metadata = Map.of();
        finishReason = null;
        errorMessage = null;
    }

    @Override
    public ModelResponse build() {
        if (!isValid()) {
            throw new IllegalArgumentException("Invalid ModelResponseBuilder state: " + getValidationErrors());
        }
        String resolvedId = id != null ? id
                : ("model-response-" + System.currentTimeMillis() + "-" + System.nanoTime());
        return new ModelResponse(content, modelName, providerType, timestamp, promptTokens, completionTokens,
                totalTokens, cost, responseTimeMs, metadata, finishReason, errorMessage);
    }
}
