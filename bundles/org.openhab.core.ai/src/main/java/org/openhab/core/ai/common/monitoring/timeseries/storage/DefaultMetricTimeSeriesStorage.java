package org.openhab.core.ai.common.monitoring.timeseries.storage;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.timeseries.AggregatedPoint;
import org.openhab.core.ai.common.monitoring.timeseries.MetricTimeSeriesStorage;
import org.openhab.core.ai.common.monitoring.timeseries.TimeSeriesMetadata;
import org.openhab.core.ai.common.monitoring.timeseries.TimeSeriesPoint;
import org.openhab.core.ai.common.monitoring.timeseries.TimeSeriesQueryCriteria;
import org.openhab.core.ai.common.monitoring.timeseries.TimeSeriesStorageStatistics;
import org.openhab.core.storage.Storage;
import org.openhab.core.storage.StorageService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * StorageService-based implementation of MetricTimeSeriesStorage.
 * 
 * This implementation uses openHAB's StorageService to persist time series data,
 * metadata, and aggregated data. It provides efficient storage and retrieval
 * of time series data with automatic cleanup and metadata management.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@Component(service = MetricTimeSeriesStorage.class, immediate = true)
@NonNullByDefault
public class DefaultMetricTimeSeriesStorage implements MetricTimeSeriesStorage {

    private static final Logger logger = LoggerFactory.getLogger(DefaultMetricTimeSeriesStorage.class);

    @Reference
    private @Nullable StorageService storageService;

    // Storage instances
    private @Nullable Storage<Map<String, Object>> timeSeriesStorage;
    private @Nullable Storage<Map<String, Object>> metadataStorage;
    private @Nullable Storage<Map<String, Object>> aggregatedStorage;
    private @Nullable Storage<Map<String, Object>> configurationStorage;
    
    // Configuration
    private final Duration defaultRetentionPeriod = Duration.ofDays(30);
    private final Duration aggregationPeriod = Duration.ofHours(1);
    
    // Cleanup scheduler
    private @Nullable ScheduledExecutorService cleanupScheduler;
    
    // Cache for metadata to avoid frequent storage access
    private final Map<String, TimeSeriesMetadata> metadataCache = new ConcurrentHashMap<>();
    
    // Storage operation metrics
    private final java.util.concurrent.atomic.LongAdder totalOperations = new java.util.concurrent.atomic.LongAdder();
    private final java.util.concurrent.atomic.LongAdder successfulOperations = new java.util.concurrent.atomic.LongAdder();
    private final java.util.concurrent.atomic.LongAdder failedOperations = new java.util.concurrent.atomic.LongAdder();
    private final java.util.concurrent.atomic.LongAdder totalStorageTime = new java.util.concurrent.atomic.LongAdder();
    private final java.util.concurrent.atomic.LongAdder totalQueryTime = new java.util.concurrent.atomic.LongAdder();
    private final java.util.concurrent.atomic.LongAdder totalCleanupTime = new java.util.concurrent.atomic.LongAdder();
    
    // Aggregation scheduling
    private final Map<String, ScheduledFuture<?>> aggregationSchedules = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> seriesAggregationConfigs = new ConcurrentHashMap<>();
    
