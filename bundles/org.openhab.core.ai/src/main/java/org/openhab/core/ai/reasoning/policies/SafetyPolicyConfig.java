package org.openhab.core.ai.reasoning.policies;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class SafetyPolicyConfig {
    private final String policyId;
    private final String name;
    private final String description;
    private final String policyType;
    private final Map<String, Object> parameters;
    private final boolean enabled;
    private final double safetyThreshold;

    public SafetyPolicyConfig(String policyId, String name, String description, String policyType,
            Map<String, Object> parameters, boolean enabled, double safetyThreshold) {
        this.policyId = policyId;
        this.name = name;
        this.description = description;
        this.policyType = policyType;
        this.parameters = parameters;
        this.enabled = enabled;
        this.safetyThreshold = safetyThreshold;
    }

    public String getPolicyId() {
        return policyId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getPolicyType() {
        return policyType;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public double getSafetyThreshold() {
        return safetyThreshold;
    }
}
