package org.openhab.core.ai.common.statistics;

import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Base interface for all statistics in the openHAB AI system.
 * 
 * <p>
 * This interface provides a unified contract for all statistics objects,
 * ensuring consistent behavior across different statistics domains.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface Statistics {

    /**
     * Get the unique identifier for this statistics object.
     * 
     * @return the statistics identifier
     */
    String getId();

    /**
     * Get the timestamp when these statistics were collected.
     * 
     * @return the collection timestamp
     */
    @Nullable
    Instant getTimestamp();

    /**
     * Get the type of statistics.
     * 
     * @return the statistics type
     */
    StatisticsType getType();

    /**
     * Get all metrics as a map of key-value pairs.
     * 
     * @return map of metric names to values
     */
    Map<String, Object> getMetrics();

    /**
     * Get a specific metric value.
     * 
     * @param key the metric key
     * @return the metric value, or null if not found
     */
    @Nullable
    Object getMetric(String key);

    /**
     * Check if a specific metric exists.
     * 
     * @param key the metric key
     * @return true if the metric exists
     */
    boolean hasMetric(String key);

    /**
     * Get the total count of operations/events tracked.
     * 
     * @return total count, or 0 if not applicable
     */
    default long getTotalCount() {
        Object value = getMetric("totalCount");
        return value instanceof Number ? ((Number) value).longValue() : 0L;
    }

    /**
     * Get the success count of operations/events tracked.
     * 
     * @return success count, or 0 if not applicable
     */
    default long getSuccessCount() {
        Object value = getMetric("successCount");
        return value instanceof Number ? ((Number) value).longValue() : 0L;
    }

    /**
     * Get the failure count of operations/events tracked.
     * 
     * @return failure count, or 0 if not applicable
     */
    default long getFailureCount() {
        Object value = getMetric("failureCount");
        return value instanceof Number ? ((Number) value).longValue() : 0L;
    }

    /**
     * Calculate the success rate as a percentage.
     * 
     * @return success rate (0.0 to 100.0)
     */
    default double getSuccessRate() {
        long total = getTotalCount();
        if (total == 0) {
            return 0.0;
        }
        return (double) getSuccessCount() / total * 100.0;
    }

    /**
     * Check if there are any operations/events recorded.
     * 
     * @return true if there are operations recorded
     */
    default boolean hasOperations() {
        return getTotalCount() > 0;
    }

    /**
     * Check if there are any successful operations/events recorded.
     * 
     * @return true if there are successful operations recorded
     */
    default boolean hasSuccessfulOperations() {
        return getSuccessCount() > 0;
    }

    /**
     * Check if there are any failed operations/events recorded.
     * 
     * @return true if there are failed operations recorded
     */
    default boolean hasFailedOperations() {
        return getFailureCount() > 0;
    }
}
