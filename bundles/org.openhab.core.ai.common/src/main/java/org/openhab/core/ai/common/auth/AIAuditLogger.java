package org.openhab.core.ai.common.auth;

import java.time.Instant;

/**
 * Audit logger interface for AI authentication events.
 * 
 * This interface defines methods for logging security-related events
 * in AI protocol implementations for compliance and monitoring.
 * 
 * 
 */
public interface AIAuditLogger {

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
    void logSecurityViolation(String principalId, String violationType, String description, String protocol,
            Instant timestamp);
}
