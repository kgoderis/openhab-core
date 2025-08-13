package org.openhab.core.ai.agent.communication.conversation;

import java.time.Duration;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Conversation configuration settings.
 *
 * Holds tunable parameters for the conversation subsystem.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ConversationConfiguration {
    private Duration conversationTimeout = Duration.ofMinutes(30);
    private Duration historyRetentionPeriod = Duration.ofDays(30);
    private int maxParticipants = 10;
    private int maxMessageLength = 1000;
    private boolean enableAnalytics = true;
    private boolean enableExport = true;

    public Duration getConversationTimeout() { return conversationTimeout; }
    public void setConversationTimeout(Duration conversationTimeout) { this.conversationTimeout = conversationTimeout; }
    public Duration getHistoryRetentionPeriod() { return historyRetentionPeriod; }
    public void setHistoryRetentionPeriod(Duration historyRetentionPeriod) { this.historyRetentionPeriod = historyRetentionPeriod; }
    public int getMaxParticipants() { return maxParticipants; }
    public void setMaxParticipants(int maxParticipants) { this.maxParticipants = maxParticipants; }
    public int getMaxMessageLength() { return maxMessageLength; }
    public void setMaxMessageLength(int maxMessageLength) { this.maxMessageLength = maxMessageLength; }
    public boolean isEnableAnalytics() { return enableAnalytics; }
    public void setEnableAnalytics(boolean enableAnalytics) { this.enableAnalytics = enableAnalytics; }
    public boolean isEnableExport() { return enableExport; }
    public void setEnableExport(boolean enableExport) { this.enableExport = enableExport; }
}
