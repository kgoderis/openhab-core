package org.openhab.core.ai.agent.communication.conversation;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Conversation aggregate.
 *
 * Represents a live or archived agent conversation.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class Conversation {
    private final String conversationId;
    private final Set<String> participantIds;
    private final @Nullable String templateId;
    private final Map<String, Object> context;
    private final Instant startTime;
    private ConversationState state;
    private @Nullable Instant endTime;
    private @Nullable String endReason;
    private Instant lastActivity;
    private int messageCount;

    Conversation(ConversationBuilder builder) {
        this.conversationId = builder.conversationId;
        this.participantIds = builder.participantIds;
        this.templateId = builder.templateId;
        this.context = builder.context;
        this.startTime = builder.startTime;
        this.state = builder.state;
        this.lastActivity = builder.lastActivity;
        this.messageCount = builder.messageCount;
    }

    public String getConversationId() {
        return conversationId;
    }

    public Set<String> getParticipantIds() {
        return participantIds;
    }

    public @Nullable String getTemplateId() {
        return templateId;
    }

    public Map<String, Object> getContext() {
        return context;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public ConversationState getState() {
        return state;
    }

    public void setState(ConversationState state) {
        this.state = state;
    }

    public @Nullable Instant getEndTime() {
        return endTime;
    }

    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }

    public @Nullable String getEndReason() {
        return endReason;
    }

    public void setEndReason(String endReason) {
        this.endReason = endReason;
    }

    public Instant getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(Instant lastActivity) {
        this.lastActivity = lastActivity;
    }

    public int getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(int messageCount) {
        this.messageCount = messageCount;
    }

    public static ConversationBuilder builder() {
        return new ConversationBuilder();
    }

    /* Extracted: org.openhab.core.ai.agent.communication.conversation.ConversationBuilder */
}
