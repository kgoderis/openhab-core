# Advanced Metrics Architecture Plan

## Current Implementation Status

### ✅ **COMPLETED: Phase 3 - Unified Metrics Architecture**

### 🚀 **NEW: Phase 3.6 - Time Series Hybrid Architecture with StorageService**

This phase introduces a hybrid time series storage architecture that combines the existing centralized MetricsService with persistent time series storage using openHAB's StorageService. This approach provides both real-time metrics collection and historical data analysis capabilities without dependency on openHAB Items.

#### **Architecture Overview**
```
┌─────────────────────────────────────────────────────────────┐
│                Time Series Hybrid Architecture              │
├─────────────────────────────────────────────────────────────┤
│  MetricsService (Real-time Collection)                     │
│  ├── DefaultMetricsService (Current Implementation)        │
│  │   ├── OperationRecorder (Builder Pattern)               │
│  │   ├── SnapshotFactory (Type-Safe Snapshots)             │
│  │   └── StatisticsFactory (Historical Analysis)           │
│  │                                                         │
│  MetricTimeSeriesStorage (Historical Storage)                  │
│  ├── StorageServiceTimeSeriesStorage (Primary Implementation) │
│  │   ├── Time Series Points Storage                        │
│  │   ├── Aggregated Metrics Storage                        │
│  │   └── Query Interface                                   │
│  │                                                         │
│  StorageService Integration (Configuration & Metadata)     │
│  ├── Configuration Storage                                 │
│  ├── Metadata Storage                                      │
│  └── Backup & Recovery                                     │
│  │                                                         │
│  Benefits:                                                  │
│  • Real-time metrics collection (existing)                 │
│  • Historical data persistence (new)                       │
│  • No dependency on openHAB Items                          │
│  • Hybrid storage for different data types                 │
│  • Advanced querying and aggregation                       │
│  • Automatic data aging and cleanup                        │
└─────────────────────────────────────────────────────────────┘
```

#### **Implementation Plan**

##### **3.6.1 Core Time Series Storage Interface**

**Tasks:**
- [ ] **Interface Design and Definition**
  - [ ] Create MetricTimeSeriesStorage interface in `org.openhab.core.ai.common.monitoring.timeseries`
  - [ ] Define core methods: storeTimeSeriesPoint, queryTimeSeries, queryWithAggregation
  - [ ] Add utility methods: getAvailableSeries, cleanupOldData
  - [ ] Document interface with comprehensive javadoc
  - [ ] Add null safety annotations (@NonNullByDefault, @Nullable)

- [ ] **Data Model Creation**
  - [ ] Create TimeSeriesPoint record class
  - [ ] Create AggregatedPoint record class
  - [ ] Create TimeSeriesQueryCriteria class for complex queries
  - [ ] Create TimeSeriesMetadata class for series information
  - [ ] Add validation methods to data models
  - [ ] Implement equals/hashCode/toString for all models

- [ ] **Query and Aggregation Interface**
  - [ ] Define aggregation functions enum (AVG, MIN, MAX, SUM, COUNT, FIRST, LAST)
  - [ ] Create TimeSeriesQueryBuilder for fluent query construction
  - [ ] Define aggregation period types (SECOND, MINUTE, HOUR, DAY, WEEK, MONTH)
  - [ ] Add query result pagination support
  - [ ] Create query performance optimization hints

- [ ] **Configuration Management Interface**
  - [ ] Create MetricTimeSeriesConfiguration interface
  - [ ] Define configuration properties (retention, aggregation, cleanup)
  - [ ] Add configuration validation methods
  - [ ] Create configuration change listeners
  - [ ] Add configuration backup/restore methods

**Deliverables:**
```java
@NonNullByDefault
public interface MetricTimeSeriesStorage {
    
    /**
     * Store a time series point with tags and fields
     */
    void storeTimeSeriesPoint(String seriesId, Instant timestamp, 
                             Map<String, String> tags, Map<String, Object> fields);
    
    /**
     * Query time series data within a time range
     */
    List<TimeSeriesPoint> queryTimeSeries(String seriesId, Instant startTime, Instant endTime);
    
    /**
     * Query with aggregation (avg, min, max, sum, count)
     */
    List<AggregatedPoint> queryWithAggregation(String seriesId, Instant startTime, 
                                              Instant endTime, String aggregationFunction, 
                                              Duration aggregationPeriod);
    
    /**
     * Get available time series
     */
    Set<String> getAvailableSeries();
    
    /**
     * Clean up old data based on retention policy
     */
    void cleanupOldData(Duration retentionPeriod);
}

@NonNullByDefault
public record TimeSeriesPoint(
    Instant timestamp,
    Map<String, String> tags,
    Map<String, Object> fields
) {}

@NonNullByDefault
public record AggregatedPoint(
    Instant timestamp,
    Duration period,
    String aggregationFunction,
    Map<String, Object> aggregatedValues
) {}
```

##### **3.6.2 StorageService Time Series Storage Implementation**

**Tasks:**
- [ ] **StorageService Integration Setup**
  - [ ] Create StorageServiceTimeSeriesStorage class in `org.openhab.core.ai.common.monitoring.timeseries.storage`
  - [ ] Implement OSGi component annotations (@Component, @Activate, @Deactivate)
  - [ ] Set up StorageService dependency injection (@Reference)
  - [ ] Create storage instances for time series data, metadata, and configuration
  - [ ] Implement proper error handling for StorageService unavailability
  - [ ] Add storage initialization validation and logging

- [ ] **Data Structure Implementation**
  - [ ] Create Storage<TimeSeriesPoint> for time series points storage
  - [ ] Create Storage<TimeSeriesMetadata> for series metadata storage
  - [ ] Create Storage<AggregatedPoint> for aggregated data storage
  - [ ] Implement proper serialization for TimeSeriesPoint and AggregatedPoint
  - [ ] Add data structure validation and integrity checks
  - [ ] Implement thread-safe access patterns using StorageService

- [ ] **Core Storage Operations**
  - [ ] Implement storeTimeSeriesPoint method with error handling
  - [ ] Add automatic metadata updates on data insertion
  - [ ] Implement efficient key generation for time series data
  - [ ] Add data validation before storage
  - [ ] Implement batch operations for performance optimization
  - [ ] Add storage operation metrics and monitoring

- [ ] **Query Implementation**
  - [ ] Implement queryTimeSeries with time range filtering
  - [ ] Add sorting and ordering capabilities using StorageService queries
  - [ ] Implement result pagination and limiting
  - [ ] Add query performance optimization with proper key design
  - [ ] Implement query result caching for frequently accessed data
  - [ ] Add query execution metrics and logging

