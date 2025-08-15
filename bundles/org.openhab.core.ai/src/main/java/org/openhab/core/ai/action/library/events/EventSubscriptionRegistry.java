package org.openhab.core.ai.action.library.events;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.events.EventSubscriber;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registry for managing event subscriptions for MCP clients with SSE support.
 * 
 * This component manages event subscriptions per client, allowing MCP clients
 * to subscribe to specific event types and filters, then receive events via SSE.
 */
@NonNullByDefault
@Component(service = EventSubscriptionRegistry.class, immediate = true)
public class EventSubscriptionRegistry {

    private static final Logger logger = LoggerFactory.getLogger(EventSubscriptionRegistry.class);

    // subscriptionId -> EventSubscriber
    private final Map<String, EventSubscriber> subscriptions = new ConcurrentHashMap<>();

    // subscriptionId -> SubscriptionInfo
    private final Map<String, SubscriptionInfo> subscriptionInfos = new ConcurrentHashMap<>();

    // clientId -> Set<subscriptionId>
    private final Map<String, CopyOnWriteArrayList<String>> clientSubscriptions = new ConcurrentHashMap<>();

    @Activate
    protected void activate() {
        logger.debug("EventSubscriptionRegistry activated");
    }

    @Deactivate
    protected void deactivate() {
        logger.debug("EventSubscriptionRegistry deactivated");

        // Clean up all subscriptions
        subscriptions.keySet().forEach(this::unsubscribe);
        subscriptions.clear();
        subscriptionInfos.clear();
        clientSubscriptions.clear();
    }

    /**
     * Subscribe a client to events with specified types and filters.
     * 
     * @param clientId The MCP client identifier
     * @param eventTypes Set of event types to subscribe to
     * @param filters Optional filters for event filtering
     * @return SubscriptionInfo containing subscription details
     */
    public SubscriptionInfo subscribe(String clientId, Set<String> eventTypes, @Nullable Map<String, String> filters) {

        String subscriptionId = UUID.randomUUID().toString();

        // Create filtering event subscriber
        EventSubscriber subscriber = new FilteringEventSubscriber(clientId, subscriptionId, eventTypes, filters);

        // Register with EventBus (using EventPublisher for now)
        // Note: In openHAB, EventSubscriber registration is typically done through OSGi service registration
        // This is a simplified approach for the AI bundle

        // Store subscription
        subscriptions.put(subscriptionId, subscriber);

        // Create subscription info
        SubscriptionInfo info = new SubscriptionInfo(subscriptionId, clientId, eventTypes, filters);
        subscriptionInfos.put(subscriptionId, info);

        // Track client subscriptions
        clientSubscriptions.computeIfAbsent(clientId, k -> new CopyOnWriteArrayList<>()).add(subscriptionId);

        logger.debug("Created event subscription: clientId={}, subscriptionId={}, eventTypes={}", clientId,
                subscriptionId, eventTypes);

        return info;
    }

    /**
     * Unsubscribe from events.
     * 
     * @param subscriptionId The subscription ID to remove
     * @return true if subscription was found and removed
     */
    public boolean unsubscribe(String subscriptionId) {
        EventSubscriber subscriber = subscriptions.remove(subscriptionId);
        if (subscriber != null) {
            // Unregister from EventBus (using EventPublisher for now)
            // Note: In openHAB, EventSubscriber unregistration is typically done through OSGi service unregistration

            // Remove subscription info
            SubscriptionInfo info = subscriptionInfos.remove(subscriptionId);
            if (info != null) {
                // Remove from client tracking
                String clientId = info.getClientId();
                CopyOnWriteArrayList<String> clientSubs = clientSubscriptions.get(clientId);
                if (clientSubs != null) {
                    clientSubs.remove(subscriptionId);
                    if (clientSubs.isEmpty()) {
                        clientSubscriptions.remove(clientId);
                    }
                }
            }

            logger.debug("Removed event subscription: subscriptionId={}", subscriptionId);
            return true;
        }

        logger.warn("Attempted to remove non-existent subscription: subscriptionId={}", subscriptionId);
        return false;
    }

    /**
     * List all subscriptions for a client.
     * 
     * @param clientId The client identifier
     * @return List of subscription information
     */
    public List<SubscriptionInfo> listSubscriptions(String clientId) {
        CopyOnWriteArrayList<String> subscriptionIds = clientSubscriptions.get(clientId);
        if (subscriptionIds == null) {
            return List.of();
        }

        return subscriptionIds.stream().map(subscriptionInfos::get).filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Get subscription info by ID.
     * 
     * @param subscriptionId The subscription ID
     * @return SubscriptionInfo or null if not found
     */
    public @Nullable SubscriptionInfo getSubscription(String subscriptionId) {
        return subscriptionInfos.get(subscriptionId);
    }

    /**
     * Check if a subscription exists.
     * 
     * @param subscriptionId The subscription ID
     * @return true if subscription exists
     */
    public boolean hasSubscription(String subscriptionId) {
        return subscriptions.containsKey(subscriptionId);
    }

    /**
     * Get the number of active subscriptions.
     * 
     * @return Number of active subscriptions
     */
    public int getSubscriptionCount() {
        return subscriptions.size();
    }

    /**
     * Get the number of clients with subscriptions.
     * 
     * @return Number of clients with subscriptions
     */
    public int getClientCount() {
        return clientSubscriptions.size();
    }

    /**
     * Information about an event subscription.
     */
    // Inner class extracted to top-level: org.openhab.core.ai.action.library.events.SubscriptionInfo

    /**
     * Filtering event subscriber that forwards events to SSE endpoints.
     */
}
