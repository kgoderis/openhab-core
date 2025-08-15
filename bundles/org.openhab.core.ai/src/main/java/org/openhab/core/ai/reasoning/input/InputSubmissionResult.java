package org.openhab.core.ai.reasoning.input;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Result for single input submission to the autonomous reasoning manager.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class InputSubmissionResult {
    private final boolean success;
    private final @Nullable String inputId;
    private final double quality;
    private final @Nullable String error;

    private InputSubmissionResult(boolean success, @Nullable String inputId, double quality, @Nullable String error) {
        this.success = success;
        this.inputId = inputId;
        this.quality = quality;
        this.error = error;
    }

    public static InputSubmissionResult success(String inputId, double quality) {
        return new InputSubmissionResult(true, inputId, quality, null);
    }

    public static InputSubmissionResult validationFailed(String reason) {
        return new InputSubmissionResult(false, null, 0.0, reason);
    }

    public static InputSubmissionResult queueFull(String reason) {
        return new InputSubmissionResult(false, null, 0.0, reason);
    }

    public static InputSubmissionResult error(String reason) {
        return new InputSubmissionResult(false, null, 0.0, reason);
    }

    public boolean isSuccess() {
        return success;
    }

    public @Nullable String getInputId() {
        return inputId;
    }

    public double getQuality() {
        return quality;
    }

    public @Nullable String getError() {
        return error;
    }
}
