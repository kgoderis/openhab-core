package org.openhab.core.ai.agent.lifecycle;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Agent message with metadata
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record AgentMessage(String messageId, String senderId, String content, long timestamp) {
    public AgentMessage {
        if (messageId == null || messageId.trim().isEmpty()) {
            throw new IllegalArgumentException("Message ID cannot be null or empty");
        }
        if (senderId == null || senderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Sender ID cannot be null or empty");
        }
        if (content == null) {
            throw new IllegalArgumentException("Message content cannot be null");
        }
    }
}
