package org.openhab.core.ai.common.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Abstract base class implementing the Builder interface.
 * 
 * <p>
 * This class provides common functionality for all builder implementations:
 * - Validation error collection and reporting
 * - Common validation utilities
 * - Standard error handling patterns
 * - Builder state management
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public abstract class AbstractBuilder<T> implements Builder<T> {

    private final List<String> validationErrors = new ArrayList<>();

    @Override
    public boolean isValid() {
        validationErrors.clear();
        validate();
        return validationErrors.isEmpty();
    }

    @Override
    public @Nullable String getValidationErrors() {
        if (validationErrors.isEmpty()) {
            return null;
        }
        return String.join("; ", validationErrors);
    }

    @Override
    public Builder<T> reset() {
        validationErrors.clear();
        doReset();
        return this;
    }

    /**
     * Add a validation error to the error list.
     * 
     * @param error the validation error message
     */
    protected void addValidationError(String error) {
        validationErrors.add(Objects.requireNonNull(error, "Validation error cannot be null"));
    }

    /**
     * Validate that a required field is not null.
     * 
     * @param value the value to validate
     * @param fieldName the name of the field for error reporting
     * @return true if the value is not null
     */
    protected boolean validateRequired(@Nullable Object value, String fieldName) {
        if (value == null) {
            addValidationError(fieldName + " is required");
            return false;
        }
        return true;
    }

    /**
     * Validate that a string field is not null or blank.
     * 
     * @param value the string value to validate
     * @param fieldName the name of the field for error reporting
     * @return true if the value is not null and not blank
     */
    protected boolean validateRequiredString(@Nullable String value, String fieldName) {
        if (!validateRequired(value, fieldName)) {
            return false;
        }
        if (value.trim().isEmpty()) {
            addValidationError(fieldName + " cannot be blank");
            return false;
        }
        return true;
    }

    /**
     * Validate that a numeric value is within a valid range.
     * 
     * @param value the numeric value to validate
     * @param fieldName the name of the field for error reporting
     * @param min the minimum allowed value (inclusive)
     * @param max the maximum allowed value (inclusive)
     * @return true if the value is within the valid range
     */
    protected boolean validateRange(int value, String fieldName, int min, int max) {
        if (value < min || value > max) {
            addValidationError(fieldName + " must be between " + min + " and " + max + " (got: " + value + ")");
            return false;
        }
        return true;
    }

    /**
     * Validate that a numeric value is positive.
     * 
     * @param value the numeric value to validate
     * @param fieldName the name of the field for error reporting
     * @return true if the value is positive
     */
    protected boolean validatePositive(int value, String fieldName) {
        if (value <= 0) {
            addValidationError(fieldName + " must be positive (got: " + value + ")");
            return false;
        }
        return true;
    }

    /**
     * Validate that a numeric value is non-negative.
     * 
     * @param value the numeric value to validate
     * @param fieldName the name of the field for error reporting
     * @return true if the value is non-negative
     */
    protected boolean validateNonNegative(int value, String fieldName) {
        if (value < 0) {
            addValidationError(fieldName + " must be non-negative (got: " + value + ")");
            return false;
        }
        return true;
    }

    /**
     * Perform validation of the current builder state.
     * 
     * <p>
     * Subclasses should override this method to implement their specific
     * validation logic. Use the protected validation methods to add errors
     * to the validation error list.
     * </p>
     */
    protected void validate() {
        // Default implementation does nothing, subclasses can override
    }

    /**
     * Reset the builder to its initial state.
     * 
     * <p>
     * Subclasses should override this method to reset their specific fields
     * to their initial values.
     * </p>
     */
    protected void doReset() {
        // Default implementation does nothing, subclasses can override
    }
}
