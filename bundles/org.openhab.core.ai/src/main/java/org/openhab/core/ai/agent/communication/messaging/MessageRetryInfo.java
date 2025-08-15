package org.openhab.core.ai.agent.communication.messaging;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Message retry tracking info.
 *
 * Tracks retry attempts and scheduling for a message delivery.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class MessageRetryInfo {
    private final String messageId;
    private final int retryCount;
    private final Instant lastRetryTime;
    private final Instant nextRetryTime;

    public MessageRetryInfo(String messageId, int retryCount, Instant lastRetryTime, Instant nextRetryTime) {
        this.messageId = messageId;
        this.retryCount = retryCount;
        this.lastRetryTime = lastRetryTime;
        this.nextRetryTime = nextRetryTime;
    }

    public String getMessageId() {
        return messageId;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public Instant getLastRetryTime() {
        return lastRetryTime;
    }

    public Instant getNextRetryTime() {
        return nextRetryTime;
    }
}
