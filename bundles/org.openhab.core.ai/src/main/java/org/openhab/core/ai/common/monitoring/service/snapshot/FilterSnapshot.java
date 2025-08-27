package org.openhab.core.ai.common.monitoring.service.snapshot;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.FilterMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;

/**
 * Snapshot for filter metrics.
 * 
 * <p>
 * This class provides a snapshot of filter performance metrics including
 * filter operation counts, latency, and filter specific metrics.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record FilterSnapshot(long total, long success, long failure, long totalDurationNanos, long timestampMs,
        long totalFiltered, long totalAllowed, long totalBlocked, long totalQuarantined, long totalRules)
        implements CountsMetrics, LatencyMetrics, FilterMetrics {

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
    public long totalFiltered() {
        return totalFiltered;
    }

    @Override
    public long totalAllowed() {
        return totalAllowed;
    }

    @Override
    public long totalBlocked() {
        return totalBlocked;
    }

    @Override
    public long totalQuarantined() {
        return totalQuarantined;
    }

    @Override
    public long totalRules() {
        return totalRules;
    }

    @Override
    public double filteringEfficiency() {
        if (total == 0)
            return 0.0;
        return (totalFiltered * 100.0) / total;
    }

    @Override
    public double blockingRate() {
        if (total == 0)
            return 0.0;
        return (totalBlocked * 100.0) / total;
    }

    @Override
    public double quarantineRate() {
        if (total == 0)
            return 0.0;
        return (totalQuarantined * 100.0) / total;
    }

    @Override
    public double ruleUtilization() {
        if (totalRules == 0)
            return 0.0;
        return (total * 100.0) / totalRules;
    }
}
