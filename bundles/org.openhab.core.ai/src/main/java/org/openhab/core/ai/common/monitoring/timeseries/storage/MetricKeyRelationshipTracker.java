package org.openhab.core.ai.common.monitoring.timeseries.storage;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.MetricKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tracker for MetricKey relationships and dependencies.
 * 
 * <p>
 * This class provides utilities for tracking relationships between MetricKey instances,
 * managing dependencies, and enabling efficient querying based on key relationships.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class MetricKeyRelationshipTracker {

    private static final Logger logger = LoggerFactory.getLogger(MetricKeyRelationshipTracker.class);

    // Relationship types
    private static final String PARENT_CHILD_RELATIONSHIP = "parent-child";
    private static final String DOMAIN_RELATIONSHIP = "domain";
    private static final String OPERATION_RELATIONSHIP = "operation";
    private static final String CAPABILITY_RELATIONSHIP = "capability";
    private static final String KIND_RELATIONSHIP = "kind";
    private static final String TEMPORAL_RELATIONSHIP = "temporal";

    // In-memory relationship storage
    private static final Map<String, Set<String>> parentChildRelationships = new ConcurrentHashMap<>();
    private static final Map<String, Set<String>> domainRelationships = new ConcurrentHashMap<>();
    private static final Map<String, Set<String>> operationRelationships = new ConcurrentHashMap<>();
    private static final Map<String, Set<String>> capabilityRelationships = new ConcurrentHashMap<>();
    private static final Map<String, Set<String>> kindRelationships = new ConcurrentHashMap<>();
    private static final Map<String, List<TemporalRelationship>> temporalRelationships = new ConcurrentHashMap<>();

    private MetricKeyRelationshipTracker() {
        // Utility class - prevent instantiation
    }

    /**
     * Register a MetricKey and its relationships.
     * 
     * @param key the metric key to register
     * @param timestamp the timestamp when the key was created
     */
    public static void registerMetricKey(MetricKey key, Instant timestamp) {
        try {
            String keyId = key.id();
            MetricKeyStorageHandler.MetricKeyRelationshipInfo relationshipInfo = 
                MetricKeyStorageHandler.createKeyRelationshipInfo(key);
            
            // Register parent-child relationships
            registerParentChildRelationships(keyId, relationshipInfo);
            
            // Register domain relationships
            registerDomainRelationships(keyId, relationshipInfo);
            
            // Register operation relationships
            registerOperationRelationships(keyId, relationshipInfo);
            
            // Register capability relationships
            registerCapabilityRelationships(keyId, key.capabilities());
            
            // Register kind relationships
            registerKindRelationships(keyId, key.kind());
            
            // Register temporal relationships
            registerTemporalRelationships(keyId, timestamp);
            
        } catch (Exception e) {
            logger.warn("Failed to register MetricKey relationships: {}", key.id(), e);
        }
    }

    /**
     * Unregister a MetricKey and clean up its relationships.
     * 
     * @param key the metric key to unregister
     */
    public static void unregisterMetricKey(MetricKey key) {
        try {
            String keyId = key.id();
            
            // Remove from all relationship maps
            parentChildRelationships.remove(keyId);
            domainRelationships.remove(keyId);
            operationRelationships.remove(keyId);
            capabilityRelationships.remove(keyId);
            kindRelationships.remove(keyId);
            temporalRelationships.remove(keyId);
            
            // Clean up references to this key in other relationships
            cleanupRelationshipReferences(keyId);
            
        } catch (Exception e) {
            logger.warn("Failed to unregister MetricKey relationships: {}", key.id(), e);
        }
    }

    /**
     * Get all related MetricKey IDs for a given key.
     * 
     * @param key the metric key
     * @param relationshipTypes the types of relationships to include
     * @return set of related key IDs
     */
    public static Set<String> getRelatedKeyIds(MetricKey key, Set<String> relationshipTypes) {
        Set<String> relatedIds = new HashSet<>();
        String keyId = key.id();
        
        try {
            if (relationshipTypes.contains(PARENT_CHILD_RELATIONSHIP)) {
                relatedIds.addAll(getParentChildRelatedIds(keyId));
            }
            
            if (relationshipTypes.contains(DOMAIN_RELATIONSHIP)) {
                relatedIds.addAll(getDomainRelatedIds(keyId));
            }
            
            if (relationshipTypes.contains(OPERATION_RELATIONSHIP)) {
                relatedIds.addAll(getOperationRelatedIds(keyId));
            }
            
            if (relationshipTypes.contains(CAPABILITY_RELATIONSHIP)) {
                relatedIds.addAll(getCapabilityRelatedIds(keyId));
            }
            
            if (relationshipTypes.contains(KIND_RELATIONSHIP)) {
                relatedIds.addAll(getKindRelatedIds(keyId));
            }
            
            if (relationshipTypes.contains(TEMPORAL_RELATIONSHIP)) {
                relatedIds.addAll(getTemporalRelatedIds(keyId));
            }
            
        } catch (Exception e) {
            logger.warn("Failed to get related key IDs for MetricKey: {}", keyId, e);
        }
        
        return relatedIds;
    }

    /**
     * Get all related MetricKey IDs for a given key (all relationship types).
     * 
     * @param key the metric key
     * @return set of all related key IDs
     */
    public static Set<String> getAllRelatedKeyIds(MetricKey key) {
        Set<String> allRelationshipTypes = Set.of(
            PARENT_CHILD_RELATIONSHIP,
            DOMAIN_RELATIONSHIP,
            OPERATION_RELATIONSHIP,
            CAPABILITY_RELATIONSHIP,
            KIND_RELATIONSHIP,
            TEMPORAL_RELATIONSHIP
        );
        
        return getRelatedKeyIds(key, allRelationshipTypes);
    }

    /**
     * Get parent-child related key IDs.
     * 
     * @param keyId the key ID
     * @return set of parent-child related key IDs
     */
    private static Set<String> getParentChildRelatedIds(String keyId) {
        Set<String> relatedIds = new HashSet<>();
        
        // Get children
        Set<String> children = parentChildRelationships.get(keyId);
        if (children != null) {
            relatedIds.addAll(children);
        }
        
        // Get parents
        for (Map.Entry<String, Set<String>> entry : parentChildRelationships.entrySet()) {
            if (entry.getValue().contains(keyId)) {
                relatedIds.add(entry.getKey());
            }
        }
        
        return relatedIds;
    }

    /**
     * Get domain-related key IDs.
     * 
     * @param keyId the key ID
     * @return set of domain-related key IDs
     */
    private static Set<String> getDomainRelatedIds(String keyId) {
        Set<String> relatedIds = new HashSet<>();
        
        // Get keys in the same domain
        Set<String> domainKeys = domainRelationships.get(keyId);
        if (domainKeys != null) {
            relatedIds.addAll(domainKeys);
        }
        
        return relatedIds;
    }

    /**
     * Get operation-related key IDs.
     * 
     * @param keyId the key ID
     * @return set of operation-related key IDs
     */
    private static Set<String> getOperationRelatedIds(String keyId) {
        Set<String> relatedIds = new HashSet<>();
        
        // Get keys with the same operation
        Set<String> operationKeys = operationRelationships.get(keyId);
        if (operationKeys != null) {
            relatedIds.addAll(operationKeys);
        }
        
        return relatedIds;
    }

    /**
     * Get capability-related key IDs.
     * 
     * @param keyId the key ID
     * @return set of capability-related key IDs
     */
    private static Set<String> getCapabilityRelatedIds(String keyId) {
        Set<String> relatedIds = new HashSet<>();
        
        // Get keys with shared capabilities
        Set<String> capabilityKeys = capabilityRelationships.get(keyId);
        if (capabilityKeys != null) {
            relatedIds.addAll(capabilityKeys);
        }
        
        return relatedIds;
    }

    /**
     * Get kind-related key IDs.
     * 
     * @param keyId the key ID
     * @return set of kind-related key IDs
     */
    private static Set<String> getKindRelatedIds(String keyId) {
        Set<String> relatedIds = new HashSet<>();
        
        // Get keys of the same kind
        Set<String> kindKeys = kindRelationships.get(keyId);
        if (kindKeys != null) {
            relatedIds.addAll(kindKeys);
        }
        
        return relatedIds;
    }

    /**
     * Get temporal-related key IDs.
     * 
     * @param keyId the key ID
     * @return set of temporal-related key IDs
     */
    private static Set<String> getTemporalRelatedIds(String keyId) {
        Set<String> relatedIds = new HashSet<>();
        
        // Get keys created around the same time
        List<TemporalRelationship> temporalRels = temporalRelationships.get(keyId);
        if (temporalRels != null) {
            for (TemporalRelationship rel : temporalRels) {
                relatedIds.add(rel.relatedKeyId());
            }
        }
        
        return relatedIds;
    }

    /**
     * Register parent-child relationships.
     * 
     * @param keyId the key ID
     * @param relationshipInfo the relationship information
     */
    private static void registerParentChildRelationships(String keyId, 
                                                        MetricKeyStorageHandler.MetricKeyRelationshipInfo relationshipInfo) {
        Set<String> children = new HashSet<>(relationshipInfo.childKeys());
        if (!children.isEmpty()) {
            parentChildRelationships.put(keyId, children);
        }
    }

    /**
     * Register domain relationships.
     * 
     * @param keyId the key ID
     * @param relationshipInfo the relationship information
     */
    private static void registerDomainRelationships(String keyId, 
                                                   MetricKeyStorageHandler.MetricKeyRelationshipInfo relationshipInfo) {
        if (relationshipInfo.domain() != null) {
            String domain = relationshipInfo.domain();
            Set<String> domainKeys = domainRelationships.computeIfAbsent(domain, k -> new HashSet<>());
            domainKeys.add(keyId);
        }
    }

    /**
     * Register operation relationships.
     * 
     * @param keyId the key ID
     * @param relationshipInfo the relationship information
     */
    private static void registerOperationRelationships(String keyId, 
                                                      MetricKeyStorageHandler.MetricKeyRelationshipInfo relationshipInfo) {
        if (relationshipInfo.operation() != null) {
            String operation = relationshipInfo.operation();
            Set<String> operationKeys = operationRelationships.computeIfAbsent(operation, k -> new HashSet<>());
            operationKeys.add(keyId);
        }
    }

    /**
     * Register capability relationships.
     * 
     * @param keyId the key ID
     * @param capabilities the capabilities
     */
    private static void registerCapabilityRelationships(String keyId, Set<String> capabilities) {
        for (String capability : capabilities) {
            Set<String> capabilityKeys = capabilityRelationships.computeIfAbsent(capability, k -> new HashSet<>());
            capabilityKeys.add(keyId);
        }
    }

    /**
     * Register kind relationships.
     * 
     * @param keyId the key ID
     * @param kind the kind
     */
    private static void registerKindRelationships(String keyId, String kind) {
        if (kind != null && !kind.isEmpty()) {
            Set<String> kindKeys = kindRelationships.computeIfAbsent(kind, k -> new HashSet<>());
            kindKeys.add(keyId);
        }
    }

    /**
     * Register temporal relationships.
     * 
     * @param keyId the key ID
     * @param timestamp the timestamp
     */
    private static void registerTemporalRelationships(String keyId, Instant timestamp) {
        // Find keys created within a time window (e.g., 1 hour)
        Instant windowStart = timestamp.minusSeconds(3600); // 1 hour ago
        Instant windowEnd = timestamp.plusSeconds(3600); // 1 hour from now
        
        List<TemporalRelationship> temporalRels = new ArrayList<>();
        
        for (Map.Entry<String, List<TemporalRelationship>> entry : temporalRelationships.entrySet()) {
            String otherKeyId = entry.getKey();
            if (!otherKeyId.equals(keyId)) {
                // Check if this key was created within the time window
                for (TemporalRelationship rel : entry.getValue()) {
                    if (rel.timestamp().isAfter(windowStart) && rel.timestamp().isBefore(windowEnd)) {
                        temporalRels.add(new TemporalRelationship(otherKeyId, rel.timestamp(), 
                            "temporal-proximity"));
                    }
                }
            }
        }
        
        if (!temporalRels.isEmpty()) {
            temporalRelationships.put(keyId, temporalRels);
        }
    }

    /**
     * Clean up references to a key in all relationship maps.
     * 
     * @param keyId the key ID to clean up
     */
    private static void cleanupRelationshipReferences(String keyId) {
        // Clean up domain relationships
        for (Set<String> domainKeys : domainRelationships.values()) {
            domainKeys.remove(keyId);
        }
        
        // Clean up operation relationships
        for (Set<String> operationKeys : operationRelationships.values()) {
            operationKeys.remove(keyId);
        }
        
        // Clean up capability relationships
        for (Set<String> capabilityKeys : capabilityRelationships.values()) {
            capabilityKeys.remove(keyId);
        }
        
        // Clean up kind relationships
        for (Set<String> kindKeys : kindRelationships.values()) {
            kindKeys.remove(keyId);
        }
        
        // Clean up temporal relationships
        for (List<TemporalRelationship> temporalRels : temporalRelationships.values()) {
            temporalRels.removeIf(rel -> rel.relatedKeyId().equals(keyId));
        }
    }

    /**
     * Get relationship statistics.
     * 
     * @return relationship statistics
     */
    public static RelationshipStatistics getRelationshipStatistics() {
        int totalKeys = parentChildRelationships.size() + domainRelationships.size() + 
                       operationRelationships.size() + capabilityRelationships.size() + 
                       kindRelationships.size() + temporalRelationships.size();
        
        int totalRelationships = parentChildRelationships.values().stream().mapToInt(Set::size).sum() +
                               domainRelationships.values().stream().mapToInt(Set::size).sum() +
                               operationRelationships.values().stream().mapToInt(Set::size).sum() +
                               capabilityRelationships.values().stream().mapToInt(Set::size).sum() +
                               kindRelationships.values().stream().mapToInt(Set::size).sum() +
                               temporalRelationships.values().stream().mapToInt(List::size).sum();
        
        return new RelationshipStatistics(
            totalKeys,
            totalRelationships,
            parentChildRelationships.size(),
            domainRelationships.size(),
            operationRelationships.size(),
            capabilityRelationships.size(),
            kindRelationships.size(),
            temporalRelationships.size()
        );
    }

    /**
     * Clear all relationship data.
     */
    public static void clearAllRelationships() {
        parentChildRelationships.clear();
        domainRelationships.clear();
        operationRelationships.clear();
        capabilityRelationships.clear();
        kindRelationships.clear();
        temporalRelationships.clear();
    }

    /**
     * Export relationship data for persistence.
     * 
     * @return relationship data export
     */
    public static RelationshipDataExport exportRelationshipData() {
        return new RelationshipDataExport(
            new HashMap<>(parentChildRelationships),
            new HashMap<>(domainRelationships),
            new HashMap<>(operationRelationships),
            new HashMap<>(capabilityRelationships),
            new HashMap<>(kindRelationships),
            new HashMap<>(temporalRelationships)
        );
    }

    /**
     * Import relationship data from persistence.
     * 
     * @param export the relationship data export
     */
    public static void importRelationshipData(RelationshipDataExport export) {
        parentChildRelationships.clear();
        parentChildRelationships.putAll(export.parentChildRelationships());
        
        domainRelationships.clear();
        domainRelationships.putAll(export.domainRelationships());
        
        operationRelationships.clear();
        operationRelationships.putAll(export.operationRelationships());
        
        capabilityRelationships.clear();
        capabilityRelationships.putAll(export.capabilityRelationships());
        
        kindRelationships.clear();
        kindRelationships.putAll(export.kindRelationships());
        
        temporalRelationships.clear();
        temporalRelationships.putAll(export.temporalRelationships());
    }

    /**
     * Temporal relationship record.
     */
    public record TemporalRelationship(
        String relatedKeyId,
        Instant timestamp,
        String relationshipType
    ) {}

    /**
     * Relationship statistics record.
     */
    public record RelationshipStatistics(
        int totalKeys,
        int totalRelationships,
        int parentChildRelationships,
        int domainRelationships,
        int operationRelationships,
        int capabilityRelationships,
        int kindRelationships,
        int temporalRelationships
    ) {}

    /**
     * Relationship data export record.
     */
    public record RelationshipDataExport(
        Map<String, Set<String>> parentChildRelationships,
        Map<String, Set<String>> domainRelationships,
        Map<String, Set<String>> operationRelationships,
        Map<String, Set<String>> capabilityRelationships,
        Map<String, Set<String>> kindRelationships,
        Map<String, List<TemporalRelationship>> temporalRelationships
    ) {}
}
