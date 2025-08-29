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
- [x] **StorageService Integration Setup**
  - [x] Create StorageServiceTimeSeriesStorage class in `org.openhab.core.ai.common.monitoring.timeseries.storage`
  - [x] Implement OSGi component annotations (@Component, @Activate, @Deactivate)
  - [x] Set up StorageService dependency injection (@Reference)
  - [x] Create storage instances for time series data, metadata, and configuration
  - [x] Implement proper error handling for StorageService unavailability
  - [x] Add storage initialization validation and logging

- [x] **Data Structure Implementation**
  - [x] Create Storage<TimeSeriesPoint> for time series points storage
  - [x] Create Storage<TimeSeriesMetadata> for series metadata storage
  - [x] Create Storage<AggregatedPoint> for aggregated data storage
  - [x] Implement proper serialization for TimeSeriesPoint and AggregatedPoint
  - [x] Add data structure validation and integrity checks
  - [x] Implement thread-safe access patterns using StorageService

- [x] **Core Storage Operations**
  - [x] Implement storeTimeSeriesPoint method with error handling
  - [x] Add automatic metadata updates on data insertion
  - [x] Implement efficient key generation for time series data
  - [x] Add data validation before storage
  - [x] Implement batch operations for performance optimization
  - [x] Add storage operation metrics and monitoring

- [x] **Query Implementation**
  - [x] Implement queryTimeSeries with time range filtering
  - [x] Add sorting and ordering capabilities using StorageService queries
  - [x] Implement result pagination and limiting
  - [x] Add query performance optimization with proper key design
  - [x] Implement query result caching for frequently accessed data
  - [x] Add query execution metrics and logging

- [x] **Aggregation Logic**
  - [x] Implement real-time aggregation calculation
  - [x] Add pre-computed aggregation storage using StorageService
  - [ ] Implement aggregation scheduling and triggers
  - [x] Add support for multiple aggregation functions (AVG, MIN, MAX, SUM, COUNT)
  - [x] Implement aggregation period management
  - [x] Add aggregation result validation and error handling

- [x] **Data Optimization and Management**
  - [x] Implement efficient key design for time series data access
  - [x] Add data deduplication logic using StorageService capabilities
  - [x] Implement efficient storage format for time series data
  - [x] Add memory usage optimization with StorageService
  - [x] Implement lazy loading for large datasets
  - [x] Add storage size monitoring and alerts

- [x] **Retention Policies**
  - [x] Implement automatic data cleanup based on retention period
  - [x] Add configurable retention policies per series
  - [x] Implement cleanup scheduling and execution
  - [x] Add cleanup operation logging and metrics
  - [x] Implement graceful cleanup with system resource management
  - [x] Add cleanup operation rollback capabilities

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
        if (timeSeriesStorage == null) {
            return List.of();
        }
        
        try {
            long startMillis = startTime.toEpochMilli();
            long endMillis = endTime.toEpochMilli();
            
            return timeSeriesStorage.stream()
                .filter(entry -> {
                    String key = entry.getKey();
                    if (key.startsWith(seriesId + ":")) {
                        try {
                            long timestamp = Long.parseLong(key.substring(seriesId.length() + 1));
                            return timestamp >= startMillis && timestamp <= endMillis;
                        } catch (NumberFormatException e) {
                            return false;
                        }
                    }
                    return false;
                })
                .map(entry -> {
                    Map<String, Object> data = entry.getValue();
                    Instant timestamp = Instant.ofEpochMilli((Long) data.get("timestamp"));
                    @SuppressWarnings("unchecked")
                    Map<String, String> tags = (Map<String, String>) data.get("tags");
                    @SuppressWarnings("unchecked")
                    Map<String, Object> fields = (Map<String, Object>) data.get("fields");
                    return new TimeSeriesPoint(timestamp, tags, fields);
                })
                .sorted(Comparator.comparing(TimeSeriesPoint::timestamp))
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            logger.error("Failed to query time series for series: {}", seriesId, e);
            return List.of();
        }
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

##### **3.6.3 Enhanced StorageService Features and Optimization**

**Tasks:**
- [ ] **Advanced StorageService Features**
  - [ ] Implement advanced key design patterns for time series data
  - [ ] Add composite key support for complex queries
  - [ ] Create storage partitioning strategies for large datasets
  - [ ] Implement storage sharding for performance optimization
  - [ ] Add storage compression and optimization techniques
  - [ ] Create storage indexing strategies for fast queries

- [ ] **Enhanced Query Capabilities**
  - [ ] Implement advanced filtering and search capabilities
  - [ ] Add complex query builder with fluent API
  - [ ] Create query optimization and execution planning
  - [ ] Implement query result streaming for large datasets
  - [ ] Add query result caching with intelligent invalidation
  - [ ] Create query performance monitoring and profiling

- [ ] **Data Lifecycle Management**
  - [ ] Implement intelligent data archiving strategies
  - [ ] Add data tiering (hot, warm, cold storage)
  - [ ] Create data migration and rebalancing tools
  - [ ] Implement data lifecycle automation
  - [ ] Add data lifecycle monitoring and reporting
  - [ ] Create data lifecycle policy management

- [ ] **Advanced Aggregation Features**
  - [ ] Implement real-time streaming aggregations
  - [ ] Add windowed aggregations with sliding windows
  - [ ] Create custom aggregation functions
  - [ ] Implement aggregation result materialization
  - [ ] Add aggregation result caching and optimization
  - [ ] Create aggregation monitoring and alerting

- [ ] **Storage Optimization and Tuning**
  - [ ] Implement storage performance profiling
  - [ ] Add storage capacity planning and forecasting
  - [ ] Create storage optimization recommendations
  - [ ] Implement storage tuning and configuration management
  - [ ] Add storage performance benchmarking
  - [ ] Create storage optimization automation

- [ ] **Advanced Backup and Recovery**
  - [ ] Implement incremental backup strategies
  - [ ] Add point-in-time recovery capabilities
  - [ ] Create backup verification and testing
  - [ ] Implement backup encryption and security
  - [ ] Add backup scheduling and automation
  - [ ] Create disaster recovery testing and validation

- [ ] **Monitoring and Analytics**
  - [ ] Implement comprehensive storage monitoring
  - [ ] Add storage analytics and insights
  - [ ] Create storage performance dashboards
  - [ ] Implement storage anomaly detection
  - [ ] Add storage capacity forecasting
  - [ ] Create storage health scoring and alerting

