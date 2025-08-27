package org.openhab.core.ai.common.monitoring.api;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Capability interface for statistics monitoring metrics.
 * 
 * <p>
 * This interface provides functionality for statistics monitoring, including counts,
 * aggregations, trends, and historical data analysis. It replaces the Monitoring
 * hierarchy's Statistics interface with a capability-based approach.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface StatisticsMetrics {

    /**
     * Get the total count of items or events.
     * 
     * @return the total count
     */
    long totalCount();

    /**
     * Get the count of successful items or events.
     * 
     * @return the successful count
     */
    long successCount();

    /**
     * Get the count of failed items or events.
     * 
     * @return the failed count
     */
    long failureCount();

    /**
     * Get the timestamp when statistics collection started.
     * 
     * @return the start timestamp, or null if not tracked
     */
    @Nullable
    Instant collectionStartTime();

    /**
     * Get the timestamp when statistics collection ended.
     * 
     * @return the end timestamp, or null if collection is ongoing
     */
    @Nullable
    Instant collectionEndTime();

    /**
     * Get the duration of the statistics collection period in milliseconds.
     * 
     * @return the collection duration, or 0 if not applicable
     */
    default long collectionDurationMs() {
        Instant start = collectionStartTime();
        Instant end = collectionEndTime();
        if (start == null || end == null) {
            return 0;
        }
        return end.toEpochMilli() - start.toEpochMilli();
    }



    /**
     * Get the throughput (items per second).
     * 
     * @return throughput rate, or 0 if no data available
     */
    default double throughputPerSecond() {
        long durationMs = collectionDurationMs();
        if (durationMs == 0) {
            return 0.0;
        }
        return (double) totalCount() / (durationMs / 1000.0);
    }

    /**
     * Check if statistics collection is ongoing.
     * 
     * @return true if collection is ongoing, false if completed
     */
    default boolean isCollectionOngoing() {
        return collectionEndTime() == null;
    }

    /**
     * Get the collection period as a human-readable string.
     * 
     * @return collection period description, or null if not available
     */
    @Nullable
    default String collectionPeriod() {
        Instant start = collectionStartTime();
        Instant end = collectionEndTime();
        if (start == null) {
            return null;
        }
        if (end == null) {
            return "Started at " + start.toString();
        }
        return start.toString() + " to " + end.toString();
    }
}
