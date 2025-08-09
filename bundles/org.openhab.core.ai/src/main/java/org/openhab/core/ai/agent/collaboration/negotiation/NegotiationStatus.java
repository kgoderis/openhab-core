package org.openhab.core.ai.agent.negotiation;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Represents the status of a negotiation session
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public enum NegotiationStatus {
    ACTIVE,
    AGREED,
    ABORTED,
    TIMEOUT,
    FAILED,
    NOT_FOUND
}
