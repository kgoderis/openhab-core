package org.openhab.core.ai.common.monitoring.service.snapshot;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.EventBusMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;

/**
 * Snapshot for event bus metrics.
 * 
 * <p>
 * This class provides a snapshot of event bus performance metrics including
 * event processing counts, latency, and event bus specific metrics.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record EventBusSnapshot(long total, long success, long failure, long totalDurationNanos, long timestampMs,
        long totalEvents, long totalSubscriptions, long totalSchemas, long totalPublishers, long totalSubscribers)
        implements CountsMetrics, LatencyMetrics, EventBusMetrics {

    @Override
    public double successRate() {
        if (total == 0)
            return 0.0;
        return (success * 100.0) / total;
    }

    @Override
    public double averageMs(long total) {
        if (total == 0)
            return 0.0;
        return totalDurationNanos / (total * 1_000_000.0);
    }

    @Override
    public long totalEvents() {
        return totalEvents;
    }

    @Override
    public long totalSubscriptions() {
        return totalSubscriptions;
    }

    @Override
    public long totalSchemas() {
        return totalSchemas;
    }

    @Override
    public long totalPublishers() {
        return totalPublishers;
    }

    @Override
    public long totalSubscribers() {
        return totalSubscribers;
    }

    @Override
    public double eventsPerSecond() {
        if (totalDurationNanos == 0)
            return 0.0;
        return (totalEvents * 1_000_000_000.0) / totalDurationNanos;
    }

    @Override
    public double averageEventsPerOperation() {
        if (total == 0)
            return 0.0;
        return (double) totalEvents / total;
    }

    @Override
    public double subscriptionUtilization() {
        if (totalSubscribers == 0)
            return 0.0;
        return (double) totalSubscriptions / totalSubscribers;
    }

    @Override
    public double publisherUtilization() {
        if (totalPublishers == 0)
            return 0.0;
        return (double) totalEvents / totalPublishers;
    }
}
