package org.openhab.core.ai.agent.communication.events;

import org.eclipse.jdt.annotation.NonNullByDefault;
import java.time.Instant;
import java.util.Set;

@NonNullByDefault
public class EventSubscriptionBuilder {
    String subscriptionId;
    String agentId;
    Set<String> eventTypes;
    EventFilter filter;
    EventHandler handler;
    Instant timestamp;

    public EventSubscriptionBuilder subscriptionId(String subscriptionId) { this.subscriptionId = subscriptionId; return this; }
    public EventSubscriptionBuilder eventTypes(Set<String> eventTypes) { this.eventTypes = eventTypes; return this; }
    public EventSubscriptionBuilder agentId(String agentId) { this.agentId = agentId; return this; }
    public EventSubscriptionBuilder filter(EventFilter filter) { this.filter = filter; return this; }
    public EventSubscriptionBuilder handler(EventHandler handler) { this.handler = handler; return this; }
    public EventSubscriptionBuilder timestamp(Instant timestamp) { this.timestamp = timestamp; return this; }

    public EventSubscription build() { return EventSubscription.builder().subscriptionId(subscriptionId).eventTypes(eventTypes).filter(filter).handler(handler).timestamp(timestamp).build(); }
}


