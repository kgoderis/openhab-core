package org.openhab.core.ai.action;

import java.time.Duration;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.PercentileMetrics;
import org.openhab.core.ai.common.monitoring.api.TrendMetrics;
import org.openhab.core.ai.common.monitoring.service.statistics.StatisticsSnapshot;

/**
 * Context performance metrics for dynamic context building operations.
 * 
 * Tracks performance metrics for context building, optimization, caching,
 * and validation operations.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record ContextPerformanceMetrics(List<ContextPerformanceSnapshot> snapshots, Duration timeRange,
        long timestampMs) implements StatisticsSnapshot, TrendMetrics, PercentileMetrics {

    /**
     * Get total context building operations
     */
    public long totalContextBuilds() {
        return snapshots.stream().mapToLong(s -> s.totalContextBuilds()).sum();
    }

    /**
     * Get successful context building operations
     */
    public long successfulContextBuilds() {
        return snapshots.stream().mapToLong(s -> s.successfulContextBuilds()).sum();
    }

    /**
     * Get failed context building operations
     */
    public long failedContextBuilds() {
        return snapshots.stream().mapToLong(s -> s.failedContextBuilds()).sum();
    }

    /**
     * Get context building success rate
     */
    public double contextBuildSuccessRate() {
        long total = totalContextBuilds();
        if (total == 0)
            return 0.0;
        return (successfulContextBuilds() * 100.0) / total;
    }

    /**
     * Get total context optimization operations
     */
    public long totalOptimizations() {
        return snapshots.stream().mapToLong(s -> s.totalOptimizations()).sum();
    }

    /**
     * Get successful context optimization operations
     */
    public long successfulOptimizations() {
        return snapshots.stream().mapToLong(s -> s.successfulOptimizations()).sum();
    }

    /**
     * Get context optimization success rate
     */
    public double optimizationSuccessRate() {
        long total = totalOptimizations();
        if (total == 0)
            return 0.0;
        return (successfulOptimizations() * 100.0) / total;
    }

    /**
     * Get total cache operations
     */
    public long totalCacheOperations() {
        return snapshots.stream().mapToLong(s -> s.totalCacheOperations()).sum();
    }

    /**
     * Get cache hit rate
     */
    public double cacheHitRate() {
        return snapshots.stream().mapToDouble(s -> s.cacheHitRate()).average().orElse(0.0);
    }

    /**
     * Get average context building time in milliseconds
     */
    public double averageContextBuildTimeMs() {
        long totalDuration = snapshots.stream().mapToLong(s -> s.totalContextBuildDurationNanos()).sum();
        long totalBuilds = totalContextBuilds();
        if (totalBuilds == 0)
            return 0.0;
        return totalDuration / (totalBuilds * 1_000_000.0);
    }

    /**
     * Get average context optimization time in milliseconds
     */
    public double averageOptimizationTimeMs() {
        long totalDuration = snapshots.stream().mapToLong(s -> s.totalOptimizationDurationNanos()).sum();
        long totalOpts = totalOptimizations();
        if (totalOpts == 0)
            return 0.0;
        return totalDuration / (totalOpts * 1_000_000.0);
    }

    /**
     * Get context building operations per second
     */
    public double contextBuildsPerSecond() {
        if (timeRange.toNanos() == 0)
            return 0.0;
        return totalContextBuilds() / (timeRange.toNanos() / 1_000_000_000.0);
    }

    /**
     * Get optimization operations per second
     */
    public double optimizationsPerSecond() {
        if (timeRange.toNanos() == 0)
            return 0.0;
        return totalOptimizations() / (timeRange.toNanos() / 1_000_000_000.0);
    }

    /**
     * Get 95th percentile context building time in milliseconds
     */
    public double contextBuildTime95thPercentileMs() {
        return snapshots.stream().mapToDouble(s -> s.contextBuildTime95thPercentileMs()).average().orElse(0.0);
    }

    /**
     * Get 99th percentile context building time in milliseconds
     */
    public double contextBuildTime99thPercentileMs() {
        return snapshots.stream().mapToDouble(s -> s.contextBuildTime99thPercentileMs()).average().orElse(0.0);
    }

    /**
     * Get trend direction for context building success rate
     */
    public TrendDirection contextBuildSuccessRateTrend() {
        if (snapshots.size() < 2)
            return TrendDirection.STABLE;

        double firstHalf = snapshots.subList(0, snapshots.size() / 2).stream()
                .mapToDouble(s -> s.contextBuildSuccessRate()).average().orElse(0.0);
        double secondHalf = snapshots.subList(snapshots.size() / 2, snapshots.size()).stream()
                .mapToDouble(s -> s.contextBuildSuccessRate()).average().orElse(0.0);

        if (secondHalf > firstHalf + 5.0)
            return TrendDirection.IMPROVING;
        if (secondHalf < firstHalf - 5.0)
            return TrendDirection.DEGRADING;
        return TrendDirection.STABLE;
    }

    /**
     * Get trend direction for context building performance
     */
    public TrendDirection contextBuildPerformanceTrend() {
        if (snapshots.size() < 2)
            return TrendDirection.STABLE;

        double firstHalf = snapshots.subList(0, snapshots.size() / 2).stream()
                .mapToDouble(s -> s.averageContextBuildTimeMs()).average().orElse(0.0);
        double secondHalf = snapshots.subList(snapshots.size() / 2, snapshots.size()).stream()
                .mapToDouble(s -> s.averageContextBuildTimeMs()).average().orElse(0.0);

        if (secondHalf < firstHalf - 10.0)
            return TrendDirection.IMPROVING;
        if (secondHalf > firstHalf + 10.0)
            return TrendDirection.DEGRADING;
        return TrendDirection.STABLE;
    }

    // TrendMetrics interface implementations
    @Override
    public double trendPercentage() {
        if (snapshots.size() < 2)
            return 0.0;

        double firstHalf = snapshots.subList(0, snapshots.size() / 2).stream()
                .mapToDouble(s -> s.contextBuildSuccessRate()).average().orElse(0.0);
        double secondHalf = snapshots.subList(snapshots.size() / 2, snapshots.size()).stream()
                .mapToDouble(s -> s.contextBuildSuccessRate()).average().orElse(0.0);

        if (firstHalf == 0)
            return 0.0;
        return ((secondHalf - firstHalf) / firstHalf) * 100.0;
    }

    @Override
    public String trendDirection() {
        TrendDirection direction = contextBuildSuccessRateTrend();
        return direction.name().toLowerCase();
    }

    @Override
    public double changeRate() {
        if (timeRange.toNanos() == 0)
            return 0.0;
        return trendPercentage() / (timeRange.toNanos() / 1_000_000_000.0);
    }

    // PercentileMetrics interface implementations
    @Override
    public double percentile50() {
        return snapshots.stream().mapToDouble(s -> s.averageContextBuildTimeMs()).sorted().skip(snapshots.size() / 2)
                .findFirst().orElse(0.0);
    }

    @Override
    public double percentile90() {
        int index = (int) (snapshots.size() * 0.9);
        return snapshots.stream().mapToDouble(s -> s.averageContextBuildTimeMs()).sorted().skip(index).findFirst()
                .orElse(0.0);
    }

    @Override
    public double percentile95() {
        return contextBuildTime95thPercentileMs();
    }

    @Override
    public double percentile99() {
        return contextBuildTime99thPercentileMs();
    }

    /**
     * Factory method to create from snapshots
     */
    public static ContextPerformanceMetrics fromSnapshots(List<ContextPerformanceSnapshot> snapshots,
            Duration timeRange) {
        return new ContextPerformanceMetrics(snapshots, timeRange, System.currentTimeMillis());
    }

    /**
     * Create empty metrics
     */
    public static ContextPerformanceMetrics empty(Duration timeRange) {
        return new ContextPerformanceMetrics(List.of(), timeRange, System.currentTimeMillis());
    }

    /**
     * Trend direction enum
     */
    public enum TrendDirection {
        IMPROVING,
        STABLE,
        DEGRADING
    }
}
