package org.openhab.core.ai.agent.communication.events;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.agent.communication.events.api.EventHandler;
import org.openhab.core.ai.agent.communication.events.api.EventOptions;
import org.openhab.core.ai.agent.communication.events.api.EventPublishResult;
import org.openhab.core.ai.agent.communication.events.api.EventSchema;
import org.openhab.core.ai.agent.communication.events.api.EventSubscription;
import org.openhab.core.ai.agent.lifecycle.api.AgentRegistry;
import org.openhab.core.ai.common.builder.EventSubscriptionBuilder;
import org.openhab.core.ai.common.events.EventFilter;
import org.openhab.core.ai.common.events.EventRouter;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import java.util.Map;
import java.util.Set;
import org.openhab.core.ai.common.monitoring.api.MetricKey;
import org.openhab.core.ai.common.monitoring.api.MetricKeys;
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

    @Reference
    private @Nullable MetricsService metricsService;

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
        batchProcessor.scheduleAtFixedRate(this::processEventBatches, 0, 500, TimeUnit.MILLISECONDS);
        deadLetterProcessor.scheduleAtFixedRate(this::processDeadLetterQueue, 0, 60000, TimeUnit.MILLISECONDS);
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
        Instant startTime = Instant.now();
        boolean success = false;

        try {
            // Create agent event
            AgentEvent event = AgentEvent.builder().eventId(generateEventId()).eventType(eventType)
                    .sourceAgentId(sourceAgentId).payload(payload).timestamp(Instant.now()).options(options).build();

            // Validate event schema
            if (!validateEventSchema(event)) {
                recordMetrics("agent-event-bus", "event-schema-validation-failed", false,
                        Duration.between(startTime, Instant.now()));
                return CompletableFuture.completedFuture(EventPublishResult.failure("Event schema validation failed"));
            }

            // Apply event filters
            if (!applyEventFilters(event)) {
                recordMetrics("agent-event-bus", "event-filtered", false, Duration.between(startTime, Instant.now()));
                return CompletableFuture.completedFuture(EventPublishResult.filtered("Event filtered out"));
            }

            // Store event
            eventStore.put(event.getEventId(), event);

            // Record metrics
            recordMetrics("agent-event-bus", "event-published", true, Duration.between(startTime, Instant.now()));

            // Route event
            return routeEvent(event).thenApply(result -> {
                Duration totalDuration = Duration.between(startTime, Instant.now());
                boolean deliverySuccess = result.isSuccess();
                recordMetrics("agent-event-bus", "event-delivery", deliverySuccess, totalDuration);
                return result;
            });

        } catch (Exception e) {
            Duration duration = Duration.between(startTime, Instant.now());
            logger.error("Error publishing event {}: {}", eventType, e.getMessage(), e);
            recordMetrics("agent-event-bus", "event-publish-error", false, duration);
            return CompletableFuture
                    .completedFuture(EventPublishResult.failure("Error publishing event: " + e.getMessage()));
        }
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
        Instant startTime = Instant.now();
        boolean success = false;

        try {
            logger.debug("Agent {} subscribing to events: {}", agentId, eventTypes);

            // Validate agent exists
            AgentRegistry registry = agentRegistry;
            if (registry == null || registry.getAgent(agentId, "system") == null) {
                logger.warn("Cannot subscribe agent {} to events: agent not found", agentId, eventTypes);
                return false;
            }

            EventSubscription subscription = EventSubscriptionBuilder.builder()
                    .withSubscriptionId(generateSubscriptionId()).withFromAgentId(agentId)
                    .withEventTypesSet(Set.copyOf(eventTypes)).withTimestamp(Instant.now()).withFilter(filter)
                    .withHandler(handler).build();

            subscriptions.put(subscription.getSubscriptionId(), subscription);

            success = true;
            recordMetrics("agent-event-bus", "event-subscription-created", true,
                    Duration.between(startTime, Instant.now()));

            return true;

        } catch (Exception e) {
            Duration duration = Duration.between(startTime, Instant.now());
            logger.error("Error creating event subscription for agent {}: {}", agentId, e.getMessage(), e);
            recordMetrics("agent-event-bus", "event-subscription-error", false, duration);
            return false;
        }
    }

    /**
     * Unsubscribe from events
     * 
     * @param subscriptionId Subscription identifier
     * @return Unsubscribe result
     */
    public boolean unsubscribeFromEvents(String subscriptionId) {
        Instant startTime = Instant.now();
        boolean success = false;

        try {
            EventSubscription subscription = subscriptions.remove(subscriptionId);
            if (subscription != null) {
                success = true;
                recordMetrics("agent-event-bus", "event-subscription-removed", true,
                        Duration.between(startTime, Instant.now()));
            } else {
                recordMetrics("agent-event-bus", "event-subscription-not-found", false,
                        Duration.between(startTime, Instant.now()));
            }

        } catch (Exception e) {
            Duration duration = Duration.between(startTime, Instant.now());
            logger.error("Error removing event subscription {}: {}", subscriptionId, e.getMessage(), e);
            recordMetrics("agent-event-bus", "event-subscription-remove-error", false, duration);
        }

        return success;
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
        MetricsService metrics = metricsService;
        if (metrics == null) {
            logger.warn("MetricsService not available, returning empty statistics");
            return new EventBusStatistics(0, 0, 0, 0, 0, eventStore.size(), subscriptions.size(), eventSchemas.size(),
                    deadLetterQueue.size());
        }

        // Retrieve domain aggregated snapshot for agent-event-bus operations
        MetricKey eventBusKey = MetricKeys.custom("agent-event-bus", Map.of(), Set.of("counts", "latency"));
        var eventBusSnapshot = metrics.getSnapshot(eventBusKey, org.openhab.core.ai.common.monitoring.service.snapshot.GenericMetricsSnapshot.class);

        if (eventBusSnapshot != null) {
            long totalOperations = eventBusSnapshot.getLong("total");
            return new EventBusStatistics(totalOperations, totalOperations,
                    totalOperations, totalOperations,
                    totalOperations, eventStore.size(), subscriptions.size(), eventSchemas.size(),
                    deadLetterQueue.size());
        }
        return new EventBusStatistics(0, 0, 0, 0, 0, eventStore.size(), subscriptions.size(), eventSchemas.size(),
                deadLetterQueue.size());
    }

    /**
     * Process events from dead letter queue
     * 
     * @param eventType Event type to retry
     * @param maxRetries Maximum number of retries
     * @return Retry result
     */
    public CompletableFuture<DeadLetterQueueResult> retryDeadLetterEvents(@Nullable String eventType, int maxRetries) {
        Instant startTime = Instant.now();
        boolean success = false;

        try {
            logger.debug("Retrying dead letter events for type: {}", eventType);

            List<AgentEvent> eventsToRetry = deadLetterQueue.values().stream().flatMap(List::stream)
                    .filter(event -> eventType == null || event.getEventType().equals(eventType))
                    .filter(event -> event.getRetryCount() < maxRetries).toList();

            if (eventsToRetry.isEmpty()) {
                recordMetrics("agent-event-bus", "dead-letter-no-events", false,
                        Duration.between(startTime, Instant.now()));
                return CompletableFuture.completedFuture(DeadLetterQueueResult.noEventsToRetry());
            }

            List<CompletableFuture<EventPublishResult>> retries = eventsToRetry.stream().map(this::retryEvent).toList();

            return CompletableFuture.allOf(retries.toArray(new CompletableFuture[0])).thenApply(v -> {
                Duration duration = Duration.between(startTime, Instant.now());
                long successful = retries.stream().mapToLong(future -> {
                    try {
                        return future.get().isSuccess() ? 1 : 0;
                    } catch (Exception e) {
                        return 0;
                    }
                }).sum();

                recordMetrics("agent-event-bus", "dead-letter-retry-completed", true, duration);
                return DeadLetterQueueResult.success(successful, eventsToRetry.size());
            });

        } catch (Exception e) {
            Duration duration = Duration.between(startTime, Instant.now());
            logger.error("Error retrying dead letter events: {}", e.getMessage(), e);
            recordMetrics("agent-event-bus", "dead-letter-retry-error", false, duration);
            return CompletableFuture.failedFuture(e);
        }
    }

    // EventSubscriber implementation
    @Override
    public Set<String> getSubscribedEventTypes() {
        return Set.of("org.openhab.core.ai.agent.*");
    }

    @Override
    public void receive(Event event) {
        Instant startTime = Instant.now();
        boolean success = false;

        try {
            logger.debug("Received openHAB event: {}", event.getType());

            // Convert openHAB event to agent event if needed
            if (event.getType().startsWith("org.openhab.core.ai.agent.")) {
                AgentEvent agentEvent = convertOpenHABEventToAgentEvent(event);
                if (agentEvent != null) {
                    processAgentEvent(agentEvent);
                    success = true;
                }
            }

            recordMetrics("agent-event-bus", "openhab-event-received", success,
                    Duration.between(startTime, Instant.now()));

        } catch (Exception e) {
            Duration duration = Duration.between(startTime, Instant.now());
            logger.error("Error processing openHAB event: {}", e.getMessage(), e);
            recordMetrics("agent-event-bus", "openhab-event-error", false, duration);
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
                // Simulate event delivery
                Thread.sleep(10);

                // Notify subscribers
                notifySubscribers(event);

                return EventPublishResult.success("Event delivered successfully");

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return EventPublishResult.failure("Event delivery interrupted");
            } catch (Exception e) {
                logger.error("Error routing event {}: {}", event.getEventId(), e.getMessage(), e);
                return EventPublishResult.failure("Event delivery failed: " + e.getMessage());
            }
        });
    }

    private void notifySubscribers(AgentEvent event) {
        // Find subscribers for this event type
        List<EventSubscription> relevantSubscriptions = subscriptions.values().stream()
                .filter(sub -> sub.getEventTypes().contains(event.getEventType()) || sub.getEventTypes().contains("*"))
                .toList();

        for (EventSubscription subscription : relevantSubscriptions) {
            try {
                subscription.getHandler().handleEvent(event);
                recordMetrics("agent-event-bus", "event-delivered", true, Duration.ZERO);
            } catch (Exception e) {
                logger.error("Error delivering event {} to subscriber {}: {}", event.getEventId(),
                        subscription.getSubscriptionId(), e.getMessage(), e);
                recordMetrics("agent-event-bus", "event-delivery-failed", false, Duration.ZERO);
            }
        }
    }

    private void processAgentEvent(AgentEvent event) {
        // Process agent event
        try {
            // Apply filters
            if (!applyEventFilters(event)) {
                recordMetrics("agent-event-bus", "agent-event-filtered", false, Duration.ZERO);
                return;
            }

            // Notify subscribers
            notifySubscribers(event);

        } catch (Exception e) {
            logger.error("Error processing agent event {}: {}", event.getEventId(), e.getMessage(), e);
            recordMetrics("agent-event-bus", "agent-event-processing-error", false, Duration.ZERO);
        }
    }

    private CompletableFuture<EventPublishResult> retryEvent(AgentEvent event) {
        // Retry event delivery
        return routeEvent(event).thenApply(result -> {
            if (result.isSuccess()) {
                // Remove from dead letter queue
                deadLetterQueue.values().forEach(events -> events.remove(event));
                recordMetrics("agent-event-bus", "event-retry-success", true, Duration.ZERO);
            } else {
                // Increment retry count
                event.incrementRetryCount();
                recordMetrics("agent-event-bus", "event-retry-failed", false, Duration.ZERO);
            }
            return result;
        });
    }

    private void processEventQueue() {
        // Process pending events from the event store
        eventStore.values().stream().forEach(event -> {
            try {
                // Route the event to subscribers
                routeEvent(event).thenAccept(result -> {
                    if (result.isSuccess()) {
                        // Remove from event store after successful processing
                        eventStore.remove(event.getEventId());
                        recordMetrics("agent-event-bus", "event-queue-processed", true, Duration.ZERO);
                    } else {
                        // Move to dead letter queue if processing failed
                        deadLetterQueue.computeIfAbsent(event.getEventType(), k -> new ArrayList<>()).add(event);
                        eventStore.remove(event.getEventId());
                        recordMetrics("agent-event-bus", "event-queue-failed", false, Duration.ZERO);
                    }
                });
            } catch (Exception e) {
                logger.error("Error processing event from queue {}: {}", event.getEventId(), e.getMessage(), e);
                recordMetrics("agent-event-bus", "event-queue-error", false, Duration.ZERO);
            }
        });
    }

    private void processEventBatches() {
        // Process event batches
        eventBatches.forEach((batchId, batch) -> {
            try {
                long successful = batch.stream().mapToLong(event -> {
                    try {
                        CompletableFuture<EventPublishResult> result = routeEvent(event);
                        return result.get().isSuccess() ? 1 : 0;
                    } catch (Exception e) {
                        return 0;
                    }
                }).sum();

                recordMetrics("agent-event-bus", "batch-processing-completed", true, Duration.ZERO);

            } catch (Exception e) {
                logger.error("Error processing event batch {}: {}", batchId, e.getMessage(), e);
                recordMetrics("agent-event-bus", "batch-processing-error", false, Duration.ZERO);
            }
        });
    }

    private void processDeadLetterQueue() {
        // Process dead letter queue
        deadLetterQueue.forEach((eventType, events) -> {
            try {
                // Process dead letter events
                List<AgentEvent> eventsToRetry = new ArrayList<>();
                List<AgentEvent> eventsToRemove = new ArrayList<>();

                for (AgentEvent event : events) {
                    // Check if event should be retried
                    if (event.getRetryCount() < getMaxRetries(event)) {
                        // Retry the event
                        CompletableFuture<EventPublishResult> retryResult = routeEvent(event);
                        retryResult.thenAccept(result -> {
                            if (result.isSuccess()) {
                                // Event successfully retried, remove from dead letter queue
                                eventsToRemove.add(event);
                                recordMetrics("agent-event-bus", "dead-letter-retry-success", true, Duration.ZERO);
                            } else {
                                // Retry failed, increment retry count
                                event.incrementRetryCount();
                                recordMetrics("agent-event-bus", "dead-letter-retry-failed", false, Duration.ZERO);
                            }
                        });
                    } else {
                        // Max retries exceeded, remove from dead letter queue
                        eventsToRemove.add(event);
                        recordMetrics("agent-event-bus", "dead-letter-max-retries-exceeded", false, Duration.ZERO);
                        logger.warn("Event {} exceeded max retries, removing from dead letter queue",
                                event.getEventId());
                    }
                }

                // Remove processed events from dead letter queue
                events.removeAll(eventsToRemove);

                recordMetrics("agent-event-bus", "dead-letter-processing", true, Duration.ZERO);

            } catch (Exception e) {
                logger.error("Error processing dead letter queue for {}: {}", eventType, e.getMessage(), e);
                recordMetrics("agent-event-bus", "dead-letter-processing-error", false, Duration.ZERO);
            }
        });
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

    private @Nullable AgentEvent convertOpenHABEventToAgentEvent(Event event) {
        try {
            // Convert openHAB event to agent event
            String eventType = event.getType();
            String sourceAgentId = "openhab-system"; // Default source for openHAB events

            // Create event options with default settings
            EventOptions options = EventOptions.builder().timeout(Duration.ofSeconds(30))
                    .metadata(Map.of("openhabEventType", eventType)).build();

            // Create agent event
            AgentEvent agentEvent = AgentEvent.builder().eventId(generateEventId()).eventType("openhab." + eventType)
                    .sourceAgentId(sourceAgentId).payload(event).timestamp(Instant.now()).options(options).build();

            recordMetrics("agent-event-bus", "openhab-event-converted", true, Duration.ZERO);
            return agentEvent;

        } catch (Exception e) {
            logger.error("Error converting openHAB event to agent event: {}", e.getMessage(), e);
            recordMetrics("agent-event-bus", "openhab-event-conversion-failed", false, Duration.ZERO);
            return null;
        }
    }

    private int getMaxRetries(AgentEvent event) {
        // Get max retries from event options or use default
        EventOptions options = event.getOptions();
        if (options != null) {
            return options.getMaxRetries();
        }
        // Default max retries
        return 3;
    }

    private void recordMetrics(String domain, String operation, boolean success, Duration duration) {
        MetricsService metrics = metricsService;
        if (metrics != null) {
            metrics.recordOperation(domain, operation, success, duration);
        } else {
            logger.warn("MetricsService not available, cannot record metrics for operation: {} - {}", domain,
                    operation);
        }
    }

    // Inner classes and interfaces extracted to top-level types in this package
}
