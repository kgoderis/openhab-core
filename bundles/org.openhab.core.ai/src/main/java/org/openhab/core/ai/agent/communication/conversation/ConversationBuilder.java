package org.openhab.core.ai.agent.communication.conversation;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@NonNullByDefault
public class ConversationBuilder {
    String conversationId;
    Set<String> participantIds;
    @Nullable
    String templateId;
    Map<String, Object> context;
    Instant startTime;
    ConversationState state;
    Instant lastActivity;
    int messageCount;

    public ConversationBuilder conversationId(String conversationId) {
        this.conversationId = conversationId;
        return this;
    }

    public ConversationBuilder participantIds(List<String> participantIds) {
        this.participantIds = Set.copyOf(participantIds);
        return this;
    }

    public ConversationBuilder templateId(@Nullable String templateId) {
        this.templateId = templateId;
        return this;
    }

    public ConversationBuilder context(Map<String, Object> context) {
        this.context = context;
        return this;
    }

    public ConversationBuilder startTime(Instant startTime) {
        this.startTime = startTime;
        return this;
    }

    public ConversationBuilder state(ConversationState state) {
        this.state = state;
        return this;
    }

    public ConversationBuilder lastActivity(Instant lastActivity) {
        this.lastActivity = lastActivity;
        return this;
    }

    public ConversationBuilder messageCount(int messageCount) {
        this.messageCount = messageCount;
        return this;
    }

    public Conversation build() {
        return Conversation.builder().conversationId(conversationId).participantIds(List.copyOf(participantIds))
                .templateId(templateId).context(context).startTime(startTime).state(state).lastActivity(lastActivity)
                .messageCount(messageCount).build();
    }
}
