package org.openhab.core.ai.reasoning.configuration;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.configuration.BaseConfiguration;

/**
 * Configuration for AI agent behavior and capabilities.
 * 
 * This class defines configuration settings for autonomous agents including
 * behavior policies, safety constraints, learning capabilities, and operational parameters.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class AgentConfiguration extends BaseConfiguration {
    private final boolean autonomousModeEnabled;
    private final boolean behaviorLearningEnabled;
    private final boolean safetyConstraintsEnabled;
    private final double confidenceThreshold;
    private final Duration timeout;
    private final int maxConcurrentActions;
    private final List<String> behaviorPolicies;
    private final List<String> constraints;
    private final List<String> safetyPolicies;

    /* package */ AgentConfiguration(AgentConfigurationBuilder builder) {
        super(builder.agentId, true, "Agent Configuration", "1.0.0", builder.customSettings);
        this.autonomousModeEnabled = builder.autonomousModeEnabled;
        this.behaviorLearningEnabled = builder.behaviorLearningEnabled;
        this.safetyConstraintsEnabled = builder.safetyConstraintsEnabled;
        this.confidenceThreshold = builder.confidenceThreshold;
        this.timeout = builder.timeout;
        this.maxConcurrentActions = builder.maxConcurrentActions;
        this.behaviorPolicies = builder.behaviorPolicies;
        this.constraints = builder.constraints;
        this.safetyPolicies = builder.safetyPolicies;
    }

    public static AgentConfigurationBuilder builder() {
        return new AgentConfigurationBuilder();
    }

    /**
     * Get the agent ID.
     * 
     * @return the agent ID
     */
    public String getAgentId() {
        return getId();
    }

    public boolean isAutonomousModeEnabled() {
        return autonomousModeEnabled;
    }

    public boolean isBehaviorLearningEnabled() {
        return behaviorLearningEnabled;
    }

    public boolean isSafetyConstraintsEnabled() {
        return safetyConstraintsEnabled;
    }

    public double getConfidenceThreshold() {
        return confidenceThreshold;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public int getMaxConcurrentActions() {
        return maxConcurrentActions;
    }

    public List<String> getBehaviorPolicies() {
        return behaviorPolicies;
    }

    public List<String> getConstraints() {
        return constraints;
    }

    public List<String> getSafetyPolicies() {
        return safetyPolicies;
    }

    /**
     * Get custom settings.
     * 
     * @return custom settings map
     */
    public Map<String, Object> getCustomSettings() {
        return getCustomOptions();
    }

    // Inner Builder extracted to top-level class org.openhab.core.ai.reasoning.AgentConfigurationBuilder
}
