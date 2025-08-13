package org.openhab.core.ai.reasoning;

import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Event DTO for autonomous processing.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class Event {
    private final String id;
    private final String type;
    private final String agentId;
    private final Instant timestamp;
    private final Map<String, Object> data;

    public Event(String id, String type, String agentId, Map<String, Object> data) {
        this.id = id;
        this.type = type;
        this.agentId = agentId;
        this.timestamp = Instant.now();
        this.data = data;
    }

    public String getId() { return id; }
    public String getType() { return type; }
    public String getAgentId() { return agentId; }
    public Instant getTimestamp() { return timestamp; }
    public Map<String, Object> getData() { return data; }
}


