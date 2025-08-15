package org.openhab.core.ai.auth;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Security metrics snapshot for audit logging and monitoring.
 *
 * author Karel Goderis - Initial Contribution
 * since 1.0.0
 */
@NonNullByDefault
public class SecurityMetrics {
    private final long totalAuthenticationAttempts;
    private final long successfulAuthentications;
    private final long failedAuthentications;
    private final long totalPermissionChecks;
    private final long grantedPermissions;
    private final long deniedPermissions;
    private final long securityViolations;
    private final long sessionCreations;
    private final long sessionTimeouts;
    private final int activeIncidents;
    private final long timestamp;

    public SecurityMetrics(long totalAuthenticationAttempts, long successfulAuthentications, long failedAuthentications,
            long totalPermissionChecks, long grantedPermissions, long deniedPermissions, long securityViolations,
            long sessionCreations, long sessionTimeouts, int activeIncidents, long timestamp) {
        this.totalAuthenticationAttempts = totalAuthenticationAttempts;
        this.successfulAuthentications = successfulAuthentications;
        this.failedAuthentications = failedAuthentications;
        this.totalPermissionChecks = totalPermissionChecks;
        this.grantedPermissions = grantedPermissions;
        this.deniedPermissions = deniedPermissions;
        this.securityViolations = securityViolations;
        this.sessionCreations = sessionCreations;
        this.sessionTimeouts = sessionTimeouts;
        this.activeIncidents = activeIncidents;
        this.timestamp = timestamp;
    }

    public long getTotalAuthenticationAttempts() {
        return totalAuthenticationAttempts;
    }

    public long getSuccessfulAuthentications() {
        return successfulAuthentications;
    }

    public long getFailedAuthentications() {
        return failedAuthentications;
    }

    public long getTotalPermissionChecks() {
        return totalPermissionChecks;
    }

    public long getGrantedPermissions() {
        return grantedPermissions;
    }

    public long getDeniedPermissions() {
        return deniedPermissions;
    }

    public long getSecurityViolations() {
        return securityViolations;
    }

    public long getSessionCreations() {
        return sessionCreations;
    }

    public long getSessionTimeouts() {
        return sessionTimeouts;
    }

    public int getActiveIncidents() {
        return activeIncidents;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public double getAuthenticationSuccessRate() {
        return totalAuthenticationAttempts > 0 ? (double) successfulAuthentications / totalAuthenticationAttempts : 0.0;
    }

    public double getPermissionGrantRate() {
        return totalPermissionChecks > 0 ? (double) grantedPermissions / totalPermissionChecks : 0.0;
    }

    @Override
    public String toString() {
        return String.format(
                "SecurityMetrics{totalAuthAttempts=%d, successfulAuth=%d, failedAuth=%d, totalPermissionChecks=%d, grantedPermissions=%d, deniedPermissions=%d, securityViolations=%d, sessionCreations=%d, sessionTimeouts=%d, activeIncidents=%d, authSuccessRate=%.2f%%, permissionGrantRate=%.2f%%}",
                totalAuthenticationAttempts, successfulAuthentications, failedAuthentications, totalPermissionChecks,
                grantedPermissions, deniedPermissions, securityViolations, sessionCreations, sessionTimeouts,
                activeIncidents, getAuthenticationSuccessRate() * 100, getPermissionGrantRate() * 100);
    }
}
