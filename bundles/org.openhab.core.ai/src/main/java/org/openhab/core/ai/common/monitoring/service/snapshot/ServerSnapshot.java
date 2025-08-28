package org.openhab.core.ai.common.monitoring.service.snapshot;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.ServerMetrics;

/**
 * Snapshot for server metrics.
 * 
 * <p>
 * This class provides a snapshot of server performance metrics including
 * server operation counts, latency, and server specific metrics.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record ServerSnapshot(long total, long success, long failure, long totalDurationNanos, long timestampMs,
        long totalRequests, long totalResponses, long totalConnections, long totalSessions,
        long totalEndpoints) implements CountsMetrics, LatencyMetrics, ServerMetrics {

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
    public long totalRequests() {
        return totalRequests;
    }

    @Override
    public long totalResponses() {
        return totalResponses;
    }

    @Override
    public long totalConnections() {
        return totalConnections;
    }

    @Override
    public long totalSessions() {
        return totalSessions;
    }

    @Override
    public long totalEndpoints() {
        return totalEndpoints;
    }

    @Override
    public double requestRate() {
        if (totalDurationNanos == 0)
            return 0.0;
        return (totalRequests * 1_000_000_000.0) / totalDurationNanos;
    }

    @Override
    public double responseRate() {
        if (totalDurationNanos == 0)
            return 0.0;
        return (totalResponses * 1_000_000_000.0) / totalDurationNanos;
    }

    @Override
    public double connectionUtilization() {
        if (totalConnections == 0)
            return 0.0;
        return (total * 100.0) / totalConnections;
    }

    @Override
    public double sessionUtilization() {
        if (totalSessions == 0)
            return 0.0;
        return (total * 100.0) / totalSessions;
    }
}
