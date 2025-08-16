package org.openhab.core.ai.agent.collaboration.negotiation;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.agent.collaboration.negotiation.api.NegotiationStrategy;

/**
 * Default negotiation strategy implementation.
 */
@NonNullByDefault
public class DefaultNegotiationStrategy implements NegotiationStrategy {
    private final String strategyId;

    public DefaultNegotiationStrategy(String strategyId) {
        this.strategyId = strategyId;
    }

    @Override
    public String getStrategyId() {
        return strategyId;
    }

    @Override
    public NegotiationOutcome evaluateProposal(NegotiationSession session, NegotiationProposal proposal) {
        if (session.getProposals().size() >= session.getParticipantIds().size()) {
            return NegotiationOutcome.agreement(Map.of("agreement", "default"));
        }
        return NegotiationOutcome.noAgreement("Waiting for more proposals");
    }
}
