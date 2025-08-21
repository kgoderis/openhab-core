package org.openhab.core.ai.common.monitoring.api;

import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Interface for statistics monitoring data in the openHAB AI system.
 * 
 * <p>
 * This interface extends MonitoringData to provide specialized methods for
 * statistics-related monitoring, focusing on counts, aggregations, trends,
 * and historical data analysis.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface Statistics extends Monitoring {

    /**
     * Get the total count of items or events.
     * 
     * @return the total count
     */
    long getTotalCount();

    /**
     * Get the count of successful items or events.
     * 
     * @return the successful count
     */
    long getSuccessCount();

    /**
     * Get the count of failed items or events.
     * 
     * @return the failed count
     */
    long getFailureCount();

    /**
     * Get the timestamp when statistics collection started.
     * 
     * @return the start timestamp, or null if not tracked
     */
    @Nullable
    Instant getCollectionStartTime();

    /**
     * Get the timestamp when statistics collection ended.
     * 
     * @return the end timestamp, or null if collection is ongoing
     */
    @Nullable
    Instant getCollectionEndTime();

    /**
     * Get the duration of the statistics collection period in milliseconds.
     * 
     * @return the collection duration, or 0 if not applicable
     */
    default long getCollectionDurationMs() {
        Instant start = getCollectionStartTime();
        Instant end = getCollectionEndTime();
        if (start == null || end == null) {
            return 0;
        }
        return end.toEpochMilli() - start.toEpochMilli();
    }

    /**
     * Get the success rate as a percentage.
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
     * Get the failure rate as a percentage.
     * 
     * @return failure rate (0.0 to 100.0)
     */
    default double getFailureRate() {
        long total = getTotalCount();
        if (total == 0) {
            return 0.0;
        }
        return (double) getFailureCount() / total * 100.0;
    }

    /**
     * Get the items per second rate.
     * 
     * @return items per second
     */
    default double getItemsPerSecond() {
        long duration = getCollectionDurationMs();
        if (duration == 0) {
            return 0.0;
        }
        return (double) getTotalCount() / (duration / 1000.0);
    }

    /**
     * Get additional statistical measures.
     * 
     * @return map of additional statistical measures, or null if none available
     */
    @Nullable
    default Map<String, Double> getAdditionalMeasures() {
        return null;
    }

    /**
     * Get a specific statistical measure by name.
     * 
     * @param measureName the name of the measure
     * @return the measure value, or null if not found
     */
    @Nullable
    default Double getMeasure(String measureName) {
        Map<String, Double> measures = getAdditionalMeasures();
        return measures != null ? measures.get(measureName) : null;
    }

    @Override
    default MonitoringType getType() {
        return MonitoringType.STATISTICS;
    }
}
