package org.openhab.core.ai.agent.communication.messaging;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Message delivery status values.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public enum MessageDeliveryStatus {
    PENDING,
    DELIVERED,
    ACKNOWLEDGED,
    FAILED,
    RETRYING
}
