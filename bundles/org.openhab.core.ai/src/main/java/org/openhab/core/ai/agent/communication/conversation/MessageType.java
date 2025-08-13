package org.openhab.core.ai.agent.communication.conversation;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Conversation message type enumeration.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public enum MessageType {
    TEXT,
    COMMAND,
    QUERY,
    RESPONSE,
    NOTIFICATION,
    SYSTEM
}