- [ ] **Aggregation Logic**
  - [ ] Implement real-time aggregation calculation
  - [ ] Add pre-computed aggregation storage using StorageService
  - [ ] Implement aggregation scheduling and triggers
  - [ ] Add support for multiple aggregation functions (AVG, MIN, MAX, SUM, COUNT)
  - [ ] Implement aggregation period management
  - [ ] Add aggregation result validation and error handling

- [ ] **Data Optimization and Management**
  - [ ] Implement efficient key design for time series data access
  - [ ] Add data deduplication logic using StorageService capabilities
  - [ ] Implement efficient storage format for time series data
  - [ ] Add memory usage optimization with StorageService
  - [ ] Implement lazy loading for large datasets
  - [ ] Add storage size monitoring and alerts

- [ ] **Retention Policies**
  - [ ] Implement automatic data cleanup based on retention period
  - [ ] Add configurable retention policies per series
  - [ ] Implement cleanup scheduling and execution
  - [ ] Add cleanup operation logging and metrics
  - [ ] Implement graceful cleanup with system resource management
  - [ ] Add cleanup operation rollback capabilities

**Deliverables:**
```java
@Component(service = MetricTimeSeriesStorage.class)
@NonNullByDefault
public class StorageServiceTimeSeriesStorage implements MetricTimeSeriesStorage {
    
    private static final Logger logger = LoggerFactory.getLogger(StorageServiceTimeSeriesStorage.class);
    
    // StorageService instances for different data types
    @Reference
    private @Nullable StorageService storageService;
    
    private @Nullable Storage<Map<String, Object>> timeSeriesStorage;
    private @Nullable Storage<Map<String, Object>> metadataStorage;
    private @Nullable Storage<Map<String, Object>> aggregatedStorage;
    private @Nullable Storage<Map<String, Object>> configurationStorage;
    
    // Configuration
    private final Duration defaultRetentionPeriod = Duration.ofDays(30);
    private final Duration aggregationPeriod = Duration.ofHours(1);
    
    @Activate
    public void activate() {
        try {
            if (storageService != null) {
                // Initialize storage instances
                timeSeriesStorage = storageService.getStorage("ai-timeseries-data", this.getClass().getClassLoader());
                metadataStorage = storageService.getStorage("ai-timeseries-metadata", this.getClass().getClassLoader());
                aggregatedStorage = storageService.getStorage("ai-timeseries-aggregated", this.getClass().getClassLoader());
                configurationStorage = storageService.getStorage("ai-timeseries-config", this.getClass().getClassLoader());
                
                // Schedule cleanup task
                scheduleCleanup();
                
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
        // StorageService handles cleanup automatically
        logger.info("StorageService time series storage deactivated");
    }
    
    @Override
    public void storeTimeSeriesPoint(String seriesId, Instant timestamp, 
                                   Map<String, String> tags, Map<String, Object> fields) {
        try {
            if (timeSeriesStorage != null) {
                TimeSeriesPoint point = new TimeSeriesPoint(timestamp, tags, fields);
                
                // Create storage key with timestamp for efficient querying
                String storageKey = seriesId + ":" + timestamp.toEpochMilli();
                
                // Store the time series point
                Map<String, Object> pointData = new HashMap<>();
                pointData.put("timestamp", timestamp.toEpochMilli());
                pointData.put("tags", tags);
                pointData.put("fields", fields);
                
                timeSeriesStorage.put(storageKey, pointData);
                
                // Update metadata
                updateSeriesMetadata(seriesId, timestamp);
                
                // Trigger aggregation if needed
                scheduleAggregation(seriesId, timestamp);
                
            } else {
                logger.warn("Time series storage not available - cannot store point for series: {}", seriesId);
            }
            
        } catch (Exception e) {
            logger.error("Failed to store time series point for series: {}", seriesId, e);
        }
    }
    
    @Override
    public List<TimeSeriesPoint> queryTimeSeries(String seriesId, Instant startTime, Instant endTime) {
        List<TimeSeriesPoint> points = timeSeriesMap.get(seriesId);
        if (points == null) {
            return Collections.emptyList();
        }
        
        return points.stream()
            .filter(point -> point.timestamp().isAfter(startTime) && 
                            point.timestamp().isBefore(endTime))
            .sorted(Comparator.comparing(TimeSeriesPoint::timestamp))
            .collect(Collectors.toList());
    }
    
    @Override
    public List<AggregatedPoint> queryWithAggregation(String seriesId, Instant startTime, 
                                                     Instant endTime, String aggregationFunction, 
                                                     Duration aggregationPeriod) {
        // Check if aggregated data exists
        String aggregationKey = seriesId + ":" + aggregationFunction + ":" + aggregationPeriod.toMinutes();
        List<AggregatedPoint> aggregated = aggregatedData.get(aggregationKey);
        
        if (aggregated != null && !aggregated.isEmpty()) {
            return aggregated.stream()
                .filter(point -> point.timestamp().isAfter(startTime) && 
                                point.timestamp().isBefore(endTime))
                .sorted(Comparator.comparing(AggregatedPoint::timestamp))
                .collect(Collectors.toList());
        }
        
        // Fallback to real-time aggregation
        return performRealTimeAggregation(seriesId, startTime, endTime, 
                                        aggregationFunction, aggregationPeriod);
    }
    
    private void updateSeriesMetadata(String seriesId, Instant timestamp) {
        seriesMetadata.compute(seriesId, (key, existing) -> {
            if (existing == null) {
                existing = new HashMap<>();
            }
            existing.put("lastUpdate", timestamp.toString());
            existing.put("pointCount", (Integer) existing.getOrDefault("pointCount", 0) + 1);
            return existing;
        });
    }
    
    private void scheduleAggregation(String seriesId, Instant timestamp) {
        // Simple aggregation scheduling - in production, use a proper scheduler
        if (timestamp.getEpochSecond() % aggregationPeriod.toSeconds() == 0) {
            performAggregation(seriesId, timestamp, aggregationPeriod);
        }
    }
    
    private void performAggregation(String seriesId, Instant timestamp, Duration period) {
        Instant periodStart = timestamp.minus(period);
        List<TimeSeriesPoint> points = queryTimeSeries(seriesId, periodStart, timestamp);
        
        if (points.isEmpty()) return;
        
        // Calculate aggregations
        Map<String, Object> aggregatedValues = calculateAggregations(points);
        
        AggregatedPoint aggregatedPoint = new AggregatedPoint(
            timestamp, period, "avg", aggregatedValues
        );
        
        String aggregationKey = seriesId + ":avg:" + period.toMinutes();
        aggregatedData.compute(aggregationKey, (key, existing) -> {
            if (existing == null) {
                existing = new ArrayList<>();
            }
            existing.add(aggregatedPoint);
            return existing;
        });
    }
    
    private Map<String, Object> calculateAggregations(List<TimeSeriesPoint> points) {
        Map<String, Object> result = new HashMap<>();
        
        // Calculate numeric field aggregations
        Map<String, List<Double>> numericFields = new HashMap<>();
        
        for (TimeSeriesPoint point : points) {
            for (Map.Entry<String, Object> entry : point.fields().entrySet()) {
                if (entry.getValue() instanceof Number) {
                    numericFields.computeIfAbsent(entry.getKey(), k -> new ArrayList<>())
                        .add(((Number) entry.getValue()).doubleValue());
                }
            }
        }
        
        for (Map.Entry<String, List<Double>> entry : numericFields.entrySet()) {
            List<Double> values = entry.getValue();
            if (!values.isEmpty()) {
                result.put(entry.getKey() + "_avg", values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0));
                result.put(entry.getKey() + "_min", values.stream().mapToDouble(Double::doubleValue).min().orElse(0.0));
                result.put(entry.getKey() + "_max", values.stream().mapToDouble(Double::doubleValue).max().orElse(0.0));
                result.put(entry.getKey() + "_sum", values.stream().mapToDouble(Double::doubleValue).sum());
                result.put(entry.getKey() + "_count", values.size());
            }
        }
        
        return result;
    }
    
    @Override
    public Set<String> getAvailableSeries() {
        return new HashSet<>(timeSeriesMap.keySet());
    }
    
    @Override
    public void cleanupOldData(Duration retentionPeriod) {
        Instant cutoffTime = Instant.now().minus(retentionPeriod);
        
        timeSeriesMap.entrySet().removeIf(entry -> {
            List<TimeSeriesPoint> points = entry.getValue();
            points.removeIf(point -> point.timestamp().isBefore(cutoffTime));
            return points.isEmpty();
        });
        
        // Clean up aggregated data
        aggregatedData.entrySet().removeIf(entry -> {
            List<AggregatedPoint> points = entry.getValue();
            points.removeIf(point -> point.timestamp().isBefore(cutoffTime));
            return points.isEmpty();
        });
        
        logger.info("Cleaned up time series data older than: {}", cutoffTime);
    }
}
```

