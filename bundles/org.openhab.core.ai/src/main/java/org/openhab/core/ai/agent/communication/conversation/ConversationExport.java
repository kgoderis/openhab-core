package org.openhab.core.ai.agent.communication.conversation;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Conversation export bundle.
 *
 * DTO used to export a conversation and its history snapshot.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ConversationExport {
    private final Conversation conversation;
    private final ConversationHistory history;
    private final Instant exportTime;

    ConversationExport(ConversationExportBuilder builder) {
        this.conversation = builder.conversation;
        this.history = builder.history;
        this.exportTime = builder.exportTime;
    }

    public Conversation getConversation() { return conversation; }
    public ConversationHistory getHistory() { return history; }
    public Instant getExportTime() { return exportTime; }

    public static ConversationExportBuilder builder() { return new ConversationExportBuilder(); }

    /* Extracted: org.openhab.core.ai.agent.communication.conversation.ConversationExportBuilder */
}
