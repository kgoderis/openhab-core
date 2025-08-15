package org.openhab.core.ai.agent.communication.messaging;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Message acknowledgment record.
 *
 * Immutable record of a message acknowledgment.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class MessageAcknowledgment {
    private final String messageId;
    private final String agentId;
    private final AcknowledgmentType acknowledgmentType;
    private final Instant timestamp;

    MessageAcknowledgment(MessageAcknowledgmentBuilder builder) {
        this.messageId = builder.messageId;
        this.agentId = builder.agentId;
        this.acknowledgmentType = builder.acknowledgmentType;
        this.timestamp = builder.timestamp;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getAgentId() {
        return agentId;
    }

    public AcknowledgmentType getAcknowledgmentType() {
        return acknowledgmentType;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public static MessageAcknowledgmentBuilder builder() {
        return new MessageAcknowledgmentBuilder();
    }
}
