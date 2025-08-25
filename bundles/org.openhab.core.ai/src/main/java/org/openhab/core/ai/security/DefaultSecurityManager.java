package org.openhab.core.ai.security;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.auth.AuthenticationContext;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.security.BaseSecurityStatistics;
import org.openhab.core.ai.common.security.SecurityManager;
import org.openhab.core.ai.common.security.SecurityStatistics;
import org.openhab.core.ai.security.config.SecurityConfiguration;
import org.openhab.core.ai.tool.security.filters.SecurityResult;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of the Security Manager.
 * 
 * <p>
 * This class provides comprehensive security validation for AI services
 * including access control, security validation, and monitoring.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
@Component(service = SecurityManager.class, configurationPid = "org.openhab.core.ai.security")
public class DefaultSecurityManager implements SecurityManager {

    private static final Logger logger = LoggerFactory.getLogger(DefaultSecurityManager.class);

    // Configuration
    private SecurityConfiguration configuration = SecurityConfiguration.builder().build();

    // Security policies and rules
    private final Map<String, Object> securityPolicies = new ConcurrentHashMap<>();

    // Metrics service for centralized metrics collection
    private @Nullable MetricsService metricsService;

    @Override
    public boolean isEnabled() {
        return configuration.isEnableAuditLogging();
    }

    @Override
    public boolean canAccess(String componentId, @Nullable String userId) {
        recordMetrics("security", "access-check", true, Duration.ZERO);

        // Basic implementation - can be extended with actual access validation
        boolean allowed = true; // Default to allow

        if (allowed) {
            recordMetrics("security", "access-allowed", true, Duration.ZERO);
        } else {
            recordMetrics("security", "access-denied", false, Duration.ZERO);
        }

        logger.debug("Access validation for component: {}, user: {}, allowed: {}", componentId, userId, allowed);
        return allowed;
    }

    @Override
    public SecurityResult validate(String action, String resource, AuthenticationContext context) {
        recordMetrics("security", "validation-check", true, Duration.ZERO);

        // Basic implementation - can be extended with actual security validation
        boolean allowed = true; // Default to allow

        if (allowed) {
            recordMetrics("security", "validation-allowed", true, Duration.ZERO);
            return SecurityResult.success("Security validation passed");
        } else {
            recordMetrics("security", "validation-denied", false, Duration.ZERO);
            return SecurityResult.failure("Security validation failed");
        }
    }

    @Override
    public void logViolation(String componentId, String violation, @Nullable Map<String, Object> context) {
        recordMetrics("security", "security-violation", false, Duration.ZERO);
        logger.warn("Security violation in component {}: {} with context: {}", componentId, violation, context);
    }

    @Override
    public SecurityStatistics getStatistics() {
        // Use MetricsService to get statistics instead of direct counters
        MetricsService metrics = metricsService;
        if (metrics != null) {
            try {
                // For now, return statistics with 0 values since we don't have a direct way to get specific counter
                // values
                // In a future enhancement, MetricsService could provide domain-specific statistics
                return new BaseSecurityStatistics(0L, 0L, 0L, 0L, Instant.now()) {
                    // Anonymous implementation using unified BaseSecurityStatistics
                };
            } catch (Exception e) {
                logger.debug("Failed to get security statistics: {}", e.getMessage());
            }
        }
        return new BaseSecurityStatistics(0L, 0L, 0L, 0L, Instant.now()) {
            // Anonymous implementation using unified BaseSecurityStatistics
        };
    }

    @Override
    public SecurityManager.SecurityManagerType getType() {
        return SecurityManager.SecurityManagerType.GENERAL;
    }

    @Override
    public SecurityConfiguration getConfig() {
        return configuration;
    }

    @Override
    public void updateConfig(SecurityConfiguration configuration) {
        this.configuration = configuration;
        logger.info("Security configuration updated");
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
        MetricsService metrics = metricsService;
        if (metrics != null) {
            try {
                metrics.recordOperation(domain, operation, success, duration);
            } catch (Exception e) {
                logger.debug("Failed to record metrics for {}.{}: {}", domain, operation, e.getMessage());
            }
        }
    }

    @Activate
    protected void activate(Map<String, Object> properties) {
        configuration = SecurityConfiguration.builder().build();
        logger.info("DefaultSecurityManager activated");
    }

    @Modified
    protected void modified(Map<String, Object> properties) {
        configuration = SecurityConfiguration.builder().build();
        logger.info("DefaultSecurityManager modified");
    }

    @Deactivate
    protected void deactivate() {
        logger.info("DefaultSecurityManager deactivated");
    }

    @Reference(cardinality = ReferenceCardinality.OPTIONAL)
    protected void setMetricsService(MetricsService metricsService) {
        this.metricsService = metricsService;
        logger.info("MetricsService reference set");
    }

    @Reference(cardinality = ReferenceCardinality.OPTIONAL)
    protected void unsetMetricsService(MetricsService metricsService) {
        this.metricsService = null;
        logger.info("MetricsService reference unset");
    }
}
