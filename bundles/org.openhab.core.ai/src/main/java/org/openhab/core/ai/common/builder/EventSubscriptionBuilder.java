package org.openhab.core.ai.common.builder;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.agent.communication.events.api.EventFilter;
import org.openhab.core.ai.agent.communication.events.api.EventHandler;
import org.openhab.core.ai.agent.communication.events.api.EventSubscription;

/**
 * Unified builder for EventSubscription objects.
 *
 * <p>
 * This builder provides a standardized way to create EventSubscription objects
 * with proper validation and consistent API.
 * </p>
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class EventSubscriptionBuilder extends CommunicationBuilder<EventSubscription> {

    private @Nullable EventFilter filter;
    private @Nullable EventHandler handler;

    /**
     * Set the event filter.
     *
     * @param filter the event filter
     * @return this builder
     */
    public EventSubscriptionBuilder withFilter(@Nullable EventFilter filter) {
        this.filter = filter;
        return this;
    }

    /**
     * Set the event handler.
     *
     * @param handler the event handler
     * @return this builder
     */
    public EventSubscriptionBuilder withHandler(@Nullable EventHandler handler) {
        this.handler = handler;
        return this;
    }

    /**
     * Set the event types as a Set.
     *
     * @param eventTypes the event types
     * @return this builder
     */
    public EventSubscriptionBuilder withEventTypesSet(Set<String> eventTypes) {
        this.eventTypes = List.copyOf(eventTypes);
        return this;
    }

    @Override
    public EventSubscriptionBuilder withSubscriptionId(String subscriptionId) {
        super.withSubscriptionId(subscriptionId);
        return this;
    }

    @Override
    public EventSubscriptionBuilder withFromAgentId(String fromAgentId) {
        super.withFromAgentId(fromAgentId);
        return this;
    }

    @Override
    public EventSubscriptionBuilder withEventTypes(List<String> eventTypes) {
        super.withEventTypes(eventTypes);
        return this;
    }

    @Override
    public EventSubscriptionBuilder withTimestamp(@Nullable Instant timestamp) {
        super.withTimestamp(timestamp);
        return this;
    }

    @Override
    public EventSubscription build() {
        validate();
        return new EventSubscription(subscriptionId, fromAgentId, Set.copyOf(eventTypes), filter, handler, timestamp);
    }

    @Override
    protected void validate() {
        super.validate();
        // Additional validation specific to EventSubscription
        if (subscriptionId.isBlank()) {
            throw new IllegalArgumentException("subscriptionId must not be blank");
        }
        if (eventTypes.isEmpty()) {
            throw new IllegalArgumentException("eventTypes must not be empty");
        }
    }

    @Override
    protected void doReset() {
        super.doReset();
        filter = null;
        handler = null;
    }

    /**
     * Create a new EventSubscriptionBuilder instance.
     *
     * @return a new builder instance
     */
    public static EventSubscriptionBuilder builder() {
        return new EventSubscriptionBuilder();
    }

    /**
     * Create a builder from an existing EventSubscription.
     *
     * @param subscription the existing subscription
     * @return a builder with values from the existing subscription
     */
    public static EventSubscriptionBuilder builder(EventSubscription subscription) {
        Objects.requireNonNull(subscription, "subscription");
        EventSubscriptionBuilder builder = (EventSubscriptionBuilder) builder()
                .withSubscriptionId(subscription.getSubscriptionId())
                .withEventTypes(List.copyOf(subscription.getEventTypes())).withTimestamp(subscription.getTimestamp());
        return builder.withFilter(subscription.getFilter()).withHandler(subscription.getHandler());
    }

    // Getters for the EventSubscription constructor
    public String getSubscriptionId() {
        return subscriptionId;
    }

    public String getAgentId() {
        return subscriberId;
    }

    public Set<String> getEventTypes() {
        return Set.copyOf(eventTypes);
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
}
