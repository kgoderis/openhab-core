package org.openhab.core.ai.reasoning;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class AgentFullConfiguration {
    private final AutonomousBehaviorConfig.AgentConfiguration agentConfiguration;
    private final List<AutonomousBehaviorConfig.BehaviorPolicy> behaviorPolicies;
    private final List<AutonomousBehaviorConfig.ConstraintDefinition> constraints;
    private final List<AutonomousBehaviorConfig.SafetyPolicyConfig> safetyPolicies;

    public AgentFullConfiguration(AutonomousBehaviorConfig.AgentConfiguration agentConfiguration,
            List<AutonomousBehaviorConfig.BehaviorPolicy> behaviorPolicies,
            List<AutonomousBehaviorConfig.ConstraintDefinition> constraints,
            List<AutonomousBehaviorConfig.SafetyPolicyConfig> safetyPolicies) {
        this.agentConfiguration = agentConfiguration;
        this.behaviorPolicies = behaviorPolicies;
        this.constraints = constraints;
        this.safetyPolicies = safetyPolicies;
    }

    public AutonomousBehaviorConfig.AgentConfiguration getAgentConfiguration() { return agentConfiguration; }
    public List<AutonomousBehaviorConfig.BehaviorPolicy> getBehaviorPolicies() { return behaviorPolicies; }
    public List<AutonomousBehaviorConfig.ConstraintDefinition> getConstraints() { return constraints; }
    public List<AutonomousBehaviorConfig.SafetyPolicyConfig> getSafetyPolicies() { return safetyPolicies; }
}
