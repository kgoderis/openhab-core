package org.openhab.core.ai.tool.security.api;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Tool Security Service Interface
 * 
 * <p>
 * This interface defines the contract for tool security service implementations that provide:
 * - Tool filtering and access control
 * - Role-based access control (RBAC)
 * - Specification permissions management
 * - Access logging and monitoring
 * - Rate limiting and security alerts
 * - Security metrics and reporting
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface ToolSecurityService {

    /**
     * Check if security is enabled
     * 
     * @return true if security is enabled
     */
    boolean isSecurityEnabled();

    /**
     * Check if the security service is healthy
     * 
     * @return true if healthy
     */
    boolean isHealthy();

    /**
     * Check if a user has access to a specification
     * 
     * @param userId User ID
     * @param specificationId Specification ID
     * @param action Action to perform
     * @return true if access is allowed
     */
    boolean checkSpecificationAccess(String userId, String specificationId, String action);

    /**
     * Set specification permissions
     * 
     * @param specificationId Specification ID
     * @param permissions Specification permissions
     */
    void setSpecificationPermissions(String specificationId, SpecificationPermissions permissions);

    /**
     * Assign a role to a user
     * 
     * @param userId User ID
     * @param role Role to assign
     * @param permissions Set of permissions for the role
     */
    void assignUserRole(String userId, String role, Set<String> permissions);

    /**
     * Get access log entries
     * 
     * @param userId User ID (optional)
     * @param specificationId Specification ID (optional)
     * @return List of access log entries
     */
    List<AccessLogEntry> getAccessLog(@Nullable String userId, @Nullable String specificationId);

    /**
     * Create a security alert
     * 
     * @param type Alert type
     * @param message Alert message
     * @param severity Alert severity
     */
    void createSecurityAlert(String type, String message, String severity);

    /**
     * Get security statistics
     * 
     * @return Security statistics
     */
    SecurityStatistics getSecurityStatistics();

    /**
     * Get specification permissions
     * 
     * @param specificationId Specification ID
     * @return Specification permissions or null if not found
     */
    @Nullable
    SpecificationPermissions getSpecificationPermissions(String specificationId);

    /**
     * Get user role
     * 
     * @param userId User ID
     * @return User role or null if not found
     */
    @Nullable
    UserRole getUserRole(String userId);

    /**
     * Remove user role
     * 
     * @param userId User ID
     */
    void removeUserRole(String userId);

    /**
     * Get rate limit info for a user
     * 
     * @param userId User ID
     * @return Rate limit info or null if not found
     */
    @Nullable
    RateLimitInfo getRateLimitInfo(String userId);

    /**
     * Set rate limit for a user
     * 
     * @param userId User ID
     * @param maxRequests Maximum requests allowed
     * @param resetTime Reset time
     */
    void setRateLimit(String userId, int maxRequests, Instant resetTime);

    /**
     * Check if role-based access control is enabled
     * 
     * @return true if RBAC is enabled
     */
    boolean isRoleBasedAccessControlEnabled();

    /**
     * Set role-based access control enabled
     * 
     * @param enabled true to enable RBAC
     */
    void setRoleBasedAccessControlEnabled(boolean enabled);

    /**
     * Check if specification encryption is enabled
     * 
     * @return true if specification encryption is enabled
     */
    boolean isSpecificationEncryptionEnabled();

    /**
     * Set specification encryption enabled
     * 
     * @param enabled true to enable specification encryption
     */
    void setSpecificationEncryptionEnabled(boolean enabled);

    /**
     * Check if access logging is enabled
     * 
     * @return true if access logging is enabled
     */
    boolean isAccessLoggingEnabled();

    /**
     * Set access logging enabled
     * 
     * @param enabled true to enable access logging
     */
    void setAccessLoggingEnabled(boolean enabled);

    /**
     * Check if rate limiting is enabled
     * 
     * @return true if rate limiting is enabled
     */
    boolean isRateLimitingEnabled();

    /**
     * Set rate limiting enabled
     * 
     * @param enabled true to enable rate limiting
     */
    void setRateLimitingEnabled(boolean enabled);

    /**
     * Get total access attempts
     * 
     * @return Total access attempts
     */
    long getTotalAccessAttempts();

    /**
     * Get allowed access attempts
     * 
     * @return Allowed access attempts
     */
    long getAllowedAccessAttempts();

    /**
     * Get denied access attempts
     * 
     * @return Denied access attempts
     */
    long getDeniedAccessAttempts();

    /**
     * Get security alerts count
     * 
     * @return Security alerts count
     */
    long getSecurityAlertsCount();

    /**
     * Clear access log
     */
    void clearAccessLog();

    /**
     * Clear rate limits
     */
    void clearRateLimits();

    /**
     * Get all specification permissions
     * 
     * @return Map of specification permissions
     */
    Map<String, SpecificationPermissions> getAllSpecificationPermissions();

    /**
     * Get all user roles
     * 
     * @return Map of user roles
     */
    Map<String, UserRole> getAllUserRoles();
}
