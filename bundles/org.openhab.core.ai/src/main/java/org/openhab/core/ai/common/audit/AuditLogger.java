package org.openhab.core.ai.common.audit;

import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Unified audit logger for all AI system operations.
 * 
 * This interface provides comprehensive logging of security events, tool operations,
 * authentication attempts, authorization decisions, and security violations across
 * the entire AI bundle.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface AuditLogger {

    // ===== Configuration and Management Methods =====

    /**
     * Get the audit logger ID.
     * 
     * @return the audit logger ID
     */
    String getAuditLoggerId();

    /**
     * Get the audit logger name.
     * 
     * @return the audit logger name
     */
    String getAuditLoggerName();

    /**
     * Get the audit logger description.
     * 
     * @return the audit logger description
     */
    String getAuditLoggerDescription();

    /**
     * Get the supported audit levels.
     * 
     * @return list of supported audit levels
     */
    String[] getSupportedAuditLevels();

    /**
     * Check if the audit logger is enabled.
     * 
     * @return true if the audit logger is enabled
     */
    boolean isEnabled();

    /**
     * Get the audit logger configuration.
     * 
     * @return the audit logger configuration
     */
    Map<String, Object> getConfiguration();

    /**
     * Update the audit logger configuration.
     * 
     * @param configuration the new configuration
     */
    void updateConfiguration(Map<String, Object> configuration);

    // ===== General Audit Event Methods =====

    /**
     * Log an audit event.
     * 
     * @param event the audit event to log
     */
    void logAuditEvent(AuditEvent event);

    /**
     * Log an audit event with the given parameters.
     * 
     * @param level the audit level
     * @param action the action being audited
     * @param userId the user ID
     * @param details additional audit details
     */
    void logAuditEvent(String level, String action, String userId, Map<String, Object> details);

    // ===== Authentication and Authorization Methods =====

    /**
     * Log an authentication attempt.
     * 
     * @param clientId Client identifier
     * @param protocol Protocol name
     * @param timestamp When the attempt occurred
     */
    void logAuthenticationAttempt(String clientId, String protocol, Instant timestamp);

    /**
     * Log a successful authentication.
     * 
     * @param clientId Client identifier
     * @param principalId Principal identifier
     * @param protocol Protocol name
     * @param timestamp When the authentication succeeded
     */
    void logAuthenticationSuccess(String clientId, String principalId, String protocol, Instant timestamp);

    /**
     * Log a failed authentication.
     * 
     * @param clientId Client identifier
     * @param provider Provider that failed
     * @param reason Failure reason
     * @param timestamp When the failure occurred
     */
    void logAuthenticationFailure(String clientId, String provider, String reason, Instant timestamp);

    /**
     * Log a successful JWT authentication.
     * 
     * @param principalId Principal identifier
     * @param protocol Protocol name
     * @param timestamp When the authentication succeeded
     */
    void logJWTAuthenticationSuccess(String principalId, String protocol, Instant timestamp);

    /**
     * Log a failed JWT authentication.
     * 
     * @param tokenPrefix First few characters of the token (for identification)
     * @param reason Failure reason
     * @param timestamp When the failure occurred
     */
    void logJWTAuthenticationFailure(String tokenPrefix, String reason, Instant timestamp);

    /**
     * Log a token refresh event.
     * 
     * @param principalId Principal identifier
     * @param timestamp When the refresh occurred
     */
    void logTokenRefresh(String principalId, Instant timestamp);

    /**
     * Log a logout event.
     * 
     * @param principalId Principal identifier
     * @param timestamp When the logout occurred
     */
    void logLogout(String principalId, Instant timestamp);

    /**
     * Log a permission check.
     * 
     * @param principalId Principal identifier
     * @param permission Permission being checked
     * @param protocol Protocol name
     * @param granted Whether permission was granted
     * @param timestamp When the check occurred
     */
    void logPermissionCheck(String principalId, String permission, String protocol, boolean granted, Instant timestamp);

    /**
     * Log a security violation.
     * 
     * @param principalId Principal identifier (if known)
     * @param violationType Type of violation
     * @param description Violation description
     * @param protocol Protocol name
     * @param timestamp When the violation occurred
     */
    void logSecurityViolation(@Nullable String principalId, String violationType, String description, String protocol,
            Instant timestamp);

    // ===== Tool Operation Methods =====

    /**
     * Log a tool execution.
     * 
     * @param toolId Tool identifier
     * @param userId User identifier
     * @param operation Operation performed
     * @param parameters Operation parameters
     * @param success Whether the operation was successful
     * @param timestamp When the operation occurred
     */
    void logToolExecution(String toolId, String userId, String operation, Map<String, Object> parameters,
            boolean success, Instant timestamp);

    /**
     * Log a tool access attempt.
     * 
     * @param toolId Tool identifier
     * @param userId User identifier
     * @param granted Whether access was granted
     * @param timestamp When the attempt occurred
     */
    void logToolAccess(String toolId, String userId, boolean granted, Instant timestamp);

    // ===== Maintenance Methods =====

    /**
     * Rotate audit logs.
     */
    void rotateLogs();

    /**
     * Get audit log statistics.
     * 
     * @return audit log statistics
     */
    Map<String, Object> getAuditStatistics();
}
