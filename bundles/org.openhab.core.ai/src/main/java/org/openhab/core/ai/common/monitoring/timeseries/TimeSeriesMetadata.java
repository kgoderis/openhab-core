package org.openhab.core.ai.common.monitoring.timeseries;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Metadata information for a time series, including creation time, statistics, and configuration.
 * 
 * <p>
 * TimeSeriesMetadata provides comprehensive information about a time series, including:
 * <ul>
 *   <li><strong>seriesId</strong>: Unique identifier for the time series</li>
 *   <li><strong>createdAt</strong>: When the time series was first created</li>
 *   <li><strong>lastUpdated</strong>: When the time series was last updated</li>
 *   <li><strong>pointCount</strong>: Total number of data points in the series</li>
 *   <li><strong>retentionPeriod</strong>: How long data is retained</li>
 *   <li><strong>aggregationPeriod</strong>: Default aggregation period for this series</li>
 *   <li><strong>tags</strong>: Metadata tags associated with the series</li>
 *   <li><strong>fields</strong>: Field definitions and their types</li>
 * </ul>
 * </p>
 * 
 * <h3>Usage Example</h3>
 * <pre>{@code
 * TimeSeriesMetadata metadata = new TimeSeriesMetadata(
 *     "metrics:model:completion",
 *     Instant.now(),
 *     Instant.now(),
 *     1000L,
 *     Duration.ofDays(30),
 *     Duration.ofHours(1),
 *     Map.of("domain", "model", "operation", "completion"),
 *     Map.of("duration_ms", "Number", "success", "Boolean")
 * );
 * }</pre>
 * 
 * <h3>Design Principles</h3>
 * <ul>
 *   <li><strong>Immutability</strong>: All data is immutable to ensure thread safety</li>
 *   <li><strong>Comprehensive</strong>: Provides all necessary information for series management</li>
 *   <li><strong>Extensible</strong>: Supports additional metadata through tags and fields</li>
 *   <li><strong>Performance</strong>: Optimized for frequent access and updates</li>
 * </ul>
 * 
 * @param seriesId the unique identifier for the time series
 * @param createdAt when the time series was first created
 * @param lastUpdated when the time series was last updated
 * @param pointCount total number of data points in the series
 * @param retentionPeriod how long data is retained (null means no retention limit)
 * @param aggregationPeriod default aggregation period for this series (null means no default)
 * @param tags metadata tags associated with the series (immutable)
 * @param fields field definitions and their types (immutable)
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record TimeSeriesMetadata(
    String seriesId,
    Instant createdAt,
    Instant lastUpdated,
    long pointCount,
    Duration retentionPeriod,
    Duration aggregationPeriod,
    Map<String, String> tags,
    Map<String, String> fields
) {

    /**
     * Creates a new TimeSeriesMetadata with the specified parameters.
     * 
     * <p>
     * The tags and fields maps are copied to ensure immutability. Null values
     * are not allowed for seriesId, createdAt, or lastUpdated.
     * </p>
     * 
     * @param seriesId the unique identifier for the time series (must not be null or empty)
     * @param createdAt when the time series was first created (must not be null)
     * @param lastUpdated when the time series was last updated (must not be null)
     * @param pointCount total number of data points in the series (must not be negative)
     * @param retentionPeriod how long data is retained (can be null)
     * @param aggregationPeriod default aggregation period for this series (can be null)
     * @param tags metadata tags associated with the series (can be empty, will be copied)
     * @param fields field definitions and their types (can be empty, will be copied)
     * @throws IllegalArgumentException if seriesId is null or empty, or if pointCount is negative
     */
    public TimeSeriesMetadata {
        Objects.requireNonNull(seriesId, "seriesId must not be null");
        if (seriesId.trim().isEmpty()) {
            throw new IllegalArgumentException("seriesId must not be empty");
        }
        
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        Objects.requireNonNull(lastUpdated, "lastUpdated must not be null");
        
        if (pointCount < 0) {
            throw new IllegalArgumentException("pointCount must not be negative");
        }
        
        // Create defensive copies to ensure immutability
        tags = tags != null ? Map.copyOf(tags) : Map.of();
        fields = fields != null ? Map.copyOf(fields) : Map.of();
    }

    /**
     * Gets the age of the time series (time since creation).
     * 
     * @return the duration since the time series was created
     */
    public Duration getAge() {
        return Duration.between(createdAt, Instant.now());
    }

    /**
     * Gets the time since the last update.
     * 
     * @return the duration since the last update
     */
    public Duration getTimeSinceLastUpdate() {
        return Duration.between(lastUpdated, Instant.now());
    }

    /**
     * Checks if the time series has a retention period configured.
     * 
     * @return true if retention period is configured, false otherwise
     */
    public boolean hasRetentionPeriod() {
        return retentionPeriod != null;
    }

    /**
     * Checks if the time series has a default aggregation period configured.
     * 
     * @return true if aggregation period is configured, false otherwise
     */
    public boolean hasAggregationPeriod() {
        return aggregationPeriod != null;
    }

    /**
     * Gets the value of a specific tag.
     * 
     * @param tagName the name of the tag
     * @return the tag value, or null if the tag doesn't exist
     */
    public String getTag(String tagName) {
        return tags.get(tagName);
    }

    /**
     * Checks if the time series has a specific tag.
     * 
     * @param tagName the name of the tag
     * @return true if the tag exists, false otherwise
     */
    public boolean hasTag(String tagName) {
        return tags.containsKey(tagName);
    }

    /**
     * Gets the type of a specific field.
     * 
     * @param fieldName the name of the field
     * @return the field type, or null if the field doesn't exist
     */
    public String getFieldType(String fieldName) {
        return fields.get(fieldName);
    }

    /**
     * Checks if the time series has a specific field defined.
     * 
     * @param fieldName the name of the field
     * @return true if the field is defined, false otherwise
     */
    public boolean hasField(String fieldName) {
        return fields.containsKey(fieldName);
    }

    /**
     * Gets the number of tags in this metadata.
     * 
     * @return the number of tags
     */
    public int getTagCount() {
        return tags.size();
    }

    /**
     * Gets the number of fields defined in this metadata.
     * 
     * @return the number of fields
     */
    public int getFieldCount() {
        return fields.size();
    }

    /**
     * Checks if the time series is empty (no data points).
     * 
     * @return true if the series has no data points, false otherwise
     */
    public boolean isEmpty() {
        return pointCount == 0;
    }

    /**
     * Checks if the time series is active (recently updated).
     * 
     * <p>
     * A time series is considered active if it has been updated within the last hour.
     * </p>
     * 
     * @return true if the series is active, false otherwise
     */
    public boolean isActive() {
        return getTimeSinceLastUpdate().toHours() < 1;
    }

    /**
     * Checks if the time series is stale (not updated recently).
     * 
     * <p>
     * A time series is considered stale if it hasn't been updated within the last 24 hours.
     * </p>
     * 
     * @return true if the series is stale, false otherwise
     */
    public boolean isStale() {
        return getTimeSinceLastUpdate().toHours() >= 24;
    }

    /**
     * Creates a new TimeSeriesMetadata with updated point count and last updated time.
     * 
     * @param newPointCount the new point count
     * @param newLastUpdated the new last updated time
     * @return a new TimeSeriesMetadata with the updated values
     */
    public TimeSeriesMetadata withUpdate(long newPointCount, Instant newLastUpdated) {
        return new TimeSeriesMetadata(
            seriesId,
            createdAt,
            newLastUpdated,
            newPointCount,
            retentionPeriod,
            aggregationPeriod,
            tags,
            fields
        );
    }

    /**
     * Creates a new TimeSeriesMetadata with additional tags.
     * 
     * @param additionalTags additional tags to add
     * @return a new TimeSeriesMetadata with the additional tags
     */
    public TimeSeriesMetadata withAdditionalTags(Map<String, String> additionalTags) {
        if (additionalTags == null || additionalTags.isEmpty()) {
            return this;
        }
        
        Map<String, String> newTags = new java.util.HashMap<>(tags);
        newTags.putAll(additionalTags);
        return new TimeSeriesMetadata(
            seriesId,
            createdAt,
            lastUpdated,
            pointCount,
            retentionPeriod,
            aggregationPeriod,
            newTags,
            fields
        );
    }

    /**
     * Creates a new TimeSeriesMetadata with additional field definitions.
     * 
     * @param additionalFields additional field definitions to add
     * @return a new TimeSeriesMetadata with the additional fields
     */
    public TimeSeriesMetadata withAdditionalFields(Map<String, String> additionalFields) {
        if (additionalFields == null || additionalFields.isEmpty()) {
            return this;
        }
        
        Map<String, String> newFields = new java.util.HashMap<>(fields);
        newFields.putAll(additionalFields);
        return new TimeSeriesMetadata(
            seriesId,
            createdAt,
            lastUpdated,
            pointCount,
            retentionPeriod,
            aggregationPeriod,
            tags,
            newFields
        );
    }

    /**
     * Returns a string representation of this TimeSeriesMetadata.
     * 
     * <p>
     * The string includes the series ID, point count, age, and tag/field counts
     * for debugging purposes.
     * </p>
     * 
     * @return string representation of this TimeSeriesMetadata
     */
    @Override
    public String toString() {
        return String.format("TimeSeriesMetadata{id=%s, points=%d, age=%s, tags=%d, fields=%d}", 
                           seriesId, pointCount, getAge(), tags.size(), fields.size());
    }
}
