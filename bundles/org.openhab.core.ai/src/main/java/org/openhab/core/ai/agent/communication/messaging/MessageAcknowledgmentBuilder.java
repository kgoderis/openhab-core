package org.openhab.core.ai.agent.communication.messaging;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Builder for MessageAcknowledgment.
 */
@NonNullByDefault
public class MessageAcknowledgmentBuilder {
    String messageId;
    String agentId;
    AcknowledgmentType acknowledgmentType;
    Instant timestamp;

    public MessageAcknowledgmentBuilder messageId(String messageId) {
        this.messageId = messageId;
        return this;
    }

    public MessageAcknowledgmentBuilder agentId(String agentId) {
        this.agentId = agentId;
        return this;
    }

    public MessageAcknowledgmentBuilder acknowledgmentType(AcknowledgmentType acknowledgmentType) {
        this.acknowledgmentType = acknowledgmentType;
        return this;
    }

    public MessageAcknowledgmentBuilder timestamp(Instant timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public MessageAcknowledgment build() {
        return new MessageAcknowledgment(this);
    }
}