##### **3.6.3 StorageService Integration for Configuration and Metadata**

**Tasks:**
- [ ] **StorageService Integration Setup**
  - [ ] Create DefaultMetricTimeSeriesStorage class in `org.openhab.core.ai.common.monitoring.timeseries.storage`
  - [ ] Implement OSGi component annotations and service registration
  - [ ] Set up StorageService dependency injection (@Reference)
  - [ ] Create storage instances for time series, configuration, and metadata
  - [ ] Implement proper error handling for StorageService unavailability
  - [ ] Add storage initialization validation and logging

- [ ] **Configuration Storage Interface**
  - [ ] Create configuration storage schema and structure
  - [ ] Implement configuration CRUD operations (create, read, update, delete)
  - [ ] Add configuration validation and schema enforcement
  - [ ] Implement configuration versioning and migration
  - [ ] Add configuration change notifications and listeners
  - [ ] Create configuration backup and restore functionality

- [ ] **Metadata Management**
  - [ ] Implement series metadata storage and retrieval
  - [ ] Add metadata update triggers and automatic updates
  - [ ] Create metadata querying and filtering capabilities
  - [ ] Implement metadata statistics and analytics
  - [ ] Add metadata validation and consistency checks
  - [ ] Create metadata export and import functionality

- [ ] **Time Series Data Storage**
  - [ ] Implement time series point storage using StorageService
  - [ ] Add efficient key generation for time series data
  - [ ] Implement data serialization and deserialization
  - [ ] Add data integrity validation and error recovery
  - [ ] Implement batch operations for performance optimization
  - [ ] Add data compression and storage optimization

- [ ] **Query Implementation for StorageService**
  - [ ] Implement time range querying with StorageService
  - [ ] Add result filtering and sorting capabilities
  - [ ] Implement query result pagination and limiting
  - [ ] Add query performance optimization
  - [ ] Implement query result caching
  - [ ] Add query execution monitoring and metrics

- [ ] **Backup and Recovery Capabilities**
  - [ ] Implement automatic backup scheduling
  - [ ] Add manual backup creation and management
  - [ ] Implement backup verification and integrity checks
  - [ ] Add backup restoration functionality
  - [ ] Create backup retention and cleanup policies
  - [ ] Implement disaster recovery procedures

- [ ] **Performance and Monitoring**
  - [ ] Add storage operation performance metrics
  - [ ] Implement storage health monitoring
  - [ ] Add storage capacity monitoring and alerts
  - [ ] Create storage performance optimization
  - [ ] Implement storage operation logging
  - [ ] Add storage maintenance and cleanup procedures

