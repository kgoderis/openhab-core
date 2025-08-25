package org.openhab.core.ai.common.monitoring.service.snapshot;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.Counts;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.MetricsSnapshot;
import org.openhab.core.ai.common.monitoring.api.Timing;
import org.openhab.core.ai.common.monitoring.api.ToolMetrics;

/**
 * Snapshot for tool file read operations with capability interfaces.
 * 
 * <p>
 * This snapshot provides comprehensive metrics for tool file read operations:
 * - Basic counting metrics (total, success, failure)
 * - Latency metrics (duration, averages)
 * - Tool-specific metrics (throughput, resource usage)
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record ToolFileReadSnapshot(Counts counts, Timing timing, long timestampMs, long totalBytesRead,
        long totalFilesProcessed) implements MetricsSnapshot, CountsMetrics, LatencyMetrics, ToolMetrics {

    // CountsMetrics implementation
    @Override
    public long total() {
        return counts.total();
    }

    @Override
    public long success() {
        return counts.success();
    }

    @Override
    public long failure() {
        return counts.failure();
    }

    @Override
    public double successRate() {
        if (total() == 0)
            return 0.0;
        return (success() * 100.0) / total();
    }

    // LatencyMetrics implementation
    @Override
    public long totalDurationNanos() {
        return timing.totalDurationNanos();
    }

    public double averageMs() {
        return LatencyMetrics.super.averageMs(total());
    }

    public double operationsPerSecond() {
        if (timing.totalDurationNanos() == 0)
            return 0.0;
        double durationSeconds = timing.totalDurationNanos() / 1_000_000_000.0;
        return total() / durationSeconds;
    }

    // ToolMetrics implementation
    public double throughputPerSecond() {
        if (timing.totalDurationNanos() == 0)
            return 0.0;
        double durationSeconds = timing.totalDurationNanos() / 1_000_000_000.0;
        return totalBytesRead / durationSeconds;
    }

    public double averageResourceUsage() {
        if (total() == 0)
            return 0.0;
        return (double) totalBytesRead / total();
    }

    public int maxConcurrentExecutions() {
        // For now, return a default value. This could be enhanced to track actual concurrent executions
        // by analyzing operation timestamps and overlapping execution windows.
        return total() > 0 ? (int) Math.min(total(), 10) : 1; // Assume max 10 concurrent executions or total operations
    }
}
