package org.openhab.core.ai.common.monitoring.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Capability interface for security-specific metrics.
 * 
 * <p>
 * This interface provides security-specific functionality including authentication rates,
 * permission grant rates, security violation tracking, and session management metrics.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface SecurityMetrics {

    /**
     * Get the number of security violations.
     * 
     * @return security violations count
     */
    long securityViolations();

    /**
     * Get the number of active clients.
     * 
     * @return active clients count
     */
    int activeClients();

    /**
     * Get the number of blocked clients.
     * 
     * @return blocked clients count
     */
    int blockedClients();

    /**
     * Check if authentication is enabled.
     * 
     * @return true if authentication is enabled
     */
    boolean authenticationEnabled();

    /**
     * Check if request validation is enabled.
     * 
     * @return true if request validation is enabled
     */
    boolean requestValidationEnabled();

    /**
     * Get the maximum allowed connections.
     * 
     * @return maximum connections
     */
    int maxConnections();

    /**
     * Get the rate limit per minute.
     * 
     * @return rate limit per minute
     */
    int rateLimitPerMinute();

    /**
     * Get the number of active sessions.
     * 
     * @return active sessions count
     */
    int activeSessions();

    /**
     * Calculate the security violation rate as a percentage.
     * 
     * @return security violation rate between 0.0 and 1.0, or 0.0 if no operations
     */
    default double securityViolationRate() {
        long total = total();
        return total > 0 ? (double) securityViolations() / total : 0.0;
    }

    /**
     * Calculate the client utilization rate.
     * 
     * @return client utilization rate between 0.0 and 1.0
     */
    default double clientUtilizationRate() {
        int max = maxConnections();
        return max > 0 ? (double) activeClients() / max : 0.0;
    }

    /**
     * Calculate the session utilization rate.
     * 
     * @return session utilization rate between 0.0 and 1.0
     */
    default double sessionUtilizationRate() {
        int max = maxConnections();
        return max > 0 ? (double) activeSessions() / max : 0.0;
    }

    /**
     * Get the total count of operations (from CountsMetrics).
     * 
     * @return total count
     */
    long total();
}
