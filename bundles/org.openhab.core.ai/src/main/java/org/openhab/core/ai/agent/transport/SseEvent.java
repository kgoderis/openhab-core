package org.openhab.core.ai.agent.transport;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * SSE Event representation for agent transport
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class SseEvent {
    private final String eventType;
    private final String data;
    private final String id;
    private final long timestamp;

    public SseEvent(String eventType, String data, String id) {
        this.eventType = eventType;
        this.data = data;
        this.id = id;
        this.timestamp = System.currentTimeMillis();
    }

    public String getEventType() {
        return eventType;
    }

    public String getData() {
        return data;
    }

    public String getId() {
        return id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String toSseFormat() {
        StringBuilder sb = new StringBuilder();
        if (id != null) {
            sb.append("id: ").append(id).append("\n");
        }
        if (eventType != null) {
            sb.append("event: ").append(eventType).append("\n");
        }
        sb.append("data: ").append(data).append("\n\n");
        return sb.toString();
    }
}