**Deliverables:**
```java
@Component(service = MetricTimeSeriesStorage.class)
@NonNullByDefault
public class DefaultMetricTimeSeriesStorage implements MetricTimeSeriesStorage {
    
    private static final Logger logger = LoggerFactory.getLogger(DefaultMetricTimeSeriesStorage.class);
    
    @Reference
    private @Nullable StorageService storageService;
    
    // Storage instances
    private Storage<Map<String, Object>> timeSeriesStorage;
    private Storage<Map<String, Object>> configurationStorage;
    private Storage<Map<String, Object>> metadataStorage;
    
    @Activate
    public void activate() {
        if (storageService != null) {
            timeSeriesStorage = storageService.getStorage("ai-timeseries", this.getClass().getClassLoader());
            configurationStorage = storageService.getStorage("ai-timeseries-config", this.getClass().getClassLoader());
            metadataStorage = storageService.getStorage("ai-timeseries-metadata", this.getClass().getClassLoader());
            
            logger.info("StorageService time series storage initialized");
        } else {
            logger.warn("StorageService not available, time series storage disabled");
        }
    }
    
    @Override
    public void storeTimeSeriesPoint(String seriesId, Instant timestamp, 
                                   Map<String, String> tags, Map<String, Object> fields) {
        if (timeSeriesStorage == null) return;
        
        try {
            String key = seriesId + ":" + timestamp.toEpochMilli();
            Map<String, Object> data = new HashMap<>();
            data.put("timestamp", timestamp.toString());
            data.put("tags", tags);
            data.put("fields", fields);
            
            timeSeriesStorage.put(key, data);
            
            // Update metadata
            updateMetadata(seriesId, timestamp);
            
        } catch (Exception e) {
            logger.error("Failed to store time series point: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public List<TimeSeriesPoint> queryTimeSeries(String seriesId, Instant startTime, Instant endTime) {
        if (timeSeriesStorage == null) return Collections.emptyList();
        
        List<TimeSeriesPoint> points = new ArrayList<>();
        
        try {
            timeSeriesStorage.stream()
                .filter(entry -> entry.getKey().startsWith(seriesId + ":"))
                .forEach(entry -> {
                    try {
                        String[] parts = entry.getKey().split(":");
                        long timestamp = Long.parseLong(parts[1]);
                        Instant pointTime = Instant.ofEpochMilli(timestamp);
                        
                        if (pointTime.isAfter(startTime) && pointTime.isBefore(endTime)) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> data = (Map<String, Object>) entry.getValue();
                            
                            @SuppressWarnings("unchecked")
                            Map<String, String> tags = (Map<String, String>) data.get("tags");
                            
                            @SuppressWarnings("unchecked")
                            Map<String, Object> fields = (Map<String, Object>) data.get("fields");
                            
                            points.add(new TimeSeriesPoint(pointTime, tags, fields));
                        }
                    } catch (Exception e) {
                        logger.warn("Failed to process time series point: {}", e.getMessage());
                    }
                });
                
        } catch (Exception e) {
            logger.error("Failed to query time series: {}", e.getMessage(), e);
        }
        
        return points.stream()
            .sorted(Comparator.comparing(TimeSeriesPoint::timestamp))
            .collect(Collectors.toList());
    }
    
    private void updateMetadata(String seriesId, Instant timestamp) {
        if (metadataStorage == null) return;
        
        metadataStorage.compute(seriesId, (key, existing) -> {
            if (existing == null) {
                existing = new HashMap<>();
            }
            existing.put("lastUpdate", timestamp.toString());
            existing.put("pointCount", (Integer) existing.getOrDefault("pointCount", 0) + 1);
            return existing;
        });
    }
    
    // Configuration management
    public void storeConfiguration(String configId, Map<String, Object> config) {
        if (configurationStorage != null) {
            configurationStorage.put(configId, config);
        }
    }
    
    public Map<String, Object> getConfiguration(String configId) {
        if (configurationStorage != null) {
            return configurationStorage.get(configId);
        }
        return Collections.emptyMap();
    }
    
    // Backup and recovery
    public void createBackup(String backupId) {
        // Implementation for creating backup of time series data
        logger.info("Creating backup: {}", backupId);
    }
    
    public void restoreBackup(String backupId) {
        // Implementation for restoring from backup
        logger.info("Restoring backup: {}", backupId);
    }
}
```

##### **3.6.4 Enhanced MetricsService Integration**

**Tasks:**
- [ ] **MetricsService Enhancement**
  - [ ] Create EnhancedMetricsService class extending existing MetricsService
  - [ ] Add MetricTimeSeriesStorage dependency injection (@Reference)
  - [ ] Implement dual recording (real-time + time series)
  - [ ] Add time series recording error handling and fallback
  - [ ] Implement time series recording performance optimization
  - [ ] Add time series recording configuration and toggles

- [ ] **Automatic Time Series Recording**
  - [ ] Implement automatic time series point creation from metrics
  - [ ] Add intelligent data transformation (metrics → time series format)
  - [ ] Implement batch time series recording for performance
  - [ ] Add time series recording scheduling and triggers
  - [ ] Implement time series recording rate limiting and throttling
  - [ ] Add time series recording quality of service management

- [ ] **Historical Metrics Retrieval**
  - [ ] Implement getHistoricalMetrics method with time range support
  - [ ] Add getAggregatedMetrics method with aggregation functions
  - [ ] Create getMetricsAnalytics method with statistical analysis
  - [ ] Implement metrics trend analysis and pattern detection
  - [ ] Add metrics comparison and benchmarking capabilities
  - [ ] Create metrics forecasting and prediction methods

- [ ] **Analytics and Reporting Features**
  - [ ] Implement statistical analysis (mean, median, percentiles)
  - [ ] Add trend analysis and change detection
  - [ ] Create performance analytics and bottleneck identification
  - [ ] Implement anomaly detection and alerting
  - [ ] Add comparative analysis and benchmarking
  - [ ] Create custom analytics and reporting framework

- [ ] **Data Transformation and Mapping**
  - [ ] Implement metrics to time series data mapping
  - [ ] Add context data transformation and normalization
  - [ ] Create tag generation and management
  - [ ] Implement field extraction and validation
  - [ ] Add data type conversion and optimization
  - [ ] Create data quality validation and cleansing

- [ ] **Performance Optimization**
  - [ ] Implement asynchronous time series recording
  - [ ] Add time series recording batching and buffering
  - [ ] Create time series recording compression and optimization
  - [ ] Implement time series recording caching strategies
  - [ ] Add time series recording load balancing
  - [ ] Create time series recording resource management

- [ ] **Integration Testing and Validation**
  - [ ] Create comprehensive integration tests
  - [ ] Add performance benchmarking and validation
  - [ ] Implement data consistency validation
  - [ ] Add error handling and recovery testing
  - [ ] Create load testing and stress testing
  - [ ] Add end-to-end workflow testing

