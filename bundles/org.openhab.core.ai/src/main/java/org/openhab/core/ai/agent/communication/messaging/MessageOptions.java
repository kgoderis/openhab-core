package org.openhab.core.ai.agent.communication.messaging;

import java.time.Duration;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Message options.
 *
 * Builder-configurable options used when sending messages.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class MessageOptions {
    private final boolean encrypted;
    private final boolean persistent;
    private final Duration timeout;
    private final int maxRetries;
    private final Map<String, Object> metadata;

    MessageOptions(MessageOptionsBuilder builder) {
        this.encrypted = builder.encrypted;
        this.persistent = builder.persistent;
        this.timeout = builder.timeout;
        this.maxRetries = builder.maxRetries;
        this.metadata = builder.metadata;
    }

    public boolean isEncrypted() {
        return encrypted;
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

    public static MessageOptionsBuilder builder() {
        return new MessageOptionsBuilder();
    }
}
