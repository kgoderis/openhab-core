package org.openhab.core.ai.reasoning;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class AgentConfiguration {
    private final String agentId;
    private final boolean autonomousModeEnabled;
    private final boolean behaviorLearningEnabled;
    private final boolean safetyConstraintsEnabled;
    private final double confidenceThreshold;
    private final Duration timeout;
    private final int maxConcurrentActions;
    private final List<String> behaviorPolicies;
    private final List<String> constraints;
    private final List<String> safetyPolicies;
    private final Map<String, Object> customSettings;

    private AgentConfiguration(Builder builder) {
        this.agentId = builder.agentId;
        this.autonomousModeEnabled = builder.autonomousModeEnabled;
        this.behaviorLearningEnabled = builder.behaviorLearningEnabled;
        this.safetyConstraintsEnabled = builder.safetyConstraintsEnabled;
        this.confidenceThreshold = builder.confidenceThreshold;
        this.timeout = builder.timeout;
        this.maxConcurrentActions = builder.maxConcurrentActions;
        this.behaviorPolicies = builder.behaviorPolicies;
        this.constraints = builder.constraints;
        this.safetyPolicies = builder.safetyPolicies;
        this.customSettings = builder.customSettings;
    }

    /* package */ AgentConfiguration(AgentConfigurationBuilder builder) {
        this.agentId = builder.agentId;
        this.autonomousModeEnabled = builder.autonomousModeEnabled;
        this.behaviorLearningEnabled = builder.behaviorLearningEnabled;
        this.safetyConstraintsEnabled = builder.safetyConstraintsEnabled;
        this.confidenceThreshold = builder.confidenceThreshold;
        this.timeout = builder.timeout;
        this.maxConcurrentActions = builder.maxConcurrentActions;
        this.behaviorPolicies = builder.behaviorPolicies;
        this.constraints = builder.constraints;
        this.safetyPolicies = builder.safetyPolicies;
        this.customSettings = builder.customSettings;
    }

    public static AgentConfigurationBuilder builder() { return new AgentConfigurationBuilder(); }

    public String getAgentId() { return agentId; }
    public boolean isAutonomousModeEnabled() { return autonomousModeEnabled; }
    public boolean isBehaviorLearningEnabled() { return behaviorLearningEnabled; }
    public boolean isSafetyConstraintsEnabled() { return safetyConstraintsEnabled; }
    public double getConfidenceThreshold() { return confidenceThreshold; }
    public Duration getTimeout() { return timeout; }
    public int getMaxConcurrentActions() { return maxConcurrentActions; }
    public List<String> getBehaviorPolicies() { return behaviorPolicies; }
    public List<String> getConstraints() { return constraints; }
    public List<String> getSafetyPolicies() { return safetyPolicies; }
    public Map<String, Object> getCustomSettings() { return customSettings; }

    // Inner Builder extracted to top-level class org.openhab.core.ai.reasoning.AgentConfigurationBuilder
}


