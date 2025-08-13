package org.openhab.core.ai.agent.communication.conversation;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Conversation lifecycle state.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public enum ConversationState {
    ACTIVE,
    PAUSED,
    ENDED,
    ARCHIVED
}
