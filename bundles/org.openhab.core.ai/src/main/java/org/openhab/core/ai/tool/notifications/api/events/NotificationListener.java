package org.openhab.core.ai.tool.notifications.api.events;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Listener interface for notification events.
 * 
 * Implementations can register to receive notification events.
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public interface NotificationListener {

    /**
     * Handle a notification event.
     * 
     * @param notification the notification event
     */
    void onNotification(Notification notification);

    /**
     * Get the listener ID.
     * 
     * @return the listener ID
     */
    String getListenerId();

    /**
     * Check if the listener is active.
     * 
     * @return true if the listener is active
     */
    boolean isActive();

    /**
     * Get the listener priority.
     * 
     * @return the listener priority (lower numbers = higher priority)
     */
    int getPriority();
}
