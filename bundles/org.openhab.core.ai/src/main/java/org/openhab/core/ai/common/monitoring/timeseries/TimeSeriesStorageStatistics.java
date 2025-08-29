package org.openhab.core.ai.common.monitoring.timeseries;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Statistics and health information for time series storage system.
 * 
 * <p>
 * TimeSeriesStorageStatistics provides comprehensive information about the storage
 * system's health, performance, and operational status, including:
 * <ul>
 *   <li><strong>totalSeries</strong>: Total number of time series in the system</li>
 *   <li><strong>totalPoints</strong>: Total number of data points across all series</li>
 *   <li><strong>storageSize</strong>: Total storage size in bytes</li>
 *   <li><strong>lastUpdate</strong>: When the statistics were last updated</li>
 *   <li><strong>healthStatus</strong>: Overall health status of the storage system</li>
 *   <li><strong>performanceMetrics</strong>: Performance-related metrics</li>
 * </ul>
 * </p>
 * 
 * <h3>Usage Example</h3>
 * <pre>{@code
 * TimeSeriesStorageStatistics stats = storage.getStorageStatistics();
 * System.out.println("Total series: " + stats.getTotalSeries());
 * System.out.println("Total points: " + stats.getTotalPoints());
 * System.out.println("Storage size: " + stats.getStorageSize() + " bytes");
 * System.out.println("Health status: " + stats.getHealthStatus());
 * }</pre>
 * 
 * <h3>Design Principles</h3>
 * <ul>
 *   <li><strong>Immutability</strong>: All data is immutable to ensure thread safety</li>
 *   <li><strong>Comprehensive</strong>: Provides all necessary information for monitoring</li>
 *   <li><strong>Performance</strong>: Optimized for frequent access and updates</li>
 *   <li><strong>Extensible</strong>: Supports additional metrics through custom properties</li>
 * </ul>
 * 
 * @param totalSeries total number of time series in the system
 * @param totalPoints total number of data points across all series
 * @param storageSize total storage size in bytes
 * @param lastUpdate when the statistics were last updated
 * @param healthStatus overall health status of the storage system
 * @param performanceMetrics performance-related metrics (immutable)
 * @param customProperties additional custom metrics (immutable)
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record TimeSeriesStorageStatistics(
    int totalSeries,
    long totalPoints,
    long storageSize,
    Instant lastUpdate,
    HealthStatus healthStatus,
    Map<String, Object> performanceMetrics,
    Map<String, Object> customProperties
) {

    /**
     * Health status enumeration for the storage system.
     */
    public enum HealthStatus {
        /**
         * System is healthy and operating normally.
         */
        HEALTHY,
        
        /**
         * System is experiencing minor issues but still functional.
         */
        WARNING,
        
        /**
         * System is experiencing significant issues and may not be fully functional.
         */
        CRITICAL,
        
        /**
         * System is down or completely non-functional.
         */
        DOWN
    }

    /**
     * Creates a new TimeSeriesStorageStatistics with the specified parameters.
     * 
     * <p>
     * The performanceMetrics and customProperties maps are copied to ensure immutability.
     * The lastUpdate and healthStatus are required and cannot be null.
     * </p>
     * 
     * @param totalSeries total number of time series in the system (must not be negative)
     * @param totalPoints total number of data points across all series (must not be negative)
     * @param storageSize total storage size in bytes (must not be negative)
     * @param lastUpdate when the statistics were last updated (must not be null)
     * @param healthStatus overall health status of the storage system (must not be null)
     * @param performanceMetrics performance-related metrics (can be empty, will be copied)
     * @param customProperties additional custom metrics (can be empty, will be copied)
     * @throws IllegalArgumentException if any parameter is invalid
     */
    public TimeSeriesStorageStatistics {
        if (totalSeries < 0) {
            throw new IllegalArgumentException("totalSeries must not be negative");
        }
        if (totalPoints < 0) {
            throw new IllegalArgumentException("totalPoints must not be negative");
        }
        if (storageSize < 0) {
            throw new IllegalArgumentException("storageSize must not be negative");
        }
        
        Objects.requireNonNull(lastUpdate, "lastUpdate must not be null");
        Objects.requireNonNull(healthStatus, "healthStatus must not be null");
        
        // Create defensive copies to ensure immutability
        performanceMetrics = performanceMetrics != null ? Map.copyOf(performanceMetrics) : Map.of();
        customProperties = customProperties != null ? Map.copyOf(customProperties) : Map.of();
    }

    /**
     * Gets the average number of points per series.
     * 
     * @return the average points per series, or 0 if there are no series
     */
    public double getAveragePointsPerSeries() {
        return totalSeries > 0 ? (double) totalPoints / totalSeries : 0.0;
    }

    /**
     * Gets the average storage size per series in bytes.
     * 
     * @return the average storage size per series, or 0 if there are no series
     */
    public double getAverageStorageSizePerSeries() {
        return totalSeries > 0 ? (double) storageSize / totalSeries : 0.0;
    }

    /**
     * Gets the average storage size per point in bytes.
     * 
     * @return the average storage size per point, or 0 if there are no points
     */
    public double getAverageStorageSizePerPoint() {
        return totalPoints > 0 ? (double) storageSize / totalPoints : 0.0;
    }

    /**
     * Checks if the storage system is healthy.
     * 
     * @return true if the health status is HEALTHY, false otherwise
     */
    public boolean isHealthy() {
        return healthStatus == HealthStatus.HEALTHY;
    }

    /**
     * Checks if the storage system has warnings.
     * 
     * @return true if the health status is WARNING, false otherwise
     */
    public boolean hasWarnings() {
        return healthStatus == HealthStatus.WARNING;
    }

    /**
     * Checks if the storage system is in critical state.
     * 
     * @return true if the health status is CRITICAL, false otherwise
     */
    public boolean isCritical() {
        return healthStatus == HealthStatus.CRITICAL;
    }

    /**
     * Checks if the storage system is down.
     * 
     * @return true if the health status is DOWN, false otherwise
     */
    public boolean isDown() {
        return healthStatus == HealthStatus.DOWN;
    }

    /**
     * Gets a performance metric value.
     * 
     * @param metricName the name of the performance metric
     * @return the metric value, or null if not found
     */
    public Object getPerformanceMetric(String metricName) {
        return performanceMetrics.get(metricName);
    }

    /**
     * Gets a performance metric value as a Number.
     * 
     * @param metricName the name of the performance metric
     * @return the metric value as a Number, or null if not found or not a Number
     */
    public Number getPerformanceMetricAsNumber(String metricName) {
        Object value = performanceMetrics.get(metricName);
        return value instanceof Number ? (Number) value : null;
    }

    /**
     * Gets a performance metric value as a String.
     * 
     * @param metricName the name of the performance metric
     * @return the metric value as a String, or null if not found
     */
    public String getPerformanceMetricAsString(String metricName) {
        Object value = performanceMetrics.get(metricName);
        return value != null ? value.toString() : null;
    }

    /**
     * Checks if a performance metric exists.
     * 
     * @param metricName the name of the performance metric
     * @return true if the metric exists, false otherwise
     */
    public boolean hasPerformanceMetric(String metricName) {
        return performanceMetrics.containsKey(metricName);
    }

    /**
     * Gets a custom property value.
     * 
     * @param propertyName the name of the custom property
     * @return the property value, or null if not found
     */
    public Object getCustomProperty(String propertyName) {
        return customProperties.get(propertyName);
    }

    /**
     * Checks if a custom property exists.
     * 
     * @param propertyName the name of the custom property
     * @return true if the property exists, false otherwise
     */
    public boolean hasCustomProperty(String propertyName) {
        return customProperties.containsKey(propertyName);
    }

    /**
     * Gets the number of performance metrics.
     * 
     * @return the number of performance metrics
     */
    public int getPerformanceMetricCount() {
        return performanceMetrics.size();
    }

    /**
     * Gets the number of custom properties.
     * 
     * @return the number of custom properties
     */
    public int getCustomPropertyCount() {
        return customProperties.size();
    }

    /**
     * Creates a new TimeSeriesStorageStatistics with updated values.
     * 
     * @param newTotalSeries the new total series count
     * @param newTotalPoints the new total points count
     * @param newStorageSize the new storage size
     * @param newLastUpdate the new last update time
     * @param newHealthStatus the new health status
     * @return a new TimeSeriesStorageStatistics with the updated values
     */
    public TimeSeriesStorageStatistics withUpdate(int newTotalSeries, long newTotalPoints, 
                                                 long newStorageSize, Instant newLastUpdate, 
                                                 HealthStatus newHealthStatus) {
        return new TimeSeriesStorageStatistics(
            newTotalSeries,
            newTotalPoints,
            newStorageSize,
            newLastUpdate,
            newHealthStatus,
            performanceMetrics,
            customProperties
        );
    }

    /**
     * Creates a new TimeSeriesStorageStatistics with additional performance metrics.
     * 
     * @param additionalMetrics additional performance metrics to add
     * @return a new TimeSeriesStorageStatistics with the additional metrics
     */
    public TimeSeriesStorageStatistics withAdditionalPerformanceMetrics(Map<String, Object> additionalMetrics) {
        if (additionalMetrics == null || additionalMetrics.isEmpty()) {
            return this;
        }
        
        Map<String, Object> newMetrics = new java.util.HashMap<>(performanceMetrics);
        newMetrics.putAll(additionalMetrics);
        return new TimeSeriesStorageStatistics(
            totalSeries,
            totalPoints,
            storageSize,
            lastUpdate,
            healthStatus,
            newMetrics,
            customProperties
        );
    }

    /**
     * Creates a new TimeSeriesStorageStatistics with additional custom properties.
     * 
     * @param additionalProperties additional custom properties to add
     * @return a new TimeSeriesStorageStatistics with the additional properties
     */
    public TimeSeriesStorageStatistics withAdditionalCustomProperties(Map<String, Object> additionalProperties) {
        if (additionalProperties == null || additionalProperties.isEmpty()) {
            return this;
        }
        
        Map<String, Object> newProperties = new java.util.HashMap<>(customProperties);
        newProperties.putAll(additionalProperties);
        return new TimeSeriesStorageStatistics(
            totalSeries,
            totalPoints,
            storageSize,
            lastUpdate,
            healthStatus,
            performanceMetrics,
            newProperties
        );
    }

    /**
     * Returns a string representation of this TimeSeriesStorageStatistics.
     * 
     * <p>
     * The string includes the key statistics and health status for debugging purposes.
     * </p>
     * 
     * @return string representation of this TimeSeriesStorageStatistics
     */
    @Override
    public String toString() {
        return String.format("TimeSeriesStorageStatistics{series=%d, points=%d, size=%d bytes, health=%s, metrics=%d, properties=%d}", 
                           totalSeries, totalPoints, storageSize, healthStatus, 
                           performanceMetrics.size(), customProperties.size());
    }
}
