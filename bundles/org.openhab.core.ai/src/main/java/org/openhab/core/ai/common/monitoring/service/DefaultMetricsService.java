package org.openhab.core.ai.common.monitoring.service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.MetricKey;
import org.openhab.core.ai.common.monitoring.api.MetricKeys;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.api.MetricsSnapshot;
import org.openhab.core.ai.common.monitoring.collector.MetricsCollector;
import org.openhab.core.ai.common.monitoring.registry.MetricsRegistry;
import org.openhab.core.ai.common.monitoring.service.factory.SnapshotFactory;
import org.openhab.core.ai.common.monitoring.service.factory.StatisticsFactory;
import org.openhab.core.ai.common.monitoring.service.snapshot.DomainAggregatedSnapshot;
import org.openhab.core.ai.common.monitoring.service.snapshot.GenericMetricsSnapshot;
import org.openhab.core.ai.common.monitoring.timeseries.MetricTimeSeriesStorage;
import org.openhab.core.ai.common.monitoring.timeseries.TimeSeriesPoint;
import org.openhab.core.ai.common.monitoring.timeseries.serialization.SnapshotSerializer;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the centralized Metrics Service.
 * 
 * <p>
 * This implementation provides:
 * - Basic operation recording with success/failure and timing
 * - Domain-specific convenience methods for models, tools, and agents
 * - Snapshot retrieval for current metrics state
 * - Statistics computation for historical insights
 * - Integration with the existing MonitoringRegistry
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@Component(service = MetricsService.class)
@NonNullByDefault
public class DefaultMetricsService implements MetricsService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultMetricsService.class);

    // Reference to the monitoring registry for unified metrics collection
    @Reference
    private @Nullable MetricsRegistry monitoringRegistry;
    
    // Reference to the time series storage for historical data persistence
    @Reference
    private @Nullable MetricTimeSeriesStorage timeSeriesStorage;
    
    // Reference to the snapshot serializer for time series conversion
    @Reference
    private @Nullable SnapshotSerializer snapshotSerializer;
    
    // Configuration for time series recording
    private boolean timeSeriesRecordingEnabled = true;
    private boolean timeSeriesRecordingOptimized = true;
    private int timeSeriesBatchSize = 100;
    private Duration timeSeriesBatchTimeout = Duration.ofSeconds(5);
    
    // Batch recording support
    private final List<TimeSeriesBatchEntry> batchBuffer = new ArrayList<>();
    private final Object batchLock = new Object();
    private volatile boolean batchProcessing = false;

    // ===== Unified Generic Retrieval with Type Safety =====

    @Override
    public <T extends MetricsSnapshot> T getSnapshot(MetricKey key, Class<T> snapshotType) {
        logger.debug("Getting snapshot for key: {} type: {}", key.id(), snapshotType.getSimpleName());

        MetricsRegistry registry = monitoringRegistry;
        if (registry == null) {
            logger.warn("Monitoring registry not available for snapshot: {}:{}", key.id(),
                    snapshotType.getSimpleName());
            return SnapshotFactory.createEmptySnapshot(snapshotType, key);
        }

        try {
            MetricsCollector collector = registry.getCollector(key);
            return SnapshotFactory.createSnapshot(collector, snapshotType, key);
        } catch (Exception e) {
            logger.warn("Failed to get snapshot for key: {} type: {}", key.id(), snapshotType.getSimpleName(), e);
            return SnapshotFactory.createEmptySnapshot(snapshotType, key);
        }
    }

    @Override
    public MetricsSnapshot getSnapshot(MetricKey key) {
        // Default to generic snapshot
        return getSnapshot(key, GenericMetricsSnapshot.class);
    }

    @Override
    public <T extends MetricsSnapshot> List<T> getSnapshotsByCapability(Class<T> capabilityType) {
        logger.debug("Getting snapshots by capability: {}", capabilityType.getSimpleName());

        MetricsRegistry registry = monitoringRegistry;
        if (registry == null) {
            logger.warn("Monitoring registry not available for capability snapshots: {}",
                    capabilityType.getSimpleName());
            return List.of();
        }

        List<T> snapshots = new ArrayList<>();

        // Get all available keys and filter by capability
        Collection<MetricKey> allKeys = registry.getAllKeys();
        for (MetricKey key : allKeys) {
            try {
                MetricsCollector collector = registry.getCollector(key);
                GenericMetricsSnapshot genericSnapshot = SnapshotFactory.createSnapshot(collector,
                        GenericMetricsSnapshot.class, key);

                // Check if the snapshot supports the requested capability
                if (genericSnapshot.hasCapability(capabilityType)) {
                    T typedSnapshot = genericSnapshot.as(capabilityType);
                    snapshots.add(typedSnapshot);
                }
            } catch (Exception e) {
                logger.warn("Failed to process snapshot for key: {}", key.id(), e);
            }
        }

        return snapshots;
    }

    @Override
    public <T extends MetricsSnapshot> List<T> getSnapshotsByDomain(String domain, Class<T> snapshotType) {
        logger.debug("Getting snapshots by domain: {} type: {}", domain, snapshotType.getSimpleName());

        MetricsRegistry registry = monitoringRegistry;
        if (registry == null) {
            logger.warn("Monitoring registry not available for domain snapshots: {}:{}", domain,
                    snapshotType.getSimpleName());
            return List.of();
        }

        List<T> snapshots = new ArrayList<>();

        // Get all available keys and filter by domain
        Collection<MetricKey> allKeys = registry.getAllKeys();
        for (MetricKey key : allKeys) {
            if (domain.equals(key.labels().get("domain"))) {
                try {
                    MetricsCollector collector = registry.getCollector(key);
                    T snapshot = SnapshotFactory.createSnapshot(collector, snapshotType, key);
                    snapshots.add(snapshot);
                } catch (Exception e) {
                    logger.warn("Failed to process snapshot for key: {}", key.id(), e);
                }
            }
        }

        return snapshots;
    }

    // ===== Recording Methods =====

    @Override
    public void recordOperation(String domain, String operation, boolean success, Duration duration) {
        try {
            MetricsRegistry registry = monitoringRegistry;
            if (registry != null) {
                // Create a metric key for the registry
                MetricKey metricKey = new MetricKeys.SimpleMetricKey(domain + "." + operation,
                        Map.of("domain", domain, "operation", operation), Set.of("counts", "latency"));
                MetricsCollector collector = registry.getCollector(metricKey);
                collector.recordExecution(success, duration.toNanos());
                logger.debug("Recorded operation in unified collector: {} (success={}, duration={}ns)", metricKey.id(),
                        success, duration.toNanos());
            } else {
                logger.warn("Monitoring registry not available for operation: {}:{}", domain, operation);
            }
            
            // Record in time series storage for historical analysis
            recordOperationInTimeSeries(domain, operation, success, duration, Map.of());
            
        } catch (Exception e) {
            logger.warn("Failed to record operation: {}:{}", domain, operation, e);
        }
    }

    @Override
    public OperationRecorder recordOperation(String domain, String operation) {
        return new OperationRecorder(this, domain, operation);
    }

    @Override
    public void recordOperationWithData(String domain, String operation, boolean success, Duration duration,
            Map<String, Object> data) {
        logger.debug("Recording operation with data: {}:{} (success={}, duration={}, dataSize={})", domain, operation,
                success, duration, data.size());

        MetricsRegistry registry = monitoringRegistry;
        if (registry != null) {
            try {
                MetricKey metricKey = new MetricKeys.SimpleMetricKey(domain + "." + operation,
                        Map.of("domain", domain, "operation", operation), Set.of("counts", "latency"));
                MetricsCollector collector = registry.getCollector(metricKey);
                collector.recordExecution(success, duration.toNanos());

                // Record data if provided
                if (!data.isEmpty()) {
                    collector.recordExtendedData(data);
                    logger.debug("Recorded data: {}", data);
                }

                logger.debug("Recorded operation with data in unified collector: {} (success={}, duration={}ns)",
                        metricKey.id(), success, duration.toNanos());
            } catch (Exception e) {
                logger.warn("Failed to record operation with data: {}:{}", domain, operation, e);
            }
        } else {
            logger.warn("Monitoring registry not available for operation with data: {}:{}", domain, operation);
        }
        
        // Record in time series storage for historical analysis
        recordOperationInTimeSeries(domain, operation, success, duration, data);
    }

    @Override
    public <T extends MetricsSnapshot> List<T> getAllSnapshots(Class<T> snapshotType) {
        logger.debug("Getting all snapshots of type: {}", snapshotType.getSimpleName());

        List<T> snapshots = new ArrayList<>();

        // For now, return empty list since we don't have a way to enumerate all available metrics
        // In a future enhancement, the MonitoringRegistry could be extended to support enumeration
        // of all available metric keys and their corresponding snapshots
        logger.debug("Snapshot enumeration not yet supported in unified collector architecture");

        return snapshots;
    }

    @Override
    public GenericMetricsSnapshot getSnapshot(String domain, String operation) {
        logger.debug("Getting generic snapshot for domain: {} operation: {}", domain, operation);

        MetricsRegistry registry = monitoringRegistry;
        if (registry == null) {
            logger.warn("Monitoring registry not available for generic snapshot: {}:{}", domain, operation);
            return GenericMetricsSnapshot.builder(domain, operation).withMetric("total_count", 0L)
                    .withMetric("success_count", 0L).withMetric("failure_count", 0L).withMetric("total_duration_ms", 0L)
                    .withMetric("success_rate", 0.0).build();
        }

        try {
            MetricKey metricKey = new MetricKeys.SimpleMetricKey(domain + "." + operation,
                    Map.of("domain", domain, "operation", operation), Set.of("counts", "latency"));
            return getSnapshot(metricKey, GenericMetricsSnapshot.class);
        } catch (Exception e) {
            logger.warn("Failed to get generic snapshot for: {}:{}", domain, operation, e);
            return GenericMetricsSnapshot.builder(domain, operation).withMetric("total_count", 0L)
                    .withMetric("success_count", 0L).withMetric("failure_count", 0L).withMetric("total_duration_ms", 0L)
                    .withMetric("success_rate", 0.0).build();
        }
    }

    // ===== Generic Statistics Retrieval =====

    @Override
    public <T extends org.openhab.core.ai.common.monitoring.service.statistics.StatisticsSnapshot> T getStatistics(
            MetricKey key, Class<T> statisticsType, Duration timeRange) {
        
        try {
            // Get historical data from time series storage
            Instant endTime = Instant.now();
            Instant startTime = endTime.minus(timeRange);
            
            String seriesId = createSeriesId(key);
            MetricTimeSeriesStorage storage = timeSeriesStorage;
            if (storage == null) {
                logger.warn("Time series storage not available - returning empty statistics");
                return StatisticsFactory.createStatistics(new ArrayList<>(), statisticsType, key, timeRange);
            }
            
            List<TimeSeriesPoint> points = storage.queryTimeSeries(seriesId, startTime, endTime);
            
            // Convert time series points back to MetricsSnapshot objects
            List<MetricsSnapshot> snapshots = convertTimeSeriesToSnapshots(points, key);
            
            return StatisticsFactory.createStatistics(snapshots, statisticsType, key, timeRange);
            
        } catch (Exception e) {
            logger.error("Failed to get statistics for key: {}", key.id(), e);
            return StatisticsFactory.createStatistics(new ArrayList<>(), statisticsType, key, timeRange);
        }
    }

    @Override
    public <T extends org.openhab.core.ai.common.monitoring.service.statistics.StatisticsSnapshot> List<T> getStatisticsByCapability(
            Class<T> capabilityType, Duration timeRange) {
        List<T> statistics = new ArrayList<>();
        
        try {
            MetricTimeSeriesStorage storage = timeSeriesStorage;
            if (storage == null) {
                logger.warn("Time series storage not available - returning empty statistics list");
                return statistics;
            }
            
            // Get all available series
            Set<String> availableSeries = storage.getAvailableSeries();
            
            for (String seriesId : availableSeries) {
                try {
                    // Extract MetricKey from seriesId
                    MetricKey key = extractMetricKeyFromSeriesId(seriesId);
                    
                    // Check if key supports the requested capability
                    if (key.capabilities().stream().anyMatch(capabilityType.getSimpleName().toLowerCase()::contains)) {
                        T statistic = getStatistics(key, capabilityType, timeRange);
                        statistics.add(statistic);
                    }
                } catch (Exception e) {
                    logger.debug("Skipping series {} for capability {}: {}", seriesId, capabilityType.getSimpleName(), e.getMessage());
                }
            }
            
        } catch (Exception e) {
            logger.error("Failed to get statistics by capability: {}", capabilityType.getSimpleName(), e);
        }
        
        return statistics;
    }

    @Override
    public <T extends org.openhab.core.ai.common.monitoring.service.statistics.StatisticsSnapshot> List<T> getStatisticsByDomain(
            String domain, Class<T> statisticsType, Duration timeRange) {
        List<T> statistics = new ArrayList<>();
        
        try {
            MetricTimeSeriesStorage storage = timeSeriesStorage;
            if (storage == null) {
                logger.warn("Time series storage not available - returning empty statistics list");
                return statistics;
            }
            
            // Query time series by domain tag
            List<TimeSeriesPoint> domainPoints = queryTimeSeriesByTag("domain", domain, timeRange);
            
            // Group points by series and convert to snapshots
            Map<String, List<TimeSeriesPoint>> pointsBySeries = domainPoints.stream()
                .collect(Collectors.groupingBy(this::extractSeriesIdFromPoint));
            
            for (Map.Entry<String, List<TimeSeriesPoint>> entry : pointsBySeries.entrySet()) {
                try {
                    String seriesId = entry.getKey();
                    List<TimeSeriesPoint> points = entry.getValue();
                    
                    MetricKey key = extractMetricKeyFromSeriesId(seriesId);
                    List<MetricsSnapshot> snapshots = convertTimeSeriesToSnapshots(points, key);
                    
                    T statistic = StatisticsFactory.createStatistics(snapshots, statisticsType, key, timeRange);
                    statistics.add(statistic);
                    
                } catch (Exception e) {
                    logger.debug("Skipping series {} for domain {}: {}", entry.getKey(), domain, e.getMessage());
                }
            }
            
        } catch (Exception e) {
            logger.error("Failed to get statistics by domain: {}", domain, e);
        }
        
        return statistics;
    }

    // ===== Phase 4: Advanced Features - Aggregation and Filtering =====

    @Override
    public <T extends MetricsSnapshot> List<T> getAggregatedSnapshots(String domain, Class<T> snapshotType) {
        logger.debug("Getting aggregated snapshots for domain: {} type: {}", domain, snapshotType.getSimpleName());

        MetricsRegistry registry = monitoringRegistry;
        if (registry == null) {
            logger.warn("Monitoring registry not available for aggregated snapshots: {}:{}", domain,
                    snapshotType.getSimpleName());
            return List.of();
        }

        List<T> aggregatedSnapshots = new ArrayList<>();

        try {
            // Get all keys for the domain
            Collection<MetricKey> domainKeys = registry.getKeysByDomain(domain);

            // Group snapshots by operation type for aggregation
            Map<String, List<MetricsSnapshot>> operationGroups = new java.util.HashMap<>();

            for (MetricKey key : domainKeys) {
                try {
                    MetricsCollector collector = registry.getCollector(key);
                    GenericMetricsSnapshot snapshot = SnapshotFactory.createSnapshot(collector,
                            GenericMetricsSnapshot.class, key);

                    String operation = key.labels().get("operation");
                    if (operation != null) {
                        operationGroups.computeIfAbsent(operation, k -> new ArrayList<>()).add(snapshot);
                    }
                } catch (Exception e) {
                    logger.warn("Failed to process snapshot for key: {}", key.id(), e);
                }
            }

            // Create aggregated snapshots for each operation group
            for (Map.Entry<String, List<MetricsSnapshot>> entry : operationGroups.entrySet()) {
                String operation = entry.getKey();
                List<MetricsSnapshot> snapshots = entry.getValue();

                if (!snapshots.isEmpty()) {
                    // Aggregate the snapshots
                    T aggregatedSnapshot = createAggregatedSnapshot(domain, operation, snapshots, snapshotType);
                    if (aggregatedSnapshot != null) {
                        aggregatedSnapshots.add(aggregatedSnapshot);
                    }
                }
            }

        } catch (Exception e) {
            logger.warn("Failed to get aggregated snapshots for domain: {}", domain, e);
        }

        return aggregatedSnapshots;
    }

    @Override
    public <T extends MetricsSnapshot> List<T> getSnapshotsInRange(String domain, String operation,
            Class<T> snapshotType, Instant start, Instant end) {
        logger.debug("Getting snapshots in range for domain: {} operation: {} type: {} from {} to {}", domain,
                operation, snapshotType.getSimpleName(), start, end);

        MetricsRegistry registry = monitoringRegistry;
        if (registry == null) {
            logger.warn("Monitoring registry not available for time range snapshots: {}:{}", domain, operation);
            return List.of();
        }

        List<T> rangeSnapshots = new ArrayList<>();

        try {
            // Get all keys for the domain and operation
            Collection<MetricKey> allKeys = registry.getAllKeys();

            for (MetricKey key : allKeys) {
                if (domain.equals(key.labels().get("domain"))
                        && (operation == null || operation.equals(key.labels().get("operation")))) {

                    try {
                        MetricsCollector collector = registry.getCollector(key);
                        GenericMetricsSnapshot snapshot = SnapshotFactory.createSnapshot(collector,
                                GenericMetricsSnapshot.class, key);

                        // Check if snapshot timestamp is within range
                        if (snapshot.getTimestamp().isAfter(start) && snapshot.getTimestamp().isBefore(end)) {
                            T typedSnapshot = snapshot.as(snapshotType);
                            rangeSnapshots.add(typedSnapshot);
                        }
                    } catch (Exception e) {
                        logger.warn("Failed to process snapshot for key: {}", key.id(), e);
                    }
                }
            }

        } catch (Exception e) {
            logger.warn("Failed to get snapshots in range for domain: {} operation: {}", domain, operation, e);
        }

        return rangeSnapshots;
    }

    @Override
    public DomainAggregatedSnapshot getDomainAggregatedSnapshot(String domain) {
        logger.debug("Getting domain aggregated snapshot for domain: {}", domain);

        MetricsRegistry registry = monitoringRegistry;
        if (registry == null) {
            logger.warn("Monitoring registry not available for domain aggregated snapshot: {}", domain);
            return DomainAggregatedSnapshot.empty(domain);
        }

        try {
            // Get all keys for the domain
            Collection<MetricKey> domainKeys = registry.getKeysByDomain(domain);

            long totalOperations = 0L;
            long successfulOperations = 0L;
            long failedOperations = 0L;
            long totalDurationNanos = 0L;
            List<MetricsSnapshot> operationSnapshots = new ArrayList<>();

            for (MetricKey key : domainKeys) {
                try {
                    MetricsCollector collector = registry.getCollector(key);
                    GenericMetricsSnapshot snapshot = SnapshotFactory.createSnapshot(collector,
                            GenericMetricsSnapshot.class, key);

                    // Aggregate the metrics
                    totalOperations += snapshot.total();
                    successfulOperations += snapshot.success();
                    failedOperations += snapshot.failure();
                    totalDurationNanos += snapshot.totalDurationNanos();
                    operationSnapshots.add(snapshot);

                } catch (Exception e) {
                    logger.warn("Failed to process snapshot for key: {}", key.id(), e);
                }
            }

            // Calculate average success rate
            double averageSuccessRate = totalOperations > 0 ? (successfulOperations * 100.0) / totalOperations : 0.0;

            return new DomainAggregatedSnapshot(domain, totalOperations, successfulOperations, failedOperations,
                    totalDurationNanos, averageSuccessRate, operationSnapshots, Instant.now());

        } catch (Exception e) {
            logger.warn("Failed to get domain aggregated snapshot for domain: {}", domain, e);
            return DomainAggregatedSnapshot.empty(domain);
        }
    }

    // ===== Private Helper Methods =====

    /**
     * Create an aggregated snapshot from a list of snapshots.
     * 
     * @param <T> the snapshot type
     * @param domain the domain
     * @param operation the operation
     * @param snapshots the snapshots to aggregate
     * @param snapshotType the target snapshot type
     * @return aggregated snapshot or null if aggregation fails
     */
    @SuppressWarnings("unchecked")
    private <T extends MetricsSnapshot> T createAggregatedSnapshot(String domain, String operation,
            List<MetricsSnapshot> snapshots, Class<T> snapshotType) {
        try {
            // Aggregate basic metrics
            long totalOperations = snapshots.stream().filter(s -> s instanceof GenericMetricsSnapshot)
                    .mapToLong(s -> ((GenericMetricsSnapshot) s).total()).sum();
            long successfulOperations = snapshots.stream().filter(s -> s instanceof GenericMetricsSnapshot)
                    .mapToLong(s -> ((GenericMetricsSnapshot) s).success()).sum();
            long failedOperations = snapshots.stream().filter(s -> s instanceof GenericMetricsSnapshot)
                    .mapToLong(s -> ((GenericMetricsSnapshot) s).failure()).sum();
            long totalDurationNanos = snapshots.stream().filter(s -> s instanceof GenericMetricsSnapshot)
                    .mapToLong(s -> ((GenericMetricsSnapshot) s).totalDurationNanos()).sum();

            // Create a generic aggregated snapshot
            GenericMetricsSnapshot aggregated = GenericMetricsSnapshot.builder(domain, operation)
                    .withMetric("total_count", totalOperations).withMetric("success_count", successfulOperations)
                    .withMetric("failure_count", failedOperations)
                    .withMetric("total_duration_nanos", totalDurationNanos).withMetric("success_rate",
                            totalOperations > 0 ? (successfulOperations * 100.0) / totalOperations : 0.0)
                    .build();

            // Convert to the requested type if possible
            if (snapshotType == GenericMetricsSnapshot.class) {
                return (T) aggregated;
            } else {
                return aggregated.as(snapshotType);
            }
        } catch (Exception e) {
            logger.warn("Failed to create aggregated snapshot for domain: {} operation: {}", domain, operation, e);
            return null;
        }
    }
    
    // ===== Time Series Integration Methods =====
    
    /**
     * Records an operation in the time series storage for historical analysis.
     * 
     * @param domain the domain of the operation
     * @param operation the operation name
     * @param success whether the operation was successful
     * @param duration the duration of the operation
     * @param data additional data associated with the operation
     */
    private void recordOperationInTimeSeries(String domain, String operation, boolean success, Duration duration, 
                                           Map<String, Object> data) {
        // Check if time series recording is enabled
        if (!timeSeriesRecordingEnabled) {
            logger.debug("Time series recording disabled for operation: {}:{}", domain, operation);
            return;
        }
        
        try {
            MetricTimeSeriesStorage storage = timeSeriesStorage;
            if (storage != null) {
                // Create optimized time series ID for better performance
                String seriesId = createOptimizedSeriesId(domain, operation);
                
                // Create optimized tags for the time series point
                Map<String, String> tags = createOptimizedTags(domain, operation, success);
                
                // Create optimized fields for the time series point
                Map<String, Object> fields = createOptimizedFields(duration, success, data);
                
                // Store the time series point with performance optimization
                storeTimeSeriesPointOptimized(storage, seriesId, tags, fields);
                
                logger.debug("Recorded operation in time series storage: {} (success={}, duration={}ms)", 
                           seriesId, success, duration.toMillis());
            } else {
                logger.debug("Time series storage not available for operation: {}:{}", domain, operation);
            }
        } catch (Exception e) {
            logger.warn("Failed to record operation in time series storage: {}:{}", domain, operation, e);
        }
    }
    
    /**
     * Creates an optimized series ID for better storage performance.
     * 
     * @param domain the domain name
     * @param operation the operation name
     * @return optimized series ID
     */
    private String createOptimizedSeriesId(String domain, String operation) {
        // Use shorter prefix and normalized names for better performance
        return "m:" + domain + ":" + operation;
    }
    
    /**
     * Creates optimized tags for time series storage.
     * 
     * @param domain the domain name
     * @param operation the operation name
     * @param success the success status
     * @return optimized tags map
     */
    private Map<String, String> createOptimizedTags(String domain, String operation, boolean success) {
        Map<String, String> tags = new HashMap<>();
        tags.put("d", domain);  // Shortened key for performance
        tags.put("o", operation);  // Shortened key for performance
        tags.put("s", success ? "1" : "0");  // Use numeric values for better compression
        return tags;
    }
    
    /**
     * Creates optimized fields for time series storage.
     * 
     * @param duration the operation duration
     * @param success the success status
     * @param data additional data
     * @return optimized fields map
     */
    private Map<String, Object> createOptimizedFields(Duration duration, boolean success, Map<String, Object> data) {
        Map<String, Object> fields = new HashMap<>();
        fields.put("dur", duration.toMillis());  // Shortened key for performance
        fields.put("ok", success ? 1 : 0);  // Use numeric values for better compression
        
        if (data != null && !data.isEmpty()) {
            // Only include essential data to reduce storage size
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                
                // Skip large objects and only include essential metrics
                if (isEssentialMetric(key, value)) {
                    fields.put(key, value);
                }
            }
        }
        
        return fields;
    }
    
    /**
     * Determines if a metric is essential for storage optimization.
     * 
     * @param key the metric key
     * @param value the metric value
     * @return true if the metric is essential
     */
    private boolean isEssentialMetric(String key, Object value) {
        // Skip large objects and non-essential data
        if (value == null) {
            return false;
        }
        
        // Include numeric values and small strings
        if (value instanceof Number || value instanceof Boolean) {
            return true;
        }
        
        if (value instanceof String) {
            String str = (String) value;
            return str.length() <= 100;  // Only include short strings
        }
        
        return false;
    }
    
    /**
     * Stores time series point with performance optimization.
     * 
     * @param storage the time series storage
     * @param seriesId the series ID
     * @param tags the tags
     * @param fields the fields
     */
    private void storeTimeSeriesPointOptimized(MetricTimeSeriesStorage storage, String seriesId, 
                                             Map<String, String> tags, Map<String, Object> fields) {
        try {
            // Use current timestamp for consistency
            Instant timestamp = Instant.now();
            
            // Store with error handling
            storage.storeTimeSeriesPoint(seriesId, timestamp, tags, fields);
            
        } catch (Exception e) {
            logger.warn("Failed to store optimized time series point: {}", e.getMessage());
        }
    }
    
    /**
     * Gets historical metrics for a specific domain and operation within a time range.
     * 
     * @param domain the domain of the operation
     * @param operation the operation name
     * @param startTime the start time for the query
     * @param endTime the end time for the query
     * @return list of historical metrics points
     */
    public List<Map<String, Object>> getHistoricalMetrics(String domain, String operation, 
                                                         Instant startTime, Instant endTime) {
        try {
            MetricTimeSeriesStorage storage = timeSeriesStorage;
            if (storage != null) {
                String seriesId = "metrics:" + domain + ":" + operation;
                var points = storage.queryTimeSeries(seriesId, startTime, endTime);
                
                // Convert time series points to map format for easier consumption
                return points.stream()
                    .map(point -> {
                        Map<String, Object> result = new HashMap<>();
                        result.put("timestamp", point.timestamp());
                        result.put("tags", point.tags());
                        result.putAll(point.fields());
                        return result;
                    })
                    .collect(Collectors.toList());
            } else {
                logger.warn("Time series storage not available for historical metrics query");
                return new ArrayList<>();
            }
        } catch (Exception e) {
            logger.error("Failed to get historical metrics for {}:{}", domain, operation, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Gets aggregated metrics for a specific domain and operation within a time range.
     * 
     * @param domain the domain of the operation
     * @param operation the operation name
     * @param startTime the start time for the query
     * @param endTime the end time for the query
     * @param aggregationFunction the aggregation function to apply
     * @param aggregationPeriod the time period for aggregation
     * @return list of aggregated metrics points
     */
    public List<Map<String, Object>> getAggregatedMetrics(String domain, String operation,
                                                         Instant startTime, Instant endTime,
                                                         String aggregationFunction, Duration aggregationPeriod) {
        try {
            MetricTimeSeriesStorage storage = timeSeriesStorage;
            if (storage != null) {
                String seriesId = "metrics:" + domain + ":" + operation;
                var aggregatedPoints = storage.queryWithAggregation(seriesId, startTime, endTime, 
                                                                   aggregationFunction, aggregationPeriod);
                
                // Convert aggregated points to map format
                return aggregatedPoints.stream()
                    .map(point -> {
                        Map<String, Object> result = new HashMap<>();
                        result.put("timestamp", point.timestamp());
                        result.put("period", point.period());
                        result.put("aggregation_function", point.aggregationFunction());
                        result.putAll(point.aggregatedValues());
                        return result;
                    })
                    .collect(Collectors.toList());
            } else {
                logger.warn("Time series storage not available for aggregated metrics query");
                return new ArrayList<>();
            }
        } catch (Exception e) {
            logger.error("Failed to get aggregated metrics for {}:{}", domain, operation, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Gets analytics and statistics for a specific domain and operation.
     * 
     * @param domain the domain of the operation
     * @param operation the operation name
     * @param startTime the start time for the analysis
     * @param endTime the end time for the analysis
     * @return map containing various analytics and statistics
     */
    public Map<String, Object> getMetricsAnalytics(String domain, String operation, 
                                                  Instant startTime, Instant endTime) {
        try {
            MetricTimeSeriesStorage storage = timeSeriesStorage;
            if (storage != null) {
                String seriesId = "metrics:" + domain + ":" + operation;
                var points = storage.queryTimeSeries(seriesId, startTime, endTime);
                
                if (points.isEmpty()) {
                    return Map.of("message", "No data available for the specified time range");
                }
                
                // Calculate basic statistics
                List<Double> durations = points.stream()
                    .map(point -> {
                        Object duration = point.fields().get("duration_ms");
                        if (duration instanceof Number) {
                            return ((Number) duration).doubleValue();
                        }
                        return 0.0;
                    })
                    .collect(Collectors.toList());
                
                List<Boolean> successes = points.stream()
                    .map(point -> {
                        Object success = point.fields().get("success");
                        if (success instanceof Boolean) {
                            return (Boolean) success;
                        }
                        return false;
                    })
                    .collect(Collectors.toList());
                
                // Calculate statistics
                double avgDuration = durations.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                double minDuration = durations.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
                double maxDuration = durations.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
                long successCount = successes.stream().mapToLong(b -> b ? 1 : 0).sum();
                double successRate = (double) successCount / successes.size() * 100.0;
                
                Map<String, Object> analytics = new HashMap<>();
                analytics.put("total_operations", points.size());
                analytics.put("successful_operations", successCount);
                analytics.put("failed_operations", points.size() - successCount);
                analytics.put("success_rate_percent", successRate);
                analytics.put("avg_duration_ms", avgDuration);
                analytics.put("min_duration_ms", minDuration);
                analytics.put("max_duration_ms", maxDuration);
                analytics.put("time_range_start", startTime);
                analytics.put("time_range_end", endTime);
                
                return analytics;
            } else {
                logger.warn("Time series storage not available for analytics query");
                return Map.of("error", "Time series storage not available");
            }
        } catch (Exception e) {
            logger.error("Failed to get metrics analytics for {}:{}", domain, operation, e);
            return Map.of("error", "Failed to calculate analytics: " + e.getMessage());
        }
    }
    
    /**
     * Performs trend analysis on metrics data to identify patterns and changes.
     * 
     * @param domain the domain of the operation
     * @param operation the operation name
     * @param startTime the start time for analysis
     * @param endTime the end time for analysis
     * @return map containing trend analysis results
     */
    public Map<String, Object> getMetricsTrendAnalysis(String domain, String operation, 
                                                      Instant startTime, Instant endTime) {
        try {
            MetricTimeSeriesStorage storage = timeSeriesStorage;
            if (storage != null) {
                String seriesId = createOptimizedSeriesId(domain, operation);
                var points = storage.queryTimeSeries(seriesId, startTime, endTime);
                
                if (points.isEmpty()) {
                    return Map.of("error", "No data available for trend analysis");
                }
                
                Map<String, Object> trendAnalysis = new HashMap<>();
                
                // Analyze duration trends
                List<Double> durations = points.stream()
                    .map(point -> {
                        Object dur = point.fields().get("dur");
                        if (dur instanceof Number) {
                            return ((Number) dur).doubleValue();
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
                
                if (!durations.isEmpty()) {
                    trendAnalysis.put("duration_trend", analyzeTrend(durations));
                }
                
                // Analyze success rate trends
                List<Double> successRates = calculateSuccessRatesByTimeWindow(points, Duration.ofHours(1));
                if (!successRates.isEmpty()) {
                    trendAnalysis.put("success_rate_trend", analyzeTrend(successRates));
                }
                
                // Detect anomalies
                trendAnalysis.put("anomalies", detectAnomalies(points));
                
                // Pattern detection
                trendAnalysis.put("patterns", detectPatterns(points));
                
                return trendAnalysis;
            } else {
                logger.warn("Time series storage not available for trend analysis");
                return Map.of("error", "Time series storage not available");
            }
        } catch (Exception e) {
            logger.error("Failed to get trend analysis for {}:{}", domain, operation, e);
            return Map.of("error", "Failed to calculate trend analysis: " + e.getMessage());
        }
    }
    
    /**
     * Analyzes trend in a list of numeric values.
     * 
     * @param values the values to analyze
     * @return trend analysis results
     */
    private Map<String, Object> analyzeTrend(List<Double> values) {
        if (values.size() < 2) {
            return Map.of("trend", "insufficient_data");
        }
        
        // Calculate linear regression
        double sumX = 0, sumY = 0, sumXY = 0, sumXX = 0;
        int n = values.size();
        
        for (int i = 0; i < n; i++) {
            double x = i;
            double y = values.get(i);
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumXX += x * x;
        }
        
        double slope = (n * sumXY - sumX * sumY) / (n * sumXX - sumX * sumX);
        double intercept = (sumY - slope * sumX) / n;
        
        // Calculate R-squared
        double yMean = sumY / n;
        double ssRes = 0, ssTot = 0;
        for (int i = 0; i < n; i++) {
            double y = values.get(i);
            double yPred = slope * i + intercept;
            ssRes += Math.pow(y - yPred, 2);
            ssTot += Math.pow(y - yMean, 2);
        }
        double rSquared = 1 - (ssRes / ssTot);
        
        // Determine trend direction
        String trend;
        if (Math.abs(slope) < 0.01) {
            trend = "stable";
        } else if (slope > 0) {
            trend = "increasing";
        } else {
            trend = "decreasing";
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("trend", trend);
        result.put("slope", slope);
        result.put("r_squared", rSquared);
        result.put("confidence", rSquared > 0.7 ? "high" : rSquared > 0.4 ? "medium" : "low");
        result.put("change_percent", calculateChangePercent(values));
        
        return result;
    }
    
    /**
     * Calculates the percentage change from first to last value.
     * 
     * @param values the values to analyze
     * @return percentage change
     */
    private double calculateChangePercent(List<Double> values) {
        if (values.size() < 2) {
            return 0.0;
        }
        
        double first = values.get(0);
        double last = values.get(values.size() - 1);
        
        if (first == 0) {
            return last == 0 ? 0.0 : 100.0;
        }
        
        return ((last - first) / first) * 100.0;
    }
    
    /**
     * Calculates success rates by time window.
     * 
     * @param points the time series points
     * @param windowSize the time window size
     * @return list of success rates
     */
    private List<Double> calculateSuccessRatesByTimeWindow(List<org.openhab.core.ai.common.monitoring.timeseries.TimeSeriesPoint> points, 
                                                          Duration windowSize) {
        if (points.isEmpty()) {
            return List.of();
        }
        
        // Group points by time window
        Map<Instant, List<org.openhab.core.ai.common.monitoring.timeseries.TimeSeriesPoint>> windows = new HashMap<>();
        
        for (org.openhab.core.ai.common.monitoring.timeseries.TimeSeriesPoint point : points) {
            Instant windowStart = point.timestamp().truncatedTo(java.time.temporal.ChronoUnit.HOURS);
            windows.computeIfAbsent(windowStart, k -> new ArrayList<>()).add(point);
        }
        
        // Calculate success rate for each window
        return windows.values().stream()
            .map(windowPoints -> {
                long total = windowPoints.size();
                long successful = windowPoints.stream()
                    .mapToLong(point -> {
                        Object ok = point.fields().get("ok");
                        return (ok instanceof Number && ((Number) ok).intValue() == 1) ? 1 : 0;
                    })
                    .sum();
                return total > 0 ? (double) successful / total * 100.0 : 0.0;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Detects anomalies in time series data.
     * 
     * @param points the time series points
     * @return list of detected anomalies
     */
    private List<Map<String, Object>> detectAnomalies(List<org.openhab.core.ai.common.monitoring.timeseries.TimeSeriesPoint> points) {
        List<Map<String, Object>> anomalies = new ArrayList<>();
        
        if (points.size() < 3) {
            return anomalies;
        }
        
        // Extract durations for anomaly detection
        List<Double> durations = points.stream()
            .map(point -> {
                Object dur = point.fields().get("dur");
                if (dur instanceof Number) {
                    return ((Number) dur).doubleValue();
                }
                return null;
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        
        if (durations.size() < 3) {
            return anomalies;
        }
        
        // Calculate mean and standard deviation
        double mean = durations.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double variance = durations.stream()
            .mapToDouble(d -> Math.pow(d - mean, 2))
            .average()
            .orElse(0.0);
        double stdDev = Math.sqrt(variance);
        
        // Detect outliers (values more than 2 standard deviations from mean)
        double threshold = 2.0 * stdDev;
        
        for (int i = 0; i < durations.size(); i++) {
            double duration = durations.get(i);
            if (Math.abs(duration - mean) > threshold) {
                Map<String, Object> anomaly = new HashMap<>();
                anomaly.put("timestamp", points.get(i).timestamp());
                anomaly.put("value", duration);
                anomaly.put("type", duration > mean ? "high" : "low");
                anomaly.put("severity", Math.abs(duration - mean) > 3 * stdDev ? "high" : "medium");
                anomaly.put("deviation", Math.abs(duration - mean) / stdDev);
                anomalies.add(anomaly);
            }
        }
        
        return anomalies;
    }
    
    /**
     * Detects patterns in time series data.
     * 
     * @param points the time series points
     * @return detected patterns
     */
    private Map<String, Object> detectPatterns(List<org.openhab.core.ai.common.monitoring.timeseries.TimeSeriesPoint> points) {
        Map<String, Object> patterns = new HashMap<>();
        
        if (points.size() < 10) {
            return patterns;
        }
        
        // Detect daily patterns
        Map<Integer, List<Double>> hourlyData = new HashMap<>();
        for (org.openhab.core.ai.common.monitoring.timeseries.TimeSeriesPoint point : points) {
            int hour = point.timestamp().atZone(java.time.ZoneId.systemDefault()).getHour();
            Object dur = point.fields().get("dur");
            if (dur instanceof Number) {
                hourlyData.computeIfAbsent(hour, k -> new ArrayList<>()).add(((Number) dur).doubleValue());
            }
        }
        
        // Calculate average duration by hour
        Map<Integer, Double> hourlyAverages = new HashMap<>();
        for (Map.Entry<Integer, List<Double>> entry : hourlyData.entrySet()) {
            double avg = entry.getValue().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            hourlyAverages.put(entry.getKey(), avg);
        }
        
        if (!hourlyAverages.isEmpty()) {
            patterns.put("hourly_pattern", hourlyAverages);
            
            // Find peak hours
            int peakHour = hourlyAverages.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(0);
            patterns.put("peak_hour", peakHour);
        }
        
        // Detect weekly patterns
        Map<Integer, List<Double>> dailyData = new HashMap<>();
        for (org.openhab.core.ai.common.monitoring.timeseries.TimeSeriesPoint point : points) {
            int dayOfWeek = point.timestamp().atZone(java.time.ZoneId.systemDefault()).getDayOfWeek().getValue();
            Object dur = point.fields().get("dur");
            if (dur instanceof Number) {
                dailyData.computeIfAbsent(dayOfWeek, k -> new ArrayList<>()).add(((Number) dur).doubleValue());
            }
        }
        
        Map<Integer, Double> dailyAverages = new HashMap<>();
        for (Map.Entry<Integer, List<Double>> entry : dailyData.entrySet()) {
            double avg = entry.getValue().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            dailyAverages.put(entry.getKey(), avg);
        }
        
        if (!dailyAverages.isEmpty()) {
            patterns.put("daily_pattern", dailyAverages);
        }
        
        return patterns;
    }
    
    // ===== Time Series Configuration Methods =====
    
    /**
     * Enables or disables time series recording.
     * 
     * @param enabled true to enable time series recording, false to disable
     */
    public void setTimeSeriesRecordingEnabled(boolean enabled) {
        this.timeSeriesRecordingEnabled = enabled;
        logger.info("Time series recording {} for DefaultMetricsService", enabled ? "enabled" : "disabled");
    }
    
    /**
     * Gets the current time series recording status.
     * 
     * @return true if time series recording is enabled, false otherwise
     */
    public boolean isTimeSeriesRecordingEnabled() {
        return timeSeriesRecordingEnabled;
    }
    
    /**
     * Enables or disables optimized time series recording.
     * 
     * @param optimized true to enable optimized recording, false to disable
     */
    public void setTimeSeriesRecordingOptimized(boolean optimized) {
        this.timeSeriesRecordingOptimized = optimized;
        logger.info("Optimized time series recording {} for DefaultMetricsService", optimized ? "enabled" : "disabled");
    }
    
    /**
     * Gets the current optimized time series recording status.
     * 
     * @return true if optimized recording is enabled, false otherwise
     */
    public boolean isTimeSeriesRecordingOptimized() {
        return timeSeriesRecordingOptimized;
    }
    
    /**
     * Sets the batch size for time series recording.
     * 
     * @param batchSize the batch size for time series recording
     */
    public void setTimeSeriesBatchSize(int batchSize) {
        if (batchSize <= 0) {
            throw new IllegalArgumentException("Batch size must be positive");
        }
        this.timeSeriesBatchSize = batchSize;
        logger.info("Time series batch size set to {} for DefaultMetricsService", batchSize);
    }
    
    /**
     * Gets the current time series batch size.
     * 
     * @return the current batch size
     */
    public int getTimeSeriesBatchSize() {
        return timeSeriesBatchSize;
    }
    
    /**
     * Sets the batch timeout for time series recording.
     * 
     * @param timeout the batch timeout for time series recording
     */
    public void setTimeSeriesBatchTimeout(Duration timeout) {
        if (timeout == null || timeout.isNegative()) {
            throw new IllegalArgumentException("Timeout must be non-negative");
        }
        this.timeSeriesBatchTimeout = timeout;
        logger.info("Time series batch timeout set to {} for DefaultMetricsService", timeout);
    }
    
    /**
     * Gets the current time series batch timeout.
     * 
     * @return the current batch timeout
     */
    public Duration getTimeSeriesBatchTimeout() {
        return timeSeriesBatchTimeout;
    }
    
    /**
     * Gets the current time series configuration as a map.
     * 
     * @return map containing current time series configuration
     */
    public Map<String, Object> getTimeSeriesConfiguration() {
        Map<String, Object> config = new HashMap<>();
        config.put("enabled", timeSeriesRecordingEnabled);
        config.put("optimized", timeSeriesRecordingOptimized);
        config.put("batchSize", timeSeriesBatchSize);
        config.put("batchTimeout", timeSeriesBatchTimeout.toString());
        return config;
    }
    
    // ===== Batch Recording Support =====
    
    /**
     * Records an operation in the batch buffer for later processing.
     * 
     * @param domain the domain name
     * @param operation the operation name
     * @param success the success status
     * @param duration the operation duration
     * @param data additional data
     */
    private void recordOperationInBatch(String domain, String operation, boolean success, Duration duration, 
                                      Map<String, Object> data) {
        if (!timeSeriesRecordingEnabled) {
            return;
        }
        
        synchronized (batchLock) {
            // Create batch entry
            TimeSeriesBatchEntry entry = new TimeSeriesBatchEntry(
                createOptimizedSeriesId(domain, operation),
                createOptimizedTags(domain, operation, success),
                createOptimizedFields(duration, success, data),
                Instant.now()
            );
            
            batchBuffer.add(entry);
            
            // Check if we need to process the batch
            if (batchBuffer.size() >= timeSeriesBatchSize) {
                processBatch();
            }
        }
    }
    
    /**
     * Processes the current batch buffer.
     */
    private void processBatch() {
        if (batchProcessing || batchBuffer.isEmpty()) {
            return;
        }
        
        batchProcessing = true;
        
        try {
            MetricTimeSeriesStorage storage = timeSeriesStorage;
            if (storage != null) {
                // Create a copy of the current batch
                List<TimeSeriesBatchEntry> currentBatch;
                synchronized (batchLock) {
                    currentBatch = new ArrayList<>(batchBuffer);
                    batchBuffer.clear();
                }
                
                // Store each batch entry individually since we have mixed series IDs
                for (TimeSeriesBatchEntry entry : currentBatch) {
                    storage.storeTimeSeriesPoint(entry.seriesId(), entry.timestamp(), entry.tags(), entry.fields());
                }
                
                logger.debug("Processed batch of {} time series points", currentBatch.size());
            }
        } catch (Exception e) {
            logger.warn("Failed to process time series batch: {}", e.getMessage());
        } finally {
            batchProcessing = false;
        }
    }
    
    /**
     * Forces processing of the current batch buffer.
     */
    public void flushBatch() {
        synchronized (batchLock) {
            if (!batchBuffer.isEmpty()) {
                processBatch();
            }
        }
    }
    
    /**
     * Gets the current batch buffer size.
     * 
     * @return the current batch buffer size
     */
    public int getBatchBufferSize() {
        synchronized (batchLock) {
            return batchBuffer.size();
        }
    }
    
    /**
     * Represents a single entry in the time series batch buffer.
     */
    private static class TimeSeriesBatchEntry {
        private final String seriesId;
        private final Map<String, String> tags;
        private final Map<String, Object> fields;
        private final Instant timestamp;
        
        public TimeSeriesBatchEntry(String seriesId, Map<String, String> tags, 
                                  Map<String, Object> fields, Instant timestamp) {
            this.seriesId = seriesId;
            this.tags = tags;
            this.fields = fields;
            this.timestamp = timestamp;
        }
        
        public String seriesId() { return seriesId; }
        public Map<String, String> tags() { return tags; }
        public Map<String, Object> fields() { return fields; }
        public Instant timestamp() { return timestamp; }
    }
    
    // ===== Statistics Integration Helper Methods =====
    
    /**
     * Convert time series points to MetricsSnapshot objects
     */
    private List<MetricsSnapshot> convertTimeSeriesToSnapshots(List<TimeSeriesPoint> points, MetricKey key) {
        SnapshotSerializer serializer = snapshotSerializer;
        if (serializer == null) {
            logger.warn("SnapshotSerializer not available - cannot convert time series points to snapshots");
            return new ArrayList<>();
        }
        
        return points.stream()
            .filter(point -> point.snapshotData() != null)
            .map(point -> {
                try {
                    return serializer.deserialize(
                        point.snapshotData(), 
                        point.snapshotType(), 
                        MetricsSnapshot.class
                    );
                } catch (Exception e) {
                    logger.warn("Failed to deserialize snapshot from time series point: {}", e.getMessage());
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
    
    /**
     * Query time series by tag value
     */
    private List<TimeSeriesPoint> queryTimeSeriesByTag(String tagKey, String tagValue, Duration timeRange) {
        MetricTimeSeriesStorage storage = timeSeriesStorage;
        if (storage == null) {
            return new ArrayList<>();
        }
        
        Instant endTime = Instant.now();
        Instant startTime = endTime.minus(timeRange);
        
        Set<String> allSeries = storage.getAvailableSeries();
        List<TimeSeriesPoint> matchingPoints = new ArrayList<>();
        
        for (String seriesId : allSeries) {
            try {
                List<TimeSeriesPoint> points = storage.queryTimeSeries(seriesId, startTime, endTime);
                
                List<TimeSeriesPoint> filteredPoints = points.stream()
                    .filter(point -> tagValue.equals(point.tags().get(tagKey)))
                    .collect(Collectors.toList());
                
                matchingPoints.addAll(filteredPoints);
            } catch (Exception e) {
                logger.debug("Failed to query series {} for tag {}: {}", seriesId, tagKey, e.getMessage());
            }
        }
        
        return matchingPoints;
    }
    
    /**
     * Extract series ID from a time series point
     */
    private String extractSeriesIdFromPoint(TimeSeriesPoint point) {
        // This is a simplified implementation - in a real scenario, you'd need to
        // store the series ID in the point or reconstruct it from the tags
        return point.tags().getOrDefault("seriesId", "unknown");
    }
    
    /**
     * Extract MetricKey from series ID
     */
    private MetricKey extractMetricKeyFromSeriesId(String seriesId) {
        // This is a simplified implementation - in a real scenario, you'd need to
        // parse the series ID to reconstruct the MetricKey
        // For now, create a basic key from the series ID
        return MetricKeys.custom("generic", Map.of("id", seriesId), Set.of("counts", "latency"));
    }
    
    /**
     * Create series ID from MetricKey
     */
    private String createSeriesId(MetricKey key) {
        return key.kind() + ":" + key.id();
    }
}
