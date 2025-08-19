package org.openhab.core.ai.common.builder;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.agent.communication.messaging.MessageOptions;

/**
 * Unified builder for MessageOptions objects.
 *
 * <p>
 * This builder provides a standardized way to create MessageOptions objects
 * with proper validation and consistent API.
 * </p>
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class MessageOptionsBuilder extends CommunicationBuilder<MessageOptions> {

    boolean encrypted = false;
    boolean persistent = false;
    Duration timeout = Duration.ofMinutes(5);
    int maxRetries = 3;
    Map<String, Object> metadata = Map.of();

    /**
     * Set whether the message should be encrypted.
     *
     * @param encrypted true if encrypted, false otherwise
     * @return this builder
     */
    public MessageOptionsBuilder withEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
        return this;
    }

    /**
     * Set whether the message should be persistent.
     *
     * @param persistent true if persistent, false otherwise
     * @return this builder
     */
    public MessageOptionsBuilder withPersistent(boolean persistent) {
        this.persistent = persistent;
        return this;
    }

    /**
     * Set the timeout duration.
     *
     * @param timeout the timeout duration
     * @return this builder
     */
    public MessageOptionsBuilder withTimeout(Duration timeout) {
        this.timeout = Objects.requireNonNull(timeout, "timeout");
        return this;
    }

    /**
     * Set the maximum number of retries.
     *
     * @param maxRetries the maximum number of retries
     * @return this builder
     */
    public MessageOptionsBuilder withMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
        return this;
    }

    /**
     * Set the metadata.
     *
     * @param metadata the metadata
     * @return this builder
     */
    public MessageOptionsBuilder withMetadata(Map<String, Object> metadata) {
        this.metadata = Objects.requireNonNull(metadata, "metadata");
        return this;
    }

    @Override
    public MessageOptions build() {
        validate();
        return new MessageOptions(this);
    }

    @Override
    protected void validate() {
        super.validate();
        // Additional validation specific to MessageOptions
        if (timeout.isNegative() || timeout.isZero()) {
            throw new IllegalArgumentException("timeout must be positive");
        }
        if (maxRetries < 0) {
            throw new IllegalArgumentException("maxRetries must be non-negative");
        }
    }

    @Override
    protected void doReset() {
        super.doReset();
        encrypted = false;
        persistent = false;
        timeout = Duration.ofMinutes(5);
        maxRetries = 3;
    }

    /**
     * Create a new MessageOptionsBuilder instance.
     *
     * @return a new builder instance
     */
    public static MessageOptionsBuilder builder() {
        return new MessageOptionsBuilder();
    }

    /**
     * Create a builder from an existing MessageOptions.
     *
     * @param options the existing options
     * @return a builder with values from the existing options
     */
    public static MessageOptionsBuilder builder(MessageOptions options) {
        Objects.requireNonNull(options, "options");
        return builder().withEncrypted(options.isEncrypted()).withPersistent(options.isPersistent())
                .withTimeout(options.getTimeout()).withMaxRetries(options.getMaxRetries())
                .withMetadata(options.getMetadata());
    }

    // Getters for the MessageOptions constructor
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
        return eventData;
    }
}
