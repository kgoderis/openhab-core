package org.openhab.core.ai.common.security;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.auth.AuthenticationContext;
import org.openhab.core.ai.security.config.SecurityConfiguration;
import org.openhab.core.ai.tool.security.filters.SecurityResult;

/**
 * Common Security Manager Interface
 * 
 * <p>
 * This interface defines the core security operations that are common across
 * all security manager implementations (general, agent, reasoning, tool).
 * It provides a unified contract for basic security functionality while
 * allowing domain-specific implementations to extend with additional features.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public interface SecurityManager {

    /**
     * Check if security is enabled for this manager.
     *
     * @return true if security is enabled
     */
    boolean isEnabled();

    /**
     * Check if access to a component or resource is allowed.
     *
     * @param componentId the component ID
     * @param userId the user ID (may be null for system operations)
     * @return true if access is allowed
     */
    boolean canAccess(String componentId, @Nullable String userId);

    /**
     * Validate security for a specific action.
     * 
     * @param action Action to validate
     * @param resource Resource being accessed
     * @param context Authentication context
     * @return Security result
     */
    SecurityResult validate(String action, String resource, AuthenticationContext context);

    /**
     * Log a security violation or incident.
     *
     * @param componentId the component ID where the violation occurred
     * @param violation the violation description
     * @param context additional context information (may be null)
     */
    void logViolation(String componentId, String violation, @Nullable Map<String, Object> context);

    // Eliminated getStatistics() method - consumers should access statistics directly via MetricsService
    // Use: metricsService.getStatistics(MetricKeys.custom("security-monitoring", ...),
    // SecurityMonitoringStatistics.class, duration)

    /**
     * Get the security manager type.
     *
     * @return the security manager type
     */
    SecurityManagerType getType();

    /**
     * Get security configuration.
     * 
     * @return Security configuration
     */
    SecurityConfiguration getConfig();

    /**
     * Update security configuration.
     * 
     * @param configuration New security configuration
     */
    void updateConfig(SecurityConfiguration configuration);

    /**
     * Security manager types.
     */
    enum SecurityManagerType {
        GENERAL,
        AGENT,
        REASONING,
        TOOL,
        MESSAGE
    }
}
