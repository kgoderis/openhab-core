package org.openhab.core.ai.agent.communication.events;

import java.time.Instant;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

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

    EventSubscription(EventSubscriptionBuilder builder) {
        this.subscriptionId = builder.subscriptionId;
        this.agentId = builder.agentId;
        this.eventTypes = builder.eventTypes;
        this.filter = builder.filter;
        this.handler = builder.handler;
        this.timestamp = builder.timestamp;
    }

    public String getSubscriptionId() { return subscriptionId; }
    public String getAgentId() { return agentId; }
    public Set<String> getEventTypes() { return eventTypes; }
    public @Nullable EventFilter getFilter() { return filter; }
    public EventHandler getHandler() { return handler; }
    public Instant getTimestamp() { return timestamp; }

    public static EventSubscriptionBuilder builder() { return new EventSubscriptionBuilder(); }

    /* Extracted: org.openhab.core.ai.agent.communication.events.EventSubscriptionBuilder */
}
