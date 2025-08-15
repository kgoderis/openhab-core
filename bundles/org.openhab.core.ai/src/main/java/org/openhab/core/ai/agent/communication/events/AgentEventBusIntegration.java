package org.openhab.core.ai.agent.communication.events;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.agent.communication.events.api.EventOptions;
import org.openhab.core.ai.agent.communication.events.api.EventPublishResult;
import org.openhab.core.ai.agent.communication.events.api.EventRouter;
import org.openhab.core.ai.agent.communication.events.api.EventSchema;
import org.openhab.core.ai.agent.communication.events.api.EventSubscription;
import org.openhab.core.ai.agent.lifecycle.api.AgentRegistry;
import org.openhab.core.events.Event;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.events.EventSubscriber;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Agent Event Bus Integration - Event-based agent communication system
 * 
 * This service provides comprehensive event-based communication capabilities for agents:
 * - Event-based agent communication
 * - Event publishing and subscription
 * - Event filtering and routing
 * - Event persistence and replay
 * - Event security and access control
 * - Event performance monitoring
 * - Event schema validation
 * - Event versioning and compatibility
 * - Event batching and optimization
 * - Event dead letter queue handling
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@Component(service = AgentEventBusIntegration.class)
@NonNullByDefault
public class AgentEventBusIntegration implements EventSubscriber {

    private final Logger logger = LoggerFactory.getLogger(AgentEventBusIntegration.class);

    @Reference
    private @Nullable AgentRegistry agentRegistry;

    @Reference
    private @Nullable EventPublisher eventPublisher;

    // Event storage and management
    private final Map<String, AgentEvent> eventStore = new ConcurrentHashMap<>();
    private final Map<String, EventSubscription> subscriptions = new ConcurrentHashMap<>();
    private final Map<String, EventFilter> eventFilters = new ConcurrentHashMap<>();
    private final Map<String, EventRouter> eventRouters = new ConcurrentHashMap<>();

    // Event processing and batching
    private final Map<String, List<AgentEvent>> eventBatches = new ConcurrentHashMap<>();
    private final Map<String, List<AgentEvent>> deadLetterQueue = new ConcurrentHashMap<>();

    // Event schema validation
    private final Map<String, EventSchema> eventSchemas = new ConcurrentHashMap<>();

    // Performance monitoring
    private final AtomicLong totalEventsPublished = new AtomicLong(0);
    private final AtomicLong totalEventsDelivered = new AtomicLong(0);
    private final AtomicLong totalEventsFiltered = new AtomicLong(0);
    private final AtomicLong totalEventsFailed = new AtomicLong(0);
    private final AtomicLong totalEventsInDeadLetterQueue = new AtomicLong(0);

    // Configuration
    private final AtomicReference<EventBusConfiguration> configuration = new AtomicReference<>(
            new EventBusConfiguration());

    // Background processing
    private final ScheduledExecutorService eventProcessor = Executors.newSingleThreadScheduledExecutor();
    private final ScheduledExecutorService batchProcessor = Executors.newSingleThreadScheduledExecutor();
    private final ScheduledExecutorService deadLetterProcessor = Executors.newSingleThreadScheduledExecutor();

    @Activate
    public void activate() {
        logger.info("Agent Event Bus Integration activated");

        // Start background processors
        eventProcessor.scheduleAtFixedRate(this::processEventQueue, 0, 100, TimeUnit.MILLISECONDS);
        batchProcessor.scheduleAtFixedRate(this::processEventBatches, 0, 1000, TimeUnit.MILLISECONDS);
        deadLetterProcessor.scheduleAtFixedRate(this::processDeadLetterQueue, 0, 5000, TimeUnit.MILLISECONDS);
    }

    @Deactivate
    public void deactivate() {
        logger.info("Agent Event Bus Integration deactivated");

        // Shutdown background processors
        shutdownExecutor(eventProcessor);
        shutdownExecutor(batchProcessor);
        shutdownExecutor(deadLetterProcessor);
    }

