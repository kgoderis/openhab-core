package org.openhab.core.ai.tool.security.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.security.SecurityManager;
import org.openhab.core.ai.tool.security.filters.SecurityResult;

/**
 * Tool Security Manager Interface
 * 
 * This interface extends the common SecurityManager interface and provides
 * additional security operations specific to tool operations.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface ToolSecurityManager extends SecurityManager {

    /**
     * Validate tool execution security.
     * 
     * @param toolId Tool identifier
     * @param userId User identifier
     * @param parameters Tool parameters
     * @return Security result
     */
    SecurityResult validateExecution(String toolId, String userId, Object parameters);

    /**
     * Check tool access permissions.
     * 
     * @param toolId Tool identifier
     * @param userId User identifier
     * @return Security result
     */
    SecurityResult checkAccess(String toolId, String userId);

    // Eliminated getToolStatistics() method - consumers should access statistics directly via MetricsService
    // Use: metricsService.getSnapshot(MetricKeys.custom("tool-security", Map.of("operation", "security-check")),
    // ToolSecurityStatistics.class, Duration.ofHours(24))
}
