package org.openhab.core.ai.agent.communication.events.api;

import java.time.Duration;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Builder for EventOptions.
 */
@NonNullByDefault
public class EventOptionsBuilder {
    boolean persistent = false;
    Duration timeout = Duration.ofMinutes(5);
    int maxRetries = 3;
    Map<String, Object> metadata = Map.of();

    public EventOptionsBuilder persistent(boolean persistent) {
        this.persistent = persistent;
        return this;
    }

    public EventOptionsBuilder timeout(Duration timeout) {
        this.timeout = timeout;
        return this;
    }

    public EventOptionsBuilder maxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
        return this;
    }

    public EventOptionsBuilder metadata(Map<String, Object> metadata) {
        this.metadata = metadata;
        return this;
    }

    public EventOptions build() {
        return EventOptions.builder().persistent(persistent).timeout(timeout).maxRetries(maxRetries).metadata(metadata)
                .build();
    }
}