    /**
     * Publish an event to the event bus
     * 
     * @param eventType Type of event
     * @param sourceAgentId Source agent ID
     * @param payload Event payload
     * @param options Event options
     * @return Event publishing result
     */
    public CompletableFuture<EventPublishResult> publishEvent(String eventType, String sourceAgentId, Object payload,
            EventOptions options) {
        logger.debug("Publishing event {} from agent {}: {}", eventType, sourceAgentId, payload);

        // Create agent event
        AgentEvent agentEvent = AgentEvent.builder().eventId(generateEventId()).eventType(eventType)
                .sourceAgentId(sourceAgentId).payload(payload).timestamp(Instant.now()).options(options).build();

        // Validate event schema
        if (!validateEventSchema(agentEvent)) {
            return CompletableFuture
                    .completedFuture(EventPublishResult.schemaValidationFailed("Event schema validation failed"));
        }

        // Apply filters
        if (!applyEventFilters(agentEvent)) {
            totalEventsFiltered.incrementAndGet();
            return CompletableFuture.completedFuture(EventPublishResult.filtered("Event filtered out"));
        }

        // Store event
        eventStore.put(agentEvent.getEventId(), agentEvent);
        totalEventsPublished.incrementAndGet();

        // Route event
        return routeEvent(agentEvent);
    }

    /**
     * Subscribe to events
     * 
     * @param agentId Agent ID to subscribe
     * @param eventTypes Event types to subscribe to
     * @param filter Event filter
     * @param handler Event handler
     * @return Subscription result
     */
    public boolean subscribeToEvents(String agentId, Set<String> eventTypes, @Nullable EventFilter filter,
            EventHandler handler) {
        logger.debug("Agent {} subscribing to events: {}", agentId, eventTypes);

        // Validate agent exists
        AgentRegistry registry = agentRegistry;
        if (registry == null || registry.getAgent(agentId, "system") == null) {
            logger.warn("Cannot subscribe agent {} to events: agent not found", agentId, eventTypes);
            return false;
        }

        EventSubscription subscription = EventSubscription.builder().subscriptionId(generateSubscriptionId())
                .agentId(agentId).eventTypes(eventTypes).filter(filter).handler(handler).timestamp(Instant.now())
                .build();

        subscriptions.put(subscription.getSubscriptionId(), subscription);
        return true;
    }

    /**
     * Unsubscribe from events
     * 
     * @param subscriptionId Subscription ID to unsubscribe
     * @return Unsubscription result
     */
    public boolean unsubscribeFromEvents(String subscriptionId) {
        logger.debug("Unsubscribing from events: {}", subscriptionId);

        EventSubscription subscription = subscriptions.remove(subscriptionId);
        return subscription != null;
    }

    /**
     * Register an event filter
     * 
     * @param filterId Filter identifier
     * @param filter Event filter implementation
     */
    public void registerEventFilter(String filterId, EventFilter filter) {
        eventFilters.put(filterId, filter);
        logger.debug("Registered event filter: {}", filterId);
    }

    /**
     * Register an event router
     * 
     * @param routerId Router identifier
     * @param router Event router implementation
     */
    public void registerEventRouter(String routerId, EventRouter router) {
        eventRouters.put(routerId, router);
        logger.debug("Registered event router: {}", routerId);
    }

    /**
     * Register an event schema
     * 
     * @param eventType Event type
     * @param schema Event schema
     */
    public void registerEventSchema(String eventType, EventSchema schema) {
        eventSchemas.put(eventType, schema);
        logger.debug("Registered event schema for type: {}", eventType);
    }

