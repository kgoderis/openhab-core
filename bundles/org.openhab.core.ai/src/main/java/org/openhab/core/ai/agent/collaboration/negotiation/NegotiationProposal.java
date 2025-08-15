package org.openhab.core.ai.agent.collaboration.negotiation;

import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Represents a proposal submitted by an agent in a negotiation session
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class NegotiationProposal {

    private final String sessionId;
    private final String agentId;
    private final Map<String, Object> proposal;
    private final Instant submittedAt;

    /* package */ NegotiationProposal(NegotiationProposalBuilder builder) {
        this.sessionId = builder.sessionId;
        this.agentId = builder.agentId;
        this.proposal = builder.proposal;
        this.submittedAt = builder.submittedAt;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getAgentId() {
        return agentId;
    }

    public Map<String, Object> getProposal() {
        return proposal;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public static NegotiationProposalBuilder builder() {
        return new NegotiationProposalBuilder();
    }
}
