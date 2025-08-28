package org.openhab.core.ai.common.monitoring.collector;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.HealthStatus;

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
 * The collector provides raw data access for metrics collection and storage.
 * Snapshot creation is handled by the MetricsService layer.
 * </p>
 * 
 * <p>
 * <strong>Data Storage Strategy:</strong>
 * <ul>
 * <li><strong>Specific Fields:</strong> Use for standard metrics (counts, timing, health status, etc.)
 * These are optimized for performance and provide type-safe access.</li>
 * <li><strong>Extended Data:</strong> Use for additional metrics not covered by specific fields,
 * such as custom business metrics, configuration data, or domain-specific information.</li>
 * </ul>
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public final class MetricsCollector {

    private final LongAdder totalDurationNanos = new LongAdder();
    private final AtomicReference<HealthStatus> healthStatus = new AtomicReference<>(HealthStatus.UNKNOWN);
    private final AtomicReference<String> statusMessage = new AtomicReference<>("");
    private final LongAdder failureCount = new LongAdder();
    private final LongAdder successCount = new LongAdder();
    private final LongAdder consecutiveFailures = new LongAdder();
    private final AtomicReference<Long> lastFailureTime = new AtomicReference<>(0L);
    private final AtomicReference<Long> lastSuccessTime = new AtomicReference<>(0L);
    private final AtomicReference<String> lastError = new AtomicReference<>("");
    private final LongAdder lastCheckTime = new LongAdder();

    // ===== Extended Data Storage =====
    /**
     * Extended data storage for additional metrics not covered by specific fields.
     * 
     * <p>
     * <strong>When to use Extended Data:</strong>
     * <ul>
     * <li>Custom business metrics (e.g., "user_sessions", "cache_hits")</li>
     * <li>Configuration data (e.g., "timeout_ms", "retry_count")</li>
     * <li>Domain-specific information (e.g., "model_version", "api_endpoint")</li>
     * <li>Temporary or experimental metrics</li>
     * <li>Complex data structures that don't fit specific fields</li>
     * </ul>
     * 
     * <p>
     * <strong>When NOT to use Extended Data:</strong>
     * <ul>
     * <li>Standard execution metrics (use specific fields instead)</li>
     * <li>Health status information (use specific fields instead)</li>
     * <li>Performance-critical counters (use LongAdder fields instead)</li>
     * </ul>
     * </p>
     */
    private final Map<String, Object> extendedData = new ConcurrentHashMap<>();

    // ===== Recording Methods =====

    /**
     * Records an execution with the given outcome and duration.
     * This method updates both execution and health metrics.
     * 
     * @param success true if the execution was successful, false otherwise
     * @param durationNanos the duration of the execution in nanoseconds
     */
    public void recordExecution(boolean success, long durationNanos) {
        // Update execution metrics
        totalDurationNanos.add(durationNanos);

        if (success) {
            successCount.increment();
            lastSuccessTime.set(System.currentTimeMillis());
            consecutiveFailures.reset();
            setHealthStatus(HealthStatus.HEALTHY, "Operation successful");
        } else {
            failureCount.increment();
            lastFailureTime.set(System.currentTimeMillis());
            consecutiveFailures.increment();
            setHealthStatus(HealthStatus.UNHEALTHY, "Operation failed");
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
    public void recordError(String errorType, @Nullable String errorMessage) {
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

        setHealthStatus(HealthStatus.UNHEALTHY, statusMsg);
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
     * Records extended data with a key.
     * 
     * <p>
     * Use this method for additional metrics not covered by specific fields.
     * Examples:
     * </p>
     * <ul>
     * <li>Custom business metrics: {@code recordExtendedData("user_sessions", 150)}</li>
     * <li>Configuration data: {@code recordExtendedData("timeout_ms", 5000)}</li>
     * <li>Domain-specific info: {@code recordExtendedData("model_version", "v2.1")}</li>
     * <li>Complex data: {@code recordExtendedData("api_endpoints", List.of("GET", "POST"))}</li>
     * </ul>
     * 
     * <p>
     * <strong>Note:</strong> Extended data is cleared when {@link #reset()} is called.
     * If you need persistent configuration data, consider storing it elsewhere.
     * </p>
     * 
     * @param key the data key
     * @param value the value to store
     */
    public void recordExtendedData(String key, Object value) {
        extendedData.put(key, value);
    }

    /**
     * Records multiple extended data items.
     * 
     * <p>
     * Use this method to record multiple extended data items at once.
     * See {@link #recordExtendedData(String, Object)} for usage guidelines.
     * </p>
     * 
     * @param dataMap map of data to store
     */
    public void recordExtendedData(Map<String, Object> dataMap) {
        extendedData.putAll(dataMap);
    }

    // ===== Health Status Management =====

    /**
     * Sets the health status and message.
     * 
     * @param status the new health status
     * @param message the status message
     */
    public void setHealthStatus(HealthStatus status, String message) {
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
        long totalOps = getTotal();
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

    // ===== Execution Metrics Access Methods =====

    /**
     * Get the current total count.
     * 
     * @return current total count
     */
    public long getTotal() {
        return successCount.sum() + failureCount.sum();
    }

    /**
     * Get the current success count.
     * 
     * @return current success count
     */
    public long getSuccess() {
        return successCount.sum();
    }

    /**
     * Get the current failure count.
     * 
     * @return current failure count
     */
    public long getFailure() {
        return failureCount.sum();
    }

    /**
     * Get the current total duration in nanoseconds.
     * 
     * @return current total duration in nanoseconds
     */
    public long getTotalDurationNanos() {
        return totalDurationNanos.sum();
    }

    /**
     * Get the current error count.
     * 
     * @return current error count
     */
    public long getErrorCount() {
        return failureCount.sum();
    }

    // ===== Health Metrics Access Methods =====

    /**
     * Get current health status.
     * 
     * @return current health status
     */
    public HealthStatus getHealthStatus() {
        return healthStatus.get();
    }

    /**
     * Get current status message.
     * 
     * @return current status message
     */
    public String getStatusMessage() {
        return statusMessage.get();
    }

    /**
     * Get current failure count (health perspective).
     * 
     * @return current failure count
     */
    public long getFailureCount() {
        return failureCount.sum();
    }

    /**
     * Get current success count (health perspective).
     * 
     * @return current success count
     */
    public long getSuccessCount() {
        return successCount.sum();
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
     * Get the last failure time in milliseconds.
     * 
     * @return last failure time in milliseconds
     */
    public long getLastFailureTime() {
        return lastFailureTime.get();
    }

    /**
     * Get the last success time in milliseconds.
     * 
     * @return last success time in milliseconds
     */
    public long getLastSuccessTime() {
        return lastSuccessTime.get();
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

    // ===== Extended Data Access Methods =====

    /**
     * Get extended data value.
     * 
     * <p>
     * Retrieves additional data that was stored using {@link #recordExtendedData(String, Object)}.
     * Returns null if the key is not found.
     * </p>
     * 
     * @param key the data key
     * @return the stored value, or null if not found
     */
    @Nullable
    public Object getExtendedData(String key) {
        return extendedData.get(key);
    }

    /**
     * Get extended data value with type casting.
     * 
     * <p>
     * Retrieves and casts extended data to the specified type.
     * Returns null if the key is not found or the value cannot be cast to the specified type.
     * </p>
     * 
     * <p>
     * Example usage:
     * </p>
     * 
     * <pre>{@code
     * Integer timeout = collector.getExtendedData("timeout_ms", Integer.class);
     * String version = collector.getExtendedData("model_version", String.class);
     * List<String> endpoints = collector.getExtendedData("api_endpoints", List.class);
     * }</pre>
     * 
     * @param <T> the expected type
     * @param key the data key
     * @param type the expected class type
     * @return the stored value cast to the expected type, or null if not found or wrong type
     */
    @Nullable
    public <T> T getExtendedData(String key, Class<T> type) {
        Object value = extendedData.get(key);
        if (value != null && type.isInstance(value)) {
            return type.cast(value);
        }
        return null;
    }

    /**
     * Get all extended data as a map.
     * 
     * <p>
     * Returns a defensive copy of all extended data. Modifications to the returned map
     * will not affect the internal storage.
     * </p>
     * 
     * @return map of all extended data
     */
    public Map<String, Object> getExtendedData() {
        return new ConcurrentHashMap<>(extendedData);
    }

    // ===== Utility Methods =====

    /**
     * Resets all counters and state to initial values.
     * This method resets both execution and health metrics.
     * 
     * <p>
     * <strong>Note:</strong> This also clears all extended data. If you need to preserve
     * configuration data, consider storing it elsewhere or re-recording it after reset.
     * </p>
     */
    public void reset() {
        // Reset execution metrics
        totalDurationNanos.reset();

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

        // Reset extended data
        extendedData.clear();
    }

    /**
     * Check if this collector has recorded any data.
     * 
     * @return true if any operations have been recorded
     */
    public boolean hasData() {
        return getTotal() > 0;
    }

    /**
     * Get current total operations count (health perspective).
     * 
     * @return total operations count
     */
    public long getTotalOperations() {
        return getTotal();
    }

    /**
     * Get current success rate as percentage.
     * 
     * @return success rate percentage (0.0-100.0)
     */
    public double getSuccessRate() {
        long total = getTotal();
        if (total == 0) {
            return 0.0;
        }
        return (successCount.sum() * 100.0) / total;
    }

    /**
     * Check if the component is currently healthy based on lightweight checks.
     * 
     * @return true if status is HEALTHY and success rate is above 95%
     */
    public boolean isHealthy() {
        return healthStatus.get() == HealthStatus.HEALTHY && getSuccessRate() >= 95.0;
    }
}