**Deliverables:**
```java
@Component(service = MetricTimeSeriesStorage.class)
@NonNullByDefault
public class EnhancedStorageServiceTimeSeriesStorage implements MetricTimeSeriesStorage {
    
    private static final Logger logger = LoggerFactory.getLogger(EnhancedStorageServiceTimeSeriesStorage.class);
    
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
- [x] **MetricsService Enhancement**
  - [x] Modify existing DefaultMetricsService class to add time series capabilities
  - [x] Add MetricTimeSeriesStorage dependency injection (@Reference)
  - [x] Implement dual recording (real-time + time series) in existing methods
  - [x] Add time series recording error handling and fallback
  - [ ] Implement time series recording performance optimization
  - [ ] Add time series recording configuration and toggles

- [x] **Automatic Time Series Recording**
  - [x] Modify existing recordOperation() method to add time series storage
  - [x] Modify existing recordOperationWithData() method to add time series storage
  - [x] Add intelligent data transformation (metrics → time series format)
  - [ ] Implement batch time series recording for performance
  - [x] Add time series recording error handling and fallback
  - [ ] Add time series recording configuration and toggles

- [x] **Historical Metrics Retrieval**
  - [x] Implement getHistoricalMetrics method with time range support
  - [x] Add getAggregatedMetrics method with aggregation functions
  - [x] Create getMetricsAnalytics method with statistical analysis
  - [ ] Implement metrics trend analysis and pattern detection
  - [ ] Add metrics comparison and benchmarking capabilities
  - [ ] Create metrics forecasting and prediction methods

- [x] **Analytics and Reporting Features**
  - [x] Implement statistical analysis (mean, median, percentiles)
  - [ ] Add trend analysis and change detection
  - [ ] Create performance analytics and bottleneck identification
  - [ ] Implement anomaly detection and alerting
  - [ ] Add comparative analysis and benchmarking
  - [ ] Create custom analytics and reporting framework

- [x] **Data Transformation and Mapping**
  - [x] Implement metrics to time series data mapping
  - [x] Add context data transformation and normalization
  - [x] Create tag generation and management
  - [x] Implement field extraction and validation
  - [x] Add data type conversion and optimization
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

##### **3.6.5 Snapshot Storage with Minimal Modification**

**Objective**: Enable time series storage to handle any MetricsSnapshot type without requiring conversion to GenericMetricsSnapshot, preserving original snapshot types and capabilities.

**Tasks:**
- [x] **Extend TimeSeriesPoint for Snapshot Storage**
  - [x] Add snapshotType and snapshotData fields to TimeSeriesPoint record
  - [x] Update TimeSeriesPoint serialization/deserialization methods
  - [x] Add snapshot type validation and error handling
  - [x] Create backward compatibility for existing TimeSeriesPoint usage
  - [x] Add snapshot metadata preservation (capabilities, labels, etc.)

- [x] **Generic Snapshot Serialization Framework**
  - [x] Create SnapshotSerializer interface for type-safe serialization
  - [x] Implement JSON-based serialization for MetricsSnapshot objects
  - [x] Add snapshot type registry for deserialization
  - [x] Create snapshot validation and integrity checking
  - [x] Add snapshot versioning for future compatibility
  - [ ] Implement snapshot compression for storage optimization

- [x] **Capability-Aware Storage Methods**
  - [x] Create storeSnapshot() method that accepts any MetricsSnapshot
  - [x] Implement capability-based snapshot filtering and querying
  - [x] Add snapshot type-specific storage optimization
  - [x] Create snapshot capability validation during storage
  - [x] Add snapshot metadata indexing for efficient retrieval
  - [ ] Implement snapshot lifecycle management

- [x] **Enhanced Query Interface**
  - [x] Add queryBySnapshotType() method for type-specific queries
  - [x] Implement queryByCapabilities() for capability-based filtering
  - [x] Create snapshot type-aware deserialization
  - [x] Add snapshot metadata querying capabilities
  - [ ] Implement snapshot relationship tracking
  - [ ] Create snapshot aggregation by type and capabilities

**Deliverables:**
```java
@NonNullByDefault
public record TimeSeriesPoint(
    Instant timestamp,
    Map<String, String> tags,
    Map<String, Object> fields,
    @Nullable String snapshotType,  // NEW: Type of snapshot stored
    @Nullable String snapshotData   // NEW: Serialized snapshot data
) {}

@NonNullByDefault
public interface SnapshotSerializer {
    /**
     * Serialize any MetricsSnapshot to storage format
     */
    String serialize(MetricsSnapshot snapshot);
    
    /**
     * Deserialize snapshot data back to original type
     */
    <T extends MetricsSnapshot> T deserialize(String snapshotData, String snapshotType, Class<T> targetType);
    
    /**
     * Get supported snapshot types
     */
    Set<String> getSupportedTypes();
}

@Component(service = SnapshotSerializer.class)
@NonNullByDefault
public class JsonSnapshotSerializer implements SnapshotSerializer {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, Class<? extends MetricsSnapshot>> typeRegistry = new ConcurrentHashMap<>();
    
    @Override
    public String serialize(MetricsSnapshot snapshot) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("type", snapshot.getClass().getName());
            data.put("timestamp", snapshot.timestampMs());
            data.put("data", extractSnapshotData(snapshot));
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize snapshot", e);
        }
    }
    
    @Override
    public <T extends MetricsSnapshot> T deserialize(String snapshotData, String snapshotType, Class<T> targetType) {
        try {
            Map<String, Object> data = objectMapper.readValue(snapshotData, Map.class);
            return reconstructSnapshot(data, targetType);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize snapshot", e);
        }
    }
    
    private Map<String, Object> extractSnapshotData(MetricsSnapshot snapshot) {
        // Extract all data from snapshot using reflection or interface methods
        Map<String, Object> data = new HashMap<>();
        
        // Add common snapshot data
        data.put("domain", snapshot.getDomain());
        data.put("operation", snapshot.getOperation());
        data.put("timestamp", snapshot.timestampMs());
        
        // Add type-specific data based on capabilities
        if (snapshot instanceof CountsMetrics counts) {
            data.put("total", counts.total());
            data.put("success", counts.success());
            data.put("failure", counts.failure());
        }
        
        if (snapshot instanceof LatencyMetrics latency) {
            data.put("totalDurationNanos", latency.totalDurationNanos());
            data.put("avgDurationMs", latency.avgDurationMs());
        }
        
        // Add any additional data from the snapshot
        // This would be implemented based on the specific snapshot types
        
        return data;
    }
    
    private <T extends MetricsSnapshot> T reconstructSnapshot(Map<String, Object> data, Class<T> targetType) {
        // Reconstruct snapshot from serialized data
        // Implementation depends on specific snapshot constructors
        // This would use reflection or builder patterns to recreate the snapshot
        throw new UnsupportedOperationException("Snapshot reconstruction not yet implemented");
    }
}

@Component(service = MetricTimeSeriesStorage.class)
@NonNullByDefault
public class SnapshotAwareTimeSeriesStorage implements MetricTimeSeriesStorage {
    
    @Reference
    private @Nullable SnapshotSerializer snapshotSerializer;
    
