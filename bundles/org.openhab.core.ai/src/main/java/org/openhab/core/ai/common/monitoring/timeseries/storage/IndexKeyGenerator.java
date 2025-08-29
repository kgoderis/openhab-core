package org.openhab.core.ai.common.monitoring.timeseries.storage;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.MetricKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generator for index keys to enable efficient filtering and querying of time series data.
 * 
 * <p>
 * This class provides utilities for generating various types of index keys that enable
 * efficient querying by time, domain, operation, capability, and label values.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class IndexKeyGenerator {

    private static final Logger logger = LoggerFactory.getLogger(IndexKeyGenerator.class);

    // Index key prefixes
    private static final String TIME_INDEX_PREFIX = "idx:time:";
    private static final String DOMAIN_INDEX_PREFIX = "idx:domain:";
    private static final String OPERATION_INDEX_PREFIX = "idx:operation:";
    private static final String CAPABILITY_INDEX_PREFIX = "idx:capability:";
    private static final String LABEL_INDEX_PREFIX = "idx:label:";
    private static final String SERIES_INDEX_PREFIX = "idx:series:";
    private static final String KIND_INDEX_PREFIX = "idx:kind:";
    private static final String COMBINATION_INDEX_PREFIX = "idx:combination:";

    // Key separators
    private static final String KEY_SEPARATOR = ":";
    private static final String VALUE_SEPARATOR = "=";
    private static final String CAPABILITY_SEPARATOR = ",";

    private IndexKeyGenerator() {
        // Utility class - prevent instantiation
    }

    /**
     * Generate all index keys for a MetricKey and timestamp.
     * 
     * @param key the metric key
     * @param timestamp the timestamp
     * @return set of all index keys
     */
    public static Set<String> generateAllIndexKeys(MetricKey key, Instant timestamp) {
        Set<String> indexKeys = new HashSet<>();
        
        try {
            indexKeys.addAll(generateTimeBasedIndexKeys(key, timestamp));
            indexKeys.addAll(generateDomainIndexKeys(key));
            indexKeys.addAll(generateOperationIndexKeys(key));
            indexKeys.addAll(generateCapabilityIndexKeys(key));
            indexKeys.addAll(generateLabelIndexKeys(key));
            indexKeys.addAll(generateSeriesIndexKeys(key));
            indexKeys.addAll(generateKindIndexKeys(key));
            indexKeys.addAll(generateCombinationIndexKeys(key));
        } catch (Exception e) {
            logger.warn("Failed to generate index keys for MetricKey: {}", key.id(), e);
        }
        
        return indexKeys;
    }

    /**
     * Generate time-based index keys for efficient time range queries.
     * 
     * @param key the metric key
     * @param timestamp the timestamp
     * @return list of time-based index keys
     */
    public static List<String> generateTimeBasedIndexKeys(MetricKey key, Instant timestamp) {
        List<String> indexKeys = new ArrayList<>();
        
        try {
            String seriesId = StorageKeyConverter.toSeriesStorageKey(key);
            
            // Generate indexes for different time granularities
            indexKeys.add(generateTimeIndexKey("minute", timestamp, seriesId));
            indexKeys.add(generateTimeIndexKey("hour", timestamp, seriesId));
            indexKeys.add(generateTimeIndexKey("day", timestamp, seriesId));
            indexKeys.add(generateTimeIndexKey("week", timestamp, seriesId));
            indexKeys.add(generateTimeIndexKey("month", timestamp, seriesId));
            
        } catch (Exception e) {
            logger.warn("Failed to generate time-based index keys for MetricKey: {}", key.id(), e);
        }
        
        return indexKeys;
    }

    /**
     * Generate domain-based index keys.
     * 
     * @param key the metric key
     * @return list of domain-based index keys
     */
    public static List<String> generateDomainIndexKeys(MetricKey key) {
        List<String> indexKeys = new ArrayList<>();
        
        try {
            String seriesId = StorageKeyConverter.toSeriesStorageKey(key);
            MetricKeyStorageHandler.MetricKeyRelationshipInfo relationshipInfo = 
                MetricKeyStorageHandler.createKeyRelationshipInfo(key);
            
            if (relationshipInfo.domain() != null) {
                indexKeys.add(DOMAIN_INDEX_PREFIX + relationshipInfo.domain() + KEY_SEPARATOR + seriesId);
                
                if (relationshipInfo.subDomain() != null) {
                    indexKeys.add(DOMAIN_INDEX_PREFIX + relationshipInfo.domain() + "/" + 
                                relationshipInfo.subDomain() + KEY_SEPARATOR + seriesId);
                }
            }
            
            // Add indexes for related domains
            for (String relatedDomain : relationshipInfo.relatedDomains()) {
                indexKeys.add(DOMAIN_INDEX_PREFIX + relatedDomain + KEY_SEPARATOR + seriesId);
            }
            
        } catch (Exception e) {
            logger.warn("Failed to generate domain index keys for MetricKey: {}", key.id(), e);
        }
        
        return indexKeys;
    }

    /**
     * Generate operation-based index keys.
     * 
     * @param key the metric key
     * @return list of operation-based index keys
     */
    public static List<String> generateOperationIndexKeys(MetricKey key) {
        List<String> indexKeys = new ArrayList<>();
        
        try {
            String seriesId = StorageKeyConverter.toSeriesStorageKey(key);
            MetricKeyStorageHandler.MetricKeyRelationshipInfo relationshipInfo = 
                MetricKeyStorageHandler.createKeyRelationshipInfo(key);
            
            if (relationshipInfo.operation() != null) {
                indexKeys.add(OPERATION_INDEX_PREFIX + relationshipInfo.operation() + KEY_SEPARATOR + seriesId);
                
                if (relationshipInfo.subOperation() != null) {
                    indexKeys.add(OPERATION_INDEX_PREFIX + relationshipInfo.operation() + "/" + 
                                relationshipInfo.subOperation() + KEY_SEPARATOR + seriesId);
                }
            }
            
            // Add indexes for related operations
            for (String relatedOperation : relationshipInfo.relatedOperations()) {
                indexKeys.add(OPERATION_INDEX_PREFIX + relatedOperation + KEY_SEPARATOR + seriesId);
            }
            
        } catch (Exception e) {
            logger.warn("Failed to generate operation index keys for MetricKey: {}", key.id(), e);
        }
        
        return indexKeys;
    }

    /**
     * Generate capability-based index keys.
     * 
     * @param key the metric key
     * @return list of capability-based index keys
     */
    public static List<String> generateCapabilityIndexKeys(MetricKey key) {
        List<String> indexKeys = new ArrayList<>();
        
        try {
            String seriesId = StorageKeyConverter.toSeriesStorageKey(key);
            Set<String> capabilities = key.capabilities();
            
            // Generate individual capability indexes
            for (String capability : capabilities) {
                indexKeys.add(CAPABILITY_INDEX_PREFIX + capability + KEY_SEPARATOR + seriesId);
            }
            
            // Generate combination indexes for common capability pairs
            List<String> capabilityList = new ArrayList<>(capabilities);
            for (int i = 0; i < capabilityList.size(); i++) {
                for (int j = i + 1; j < capabilityList.size(); j++) {
                    String combination = capabilityList.get(i) + CAPABILITY_SEPARATOR + capabilityList.get(j);
                    indexKeys.add(CAPABILITY_INDEX_PREFIX + "combination:" + combination + KEY_SEPARATOR + seriesId);
                }
            }
            
            // Generate indexes for capability groups
            indexKeys.addAll(generateCapabilityGroupIndexKeys(capabilities, seriesId));
            
        } catch (Exception e) {
            logger.warn("Failed to generate capability index keys for MetricKey: {}", key.id(), e);
        }
        
        return indexKeys;
    }

    /**
     * Generate label-based index keys.
     * 
     * @param key the metric key
     * @return list of label-based index keys
     */
    public static List<String> generateLabelIndexKeys(MetricKey key) {
        List<String> indexKeys = new ArrayList<>();
        
        try {
            String seriesId = StorageKeyConverter.toSeriesStorageKey(key);
            Map<String, String> labels = key.labels();
            
            for (Map.Entry<String, String> entry : labels.entrySet()) {
                String labelKey = entry.getKey();
                String labelValue = entry.getValue();
                
                // Skip high-cardinality labels
                if (isHighCardinalityLabel(labelKey, labelValue)) {
                    continue;
                }
                
                indexKeys.add(LABEL_INDEX_PREFIX + labelKey + VALUE_SEPARATOR + labelValue + KEY_SEPARATOR + seriesId);
            }
            
        } catch (Exception e) {
            logger.warn("Failed to generate label index keys for MetricKey: {}", key.id(), e);
        }
        
        return indexKeys;
    }

    /**
     * Generate series-based index keys.
     * 
     * @param key the metric key
     * @return list of series-based index keys
     */
    public static List<String> generateSeriesIndexKeys(MetricKey key) {
        List<String> indexKeys = new ArrayList<>();
        
        try {
            String seriesId = StorageKeyConverter.toSeriesStorageKey(key);
            indexKeys.add(SERIES_INDEX_PREFIX + seriesId);
            
            // Generate parent series indexes
            MetricKeyStorageHandler.MetricKeyRelationshipInfo relationshipInfo = 
                MetricKeyStorageHandler.createKeyRelationshipInfo(key);
            
            for (String parentKey : relationshipInfo.parentKeys()) {
                indexKeys.add(SERIES_INDEX_PREFIX + "parent:" + parentKey + KEY_SEPARATOR + seriesId);
            }
            
            // Generate child series indexes
            for (String childKey : relationshipInfo.childKeys()) {
                indexKeys.add(SERIES_INDEX_PREFIX + "child:" + childKey + KEY_SEPARATOR + seriesId);
            }
            
        } catch (Exception e) {
            logger.warn("Failed to generate series index keys for MetricKey: {}", key.id(), e);
        }
        
        return indexKeys;
    }

    /**
     * Generate kind-based index keys.
     * 
     * @param key the metric key
     * @return list of kind-based index keys
     */
    public static List<String> generateKindIndexKeys(MetricKey key) {
        List<String> indexKeys = new ArrayList<>();
        
        try {
            String seriesId = StorageKeyConverter.toSeriesStorageKey(key);
            String kind = key.kind();
            
            indexKeys.add(KIND_INDEX_PREFIX + kind + KEY_SEPARATOR + seriesId);
            
            // Generate sub-kind indexes if kind contains separators
            if (kind.contains("-")) {
                String[] kindParts = kind.split("-");
                for (int i = 1; i <= kindParts.length; i++) {
                    String subKind = String.join("-", java.util.Arrays.copyOfRange(kindParts, 0, i));
                    indexKeys.add(KIND_INDEX_PREFIX + "sub:" + subKind + KEY_SEPARATOR + seriesId);
                }
            }
            
        } catch (Exception e) {
            logger.warn("Failed to generate kind index keys for MetricKey: {}", key.id(), e);
        }
        
        return indexKeys;
    }

    /**
     * Generate combination index keys for complex queries.
     * 
     * @param key the metric key
     * @return list of combination index keys
     */
    public static List<String> generateCombinationIndexKeys(MetricKey key) {
        List<String> indexKeys = new ArrayList<>();
        
        try {
            String seriesId = StorageKeyConverter.toSeriesStorageKey(key);
            MetricKeyStorageHandler.MetricKeyRelationshipInfo relationshipInfo = 
                MetricKeyStorageHandler.createKeyRelationshipInfo(key);
            
            // Domain + Operation combinations
            if (relationshipInfo.domain() != null && relationshipInfo.operation() != null) {
                indexKeys.add(COMBINATION_INDEX_PREFIX + "domain-operation:" + 
                            relationshipInfo.domain() + "/" + relationshipInfo.operation() + KEY_SEPARATOR + seriesId);
            }
            
            // Domain + Capability combinations
            if (relationshipInfo.domain() != null) {
                for (String capability : key.capabilities()) {
                    indexKeys.add(COMBINATION_INDEX_PREFIX + "domain-capability:" + 
                                relationshipInfo.domain() + "/" + capability + KEY_SEPARATOR + seriesId);
                }
            }
            
            // Operation + Capability combinations
            if (relationshipInfo.operation() != null) {
                for (String capability : key.capabilities()) {
                    indexKeys.add(COMBINATION_INDEX_PREFIX + "operation-capability:" + 
                                relationshipInfo.operation() + "/" + capability + KEY_SEPARATOR + seriesId);
                }
            }
            
            // Kind + Domain combinations
            if (relationshipInfo.domain() != null) {
                indexKeys.add(COMBINATION_INDEX_PREFIX + "kind-domain:" + 
                            key.kind() + "/" + relationshipInfo.domain() + KEY_SEPARATOR + seriesId);
            }
            
        } catch (Exception e) {
            logger.warn("Failed to generate combination index keys for MetricKey: {}", key.id(), e);
        }
        
        return indexKeys;
    }

    /**
     * Generate a time-based index key.
     * 
     * @param timeUnit the time unit
     * @param timestamp the timestamp
     * @param seriesId the series ID
     * @return the time-based index key
     */
    private static String generateTimeIndexKey(String timeUnit, Instant timestamp, String seriesId) {
        long timeBucket = getTimeBucket(timestamp, timeUnit);
        return TIME_INDEX_PREFIX + timeUnit + ":" + timeBucket + KEY_SEPARATOR + seriesId;
    }

    /**
     * Generate capability group index keys.
     * 
     * @param capabilities the capabilities
     * @param seriesId the series ID
     * @return list of capability group index keys
     */
    private static List<String> generateCapabilityGroupIndexKeys(Set<String> capabilities, String seriesId) {
        List<String> indexKeys = new ArrayList<>();
        
        // Define capability groups
        Map<String, Set<String>> capabilityGroups = Map.of(
            "performance", Set.of("counts", "latency", "timing"),
            "health", Set.of("health", "circuit-breaker", "status"),
            "security", Set.of("security", "authentication", "authorization"),
            "cache", Set.of("cache-stats", "hit-rate", "eviction"),
            "model", Set.of("model", "model-stats", "completion"),
            "tool", Set.of("tool", "tool-stats", "execution"),
            "agent", Set.of("agent", "agent-stats", "behavior-stats")
        );
        
        // Generate indexes for capability groups
        for (Map.Entry<String, Set<String>> group : capabilityGroups.entrySet()) {
            String groupName = group.getKey();
            Set<String> groupCapabilities = group.getValue();
            
            if (capabilities.stream().anyMatch(groupCapabilities::contains)) {
                indexKeys.add(CAPABILITY_INDEX_PREFIX + "group:" + groupName + KEY_SEPARATOR + seriesId);
            }
        }
        
        return indexKeys;
    }

    /**
     * Get time bucket for a given timestamp and time unit.
     * 
     * @param timestamp the timestamp
     * @param timeUnit the time unit
     * @return the time bucket
     */
    private static long getTimeBucket(Instant timestamp, String timeUnit) {
        long epochMillis = timestamp.toEpochMilli();
        
        switch (timeUnit.toLowerCase()) {
            case "minute":
                return epochMillis / (60 * 1000) * (60 * 1000);
            case "hour":
                return epochMillis / (60 * 60 * 1000) * (60 * 60 * 1000);
            case "day":
                return epochMillis / (24 * 60 * 60 * 1000) * (24 * 60 * 60 * 1000);
            case "week":
                return epochMillis / (7 * 24 * 60 * 60 * 1000) * (7 * 24 * 60 * 60 * 1000);
            case "month":
                // Approximate month as 30 days
                return epochMillis / (30 * 24 * 60 * 60 * 1000) * (30 * 24 * 60 * 60 * 1000);
            default:
                return epochMillis;
        }
    }

    /**
     * Check if a label represents high-cardinality data.
     * 
     * @param labelKey the label key
     * @param labelValue the label value
     * @return true if high-cardinality
     */
    private static boolean isHighCardinalityLabel(String labelKey, String labelValue) {
        // High-cardinality label keys
        String[] highCardinalityKeys = {"id", "uuid", "timestamp", "requestId", "sessionId", "userId"};
        for (String key : highCardinalityKeys) {
            if (labelKey.toLowerCase().contains(key)) {
                return true;
            }
        }
        
        // High-cardinality value patterns
        if (labelValue.matches(".*\\d{10,}.*")) { // Contains long numbers (timestamps, IDs)
            return true;
        }
        
        if (labelValue.length() > 50) { // Very long values
            return true;
        }
        
        return false;
    }

    /**
     * Generate index keys for a specific query pattern.
     * 
     * @param queryPattern the query pattern
     * @param key the metric key
     * @param timestamp the timestamp
     * @return list of index keys for the query pattern
     */
    public static List<String> generateIndexKeysForQuery(QueryPattern queryPattern, MetricKey key, Instant timestamp) {
        List<String> indexKeys = new ArrayList<>();
        
        try {
            switch (queryPattern) {
                case TIME_RANGE:
                    indexKeys.addAll(generateTimeBasedIndexKeys(key, timestamp));
                    break;
                case DOMAIN_QUERY:
                    indexKeys.addAll(generateDomainIndexKeys(key));
                    break;
                case OPERATION_QUERY:
                    indexKeys.addAll(generateOperationIndexKeys(key));
                    break;
                case CAPABILITY_QUERY:
                    indexKeys.addAll(generateCapabilityIndexKeys(key));
                    break;
                case LABEL_QUERY:
                    indexKeys.addAll(generateLabelIndexKeys(key));
                    break;
                case COMBINATION_QUERY:
                    indexKeys.addAll(generateCombinationIndexKeys(key));
                    break;
                case ALL_INDEXES:
                default:
                    indexKeys.addAll(generateAllIndexKeys(key, timestamp));
                    break;
            }
        } catch (Exception e) {
            logger.warn("Failed to generate index keys for query pattern: {}", queryPattern, e);
        }
        
        return indexKeys;
    }

    /**
     * Query patterns for index key generation.
     */
    public enum QueryPattern {
        TIME_RANGE,
        DOMAIN_QUERY,
        OPERATION_QUERY,
        CAPABILITY_QUERY,
        LABEL_QUERY,
        COMBINATION_QUERY,
        ALL_INDEXES
    }
}
