package org.openhab.core.ai.common.monitoring.timeseries.serialization;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.MetricsSnapshot;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * JSON-based implementation of SnapshotSerializer for MetricsSnapshot objects.
 * 
 * <p>
 * This implementation provides JSON serialization and deserialization for any MetricsSnapshot
 * type. It uses Jackson ObjectMapper for JSON processing and reflection for extracting
 * snapshot data. The serializer maintains a registry of supported snapshot types and
 * provides validation and integrity checking.
 * </p>
 * 
 * <h3>Features</h3>
 * <ul>
 *   <li>JSON-based serialization for human readability</li>
 *   <li>Automatic type registry management</li>
 *   <li>Reflection-based data extraction</li>
 *   <li>Validation and integrity checking</li>
 *   <li>Versioning support</li>
 *   <li>Error handling and logging</li>
 * </ul>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@Component(service = SnapshotSerializer.class)
@NonNullByDefault
public class JsonSnapshotSerializer implements SnapshotSerializer {
    
    private static final Logger logger = LoggerFactory.getLogger(JsonSnapshotSerializer.class);
    private static final String VERSION = "1.0.0";
    
    private final ObjectMapper objectMapper;
    private final Map<String, Class<? extends MetricsSnapshot>> typeRegistry = new ConcurrentHashMap<>();
    
