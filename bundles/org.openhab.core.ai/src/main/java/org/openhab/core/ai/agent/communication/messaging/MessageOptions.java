package org.openhab.core.ai.agent.communication.messaging;

import java.time.Duration;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.builder.MessageOptionsBuilder;

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

    public MessageOptions(MessageOptionsBuilder builder) {
        this.encrypted = builder.isEncrypted();
        this.persistent = builder.isPersistent();
        this.timeout = builder.getTimeout();
        this.maxRetries = builder.getMaxRetries();
        this.metadata = builder.getMetadata();
    }

    public MessageOptions(boolean encrypted, boolean persistent, Duration timeout, int maxRetries,
            Map<String, Object> metadata) {
        this.encrypted = encrypted;
        this.persistent = persistent;
        this.timeout = timeout;
        this.maxRetries = maxRetries;
        this.metadata = metadata;
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
