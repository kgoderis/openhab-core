package org.openhab.core.ai.reasoning.configuration.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Result of configuration validation.
 *
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public class ConfigurationValidationResult {
    private final boolean valid;
    private final String[] errors;
    private final String[] warnings;

    public ConfigurationValidationResult(boolean valid, String[] errors, String[] warnings) {
        this.valid = valid;
        this.errors = errors;
        this.warnings = warnings;
    }

    public boolean isValid() {
        return valid;
    }

    public String[] getErrors() {
        return errors;
    }

    public String[] getWarnings() {
        return warnings;
    }
}
