package org.openhab.core.ai.agent.communication.conversation;

import java.time.Instant;
import java.util.Objects;

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
    private final ConversationMessageType messageType;
    private final Instant timestamp;

    private ConversationMessage(Builder builder) {
        this.messageId = Objects.requireNonNull(builder.messageId, "messageId");
        this.conversationId = Objects.requireNonNull(builder.conversationId, "conversationId");
        this.fromAgentId = Objects.requireNonNull(builder.fromAgentId, "fromAgentId");
        this.content = Objects.requireNonNull(builder.content, "content");
        this.messageType = Objects.requireNonNull(builder.messageType, "messageType");
        this.timestamp = Objects.requireNonNull(builder.timestamp, "timestamp");
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

    public ConversationMessageType getMessageType() {
        return messageType;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private String messageId = "";
        private String conversationId = "";
        private String fromAgentId = "";
        private String content = "";
        private ConversationMessageType messageType = ConversationMessageType.TEXT;
        private Instant timestamp = Instant.now();

        public Builder() {
        }

        public Builder(ConversationMessage source) {
            this.messageId = source.messageId;
            this.conversationId = source.conversationId;
            this.fromAgentId = source.fromAgentId;
            this.content = source.content;
            this.messageType = source.messageType;
            this.timestamp = source.timestamp;
        }

        public Builder withMessageId(String messageId) {
            this.messageId = Objects.requireNonNull(messageId, "messageId");
            return this;
        }

        public Builder withConversationId(String conversationId) {
            this.conversationId = Objects.requireNonNull(conversationId, "conversationId");
            return this;
        }

        public Builder withFromAgentId(String fromAgentId) {
            this.fromAgentId = Objects.requireNonNull(fromAgentId, "fromAgentId");
            return this;
        }

        public Builder withContent(String content) {
            this.content = Objects.requireNonNull(content, "content");
            return this;
        }

        public Builder withMessageType(ConversationMessageType messageType) {
            this.messageType = Objects.requireNonNull(messageType, "messageType");
            return this;
        }

        public Builder withTimestamp(Instant timestamp) {
            this.timestamp = Objects.requireNonNull(timestamp, "timestamp");
            return this;
        }

        public ConversationMessage build() {
            if (messageId.isBlank()) {
                throw new IllegalArgumentException("messageId must not be blank");
            }
            if (conversationId.isBlank()) {
                throw new IllegalArgumentException("conversationId must not be blank");
            }
            if (fromAgentId.isBlank()) {
                throw new IllegalArgumentException("fromAgentId must not be blank");
            }
            if (content.isBlank()) {
                throw new IllegalArgumentException("content must not be blank");
            }
            return new ConversationMessage(this);
        }
    }
}
