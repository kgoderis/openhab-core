package org.openhab.core.ai.common.monitoring.timeseries.storage;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;


import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.MetricKey;
import org.openhab.core.ai.common.monitoring.api.MetricKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities for converting MetricKey instances to storage keys and vice versa.
 * 
 * <p>
 * This class provides conversion utilities between MetricKey instances and various
 * storage key formats, enabling efficient storage and retrieval operations.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class StorageKeyConverter {

    private static final Logger logger = LoggerFactory.getLogger(StorageKeyConverter.class);

    // Storage key prefixes and separators
    private static final String METRIC_PREFIX = "metric:";
    private static final String SERIES_PREFIX = "series:";
    private static final String INDEX_PREFIX = "index:";
    private static final String KEY_SEPARATOR = ":";

    private StorageKeyConverter() {
        // Utility class - prevent instantiation
    }

    /**
     * Convert a MetricKey to a storage key.
     * 
     * @param key the metric key
     * @return the storage key
     */
    public static String toStorageKey(MetricKey key) {
        try {
            return METRIC_PREFIX + key.kind() + KEY_SEPARATOR + key.id();
        } catch (Exception e) {
            logger.warn("Failed to convert MetricKey to storage key: {}", key.id(), e);
            return METRIC_PREFIX + "error:" + System.currentTimeMillis();
        }
    }

    /**
     * Convert a MetricKey to a series storage key.
     * 
     * @param key the metric key
     * @return the series storage key
     */
    public static String toSeriesStorageKey(MetricKey key) {
        try {
            return SERIES_PREFIX + key.kind() + KEY_SEPARATOR + key.id();
        } catch (Exception e) {
            logger.warn("Failed to convert MetricKey to series storage key: {}", key.id(), e);
            return SERIES_PREFIX + "error:" + System.currentTimeMillis();
        }
    }

    /**
     * Convert a MetricKey to a hierarchical storage key with timestamp.
     * 
     * @param key the metric key
     * @param timestamp the timestamp
     * @return the hierarchical storage key
     */
    public static String toHierarchicalStorageKey(MetricKey key, Instant timestamp) {
        try {
            return MetricKeyStorageHandler.createHierarchicalStorageKey(key, timestamp);
        } catch (Exception e) {
            logger.warn("Failed to convert MetricKey to hierarchical storage key: {}", key.id(), e);
            return "hierarchical:error:" + System.currentTimeMillis();
        }
    }

    /**
     * Convert a MetricKey to an index storage key.
     * 
     * @param key the metric key
     * @param indexType the type of index
     * @return the index storage key
     */
    public static String toIndexStorageKey(MetricKey key, String indexType) {
        try {
            return INDEX_PREFIX + indexType + KEY_SEPARATOR + key.kind() + KEY_SEPARATOR + key.id();
        } catch (Exception e) {
            logger.warn("Failed to convert MetricKey to index storage key: {}", key.id(), e);
            return INDEX_PREFIX + indexType + ":error:" + System.currentTimeMillis();
        }
    }

    /**
     * Convert a storage key back to a MetricKey (if possible).
     * 
     * @param storageKey the storage key
     * @return the reconstructed MetricKey or null if conversion fails
     */
    @Nullable
    public static MetricKey fromStorageKey(String storageKey) {
        try {
            if (storageKey == null || storageKey.isEmpty()) {
                return null;
            }

            if (storageKey.startsWith(METRIC_PREFIX)) {
                return fromMetricStorageKey(storageKey);
            } else if (storageKey.startsWith(SERIES_PREFIX)) {
                return fromSeriesStorageKey(storageKey);
            } else if (storageKey.startsWith(INDEX_PREFIX)) {
                return fromIndexStorageKey(storageKey);
            } else {
                // Try to parse as hierarchical key
                return fromHierarchicalStorageKey(storageKey);
            }
        } catch (Exception e) {
            logger.warn("Failed to convert storage key to MetricKey: {}", storageKey, e);
            return null;
        }
    }

    /**
     * Convert a metric storage key back to a MetricKey.
     * 
     * @param storageKey the metric storage key
     * @return the reconstructed MetricKey or null if conversion fails
     */
    @Nullable
    private static MetricKey fromMetricStorageKey(String storageKey) {
        try {
            String withoutPrefix = storageKey.substring(METRIC_PREFIX.length());
            String[] parts = withoutPrefix.split(KEY_SEPARATOR, 2);
            
            if (parts.length < 2) {
                return null;
            }
            
            String kind = parts[0];
            String id = parts[1];
            
            // Parse the ID to extract labels and capabilities
            return parseMetricKeyFromId(kind, id);
        } catch (Exception e) {
            logger.warn("Failed to parse metric storage key: {}", storageKey, e);
            return null;
        }
    }

    /**
     * Convert a series storage key back to a MetricKey.
     * 
     * @param storageKey the series storage key
     * @return the reconstructed MetricKey or null if conversion fails
     */
    @Nullable
    private static MetricKey fromSeriesStorageKey(String storageKey) {
        try {
            String withoutPrefix = storageKey.substring(SERIES_PREFIX.length());
            String[] parts = withoutPrefix.split(KEY_SEPARATOR, 2);
            
            if (parts.length < 2) {
                return null;
            }
            
            String kind = parts[0];
            String id = parts[1];
            
            return parseMetricKeyFromId(kind, id);
        } catch (Exception e) {
            logger.warn("Failed to parse series storage key: {}", storageKey, e);
            return null;
        }
    }

    /**
     * Convert an index storage key back to a MetricKey.
     * 
     * @param storageKey the index storage key
     * @return the reconstructed MetricKey or null if conversion fails
     */
    @Nullable
    private static MetricKey fromIndexStorageKey(String storageKey) {
        try {
            String withoutPrefix = storageKey.substring(INDEX_PREFIX.length());
            String[] parts = withoutPrefix.split(KEY_SEPARATOR, 3);
            
            if (parts.length < 3) {
                return null;
            }
            
            String kind = parts[1];
            String id = parts[2];
            
            return parseMetricKeyFromId(kind, id);
        } catch (Exception e) {
            logger.warn("Failed to parse index storage key: {}", storageKey, e);
            return null;
        }
    }

    /**
     * Convert a hierarchical storage key back to a MetricKey.
     * 
     * @param storageKey the hierarchical storage key
     * @return the reconstructed MetricKey or null if conversion fails
     */
    @Nullable
    private static MetricKey fromHierarchicalStorageKey(String storageKey) {
        try {
            // Hierarchical keys have format: domain/operation/timestamp or kind/subdomain/timestamp
            String[] parts = storageKey.split("/");
            
            if (parts.length < 3) {
                return null;
            }
            
            String domainOrKind = parts[0];
            String operationOrSubdomain = parts[1];
            String timestampStr = parts[2];
            
            // Try to parse as domain/operation structure
            Map<String, String> labels = new HashMap<>();
            labels.put("domain", domainOrKind);
            labels.put("operation", operationOrSubdomain);
            
            // Add timestamp as a label for reference
            labels.put("timestamp", timestampStr);
            
            // Create a basic capability set
            return new MetricKeys.SimpleMetricKey(
                domainOrKind + "-" + operationOrSubdomain,
                labels,
                java.util.Set.of("counts", "latency")
            );
        } catch (Exception e) {
            logger.warn("Failed to parse hierarchical storage key: {}", storageKey, e);
            return null;
        }
    }

    /**
     * Parse a MetricKey from its ID string.
     * 
     * @param kind the metric kind
     * @param id the metric ID
     * @return the reconstructed MetricKey
     */
    private static MetricKey parseMetricKeyFromId(String kind, String id) {
        try {
            // The ID format is: kind|label1=value1,label2=value2
            // We need to extract labels from this format
            Map<String, String> labels = new HashMap<>();
            java.util.Set<String> capabilities = new java.util.HashSet<>();
            
            if (id.contains("|")) {
                String[] idParts = id.split("\\|", 2);
                if (idParts.length > 1) {
                    String labelsStr = idParts[1];
                    String[] labelPairs = labelsStr.split(",");
                    
                    for (String labelPair : labelPairs) {
                        if (labelPair.contains("=")) {
                            String[] keyValue = labelPair.split("=", 2);
                            if (keyValue.length == 2) {
                                labels.put(keyValue[0], keyValue[1]);
                            }
                        }
                    }
                }
            }
            
            // Add default capabilities based on kind
            capabilities.addAll(getDefaultCapabilitiesForKind(kind));
            
            return new MetricKeys.SimpleMetricKey(kind, labels, capabilities);
        } catch (Exception e) {
            logger.warn("Failed to parse MetricKey from ID: kind={}, id={}", kind, id, e);
            // Return a basic MetricKey as fallback
            return new MetricKeys.SimpleMetricKey(kind, Map.of(), java.util.Set.of("counts", "latency"));
        }
    }

    /**
     * Get default capabilities for a given metric kind.
     * 
     * @param kind the metric kind
     * @return set of default capabilities
     */
    private static java.util.Set<String> getDefaultCapabilitiesForKind(String kind) {
        java.util.Set<String> capabilities = new java.util.HashSet<>();
        capabilities.add("counts");
        capabilities.add("latency");
        
        // Add kind-specific capabilities
        if (kind.contains("health")) {
            capabilities.add("health");
        }
        if (kind.contains("cache")) {
            capabilities.add("cache-stats");
        }
        if (kind.contains("security")) {
            capabilities.add("security");
        }
        if (kind.contains("model")) {
            capabilities.add("model");
        }
        if (kind.contains("tool")) {
            capabilities.add("tool");
        }
        if (kind.contains("agent")) {
            capabilities.add("agent");
        }
        
        return capabilities;
    }

    /**
     * Create a storage key for a specific label value.
     * 
     * @param key the metric key
     * @param labelKey the label key
     * @param labelValue the label value
     * @return the label-specific storage key
     */
    public static String toLabelStorageKey(MetricKey key, String labelKey, String labelValue) {
        try {
            return "label:" + labelKey + ":" + labelValue + ":" + key.kind() + ":" + key.id();
        } catch (Exception e) {
            logger.warn("Failed to create label storage key: {}", e.getMessage());
            return "label:error:" + System.currentTimeMillis();
        }
    }

    /**
     * Create a storage key for a specific capability.
     * 
     * @param key the metric key
     * @param capability the capability
     * @return the capability-specific storage key
     */
    public static String toCapabilityStorageKey(MetricKey key, String capability) {
        try {
            return "capability:" + capability + ":" + key.kind() + ":" + key.id();
        } catch (Exception e) {
            logger.warn("Failed to create capability storage key: {}", e.getMessage());
            return "capability:error:" + System.currentTimeMillis();
        }
    }

    /**
     * Create a time-based storage key.
     * 
     * @param key the metric key
     * @param timestamp the timestamp
     * @param timeUnit the time unit (minute, hour, day)
     * @return the time-based storage key
     */
    public static String toTimeBasedStorageKey(MetricKey key, Instant timestamp, String timeUnit) {
        try {
            long timeBucket = getTimeBucket(timestamp, timeUnit);
            return "time:" + timeUnit + ":" + timeBucket + ":" + key.kind() + ":" + key.id();
        } catch (Exception e) {
            logger.warn("Failed to create time-based storage key: {}", e.getMessage());
            return "time:error:" + System.currentTimeMillis();
        }
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
            default:
                return epochMillis;
        }
    }

    /**
     * Validate a storage key format.
     * 
     * @param storageKey the storage key to validate
     * @return true if the storage key format is valid
     */
    public static boolean isValidStorageKey(String storageKey) {
        if (storageKey == null || storageKey.isEmpty()) {
            return false;
        }
        
        try {
            // Check if it's a known format
            return storageKey.startsWith(METRIC_PREFIX) ||
                   storageKey.startsWith(SERIES_PREFIX) ||
                   storageKey.startsWith(INDEX_PREFIX) ||
                   storageKey.contains("/") || // Hierarchical format
                   storageKey.startsWith("label:") ||
                   storageKey.startsWith("capability:") ||
                   storageKey.startsWith("time:");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extract the metric kind from a storage key.
     * 
     * @param storageKey the storage key
     * @return the metric kind or null if extraction fails
     */
    @Nullable
    public static String extractMetricKind(String storageKey) {
        try {
            if (storageKey.startsWith(METRIC_PREFIX)) {
                String withoutPrefix = storageKey.substring(METRIC_PREFIX.length());
                String[] parts = withoutPrefix.split(KEY_SEPARATOR, 2);
                return parts.length > 0 ? parts[0] : null;
            } else if (storageKey.startsWith(SERIES_PREFIX)) {
                String withoutPrefix = storageKey.substring(SERIES_PREFIX.length());
                String[] parts = withoutPrefix.split(KEY_SEPARATOR, 2);
                return parts.length > 0 ? parts[0] : null;
            } else if (storageKey.startsWith(INDEX_PREFIX)) {
                String withoutPrefix = storageKey.substring(INDEX_PREFIX.length());
                String[] parts = withoutPrefix.split(KEY_SEPARATOR, 3);
                return parts.length > 1 ? parts[1] : null;
            } else if (storageKey.contains("/")) {
                // Hierarchical format
                String[] parts = storageKey.split("/");
                return parts.length > 0 ? parts[0] : null;
            }
        } catch (Exception e) {
            logger.warn("Failed to extract metric kind from storage key: {}", storageKey, e);
        }
        
        return null;
    }

    /**
     * Extract the metric ID from a storage key.
     * 
     * @param storageKey the storage key
     * @return the metric ID or null if extraction fails
     */
    @Nullable
    public static String extractMetricId(String storageKey) {
        try {
            if (storageKey.startsWith(METRIC_PREFIX)) {
                String withoutPrefix = storageKey.substring(METRIC_PREFIX.length());
                String[] parts = withoutPrefix.split(KEY_SEPARATOR, 2);
                return parts.length > 1 ? parts[1] : null;
            } else if (storageKey.startsWith(SERIES_PREFIX)) {
                String withoutPrefix = storageKey.substring(SERIES_PREFIX.length());
                String[] parts = withoutPrefix.split(KEY_SEPARATOR, 2);
                return parts.length > 1 ? parts[1] : null;
            } else if (storageKey.startsWith(INDEX_PREFIX)) {
                String withoutPrefix = storageKey.substring(INDEX_PREFIX.length());
                String[] parts = withoutPrefix.split(KEY_SEPARATOR, 3);
                return parts.length > 2 ? parts[2] : null;
            }
        } catch (Exception e) {
            logger.warn("Failed to extract metric ID from storage key: {}", storageKey, e);
        }
        
        return null;
    }
}
