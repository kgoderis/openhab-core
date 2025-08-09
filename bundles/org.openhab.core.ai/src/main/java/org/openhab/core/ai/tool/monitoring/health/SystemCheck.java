package org.openhab.core.ai.tool.monitoring.health;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Health check interface for monitoring tool system health.
 * 
 * This interface defines the contract for health checks that can monitor
 * the health and status of various tool system components.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface SystemCheck {

    /**
     * Get the health check ID.
     * 
     * @return the health check ID
     */
    String getHealthCheckId();

    /**
     * Get the health check name.
     * 
     * @return the health check name
     */
    String getHealthCheckName();

    /**
     * Get the health check description.
     * 
     * @return the health check description
     */
    String getHealthCheckDescription();

    /**
     * Get the health check category.
     * 
     * @return the health check category
     */
    String getCategory();

    /**
     * Get the health check priority.
     * 
     * @return the health check priority (higher values = higher priority)
     */
    int getPriority();

    /**
     * Check if the health check is enabled.
     * 
     * @return true if the health check is enabled
     */
    boolean isEnabled();

    /**
     * Perform the health check.
     * 
     * @return health check result
     */
    SystemCheckResult performCheck();

    /**
     * Get the health check configuration.
     * 
     * @return the health check configuration
     */
    Map<String, Object> getConfiguration();

    /**
     * Update the health check configuration.
     * 
     * @param configuration the new configuration
     */
    void updateConfiguration(Map<String, Object> configuration);

    // TODO: Implement health check logic
    // TODO: Add support for health check dependencies
    // TODO: Implement health check performance monitoring
    // TODO: Add support for health check versioning
}
