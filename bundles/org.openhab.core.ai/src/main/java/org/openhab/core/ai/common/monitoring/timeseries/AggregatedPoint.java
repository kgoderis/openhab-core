package org.openhab.core.ai.common.monitoring.timeseries;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Represents an aggregated data point in a time series with aggregated values over a time period.
 * 
 * <p>
 * An AggregatedPoint contains:
 * <ul>
 *   <li><strong>timestamp</strong>: The timestamp representing the end of the aggregation period</li>
 *   <li><strong>period</strong>: The time period over which the aggregation was performed</li>
 *   <li><strong>aggregationFunction</strong>: The function used for aggregation (AVG, MIN, MAX, SUM, COUNT, FIRST, LAST)</li>
 *   <li><strong>aggregatedValues</strong>: The aggregated metric values (immutable)</li>
 * </ul>
 * </p>
 * 
 * <h3>Usage Example</h3>
 * <pre>{@code
 * Map<String, Object> values = Map.of(
 *     "duration_ms_avg", 150.5,
 *     "duration_ms_min", 50.0,
 *     "duration_ms_max", 300.0,
 *     "success_count", 95,
 *     "failure_count", 5
 * );
 * AggregatedPoint point = new AggregatedPoint(
 *     Instant.now(), 
 *     Duration.ofMinutes(5), 
 *     "AVG", 
 *     values
 * );
 * }</pre>
 * 
 * <h3>Design Principles</h3>
 * <ul>
 *   <li><strong>Immutability</strong>: All data is immutable to ensure thread safety</li>
 *   <li><strong>Flexibility</strong>: Supports multiple aggregation functions and field types</li>
 *   <li><strong>Performance</strong>: Optimized for time series aggregation operations</li>
 *   <li><strong>Serialization</strong>: JSON-friendly for storage and transmission</li>
 * </ul>
 * 
 * @param timestamp the timestamp representing the end of the aggregation period
 * @param period the time period over which the aggregation was performed
 * @param aggregationFunction the function used for aggregation (AVG, MIN, MAX, SUM, COUNT, FIRST, LAST)
 * @param aggregatedValues the aggregated metric values (immutable)
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record AggregatedPoint(
    Instant timestamp,
    Duration period,
    String aggregationFunction,
    Map<String, Object> aggregatedValues
) {

    /**
     * Creates a new AggregatedPoint with the specified timestamp, period, aggregation function, and values.
     * 
     * <p>
     * The aggregatedValues map is copied to ensure immutability. Null values
     * are not allowed for timestamp, period, or aggregationFunction.
     * </p>
     * 
     * @param timestamp the timestamp representing the end of the aggregation period (must not be null)
     * @param period the time period over which the aggregation was performed (must not be null)
     * @param aggregationFunction the function used for aggregation (must not be null or empty)
     * @param aggregatedValues the aggregated metric values (can be empty, will be copied)
     * @throws IllegalArgumentException if timestamp, period, or aggregationFunction is null, or if aggregationFunction is empty
     */
    public AggregatedPoint {
        Objects.requireNonNull(timestamp, "timestamp must not be null");
        Objects.requireNonNull(period, "period must not be null");
        Objects.requireNonNull(aggregationFunction, "aggregationFunction must not be null");
        
        if (aggregationFunction.trim().isEmpty()) {
            throw new IllegalArgumentException("aggregationFunction must not be empty");
        }
        
        // Create defensive copy to ensure immutability
        aggregatedValues = aggregatedValues != null ? Map.copyOf(aggregatedValues) : Map.of();
    }

    /**
     * Gets the value of a specific aggregated field as a Number.
     * 
     * @param fieldName the name of the aggregated field
     * @return the field value as a Number, or null if the field doesn't exist or isn't a Number
     */
    public Number getValueAsNumber(String fieldName) {
        Object value = aggregatedValues.get(fieldName);
        return value instanceof Number ? (Number) value : null;
    }

    /**
     * Gets the value of a specific aggregated field as a Double.
     * 
     * @param fieldName the name of the aggregated field
     * @return the field value as a Double, or null if the field doesn't exist or isn't a Number
     */
    public Double getValueAsDouble(String fieldName) {
        Number value = getValueAsNumber(fieldName);
        return value != null ? value.doubleValue() : null;
    }

    /**
     * Gets the value of a specific aggregated field as a Long.
     * 
     * @param fieldName the name of the aggregated field
     * @return the field value as a Long, or null if the field doesn't exist or isn't a Number
     */
    public Long getValueAsLong(String fieldName) {
        Number value = getValueAsNumber(fieldName);
        return value != null ? value.longValue() : null;
    }

    /**
     * Gets the value of a specific aggregated field as an Integer.
     * 
     * @param fieldName the name of the aggregated field
     * @return the field value as an Integer, or null if the field doesn't exist or isn't a Number
     */
    public Integer getValueAsInteger(String fieldName) {
        Number value = getValueAsNumber(fieldName);
        return value != null ? value.intValue() : null;
    }

    /**
     * Gets the value of a specific aggregated field as a String.
     * 
     * @param fieldName the name of the aggregated field
     * @return the field value as a String, or null if the field doesn't exist
     */
    public String getValueAsString(String fieldName) {
        Object value = aggregatedValues.get(fieldName);
        return value != null ? value.toString() : null;
    }

    /**
     * Checks if this aggregated point has a specific field.
     * 
     * @param fieldName the name of the field
     * @return true if the field exists, false otherwise
     */
    public boolean hasValue(String fieldName) {
        return aggregatedValues.containsKey(fieldName);
    }

    /**
     * Gets the number of aggregated values in this point.
     * 
     * @return the number of aggregated values
     */
    public int getValueCount() {
        return aggregatedValues.size();
    }

    /**
     * Gets the start time of the aggregation period.
     * 
     * @return the start time (timestamp minus period)
     */
    public Instant getPeriodStart() {
        return timestamp.minus(period);
    }

    /**
     * Gets the end time of the aggregation period.
     * 
     * @return the end time (same as timestamp)
     */
    public Instant getPeriodEnd() {
        return timestamp;
    }

    /**
     * Checks if this aggregated point represents a specific aggregation function.
     * 
     * @param function the aggregation function to check
     * @return true if this point uses the specified function, false otherwise
     */
    public boolean isAggregationFunction(String function) {
        return aggregationFunction.equals(function);
    }

    /**
     * Creates a new AggregatedPoint with additional aggregated values.
     * 
     * <p>
     * This method creates a new AggregatedPoint with the existing values plus
     * the additional values. If a value with the same name already exists, it
     * will be overwritten with the new value.
     * </p>
     * 
     * @param additionalValues additional aggregated values to add
     * @return a new AggregatedPoint with the additional values
     */
    public AggregatedPoint withAdditionalValues(Map<String, Object> additionalValues) {
        if (additionalValues == null || additionalValues.isEmpty()) {
            return this;
        }
        
        Map<String, Object> newValues = new java.util.HashMap<>(aggregatedValues);
        newValues.putAll(additionalValues);
        return new AggregatedPoint(timestamp, period, aggregationFunction, newValues);
    }

    /**
     * Returns a string representation of this AggregatedPoint.
     * 
     * <p>
     * The string includes the timestamp, period, aggregation function, and value count
     * for debugging purposes. The actual aggregated values are not included to avoid
     * potentially large output.
     * </p>
     * 
     * @return string representation of this AggregatedPoint
     */
    @Override
    public String toString() {
        return String.format("AggregatedPoint{timestamp=%s, period=%s, function=%s, values=%d}", 
                           timestamp, period, aggregationFunction, aggregatedValues.size());
    }
}
