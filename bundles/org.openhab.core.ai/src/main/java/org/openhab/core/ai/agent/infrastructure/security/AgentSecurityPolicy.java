package org.openhab.core.ai.agent.infrastructure.security;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.security.SecurityLevel;
import org.openhab.core.ai.common.security.SecurityPolicy;
import org.openhab.core.ai.common.security.SecurityPolicyType;

/**
 * Security policy for agent security management
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class AgentSecurityPolicy implements SecurityPolicy {
    private final String agentId;
    private final boolean encryptionRequired;
    private final boolean authenticationRequired;
    private final boolean auditLoggingRequired;
    private final Set<String> allowedActions;
    private final Set<String> allowedResources;
    private final int maxKeyRotationInterval;
    private final boolean signatureVerificationRequired;

    public AgentSecurityPolicy(String agentId, boolean encryptionRequired, boolean authenticationRequired,
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

    /**
     * Check if encryption is enabled for this agent.
     * 
     * @return true if encryption is enabled
     */
    public boolean isEncryptionEnabled() {
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

    // Implementation of SecurityPolicy interface methods
    @Override
    public String getPolicyId() {
        return agentId;
    }

    @Override
    public String getPolicyName() {
        return "Agent Security Policy for " + agentId;
    }

    @Override
    public boolean isEnabled() {
        return true; // Agent policies are always enabled
    }

    @Override
    public SecurityLevel getSecurityLevel() {
        // Determine security level based on requirements
        if (encryptionRequired && authenticationRequired && signatureVerificationRequired) {
            return SecurityLevel.HIGH;
        } else if (encryptionRequired || authenticationRequired) {
            return SecurityLevel.MEDIUM;
        } else {
            return SecurityLevel.LOW;
        }
    }

    @Override
    public SecurityPolicyType getPolicyType() {
        return SecurityPolicyType.AGENT_SECURITY;
    }

    @Override
    public boolean validateContext(org.openhab.core.ai.common.security.SecurityContext context) {
        // Check if the action is allowed
        if (!allowedActions.contains(context.getAction()) && !allowedActions.contains("*")) {
            return false;
        }

        // Check if the resource is allowed
        if (!allowedResources.contains(context.getResourceId()) && !allowedResources.contains("*")) {
            return false;
        }

        return true;
    }

    @Override
    public Map<String, Object> getPolicyMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("agentId", agentId);
        metadata.put("encryptionRequired", encryptionRequired);
        metadata.put("authenticationRequired", authenticationRequired);
        metadata.put("auditLoggingRequired", auditLoggingRequired);
        metadata.put("allowedActions", allowedActions);
        metadata.put("allowedResources", allowedResources);
        metadata.put("maxKeyRotationInterval", maxKeyRotationInterval);
        metadata.put("signatureVerificationRequired", signatureVerificationRequired);
        metadata.put("securityLevel", getSecurityLevel());
        metadata.put("policyType", getPolicyType());
        return metadata;
    }
}
