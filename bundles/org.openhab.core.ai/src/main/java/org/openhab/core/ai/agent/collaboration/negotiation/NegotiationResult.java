package org.openhab.core.ai.agent.collaboration.negotiation;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Negotiation result DTO for session actions.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record NegotiationResult(String sessionId, NegotiationStatus status, String message,
        @Nullable Map<String, Object> agreement, @Nullable NegotiationProposal proposal) {

    public static NegotiationResult agreement(NegotiationSession session, Map<String, Object> agreement) {
        return new NegotiationResult(session.getSessionId(), NegotiationStatus.AGREED, "Agreement reached", agreement,
                null);
    }

    public static NegotiationResult proposalAccepted(NegotiationSession session, NegotiationProposal proposal) {
        return new NegotiationResult(session.getSessionId(), NegotiationStatus.ACTIVE, "Proposal accepted", null,
                proposal);
    }

    public static NegotiationResult aborted(NegotiationSession session, String reason) {
        return new NegotiationResult(session.getSessionId(), NegotiationStatus.ABORTED, "Aborted: " + reason, null,
                null);
    }

    public static NegotiationResult notFound(String message) {
        return new NegotiationResult("", NegotiationStatus.NOT_FOUND, message, null, null);
    }

    public static NegotiationResult failure(String message) {
        return new NegotiationResult("", NegotiationStatus.FAILED, message, null, null);
    }
}
