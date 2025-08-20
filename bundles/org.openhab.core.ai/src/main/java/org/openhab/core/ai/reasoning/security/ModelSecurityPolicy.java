package org.openhab.core.ai.reasoning.security;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.security.SecurityContext;
import org.openhab.core.ai.common.security.SecurityLevel;
import org.openhab.core.ai.common.security.SecurityPolicy;
import org.openhab.core.ai.common.security.SecurityPolicyType;

/**
 * Security policy for model access.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ModelSecurityPolicy implements SecurityPolicy {
    private final String policyId;
    private final String name;
    private final SecurityLevel level;
    private final Map<String, Object> rules;
    private final boolean enabled;

    public ModelSecurityPolicy(String policyId, String name, SecurityLevel level, Map<String, Object> rules,
            boolean enabled) {
        this.policyId = policyId;
        this.name = name;
        this.level = level;
        this.rules = new ConcurrentHashMap<>(rules);
        this.enabled = enabled;
    }

    public String getName() {
        return name;
    }

    public Map<String, Object> getRules() {
        return new ConcurrentHashMap<>(rules);
    }

    // Implementation of CommonSecurityPolicy interface methods
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String getPolicyId() {
        return policyId;
    }

    @Override
    public String getPolicyName() {
        return name;
    }

    @Override
    public SecurityLevel getSecurityLevel() {
        return level;
    }

    @Override
    public SecurityPolicyType getPolicyType() {
        return SecurityPolicyType.MODEL_SECURITY;
    }

    @Override
    public boolean validateContext(SecurityContext context) {
        if (!enabled) {
            return false;
        }

        // Check if the action is allowed based on rules
        Object actionRule = rules.get("allowedActions");
        if (actionRule instanceof String) {
            String allowedAction = (String) actionRule;
            if (!"*".equals(allowedAction) && !allowedAction.equals(context.getAction())) {
                return false;
            }
        }

        return true;
    }

    @Override
    public Map<String, Object> getPolicyMetadata() {
        Map<String, Object> metadata = new ConcurrentHashMap<>();
        metadata.put("policyId", policyId);
        metadata.put("name", name);
        metadata.put("level", level.name());
        metadata.put("enabled", enabled);
        metadata.put("rules", new ConcurrentHashMap<>(rules));
        return metadata;
    }

    /**
     * Check if a model is allowed by this policy.
     * 
     * @param modelId the model ID to check
     * @return true if the model is allowed
     */
    public boolean isModelAllowed(String modelId) {
        if (!enabled) {
            return false;
        }

        Object allowedModels = rules.get("allowedModels");
        if (allowedModels instanceof String) {
            String allowedModel = (String) allowedModels;
            return "*".equals(allowedModel) || allowedModel.equals(modelId);
        }

        return true; // Default to allowed if no specific rule
    }

    /**
     * Check if an operation is allowed by this policy.
     * 
     * @param operation the operation to check
     * @return true if the operation is allowed
     */
    public boolean isOperationAllowed(String operation) {
        if (!enabled) {
            return false;
        }

        Object allowedOperations = rules.get("allowedOperations");
        if (allowedOperations instanceof String) {
            String allowedOperation = (String) allowedOperations;
            return "*".equals(allowedOperation) || allowedOperation.equals(operation);
        }

        return true; // Default to allowed if no specific rule
    }
}
