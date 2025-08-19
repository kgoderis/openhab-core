package org.openhab.core.ai.security;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.auth.AuthenticationContext;
import org.openhab.core.ai.common.security.SecurityStatistics;
import org.openhab.core.ai.tool.security.filters.SecurityResult;

/**
 * Unified security manager interface for AI services.
 * 
 * This interface provides a unified contract for security operations across
 * different AI components (tools, agents, messages) while maintaining
 * component-specific security requirements.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface SecurityManager {

    /**
     * Check if an operation is allowed.
     * 
     * @param context the authentication context
     * @return security result
     */
    SecurityResult validateOperation(AuthenticationContext context);

    /**
     * Check if execution is allowed for a component.
     * 
     * @param componentId the component ID
     * @param context the execution context
     * @return true if execution is allowed
     */
    boolean isExecutionAllowed(String componentId, String context);

    /**
     * Validate access to a component.
     * 
     * @param componentId the component ID
     * @param userId the user ID
     * @return true if access is valid
     */
    boolean validateAccess(String componentId, @Nullable String userId);

    /**
     * Log a security violation.
     * 
     * @param componentId the component ID
     * @param violation the violation description
     * @param context additional context
     */
    void logSecurityViolation(String componentId, String violation, @Nullable Map<String, Object> context);

    /**
     * Get security statistics.
     * 
     * @return security statistics
     */
    SecurityStatistics getSecurityStatistics();
}
