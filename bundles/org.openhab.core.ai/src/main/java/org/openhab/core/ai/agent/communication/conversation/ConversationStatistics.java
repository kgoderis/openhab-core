package org.openhab.core.ai.agent.communication.conversation;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Conversation statistics snapshot.
 *
 * Aggregates counts and totals for conversation subsystem.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ConversationStatistics {
    private final long totalConversations;
    private final long totalMessages;
    private final long totalConversationTime;
    private final long totalParticipants;
    private final int activeConversations;
    private final int conversationHistories;
    private final int conversationTemplates;
    private final int conversationPatterns;

    public ConversationStatistics(long totalConversations, long totalMessages, long totalConversationTime,
            long totalParticipants, int activeConversations, int conversationHistories, int conversationTemplates,
            int conversationPatterns) {
        this.totalConversations = totalConversations;
        this.totalMessages = totalMessages;
        this.totalConversationTime = totalConversationTime;
        this.totalParticipants = totalParticipants;
        this.activeConversations = activeConversations;
        this.conversationHistories = conversationHistories;
        this.conversationTemplates = conversationTemplates;
        this.conversationPatterns = conversationPatterns;
    }

    public long getTotalConversations() { return totalConversations; }
    public long getTotalMessages() { return totalMessages; }
    public long getTotalConversationTime() { return totalConversationTime; }
    public long getTotalParticipants() { return totalParticipants; }
    public int getActiveConversations() { return activeConversations; }
    public int getConversationHistories() { return conversationHistories; }
    public int getConversationTemplates() { return conversationTemplates; }
    public int getConversationPatterns() { return conversationPatterns; }
}
