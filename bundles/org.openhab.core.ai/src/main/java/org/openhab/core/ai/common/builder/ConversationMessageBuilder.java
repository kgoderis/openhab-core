package org.openhab.core.ai.common.builder;

import java.time.Instant;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.agent.communication.conversation.ConversationMessage;
import org.openhab.core.ai.agent.communication.conversation.MessageType;

/**
 * Unified builder for ConversationMessage objects.
 *
 * <p>
 * This builder provides a standardized way to create ConversationMessage objects
 * with proper validation and consistent API.
 * </p>
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class ConversationMessageBuilder extends CommunicationBuilder<ConversationMessage> {

    private @Nullable MessageType messageType;

    /**
     * Set the message type.
     *
     * @param messageType the message type
     * @return this builder
     */
    public ConversationMessageBuilder withMessageType(@Nullable MessageType messageType) {
        this.messageType = messageType;
        return this;
    }

    @Override
    public ConversationMessage build() {
        if (!isValid()) {
            throw new IllegalArgumentException("Invalid ConversationMessageBuilder state: " + getValidationErrors());
        }
        return new ConversationMessage(this);
    }

    // Getters for the ConversationMessage constructor
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
        return timestamp != null ? timestamp : Instant.now();
    }

    @Override
    protected void validate() {
        super.validate();
        // Additional validation specific to ConversationMessage
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
    }

    @Override
    protected void doReset() {
        super.doReset();
        messageType = null;
    }

    /**
     * Create a new ConversationMessageBuilder instance.
     *
     * @return a new builder instance
     */
    public static ConversationMessageBuilder builder() {
        return new ConversationMessageBuilder();
    }

    /**
     * Create a builder from an existing ConversationMessage.
     *
     * @param message the existing message
     * @return a builder with values from the existing message
     */
    public static ConversationMessageBuilder builder(ConversationMessage message) {
        Objects.requireNonNull(message, "message");
        ConversationMessageBuilder b = new ConversationMessageBuilder();
        b.withMessageId(message.getMessageId()).withConversationId(message.getConversationId())
                .withFromAgentId(message.getFromAgentId()).withContent(message.getContent())
                .withMessageType(message.getMessageType()).withTimestamp(message.getTimestamp());
        return b;
    }
}