    /**
     * Store any MetricsSnapshot directly without conversion
     */
    public void storeSnapshot(MetricsSnapshot snapshot, MetricKey key) {
        try {
            String seriesId = createSeriesId(key);
            String snapshotType = snapshot.getClass().getName();
            String snapshotData = snapshotSerializer.serialize(snapshot);
            
            Map<String, String> tags = new HashMap<>(key.labels());
            tags.put("snapshotType", snapshotType);
            tags.put("capabilities", String.join(",", key.capabilities()));
            
            Map<String, Object> fields = Map.of("snapshotData", snapshotData);
            
            storeTimeSeriesPoint(seriesId, Instant.now(), tags, fields, snapshotType, snapshotData);
            
        } catch (Exception e) {
            logger.error("Failed to store snapshot: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Query snapshots by type
     */
    public <T extends MetricsSnapshot> List<T> queryBySnapshotType(
            String seriesId, Class<T> snapshotType, Instant startTime, Instant endTime) {
        
        List<TimeSeriesPoint> points = queryTimeSeries(seriesId, startTime, endTime);
        
        return points.stream()
            .filter(point -> snapshotType.getName().equals(point.snapshotType()))
            .filter(point -> point.snapshotData() != null)
            .map(point -> snapshotSerializer.deserialize(point.snapshotData(), point.snapshotType(), snapshotType))
            .collect(Collectors.toList());
    }
    
    /**
     * Query snapshots by capabilities
     */
    public <T extends MetricsSnapshot> List<T> queryByCapabilities(
            Set<String> requiredCapabilities, Class<T> snapshotType, Duration timeRange) {
        
        // Use capability index for efficient filtering
        Set<String> matchingSeries = getSeriesByCapabilities(requiredCapabilities);
        
        List<T> results = new ArrayList<>();
        for (String seriesId : matchingSeries) {
            List<T> snapshots = queryBySnapshotType(seriesId, snapshotType, 
                Instant.now().minus(timeRange), Instant.now());
            results.addAll(snapshots);
        }
        
        return results;
    }
}
```

##### **3.6.6 Fix Statistics Integration**

**Objective**: Fix the broken statistics system by connecting it to time series data instead of empty MetricsRegistry data, enabling proper historical analysis and statistics generation.

**Current Problem**: The `getStatistics` method calls `getAllSnapshots(MetricsSnapshot.class)` which returns an empty list, making statistics non-functional.

**Tasks:**
- [x] **Fix getStatistics Method Implementation**
  - [x] Replace empty getAllSnapshots() calls with time series data queries
  - [x] Implement time series to snapshot conversion logic
  - [x] Add proper error handling for time series queries
  - [x] Create fallback mechanisms when time series storage is unavailable
  - [x] Add performance optimization for large time series queries
  - [ ] Implement query result caching for statistics

- [x] **Time Series to Snapshot Conversion**
  - [x] Create TimeSeriesToSnapshotConverter utility class
  - [x] Implement conversion from TimeSeriesPoint to MetricsSnapshot
  - [x] Add snapshot type detection and reconstruction
  - [ ] Create batch conversion for performance optimization
  - [x] Add conversion validation and error handling
  - [ ] Implement conversion caching to avoid repeated work

- [x] **Enhanced Statistics Query Interface**
  - [x] Add time range validation for statistics queries
  - [x] Implement statistics query optimization
  - [ ] Create statistics query result caching
  - [ ] Add statistics query performance monitoring
  - [x] Implement statistics query error recovery
  - [ ] Create statistics query result pagination

- [ ] **Statistics Performance Optimization**
  - [ ] Implement asynchronous statistics calculation
  - [ ] Add statistics calculation caching
  - [ ] Create statistics pre-computation for common queries
  - [ ] Implement statistics calculation batching
  - [ ] Add statistics calculation resource management
  - [ ] Create statistics calculation monitoring

- [ ] **Statistics Integration Testing**
  - [ ] Create comprehensive statistics integration tests
  - [ ] Add statistics performance benchmarking
  - [ ] Implement statistics data consistency validation
  - [ ] Add statistics error handling testing
  - [ ] Create statistics load testing
  - [ ] Add statistics end-to-end workflow testing

**Deliverables:**
```java
@Component(service = MetricsService.class)
@NonNullByDefault
public class DefaultMetricsService implements MetricsService {
    
    @Reference
    private @Nullable MetricTimeSeriesStorage timeSeriesStorage;
    
    @Reference
    private @Nullable SnapshotSerializer snapshotSerializer;
    
    // FIXED: Statistics now use time series data instead of empty lists
    @Override
    public <T extends StatisticsSnapshot> T getStatistics(
            MetricKey key, Class<T> statisticsType, Duration timeRange) {
        
        try {
            // Get historical data from time series storage
            Instant endTime = Instant.now();
            Instant startTime = endTime.minus(timeRange);
            
            String seriesId = createSeriesId(key);
            List<TimeSeriesPoint> points = timeSeriesStorage.queryTimeSeries(seriesId, startTime, endTime);
            
            // Convert time series points back to MetricsSnapshot objects
            List<MetricsSnapshot> snapshots = convertTimeSeriesToSnapshots(points, key);
            
            return StatisticsFactory.createStatistics(snapshots, statisticsType, key, timeRange);
            
        } catch (Exception e) {
            logger.error("Failed to get statistics for key: {}", key.id(), e);
            return StatisticsFactory.createEmptyStatistics(statisticsType, key, timeRange);
        }
    }
    
    @Override
    public <T extends StatisticsSnapshot> List<T> getStatisticsByCapability(
            Class<T> capabilityType, Duration timeRange) {
        
        List<T> statistics = new ArrayList<>();
        
        try {
            // Get all available series
            Set<String> availableSeries = timeSeriesStorage.getAvailableSeries();
            
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
    public <T extends StatisticsSnapshot> List<T> getStatisticsByDomain(
            String domain, Class<T> statisticsType, Duration timeRange) {
        
        List<T> statistics = new ArrayList<>();
        
        try {
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
    
    /**
     * Convert time series points to MetricsSnapshot objects
     */
    private List<MetricsSnapshot> convertTimeSeriesToSnapshots(List<TimeSeriesPoint> points, MetricKey key) {
        return points.stream()
            .filter(point -> point.snapshotData() != null)
            .map(point -> {
                try {
                    return snapshotSerializer.deserialize(
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
        Instant endTime = Instant.now();
        Instant startTime = endTime.minus(timeRange);
        
        Set<String> allSeries = timeSeriesStorage.getAvailableSeries();
        List<TimeSeriesPoint> matchingPoints = new ArrayList<>();
        
        for (String seriesId : allSeries) {
            List<TimeSeriesPoint> points = timeSeriesStorage.queryTimeSeries(seriesId, startTime, endTime);
            
            List<TimeSeriesPoint> filteredPoints = points.stream()
                .filter(point -> tagValue.equals(point.tags().get(tagKey)))
                .collect(Collectors.toList());
            
            matchingPoints.addAll(filteredPoints);
        }
        
        return matchingPoints;
    }
    
    /**
     * Create series ID from MetricKey
     */
    private String createSeriesId(MetricKey key) {
        return "metrics:" + key.labels().get("domain") + ":" + key.labels().get("operation");
    }
    
    /**
     * Extract MetricKey from series ID
     */
    private MetricKey extractMetricKeyFromSeriesId(String seriesId) {
        // Parse seriesId format: "metrics:domain:operation"
        String[] parts = seriesId.split(":");
        if (parts.length >= 3) {
            String domain = parts[1];
            String operation = parts[2];
            return new MetricKeys.SimpleMetricKey(
                domain + "." + operation,
                Map.of("domain", domain, "operation", operation),
                Set.of("counts", "latency") // Default capabilities
            );
        }
        throw new IllegalArgumentException("Invalid series ID format: " + seriesId);
    }
    
    /**
     * Extract series ID from time series point
     */
    private String extractSeriesIdFromPoint(TimeSeriesPoint point) {
        // This would be implemented based on how series ID is stored in the point
        // For now, reconstruct from tags
        String domain = point.tags().get("domain");
        String operation = point.tags().get("operation");
        return "metrics:" + domain + ":" + operation;
    }
}
```

**Deliverables:**
```java
@Component(service = MetricsService.class)
@NonNullByDefault
public class DefaultMetricsService implements MetricsService {
    
    private static final Logger logger = LoggerFactory.getLogger(DefaultMetricsService.class);
    
    // Existing reference to monitoring registry
    @Reference
    private @Nullable MetricsRegistry monitoringRegistry;
    
    // New reference to time series storage
    @Reference
    private @Nullable MetricTimeSeriesStorage timeSeriesStorage;
    
    // Existing metrics collection (unchanged)
    private final Map<MetricKey, MetricsCollector> collectors = new ConcurrentHashMap<>();
    
    @Override
    public void recordOperation(String domain, String operation, boolean success, Duration duration) {
        // Existing implementation - record to centralized metrics
        try {
            MetricsRegistry registry = monitoringRegistry;
            if (registry != null) {
                MetricKey metricKey = new MetricKeys.SimpleMetricKey(domain + "." + operation,
                        Map.of("domain", domain, "operation", operation), Set.of("counts", "latency"));
                MetricsCollector collector = registry.getCollector(metricKey);
                collector.recordExecution(success, duration.toNanos());
                logger.debug("Recorded operation in unified collector: {} (success={}, duration={}ns)", 
                        metricKey.id(), success, duration.toNanos());
            }
        } catch (Exception e) {
            logger.warn("Failed to record operation: {}:{}", domain, operation, e);
        }
        
        // NEW: Also store as time series for historical analysis
        if (timeSeriesStorage != null) {
            storeAsTimeSeries(domain, operation, success, duration, Map.of());
        }
    }
    
    // Note: The existing recordOperation and recordOperationWithData methods 
    // will be modified to add time series storage alongside existing functionality
    
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

##### **3.6.7 Multi-Layer Caching Architecture**

**Objective**: Implement comprehensive caching solutions to address current StorageService performance bottlenecks and provide efficient MetricKey handling strategies.

**Current Performance Issues:**
- Linear scan queries with O(n) complexity
- No indexing for efficient range queries
- High storage overhead per point
- No in-memory caching of frequently accessed data

**Tasks:**
- [ ] **L1 Cache - Hot Data Cache (In-Memory)**
  - [ ] Implement Caffeine-based recent data cache (last 15 minutes)
  - [ ] Add metadata cache for series information
  - [ ] Create snapshot cache for frequently accessed snapshots
  - [ ] Implement cache statistics and monitoring
  - [ ] Add cache eviction policies and memory management
  - [ ] Create cache health monitoring and alerts

- [ ] **L2 Cache - Aggregated Data Cache**
  - [ ] Implement pre-computed aggregations cache
  - [ ] Add statistics cache for common queries
  - [ ] Create query result cache with intelligent invalidation
  - [ ] Implement cache warming strategies
  - [ ] Add cache hit/miss ratio monitoring
  - [ ] Create cache performance optimization

- [ ] **L3 Cache - Persistent Cache (StorageService)**
  - [ ] Add proper indexing to StorageService for efficient range queries
  - [ ] Implement time-based index (timestamp -> series IDs)
  - [ ] Create series-based index (seriesId -> timestamps)
  - [ ] Add domain-based index (domain -> series IDs)
  - [ ] Implement capability-based index for snapshot filtering
  - [ ] Create index maintenance and optimization

- [x] **MetricKey Handling Strategies**
  - [x] Implement hierarchical key structure for efficient querying
  - [x] Create capability-aware storage with metadata preservation
  - [x] Add MetricKey to storage key conversion utilities
  - [x] Implement index key generation for efficient filtering
  - [x] Create MetricKey validation and normalization
  - [x] Add MetricKey relationship tracking

- [x] **Hierarchical Key Structure Implementation**
  - [x] Create MetricKeyStorageHandler class with hierarchical key generation
  - [x] Implement domain/operation/timestamp key structure (e.g., "model/completion/1703123456789")
  - [x] Add fallback to kind-based keys for non-standard MetricKeys
  - [x] Create sub-domain and sub-operation hierarchical indexes
  - [x] Implement capability combination indexes for multi-capability queries
  - [x] Add time-based hierarchical indexes (minute/hour/day buckets)
  - [x] Create label-based hierarchical indexes for common labels
  - [x] Implement key validation and normalization utilities
  - [x] Add key relationship tracking and dependency management
  - [ ] Create key migration utilities for existing data

- [ ] **Advanced Caching Strategies**
  - [ ] Implement predictive caching based on access patterns
  - [ ] Add smart cache eviction with multiple factors
  - [ ] Create cache preloading for anticipated queries
  - [ ] Implement cache compression and optimization
  - [ ] Add cache clustering and distributed caching
  - [ ] Create cache monitoring and analytics

**Deliverables:**

##### **Hierarchical Key Structure Implementation**

```java
@Component
@NonNullByDefault
public class MetricKeyStorageHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(MetricKeyStorageHandler.class);
    
    // Common indexable labels to avoid index explosion
    private static final Set<String> INDEXABLE_LABELS = Set.of(
        "domain", "operation", "provider", "model", "status", "type", "category"
    );
    
    /**
     * Convert MetricKey to optimized hierarchical storage key
     */
    public String createStorageKey(MetricKey key, Instant timestamp) {
        // Extract components from MetricKey
        String domain = key.labels().get("domain");
        String operation = key.labels().get("operation");
        String kind = key.kind();
        
        if (domain != null && operation != null) {
            // Hierarchical key: domain/operation/timestamp
            // Example: "model/completion/1703123456789"
            return String.format("%s/%s/%d", domain, operation, timestamp.toEpochMilli());
        } else {
            // Fallback to kind-based key
            // Example: "execution/abc123/1703123456789"
            return String.format("%s/%s/%d", kind, key.id().hashCode(), timestamp.toEpochMilli());
        }
    }
    
    /**
     * Create multiple index keys for efficient querying
     */
    public Set<String> createIndexKeys(MetricKey key, Instant timestamp) {
        Set<String> indexKeys = new HashSet<>();
        
        // Time-based hierarchical indexes (multiple granularities)
        long minuteBucket = timestamp.toEpochMilli() / (60 * 1000);
        long hourBucket = timestamp.toEpochMilli() / (60 * 60 * 1000);
        long dayBucket = timestamp.toEpochMilli() / (24 * 60 * 60 * 1000);
        
        indexKeys.add("time:minute:" + minuteBucket);
        indexKeys.add("time:hour:" + hourBucket);
        indexKeys.add("time:day:" + dayBucket);
        
        // Domain hierarchy index
        String domain = key.labels().get("domain");
        if (domain != null) {
            indexKeys.add("domain:" + domain);
            
            // Sub-domain hierarchical indexes
            String[] domainParts = domain.split("\\.");
            for (int i = 0; i < domainParts.length; i++) {
                String subDomain = String.join(".", Arrays.copyOfRange(domainParts, 0, i + 1));
                indexKeys.add("subdomain:" + subDomain);
            }
        }
        
        // Operation hierarchy index
        String operation = key.labels().get("operation");
        if (operation != null) {
            indexKeys.add("operation:" + operation);
            
            // Sub-operation hierarchical indexes
            String[] operationParts = operation.split("\\.");
            for (int i = 0; i < operationParts.length; i++) {
                String subOperation = String.join(".", Arrays.copyOfRange(operationParts, 0, i + 1));
                indexKeys.add("suboperation:" + subOperation);
            }
        }
        
        // Kind-based index
        indexKeys.add("kind:" + key.kind());
        
        // Capability-based indexes
        for (String capability : key.capabilities()) {
            indexKeys.add("capability:" + capability);
        }
        
        // Capability combination indexes for multi-capability queries
        Set<String> capabilities = key.capabilities();
        if (capabilities.size() > 1) {
            List<String> capabilityList = new ArrayList<>(capabilities);
            for (int i = 0; i < capabilityList.size(); i++) {
                for (int j = i + 1; j < capabilityList.size(); j++) {
                    String combination = capabilityList.get(i) + "+" + capabilityList.get(j);
                    indexKeys.add("capability-combo:" + combination);
                }
            }
        }
        
        // Label-based indexes for common labels only
        for (Map.Entry<String, String> entry : key.labels().entrySet()) {
            String labelKey = entry.getKey();
            String labelValue = entry.getValue();
            
            if (INDEXABLE_LABELS.contains(labelKey)) {
                indexKeys.add("label:" + labelKey + ":" + labelValue);
            }
        }
        
        return indexKeys;
    }
    
    /**
     * Validate and normalize MetricKey for consistent storage
     */
    public MetricKey validateAndNormalize(MetricKey key) {
        Map<String, String> normalizedLabels = new HashMap<>();
        
        // Normalize labels
        for (Map.Entry<String, String> entry : key.labels().entrySet()) {
            String normalizedKey = normalizeLabelKey(entry.getKey());
            String normalizedValue = normalizeLabelValue(entry.getValue());
            normalizedLabels.put(normalizedKey, normalizedValue);
        }
        
        // Normalize capabilities
        Set<String> normalizedCapabilities = key.capabilities().stream()
            .map(this::normalizeCapability)
            .collect(Collectors.toSet());
        
        return new MetricKeys.SimpleMetricKey(
            normalizeKind(key.kind()),
            normalizedLabels,
            normalizedCapabilities
        );
    }
    
    private String normalizeLabelKey(String key) {
        return key.toLowerCase().replaceAll("[^a-z0-9]", "-");
    }
    
    private String normalizeLabelValue(String value) {
        return value.toLowerCase().replaceAll("[^a-z0-9]", "-");
    }
    
    private String normalizeCapability(String capability) {
        return capability.toLowerCase().replaceAll("[^a-z0-9]", "-");
    }
    
    private String normalizeKind(String kind) {
        return kind.toLowerCase().replaceAll("[^a-z0-9]", "-");
    }
    
    /**
     * Extract MetricKey components from hierarchical storage key
     */
    public MetricKey extractMetricKeyFromStorageKey(String storageKey) {
        String[] parts = storageKey.split("/");
        if (parts.length >= 3) {
            String domain = parts[0];
            String operation = parts[1];
            // Timestamp is parts[2], but we don't need it for MetricKey reconstruction
            
            return new MetricKeys.SimpleMetricKey(
                "execution",
                Map.of("domain", domain, "operation", operation),
                Set.of("counts", "latency") // Default capabilities
            );
        }
        
        throw new IllegalArgumentException("Invalid hierarchical storage key format: " + storageKey);
    }
    
    /**
     * Create series ID from MetricKey for time series storage
     */
    public String createSeriesId(MetricKey key) {
        String domain = key.labels().get("domain");
        String operation = key.labels().get("operation");
        
        if (domain != null && operation != null) {
            return "metrics:" + domain + ":" + operation;
        } else {
            return "metrics:" + key.kind() + ":" + key.id().hashCode();
        }
    }
    
    /**
     * Query using index keys for efficient retrieval
     */
    public Set<String> queryByIndexKeys(Set<String> indexKeys) {
        Set<String> resultSeries = new HashSet<>();
        
        for (String indexKey : indexKeys) {
            Set<String> indexSeries = getIndexSeries(indexKey);
            if (indexSeries != null) {
                if (resultSeries.isEmpty()) {
                    resultSeries.addAll(indexSeries);
                } else {
                    resultSeries.retainAll(indexSeries);
                }
            }
        }
        
        return resultSeries;
    }
    
    private Set<String> getIndexSeries(String indexKey) {
        // Implementation would depend on the specific index storage mechanism
        // This would query the appropriate index (time, domain, capability, etc.)
        return Collections.emptySet();
    }
}

@Component
@NonNullByDefault
public class HierarchicalIndexManager {
    
    // Hierarchical index storage
    private final Map<String, Set<String>> timeIndex = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> domainIndex = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> operationIndex = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> capabilityIndex = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> labelIndex = new ConcurrentHashMap<>();
    
    /**
     * Update hierarchical indexes when storing new data
     */
    public void updateIndexes(String seriesId, MetricKey key, Instant timestamp) {
        // Update time-based indexes
        updateTimeIndexes(seriesId, timestamp);
        
        // Update domain hierarchy indexes
        updateDomainIndexes(seriesId, key);
        
        // Update operation hierarchy indexes
        updateOperationIndexes(seriesId, key);
        
        // Update capability indexes
        updateCapabilityIndexes(seriesId, key);
        
        // Update label indexes
        updateLabelIndexes(seriesId, key);
    }
    
    private void updateTimeIndexes(String seriesId, Instant timestamp) {
        long minuteBucket = timestamp.toEpochMilli() / (60 * 1000);
        long hourBucket = timestamp.toEpochMilli() / (60 * 60 * 1000);
        long dayBucket = timestamp.toEpochMilli() / (24 * 60 * 60 * 1000);
        
        timeIndex.computeIfAbsent("minute:" + minuteBucket, k -> new HashSet<>()).add(seriesId);
        timeIndex.computeIfAbsent("hour:" + hourBucket, k -> new HashSet<>()).add(seriesId);
        timeIndex.computeIfAbsent("day:" + dayBucket, k -> new HashSet<>()).add(seriesId);
    }
    
    private void updateDomainIndexes(String seriesId, MetricKey key) {
        String domain = key.labels().get("domain");
        if (domain != null) {
            // Add full domain
            domainIndex.computeIfAbsent("domain:" + domain, k -> new HashSet<>()).add(seriesId);
            
            // Add sub-domain hierarchy
            String[] domainParts = domain.split("\\.");
            for (int i = 0; i < domainParts.length; i++) {
                String subDomain = String.join(".", Arrays.copyOfRange(domainParts, 0, i + 1));
                domainIndex.computeIfAbsent("subdomain:" + subDomain, k -> new HashSet<>()).add(seriesId);
            }
        }
    }
    
    private void updateOperationIndexes(String seriesId, MetricKey key) {
        String operation = key.labels().get("operation");
        if (operation != null) {
            // Add full operation
            operationIndex.computeIfAbsent("operation:" + operation, k -> new HashSet<>()).add(seriesId);
            
            // Add sub-operation hierarchy
            String[] operationParts = operation.split("\\.");
            for (int i = 0; i < operationParts.length; i++) {
                String subOperation = String.join(".", Arrays.copyOfRange(operationParts, 0, i + 1));
                operationIndex.computeIfAbsent("suboperation:" + subOperation, k -> new HashSet<>()).add(seriesId);
            }
        }
    }
    
    private void updateCapabilityIndexes(String seriesId, MetricKey key) {
        // Add individual capability indexes
        for (String capability : key.capabilities()) {
            capabilityIndex.computeIfAbsent("capability:" + capability, k -> new HashSet<>()).add(seriesId);
        }
        
        // Add capability combination indexes
        Set<String> capabilities = key.capabilities();
        if (capabilities.size() > 1) {
            List<String> capabilityList = new ArrayList<>(capabilities);
            for (int i = 0; i < capabilityList.size(); i++) {
                for (int j = i + 1; j < capabilityList.size(); j++) {
                    String combination = capabilityList.get(i) + "+" + capabilityList.get(j);
                    capabilityIndex.computeIfAbsent("capability-combo:" + combination, k -> new HashSet<>()).add(seriesId);
                }
            }
        }
    }
    
    private void updateLabelIndexes(String seriesId, MetricKey key) {
        for (Map.Entry<String, String> entry : key.labels().entrySet()) {
            String labelKey = entry.getKey();
            String labelValue = entry.getValue();
            
            // Only index common labels to avoid index explosion
            if (MetricKeyStorageHandler.INDEXABLE_LABELS.contains(labelKey)) {
                String indexKey = "label:" + labelKey + ":" + labelValue;
                labelIndex.computeIfAbsent(indexKey, k -> new HashSet<>()).add(seriesId);
            }
        }
    }
    
    /**
     * Query series by hierarchical criteria
     */
    public Set<String> queryByHierarchy(String domain, String operation, Set<String> capabilities, 
                                       Instant startTime, Instant endTime) {
        Set<String> resultSeries = new HashSet<>();
        
        // Start with domain-based filtering
        if (domain != null) {
            Set<String> domainSeries = domainIndex.get("domain:" + domain);
            if (domainSeries != null) {
                resultSeries.addAll(domainSeries);
            }
        }
        
        // Filter by operation
        if (operation != null && !resultSeries.isEmpty()) {
            Set<String> operationSeries = operationIndex.get("operation:" + operation);
            if (operationSeries != null) {
                resultSeries.retainAll(operationSeries);
            }
        }
        
        // Filter by capabilities
        if (capabilities != null && !capabilities.isEmpty() && !resultSeries.isEmpty()) {
            for (String capability : capabilities) {
                Set<String> capabilitySeries = capabilityIndex.get("capability:" + capability);
                if (capabilitySeries != null) {
                    resultSeries.retainAll(capabilitySeries);
                }
            }
        }
        
        // Filter by time range
        if (startTime != null && endTime != null && !resultSeries.isEmpty()) {
            Set<String> timeSeries = queryByTimeRange(startTime, endTime);
            resultSeries.retainAll(timeSeries);
        }
        
        return resultSeries;
    }
    
    private Set<String> queryByTimeRange(Instant startTime, Instant endTime) {
        Set<String> timeSeries = new HashSet<>();
        
        long startMinute = startTime.toEpochMilli() / (60 * 1000);
        long endMinute = endTime.toEpochMilli() / (60 * 1000);
        
        for (long minute = startMinute; minute <= endMinute; minute++) {
            Set<String> minuteSeries = timeIndex.get("minute:" + minute);
            if (minuteSeries != null) {
                timeSeries.addAll(minuteSeries);
            }
        }
        
        return timeSeries;
    }
}
```

##### **Multi-Layer Caching Implementation**

```java
@Component
@NonNullByDefault
public class TimeSeriesL1Cache {
    
    // Recent data cache (last 15 minutes)
    private final Cache<String, List<TimeSeriesPoint>> recentDataCache;
    
    // Metadata cache
    private final Cache<String, TimeSeriesMetadata> metadataCache;
    
    // Snapshot cache for frequently accessed snapshots
    private final Cache<String, MetricsSnapshot> snapshotCache;
    
    public TimeSeriesL1Cache() {
        this.recentDataCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(15, TimeUnit.MINUTES)
            .recordStats()
            .build();
            
        this.metadataCache = Caffeine.newBuilder()
            .maximumSize(500)
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build();
            
        this.snapshotCache = Caffeine.newBuilder()
            .maximumSize(2000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();
    }
    
    public List<TimeSeriesPoint> getRecentData(String seriesId, Instant startTime, Instant endTime) {
        String cacheKey = seriesId + ":" + startTime.toEpochMilli() + ":" + endTime.toEpochMilli();
        return recentDataCache.getIfPresent(cacheKey);
    }
    
    public void putRecentData(String seriesId, Instant startTime, Instant endTime, List<TimeSeriesPoint> data) {
        String cacheKey = seriesId + ":" + startTime.toEpochMilli() + ":" + endTime.toEpochMilli();
        recentDataCache.put(cacheKey, data);
    }
    
    public CacheStats getCacheStats() {
        return recentDataCache.stats();
    }
}

@Component
@NonNullByDefault
public class MetricKeyStorageHandler {
    
    /**
     * Convert MetricKey to optimized storage key structure
     */
    public String createStorageKey(MetricKey key, Instant timestamp) {
        // Use hierarchical structure for efficient querying
        String domain = key.labels().get("domain");
        String operation = key.labels().get("operation");
        
        if (domain != null && operation != null) {
            // Hierarchical key: domain/operation/timestamp
            return String.format("%s/%s/%d", domain, operation, timestamp.toEpochMilli());
        } else {
            // Fallback to kind-based key
            return String.format("%s/%s/%d", key.kind(), key.id().hashCode(), timestamp.toEpochMilli());
        }
    }
    
    /**
     * Create index keys for efficient querying
     */
    public Set<String> createIndexKeys(MetricKey key, Instant timestamp) {
        Set<String> indexKeys = new HashSet<>();
        
        // Time-based index
        long timeBucket = timestamp.toEpochMilli() / (60 * 1000); // 1-minute buckets
        indexKeys.add("time:" + timeBucket);
        
        // Domain-based index
        String domain = key.labels().get("domain");
        if (domain != null) {
            indexKeys.add("domain:" + domain);
        }
        
        // Kind-based index
        indexKeys.add("kind:" + key.kind());
        
        // Capability-based index
        for (String capability : key.capabilities()) {
            indexKeys.add("capability:" + capability);
        }
        
        return indexKeys;
    }
}

@Component
@NonNullByDefault
public class PredictiveCacheManager {
    
    private final Map<String, AccessPattern> accessPatterns = new ConcurrentHashMap<>();
    
    /**
     * Analyze access patterns and pre-cache likely needed data
     */
    public void preloadPredictiveData(String seriesId) {
        AccessPattern pattern = accessPatterns.get(seriesId);
        if (pattern != null) {
            // Pre-cache based on historical access patterns
            Instant now = Instant.now();
            Duration typicalRange = pattern.getTypicalQueryRange();
            
            // Pre-cache recent data
            List<TimeSeriesPoint> recentData = queryTimeSeries(seriesId, 
                now.minus(typicalRange), now);
            cacheRecentData(seriesId, recentData);
            
            // Pre-cache common aggregations
            for (String aggFunction : pattern.getCommonAggregations()) {
                Duration aggPeriod = pattern.getCommonAggregationPeriod();
                List<AggregatedPoint> aggregated = queryWithAggregation(seriesId,
                    now.minus(typicalRange), now, aggFunction, aggPeriod);
                cacheAggregatedData(seriesId, aggFunction, aggPeriod, aggregated);
            }
        }
    }
    
    /**
     * Track access patterns for predictive caching
     */
    public void recordAccess(String seriesId, Duration queryRange, 
                           String aggregationFunction, Duration aggregationPeriod) {
        accessPatterns.computeIfAbsent(seriesId, k -> new AccessPattern())
            .recordAccess(queryRange, aggregationFunction, aggregationPeriod);
    }
}

@Component
@NonNullByDefault
public class OptimizedTimeSeriesStorage implements MetricTimeSeriesStorage {
    
    // Batch operations for better performance
    private final Map<String, List<TimeSeriesPoint>> writeBuffer = new ConcurrentHashMap<>();
    private final ScheduledExecutorService batchProcessor = Executors.newSingleThreadScheduledExecutor();
    
    // Index storage for efficient queries
    private final Map<Long, Set<String>> timeIndex = new ConcurrentHashMap<>();
    private final Map<String, List<Long>> seriesIndex = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> domainIndex = new ConcurrentHashMap<>();
    
    @PostConstruct
    public void init() {
        // Process batches every 5 seconds
        batchProcessor.scheduleAtFixedRate(this::processWriteBatch, 5, 5, TimeUnit.SECONDS);
    }
    
    @Override
    public void storeTimeSeriesPoint(String seriesId, Instant timestamp, 
                                   Map<String, String> tags, Map<String, Object> fields) {
        // Add to write buffer instead of immediate storage
        TimeSeriesPoint point = new TimeSeriesPoint(timestamp, tags, fields, null, null);
        writeBuffer.computeIfAbsent(seriesId, k -> new ArrayList<>()).add(point);
        
        // Update indexes immediately for fast queries
        updateIndexes(seriesId, timestamp, tags);
        
        // Check if buffer is full and needs immediate processing
        if (writeBuffer.get(seriesId).size() >= 100) {
            processSeriesBatch(seriesId);
        }
    }
    
    private void processWriteBatch() {
        for (Map.Entry<String, List<TimeSeriesPoint>> entry : writeBuffer.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                processSeriesBatch(entry.getKey());
            }
        }
    }
    
    private void processSeriesBatch(String seriesId) {
        List<TimeSeriesPoint> points = writeBuffer.remove(seriesId);
        if (points != null && !points.isEmpty()) {
            // Batch write to storage
            batchWriteToStorage(seriesId, points);
        }
    }
    
    private void updateIndexes(String seriesId, Instant timestamp, Map<String, String> tags) {
        long timeBucket = timestamp.toEpochMilli() / (60 * 1000);
        
        // Update time index
        timeIndex.computeIfAbsent(timeBucket, k -> new HashSet<>()).add(seriesId);
        
        // Update series index
        seriesIndex.computeIfAbsent(seriesId, k -> new ArrayList<>()).add(timestamp.toEpochMilli());
        
        // Update domain index
        String domain = tags.get("domain");
        if (domain != null) {
            domainIndex.computeIfAbsent(domain, k -> new HashSet<>()).add(seriesId);
        }
    }
    
    /**
     * Optimized query using indexes
     */
    public List<TimeSeriesPoint> queryOptimized(String seriesId, Instant startTime, Instant endTime) {
        // Check L1 cache first
        String cacheKey = seriesId + ":" + startTime.toEpochMilli() + ":" + endTime.toEpochMilli();
        List<TimeSeriesPoint> cached = l1Cache.getIfPresent(cacheKey);
        if (cached != null) {
            return cached;
        }
        
        // Use time index for efficient range queries
        Set<String> candidateKeys = getKeysFromTimeIndex(startTime, endTime);
        Set<String> seriesKeys = candidateKeys.stream()
            .filter(key -> key.startsWith(seriesId + ":"))
            .collect(Collectors.toSet());
        
        // Batch read from storage
        List<TimeSeriesPoint> results = batchReadFromStorage(seriesKeys);
        
        // Cache results
        l1Cache.put(cacheKey, results);
        
        return results;
    }
}
```

##### **3.6.9 Configuration and Management**

**Tasks:**
- [ ] **Configuration Management Interface**
  - [ ] Create `timeseries.cfg` configuration file in `src/main/resources/OH-INF/config/`
  - [ ] Implement `MetricTimeSeriesConfigurationService` extending existing `ConfigurationService` pattern
  - [ ] Add configuration properties validation using existing validation framework
  - [ ] Implement configuration change handling with `ConfigurationChangeListener` pattern
  - [ ] Add configuration persistence using existing storage service integration
  - [ ] Create configuration migration using existing upgrade procedures

- [ ] **Retention Policies Implementation**
  - [ ] Implement configurable retention periods per series type via `.cfg` file
  - [ ] Add automatic cleanup scheduling using existing `ScheduledExecutorService` patterns
  - [ ] Create retention policy validation using existing validation utilities
  - [ ] Implement retention policy change notifications via `ConfigurationChangeEvent`
  - [ ] Add retention policy compliance monitoring using existing metrics framework
  - [ ] Create retention policy audit and reporting using existing logging patterns

- [ ] **Monitoring and Health Checks**
  - [ ] Implement storage health monitoring using existing health check patterns
  - [ ] Add performance monitoring using existing `MetricsService` integration
  - [ ] Create storage capacity monitoring using existing alerting framework
  - [ ] Implement data integrity monitoring using existing validation patterns
  - [ ] Add system resource monitoring using existing performance monitoring
  - [ ] Create health check reporting using existing REST API patterns

- [ ] **Administrative Tools**
  - [ ] Create administrative REST API endpoints using existing REST service patterns
  - [ ] Implement storage management commands using existing action framework
  - [ ] Add data export and import functionality using existing file handling patterns
  - [ ] Create storage statistics and reporting tools using existing reporting framework
  - [ ] Implement storage maintenance and optimization tools using existing utility patterns
  - [ ] Add storage troubleshooting and diagnostic tools using existing diagnostic framework

- [ ] **Scheduling and Automation**
  - [ ] Implement cleanup task scheduling using existing `ScheduledExecutorService` patterns
  - [ ] Add aggregation task scheduling using existing task management framework
  - [ ] Create backup task scheduling using existing backup service integration
  - [ ] Implement maintenance task scheduling using existing maintenance patterns
  - [ ] Add task failure handling using existing error handling and retry mechanisms
  - [ ] Create task monitoring using existing monitoring and alerting framework

- [ ] **Security and Access Control**
  - [ ] Implement access control using existing authentication and authorization framework
  - [ ] Add audit logging using existing audit logging patterns from `ai.common.security.audit.logging`
  - [ ] Create secure configuration storage using existing storage service encryption
  - [ ] Implement data encryption using existing `ai.common.util.encryption.enabled` framework
  - [ ] Add authentication and authorization using existing `ai.common.security.auth` framework
  - [ ] Create security monitoring using existing `ai.common.security.monitoring` patterns

- [ ] **Documentation and Support**
  - [ ] Create comprehensive configuration documentation following existing doc patterns
  - [ ] Add troubleshooting guides using existing troubleshooting framework
  - [ ] Implement configuration validation using existing `ai.common.config.validation` framework
  - [ ] Create performance tuning guides using existing performance monitoring patterns
  - [ ] Add best practices documentation following existing documentation standards
  - [ ] Create migration and upgrade guides using existing upgrade procedures

**Deliverables:**

**1. Configuration File (`timeseries.cfg`):**
```properties
# openHAB AI Time Series Configuration
# This file configures time series storage and management features

# =============================================================================
# Time Series Storage Configuration
# =============================================================================

# Storage Settings
ai.timeseries.storage.enabled=true
ai.timeseries.storage.service=mapdb
ai.timeseries.storage.data.directory=${OPENHAB_USERDATA}/ai/timeseries
ai.timeseries.storage.metadata.directory=${OPENHAB_USERDATA}/ai/timeseries/metadata

# Retention Policies
ai.timeseries.retention.default.days=30
ai.timeseries.retention.metrics.days=90
ai.timeseries.retention.performance.days=7
ai.timeseries.retention.health.days=14

# Aggregation Settings
ai.timeseries.aggregation.enabled=true
ai.timeseries.aggregation.interval.hours=1
ai.timeseries.aggregation.functions=avg,min,max,sum,count
ai.timeseries.aggregation.precompute=true

# Performance Settings
ai.timeseries.performance.max.points.per.series=10000
ai.timeseries.performance.batch.size=1000
ai.timeseries.performance.flush.interval.seconds=60
ai.timeseries.performance.compression.enabled=true

# Cleanup and Maintenance
ai.timeseries.cleanup.enabled=true
ai.timeseries.cleanup.interval.hours=24
ai.timeseries.cleanup.parallel.enabled=true
ai.timeseries.cleanup.thread.pool.size=2

# Health Monitoring
ai.timeseries.health.monitoring.enabled=true
ai.timeseries.health.check.interval.seconds=300
ai.timeseries.health.alert.threshold.percent=85
```

**2. Configuration Service Implementation:**
```java
@Component(service = MetricTimeSeriesConfigurationService.class)
@NonNullByDefault
public class MetricTimeSeriesConfigurationService implements ConfigurationService {
    
    private static final Logger logger = LoggerFactory.getLogger(MetricTimeSeriesConfigurationService.class);
    
    @Reference
    private @Nullable MetricTimeSeriesStorage timeSeriesStorage;
    
    @Reference
    private @Nullable StorageService storageService;
    
    // Configuration properties with defaults
    private Duration retentionPeriod = Duration.ofDays(30);
    private Duration aggregationPeriod = Duration.ofHours(1);
    private boolean autoCleanup = true;
    private int maxPointsPerSeries = 10000;
    private boolean compressionEnabled = true;
    
    @Activate
    public void activate(Map<String, Object> config) {
        updateConfiguration(config);
        
        if (autoCleanup) {
            scheduleCleanup();
        }
        
        logger.info("Time series configuration service activated");
    }
    
    @Modified
    public void modified(Map<String, Object> config) {
        updateConfiguration(config);
        logger.info("Time series configuration modified");
    }
    
    @Deactivate
    public void deactivate() {
        logger.info("Time series configuration service deactivated");
    }
    
    private void updateConfiguration(Map<String, Object> config) {
        retentionPeriod = Duration.ofDays(getConfigValue(config, "ai.timeseries.retention.default.days", 30));
        aggregationPeriod = Duration.ofHours(getConfigValue(config, "ai.timeseries.aggregation.interval.hours", 1));
        autoCleanup = getConfigValue(config, "ai.timeseries.cleanup.enabled", true);
        maxPointsPerSeries = getConfigValue(config, "ai.timeseries.performance.max.points.per.series", 10000);
        compressionEnabled = getConfigValue(config, "ai.timeseries.performance.compression.enabled", true);
        
        logger.info("Time series configuration updated: retention={}, aggregation={}, autoCleanup={}", 
                   retentionPeriod, aggregationPeriod, autoCleanup);
    }
    
    private <T> T getConfigValue(Map<String, Object> config, String key, T defaultValue) {
        Object value = config.get(key);
        if (value != null) {
            try {
                return (T) value;
            } catch (ClassCastException e) {
                logger.warn("Invalid configuration value for key {}: {}", key, value);
            }
        }
        return defaultValue;
    }
    
    private void scheduleCleanup() {
        // Use existing ScheduledExecutorService patterns from the codebase
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1, 
            r -> new Thread(r, "timeseries-cleanup"));
        
        scheduler.scheduleAtFixedRate(() -> {
            if (timeSeriesStorage != null) {
                try {
                timeSeriesStorage.cleanupOldData(retentionPeriod);
                } catch (Exception e) {
                    logger.error("Error during time series cleanup", e);
                }
            }
        }, 24, 24, TimeUnit.HOURS);
    }
    
    // ConfigurationService interface implementation
    @Override
    public Optional<String> getConfigValue(String key) {
        // Implementation using existing configuration patterns
        return Optional.empty();
    }
    
    @Override
    public String getConfigValue(String key, @Nullable String defaultValue) {
        // Implementation using existing configuration patterns
        return defaultValue;
    }
    
    // Additional time series specific methods
    public Duration getRetentionPeriod() {
        return retentionPeriod;
    }
    
    public Duration getAggregationPeriod() {
        return aggregationPeriod;
    }
    
    public boolean isAutoCleanupEnabled() {
        return autoCleanup;
    }
    
    public int getMaxPointsPerSeries() {
        return maxPointsPerSeries;
    }
    
    public boolean isCompressionEnabled() {
        return compressionEnabled;
    }
}
```

#### **3.6.10 Implementation Timeline**

**Week 1: Core Infrastructure**
- [ ] **Interface and Data Models (Days 1-2)**
  - [ ] Implement MetricTimeSeriesStorage interface
  - [ ] Create TimeSeriesPoint and AggregatedPoint data models
  - [ ] Create TimeSeriesQueryCriteria and TimeSeriesMetadata classes
  - [ ] Add validation methods and null safety annotations
  - [ ] Create aggregation functions enum and query builder

- [ ] **StorageService Integration Setup (Days 3-4)**
  - [ ] Create StorageServiceTimeSeriesStorage class with OSGi annotations
  - [ ] Set up StorageService dependency injection and initialization
  - [ ] Create storage instances for time series data, metadata, and configuration
  - [ ] Implement proper error handling for StorageService unavailability
  - [ ] Add storage initialization validation and logging

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
- [ ] **Enhanced StorageService Features (Days 1-2)**
  - [ ] Implement advanced key design patterns and composite keys
  - [ ] Add storage partitioning and sharding strategies
  - [ ] Create enhanced query capabilities with fluent API
  - [ ] Implement data lifecycle management and tiering
  - [ ] Add advanced backup and recovery features

- [ ] **MetricsService Integration (Days 3-4)**
  - [ ] Modify existing DefaultMetricsService class
  - [ ] Implement dual recording (real-time + time series) in existing methods
  - [ ] Add automatic time series recording to existing recordOperation methods
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

#### **3.6.11 Benefits of This Approach**

1. **No Item Dependency**: Works independently of openHAB Items
2. **Hybrid Storage**: Combines real-time metrics with historical storage
3. **Flexible Configuration**: Uses openHAB's StorageService for configuration
4. **Advanced Analytics**: Provides rich querying and aggregation capabilities
5. **Automatic Management**: Includes retention policies and cleanup
6. **Performance Optimized**: Uses openHAB StorageService for high-performance time series storage
7. **Scalable**: Can handle large volumes of metrics data
8. **Integration Ready**: Seamlessly integrates with existing MetricsService
9. **Snapshot Type Preservation**: Stores any MetricsSnapshot type without conversion to GenericMetricsSnapshot
10. **Functional Statistics**: Fixed statistics system that works with time series data
11. **Multi-Layer Caching**: Comprehensive caching architecture for optimal performance
12. **Efficient MetricKey Handling**: Hierarchical key structure with proper indexing
13. **Predictive Caching**: Intelligent cache preloading based on access patterns
14. **Batch Operations**: Optimized storage operations with batching and buffering

#### **3.6.12 Task Tracking Summary**

**Total Tasks Breakdown:**
- **3.6.1 Core Interface**: 20 tasks (4 major categories)
- **3.6.2 StorageService Implementation**: 42 tasks (7 major categories) ✅ **COMPLETE**
- **3.6.3 Enhanced StorageService Features**: 42 tasks (7 major categories)
- **3.6.4 MetricsService Integration**: 42 tasks (7 major categories)
- **3.6.5 Snapshot Storage with Minimal Modification**: 24 tasks (4 major categories) ✅ **COMPLETE**
- **3.6.6 Fix Statistics Integration**: 30 tasks (5 major categories) ✅ **COMPLETE**
- **3.6.7 Multi-Layer Caching Architecture**: 30 tasks (5 major categories)
- **3.6.8 Hierarchical Key Structure Implementation**: 10 tasks (1 major category) ✅ **COMPLETE**
- **3.6.9 Configuration Management**: 42 tasks (7 major categories)
- **3.6.10 Implementation Timeline**: 35 tasks (4 weeks, daily breakdown)

**Grand Total: 307 Trackable Tasks**

**Progress Tracking Categories:**
- [x] **Core Infrastructure** (Week 1): 35 tasks ✅ **COMPLETE**
- [x] **Advanced Features** (Week 2): 35 tasks ✅ **COMPLETE**
- [ ] **Integration** (Week 3): 35 tasks
- [ ] **Testing & Optimization** (Week 4): 35 tasks
- [x] **Snapshot Storage & Statistics** (Week 5): 54 tasks (3.6.5 + 3.6.6) ✅ **COMPLETE**
- [x] **Caching & Performance** (Week 6): 40 tasks (3.6.7 + 3.6.8) ✅ **PARTIAL** (3.6.8 complete, 3.6.7 pending)
- [ ] **Cross-cutting Concerns**: 83 tasks (distributed across all phases)

**Key Milestones:**
- [ ] **Milestone 1**: Core interfaces and data models complete
- [x] **Milestone 2**: StorageService time series implementation functional ✅ **COMPLETE**
- [ ] **Milestone 3**: Enhanced StorageService features operational
- [ ] **Milestone 4**: DefaultMetricsService modification complete
- [x] **Milestone 5**: Snapshot storage with minimal modification complete ✅ **COMPLETE**
- [x] **Milestone 6**: Statistics integration fixed and operational ✅ **COMPLETE**
- [ ] **Milestone 7**: Multi-layer caching architecture implemented
- [x] **Milestone 8**: Hierarchical key structure and indexing complete ✅ **COMPLETE**
- [ ] **Milestone 9**: Configuration management implemented
- [ ] **Milestone 10**: All testing and optimization complete
- [ ] **Milestone 11**: Production-ready deployment

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
