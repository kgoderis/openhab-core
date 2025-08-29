package org.openhab.core.ai.common.monitoring.timeseries;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Time series storage interface for metrics data persistence and retrieval.
 * 
 * <p>
 * This interface provides comprehensive time series storage capabilities for metrics data,
 * including storage, querying, aggregation, and data lifecycle management. It supports
 * both real-time metrics collection and historical data analysis.
 * </p>
 * 
 * <h3>Key Features</h3>
 * <ul>
 *   <li>Time series point storage with tags and fields</li>
 *   <li>Flexible querying with time range filtering</li>
 *   <li>Aggregation functions (AVG, MIN, MAX, SUM, COUNT, FIRST, LAST)</li>
 *   <li>Automatic data cleanup and retention policies</li>
 *   <li>Series metadata management</li>
 *   <li>Performance optimization for large datasets</li>
 * </ul>
 * 
 * <h3>Usage Example</h3>
 * <pre>{@code
 * // Store a time series point
 * Map<String, String> tags = Map.of("domain", "model", "operation", "completion");
 * Map<String, Object> fields = Map.of("duration_ms", 150, "success", true);
 * storage.storeTimeSeriesPoint("metrics:model:completion", Instant.now(), tags, fields);
 * 
 * // Query time series data
 * List<TimeSeriesPoint> points = storage.queryTimeSeries("metrics:model:completion", 
 *     startTime, endTime);
 * 
 * // Query with aggregation
 * List<AggregatedPoint> aggregated = storage.queryWithAggregation("metrics:model:completion",
 *     startTime, endTime, "AVG", Duration.ofHours(1));
 * }</pre>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface MetricTimeSeriesStorage {

    /**
     * Store a time series point with tags and fields.
     * 
     * <p>
     * This method stores a single time series point with associated metadata.
     * Tags are used for indexing and filtering, while fields contain the actual
     * metric values. The seriesId uniquely identifies the time series.
     * </p>
     * 
     * @param seriesId the unique identifier for the time series
     * @param timestamp the timestamp of the data point
     * @param tags metadata tags for indexing and filtering (immutable)
     * @param fields the actual metric values (immutable)
     * @throws IllegalArgumentException if seriesId is null or empty, or if timestamp is null
     */
    void storeTimeSeriesPoint(String seriesId, Instant timestamp, 
                             Map<String, String> tags, Map<String, Object> fields);

    /**
     * Query time series data within a time range.
     * 
     * <p>
     * This method retrieves all time series points for a specific series within
     * the specified time range. Results are ordered by timestamp in ascending order.
     * </p>
     * 
     * @param seriesId the unique identifier for the time series
     * @param startTime the start time (inclusive)
     * @param endTime the end time (inclusive)
     * @return list of time series points within the range, ordered by timestamp
     * @throws IllegalArgumentException if seriesId is null or empty, or if time range is invalid
     */
    List<TimeSeriesPoint> queryTimeSeries(String seriesId, Instant startTime, Instant endTime);

    /**
     * Query with aggregation over a specified time period.
     * 
     * <p>
     * This method performs aggregation on time series data within the specified
     * time range. The aggregation is performed over the specified period, and
     * the aggregation function determines how values are combined.
     * </p>
     * 
     * @param seriesId the unique identifier for the time series
     * @param startTime the start time (inclusive)
     * @param endTime the end time (inclusive)
     * @param aggregationFunction the aggregation function (AVG, MIN, MAX, SUM, COUNT, FIRST, LAST)
     * @param aggregationPeriod the time period for aggregation
     * @return list of aggregated points within the range
     * @throws IllegalArgumentException if parameters are invalid or aggregation function is unsupported
     */
    List<AggregatedPoint> queryWithAggregation(String seriesId, Instant startTime, 
                                              Instant endTime, String aggregationFunction, 
                                              Duration aggregationPeriod);

    /**
     * Get available time series identifiers.
     * 
     * <p>
     * This method returns all available time series identifiers that have been
     * stored in the system. This can be used for discovery and management purposes.
     * </p>
     * 
     * @return set of available time series identifiers
     */
    Set<String> getAvailableSeries();

    /**
     * Clean up old data based on retention policy.
     * 
     * <p>
     * This method removes time series data that is older than the specified
     * retention period. This helps manage storage space and ensures compliance
     * with data retention policies.
     * </p>
     * 
     * @param retentionPeriod the retention period - data older than this will be removed
     * @throws IllegalArgumentException if retentionPeriod is null or negative
     */
    void cleanupOldData(Duration retentionPeriod);

    /**
     * Get metadata for a specific time series.
     * 
     * <p>
     * This method retrieves metadata information about a specific time series,
     * including creation time, last update, point count, and other relevant
     * information.
     * </p>
     * 
     * @param seriesId the unique identifier for the time series
     * @return time series metadata, or null if series doesn't exist
     * @throws IllegalArgumentException if seriesId is null or empty
     */
    TimeSeriesMetadata getSeriesMetadata(String seriesId);

    /**
     * Query time series with advanced filtering criteria.
     * 
     * <p>
     * This method provides advanced querying capabilities with complex filtering
     * criteria, including tag-based filtering, field-based filtering, and
     * pagination support.
     * </p>
     * 
     * @param criteria the query criteria containing filters and options
     * @return list of time series points matching the criteria
     * @throws IllegalArgumentException if criteria is null or invalid
     */
    List<TimeSeriesPoint> queryWithCriteria(TimeSeriesQueryCriteria criteria);

    /**
     * Get storage statistics and health information.
     * 
     * <p>
     * This method provides information about the storage system's health,
     * including total series count, total points, storage size, and other
     * relevant statistics.
     * </p>
     * 
     * @return storage statistics and health information
     */
    TimeSeriesStorageStatistics getStorageStatistics();

    /**
     * Check if the storage system is healthy and operational.
     * 
     * <p>
     * This method performs a health check on the storage system to ensure
     * it is operational and can handle requests. This is useful for monitoring
     * and alerting purposes.
     * </p>
     * 
     * @return true if the storage system is healthy, false otherwise
     */
    boolean isHealthy();
    
    // ===== Capability-Aware Storage Methods =====
    
    /**
     * Store a time series point with snapshot data.
     * 
     * <p>
     * This method stores a time series point that includes snapshot data,
     * enabling storage of any MetricsSnapshot type without conversion.
     * </p>
     * 
     * @param seriesId the unique identifier for the time series
     * @param timestamp the timestamp of the data point
     * @param tags metadata tags for indexing and filtering (immutable)
     * @param fields the actual metric values (immutable)
     * @param snapshotType the type of snapshot stored (can be null)
     * @param snapshotData the serialized snapshot data (can be null)
     * @throws IllegalArgumentException if seriesId is null or empty, or if timestamp is null
     */
    void storeTimeSeriesPoint(String seriesId, Instant timestamp, 
                             Map<String, String> tags, Map<String, Object> fields,
                             @Nullable String snapshotType, @Nullable String snapshotData);
    
    /**
     * Query time series data by snapshot type.
     * 
     * <p>
     * This method retrieves time series points that contain specific snapshot types
     * within the specified time range. This enables type-specific queries for
     * snapshot data.
     * </p>
     * 
     * @param seriesId the unique identifier for the time series
     * @param snapshotType the type of snapshot to query for
     * @param startTime the start time (inclusive)
     * @param endTime the end time (inclusive)
     * @return list of time series points with the specified snapshot type
     */
    List<TimeSeriesPoint> queryBySnapshotType(String seriesId, String snapshotType, 
                                             Instant startTime, Instant endTime);
    
    /**
     * Query time series data by capabilities.
     * 
     * <p>
     * This method retrieves time series points that have specific capabilities
     * within the specified time range. Capabilities are stored as tags and
     * can be used for efficient filtering.
     * </p>
     * 
     * @param requiredCapabilities the set of required capabilities
     * @param startTime the start time (inclusive)
     * @param endTime the end time (inclusive)
     * @return list of time series points with the required capabilities
     */
    List<TimeSeriesPoint> queryByCapabilities(Set<String> requiredCapabilities, 
                                             Instant startTime, Instant endTime);
    
    /**
     * Get series IDs that have specific capabilities.
     * 
     * <p>
     * This method returns all series IDs that contain the specified capabilities.
     * This is useful for capability-based filtering and indexing.
     * </p>
     * 
     * @param requiredCapabilities the set of required capabilities
     * @return set of series IDs that have the required capabilities
     */
    Set<String> getSeriesByCapabilities(Set<String> requiredCapabilities);
    
    /**
     * Get all snapshot types stored in the system.
     * 
     * <p>
     * This method returns all unique snapshot types that are currently stored
     * in the time series storage. This is useful for discovery and management.
     * </p>
     * 
     * @return set of all snapshot types
     */
    Set<String> getAllSnapshotTypes();
    
    /**
     * Get metadata for a specific time series.
     * 
     * <p>
     * This method retrieves metadata information for a specific time series,
     * including capabilities, snapshot types, and other relevant information.
     * </p>
     * 
     * @param seriesId the unique identifier for the time series
     * @return metadata for the time series, or null if not found
     */
    @Nullable TimeSeriesMetadata getTimeSeriesMetadata(String seriesId);
}
