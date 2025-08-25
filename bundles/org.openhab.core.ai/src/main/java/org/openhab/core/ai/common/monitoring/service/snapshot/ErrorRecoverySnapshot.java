package org.openhab.core.ai.common.monitoring.service.snapshot;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.Counts;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.MetricsSnapshot;
import org.openhab.core.ai.common.monitoring.api.Timing;

/**
 * Snapshot for error recovery operations.
 * 
 * <p>
 * This snapshot provides current metrics state for error recovery operations
 * including error counts, recovery success rates, and timing information.
 * It implements capability interfaces for clean, type-safe metrics access.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record ErrorRecoverySnapshot(Counts counts, Timing timing, long timestampMs, long totalErrors,
        long totalRecoveries, long totalFallbacks, long totalFailures, long recoveryAttempts,
        Map<String, Long> errorCountsByType,
        Map<String, Long> recoveryCountsByStrategy) implements MetricsSnapshot, CountsMetrics, LatencyMetrics {

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

    @Override
    public double averageMs(long total) {
        if (total == 0)
            return 0.0;
        return totalDurationNanos() / (total * 1_000_000.0);
    }

    // Error recovery specific methods
    /**
     * Get recovery success rate as a percentage.
     * 
     * @return recovery success rate (0.0 to 100.0)
     */
    public double getRecoverySuccessRate() {
        return totalErrors > 0 ? (double) totalRecoveries / totalErrors * 100.0 : 0.0;
    }

    /**
     * Get fallback rate as a percentage.
     * 
     * @return fallback rate (0.0 to 100.0)
     */
    public double getFallbackRate() {
        return totalErrors > 0 ? (double) totalFallbacks / totalErrors * 100.0 : 0.0;
    }

    /**
     * Get failure rate as a percentage.
     * 
     * @return failure rate (0.0 to 100.0)
     */
    public double getFailureRate() {
        return totalErrors > 0 ? (double) totalFailures / totalErrors * 100.0 : 0.0;
    }

    /**
     * Get average recovery attempts per error.
     * 
     * @return average recovery attempts
     */
    public double getAverageRecoveryAttempts() {
        return totalErrors > 0 ? (double) recoveryAttempts / totalErrors : 0.0;
    }

    /**
     * Get the most common error type.
     * 
     * @return the most common error type, or null if no errors
     */
    public String getMostCommonErrorType() {
        if (errorCountsByType.isEmpty())
            return null;
        return errorCountsByType.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey)
                .orElse(null);
    }

    /**
     * Get the most successful recovery strategy.
     * 
     * @return the most successful recovery strategy, or null if no recoveries
     */
    public String getMostSuccessfulRecoveryStrategy() {
        if (recoveryCountsByStrategy.isEmpty())
            return null;
        return recoveryCountsByStrategy.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey)
                .orElse(null);
    }

    /**
     * Create empty error recovery snapshot.
     * 
     * @return empty error recovery snapshot
     */
    public static ErrorRecoverySnapshot empty() {
        return new ErrorRecoverySnapshot(new Counts(0, 0, 0), new Timing(0), System.currentTimeMillis(), 0L, 0L, 0L, 0L,
                0L, Map.of(), Map.of());
    }
}
