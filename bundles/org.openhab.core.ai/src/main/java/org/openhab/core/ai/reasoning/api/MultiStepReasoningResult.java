package org.openhab.core.ai.reasoning.api;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.action.api.ActionResult;
import org.openhab.core.ai.common.context.ReasoningContext;

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

    public MultiStepReasoningResult(String reasoningId, String sessionId, boolean successful, boolean completed,
            String finalAnswer, String finalReasoning, List<ReasoningStep> steps, List<ActionResult> toolCalls,
            Map<String, Object> metadata, @Nullable String errorMessage, @Nullable ReasoningContext context,
            double confidence, long totalDurationMs, Instant startTime, Instant endTime) {
        this.reasoningId = reasoningId;
        this.sessionId = sessionId;
        this.successful = successful;
        this.completed = completed;
        this.finalAnswer = finalAnswer;
        this.finalReasoning = finalReasoning;
        this.steps = steps;
        this.toolCalls = toolCalls;
        this.metadata = metadata;
        this.errorMessage = errorMessage;
        this.context = context;
        this.confidence = confidence;
        this.totalDurationMs = totalDurationMs;
        this.startTime = startTime;
        this.endTime = endTime;
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

    /**
     * Backward compatibility constructor.
     */
    public MultiStepReasoningResult(String reasoningId, boolean successful, String finalAnswer,
            List<ReasoningStep> steps, Map<String, Object> metadata, @Nullable String errorMessage,
            long totalDurationMs) {
        this(reasoningId, reasoningId, successful, successful, finalAnswer, finalAnswer, steps, List.of(), metadata,
                errorMessage, null, 1.0, totalDurationMs, Instant.now(), Instant.now());
    }

    public static MultiStepReasoningResultBuilder builder() {
        return new MultiStepReasoningResultBuilder();
    }

    /**
     * Represents a single step in the reasoning process.
     */
    public static class ReasoningStep {
        private final int stepNumber;
        private final String description;
        private final String action;
        private final String result;
        private final long durationMs;

        public ReasoningStep(int stepNumber, String description, String action, String result, long durationMs) {
            this.stepNumber = stepNumber;
            this.description = description;
            this.action = action;
            this.result = result;
            this.durationMs = durationMs;
        }

        public int getStepNumber() {
            return stepNumber;
        }

        public String getDescription() {
            return description;
        }

        public String getAction() {
            return action;
        }

        public String getResult() {
            return result;
        }

        public long getDurationMs() {
            return durationMs;
        }
    }

    /**
     * Builder for MultiStepReasoningResult.
     */
    public static class MultiStepReasoningResultBuilder {
        private String reasoningId;
        private String sessionId;
        private boolean successful;
        private boolean completed;
        private String finalAnswer = "";
        private String finalReasoning = "";
        private List<ReasoningStep> steps = List.of();
        private List<ActionResult> toolCalls = List.of();
        private Map<String, Object> metadata = Map.of();
        private @Nullable String errorMessage;
        private @Nullable ReasoningContext context;
        private double confidence = 1.0;
        private long totalDurationMs;
        private Instant startTime = Instant.now();
        private Instant endTime = Instant.now();

        public MultiStepReasoningResultBuilder reasoningId(String reasoningId) {
            this.reasoningId = reasoningId;
            return this;
        }

        public MultiStepReasoningResultBuilder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public MultiStepReasoningResultBuilder successful(boolean successful) {
            this.successful = successful;
            return this;
        }

        public MultiStepReasoningResultBuilder completed(boolean completed) {
            this.completed = completed;
            return this;
        }

        public MultiStepReasoningResultBuilder finalAnswer(String finalAnswer) {
            this.finalAnswer = finalAnswer;
            return this;
        }

        public MultiStepReasoningResultBuilder finalReasoning(String finalReasoning) {
            this.finalReasoning = finalReasoning;
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

        public MultiStepReasoningResultBuilder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public MultiStepReasoningResultBuilder errorMessage(@Nullable String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public MultiStepReasoningResultBuilder error(String errorMessage) {
            this.errorMessage = errorMessage;
            this.successful = false;
            return this;
        }

        public MultiStepReasoningResultBuilder context(@Nullable ReasoningContext context) {
            this.context = context;
            return this;
        }

        public MultiStepReasoningResultBuilder confidence(double confidence) {
            this.confidence = confidence;
            return this;
        }

        public MultiStepReasoningResultBuilder totalDurationMs(long totalDurationMs) {
            this.totalDurationMs = totalDurationMs;
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

        public MultiStepReasoningResult build() {
            return new MultiStepReasoningResult(reasoningId, sessionId, successful, completed, finalAnswer,
                    finalReasoning, steps, toolCalls, metadata, errorMessage, context, confidence, totalDurationMs,
                    startTime, endTime);
        }
    }
}
