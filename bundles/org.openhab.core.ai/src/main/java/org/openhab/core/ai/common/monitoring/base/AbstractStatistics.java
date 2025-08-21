package org.openhab.core.ai.common.monitoring.base;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.MonitoringType;
import org.openhab.core.ai.common.monitoring.api.Statistics;
import org.openhab.core.ai.common.monitoring.api.ValidationUtils;

/**
 * Abstract base class for statistics monitoring data implementations.
 * 
 * <p>
 * This class extends AbstractMonitoring and implements StatisticsData
 * to provide common functionality for statistics-related monitoring,
 * focusing on counts, aggregations, trends, and historical data analysis.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public abstract class AbstractStatistics extends AbstractMonitoring implements Statistics {

    private final long totalCount;
    private final long successCount;
    private final long failureCount;
    private final @Nullable Instant collectionStartTime;
    private final @Nullable Instant collectionEndTime;
    private final Map<String, Double> additionalMeasures;

    /**
     * Create a new AbstractStatistics instance.
     * 
     * @param id the unique identifier
     * @param timestamp the timestamp when data was created
     * @param domain the domain this data belongs to
     * @param source the source component, or null if not specified
     * @param description the description, or null if not available
     * @param data the raw data map, or null if no raw data
     * @param totalCount the total count of items or events
     * @param successCount the count of successful items or events
     * @param failureCount the count of failed items or events
     * @param collectionStartTime the timestamp when collection started, or null if not tracked
     * @param collectionEndTime the timestamp when collection ended, or null if collection is ongoing
     * @param additionalMeasures additional statistical measures, or null if none
     */
    protected AbstractStatistics(String id, Instant timestamp, String domain, @Nullable String source,
            @Nullable String description, @Nullable Map<String, Object> data, long totalCount, long successCount,
            long failureCount, @Nullable Instant collectionStartTime, @Nullable Instant collectionEndTime,
            @Nullable Map<String, Double> additionalMeasures) {
        super(id, timestamp, domain, source, description, data);
        this.totalCount = totalCount;
        this.successCount = successCount;
        this.failureCount = failureCount;
        this.collectionStartTime = collectionStartTime;
        this.collectionEndTime = collectionEndTime;
        this.additionalMeasures = additionalMeasures != null ? new HashMap<>(additionalMeasures) : new HashMap<>();

        // Validate monitoring data first, then statistics-specific data
        validateMonitoringData();
        validateStatisticsData();
    }

    @Override
    public long getTotalCount() {
        return totalCount;
    }

    @Override
    public long getSuccessCount() {
        return successCount;
    }

    @Override
    public long getFailureCount() {
        return failureCount;
    }

    @Override
    public @Nullable Instant getCollectionStartTime() {
        return collectionStartTime;
    }

    @Override
    public @Nullable Instant getCollectionEndTime() {
        return collectionEndTime;
    }

    @Override
    public @Nullable Map<String, Double> getAdditionalMeasures() {
        return additionalMeasures.isEmpty() ? null : Map.copyOf(additionalMeasures);
    }

    @Override
    public MonitoringType getType() {
        return MonitoringType.STATISTICS;
    }

    /**
     * Get the raw additional measures map for internal use (mutable).
     * 
     * @return the raw additional measures map
     */
    protected Map<String, Double> getRawAdditionalMeasures() {
        return additionalMeasures;
    }

    /**
     * Add an additional measure to the statistics.
     * 
     * @param measureName the name of the measure
     * @param value the measure value
     */
    protected void addMeasure(String measureName, double value) {
        additionalMeasures.put(Objects.requireNonNull(measureName, "measureName"), value);
    }

    /**
     * Remove an additional measure from the statistics.
     * 
     * @param measureName the name of the measure to remove
     */
    protected void removeMeasure(String measureName) {
        additionalMeasures.remove(measureName);
    }

    /**
     * Clear all additional measures from the statistics.
     */
    protected void clearMeasures() {
        additionalMeasures.clear();
    }

    /**
     * Validate the statistics data for consistency.
     * 
     * @throws IllegalArgumentException if the data is invalid
     */
    protected void validateStatisticsData() {
        ValidationUtils.validateNonNegative(totalCount, "Total count");
        ValidationUtils.validateNonNegative(successCount, "Success count");
        ValidationUtils.validateNonNegative(failureCount, "Failure count");

        // Check that total count equals success + failure
        ValidationUtils.validateOperationCounts(totalCount, successCount, failureCount, "Total count", "Success count",
                "Failure count");

        // Check that collection start time is before end time if both are provided
        ValidationUtils.validateTimeRange(collectionStartTime, collectionEndTime, "Collection start time",
                "Collection end time");
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        AbstractStatistics other = (AbstractStatistics) obj;
        return totalCount == other.totalCount && successCount == other.successCount
                && failureCount == other.failureCount && Objects.equals(collectionStartTime, other.collectionStartTime)
                && Objects.equals(collectionEndTime, other.collectionEndTime)
                && Objects.equals(additionalMeasures, other.additionalMeasures);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (int) (totalCount ^ (totalCount >>> 32));
        result = prime * result + (int) (successCount ^ (successCount >>> 32));
        result = prime * result + (int) (failureCount ^ (failureCount >>> 32));
        result = prime * result + Objects.hashCode(collectionStartTime);
        result = prime * result + Objects.hashCode(collectionEndTime);
        result = prime * result + Objects.hashCode(additionalMeasures);
        return result;
    }

    @Override
    public String toString() {
        return super.toString().replace("}", "") + ", totalCount=" + totalCount + ", successCount=" + successCount
                + ", failureCount=" + failureCount + ", collectionStartTime=" + collectionStartTime
                + ", collectionEndTime=" + collectionEndTime + ", additionalMeasuresSize=" + additionalMeasures.size()
                + '}';
    }
}