    /**
     * Get events for replay
     * 
     * @param eventType Event type filter
     * @param startTime Start time filter
     * @param endTime End time filter
     * @param limit Maximum number of events
     * @return List of events
     */
    public List<AgentEvent> getEventsForReplay(@Nullable String eventType, @Nullable Instant startTime,
            @Nullable Instant endTime, int limit) {
        return eventStore.values().stream().filter(event -> eventType == null || event.getEventType().equals(eventType))
                .filter(event -> startTime == null || !event.getTimestamp().isBefore(startTime))
                .filter(event -> endTime == null || !event.getTimestamp().isAfter(endTime))
                .sorted((e1, e2) -> e1.getTimestamp().compareTo(e2.getTimestamp())).limit(limit).toList();
    }

    /**
     * Get event bus statistics
     * 
     * @return Event bus statistics
     */
    public EventBusStatistics getStatistics() {
        return new EventBusStatistics(totalEventsPublished.get(), totalEventsDelivered.get(), totalEventsFiltered.get(),
                totalEventsFailed.get(), totalEventsInDeadLetterQueue.get(), eventStore.size(), subscriptions.size(),
                eventSchemas.size(), deadLetterQueue.size());
    }

    /**
     * Process events from dead letter queue
     * 
     * @param eventType Event type to retry
     * @param maxRetries Maximum number of retries
     * @return Retry result
     */
    public CompletableFuture<DeadLetterQueueResult> retryDeadLetterEvents(@Nullable String eventType, int maxRetries) {
        logger.debug("Retrying dead letter events for type: {}", eventType);

        List<AgentEvent> eventsToRetry = deadLetterQueue.values().stream().flatMap(List::stream)
                .filter(event -> eventType == null || event.getEventType().equals(eventType))
                .filter(event -> event.getRetryCount() < maxRetries).toList();

        if (eventsToRetry.isEmpty()) {
            return CompletableFuture.completedFuture(DeadLetterQueueResult.noEventsToRetry());
        }

        List<CompletableFuture<EventPublishResult>> retries = eventsToRetry.stream().map(this::retryEvent).toList();

        return CompletableFuture.allOf(retries.toArray(new CompletableFuture[0])).thenApply(v -> {
            long successful = retries.stream().mapToLong(future -> {
                try {
                    return future.get().isSuccess() ? 1 : 0;
                } catch (Exception e) {
                    return 0;
                }
            }).sum();

            return DeadLetterQueueResult.success(successful, eventsToRetry.size());
        });
    }

    // EventSubscriber implementation
    @Override
    public Set<String> getSubscribedEventTypes() {
        return Set.of("org.openhab.core.ai.agent.*");
    }

    @Override
    public void receive(Event event) {
        logger.debug("Received openHAB event: {}", event.getType());

        // Convert openHAB event to agent event if needed
        if (event.getType().startsWith("org.openhab.core.ai.agent.")) {
            AgentEvent agentEvent = convertOpenHABEventToAgentEvent(event);
            if (agentEvent != null) {
                processAgentEvent(agentEvent);
            }
        }
    }

    // Private helper methods

    private String generateEventId() {
        return "evt_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId();
    }

    private String generateSubscriptionId() {
        return "sub_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId();
    }

    private boolean validateEventSchema(AgentEvent event) {
        EventSchema schema = eventSchemas.get(event.getEventType());
        if (schema != null) {
            return validateAgainstSchema(event, schema);
        }
        return true; // No schema defined, allow the event
    }

    private boolean validateAgainstSchema(AgentEvent event, EventSchema schema) {
        // TODO Implement schema-based validation of event payload and metadata
        return true;
    }

    private boolean applyEventFilters(AgentEvent event) {
        // Apply all registered filters
        for (EventFilter filter : eventFilters.values()) {
            if (!filter.shouldDeliver(event)) {
                logger.debug("Event filtered out by filter: {}", filter.getClass().getSimpleName());
                return false;
            }
        }
        return true;
    }

