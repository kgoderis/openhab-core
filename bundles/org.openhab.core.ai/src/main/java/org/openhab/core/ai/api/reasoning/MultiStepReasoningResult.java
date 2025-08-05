package org.openhab.core.ai.api.reasoning;

import java.time.Instant;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.action.ActionResult;

/**
 * Result of a multi-step reasoning session
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public class MultiStepReasoningResult {

    private final String sessionId;
    private final List<ReasoningStep> steps;
    private final List<ActionResult> toolCalls;
    private final String finalReasoning;
    private final double confidence;
    private final boolean completed;
    private final @Nullable ReasoningContext context;
    private final Instant startTime;
    private final Instant endTime;
    private final @Nullable String error;

    private MultiStepReasoningResult(Builder builder) {
        this.sessionId = builder.sessionId;
        this.steps = builder.steps;
        this.toolCalls = builder.toolCalls;
        this.finalReasoning = builder.finalReasoning;
        this.confidence = builder.confidence;
        this.completed = builder.completed;
        this.context = builder.context;
        this.startTime = builder.startTime;
        this.endTime = builder.endTime;
        this.error = builder.error;
    }

    public String getSessionId() {
        return sessionId;
    }

    public List<ReasoningStep> getSteps() {
        return steps;
    }

    public List<ActionResult> getToolCalls() {
        return toolCalls;
    }

    public String getFinalReasoning() {
        return finalReasoning;
    }

    public double getConfidence() {
        return confidence;
    }

    public boolean isCompleted() {
        return completed;
    }

    public @Nullable ReasoningContext getContext() {
        return context;
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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String sessionId = "";
        private List<ReasoningStep> steps = List.of();
        private List<ActionResult> toolCalls = List.of();
        private String finalReasoning = "";
        private double confidence = 0.0;
        private boolean completed = false;
        private @Nullable ReasoningContext context;
        private Instant startTime = Instant.now();
        private Instant endTime = Instant.now();
        private @Nullable String error;

        public Builder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public Builder steps(List<ReasoningStep> steps) {
            this.steps = steps;
            return this;
        }

        public Builder toolCalls(List<ActionResult> toolCalls) {
            this.toolCalls = toolCalls;
            return this;
        }

        public Builder finalReasoning(String finalReasoning) {
            this.finalReasoning = finalReasoning;
            return this;
        }

        public Builder confidence(double confidence) {
            this.confidence = confidence;
            return this;
        }

        public Builder completed(boolean completed) {
            this.completed = completed;
            return this;
        }

        public Builder context(ReasoningContext context) {
            this.context = context;
            return this;
        }

        public Builder startTime(Instant startTime) {
            this.startTime = startTime;
            return this;
        }

        public Builder endTime(Instant endTime) {
            this.endTime = endTime;
            return this;
        }

        public Builder error(@Nullable String error) {
            this.error = error;
            return this;
        }

        public MultiStepReasoningResult build() {
            return new MultiStepReasoningResult(this);
        }
    }
}
