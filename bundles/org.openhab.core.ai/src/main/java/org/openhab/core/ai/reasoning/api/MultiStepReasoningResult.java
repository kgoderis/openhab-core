package org.openhab.core.ai.reasoning.api;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Result of multi-step reasoning process.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class MultiStepReasoningResult {
    private final String reasoningId;
    private final boolean successful;
    private final String finalAnswer;
    private final List<ReasoningStep> steps;
    private final Map<String, Object> metadata;
    private final @Nullable String errorMessage;
    private final long totalDurationMs;

    public MultiStepReasoningResult(String reasoningId, boolean successful, String finalAnswer,
            List<ReasoningStep> steps, Map<String, Object> metadata, @Nullable String errorMessage,
            long totalDurationMs) {
        this.reasoningId = reasoningId;
        this.successful = successful;
        this.finalAnswer = finalAnswer;
        this.steps = steps;
        this.metadata = metadata;
        this.errorMessage = errorMessage;
        this.totalDurationMs = totalDurationMs;
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
}
