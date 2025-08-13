package org.openhab.core.ai.agent.collaboration.negotiation;

import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class NegotiationProposalBuilder {
    private String sessionId = "";
    private String agentId = "";
    private Map<String, Object> proposal = Map.of();
    private Instant submittedAt = Instant.now();

    public NegotiationProposalBuilder sessionId(String sessionId) { this.sessionId = sessionId; return this; }
    public NegotiationProposalBuilder agentId(String agentId) { this.agentId = agentId; return this; }
    public NegotiationProposalBuilder proposal(Map<String, Object> proposal) { this.proposal = proposal; return this; }
    public NegotiationProposalBuilder submittedAt(Instant submittedAt) { this.submittedAt = submittedAt; return this; }

    public NegotiationProposal build() { return NegotiationProposal.builder().sessionId(sessionId).agentId(agentId).proposal(proposal).submittedAt(submittedAt).build(); }
}


