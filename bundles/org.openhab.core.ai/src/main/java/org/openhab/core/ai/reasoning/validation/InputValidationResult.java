package org.openhab.core.ai.reasoning.validation;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Validation result for reasoning input.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class InputValidationResult {
    private final boolean valid;
    private final @Nullable String reason;

    private InputValidationResult(boolean valid, @Nullable String reason) {
        this.valid = valid;
        this.reason = reason;
    }

    public static InputValidationResult valid() {
        return new InputValidationResult(true, null);
    }

    public static InputValidationResult invalid(String reason) {
        return new InputValidationResult(false, reason);
    }

    public boolean isValid() {
        return valid;
    }

    public @Nullable String getReason() {
        return reason;
    }
}
