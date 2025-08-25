package org.openhab.core.ai.common.monitoring.collector;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.Counts;
import org.openhab.core.ai.common.monitoring.api.Health.HealthStatus;
import org.openhab.core.ai.common.monitoring.api.Timing;
import org.openhab.core.ai.common.monitoring.snapshot.ExecutionMetricsSnapshot;

/**
 * Unified thread-safe collector for both execution and health metrics.
 * 
 * <p>
 * This collector combines the functionality of ExecutionMetricsCollector and
 * ProviderHealthCollector into a single, unified collector that handles both
 * execution metrics (counts, timing) and health metrics (status, errors) in a
 * thread-safe manner using LongAdder for optimal performance.
 * </p>
 * 
 * <p>
 * The collector provides multiple snapshot views:
 * - Execution metrics snapshot (counts, timing, performance)
 * - Health metrics snapshot (status, errors, health indicators)
 * - Unified snapshot (combines both execution and health data)
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public final class MetricsCollector {

    // ===== Execution Metrics (from ExecutionMetricsCollector) =====
    private final LongAdder total = new LongAdder();
    private final LongAdder success = new LongAdder();
    private final LongAdder totalDurationNanos = new LongAdder();
    private final LongAdder errorCount = new LongAdder();

    // ===== Health Metrics (from ProviderHealthCollector) =====
    private final AtomicReference<HealthStatus> healthStatus = new AtomicReference<>(HealthStatus.UNKNOWN);
    private final AtomicReference<String> statusMessage = new AtomicReference<>("");
    private final LongAdder failureCount = new LongAdder();
    private final LongAdder successCount = new LongAdder();
    private final AtomicReference<Long> lastFailureTime = new AtomicReference<>(0L);
    private final AtomicReference<Long> lastSuccessTime = new AtomicReference<>(0L);
    private final AtomicReference<String> lastError = new AtomicReference<>("");
    private final LongAdder consecutiveFailures = new LongAdder();

    // ===== Unified Metrics =====
    private final LongAdder lastCheckTime = new LongAdder();

    // ===== Data Storage =====
    private final Map<String, Object> data = new ConcurrentHashMap<>();

    /**
     * Records an execution with the given outcome and duration.
     * This method updates both execution and health metrics.
     * 
     * @param success true if the execution was successful, false otherwise
     * @param durationNanos the duration of the execution in nanoseconds
     */
    public void recordExecution(boolean success, long durationNanos) {
        // Update execution metrics
        total.increment();
        totalDurationNanos.add(durationNanos);

        if (success) {
            this.success.increment();
            successCount.increment();
            lastSuccessTime.set(System.currentTimeMillis());
            consecutiveFailures.reset();
            updateHealthStatus(HealthStatus.HEALTHY, "Operation successful");
        } else {
            failureCount.increment();
            lastFailureTime.set(System.currentTimeMillis());
            consecutiveFailures.increment();
            updateHealthStatus(HealthStatus.UNHEALTHY, "Operation failed");
        }

        lastCheckTime.reset();
        lastCheckTime.add(System.currentTimeMillis());
    }

    /**
     * Records an error with optional error message.
     * This method updates health metrics and error tracking.
     * 
     * @param errorType the type of error (e.g., "timeout", "connection", "validation")
     * @param errorMessage optional error message, may be null
     */
    public void recordError(String errorType, @org.eclipse.jdt.annotation.Nullable String errorMessage) {
        errorCount.increment();
        failureCount.increment();
        consecutiveFailures.increment();
        lastFailureTime.set(System.currentTimeMillis());

        if (errorMessage != null) {
            lastError.set(errorMessage);
        }

        String statusMsg = "Error occurred: " + errorType;
        if (errorMessage != null) {
            statusMsg += " - " + errorMessage;
        }

        updateHealthStatus(HealthStatus.UNHEALTHY, statusMsg);
        lastCheckTime.reset();
        lastCheckTime.add(System.currentTimeMillis());
    }

    /**
     * Records an error with just the error type.
     * 
     * @param errorType the type of error
     */
    public void recordError(String errorType) {
        recordError(errorType, null);
    }

    /**
     * Records data with a key.
     * 
     * @param key the data key
     * @param value the value to store
     */
    public void recordData(String key, Object value) {
        data.put(key, value);
    }

    /**
     * Records multiple data items.
     * 
     * @param dataMap map of data to store
     */
    public void recordData(Map<String, Object> dataMap) {
        data.putAll(dataMap);
    }

    /**
     * Updates the health status and message.
     * 
     * @param status the new health status
     * @param message the status message
     */
    public void updateHealthStatus(HealthStatus status, String message) {
        healthStatus.set(status);
        statusMessage.set(message != null ? message : "");
    }

    /**
     * Updates the health status based on current metrics.
     * This method automatically determines health status based on:
     * - Success rate
     * - Consecutive failures
     * - Recent activity
     */
    public void updateHealthStatus() {
        long totalOps = total.sum();
        long failures = consecutiveFailures.sum();
        long successes = successCount.sum();

        if (totalOps == 0) {
            healthStatus.set(HealthStatus.UNKNOWN);
            statusMessage.set("No operations recorded");
        } else if (failures >= 5) {
            healthStatus.set(HealthStatus.UNHEALTHY);
            statusMessage.set("Multiple consecutive failures detected");
        } else if (failures >= 2) {
            healthStatus.set(HealthStatus.DEGRADED);
            statusMessage.set("Some consecutive failures detected");
        } else if (successes > 0) {
            double successRate = (double) successes / (successes + failureCount.sum());
            if (successRate >= 0.95) {
                healthStatus.set(HealthStatus.HEALTHY);
                statusMessage.set("Operations successful");
            } else if (successRate >= 0.8) {
                healthStatus.set(HealthStatus.DEGRADED);
                statusMessage.set("Some operations failing");
            } else {
                healthStatus.set(HealthStatus.UNHEALTHY);
                statusMessage.set("High failure rate");
            }
        } else {
            healthStatus.set(HealthStatus.UNKNOWN);
            statusMessage.set("No successful operations recorded");
        }

        lastCheckTime.reset();
        lastCheckTime.add(System.currentTimeMillis());
    }

    // ===== Snapshot Methods =====

    /**
     * Creates an immutable snapshot of the current execution metrics with health data.
     * 
     * @return an immutable ExecutionMetricsSnapshot containing the current execution and health metrics
     */
    public ExecutionMetricsSnapshot executionSnapshot() {
        long t = total.sum();
        long s = success.sum();
        long d = totalDurationNanos.sum();
        long failures = failureCount.sum();
        long successes = successCount.sum();
        long consecutive = consecutiveFailures.sum();

        return new ExecutionMetricsSnapshot(new Counts(t, s, Math.max(0, t - s)), new Timing(d),
                System.currentTimeMillis(), healthStatus.get(), statusMessage.get(), lastFailureTime.get(),
                lastSuccessTime.get(), lastError.get(), consecutive);
    }

    // ===== Reset and Utility Methods =====

    /**
     * Resets all counters and state to initial values.
     * This method resets both execution and health metrics.
     */
    public void reset() {
        // Reset execution metrics
        total.reset();
        success.reset();
        totalDurationNanos.reset();
        errorCount.reset();

        // Reset health metrics
        failureCount.reset();
        successCount.reset();
        consecutiveFailures.reset();
        healthStatus.set(HealthStatus.UNKNOWN);
        statusMessage.set("");
        lastFailureTime.set(0L);
        lastSuccessTime.set(0L);
        lastError.set("");
        lastCheckTime.reset();

        // Reset data
        data.clear();
    }

    /**
     * Get the current total count without creating a full snapshot.
     * 
     * @return current total count
     */
    public long getCurrentTotal() {
        return total.sum();
    }

    /**
     * Get the current success count without creating a full snapshot.
     * 
     * @return current success count
     */
    public long getCurrentSuccess() {
        return success.sum();
    }

    /**
     * Get current health status without creating a full snapshot.
     * 
     * @return current health status
     */
    public HealthStatus getCurrentHealthStatus() {
        return healthStatus.get();
    }

    /**
     * Get current total operations count (health perspective).
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
     * Check if this collector has recorded any data.
     * 
     * @return true if any operations have been recorded
     */
    public boolean hasData() {
        return total.sum() > 0 || getCurrentTotalOperations() > 0;
    }

    /**
     * Check if the component is currently healthy based on lightweight checks.
     * 
     * @return true if status is HEALTHY and success rate is above 95%
     */
    public boolean isCurrentlyHealthy() {
        return healthStatus.get() == HealthStatus.HEALTHY && getCurrentSuccessRate() >= 95.0;
    }

    /**
     * Get the number of consecutive failures.
     * 
     * @return consecutive failure count
     */
    public long getConsecutiveFailures() {
        return consecutiveFailures.sum();
    }

    /**
     * Get the last error message.
     * 
     * @return last error message, or empty string if none
     */
    public String getLastError() {
        return lastError.get();
    }

    /**
     * Get the last check time in milliseconds.
     * 
     * @return last check time in milliseconds
     */
    public long getLastCheckTime() {
        return lastCheckTime.sum();
    }

    /**
     * Get data value.
     * 
     * @param key the data key
     * @return the stored value, or null if not found
     */
    @Nullable
    public Object getData(String key) {
        return data.get(key);
    }

    /**
     * Get data value with type casting.
     * 
     * @param <T> the expected type
     * @param key the data key
     * @param type the expected class type
     * @return the stored value cast to the expected type, or null if not found or wrong type
     */
    @Nullable
    public <T> T getData(String key, Class<T> type) {
        Object value = data.get(key);
        if (value != null && type.isInstance(value)) {
            return type.cast(value);
        }
        return null;
    }

    /**
     * Get all data as a map.
     * 
     * @return map of all data
     */
    public Map<String, Object> getData() {
        return new ConcurrentHashMap<>(data);
    }
}
