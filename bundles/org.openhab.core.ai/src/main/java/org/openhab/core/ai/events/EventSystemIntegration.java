package org.openhab.core.ai.events;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.action.library.events.EventSubscriptionRegistry;
import org.openhab.core.ai.api.reasoning.MultiStepReasoningResult;
import org.openhab.core.ai.api.reasoning.ReasoningContext;
import org.openhab.core.ai.reasoning.MultiStepReasoningEngine;
import org.openhab.core.events.Event;
import org.openhab.core.events.EventSubscriber;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Event System Integration - Comprehensive event processing pipeline
 * 
 * This component provides a complete event processing pipeline including:
 * - Event bus integration with openHAB EventBus
 * - Event filtering and enrichment
 * - Event routing and persistence
 * - Event replay capabilities
 * - Integration with reasoning engine for autonomous behavior
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@Component(service = EventSystemIntegration.class)
@NonNullByDefault
public class EventSystemIntegration implements EventSubscriber {

    private static final Logger logger = LoggerFactory.getLogger(EventSystemIntegration.class);

    // Core components
    @Reference
    private @Nullable EventFilter eventFilter;

    @Reference
    private @Nullable EventEnricher eventEnricher;

    @Reference
    private @Nullable EventRouter eventRouter;

    @Reference
    private @Nullable EventPersistenceManager eventPersistenceManager;

    @Reference
    private @Nullable MultiStepReasoningEngine reasoningEngine;

    @Reference
    private @Nullable EventSubscriptionRegistry subscriptionRegistry;

    // Event processing statistics
    private final AtomicLong totalEventsProcessed = new AtomicLong(0);
    private final AtomicLong filteredEvents = new AtomicLong(0);
    private final AtomicLong enrichedEvents = new AtomicLong(0);
    private final AtomicLong routedEvents = new AtomicLong(0);
    private final AtomicLong persistedEvents = new AtomicLong(0);
    private final AtomicLong reasoningTriggers = new AtomicLong(0);

    // Configuration
    private final AtomicReference<Boolean> enableEventProcessing = new AtomicReference<>(true);
    private final AtomicReference<Boolean> enableEventPersistence = new AtomicReference<>(true);
    private final AtomicReference<Boolean> enableReasoningIntegration = new AtomicReference<>(true);
    private final AtomicReference<Duration> eventRetentionPeriod = new AtomicReference<>(Duration.ofDays(30));

    // Event replay state
    private final AtomicReference<Boolean> replayMode = new AtomicReference<>(false);
    private final AtomicReference<Instant> replayStartTime = new AtomicReference<>();
    private final AtomicReference<Instant> replayEndTime = new AtomicReference<>();

    @Activate
    protected void activate() {
        logger.debug("EventSystemIntegration activated");
    }

    @Deactivate
    protected void deactivate() {
        logger.debug("EventSystemIntegration deactivated");
        // Clean up any ongoing operations
        stopReplay();
    }

    @Override
    public void receive(Event event) {
        if (!enableEventProcessing.get()) {
            logger.debug("Event processing disabled, skipping event: {}", event.getType());
            return;
        }

        totalEventsProcessed.incrementAndGet();
        logger.debug("Processing event: type={}, source={}", event.getType(), event.getSource());

        try {
            // Step 1: Event Filtering
            if (!shouldProcessEvent(event)) {
                filteredEvents.incrementAndGet();
                logger.debug("Event filtered out: {}", event.getType());
                return;
            }

            // Step 2: Event Enrichment
            Event enrichedEvent = enrichEvent(event);
            if (enrichedEvent != null) {
                enrichedEvents.incrementAndGet();
                event = enrichedEvent;
            }

            // Step 3: Event Routing
            routeEvent(event);
            routedEvents.incrementAndGet();

            // Step 4: Event Persistence
            if (enableEventPersistence.get()) {
                persistEvent(event);
                persistedEvents.incrementAndGet();
            }

            // Step 5: Reasoning Integration
            if (enableReasoningIntegration.get()) {
                triggerReasoning(event);
                reasoningTriggers.incrementAndGet();
            }

        } catch (Exception e) {
            logger.error("Error processing event: {}", event.getType(), e);
            handleEventProcessingError(event, e);
        }
    }

    @Override
    public Set<String> getSubscribedEventTypes() {
        // Subscribe to all event types for comprehensive processing
        return Set.of("*");
    }

    /**
     * Check if event should be processed based on filtering rules
     */
    private boolean shouldProcessEvent(Event event) {
        EventFilter filter = eventFilter;
        if (filter == null) {
            return true; // Default to processing if no filter available
        }
        return filter.shouldProcess(event);
    }

