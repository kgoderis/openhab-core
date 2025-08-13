package org.openhab.core.ai.agent.collaboration.negotiation.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.agent.collaboration.negotiation.NegotiationOutcome;
import org.openhab.core.ai.agent.collaboration.negotiation.NegotiationProposal;
import org.openhab.core.ai.agent.collaboration.negotiation.NegotiationSession;

/**
 * Interface for negotiation strategies.
 *
 * This interface defines the contract for negotiation strategies that can evaluate
 * proposals and determine negotiation outcomes.
 *
 * Author: Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public interface NegotiationStrategy {

    /**
     * Get the strategy identifier.
     *
     * @return the strategy ID
     */
    String getStrategyId();

    /**
     * Evaluate a proposal in the context of a negotiation session.
     *
     * @param session the negotiation session
     * @param proposal the proposal to evaluate
     * @return the negotiation outcome
     */
    NegotiationOutcome evaluateProposal(NegotiationSession session, NegotiationProposal proposal);
}
