package org.openhab.core.ai.agent.communication.events;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.agent.communication.events.api.EventOptions;

@NonNullByDefault
public class AgentEventBuilder {
    String eventId;
    String eventType;
    String sourceAgentId;
    Object payload;
    Instant timestamp;
    EventOptions options;

    public AgentEventBuilder eventId(String eventId) {
        this.eventId = eventId;
        return this;
    }

    public AgentEventBuilder eventType(String eventType) {
        this.eventType = eventType;
        return this;
    }

    public AgentEventBuilder sourceAgentId(String sourceAgentId) {
        this.sourceAgentId = sourceAgentId;
        return this;
    }

    public AgentEventBuilder payload(Object payload) {
        this.payload = payload;
        return this;
    }

    public AgentEventBuilder timestamp(Instant timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public AgentEventBuilder options(EventOptions options) {
        this.options = options;
        return this;
    }

    public AgentEvent build() {
        return AgentEvent.builder().eventId(eventId).eventType(eventType).sourceAgentId(sourceAgentId).payload(payload)
                .timestamp(timestamp).options(options).build();
    }
}
