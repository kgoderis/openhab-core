package org.openhab.core.ai.reasoning.metrics;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class ConfigurationPerformanceMetrics {
    private final long totalConfigurations;
    private final long totalPolicyUpdates;
    private final long totalPreferenceUpdates;
    private final long totalConstraintUpdates;
    private final int agentConfigurationCount;
    private final int behaviorPolicyCount;
    private final int userPreferenceCount;
    private final int constraintDefinitionCount;
    private final int safetyPolicyCount;

    ConfigurationPerformanceMetrics(ConfigurationPerformanceMetricsBuilder builder) {
        this.totalConfigurations = builder.totalConfigurations;
        this.totalPolicyUpdates = builder.totalPolicyUpdates;
        this.totalPreferenceUpdates = builder.totalPreferenceUpdates;
        this.totalConstraintUpdates = builder.totalConstraintUpdates;
        this.agentConfigurationCount = builder.agentConfigurationCount;
        this.behaviorPolicyCount = builder.behaviorPolicyCount;
        this.userPreferenceCount = builder.userPreferenceCount;
        this.constraintDefinitionCount = builder.constraintDefinitionCount;
        this.safetyPolicyCount = builder.safetyPolicyCount;
    }

    public long getTotalConfigurations() {
        return totalConfigurations;
    }

    public long getTotalPolicyUpdates() {
        return totalPolicyUpdates;
    }

    public long getTotalPreferenceUpdates() {
        return totalPreferenceUpdates;
    }

    public long getTotalConstraintUpdates() {
        return totalConstraintUpdates;
    }

    public int getAgentConfigurationCount() {
        return agentConfigurationCount;
    }

    public int getBehaviorPolicyCount() {
        return behaviorPolicyCount;
    }

    public int getUserPreferenceCount() {
        return userPreferenceCount;
    }

    public int getConstraintDefinitionCount() {
        return constraintDefinitionCount;
    }

    public int getSafetyPolicyCount() {
        return safetyPolicyCount;
    }

    public static ConfigurationPerformanceMetricsBuilder builder() {
        return new ConfigurationPerformanceMetricsBuilder();
    }
}
