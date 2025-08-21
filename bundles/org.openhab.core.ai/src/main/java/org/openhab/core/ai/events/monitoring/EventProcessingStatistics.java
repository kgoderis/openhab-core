package org.openhab.core.ai.events.monitoring;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.base.AbstractStatistics;

/**
 * Consolidated event processing statistics that extends the unified monitoring framework.
 *
 * <p>
 * This class provides comprehensive statistics for event processing operations including
 * filtering, enrichment, routing, persistence, and reasoning triggers.
 * It implements CountsMetrics capability interface.
 * </p>
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class EventProcessingStatistics extends AbstractStatistics implements CountsMetrics {

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

    /**
     * Create a new EventProcessingStatistics instance.
     *
     * @param id unique identifier for this statistics instance
     * @param timestamp timestamp when statistics were collected
     * @param totalOperations total number of event processing operations
     * @param successfulOperations number of successful operations
     * @param failedOperations number of failed operations
     * @param filteredEvents number of filtered events
     * @param enrichedEvents number of enriched events
     * @param routedEvents number of routed events
     * @param persistedEvents number of persisted events
     * @param reasoningTriggers number of reasoning triggers
     * @param eventProcessingEnabled whether event processing is enabled
     * @param eventPersistenceEnabled whether event persistence is enabled
     * @param reasoningIntegrationEnabled whether reasoning integration is enabled
     * @param eventRetentionPeriod event retention period
     * @param replayInProgress whether replay is in progress
     * @param data additional monitoring data
     */
    public EventProcessingStatistics(String id, Instant timestamp, long totalOperations, long successfulOperations,
            long failedOperations, long filteredEvents, long enrichedEvents, long routedEvents, long persistedEvents,
            long reasoningTriggers, boolean eventProcessingEnabled, boolean eventPersistenceEnabled,
            boolean reasoningIntegrationEnabled, Duration eventRetentionPeriod, boolean replayInProgress,
            @Nullable Map<String, Object> data) {
        super(id, timestamp, "events", "event-processing-statistics", "Event processing statistics", data,
                totalOperations, successfulOperations, failedOperations, null, null, null);
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

    /**
     * Create a new EventProcessingStatistics instance with current timestamp.
     *
     * @param id unique identifier for this statistics instance
     * @param totalOperations total number of event processing operations
     * @param successfulOperations number of successful operations
     * @param failedOperations number of failed operations
     * @param filteredEvents number of filtered events
     * @param enrichedEvents number of enriched events
     * @param routedEvents number of routed events
     * @param persistedEvents number of persisted events
     * @param reasoningTriggers number of reasoning triggers
     * @param eventProcessingEnabled whether event processing is enabled
     * @param eventPersistenceEnabled whether event persistence is enabled
     * @param reasoningIntegrationEnabled whether reasoning integration is enabled
     * @param eventRetentionPeriod event retention period
     * @param replayInProgress whether replay is in progress
     */
    public EventProcessingStatistics(String id, long totalOperations, long successfulOperations, long failedOperations,
            long filteredEvents, long enrichedEvents, long routedEvents, long persistedEvents, long reasoningTriggers,
            boolean eventProcessingEnabled, boolean eventPersistenceEnabled, boolean reasoningIntegrationEnabled,
            Duration eventRetentionPeriod, boolean replayInProgress) {
        this(id, Instant.now(), totalOperations, successfulOperations, failedOperations, filteredEvents, enrichedEvents,
                routedEvents, persistedEvents, reasoningTriggers, eventProcessingEnabled, eventPersistenceEnabled,
                reasoningIntegrationEnabled, eventRetentionPeriod, replayInProgress, null);
    }

    /**
     * Get the number of filtered events.
     *
     * @return filtered events count
     */
    public long getFilteredEvents() {
        return filteredEvents;
    }

    /**
     * Get the number of enriched events.
     *
     * @return enriched events count
     */
    public long getEnrichedEvents() {
        return enrichedEvents;
    }

    /**
     * Get the number of routed events.
     *
     * @return routed events count
     */
    public long getRoutedEvents() {
        return routedEvents;
    }

    /**
     * Get the number of persisted events.
     *
     * @return persisted events count
     */
    public long getPersistedEvents() {
        return persistedEvents;
    }

    /**
     * Get the number of reasoning triggers.
     *
     * @return reasoning triggers count
     */
    public long getReasoningTriggers() {
        return reasoningTriggers;
    }

    /**
     * Check if event processing is enabled.
     *
     * @return true if event processing is enabled
     */
    public boolean isEventProcessingEnabled() {
        return eventProcessingEnabled;
    }

    /**
     * Check if event persistence is enabled.
     *
     * @return true if event persistence is enabled
     */
    public boolean isEventPersistenceEnabled() {
        return eventPersistenceEnabled;
    }

    /**
     * Check if reasoning integration is enabled.
     *
     * @return true if reasoning integration is enabled
     */
    public boolean isReasoningIntegrationEnabled() {
        return reasoningIntegrationEnabled;
    }

    /**
     * Get the event retention period.
     *
     * @return event retention period
     */
    public Duration getEventRetentionPeriod() {
        return eventRetentionPeriod;
    }

    /**
     * Check if replay is in progress.
     *
     * @return true if replay is in progress
     */
    public boolean isReplayInProgress() {
        return replayInProgress;
    }

    // CountsMetrics interface implementation
    @Override
    public long total() {
        return getTotalCount();
    }

    @Override
    public long success() {
        return getSuccessCount();
    }

    @Override
    public long failure() {
        return getFailureCount();
    }

    /**
     * Get the filter rate as a percentage.
     *
     * @return filter rate percentage
     */
    public double getFilterRate() {
        return total() > 0 ? (double) filteredEvents / total() : 0.0;
    }

    /**
     * Get the enrichment rate as a percentage.
     *
     * @return enrichment rate percentage
     */
    public double getEnrichmentRate() {
        return total() > 0 ? (double) enrichedEvents / total() : 0.0;
    }

    /**
     * Get the reasoning trigger rate as a percentage.
     *
     * @return reasoning trigger rate percentage
     */
    public double getReasoningTriggerRate() {
        return total() > 0 ? (double) reasoningTriggers / total() : 0.0;
    }

    /**
     * Get the event processing efficiency score.
     *
     * @return event processing efficiency score between 0.0 and 1.0
     */
    public double getEventProcessingEfficiency() {
        double successRate = successRate();
        double filterEfficiency = getFilterRate() > 0.8 ? 1.0
                : getFilterRate() > 0.6 ? 0.8 : getFilterRate() > 0.4 ? 0.6 : 0.4;
        double enrichmentEfficiency = getEnrichmentRate() > 0.7 ? 1.0
                : getEnrichmentRate() > 0.5 ? 0.8 : getEnrichmentRate() > 0.3 ? 0.6 : 0.4;
        double reasoningEfficiency = getReasoningTriggerRate() > 0.1 ? 1.0
                : getReasoningTriggerRate() > 0.05 ? 0.8 : getReasoningTriggerRate() > 0.01 ? 0.6 : 0.4;

        return (successRate * 0.4) + (filterEfficiency * 0.3) + (enrichmentEfficiency * 0.2)
                + (reasoningEfficiency * 0.1);
    }

    /**
     * Check if event processing is performing well (high success rate, good filtering).
     *
     * @return true if event processing is performing well
     */
    public boolean isPerformingWell() {
        return successRate() > 0.9 && getFilterRate() > 0.6 && getEnrichmentRate() > 0.5;
    }

    /**
     * Check if there are critical event processing issues (low success rate or poor filtering).
     *
     * @return true if there are critical event processing issues
     */
    public boolean hasCriticalIssues() {
        return successRate() < 0.8 || getFilterRate() < 0.3 || getEnrichmentRate() < 0.2;
    }
}
