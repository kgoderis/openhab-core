package org.openhab.core.ai.common.monitoring.service.statistics;

import java.time.Duration;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.PercentileMetrics;
import org.openhab.core.ai.common.monitoring.api.TrendMetrics;
import org.openhab.core.ai.common.monitoring.service.snapshot.HttpTransportSnapshot;

/**
 * Statistics class for transport metrics.
 * 
 * <p>
 * This class provides comprehensive transport statistics including
 * request throughput, error rates, bandwidth utilization, and performance metrics.
 * It aggregates multiple TransportSnapshot instances to provide
 * historical and statistical analysis of transport behavior.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record TransportStatistics(List<HttpTransportSnapshot> snapshots, Duration timeRange, long timestampMs)
        implements
            StatisticsSnapshot,
            CountsMetrics,
            LatencyMetrics,
            TrendMetrics,
            PercentileMetrics {

    // CountsMetrics implementation (aggregated across all snapshots)
    @Override
    public long total() {
        return snapshots.stream().mapToLong(HttpTransportSnapshot::totalRequests).sum();
    }

    @Override
    public long success() {
        return total() - failure(); // Total requests minus errors
    }

    @Override
    public long failure() {
        return snapshots.stream().mapToLong(HttpTransportSnapshot::totalErrors).sum();
    }

    // LatencyMetrics implementation
    @Override
    public long totalDurationNanos() {
        return snapshots.stream().mapToLong(HttpTransportSnapshot::totalDurationNanos).sum();
    }

    // TrendMetrics implementation
    @Override
    public double trendPercentage() {
        if (snapshots.size() < 2) {
            return 0.0;
        }
        // Calculate throughput trend from first to last snapshot
        double firstThroughput = snapshots.get(0).requestsPerSecond();
        double lastThroughput = snapshots.get(snapshots.size() - 1).requestsPerSecond();
        return firstThroughput > 0 ? ((lastThroughput - firstThroughput) / firstThroughput) * 100.0 : 0.0;
    }

    @Override
    public String trendDirection() {
        double percentage = trendPercentage();
        if (percentage > 5.0) {
            return "increasing";
        } else if (percentage < -5.0) {
            return "decreasing";
        } else {
            return "stable";
        }
    }

    @Override
    public double changeRate() {
        if (snapshots.size() < 2) {
            return 0.0;
        }
        // Calculate change rate in requests per second
        long totalRequests = total();
        long timeSpanMs = snapshots.get(snapshots.size() - 1).timestampMs() - snapshots.get(0).timestampMs();
        return timeSpanMs > 0 ? (double) totalRequests / (timeSpanMs / 1000.0) : 0.0;
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

    private double calculatePercentile(double percentile) {
        if (snapshots.isEmpty()) {
            return 0.0;
        }

        List<Double> errorRates = snapshots.stream().mapToDouble(HttpTransportSnapshot::errorRate).sorted().boxed()
                .toList();

        if (errorRates.isEmpty()) {
            return 0.0;
        }

        int index = (int) Math.ceil(percentile * errorRates.size()) - 1;
        index = Math.max(0, Math.min(index, errorRates.size() - 1));
        return errorRates.get(index);
    }

    /**
     * Get total bytes transferred across all snapshots.
     * 
     * @return total bytes transferred
     */
    public long totalBytesTransferred() {
        return snapshots.stream().mapToLong(HttpTransportSnapshot::totalBytesTransferred).sum();
    }

    /**
     * Get average requests per second across all snapshots.
     * 
     * @return average requests per second
     */
    public double averageRequestsPerSecond() {
        return snapshots.stream().mapToDouble(HttpTransportSnapshot::requestsPerSecond).average().orElse(0.0);
    }

    /**
     * Get maximum requests per second across all snapshots.
     * 
     * @return maximum requests per second
     */
    public double maxRequestsPerSecond() {
        return snapshots.stream().mapToDouble(HttpTransportSnapshot::requestsPerSecond).max().orElse(0.0);
    }

    /**
     * Get average error rate across all snapshots.
     * 
     * @return average error rate
     */
    public double averageErrorRate() {
        return snapshots.stream().mapToDouble(HttpTransportSnapshot::errorRate).average().orElse(0.0);
    }

    /**
     * Get average bandwidth utilization across all snapshots.
     * 
     * @return average bandwidth in bytes per second
     */
    public double averageBandwidth() {
        return snapshots.stream().mapToDouble(HttpTransportSnapshot::bandwidthBytesPerSecond).average().orElse(0.0);
    }

    /**
     * Get provider information from the most recent snapshot.
     * 
     * @return provider ID or "unknown" if no snapshots
     */
    public String providerId() {
        return snapshots.isEmpty() ? "unknown" : snapshots.get(snapshots.size() - 1).providerId();
    }

    /**
     * Get provider name from the most recent snapshot.
     * 
     * @return provider name or "unknown" if no snapshots
     */
    public String providerName() {
        return snapshots.isEmpty() ? "unknown" : snapshots.get(snapshots.size() - 1).providerName();
    }

    /**
     * Check if transport is currently running based on the most recent snapshot.
     * 
     * @return true if running, false otherwise
     */
    public boolean isRunning() {
        return !snapshots.isEmpty() && snapshots.get(snapshots.size() - 1).running();
    }

    /**
     * Get uptime from the most recent snapshot.
     * 
     * @return uptime in milliseconds
     */
    public long uptime() {
        return snapshots.isEmpty() ? 0L : snapshots.get(snapshots.size() - 1).uptime();
    }

    /**
     * Check if transport performance is considered healthy.
     * 
     * @return true if error rate is below 5% and requests per second is positive
     */
    public boolean isHealthy() {
        return averageErrorRate() < 0.05 && averageRequestsPerSecond() > 0.0;
    }
}
