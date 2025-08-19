package org.openhab.core.ai.agent.communication.events.api;

import java.time.Instant;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.builder.EventSubscriptionBuilder;

/**
 * Event subscription.
 *
 * Captures a subscription for events with optional filter and handler.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class EventSubscription {
    private final String subscriptionId;
    private final String agentId;
    private final Set<String> eventTypes;
    private final @Nullable EventFilter filter;
    private final EventHandler handler;
    private final Instant timestamp;

    public EventSubscription(EventSubscriptionBuilder builder) {
        this.subscriptionId = builder.getSubscriptionId();
        this.agentId = builder.getAgentId();
        this.eventTypes = builder.getEventTypes();
        this.filter = builder.getFilter();
        this.handler = builder.getHandler();
        this.timestamp = builder.getTimestamp() != null ? builder.getTimestamp() : Instant.now();
    }

    public EventSubscription(String subscriptionId, String agentId, Set<String> eventTypes,
            @Nullable EventFilter filter, EventHandler handler, Instant timestamp) {
        this.subscriptionId = subscriptionId;
        this.agentId = agentId;
        this.eventTypes = eventTypes;
        this.filter = filter;
        this.handler = handler;
        this.timestamp = timestamp;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public String getAgentId() {
        return agentId;
    }

    public Set<String> getEventTypes() {
        return eventTypes;
    }

    public @Nullable EventFilter getFilter() {
        return filter;
    }

    public EventHandler getHandler() {
        return handler;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public static EventSubscriptionBuilder builder() {
        return new EventSubscriptionBuilder();
    }
}
