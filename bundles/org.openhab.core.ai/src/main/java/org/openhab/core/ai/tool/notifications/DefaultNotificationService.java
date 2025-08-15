package org.openhab.core.ai.tool.notifications;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.notifications.api.events.Notification;
import org.openhab.core.ai.tool.notifications.api.events.NotificationListener;
import org.openhab.core.ai.tool.notifications.api.events.NotificationType;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default Notification Service implementation for MCP Utilities
 * 
 * Provides structured event-driven communication for MCP operations,
 * including tool execution notifications, error alerts, and system events.
 * 
 * @author Karel Goderis - Initial Contribution
 */
@Component(service = NotificationService.class, immediate = true)
@NonNullByDefault
public class DefaultNotificationService implements NotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultNotificationService.class);

    /** Map of notification listeners by ID */
    private final Map<String, NotificationListener> listeners = new ConcurrentHashMap<>();

    /** Performance monitoring */
    private final AtomicLong totalNotifications = new AtomicLong(0);
    private final AtomicLong successfulNotifications = new AtomicLong(0);
    private final AtomicLong failedNotifications = new AtomicLong(0);
    private final AtomicLong totalResponseTimeMs = new AtomicLong(0);

    @Activate
    public DefaultNotificationService() {
        LOGGER.debug("Initializing Notification Manager");
    }

    @Deactivate
    public void deactivate() {
        LOGGER.debug("Deactivating OpenHAB Notification Service");
        listeners.clear();
    }

    @Modified
    public void modified() {
        LOGGER.debug("Modifying OpenHAB Notification Service");
    }

    @Override
    public boolean notify(String notificationId, NotificationType type, String message,
            @Nullable Map<String, Object> data) {
        totalNotifications.incrementAndGet();
        long startTime = System.currentTimeMillis();

        try {
            LOGGER.debug("Sending notification: {} - Type: {} - Message: {}", notificationId, type, message);

            // Validate input parameters
            if (notificationId == null || notificationId.trim().isEmpty()) {
                throw new IllegalArgumentException("Notification ID cannot be null or empty");
            }
            if (type == null) {
                throw new IllegalArgumentException("Notification type cannot be null");
            }
            if (message == null || message.trim().isEmpty()) {
                throw new IllegalArgumentException("Notification message cannot be null or empty");
            }

            // Create notification object (title = message, source = NotificationManager)
            Notification notification = new Notification(notificationId, message, message, type, "NotificationManager",
                    null, null);

            // Send to all registered listeners
            boolean sentToAll = true;
            for (NotificationListener listener : listeners.values()) {
                try {
                    listener.onNotification(notification);
                } catch (Exception e) {
                    sentToAll = false;
                    LOGGER.error("Error sending notification to listener: {}", listener.getListenerId(), e);
                }
            }

            // Update counters
            if (sentToAll) {
                successfulNotifications.incrementAndGet();
                LOGGER.info("Notification sent successfully: {} - Type: {} - Recipients: {}", notificationId, type,
                        listeners.size());
            } else {
                failedNotifications.incrementAndGet();
                LOGGER.warn("Notification partially failed: {} - Type: {}", notificationId, type);
            }

            return sentToAll;

        } catch (Exception e) {
            LOGGER.error("Error sending notification: {} - Type: {}", notificationId, type, e);
            failedNotifications.incrementAndGet();
            return false;
        } finally {
            long responseTime = System.currentTimeMillis() - startTime;
            totalResponseTimeMs.addAndGet(responseTime);
        }
    }

    @Override
    public boolean addListener(String listenerId, NotificationListener listener) {
        try {
            LOGGER.debug("Adding notification listener: {}", listenerId);

            if (listenerId == null || listenerId.trim().isEmpty()) {
                throw new IllegalArgumentException("Listener ID cannot be null or empty");
            }
            if (listener == null) {
                throw new IllegalArgumentException("Listener cannot be null");
            }

            NotificationListener existing = listeners.put(listenerId, listener);
            if (existing != null) {
                LOGGER.warn("Replaced existing notification listener: {}", listenerId);
            } else {
                LOGGER.info("Added notification listener: {}", listenerId);
            }

            return true;

        } catch (Exception e) {
            LOGGER.error("Error adding notification listener: {}", listenerId, e);
            return false;
        }
    }

    @Override
    public boolean removeListener(String listenerId) {
        try {
            LOGGER.debug("Removing notification listener: {}", listenerId);

            NotificationListener removed = listeners.remove(listenerId);
            if (removed != null) {
                LOGGER.info("Removed notification listener: {}", listenerId);
                return true;
            } else {
                LOGGER.warn("Notification listener not found for removal: {}", listenerId);
                return false;
            }

        } catch (Exception e) {
            LOGGER.error("Error removing notification listener: {}", listenerId, e);
            return false;
        }
    }

    @Override
    public @Nullable NotificationListener getListener(String listenerId) {
        return listeners.get(listenerId);
    }

    @Override
    public Map<String, NotificationListener> getAllListeners() {
        return new ConcurrentHashMap<>(listeners);
    }

    @Override
    public int getListenerCount() {
        return listeners.size();
    }

    @Override
    public Map<String, Object> getPerformanceMetrics() {
        Map<String, Object> metrics = new ConcurrentHashMap<>();
        metrics.put("totalNotifications", totalNotifications.get());
        metrics.put("successfulNotifications", successfulNotifications.get());
        metrics.put("failedNotifications", failedNotifications.get());
        metrics.put("activeListeners", listeners.size());
        metrics.put("totalResponseTimeMs", totalResponseTimeMs.get());
        metrics.put("averageResponseTimeMs",
                totalNotifications.get() > 0 ? totalResponseTimeMs.get() / totalNotifications.get() : 0);
        metrics.put("successRate",
                totalNotifications.get() > 0 ? (double) successfulNotifications.get() / totalNotifications.get() : 0.0);
        return metrics;
    }

    // Removed DefaultNotification: using org.openhab.core.ai.tool.notifications.events.Notification class instead
}
