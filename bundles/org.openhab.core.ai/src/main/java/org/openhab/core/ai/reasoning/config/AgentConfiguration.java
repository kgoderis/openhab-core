package org.openhab.core.ai.reasoning.config;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.config.common.BaseConfiguration;

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

    private AgentConfiguration(Builder builder) {
        super(builder.agentId, true, "Agent Configuration", "1.0.0", builder.customSettings);
        this.autonomousModeEnabled = builder.autonomousModeEnabled;
        this.behaviorLearningEnabled = builder.behaviorLearningEnabled;
        this.safetyConstraintsEnabled = builder.safetyConstraintsEnabled;
        this.confidenceThreshold = builder.confidenceThreshold;
        this.timeout = builder.timeout;
        this.maxConcurrentActions = builder.maxConcurrentActions;
        this.behaviorPolicies = List.copyOf(builder.behaviorPolicies);
        this.constraints = List.copyOf(builder.constraints);
        this.safetyPolicies = List.copyOf(builder.safetyPolicies);
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
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

    public static final class Builder {
        private String agentId;
        private boolean autonomousModeEnabled = true;
        private boolean behaviorLearningEnabled = true;
        private boolean safetyConstraintsEnabled = true;
        private double confidenceThreshold = 0.7;
        private Duration timeout = Duration.ofMinutes(5);
        private int maxConcurrentActions = 10;
        private List<String> behaviorPolicies = new ArrayList<>();
        private List<String> constraints = new ArrayList<>();
        private List<String> safetyPolicies = new ArrayList<>();
        private Map<String, Object> customSettings = new HashMap<>();

        public Builder() {
        }

        public Builder(AgentConfiguration source) {
            this.agentId = source.getAgentId();
            this.autonomousModeEnabled = source.autonomousModeEnabled;
            this.behaviorLearningEnabled = source.behaviorLearningEnabled;
            this.safetyConstraintsEnabled = source.safetyConstraintsEnabled;
            this.confidenceThreshold = source.confidenceThreshold;
            this.timeout = source.timeout;
            this.maxConcurrentActions = source.maxConcurrentActions;
            this.behaviorPolicies = new ArrayList<>(source.behaviorPolicies);
            this.constraints = new ArrayList<>(source.constraints);
            this.safetyPolicies = new ArrayList<>(source.safetyPolicies);
            this.customSettings = new HashMap<>(source.getCustomSettings());
        }

        public Builder withAgentId(String agentId) {
            this.agentId = Objects.requireNonNull(agentId, "agentId");
            return this;
        }

        public Builder withAutonomousModeEnabled(boolean autonomousModeEnabled) {
            this.autonomousModeEnabled = autonomousModeEnabled;
            return this;
        }

        public Builder withBehaviorLearningEnabled(boolean behaviorLearningEnabled) {
            this.behaviorLearningEnabled = behaviorLearningEnabled;
            return this;
        }

        public Builder withSafetyConstraintsEnabled(boolean safetyConstraintsEnabled) {
            this.safetyConstraintsEnabled = safetyConstraintsEnabled;
            return this;
        }

        public Builder withConfidenceThreshold(double confidenceThreshold) {
            this.confidenceThreshold = confidenceThreshold;
            return this;
        }

        public Builder withTimeout(Duration timeout) {
            this.timeout = Objects.requireNonNull(timeout, "timeout");
            return this;
        }

        public Builder withMaxConcurrentActions(int maxConcurrentActions) {
            this.maxConcurrentActions = maxConcurrentActions;
            return this;
        }

        public Builder withBehaviorPolicies(List<String> behaviorPolicies) {
            this.behaviorPolicies = Objects.requireNonNull(behaviorPolicies, "behaviorPolicies");
            return this;
        }

        public Builder withConstraints(List<String> constraints) {
            this.constraints = Objects.requireNonNull(constraints, "constraints");
            return this;
        }

        public Builder withSafetyPolicies(List<String> safetyPolicies) {
            this.safetyPolicies = Objects.requireNonNull(safetyPolicies, "safetyPolicies");
            return this;
        }

        public Builder withCustomSettings(Map<String, Object> customSettings) {
            this.customSettings = Objects.requireNonNull(customSettings, "customSettings");
            return this;
        }

        public AgentConfiguration build() {
            if (agentId == null || agentId.isBlank()) {
                throw new IllegalArgumentException("agentId must not be null or blank");
            }
            if (confidenceThreshold < 0.0 || confidenceThreshold > 1.0) {
                throw new IllegalArgumentException("confidenceThreshold must be between 0.0 and 1.0");
            }
            if (maxConcurrentActions <= 0) {
                throw new IllegalArgumentException("maxConcurrentActions must be positive");
            }
            if (timeout.toMinutes() < 0) {
                throw new IllegalArgumentException("timeout must be non-negative");
            }
            return new AgentConfiguration(this);
        }
    }
}
