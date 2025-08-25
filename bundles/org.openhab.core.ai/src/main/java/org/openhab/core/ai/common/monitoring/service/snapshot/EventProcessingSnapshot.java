package org.openhab.core.ai.common.monitoring.service.snapshot;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.EventMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.service.statistics.StatisticsSnapshot;

@NonNullByDefault
public record EventProcessingSnapshot(long total, long success, long failure, long totalDurationNanos,
        double eventProcessingRate, long eventQueueDepth, double eventCorrelationEfficiency,
        double eventProcessingLatency, double eventErrorRate, double eventThroughput, double eventHandlingSuccessRate,
        double eventFilteringEfficiency, double eventRoutingAccuracy, double eventPriorityHandlingEfficiency,
        long timestampMs) implements StatisticsSnapshot, CountsMetrics, LatencyMetrics, EventMetrics {

    // CountsMetrics implementation
    @Override
    public long total() {
        return total;
    }

    @Override
    public long success() {
        return success;
    }

    @Override
    public long failure() {
        return failure;
    }

    @Override
    public double successRate() {
        if (total == 0) {
            return 0.0;
        }
        return (success * 100.0) / total;
    }

    // LatencyMetrics implementation
    @Override
    public long totalDurationNanos() {
        return totalDurationNanos;
    }

    @Override
    public double averageMs(long total) {
        if (total == 0) {
            return 0.0;
        }
        return totalDurationNanos / (total * 1_000_000.0);
    }

    // EventMetrics implementation
    @Override
    public double eventProcessingRate() {
        return eventProcessingRate;
    }

    @Override
    public long eventQueueDepth() {
        return eventQueueDepth;
    }

    @Override
    public double eventCorrelationEfficiency() {
        return eventCorrelationEfficiency;
    }

    @Override
    public double eventProcessingLatency() {
        return eventProcessingLatency;
    }

    @Override
    public double eventErrorRate() {
        return eventErrorRate;
    }

    @Override
    public double eventThroughput() {
        return eventThroughput;
    }

    @Override
    public double eventHandlingSuccessRate() {
        return eventHandlingSuccessRate;
    }

    @Override
    public double eventFilteringEfficiency() {
        return eventFilteringEfficiency;
    }

    @Override
    public double eventRoutingAccuracy() {
        return eventRoutingAccuracy;
    }

    @Override
    public double eventPriorityHandlingEfficiency() {
        return eventPriorityHandlingEfficiency;
    }
}
