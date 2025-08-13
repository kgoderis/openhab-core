package org.openhab.core.ai.agent.communication.messaging;

import java.time.Duration;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Builder for MessageOptions.
 *
 * Provides a fluent API to configure {@link MessageOptions}.
 */
@NonNullByDefault
public class MessageOptionsBuilder {
    boolean encrypted = false;
    boolean persistent = false;
    Duration timeout = Duration.ofMinutes(5);
    int maxRetries = 3;
    Map<String, Object> metadata = Map.of();

    public MessageOptionsBuilder encrypted(boolean encrypted) { this.encrypted = encrypted; return this; }
    public MessageOptionsBuilder persistent(boolean persistent) { this.persistent = persistent; return this; }
    public MessageOptionsBuilder timeout(Duration timeout) { this.timeout = timeout; return this; }
    public MessageOptionsBuilder maxRetries(int maxRetries) { this.maxRetries = maxRetries; return this; }
    public MessageOptionsBuilder metadata(Map<String, Object> metadata) { this.metadata = metadata; return this; }

    public MessageOptions build() { return new MessageOptions(this); }
}


