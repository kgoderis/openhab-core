package org.openhab.core.ai.tool.security;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.modelcontextprotocol.server.McpServerFeatures;

/**
 * Default Tool Security Service - Security manager for MCP Tools.
 * 
 * This class provides security functionality for the MCP Tool server,
 * including tool filtering and access control.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@Component(service = org.openhab.core.ai.tool.security.api.ToolSecurityService.class)
@NonNullByDefault
public class DefaultToolSecurityService implements org.openhab.core.ai.tool.security.api.ToolSecurityService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultToolSecurityService.class);

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
     * Create a new security service.
     * 
     * @param securityEnabled true if security is enabled
     */
    public DefaultToolSecurityService(boolean securityEnabled) {
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
        try {
            // Check if security processor is running
            if (securityProcessor.isShutdown() || securityProcessor.isTerminated()) {
                logger.warn("Security processor is not running");
                return false;
            }

            // Check if alert processor is running
            if (alertProcessor.isShutdown() || alertProcessor.isTerminated()) {
                logger.warn("Alert processor is not running");
                return false;
            }

            // Check memory usage
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory();
            long usedMemory = runtime.totalMemory() - runtime.freeMemory();
            double memoryUsage = (double) usedMemory / maxMemory;

            if (memoryUsage > 0.9) { // 90% threshold
                logger.warn("High memory usage detected: {}%", String.format("%.1f", memoryUsage * 100));
                return false;
            }

            // Check if critical security components are accessible
            if (specificationPermissions == null || userRoles == null || accessLog == null || rateLimits == null) {
                logger.warn("Critical security components are null");
                return false;
            }

            // Check if security statistics are being tracked
            long totalAttempts = totalAccessAttempts.get();
            long allowedAttempts = allowedAccessAttempts.get();
            long deniedAttempts = deniedAccessAttempts.get();

            // Basic sanity check on statistics
            if (totalAttempts < 0 || allowedAttempts < 0 || deniedAttempts < 0) {
                logger.warn("Invalid security statistics detected");
                return false;
            }

            if (totalAttempts != (allowedAttempts + deniedAttempts)) {
                logger.warn("Security statistics mismatch: total={}, allowed={}, denied={}", totalAttempts,
                        allowedAttempts, deniedAttempts);
                return false;
            }

            // Check if security features are properly configured
            if (securityEnabled && !roleBasedAccessControl.get() && !rateLimiting.get()) {
                logger.warn("Security enabled but no security features are active");
                return false;
            }

            logger.debug("Security manager health check passed");
            return true;

        } catch (Exception e) {
            logger.error("Health check failed", e);
            return false;
        }
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

        // Implement security filtering
        List<McpServerFeatures.SyncToolSpecification> filteredSpecs = new ArrayList<>();

        for (McpServerFeatures.SyncToolSpecification spec : toolSpecs) {
            try {
                // Get tool information for filtering
                String toolName = spec.tool().name();
                String toolDescription = spec.tool().description();

                // Check if tool should be filtered based on security rules
                if (shouldAllowTool(toolName, toolDescription)) {
                    filteredSpecs.add(spec);
                    logger.debug("Allowed sync tool: {}", toolName);
                } else {
                    logger.debug("Filtered out sync tool: {}", toolName);
                }

            } catch (Exception e) {
                logger.error("Error filtering sync tool specification", e);
                // In case of error, be conservative and filter out the tool
            }
        }

        logger.debug("Filtered {} sync tools to {} tools", toolSpecs.length, filteredSpecs.size());
        return filteredSpecs.toArray(new McpServerFeatures.SyncToolSpecification[0]);
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

        // Implement security filtering
        List<McpServerFeatures.AsyncToolSpecification> filteredSpecs = new ArrayList<>();

        for (McpServerFeatures.AsyncToolSpecification spec : toolSpecs) {
            try {
                // Get tool information for filtering
                String toolName = spec.tool().name();
                String toolDescription = spec.tool().description();

                // Check if tool should be filtered based on security rules
                if (shouldAllowTool(toolName, toolDescription)) {
                    filteredSpecs.add(spec);
                    logger.debug("Allowed async tool: {}", toolName);
                } else {
                    logger.debug("Filtered out async tool: {}", toolName);
                }

            } catch (Exception e) {
                logger.error("Error filtering async tool specification", e);
                // In case of error, be conservative and filter out the tool
            }
        }

        logger.debug("Filtered {} async tools to {} tools", toolSpecs.length, filteredSpecs.size());
        return filteredSpecs.toArray(new McpServerFeatures.AsyncToolSpecification[0]);
    }

    /**
     * Check if a tool should be allowed based on security rules
     * 
     * @param toolName the tool name
     * @param toolDescription the tool description
     * @return true if the tool should be allowed
     */
    private boolean shouldAllowTool(String toolName, String toolDescription) {
        if (toolName == null || toolName.trim().isEmpty()) {
            logger.warn("Tool name is null or empty");
            return false;
        }

        // Check for dangerous tool names
        List<String> dangerousToolPatterns = List.of("delete", "remove", "destroy", "format", "wipe", "clear",
                "shutdown", "restart", "reboot", "kill", "terminate", "admin", "root", "system", "privileged",
                "elevated");

        String lowerToolName = toolName.toLowerCase();
        String lowerDescription = toolDescription != null ? toolDescription.toLowerCase() : "";

        // Check if tool name contains dangerous patterns
        for (String pattern : dangerousToolPatterns) {
            if (lowerToolName.contains(pattern) || lowerDescription.contains(pattern)) {
                logger.warn("Tool filtered due to dangerous pattern '{}': {}", pattern, toolName);
                return false;
            }
        }

        // Check if tool requires special permissions
        if (lowerToolName.contains("admin") || lowerToolName.contains("system")) {
            // These tools require special handling - for now, filter them out
            logger.warn("Tool filtered due to requiring special permissions: {}", toolName);
            return false;
        }

        return true;
    }

    // Removed old nested record declarations; using top-level classes

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
    // Removed duplicate non-override setSpecificationPermissions; using @Override implementation below

    /**
     * Assign a role to a user
     * 
     * @param userId the user ID
     * @param role the role to assign
     * @param permissions the permissions for the role
     */
    public void assignUserRole(String userId, String role, Set<String> permissions) {
        UserRole userRole = new UserRole(userId, role, permissions, Instant.now(), "system");
        userRoles.put(userId, userRole);
        logger.info("Assigned role {} to user {} with permissions: {}", role, userId, permissions);
    }

    /**
     * Get access log entries
     * 
     * @param userId optional user ID filter
     * @param specificationId optional specification ID filter
     * @return list of access log entries
     */
    @Override
    public List<AccessLogEntry> getAccessLog(@Nullable String userId, @Nullable String specificationId) {
        return accessLog.values().stream().filter(entry -> userId == null || entry.getUserId().equals(userId))
                .filter(entry -> specificationId == null || entry.getSpecificationId().equals(specificationId))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Create a security alert
     * 
     * @param type the alert type
     * @param message the alert message
     * @param severity the alert severity
     */
    public void createSecurityAlert(String type, String message, String severity) {
        // TODO: wire into a dedicated SecurityAlert store if available
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
        if (specPerms.getDeniedUsers().contains(userId)) {
            return false;
        }

        // Check if user is explicitly allowed
        if (specPerms.getAllowedUsers().contains(userId)) {
            return true;
        }

        // Check if user's role is allowed
        return specPerms.getAllowedRoles().contains(userRole.role());
    }

    private boolean isRateLimited(String userId) {
        RateLimitInfo rateLimit = rateLimits.get(userId);
        if (rateLimit == null) {
            // Create default rate limit: 100 requests per minute
            rateLimit = new RateLimitInfo(userId, 100, 0, Instant.now().plusSeconds(60), false);
            rateLimits.put(userId, rateLimit);
        }

        // Check if rate limit has reset
        if (Instant.now().isAfter(rateLimit.getResetTime())) {
            rateLimit = new RateLimitInfo(userId, rateLimit.maxRequests(), 0, Instant.now().plusSeconds(60), false);
            rateLimits.put(userId, rateLimit);
        }

        // Check if current requests exceed limit
        if (rateLimit.currentRequests() >= rateLimit.maxRequests()) {
            return true;
        }

        // Increment current requests
        rateLimit = new RateLimitInfo(userId, rateLimit.maxRequests(), rateLimit.currentRequests() + 1,
                rateLimit.getResetTime(), false);
        rateLimits.put(userId, rateLimit);
        return false;
    }

    private void logAccess(String userId, String specificationId, String action, boolean allowed, String reason) {
        if (!accessLogging.get()) {
            return;
        }

        String logId = userId + "_" + specificationId + "_" + System.currentTimeMillis();
        AccessLogEntry entry = new AccessLogEntry(userId, action, specificationId, "tool", allowed, reason,
                Instant.now(), "");
        accessLog.put(logId, entry);
    }

    // Removed inner SecurityStatistics; using top-level SecurityStatistics

    // Interface implementation methods
    @Override
    public long getAllowedAccessAttempts() {
        return allowedAccessAttempts.get();
    }

    @Override
    public boolean isSpecificationEncryptionEnabled() {
        return specificationEncryption.get();
    }

    @Override
    public boolean isRateLimitingEnabled() {
        return rateLimiting.get();
    }

    @Override
    public long getTotalAccessAttempts() {
        return totalAccessAttempts.get();
    }

    @Override
    public void setRoleBasedAccessControlEnabled(boolean enabled) {
        roleBasedAccessControl.set(enabled);
    }

    @Override
    public void clearAccessLog() {
        accessLog.clear();
    }

    @Override
    public void clearRateLimits() {
        rateLimits.clear();
    }

    @Override
    public boolean isAccessLoggingEnabled() {
        return accessLogging.get();
    }

    @Override
    public void setRateLimit(String identifier, int limit, Instant resetTime) {
        RateLimitInfo rateLimit = new RateLimitInfo(identifier, limit, 0, resetTime, false);
        rateLimits.put(identifier, rateLimit);
    }

    @Override
    public boolean isRoleBasedAccessControlEnabled() {
        return roleBasedAccessControl.get();
    }

    @Override
    public void removeUserRole(String userId) {
        userRoles.remove(userId);
    }

    @Override
    public long getDeniedAccessAttempts() {
        return deniedAccessAttempts.get();
    }

    @Override
    public void setRateLimitingEnabled(boolean enabled) {
        rateLimiting.set(enabled);
    }

    @Override
    public long getSecurityAlertsCount() {
        return securityAlerts.get();
    }

    @Override
    public void setSpecificationEncryptionEnabled(boolean enabled) {
        specificationEncryption.set(enabled);
    }

    @Override
    public void setAccessLoggingEnabled(boolean enabled) {
        accessLogging.set(enabled);
    }

    @Override
    public SecurityStatistics getSecurityStatistics() {
        return new SecurityStatistics(totalAccessAttempts.get(), allowedAccessAttempts.get(),
                deniedAccessAttempts.get(), securityAlerts.get(), Instant.now());
    }

    @Override
    public SpecificationPermissions getSpecificationPermissions(String specificationId) {
        return specificationPermissions.get(specificationId);
    }

    @Override
    public UserRole getUserRole(String userId) {
        return userRoles.get(userId);
    }

    // Convenience accessor, not part of the interface
    public List<AccessLogEntry> getAccessLog() {
        return new ArrayList<>(accessLog.values());
    }

    // Convenience accessor, not part of the interface
    public RateLimitInfo getRateLimit(String identifier) {
        return rateLimits.get(identifier);
    }

    @Override
    public void setSpecificationPermissions(String specificationId, SpecificationPermissions permissions) {
        specificationPermissions.put(specificationId, permissions);
        logger.info("Set permissions for specification {}: roles={}, users={}", specificationId,
                permissions.getAllowedRoles(), permissions.getAllowedUsers());
    }

    public void setUserRole(String userId, UserRole role) {
        userRoles.put(userId, role);
    }

    @Override
    public java.util.Map<String, SpecificationPermissions> getAllSpecificationPermissions() {
        return new java.util.HashMap<>(specificationPermissions);
    }

    @Override
    public java.util.Map<String, UserRole> getAllUserRoles() {
        return new java.util.HashMap<>(userRoles);
    }

    @Override
    public RateLimitInfo getRateLimitInfo(String userId) {
        return rateLimits.get(userId);
    }
}
