package org.openhab.core.ai.tool.notifications.events;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Event for tool system notifications.
 * 
 * This class represents a notification event that can be sent to users
 * or other systems to inform about tool operations and status changes.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class SystemEvent {

    private final String id;
    private final String type;
    private final String title;
    private final String message;
    private final String severity;
    private final String timestamp;
    private final Map<String, Object> metadata;

    /**
     * Create a new notification event.
     * 
     * @param id the event ID
     * @param type the event type
     * @param title the event title
     * @param message the event message
     * @param severity the event severity
     * @param timestamp the event timestamp
     * @param metadata additional event metadata
     */
    public SystemEvent(String id, String type, String title, String message, String severity, String timestamp,
            Map<String, Object> metadata) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.message = message;
        this.severity = severity;
        this.timestamp = timestamp;
        this.metadata = metadata;
    }

    /**
     * Get the event ID.
     * 
     * @return the event ID
     */
    public String getId() {
        return id;
    }

    /**
     * Get the event type.
     * 
     * @return the event type
     */
    public String getType() {
        return type;
    }

    /**
     * Get the event title.
     * 
     * @return the event title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Get the event message.
     * 
     * @return the event message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Get the event severity.
     * 
     * @return the event severity
     */
    public String getSeverity() {
        return severity;
    }

    /**
     * Get the event timestamp.
     * 
     * @return the event timestamp
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * Get the event metadata.
     * 
     * @return the event metadata
     */
    public Map<String, Object> getMetadata() {
        return metadata;
    }
}
