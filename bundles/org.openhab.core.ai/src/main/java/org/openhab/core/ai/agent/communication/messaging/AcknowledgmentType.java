package org.openhab.core.ai.agent.communication.messaging;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Message acknowledgment type for agent messaging service.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public enum AcknowledgmentType {
    RECEIVED,
    PROCESSED,
    COMPLETED,
    FAILED
}