**Deliverables:**
```java
@Component(service = MetricsService.class)
@NonNullByDefault
public class EnhancedMetricsService implements MetricsService {
    
    private static final Logger logger = LoggerFactory.getLogger(EnhancedMetricsService.class);
    
    @Reference
    private @Nullable MetricTimeSeriesStorage timeSeriesStorage;
    
    @Reference
    private @Nullable MetricsRegistry metricsRegistry;
    
    // Existing metrics collection (unchanged)
    private final Map<MetricKey, MetricsCollector> collectors = new ConcurrentHashMap<>();
    
    @Override
    public void recordOperation(String domain, String operation, boolean success, 
                              Duration duration, Map<String, Object> context) {
        // Record to existing centralized metrics
        recordToCentralizedMetrics(domain, operation, success, duration, context);
        
        // Also store as time series for historical analysis
        if (timeSeriesStorage != null) {
            storeAsTimeSeries(domain, operation, success, duration, context);
        }
    }
    
    private void recordToCentralizedMetrics(String domain, String operation, boolean success, 
                                          Duration duration, Map<String, Object> context) {
        // Existing implementation unchanged
        MetricKey key = MetricKeys.operation(domain, operation);
        MetricsCollector collector = collectors.computeIfAbsent(key, k -> new MetricsCollector());
        collector.recordExecution(success, duration.toNanos());
    }
    
    private void storeAsTimeSeries(String domain, String operation, boolean success, 
                                 Duration duration, Map<String, Object> context) {
        try {
            String seriesId = "metrics:" + domain + ":" + operation;
            Instant timestamp = Instant.now();
            
            Map<String, String> tags = Map.of(
                "domain", domain,
                "operation", operation,
                "success", String.valueOf(success)
            );
            
            Map<String, Object> fields = new HashMap<>();
            fields.put("duration_ms", duration.toMillis());
            fields.put("success", success ? 1 : 0);
            fields.put("timestamp", timestamp.toEpochMilli());
            
            // Add context data as fields
            context.forEach((key, value) -> {
                if (value instanceof Number) {
                    fields.put("context_" + key, value);
                } else {
                    fields.put("context_" + key, value.toString());
                }
            });
            
            timeSeriesStorage.storeTimeSeriesPoint(seriesId, timestamp, tags, fields);
            
        } catch (Exception e) {
            logger.error("Failed to store metrics as time series: {}", e.getMessage(), e);
        }
    }
    
    // New methods for historical analysis
    public List<TimeSeriesPoint> getHistoricalMetrics(String domain, String operation, 
                                                     Instant startTime, Instant endTime) {
        if (timeSeriesStorage == null) {
            return Collections.emptyList();
        }
        
        String seriesId = "metrics:" + domain + ":" + operation;
        return timeSeriesStorage.queryTimeSeries(seriesId, startTime, endTime);
    }
    
    public List<AggregatedPoint> getAggregatedMetrics(String domain, String operation, 
                                                     Instant startTime, Instant endTime,
                                                     String aggregationFunction, Duration period) {
        if (timeSeriesStorage == null) {
            return Collections.emptyList();
        }
        
        String seriesId = "metrics:" + domain + ":" + operation;
        return timeSeriesStorage.queryWithAggregation(seriesId, startTime, endTime, 
                                                    aggregationFunction, period);
    }
    
    public Map<String, Object> getMetricsAnalytics(String domain, String operation, 
                                                  Instant startTime, Instant endTime) {
        List<TimeSeriesPoint> points = getHistoricalMetrics(domain, operation, startTime, endTime);
        
        if (points.isEmpty()) {
            return Collections.emptyMap();
        }
        
        // Calculate analytics
        List<Double> durations = points.stream()
            .map(point -> (Double) point.fields().get("duration_ms"))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        
        List<Integer> successes = points.stream()
            .map(point -> (Integer) point.fields().get("success"))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        
        Map<String, Object> analytics = new HashMap<>();
        analytics.put("totalOperations", points.size());
        analytics.put("successRate", successes.stream().mapToInt(Integer::intValue).average().orElse(0.0));
        analytics.put("avgDuration", durations.stream().mapToDouble(Double::doubleValue).average().orElse(0.0));
        analytics.put("minDuration", durations.stream().mapToDouble(Double::doubleValue).min().orElse(0.0));
        analytics.put("maxDuration", durations.stream().mapToDouble(Double::doubleValue).max().orElse(0.0));
        analytics.put("p95Duration", calculatePercentile(durations, 0.95));
        analytics.put("p99Duration", calculatePercentile(durations, 0.99));
        
        return analytics;
    }
    
    private double calculatePercentile(List<Double> values, double percentile) {
        if (values.isEmpty()) return 0.0;
        
        List<Double> sorted = values.stream().sorted().collect(Collectors.toList());
        int index = (int) Math.ceil(percentile * sorted.size()) - 1;
        return sorted.get(Math.max(0, Math.min(index, sorted.size() - 1)));
    }
}
```

##### **3.6.5 Configuration and Management**

**Tasks:**
- [ ] **Configuration Management Interface**
  - [ ] Create MetricTimeSeriesConfiguration class in `org.openhab.core.ai.common.monitoring.timeseries.config`
  - [ ] Implement OSGi configuration management (@ObjectClassDefinition, @Designate)
  - [ ] Add configuration properties validation and constraints
  - [ ] Implement configuration change handling (@Modified)
  - [ ] Add configuration persistence and backup
  - [ ] Create configuration migration and upgrade procedures

- [ ] **Retention Policies Implementation**
  - [ ] Implement configurable retention periods per series type
  - [ ] Add automatic cleanup scheduling and execution
  - [ ] Create retention policy validation and enforcement
  - [ ] Implement retention policy change notifications
  - [ ] Add retention policy compliance monitoring
  - [ ] Create retention policy audit and reporting

- [ ] **Monitoring and Health Checks**
  - [ ] Implement storage health monitoring and status checks
  - [ ] Add performance monitoring and metrics collection
  - [ ] Create storage capacity monitoring and alerts
  - [ ] Implement data integrity monitoring and validation
  - [ ] Add system resource monitoring (CPU, memory, disk)
  - [ ] Create health check reporting and dashboard

- [ ] **Administrative Tools**
  - [ ] Create administrative REST API endpoints
  - [ ] Implement storage management commands and operations
  - [ ] Add data export and import functionality
  - [ ] Create storage statistics and reporting tools
  - [ ] Implement storage maintenance and optimization tools
  - [ ] Add storage troubleshooting and diagnostic tools

- [ ] **Scheduling and Automation**
  - [ ] Implement cleanup task scheduling with Quartz or similar
  - [ ] Add aggregation task scheduling and management
  - [ ] Create backup task scheduling and automation
  - [ ] Implement maintenance task scheduling
  - [ ] Add task failure handling and retry mechanisms
  - [ ] Create task monitoring and alerting

- [ ] **Security and Access Control**
  - [ ] Implement access control for administrative operations
  - [ ] Add audit logging for configuration changes
  - [ ] Create secure configuration storage
  - [ ] Implement data encryption for sensitive information
  - [ ] Add authentication and authorization for admin tools
  - [ ] Create security monitoring and threat detection

- [ ] **Documentation and Support**
  - [ ] Create comprehensive configuration documentation
  - [ ] Add troubleshooting guides and FAQ
  - [ ] Implement configuration validation and error reporting
  - [ ] Create performance tuning guides
  - [ ] Add best practices documentation
  - [ ] Create migration and upgrade guides

