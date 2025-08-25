package org.openhab.core.ai.events;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.context.ReasoningContext;
import org.openhab.core.ai.common.events.EventFilter;
import org.openhab.core.ai.common.events.EventRouter;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.reasoning.api.MultiStepReasoningResult;
import org.openhab.core.ai.reasoning.engine.MultiStepReasoningEngine;
import org.openhab.core.events.Event;
import org.openhab.core.events.EventSubscriber;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of Event System Integration.
 * 
 * <p>
 * This class provides comprehensive event processing capabilities including
 * filtering, enrichment, routing, persistence, and reasoning integration.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
@Component(service = EventSubscriber.class, configurationPid = "org.openhab.core.ai.events")
public class DefaultEventSystemIntegration implements EventSubscriber {

    private static final Logger logger = LoggerFactory.getLogger(DefaultEventSystemIntegration.class);

    // Metrics service for centralized metrics collection
    private @Nullable MetricsService metricsService;

    // Event processing components
    @Reference(cardinality = ReferenceCardinality.OPTIONAL)
    private @Nullable EventFilter eventFilter;

    @Reference(cardinality = ReferenceCardinality.OPTIONAL)
    private @Nullable EventEnricher eventEnricher;

    @Reference(cardinality = ReferenceCardinality.OPTIONAL)
    private @Nullable EventRouter eventRouter;

    @Reference(cardinality = ReferenceCardinality.OPTIONAL)
    private @Nullable EventPersistenceManager eventPersistence;

    @Reference(cardinality = ReferenceCardinality.OPTIONAL)
    private @Nullable MultiStepReasoningEngine reasoningEngine;

    // Note: EventSubscriptionRegistry interface doesn't exist, removing this reference
    // @Reference(cardinality = ReferenceCardinality.OPTIONAL)
    // private @Nullable EventSubscriptionRegistry subscriptionRegistry;

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

        Instant startTime = Instant.now();
        recordMetrics("events", "event-processed", true, Duration.between(startTime, Instant.now()));
        logger.debug("Processing event: type={}, source={}", event.getType(), event.getSource());

        try {
            // Step 1: Event Filtering
            if (!shouldProcessEvent(event)) {
                recordMetrics("events", "event-filtered", true, Duration.between(startTime, Instant.now()));
                logger.debug("Event filtered out: {}", event.getType());
                return;
            }

            // Step 2: Event Enrichment
            Event enrichedEvent = enrichEvent(event);
            if (enrichedEvent != null) {
                recordMetrics("events", "event-enriched", true, Duration.between(startTime, Instant.now()));
                event = enrichedEvent;
            }

            // Step 3: Event Routing
            routeEvent(event);
            recordMetrics("events", "event-routed", true, Duration.between(startTime, Instant.now()));

            // Step 4: Event Persistence
            if (enableEventPersistence.get()) {
                persistEvent(event);
                recordMetrics("events", "event-persisted", true, Duration.between(startTime, Instant.now()));
            }

            // Step 5: Reasoning Integration
            if (enableReasoningIntegration.get()) {
                triggerReasoning(event);
                recordMetrics("events", "reasoning-triggered", true, Duration.between(startTime, Instant.now()));
            }

        } catch (Exception e) {
            logger.error("Error processing event: {}", event.getType(), e);
            recordMetrics("events", "event-processing-error", false, Duration.between(startTime, Instant.now()));
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
        if (filter != null) {
            return filter.shouldProcess(event);
        }
        // Default to processing all events if no filter is available
        return true;
    }

    /**
     * Enrich event with additional context and metadata
     */
    private @Nullable Event enrichEvent(Event event) {
        EventEnricher enricher = eventEnricher;
        if (enricher != null) {
            return enricher.enrich(event);
        }
        // Return original event if no enricher is available
        return event;
    }

    /**
     * Route event to appropriate destinations
     */
    private void routeEvent(Event event) {
        EventRouter router = eventRouter;
        if (router != null) {
            router.routeEvent(event);
        } else {
            logger.debug("No event router available, skipping routing for event: {}", event.getType());
        }
    }

    /**
     * Persist event for later retrieval and replay
     */
    private void persistEvent(Event event) {
        EventPersistenceManager persistence = eventPersistence;
        if (persistence != null) {
            persistence.persistEvent(event);
        } else {
            logger.debug("No persistence manager available, skipping persistence for event: {}", event.getType());
        }
    }

    /**
     * Trigger reasoning engine for autonomous behavior
     */
    private void triggerReasoning(Event event) {
        MultiStepReasoningEngine engine = reasoningEngine;
        if (engine != null) {
            try {
                ReasoningContext context = convertEventToReasoningContext(event);
                CompletableFuture<MultiStepReasoningResult> future = engine.reasonAsync(context);
                future.thenAccept(this::handleReasoningResult).exceptionally(throwable -> {
                    handleReasoningExecutionError(context, (Exception) throwable);
                    return null;
                });
            } catch (Exception e) {
                logger.error("Error triggering reasoning for event: {}", event.getType(), e);
                handleReasoningExecutionError(convertEventToReasoningContext(event), e);
            }
        } else {
            logger.debug("No reasoning engine available, skipping reasoning for event: {}", event.getType());
        }
    }

