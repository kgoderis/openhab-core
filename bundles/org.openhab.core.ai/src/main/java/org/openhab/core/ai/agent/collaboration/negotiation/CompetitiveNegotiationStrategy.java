package org.openhab.core.ai.agent.collaboration.negotiation;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.agent.collaboration.negotiation.api.NegotiationStrategy;

/**
 * Competitive negotiation strategy implementation.
 */
@NonNullByDefault
public class CompetitiveNegotiationStrategy implements NegotiationStrategy {
    private final String strategyId;

    public CompetitiveNegotiationStrategy(String strategyId) { this.strategyId = strategyId; }
    @Override public String getStrategyId() { return strategyId; }
    @Override public NegotiationOutcome evaluateProposal(NegotiationSession session, NegotiationProposal proposal) {
        if (session.getProposals().size() >= session.getParticipantIds().size()) {
            return NegotiationOutcome.agreement(java.util.Map.of("agreement", "competitive"));
        }
        return NegotiationOutcome.noAgreement("Waiting for all competitive proposals");
    }
}


