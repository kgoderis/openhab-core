package org.openhab.core.ai.agent.lifecycle;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Message response indicating processing status
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class MessageResponse {
    private final boolean acknowledged;
    private final String response;
    private final long timestamp;

    public MessageResponse(boolean acknowledged, String response) {
        this.acknowledged = acknowledged;
        this.response = response;
        this.timestamp = System.currentTimeMillis();
    }

    public boolean isAcknowledged() {
        return acknowledged;
    }

    public String getResponse() {
        return response;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public static MessageResponse success(String response) {
        return new MessageResponse(true, response);
    }

    public static MessageResponse failure(String error) {
        return new MessageResponse(false, error);
    }
}
