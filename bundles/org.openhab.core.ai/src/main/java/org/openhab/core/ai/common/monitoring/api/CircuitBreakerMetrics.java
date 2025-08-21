package org.openhab.core.ai.common.monitoring.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Capability interface for circuit breaker metrics.
 * 
 * <p>
 * This interface provides functionality for tracking circuit breaker state
 * and failure information including breaker state, failure counts, and timing.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface CircuitBreakerMetrics {

    /**
     * Get the current circuit breaker state.
     * 
     * @return circuit breaker state (e.g., "CLOSED", "OPEN", "HALF_OPEN")
     */
    String breakerState();

    /**
     * Check if the circuit breaker is currently open.
     * 
     * @return true if circuit breaker is open, false otherwise
     */
    boolean isOpen();

    /**
     * Get the total number of failures that triggered the circuit breaker.
     * 
     * @return failure count
     */
    long failureCount();

    /**
     * Get the timestamp of the last failure in milliseconds since epoch.
     * 
     * @return last failure timestamp, or 0 if no failures
     */
    long lastFailureTime();

    /**
     * Check if the circuit breaker is currently closed (normal operation).
     * 
     * @return true if circuit breaker is closed, false otherwise
     */
    default boolean isClosed() {
        return !isOpen();
    }

    /**
     * Check if the circuit breaker is in half-open state.
     * 
     * @return true if circuit breaker is half-open, false otherwise
     */
    default boolean isHalfOpen() {
        return !isClosed() && !isOpen();
    }
}
