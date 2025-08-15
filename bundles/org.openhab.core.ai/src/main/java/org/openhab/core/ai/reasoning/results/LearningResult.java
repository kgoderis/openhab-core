package org.openhab.core.ai.reasoning.results;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class LearningResult {
    private final boolean success;
    private final String message;
    private final double confidence;

    private LearningResult(boolean success, String message, double confidence) {
        this.success = success;
        this.message = message;
        this.confidence = confidence;
    }

    public static LearningResult success(double confidence) {
        return new LearningResult(true, "Learning successful", confidence);
    }

    public static LearningResult disabled(String reason) {
        return new LearningResult(false, "Learning disabled: " + reason, 0.0);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public double getConfidence() {
        return confidence;
    }
}
