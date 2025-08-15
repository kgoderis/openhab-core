package org.openhab.core.ai.reasoning.engine.api;

import java.time.Instant;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.action.api.ActionContext;

@NonNullByDefault
public class ReasoningStepBuilder {
    String sessionId = "";
    int stepNumber = 0;
    String reasoning = "";
    List<ActionContext> toolCalls = List.of();
    double confidence = 0.0;
    boolean isComplete = false;
    Instant startTime = Instant.now();
    Instant endTime = Instant.now();
    @Nullable
    String error;

    public ReasoningStepBuilder sessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    public ReasoningStepBuilder stepNumber(int stepNumber) {
        this.stepNumber = stepNumber;
        return this;
    }

    public ReasoningStepBuilder reasoning(String reasoning) {
        this.reasoning = reasoning;
        return this;
    }

    public ReasoningStepBuilder toolCalls(List<ActionContext> toolCalls) {
        this.toolCalls = toolCalls;
        return this;
    }

    public ReasoningStepBuilder confidence(double confidence) {
        this.confidence = confidence;
        return this;
    }

    public ReasoningStepBuilder isComplete(boolean isComplete) {
        this.isComplete = isComplete;
        return this;
    }

    public ReasoningStepBuilder startTime(Instant startTime) {
        this.startTime = startTime;
        return this;
    }

    public ReasoningStepBuilder endTime(Instant endTime) {
        this.endTime = endTime;
        return this;
    }

    public ReasoningStepBuilder error(@Nullable String error) {
        this.error = error;
        return this;
    }

    public ReasoningStep build() {
        return new ReasoningStep(this);
    }
}
