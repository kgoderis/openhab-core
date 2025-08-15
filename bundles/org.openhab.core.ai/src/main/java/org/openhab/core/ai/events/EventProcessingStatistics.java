package org.openhab.core.ai.events;

import java.time.Duration;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Event processing statistics for {@link EventSystemIntegration}.
 *
 * Captures counters and configuration flags for the end-to-end
 * event processing pipeline, including filtering, enrichment,
 * routing, persistence, and reasoning triggers.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class EventProcessingStatistics {
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
