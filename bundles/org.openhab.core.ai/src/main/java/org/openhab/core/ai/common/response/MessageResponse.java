package org.openhab.core.ai.common.response;

import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Response implementation for simple message responses.
 * 
 * This class represents simple message responses,
 * implementing the unified Response interface.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class MessageResponse implements Response<String> {

    private final String id;
    private final String message;
    private final long timestamp;
    private final String messageType;
    private final String source;
    private final Map<String, Object> metadata;
    private final @Nullable String errorMessage;

    /**
     * Create a new MessageResponse.
     * 
     * @param id the response ID
     * @param message the message content
     * @param timestamp the response timestamp
     * @param messageType the type of message
     * @param source the source of the message
     * @param metadata additional metadata
     * @param errorMessage the error message if any
     */
    public MessageResponse(String id, String message, long timestamp, String messageType, String source,
            Map<String, Object> metadata, @Nullable String errorMessage) {
        this.id = Objects.requireNonNull(id, "id");
        this.message = Objects.requireNonNull(message, "message");
        this.timestamp = timestamp;
        this.messageType = Objects.requireNonNull(messageType, "messageType");
        this.source = Objects.requireNonNull(source, "source");
        this.metadata = Objects.requireNonNull(metadata, "metadata");
        this.errorMessage = errorMessage;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isSuccess() {
        return errorMessage == null;
    }

    @Override
    public String getData() {
        return message;
    }

    @Override
    public @Nullable String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Get the message content.
     * 
     * @return the message content
     */
    public String getMessage() {
        return message;
    }

    /**
     * Get the type of message.
     * 
     * @return the message type
     */
    public String getMessageType() {
        return messageType;
    }

    /**
     * Get the source of the message.
     * 
     * @return the message source
     */
    public String getSource() {
        return source;
    }

    /**
     * Get additional metadata for this response.
     * 
     * @return the metadata map
     */
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    /**
     * Create a successful message response.
     * 
     * @param message the message content
     * @param messageType the type of message
     * @param source the source of the message
     * @return a successful MessageResponse
     */
    public static MessageResponse success(String message, String messageType, String source) {
        return new MessageResponse(generateId(), message, System.currentTimeMillis(), messageType, source, Map.of(),
                null);
    }

    /**
     * Create an error message response.
     * 
     * @param errorMessage the error message
     * @param messageType the type of message
     * @param source the source of the message
     * @return an error MessageResponse
     */
    public static MessageResponse error(String errorMessage, String messageType, String source) {
        return new MessageResponse(generateId(), "", System.currentTimeMillis(), messageType, source, Map.of(),
                errorMessage);
    }

    /**
     * Create an info message response.
     * 
     * @param message the message content
     * @param source the source of the message
     * @return an info MessageResponse
     */
    public static MessageResponse info(String message, String source) {
        return new MessageResponse(generateId(), message, System.currentTimeMillis(), "INFO", source, Map.of(), null);
    }

    /**
     * Create a warning message response.
     * 
     * @param message the message content
     * @param source the source of the message
     * @return a warning MessageResponse
     */
    public static MessageResponse warning(String message, String source) {
        return new MessageResponse(generateId(), message, System.currentTimeMillis(), "WARNING", source, Map.of(),
                null);
    }

    /**
     * Create a debug message response.
     * 
     * @param message the message content
     * @param source the source of the message
     * @return a debug MessageResponse
     */
    public static MessageResponse debug(String message, String source) {
        return new MessageResponse(generateId(), message, System.currentTimeMillis(), "DEBUG", source, Map.of(), null);
    }

    private static String generateId() {
        return "message-response-" + System.currentTimeMillis() + "-" + System.nanoTime();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        MessageResponse other = (MessageResponse) obj;
        return Objects.equals(id, other.id) && Objects.equals(message, other.message) && timestamp == other.timestamp
                && Objects.equals(messageType, other.messageType) && Objects.equals(source, other.source)
                && Objects.equals(metadata, other.metadata) && Objects.equals(errorMessage, other.errorMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, message, timestamp, messageType, source, metadata, errorMessage);
    }

    @Override
    public String toString() {
        return "MessageResponse{" + "id='" + id + '\'' + ", message='" + message + '\'' + ", timestamp=" + timestamp
                + ", messageType='" + messageType + '\'' + ", source='" + source + '\'' + ", metadata=" + metadata
                + ", errorMessage='" + errorMessage + '\'' + '}';
    }
}
