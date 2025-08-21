package org.openhab.core.ai.common.monitoring.collector;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.Health.HealthStatus;
import org.openhab.core.ai.common.monitoring.snapshot.ProviderHealthSnapshot;

/**
 * Thread-safe collector for provider health metrics with circuit breaker state tracking.
 * 
 * This collector tracks health state transitions, circuit breaker states, and health
 * indicators in a thread-safe manner, providing immutable snapshots for monitoring.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public final class ProviderHealthCollector {
    private final AtomicReference<HealthStatus> currentStatus = new AtomicReference<>(HealthStatus.UNKNOWN);
    private final AtomicReference<String> statusMessage = new AtomicReference<>("");
    private final LongAdder failureCount = new LongAdder();
    private final LongAdder successCount = new LongAdder();
    private final AtomicReference<Long> lastFailureTime = new AtomicReference<>(0L);
    private final AtomicReference<Long> lastSuccessTime = new AtomicReference<>(0L);
    private final AtomicReference<String> lastError = new AtomicReference<>("");

    /**
     * Records a successful operation.
     */
    public void recordSuccess() {
        successCount.increment();
        lastSuccessTime.set(System.currentTimeMillis());
        updateStatus(HealthStatus.HEALTHY, "Operation successful");
    }

    /**
     * Records a failed operation with optional error message.
     * 
     * @param errorMessage the error message, may be null
     */
    public void recordFailure(@org.eclipse.jdt.annotation.Nullable String errorMessage) {
        failureCount.increment();
        lastFailureTime.set(System.currentTimeMillis());
        if (errorMessage != null) {
            lastError.set(errorMessage);
        }
        updateStatus(HealthStatus.UNHEALTHY,
                "Operation failed: " + (errorMessage != null ? errorMessage : "Unknown error"));
    }

    /**
     * Updates the health status and message.
     * 
     * @param status the new health status
     * @param message the status message
     */
    public void updateStatus(HealthStatus status, String message) {
        currentStatus.set(status);
        statusMessage.set(message != null ? message : "");
    }

    /**
     * Creates an immutable snapshot of the current health state.
     * 
     * @return an immutable ProviderHealthSnapshot containing the current health metrics
     */
    public ProviderHealthSnapshot snapshot() {
        long failures = failureCount.sum();
        long successes = successCount.sum();
        long total = failures + successes;
        double successRate = total > 0 ? (double) successes / total : 0.0;

        return new ProviderHealthSnapshot(currentStatus.get(), statusMessage.get(), total, successes, failures,
                successRate, lastFailureTime.get(), lastSuccessTime.get(), lastError.get(), System.currentTimeMillis());
    }

    /**
     * Resets all counters and state to initial values.
     */
    public void reset() {
        failureCount.reset();
        successCount.reset();
        currentStatus.set(HealthStatus.UNKNOWN);
        statusMessage.set("");
        lastFailureTime.set(0L);
        lastSuccessTime.set(0L);
        lastError.set("");
    }

    // ===== Performance Optimization Methods =====

    /**
     * Get current health status without creating a full snapshot.
     * 
     * @return current health status
     */
    public HealthStatus getCurrentStatus() {
        return currentStatus.get();
    }

    /**
     * Get current total operations count.
     * 
     * @return total operations count
     */
    public long getCurrentTotalOperations() {
        return failureCount.sum() + successCount.sum();
    }

    /**
     * Get current success rate as percentage without creating a full snapshot.
     * 
     * @return success rate percentage (0.0-100.0)
     */
    public double getCurrentSuccessRate() {
        long total = getCurrentTotalOperations();
        if (total == 0) {
            return 0.0;
        }
        return (successCount.sum() * 100.0) / total;
    }

    /**
     * Check if this collector has recorded any health data.
     * 
     * @return true if any health data has been recorded
     */
    public boolean hasData() {
        return getCurrentTotalOperations() > 0 || !lastError.get().isEmpty();
    }

    /**
     * Check if the provider is currently healthy based on lightweight checks.
     * 
     * @return true if status is HEALTHY and success rate is above 95%
     */
    public boolean isCurrentlyHealthy() {
        return currentStatus.get() == HealthStatus.HEALTHY && getCurrentSuccessRate() >= 95.0;
    }
}
