package org.openhab.core.ai.tool.api.validation;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Result of validation comparison.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ValidationComparisonResult {
    private final boolean sameValidity;
    private final boolean sameErrors;
    private final boolean sameWarnings;

    public ValidationComparisonResult(boolean sameValidity, boolean sameErrors, boolean sameWarnings) {
        this.sameValidity = sameValidity;
        this.sameErrors = sameErrors;
        this.sameWarnings = sameWarnings;
    }

    public boolean isSameValidity() {
        return sameValidity;
    }

    public boolean isSameErrors() {
        return sameErrors;
    }

    public boolean isSameWarnings() {
        return sameWarnings;
    }

    public boolean isIdentical() {
        return sameValidity && sameErrors && sameWarnings;
    }
}


