package org.openhab.core.ai.common.monitoring.timeseries;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Enumeration of supported aggregation functions for time series data.
 * 
 * <p>
 * This enum defines the available aggregation functions that can be applied
 * to time series data points to compute aggregated values over time periods.
 * Each function has a specific mathematical operation and can be applied
 * to numeric fields in time series data.
 * </p>
 * 
 * <h3>Supported Functions</h3>
 * <ul>
 *   <li><strong>AVG</strong>: Average (arithmetic mean) of values</li>
 *   <li><strong>MIN</strong>: Minimum value</li>
 *   <li><strong>MAX</strong>: Maximum value</li>
 *   <li><strong>SUM</strong>: Sum of all values</li>
 *   <li><strong>COUNT</strong>: Count of non-null values</li>
 *   <li><strong>FIRST</strong>: First value in the time period</li>
 *   <li><strong>LAST</strong>: Last value in the time period</li>
 * </ul>
 * 
 * <h3>Usage Example</h3>
 * <pre>{@code
 * // Apply average aggregation
 * AggregationFunction avg = AggregationFunction.AVG;
 * Double result = avg.apply(List.of(10.0, 20.0, 30.0)); // Returns 20.0
 * 
 * // Apply count aggregation
 * AggregationFunction count = AggregationFunction.COUNT;
 * Long result = count.apply(List.of(10.0, null, 30.0)); // Returns 2
 * }</pre>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public enum AggregationFunction {

    /**
     * Average (arithmetic mean) of numeric values.
     * 
     * <p>
     * Computes the arithmetic mean of all non-null numeric values.
     * Returns null if no valid numeric values are found.
     * </p>
     */
    AVG("Average", values -> {
        List<Double> numericValues = values.stream()
            .filter(Objects::nonNull)
            .filter(v -> v instanceof Number)
            .map(v -> ((Number) v).doubleValue())
            .toList();
        
        if (numericValues.isEmpty()) {
            return null;
        }
        
        return numericValues.stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.0);
    }),

    /**
     * Minimum value among numeric values.
     * 
     * <p>
     * Finds the smallest numeric value among all non-null values.
     * Returns null if no valid numeric values are found.
     * </p>
     */
    MIN("Minimum", values -> {
        return values.stream()
            .filter(Objects::nonNull)
            .filter(v -> v instanceof Number)
            .map(v -> ((Number) v).doubleValue())
            .min(Double::compareTo)
            .orElse(null);
    }),

    /**
     * Maximum value among numeric values.
     * 
     * <p>
     * Finds the largest numeric value among all non-null values.
     * Returns null if no valid numeric values are found.
     * </p>
     */
    MAX("Maximum", values -> {
        return values.stream()
            .filter(Objects::nonNull)
            .filter(v -> v instanceof Number)
            .map(v -> ((Number) v).doubleValue())
            .max(Double::compareTo)
            .orElse(null);
    }),

    /**
     * Sum of all numeric values.
     * 
     * <p>
     * Computes the sum of all non-null numeric values.
     * Returns 0.0 if no valid numeric values are found.
     * </p>
     */
    SUM("Sum", values -> {
        return values.stream()
            .filter(Objects::nonNull)
            .filter(v -> v instanceof Number)
            .mapToDouble(v -> ((Number) v).doubleValue())
            .sum();
    }),

    /**
     * Count of non-null values.
     * 
     * <p>
     * Counts the number of non-null values in the list.
     * Returns 0 if all values are null.
     * </p>
     */
    COUNT("Count", values -> {
        return (double) values.stream()
            .filter(Objects::nonNull)
            .count();
    }),

    /**
     * First value in the time period.
     * 
     * <p>
     * Returns the first non-null value in the list.
     * Returns null if all values are null.
     * </p>
     */
    FIRST("First", values -> {
        return values.stream()
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
    }),

    /**
     * Last value in the time period.
     * 
     * <p>
     * Returns the last non-null value in the list.
     * Returns null if all values are null.
     * </p>
     */
    LAST("Last", values -> {
        return values.stream()
            .filter(Objects::nonNull)
            .reduce((first, second) -> second)
            .orElse(null);
    });

    private final String displayName;
    private final Function<List<Object>, Object> aggregator;

    /**
     * Creates a new AggregationFunction with the specified display name and aggregator function.
     * 
     * @param displayName the human-readable name of the function
     * @param aggregator the function that performs the aggregation
     */
    AggregationFunction(String displayName, Function<List<Object>, Object> aggregator) {
        this.displayName = displayName;
        this.aggregator = aggregator;
    }

    /**
     * Gets the human-readable display name of this aggregation function.
     * 
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Applies this aggregation function to a list of values.
     * 
     * <p>
     * The aggregation function processes the list of values according to its
     * specific mathematical operation and returns the aggregated result.
     * </p>
     * 
     * @param values the list of values to aggregate
     * @return the aggregated result, or null if no valid values are found
     * @throws IllegalArgumentException if values is null
     */
    public Object apply(List<Object> values) {
        Objects.requireNonNull(values, "values must not be null");
        return aggregator.apply(values);
    }

    /**
     * Applies this aggregation function to a list of numeric values.
     * 
     * <p>
     * This is a convenience method that converts the input to a list of Objects
     * and applies the aggregation function.
     * </p>
     * 
     * @param values the list of numeric values to aggregate
     * @return the aggregated result as a Number, or null if no valid values are found
     * @throws IllegalArgumentException if values is null
     */
    public Number applyToNumbers(List<? extends Number> values) {
        Objects.requireNonNull(values, "values must not be null");
        List<Object> objectValues = values.stream().map(v -> (Object) v).toList();
        Object result = apply(objectValues);
        return result instanceof Number ? (Number) result : null;
    }

    /**
     * Checks if this aggregation function is suitable for numeric data.
     * 
     * <p>
     * Some aggregation functions (like AVG, MIN, MAX, SUM) are primarily
     * designed for numeric data, while others (like COUNT, FIRST, LAST)
     * can work with any data type.
     * </p>
     * 
     * @return true if this function is suitable for numeric data, false otherwise
     */
    public boolean isNumericFunction() {
        return this == AVG || this == MIN || this == MAX || this == SUM;
    }

    /**
     * Checks if this aggregation function can handle null values.
     * 
     * <p>
     * Most aggregation functions can handle null values by filtering them out,
     * but some functions may have specific behavior with null values.
     * </p>
     * 
     * @return true if this function can handle null values, false otherwise
     */
    public boolean handlesNullValues() {
        return true; // All current functions handle null values by filtering them out
    }

    /**
     * Gets the aggregation function by name (case-insensitive).
     * 
     * @param name the name of the aggregation function
     * @return the aggregation function, or null if not found
     */
    public static AggregationFunction fromName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        
        try {
            return valueOf(name.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Gets all aggregation function names as an array of strings.
     * 
     * @return array of aggregation function names
     */
    public static String[] getAllNames() {
        AggregationFunction[] functions = values();
        String[] names = new String[functions.length];
        for (int i = 0; i < functions.length; i++) {
            names[i] = functions[i].name();
        }
        return names;
    }

    /**
     * Returns the string representation of this aggregation function.
     * 
     * @return the name of this aggregation function
     */
    @Override
    public String toString() {
        return name();
    }
}
