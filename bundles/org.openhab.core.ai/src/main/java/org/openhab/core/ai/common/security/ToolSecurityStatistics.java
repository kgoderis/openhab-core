package org.openhab.core.ai.common.security;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Tool-specific security statistics implementation.
 * 
 * <p>
 * This class provides security statistics specific to tool operations,
 * including access attempts, allowed/denied requests, and security alerts.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ToolSecurityStatistics extends BaseSecurityStatistics {

    private final long securityAlertsCount;

    /**
     * Constructor for ToolSecurityStatistics.
     * 
     * @param totalAccessAttempts Total number of access attempts
     * @param allowedAccessAttempts Number of allowed access attempts
     * @param deniedAccessAttempts Number of denied access attempts
     * @param securityAlertsCount Number of security alerts generated
     * @param lastAccessTime Timestamp of last access attempt
     */
    public ToolSecurityStatistics(long totalAccessAttempts, long allowedAccessAttempts, long deniedAccessAttempts,
            long securityAlertsCount, @Nullable Instant lastAccessTime) {
        super(totalAccessAttempts, allowedAccessAttempts, deniedAccessAttempts, securityAlertsCount, lastAccessTime);
        this.securityAlertsCount = securityAlertsCount;
    }

    /**
     * Get the number of security alerts generated.
     * 
     * @return security alerts count
     */
    public long getSecurityAlertsCount() {
        return securityAlertsCount;
    }

    // Backward-compatible aliases for existing code
    /**
     * Get total number of access attempts (alias for getTotalOperations).
     * 
     * @return total access attempts
     */
    public long getTotalAccessAttempts() {
        return getTotalOperations();
    }

    /**
     * Get number of allowed access attempts (alias for getSuccessfulOperations).
     * 
     * @return allowed access attempts
     */
    public long getAllowedAccessAttempts() {
        return getSuccessfulOperations();
    }

    /**
     * Get number of denied access attempts (alias for getFailedOperations).
     * 
     * @return denied access attempts
     */
    public long getDeniedAccessAttempts() {
        return getFailedOperations();
    }

    /**
     * Get timestamp of last access attempt (alias for getLastOperationTime).
     * 
     * @return last access time
     */
    public @Nullable Instant getLastAccessTime() {
        return getLastOperationTime();
    }

    // Additional backward-compatible aliases
    public long getTotalRequests() {
        return getTotalOperations();
    }

    public long getAllowedRequests() {
        return getSuccessfulOperations();
    }

    public long getDeniedRequests() {
        return getFailedOperations();
    }
}
