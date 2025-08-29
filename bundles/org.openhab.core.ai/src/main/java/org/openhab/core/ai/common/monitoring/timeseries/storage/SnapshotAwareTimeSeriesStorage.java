package org.openhab.core.ai.common.monitoring.timeseries.storage;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.MetricKey;
import org.openhab.core.ai.common.monitoring.api.MetricsSnapshot;
import org.openhab.core.ai.common.monitoring.timeseries.AggregatedPoint;
import org.openhab.core.ai.common.monitoring.timeseries.MetricTimeSeriesStorage;
import org.openhab.core.ai.common.monitoring.timeseries.TimeSeriesMetadata;
import org.openhab.core.ai.common.monitoring.timeseries.TimeSeriesPoint;
import org.openhab.core.ai.common.monitoring.timeseries.TimeSeriesQueryCriteria;
import org.openhab.core.ai.common.monitoring.timeseries.TimeSeriesStorageStatistics;
import org.openhab.core.ai.common.monitoring.timeseries.serialization.SnapshotSerializer;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Snapshot-aware implementation of MetricTimeSeriesStorage that can store any MetricsSnapshot type.
 * 
 * <p>
 * This implementation extends the basic time series storage capabilities to support
 * storing any MetricsSnapshot type without requiring conversion to a generic format.
 * It provides capability-aware storage and querying methods.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@Component(service = MetricTimeSeriesStorage.class)
@NonNullByDefault
public class SnapshotAwareTimeSeriesStorage implements MetricTimeSeriesStorage {
    
    private static final Logger logger = LoggerFactory.getLogger(SnapshotAwareTimeSeriesStorage.class);
    
    @Reference
    private @Nullable SnapshotSerializer snapshotSerializer;
    
    @Reference
    private @Nullable DefaultMetricTimeSeriesStorage delegateStorage;
    
    /**
     * Store any MetricsSnapshot directly without conversion.
     * 
     * @param snapshot the snapshot to store
     * @param key the metric key for the snapshot
     */
    public void storeSnapshot(MetricsSnapshot snapshot, MetricKey key) {
        try {
            SnapshotSerializer serializer = snapshotSerializer;
            if (serializer == null) {
                logger.warn("SnapshotSerializer not available - cannot store snapshot");
                return;
            }
            
            String seriesId = createSeriesId(key);
            String snapshotType = snapshot.getClass().getName();
            String snapshotData = serializer.serialize(snapshot);
            
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
     * Query snapshots by type.
     * 
     * @param seriesId the series ID
     * @param snapshotType the snapshot type
     * @param startTime the start time
     * @param endTime the end time
     * @return list of time series points with the specified snapshot type
     */
    public List<TimeSeriesPoint> queryBySnapshotType(String seriesId, String snapshotType, 
                                                   Instant startTime, Instant endTime) {
        List<TimeSeriesPoint> points = queryTimeSeries(seriesId, startTime, endTime);
        
        return points.stream()
            .filter(point -> snapshotType.equals(point.snapshotType()))
            .filter(point -> point.snapshotData() != null)
            .collect(Collectors.toList());
    }
    
    /**
     * Query snapshots by capabilities.
     * 
     * @param requiredCapabilities the required capabilities
     * @param startTime the start time
     * @param endTime the end time
     * @return list of time series points with the required capabilities
     */
    public List<TimeSeriesPoint> queryByCapabilities(Set<String> requiredCapabilities, 
                                                   Instant startTime, Instant endTime) {
        Set<String> matchingSeries = getSeriesByCapabilities(requiredCapabilities);
        
        List<TimeSeriesPoint> results = new ArrayList<>();
        for (String seriesId : matchingSeries) {
            List<TimeSeriesPoint> points = queryTimeSeries(seriesId, startTime, endTime);
            results.addAll(points);
        }
        
        return results;
    }
    
    // Delegate methods to the underlying storage implementation
    
    @Override
    public void storeTimeSeriesPoint(String seriesId, Instant timestamp, 
                                   Map<String, String> tags, Map<String, Object> fields) {
        DefaultMetricTimeSeriesStorage storage = delegateStorage;
        if (storage != null) {
            storage.storeTimeSeriesPoint(seriesId, timestamp, tags, fields);
        }
    }
    
    @Override
    public void storeTimeSeriesPoint(String seriesId, Instant timestamp, 
                                   Map<String, String> tags, Map<String, Object> fields,
                                   @Nullable String snapshotType, @Nullable String snapshotData) {
        DefaultMetricTimeSeriesStorage storage = delegateStorage;
        if (storage != null) {
            // Create a TimeSeriesPoint with snapshot data
            TimeSeriesPoint point = new TimeSeriesPoint(timestamp, tags, fields, snapshotType, snapshotData);
            // Store using the delegate's batch method
            storage.storeTimeSeriesPointsBatch(List.of(point), seriesId);
        }
    }
    
    @Override
    public List<TimeSeriesPoint> queryTimeSeries(String seriesId, Instant startTime, Instant endTime) {
        DefaultMetricTimeSeriesStorage storage = delegateStorage;
        if (storage != null) {
            return storage.queryTimeSeries(seriesId, startTime, endTime);
        }
        return List.of();
    }
    
    @Override
    public Set<String> getSeriesByCapabilities(Set<String> requiredCapabilities) {
        // This would need to be implemented based on the storage backend
        // For now, return empty set
        return new HashSet<>();
    }
    
    @Override
    public Set<String> getAllSnapshotTypes() {
        // This would need to be implemented based on the storage backend
        // For now, return empty set
        return new HashSet<>();
    }
    
    @Override
    public @Nullable TimeSeriesMetadata getTimeSeriesMetadata(String seriesId) {
        DefaultMetricTimeSeriesStorage storage = delegateStorage;
        if (storage != null) {
            return storage.getTimeSeriesMetadata(seriesId);
        }
        return null;
    }
    
    // Additional delegate methods would be implemented here...
    
    private String createSeriesId(MetricKey key) {
        return "snapshot:" + key.kind() + ":" + key.id();
    }
}
