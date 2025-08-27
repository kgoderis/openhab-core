package org.openhab.core.ai.common.monitoring.service.snapshot;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.ModelMetrics;
import org.openhab.core.ai.common.monitoring.api.MetricsSnapshot;

/**
 * Generic metrics snapshot that stores metrics as a flexible data map.
 * 
 * <p>
 * This snapshot provides a generic, reusable structure for storing any combination
 * of metrics. It's particularly useful for domains and operations that don't have
 * specific snapshot types defined yet, or for cases where flexibility is needed.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class GenericMetricsSnapshot implements MetricsSnapshot {

    private final String domain;
    private final String operation;
    private final long timestampMs;
    private final Map<String, Object> metrics;
    private final Set<Class<?>> detectedCapabilities;

    // ===== Constructors =====

    /**
     * Create a new generic metrics snapshot.
     * 
     * @param domain the operation domain
     * @param operation the operation name
     * @param metrics the metrics data map
     */
    public GenericMetricsSnapshot(String domain, String operation, Map<String, Object> metrics) {
        this.domain = domain;
        this.operation = operation;
        this.timestampMs = System.currentTimeMillis();
        this.metrics = new HashMap<>(metrics); // Defensive copy
        this.detectedCapabilities = detectCapabilities(metrics);
    }

    /**
     * Create a new generic metrics snapshot with current timestamp.
     * 
     * @param domain the operation domain
     * @param operation the operation name
     */
    public GenericMetricsSnapshot(String domain, String operation) {
        this(domain, operation, new HashMap<>());
    }

    // ===== Core Getters =====

    /**
     * Get the operation domain.
     * 
     * @return the domain
     */
    public String getDomain() {
        return domain;
    }

    /**
     * Get the operation name.
     * 
     * @return the operation
     */
    public String getOperation() {
        return operation;
    }

    /**
     * Get the timestamp when this snapshot was created.
     * 
     * @return timestamp in milliseconds since epoch
     */
    @Override
    public long getTimestampMs() {
        return timestampMs;
    }

    /**
     * Get the timestamp as an Instant.
     * 
     * @return the timestamp as Instant
     */
    public Instant getTimestamp() {
        return Instant.ofEpochMilli(timestampMs);
    }

    /**
     * Get all metrics as an unmodifiable map.
     * 
     * @return unmodifiable map of metrics
     */
    public Map<String, Object> getMetrics() {
        return Map.copyOf(metrics);
    }

    /**
     * Get the number of metrics in this snapshot.
     * 
     * @return the number of metrics
     */
    public int getMetricCount() {
        return metrics.size();
    }

    // ===== Type-Safe Getters =====

    /**
     * Get a specific metric value.
     * 
     * @param key the metric key
     * @return the metric value, or null if not found
     */
    public Object getMetric(String key) {
        return metrics.get(key);
    }

    /**
     * Get a specific metric value as a long.
     * 
     * @param key the metric key
     * @return the metric value as long, or 0L if not found or not a number
     */
    public long getLong(String key) {
        Object value = metrics.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return 0L;
    }

    /**
     * Get a specific metric value as a double.
     * 
     * @param key the metric key
     * @return the metric value as double, or 0.0 if not found or not a number
     */
    public double getDouble(String key) {
        Object value = metrics.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return 0.0;
    }

    /**
     * Get a specific metric value as a string.
     * 
     * @param key the metric key
     * @return the metric value as string, or null if not found
     */
    public String getString(String key) {
        Object value = metrics.get(key);
        return value != null ? value.toString() : null;
    }

    // ===== Type-Safe Getters with Defaults =====

    /**
     * Get a long value with default.
     * 
     * @param key the metric key
     * @param defaultValue the default value if not found or not a number
     * @return the metric value as long, or defaultValue
     */
    public long getLong(String key, long defaultValue) {
        Object value = metrics.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return defaultValue;
    }

    /**
     * Get a double value with default.
     * 
     * @param key the metric key
     * @param defaultValue the default value if not found or not a number
     * @return the metric value as double, or defaultValue
     */
    public double getDouble(String key, double defaultValue) {
        Object value = metrics.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return defaultValue;
    }

    /**
     * Get a string value with default.
     * 
     * @param key the metric key
     * @param defaultValue the default value if not found
     * @return the metric value as string, or defaultValue
     */
    public String getString(String key, String defaultValue) {
        Object value = metrics.get(key);
        return value != null ? value.toString() : defaultValue;
    }

    /**
     * Get a boolean value with default.
     * 
     * @param key the metric key
     * @param defaultValue the default value if not found
     * @return the metric value as boolean, or defaultValue
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        Object value = metrics.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return defaultValue;
    }

    // ===== Convenience Methods for Common Metrics =====

    /**
     * Get total count.
     * 
     * @return total count, or 0L if not available
     */
    public long getTotal() {
        return getLong("total", 0L);
    }

    /**
     * Get success count.
     * 
     * @return success count, or 0L if not available
     */
    public long getSuccess() {
        return getLong("success", 0L);
    }

    /**
     * Get failure count.
     * 
     * @return failure count, or 0L if not available
     */
    public long getFailure() {
        return getLong("failure", 0L);
    }

    /**
     * Calculate success rate as percentage.
     * 
     * @return success rate percentage (0.0-100.0), or 0.0 if no data
     */
    public double getSuccessRate() {
        long total = getTotal();
        return total > 0 ? (getSuccess() * 100.0) / total : 0.0;
    }

    /**
     * Get total duration in nanoseconds.
     * 
     * @return total duration in nanoseconds, or 0L if not available
     */
    public long getTotalDurationNanos() {
        return getLong("totalDurationNanos", 0L);
    }

    /**
     * Calculate average duration in milliseconds.
     * 
     * @return average duration in milliseconds, or 0.0 if no data
     */
    public double getAverageMs() {
        long total = getTotal();
        return total > 0 ? getTotalDurationNanos() / (total * 1_000_000.0) : 0.0;
    }

    /**
     * Get tokens count.
     * 
     * @return tokens count, or 0L if not available
     */
    public long getTokens() {
        return getLong("tokens", 0L);
    }

    /**
     * Get cost.
     * 
     * @return cost, or 0.0 if not available
     */
    public double getCost() {
        return getDouble("cost", 0.0);
    }

    /**
     * Get throughput.
     * 
     * @return throughput, or 0.0 if not available
     */
    public double getThroughput() {
        return getDouble("throughput", 0.0);
    }

    // ===== Capability Detection =====

    /**
     * Check if this snapshot has counts metrics.
     * 
     * @return true if counts data is available
     */
    public boolean hasCounts() {
        return metrics.containsKey("total");
    }

    /**
     * Check if this snapshot has latency metrics.
     * 
     * @return true if latency data is available
     */
    public boolean hasLatency() {
        return metrics.containsKey("totalDurationNanos");
    }

    /**
     * Check if this snapshot has model metrics.
     * 
     * @return true if model data is available
     */
    public boolean hasModelMetrics() {
        return metrics.containsKey("tokens");
    }

    /**
     * Check if this snapshot has health metrics.
     * 
     * @return true if health data is available
     */
    public boolean hasHealthMetrics() {
        return metrics.containsKey("healthStatus");
    }

    /**
     * Check if this snapshot has integration metrics.
     * 
     * @return true if integration data is available
     */
    public boolean hasIntegrationMetrics() {
        return metrics.containsKey("integrationSuccessRate");
    }

    /**
     * Check if a specific metric exists.
     * 
     * @param key the metric key
     * @return true if the metric exists
     */
    public boolean hasMetric(String key) {
        return metrics.containsKey(key);
    }

    // ===== Dynamic Capability Detection =====

    /**
     * Dynamic capability checking.
     * 
     * @param capability the capability class to check
     * @return true if the capability is supported
     */
    public boolean hasCapability(Class<?> capability) {
        return detectedCapabilities.contains(capability);
    }

    /**
     * Safe capability casting.
     * 
     * @param <T> the capability type
     * @param capability the capability class
     * @return this snapshot cast to the capability type
     * @throws IllegalArgumentException if the capability is not supported
     */
    @SuppressWarnings("unchecked")
    public <T> T as(Class<T> capability) {
        if (hasCapability(capability)) {
            return (T) this;
        }
        throw new IllegalArgumentException("Snapshot does not support capability: " + capability);
    }

    /**
     * Get the set of capability classes this snapshot supports.
     * 
     * @return set of capability classes
     */
    public java.util.Set<Class<?>> getCapabilities() {
        return new java.util.HashSet<>(detectedCapabilities);
    }


    /**
     * Automatic capability detection.
     * 
     * @param metrics the metrics map
     * @return set of detected capability classes
     */
    private Set<Class<?>> detectCapabilities(Map<String, Object> metrics) {
        Set<Class<?>> capabilities = new HashSet<>();
        
        if (metrics.containsKey("total") && metrics.containsKey("success") && metrics.containsKey("failure")) {
            capabilities.add(CountsMetrics.class);
        }
        
        if (metrics.containsKey("totalDurationNanos")) {
            capabilities.add(LatencyMetrics.class);
        }
        
        if (metrics.containsKey("tokens") && metrics.containsKey("cost")) {
            capabilities.add(ModelMetrics.class);
        }
        
        return capabilities;
    }

    // ===== Builder Pattern =====

    /**
     * Create a builder for constructing GenericMetricsSnapshot instances.
     * 
     * @param domain the operation domain
     * @param operation the operation name
     * @return a new builder instance
     */
    public static Builder builder(String domain, String operation) {
        return new Builder(domain, operation);
    }

    /**
     * Builder for GenericMetricsSnapshot.
     */
    public static final class Builder {
        private final String domain;
        private final String operation;
        private final Map<String, Object> metrics = new HashMap<>();

        private Builder(String domain, String operation) {
            this.domain = domain;
            this.operation = operation;
        }

        /**
         * Add a metric.
         * 
         * @param key the metric key
         * @param value the metric value
         * @return this builder
         */
        public Builder withMetric(String key, Object value) {
            metrics.put(key, value);
            return this;
        }

        /**
         * Add multiple metrics.
         * 
         * @param metrics the metrics to add
         * @return this builder
         */
        public Builder withMetrics(Map<String, Object> metrics) {
            this.metrics.putAll(metrics);
            return this;
        }

        /**
         * Add counts metrics.
         * 
         * @param total total count
         * @param success success count
         * @return this builder
         */
        public Builder withCounts(long total, long success) {
            metrics.put("total", total);
            metrics.put("success", success);
            metrics.put("failure", Math.max(0, total - success));
            return this;
        }

        /**
         * Add latency metrics.
         * 
         * @param totalDurationNanos total duration in nanoseconds
         * @return this builder
         */
        public Builder withLatency(long totalDurationNanos) {
            metrics.put("totalDurationNanos", totalDurationNanos);
            return this;
        }

        /**
         * Add model metrics.
         * 
         * @param tokens token count
         * @param cost cost
         * @return this builder
         */
        public Builder withModelMetrics(long tokens, double cost) {
            metrics.put("tokens", tokens);
            metrics.put("cost", cost);
            return this;
        }

        /**
         * Add health metrics.
         * 
         * @param healthStatus health status
         * @param lastCheckTime last check time
         * @return this builder
         */
        public Builder withHealthMetrics(String healthStatus, long lastCheckTime) {
            metrics.put("healthStatus", healthStatus);
            metrics.put("lastCheckTime", lastCheckTime);
            return this;
        }

        /**
         * Add integration metrics.
         * 
         * @param integrationSuccessRate integration success rate
         * @param lastIntegrationTime last integration time
         * @return this builder
         */
        public Builder withIntegrationMetrics(double integrationSuccessRate, long lastIntegrationTime) {
            metrics.put("integrationSuccessRate", integrationSuccessRate);
            metrics.put("lastIntegrationTime", lastIntegrationTime);
            return this;
        }

        /**
         * Build the GenericMetricsSnapshot.
         * 
         * @return the new snapshot instance
         */
        public GenericMetricsSnapshot build() {
            return new GenericMetricsSnapshot(domain, operation, metrics);
        }
    }

    // ===== Object Methods =====

    @Override
    public String toString() {
        return String.format("GenericMetricsSnapshot{domain='%s', operation='%s', timestamp=%d, metrics=%s, capabilities=%s}", 
                domain, operation, timestampMs, metrics, detectedCapabilities);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        GenericMetricsSnapshot other = (GenericMetricsSnapshot) obj;
        return timestampMs == other.timestampMs && 
               domain.equals(other.domain) && 
               operation.equals(other.operation) && 
               metrics.equals(other.metrics);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(domain, operation, timestampMs, metrics);
    }
}