**Deliverables:**
```java
@Component(service = MetricTimeSeriesConfiguration.class)
@NonNullByDefault
public class MetricTimeSeriesConfiguration {
    
    private static final Logger logger = LoggerFactory.getLogger(MetricTimeSeriesConfiguration.class);
    
    @Reference
    private @Nullable MetricTimeSeriesStorage timeSeriesStorage;
    
    // Configuration properties
    private Duration retentionPeriod = Duration.ofDays(30);
    private Duration aggregationPeriod = Duration.ofHours(1);
    private boolean autoCleanup = true;
    private int maxPointsPerSeries = 10000;
    
    @Activate
    public void activate(Map<String, Object> config) {
        updateConfiguration(config);
        
        if (autoCleanup) {
            scheduleCleanup();
        }
    }
    
    @Modified
    public void modified(Map<String, Object> config) {
        updateConfiguration(config);
    }
    
    private void updateConfiguration(Map<String, Object> config) {
        retentionPeriod = Duration.ofDays((Integer) config.getOrDefault("retentionDays", 30));
        aggregationPeriod = Duration.ofHours((Integer) config.getOrDefault("aggregationHours", 1));
        autoCleanup = (Boolean) config.getOrDefault("autoCleanup", true);
        maxPointsPerSeries = (Integer) config.getOrDefault("maxPointsPerSeries", 10000);
        
        logger.info("Time series configuration updated: retention={}, aggregation={}, autoCleanup={}", 
                   retentionPeriod, aggregationPeriod, autoCleanup);
    }
    
    private void scheduleCleanup() {
        // Schedule daily cleanup
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            if (timeSeriesStorage != null) {
                timeSeriesStorage.cleanupOldData(retentionPeriod);
            }
        }, 24, 24, TimeUnit.HOURS);
    }
    
    public Map<String, Object> getStorageStatistics() {
        if (timeSeriesStorage == null) {
            return Collections.emptyMap();
        }
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("availableSeries", timeSeriesStorage.getAvailableSeries().size());
        stats.put("retentionPeriod", retentionPeriod.toString());
        stats.put("aggregationPeriod", aggregationPeriod.toString());
        stats.put("autoCleanup", autoCleanup);
        stats.put("maxPointsPerSeries", maxPointsPerSeries);
        
        return stats;
    }
}
```

#### **3.6.6 Implementation Timeline**

**Week 1: Core Infrastructure**
- [ ] **Interface and Data Models (Days 1-2)**
  - [ ] Implement MetricTimeSeriesStorage interface
  - [ ] Create TimeSeriesPoint and AggregatedPoint data models
  - [ ] Create TimeSeriesQueryCriteria and TimeSeriesMetadata classes
  - [ ] Add validation methods and null safety annotations
  - [ ] Create aggregation functions enum and query builder

- [ ] **MapDB Integration Setup (Days 3-4)**
  - [ ] Add MapDB dependency to pom.xml
  - [ ] Create MapDBTimeSeriesStorage class with OSGi annotations
  - [ ] Set up MapDB database initialization and configuration
  - [ ] Create storage directory structure and error handling
  - [ ] Implement basic database connection management

- [ ] **Basic Storage Operations (Days 5-7)**
  - [ ] Implement storeTimeSeriesPoint method
  - [ ] Add automatic metadata updates
  - [ ] Implement memory management and data validation
  - [ ] Add basic queryTimeSeries functionality
  - [ ] Create initial unit tests for core functionality

**Week 2: Advanced Features**
- [ ] **Query and Aggregation Logic (Days 1-3)**
  - [ ] Implement advanced querying with time range filtering
  - [ ] Add sorting, ordering, and pagination capabilities
  - [ ] Implement real-time aggregation calculation
  - [ ] Add pre-computed aggregation storage
  - [ ] Create aggregation scheduling and triggers

- [ ] **Data Optimization (Days 4-5)**
  - [ ] Implement data compression for historical data
  - [ ] Add data deduplication and storage optimization
  - [ ] Implement efficient storage format for time series
  - [ ] Add memory usage optimization and lazy loading
  - [ ] Create storage size monitoring and alerts

- [ ] **Retention Policies (Days 6-7)**
  - [ ] Implement automatic data cleanup based on retention period
  - [ ] Add configurable retention policies per series
  - [ ] Implement cleanup scheduling and execution
  - [ ] Add cleanup operation logging and metrics
  - [ ] Create retention policy validation and enforcement

**Week 3: Integration**
- [ ] **StorageService Integration (Days 1-2)**
  - [ ] Create DefaultMetricTimeSeriesStorage class
  - [ ] Implement StorageService dependency injection
  - [ ] Add configuration and metadata storage
  - [ ] Implement backup and recovery capabilities
  - [ ] Create storage operation performance monitoring

- [ ] **MetricsService Integration (Days 3-4)**
  - [ ] Create EnhancedMetricsService class
  - [ ] Implement dual recording (real-time + time series)
  - [ ] Add automatic time series recording
  - [ ] Implement historical metrics retrieval methods
  - [ ] Create analytics and reporting features

- [ ] **Configuration Management (Days 5-6)**
  - [ ] Create MetricTimeSeriesConfiguration class
  - [ ] Implement OSGi configuration management
  - [ ] Add configuration validation and change handling
  - [ ] Implement retention policy configuration
  - [ ] Create configuration backup and migration

- [ ] **Monitoring and Health Checks (Day 7)**
  - [ ] Implement storage health monitoring
  - [ ] Add performance monitoring and metrics collection
  - [ ] Create storage capacity monitoring and alerts
  - [ ] Implement data integrity monitoring
  - [ ] Add system resource monitoring

**Week 4: Testing and Optimization**
- [ ] **Comprehensive Testing (Days 1-3)**
  - [ ] Create unit tests for all components
  - [ ] Add integration tests for end-to-end workflows
  - [ ] Implement performance benchmarking and validation
  - [ ] Add load testing and stress testing
  - [ ] Create data consistency validation tests

- [ ] **Performance Optimization (Days 4-5)**
  - [ ] Optimize query performance with indexing
  - [ ] Implement query result caching
  - [ ] Add batch operations for performance
  - [ ] Optimize memory usage and garbage collection
  - [ ] Create performance monitoring and tuning

