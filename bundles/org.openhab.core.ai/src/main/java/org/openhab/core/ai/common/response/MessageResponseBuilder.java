package org.openhab.core.ai.common.response;

import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.builder.AbstractBuilder;

/**
 * Builder for {@link MessageResponse} in the unified response hierarchy.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class MessageResponseBuilder extends AbstractBuilder<MessageResponse> {

    private @Nullable String id;
    private String message = "";
    private long timestamp = System.currentTimeMillis();
    private String messageType = "INFO";
    private String source = "system";
    private Map<String, Object> metadata = Map.of();
    private @Nullable String errorMessage;

    public MessageResponseBuilder withId(String id) {
        this.id = Objects.requireNonNull(id, "id");
        return this;
    }

    public MessageResponseBuilder withMessage(String message) {
        this.message = Objects.requireNonNull(message, "message");
        return this;
    }

    public MessageResponseBuilder withTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public MessageResponseBuilder withMessageType(String messageType) {
        this.messageType = Objects.requireNonNull(messageType, "messageType");
        return this;
    }

    public MessageResponseBuilder withSource(String source) {
        this.source = Objects.requireNonNull(source, "source");
        return this;
    }

    public MessageResponseBuilder withMetadata(Map<String, Object> metadata) {
        this.metadata = Objects.requireNonNull(metadata, "metadata");
        return this;
    }

    public MessageResponseBuilder withErrorMessage(@Nullable String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    @Override
    protected void validate() {
        validateRequiredString(message, "message");
        validateRequiredString(messageType, "messageType");
        validateRequiredString(source, "source");
    }

    @Override
    protected void doReset() {
        id = null;
        message = "";
        timestamp = System.currentTimeMillis();
        messageType = "INFO";
        source = "system";
        metadata = Map.of();
        errorMessage = null;
    }

    @Override
    public MessageResponse build() {
        if (!isValid()) {
            throw new IllegalArgumentException("Invalid MessageResponseBuilder state: " + getValidationErrors());
        }
        String resolvedId = id != null ? id
                : ("message-response-" + System.currentTimeMillis() + "-" + System.nanoTime());
        return new MessageResponse(resolvedId, message, timestamp, messageType, source, metadata, errorMessage);
    }
}
