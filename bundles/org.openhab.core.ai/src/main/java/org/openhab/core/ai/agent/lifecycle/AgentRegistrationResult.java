package org.openhab.core.ai.agent.lifecycle;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Agent registration result
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class AgentRegistrationResult {
    private final boolean success;
    private final String agentId;
    private final List<String> errors;

    private AgentRegistrationResult(boolean success, String agentId, List<String> errors) {
        this.success = success;
        this.agentId = agentId;
        this.errors = errors;
    }

    public static AgentRegistrationResult success(String agentId) {
        return new AgentRegistrationResult(true, agentId, List.of());
    }

    public static AgentRegistrationResult failure(List<String> errors) {
        return new AgentRegistrationResult(false, "", errors);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getAgentId() {
        return agentId;
    }

    public List<String> getErrors() {
        return errors;
    }
}
