package org.openhab.core.ai.common.security;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Message-specific security statistics implementation.
 * 
 * <p>
 * This class provides security statistics specific to message operations,
 * including encryption, decryption, signature verification, and authentication.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class MessageSecurityStatistics extends BaseSecurityStatistics {

    private final long totalMessagesEncrypted;
    private final long totalMessagesDecrypted;
    private final long totalSignaturesVerified;
    private final long totalAuthenticationFailures;
    private final int securityPolicies;
    private final int agentKeyPairs;
    private final int auditLogs;

    /**
     * Constructor for MessageSecurityStatistics.
     * 
     * @param totalOperations Total number of security operations
     * @param successfulOperations Number of successful security operations
     * @param failedOperations Number of failed security operations
     * @param securityViolations Number of security violations
     * @param lastOperationTime Timestamp of last security operation
     * @param totalMessagesEncrypted Total number of messages encrypted
     * @param totalMessagesDecrypted Total number of messages decrypted
     * @param totalSignaturesVerified Total number of signatures verified
     * @param totalAuthenticationFailures Total number of authentication failures
     * @param securityPolicies Number of security policies
     * @param agentKeyPairs Number of agent key pairs
     * @param auditLogs Number of audit logs
     */
    public MessageSecurityStatistics(long totalOperations, long successfulOperations, long failedOperations,
            long securityViolations, @Nullable Instant lastOperationTime, long totalMessagesEncrypted,
            long totalMessagesDecrypted, long totalSignaturesVerified, long totalAuthenticationFailures,
            int securityPolicies, int agentKeyPairs, int auditLogs) {
        super(totalOperations, successfulOperations, failedOperations, securityViolations, lastOperationTime);
        this.totalMessagesEncrypted = totalMessagesEncrypted;
        this.totalMessagesDecrypted = totalMessagesDecrypted;
        this.totalSignaturesVerified = totalSignaturesVerified;
        this.totalAuthenticationFailures = totalAuthenticationFailures;
        this.securityPolicies = securityPolicies;
        this.agentKeyPairs = agentKeyPairs;
        this.auditLogs = auditLogs;
    }

    /**
     * Get the total number of messages encrypted.
     * 
     * @return total messages encrypted
     */
    public long getTotalMessagesEncrypted() {
        return totalMessagesEncrypted;
    }

    /**
     * Get the total number of messages decrypted.
     * 
     * @return total messages decrypted
     */
    public long getTotalMessagesDecrypted() {
        return totalMessagesDecrypted;
    }

    /**
     * Get the total number of signatures verified.
     * 
     * @return total signatures verified
     */
    public long getTotalSignaturesVerified() {
        return totalSignaturesVerified;
    }

    /**
     * Get the total number of authentication failures.
     * 
     * @return total authentication failures
     */
    public long getTotalAuthenticationFailures() {
        return totalAuthenticationFailures;
    }

    /**
     * Get the number of security policies.
     * 
     * @return security policies count
     */
    public int getSecurityPolicies() {
        return securityPolicies;
    }

    /**
     * Get the number of agent key pairs.
     * 
     * @return agent key pairs count
     */
    public int getAgentKeyPairs() {
        return agentKeyPairs;
    }

    /**
     * Get the number of audit logs.
     * 
     * @return audit logs count
     */
    public int getAuditLogs() {
        return auditLogs;
    }

    // Backward-compatible aliases for existing code
    /**
     * Get total security incidents (alias for getSecurityViolations).
     * 
     * @return total security incidents
     */
    public long getTotalSecurityIncidents() {
        return getSecurityViolations();
    }

    /**
     * Get security incidents count (alias for getSecurityViolations).
     * 
     * @return security incidents count
     */
    public int getSecurityIncidents() {
        return (int) getSecurityViolations();
    }
}
