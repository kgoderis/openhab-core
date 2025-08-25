package org.openhab.core.ai.stub;

import java.time.Duration;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.api.TrendMetrics;
import org.openhab.core.ai.common.monitoring.service.statistics.StatisticsSnapshot;
import org.osgi.service.component.annotations.Reference;

/**
 * Statistics class for the AI stub framework.
 * 
 * This class provides computed statistics and insights about stub framework usage
 * derived from metrics data over time ranges. It implements capability interfaces
 * for clean, type-safe statistics access.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record StubStatistics(boolean enabled, int registeredServices, long httpRequestCount,
        long webSocketConnectionCount, long mqttMessageCount, Duration timeRange,
        long timestampMs) implements StatisticsSnapshot, CountsMetrics, LatencyMetrics, TrendMetrics {

    @Reference
    private static @Nullable MetricsService metricsService;

    // CountsMetrics implementation
    @Override
    public long total() {
        return httpRequestCount + webSocketConnectionCount + mqttMessageCount;
    }

    @Override
    public long success() {
        // For stub statistics, we consider all operations as successful
        return total();
    }

    @Override
    public long failure() {
        // Stub framework doesn't track failures separately
        return 0;
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
        // Stub framework doesn't track duration
        return 0;
    }

    // TrendMetrics implementation
    @Override
    public double trendPercentage() {
        // Simplified implementation - would need historical data for proper trend calculation
        return 0.0;
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

    // Factory method for creating from current state
    public static StubStatistics fromCurrentState(boolean enabled, int registeredServices, long httpRequestCount,
            long webSocketConnectionCount, long mqttMessageCount) {
        return new StubStatistics(enabled, registeredServices, httpRequestCount, webSocketConnectionCount,
                mqttMessageCount, Duration.ofDays(1), System.currentTimeMillis());
    }

    /**
     * Create StubStatistics from a list of snapshots.
     * 
     * @param snapshots the list of stub snapshots
     * @param timeRange the time range for the statistics
     * @return stub statistics
     */
    public static StubStatistics fromSnapshots(List<StubSnapshot> snapshots, Duration timeRange) {
        // Aggregate data from snapshots
        boolean enabled = snapshots.stream().anyMatch(s -> s.enabled());
        int registeredServices = (int) snapshots.stream().mapToLong(s -> s.registeredServices()).max().orElse(0);
        long httpRequestCount = snapshots.stream().mapToLong(s -> s.httpRequestCount()).sum();
        long webSocketConnectionCount = snapshots.stream().mapToLong(s -> s.webSocketConnectionCount()).sum();
        long mqttMessageCount = snapshots.stream().mapToLong(s -> s.mqttMessageCount()).sum();

        return new StubStatistics(enabled, registeredServices, httpRequestCount, webSocketConnectionCount,
                mqttMessageCount, timeRange, System.currentTimeMillis());
    }

    /**
     * Record stub statistics using MetricsService.
     */
    public static void recordStubStatistics(boolean success, long durationMs) {
        MetricsService metrics = metricsService;
        if (metrics != null) {
            metrics.recordOperation("stub", "statistics", success, java.time.Duration.ofMillis(durationMs));
        }
    }
}
