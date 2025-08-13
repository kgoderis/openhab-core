package org.openhab.core.ai.agent.communication.conversation;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Builder for ConversationExport.
 */
@NonNullByDefault
public class ConversationExportBuilder {
    Conversation conversation;
    ConversationHistory history;
    Instant exportTime;

    public ConversationExportBuilder conversation(Conversation conversation) { this.conversation = conversation; return this; }
    public ConversationExportBuilder history(ConversationHistory history) { this.history = history; return this; }
    public ConversationExportBuilder exportTime(Instant exportTime) { this.exportTime = exportTime; return this; }

    public ConversationExport build() { return ConversationExport.builder().conversation(conversation).history(history).exportTime(exportTime).build(); }
}
