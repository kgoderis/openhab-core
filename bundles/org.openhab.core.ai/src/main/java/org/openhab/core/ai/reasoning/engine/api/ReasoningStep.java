package org.openhab.core.ai.reasoning.engine.api;

import java.time.Instant;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.builder.ReasoningStepBuilder;
import org.openhab.core.ai.common.context.ExecutionContext;

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
    private final List<ExecutionContext> toolCalls;
    private final double confidence;
    private final boolean isComplete;
    private final Instant startTime;
    private final Instant endTime;
    private final @Nullable String error;

    public ReasoningStep(ReasoningStepBuilder builder) {
        this.sessionId = builder.getStepId();
        this.stepNumber = 1; // Default value since unified builder doesn't have stepNumber
        this.reasoning = builder.getDescription();
        this.toolCalls = List.of(); // Default empty list since unified builder has different structure
        this.confidence = 1.0; // Default value since unified builder doesn't have confidence
        this.isComplete = builder.isSuccess();
        this.startTime = Instant.now(); // Default value since unified builder doesn't have startTime
        this.endTime = Instant.now(); // Default value since unified builder doesn't have endTime
        this.error = builder.getErrorMessage();
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

    public static ReasoningStepBuilder builder() {
        return new ReasoningStepBuilder("step-" + System.currentTimeMillis());
    }
}
