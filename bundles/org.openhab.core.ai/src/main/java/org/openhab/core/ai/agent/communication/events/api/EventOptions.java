package org.openhab.core.ai.agent.communication.events.api;

import java.time.Duration;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Event options.
 *
 * Builder-configurable options used when publishing events.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class EventOptions {
    private final boolean persistent;
    private final Duration timeout;
    private final int maxRetries;
    private final Map<String, Object> metadata;

    EventOptions(EventOptionsBuilder builder) {
        this.persistent = builder.persistent;
        this.timeout = builder.timeout;
        this.maxRetries = builder.maxRetries;
        this.metadata = builder.metadata;
    }

    public boolean isPersistent() {
        return persistent;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public static EventOptionsBuilder builder() {
        return new EventOptionsBuilder();
    }

    /* Extracted: org.openhab.core.ai.agent.communication.events.EventOptionsBuilder */
}
