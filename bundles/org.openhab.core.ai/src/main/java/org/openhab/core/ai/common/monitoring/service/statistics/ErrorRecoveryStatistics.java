package org.openhab.core.ai.common.monitoring.service.statistics;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.PercentileMetrics;
import org.openhab.core.ai.common.monitoring.api.TrendMetrics;
import org.openhab.core.ai.common.monitoring.service.snapshot.ErrorRecoverySnapshot;

/**
 * Statistics class for error recovery operations.
 * 
 * <p>
 * This class provides computed statistics and insights about error recovery
 * operations derived from metrics data over time ranges. It implements
 * capability interfaces for clean, type-safe statistics access.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record ErrorRecoveryStatistics(List<ErrorRecoverySnapshot> snapshots, Duration timeRange,
        long timestampMs) implements StatisticsSnapshot, CountsMetrics, TrendMetrics, PercentileMetrics {

    // CountsMetrics implementation
    @Override
    public long total() {
        return snapshots.stream().mapToLong(s -> s.total()).sum();
    }

    @Override
    public long success() {
        return snapshots.stream().mapToLong(s -> s.success()).sum();
    }

    @Override
    public long failure() {
        return snapshots.stream().mapToLong(s -> s.failure()).sum();
    }

    @Override
    public double successRate() {
        if (total() == 0)
            return 0.0;
        return (success() * 100.0) / total();
    }

    // TrendMetrics implementation
    @Override
    public double trendPercentage() {
        if (snapshots.size() < 2)
            return 0.0;

        // Calculate trend based on recovery success rate over time
        double firstHalf = calculateAverageSuccessRate(0, snapshots.size() / 2);
        double secondHalf = calculateAverageSuccessRate(snapshots.size() / 2, snapshots.size());

        if (firstHalf == 0)
            return 0.0;
        return ((secondHalf - firstHalf) / firstHalf) * 100.0;
    }

    @Override
    public String trendDirection() {
        double trend = trendPercentage();
        if (trend > 1.0)
            return "increasing";
        if (trend < -1.0)
            return "decreasing";
        return "stable";
    }

    @Override
    public double changeRate() {
        return trendPercentage() / timeRange.toDays();
    }

    // PercentileMetrics implementation
    @Override
    public double percentile50() {
        return calculatePercentile(0.5);
    }

    @Override
    public double percentile90() {
        return calculatePercentile(0.9);
    }

    @Override
    public double percentile95() {
        return calculatePercentile(0.95);
    }

    @Override
    public double percentile99() {
        return calculatePercentile(0.99);
    }

    // Additional error recovery specific methods
    /**
     * Get recovery success rate as a percentage.
     * 
     * @return recovery success rate (0.0 to 100.0)
     */
    public double getRecoverySuccessRate() {
        long totalAttempts = snapshots.stream().mapToLong(s -> s.total()).sum();
        long totalRecoveries = snapshots.stream().mapToLong(s -> s.success()).sum();
        return totalAttempts > 0 ? (double) totalRecoveries / totalAttempts * 100.0 : 0.0;
    }

    /**
     * Get total recovery attempts.
     * 
     * @return total recovery attempts
     */
    public long getTotalRecoveryAttempts() {
        return total();
    }

    /**
     * Get successful recoveries.
     * 
     * @return successful recoveries
     */
    public long getSuccessfulRecoveries() {
        return success();
    }

    /**
     * Get failed recoveries.
     * 
     * @return failed recoveries
     */
    public long getFailedRecoveries() {
        return failure();
    }

    /**
     * Get success rate as a percentage.
     * 
     * @return success rate (0.0 to 100.0)
     */
    public double getSuccessRate() {
        return successRate();
    }

    /**
     * Get fallback rate as a percentage.
     * 
     * @return fallback rate (0.0 to 100.0)
     */
    public double getFallbackRate() {
        // For now, return 0 since fallback tracking is not implemented in the current snapshot
        return 0.0;
    }

    /**
     * Get failure rate as a percentage.
     * 
     * @return failure rate (0.0 to 100.0)
     */
    public double getFailureRate() {
        long totalAttempts = snapshots.stream().mapToLong(s -> s.total()).sum();
        long totalFailures = snapshots.stream().mapToLong(s -> s.failure()).sum();
        return totalAttempts > 0 ? (double) totalFailures / totalAttempts * 100.0 : 0.0;
    }

    /**
     * Get average recovery attempts per error.
     * 
     * @return average recovery attempts
     */
    public double getAverageRecoveryAttempts() {
        long totalAttempts = snapshots.stream().mapToLong(s -> s.total()).sum();
        return totalAttempts > 0 ? (double) totalAttempts / totalAttempts : 0.0;
    }

    /**
     * Get the most common error type.
     * 
     * @return the most common error type, or null if no errors
     */
    public String getMostCommonErrorType() {
        Map<String, Long> allErrorCounts = new HashMap<>();
        for (ErrorRecoverySnapshot snapshot : snapshots) {
            for (Map.Entry<String, Long> entry : snapshot.errorCountsByType().entrySet()) {
                allErrorCounts.merge(entry.getKey(), entry.getValue(), Long::sum);
            }
        }

        if (allErrorCounts.isEmpty())
            return null;
        return allErrorCounts.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(null);
    }

    /**
     * Get the most successful recovery strategy.
     * 
     * @return the most successful recovery strategy, or null if no recoveries
     */
    public String getMostSuccessfulRecoveryStrategy() {
        Map<String, Long> allRecoveryCounts = new HashMap<>();
        for (ErrorRecoverySnapshot snapshot : snapshots) {
            for (Map.Entry<String, Long> entry : snapshot.recoveryCountsByStrategy().entrySet()) {
                allRecoveryCounts.merge(entry.getKey(), entry.getValue(), Long::sum);
            }
        }

        if (allRecoveryCounts.isEmpty())
            return null;
        return allRecoveryCounts.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey)
                .orElse(null);
    }

    /**
     * Create statistics from snapshots.
     * 
     * @param snapshots list of error recovery snapshots
     * @param timeRange time range for statistics
     * @return error recovery statistics
     */
    public static ErrorRecoveryStatistics fromSnapshots(List<ErrorRecoverySnapshot> snapshots, Duration timeRange) {
        return new ErrorRecoveryStatistics(snapshots, timeRange, System.currentTimeMillis());
    }

    /**
     * Create empty statistics.
     * 
     * @param timeRange time range for statistics
     * @return empty error recovery statistics
     */
    public static ErrorRecoveryStatistics empty(Duration timeRange) {
        return new ErrorRecoveryStatistics(List.of(), timeRange, System.currentTimeMillis());
    }

    // Helper methods
    private double calculateAverageSuccessRate(int start, int end) {
        return snapshots.subList(start, end).stream().mapToDouble(s -> s.successRate()).average().orElse(0.0);
    }

    private double calculatePercentile(double percentile) {
        List<Double> successRates = snapshots.stream().map(s -> s.successRate()).sorted().collect(Collectors.toList());

        if (successRates.isEmpty())
            return 0.0;

        int index = (int) Math.ceil(percentile * successRates.size()) - 1;
        return successRates.get(Math.max(0, index));
    }
}
