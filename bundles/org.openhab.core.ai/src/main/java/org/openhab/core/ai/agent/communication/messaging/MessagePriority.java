package org.openhab.core.ai.agent.communication.messaging;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Message priority levels.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public enum MessagePriority {
    LOW,
    NORMAL,
    HIGH,
    URGENT
}