- [ ] **Documentation and Production Readiness (Days 6-7)**
  - [ ] Create comprehensive API documentation
  - [ ] Add configuration and usage examples
  - [ ] Create troubleshooting guides and FAQ
  - [ ] Implement production deployment validation
  - [ ] Add security review and hardening
  - [ ] Create migration and upgrade procedures

#### **3.6.7 Benefits of This Approach**

1. **No Item Dependency**: Works independently of openHAB Items
2. **Hybrid Storage**: Combines real-time metrics with historical storage
3. **Flexible Configuration**: Uses openHAB's StorageService for configuration
4. **Advanced Analytics**: Provides rich querying and aggregation capabilities
5. **Automatic Management**: Includes retention policies and cleanup
6. **Performance Optimized**: Uses MapDB for high-performance time series storage
7. **Scalable**: Can handle large volumes of metrics data
8. **Integration Ready**: Seamlessly integrates with existing MetricsService

#### **3.6.8 Task Tracking Summary**

**Total Tasks Breakdown:**
- **3.6.1 Core Interface**: 20 tasks (4 major categories)
- **3.6.2 MapDB Implementation**: 42 tasks (7 major categories)
- **3.6.3 StorageService Integration**: 42 tasks (7 major categories)
- **3.6.4 MetricsService Integration**: 42 tasks (7 major categories)
- **3.6.5 Configuration Management**: 42 tasks (7 major categories)
- **3.6.6 Implementation Timeline**: 35 tasks (4 weeks, daily breakdown)

**Grand Total: 223 Trackable Tasks**

**Progress Tracking Categories:**
- [ ] **Core Infrastructure** (Week 1): 35 tasks
- [ ] **Advanced Features** (Week 2): 35 tasks
- [ ] **Integration** (Week 3): 35 tasks
- [ ] **Testing & Optimization** (Week 4): 35 tasks
- [ ] **Cross-cutting Concerns**: 83 tasks (distributed across all phases)

**Key Milestones:**
- [ ] **Milestone 1**: Core interfaces and data models complete
- [ ] **Milestone 2**: MapDB storage implementation functional
- [ ] **Milestone 3**: StorageService integration operational
- [ ] **Milestone 4**: MetricsService integration complete
- [ ] **Milestone 5**: Configuration management implemented
- [ ] **Milestone 6**: All testing and optimization complete
- [ ] **Milestone 7**: Production-ready deployment

**Risk Mitigation Tasks:**
- [ ] **Performance Testing**: Load testing with large datasets
- [ ] **Memory Management**: Memory leak detection and optimization
- [ ] **Data Integrity**: Consistency validation across storage layers
- [ ] **Error Recovery**: Failure handling and data recovery procedures
- [ ] **Security Review**: Access control and data protection validation
- [ ] **Documentation**: Comprehensive user and developer documentation

**Quality Assurance Tasks:**
- [ ] **Unit Test Coverage**: Minimum 80% code coverage
- [ ] **Integration Testing**: End-to-end workflow validation
- [ ] **Performance Benchmarking**: Response time and throughput validation
- [ ] **Stress Testing**: High-load scenario testing
- [ ] **Compatibility Testing**: OSGi and openHAB version compatibility
- [ ] **Security Testing**: Vulnerability assessment and penetration testing



```

## Phase 4: Advanced Features Implementation



### 4.2 Snapshot Scheduler Implementation

**Objective**: Implement automatic snapshot scheduling to periodically capture metrics data for historical analysis.

**Current Status**: ❌ **NOT IMPLEMENTED** - No automatic snapshot scheduling

**Tasks**:
- [ ] **Create SnapshotScheduler** - OSGi component for automatic snapshot scheduling
- [ ] **Implement Configurable Intervals** - Support for different snapshot frequencies
- [ ] **Add Adaptive Scheduling** - Adjust frequency based on activity levels
- [ ] **Integrate with MetricsRegistry** - Trigger snapshots for all active collectors
- [ ] **Add Error Handling** - Graceful handling of snapshot failures
- [ ] **Create Monitoring** - Monitor scheduler performance and health
- [ ] **Add Configuration** - Configurable scheduling parameters

**Implementation**:
```java
@Component(service = SnapshotScheduler.class)
@NonNullByDefault
public class SnapshotScheduler {
    
    private static final Logger logger = LoggerFactory.getLogger(SnapshotScheduler.class);
    
    @Reference
    private @Nullable MetricsRegistry metricsRegistry;
    
    @Reference
    private @Nullable MetricsStorage metricsStorage;
    
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private volatile boolean isRunning = false;
    
    @Activate
    public void activate() {
        startScheduler();
    }
    
    @Deactivate
    public void deactivate() {
        stopScheduler();
    }
    
    public void startScheduler() {
        if (!isRunning) {
            isRunning = true;
            // Take snapshots every minute
            scheduler.scheduleAtFixedRate(this::takePeriodicSnapshots, 0, 1, TimeUnit.MINUTES);
            logger.info("Snapshot scheduler started");
        }
    }
    
    public void stopScheduler() {
        if (isRunning) {
            isRunning = false;
            scheduler.shutdown();
            logger.info("Snapshot scheduler stopped");
        }
    }
    
    private void takePeriodicSnapshots() {
        if (metricsRegistry == null || metricsStorage == null) {
            return;
        }
        
        try {
            // Take snapshots for all active collectors
            Collection<MetricKey> keys = metricsRegistry.getAllKeys();
            for (MetricKey key : keys) {
                try {
                    MetricsCollector collector = metricsRegistry.getCollector(key);
                    GenericMetricsSnapshot snapshot = SnapshotFactory.createSnapshot(collector, 
                        GenericMetricsSnapshot.class, key);
                    metricsStorage.addSnapshot(key, snapshot);
                } catch (Exception e) {
                    logger.warn("Failed to take snapshot for key: {}", key, e);
                }
            }
            
            logger.debug("Took snapshots for {} metric keys", keys.size());
        } catch (Exception e) {
            logger.error("Error during periodic snapshot collection", e);
        }
    }
    
    public void takeSnapshotNow(MetricKey key) {
        if (metricsRegistry != null && metricsStorage != null) {
            try {
                MetricsCollector collector = metricsRegistry.getCollector(key);
                GenericMetricsSnapshot snapshot = SnapshotFactory.createSnapshot(collector, 
                    GenericMetricsSnapshot.class, key);
                metricsStorage.addSnapshot(key, snapshot);
                logger.debug("Manual snapshot taken for key: {}", key);
            } catch (Exception e) {
                logger.warn("Failed to take manual snapshot for key: {}", key, e);
            }
        }
    }
    