    public JsonSnapshotSerializer() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        this.objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        
        // Register common snapshot types
        registerCommonTypes();
    }
    
    @Override
    public String serialize(MetricsSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot must not be null");
        
        try {
            String snapshotType = snapshot.getClass().getName();
            
            // Register the type if not already registered
            if (!typeRegistry.containsKey(snapshotType)) {
                Class<? extends MetricsSnapshot> clazz = (Class<? extends MetricsSnapshot>) snapshot.getClass();
                typeRegistry.put(snapshotType, clazz);
            }
            
            Map<String, Object> data = new HashMap<>();
            data.put("version", VERSION);
            data.put("type", snapshotType);
            data.put("timestamp", System.currentTimeMillis());
            data.put("data", extractSnapshotData(snapshot));
            
            return objectMapper.writeValueAsString(data);
            
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize snapshot of type {}: {}", 
                        snapshot.getClass().getSimpleName(), e.getMessage());
            throw new RuntimeException("Failed to serialize snapshot", e);
        }
    }
    
    @Override
    public <T extends MetricsSnapshot> T deserialize(String snapshotData, String snapshotType, Class<T> targetType) {
        Objects.requireNonNull(snapshotData, "snapshotData must not be null");
        Objects.requireNonNull(snapshotType, "snapshotType must not be null");
        Objects.requireNonNull(targetType, "targetType must not be null");
        
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = objectMapper.readValue(snapshotData, Map.class);
            
            // Validate version compatibility
            String version = (String) data.get("version");
            if (!VERSION.equals(version)) {
                logger.warn("Version mismatch: expected {}, got {}", VERSION, version);
            }
            
            // Validate type
            String actualType = (String) data.get("type");
            if (!snapshotType.equals(actualType)) {
                throw new IllegalArgumentException("Type mismatch: expected " + snapshotType + ", got " + actualType);
            }
            
            @SuppressWarnings("unchecked")
            Map<String, Object> snapshotDataMap = (Map<String, Object>) data.get("data");
            
            return reconstructSnapshot(snapshotDataMap, targetType);
            
        } catch (JsonProcessingException e) {
            logger.error("Failed to deserialize snapshot of type {}: {}", snapshotType, e.getMessage());
            throw new RuntimeException("Failed to deserialize snapshot", e);
        }
    }
    
    @Override
    public Set<String> getSupportedTypes() {
        return new HashSet<>(typeRegistry.keySet());
    }
    
    @Override
    public boolean isSupported(String snapshotType) {
        return typeRegistry.containsKey(snapshotType);
    }
    
    @Override
    public boolean validate(String snapshotData) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = objectMapper.readValue(snapshotData, Map.class);
            
            // Check required fields
            if (!data.containsKey("version") || !data.containsKey("type") || !data.containsKey("data")) {
                return false;
            }
            
            // Check if type is supported
            String type = (String) data.get("type");
            return typeRegistry.containsKey(type);
            
        } catch (Exception e) {
            logger.debug("Validation failed for snapshot data: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public String getVersion() {
        return VERSION;
    }
    
    /**
     * Extract data from a MetricsSnapshot using reflection.
     * 
     * @param snapshot the snapshot to extract data from
     * @return map containing the extracted data
     */
    private Map<String, Object> extractSnapshotData(MetricsSnapshot snapshot) {
        Map<String, Object> data = new HashMap<>();
        
        try {
            // Extract common snapshot data using reflection
            Class<?> clazz = snapshot.getClass();
            
            // Try to get timestamp
            try {
                Method timestampMethod = clazz.getMethod("timestampMs");
                Object timestamp = timestampMethod.invoke(snapshot);
                if (timestamp != null) {
                    data.put("timestampMs", timestamp);
                }
            } catch (Exception e) {
                logger.debug("Could not extract timestamp from snapshot: {}", e.getMessage());
            }
            
            // Try to get domain
            try {
                Method domainMethod = clazz.getMethod("getDomain");
                Object domain = domainMethod.invoke(snapshot);
                if (domain != null) {
                    data.put("domain", domain);
                }
            } catch (Exception e) {
                logger.debug("Could not extract domain from snapshot: {}", e.getMessage());
            }
            
            // Try to get operation
            try {
                Method operationMethod = clazz.getMethod("getOperation");
                Object operation = operationMethod.invoke(snapshot);
                if (operation != null) {
                    data.put("operation", operation);
                }
            } catch (Exception e) {
                logger.debug("Could not extract operation from snapshot: {}", e.getMessage());
            }
            
            // Extract type-specific data based on common interfaces
            extractInterfaceData(snapshot, data);
            
            // Extract all public getter methods
            extractGetterMethods(snapshot, data);
            
        } catch (Exception e) {
            logger.warn("Failed to extract data from snapshot: {}", e.getMessage());
        }
        
        return data;
    }
    
    /**
     * Extract data from common snapshot interfaces.
     * 
     * @param snapshot the snapshot to extract from
     * @param data the data map to populate
     */
    private void extractInterfaceData(MetricsSnapshot snapshot, Map<String, Object> data) {
        // Extract CountsMetrics data
        try {
            Class<?> countsInterface = Class.forName("org.openhab.core.ai.common.monitoring.api.CountsMetrics");
            if (countsInterface.isInstance(snapshot)) {
                Method totalMethod = countsInterface.getMethod("total");
                Method successMethod = countsInterface.getMethod("success");
                Method failureMethod = countsInterface.getMethod("failure");
                
                data.put("total", totalMethod.invoke(snapshot));
                data.put("success", successMethod.invoke(snapshot));
                data.put("failure", failureMethod.invoke(snapshot));
            }
        } catch (Exception e) {
            logger.debug("Could not extract CountsMetrics data: {}", e.getMessage());
        }
        
        // Extract LatencyMetrics data
        try {
            Class<?> latencyInterface = Class.forName("org.openhab.core.ai.common.monitoring.api.LatencyMetrics");
            if (latencyInterface.isInstance(snapshot)) {
                Method totalDurationMethod = latencyInterface.getMethod("totalDurationNanos");
                Method avgDurationMethod = latencyInterface.getMethod("avgDurationMs");
                
                data.put("totalDurationNanos", totalDurationMethod.invoke(snapshot));
                data.put("avgDurationMs", avgDurationMethod.invoke(snapshot));
            }
        } catch (Exception e) {
            logger.debug("Could not extract LatencyMetrics data: {}", e.getMessage());
        }
    }
    
    /**
     * Extract data from all public getter methods.
     * 
     * @param snapshot the snapshot to extract from
     * @param data the data map to populate
     */
    private void extractGetterMethods(MetricsSnapshot snapshot, Map<String, Object> data) {
        Class<?> clazz = snapshot.getClass();
        Method[] methods = clazz.getMethods();
        
        for (Method method : methods) {
            String methodName = method.getName();
            
            // Check if it's a getter method
            if (methodName.startsWith("get") && methodName.length() > 3 && 
                method.getParameterCount() == 0 && !methodName.equals("getClass")) {
                
                try {
                    String fieldName = Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
                    Object value = method.invoke(snapshot);
                    
                    // Only add non-null values and avoid duplicates
                    if (value != null && !data.containsKey(fieldName)) {
                        data.put(fieldName, value);
                    }
                } catch (Exception e) {
                    logger.debug("Could not invoke getter method {}: {}", methodName, e.getMessage());
                }
            }
        }
    }
    
    /**
     * Reconstruct a snapshot from serialized data.
     * 
     * @param <T> the target snapshot type
     * @param data the serialized data
     * @param targetType the target class type
     * @return reconstructed snapshot
     */
    private <T extends MetricsSnapshot> T reconstructSnapshot(Map<String, Object> data, Class<T> targetType) {
        // For now, this is a simplified implementation
        // In a full implementation, this would use reflection or builder patterns
        // to reconstruct the snapshot from the serialized data
        
        logger.warn("Snapshot reconstruction not fully implemented for type: {}", targetType.getSimpleName());
        throw new UnsupportedOperationException("Snapshot reconstruction not yet fully implemented for type: " + targetType.getSimpleName());
    }
    
    /**
     * Register common snapshot types.
     */
    private void registerCommonTypes() {
        // Register common snapshot types that are likely to be used
        try {
            // Try to register common types if they exist
            String[] commonTypes = {
                "org.openhab.core.ai.common.monitoring.snapshot.ExecutionMetricsSnapshot",
                "org.openhab.core.ai.common.monitoring.snapshot.ProviderHealthSnapshot",
                "org.openhab.core.ai.common.monitoring.snapshot.ToolMetricsSnapshot"
            };
            
            for (String typeName : commonTypes) {
                try {
                    @SuppressWarnings("unchecked")
                    Class<? extends MetricsSnapshot> clazz = (Class<? extends MetricsSnapshot>) Class.forName(typeName);
                    typeRegistry.put(typeName, clazz);
                    logger.debug("Registered snapshot type: {}", typeName);
                } catch (ClassNotFoundException e) {
                    logger.debug("Common snapshot type not found: {}", typeName);
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to register common snapshot types: {}", e.getMessage());
        }
    }
}
