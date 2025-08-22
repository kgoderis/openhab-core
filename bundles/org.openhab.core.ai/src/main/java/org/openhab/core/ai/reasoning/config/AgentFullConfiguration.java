package org.openhab.core.ai.reasoning.config;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.reasoning.constraints.ConstraintDefinition;
import org.openhab.core.ai.reasoning.policies.BehaviorPolicy;
import org.openhab.core.ai.reasoning.policies.SafetyPolicyConfig;

@NonNullByDefault
public class AgentFullConfiguration {
    private final AgentConfiguration agentConfiguration;
    private final List<BehaviorPolicy> behaviorPolicies;
    private final List<ConstraintDefinition> constraints;
    private final List<SafetyPolicyConfig> safetyPolicies;

    public AgentFullConfiguration(AgentConfiguration agentConfiguration, List<BehaviorPolicy> behaviorPolicies,
            List<ConstraintDefinition> constraints, List<SafetyPolicyConfig> safetyPolicies) {
        this.agentConfiguration = agentConfiguration;
        this.behaviorPolicies = behaviorPolicies;
        this.constraints = constraints;
        this.safetyPolicies = safetyPolicies;
    }

    public AgentConfiguration getAgentConfiguration() {
        return agentConfiguration;
    }

    public List<BehaviorPolicy> getBehaviorPolicies() {
        return behaviorPolicies;
    }

    public List<ConstraintDefinition> getConstraints() {
        return constraints;
    }

    public List<SafetyPolicyConfig> getSafetyPolicies() {
        return safetyPolicies;
    }
}