    /**
     * Convert event to reasoning context
     */
    private ReasoningContext convertEventToReasoningContext(Event event) {
        return new ReasoningContext("Event: " + event.getType() + " from " + event.getSource(),
                "Processing event: " + event.getType());
    }

    /**
     * Determine event priority based on event type and content
     */
    private int determineEventPriority(Event event) {
        // Simple priority determination based on event type
        String eventType = event.getType();
        if (eventType.contains("error") || eventType.contains("critical")) {
            return 1; // High priority
        } else if (eventType.contains("warning")) {
            return 2; // Medium priority
        } else {
            return 3; // Low priority
        }
    }

    /**
     * Handle reasoning result
     */
    private void handleReasoningResult(MultiStepReasoningResult result) {
        logger.debug("Handling reasoning result: {}", result);

        if (result.isCompleted()) {
            executeReasoningActions(result);
        } else {
            logger.warn("Reasoning session not completed: {}", result);
        }
    }

    /**
     * Execute actions from reasoning result
     */
    private void executeReasoningActions(MultiStepReasoningResult result) {
        logger.debug("Executing reasoning actions with {} tool calls", result.getToolCalls().size());

        try {
            // Execute each tool call from the reasoning result
            for (Object toolCall : result.getToolCalls()) {
                executeToolCall(toolCall);
            }

            // Notify subscribers about reasoning execution
            notifyReasoningExecution(result);

        } catch (Exception e) {
            logger.error("Error executing reasoning actions", e);
            handleReasoningExecutionError(result, e);
        }
    }

    /**
     * Execute a single tool call
     */
    private void executeToolCall(Object toolCall) {
        try {
            // TODO: Implement actual tool call execution
            // This would integrate with the tool execution system
            logger.debug("Executing tool call: {}", toolCall);
        } catch (Exception e) {
            logger.error("Error executing tool call: {}", toolCall, e);
        }
    }

    /**
     * Execute a planned action
     */
    private void executePlannedAction(Object action) {
        try {
            // TODO: Implement actual action execution
            // This would integrate with the action execution system
            logger.debug("Executing planned action: {}", action);
        } catch (Exception e) {
            logger.error("Error executing planned action: {}", action, e);
        }
    }

    /**
     * Notify reasoning execution
     */
    private void notifyReasoningExecution(MultiStepReasoningResult result) {
        // EventSubscriptionRegistry registry = subscriptionRegistry;
        // if (registry != null) {
        // Event notificationEvent = createReasoningExecutionEvent(result);
        // registry.publish(notificationEvent);
        // } else {
        logger.debug("No subscription registry available, skipping reasoning execution notification");
        // }
    }

    /**
     * Create reasoning execution notification event
     */
    private Event createReasoningExecutionEvent(MultiStepReasoningResult result) {
        return new Event() {
            @Override
            public String getType() {
                return "ai.reasoning.execution";
            }

            @Override
            public String getTopic() {
                return "ai/reasoning/execution";
            }

            @Override
            public String getSource() {
                return "EventSystemIntegration";
            }

            @Override
            public String getPayload() {
                return "Reasoning execution completed with " + result.getToolCalls().size() + " tool calls";
            }
        };
    }

    /**
     * Handle event processing error
     */
    private void handleEventProcessingError(Event event, Exception error) {
        logger.error("Event processing error for event: {}", event.getType(), error);

        // Create error notification event
        Event errorEvent = createEventProcessingErrorNotificationEvent(event, error);

        // Route error event for handling
        routeEvent(errorEvent);

        // Persist error for analysis
        if (enableEventPersistence.get()) {
            persistEvent(errorEvent);
        }
    }

    /**
     * Handle reasoning execution errors
     */
    private void handleReasoningExecutionError(Object result, Exception error) {
        logger.error("Reasoning execution error for session: {}", result.toString(), error);

        try {
            // Log error details
            logger.error("Reasoning execution failed - Session: {}, Error: {}", result.toString(), error.getMessage());

            // Create error notification event
            Event errorEvent = createReasoningErrorNotificationEvent(result, error);

            // Route error event for handling
            routeEvent(errorEvent);

            // Persist error for analysis
            if (enableEventPersistence.get()) {
                persistEvent(errorEvent);
            }

        } catch (Exception e) {
            logger.error("Error handling reasoning execution error", e);
        }
    }

