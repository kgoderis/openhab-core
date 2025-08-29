package org.openhab.core.ai.common.monitoring.timeseries;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Fluent query builder for constructing complex time series queries.
 * 
 * <p>
 * TimeSeriesQueryBuilder provides a fluent API for constructing TimeSeriesQueryCriteria
 * with complex filtering, pagination, sorting, and aggregation options. It supports
 * method chaining for easy and readable query construction.
 * </p>
 * 
 * <h3>Usage Example</h3>
 * <pre>{@code
 * TimeSeriesQueryCriteria criteria = TimeSeriesQueryBuilder
 *     .forSeries("metrics:model:completion")
 *     .inTimeRange(startTime, endTime)
 *     .withTag("domain", "model")
 *     .withTag("operation", "completion")
 *     .withField("success", true)
 *     .withField("duration_ms", Range.greaterThan(100))
 *     .limit(100)
 *     .sortByTime(SortOrder.DESCENDING)
 *     .aggregate(AggregationFunction.AVG, Duration.ofMinutes(5))
 *     .build();
 * }</pre>
 * 
 * <h3>Design Principles</h3>
 * <ul>
 *   <li><strong>Fluent API</strong>: Method chaining for readable query construction</li>
 *   <li><strong>Type Safety</strong>: Compile-time validation of query parameters</li>
 *   <li><strong>Flexibility</strong>: Supports complex filtering and aggregation scenarios</li>
 *   <li><strong>Performance</strong>: Optimized for efficient query execution</li>
 * </ul>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class TimeSeriesQueryBuilder {

    private final String seriesId;
    private Instant startTime;
    private Instant endTime;
    private final Map<String, String> tagFilters = new java.util.HashMap<>();
    private final Map<String, Object> fieldFilters = new java.util.HashMap<>();
    private Integer limit;
    private Integer offset;
    private TimeSeriesQueryCriteria.SortOrder sortOrder;
    private String aggregationFunction;
    private Duration aggregationPeriod;

    /**
     * Creates a new query builder for the specified time series.
     * 
     * @param seriesId the time series identifier to query
     * @return a new query builder instance
     */
    public static TimeSeriesQueryBuilder forSeries(String seriesId) {
        return new TimeSeriesQueryBuilder(seriesId);
    }

    /**
     * Private constructor for the query builder.
     * 
     * @param seriesId the time series identifier to query
     */
    private TimeSeriesQueryBuilder(String seriesId) {
        this.seriesId = Objects.requireNonNull(seriesId, "seriesId must not be null");
    }

    /**
     * Sets the time range for the query.
     * 
     * @param startTime the start time (inclusive)
     * @param endTime the end time (inclusive)
     * @return this builder instance
     */
    public TimeSeriesQueryBuilder inTimeRange(Instant startTime, Instant endTime) {
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
    public TimeSeriesQueryBuilder fromTime(Instant startTime) {
        this.startTime = startTime;
        return this;
    }

    /**
     * Sets the end time for the query.
     * 
     * @param endTime the end time (inclusive)
     * @return this builder instance
     */
    public TimeSeriesQueryBuilder toTime(Instant endTime) {
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
    public TimeSeriesQueryBuilder withTag(String tagName, String tagValue) {
        if (tagName != null && tagValue != null) {
            tagFilters.put(tagName, tagValue);
        }
        return this;
    }

    /**
     * Adds multiple tag filters to the query.
     * 
     * @param tags the tag filters to add
     * @return this builder instance
     */
    public TimeSeriesQueryBuilder withTags(Map<String, String> tags) {
        if (tags != null) {
            tagFilters.putAll(tags);
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
    public TimeSeriesQueryBuilder withField(String fieldName, Object fieldValue) {
        if (fieldName != null && fieldValue != null) {
            fieldFilters.put(fieldName, fieldValue);
        }
        return this;
    }

    /**
     * Adds multiple field filters to the query.
     * 
     * @param fields the field filters to add
     * @return this builder instance
     */
    public TimeSeriesQueryBuilder withFields(Map<String, Object> fields) {
        if (fields != null) {
            fieldFilters.putAll(fields);
        }
        return this;
    }

    /**
     * Adds a field filter for numeric range.
     * 
     * @param fieldName the name of the field
     * @param range the numeric range to filter by
     * @return this builder instance
     */
    public TimeSeriesQueryBuilder withFieldRange(String fieldName, NumericRange range) {
        if (fieldName != null && range != null) {
            fieldFilters.put(fieldName, range);
        }
        return this;
    }

    /**
     * Sets the limit for the query results.
     * 
     * @param limit the maximum number of results to return
     * @return this builder instance
     */
    public TimeSeriesQueryBuilder limit(int limit) {
        this.limit = limit;
        return this;
    }

    /**
     * Sets the offset for the query results.
     * 
     * @param offset the number of results to skip
     * @return this builder instance
     */
    public TimeSeriesQueryBuilder offset(int offset) {
        this.offset = offset;
        return this;
    }

    /**
     * Sets the sort order for the query results.
     * 
     * @param sortOrder the sort order
     * @return this builder instance
     */
    public TimeSeriesQueryBuilder sortByTime(TimeSeriesQueryCriteria.SortOrder sortOrder) {
        this.sortOrder = sortOrder;
        return this;
    }

    /**
     * Sets ascending sort order for the query results.
     * 
     * @return this builder instance
     */
    public TimeSeriesQueryBuilder sortAscending() {
        this.sortOrder = TimeSeriesQueryCriteria.SortOrder.ASCENDING;
        return this;
    }

    /**
     * Sets descending sort order for the query results.
     * 
     * @return this builder instance
     */
    public TimeSeriesQueryBuilder sortDescending() {
        this.sortOrder = TimeSeriesQueryCriteria.SortOrder.DESCENDING;
        return this;
    }

    /**
     * Sets the aggregation function and period for the query.
     * 
     * @param aggregationFunction the aggregation function
     * @param aggregationPeriod the aggregation period
     * @return this builder instance
     */
    public TimeSeriesQueryBuilder aggregate(String aggregationFunction, Duration aggregationPeriod) {
        this.aggregationFunction = aggregationFunction;
        this.aggregationPeriod = aggregationPeriod;
        return this;
    }

    /**
     * Sets the aggregation function and period for the query using the enum.
     * 
     * @param aggregationFunction the aggregation function
     * @param aggregationPeriod the aggregation period
     * @return this builder instance
     */
    public TimeSeriesQueryBuilder aggregate(AggregationFunction aggregationFunction, Duration aggregationPeriod) {
        this.aggregationFunction = aggregationFunction.name();
        this.aggregationPeriod = aggregationPeriod;
        return this;
    }

    /**
     * Sets average aggregation with the specified period.
     * 
     * @param period the aggregation period
     * @return this builder instance
     */
    public TimeSeriesQueryBuilder average(Duration period) {
        return aggregate(AggregationFunction.AVG, period);
    }

    /**
     * Sets sum aggregation with the specified period.
     * 
     * @param period the aggregation period
     * @return this builder instance
     */
    public TimeSeriesQueryBuilder sum(Duration period) {
        return aggregate(AggregationFunction.SUM, period);
    }

    /**
     * Sets count aggregation with the specified period.
     * 
     * @param period the aggregation period
     * @return this builder instance
     */
    public TimeSeriesQueryBuilder count(Duration period) {
        return aggregate(AggregationFunction.COUNT, period);
    }

    /**
     * Sets minimum aggregation with the specified period.
     * 
     * @param period the aggregation period
     * @return this builder instance
     */
    public TimeSeriesQueryBuilder minimum(Duration period) {
        return aggregate(AggregationFunction.MIN, period);
    }

    /**
     * Sets maximum aggregation with the specified period.
     * 
     * @param period the aggregation period
     * @return this builder instance
     */
    public TimeSeriesQueryBuilder maximum(Duration period) {
        return aggregate(AggregationFunction.MAX, period);
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

    /**
     * Numeric range class for field filtering.
     */
    public static final class NumericRange {
        private final Double min;
        private final Double max;
        private final boolean minInclusive;
        private final boolean maxInclusive;

        private NumericRange(Double min, Double max, boolean minInclusive, boolean maxInclusive) {
            this.min = min;
            this.max = max;
            this.minInclusive = minInclusive;
            this.maxInclusive = maxInclusive;
        }

        /**
         * Creates a range with both minimum and maximum values.
         * 
         * @param min the minimum value (inclusive)
         * @param max the maximum value (inclusive)
         * @return a new numeric range
         */
        public static NumericRange between(double min, double max) {
            return new NumericRange(min, max, true, true);
        }

        /**
         * Creates a range with both minimum and maximum values and inclusivity options.
         * 
         * @param min the minimum value
         * @param max the maximum value
         * @param minInclusive whether the minimum value is inclusive
         * @param maxInclusive whether the maximum value is inclusive
         * @return a new numeric range
         */
        public static NumericRange between(double min, double max, boolean minInclusive, boolean maxInclusive) {
            return new NumericRange(min, max, minInclusive, maxInclusive);
        }

        /**
         * Creates a range with only a minimum value.
         * 
         * @param min the minimum value (inclusive)
         * @return a new numeric range
         */
        public static NumericRange greaterThan(double min) {
            return new NumericRange(min, null, false, true);
        }

        /**
         * Creates a range with only a minimum value (inclusive).
         * 
         * @param min the minimum value (inclusive)
         * @return a new numeric range
         */
        public static NumericRange greaterThanOrEqual(double min) {
            return new NumericRange(min, null, true, true);
        }

        /**
         * Creates a range with only a maximum value.
         * 
         * @param max the maximum value (inclusive)
         * @return a new numeric range
         */
        public static NumericRange lessThan(double max) {
            return new NumericRange(null, max, true, false);
        }

        /**
         * Creates a range with only a maximum value (inclusive).
         * 
         * @param max the maximum value (inclusive)
         * @return a new numeric range
         */
        public static NumericRange lessThanOrEqual(double max) {
            return new NumericRange(null, max, true, true);
        }

        /**
         * Gets the minimum value of the range.
         * 
         * @return the minimum value, or null if not set
         */
        public Double getMin() {
            return min;
        }

        /**
         * Gets the maximum value of the range.
         * 
         * @return the maximum value, or null if not set
         */
        public Double getMax() {
            return max;
        }

        /**
         * Checks if the minimum value is inclusive.
         * 
         * @return true if the minimum value is inclusive, false otherwise
         */
        public boolean isMinInclusive() {
            return minInclusive;
        }

        /**
         * Checks if the maximum value is inclusive.
         * 
         * @return true if the maximum value is inclusive, false otherwise
         */
        public boolean isMaxInclusive() {
            return maxInclusive;
        }

        /**
         * Checks if a value is within this range.
         * 
         * @param value the value to check
         * @return true if the value is within the range, false otherwise
         */
        public boolean contains(double value) {
            if (min != null) {
                if (minInclusive && value < min) return false;
                if (!minInclusive && value <= min) return false;
            }
            if (max != null) {
                if (maxInclusive && value > max) return false;
                if (!maxInclusive && value >= max) return false;
            }
            return true;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (min != null) {
                sb.append(minInclusive ? "[" : "(").append(min);
            } else {
                sb.append("(-∞");
            }
            sb.append(", ");
            if (max != null) {
                sb.append(max).append(maxInclusive ? "]" : ")");
            } else {
                sb.append("+∞)");
            }
            return sb.toString();
        }
    }
}
