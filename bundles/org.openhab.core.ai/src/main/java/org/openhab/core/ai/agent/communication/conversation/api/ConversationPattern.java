package org.openhab.core.ai.agent.communication.conversation.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.agent.communication.conversation.Conversation;
import org.openhab.core.ai.agent.communication.conversation.ConversationMessage;

/**
 * Interface for conversation patterns.
 *
 * This interface defines the contract for conversation patterns that can be applied
 * to conversations and messages to implement specific behaviors or rules.
 *
 * Author: Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public interface ConversationPattern {

    /**
     * Apply this pattern to a conversation and message.
     *
     * @param conversation the conversation to apply the pattern to
     * @param message the message that triggered the pattern application
     */
    void apply(Conversation conversation, ConversationMessage message);
}
