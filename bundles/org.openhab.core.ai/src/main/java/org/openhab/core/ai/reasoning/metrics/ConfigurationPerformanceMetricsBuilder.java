package org.openhab.core.ai.reasoning.metrics;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class ConfigurationPerformanceMetricsBuilder {
    long totalConfigurations;
    long totalPolicyUpdates;
    long totalPreferenceUpdates;
    long totalConstraintUpdates;
    int agentConfigurationCount;
    int behaviorPolicyCount;
    int userPreferenceCount;
    int constraintDefinitionCount;
    int safetyPolicyCount;

    public ConfigurationPerformanceMetricsBuilder totalConfigurations(long v) {
        this.totalConfigurations = v;
        return this;
    }

    public ConfigurationPerformanceMetricsBuilder totalPolicyUpdates(long v) {
        this.totalPolicyUpdates = v;
        return this;
    }

    public ConfigurationPerformanceMetricsBuilder totalPreferenceUpdates(long v) {
        this.totalPreferenceUpdates = v;
        return this;
    }

    public ConfigurationPerformanceMetricsBuilder totalConstraintUpdates(long v) {
        this.totalConstraintUpdates = v;
        return this;
    }

    public ConfigurationPerformanceMetricsBuilder agentConfigurationCount(int v) {
        this.agentConfigurationCount = v;
        return this;
    }

    public ConfigurationPerformanceMetricsBuilder behaviorPolicyCount(int v) {
        this.behaviorPolicyCount = v;
        return this;
    }

    public ConfigurationPerformanceMetricsBuilder userPreferenceCount(int v) {
        this.userPreferenceCount = v;
        return this;
    }

    public ConfigurationPerformanceMetricsBuilder constraintDefinitionCount(int v) {
        this.constraintDefinitionCount = v;
        return this;
    }

    public ConfigurationPerformanceMetricsBuilder safetyPolicyCount(int v) {
        this.safetyPolicyCount = v;
        return this;
    }

    public ConfigurationPerformanceMetrics build() {
        return new ConfigurationPerformanceMetrics(this);
    }
}
