package org.openhab.core.ai.reasoning.policies;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Safety policy configuration for an agent.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class SafetyPolicy {
    private final String agentId;
    private final Map<String, PolicyEntry> policies = new ConcurrentHashMap<>();

    public SafetyPolicy(String agentId) {
        this.agentId = agentId;
    }

    public SafetyValidationResult validateAction(String actionType, Map<String, Object> actionParameters) {
        for (PolicyEntry policy : policies.values()) {
            if (policy.appliesToAction(actionType, actionParameters)) {
                if (!policy.isAllowed()) {
                    return SafetyValidationResult.invalid("Policy violation: " + policy.getDescription());
                }
            }
        }
        return SafetyValidationResult.valid();
    }

    public boolean addPolicy(String policyType, Map<String, Object> policyParameters, String description) {
        PolicyEntry policy = new PolicyEntry(policyType, policyParameters, description);
        policies.put(policyType, policy);
        return true;
    }

    public int getPolicyCount() {
        return policies.size();
    }

    public String getAgentId() {
        return agentId;
    }
}
