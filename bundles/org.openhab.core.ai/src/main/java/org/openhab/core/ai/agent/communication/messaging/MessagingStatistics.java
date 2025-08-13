package org.openhab.core.ai.agent.communication.messaging;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Messaging statistics snapshot.
 *
 * Aggregated counts for the messaging subsystem.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class MessagingStatistics {
    private final long totalMessagesSent;
    private final long totalMessagesDelivered;
    private final long totalMessagesAcknowledged;
    private final long totalMessagesFailed;
    private final long totalBroadcastMessages;
    private final int storedMessages;
    private final int pendingDeliveries;
    private final int pendingAcknowledgments;
    private final int activeSubscriptions;

    public MessagingStatistics(long totalMessagesSent, long totalMessagesDelivered, long totalMessagesAcknowledged,
            long totalMessagesFailed, long totalBroadcastMessages, int storedMessages, int pendingDeliveries,
            int pendingAcknowledgments, int activeSubscriptions) {
        this.totalMessagesSent = totalMessagesSent;
        this.totalMessagesDelivered = totalMessagesDelivered;
        this.totalMessagesAcknowledged = totalMessagesAcknowledged;
        this.totalMessagesFailed = totalMessagesFailed;
        this.totalBroadcastMessages = totalBroadcastMessages;
        this.storedMessages = storedMessages;
        this.pendingDeliveries = pendingDeliveries;
        this.pendingAcknowledgments = pendingAcknowledgments;
        this.activeSubscriptions = activeSubscriptions;
    }

    public long getTotalMessagesSent() { return totalMessagesSent; }
    public long getTotalMessagesDelivered() { return totalMessagesDelivered; }
    public long getTotalMessagesAcknowledged() { return totalMessagesAcknowledged; }
    public long getTotalMessagesFailed() { return totalMessagesFailed; }
    public long getTotalBroadcastMessages() { return totalBroadcastMessages; }
    public int getStoredMessages() { return storedMessages; }
    public int getPendingDeliveries() { return pendingDeliveries; }
    public int getPendingAcknowledgments() { return pendingAcknowledgments; }
    public int getActiveSubscriptions() { return activeSubscriptions; }
}
