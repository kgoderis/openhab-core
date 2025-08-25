package org.openhab.core.ai.common.monitoring.service.snapshot;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.MetricsSnapshot;

/**
 * Domain-wide aggregated metrics snapshot.
 * 
 * <p>
 * This class provides aggregated metrics across all operations within a domain,
 * including total counts, success rates, and performance summaries.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record DomainAggregatedSnapshot(String domain, long totalOperations, long successfulOperations,
        long failedOperations, long totalDurationNanos, double averageSuccessRate,
        List<MetricsSnapshot> operationSnapshots, Instant timestamp) {

    /**
     * Calculate the overall success rate.
     * 
     * @return success rate as a percentage
     */
    public double getSuccessRate() {
        if (totalOperations == 0)
            return 0.0;
        return (successfulOperations * 100.0) / totalOperations;
    }

    /**
     * Calculate the average duration in milliseconds.
     * 
     * @return average duration in milliseconds
     */
    public double getAverageDurationMs() {
        if (totalOperations == 0)
            return 0.0;
        return (double) totalDurationNanos / totalOperations / 1_000_000.0;
    }

    /**
     * Calculate operations per second.
     * 
     * @return operations per second
     */
    public double getOperationsPerSecond() {
        if (totalDurationNanos == 0)
            return 0.0;
        double durationSeconds = totalDurationNanos / 1_000_000_000.0;
        return totalOperations / durationSeconds;
    }

    /**
     * Create an empty DomainAggregatedSnapshot for the given domain.
     */
    public static DomainAggregatedSnapshot empty(String domain) {
        return new DomainAggregatedSnapshot(domain, 0L, 0L, 0L, 0L, 0.0, new ArrayList<>(), Instant.now());
    }
}
