package org.openhab.core.ai.reasoning.engine.api;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Validation information for a reasoning step.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record ValidationInfo(boolean isValid, @Nullable String validationMessage, @Nullable String validatorId,
        @Nullable Instant validationTime, double validationScore, @Nullable String validationMethod) {

    /**
     * Create a new ValidationInfo instance.
     * 
     * @param isValid whether the step is valid
     * @param validationMessage the validation message
     * @param validatorId the validator identifier
     * @param validationTime the validation timestamp
     * @param validationScore the validation score (0.0 to 1.0)
     * @param validationMethod the validation method used
     */
    public ValidationInfo {
        if (validationScore < 0.0 || validationScore > 1.0) {
            throw new IllegalArgumentException("validationScore must be between 0.0 and 1.0");
        }
    }

    /**
     * Create a valid validation info.
     * 
     * @param validationMessage the validation message
     * @param validatorId the validator identifier
     * @param validationScore the validation score
     * @return a new ValidationInfo instance
     */
    public static ValidationInfo valid(@Nullable String validationMessage, @Nullable String validatorId,
            double validationScore) {
        return new ValidationInfo(true, validationMessage, validatorId, Instant.now(), validationScore, null);
    }

    /**
     * Create an invalid validation info.
     * 
     * @param validationMessage the validation message
     * @param validatorId the validator identifier
     * @param validationScore the validation score
     * @return a new ValidationInfo instance
     */
    public static ValidationInfo invalid(@Nullable String validationMessage, @Nullable String validatorId,
            double validationScore) {
        return new ValidationInfo(false, validationMessage, validatorId, Instant.now(), validationScore, null);
    }

    /**
     * Create a validation info with custom method.
     * 
     * @param isValid whether the step is valid
     * @param validationMessage the validation message
     * @param validatorId the validator identifier
     * @param validationScore the validation score
     * @param validationMethod the validation method
     * @return a new ValidationInfo instance
     */
    public static ValidationInfo withMethod(boolean isValid, @Nullable String validationMessage,
            @Nullable String validatorId, double validationScore, @Nullable String validationMethod) {
        return new ValidationInfo(isValid, validationMessage, validatorId, Instant.now(), validationScore,
                validationMethod);
    }

    /**
     * Create a builder for ValidationInfo.
     * 
     * @return a new ValidationInfoBuilder
     */
    public static ValidationInfoBuilder builder() {
        return new ValidationInfoBuilder();
    }

    /**
     * Builder for ValidationInfo.
     */
    public static final class ValidationInfoBuilder {
        private boolean isValid = true;
        private @Nullable String validationMessage = null;
        private @Nullable String validatorId = null;
        private @Nullable Instant validationTime = Instant.now();
        private double validationScore = 1.0;
        private @Nullable String validationMethod = null;

        public ValidationInfoBuilder withValid(boolean isValid) {
            this.isValid = isValid;
            return this;
        }

        public ValidationInfoBuilder withValidationMessage(@Nullable String validationMessage) {
            this.validationMessage = validationMessage;
            return this;
        }

        public ValidationInfoBuilder withValidatorId(@Nullable String validatorId) {
            this.validatorId = validatorId;
            return this;
        }

        public ValidationInfoBuilder withValidationTime(@Nullable Instant validationTime) {
            this.validationTime = validationTime;
            return this;
        }

        public ValidationInfoBuilder withValidationScore(double validationScore) {
            this.validationScore = validationScore;
            return this;
        }

        public ValidationInfoBuilder withValidationMethod(@Nullable String validationMethod) {
            this.validationMethod = validationMethod;
            return this;
        }

        public ValidationInfo build() {
            return new ValidationInfo(isValid, validationMessage, validatorId, validationTime, validationScore,
                    validationMethod);
        }
    }
}
