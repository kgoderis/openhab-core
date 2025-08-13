package org.openhab.core.ai.agent.communication.conversation.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.agent.communication.conversation.Conversation;

/**
 * Interface for conversation templates.
 *
 * This interface defines the contract for conversation templates that can be applied
 * to conversations to set up predefined configurations, rules, and behaviors.
 *
 * Author: Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public interface ConversationTemplate {

    /**
     * Apply this template to a conversation.
     *
     * @param conversation the conversation to apply the template to
     * @return the modified conversation
     */
    Conversation applyTo(Conversation conversation);
}