    /**
     * Enrich event with additional context and metadata
     */
    private @Nullable Event enrichEvent(Event event) {
        EventEnricher enricher = eventEnricher;
        if (enricher == null) {
            return null; // No enrichment if enricher not available
        }
        return enricher.enrich(event);
    }

    /**
     * Route event to appropriate destinations
     */
    private void routeEvent(Event event) {
        EventRouter router = eventRouter;
        if (router == null) {
            logger.debug("No event router available, skipping routing for event: {}", event.getType());
            return;
        }
        router.routeEvent(event);
    }

    /**
     * Persist event for later retrieval and replay
     */
    private void persistEvent(Event event) {
        EventPersistenceManager persistenceManager = eventPersistenceManager;
        if (persistenceManager == null) {
            logger.debug("No persistence manager available, skipping persistence for event: {}", event.getType());
            return;
        }
        persistenceManager.persistEvent(event);
    }

    /**
     * Trigger reasoning engine for autonomous behavior
     */
    private void triggerReasoning(Event event) {
        MultiStepReasoningEngine engine = reasoningEngine;
        if (engine == null) {
            logger.debug("No reasoning engine available, skipping reasoning for event: {}", event.getType());
            return;
        }

        // Convert event to reasoning context
        ReasoningContext context = convertEventToReasoningContext(event);

        // Trigger asynchronous reasoning
        CompletableFuture<MultiStepReasoningResult> reasoning = engine.reasonAsync(context);
        reasoning.thenAccept(this::handleReasoningResult).exceptionally(throwable -> {
            logger.error("Reasoning failed for event: {}", event.getType(), throwable);
            return null;
        });
    }

    /**
     * Convert event to reasoning context
     */
    private ReasoningContext convertEventToReasoningContext(Event event) {
        return ReasoningContext.builder().initialContext("Event: " + event.getType() + " from " + event.getSource())
                .currentContext("Processing event: " + event.getType()).timestamp(Instant.now()).build();
    }

    /**
     * Determine event priority for reasoning
     */
    private int determineEventPriority(Event event) {
        // High priority events
        if (event.getType().contains("ALARM") || event.getType().contains("SECURITY")) {
            return 1;
        }
        // Medium priority events
        if (event.getType().contains("STATE_CHANGED") || event.getType().contains("COMMAND")) {
            return 2;
        }
        // Low priority events
        return 3;
    }

    /**
     * Handle reasoning result
     */
    private void handleReasoningResult(MultiStepReasoningResult result) {
        if (result.isCompleted()) {
            logger.debug("Reasoning completed with confidence: {}", result.getConfidence());
            // Execute planned actions
            executeReasoningActions(result);
        }
    }

    /**
     * Execute actions from reasoning result
     */
    private void executeReasoningActions(MultiStepReasoningResult result) {
        // TODO: Implement action execution based on reasoning result
        logger.debug("Executing reasoning actions with {} tool calls", result.getToolCalls().size());
    }

    /**
     * Handle event processing errors
     */
    private void handleEventProcessingError(Event event, Exception error) {
        logger.error("Event processing error for event type: {}", event.getType(), error);
        // TODO: Implement error recovery and notification
    }

    // Event Replay Capabilities

    /**
     * Start event replay from a specific time range
     */
    public CompletableFuture<Void> startReplay(Instant startTime, Instant endTime) {
        if (replayMode.get()) {
            return CompletableFuture.failedFuture(new IllegalStateException("Replay already in progress"));
        }

        EventPersistenceManager persistenceManager = eventPersistenceManager;
        if (persistenceManager == null) {
            return CompletableFuture.failedFuture(new IllegalStateException("No persistence manager available"));
        }

        replayMode.set(true);
        replayStartTime.set(startTime);
        replayEndTime.set(endTime);

        logger.info("Starting event replay from {} to {}", startTime, endTime);

        return CompletableFuture.runAsync(() -> {
            try {
                List<Event> events = persistenceManager.getEventsInTimeRange(startTime, endTime);
                for (Event event : events) {
                    if (!replayMode.get()) {
                        break; // Stop if replay was cancelled
                    }
                    receive(event);
                    Thread.sleep(100); // Small delay to prevent overwhelming the system
                }
            } catch (Exception e) {
                logger.error("Error during event replay", e);
            } finally {
                replayMode.set(false);
                logger.info("Event replay completed");
            }
        });
    }

    /**
     * Stop event replay
     */
    public void stopReplay() {
        replayMode.set(false);
        logger.info("Event replay stopped");
    }

    /**
     * Check if replay is in progress
     */
    public boolean isReplayInProgress() {
        return replayMode.get();
    }

    // Configuration Methods

