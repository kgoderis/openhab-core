package org.openhab.core.ai.tool.util.helpers;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Parameter validation result DTO.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ParameterValidationResult {
    private final boolean valid;
    private final @Nullable String errorMessage;

    public ParameterValidationResult(boolean valid, @Nullable String errorMessage) {
        this.valid = valid;
        this.errorMessage = errorMessage;
    }

    public boolean isValid() {
        return valid;
    }

    public @Nullable String getErrorMessage() {
        return errorMessage;
    }
}


