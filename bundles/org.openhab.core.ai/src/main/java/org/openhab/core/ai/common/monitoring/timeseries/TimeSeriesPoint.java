package org.openhab.core.ai.common.monitoring.timeseries;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Represents a single point in a time series with timestamp, tags, and fields.
 * 
 * <p>
 * A TimeSeriesPoint contains:
 * <ul>
 *   <li><strong>timestamp</strong>: The exact time when this data point was recorded</li>
 *   <li><strong>tags</strong>: Immutable metadata for indexing and filtering (e.g., "domain", "operation")</li>
 *   <li><strong>fields</strong>: The actual metric values (e.g., "duration_ms", "success", "count")</li>
 * </ul>
 * </p>
 * 
 * <h3>Usage Example</h3>
 * <pre>{@code
 * Map<String, String> tags = Map.of("domain", "model", "operation", "completion");
 * Map<String, Object> fields = Map.of("duration_ms", 150, "success", true, "tokens", 1024);
 * TimeSeriesPoint point = new TimeSeriesPoint(Instant.now(), tags, fields);
 * }</pre>
 * 
 * <h3>Design Principles</h3>
 * <ul>
 *   <li><strong>Immutability</strong>: All data is immutable to ensure thread safety</li>
 *   <li><strong>Type Safety</strong>: Fields can contain any Object type for flexibility</li>
 *   <li><strong>Performance</strong>: Optimized for high-frequency time series operations</li>
 *   <li><strong>Serialization</strong>: JSON-friendly for storage and transmission</li>
 * </ul>
 * 
 * @param timestamp the timestamp when this data point was recorded
 * @param tags metadata tags for indexing and filtering (immutable)
 * @param fields the actual metric values (immutable)
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record TimeSeriesPoint(
    Instant timestamp,
    Map<String, String> tags,
    Map<String, Object> fields,
    @Nullable String snapshotType,  // NEW: Type of snapshot stored
    @Nullable String snapshotData   // NEW: Serialized snapshot data
) {

    /**
     * Creates a new TimeSeriesPoint with the specified timestamp, tags, and fields.
     * 
     * <p>
     * The tags and fields maps are copied to ensure immutability. Null values
     * are not allowed for timestamp, and empty maps are allowed for tags and fields.
     * </p>
     * 
     * @param timestamp the timestamp when this data point was recorded (must not be null)
     * @param tags metadata tags for indexing and filtering (can be empty, will be copied)
     * @param fields the actual metric values (can be empty, will be copied)
     * @param snapshotType the type of snapshot stored (can be null for backward compatibility)
     * @param snapshotData the serialized snapshot data (can be null for backward compatibility)
     * @throws IllegalArgumentException if timestamp is null or snapshot data is inconsistent
     */
    public TimeSeriesPoint {
        Objects.requireNonNull(timestamp, "timestamp must not be null");
        
        // Create defensive copies to ensure immutability
        tags = tags != null ? Map.copyOf(tags) : Map.of();
        fields = fields != null ? Map.copyOf(fields) : Map.of();
        
        // Validate snapshot data if provided
        if (snapshotType != null && snapshotData == null) {
            throw new IllegalArgumentException("snapshotData must not be null when snapshotType is provided");
        }
        if (snapshotType == null && snapshotData != null) {
            throw new IllegalArgumentException("snapshotType must not be null when snapshotData is provided");
        }
    }
    
    /**
     * Creates a new TimeSeriesPoint with the specified timestamp, tags, and fields.
     * This constructor provides backward compatibility for existing code.
     * 
     * @param timestamp the timestamp when this data point was recorded (must not be null)
     * @param tags metadata tags for indexing and filtering (can be empty, will be copied)
     * @param fields the actual metric values (can be empty, will be copied)
     * @throws IllegalArgumentException if timestamp is null
     */
    public TimeSeriesPoint(Instant timestamp, Map<String, String> tags, Map<String, Object> fields) {
        this(timestamp, tags, fields, null, null);
    }

    /**
     * Gets the value of a specific field as a String.
     * 
     * @param fieldName the name of the field
     * @return the field value as a String, or null if the field doesn't exist
     */
    public String getFieldAsString(String fieldName) {
        Object value = fields.get(fieldName);
        return value != null ? value.toString() : null;
    }

    /**
     * Gets the value of a specific field as a Number.
     * 
     * @param fieldName the name of the field
     * @return the field value as a Number, or null if the field doesn't exist or isn't a Number
     */
    public Number getFieldAsNumber(String fieldName) {
        Object value = fields.get(fieldName);
        return value instanceof Number ? (Number) value : null;
    }

    /**
     * Gets the value of a specific field as a Boolean.
     * 
     * @param fieldName the name of the field
     * @return the field value as a Boolean, or null if the field doesn't exist or isn't a Boolean
     */
    public Boolean getFieldAsBoolean(String fieldName) {
        Object value = fields.get(fieldName);
        return value instanceof Boolean ? (Boolean) value : null;
    }

    /**
     * Checks if this point has a specific tag.
     * 
     * @param tagName the name of the tag
     * @return true if the tag exists, false otherwise
     */
    public boolean hasTag(String tagName) {
        return tags.containsKey(tagName);
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
     * Checks if this point has a specific field.
     * 
     * @param fieldName the name of the field
     * @return true if the field exists, false otherwise
     */
    public boolean hasField(String fieldName) {
        return fields.containsKey(fieldName);
    }

    /**
     * Gets the number of tags in this point.
     * 
     * @return the number of tags
     */
    public int getTagCount() {
        return tags.size();
    }

    /**
     * Gets the number of fields in this point.
     * 
     * @return the number of fields
     */
    public int getFieldCount() {
        return fields.size();
    }

    /**
     * Creates a new TimeSeriesPoint with additional tags.
     * 
     * <p>
     * This method creates a new TimeSeriesPoint with the existing tags plus
     * the additional tags. If a tag with the same name already exists, it
     * will be overwritten with the new value.
     * </p>
     * 
     * @param additionalTags additional tags to add
     * @return a new TimeSeriesPoint with the additional tags
     */
    public TimeSeriesPoint withAdditionalTags(Map<String, String> additionalTags) {
        if (additionalTags == null || additionalTags.isEmpty()) {
            return this;
        }
        
        Map<String, String> newTags = new java.util.HashMap<>(tags);
        newTags.putAll(additionalTags);
        return new TimeSeriesPoint(timestamp, newTags, fields);
    }

    /**
     * Creates a new TimeSeriesPoint with additional fields.
     * 
     * <p>
     * This method creates a new TimeSeriesPoint with the existing fields plus
     * the additional fields. If a field with the same name already exists, it
     * will be overwritten with the new value.
     * </p>
     * 
     * @param additionalFields additional fields to add
     * @return a new TimeSeriesPoint with the additional fields
     */
    public TimeSeriesPoint withAdditionalFields(Map<String, Object> additionalFields) {
        if (additionalFields == null || additionalFields.isEmpty()) {
            return this;
        }
        
        Map<String, Object> newFields = new java.util.HashMap<>(fields);
        newFields.putAll(additionalFields);
        return new TimeSeriesPoint(timestamp, tags, newFields);
    }

    /**
     * Returns a string representation of this TimeSeriesPoint.
     * 
     * <p>
     * The string includes the timestamp, tag count, and field count for
     * debugging purposes. The actual tag and field values are not included
     * to avoid potentially large output.
     * </p>
     * 
     * @return string representation of this TimeSeriesPoint
     */
    /**
     * Creates a new TimeSeriesPoint with snapshot data.
     * 
     * @param snapshotType the type of snapshot stored
     * @param snapshotData the serialized snapshot data
     * @return a new TimeSeriesPoint with the snapshot data
     * @throws IllegalArgumentException if snapshot data is inconsistent
     */
    public TimeSeriesPoint withSnapshotData(String snapshotType, String snapshotData) {
        return new TimeSeriesPoint(timestamp, tags, fields, snapshotType, snapshotData);
    }
    
    /**
     * Checks if this TimeSeriesPoint contains snapshot data.
     * 
     * @return true if this point contains snapshot data, false otherwise
     */
    public boolean hasSnapshotData() {
        return snapshotType != null && snapshotData != null;
    }
    
    /**
     * Gets the snapshot type if available.
     * 
     * @return the snapshot type, or null if no snapshot data is present
     */
    public @Nullable String getSnapshotType() {
        return snapshotType;
    }
    
    /**
     * Gets the snapshot data if available.
     * 
     * @return the snapshot data, or null if no snapshot data is present
     */
    public @Nullable String getSnapshotData() {
        return snapshotData;
    }
    
    /**
     * Validates the snapshot data consistency.
     * 
     * @return true if snapshot data is consistent, false otherwise
     */
    public boolean isSnapshotDataValid() {
        if (snapshotType == null && snapshotData == null) {
            return true; // No snapshot data is valid
        }
        return snapshotType != null && snapshotData != null;
    }
    
    @Override
    public String toString() {
        return String.format("TimeSeriesPoint{timestamp=%s, tags=%d, fields=%d, hasSnapshot=%s}", 
                           timestamp, tags.size(), fields.size(), hasSnapshotData());
    }
}
