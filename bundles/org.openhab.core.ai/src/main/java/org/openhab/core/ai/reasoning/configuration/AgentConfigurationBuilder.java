package org.openhab.core.ai.reasoning.configuration;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.builder.AbstractBuilder;

/**
 * Builder for {@link AgentConfiguration}.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class AgentConfigurationBuilder extends AbstractBuilder<AgentConfiguration> {
    String agentId;
    boolean autonomousModeEnabled = true;
    boolean behaviorLearningEnabled = true;
    boolean safetyConstraintsEnabled = true;
    double confidenceThreshold = 0.7;
    Duration timeout = Duration.ofMinutes(5);
    int maxConcurrentActions = 10;
    List<String> behaviorPolicies = new ArrayList<>();
    List<String> constraints = new ArrayList<>();
    List<String> safetyPolicies = new ArrayList<>();
    Map<String, Object> customSettings = new ConcurrentHashMap<>();

    public AgentConfigurationBuilder agentId(String agentId) {
        this.agentId = agentId;
        return this;
    }

    public AgentConfigurationBuilder autonomousModeEnabled(boolean v) {
        this.autonomousModeEnabled = v;
        return this;
    }

    public AgentConfigurationBuilder behaviorLearningEnabled(boolean v) {
        this.behaviorLearningEnabled = v;
        return this;
    }

    public AgentConfigurationBuilder safetyConstraintsEnabled(boolean v) {
        this.safetyConstraintsEnabled = v;
        return this;
    }

    public AgentConfigurationBuilder confidenceThreshold(double v) {
        this.confidenceThreshold = v;
        return this;
    }

    public AgentConfigurationBuilder timeout(Duration v) {
        this.timeout = v;
        return this;
    }

    public AgentConfigurationBuilder maxConcurrentActions(int v) {
        this.maxConcurrentActions = v;
        return this;
    }

    public AgentConfigurationBuilder behaviorPolicies(List<String> v) {
        this.behaviorPolicies = v;
        return this;
    }

    public AgentConfigurationBuilder constraints(List<String> v) {
        this.constraints = v;
        return this;
    }

    public AgentConfigurationBuilder safetyPolicies(List<String> v) {
        this.safetyPolicies = v;
        return this;
    }

    public AgentConfigurationBuilder customSettings(Map<String, Object> v) {
        this.customSettings = v;
        return this;
    }

    @Override
    public AgentConfiguration build() {
        return new AgentConfiguration(this);
    }

    @Override
    protected void validate() {
        validateRequiredString(agentId, "agentId");
        validateRange((int) (confidenceThreshold * 100), "confidenceThreshold", 0, 100);
        validatePositive(maxConcurrentActions, "maxConcurrentActions");
        if (timeout.toMinutes() < 0) {
            addValidationError("timeout must be non-negative");
        }
    }

    @Override
    protected void doReset() {
        agentId = null;
        autonomousModeEnabled = true;
        behaviorLearningEnabled = true;
        safetyConstraintsEnabled = true;
        confidenceThreshold = 0.7;
        timeout = Duration.ofMinutes(5);
        maxConcurrentActions = 10;
        behaviorPolicies = new ArrayList<>();
        constraints = new ArrayList<>();
        safetyPolicies = new ArrayList<>();
        customSettings = new ConcurrentHashMap<>();
    }
}
