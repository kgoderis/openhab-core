package org.openhab.core.ai.common.monitoring.timeseries;

import java.time.Duration;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Configuration interface for time series storage and management.
 * 
 * <p>
 * MetricTimeSeriesConfiguration provides comprehensive configuration management
 * for time series storage, including retention policies, aggregation settings,
 * performance tuning, and operational parameters.
 * </p>
 * 
 * <h3>Configuration Areas</h3>
 * <ul>
 *   <li><strong>Retention Policies</strong>: Data retention and cleanup settings</li>
 *   <li><strong>Aggregation Settings</strong>: Default aggregation periods and functions</li>
 *   <li><strong>Performance Tuning</strong>: Batch sizes, cache settings, and optimization</li>
 *   <li><strong>Storage Management</strong>: Storage limits, compression, and indexing</li>
 *   <li><strong>Monitoring</strong>: Health checks, metrics collection, and alerting</li>
 * </ul>
 * 
 * <h3>Usage Example</h3>
 * <pre>{@code
 * MetricTimeSeriesConfiguration config = new DefaultMetricTimeSeriesConfiguration();
 * config.setRetentionPeriod(Duration.ofDays(30));
 * config.setDefaultAggregationPeriod(Duration.ofHours(1));
 * config.setMaxPointsPerSeries(10000);
 * config.setAutoCleanupEnabled(true);
 * }</pre>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface MetricTimeSeriesConfiguration {

    /**
     * Gets the default retention period for time series data.
     * 
     * <p>
     * Data older than this period will be automatically cleaned up
     * if auto-cleanup is enabled.
     * </p>
     * 
     * @return the default retention period, or null if no retention limit
     */
    Duration getRetentionPeriod();

    /**
     * Sets the default retention period for time series data.
     * 
     * @param retentionPeriod the retention period to set
     */
    void setRetentionPeriod(Duration retentionPeriod);

    /**
     * Gets the default aggregation period for time series data.
     * 
     * <p>
     * This period is used for automatic aggregation of time series data
     * when no specific period is provided.
     * </p>
     * 
     * @return the default aggregation period, or null if no default
     */
    Duration getDefaultAggregationPeriod();

    /**
     * Sets the default aggregation period for time series data.
     * 
     * @param aggregationPeriod the aggregation period to set
     */
    void setDefaultAggregationPeriod(Duration aggregationPeriod);

    /**
     * Gets the maximum number of points allowed per time series.
     * 
     * <p>
     * This limit helps prevent individual time series from consuming
     * excessive storage space.
     * </p>
     * 
     * @return the maximum points per series, or null if no limit
     */
    Integer getMaxPointsPerSeries();

    /**
     * Sets the maximum number of points allowed per time series.
     * 
     * @param maxPoints the maximum points to set
     */
    void setMaxPointsPerSeries(Integer maxPoints);

    /**
     * Checks if automatic cleanup is enabled.
     * 
     * <p>
     * When enabled, old data will be automatically removed based on
     * the retention period.
     * </p>
     * 
     * @return true if auto-cleanup is enabled, false otherwise
     */
    boolean isAutoCleanupEnabled();

    /**
     * Sets whether automatic cleanup is enabled.
     * 
     * @param enabled true to enable auto-cleanup, false to disable
     */
    void setAutoCleanupEnabled(boolean enabled);

    /**
     * Gets the cleanup interval for automatic data removal.
     * 
     * <p>
     * This determines how often the cleanup process runs to remove
     * old data based on the retention period.
     * </p>
     * 
     * @return the cleanup interval
     */
    Duration getCleanupInterval();

    /**
     * Sets the cleanup interval for automatic data removal.
     * 
     * @param interval the cleanup interval to set
     */
    void setCleanupInterval(Duration interval);

    /**
     * Gets the batch size for bulk operations.
     * 
     * <p>
     * This setting controls how many operations are batched together
     * for improved performance.
     * </p>
     * 
     * @return the batch size
     */
    int getBatchSize();

    /**
     * Sets the batch size for bulk operations.
     * 
     * @param batchSize the batch size to set
     */
    void setBatchSize(int batchSize);

    /**
     * Gets the cache size for frequently accessed data.
     * 
     * <p>
     * This setting controls the size of the in-memory cache for
     * frequently accessed time series data.
     * </p>
     * 
     * @return the cache size
     */
    int getCacheSize();

    /**
     * Sets the cache size for frequently accessed data.
     * 
     * @param cacheSize the cache size to set
     */
    void setCacheSize(int cacheSize);

    /**
     * Checks if compression is enabled for stored data.
     * 
     * <p>
     * When enabled, time series data will be compressed before storage
     * to reduce disk space usage.
     * </p>
     * 
     * @return true if compression is enabled, false otherwise
     */
    boolean isCompressionEnabled();

    /**
     * Sets whether compression is enabled for stored data.
     * 
     * @param enabled true to enable compression, false to disable
     */
    void setCompressionEnabled(boolean enabled);

    /**
     * Gets the compression level for data compression.
     * 
     * <p>
     * Higher levels provide better compression but require more CPU time.
     * Valid range is typically 1-9.
     * </p>
     * 
     * @return the compression level
     */
    int getCompressionLevel();

    /**
     * Sets the compression level for data compression.
     * 
     * @param level the compression level to set (typically 1-9)
     */
    void setCompressionLevel(int level);

    /**
     * Checks if indexing is enabled for improved query performance.
     * 
     * <p>
     * When enabled, indexes will be created on frequently queried fields
     * to improve query performance.
     * </p>
     * 
     * @return true if indexing is enabled, false otherwise
     */
    boolean isIndexingEnabled();

    /**
     * Sets whether indexing is enabled for improved query performance.
     * 
     * @param enabled true to enable indexing, false to disable
     */
    void setIndexingEnabled(boolean enabled);

    /**
     * Gets the maximum storage size in bytes.
     * 
     * <p>
     * This setting provides a hard limit on the total storage size
     * used by time series data.
     * </p>
     * 
     * @return the maximum storage size, or null if no limit
     */
    Long getMaxStorageSize();

    /**
     * Sets the maximum storage size in bytes.
     * 
     * @param maxSize the maximum storage size to set
     */
    void setMaxStorageSize(Long maxSize);

    /**
     * Gets the health check interval for monitoring storage health.
     * 
     * <p>
     * This determines how often health checks are performed to ensure
     * the storage system is operating correctly.
     * </p>
     * 
     * @return the health check interval
     */
    Duration getHealthCheckInterval();

    /**
     * Sets the health check interval for monitoring storage health.
     * 
     * @param interval the health check interval to set
     */
    void setHealthCheckInterval(Duration interval);

    /**
     * Gets custom configuration properties.
     * 
     * <p>
     * This allows for additional configuration properties that may be
     * specific to particular storage implementations.
     * </p>
     * 
     * @return map of custom configuration properties
     */
    Map<String, Object> getCustomProperties();

    /**
     * Sets custom configuration properties.
     * 
     * @param properties the custom properties to set
     */
    void setCustomProperties(Map<String, Object> properties);

    /**
     * Gets a custom configuration property value.
     * 
     * @param key the property key
     * @return the property value, or null if not found
     */
    Object getCustomProperty(String key);

    /**
     * Sets a custom configuration property value.
     * 
     * @param key the property key
     * @param value the property value
     */
    void setCustomProperty(String key, Object value);

    /**
     * Validates the current configuration.
     * 
     * <p>
     * This method checks that all configuration values are valid and
     * consistent with each other.
     * </p>
     * 
     * @return true if the configuration is valid, false otherwise
     */
    boolean isValid();

    /**
     * Gets validation errors for the current configuration.
     * 
     * <p>
     * This method returns a list of validation error messages if the
     * configuration is invalid.
     * </p>
     * 
     * @return list of validation error messages, or empty list if valid
     */
    java.util.List<String> getValidationErrors();

    /**
     * Creates a copy of this configuration.
     * 
     * <p>
     * This method creates a deep copy of the configuration that can be
     * modified independently of the original.
     * </p>
     * 
     * @return a copy of this configuration
     */
    MetricTimeSeriesConfiguration copy();

    /**
     * Resets the configuration to default values.
     * 
     * <p>
     * This method resets all configuration values to their default
     * settings.
     * </p>
     */
    void resetToDefaults();
}
