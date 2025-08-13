package org.openhab.core.ai.agent.communication.conversation;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Conversation analytics snapshot.
 *
 * Captures metrics and derived statistics for a conversation.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ConversationAnalytics {
    private final String conversationId;
    private final Instant startTime;
    private final @Nullable Instant endTime;
    private final long durationMs;
    private final int participantCount;
    private final int messageCount;
    private final double messagesPerMinute;
    private final Map<String, Integer> participantActivity;
    private final ConversationState state;
    private final @Nullable String endReason;

    public ConversationAnalytics(String conversationId, Instant startTime, @Nullable Instant endTime,
            long durationMs, int participantCount, int messageCount, double messagesPerMinute,
            Map<String, Integer> participantActivity, ConversationState state, @Nullable String endReason) {
        this.conversationId = conversationId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationMs = durationMs;
        this.participantCount = participantCount;
        this.messageCount = messageCount;
        this.messagesPerMinute = messagesPerMinute;
        this.participantActivity = new ConcurrentHashMap<>(participantActivity);
        this.state = state;
        this.endReason = endReason;
    }

    public String getConversationId() { return conversationId; }
    public Instant getStartTime() { return startTime; }
    public @Nullable Instant getEndTime() { return endTime; }
    public long getDurationMs() { return durationMs; }
    public int getParticipantCount() { return participantCount; }
    public int getMessageCount() { return messageCount; }
    public double getMessagesPerMinute() { return messagesPerMinute; }
    public Map<String, Integer> getParticipantActivity() { return Map.copyOf(participantActivity); }
    public ConversationState getState() { return state; }
    public @Nullable String getEndReason() { return endReason; }

    @Override
    public String toString() {
        return String.format(
                "ConversationAnalytics{conversationId='%s', duration=%dms, participants=%d, messages=%d, messagesPerMinute=%.2f, state=%s}",
                conversationId, durationMs, participantCount, messageCount, messagesPerMinute, state);
    }
}
