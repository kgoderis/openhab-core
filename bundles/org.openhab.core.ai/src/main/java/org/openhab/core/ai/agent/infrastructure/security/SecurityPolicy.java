package org.openhab.core.ai.agent.infrastructure.security;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Security policy for agent security management
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class SecurityPolicy {
    private final String agentId;
    private final boolean encryptionRequired;
    private final boolean authenticationRequired;
    private final boolean auditLoggingRequired;
    private final Set<String> allowedActions;
    private final Set<String> allowedResources;
    private final int maxKeyRotationInterval;
    private final boolean signatureVerificationRequired;

    public SecurityPolicy(String agentId, boolean encryptionRequired, boolean authenticationRequired,
            boolean auditLoggingRequired, Set<String> allowedActions, Set<String> allowedResources,
            int maxKeyRotationInterval, boolean signatureVerificationRequired) {
        this.agentId = agentId;
        this.encryptionRequired = encryptionRequired;
        this.authenticationRequired = authenticationRequired;
        this.auditLoggingRequired = auditLoggingRequired;
        this.allowedActions = allowedActions;
        this.allowedResources = allowedResources;
        this.maxKeyRotationInterval = maxKeyRotationInterval;
        this.signatureVerificationRequired = signatureVerificationRequired;
    }

    public String getAgentId() {
        return agentId;
    }

    public boolean isEncryptionRequired() {
        return encryptionRequired;
    }

    public boolean isAuthenticationRequired() {
        return authenticationRequired;
    }

    public boolean isAuditLoggingRequired() {
        return auditLoggingRequired;
    }

    public Set<String> getAllowedActions() {
        return allowedActions;
    }

    public Set<String> getAllowedResources() {
        return allowedResources;
    }

    public int getMaxKeyRotationInterval() {
        return maxKeyRotationInterval;
    }

    public boolean isSignatureVerificationRequired() {
        return signatureVerificationRequired;
    }
}
