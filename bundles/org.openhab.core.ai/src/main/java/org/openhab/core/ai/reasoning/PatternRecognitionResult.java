package org.openhab.core.ai.reasoning;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class PatternRecognitionResult {
    private final boolean success;
    private final String message;
    private final boolean patternDetected;
    private final double confidence;

    private PatternRecognitionResult(boolean success, String message, boolean patternDetected, double confidence) {
        this.success = success;
        this.message = message;
        this.patternDetected = patternDetected;
        this.confidence = confidence;
    }

    public static PatternRecognitionResult success(boolean patternDetected, double confidence) {
        return new PatternRecognitionResult(true, "Pattern recognition successful", patternDetected, confidence);
    }

    public static PatternRecognitionResult disabled(String reason) {
        return new PatternRecognitionResult(false, "Pattern recognition disabled: " + reason, false, 0.0);
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public boolean isPatternDetected() { return patternDetected; }
    public double getConfidence() { return confidence; }
}