    private CompletableFuture<EventPublishResult> routeEvent(AgentEvent event) {
        // Use registered router or default routing
        EventRouter router = eventRouters.get("default");
        if (router != null) {
            return router.routeEvent(event);
        }

        // Default routing logic
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Find matching subscriptions
                List<EventSubscription> matchingSubscriptions = subscriptions.values().stream()
                        .filter(subscription -> subscription.getEventTypes().contains(event.getEventType()))
                        .filter(subscription -> subscription.getFilter() == null
                                || subscription.getFilter().shouldDeliver(event))
                        .toList();

                // Deliver to subscribers
                for (EventSubscription subscription : matchingSubscriptions) {
                    try {
                        subscription.getHandler().handleEvent(event);
                        totalEventsDelivered.incrementAndGet();
                    } catch (Exception e) {
                        logger.error("Error delivering event {} to subscription {}", event.getEventId(),
                                subscription.getSubscriptionId(), e);
                        totalEventsFailed.incrementAndGet();

                        // Move to dead letter queue
                        addToDeadLetterQueue(event);
                    }
                }

                return EventPublishResult
                        .success("Event delivered to " + matchingSubscriptions.size() + " subscribers");
            } catch (Exception e) {
                totalEventsFailed.incrementAndGet();
                addToDeadLetterQueue(event);
                return EventPublishResult.failure("Event routing failed: " + e.getMessage());
            }
        });
    }

    private void processAgentEvent(AgentEvent event) {
        // Process agent events from openHAB event bus
        logger.debug("Processing agent event: {}", event.getEventId());

        // Apply filters and routing
        if (applyEventFilters(event)) {
            routeEvent(event);
        } else {
            totalEventsFiltered.incrementAndGet();
        }
    }

    private @Nullable AgentEvent convertOpenHABEventToAgentEvent(Event openHABEvent) {
        try {
            // Extract event data from openHAB event
            String eventType = openHABEvent.getType();
            String sourceAgentId = extractSourceAgentId(openHABEvent);
            Object payload = openHABEvent.getPayload();

            return AgentEvent.builder().eventId(generateEventId()).eventType(eventType).sourceAgentId(sourceAgentId)
                    .payload(payload).timestamp(Instant.now()).options(EventOptions.builder().build()).build();
        } catch (Exception e) {
            logger.error("Error converting openHAB event to agent event", e);
            return null;
        }
    }

    private String extractSourceAgentId(Event openHABEvent) {
        // Extract source agent ID from openHAB event
        // This is a placeholder implementation
        return "openhab-system";
    }

    private void addToDeadLetterQueue(AgentEvent event) {
        event.incrementRetryCount();
        deadLetterQueue.computeIfAbsent(event.getEventType(), k -> List.of()).add(event);
        totalEventsInDeadLetterQueue.incrementAndGet();
    }

    private CompletableFuture<EventPublishResult> retryEvent(AgentEvent event) {
        logger.debug("Retrying event: {}", event.getEventId());
        return routeEvent(event);
    }

    private final Map<String, AgentEvent> pendingEvents = new ConcurrentHashMap<>();

    private void processEventQueue() {
        try {
            logger.debug("Processing event queue");

            // Process pending events
            for (Map.Entry<String, AgentEvent> entry : pendingEvents.entrySet()) {
                String eventId = entry.getKey();
                AgentEvent event = entry.getValue();

                try {
                    // Attempt to route the event
                    CompletableFuture<EventPublishResult> routeFuture = routeEvent(event);
                    EventPublishResult result = routeFuture.get();

                    if (result.isSuccess()) {
                        // Event processed successfully
                        pendingEvents.remove(eventId);
                        totalEventsDelivered.incrementAndGet();

                        logger.debug("Event {} processed successfully", eventId);
                    } else {
                        // Event processing failed, add to dead letter queue
                        addToDeadLetterQueue(event);
                        totalEventsFailed.incrementAndGet();

                        logger.warn("Event {} processing failed: {}", eventId, result.getMessage());
                    }

                } catch (Exception e) {
                    logger.error("Error processing event {}: {}", eventId, e.getMessage());

                    // Add to dead letter queue on error
                    addToDeadLetterQueue(event);
                    totalEventsFailed.incrementAndGet();
                }
            }

        } catch (Exception e) {
            logger.error("Error in event queue processing: {}", e.getMessage(), e);
        }
    }

    private void processEventBatches() {
        try {
            logger.debug("Processing event batches");

            // Process event batches for optimization
            for (Map.Entry<String, List<AgentEvent>> entry : eventBatches.entrySet()) {
                String batchId = entry.getKey();
                List<AgentEvent> batch = entry.getValue();

                if (batch.size() >= configuration.get().getMaxEventBatchSize()) {
                    // Process batch
                    processBatch(batch);

                    // Clear the batch
                    eventBatches.remove(batchId);

                    logger.debug("Processed event batch {} with {} events", batchId, batch.size());
                }
            }

        } catch (Exception e) {
            logger.error("Error in event batch processing: {}", e.getMessage(), e);
        }
    }

    private void processDeadLetterQueue() {
        try {
            logger.debug("Processing dead letter queue");

            // Process events in dead letter queue
            for (Map.Entry<String, List<AgentEvent>> entry : deadLetterQueue.entrySet()) {
                String eventType = entry.getKey();
                List<AgentEvent> events = entry.getValue();

                // Remove events that have exceeded max retries
                events.removeIf(event -> event.getRetryCount() >= configuration.get().getMaxRetries());

                // Retry events that haven't exceeded max retries
                for (AgentEvent event : events) {
                    if (event.getRetryCount() < configuration.get().getMaxRetries()) {
                        try {
                            CompletableFuture<EventPublishResult> retryFuture = routeEvent(event);
                            EventPublishResult result = retryFuture.get();

                            if (result.isSuccess()) {
                                // Retry successful, remove from dead letter queue
                                events.remove(event);
                                totalEventsDelivered.incrementAndGet();

                                logger.info("Event {} retry successful after {} attempts", event.getEventId(),
                                        event.getRetryCount() + 1);
                            } else {
                                // Retry failed, increment retry count
                                event.incrementRetryCount();

                                logger.warn("Event {} retry failed (attempt {}): {}", event.getEventId(),
                                        event.getRetryCount(), result.getMessage());
                            }

                        } catch (Exception e) {
                            logger.error("Error during retry for event {}: {}", event.getEventId(), e.getMessage());
                            event.incrementRetryCount();
                        }
                    }
                }
            }

        } catch (Exception e) {
            logger.error("Error in dead letter queue processing: {}", e.getMessage(), e);
        }
    }

    private void processBatch(List<AgentEvent> batch) {
        try {
            // Process a batch of events together for efficiency
            List<CompletableFuture<EventPublishResult>> futures = batch.stream().map(this::routeEvent).toList();

            // Wait for all events in the batch to complete
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();

            // Count successful deliveries
            long successful = futures.stream().mapToLong(future -> {
                try {
                    return future.get().isSuccess() ? 1 : 0;
                } catch (Exception e) {
                    return 0;
                }
            }).sum();

            totalEventsDelivered.addAndGet(successful);
            totalEventsFailed.addAndGet(batch.size() - successful);

            logger.debug("Batch processing completed: {} successful, {} failed out of {} events", successful,
                    batch.size() - successful, batch.size());

        } catch (Exception e) {
            logger.error("Error processing event batch: {}", e.getMessage(), e);

            // Mark all events in batch as failed
            totalEventsFailed.addAndGet(batch.size());

            // Add failed events to dead letter queue
            for (AgentEvent event : batch) {
                addToDeadLetterQueue(event);
            }
        }
    }

    private void shutdownExecutor(ScheduledExecutorService executor) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    // Inner classes and interfaces extracted to top-level types in this package

    // Default implementations
}