    @Activate
    public void activate() {
        try {
            if (storageService != null) {
                // Initialize storage instances
                timeSeriesStorage = storageService.getStorage("ai-timeseries-data", this.getClass().getClassLoader());
                metadataStorage = storageService.getStorage("ai-timeseries-metadata", this.getClass().getClassLoader());
                aggregatedStorage = storageService.getStorage("ai-timeseries-aggregated", this.getClass().getClassLoader());
                configurationStorage = storageService.getStorage("ai-timeseries-config", this.getClass().getClassLoader());
                
                // Initialize cleanup scheduler
                cleanupScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                    Thread t = new Thread(r, "TimeSeriesStorage-Cleanup");
                    t.setDaemon(true);
                    return t;
                });
                
                // Schedule cleanup task
                scheduleCleanup();
                
                // Load metadata cache
                loadMetadataCache();
                
                logger.info("StorageService time series storage initialized successfully");
            } else {
                logger.error("StorageService not available - time series storage disabled");
                throw new RuntimeException("StorageService not available");
            }
            
        } catch (Exception e) {
            logger.error("Failed to initialize StorageService time series storage", e);
            throw new RuntimeException("Failed to initialize time series storage", e);
        }
    }
    
    @Deactivate
    public void deactivate() {
        if (cleanupScheduler != null) {
            cleanupScheduler.shutdown();
            try {
                if (!cleanupScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    cleanupScheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                cleanupScheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        logger.info("StorageService time series storage deactivated");
    }
    
    @Override
    public void storeTimeSeriesPoint(String seriesId, Instant timestamp, 
                                   Map<String, String> tags, Map<String, Object> fields) {
        // Use batch operation for single point for consistency and performance
        TimeSeriesPoint point = new TimeSeriesPoint(timestamp, tags, fields);
        storeTimeSeriesPointsBatch(List.of(point), seriesId);
    }
    
    /**
     * Check if the existing data is a duplicate of the new data.
     * 
     * @param existingData the existing data in storage
     * @param timestamp the new timestamp
     * @param tags the new tags
     * @param fields the new fields
     * @return true if the data is a duplicate, false otherwise
     */
    private boolean isDuplicateData(Map<String, Object> existingData, Instant timestamp, 
                                  Map<String, String> tags, Map<String, Object> fields) {
        try {
            // Compare timestamps
            Long existingTimestamp = (Long) existingData.get("timestamp");
            if (existingTimestamp == null || !existingTimestamp.equals(timestamp.toEpochMilli())) {
                return false;
            }
            
            // Compare tags
            @SuppressWarnings("unchecked")
            Map<String, String> existingTags = (Map<String, String>) existingData.get("tags");
            if (!Objects.equals(existingTags, tags)) {
                return false;
            }
            
            // Compare fields
            @SuppressWarnings("unchecked")
            Map<String, Object> existingFields = (Map<String, Object>) existingData.get("fields");
            if (!Objects.equals(existingFields, fields)) {
                return false;
            }
            
            return true;
        } catch (Exception e) {
            logger.warn("Error comparing data for duplicate detection", e);
            return false; // If comparison fails, treat as different data
        }
    }
    
    /**
     * Store multiple time series points in a batch operation for improved performance.
     * 
     * @param points list of time series points to store
     * @param seriesId the series ID for all points (if null, will be extracted from each point)
     */
    public void storeTimeSeriesPointsBatch(List<TimeSeriesPoint> points, @Nullable String seriesId) {
        if (points == null || points.isEmpty()) {
            return;
        }
        
        long startTime = System.nanoTime();
        totalOperations.increment();
        
        try {
            if (timeSeriesStorage != null) {
                Map<String, Map<String, Object>> batchData = new HashMap<>();
                Map<String, Instant> metadataUpdates = new HashMap<>();
                int duplicateCount = 0;
                
                // Prepare batch data
                for (TimeSeriesPoint point : points) {
                    // Use provided seriesId or extract from point
                    String currentSeriesId = seriesId != null ? seriesId : extractSeriesIdFromPoint(point);
                    String storageKey = currentSeriesId + ":" + point.timestamp().toEpochMilli();
                    
                    // Check for duplicate data with content comparison
                    Map<String, Object> existingData = timeSeriesStorage.get(storageKey);
                    if (existingData != null) {
                        if (isDuplicateData(existingData, point.timestamp(), point.tags(), point.fields())) {
                            duplicateCount++;
                            logger.debug("Duplicate data point detected for series {} at timestamp {} - skipping", 
                                       currentSeriesId, point.timestamp());
                            continue; // Skip duplicate
                        } else {
                            logger.debug("Data point exists but differs for series {} at timestamp {} - updating", 
                                       currentSeriesId, point.timestamp());
                        }
                    }
                    
                    Map<String, Object> pointData = new HashMap<>();
                    pointData.put("timestamp", point.timestamp().toEpochMilli());
                    pointData.put("tags", point.tags());
                    pointData.put("fields", point.fields());
                    
                    batchData.put(storageKey, pointData);
                    metadataUpdates.put(currentSeriesId, point.timestamp());
                }
                
                // Store all points in batch
                for (Map.Entry<String, Map<String, Object>> entry : batchData.entrySet()) {
                    timeSeriesStorage.put(entry.getKey(), entry.getValue());
                }
                
                // Update metadata for all series
                for (Map.Entry<String, Instant> entry : metadataUpdates.entrySet()) {
                    updateMetadata(entry.getKey(), entry.getValue());
                }
                
                successfulOperations.increment();
                logger.debug("Stored {} time series points in batch operation ({} duplicates skipped)", 
                           batchData.size(), duplicateCount);
            } else {
                failedOperations.increment();
                logger.warn("Time series storage not available - cannot store batch points");
            }
        } catch (Exception e) {
            failedOperations.increment();
            logger.error("Failed to store batch time series points", e);
        } finally {
            long duration = System.nanoTime() - startTime;
            totalStorageTime.add(duration);
        }
    }
    
    /**
     * Extract series ID from a time series point.
     * This is a helper method that tries to determine the series ID from the point's tags.
     * 
     * @param point the time series point
     * @return the series ID
     */
    private String extractSeriesIdFromPoint(TimeSeriesPoint point) {
        // Try to get series ID from tags
        String seriesId = point.tags().get("seriesId");
        if (seriesId != null && !seriesId.isEmpty()) {
            return seriesId;
        }
        
        // Try to construct series ID from domain and operation tags
        String domain = point.tags().get("domain");
        String operation = point.tags().get("operation");
        if (domain != null && operation != null) {
            return domain + ":" + operation;
        }
        
        // Fallback to a default series ID
        return "default:" + point.timestamp().toEpochMilli();
    }
    
    @Override
    public List<TimeSeriesPoint> queryTimeSeries(String seriesId, Instant startTime, Instant endTime) {
        long startTimeNs = System.nanoTime();
        List<TimeSeriesPoint> results = new ArrayList<>();
        int keysScanned = 0;
        int keysMatched = 0;
        int deserializationErrors = 0;
        
        try {
            if (timeSeriesStorage != null) {
                // Query all keys for the series within the time range
                String startKey = seriesId + ":" + startTime.toEpochMilli();
                String endKey = seriesId + ":" + endTime.toEpochMilli();
                
                for (String key : timeSeriesStorage.getKeys()) {
                    keysScanned++;
                    if (key.startsWith(seriesId + ":") && key.compareTo(startKey) >= 0 && key.compareTo(endKey) <= 0) {
                        keysMatched++;
                        Map<String, Object> pointData = timeSeriesStorage.get(key);
                        if (pointData != null) {
                            TimeSeriesPoint point = deserializeTimeSeriesPoint(pointData);
                            if (point != null) {
                                results.add(point);
                            } else {
                                deserializationErrors++;
                            }
                        }
                    }
                }
                
                // Sort by timestamp
                results.sort((a, b) -> a.timestamp().compareTo(b.timestamp()));
                
                long queryTimeNs = System.nanoTime() - startTimeNs;
                long queryTimeMs = queryTimeNs / 1_000_000;
                totalQueryTime.add(queryTimeNs);
                
                // Log detailed query metrics
                logger.debug("Queried {} time series points for series {} from {} to {} in {}ms " +
                           "(scanned: {}, matched: {}, deserialization errors: {})", 
                           results.size(), seriesId, startTime, endTime, queryTimeMs,
                           keysScanned, keysMatched, deserializationErrors);
                
                // Log performance warnings for slow queries
                if (queryTimeMs > 1000) { // More than 1 second
                    logger.warn("Slow query detected for series {}: {}ms for {} points (scanned {} keys)", 
                               seriesId, queryTimeMs, results.size(), keysScanned);
                }
            } else {
                logger.warn("Time series storage not available - cannot query series {}", seriesId);
            }
        } catch (Exception e) {
            logger.error("Failed to query time series for series {} from {} to {}", seriesId, startTime, endTime, e);
        }
        
        return results;
    }
    
    @Override
    public List<AggregatedPoint> queryWithAggregation(String seriesId, Instant startTime, 
                                                     Instant endTime, String aggregationFunction, 
                                                     Duration aggregationPeriod) {
        List<AggregatedPoint> results = new ArrayList<>();
        long startTimeNs = System.nanoTime();
        
        try {
            // First, try to get pre-computed aggregated data
            List<AggregatedPoint> preComputedResults = queryPreComputedAggregation(seriesId, startTime, endTime, 
                                                                                  aggregationFunction, aggregationPeriod);
            
            // Check if we have complete pre-computed coverage
            if (!preComputedResults.isEmpty()) {
                // Check for gaps in pre-computed data
                List<TimeSeriesPoint> missingPoints = findMissingAggregationData(seriesId, startTime, endTime, 
                                                                               aggregationFunction, aggregationPeriod, preComputedResults);
                
                if (missingPoints.isEmpty()) {
                    // Complete pre-computed coverage
                    long queryTimeMs = (System.nanoTime() - startTimeNs) / 1_000_000;
                    logger.debug("Retrieved {} pre-computed aggregated points for series {} in {}ms (complete coverage)", 
                               preComputedResults.size(), seriesId, queryTimeMs);
                    return preComputedResults;
                } else {
                    // Partial coverage - compute missing aggregations
                    logger.debug("Retrieved {} pre-computed aggregated points for series {}, computing {} missing periods", 
                               preComputedResults.size(), seriesId, missingPoints.size());
                    
                    // Compute missing aggregations
                    List<AggregatedPoint> missingAggregations = computeAggregationsForPoints(missingPoints, aggregationFunction, aggregationPeriod);
                    
                    // Store missing aggregations for future use
                    for (AggregatedPoint aggPoint : missingAggregations) {
                        storePreComputedAggregation(seriesId, aggPoint);
                    }
                    
                    // Combine results
                    results.addAll(preComputedResults);
                    results.addAll(missingAggregations);
                    
                    // Sort by timestamp
                    results.sort((a, b) -> a.timestamp().compareTo(b.timestamp()));
                    
                    long queryTimeMs = (System.nanoTime() - startTimeNs) / 1_000_000;
                    logger.debug("Computed {} total aggregated points for series {} with function {} and period {} in {}ms " +
                               "(pre-computed: {}, computed: {})", 
                               results.size(), seriesId, aggregationFunction, aggregationPeriod, queryTimeMs,
                               preComputedResults.size(), missingAggregations.size());
                    
                    return results;
                }
            }
            
            // No pre-computed data available, compute aggregation on-the-fly
            List<TimeSeriesPoint> points = queryTimeSeries(seriesId, startTime, endTime);
            
            if (points.isEmpty()) {
                return results;
            }
            
            // Compute all aggregations
            List<AggregatedPoint> computedAggregations = computeAggregationsForPoints(points, aggregationFunction, aggregationPeriod);
            
            // Store all computed aggregations for future use
            for (AggregatedPoint aggPoint : computedAggregations) {
                storePreComputedAggregation(seriesId, aggPoint);
            }
            
            results.addAll(computedAggregations);
            
            // Sort by timestamp
            results.sort((a, b) -> a.timestamp().compareTo(b.timestamp()));
            
            long queryTimeMs = (System.nanoTime() - startTimeNs) / 1_000_000;
            logger.debug("Computed {} aggregated points for series {} with function {} and period {} in {}ms (no pre-computed data)", 
                       results.size(), seriesId, aggregationFunction, aggregationPeriod, queryTimeMs);
            
        } catch (Exception e) {
            logger.error("Failed to query aggregated time series for series {} with function {}", 
                        seriesId, aggregationFunction, e);
        }
        
        return results;
    }
    
    /**
     * Query pre-computed aggregated data from storage.
     * 
     * @param seriesId the series ID
     * @param startTime the start time
     * @param endTime the end time
     * @param aggregationFunction the aggregation function
     * @param aggregationPeriod the aggregation period
     * @return list of pre-computed aggregated points
     */
    private List<AggregatedPoint> queryPreComputedAggregation(String seriesId, Instant startTime, 
                                                             Instant endTime, String aggregationFunction, 
                                                             Duration aggregationPeriod) {
        List<AggregatedPoint> results = new ArrayList<>();
        
        try {
            if (aggregatedStorage != null) {
                String prefix = seriesId + ":" + aggregationFunction + ":" + aggregationPeriod.toMillis() + ":";
                String startKey = prefix + startTime.toEpochMilli();
                String endKey = prefix + endTime.toEpochMilli();
                
                for (String key : aggregatedStorage.getKeys()) {
                    if (key.startsWith(prefix) && key.compareTo(startKey) >= 0 && key.compareTo(endKey) <= 0) {
                        Map<String, Object> aggregatedData = aggregatedStorage.get(key);
                        if (aggregatedData != null) {
                            AggregatedPoint point = deserializeAggregatedPoint(aggregatedData);
                            if (point != null) {
                                results.add(point);
                            }
                        }
                    }
                }
                
                results.sort((a, b) -> a.timestamp().compareTo(b.timestamp()));
            }
        } catch (Exception e) {
            logger.warn("Failed to query pre-computed aggregation for series {}", seriesId, e);
        }
        
        return results;
    }
    
    /**
     * Store pre-computed aggregated data for future use.
     * 
     * @param seriesId the series ID
     * @param aggregatedPoint the aggregated point to store
     */
    private void storePreComputedAggregation(String seriesId, AggregatedPoint aggregatedPoint) {
        try {
            if (aggregatedStorage != null) {
                String key = seriesId + ":" + aggregatedPoint.aggregationFunction() + ":" + 
                           aggregatedPoint.period().toMillis() + ":" + aggregatedPoint.timestamp().toEpochMilli();
                
                Map<String, Object> aggregatedData = serializeAggregatedPoint(aggregatedPoint);
                aggregatedStorage.put(key, aggregatedData);
                
                logger.debug("Stored pre-computed aggregation for series {} at {}", seriesId, aggregatedPoint.timestamp());
            }
        } catch (Exception e) {
            logger.warn("Failed to store pre-computed aggregation for series {}", seriesId, e);
        }
    }
    
    /**
     * Find missing aggregation data by comparing pre-computed results with the requested time range.
     * 
     * @param seriesId the series ID
     * @param startTime the start time
     * @param endTime the end time
     * @param aggregationFunction the aggregation function
     * @param aggregationPeriod the aggregation period
     * @param preComputedResults the pre-computed results
     * @return list of time series points that need aggregation
     */
    private List<TimeSeriesPoint> findMissingAggregationData(String seriesId, Instant startTime, Instant endTime,
                                                           String aggregationFunction, Duration aggregationPeriod,
                                                           List<AggregatedPoint> preComputedResults) {
        List<TimeSeriesPoint> missingPoints = new ArrayList<>();
        
        try {
            // Calculate expected aggregation periods
            Set<Long> expectedPeriods = new HashSet<>();
            long periodMs = aggregationPeriod.toMillis();
            long currentPeriod = (startTime.toEpochMilli() / periodMs) * periodMs;
            long endPeriod = (endTime.toEpochMilli() / periodMs) * periodMs;
            
            while (currentPeriod <= endPeriod) {
                expectedPeriods.add(currentPeriod);
                currentPeriod += periodMs;
            }
            
            // Find which periods are missing from pre-computed results
            Set<Long> existingPeriods = new HashSet<>();
            for (AggregatedPoint point : preComputedResults) {
                long periodStart = (point.timestamp().toEpochMilli() / periodMs) * periodMs;
                existingPeriods.add(periodStart);
            }
            
            // Get raw data for missing periods
            for (Long missingPeriod : expectedPeriods) {
                if (!existingPeriods.contains(missingPeriod)) {
                    Instant periodStart = Instant.ofEpochMilli(missingPeriod);
                    Instant periodEnd = Instant.ofEpochMilli(missingPeriod + periodMs);
                    
                    List<TimeSeriesPoint> periodPoints = queryTimeSeries(seriesId, periodStart, periodEnd);
                    missingPoints.addAll(periodPoints);
                }
            }
            
        } catch (Exception e) {
            logger.warn("Failed to find missing aggregation data for series {}", seriesId, e);
        }
        
        return missingPoints;
    }
    
    /**
     * Compute aggregations for a list of time series points.
     * 
     * @param points the time series points
     * @param aggregationFunction the aggregation function
     * @param aggregationPeriod the aggregation period
     * @return list of aggregated points
     */
    private List<AggregatedPoint> computeAggregationsForPoints(List<TimeSeriesPoint> points, String aggregationFunction, Duration aggregationPeriod) {
        List<AggregatedPoint> results = new ArrayList<>();
        
        if (points.isEmpty()) {
            return results;
        }
        
        // Group points by aggregation period
        Map<Long, List<TimeSeriesPoint>> groupedPoints = new HashMap<>();
        
        for (TimeSeriesPoint point : points) {
            long periodStart = (point.timestamp().toEpochMilli() / aggregationPeriod.toMillis()) * aggregationPeriod.toMillis();
            groupedPoints.computeIfAbsent(periodStart, k -> new ArrayList<>()).add(point);
        }
        
        // Aggregate each group
        for (Map.Entry<Long, List<TimeSeriesPoint>> entry : groupedPoints.entrySet()) {
            Instant periodEnd = Instant.ofEpochMilli(entry.getKey() + aggregationPeriod.toMillis());
            List<TimeSeriesPoint> groupPoints = entry.getValue();
            
            Map<String, Object> aggregatedValues = performAggregation(groupPoints, aggregationFunction);
            
            AggregatedPoint aggregatedPoint = new AggregatedPoint(
                periodEnd, 
                aggregationPeriod, 
                aggregationFunction, 
                aggregatedValues
            );
            
            results.add(aggregatedPoint);
        }
        
        return results;
    }
    
    /**
     * Serialize an AggregatedPoint to storage format.
     * 
     * @param point the aggregated point to serialize
     * @return serialized data map
     */
    private Map<String, Object> serializeAggregatedPoint(AggregatedPoint point) {
        Map<String, Object> data = new HashMap<>();
        data.put("timestamp", point.timestamp().toEpochMilli());
        data.put("period", point.period().toMillis());
        data.put("aggregationFunction", point.aggregationFunction());
        data.put("aggregatedValues", point.aggregatedValues());
        return data;
    }
    
    /**
     * Deserialize an AggregatedPoint from storage format.
     * 
     * @param data the serialized data
     * @return deserialized aggregated point or null if failed
     */
    private @Nullable AggregatedPoint deserializeAggregatedPoint(Map<String, Object> data) {
        try {
            Long timestampMs = (Long) data.get("timestamp");
            Long periodMs = (Long) data.get("period");
            String aggregationFunction = (String) data.get("aggregationFunction");
            @SuppressWarnings("unchecked")
            Map<String, Object> aggregatedValues = (Map<String, Object>) data.get("aggregatedValues");
            
            if (timestampMs != null && periodMs != null && aggregationFunction != null && aggregatedValues != null) {
                return new AggregatedPoint(
                    Instant.ofEpochMilli(timestampMs),
                    Duration.ofMillis(periodMs),
                    aggregationFunction,
                    aggregatedValues
                );
            }
        } catch (Exception e) {
            logger.warn("Failed to deserialize aggregated point", e);
        }
        
        return null;
    }
    
    @Override
    public Set<String> getAvailableSeries() {
        Set<String> series = new HashSet<>();
        
        try {
            if (metadataStorage != null) {
                for (String key : metadataStorage.getKeys()) {
                    if (!key.startsWith("_")) { // Skip internal keys
                        series.add(key);
                    }
                }
            }
            
            logger.debug("Found {} available time series", series.size());
        } catch (Exception e) {
            logger.error("Failed to get available series", e);
        }
        
        return series;
    }
    
    @Override
    public void cleanupOldData(Duration retentionPeriod) {
        long startTime = System.nanoTime();
        totalCleanupTime.add(System.nanoTime() - startTime);
        
        try {
            Instant cutoffTime = Instant.now().minus(retentionPeriod);
            int cleanedCount = 0;
            int rollbackCount = 0;
            
            // Group cleanup by series for rollback capability
            Map<String, List<String>> seriesKeysToRemove = new HashMap<>();
            Map<String, List<String>> seriesAggregatedKeysToRemove = new HashMap<>();
            
            if (timeSeriesStorage != null) {
                for (String key : timeSeriesStorage.getKeys()) {
                    try {
                        // Extract timestamp from key (format: seriesId:timestamp)
                        String[] parts = key.split(":", 2);
                        if (parts.length == 2) {
                            String seriesId = parts[0];
                            long timestamp = Long.parseLong(parts[1]);
                            if (Instant.ofEpochMilli(timestamp).isBefore(cutoffTime)) {
                                seriesKeysToRemove.computeIfAbsent(seriesId, k -> new ArrayList<>()).add(key);
                            }
                        }
                    } catch (NumberFormatException e) {
                        logger.warn("Invalid timestamp in storage key: {}", key);
                    }
                }
            }
            
            // Clean up old aggregated data
            if (aggregatedStorage != null) {
                for (String key : aggregatedStorage.getKeys()) {
                    try {
                        String[] parts = key.split(":", 3);
                        if (parts.length == 3) {
                            String seriesId = parts[0];
                            long timestamp = Long.parseLong(parts[2]);
                            if (Instant.ofEpochMilli(timestamp).isBefore(cutoffTime)) {
                                seriesAggregatedKeysToRemove.computeIfAbsent(seriesId, k -> new ArrayList<>()).add(key);
                            }
                        }
                    } catch (NumberFormatException e) {
                        logger.warn("Invalid timestamp in aggregated storage key: {}", key);
                    }
                }
            }
            
            // Perform cleanup with rollback capability per series
            for (Map.Entry<String, List<String>> entry : seriesKeysToRemove.entrySet()) {
                String seriesId = entry.getKey();
                
                try {
                    // Use series-specific cleanup with rollback
                    int seriesCleanedCount = cleanupSeriesDataWithRollback(seriesId, cutoffTime);
                    cleanedCount += seriesCleanedCount;
                    
                    logger.debug("Cleaned up {} data points for series {} with rollback capability", 
                               seriesCleanedCount, seriesId);
                } catch (Exception e) {
                    logger.warn("Failed to cleanup data for series {} - attempting rollback", seriesId, e);
                    rollbackCount++;
                    
                    // Attempt rollback for this series
                    try {
                        int restoredCount = rollbackCleanup(seriesId, cutoffTime.minus(Duration.ofHours(1)), cutoffTime);
                        logger.info("Rollback completed for series {} - restored {} points", seriesId, restoredCount);
                    } catch (Exception rollbackException) {
                        logger.error("Rollback failed for series {}", seriesId, rollbackException);
                    }
                }
            }
            
            // Clean up aggregated data (without rollback for aggregated data)
            for (Map.Entry<String, List<String>> entry : seriesAggregatedKeysToRemove.entrySet()) {
                String seriesId = entry.getKey();
                List<String> keysToRemove = entry.getValue();
                
                try {
                    for (String key : keysToRemove) {
                        aggregatedStorage.remove(key);
                        cleanedCount++;
                    }
                    logger.debug("Cleaned up {} aggregated data points for series {}", keysToRemove.size(), seriesId);
                } catch (Exception e) {
                    logger.warn("Failed to cleanup aggregated data for series {}", seriesId, e);
                }
            }
            
            // Update last cleanup time
            updateLastCleanupTime();
            
            long cleanupTimeMs = (System.nanoTime() - startTime) / 1_000_000;
            logger.info("Cleaned up {} old data points older than {} in {}ms (rollbacks: {})", 
                       cleanedCount, retentionPeriod, cleanupTimeMs, rollbackCount);
            
            // Log performance warnings for slow cleanup
            if (cleanupTimeMs > 5000) { // More than 5 seconds
                logger.warn("Slow cleanup detected: {}ms for {} points", cleanupTimeMs, cleanedCount);
            }
            
        } catch (Exception e) {
            logger.error("Failed to cleanup old data", e);
        } finally {
            long duration = System.nanoTime() - startTime;
            totalCleanupTime.add(duration);
        }
    }
    
    @Override
    public TimeSeriesMetadata getSeriesMetadata(String seriesId) {
        try {
            // Check cache first
            TimeSeriesMetadata cached = metadataCache.get(seriesId);
            if (cached != null) {
                return cached;
            }
            
            // Load from storage
            if (metadataStorage != null) {
                Map<String, Object> metadataData = metadataStorage.get(seriesId);
                if (metadataData != null) {
                    TimeSeriesMetadata metadata = deserializeMetadata(metadataData);
                    if (metadata != null) {
                        metadataCache.put(seriesId, metadata);
                        return metadata;
                    }
                }
            }
            
            return null;
        } catch (Exception e) {
            logger.error("Failed to get metadata for series {}", seriesId, e);
            return null;
        }
    }
    
    @Override
    public List<TimeSeriesPoint> queryWithCriteria(TimeSeriesQueryCriteria criteria) {
        List<TimeSeriesPoint> results = new ArrayList<>();
        
        try {
            // Get basic time series data
            List<TimeSeriesPoint> points = queryTimeSeries(criteria.seriesId(), criteria.startTime(), criteria.endTime());
            
            // Apply filters
            for (TimeSeriesPoint point : points) {
                boolean matches = true;
                
                // Apply tags filter
                if (!criteria.tagFilters().isEmpty()) {
                    for (Map.Entry<String, String> filter : criteria.tagFilters().entrySet()) {
                        String tagValue = point.tags().get(filter.getKey());
                        if (!filter.getValue().equals(tagValue)) {
                            matches = false;
                            break;
                        }
                    }
                }
                
                // Apply fields filter
                if (matches && !criteria.fieldFilters().isEmpty()) {
                    for (Map.Entry<String, Object> filter : criteria.fieldFilters().entrySet()) {
                        Object fieldValue = point.fields().get(filter.getKey());
                        if (!Objects.equals(filter.getValue(), fieldValue)) {
                            matches = false;
                            break;
                        }
                    }
                }
                
                if (matches) {
                    results.add(point);
                }
            }
            
            // Apply pagination
            if (criteria.limit() != null && criteria.limit() > 0) {
                int start = criteria.offset() != null ? criteria.offset() : 0;
                int end = Math.min(start + criteria.limit(), results.size());
                if (start < results.size()) {
                    results = results.subList(start, end);
                } else {
                    results = new ArrayList<>();
                }
            } else if (criteria.offset() != null && criteria.offset() > 0) {
                int start = criteria.offset();
                if (start < results.size()) {
                    results = results.subList(start, results.size());
                } else {
                    results = new ArrayList<>();
                }
            }
            
            logger.debug("Queried {} points with criteria for series {}", results.size(), criteria.seriesId());
            
        } catch (Exception e) {
            logger.error("Failed to query with criteria for series {}", criteria.seriesId(), e);
        }
        
        return results;
    }
    
    @Override
    public boolean isHealthy() {
        try {
            // Check if storage services are available
            if (timeSeriesStorage == null || metadataStorage == null) {
                return false;
            }
            
            // Check if we can perform basic operations
            getAvailableSeries();
            getStorageStatistics();
            
            return true;
        } catch (Exception e) {
            logger.warn("Health check failed", e);
            return false;
        }
    }
    
    @Override
    public TimeSeriesStorageStatistics getStorageStatistics() {
        try {
            int totalSeries = getAvailableSeries().size();
            long totalPoints = 0;
            long storageSize = 0;
            Instant lastCleanupTime = null;
            
            // Count total points
            if (timeSeriesStorage != null) {
                totalPoints = timeSeriesStorage.getKeys().size();
                // Rough estimation: assume 1KB per point
                storageSize = totalPoints * 1024;
            }
            
            // Get last cleanup time from configuration
            if (configurationStorage != null) {
                Map<String, Object> config = configurationStorage.get("lastCleanup");
                if (config != null && config.containsKey("timestamp")) {
                    Object timestamp = config.get("timestamp");
                    if (timestamp instanceof Long) {
                        lastCleanupTime = Instant.ofEpochMilli((Long) timestamp);
                    }
                }
            }
            
            // Determine health status
            TimeSeriesStorageStatistics.HealthStatus healthStatus = TimeSeriesStorageStatistics.HealthStatus.HEALTHY;
            if (timeSeriesStorage == null) {
                healthStatus = TimeSeriesStorageStatistics.HealthStatus.DOWN;
            } else if (totalPoints > 1000000) { // More than 1M points
                healthStatus = TimeSeriesStorageStatistics.HealthStatus.WARNING;
            }
            
            // Get comprehensive performance metrics including operation metrics
            Map<String, Object> performanceMetrics = getStorageOperationMetrics();
            performanceMetrics.put("defaultRetentionPeriod", defaultRetentionPeriod.toString());
            performanceMetrics.put("aggregationPeriod", aggregationPeriod.toString());
            performanceMetrics.put("lastCleanupTime", lastCleanupTime);
            
            Map<String, Object> customProperties = new HashMap<>();
            customProperties.put("storageType", "StorageService");
            customProperties.put("cacheSize", metadataCache.size());
            customProperties.put("aggregationSchedules", aggregationSchedules.size());
            customProperties.put("seriesAggregationConfigs", seriesAggregationConfigs.size());
            
            return new TimeSeriesStorageStatistics(
                totalSeries,
                totalPoints,
                storageSize,
                Instant.now(),
                healthStatus,
                performanceMetrics,
                customProperties
            );
            
        } catch (Exception e) {
            logger.error("Failed to get storage statistics", e);
            return new TimeSeriesStorageStatistics(
                0, 0, 0, Instant.now(), 
                TimeSeriesStorageStatistics.HealthStatus.CRITICAL,
                Map.of(), Map.of()
            );
        }
    }
    
    // Private helper methods
    
    private void scheduleCleanup() {
        if (cleanupScheduler != null) {
            // Schedule cleanup every 24 hours
            cleanupScheduler.scheduleAtFixedRate(() -> {
                try {
                    cleanupOldData(defaultRetentionPeriod);
                    updateLastCleanupTime();
                } catch (Exception e) {
                    logger.error("Error during scheduled cleanup", e);
                }
            }, 24, 24, TimeUnit.HOURS);
        }
    }
    
    private void loadMetadataCache() {
        try {
            if (metadataStorage != null) {
                for (String key : metadataStorage.getKeys()) {
                    Map<String, Object> metadataData = metadataStorage.get(key);
                    if (metadataData != null) {
                        TimeSeriesMetadata metadata = deserializeMetadata(metadataData);
                        if (metadata != null) {
                            metadataCache.put(key, metadata);
                        }
                    }
                }
                logger.debug("Loaded {} metadata entries into cache", metadataCache.size());
            }
        } catch (Exception e) {
            logger.error("Failed to load metadata cache", e);
        }
    }
    
    private void updateMetadata(String seriesId, Instant timestamp) {
        try {
            TimeSeriesMetadata currentMetadata = metadataCache.get(seriesId);
            
            if (currentMetadata == null) {
                // Create new metadata
                currentMetadata = new TimeSeriesMetadata(
                    seriesId,
                    timestamp,
                    timestamp,
                    1,
                    defaultRetentionPeriod,
                    aggregationPeriod,
                    Map.of(),
                    Map.of()
                );
            } else {
                // Update existing metadata
                currentMetadata = currentMetadata.withUpdate(currentMetadata.pointCount() + 1, timestamp);
            }
            
            metadataCache.put(seriesId, currentMetadata);
            
            // Persist to storage
            if (metadataStorage != null) {
                Map<String, Object> metadataData = serializeMetadata(currentMetadata);
                metadataStorage.put(seriesId, metadataData);
            }
            
        } catch (Exception e) {
            logger.error("Failed to update metadata for series {}", seriesId, e);
        }
    }
    
    private void updateLastCleanupTime() {
        try {
            if (configurationStorage != null) {
                Map<String, Object> config = new HashMap<>();
                config.put("timestamp", Instant.now().toEpochMilli());
                configurationStorage.put("lastCleanup", config);
            }
        } catch (Exception e) {
            logger.error("Failed to update last cleanup time", e);
        }
    }
    
    private Map<String, Object> performAggregation(List<TimeSeriesPoint> points, String aggregationFunction) {
        Map<String, Object> aggregatedValues = new HashMap<>();
        
        if (points.isEmpty()) {
            return aggregatedValues;
        }
        
        // Get all field names from the first point
        Set<String> fieldNames = points.get(0).fields().keySet();
        
        for (String fieldName : fieldNames) {
            List<Double> numericValues = new ArrayList<>();
            
            // Extract numeric values for this field
            for (TimeSeriesPoint point : points) {
                Object value = point.fields().get(fieldName);
                if (value instanceof Number) {
                    numericValues.add(((Number) value).doubleValue());
                }
            }
            
            if (!numericValues.isEmpty()) {
                double result = switch (aggregationFunction.toUpperCase()) {
                    case "AVG" -> numericValues.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                    case "SUM" -> numericValues.stream().mapToDouble(Double::doubleValue).sum();
                    case "MIN" -> numericValues.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
                    case "MAX" -> numericValues.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
                    case "COUNT" -> (double) numericValues.size();
                    case "FIRST" -> numericValues.get(0);
                    case "LAST" -> numericValues.get(numericValues.size() - 1);
                    default -> 0.0;
                };
                
                aggregatedValues.put(fieldName + "_" + aggregationFunction.toLowerCase(), result);
            }
        }
        
        return aggregatedValues;
    }
    
    private @Nullable TimeSeriesPoint deserializeTimeSeriesPoint(Map<String, Object> data) {
        try {
            Object timestampObj = data.get("timestamp");
            Object tagsObj = data.get("tags");
            Object fieldsObj = data.get("fields");
            
            if (timestampObj instanceof Long && tagsObj instanceof Map && fieldsObj instanceof Map) {
                Instant timestamp = Instant.ofEpochMilli((Long) timestampObj);
                @SuppressWarnings("unchecked")
                Map<String, String> tags = (Map<String, String>) tagsObj;
                @SuppressWarnings("unchecked")
                Map<String, Object> fields = (Map<String, Object>) fieldsObj;
                
                return new TimeSeriesPoint(timestamp, tags, fields);
            }
        } catch (Exception e) {
            logger.warn("Failed to deserialize time series point", e);
        }
        
        return null;
    }
    
    private Map<String, Object> serializeMetadata(TimeSeriesMetadata metadata) {
        Map<String, Object> data = new HashMap<>();
        data.put("seriesId", metadata.seriesId());
        data.put("createdAt", metadata.createdAt().toEpochMilli());
        data.put("lastUpdated", metadata.lastUpdated().toEpochMilli());
        data.put("pointCount", metadata.pointCount());
        data.put("retentionPeriod", metadata.retentionPeriod() != null ? metadata.retentionPeriod().toMillis() : null);
        data.put("aggregationPeriod", metadata.aggregationPeriod() != null ? metadata.aggregationPeriod().toMillis() : null);
        data.put("tags", metadata.tags());
        data.put("fields", metadata.fields());
        return data;
    }
    
    private @Nullable TimeSeriesMetadata deserializeMetadata(Map<String, Object> data) {
        try {
            String seriesId = (String) data.get("seriesId");
            Long createdAtMs = (Long) data.get("createdAt");
            Long lastUpdatedMs = (Long) data.get("lastUpdated");
            Long pointCount = (Long) data.get("pointCount");
            Long retentionPeriodMs = (Long) data.get("retentionPeriod");
            Long aggregationPeriodMs = (Long) data.get("aggregationPeriod");
            @SuppressWarnings("unchecked")
            Map<String, String> tags = (Map<String, String>) data.get("tags");
            @SuppressWarnings("unchecked")
            Map<String, String> fields = (Map<String, String>) data.get("fields");
            
            if (seriesId != null && createdAtMs != null && lastUpdatedMs != null && pointCount != null) {
                Duration retentionPeriod = retentionPeriodMs != null ? Duration.ofMillis(retentionPeriodMs) : null;
                Duration aggregationPeriod = aggregationPeriodMs != null ? Duration.ofMillis(aggregationPeriodMs) : null;
                
                return new TimeSeriesMetadata(
                    seriesId,
                    Instant.ofEpochMilli(createdAtMs),
                    Instant.ofEpochMilli(lastUpdatedMs),
                    pointCount,
                    retentionPeriod,
                    aggregationPeriod,
                    tags != null ? tags : Map.of(),
                    fields != null ? fields : Map.of()
                );
            }
        } catch (Exception e) {
            logger.warn("Failed to deserialize metadata", e);
        }
        
        return null;
    }
    
    /**
     * Gets storage operation metrics for monitoring and performance analysis.
     * 
     * @return map containing storage operation metrics
     */
    public Map<String, Object> getStorageOperationMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        long totalOps = totalOperations.sum();
        long successfulOps = successfulOperations.sum();
        long failedOps = failedOperations.sum();
        long totalStorageTimeNs = totalStorageTime.sum();
        long totalQueryTimeNs = totalQueryTime.sum();
        long totalCleanupTimeNs = totalCleanupTime.sum();
        
        // Basic operation metrics
        metrics.put("total_operations", totalOps);
        metrics.put("successful_operations", successfulOps);
        metrics.put("failed_operations", failedOps);
        metrics.put("success_rate_percent", totalOps > 0 ? (successfulOps * 100.0) / totalOps : 0.0);
        
        // Performance timing metrics
        metrics.put("avg_storage_time_ms", totalOps > 0 ? (totalStorageTimeNs / 1_000_000.0) / totalOps : 0.0);
        metrics.put("avg_query_time_ms", totalQueryTimeNs > 0 ? (totalQueryTimeNs / 1_000_000.0) / Math.max(1, totalOps) : 0.0);
        metrics.put("avg_cleanup_time_ms", totalCleanupTimeNs > 0 ? (totalCleanupTimeNs / 1_000_000.0) / Math.max(1, totalOps) : 0.0);
        metrics.put("total_storage_time_ms", totalStorageTimeNs / 1_000_000.0);
        metrics.put("total_query_time_ms", totalQueryTimeNs / 1_000_000.0);
        metrics.put("total_cleanup_time_ms", totalCleanupTimeNs / 1_000_000.0);
        
        // Cache and system metrics
        metrics.put("metadata_cache_size", metadataCache.size());
        metrics.put("aggregation_schedules_count", aggregationSchedules.size());
        metrics.put("series_aggregation_configs_count", seriesAggregationConfigs.size());
        
        // Query performance metrics
        if (totalQueryTimeNs > 0) {
            metrics.put("query_throughput_ops_per_sec", totalOps > 0 ? (totalOps * 1_000_000_000.0) / totalQueryTimeNs : 0.0);
        }
        
        // Storage efficiency metrics
        if (timeSeriesStorage != null) {
            int totalKeys = timeSeriesStorage.getKeys().size();
            metrics.put("total_storage_keys", totalKeys);
            metrics.put("storage_efficiency_ratio", totalKeys > 0 ? (double) successfulOps / totalKeys : 0.0);
        }
        
        metrics.put("timestamp", Instant.now());
        
        return metrics;
    }
    
    /**
     * Resets storage operation metrics.
     */
    public void resetStorageOperationMetrics() {
        totalOperations.reset();
        successfulOperations.reset();
        failedOperations.reset();
        totalStorageTime.reset();
        totalQueryTime.reset();
        totalCleanupTime.reset();
        logger.info("Storage operation metrics reset");
    }
    
    /**
     * Schedule automatic aggregation for a time series.
     * 
     * @param seriesId the series ID
     * @param aggregationFunction the aggregation function
     * @param aggregationPeriod the aggregation period
     * @param scheduleInterval the schedule interval for running aggregation
     */
    public void scheduleAggregation(String seriesId, String aggregationFunction, 
                                   Duration aggregationPeriod, Duration scheduleInterval) {
        try {
            String scheduleKey = seriesId + ":" + aggregationFunction + ":" + aggregationPeriod.toMillis();
            
            // Cancel existing schedule if any
            ScheduledFuture<?> existingSchedule = aggregationSchedules.get(scheduleKey);
            if (existingSchedule != null) {
                existingSchedule.cancel(false);
            }
            
            // Create new schedule
            if (cleanupScheduler != null) {
                ScheduledFuture<?> schedule = cleanupScheduler.scheduleAtFixedRate(
                    () -> performScheduledAggregation(seriesId, aggregationFunction, aggregationPeriod),
                    scheduleInterval.toMillis(),
                    scheduleInterval.toMillis(),
                    java.util.concurrent.TimeUnit.MILLISECONDS
                );
                
                aggregationSchedules.put(scheduleKey, schedule);
                
                // Track aggregation config for this series
                seriesAggregationConfigs.computeIfAbsent(seriesId, k -> new HashSet<>()).add(scheduleKey);
                
                logger.info("Scheduled aggregation for series {} with function {} and period {} every {}", 
                           seriesId, aggregationFunction, aggregationPeriod, scheduleInterval);
            }
        } catch (Exception e) {
            logger.error("Failed to schedule aggregation for series {}", seriesId, e);
        }
    }
    
    /**
     * Cancel aggregation schedule for a time series.
     * 
     * @param seriesId the series ID
     * @param aggregationFunction the aggregation function
     * @param aggregationPeriod the aggregation period
     */
    public void cancelAggregationSchedule(String seriesId, String aggregationFunction, 
                                         Duration aggregationPeriod) {
        try {
            String scheduleKey = seriesId + ":" + aggregationFunction + ":" + aggregationPeriod.toMillis();
            
            ScheduledFuture<?> schedule = aggregationSchedules.remove(scheduleKey);
            if (schedule != null) {
                schedule.cancel(false);
                logger.info("Cancelled aggregation schedule for series {} with function {} and period {}", 
                           seriesId, aggregationFunction, aggregationPeriod);
            }
            
            // Remove from series config
            Set<String> configs = seriesAggregationConfigs.get(seriesId);
            if (configs != null) {
                configs.remove(scheduleKey);
                if (configs.isEmpty()) {
                    seriesAggregationConfigs.remove(seriesId);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to cancel aggregation schedule for series {}", seriesId, e);
        }
    }
    
    /**
     * Perform scheduled aggregation for a time series.
     * 
     * @param seriesId the series ID
     * @param aggregationFunction the aggregation function
     * @param aggregationPeriod the aggregation period
     */
    private void performScheduledAggregation(String seriesId, String aggregationFunction, 
                                           Duration aggregationPeriod) {
        try {
            // Get the last aggregation timestamp from metadata
            TimeSeriesMetadata metadata = getSeriesMetadata(seriesId);
            if (metadata == null) {
                logger.warn("No metadata found for series {} - skipping scheduled aggregation", seriesId);
                return;
            }
            
            Instant lastUpdate = metadata.lastUpdated();
            Instant now = Instant.now();
            
            // Calculate the time range for aggregation
            Instant startTime = lastUpdate.minus(aggregationPeriod);
            Instant endTime = now;
            
            if (startTime.isAfter(endTime)) {
                logger.debug("No new data to aggregate for series {} since last update", seriesId);
                return;
            }
            
            // Perform aggregation
            List<AggregatedPoint> aggregatedPoints = queryWithAggregation(seriesId, startTime, endTime, 
                                                                        aggregationFunction, aggregationPeriod);
            
            logger.debug("Scheduled aggregation completed for series {} - generated {} aggregated points", 
                       seriesId, aggregatedPoints.size());
            
        } catch (Exception e) {
            logger.error("Failed to perform scheduled aggregation for series {}", seriesId, e);
        }
    }
    
    /**
     * Get aggregation schedules for a time series.
     * 
     * @param seriesId the series ID
     * @return set of aggregation schedule keys
     */
    public Set<String> getAggregationSchedules(String seriesId) {
        return seriesAggregationConfigs.getOrDefault(seriesId, new HashSet<>());
    }
    
    /**
     * Cancel all aggregation schedules for a time series.
     * 
     * @param seriesId the series ID
     */
    public void cancelAllAggregationSchedules(String seriesId) {
        try {
            Set<String> configs = seriesAggregationConfigs.remove(seriesId);
            if (configs != null) {
                for (String scheduleKey : configs) {
                    ScheduledFuture<?> schedule = aggregationSchedules.remove(scheduleKey);
                    if (schedule != null) {
                        schedule.cancel(false);
                    }
                }
                logger.info("Cancelled all aggregation schedules for series {}", seriesId);
            }
        } catch (Exception e) {
            logger.error("Failed to cancel all aggregation schedules for series {}", seriesId, e);
        }
    }
    
    /**
     * Query time series data with lazy loading for large datasets.
     * This method returns an iterator that loads data on-demand to handle large datasets efficiently.
     * 
     * @param seriesId the series ID
     * @param startTime the start time
     * @param endTime the end time
     * @param batchSize the number of points to load in each batch
     * @return iterator over time series points with lazy loading
     */
    public java.util.Iterator<TimeSeriesPoint> queryTimeSeriesLazy(String seriesId, Instant startTime, 
                                                                  Instant endTime, int batchSize) {
        return new LazyTimeSeriesIterator(seriesId, startTime, endTime, batchSize);
    }
    
    /**
     * Lazy loading iterator for time series data.
     * Loads data in batches to handle large datasets efficiently.
     */
    private class LazyTimeSeriesIterator implements java.util.Iterator<TimeSeriesPoint> {
        private final String seriesId;
        private final Instant startTime;
        private final Instant endTime;
        private final int batchSize;
        private final List<String> allKeys;
        private int currentIndex = 0;
        private List<TimeSeriesPoint> currentBatch = new ArrayList<>();
        private int batchIndex = 0;
        
        public LazyTimeSeriesIterator(String seriesId, Instant startTime, Instant endTime, int batchSize) {
            this.seriesId = seriesId;
            this.startTime = startTime;
            this.endTime = endTime;
            this.batchSize = Math.max(1, batchSize); // Ensure batch size is at least 1
            
            // Pre-load all keys for the time range
            this.allKeys = loadKeysForTimeRange();
        }
        
        private List<String> loadKeysForTimeRange() {
            List<String> keys = new ArrayList<>();
            try {
                if (timeSeriesStorage != null) {
                    String startKey = seriesId + ":" + startTime.toEpochMilli();
                    String endKey = seriesId + ":" + endTime.toEpochMilli();
                    
                    for (String key : timeSeriesStorage.getKeys()) {
                        if (key.startsWith(seriesId + ":") && key.compareTo(startKey) >= 0 && key.compareTo(endKey) <= 0) {
                            keys.add(key);
                        }
                    }
                    
                    // Sort keys by timestamp
                    keys.sort(String::compareTo);
                }
            } catch (Exception e) {
                logger.error("Failed to load keys for lazy time series iterator", e);
            }
            return keys;
        }
        
        @Override
        public boolean hasNext() {
            // Load next batch if current batch is exhausted
            if (batchIndex >= currentBatch.size() && currentIndex < allKeys.size()) {
                loadNextBatch();
            }
            return batchIndex < currentBatch.size();
        }
        
        @Override
        public TimeSeriesPoint next() {
            if (!hasNext()) {
                throw new java.util.NoSuchElementException("No more elements in lazy iterator");
            }
            return currentBatch.get(batchIndex++);
        }
        
        private void loadNextBatch() {
            currentBatch.clear();
            batchIndex = 0;
            
            int endIndex = Math.min(currentIndex + batchSize, allKeys.size());
            
            try {
                for (int i = currentIndex; i < endIndex; i++) {
                    String key = allKeys.get(i);
                    Map<String, Object> pointData = timeSeriesStorage.get(key);
                    if (pointData != null) {
                        TimeSeriesPoint point = deserializeTimeSeriesPoint(pointData);
                        if (point != null) {
                            currentBatch.add(point);
                        }
                    }
                }
                currentIndex = endIndex;
                
                logger.debug("Loaded batch of {} points for series {} (total: {})", 
                           currentBatch.size(), seriesId, allKeys.size());
            } catch (Exception e) {
                logger.error("Failed to load batch for lazy time series iterator", e);
            }
        }
    }
    
    /**
     * Get the total count of time series points for a series within a time range without loading all data.
     * This is useful for pagination and progress tracking with large datasets.
     * 
     * @param seriesId the series ID
     * @param startTime the start time
     * @param endTime the end time
     * @return the total count of points in the time range
     */
    public long getTimeSeriesPointCount(String seriesId, Instant startTime, Instant endTime) {
        try {
            if (timeSeriesStorage != null) {
                String startKey = seriesId + ":" + startTime.toEpochMilli();
                String endKey = seriesId + ":" + endTime.toEpochMilli();
                
                long count = 0;
                for (String key : timeSeriesStorage.getKeys()) {
                    if (key.startsWith(seriesId + ":") && key.compareTo(startKey) >= 0 && key.compareTo(endKey) <= 0) {
                        count++;
                    }
                }
                
                logger.debug("Counted {} points for series {} from {} to {}", 
                           count, seriesId, startTime, endTime);
                return count;
            }
        } catch (Exception e) {
            logger.error("Failed to count time series points for series {}", seriesId, e);
        }
        return 0;
    }
    
    /**
     * Clean up old data for a specific series with rollback capabilities.
     * 
     * @param seriesId the series ID
     * @param cutoffTime the cutoff time for data retention
     * @return number of deleted points
     */
    public int cleanupSeriesDataWithRollback(String seriesId, Instant cutoffTime) {
        int deletedCount = 0;
        List<String> deletedKeys = new ArrayList<>();
        
        try {
            if (timeSeriesStorage != null) {
                String cutoffKey = seriesId + ":" + cutoffTime.toEpochMilli();
                
                // Collect keys to delete
                for (String key : timeSeriesStorage.getKeys()) {
                    if (key.startsWith(seriesId + ":") && key.compareTo(cutoffKey) < 0) {
                        deletedKeys.add(key);
                    }
                }
                
                // Delete data with rollback support
                for (String key : deletedKeys) {
                    try {
                        // Store backup data before deletion
                        Map<String, Object> backupData = timeSeriesStorage.get(key);
                        if (backupData != null) {
                            // Store backup in a separate storage area
                            String backupKey = "backup:" + key;
                            timeSeriesStorage.put(backupKey, backupData);
                            
                            // Delete the original data
                            timeSeriesStorage.remove(key);
                            deletedCount++;
                            
                            logger.debug("Deleted old time series point with backup: {}", key);
                        }
                    } catch (Exception e) {
                        logger.warn("Failed to delete time series point {} - skipping", key, e);
                        // Continue with other deletions even if one fails
                    }
                }
                
                // Clean up old backup data (keep backups for 7 days)
                cleanupOldBackupData(seriesId, Instant.now().minus(Duration.ofDays(7)));
            }
        } catch (Exception e) {
            logger.error("Failed to cleanup series data for series {}", seriesId, e);
        }
        
        return deletedCount;
    }
    
    /**
     * Clean up old backup data to prevent storage bloat.
     * 
     * @param seriesId the series ID
     * @param cutoffTime the cutoff time for backup retention
     */
    private void cleanupOldBackupData(String seriesId, Instant cutoffTime) {
        try {
            if (timeSeriesStorage != null) {
                String backupPrefix = "backup:" + seriesId + ":";
                String cutoffKey = backupPrefix + cutoffTime.toEpochMilli();
                
                List<String> oldBackupKeys = new ArrayList<>();
                for (String key : timeSeriesStorage.getKeys()) {
                    if (key.startsWith(backupPrefix) && key.compareTo(cutoffKey) < 0) {
                        oldBackupKeys.add(key);
                    }
                }
                
                // Delete old backup data
                for (String backupKey : oldBackupKeys) {
                    timeSeriesStorage.remove(backupKey);
                    logger.debug("Cleaned up old backup data: {}", backupKey);
                }
                
                if (!oldBackupKeys.isEmpty()) {
                    logger.debug("Cleaned up {} old backup entries for series {}", oldBackupKeys.size(), seriesId);
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to cleanup old backup data for series {}", seriesId, e);
        }
    }
    
    /**
     * Rollback cleanup operations by restoring data from backup.
     * 
     * @param seriesId the series ID
     * @param startTime the start time for rollback
     * @param endTime the end time for rollback
     * @return number of restored points
     */
    public int rollbackCleanup(String seriesId, Instant startTime, Instant endTime) {
        int restoredCount = 0;
        
        try {
            if (timeSeriesStorage != null) {
                String backupPrefix = "backup:" + seriesId + ":";
                String startKey = backupPrefix + startTime.toEpochMilli();
                String endKey = backupPrefix + endTime.toEpochMilli();
                
                List<String> backupKeys = new ArrayList<>();
                for (String key : timeSeriesStorage.getKeys()) {
                    if (key.startsWith(backupPrefix) && key.compareTo(startKey) >= 0 && key.compareTo(endKey) <= 0) {
                        backupKeys.add(key);
                    }
                }
                
                // Restore data from backup
                for (String backupKey : backupKeys) {
                    try {
                        Map<String, Object> backupData = timeSeriesStorage.get(backupKey);
                        if (backupData != null) {
                            // Restore to original key
                            String originalKey = backupKey.substring(7); // Remove "backup:" prefix
                            timeSeriesStorage.put(originalKey, backupData);
                            
                            // Remove backup
                            timeSeriesStorage.remove(backupKey);
                            restoredCount++;
                            
                            logger.debug("Restored time series point from backup: {}", originalKey);
                        }
                    } catch (Exception e) {
                        logger.warn("Failed to restore time series point from backup {} - skipping", backupKey, e);
                    }
                }
                
                logger.info("Rollback completed - restored {} time series points for series {}", 
                           restoredCount, seriesId);
            }
        } catch (Exception e) {
            logger.error("Failed to rollback cleanup for series {}", seriesId, e);
        }
        
        return restoredCount;
    }
}
