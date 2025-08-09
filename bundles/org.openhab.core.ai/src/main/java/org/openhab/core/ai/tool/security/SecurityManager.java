package org.openhab.core.ai.tool.security;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.modelcontextprotocol.server.McpServerFeatures;

/**
 * Security manager for MCP Tools.
 * 
 * This class provides security functionality for the MCP Tool server,
 * including tool filtering and access control.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@Component(service = SecurityManager.class)
@NonNullByDefault
public class SecurityManager {

    private static final Logger logger = LoggerFactory.getLogger(SecurityManager.class);

    // Security configuration
    private final boolean securityEnabled;
    private final AtomicReference<Boolean> roleBasedAccessControl = new AtomicReference<>(true);
    private final AtomicReference<Boolean> specificationEncryption = new AtomicReference<>(false);
    private final AtomicReference<Boolean> accessLogging = new AtomicReference<>(true);
    private final AtomicReference<Boolean> rateLimiting = new AtomicReference<>(true);

    // Security state tracking
    private final ConcurrentHashMap<String, SpecificationPermissions> specificationPermissions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, UserRole> userRoles = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AccessLogEntry> accessLog = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, RateLimitInfo> rateLimits = new ConcurrentHashMap<>();

    // Security metrics
    private final AtomicLong totalAccessAttempts = new AtomicLong(0);
    private final AtomicLong allowedAccessAttempts = new AtomicLong(0);
    private final AtomicLong deniedAccessAttempts = new AtomicLong(0);
    private final AtomicLong securityAlerts = new AtomicLong(0);

    // Background processors
    private final ScheduledExecutorService securityProcessor = Executors.newScheduledThreadPool(1);
    private final ScheduledExecutorService alertProcessor = Executors.newScheduledThreadPool(1);

    /**
     * Create a new security manager.
     * 
     * @param securityEnabled true if security is enabled
     */
    public SecurityManager(boolean securityEnabled) {
        this.securityEnabled = securityEnabled;
    }

    /**
     * Check if security is enabled.
     * 
     * @return true if security is enabled
     */
    public boolean isSecurityEnabled() {
        return securityEnabled;
    }

    /**
     * Check if the security manager is healthy.
     * 
     * @return true if healthy
     */
    public boolean isHealthy() {
        return true; // TODO: Implement health check
    }

    /**
     * Filter sync tools by security.
     * 
     * @param toolSpecs the tool specifications to filter
     * @return the filtered tool specifications
     */
    public McpServerFeatures.SyncToolSpecification[] filterSyncTools(
            McpServerFeatures.SyncToolSpecification[] toolSpecs) {
        if (!securityEnabled) {
            return toolSpecs;
        }

        logger.debug("Filtering {} sync tools by security", toolSpecs.length);
        // TODO: Implement security filtering
        return toolSpecs;
    }

    /**
     * Filter async tools by security.
     * 
     * @param toolSpecs the tool specifications to filter
     * @return the filtered tool specifications
     */
    public McpServerFeatures.AsyncToolSpecification[] filterAsyncTools(
            McpServerFeatures.AsyncToolSpecification[] toolSpecs) {
        if (!securityEnabled) {
            return toolSpecs;
        }

        logger.debug("Filtering {} async tools by security", toolSpecs.length);
        // TODO: Implement security filtering
        return toolSpecs;
    }

    // Record classes for security data
    public record SpecificationPermissions(String specificationId, Set<String> allowedRoles, Set<String> allowedUsers,
            Set<String> deniedUsers, boolean requiresEncryption, Instant createdAt) {
    }

    public record UserRole(String userId, String role, Set<String> permissions, Instant assignedAt) {
    }

    public record AccessLogEntry(String userId, String specificationId, String action, boolean allowed, String reason,
            Instant timestamp) {
    }

    public record RateLimitInfo(String userId, int maxRequests, int currentRequests, Instant resetTime) {
    }

    public record SecurityAlert(String alertId, String type, String message, String severity, Instant timestamp) {
    }

    // Security and Access Control Methods

    /**
     * Check if a user has access to a specification
     * 
     * @param userId the user ID
     * @param specificationId the specification ID
     * @param action the action being performed
     * @return true if access is allowed
     */
    public boolean checkSpecificationAccess(String userId, String specificationId, String action) {
        totalAccessAttempts.incrementAndGet();

        if (!securityEnabled) {
            allowedAccessAttempts.incrementAndGet();
            return true;
        }

        // Check rate limiting
        if (rateLimiting.get() && isRateLimited(userId)) {
            deniedAccessAttempts.incrementAndGet();
            logAccess(userId, specificationId, action, false, "Rate limit exceeded");
            return false;
        }

        // Check role-based access control
        if (roleBasedAccessControl.get()) {
            boolean hasAccess = checkRoleBasedAccess(userId, specificationId, action);
            if (hasAccess) {
                allowedAccessAttempts.incrementAndGet();
                logAccess(userId, specificationId, action, true, "Access granted");
                return true;
            } else {
                deniedAccessAttempts.incrementAndGet();
                logAccess(userId, specificationId, action, false, "Insufficient permissions");
                return false;
            }
        }

        // Default allow if no specific restrictions
        allowedAccessAttempts.incrementAndGet();
        logAccess(userId, specificationId, action, true, "Default access granted");
        return true;
    }

    /**
     * Set permissions for a specification
     * 
     * @param specificationId the specification ID
     * @param permissions the permissions to set
     */
    public void setSpecificationPermissions(String specificationId, SpecificationPermissions permissions) {
        specificationPermissions.put(specificationId, permissions);
        logger.info("Set permissions for specification {}: roles={}, users={}", specificationId,
                permissions.allowedRoles(), permissions.allowedUsers());
    }

    /**
     * Assign a role to a user
     * 
     * @param userId the user ID
     * @param role the role to assign
     * @param permissions the permissions for the role
     */
    public void assignUserRole(String userId, String role, Set<String> permissions) {
        UserRole userRole = new UserRole(userId, role, permissions, Instant.now());
        userRoles.put(userId, userRole);
        logger.info("Assigned role {} to user {} with permissions: {}", role, userId, permissions);
    }

    /**
     * Get security statistics
     * 
     * @return security statistics
     */
    public SecurityStatistics getSecurityStatistics() {
        return new SecurityStatistics(totalAccessAttempts.get(), allowedAccessAttempts.get(),
                deniedAccessAttempts.get(), securityAlerts.get());
    }

    /**
     * Get access log entries
     * 
     * @param userId optional user ID filter
     * @param specificationId optional specification ID filter
     * @return list of access log entries
     */
    public List<AccessLogEntry> getAccessLog(@Nullable String userId, @Nullable String specificationId) {
        return accessLog.values().stream().filter(entry -> userId == null || entry.userId().equals(userId))
                .filter(entry -> specificationId == null || entry.specificationId().equals(specificationId))
                .collect(Collectors.toList());
    }

    /**
     * Create a security alert
     * 
     * @param type the alert type
     * @param message the alert message
     * @param severity the alert severity
     */
    public void createSecurityAlert(String type, String message, String severity) {
        String alertId = "alert_" + System.currentTimeMillis();
        SecurityAlert alert = new SecurityAlert(alertId, type, message, severity, Instant.now());
        securityAlerts.incrementAndGet();
        logger.warn("Security alert created: {} - {}", type, message);
    }

    // Helper methods
    private boolean checkRoleBasedAccess(String userId, String specificationId, String action) {
        UserRole userRole = userRoles.get(userId);
        if (userRole == null) {
            return false;
        }

        SpecificationPermissions specPerms = specificationPermissions.get(specificationId);
        if (specPerms == null) {
            return true; // No specific restrictions
        }

        // Check if user is explicitly denied
        if (specPerms.deniedUsers().contains(userId)) {
            return false;
        }

        // Check if user is explicitly allowed
        if (specPerms.allowedUsers().contains(userId)) {
            return true;
        }

        // Check if user's role is allowed
        return specPerms.allowedRoles().contains(userRole.role());
    }

    private boolean isRateLimited(String userId) {
        RateLimitInfo rateLimit = rateLimits.get(userId);
        if (rateLimit == null) {
            // Create default rate limit: 100 requests per minute
            rateLimit = new RateLimitInfo(userId, 100, 0, Instant.now().plusSeconds(60));
            rateLimits.put(userId, rateLimit);
        }

        // Check if rate limit has reset
        if (Instant.now().isAfter(rateLimit.resetTime())) {
            rateLimit = new RateLimitInfo(userId, rateLimit.maxRequests(), 0, Instant.now().plusSeconds(60));
            rateLimits.put(userId, rateLimit);
        }

        // Check if current requests exceed limit
        if (rateLimit.currentRequests() >= rateLimit.maxRequests()) {
            return true;
        }

        // Increment current requests
        rateLimit = new RateLimitInfo(userId, rateLimit.maxRequests(), rateLimit.currentRequests() + 1,
                rateLimit.resetTime());
        rateLimits.put(userId, rateLimit);
        return false;
    }

    private void logAccess(String userId, String specificationId, String action, boolean allowed, String reason) {
        if (!accessLogging.get()) {
            return;
        }

        String logId = userId + "_" + specificationId + "_" + System.currentTimeMillis();
        AccessLogEntry entry = new AccessLogEntry(userId, specificationId, action, allowed, reason, Instant.now());
        accessLog.put(logId, entry);
    }

    /**
     * Security statistics.
     */
    public static class SecurityStatistics {
        private final long totalRequests;
        private final long allowedRequests;
        private final long deniedRequests;
        private final long securityAlerts;

        public SecurityStatistics(long totalRequests, long allowedRequests, long deniedRequests, long securityAlerts) {
            this.totalRequests = totalRequests;
            this.allowedRequests = allowedRequests;
            this.deniedRequests = deniedRequests;
            this.securityAlerts = securityAlerts;
        }

        public long getTotalRequests() {
            return totalRequests;
        }

        public long getAllowedRequests() {
            return allowedRequests;
        }

        public long getDeniedRequests() {
            return deniedRequests;
        }

        public long getSecurityAlerts() {
            return securityAlerts;
        }
    }
}
