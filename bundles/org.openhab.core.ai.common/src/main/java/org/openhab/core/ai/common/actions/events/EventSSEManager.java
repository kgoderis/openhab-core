package org.openhab.core.ai.common.actions.events;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.events.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manager for Server-Sent Events (SSE) connections.
 * 
 * This singleton class manages SSE connections for event subscriptions,
 * forwarding events from the EventBus to connected MCP clients.
 */
@NonNullByDefault
public class EventSSEManager {

    private static final Logger logger = LoggerFactory.getLogger(EventSSEManager.class);

    private static @Nullable EventSSEManager instance;

    // subscriptionId -> list of SSE sinks
    private final Map<String, CopyOnWriteArrayList<SSESink>> sinks = new ConcurrentHashMap<>();

    private EventSSEManager() {
        // Private constructor for singleton
    }

    public static EventSSEManager getInstance() {
        if (instance == null) {
            synchronized (EventSSEManager.class) {
                if (instance == null) {
                    instance = new EventSSEManager();
                }
            }
        }
        return instance;
    }

    /**
     * Register an SSE sink for a subscription.
     * 
     * @param subscriptionId The subscription ID
     * @param sink The SSE sink to register
     */
    public void registerSink(String subscriptionId, SSESink sink) {
        sinks.computeIfAbsent(subscriptionId, k -> new CopyOnWriteArrayList<>()).add(sink);
        logger.debug("Registered SSE sink for subscription: {}", subscriptionId);
    }

    /**
     * Unregister an SSE sink for a subscription.
     * 
     * @param subscriptionId The subscription ID
     * @param sink The SSE sink to unregister
     */
    public void unregisterSink(String subscriptionId, SSESink sink) {
        CopyOnWriteArrayList<SSESink> subscriptionSinks = sinks.get(subscriptionId);
        if (subscriptionSinks != null) {
            subscriptionSinks.remove(sink);
            if (subscriptionSinks.isEmpty()) {
                sinks.remove(subscriptionId);
            }
        }
        logger.debug("Unregistered SSE sink for subscription: {}", subscriptionId);
    }

    /**
     * Forward an event to all SSE sinks for a subscription.
     * 
     * @param subscriptionId The subscription ID
     * @param event The event to forward
     */
    public void forwardEvent(String subscriptionId, Event event) {
        CopyOnWriteArrayList<SSESink> subscriptionSinks = sinks.get(subscriptionId);
        if (subscriptionSinks == null || subscriptionSinks.isEmpty()) {
            return;
        }

        String eventData = formatEventAsSSE(event);

        // Forward to all sinks, removing closed ones
        subscriptionSinks.removeIf(sink -> {
            try {
                sink.send(eventData);
                return false; // Keep the sink
            } catch (Exception e) {
                logger.debug("Failed to send event to SSE sink, removing: {}", e.getMessage());
                return true; // Remove the sink
            }
        });

        // Clean up empty subscription
        if (subscriptionSinks.isEmpty()) {
            sinks.remove(subscriptionId);
        }
    }

    /**
     * Remove all sinks for a subscription (when subscription is removed).
     * 
     * @param subscriptionId The subscription ID
     */
    public void removeSubscription(String subscriptionId) {
        sinks.remove(subscriptionId);
        logger.debug("Removed all SSE sinks for subscription: {}", subscriptionId);
    }

    /**
     * Get the number of active SSE connections.
     * 
     * @return Number of active connections
     */
    public int getActiveConnectionCount() {
        return sinks.values().stream().mapToInt(CopyOnWriteArrayList::size).sum();
    }

    /**
     * Get the number of subscriptions with active connections.
     * 
     * @return Number of subscriptions with connections
     */
    public int getSubscriptionCount() {
        return sinks.size();
    }

    /**
     * Format an event as SSE data.
     * 
     * @param event The event to format
     * @return SSE-formatted string
     */
    private String formatEventAsSSE(Event event) {
        StringBuilder sb = new StringBuilder();
        sb.append("data: {\n");
        sb.append("  \"type\": \"").append(event.getType()).append("\",\n");
        sb.append("  \"topic\": \"").append(event.getTopic()).append("\",\n");
        sb.append("  \"source\": \"").append(event.getSource()).append("\",\n");
        sb.append("  \"payload\": \"").append(event.getPayload()).append("\",\n");
        sb.append("  \"timestamp\": \"").append(System.currentTimeMillis()).append("\"\n");
        sb.append("}\n\n");
        return sb.toString();
    }

    /**
     * Interface for SSE sinks.
     */
    public interface SSESink {
        /**
         * Send data to the SSE client.
         * 
         * @param data The data to send
         * @throws IOException if sending fails
         */
        void send(String data) throws IOException;

        /**
         * Check if the sink is still open.
         * 
         * @return true if open, false if closed
         */
        boolean isOpen();
    }
}
