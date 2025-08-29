package org.openhab.core.ai.common.monitoring.timeseries.storage;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
import org.openhab.core.ai.common.monitoring.api.MetricKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for MetricKey storage operations with hierarchical key structure support.
 * 
 * <p>
 * This class provides utilities for converting MetricKey instances to hierarchical
 * storage keys, creating indexes for efficient querying, and managing key relationships.
 * It supports domain/operation/timestamp key structures and various indexing strategies.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class MetricKeyStorageHandler {

    private static final Logger logger = LoggerFactory.getLogger(MetricKeyStorageHandler.class);

    // Key separators for hierarchical structure
    private static final String KEY_SEPARATOR = "/";
    private static final String CAPABILITY_SEPARATOR = ",";

    // Index prefixes for different types of indexes
    private static final String TIME_INDEX_PREFIX = "time:";
    private static final String DOMAIN_INDEX_PREFIX = "domain:";
    private static final String OPERATION_INDEX_PREFIX = "operation:";
    private static final String CAPABILITY_INDEX_PREFIX = "capability:";
    private static final String LABEL_INDEX_PREFIX = "label:";

    private MetricKeyStorageHandler() {
        // Utility class - prevent instantiation
    }

    /**
     * Create a hierarchical storage key from a MetricKey.
     * 
     * <p>
     * The hierarchical structure follows the pattern:
     * domain/operation/timestamp or kind/subdomain/timestamp
     * </p>
     * 
     * @param key the metric key
     * @param timestamp the timestamp for time-based hierarchy
     * @return the hierarchical storage key
     */
    public static String createHierarchicalStorageKey(MetricKey key, Instant timestamp) {
        try {
            // Extract domain and operation from labels if available
            String domain = extractDomain(key);
            String operation = extractOperation(key);
            
            if (domain != null && operation != null) {
                // Use domain/operation/timestamp structure
                return domain + KEY_SEPARATOR + operation + KEY_SEPARATOR + timestamp.toEpochMilli();
            } else {
                // Fallback to kind-based structure
                return key.kind() + KEY_SEPARATOR + "default" + KEY_SEPARATOR + timestamp.toEpochMilli();
            }
        } catch (Exception e) {
            logger.warn("Failed to create hierarchical storage key for MetricKey: {}", key.id(), e);
            // Fallback to simple key structure
            return "fallback" + KEY_SEPARATOR + key.kind() + KEY_SEPARATOR + timestamp.toEpochMilli();
        }
    }

    /**
     * Create a series ID from a MetricKey.
     * 
     * @param key the metric key
     * @return the series ID
     */
    public static String createSeriesId(MetricKey key) {
        return key.kind() + KEY_SEPARATOR + key.id();
    }

    /**
     * Create time-based hierarchical indexes for efficient time range queries.
     * 
     * @param key the metric key
     * @param timestamp the timestamp
     * @return list of time-based index keys
     */
    public static List<String> createTimeBasedIndexes(MetricKey key, Instant timestamp) {
        List<String> indexes = new ArrayList<>();
        
        try {
            String seriesId = createSeriesId(key);
            
            // Minute-level index
            Instant minuteBucket = timestamp.truncatedTo(ChronoUnit.MINUTES);
            indexes.add(TIME_INDEX_PREFIX + "minute:" + minuteBucket.toEpochMilli() + ":" + seriesId);
            
            // Hour-level index
            Instant hourBucket = timestamp.truncatedTo(ChronoUnit.HOURS);
            indexes.add(TIME_INDEX_PREFIX + "hour:" + hourBucket.toEpochMilli() + ":" + seriesId);
            
            // Day-level index
            Instant dayBucket = timestamp.truncatedTo(ChronoUnit.DAYS);
            indexes.add(TIME_INDEX_PREFIX + "day:" + dayBucket.toEpochMilli() + ":" + seriesId);
            
        } catch (Exception e) {
            logger.warn("Failed to create time-based indexes for MetricKey: {}", key.id(), e);
        }
        
        return indexes;
    }

    /**
     * Create domain-based hierarchical indexes.
     * 
     * @param key the metric key
     * @return list of domain-based index keys
     */
    public static List<String> createDomainIndexes(MetricKey key) {
        List<String> indexes = new ArrayList<>();
        
        try {
            String domain = extractDomain(key);
            if (domain != null) {
                String seriesId = createSeriesId(key);
                indexes.add(DOMAIN_INDEX_PREFIX + domain + ":" + seriesId);
                
                // Create sub-domain indexes if available
                String subDomain = extractSubDomain(key);
                if (subDomain != null) {
                    indexes.add(DOMAIN_INDEX_PREFIX + domain + KEY_SEPARATOR + subDomain + ":" + seriesId);
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to create domain indexes for MetricKey: {}", key.id(), e);
        }
        
        return indexes;
    }

    /**
     * Create operation-based hierarchical indexes.
     * 
     * @param key the metric key
     * @return list of operation-based index keys
     */
    public static List<String> createOperationIndexes(MetricKey key) {
        List<String> indexes = new ArrayList<>();
        
        try {
            String operation = extractOperation(key);
            if (operation != null) {
                String seriesId = createSeriesId(key);
                indexes.add(OPERATION_INDEX_PREFIX + operation + ":" + seriesId);
                
                // Create sub-operation indexes if available
                String subOperation = extractSubOperation(key);
                if (subOperation != null) {
                    indexes.add(OPERATION_INDEX_PREFIX + operation + KEY_SEPARATOR + subOperation + ":" + seriesId);
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to create operation indexes for MetricKey: {}", key.id(), e);
        }
        
        return indexes;
    }

    /**
     * Create capability-based indexes for multi-capability queries.
     * 
     * @param key the metric key
     * @return list of capability-based index keys
     */
    public static List<String> createCapabilityIndexes(MetricKey key) {
        List<String> indexes = new ArrayList<>();
        
        try {
            String seriesId = createSeriesId(key);
            Set<String> capabilities = key.capabilities();
            
            // Create individual capability indexes
            for (String capability : capabilities) {
                indexes.add(CAPABILITY_INDEX_PREFIX + capability + ":" + seriesId);
            }
            
            // Create combination indexes for common capability pairs
            List<String> capabilityList = new ArrayList<>(capabilities);
            for (int i = 0; i < capabilityList.size(); i++) {
                for (int j = i + 1; j < capabilityList.size(); j++) {
                    String combination = capabilityList.get(i) + CAPABILITY_SEPARATOR + capabilityList.get(j);
                    indexes.add(CAPABILITY_INDEX_PREFIX + "combination:" + combination + ":" + seriesId);
                }
            }
            
        } catch (Exception e) {
            logger.warn("Failed to create capability indexes for MetricKey: {}", key.id(), e);
        }
        
        return indexes;
    }

    /**
     * Create label-based indexes for common labels.
     * 
     * @param key the metric key
     * @return list of label-based index keys
     */
    public static List<String> createLabelIndexes(MetricKey key) {
        List<String> indexes = new ArrayList<>();
        
        try {
            String seriesId = createSeriesId(key);
            Map<String, String> labels = key.labels();
            
            // Create indexes for common label patterns
            for (Map.Entry<String, String> entry : labels.entrySet()) {
                String labelKey = entry.getKey();
                String labelValue = entry.getValue();
                
                // Skip high-cardinality values (IDs, timestamps, etc.)
                if (isHighCardinalityLabel(labelKey, labelValue)) {
                    continue;
                }
                
                indexes.add(LABEL_INDEX_PREFIX + labelKey + ":" + labelValue + ":" + seriesId);
            }
            
        } catch (Exception e) {
            logger.warn("Failed to create label indexes for MetricKey: {}", key.id(), e);
        }
        
        return indexes;
    }

    /**
     * Create all index keys for a MetricKey and timestamp.
     * 
     * @param key the metric key
     * @param timestamp the timestamp
     * @return set of all index keys
     */
    public static Set<String> createAllIndexes(MetricKey key, Instant timestamp) {
        Set<String> allIndexes = new HashSet<>();
        
        allIndexes.addAll(createTimeBasedIndexes(key, timestamp));
        allIndexes.addAll(createDomainIndexes(key));
        allIndexes.addAll(createOperationIndexes(key));
        allIndexes.addAll(createCapabilityIndexes(key));
        allIndexes.addAll(createLabelIndexes(key));
        
        return allIndexes;
    }

    /**
     * Extract domain from MetricKey labels.
     * 
     * @param key the metric key
     * @return the domain or null if not found
     */
    @Nullable
    private static String extractDomain(MetricKey key) {
        Map<String, String> labels = key.labels();
        
        // Try common domain label names
        String[] domainLabels = {"domain", "service", "component", "module"};
        for (String label : domainLabels) {
            String value = labels.get(label);
            if (value != null && !value.isEmpty()) {
                return value;
            }
        }
        
        // Try to extract from kind
        String kind = key.kind();
        if (kind.contains("-")) {
            return kind.split("-")[0];
        }
        
        return null;
    }

    /**
     * Extract operation from MetricKey labels.
     * 
     * @param key the metric key
     * @return the operation or null if not found
     */
    @Nullable
    private static String extractOperation(MetricKey key) {
        Map<String, String> labels = key.labels();
        
        // Try common operation label names
        String[] operationLabels = {"operation", "action", "method", "function"};
        for (String label : operationLabels) {
            String value = labels.get(label);
            if (value != null && !value.isEmpty()) {
                return value;
            }
        }
        
        // Try to extract from kind
        String kind = key.kind();
        if (kind.contains("-")) {
            String[] parts = kind.split("-");
            if (parts.length > 1) {
                return parts[1];
            }
        }
        
        return null;
    }

    /**
     * Extract sub-domain from MetricKey labels.
     * 
     * @param key the metric key
     * @return the sub-domain or null if not found
     */
    @Nullable
    private static String extractSubDomain(MetricKey key) {
        Map<String, String> labels = key.labels();
        
        String[] subDomainLabels = {"subdomain", "subservice", "subcomponent", "submodule"};
        for (String label : subDomainLabels) {
            String value = labels.get(label);
            if (value != null && !value.isEmpty()) {
                return value;
            }
        }
        
        return null;
    }

    /**
     * Extract sub-operation from MetricKey labels.
     * 
     * @param key the metric key
     * @return the sub-operation or null if not found
     */
    @Nullable
    private static String extractSubOperation(MetricKey key) {
        Map<String, String> labels = key.labels();
        
        String[] subOperationLabels = {"suboperation", "subaction", "submethod", "subfunction"};
        for (String label : subOperationLabels) {
            String value = labels.get(label);
            if (value != null && !value.isEmpty()) {
                return value;
            }
        }
        
        return null;
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
     * Validate and normalize a MetricKey.
     * 
     * @param key the metric key to validate
     * @return validation result with normalized key if valid
     */
    public static MetricKeyValidationResult validateAndNormalize(MetricKey key) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        try {
            // Validate kind
            if (key.kind() == null || key.kind().trim().isEmpty()) {
                errors.add("MetricKey kind cannot be null or empty");
            } else if (key.kind().length() > 100) {
                errors.add("MetricKey kind too long (max 100 characters)");
            }
            
            // Validate labels
            Map<String, String> labels = key.labels();
            if (labels == null) {
                errors.add("MetricKey labels cannot be null");
            } else {
                if (labels.size() > 20) {
                    warnings.add("MetricKey has many labels (" + labels.size() + "), consider reducing");
                }
                
                for (Map.Entry<String, String> entry : labels.entrySet()) {
                    String labelKey = entry.getKey();
                    String labelValue = entry.getValue();
                    
                    if (labelKey == null || labelKey.trim().isEmpty()) {
                        errors.add("Label key cannot be null or empty");
                    } else if (labelKey.length() > 50) {
                        errors.add("Label key too long: " + labelKey);
                    }
                    
                    if (labelValue == null) {
                        errors.add("Label value cannot be null for key: " + labelKey);
                    } else if (labelValue.length() > 200) {
                        warnings.add("Label value too long for key " + labelKey + ": " + labelValue.length() + " characters");
                    }
                }
            }
            
            // Validate capabilities
            Set<String> capabilities = key.capabilities();
            if (capabilities == null) {
                errors.add("MetricKey capabilities cannot be null");
            } else {
                if (capabilities.isEmpty()) {
                    warnings.add("MetricKey has no capabilities");
                } else if (capabilities.size() > 10) {
                    warnings.add("MetricKey has many capabilities (" + capabilities.size() + "), consider reducing");
                }
                
                for (String capability : capabilities) {
                    if (capability == null || capability.trim().isEmpty()) {
                        errors.add("Capability cannot be null or empty");
                    } else if (capability.length() > 50) {
                        errors.add("Capability too long: " + capability);
                    }
                }
            }
            
            // Create normalized key if validation passes
            MetricKey normalizedKey = key;
            if (errors.isEmpty()) {
                // Normalize by trimming and lowercasing where appropriate
                Map<String, String> normalizedLabels = new HashMap<>();
                if (labels != null) {
                    for (Map.Entry<String, String> entry : labels.entrySet()) {
                        String normalizedLabelKey = entry.getKey().trim();
                        String normalizedValue = entry.getValue().trim();
                        normalizedLabels.put(normalizedLabelKey, normalizedValue);
                    }
                }
                
                Set<String> normalizedCapabilities = new HashSet<>();
                if (capabilities != null) {
                    normalizedCapabilities = capabilities.stream()
                        .map(String::trim)
                        .collect(Collectors.toSet());
                }
                
                normalizedKey = new MetricKeys.SimpleMetricKey(
                    key.kind().trim(),
                    normalizedLabels,
                    normalizedCapabilities
                );
            }
            
            return new MetricKeyValidationResult(errors.isEmpty(), errors, warnings, normalizedKey);
            
        } catch (Exception e) {
            errors.add("Validation failed with exception: " + e.getMessage());
            return new MetricKeyValidationResult(false, errors, warnings, key);
        }
    }

    /**
     * Create key relationship tracking information.
     * 
     * @param key the metric key
     * @return key relationship information
     */
    public static MetricKeyRelationshipInfo createKeyRelationshipInfo(MetricKey key) {
        try {
            String domain = extractDomain(key);
            String operation = extractOperation(key);
            String subDomain = extractSubDomain(key);
            String subOperation = extractSubOperation(key);
            
            Set<String> relatedDomains = new HashSet<>();
            Set<String> relatedOperations = new HashSet<>();
            Set<String> parentKeys = new HashSet<>();
            Set<String> childKeys = new HashSet<>();
            
            // Determine parent-child relationships
            if (domain != null && operation != null) {
                // This is a specific operation within a domain
                parentKeys.add(domain + KEY_SEPARATOR + "all");
                parentKeys.add("all" + KEY_SEPARATOR + operation);
                
                if (subDomain != null) {
                    childKeys.add(domain + KEY_SEPARATOR + subDomain + KEY_SEPARATOR + operation);
                }
                if (subOperation != null) {
                    childKeys.add(domain + KEY_SEPARATOR + operation + KEY_SEPARATOR + subOperation);
                }
            }
            
            // Determine related domains and operations
            if (domain != null) {
                relatedDomains.add(domain);
                if (subDomain != null) {
                    relatedDomains.add(domain + KEY_SEPARATOR + subDomain);
                }
            }
            
            if (operation != null) {
                relatedOperations.add(operation);
                if (subOperation != null) {
                    relatedOperations.add(operation + KEY_SEPARATOR + subOperation);
                }
            }
            
            return new MetricKeyRelationshipInfo(
                key.id(),
                domain,
                operation,
                subDomain,
                subOperation,
                relatedDomains,
                relatedOperations,
                parentKeys,
                childKeys
            );
            
        } catch (Exception e) {
            logger.warn("Failed to create key relationship info for MetricKey: {}", key.id(), e);
            return new MetricKeyRelationshipInfo(key.id(), null, null, null, null, 
                Set.of(), Set.of(), Set.of(), Set.of());
        }
    }

    /**
     * Result of MetricKey validation.
     */
    public record MetricKeyValidationResult(
        boolean isValid,
        List<String> errors,
        List<String> warnings,
        MetricKey normalizedKey
    ) {}

    /**
     * Information about MetricKey relationships.
     */
    public record MetricKeyRelationshipInfo(
        String keyId,
        @Nullable String domain,
        @Nullable String operation,
        @Nullable String subDomain,
        @Nullable String subOperation,
        Set<String> relatedDomains,
        Set<String> relatedOperations,
        Set<String> parentKeys,
        Set<String> childKeys
    ) {}
}
