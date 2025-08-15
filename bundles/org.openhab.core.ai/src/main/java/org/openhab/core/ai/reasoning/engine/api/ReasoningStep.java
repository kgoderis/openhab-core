package org.openhab.core.ai.reasoning.engine.api;

import java.time.Instant;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.action.api.ActionContext;

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

    ReasoningStep(ReasoningStepBuilder builder) {
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

    public static ReasoningStepBuilder builder() {
        return new ReasoningStepBuilder();
    }
}
