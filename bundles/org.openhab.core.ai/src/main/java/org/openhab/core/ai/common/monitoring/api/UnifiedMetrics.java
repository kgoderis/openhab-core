package org.openhab.core.ai.common.monitoring.api;

import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Unified metrics interface that combines all monitoring capabilities.
 * 
 * <p>
 * This interface provides a comprehensive replacement for the Monitoring hierarchy,
 * combining all capability interfaces into a single, unified approach. It serves
 * as the foundation for the new metrics system that replaces the old Monitoring
 * hierarchy.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface UnifiedMetrics extends 
    MetricsSnapshot,
    CountsMetrics,
    LatencyMetrics,
    HealthMetrics,
    PerformanceMetrics,
    StatisticsMetrics {

    /**
     * Get the unique identifier for this metrics data.
     * 
     * @return the unique identifier
     */
    String getId();

    /**
     * Get the domain this metrics data belongs to.
     * 
     * @return the domain name (e.g., "tool", "agent", "reasoning")
     */
    String getDomain();

    /**
     * Get the operation or component name.
     * 
     * @return the operation/component name
     */
    String getOperation();

    /**
     * Get the source component that generated this metrics data.
     * 
     * @return the source component name, or null if not specified
     */
    @Nullable
    String getSource();

    /**
     * Get the raw metrics data as a map of key-value pairs.
     * 
     * @return the raw data map, or null if no raw data is available
     */
    @Nullable
    Map<String, Object> getRawData();

    /**
     * Get a specific metric value by key.
     * 
     * @param key the metric key
     * @return the metric value, or null if not found
     */
    @Nullable
    default Object getMetric(String key) {
        Map<String, Object> data = getRawData();
        return data != null ? data.get(key) : null;
    }

    /**
     * Get a specific metric value as a long.
     * 
     * @param key the metric key
     * @return the metric value as long, or 0 if not found or not a number
     */
    default long getMetricAsLong(String key) {
        Object value = getMetric(key);
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
    default double getMetricAsDouble(String key) {
        Object value = getMetric(key);
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
    @Nullable
    default String getMetricAsString(String key) {
        Object value = getMetric(key);
        return value != null ? value.toString() : null;
    }

    /**
     * Check if this metrics data is valid.
     * 
     * @return true if the data is valid, false otherwise
     */
    default boolean isValid() {
        return getId() != null && !getId().isBlank() && 
               getDomain() != null && !getDomain().isBlank() &&
               getOperation() != null && !getOperation().isBlank();
    }

    /**
     * Get a human-readable description of this metrics data.
     * 
     * @return the description, or null if not available
     */
    @Nullable
    default String getDescription() {
        return String.format("%s.%s metrics for %s", getDomain(), getOperation(), getId());
    }

    /**
     * Get the timestamp when this metrics data was created or collected.
     * 
     * @return the timestamp as Instant
     */
    default Instant getTimestamp() {
        return Instant.ofEpochMilli(getTimestampMs());
    }

    /**
     * Get the metrics type as a string.
     * 
     * @return the metrics type string
     */
    default String getMetricsType() {
        // Determine type based on implemented capabilities
        if (this instanceof HealthMetrics) {
            return "health";
        } else if (this instanceof StatisticsMetrics) {
            return "statistics";
        } else if (this instanceof PerformanceMetrics) {
            return "performance";
        } else {
            return "metrics";
        }
    }

    /**
     * Get a summary of this metrics data.
     * 
     * @return summary string
     */
    default String getSummary() {
        return String.format("%s: %d total, %.1f%% success, %.2fms avg", 
            getDescription(), 
            total(), 
            successRate(), 
            averageMs(total()));
    }
}
