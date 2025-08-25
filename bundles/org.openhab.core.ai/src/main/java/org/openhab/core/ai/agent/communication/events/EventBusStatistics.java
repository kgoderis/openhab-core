package org.openhab.core.ai.agent.communication.events;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.osgi.service.component.annotations.Reference;

/**
 * Event bus statistics snapshot.
 *
 * Summarizes current and historical event bus metrics.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class EventBusStatistics {

    @Reference
    private @Nullable MetricsService metricsService;
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

    /**
     * Record event bus statistics using MetricsService.
     */
    public void recordEventBusStatistics() {
        MetricsService metrics = metricsService;
        if (metrics != null) {
            long startTime = System.currentTimeMillis();
            boolean success = totalEventsFailed == 0 && deadLetterQueueSize == 0;
            long duration = System.currentTimeMillis() - startTime;

            metrics.recordOperation("event-bus", "statistics", success, java.time.Duration.ofMillis(duration));
        }
    }
}
