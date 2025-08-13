package org.openhab.core.ai.tool.util;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Enhanced validation result with additional context.
 *
 * <p>
 * Captures validation status, errors, warnings, tool identification,
 * parameter snapshot and validation time.
 * </p>
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class EnhancedValidationResult {
    private final boolean valid;
    private final List<String> errors;
    private final List<String> warnings;
    private final String toolId;
    private final Map<String, Object> parameters;
    private final long validationTime;

    public EnhancedValidationResult(boolean valid, List<String> errors, List<String> warnings, String toolId,
            Map<String, Object> parameters) {
        this.valid = valid;
        this.errors = errors;
        this.warnings = warnings;
        this.toolId = toolId;
        this.parameters = parameters;
        this.validationTime = System.currentTimeMillis();
    }

    public boolean isValid() {
        return valid;
    }

    public List<String> getErrors() {
        return errors;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public String getToolId() {
        return toolId;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public long getValidationTime() {
        return validationTime;
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    public String getValidationSummary() {
        if (valid) {
            return String.format("Validation passed for tool %s with %d warnings", toolId, warnings.size());
        } else {
            return String.format("Validation failed for tool %s with %d errors, %d warnings", toolId, errors.size(),
                    warnings.size());
        }
    }
}
