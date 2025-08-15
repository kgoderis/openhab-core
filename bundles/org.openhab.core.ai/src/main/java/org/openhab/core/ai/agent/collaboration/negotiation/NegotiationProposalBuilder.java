package org.openhab.core.ai.agent.collaboration.negotiation;

import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public final class NegotiationProposalBuilder {
    String sessionId = "";
    String agentId = "";
    Map<String, Object> proposal = Map.of();
    Instant submittedAt = Instant.now();

    public NegotiationProposalBuilder sessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    public NegotiationProposalBuilder agentId(String agentId) {
        this.agentId = agentId;
        return this;
    }

    public NegotiationProposalBuilder proposal(Map<String, Object> proposal) {
        this.proposal = proposal;
        return this;
    }

    public NegotiationProposalBuilder submittedAt(Instant submittedAt) {
        this.submittedAt = submittedAt;
        return this;
    }

    public NegotiationProposal build() {
        return new NegotiationProposal(this);
    }
}
