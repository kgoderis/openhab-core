package org.openhab.core.ai.agent.transport;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Agent card summary for transport management
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class AgentCardSummary {
    private final List<String> agentIds;
    private final TransportSummary transports;

    public AgentCardSummary(List<String> agentIds, TransportSummary transports) {
        this.agentIds = agentIds;
        this.transports = transports;
    }

    public List<String> getAgentIds() {
        return agentIds;
    }

    public TransportSummary getTransports() {
        return transports;
    }
}
