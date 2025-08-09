package org.openhab.core.ai.reasoning.api;

import java.time.Instant;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.action.ActionContext;

/**
 * A single step in the reasoning process
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public class ReasoningStep {

    private final String sessionId;
    private final int stepNumber;
    private final String reasoning;
    private final List<ActionContext> toolCalls;
    private final double confidence;
    private final boolean isComplete;
    private final Instant startTime;
    private final Instant endTime;
    private final @Nullable String error;

    private ReasoningStep(Builder builder) {
        this.sessionId = builder.sessionId;
        this.stepNumber = builder.stepNumber;
        this.reasoning = builder.reasoning;
        this.toolCalls = builder.toolCalls;
        this.confidence = builder.confidence;
        this.isComplete = builder.isComplete;
        this.startTime = builder.startTime;
        this.endTime = builder.endTime;
        this.error = builder.error;
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

    public List<ActionContext> getToolCalls() {
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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String sessionId = "";
        private int stepNumber = 0;
        private String reasoning = "";
        private List<ActionContext> toolCalls = List.of();
        private double confidence = 0.0;
        private boolean isComplete = false;
        private Instant startTime = Instant.now();
        private Instant endTime = Instant.now();
        private @Nullable String error;

        public Builder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public Builder stepNumber(int stepNumber) {
            this.stepNumber = stepNumber;
            return this;
        }

        public Builder reasoning(String reasoning) {
            this.reasoning = reasoning;
            return this;
        }

        public Builder toolCalls(List<ActionContext> toolCalls) {
            this.toolCalls = toolCalls;
            return this;
        }

        public Builder confidence(double confidence) {
            this.confidence = confidence;
            return this;
        }

        public Builder isComplete(boolean isComplete) {
            this.isComplete = isComplete;
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

        public ReasoningStep build() {
            return new ReasoningStep(this);
        }
    }
}
