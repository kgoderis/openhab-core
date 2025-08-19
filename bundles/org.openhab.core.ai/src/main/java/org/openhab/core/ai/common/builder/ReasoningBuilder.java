package org.openhab.core.ai.common.builder;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Unified builder for reasoning-related objects in the openHAB AI system.
 *
 * <p>
 * This class provides a common builder pattern for creating reasoning-related objects
 * such as ReasoningStep, MultiStepReasoningResult, etc.
 * </p>
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public abstract class ReasoningBuilder<T> extends AbstractBuilder<T> {

    protected String sessionId = "";
    protected int stepNumber = 0;
    protected String reasoning = "";
    protected List<Object> toolCalls = List.of();
    protected double confidence = 0.0;
    protected boolean isComplete = false;
    protected Instant startTime = Instant.now();
    protected Instant endTime = Instant.now();
    protected @Nullable String error;
    protected String stepId = "";
    protected Map<String, Object> stepData = Map.of();
    protected @Nullable Object stepType;
    protected List<Object> subSteps = List.of();
    protected Map<String, Object> stepMetadata = Map.of();
    protected String prompt = "";
    protected Map<String, Object> promptContext = Map.of();
    protected @Nullable Object promptType;
    protected List<String> promptVariables = List.of();
    protected Map<String, Object> promptOptions = Map.of();

    /**
     * Set the session ID.
     *
     * @param sessionId the session ID
     * @return this builder
     */
    public ReasoningBuilder<T> withSessionId(String sessionId) {
        this.sessionId = Objects.requireNonNull(sessionId, "sessionId");
        return this;
    }

    /**
     * Set the step number.
     *
     * @param stepNumber the step number
     * @return this builder
     */
    public ReasoningBuilder<T> withStepNumber(int stepNumber) {
        this.stepNumber = stepNumber;
        return this;
    }

    /**
     * Set the reasoning.
     *
     * @param reasoning the reasoning
     * @return this builder
     */
    public ReasoningBuilder<T> withReasoning(String reasoning) {
        this.reasoning = Objects.requireNonNull(reasoning, "reasoning");
        return this;
    }

    /**
     * Set the tool calls.
     *
     * @param toolCalls the tool calls
     * @return this builder
     */
    public ReasoningBuilder<T> withToolCalls(List<Object> toolCalls) {
        this.toolCalls = Objects.requireNonNull(toolCalls, "toolCalls");
        return this;
    }

    /**
     * Set the confidence.
     *
     * @param confidence the confidence
     * @return this builder
     */
    public ReasoningBuilder<T> withConfidence(double confidence) {
        this.confidence = confidence;
        return this;
    }

    /**
     * Set whether the step is complete.
     *
     * @param isComplete true if complete, false otherwise
     * @return this builder
     */
    public ReasoningBuilder<T> withIsComplete(boolean isComplete) {
        this.isComplete = isComplete;
        return this;
    }

    /**
     * Set the start time.
     *
     * @param startTime the start time
     * @return this builder
     */
    public ReasoningBuilder<T> withStartTime(Instant startTime) {
        this.startTime = Objects.requireNonNull(startTime, "startTime");
        return this;
    }

    /**
     * Set the end time.
     *
     * @param endTime the end time
     * @return this builder
     */
    public ReasoningBuilder<T> withEndTime(Instant endTime) {
        this.endTime = Objects.requireNonNull(endTime, "endTime");
        return this;
    }

    /**
     * Set the error.
     *
     * @param error the error
     * @return this builder
     */
    public ReasoningBuilder<T> withError(@Nullable String error) {
        this.error = error;
        return this;
    }

    /**
     * Set the step ID.
     *
     * @param stepId the step ID
     * @return this builder
     */
    public ReasoningBuilder<T> withStepId(String stepId) {
        this.stepId = Objects.requireNonNull(stepId, "stepId");
        return this;
    }

    /**
     * Set the step data.
     *
     * @param stepData the step data
     * @return this builder
     */
    public ReasoningBuilder<T> withStepData(Map<String, Object> stepData) {
        this.stepData = Objects.requireNonNull(stepData, "stepData");
        return this;
    }

    /**
     * Set the step type.
     *
     * @param stepType the step type
     * @return this builder
     */
    public ReasoningBuilder<T> withStepType(@Nullable Object stepType) {
        this.stepType = stepType;
        return this;
    }

    /**
     * Set the sub steps.
     *
     * @param subSteps the sub steps
     * @return this builder
     */
    public ReasoningBuilder<T> withSubSteps(List<Object> subSteps) {
        this.subSteps = Objects.requireNonNull(subSteps, "subSteps");
        return this;
    }

    /**
     * Set the step metadata.
     *
     * @param stepMetadata the step metadata
     * @return this builder
     */
    public ReasoningBuilder<T> withStepMetadata(Map<String, Object> stepMetadata) {
        this.stepMetadata = Objects.requireNonNull(stepMetadata, "stepMetadata");
        return this;
    }

    /**
     * Set the prompt.
     *
     * @param prompt the prompt
     * @return this builder
     */
    public ReasoningBuilder<T> withPrompt(String prompt) {
        this.prompt = Objects.requireNonNull(prompt, "prompt");
        return this;
    }

    /**
     * Set the prompt context.
     *
     * @param promptContext the prompt context
     * @return this builder
     */
    public ReasoningBuilder<T> withPromptContext(Map<String, Object> promptContext) {
        this.promptContext = Objects.requireNonNull(promptContext, "promptContext");
        return this;
    }

    /**
     * Set the prompt type.
     *
     * @param promptType the prompt type
     * @return this builder
     */
    public ReasoningBuilder<T> withPromptType(@Nullable Object promptType) {
        this.promptType = promptType;
        return this;
    }

    /**
     * Set the prompt variables.
     *
     * @param promptVariables the prompt variables
     * @return this builder
     */
    public ReasoningBuilder<T> withPromptVariables(List<String> promptVariables) {
        this.promptVariables = Objects.requireNonNull(promptVariables, "promptVariables");
        return this;
    }

    /**
     * Set the prompt options.
     *
     * @param promptOptions the prompt options
     * @return this builder
     */
    public ReasoningBuilder<T> withPromptOptions(Map<String, Object> promptOptions) {
        this.promptOptions = Objects.requireNonNull(promptOptions, "promptOptions");
        return this;
    }

    @Override
    protected void validate() {
        super.validate();
        // Additional validation specific to ReasoningBuilder
        if (sessionId.isBlank()) {
            throw new IllegalArgumentException("sessionId must not be blank");
        }
        if (stepNumber < 0) {
            throw new IllegalArgumentException("stepNumber must be >= 0");
        }
        if (confidence < 0.0 || confidence > 1.0) {
            throw new IllegalArgumentException("confidence must be between 0.0 and 1.0");
        }
        if (endTime.isBefore(startTime)) {
            throw new IllegalArgumentException("endTime must be after startTime");
        }
    }

    @Override
    protected void doReset() {
        super.doReset();
        sessionId = "";
        stepNumber = 0;
        reasoning = "";
        toolCalls = List.of();
        confidence = 0.0;
        isComplete = false;
        startTime = Instant.now();
        endTime = Instant.now();
        error = null;
        stepId = "";
        stepData = Map.of();
        stepType = null;
        subSteps = List.of();
        stepMetadata = Map.of();
        prompt = "";
        promptContext = Map.of();
        promptType = null;
        promptVariables = List.of();
        promptOptions = Map.of();
    }
}
