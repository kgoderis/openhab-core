package org.openhab.core.ai.reasoning.engine.api;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.context.ExecutionContext;

/**
 * A single step in the reasoning process with comprehensive tracking capabilities.
 * 
 * <p>
 * This class provides detailed tracking of individual reasoning steps including:
 * - Step identification and categorization
 * - Input/output context tracking
 * - Resource usage monitoring
 * - Validation and quality assessment
 * - Dependency and relationship management
 * - Iteration and retry tracking
 * - Performance metrics
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ReasoningStep {

    private final String sessionId;
    private final int stepNumber;
    private final String reasoning;
    private final List<ExecutionContext> toolCalls;
    private final double confidence;
    private final boolean isComplete;
    private final Instant startTime;
    private final Instant endTime;
    private final @Nullable String error;

    // Enhanced fields for comprehensive tracking
    private final ReasoningStepType stepType;
    private final ReasoningStepStatus status;
    private final @Nullable String modelId;
    private final @Nullable String modelVersion;
    private final @Nullable Map<String, Object> modelParameters;
    private final @Nullable Map<String, Object> inputContext;
    private final @Nullable Map<String, Object> outputContext;
    private final @Nullable String intermediateResult;
    private final @Nullable List<String> parentStepIds;
    private final @Nullable List<String> childStepIds;
    private final @Nullable String previousStepId;
    private final @Nullable ResourceUsage resourceUsage;
    private final @Nullable ValidationInfo validationInfo;
    private final double qualityScore;
    private final int iterationNumber;
    private final int retryCount;
    private final @Nullable Map<String, Object> contextSnapshot;
    private final @Nullable Map<String, Object> contextChanges;
    private final @Nullable Map<String, Object> stepMetadata;

    /**
     * Create a new ReasoningStep.
     * 
     * @param builder the builder containing all parameters
     */
    private ReasoningStep(Builder builder) {
        this.sessionId = Objects.requireNonNull(builder.sessionId, "sessionId");
        this.stepNumber = builder.stepNumber;
        this.reasoning = Objects.requireNonNull(builder.reasoning, "reasoning");
        this.toolCalls = builder.toolCalls != null ? List.copyOf(builder.toolCalls) : List.of();
        this.confidence = builder.confidence;
        this.isComplete = builder.isComplete;
        this.startTime = Objects.requireNonNull(builder.startTime, "startTime");
        this.endTime = Objects.requireNonNull(builder.endTime, "endTime");
        this.error = builder.error;

        // Enhanced fields
        this.stepType = Objects.requireNonNull(builder.stepType, "stepType");
        this.status = Objects.requireNonNull(builder.status, "status");
        this.modelId = builder.modelId;
        this.modelVersion = builder.modelVersion;
        this.modelParameters = builder.modelParameters != null ? Map.copyOf(builder.modelParameters) : null;
        this.inputContext = builder.inputContext != null ? Map.copyOf(builder.inputContext) : null;
        this.outputContext = builder.outputContext != null ? Map.copyOf(builder.outputContext) : null;
        this.intermediateResult = builder.intermediateResult;
        this.parentStepIds = builder.parentStepIds != null ? List.copyOf(builder.parentStepIds) : null;
        this.childStepIds = builder.childStepIds != null ? List.copyOf(builder.childStepIds) : null;
        this.previousStepId = builder.previousStepId;
        this.resourceUsage = builder.resourceUsage;
        this.validationInfo = builder.validationInfo;
        this.qualityScore = builder.qualityScore;
        this.iterationNumber = builder.iterationNumber;
        this.retryCount = builder.retryCount;
        this.contextSnapshot = builder.contextSnapshot != null ? Map.copyOf(builder.contextSnapshot) : null;
        this.contextChanges = builder.contextChanges != null ? Map.copyOf(builder.contextChanges) : null;
        this.stepMetadata = builder.stepMetadata != null ? Map.copyOf(builder.stepMetadata) : null;
    }

    /**
     * Create a new ReasoningStepBuilder instance.
     * 
     * @return a new ReasoningStepBuilder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Create a builder from this instance for modification.
     * 
     * @return a new builder with current values
     */
    public Builder toBuilder() {
        return new Builder(this);
    }

    public String getSessionId() {
        return sessionId;
    }

    public int getStepNumber() {
        return stepNumber;
    }

    public String getReasoning() {
        return reasoning;
    }

    public List<ExecutionContext> getToolCalls() {
        return toolCalls;
    }

    public double getConfidence() {
        return confidence;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public @Nullable String getError() {
        return error;
    }

    // Enhanced getter methods
    public ReasoningStepType getStepType() {
        return stepType;
    }

    public ReasoningStepStatus getStatus() {
        return status;
    }

    public @Nullable String getModelId() {
        return modelId;
    }

    public @Nullable String getModelVersion() {
        return modelVersion;
    }

    public @Nullable Map<String, Object> getModelParameters() {
        return modelParameters;
    }

    public @Nullable Map<String, Object> getInputContext() {
        return inputContext;
    }

    public @Nullable Map<String, Object> getOutputContext() {
        return outputContext;
    }

    public @Nullable String getIntermediateResult() {
        return intermediateResult;
    }

    public @Nullable List<String> getParentStepIds() {
        return parentStepIds;
    }

    public @Nullable List<String> getChildStepIds() {
        return childStepIds;
    }

    public @Nullable String getPreviousStepId() {
        return previousStepId;
    }

    public @Nullable ResourceUsage getResourceUsage() {
        return resourceUsage;
    }

    public @Nullable ValidationInfo getValidationInfo() {
        return validationInfo;
    }

    public double getQualityScore() {
        return qualityScore;
    }

    public int getIterationNumber() {
        return iterationNumber;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public @Nullable Map<String, Object> getContextSnapshot() {
        return contextSnapshot;
    }

    public @Nullable Map<String, Object> getContextChanges() {
        return contextChanges;
    }

    public @Nullable Map<String, Object> getStepMetadata() {
        return stepMetadata;
    }

    /**
     * Get the duration of this reasoning step.
     * 
     * @return the duration between start and end time
     */
    public Duration getDuration() {
        return Duration.between(startTime, endTime);
    }

    /**
     * Get the duration in milliseconds.
     * 
     * @return the duration in milliseconds
     */
    public long getDurationMs() {
        return getDuration().toMillis();
    }

    /**
     * Check if this step has been validated.
     * 
     * @return true if the step has validation info
     */
    public boolean hasValidationInfo() {
        return validationInfo != null;
    }

    /**
     * Check if this step has resource usage information.
     * 
     * @return true if the step has resource usage info
     */
    public boolean hasResourceUsage() {
        return resourceUsage != null;
    }

    /**
     * Check if this step has dependencies.
     * 
     * @return true if the step has parent step IDs
     */
    public boolean hasDependencies() {
        return parentStepIds != null && !parentStepIds.isEmpty();
    }

    /**
     * Check if this step has child steps.
     * 
     * @return true if the step has child step IDs
     */
    public boolean hasChildren() {
        return childStepIds != null && !childStepIds.isEmpty();
    }

    /**
     * Builder for creating ReasoningStep objects.
     * 
     * <p>
     * Provides a fluent API with validation for constructing ReasoningStep
     * instances.
     * </p>
     * 
     * @author Karel Goderis - Initial Contribution
     * @since 1.0.0
     */
    public static final class Builder {
        private String sessionId = "";
        private int stepNumber = 1;
        private String reasoning = "";
        private @Nullable List<ExecutionContext> toolCalls;
        private double confidence = 1.0;
        private boolean isComplete = true;
        private Instant startTime = Instant.now();
        private Instant endTime = Instant.now();
        private @Nullable String error;

        // Enhanced builder fields
        private ReasoningStepType stepType = ReasoningStepType.UNKNOWN;
        private ReasoningStepStatus status = ReasoningStepStatus.PENDING;
        private @Nullable String modelId;
        private @Nullable String modelVersion;
        private @Nullable Map<String, Object> modelParameters;
        private @Nullable Map<String, Object> inputContext;
        private @Nullable Map<String, Object> outputContext;
        private @Nullable String intermediateResult;
        private @Nullable List<String> parentStepIds;
        private @Nullable List<String> childStepIds;
        private @Nullable String previousStepId;
        private @Nullable ResourceUsage resourceUsage;
        private @Nullable ValidationInfo validationInfo;
        private double qualityScore = 1.0;
        private int iterationNumber = 1;
        private int retryCount = 0;
        private @Nullable Map<String, Object> contextSnapshot;
        private @Nullable Map<String, Object> contextChanges;
        private @Nullable Map<String, Object> stepMetadata;

        public Builder() {
            this.sessionId = "step-" + System.currentTimeMillis();
        }

        public Builder(ReasoningStep source) {
            this.sessionId = source.sessionId;
            this.stepNumber = source.stepNumber;
            this.reasoning = source.reasoning;
            this.toolCalls = source.toolCalls != null ? List.copyOf(source.toolCalls) : null;
            this.confidence = source.confidence;
            this.isComplete = source.isComplete;
            this.startTime = source.startTime;
            this.endTime = source.endTime;
            this.error = source.error;

            // Enhanced fields
            this.stepType = source.stepType;
            this.status = source.status;
            this.modelId = source.modelId;
            this.modelVersion = source.modelVersion;
            this.modelParameters = source.modelParameters != null ? Map.copyOf(source.modelParameters) : null;
            this.inputContext = source.inputContext != null ? Map.copyOf(source.inputContext) : null;
            this.outputContext = source.outputContext != null ? Map.copyOf(source.outputContext) : null;
            this.intermediateResult = source.intermediateResult;
            this.parentStepIds = source.parentStepIds != null ? List.copyOf(source.parentStepIds) : null;
            this.childStepIds = source.childStepIds != null ? List.copyOf(source.childStepIds) : null;
            this.previousStepId = source.previousStepId;
            this.resourceUsage = source.resourceUsage;
            this.validationInfo = source.validationInfo;
            this.qualityScore = source.qualityScore;
            this.iterationNumber = source.iterationNumber;
            this.retryCount = source.retryCount;
            this.contextSnapshot = source.contextSnapshot != null ? Map.copyOf(source.contextSnapshot) : null;
            this.contextChanges = source.contextChanges != null ? Map.copyOf(source.contextChanges) : null;
            this.stepMetadata = source.stepMetadata != null ? Map.copyOf(source.stepMetadata) : null;
        }

        public Builder withSessionId(String sessionId) {
            this.sessionId = Objects.requireNonNull(sessionId, "sessionId");
            return this;
        }

        public Builder withStepNumber(int stepNumber) {
            this.stepNumber = stepNumber;
            return this;
        }

        public Builder withReasoning(String reasoning) {
            this.reasoning = Objects.requireNonNull(reasoning, "reasoning");
            return this;
        }

        public Builder withToolCalls(@Nullable List<ExecutionContext> toolCalls) {
            this.toolCalls = toolCalls;
            return this;
        }

        public Builder withConfidence(double confidence) {
            this.confidence = confidence;
            return this;
        }

        public Builder withComplete(boolean isComplete) {
            this.isComplete = isComplete;
            return this;
        }

        public Builder withStartTime(Instant startTime) {
            this.startTime = Objects.requireNonNull(startTime, "startTime");
            return this;
        }

        public Builder withEndTime(Instant endTime) {
            this.endTime = Objects.requireNonNull(endTime, "endTime");
            return this;
        }

        public Builder withError(@Nullable String error) {
            this.error = error;
            return this;
        }

        // Enhanced builder methods
        public Builder withStepType(ReasoningStepType stepType) {
            this.stepType = Objects.requireNonNull(stepType, "stepType");
            return this;
        }

        public Builder withStatus(ReasoningStepStatus status) {
            this.status = Objects.requireNonNull(status, "status");
            return this;
        }

        public Builder withModelId(@Nullable String modelId) {
            this.modelId = modelId;
            return this;
        }

        public Builder withModelVersion(@Nullable String modelVersion) {
            this.modelVersion = modelVersion;
            return this;
        }

        public Builder withModelParameters(@Nullable Map<String, Object> modelParameters) {
            this.modelParameters = modelParameters;
            return this;
        }

        public Builder withInputContext(@Nullable Map<String, Object> inputContext) {
            this.inputContext = inputContext;
            return this;
        }

        public Builder withOutputContext(@Nullable Map<String, Object> outputContext) {
            this.outputContext = outputContext;
            return this;
        }

        public Builder withIntermediateResult(@Nullable String intermediateResult) {
            this.intermediateResult = intermediateResult;
            return this;
        }

        public Builder withParentStepIds(@Nullable List<String> parentStepIds) {
            this.parentStepIds = parentStepIds;
            return this;
        }

        public Builder withChildStepIds(@Nullable List<String> childStepIds) {
            this.childStepIds = childStepIds;
            return this;
        }

        public Builder withPreviousStepId(@Nullable String previousStepId) {
            this.previousStepId = previousStepId;
            return this;
        }

        public Builder withResourceUsage(@Nullable ResourceUsage resourceUsage) {
            this.resourceUsage = resourceUsage;
            return this;
        }

        public Builder withValidationInfo(@Nullable ValidationInfo validationInfo) {
            this.validationInfo = validationInfo;
            return this;
        }

        public Builder withQualityScore(double qualityScore) {
            this.qualityScore = qualityScore;
            return this;
        }

        public Builder withIterationNumber(int iterationNumber) {
            this.iterationNumber = iterationNumber;
            return this;
        }

        public Builder withRetryCount(int retryCount) {
            this.retryCount = retryCount;
            return this;
        }

        public Builder withContextSnapshot(@Nullable Map<String, Object> contextSnapshot) {
            this.contextSnapshot = contextSnapshot;
            return this;
        }

        public Builder withContextChanges(@Nullable Map<String, Object> contextChanges) {
            this.contextChanges = contextChanges;
            return this;
        }

        public Builder withStepMetadata(@Nullable Map<String, Object> stepMetadata) {
            this.stepMetadata = stepMetadata;
            return this;
        }

        public ReasoningStep build() {
            if (sessionId.isBlank()) {
                throw new IllegalArgumentException("sessionId must not be blank");
            }
            if (reasoning.isBlank()) {
                throw new IllegalArgumentException("reasoning must not be blank");
            }
            if (confidence < 0.0 || confidence > 1.0) {
                throw new IllegalArgumentException("confidence must be between 0.0 and 1.0");
            }
            if (stepNumber < 1) {
                throw new IllegalArgumentException("stepNumber must be positive");
            }
            if (qualityScore < 0.0 || qualityScore > 1.0) {
                throw new IllegalArgumentException("qualityScore must be between 0.0 and 1.0");
            }
            if (iterationNumber < 1) {
                throw new IllegalArgumentException("iterationNumber must be positive");
            }
            if (retryCount < 0) {
                throw new IllegalArgumentException("retryCount must be non-negative");
            }
            return new ReasoningStep(this);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ReasoningStep other = (ReasoningStep) obj;
        return Objects.equals(sessionId, other.sessionId) && stepNumber == other.stepNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId, stepNumber);
    }

    @Override
    public String toString() {
        return String.format(
                "ReasoningStep{sessionId='%s', stepNumber=%d, stepType=%s, status=%s, reasoning='%s', confidence=%.2f, duration=%dms}",
                sessionId, stepNumber, stepType, status,
                reasoning.length() > 50 ? reasoning.substring(0, 47) + "..." : reasoning, confidence, getDurationMs());
    }
}
