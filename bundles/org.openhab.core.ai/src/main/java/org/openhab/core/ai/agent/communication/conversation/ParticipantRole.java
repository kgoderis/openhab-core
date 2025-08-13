package org.openhab.core.ai.agent.communication.conversation;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Conversation participant roles.
 *
 * Defines roles participants can take during a conversation.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public enum ParticipantRole {
    HOST,
    PARTICIPANT,
    OBSERVER,
    MODERATOR
}
