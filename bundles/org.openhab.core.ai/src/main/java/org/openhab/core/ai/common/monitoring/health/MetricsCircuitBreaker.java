package org.openhab.core.ai.common.monitoring.health;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Production-ready circuit breaker for metrics collection.
 * 
 * This circuit breaker provides configurable failure thresholds and recovery
 * conditions with exponential backoff for recovery attempts.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public class MetricsCircuitBreaker {

    /**
     * Circuit breaker states.
     */
    public enum State {
        CLOSED,
        OPEN,
        HALF_OPEN
    }

    private final AtomicReference<State> state = new AtomicReference<>(State.CLOSED);
    private final AtomicLong failureCount = new AtomicLong(0);
    private final AtomicLong lastFailureTime = new AtomicLong(0);
    private final AtomicLong lastSuccessTime = new AtomicLong(0);
    private final AtomicLong nextAttemptTime = new AtomicLong(0);

    private final long failureThreshold;
    private final Duration timeout;
    private final Duration halfOpenTimeout;
    private final boolean enableExponentialBackoff;
    private final long maxBackoffMs;

    /**
     * Builder for MetricsCircuitBreaker.
     */
    public static class Builder {
        private long failureThreshold = 5;
        private Duration timeout = Duration.ofMinutes(1);
        private Duration halfOpenTimeout = Duration.ofSeconds(30);
        private boolean enableExponentialBackoff = true;
        private long maxBackoffMs = Duration.ofMinutes(5).toMillis();

        public Builder withFailureThreshold(long failureThreshold) {
            this.failureThreshold = failureThreshold;
            return this;
        }

        public Builder withTimeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder withHalfOpenTimeout(Duration halfOpenTimeout) {
            this.halfOpenTimeout = halfOpenTimeout;
            return this;
        }

        public Builder withExponentialBackoff(boolean enableExponentialBackoff) {
            this.enableExponentialBackoff = enableExponentialBackoff;
            return this;
        }

        public Builder withMaxBackoff(Duration maxBackoff) {
            this.maxBackoffMs = maxBackoff.toMillis();
            return this;
        }

        public MetricsCircuitBreaker build() {
            return new MetricsCircuitBreaker(this);
        }
    }

    /**
     * Create a new circuit breaker with default settings.
     */
    public MetricsCircuitBreaker() {
        this(new Builder());
    }

    /**
     * Create a new circuit breaker with custom settings.
     */
    public MetricsCircuitBreaker(Builder builder) {
        this.failureThreshold = builder.failureThreshold;
        this.timeout = builder.timeout;
        this.halfOpenTimeout = builder.halfOpenTimeout;
        this.enableExponentialBackoff = builder.enableExponentialBackoff;
        this.maxBackoffMs = builder.maxBackoffMs;
    }

    /**
     * Execute a callable with circuit breaker protection.
     * 
     * @param callable the callable to execute
     * @return the result of the callable
     * @throws CircuitBreakerOpenException if the circuit breaker is open
     * @throws Exception if the callable throws an exception
     */
    public <T> T execute(Supplier<T> callable) throws Exception {
        if (!canExecute()) {
            throw new CircuitBreakerOpenException("Circuit breaker is open");
        }

        try {
            T result = callable.get();
            onSuccess();
            return result;
        } catch (Exception e) {
            onFailure();
            throw e;
        }
    }

    /**
     * Execute a runnable with circuit breaker protection.
     * 
     * @param runnable the runnable to execute
     * @throws CircuitBreakerOpenException if the circuit breaker is open
     * @throws Exception if the runnable throws an exception
     */
    public void execute(Runnable runnable) throws Exception {
        if (!canExecute()) {
            throw new CircuitBreakerOpenException("Circuit breaker is open");
        }

        try {
            runnable.run();
            onSuccess();
        } catch (Exception e) {
            onFailure();
            throw e;
        }
    }

    /**
     * Check if the circuit breaker allows execution.
     * 
     * @return true if execution is allowed, false otherwise
     */
    public boolean canExecute() {
        State currentState = state.get();
        long currentTime = System.currentTimeMillis();

        switch (currentState) {
            case CLOSED:
                return true;

            case OPEN:
                if (currentTime >= nextAttemptTime.get()) {
                    if (state.compareAndSet(State.OPEN, State.HALF_OPEN)) {
                        return true;
                    }
                }
                return false;

            case HALF_OPEN:
                return true;

            default:
                return false;
        }
    }

    /**
     * Get the current state of the circuit breaker.
     * 
     * @return current state
     */
    public State getState() {
        return state.get();
    }

    /**
     * Get the current failure count.
     * 
     * @return failure count
     */
    public long getFailureCount() {
        return failureCount.get();
    }

    /**
     * Get the time of the last failure.
     * 
     * @return last failure time in milliseconds
     */
    public long getLastFailureTime() {
        return lastFailureTime.get();
    }

    /**
     * Get the time of the last success.
     * 
     * @return last success time in milliseconds
     */
    public long getLastSuccessTime() {
        return lastSuccessTime.get();
    }

    /**
     * Get the next attempt time.
     * 
     * @return next attempt time in milliseconds
     */
    public long getNextAttemptTime() {
        return nextAttemptTime.get();
    }

    /**
     * Get circuit breaker statistics.
     * 
     * @return map containing circuit breaker statistics
     */
    public java.util.Map<String, Object> getStatistics() {
        return java.util.Map.of("state", state.get().name(), "failureCount", failureCount.get(), "lastFailureTime",
                lastFailureTime.get(), "lastSuccessTime", lastSuccessTime.get(), "nextAttemptTime",
                nextAttemptTime.get(), "failureThreshold", failureThreshold, "timeoutMs", timeout.toMillis(),
                "halfOpenTimeoutMs", halfOpenTimeout.toMillis());
    }

    /**
     * Manually open the circuit breaker.
     */
    public void open() {
        state.set(State.OPEN);
        lastFailureTime.set(System.currentTimeMillis());
        calculateNextAttemptTime();
    }

    /**
     * Manually close the circuit breaker.
     */
    public void close() {
        state.set(State.CLOSED);
        failureCount.set(0);
        lastFailureTime.set(0);
        nextAttemptTime.set(0);
    }

    /**
     * Reset the circuit breaker to initial state.
     */
    public void reset() {
        close();
    }

    // ===== Private Methods =====

    /**
     * Handle a successful execution.
     */
    private void onSuccess() {
        lastSuccessTime.set(System.currentTimeMillis());

        State currentState = state.get();
        if (currentState == State.HALF_OPEN) {
            // Success in half-open state, close the circuit breaker
            if (state.compareAndSet(State.HALF_OPEN, State.CLOSED)) {
                failureCount.set(0);
                nextAttemptTime.set(0);
            }
        }
    }

    /**
     * Handle a failed execution.
     */
    private void onFailure() {
        long currentTime = System.currentTimeMillis();
        lastFailureTime.set(currentTime);

        State currentState = state.get();
        if (currentState == State.CLOSED) {
            long failures = failureCount.incrementAndGet();
            if (failures >= failureThreshold) {
                if (state.compareAndSet(State.CLOSED, State.OPEN)) {
                    calculateNextAttemptTime();
                }
            }
        } else if (currentState == State.HALF_OPEN) {
            // Failure in half-open state, open the circuit breaker
            if (state.compareAndSet(State.HALF_OPEN, State.OPEN)) {
                failureCount.incrementAndGet();
                calculateNextAttemptTime();
            }
        }
    }

    /**
     * Calculate the next attempt time with exponential backoff.
     */
    private void calculateNextAttemptTime() {
        long currentTime = System.currentTimeMillis();
        long baseTimeout = timeout.toMillis();

        if (enableExponentialBackoff) {
            long failures = failureCount.get();
            long exponentialTimeout = baseTimeout * (long) Math.pow(2, Math.min(failures - 1, 10));
            long nextAttempt = currentTime + Math.min(exponentialTimeout, maxBackoffMs);
            nextAttemptTime.set(nextAttempt);
        } else {
            nextAttemptTime.set(currentTime + baseTimeout);
        }
    }

    /**
     * Exception thrown when the circuit breaker is open.
     */
    public static class CircuitBreakerOpenException extends RuntimeException {
        public CircuitBreakerOpenException(String message) {
            super(message);
        }
    }
}
