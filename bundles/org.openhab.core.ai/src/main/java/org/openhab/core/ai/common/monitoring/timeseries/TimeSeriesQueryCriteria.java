package org.openhab.core.ai.common.monitoring.timeseries;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Query criteria for complex time series queries with filtering, pagination, and sorting options.
 * 
 * <p>
 * TimeSeriesQueryCriteria provides comprehensive query capabilities for time series data, including:
 * <ul>
 *   <li><strong>seriesId</strong>: The time series to query (required)</li>
 *   <li><strong>startTime</strong>: Start time for the query (inclusive)</li>
 *   <li><strong>endTime</strong>: End time for the query (inclusive)</li>
 *   <li><strong>tagFilters</strong>: Filter by tag values</li>
 *   <li><strong>fieldFilters</strong>: Filter by field values</li>
 *   <li><strong>limit</strong>: Maximum number of results to return</li>
 *   <li><strong>offset</strong>: Number of results to skip</li>
 *   <li><strong>sortOrder</strong>: Sort order for results</li>
 *   <li><strong>aggregationFunction</strong>: Aggregation function to apply</li>
 *   <li><strong>aggregationPeriod</strong>: Time period for aggregation</li>
 * </ul>
 * </p>
 * 
 * <h3>Usage Example</h3>
 * <pre>{@code
 * TimeSeriesQueryCriteria criteria = TimeSeriesQueryCriteria.builder("metrics:model:completion")
 *     .withTimeRange(startTime, endTime)
 *     .withTagFilter("domain", "model")
 *     .withFieldFilter("success", true)
 *     .withLimit(100)
 *     .withSortOrder(SortOrder.DESCENDING)
 *     .build();
 * }</pre>
 * 
 * <h3>Design Principles</h3>
 * <ul>
 *   <li><strong>Immutability</strong>: All data is immutable to ensure thread safety</li>
 *   <li><strong>Flexibility</strong>: Supports complex filtering and querying scenarios</li>
 *   <li><strong>Performance</strong>: Optimized for efficient query execution</li>
 *   <li><strong>Builder Pattern</strong>: Fluent API for easy construction</li>
 * </ul>
 * 
 * @param seriesId the time series identifier to query (required)
 * @param startTime the start time for the query (inclusive, can be null)
 * @param endTime the end time for the query (inclusive, can be null)
 * @param tagFilters filter criteria for tags (immutable)
 * @param fieldFilters filter criteria for fields (immutable)
 * @param limit maximum number of results to return (null means no limit)
 * @param offset number of results to skip (null means no offset)
 * @param sortOrder sort order for results (can be null)
 * @param aggregationFunction aggregation function to apply (can be null)
 * @param aggregationPeriod time period for aggregation (can be null)
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record TimeSeriesQueryCriteria(
    String seriesId,
    Instant startTime,
    Instant endTime,
    Map<String, String> tagFilters,
    Map<String, Object> fieldFilters,
    Integer limit,
    Integer offset,
    SortOrder sortOrder,
    String aggregationFunction,
    Duration aggregationPeriod
) {

    /**
     * Sort order enumeration for query results.
     */
    public enum SortOrder {
        /**
         * Ascending order (oldest first).
         */
        ASCENDING,
        
        /**
         * Descending order (newest first).
         */
        DESCENDING
    }

    /**
     * Creates a new TimeSeriesQueryCriteria with the specified parameters.
     * 
     * <p>
     * The tagFilters and fieldFilters maps are copied to ensure immutability.
     * The seriesId is required and cannot be null or empty.
     * </p>
     * 
     * @param seriesId the time series identifier to query (must not be null or empty)
     * @param startTime the start time for the query (inclusive, can be null)
     * @param endTime the end time for the query (inclusive, can be null)
     * @param tagFilters filter criteria for tags (can be empty, will be copied)
     * @param fieldFilters filter criteria for fields (can be empty, will be copied)
     * @param limit maximum number of results to return (can be null)
     * @param offset number of results to skip (can be null)
     * @param sortOrder sort order for results (can be null)
     * @param aggregationFunction aggregation function to apply (can be null)
     * @param aggregationPeriod time period for aggregation (can be null)
     * @throws IllegalArgumentException if seriesId is null or empty
     */
    public TimeSeriesQueryCriteria {
        Objects.requireNonNull(seriesId, "seriesId must not be null");
        if (seriesId.trim().isEmpty()) {
            throw new IllegalArgumentException("seriesId must not be empty");
        }
        
        // Create defensive copies to ensure immutability
        tagFilters = tagFilters != null ? Map.copyOf(tagFilters) : Map.of();
        fieldFilters = fieldFilters != null ? Map.copyOf(fieldFilters) : Map.of();
    }

    /**
     * Creates a new builder for TimeSeriesQueryCriteria.
     * 
     * @param seriesId the time series identifier to query
     * @return a new builder instance
     */
    public static Builder builder(String seriesId) {
        return new Builder(seriesId);
    }

    /**
     * Checks if this query has a time range specified.
     * 
     * @return true if both startTime and endTime are specified, false otherwise
     */
    public boolean hasTimeRange() {
        return startTime != null && endTime != null;
    }

    /**
     * Checks if this query has tag filters.
     * 
     * @return true if tag filters are specified, false otherwise
     */
    public boolean hasTagFilters() {
        return !tagFilters.isEmpty();
    }

    /**
     * Checks if this query has field filters.
     * 
     * @return true if field filters are specified, false otherwise
     */
    public boolean hasFieldFilters() {
        return !fieldFilters.isEmpty();
    }

    /**
     * Checks if this query has pagination (limit or offset).
     * 
     * @return true if limit or offset is specified, false otherwise
     */
    public boolean hasPagination() {
        return limit != null || offset != null;
    }

    /**
     * Checks if this query has aggregation specified.
     * 
     * @return true if both aggregation function and period are specified, false otherwise
     */
    public boolean hasAggregation() {
        return aggregationFunction != null && aggregationPeriod != null;
    }

    /**
     * Gets the duration of the time range.
     * 
     * @return the duration between startTime and endTime, or null if time range is not complete
     */
    public Duration getTimeRangeDuration() {
        if (hasTimeRange()) {
            return Duration.between(startTime, endTime);
        }
        return null;
    }

    /**
     * Gets the value of a specific tag filter.
     * 
     * @param tagName the name of the tag
     * @return the tag filter value, or null if the tag filter doesn't exist
     */
    public String getTagFilter(String tagName) {
        return tagFilters.get(tagName);
    }

    /**
     * Gets the value of a specific field filter.
     * 
     * @param fieldName the name of the field
     * @return the field filter value, or null if the field filter doesn't exist
     */
    public Object getFieldFilter(String fieldName) {
        return fieldFilters.get(fieldName);
    }

    /**
     * Checks if this query has a specific tag filter.
     * 
     * @param tagName the name of the tag
     * @return true if the tag filter exists, false otherwise
     */
    public boolean hasTagFilter(String tagName) {
        return tagFilters.containsKey(tagName);
    }

    /**
     * Checks if this query has a specific field filter.
     * 
     * @param fieldName the name of the field
     * @return true if the field filter exists, false otherwise
     */
    public boolean hasFieldFilter(String fieldName) {
        return fieldFilters.containsKey(fieldName);
    }

    /**
     * Returns a string representation of this TimeSeriesQueryCriteria.
     * 
     * <p>
     * The string includes the series ID, time range, filter counts, and pagination
     * information for debugging purposes.
     * </p>
     * 
     * @return string representation of this TimeSeriesQueryCriteria
     */
    @Override
    public String toString() {
        return String.format("TimeSeriesQueryCriteria{seriesId=%s, timeRange=%s, tagFilters=%d, fieldFilters=%d, limit=%s, offset=%s, sort=%s}", 
                           seriesId, hasTimeRange() ? getTimeRangeDuration() : "none", 
                           tagFilters.size(), fieldFilters.size(), limit, offset, sortOrder);
    }

    /**
     * Builder class for TimeSeriesQueryCriteria using the builder pattern.
     */
    public static final class Builder {
        private final String seriesId;
        private Instant startTime;
        private Instant endTime;
        private final Map<String, String> tagFilters = new java.util.HashMap<>();
        private final Map<String, Object> fieldFilters = new java.util.HashMap<>();
        private Integer limit;
        private Integer offset;
        private SortOrder sortOrder;
        private String aggregationFunction;
        private Duration aggregationPeriod;

        /**
         * Creates a new builder with the specified series ID.
         * 
         * @param seriesId the time series identifier to query
         */
        public Builder(String seriesId) {
            this.seriesId = Objects.requireNonNull(seriesId, "seriesId must not be null");
        }

        /**
         * Sets the time range for the query.
         * 
         * @param startTime the start time (inclusive)
         * @param endTime the end time (inclusive)
         * @return this builder instance
         */
        public Builder withTimeRange(Instant startTime, Instant endTime) {
            this.startTime = startTime;
            this.endTime = endTime;
            return this;
        }

        /**
         * Sets the start time for the query.
         * 
         * @param startTime the start time (inclusive)
         * @return this builder instance
         */
        public Builder withStartTime(Instant startTime) {
            this.startTime = startTime;
            return this;
        }

        /**
         * Sets the end time for the query.
         * 
         * @param endTime the end time (inclusive)
         * @return this builder instance
         */
        public Builder withEndTime(Instant endTime) {
            this.endTime = endTime;
            return this;
        }

        /**
         * Adds a tag filter to the query.
         * 
         * @param tagName the name of the tag
         * @param tagValue the value to filter by
         * @return this builder instance
         */
        public Builder withTagFilter(String tagName, String tagValue) {
            if (tagName != null && tagValue != null) {
                tagFilters.put(tagName, tagValue);
            }
            return this;
        }

        /**
         * Adds multiple tag filters to the query.
         * 
         * @param tagFilters the tag filters to add
         * @return this builder instance
         */
        public Builder withTagFilters(Map<String, String> tagFilters) {
            if (tagFilters != null) {
                this.tagFilters.putAll(tagFilters);
            }
            return this;
        }

        /**
         * Adds a field filter to the query.
         * 
         * @param fieldName the name of the field
         * @param fieldValue the value to filter by
         * @return this builder instance
         */
        public Builder withFieldFilter(String fieldName, Object fieldValue) {
            if (fieldName != null && fieldValue != null) {
                fieldFilters.put(fieldName, fieldValue);
            }
            return this;
        }

        /**
         * Adds multiple field filters to the query.
         * 
         * @param fieldFilters the field filters to add
         * @return this builder instance
         */
        public Builder withFieldFilters(Map<String, Object> fieldFilters) {
            if (fieldFilters != null) {
                this.fieldFilters.putAll(fieldFilters);
            }
            return this;
        }

        /**
         * Sets the limit for the query results.
         * 
         * @param limit the maximum number of results to return
         * @return this builder instance
         */
        public Builder withLimit(Integer limit) {
            this.limit = limit;
            return this;
        }

        /**
         * Sets the offset for the query results.
         * 
         * @param offset the number of results to skip
         * @return this builder instance
         */
        public Builder withOffset(Integer offset) {
            this.offset = offset;
            return this;
        }

        /**
         * Sets the sort order for the query results.
         * 
         * @param sortOrder the sort order
         * @return this builder instance
         */
        public Builder withSortOrder(SortOrder sortOrder) {
            this.sortOrder = sortOrder;
            return this;
        }

        /**
         * Sets the aggregation function and period for the query.
         * 
         * @param aggregationFunction the aggregation function
         * @param aggregationPeriod the aggregation period
         * @return this builder instance
         */
        public Builder withAggregation(String aggregationFunction, Duration aggregationPeriod) {
            this.aggregationFunction = aggregationFunction;
            this.aggregationPeriod = aggregationPeriod;
            return this;
        }

        /**
         * Builds the TimeSeriesQueryCriteria instance.
         * 
         * @return the built TimeSeriesQueryCriteria
         */
        public TimeSeriesQueryCriteria build() {
            return new TimeSeriesQueryCriteria(
                seriesId,
                startTime,
                endTime,
                tagFilters,
                fieldFilters,
                limit,
                offset,
                sortOrder,
                aggregationFunction,
                aggregationPeriod
            );
        }
    }
}
