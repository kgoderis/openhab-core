package org.openhab.core.ai.reasoning.input;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Routes reasoning inputs to a specific agent. Currently acts as a simple sink
 * for collected inputs; replace with actual agent routing integration.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class AgentInputRouter {
    private final String agentId;
    private final List<ReasoningInput> routedInputs = new ArrayList<>();

    public AgentInputRouter(String agentId) {
        this.agentId = agentId;
    }

    public boolean routeInput(ReasoningInput input) {
        try {
            routedInputs.add(input);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getAgentId() {
        return agentId;
    }

    public List<ReasoningInput> getRoutedInputs() {
        return routedInputs;
    }
}
