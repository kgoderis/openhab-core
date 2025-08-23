package org.openhab.core.ai.reasoning.api;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.action.api.ActionResult;
import org.openhab.core.ai.common.context.ReasoningContext;
import org.openhab.core.ai.reasoning.engine.api.ReasoningStep;

/**
 * Result of multi-step reasoning process.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class MultiStepReasoningResult {
    private final String reasoningId;
    private final String sessionId;
    private final boolean successful;
    private final boolean completed;
    private final String finalAnswer;
    private final String finalReasoning;
    private final List<ReasoningStep> steps;
    private final List<ActionResult> toolCalls;
    private final Map<String, Object> metadata;
    private final @Nullable String errorMessage;
    private final @Nullable ReasoningContext context;
    private final double confidence;
    private final long totalDurationMs;
    private final Instant startTime;
    private final Instant endTime;

    private MultiStepReasoningResult(Builder builder) {
        this.reasoningId = builder.reasoningId;
        this.sessionId = builder.sessionId;
        this.successful = builder.successful;
        this.completed = builder.completed;
        this.finalAnswer = builder.finalAnswer;
        this.finalReasoning = builder.finalReasoning;
        this.steps = List.copyOf(builder.steps);
        this.toolCalls = List.copyOf(builder.toolCalls);
        this.metadata = Map.copyOf(builder.metadata);
        this.errorMessage = builder.errorMessage;
        this.context = builder.context;
        this.confidence = builder.confidence;
        this.totalDurationMs = builder.totalDurationMs;
        this.startTime = builder.startTime;
        this.endTime = builder.endTime;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public String getReasoningId() {
        return reasoningId;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public String getFinalAnswer() {
        return finalAnswer;
    }

    public List<ReasoningStep> getSteps() {
        return steps;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public @Nullable String getErrorMessage() {
        return errorMessage;
    }

    public long getTotalDurationMs() {
        return totalDurationMs;
    }

    public String getSessionId() {
        return sessionId;
    }

    public boolean isCompleted() {
        return completed;
    }

    public String getFinalReasoning() {
        return finalReasoning;
    }

    public List<ActionResult> getToolCalls() {
        return toolCalls;
    }

    public @Nullable ReasoningContext getContext() {
        return context;
    }

    public double getConfidence() {
        return confidence;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MultiStepReasoningResult that = (MultiStepReasoningResult) o;
        return successful == that.successful && completed == that.completed
                && Double.compare(that.confidence, confidence) == 0 && totalDurationMs == that.totalDurationMs
                && Objects.equals(reasoningId, that.reasoningId) && Objects.equals(sessionId, that.sessionId)
                && Objects.equals(finalAnswer, that.finalAnswer) && Objects.equals(finalReasoning, that.finalReasoning)
                && Objects.equals(steps, that.steps) && Objects.equals(toolCalls, that.toolCalls)
                && Objects.equals(metadata, that.metadata) && Objects.equals(errorMessage, that.errorMessage)
                && Objects.equals(context, that.context) && Objects.equals(startTime, that.startTime)
                && Objects.equals(endTime, that.endTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reasoningId, sessionId, successful, completed, finalAnswer, finalReasoning, steps,
                toolCalls, metadata, errorMessage, context, confidence, totalDurationMs, startTime, endTime);
    }

    @Override
    public String toString() {
        return "MultiStepReasoningResult [reasoningId=" + reasoningId + ", sessionId=" + sessionId + ", successful="
                + successful + ", completed=" + completed + ", finalAnswer=" + finalAnswer + ", finalReasoning="
                + finalReasoning + ", steps=" + steps + ", toolCalls=" + toolCalls + ", metadata=" + metadata
                + ", errorMessage=" + errorMessage + ", context=" + context + ", confidence=" + confidence
                + ", totalDurationMs=" + totalDurationMs + ", startTime=" + startTime + ", endTime=" + endTime + "]";
    }

    /**
     * Builder for MultiStepReasoningResult.
     */
    public static final class Builder {
        private String reasoningId = "";
        private String sessionId = "";
        private boolean successful = false;
        private boolean completed = false;
        private String finalAnswer = "";
        private String finalReasoning = "";
        private List<ReasoningStep> steps = List.of();
        private List<ActionResult> toolCalls = List.of();
        private Map<String, Object> metadata = Map.of();
        private @Nullable String errorMessage;
        private @Nullable ReasoningContext context;
        private double confidence = 1.0;
        private long totalDurationMs = 0L;
        private Instant startTime = Instant.now();
        private Instant endTime = Instant.now();

        public Builder() {
        }

        public Builder(MultiStepReasoningResult source) {
            this.reasoningId = source.reasoningId;
            this.sessionId = source.sessionId;
            this.successful = source.successful;
            this.completed = source.completed;
            this.finalAnswer = source.finalAnswer;
            this.finalReasoning = source.finalReasoning;
            this.steps = source.steps;
            this.toolCalls = source.toolCalls;
            this.metadata = source.metadata;
            this.errorMessage = source.errorMessage;
            this.context = source.context;
            this.confidence = source.confidence;
            this.totalDurationMs = source.totalDurationMs;
            this.startTime = source.startTime;
            this.endTime = source.endTime;
        }

        public Builder withReasoningId(String reasoningId) {
            this.reasoningId = Objects.requireNonNull(reasoningId, "reasoningId");
            return this;
        }

        public Builder withSessionId(String sessionId) {
            this.sessionId = Objects.requireNonNull(sessionId, "sessionId");
            return this;
        }

        public Builder withSuccessful(boolean successful) {
            this.successful = successful;
            return this;
        }

        public Builder withCompleted(boolean completed) {
            this.completed = completed;
            return this;
        }

        public Builder withFinalAnswer(String finalAnswer) {
            this.finalAnswer = Objects.requireNonNull(finalAnswer, "finalAnswer");
            return this;
        }

        public Builder withFinalReasoning(String finalReasoning) {
            this.finalReasoning = Objects.requireNonNull(finalReasoning, "finalReasoning");
            return this;
        }

        public Builder withSteps(List<ReasoningStep> steps) {
            this.steps = Objects.requireNonNull(steps, "steps");
            return this;
        }

        public Builder withToolCalls(List<ActionResult> toolCalls) {
            this.toolCalls = Objects.requireNonNull(toolCalls, "toolCalls");
            return this;
        }

        public Builder withMetadata(Map<String, Object> metadata) {
            this.metadata = Objects.requireNonNull(metadata, "metadata");
            return this;
        }

        public Builder withErrorMessage(@Nullable String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public Builder withContext(@Nullable ReasoningContext context) {
            this.context = context;
            return this;
        }

        public Builder withConfidence(double confidence) {
            this.confidence = Math.max(0.0, Math.min(1.0, confidence));
            return this;
        }

        public Builder withTotalDurationMs(long totalDurationMs) {
            this.totalDurationMs = totalDurationMs;
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

        public MultiStepReasoningResult build() {
            if (reasoningId == null || reasoningId.isBlank()) {
                throw new IllegalArgumentException("reasoningId must not be null or blank");
            }
            if (sessionId == null || sessionId.isBlank()) {
                throw new IllegalArgumentException("sessionId must not be null or blank");
            }
            if (finalAnswer == null || finalAnswer.isBlank()) {
                throw new IllegalArgumentException("finalAnswer must not be null or blank");
            }
            if (finalReasoning == null || finalReasoning.isBlank()) {
                throw new IllegalArgumentException("finalReasoning must not be null or blank");
            }
            if (confidence < 0.0 || confidence > 1.0) {
                throw new IllegalArgumentException("confidence must be between 0.0 and 1.0");
            }
            if (totalDurationMs < 0) {
                throw new IllegalArgumentException("totalDurationMs must be non-negative");
            }
            return new MultiStepReasoningResult(this);
        }
    }
}
