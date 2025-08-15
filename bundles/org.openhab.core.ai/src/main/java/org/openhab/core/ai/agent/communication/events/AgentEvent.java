package org.openhab.core.ai.agent.communication.events;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.agent.communication.events.api.EventOptions;

/**
 * Agent event data.
 *
 * Immutable representation of an agent-originated event.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class AgentEvent {
    private final String eventId;
    private final String eventType;
    private final String sourceAgentId;
    private final Object payload;
    private final Instant timestamp;
    private final EventOptions options;
    private int retryCount;

    AgentEvent(AgentEventBuilder builder) {
        this.eventId = builder.eventId;
        this.eventType = builder.eventType;
        this.sourceAgentId = builder.sourceAgentId;
        this.payload = builder.payload;
        this.timestamp = builder.timestamp;
        this.options = builder.options;
        this.retryCount = 0;
    }

    public String getEventId() {
        return eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getSourceAgentId() {
        return sourceAgentId;
    }

    public Object getPayload() {
        return payload;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public EventOptions getOptions() {
        return options;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void incrementRetryCount() {
        this.retryCount++;
    }

    public static AgentEventBuilder builder() {
        return new AgentEventBuilder();
    }

    /* Extracted: org.openhab.core.ai.agent.communication.events.AgentEventBuilder */
}
