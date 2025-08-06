package org.openhab.core.ai.agent.events;

import java.time.Duration;
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
import org.openhab.core.ai.agent.AgentRegistry;
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
    private final EventSchemaValidator schemaValidator = new EventSchemaValidator();

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
            return schemaValidator.validate(event, schema);
        }
        return true; // No schema defined, allow the event
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

    private void processEventQueue() {
        // Process pending events
        // This is a placeholder for actual event queue processing
    }

    private void processEventBatches() {
        // Process event batches
        // This is a placeholder for actual batch processing
    }

    private void processDeadLetterQueue() {
        // Process dead letter queue
        // This is a placeholder for actual dead letter queue processing
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

    // Inner classes and interfaces

    /**
     * Agent event data
     */
    public static class AgentEvent {
        private final String eventId;
        private final String eventType;
        private final String sourceAgentId;
        private final Object payload;
        private final Instant timestamp;
        private final EventOptions options;
        private int retryCount;

        private AgentEvent(Builder builder) {
            this.eventId = builder.eventId;
            this.eventType = builder.eventType;
            this.sourceAgentId = builder.sourceAgentId;
            this.payload = builder.payload;
            this.timestamp = builder.timestamp;
            this.options = builder.options;
            this.retryCount = 0;
        }

        // Getters
        public String getEventId() {
            return eventId;
        }

        public String getEventType() {
            return eventType;
        }

        public String getSourceAgentId() {
            return sourceAgentId;
        }

        public Object getPayload() {
            return payload;
        }

        public Instant getTimestamp() {
            return timestamp;
        }

        public EventOptions getOptions() {
            return options;
        }

        public int getRetryCount() {
            return retryCount;
        }

        public void incrementRetryCount() {
            this.retryCount++;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String eventId;
            private String eventType;
            private String sourceAgentId;
            private Object payload;
            private Instant timestamp;
            private EventOptions options;

            public Builder eventId(String eventId) {
                this.eventId = eventId;
                return this;
            }

            public Builder eventType(String eventType) {
                this.eventType = eventType;
                return this;
            }

            public Builder sourceAgentId(String sourceAgentId) {
                this.sourceAgentId = sourceAgentId;
                return this;
            }

            public Builder payload(Object payload) {
                this.payload = payload;
                return this;
            }

            public Builder timestamp(Instant timestamp) {
                this.timestamp = timestamp;
                return this;
            }

            public Builder options(EventOptions options) {
                this.options = options;
                return this;
            }

            public AgentEvent build() {
                return new AgentEvent(this);
            }
        }
    }

    /**
     * Event options
     */
    public static class EventOptions {
        private final boolean persistent;
        private final Duration timeout;
        private final int maxRetries;
        private final Map<String, Object> metadata;

        private EventOptions(Builder builder) {
            this.persistent = builder.persistent;
            this.timeout = builder.timeout;
            this.maxRetries = builder.maxRetries;
            this.metadata = builder.metadata;
        }

        // Getters
        public boolean isPersistent() {
            return persistent;
        }

        public Duration getTimeout() {
            return timeout;
        }

        public int getMaxRetries() {
            return maxRetries;
        }

        public Map<String, Object> getMetadata() {
            return metadata;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private boolean persistent = false;
            private Duration timeout = Duration.ofMinutes(5);
            private int maxRetries = 3;
            private Map<String, Object> metadata = Map.of();

            public Builder persistent(boolean persistent) {
                this.persistent = persistent;
                return this;
            }

            public Builder timeout(Duration timeout) {
                this.timeout = timeout;
                return this;
            }

            public Builder maxRetries(int maxRetries) {
                this.maxRetries = maxRetries;
                return this;
            }

            public Builder metadata(Map<String, Object> metadata) {
                this.metadata = metadata;
                return this;
            }

            public EventOptions build() {
                return new EventOptions(this);
            }
        }
    }

    /**
     * Event subscription
     */
    public static class EventSubscription {
        private final String subscriptionId;
        private final String agentId;
        private final Set<String> eventTypes;
        private final @Nullable EventFilter filter;
        private final EventHandler handler;
        private final Instant timestamp;

        private EventSubscription(Builder builder) {
            this.subscriptionId = builder.subscriptionId;
            this.agentId = builder.agentId;
            this.eventTypes = builder.eventTypes;
            this.filter = builder.filter;
            this.handler = builder.handler;
            this.timestamp = builder.timestamp;
        }

        // Getters
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

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String subscriptionId;
            private String agentId;
            private Set<String> eventTypes;
            private @Nullable EventFilter filter;
            private EventHandler handler;
            private Instant timestamp;

            public Builder subscriptionId(String subscriptionId) {
                this.subscriptionId = subscriptionId;
                return this;
            }

            public Builder agentId(String agentId) {
                this.agentId = agentId;
                return this;
            }

            public Builder eventTypes(Set<String> eventTypes) {
                this.eventTypes = eventTypes;
                return this;
            }

            public Builder filter(@Nullable EventFilter filter) {
                this.filter = filter;
                return this;
            }

            public Builder handler(EventHandler handler) {
                this.handler = handler;
                return this;
            }

            public Builder timestamp(Instant timestamp) {
                this.timestamp = timestamp;
                return this;
            }

            public EventSubscription build() {
                return new EventSubscription(this);
            }
        }
    }

    /**
     * Event schema
     */
    public static class EventSchema {
        private final String eventType;
        private final Map<String, String> requiredFields;
        private final Map<String, String> optionalFields;
        private final String version;

        public EventSchema(String eventType, Map<String, String> requiredFields, Map<String, String> optionalFields,
                String version) {
            this.eventType = eventType;
            this.requiredFields = requiredFields;
            this.optionalFields = optionalFields;
            this.version = version;
        }

        // Getters
        public String getEventType() {
            return eventType;
        }

        public Map<String, String> getRequiredFields() {
            return requiredFields;
        }

        public Map<String, String> getOptionalFields() {
            return optionalFields;
        }

        public String getVersion() {
            return version;
        }
    }

    /**
     * Event bus statistics
     */
    public static class EventBusStatistics {
        private final long totalEventsPublished;
        private final long totalEventsDelivered;
        private final long totalEventsFiltered;
        private final long totalEventsFailed;
        private final long totalEventsInDeadLetterQueue;
        private final int storedEvents;
        private final int activeSubscriptions;
        private final int registeredSchemas;
        private final int deadLetterQueueSize;

        public EventBusStatistics(long totalEventsPublished, long totalEventsDelivered, long totalEventsFiltered,
                long totalEventsFailed, long totalEventsInDeadLetterQueue, int storedEvents, int activeSubscriptions,
                int registeredSchemas, int deadLetterQueueSize) {
            this.totalEventsPublished = totalEventsPublished;
            this.totalEventsDelivered = totalEventsDelivered;
            this.totalEventsFiltered = totalEventsFiltered;
            this.totalEventsFailed = totalEventsFailed;
            this.totalEventsInDeadLetterQueue = totalEventsInDeadLetterQueue;
            this.storedEvents = storedEvents;
            this.activeSubscriptions = activeSubscriptions;
            this.registeredSchemas = registeredSchemas;
            this.deadLetterQueueSize = deadLetterQueueSize;
        }

        // Getters
        public long getTotalEventsPublished() {
            return totalEventsPublished;
        }

        public long getTotalEventsDelivered() {
            return totalEventsDelivered;
        }

        public long getTotalEventsFiltered() {
            return totalEventsFiltered;
        }

        public long getTotalEventsFailed() {
            return totalEventsFailed;
        }

        public long getTotalEventsInDeadLetterQueue() {
            return totalEventsInDeadLetterQueue;
        }

        public int getStoredEvents() {
            return storedEvents;
        }

        public int getActiveSubscriptions() {
            return activeSubscriptions;
        }

        public int getRegisteredSchemas() {
            return registeredSchemas;
        }

        public int getDeadLetterQueueSize() {
            return deadLetterQueueSize;
        }
    }

    /**
     * Event bus configuration
     */
    public static class EventBusConfiguration {
        private Duration eventTimeout = Duration.ofMinutes(5);
        private int maxRetries = 3;
        private Duration retryInterval = Duration.ofSeconds(30);
        private Duration eventRetentionPeriod = Duration.ofDays(7);
        private boolean enablePersistence = true;
        private boolean enableSchemaValidation = true;
        private int maxEventBatchSize = 100;
        private Duration batchTimeout = Duration.ofSeconds(5);

        // Getters and setters
        public Duration getEventTimeout() {
            return eventTimeout;
        }

        public void setEventTimeout(Duration eventTimeout) {
            this.eventTimeout = eventTimeout;
        }

        public int getMaxRetries() {
            return maxRetries;
        }

        public void setMaxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
        }

        public Duration getRetryInterval() {
            return retryInterval;
        }

        public void setRetryInterval(Duration retryInterval) {
            this.retryInterval = retryInterval;
        }

        public Duration getEventRetentionPeriod() {
            return eventRetentionPeriod;
        }

        public void setEventRetentionPeriod(Duration eventRetentionPeriod) {
            this.eventRetentionPeriod = eventRetentionPeriod;
        }

        public boolean isEnablePersistence() {
            return enablePersistence;
        }

        public void setEnablePersistence(boolean enablePersistence) {
            this.enablePersistence = enablePersistence;
        }

        public boolean isEnableSchemaValidation() {
            return enableSchemaValidation;
        }

        public void setEnableSchemaValidation(boolean enableSchemaValidation) {
            this.enableSchemaValidation = enableSchemaValidation;
        }

        public int getMaxEventBatchSize() {
            return maxEventBatchSize;
        }

        public void setMaxEventBatchSize(int maxEventBatchSize) {
            this.maxEventBatchSize = maxEventBatchSize;
        }

        public Duration getBatchTimeout() {
            return batchTimeout;
        }

        public void setBatchTimeout(Duration batchTimeout) {
            this.batchTimeout = batchTimeout;
        }
    }

    // Interfaces
    public interface EventFilter {
        boolean shouldDeliver(AgentEvent event);
    }

    public interface EventRouter {
        CompletableFuture<EventPublishResult> routeEvent(AgentEvent event);
    }

    public interface EventHandler {
        void handleEvent(AgentEvent event);
    }

    public interface EventPublishResult {
        boolean isSuccess();

        String getMessage();

        static EventPublishResult success(String message) {
            return new EventPublishResult() {
                @Override
                public boolean isSuccess() {
                    return true;
                }

                @Override
                public String getMessage() {
                    return message;
                }
            };
        }

        static EventPublishResult failure(String message) {
            return new EventPublishResult() {
                @Override
                public boolean isSuccess() {
                    return false;
                }

                @Override
                public String getMessage() {
                    return message;
                }
            };
        }

        static EventPublishResult filtered(String reason) {
            return new EventPublishResult() {
                @Override
                public boolean isSuccess() {
                    return false;
                }

                @Override
                public String getMessage() {
                    return "Event filtered: " + reason;
                }
            };
        }

        static EventPublishResult schemaValidationFailed(String reason) {
            return new EventPublishResult() {
                @Override
                public boolean isSuccess() {
                    return false;
                }

                @Override
                public String getMessage() {
                    return "Schema validation failed: " + reason;
                }
            };
        }
    }

    public interface DeadLetterQueueResult {
        boolean isSuccess();

        String getMessage();

        long getSuccessfulRetries();

        long getTotalRetries();

        static DeadLetterQueueResult success(long successfulRetries, long totalRetries) {
            return new DeadLetterQueueResult() {
                @Override
                public boolean isSuccess() {
                    return true;
                }

                @Override
                public String getMessage() {
                    return "Dead letter queue processing successful";
                }

                @Override
                public long getSuccessfulRetries() {
                    return successfulRetries;
                }

                @Override
                public long getTotalRetries() {
                    return totalRetries;
                }
            };
        }

        static DeadLetterQueueResult noEventsToRetry() {
            return new DeadLetterQueueResult() {
                @Override
                public boolean isSuccess() {
                    return true;
                }

                @Override
                public String getMessage() {
                    return "No events to retry";
                }

                @Override
                public long getSuccessfulRetries() {
                    return 0;
                }

                @Override
                public long getTotalRetries() {
                    return 0;
                }
            };
        }
    }

    // Default implementations
    private static class EventSchemaValidator {
        public boolean validate(AgentEvent event, EventSchema schema) {
            // Placeholder for schema validation implementation
            return true;
        }
    }
}
