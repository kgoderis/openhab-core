package org.openhab.core.ai.agent.collaboration.negotiation;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Record representing the outcome of a negotiation evaluation.
 *
 * This record contains information about whether an agreement was reached,
 * the agreement details, and the reason for the outcome.
 *
 * Author: Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public record NegotiationOutcome(boolean agreementReached, @Nullable Map<String, Object> agreement, String reason) {

    /**
     * Create an outcome representing a successful agreement.
     *
     * @param agreement the agreement details
     * @return a NegotiationOutcome with agreement reached
     */
    public static NegotiationOutcome agreement(Map<String, Object> agreement) {
        return new NegotiationOutcome(true, agreement, "Agreement reached");
    }

    /**
     * Create an outcome representing no agreement reached.
     *
     * @param reason the reason why no agreement was reached
     * @return a NegotiationOutcome with no agreement
     */
    public static NegotiationOutcome noAgreement(String reason) {
        return new NegotiationOutcome(false, null, reason);
    }
}