    /**
     * Enable or disable event processing
     */
    public void setEnableEventProcessing(boolean enable) {
        enableEventProcessing.set(enable);
        logger.info("Event processing {}", enable ? "enabled" : "disabled");
    }

    /**
     * Enable or disable event persistence
     */
    public void setEnableEventPersistence(boolean enable) {
        enableEventPersistence.set(enable);
        logger.info("Event persistence {}", enable ? "enabled" : "disabled");
    }

    /**
     * Enable or disable reasoning integration
     */
    public void setEnableReasoningIntegration(boolean enable) {
        enableReasoningIntegration.set(enable);
        logger.info("Reasoning integration {}", enable ? "enabled" : "disabled");
    }

    /**
     * Set event retention period
     */
    public void setEventRetentionPeriod(Duration retentionPeriod) {
        eventRetentionPeriod.set(retentionPeriod);
        logger.info("Event retention period set to: {}", retentionPeriod);
    }

    // Statistics and Monitoring

    /**
     * Get event processing statistics
     */
    public EventProcessingStatistics getStatistics() {
        return new EventProcessingStatistics(totalEventsProcessed.get(), filteredEvents.get(), enrichedEvents.get(),
                routedEvents.get(), persistedEvents.get(), reasoningTriggers.get(), enableEventProcessing.get(),
                enableEventPersistence.get(), enableReasoningIntegration.get(), eventRetentionPeriod.get(),
                replayMode.get());
    }

    /**
     * Reset statistics
     */
    public void resetStatistics() {
        totalEventsProcessed.set(0);
        filteredEvents.set(0);
        enrichedEvents.set(0);
        routedEvents.set(0);
        persistedEvents.set(0);
        reasoningTriggers.set(0);
        logger.info("Event processing statistics reset");
    }

    /**
     * Event processing statistics
     */
    public static class EventProcessingStatistics {
        private final long totalEventsProcessed;
        private final long filteredEvents;
        private final long enrichedEvents;
        private final long routedEvents;
        private final long persistedEvents;
        private final long reasoningTriggers;
        private final boolean eventProcessingEnabled;
        private final boolean eventPersistenceEnabled;
        private final boolean reasoningIntegrationEnabled;
        private final Duration eventRetentionPeriod;
        private final boolean replayInProgress;

        public EventProcessingStatistics(long totalEventsProcessed, long filteredEvents, long enrichedEvents,
                long routedEvents, long persistedEvents, long reasoningTriggers, boolean eventProcessingEnabled,
                boolean eventPersistenceEnabled, boolean reasoningIntegrationEnabled, Duration eventRetentionPeriod,
                boolean replayInProgress) {
            this.totalEventsProcessed = totalEventsProcessed;
            this.filteredEvents = filteredEvents;
            this.enrichedEvents = enrichedEvents;
            this.routedEvents = routedEvents;
            this.persistedEvents = persistedEvents;
            this.reasoningTriggers = reasoningTriggers;
            this.eventProcessingEnabled = eventProcessingEnabled;
            this.eventPersistenceEnabled = eventPersistenceEnabled;
            this.reasoningIntegrationEnabled = reasoningIntegrationEnabled;
            this.eventRetentionPeriod = eventRetentionPeriod;
            this.replayInProgress = replayInProgress;
        }

        public long getTotalEventsProcessed() {
            return totalEventsProcessed;
        }

        public long getFilteredEvents() {
            return filteredEvents;
        }

        public long getEnrichedEvents() {
            return enrichedEvents;
        }

        public long getRoutedEvents() {
            return routedEvents;
        }

        public long getPersistedEvents() {
            return persistedEvents;
        }

        public long getReasoningTriggers() {
            return reasoningTriggers;
        }

        public boolean isEventProcessingEnabled() {
            return eventProcessingEnabled;
        }

        public boolean isEventPersistenceEnabled() {
            return eventPersistenceEnabled;
        }

        public boolean isReasoningIntegrationEnabled() {
            return reasoningIntegrationEnabled;
        }

        public Duration getEventRetentionPeriod() {
            return eventRetentionPeriod;
        }

        public boolean isReplayInProgress() {
            return replayInProgress;
        }

        public double getFilterRate() {
            return totalEventsProcessed > 0 ? (double) filteredEvents / totalEventsProcessed : 0.0;
        }

        public double getEnrichmentRate() {
            return totalEventsProcessed > 0 ? (double) enrichedEvents / totalEventsProcessed : 0.0;
        }

        public double getReasoningTriggerRate() {
            return totalEventsProcessed > 0 ? (double) reasoningTriggers / totalEventsProcessed : 0.0;
        }
    }
}
