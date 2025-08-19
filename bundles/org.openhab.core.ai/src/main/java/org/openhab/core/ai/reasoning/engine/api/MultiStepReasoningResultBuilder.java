package org.openhab.core.ai.reasoning.engine.api;

import java.time.Instant;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.action.api.ActionResult;
import org.openhab.core.ai.common.context.ReasoningContext;

@NonNullByDefault
public class MultiStepReasoningResultBuilder {
    String sessionId = "";
    List<ReasoningStep> steps = List.of();
    List<ActionResult> toolCalls = List.of();
    String finalReasoning = "";
    double confidence = 0.0;
    boolean completed = false;
    @Nullable
    ReasoningContext context;
    Instant startTime = Instant.now();
    Instant endTime = Instant.now();
    @Nullable
    String error;

    public MultiStepReasoningResultBuilder sessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    public MultiStepReasoningResultBuilder steps(List<ReasoningStep> steps) {
        this.steps = steps;
        return this;
    }

    public MultiStepReasoningResultBuilder toolCalls(List<ActionResult> toolCalls) {
        this.toolCalls = toolCalls;
        return this;
    }

    public MultiStepReasoningResultBuilder finalReasoning(String finalReasoning) {
        this.finalReasoning = finalReasoning;
        return this;
    }

    public MultiStepReasoningResultBuilder confidence(double confidence) {
        this.confidence = confidence;
        return this;
    }

    public MultiStepReasoningResultBuilder completed(boolean completed) {
        this.completed = completed;
        return this;
    }

    public MultiStepReasoningResultBuilder context(ReasoningContext context) {
        this.context = context;
        return this;
    }

    public MultiStepReasoningResultBuilder startTime(Instant startTime) {
        this.startTime = startTime;
        return this;
    }

    public MultiStepReasoningResultBuilder endTime(Instant endTime) {
        this.endTime = endTime;
        return this;
    }

    public MultiStepReasoningResultBuilder error(@Nullable String error) {
        this.error = error;
        return this;
    }

    public MultiStepReasoningResult build() {
        return new MultiStepReasoningResult(this);
    }
}
