package org.openhab.core.ai.common.monitoring.api;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Immutable record for circuit breaker state.
 * 
 * <p>
 * This record provides a thread-safe way to share circuit breaker state
 * between collectors and snapshots.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public record CircuitBreakerState(String state, long failureCount, long lastFailureTime, long lastSuccessTime,
        long threshold, long timeoutMs) {

    /**
     * Circuit breaker states.
     */
    public static final String CLOSED = "CLOSED";
    public static final String OPEN = "OPEN";
    public static final String HALF_OPEN = "HALF_OPEN";

    /**
     * Create a new CircuitBreakerState record.
     * 
     * @param state the circuit breaker state
     * @param failureCount the number of failures
     * @param lastFailureTime the timestamp of the last failure
     * @param lastSuccessTime the timestamp of the last success
     * @param threshold the failure threshold
     * @param timeoutMs the timeout in milliseconds
     */
    public CircuitBreakerState {
        Objects.requireNonNull(state, "state");
        if (failureCount < 0) {
            throw new IllegalArgumentException("failureCount must be non-negative");
        }
        if (lastFailureTime < 0) {
            throw new IllegalArgumentException("lastFailureTime must be non-negative");
        }
        if (lastSuccessTime < 0) {
            throw new IllegalArgumentException("lastSuccessTime must be non-negative");
        }
        if (threshold < 0) {
            throw new IllegalArgumentException("threshold must be non-negative");
        }
        if (timeoutMs < 0) {
            throw new IllegalArgumentException("timeoutMs must be non-negative");
        }
    }

    /**
     * Check if the circuit breaker is closed.
     * 
     * @return true if closed, false otherwise
     */
    public boolean isClosed() {
        return CLOSED.equals(state);
    }

    /**
     * Check if the circuit breaker is open.
     * 
     * @return true if open, false otherwise
     */
    public boolean isOpen() {
        return OPEN.equals(state);
    }

    /**
     * Check if the circuit breaker is half-open.
     * 
     * @return true if half-open, false otherwise
     */
    public boolean isHalfOpen() {
        return HALF_OPEN.equals(state);
    }

    /**
     * Get the circuit breaker state as a string.
     * 
     * @return the state string
     */
    public String breakerState() {
        return state;
    }

    /**
     * Create a closed circuit breaker state.
     * 
     * @param failureCount the number of failures
     * @param lastFailureTime the timestamp of the last failure
     * @param lastSuccessTime the timestamp of the last success
     * @param threshold the failure threshold
     * @param timeoutMs the timeout in milliseconds
     * @return a closed circuit breaker state
     */
    public static CircuitBreakerState closed(long failureCount, long lastFailureTime, long lastSuccessTime,
            long threshold, long timeoutMs) {
        return new CircuitBreakerState(CLOSED, failureCount, lastFailureTime, lastSuccessTime, threshold, timeoutMs);
    }

    /**
     * Create an open circuit breaker state.
     * 
     * @param failureCount the number of failures
     * @param lastFailureTime the timestamp of the last failure
     * @param lastSuccessTime the timestamp of the last success
     * @param threshold the failure threshold
     * @param timeoutMs the timeout in milliseconds
     * @return an open circuit breaker state
     */
    public static CircuitBreakerState open(long failureCount, long lastFailureTime, long lastSuccessTime,
            long threshold, long timeoutMs) {
        return new CircuitBreakerState(OPEN, failureCount, lastFailureTime, lastSuccessTime, threshold, timeoutMs);
    }

    /**
     * Create a half-open circuit breaker state.
     * 
     * @param failureCount the number of failures
     * @param lastFailureTime the timestamp of the last failure
     * @param lastSuccessTime the timestamp of the last success
     * @param threshold the failure threshold
     * @param timeoutMs the timeout in milliseconds
     * @return a half-open circuit breaker state
     */
    public static CircuitBreakerState halfOpen(long failureCount, long lastFailureTime, long lastSuccessTime,
            long threshold, long timeoutMs) {
        return new CircuitBreakerState(HALF_OPEN, failureCount, lastFailureTime, lastSuccessTime, threshold, timeoutMs);
    }
}
