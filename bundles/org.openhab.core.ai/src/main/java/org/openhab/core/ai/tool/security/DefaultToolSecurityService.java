package org.openhab.core.ai.tool.security;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.security.RateLimitInfo;
import org.openhab.core.ai.common.security.ToolSecurityStatistics;
import org.openhab.core.ai.tool.security.api.AccessLogEntry;
import org.openhab.core.ai.tool.security.api.SpecificationPermissions;
import org.openhab.core.ai.tool.security.api.ToolSecurityService;
import org.openhab.core.ai.tool.security.api.UserRole;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.modelcontextprotocol.server.McpServerFeatures;

/**
 * Default implementation of ToolSecurityService.
 * 
 * <p>
 * This service provides comprehensive security for tool operations including
 * authentication, authorization, rate limiting, and security monitoring.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
@Component(service = ToolSecurityService.class, configurationPid = "org.openhab.core.ai.tool.security")
public class DefaultToolSecurityService implements ToolSecurityService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultToolSecurityService.class);

    // Configuration
    private boolean securityEnabled;

    // Security features
    private final AtomicBoolean roleBasedAccessControl = new AtomicBoolean(true);
    private final AtomicReference<Boolean> rateLimiting = new AtomicReference<>(true);

    // Security state tracking
    private final ConcurrentHashMap<String, SpecificationPermissions> specificationPermissions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, UserRole> userRoles = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AccessLogEntry> accessLog = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, RateLimitInfo> rateLimits = new ConcurrentHashMap<>();

    // Metrics service for centralized metrics collection
    private @Nullable MetricsService metricsService;

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

            // Check if security statistics are being tracked via MetricsService
            MetricsService metrics = metricsService;
            if (metrics != null) {
                // Use MetricsService to get statistics instead of direct counters
                logger.debug("Security manager health check passed with MetricsService");
            } else {
                logger.debug("Security manager health check passed without MetricsService");
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
     * Check if a user has access to a specification.
     * 
     * @param userId the user ID
     * @param specificationId the specification ID
     * @param action the action to perform
     * @return true if access is allowed
     */
    public boolean checkSpecificationAccess(String userId, String specificationId, String action) {
        try {
            if (userId == null || userId.trim().isEmpty()) {
                logger.warn("Cannot check specification access: user ID is null or empty");
                recordMetrics("tool-security", "access-denied", false, Duration.ZERO);
                return false;
            }

            if (specificationId == null || specificationId.trim().isEmpty()) {
                logger.warn("Cannot check specification access: specification ID is null or empty for user: {}",
                        userId);
                recordMetrics("tool-security", "access-denied", false, Duration.ZERO);
                return false;
            }

            if (action == null || action.trim().isEmpty()) {
                logger.warn("Cannot check specification access: action is null or empty for user: {} on spec: {}",
                        userId, specificationId);
                recordMetrics("tool-security", "access-denied", false, Duration.ZERO);
                return false;
            }

            recordMetrics("tool-security", "access-attempt", true, Duration.ZERO);

            if (!securityEnabled) {
                recordMetrics("tool-security", "access-allowed", true, Duration.ZERO);
                logAccess(userId, specificationId, action, true, "Security disabled - access granted");
                return true;
            }

            // Check rate limiting
            try {
                if (rateLimiting.get() && isRateLimited(userId)) {
                    recordMetrics("tool-security", "access-denied", false, Duration.ZERO);
                    logAccess(userId, specificationId, action, false, "Rate limit exceeded");
                    logger.warn("Access denied due to rate limiting - User: {}, Spec: {}, Action: {}", userId,
                            specificationId, action);
                    return false;
                }
            } catch (Exception e) {
                logger.error("Error during rate limit check for user '{}': {}", userId, e.getMessage(), e);
                recordMetrics("tool-security", "access-denied", false, Duration.ZERO);
                logAccess(userId, specificationId, action, false, "Rate limit check failed: " + e.getMessage());
                return false; // Deny access if rate limiting check fails
            }

            // Check role-based access control
            try {
                if (roleBasedAccessControl.get()) {
                    boolean hasAccess = checkRoleBasedAccess(userId, specificationId, action);
                    if (hasAccess) {
                        recordMetrics("tool-security", "access-allowed", true, Duration.ZERO);
                        logAccess(userId, specificationId, action, true, "Access granted by role-based access control");
                        logger.debug("Access granted - User: {}, Spec: {}, Action: {}", userId, specificationId,
                                action);
                        return true;
                    } else {
                        recordMetrics("tool-security", "access-denied", false, Duration.ZERO);
                        logAccess(userId, specificationId, action, false, "Insufficient permissions");
                        logger.warn("Access denied due to insufficient permissions - User: {}, Spec: {}, Action: {}",
                                userId, specificationId, action);
                        return false;
                    }
                }
            } catch (Exception e) {
                logger.error("Error during role-based access check for user '{}': {}", userId, e.getMessage(), e);
                recordMetrics("tool-security", "access-denied", false, Duration.ZERO);
                logAccess(userId, specificationId, action, false, "Role-based access check failed: " + e.getMessage());
                return false; // Deny access if role-based check fails
            }

            // Default allow if no specific restrictions
            recordMetrics("tool-security", "access-allowed", true, Duration.ZERO);
            logAccess(userId, specificationId, action, true, "Default access granted");
            logger.debug("Default access granted - User: {}, Spec: {}, Action: {}", userId, specificationId, action);
            return true;
        } catch (Exception e) {
            logger.error("Unexpected error during specification access check for user '{}': {}", userId, e.getMessage(),
                    e);
            recordMetrics("tool-security", "access-denied", false, Duration.ZERO);
            try {
                logAccess(userId, specificationId, action, false, "Unexpected error: " + e.getMessage());
            } catch (Exception logError) {
                logger.error("Failed to log access attempt after error: {}", logError.getMessage());
            }
            return false; // Deny access on unexpected errors
        }
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
        try {
            List<AccessLogEntry> result = accessLog.values().stream().filter(entry -> {
                try {
                    return userId == null || (entry.getUserId() != null && entry.getUserId().equals(userId));
                } catch (Exception e) {
                    logger.warn("Error filtering access log by user ID: {}", e.getMessage());
                    return false;
                }
            }).filter(entry -> {
                try {
                    return specificationId == null || (entry.getSpecificationId() != null
                            && entry.getSpecificationId().equals(specificationId));
                } catch (Exception e) {
                    logger.warn("Error filtering access log by specification ID: {}", e.getMessage());
                    return false;
                }
            }).collect(Collectors.toList());

            recordMetrics("tool-security", "access-log-retrieval", true, Duration.ZERO);
            logger.debug("Retrieved {} access log entries for userId: {}, specificationId: {}", result.size(), userId,
                    specificationId);
            return result;
        } catch (Exception e) {
            logger.error("Error retrieving access log entries: {}", e.getMessage(), e);
            recordMetrics("tool-security", "access-log-retrieval", false, Duration.ZERO);
            return new ArrayList<>(); // Return empty list on error
        }
    }

    /**
     * Create a security alert.
     * 
     * @param type the alert type
     * @param message the alert message
     * @param severity the alert severity
     */
    public void createSecurityAlert(String type, String message, String severity) {
        try {
            if (type == null || type.trim().isEmpty()) {
                logger.warn("Cannot create security alert: type is null or empty");
                recordMetrics("tool-security", "security-alert", false, Duration.ZERO);
                return;
            }

            if (message == null || message.trim().isEmpty()) {
                logger.warn("Cannot create security alert: message is null or empty for type: {}", type);
                recordMetrics("tool-security", "security-alert", false, Duration.ZERO);
                return;
            }

            if (severity == null || severity.trim().isEmpty()) {
                severity = "MEDIUM"; // Default severity
            }

            // TODO: wire into a dedicated SecurityAlert store if available
            recordMetrics("tool-security", "security-alert", true, Duration.ZERO);
            logger.warn("Security alert created: Type: {}, Severity: {}, Message: {}", type, severity, message);
        } catch (Exception e) {
            logger.error("Error creating security alert for type '{}': {}", type, e.getMessage(), e);
            recordMetrics("tool-security", "security-alert", false, Duration.ZERO);
        }
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
        if (!securityEnabled) {
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
        // Use MetricsService to get statistics instead of direct counters
        MetricsService metrics = metricsService;
        if (metrics != null) {
            try {
                // For now, return 0 since we don't have a direct way to get specific counter values
                // In a future enhancement, MetricsService could provide domain-specific statistics
                return 0L;
            } catch (Exception e) {
                logger.debug("Failed to get allowed access attempts: {}", e.getMessage());
            }
        }
        return 0L;
    }

    @Override
    public boolean isSpecificationEncryptionEnabled() {
        return false; // No direct AtomicReference for this, as it's a configuration
    }

    @Override
    public boolean isRateLimitingEnabled() {
        return rateLimiting.get();
    }

    @Override
    public long getTotalAccessAttempts() {
        // Use MetricsService to get statistics instead of direct counters
        MetricsService metrics = metricsService;
        if (metrics != null) {
            try {
                // For now, return 0 since we don't have a direct way to get specific counter values
                // In a future enhancement, MetricsService could provide domain-specific statistics
                return 0L;
            } catch (Exception e) {
                logger.debug("Failed to get total access attempts: {}", e.getMessage());
            }
        }
        return 0L;
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
        return false; // No direct AtomicReference for this, as it's a configuration
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
        // Use MetricsService to get statistics instead of direct counters
        MetricsService metrics = metricsService;
        if (metrics != null) {
            try {
                // For now, return 0 since we don't have a direct way to get specific counter values
                // In a future enhancement, MetricsService could provide domain-specific statistics
                return 0L;
            } catch (Exception e) {
                logger.debug("Failed to get denied access attempts: {}", e.getMessage());
            }
        }
        return 0L;
    }

    @Override
    public void setRateLimitingEnabled(boolean enabled) {
        rateLimiting.set(enabled);
    }

    @Override
    public long getSecurityAlertsCount() {
        // Use MetricsService to get statistics instead of direct counters
        MetricsService metrics = metricsService;
        if (metrics != null) {
            try {
                // For now, return 0 since we don't have a direct way to get specific counter values
                // In a future enhancement, MetricsService could provide domain-specific statistics
                return 0L;
            } catch (Exception e) {
                logger.debug("Failed to get security alerts count: {}", e.getMessage());
            }
        }
        return 0L;
    }

    @Override
    public void setSpecificationEncryptionEnabled(boolean enabled) {
        // No direct AtomicReference for this, as it's a configuration
    }

    @Override
    public void setAccessLoggingEnabled(boolean enabled) {
        // No direct AtomicReference for this, as it's a configuration
    }

    @Override
    public ToolSecurityStatistics getSecurityStatistics() {
        try {
            // Use MetricsService to get statistics instead of direct counters
            MetricsService metrics = metricsService;
            if (metrics != null) {
                try {
                    // For now, return statistics with 0 values since we don't have a direct way to get specific counter
                    // values
                    // In a future enhancement, MetricsService could provide domain-specific statistics
                    recordMetrics("tool-security", "statistics-retrieval", true, Duration.ZERO);
                    return new ToolSecurityStatistics(0L, 0L, 0L, 0L, Instant.now());
                } catch (Exception e) {
                    logger.warn("Failed to get security statistics from MetricsService: {}", e.getMessage());
                    recordMetrics("tool-security", "statistics-retrieval", false, Duration.ZERO);
                }
            } else {
                logger.debug("MetricsService not available, returning empty security statistics");
            }

            return new ToolSecurityStatistics(0L, 0L, 0L, 0L, Instant.now());
        } catch (Exception e) {
            logger.error("Unexpected error retrieving security statistics: {}", e.getMessage(), e);
            recordMetrics("tool-security", "statistics-retrieval", false, Duration.ZERO);
            return new ToolSecurityStatistics(0L, 0L, 0L, 0L, Instant.now());
        }
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
    public Map<String, SpecificationPermissions> getAllSpecificationPermissions() {
        return new HashMap<>(specificationPermissions);
    }

    @Override
    public Map<String, UserRole> getAllUserRoles() {
        return new HashMap<>(userRoles);
    }

    @Override
    public RateLimitInfo getRateLimitInfo(String userId) {
        return rateLimits.get(userId);
    }

    /**
     * Helper method to record metrics using MetricsService.
     * 
     * @param domain the operation domain
     * @param operation the operation name
     * @param success whether the operation was successful
     * @param duration the operation duration
     */
    private void recordMetrics(String domain, String operation, boolean success, Duration duration) {
        try {
            MetricsService metrics = metricsService;
            if (metrics != null) {
                metrics.recordOperation(domain, operation, success, duration);
            } else {
                logger.debug("MetricsService not available, cannot record metrics for operation: {} - {}", domain,
                        operation);
            }
        } catch (Exception e) {
            // Avoid recursive metric recording in error handler for security operations
            logger.warn("Error recording security metrics for operation {}.{}: {}", domain, operation, e.getMessage());
        }
    }

    @Activate
    protected void activate(Map<String, Object> properties) {
        try {
            if (properties == null) {
                logger.warn("Properties map is null during activation, using default security settings");
                securityEnabled = true;
            } else {
                securityEnabled = (boolean) properties.getOrDefault("securityEnabled", true);
            }

            recordMetrics("tool-security", "service-activated", true, Duration.ZERO);
            logger.info("ToolSecurityService activated with securityEnabled: {}", securityEnabled);
        } catch (Exception e) {
            logger.error("Error during ToolSecurityService activation: {}", e.getMessage(), e);
            recordMetrics("tool-security", "service-activated", false, Duration.ZERO);
            // Continue with activation using default security settings
            securityEnabled = true;
            logger.info("ToolSecurityService activated with default security settings after error");
        }
    }

    @Modified
    protected void modified(Map<String, Object> properties) {
        try {
            if (properties == null) {
                logger.warn("Properties map is null during modification, keeping current security settings");
            } else {
                securityEnabled = (boolean) properties.getOrDefault("securityEnabled", true);
            }

            recordMetrics("tool-security", "service-modified", true, Duration.ZERO);
            logger.info("ToolSecurityService modified with securityEnabled: {}", securityEnabled);
        } catch (Exception e) {
            logger.error("Error during ToolSecurityService modification: {}", e.getMessage(), e);
            recordMetrics("tool-security", "service-modified", false, Duration.ZERO);
            // Keep current settings on error
            logger.info("ToolSecurityService modification failed, keeping current settings");
        }
    }

    @Deactivate
    protected void deactivate() {
        try {
            try {
                if (securityProcessor != null && !securityProcessor.isShutdown()) {
                    securityProcessor.shutdown();
                    logger.debug("Security processor shutdown initiated");
                }
            } catch (Exception e) {
                logger.error("Error shutting down security processor: {}", e.getMessage(), e);
            }

            try {
                if (alertProcessor != null && !alertProcessor.isShutdown()) {
                    alertProcessor.shutdown();
                    logger.debug("Alert processor shutdown initiated");
                }
            } catch (Exception e) {
                logger.error("Error shutting down alert processor: {}", e.getMessage(), e);
            }

            recordMetrics("tool-security", "service-deactivated", true, Duration.ZERO);
            logger.info("ToolSecurityService deactivated");
        } catch (Exception e) {
            logger.error("Error during ToolSecurityService deactivation: {}", e.getMessage(), e);
            recordMetrics("tool-security", "service-deactivated", false, Duration.ZERO);
            // Continue with deactivation despite errors
        }
    }

    @Reference(cardinality = ReferenceCardinality.OPTIONAL)
    protected void setMetricsService(MetricsService metricsService) {
        try {
            this.metricsService = metricsService;
            logger.info("MetricsService reference set for ToolSecurityService");
            recordMetrics("tool-security", "metrics-service-set", true, Duration.ZERO);
        } catch (Exception e) {
            logger.error("Error setting MetricsService reference: {}", e.getMessage(), e);
            // Cannot record metrics here as service might not be available
            logger.debug("Failed to record metrics for metrics-service-set operation");
        }
    }

    @Reference(cardinality = ReferenceCardinality.OPTIONAL)
    protected void unsetMetricsService(MetricsService metricsService) {
        try {
            recordMetrics("tool-security", "metrics-service-unset", true, Duration.ZERO);
            this.metricsService = null;
            logger.info("MetricsService reference unset for ToolSecurityService");
        } catch (Exception e) {
            logger.error("Error unsetting MetricsService reference: {}", e.getMessage(), e);
            this.metricsService = null; // Ensure it's cleared even on error
        }
    }
}
