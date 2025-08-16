package org.openhab.core.ai.agent.communication.conversation;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Conversation history log.
 *
 * Stores the messages and last activity timestamp for a conversation.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ConversationHistory {
    private final String conversationId;
    private final CopyOnWriteArrayList<ConversationMessage> messages;
    private Instant lastActivity;

    public ConversationHistory(String conversationId) {
        this.conversationId = conversationId;
        this.messages = new CopyOnWriteArrayList<>();
        this.lastActivity = Instant.now();
    }

    public void addMessage(ConversationMessage message) {
        messages.add(message);
        lastActivity = Instant.now();
    }

    public String getConversationId() {
        return conversationId;
    }

    public List<ConversationMessage> getMessages() {
        return List.copyOf(messages);
    }

    public Instant getLastActivity() {
        return lastActivity;
    }
}
