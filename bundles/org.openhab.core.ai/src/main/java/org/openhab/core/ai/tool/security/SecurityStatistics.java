package org.openhab.core.ai.tool.security;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Security statistics for tracking access attempts and security events.
 * 
 * <p>
 * This class provides comprehensive statistics about security operations including
 * access attempts, allowed/denied requests, security alerts, and timing information.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class SecurityStatistics {
    private final long totalAccessAttempts;
    private final long allowedAccessAttempts;
    private final long deniedAccessAttempts;
    private final long securityAlertsCount;
    private final Instant lastAccessTime;

    /**
     * Constructor for SecurityStatistics.
     * 
     * @param totalAccessAttempts total number of access attempts
     * @param allowedAccessAttempts number of allowed access attempts
     * @param deniedAccessAttempts number of denied access attempts
     * @param securityAlertsCount number of security alerts generated
     * @param lastAccessTime timestamp of last access attempt
     */
    public SecurityStatistics(long totalAccessAttempts, long allowedAccessAttempts, long deniedAccessAttempts,
            long securityAlertsCount, Instant lastAccessTime) {
        this.totalAccessAttempts = totalAccessAttempts;
        this.allowedAccessAttempts = allowedAccessAttempts;
        this.deniedAccessAttempts = deniedAccessAttempts;
        this.securityAlertsCount = securityAlertsCount;
        this.lastAccessTime = lastAccessTime;
    }

    /**
     * Get total number of access attempts.
     * 
     * @return total access attempts
     */
    public long getTotalAccessAttempts() {
        return totalAccessAttempts;
    }

    // Backward-compatible alias used in various metrics classes
    public long getTotalRequests() {
        return getTotalAccessAttempts();
    }

    /**
     * Get number of allowed access attempts.
     * 
     * @return allowed access attempts
     */
    public long getAllowedAccessAttempts() {
        return allowedAccessAttempts;
    }

    public long getAllowedRequests() {
        return getAllowedAccessAttempts();
    }

    /**
     * Get number of denied access attempts.
     * 
     * @return denied access attempts
     */
    public long getDeniedAccessAttempts() {
        return deniedAccessAttempts;
    }

    public long getDeniedRequests() {
        return getDeniedAccessAttempts();
    }

    /**
     * Get number of security alerts generated.
     * 
     * @return security alerts count
     */
    public long getSecurityAlertsCount() {
        return securityAlertsCount;
    }

    /**
     * Get timestamp of last access attempt.
     * 
     * @return last access time
     */
    public Instant getLastAccessTime() {
        return lastAccessTime;
    }
}
