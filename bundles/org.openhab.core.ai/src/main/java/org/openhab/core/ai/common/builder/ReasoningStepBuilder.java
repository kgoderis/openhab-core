package org.openhab.core.ai.common.builder;

import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.reasoning.engine.api.ReasoningStep;

/**
 * Builder for ReasoningStep.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class ReasoningStepBuilder extends ReasoningBuilder<ReasoningStep> {

    private String stepId = "";
    private String description = "";
    private @Nullable String reasoningType;
    private @Nullable String modelId;
    private @Nullable String prompt;
    private @Nullable String response;
    private @Nullable List<Object> actionContextToolCalls;
    private @Nullable String errorMessage;
    private long durationMs = 0;
    private boolean success = true;

    /**
     * Create a new ReasoningStepBuilder with the given step ID.
     * 
     * @param stepId the step ID
     */
    public ReasoningStepBuilder(String stepId) {
        super();
        this.stepId = Objects.requireNonNull(stepId, "Step ID cannot be null");
    }

    /**
     * Set the step ID.
     * 
     * @param stepId the step ID
     * @return this builder
     */
    public ReasoningStepBuilder withStepId(String stepId) {
        this.stepId = Objects.requireNonNull(stepId, "Step ID cannot be null");
        return this;
    }

    /**
     * Set the description.
     * 
     * @param description the description
     * @return this builder
     */
    public ReasoningStepBuilder withDescription(String description) {
        this.description = Objects.requireNonNull(description, "Description cannot be null");
        return this;
    }

    /**
     * Set the reasoning type.
     * 
     * @param reasoningType the reasoning type
     * @return this builder
     */
    public ReasoningStepBuilder withReasoningType(@Nullable String reasoningType) {
        this.reasoningType = reasoningType;
        return this;
    }

    /**
     * Set the model ID.
     * 
     * @param modelId the model ID
     * @return this builder
     */
    public ReasoningStepBuilder withModelId(@Nullable String modelId) {
        this.modelId = modelId;
        return this;
    }

    /**
     * Set the prompt.
     * 
     * @param prompt the prompt
     * @return this builder
     */
    public ReasoningStepBuilder withPrompt(@Nullable String prompt) {
        this.prompt = prompt;
        return this;
    }

    /**
     * Set the response.
     * 
     * @param response the response
     * @return this builder
     */
    public ReasoningStepBuilder withResponse(@Nullable String response) {
        this.response = response;
        return this;
    }

    /**
     * Set the action context tool calls.
     * 
     * @param actionContextToolCalls the action context tool calls
     * @return this builder
     */
    public ReasoningStepBuilder withActionContextToolCalls(@Nullable List<Object> actionContextToolCalls) {
        this.actionContextToolCalls = actionContextToolCalls;
        return this;
    }

    /**
     * Set the error message.
     * 
     * @param errorMessage the error message
     * @return this builder
     */
    public ReasoningStepBuilder withErrorMessage(@Nullable String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    /**
     * Set the duration in milliseconds.
     * 
     * @param durationMs the duration in milliseconds
     * @return this builder
     */
    public ReasoningStepBuilder withDurationMs(long durationMs) {
        this.durationMs = durationMs;
        return this;
    }

    /**
     * Set the success flag.
     * 
     * @param success the success flag
     * @return this builder
     */
    public ReasoningStepBuilder withSuccess(boolean success) {
        this.success = success;
        return this;
    }

    @Override
    public ReasoningStep build() {
        validate();
        return new ReasoningStep(this);
    }

    @Override
    protected void validate() {
        if (stepId.isBlank()) {
            throw new IllegalArgumentException("Step ID cannot be blank");
        }
        if (description.isBlank()) {
            throw new IllegalArgumentException("Description cannot be blank");
        }
        if (durationMs < 0) {
            throw new IllegalArgumentException("Duration must be non-negative");
        }
    }

    @Override
    protected void doReset() {
        stepId = "";
        description = "";
        reasoningType = null;
        modelId = null;
        prompt = null;
        response = null;
        actionContextToolCalls = null;
        errorMessage = null;
        durationMs = 0;
        success = true;
    }

    // Getter methods for ReasoningStep constructor
    public String getStepId() {
        return stepId;
    }

    public String getDescription() {
        return description;
    }

    public @Nullable String getReasoningType() {
        return reasoningType;
    }

    public @Nullable String getModelId() {
        return modelId;
    }

    public @Nullable String getPrompt() {
        return prompt;
    }

    public @Nullable String getResponse() {
        return response;
    }

    public @Nullable List<Object> getActionContextToolCalls() {
        return actionContextToolCalls;
    }

    public @Nullable String getErrorMessage() {
        return errorMessage;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public boolean isSuccess() {
        return success;
    }
}
