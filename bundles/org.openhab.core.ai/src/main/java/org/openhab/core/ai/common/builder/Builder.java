package org.openhab.core.ai.common.builder;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Base interface for all builder patterns in the openHAB AI system.
 * 
 * <p>
 * This interface provides a unified foundation for all builder implementations:
 * - Common builder lifecycle management
 * - Type-safe building operations
 * - Validation and error handling
 * - Immutable result creation
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface Builder<T> {

    /**
     * Build the final object from the current builder state.
     * 
     * <p>
     * This method should:
     * - Validate all required fields are set
     * - Perform any cross-field validation
     * - Create an immutable result object
     * - Throw appropriate exceptions for validation failures
     * </p>
     * 
     * @return the built object
     * @throws IllegalStateException if required fields are missing or validation fails
     * @throws IllegalArgumentException if field values are invalid
     */
    T build();

    /**
     * Reset the builder to its initial state.
     * 
     * <p>
     * This method should clear all fields and return the builder to its
     * initial state, allowing it to be reused for building another object.
     * </p>
     * 
     * @return this builder for method chaining
     */
    Builder<T> reset();

    /**
     * Check if the builder is in a valid state for building.
     * 
     * <p>
     * This method should check if all required fields are set and
     * if the current state would result in a valid object.
     * </p>
     * 
     * @return true if the builder can successfully build an object
     */
    boolean isValid();

    /**
     * Get a description of any validation errors.
     * 
     * <p>
     * This method should return a human-readable description of any
     * validation errors that would prevent successful building.
     * </p>
     * 
     * @return validation error description or null if no errors
     */
    String getValidationErrors();
}
