package org.openhab.core.ai.agent.communication.conversation;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class ConversationMessageBuilder {
    String messageId;
    String conversationId;
    String fromAgentId;
    String content;
    MessageType messageType;
    Instant timestamp;

    public ConversationMessageBuilder messageId(String messageId) { this.messageId = messageId; return this; }
    public ConversationMessageBuilder conversationId(String conversationId) { this.conversationId = conversationId; return this; }
    public ConversationMessageBuilder fromAgentId(String fromAgentId) { this.fromAgentId = fromAgentId; return this; }
    public ConversationMessageBuilder content(String content) { this.content = content; return this; }
    public ConversationMessageBuilder messageType(MessageType messageType) { this.messageType = messageType; return this; }
    public ConversationMessageBuilder timestamp(Instant timestamp) { this.timestamp = timestamp; return this; }

    public ConversationMessage build() { return ConversationMessage.builder().messageId(messageId).conversationId(conversationId).fromAgentId(fromAgentId).content(content).messageType(messageType).timestamp(timestamp).build(); }
}


