package org.openhab.core.ai.reasoning;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Decision Context for model-driven decision making.
 *
 * Encapsulates the scenario, options, constraints and metadata used by
 * {@link AgentModelDecisionEngine} to produce a decision.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class DecisionContext {

    private final String contextId;
    private final String agentId;
    private final String agentType;
    private final String domain;
    private final String scenario;
    private final Map<String, Object> options;
    private final String currentState;
    private final Map<String, Object> userPreferences;
    private final String constraints;
    private final RiskLevel riskLevel;

    public DecisionContext(String contextId, String agentId, String agentType, String domain, String scenario,
            Map<String, Object> options, String currentState, Map<String, Object> userPreferences, String constraints,
            RiskLevel riskLevel) {
        this.contextId = contextId;
        this.agentId = agentId;
        this.agentType = agentType;
        this.domain = domain;
        this.scenario = scenario;
        this.options = new ConcurrentHashMap<>(options);
        this.currentState = currentState;
        this.userPreferences = new ConcurrentHashMap<>(userPreferences);
        this.constraints = constraints;
        this.riskLevel = riskLevel;
    }

    public String getContextId() {
        return contextId;
    }

    public String getAgentId() {
        return agentId;
    }

    public String getAgentType() {
        return agentType;
    }

    public String getDomain() {
        return domain;
    }

    public String getScenario() {
        return scenario;
    }

    public Map<String, Object> getOptions() {
        return new ConcurrentHashMap<>(options);
    }

    public String getCurrentState() {
        return currentState;
    }

    public Map<String, Object> getUserPreferences() {
        return new ConcurrentHashMap<>(userPreferences);
    }

    public String getConstraints() {
        return constraints;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    /**
     * Risk level enum.
     */
    // RiskLevel moved to top-level org.openhab.core.ai.reasoning.RiskLevel
}


