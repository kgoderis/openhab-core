package org.openhab.core.ai.agent.infrastructure.security;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Security statistics for agent security management
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class SecurityStatistics {
    private final long totalMessagesEncrypted;
    private final long totalMessagesDecrypted;
    private final long totalSignaturesVerified;
    private final long totalSecurityIncidents;
    private final long totalAuthenticationFailures;
    private final int securityPolicies;
    private final int agentKeyPairs;
    private final int securityIncidents;
    private final int auditLogs;

    public SecurityStatistics(long totalMessagesEncrypted, long totalMessagesDecrypted, long totalSignaturesVerified,
            long totalSecurityIncidents, long totalAuthenticationFailures, int securityPolicies, int agentKeyPairs,
            int securityIncidents, int auditLogs) {
        this.totalMessagesEncrypted = totalMessagesEncrypted;
        this.totalMessagesDecrypted = totalMessagesDecrypted;
        this.totalSignaturesVerified = totalSignaturesVerified;
        this.totalSecurityIncidents = totalSecurityIncidents;
        this.totalAuthenticationFailures = totalAuthenticationFailures;
        this.securityPolicies = securityPolicies;
        this.agentKeyPairs = agentKeyPairs;
        this.securityIncidents = securityIncidents;
        this.auditLogs = auditLogs;
    }

    // Getters
    public long getTotalMessagesEncrypted() {
        return totalMessagesEncrypted;
    }

    public long getTotalMessagesDecrypted() {
        return totalMessagesDecrypted;
    }

    public long getTotalSignaturesVerified() {
        return totalSignaturesVerified;
    }

    public long getTotalSecurityIncidents() {
        return totalSecurityIncidents;
    }

    public long getTotalAuthenticationFailures() {
        return totalAuthenticationFailures;
    }

    public int getSecurityPolicies() {
        return securityPolicies;
    }

    public int getAgentKeyPairs() {
        return agentKeyPairs;
    }

    public int getSecurityIncidents() {
        return securityIncidents;
    }

    public int getAuditLogs() {
        return auditLogs;
    }
}
