package org.openhab.core.ai.common.monitoring.service.statistics;

import java.time.Duration;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.service.snapshot.DomainAggregatedSnapshot;

/**
 * Statistics class for agent persistence operations.
 * 
 * <p>
 * This class provides computed statistics and insights about agent persistence
 * operations derived from metrics data over time ranges. It implements capability
 * interfaces for clean, type-safe statistics access.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record AgentPersistenceStatistics(List<DomainAggregatedSnapshot> snapshots, Duration timeRange,
        long timestampMs) implements StatisticsSnapshot, CountsMetrics, LatencyMetrics {

    // CountsMetrics implementation
    @Override
    public long total() {
        return snapshots.stream().mapToLong(s -> s.totalOperations()).sum();
    }

    @Override
    public long success() {
        return snapshots.stream().mapToLong(s -> s.successfulOperations()).sum();
    }

    @Override
    public long failure() {
        return snapshots.stream().mapToLong(s -> s.failedOperations()).sum();
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
        return snapshots.stream().mapToLong(s -> s.totalDurationNanos()).sum();
    }

    public double averageMs() {
        if (total() == 0)
            return 0.0;
        return totalDurationNanos() / (total() * 1_000_000.0);
    }

    public double operationsPerSecond() {
        if (timeRange.toNanos() == 0)
            return 0.0;
        return total() / (timeRange.toNanos() / 1_000_000_000.0);
    }

    /**
     * Get the average success rate across all snapshots.
     * 
     * @return average success rate as a percentage
     */
    public double getAverageSuccessRate() {
        if (snapshots.isEmpty())
            return 0.0;
        return snapshots.stream().mapToDouble(s -> s.averageSuccessRate()).average().orElse(0.0);
    }

    /**
     * Get the total number of persistence operations.
     * 
     * @return total operations count
     */
    public long getTotalPersistenceOperations() {
        return total();
    }

    /**
     * Get the number of successful persistence operations.
     * 
     * @return successful operations count
     */
    public long getSuccessfulPersistenceOperations() {
        return success();
    }

    /**
     * Get the number of failed persistence operations.
     * 
     * @return failed operations count
     */
    public long getFailedPersistenceOperations() {
        return failure();
    }

    /**
     * Create statistics from a list of snapshots.
     * 
     * @param snapshots the list of snapshots
     * @param timeRange the time range for statistics
     * @return agent persistence statistics
     */
    public static AgentPersistenceStatistics fromSnapshots(List<DomainAggregatedSnapshot> snapshots,
            Duration timeRange) {
        return new AgentPersistenceStatistics(snapshots, timeRange, System.currentTimeMillis());
    }

    /**
     * Create empty statistics for the given agent and time range.
     * 
     * @param agentId the agent identifier
     * @param timeRange the time range for statistics
     * @return empty agent persistence statistics
     */
    public static AgentPersistenceStatistics empty(String agentId, Duration timeRange) {
        return new AgentPersistenceStatistics(List.of(), timeRange, System.currentTimeMillis());
    }
}
