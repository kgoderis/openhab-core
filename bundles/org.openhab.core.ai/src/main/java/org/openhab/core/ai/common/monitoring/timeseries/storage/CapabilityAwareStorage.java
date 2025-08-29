package org.openhab.core.ai.common.monitoring.timeseries.storage;

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
import org.openhab.core.ai.common.monitoring.timeseries.TimeSeriesPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Capability-aware storage utilities for preserving metadata and enabling efficient queries.
 * 
 * <p>
 * This class provides utilities for storing and retrieving metrics data with full
 * capability awareness, ensuring that metadata is preserved and queries can be
 * efficiently filtered by capabilities.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class CapabilityAwareStorage {

    private static final Logger logger = LoggerFactory.getLogger(CapabilityAwareStorage.class);

    // Metadata keys for capability-aware storage
    private static final String CAPABILITIES_METADATA_KEY = "capabilities";
    private static final String SNAPSHOT_TYPE_METADATA_KEY = "snapshotType";
    private static final String METRIC_KEY_METADATA_KEY = "metricKey";
    private static final String DOMAIN_METADATA_KEY = "domain";
    private static final String OPERATION_METADATA_KEY = "operation";
    private static final String KIND_METADATA_KEY = "kind";

    private CapabilityAwareStorage() {
        // Utility class - prevent instantiation
    }

    /**
     * Create capability-aware tags for time series storage.
     * 
     * @param key the metric key
     * @param snapshot the metrics snapshot
     * @return map of tags with capability metadata
     */
    public static Map<String, String> createCapabilityAwareTags(MetricKey key, MetricsSnapshot snapshot) {
        Map<String, String> tags = new HashMap<>(key.labels());
        
        // Add capability metadata
        tags.put(CAPABILITIES_METADATA_KEY, String.join(",", key.capabilities()));
        tags.put(SNAPSHOT_TYPE_METADATA_KEY, snapshot.getClass().getSimpleName());
        tags.put(METRIC_KEY_METADATA_KEY, key.id());
        tags.put(KIND_METADATA_KEY, key.kind());
        
        // Add domain and operation if available
        String domain = MetricKeyStorageHandler.createKeyRelationshipInfo(key).domain();
        if (domain != null) {
            tags.put(DOMAIN_METADATA_KEY, domain);
        }
        
        String operation = MetricKeyStorageHandler.createKeyRelationshipInfo(key).operation();
        if (operation != null) {
            tags.put(OPERATION_METADATA_KEY, operation);
        }
        
        return tags;
    }

    /**
     * Create capability-aware fields for time series storage.
     * 
     * @param key the metric key
     * @param snapshot the metrics snapshot
     * @param additionalData additional data to include
     * @return map of fields with capability metadata
     */
    public static Map<String, Object> createCapabilityAwareFields(MetricKey key, MetricsSnapshot snapshot, 
                                                                  @Nullable Map<String, Object> additionalData) {
        Map<String, Object> fields = new HashMap<>();
        
        // Add snapshot data
        fields.put("snapshotData", serializeSnapshotData(snapshot));
        fields.put("snapshotType", snapshot.getClass().getName());
        
        // Add capability information
        fields.put("capabilities", new ArrayList<>(key.capabilities()));
        fields.put("metricKeyId", key.id());
        fields.put("kind", key.kind());
        
        // Add timestamp information
        fields.put("timestamp", Instant.now().toEpochMilli());
        
        // Add additional data if provided
        if (additionalData != null) {
            fields.putAll(additionalData);
        }
        
        return fields;
    }

    /**
     * Filter time series points by required capabilities.
     * 
     * @param points the time series points to filter
     * @param requiredCapabilities the required capabilities
     * @return filtered points that have all required capabilities
     */
    public static List<TimeSeriesPoint> filterByCapabilities(List<TimeSeriesPoint> points, 
                                                           Set<String> requiredCapabilities) {
        if (requiredCapabilities.isEmpty()) {
            return points;
        }
        
        return points.stream()
            .filter(point -> hasRequiredCapabilities(point, requiredCapabilities))
            .collect(Collectors.toList());
    }

    /**
     * Filter time series points by snapshot type.
     * 
     * @param points the time series points to filter
     * @param snapshotType the required snapshot type
     * @return filtered points with the specified snapshot type
     */
    public static List<TimeSeriesPoint> filterBySnapshotType(List<TimeSeriesPoint> points, String snapshotType) {
        return points.stream()
            .filter(point -> snapshotType.equals(point.snapshotType()))
            .collect(Collectors.toList());
    }

    /**
     * Filter time series points by domain.
     * 
     * @param points the time series points to filter
     * @param domain the required domain
     * @return filtered points with the specified domain
     */
    public static List<TimeSeriesPoint> filterByDomain(List<TimeSeriesPoint> points, String domain) {
        return points.stream()
            .filter(point -> domain.equals(point.tags().get(DOMAIN_METADATA_KEY)))
            .collect(Collectors.toList());
    }

    /**
     * Filter time series points by operation.
     * 
     * @param points the time series points to filter
     * @param operation the required operation
     * @return filtered points with the specified operation
     */
    public static List<TimeSeriesPoint> filterByOperation(List<TimeSeriesPoint> points, String operation) {
        return points.stream()
            .filter(point -> operation.equals(point.tags().get(OPERATION_METADATA_KEY)))
            .collect(Collectors.toList());
    }

    /**
     * Get all unique capabilities from a list of time series points.
     * 
     * @param points the time series points
     * @return set of all unique capabilities
     */
    public static Set<String> extractAllCapabilities(List<TimeSeriesPoint> points) {
        Set<String> allCapabilities = new HashSet<>();
        
        for (TimeSeriesPoint point : points) {
            String capabilitiesStr = point.tags().get(CAPABILITIES_METADATA_KEY);
            if (capabilitiesStr != null && !capabilitiesStr.isEmpty()) {
                String[] capabilities = capabilitiesStr.split(",");
                for (String capability : capabilities) {
                    allCapabilities.add(capability.trim());
                }
            }
        }
        
        return allCapabilities;
    }

    /**
     * Get all unique snapshot types from a list of time series points.
     * 
     * @param points the time series points
     * @return set of all unique snapshot types
     */
    public static Set<String> extractAllSnapshotTypes(List<TimeSeriesPoint> points) {
        return points.stream()
            .map(TimeSeriesPoint::snapshotType)
            .filter(type -> type != null && !type.isEmpty())
            .collect(Collectors.toSet());
    }

    /**
     * Get all unique domains from a list of time series points.
     * 
     * @param points the time series points
     * @return set of all unique domains
     */
    public static Set<String> extractAllDomains(List<TimeSeriesPoint> points) {
        return points.stream()
            .map(point -> point.tags().get(DOMAIN_METADATA_KEY))
            .filter(domain -> domain != null && !domain.isEmpty())
            .collect(Collectors.toSet());
    }

    /**
     * Get all unique operations from a list of time series points.
     * 
     * @param points the time series points
     * @return set of all unique operations
     */
    public static Set<String> extractAllOperations(List<TimeSeriesPoint> points) {
        return points.stream()
            .map(point -> point.tags().get(OPERATION_METADATA_KEY))
            .filter(operation -> operation != null && !operation.isEmpty())
            .collect(Collectors.toSet());
    }

    /**
     * Create capability-aware query criteria.
     * 
     * @param requiredCapabilities the required capabilities
     * @param snapshotType the required snapshot type (optional)
     * @param domain the required domain (optional)
     * @param operation the required operation (optional)
     * @param startTime the start time
     * @param endTime the end time
     * @return capability-aware query criteria
     */
    public static CapabilityAwareQueryCriteria createQueryCriteria(Set<String> requiredCapabilities,
                                                                  @Nullable String snapshotType,
                                                                  @Nullable String domain,
                                                                  @Nullable String operation,
                                                                  Instant startTime,
                                                                  Instant endTime) {
        return new CapabilityAwareQueryCriteria(
            requiredCapabilities,
            snapshotType,
            domain,
            operation,
            startTime,
            endTime
        );
    }

    /**
     * Apply capability-aware query criteria to filter points.
     * 
     * @param points the time series points to filter
     * @param criteria the query criteria
     * @return filtered points matching the criteria
     */
    public static List<TimeSeriesPoint> applyQueryCriteria(List<TimeSeriesPoint> points, 
                                                         CapabilityAwareQueryCriteria criteria) {
        List<TimeSeriesPoint> filteredPoints = new ArrayList<>(points);
        
        // Filter by capabilities
        if (!criteria.requiredCapabilities().isEmpty()) {
            filteredPoints = filterByCapabilities(filteredPoints, criteria.requiredCapabilities());
        }
        
        // Filter by snapshot type
        if (criteria.snapshotType() != null && !criteria.snapshotType().isEmpty()) {
            filteredPoints = filterBySnapshotType(filteredPoints, criteria.snapshotType());
        }
        
        // Filter by domain
        if (criteria.domain() != null && !criteria.domain().isEmpty()) {
            filteredPoints = filterByDomain(filteredPoints, criteria.domain());
        }
        
        // Filter by operation
        if (criteria.operation() != null && !criteria.operation().isEmpty()) {
            filteredPoints = filterByOperation(filteredPoints, criteria.operation());
        }
        
        // Filter by time range
        filteredPoints = filteredPoints.stream()
            .filter(point -> !point.timestamp().isBefore(criteria.startTime()) && 
                           !point.timestamp().isAfter(criteria.endTime()))
            .collect(Collectors.toList());
        
        return filteredPoints;
    }

    /**
     * Create metadata summary from a list of time series points.
     * 
     * @param points the time series points
     * @return metadata summary
     */
    public static CapabilityAwareMetadataSummary createMetadataSummary(List<TimeSeriesPoint> points) {
        Set<String> capabilities = extractAllCapabilities(points);
        Set<String> snapshotTypes = extractAllSnapshotTypes(points);
        Set<String> domains = extractAllDomains(points);
        Set<String> operations = extractAllOperations(points);
        
        Instant earliestTime = points.stream()
            .map(TimeSeriesPoint::timestamp)
            .min(Instant::compareTo)
            .orElse(Instant.now());
        
        Instant latestTime = points.stream()
            .map(TimeSeriesPoint::timestamp)
            .max(Instant::compareTo)
            .orElse(Instant.now());
        
        return new CapabilityAwareMetadataSummary(
            capabilities,
            snapshotTypes,
            domains,
            operations,
            points.size(),
            earliestTime,
            latestTime
        );
    }

    /**
     * Check if a time series point has the required capabilities.
     * 
     * @param point the time series point
     * @param requiredCapabilities the required capabilities
     * @return true if the point has all required capabilities
     */
    private static boolean hasRequiredCapabilities(TimeSeriesPoint point, Set<String> requiredCapabilities) {
        String capabilitiesStr = point.tags().get(CAPABILITIES_METADATA_KEY);
        if (capabilitiesStr == null || capabilitiesStr.isEmpty()) {
            return false;
        }
        
        Set<String> pointCapabilities = Set.of(capabilitiesStr.split(","));
        return pointCapabilities.containsAll(requiredCapabilities);
    }

    /**
     * Serialize snapshot data for storage.
     * 
     * @param snapshot the metrics snapshot
     * @return serialized snapshot data
     */
    private static String serializeSnapshotData(MetricsSnapshot snapshot) {
        // This is a simplified serialization - in practice, you'd use a proper serializer
        try {
            return snapshot.getClass().getSimpleName() + ":" + snapshot.toString();
        } catch (Exception e) {
            logger.warn("Failed to serialize snapshot data: {}", e.getMessage());
            return "serialization_error";
        }
    }

    /**
     * Capability-aware query criteria.
     */
    public record CapabilityAwareQueryCriteria(
        Set<String> requiredCapabilities,
        @Nullable String snapshotType,
        @Nullable String domain,
        @Nullable String operation,
        Instant startTime,
        Instant endTime
    ) {}

    /**
     * Capability-aware metadata summary.
     */
    public record CapabilityAwareMetadataSummary(
        Set<String> capabilities,
        Set<String> snapshotTypes,
        Set<String> domains,
        Set<String> operations,
        int pointCount,
        Instant earliestTime,
        Instant latestTime
    ) {}
}
