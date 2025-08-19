package org.openhab.core.ai.common.validation;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Reasoning-specific validation result implementation.
 * 
 * <p>
 * This class provides validation results specific to reasoning operations,
 * including mutable error and warning collection for step-by-step validation.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ReasoningValidationResult extends BaseValidationResult {

    private final List<String> mutableErrors;
    private final List<String> mutableWarnings;

    /**
     * Constructor for ReasoningValidationResult.
     * 
     * @param valid Whether the validation was successful
     * @param errors List of error messages
     * @param warnings List of warning messages
     * @param details Additional validation details
     * @param validationTime Timestamp of the validation
     */
    public ReasoningValidationResult(boolean valid, List<String> errors, List<String> warnings,
            Map<String, Object> details, @Nullable Instant validationTime) {
        super(valid, errors, warnings, details, validationTime);
        this.mutableErrors = new ArrayList<>(errors);
        this.mutableWarnings = new ArrayList<>(warnings);
    }

    /**
     * Add an error message.
     * 
     * @param error Error message to add
     */
    public void addError(String error) {
        mutableErrors.add(error);
    }

    /**
     * Add a warning message.
     * 
     * @param warning Warning message to add
     */
    public void addWarning(String warning) {
        mutableWarnings.add(warning);
    }

    @Override
    public List<String> getErrors() {
        return new ArrayList<>(mutableErrors);
    }

    @Override
    public List<String> getWarnings() {
        return new ArrayList<>(mutableWarnings);
    }

    @Override
    public boolean isValid() {
        return mutableErrors.isEmpty();
    }

    /**
     * Create a new reasoning validation result.
     * 
     * @return new reasoning validation result
     */
    public static ReasoningValidationResult create() {
        return new ReasoningValidationResult(true, List.of(), List.of(), Map.of(), Instant.now());
    }
}
