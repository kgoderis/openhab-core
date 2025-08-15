package org.openhab.core.ai.events;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Result for correlation validation containing confidence or error.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class CorrelationValidationResult {

    private final boolean valid;
    private final double confidence;
    private final @Nullable String error;

    private CorrelationValidationResult(boolean valid, double confidence, @Nullable String error) {
        this.valid = valid;
        this.confidence = confidence;
        this.error = error;
    }

    public static CorrelationValidationResult valid(double confidence) {
        return new CorrelationValidationResult(true, confidence, null);
    }

    public static CorrelationValidationResult notFound(String reason) {
        return new CorrelationValidationResult(false, 0.0, reason);
    }

    public static CorrelationValidationResult error(String reason) {
        return new CorrelationValidationResult(false, 0.0, reason);
    }

    public boolean isValid() {
        return valid;
    }

    public double getConfidence() {
        return confidence;
    }

    public @Nullable String getError() {
        return error;
    }
}
