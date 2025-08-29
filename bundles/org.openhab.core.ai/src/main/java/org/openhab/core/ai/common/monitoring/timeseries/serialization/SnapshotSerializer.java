package org.openhab.core.ai.common.monitoring.timeseries.serialization;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.MetricsSnapshot;

/**
 * Interface for serializing and deserializing MetricsSnapshot objects to/from storage format.
 * 
 * <p>
 * This interface provides type-safe serialization capabilities for storing any MetricsSnapshot
 * type in time series storage without requiring conversion to a generic format. It supports
 * both serialization to storage format and deserialization back to the original snapshot type.
 * </p>
 * 
 * <h3>Key Features</h3>
 * <ul>
 *   <li>Type-safe serialization and deserialization</li>
 *   <li>Support for any MetricsSnapshot implementation</li>
 *   <li>Snapshot type registry for deserialization</li>
 *   <li>Validation and integrity checking</li>
 *   <li>Versioning support for future compatibility</li>
 * </ul>
 * 
 * <h3>Usage Example</h3>
 * <pre>{@code
 * // Serialize a snapshot
 * String serialized = serializer.serialize(snapshot);
 * 
 * // Deserialize back to original type
 * ExecutionMetricsSnapshot restored = serializer.deserialize(
 *     serialized, "ExecutionMetricsSnapshot", ExecutionMetricsSnapshot.class);
 * }</pre>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface SnapshotSerializer {
    
    /**
     * Serialize any MetricsSnapshot to storage format.
     * 
     * @param snapshot the snapshot to serialize
     * @return serialized snapshot data as string
     * @throws IllegalArgumentException if snapshot is null or unsupported
     * @throws RuntimeException if serialization fails
     */
    String serialize(MetricsSnapshot snapshot);
    
    /**
     * Deserialize snapshot data back to original type.
     * 
     * @param <T> the target snapshot type
     * @param snapshotData the serialized snapshot data
     * @param snapshotType the type of snapshot to deserialize
     * @param targetType the target class type
     * @return deserialized snapshot of the specified type
     * @throws IllegalArgumentException if parameters are null or invalid
     * @throws RuntimeException if deserialization fails
     */
    <T extends MetricsSnapshot> T deserialize(String snapshotData, String snapshotType, Class<T> targetType);
    
    /**
     * Get supported snapshot types.
     * 
     * @return set of supported snapshot type names
     */
    Set<String> getSupportedTypes();
    
    /**
     * Check if a snapshot type is supported.
     * 
     * @param snapshotType the snapshot type to check
     * @return true if the type is supported, false otherwise
     */
    boolean isSupported(String snapshotType);
    
    /**
     * Validate serialized snapshot data.
     * 
     * @param snapshotData the serialized data to validate
     * @return true if the data is valid, false otherwise
     */
    boolean validate(String snapshotData);
    
    /**
     * Get the version of the serialization format.
     * 
     * @return the version string
     */
    String getVersion();
}
