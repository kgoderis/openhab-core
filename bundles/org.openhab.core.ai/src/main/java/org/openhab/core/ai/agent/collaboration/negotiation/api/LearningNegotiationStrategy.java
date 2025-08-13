package org.openhab.core.ai.agent.collaboration.negotiation.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.agent.collaboration.negotiation.NegotiationSession;

/**
 * Interface for learning negotiation strategies.
 *
 * This interface extends NegotiationStrategy to add learning capabilities
 * that can improve negotiation outcomes based on historical data.
 *
 * Author: Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public interface LearningNegotiationStrategy extends NegotiationStrategy {

    /**
     * Learn from a completed negotiation session.
     *
     * @param session the completed negotiation session to learn from
     */
    void learn(NegotiationSession session);
}