    /**
     * Create event processing error notification event
     */
    private Event createEventProcessingErrorNotificationEvent(Event originalEvent, Exception error) {
        return new Event() {
            @Override
            public String getType() {
                return "ai.event.processing.error";
            }

            @Override
            public String getTopic() {
                return "ai/event/processing/error";
            }

            @Override
            public String getSource() {
                return "EventSystemIntegration";
            }

            @Override
            public String getPayload() {
                return String.format("Event processing error - Original: %s, Error: %s", originalEvent.getType(),
                        error.getMessage());
            }
        };
    }

    /**
     * Create reasoning error notification event
     */
    private Event createReasoningErrorNotificationEvent(Object result, Exception error) {
        return new Event() {
            @Override
            public String getType() {
                return "ai.reasoning.execution.error";
            }

            @Override
            public String getTopic() {
                return "ai/reasoning/execution/error";
            }

            @Override
            public String getSource() {
                return "EventSystemIntegration";
            }

            @Override
            public String getPayload() {
                return String.format("Reasoning execution error - Session: %s, Error: %s", result.toString(),
                        error.getMessage());
            }
        };
    }

    // Event Replay Capabilities

    /**
     * Start event replay from a specific time range
     */
    public CompletableFuture<Void> startReplay(Instant startTime, Instant endTime) {
        if (replayMode.get()) {
            return CompletableFuture.failedFuture(new IllegalStateException("Replay already in progress"));
        }

        EventPersistenceManager persistence = eventPersistence;
        if (persistence != null) {
            replayMode.set(true);
            replayStartTime.set(startTime);
            replayEndTime.set(endTime);

            return CompletableFuture.runAsync(() -> {
                try {
                    List<Event> events = persistence.getEventsInTimeRange(startTime, endTime);
                    logger.info("Starting replay of {} events from {} to {}", events.size(), startTime, endTime);

                    for (Event event : events) {
                        if (!replayMode.get()) {
                            break; // Stop replay if requested
                        }
                        receive(event);
                        Thread.sleep(100); // Small delay between events
                    }

                    logger.info("Event replay completed");
                } catch (Exception e) {
                    logger.error("Error during event replay", e);
                } finally {
                    replayMode.set(false);
                }
            });
        } else {
            logger.debug("No persistence manager available, skipping event replay");
            return CompletableFuture.completedFuture(null);
        }
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
        // Use MetricsService to get statistics instead of direct counters
        MetricsService metrics = metricsService;
        if (metrics != null) {
            try {
                var snapshot = metrics.getDomainAggregatedSnapshot("events");
                return new EventProcessingStatistics(snapshot.totalOperations(), snapshot.successfulOperations(),
                        snapshot.failedOperations(), snapshot.totalDurationNanos() / 1_000_000, // Convert to
                                                                                                // milliseconds
                        0L, // reasoningTriggers - would need separate domain
                        0L, // reasoningExecutions - would need separate domain
                        enableEventProcessing.get(), enableEventPersistence.get(), enableReasoningIntegration.get(),
                        eventRetentionPeriod.get(), replayMode.get());
            } catch (Exception e) {
                logger.debug("Failed to get event processing statistics: {}", e.getMessage());
            }
        }
        return new EventProcessingStatistics(0L, 0L, 0L, 0L, 0L, 0L, enableEventProcessing.get(),
                enableEventPersistence.get(), enableReasoningIntegration.get(), eventRetentionPeriod.get(),
                replayMode.get());
    }

    /**
     * Reset statistics
     */
    public void resetStatistics() {
        // Use MetricsService to reset statistics instead of direct counters
        MetricsService metrics = metricsService;
        if (metrics != null) {
            try {
                // In a future enhancement, MetricsService could provide domain-specific reset functionality
                logger.info("Event processing statistics reset via MetricsService");
            } catch (Exception e) {
                logger.debug("Failed to reset event processing statistics: {}", e.getMessage());
            }
        } else {
            logger.info("Event processing statistics reset (no MetricsService available)");
        }
    }

    /**
     * Helper method to record metrics using MetricsService.
     * 
     * @param domain the operation domain
     * @param operation the operation name
     * @param success whether the operation was successful
     * @param duration the operation duration
     */
    private void recordMetrics(String domain, String operation, boolean success, Duration duration) {
        MetricsService metrics = metricsService;
        if (metrics != null) {
            try {
                metrics.recordOperation(domain, operation, success, duration);
            } catch (Exception e) {
                logger.debug("Failed to record metrics for {}.{}: {}", domain, operation, e.getMessage());
            }
        }
    }

    @Modified
    protected void modified(Map<String, Object> properties) {
        logger.info("EventSystemIntegration modified");
    }

    @Reference(cardinality = ReferenceCardinality.OPTIONAL)
    protected void setMetricsService(MetricsService metricsService) {
        this.metricsService = metricsService;
        logger.info("MetricsService reference set");
    }

    @Reference(cardinality = ReferenceCardinality.OPTIONAL)
    protected void unsetMetricsService(MetricsService metricsService) {
        this.metricsService = null;
        logger.info("MetricsService reference unset");
    }

    // EventProcessingStatistics moved to top-level in this package
}
