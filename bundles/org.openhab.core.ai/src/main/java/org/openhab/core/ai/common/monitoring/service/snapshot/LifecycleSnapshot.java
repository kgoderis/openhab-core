package org.openhab.core.ai.common.monitoring.service.snapshot;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.LifecycleMetrics;

/**
 * Snapshot for lifecycle metrics.
 * 
 * <p>
 * This class provides a snapshot of lifecycle performance metrics including
 * lifecycle operation counts, latency, and lifecycle specific metrics.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record LifecycleSnapshot(long total, long success, long failure, long totalDurationNanos, long timestampMs,
        long totalInitializations, long totalActivations, long totalDeactivations, long totalDestructions)
        implements CountsMetrics, LatencyMetrics, LifecycleMetrics {

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
    public long totalInitializations() {
        return totalInitializations;
    }

    @Override
    public long totalActivations() {
        return totalActivations;
    }

    @Override
    public long totalDeactivations() {
        return totalDeactivations;
    }

    @Override
    public long totalDestructions() {
        return totalDestructions;
    }

    @Override
    public double startupTime() {
        if (totalInitializations == 0)
            return 0.0;
        return totalDurationNanos / (totalInitializations * 1_000_000.0);
    }

    @Override
    public double shutdownTime() {
        if (totalDestructions == 0)
            return 0.0;
        return totalDurationNanos / (totalDestructions * 1_000_000.0);
    }

    @Override
    public double uptime() {
        // Convert nanoseconds to hours
        return totalDurationNanos / (3_600_000_000_000.0);
    }

    @Override
    public double restartFrequency() {
        if (totalDurationNanos == 0)
            return 0.0;
        // Convert to restarts per hour
        return (totalInitializations * 3_600_000_000_000.0) / totalDurationNanos;
    }

    @Override
    public double healthScore() {
        if (total == 0)
            return 0.0;
        return (success * 100.0) / total;
    }
}
