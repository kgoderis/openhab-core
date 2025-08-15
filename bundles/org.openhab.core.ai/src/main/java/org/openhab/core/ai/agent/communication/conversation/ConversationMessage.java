package org.openhab.core.ai.agent.communication.conversation;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Conversation message DTO.
 *
 * Represents a message sent within a conversation.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ConversationMessage {
    private final String messageId;
    private final String conversationId;
    private final String fromAgentId;
    private final String content;
    private final MessageType messageType;
    private final Instant timestamp;

    ConversationMessage(ConversationMessageBuilder builder) {
        this.messageId = builder.messageId;
        this.conversationId = builder.conversationId;
        this.fromAgentId = builder.fromAgentId;
        this.content = builder.content;
        this.messageType = builder.messageType;
        this.timestamp = builder.timestamp;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getConversationId() {
        return conversationId;
    }

    public String getFromAgentId() {
        return fromAgentId;
    }

    public String getContent() {
        return content;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public static ConversationMessageBuilder builder() {
        return new ConversationMessageBuilder();
    }

    /* Extracted: org.openhab.core.ai.agent.communication.conversation.ConversationMessageBuilder */
}