    public boolean isRunning() {
        return isRunning;
    }
}
```


## Phase 4: Advanced Features (Week 4-5)

### 4.1 Implement Aggregation and Filtering

**Tasks**:
- [ ] Add aggregation methods to MetricsService
- [ ] Implement time-based filtering
- [ ] Add domain-specific aggregation
- [ ] Create aggregation snapshots
- [ ] Add unit tests for aggregation

**Deliverables**:
```java
public interface MetricsService {
    // ... existing methods ...
    
    /**
     * Get aggregated metrics across multiple operations
     */
    <T extends MetricsSnapshot> List<T> getAggregatedSnapshots(String domain, Class<T> snapshotType);
    
    /**
     * Get metrics within time range
     */
    <T extends MetricsSnapshot> List<T> getSnapshotsInRange(String domain, String operation, 
        Class<T> snapshotType, Instant start, Instant end);
    
    /**
     * Get domain-wide aggregated metrics
     */
    DomainAggregatedSnapshot getDomainAggregatedSnapshot(String domain);
}
```

### 4.2 Implement Metrics Export

**Tasks**:
- [ ] Create JSON exporter for REST APIs
- [ ] Add filtering and formatting options
- [ ] Create unit tests for exporters

**Deliverables**:
```java
@Component(service = MetricsExporter.class)
@NonNullByDefault
public interface MetricsExporter {
    
    /**
     * Export metrics to Prometheus format
     */
    String exportPrometheus(MetricsService metricsService);
    
    /**
     * Export metrics to JSON format
     */
    String exportJSON(MetricsService metricsService);
    
    /**
     * Export metrics to JMX
     */
    void exportJMX(MetricsService metricsService);
}
```

### 4.3 Performance Optimization

**Tasks**:
- [ ] Implement metrics caching
- [ ] Add lazy loading for snapshots
- [ ] Optimize memory usage
- [ ] Add performance monitoring

**Deliverables**:
- Cached metrics retrieval
- Memory-efficient snapshot creation
- Performance benchmarks
- Memory usage optimization

### 4.4 Advanced Analytics

**Tasks**:
- [ ] Implement trend analysis
- [ ] Add anomaly detection
- [ ] Create predictive analytics
- [ ] Add correlation analysis

**Deliverables**:
- Trend analysis algorithms
- Anomaly detection patterns
- Predictive models
- Correlation analysis tools

### 4.5 Integration Features

**Tasks**:
- [ ] Add REST API endpoints
- [ ] Implement WebSocket streaming
- [ ] Create dashboard integration
- [ ] Add alerting capabilities

**Deliverables**:
- REST API for metrics access
- Real-time metrics streaming
- Dashboard integration points
- Alerting and notification system

---

## Success Criteria

### Functional Requirements

- [ ] Single centralized MetricsService operational
- [ ] Builder pattern supports flexible data recording
- [ ] Operation-specific snapshots with capability interfaces
- [ ] No duplicate or wrapper methods
- [ ] All existing metrics functionality preserved
- [ ] Aggregation and filtering operational
- [ ] Export functionality (Prometheus, JSON, JMX) working

### Centralized-Only Requirements

- [ ] **NO DIRECT COUNTERS**: All `AtomicLong`, `AtomicInteger`, and direct counter collections eliminated from classes
- [ ] **ALL STATISTICS CENTRALIZED**: Every statistics method sources data from `MetricsService`
- [ ] **NO LOCAL COLLECTION**: No classes maintain their own metric collection logic
- [ ] **NO DOMAIN getStatistics() METHODS**: Domain classes don't provide statistics access methods
- [ ] **CLIENTS CALL METRICSSERVICE DIRECTLY**: All statistics access goes through MetricsService
- [ ] **MIGRATION COMPLETE**: All existing direct collection patterns migrated to centralized approach
- [ ] **ZERO FORBIDDEN PATTERNS**: No classes use the forbidden direct counter patterns

### Performance Requirements

- [ ] No performance regression in metrics collection
- [ ] Builder pattern overhead < 1% of operation time
- [ ] Snapshot creation < 1ms per snapshot
- [ ] Memory usage optimized for high-frequency operations
- [ ] Thread-safe concurrent access

### Quality Requirements

- [ ] 100% test coverage for MetricsService
- [ ] All capability interfaces properly implemented
- [ ] No memory leaks in builder pattern
- [ ] Proper error handling throughout
- [ ] Comprehensive documentation

---

## Risk Mitigation

### Technical Risks

1. **Builder Pattern Complexity**
   - **Risk**: Builder pattern adds complexity
   - **Mitigation**: Keep builder simple, provide convenience methods

2. **Snapshot Type Safety**
   - **Risk**: Type casting issues with generic snapshots
   - **Mitigation**: Use type-specific methods, avoid generic casting

3. **Migration Complexity**
   - **Risk**: Difficult migration from existing metrics
   - **Mitigation**: Gradual migration, parallel implementation

### Integration Risks

1. **OSGi Service Dependencies**
   - **Risk**: Complex service dependency management
   - **Mitigation**: Use proper OSGi annotations, handle service unavailability

2. **Performance Impact**
   - **Risk**: Centralized metrics impact performance
   - **Mitigation**: Optimize hot paths, use efficient data structures

3. **Memory Usage**
   - **Risk**: Increased memory usage with centralized collection
   - **Mitigation**: Implement efficient storage, cleanup old data

---

## Timeline

### Week 4: Advanced Features Implementation
- **Days 1-2**: Implement aggregation and filtering
- **Days 3-4**: Create metrics export functionality
- **Day 5**: Performance optimization and testing

### Week 5: Integration and Testing
- **Days 1-2**: Advanced analytics implementation
- **Days 3-4**: Integration features (REST API, WebSocket)
- **Day 5**: Comprehensive testing and validation

---

## Dependencies

### Internal Dependencies
- MetricsService implementation (Phase 3)
- MonitoringRegistry (Phase 3.5)
- Snapshot classes (Phase 3)

### External Dependencies
- openHAB Storage Service
- Prometheus client libraries
- JSON processing libraries
- WebSocket implementation

---

## Notes

This plan focuses on advanced features that build upon the core metrics centralization achieved in Phase 3. The unified collector architecture and hybrid storage provide the foundation for scalable, high-performance metrics collection and analysis.

The advanced features in Phase 4 extend the capabilities of the centralized metrics system to provide comprehensive monitoring, analytics, and integration capabilities for the openHAB AI bundle.
