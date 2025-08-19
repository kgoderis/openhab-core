package org.openhab.core.ai.common.validation;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Base implementation of ValidationResult interface.
 * 
 * <p>
 * This class provides common functionality for all validation result implementations,
 * including basic properties and utility methods.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public abstract class BaseValidationResult implements ValidationResult {

    private final boolean valid;
    private final List<String> errors;
    private final List<String> warnings;
    private final Map<String, Object> details;
    private final @Nullable Instant validationTime;

    /**
     * Protected constructor for subclasses.
     * 
     * @param valid Whether the validation was successful
     * @param errors List of error messages
     * @param warnings List of warning messages
     * @param details Additional validation details
     * @param validationTime Timestamp of the validation
     */
    protected BaseValidationResult(boolean valid, List<String> errors, List<String> warnings,
            Map<String, Object> details, @Nullable Instant validationTime) {
        this.valid = valid;
        this.errors = List.copyOf(errors);
        this.warnings = List.copyOf(warnings);
        this.details = Map.copyOf(details);
        this.validationTime = validationTime;
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public List<String> getErrors() {
        return errors;
    }

    @Override
    public List<String> getWarnings() {
        return warnings;
    }

    @Override
    public Map<String, Object> getDetails() {
        return details;
    }

    @Override
    public @Nullable Instant getValidationTime() {
        return validationTime;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        BaseValidationResult other = (BaseValidationResult) obj;
        return valid == other.valid && Objects.equals(errors, other.errors) && Objects.equals(warnings, other.warnings)
                && Objects.equals(details, other.details) && Objects.equals(validationTime, other.validationTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valid, errors, warnings, details, validationTime);
    }

    @Override
    public String toString() {
        return String.format("BaseValidationResult{valid=%s, errors=%s, warnings=%s, details=%s, validationTime=%s}",
                valid, errors, warnings, details, validationTime);
    }
}
