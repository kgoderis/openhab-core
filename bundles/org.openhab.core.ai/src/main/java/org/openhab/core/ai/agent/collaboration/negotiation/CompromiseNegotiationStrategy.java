package org.openhab.core.ai.agent.collaboration.negotiation;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.agent.collaboration.negotiation.api.NegotiationStrategy;

/**
 * Compromise negotiation strategy implementation.
 */
@NonNullByDefault
public class CompromiseNegotiationStrategy implements NegotiationStrategy {
    private final String strategyId;

    public CompromiseNegotiationStrategy(String strategyId) {
        this.strategyId = strategyId;
    }

    @Override
    public String getStrategyId() {
        return strategyId;
    }

    @Override
    public NegotiationOutcome evaluateProposal(NegotiationSession session, NegotiationProposal proposal) {
        if (session.getProposals().size() >= 2) {
            return NegotiationOutcome.agreement(java.util.Map.of("agreement", "compromise"));
        }
        return NegotiationOutcome.noAgreement("Need at least 2 proposals for compromise");
    }
}
