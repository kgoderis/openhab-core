package org.openhab.core.ai.tool.notifications.api.events;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Notification event for tool operations.
 * 
 * Represents a notification that can be sent to users or systems.
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public class Notification {

    private final String id;
    private final String title;
    private final String message;
    private final NotificationType type;
    private final Instant timestamp;
    private final String source;
    private final @Nullable String userId;
    private final @Nullable String toolId;

    /**
     * Create a new notification.
     * 
     * @param id the notification ID
     * @param title the notification title
     * @param message the notification message
     * @param type the notification type
     * @param source the notification source
     * @param userId the user ID (optional)
     * @param toolId the tool ID (optional)
     */
    public Notification(String id, String title, String message, NotificationType type, String source,
            @Nullable String userId, @Nullable String toolId) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.type = type;
        this.timestamp = Instant.now();
        this.source = source;
        this.userId = userId;
        this.toolId = toolId;
    }

    /**
     * Get the notification ID.
     * 
     * @return the notification ID
     */
    public String getId() {
        return id;
    }

    /**
     * Get the notification title.
     * 
     * @return the notification title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Get the notification message.
     * 
     * @return the notification message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Get the notification type.
     * 
     * @return the notification type
     */
    public NotificationType getType() {
        return type;
    }

    /**
     * Get the notification timestamp.
     * 
     * @return the notification timestamp
     */
    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * Get the notification source.
     * 
     * @return the notification source
     */
    public String getSource() {
        return source;
    }

    /**
     * Get the user ID.
     * 
     * @return the user ID or null if not specified
     */
    @Nullable
    public String getUserId() {
        return userId;
    }

    /**
     * Get the tool ID.
     * 
     * @return the tool ID or null if not specified
     */
    @Nullable
    public String getToolId() {
        return toolId;
    }

    @Override
    public String toString() {
        return "Notification{id='" + id + "', title='" + title + "', type=" + type + ", timestamp=" + timestamp
                + ", source='" + source + "'}";
    }
}
