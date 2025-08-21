package org.openhab.core.ai.common.monitoring.api;

import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Base interface for all monitoring data in the openHAB AI system.
 * 
 * <p>
 * This interface provides a unified contract for all monitoring information,
 * including performance metrics, statistics, and health data. It ensures
 * consistent behavior across different monitoring domains while maintaining
 * domain-specific organization.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface Monitoring {

    /**
     * Get the unique identifier for this monitoring data.
     * 
     * @return the unique identifier
     */
    String getId();

    /**
     * Get the timestamp when this monitoring data was created or collected.
     * 
     * @return the timestamp
     */
    Instant getTimestamp();

    /**
     * Get the type of monitoring data.
     * 
     * @return the monitoring type
     */
    MonitoringType getType();

    /**
     * Get the raw data as a map of key-value pairs.
     * 
     * @return the raw data map, or null if no raw data is available
     */
    @Nullable
    Map<String, Object> getData();

    /**
     * Get the domain this monitoring data belongs to.
     * 
     * @return the domain name (e.g., "tool", "agent", "reasoning")
     */
    String getDomain();

    /**
     * Get the source component that generated this monitoring data.
     * 
     * @return the source component name, or null if not specified
     */
    @Nullable
    String getSource();

    /**
     * Check if this monitoring data is valid.
     * 
     * @return true if the data is valid, false otherwise
     */
    default boolean isValid() {
        return getId() != null && !getId().isBlank() && getTimestamp() != null && getType() != null;
    }

    /**
     * Get a human-readable description of this monitoring data.
     * 
     * @return the description, or null if not available
     */
    @Nullable
    default String getDescription() {
        return null;
    }
}
