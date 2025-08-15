package org.openhab.core.ai.agent.collaboration.negotiation;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Aggregated negotiation statistics snapshot.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record NegotiationStatistics(long totalNegotiations, long successfulNegotiations, long failedNegotiations,
        long timeoutNegotiations, long abortedNegotiations, int activeSessions, int templates, int strategies) {
}
