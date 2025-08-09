package org.openhab.core.ai.tool.notifications;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.notifications.events.NotificationListener;
import org.openhab.core.ai.tool.notifications.events.NotificationType;

/**
 * Interface for MCP Notification service.
 * 
 * This service provides structured event-driven communication for MCP operations,
 * including tool execution notifications, error alerts, and system events.
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public interface NotificationService {

    /**
     * Send a notification.
     * 
     * @param notificationId the notification ID
     * @param type the notification type
     * @param message the notification message
     * @param data optional notification data
     * @return true if notification was sent successfully
     */
    boolean notify(String notificationId, NotificationType type, String message, @Nullable Map<String, Object> data);

    /**
     * Add a notification listener.
     * 
     * @param listenerId the listener ID
     * @param listener the notification listener
     * @return true if listener was added successfully
     */
    boolean addListener(String listenerId, NotificationListener listener);

    /**
     * Remove a notification listener.
     * 
     * @param listenerId the listener ID
     * @return true if listener was removed successfully
     */
    boolean removeListener(String listenerId);

    /**
     * Get a notification listener by ID.
     * 
     * @param listenerId the listener ID
     * @return the notification listener or null if not found
     */
    @Nullable
    NotificationListener getListener(String listenerId);

    /**
     * Get all notification listeners.
     * 
     * @return map of all listeners
     */
    Map<String, NotificationListener> getAllListeners();

    /**
     * Get the count of active listeners.
     * 
     * @return listener count
     */
    int getListenerCount();

    /**
     * Get performance metrics.
     * 
     * @return performance metrics as a map
     */
    Map<String, Object> getPerformanceMetrics();
}
