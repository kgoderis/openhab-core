package org.openhab.core.ai.common.monitoring.service.snapshot;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.CommunicationMetrics;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.service.statistics.StatisticsSnapshot;

/**
 * Snapshot for bandwidth metrics.
 * 
 * <p>
 * This snapshot captures bandwidth performance metrics including data transfer rates,
 * throughput measurements, and bandwidth utilization statistics. It implements multiple
 * capability interfaces to provide comprehensive bandwidth analysis.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record BandwidthSnapshot(long total, long success, long failure, long totalDurationNanos,
        long totalBytesTransferred, double averageBandwidthBytesPerSecond, double peakBandwidthBytesPerSecond,
        double bandwidthUtilizationPercent,
        long timestampMs) implements StatisticsSnapshot, CountsMetrics, LatencyMetrics, CommunicationMetrics {

    // CountsMetrics implementation
    @Override
    public long total() {
        return total;
    }

    @Override
    public long success() {
        return success;
    }

    @Override
    public long failure() {
        return failure;
    }

    @Override
    public double successRate() {
        if (total == 0) {
            return 0.0;
        }
        return (success * 100.0) / total;
    }

    // LatencyMetrics implementation
    @Override
    public long totalDurationNanos() {
        return totalDurationNanos;
    }

    @Override
    public double averageMs(long total) {
        if (total == 0) {
            return 0.0;
        }
        return totalDurationNanos / (total * 1_000_000.0);
    }

    // CommunicationMetrics implementation
    @Override
    public double communicationSuccessRate() {
        return successRate();
    }

    @Override
    public double messageThroughput() {
        if (totalDurationNanos == 0) {
            return 0.0;
        }
        double durationSeconds = totalDurationNanos / 1_000_000_000.0;
        return total / durationSeconds;
    }

    @Override
    public double communicationLatency() {
        return averageMs(total);
    }

    @Override
    public double communicationReliability() {
        return successRate();
    }

    @Override
    public double messageDeliveryRate() {
        return successRate();
    }

    @Override
    public double communicationErrorRate() {
        if (total == 0) {
            return 0.0;
        }
        return (failure * 100.0) / total;
    }

    @Override
    public long messageQueueDepth() {
        // For bandwidth metrics, we can represent pending transfers
        return Math.max(0, total - success - failure);
    }

    @Override
    public double bandwidthUtilization() {
        return bandwidthUtilizationPercent;
    }

    @Override
    public double communicationRetryRate() {
        // For bandwidth, retry rate is based on failed transfers that had to be retried
        if (total == 0) {
            return 0.0;
        }
        return (failure * 100.0) / total;
    }

    @Override
    public double communicationTimeoutRate() {
        // For bandwidth, timeout rate is similar to failure rate
        if (total == 0) {
            return 0.0;
        }
        return (failure * 100.0) / total;
    }

    // Bandwidth-specific methods
    /**
     * Get the total bytes transferred.
     * 
     * @return total bytes transferred
     */
    public long getTotalBytesTransferred() {
        return totalBytesTransferred;
    }

    /**
     * Get the average bandwidth in bytes per second.
     * 
     * @return average bandwidth
     */
    public double getAverageBandwidthBytesPerSecond() {
        return averageBandwidthBytesPerSecond;
    }

    /**
     * Get the peak bandwidth in bytes per second.
     * 
     * @return peak bandwidth
     */
    public double getPeakBandwidthBytesPerSecond() {
        return peakBandwidthBytesPerSecond;
    }

    /**
     * Get the bandwidth utilization as a percentage.
     * 
     * @return bandwidth utilization percentage
     */
    public double getBandwidthUtilizationPercent() {
        return bandwidthUtilizationPercent;
    }

    /**
     * Create an empty bandwidth snapshot.
     * 
     * @param agentId the agent identifier
     * @return empty bandwidth snapshot
     */
    public static BandwidthSnapshot empty(String agentId) {
        return new BandwidthSnapshot(0L, 0L, 0L, 0L, 0L, 0.0, 0.0, 0.0, System.currentTimeMillis());
    }

    /**
     * Create a bandwidth snapshot from measurements.
     * 
     * @param totalTransfers total number of transfers
     * @param successfulTransfers successful transfers
     * @param failedTransfers failed transfers
     * @param totalDurationNanos total duration in nanoseconds
     * @param totalBytes total bytes transferred
     * @param averageBandwidth average bandwidth in bytes per second
     * @param peakBandwidth peak bandwidth in bytes per second
     * @param utilizationPercent bandwidth utilization percentage
     * @return bandwidth snapshot
     */
    public static BandwidthSnapshot from(long totalTransfers, long successfulTransfers, long failedTransfers,
            long totalDurationNanos, long totalBytes, double averageBandwidth, double peakBandwidth,
            double utilizationPercent) {
        return new BandwidthSnapshot(totalTransfers, successfulTransfers, failedTransfers, totalDurationNanos,
                totalBytes, averageBandwidth, peakBandwidth, utilizationPercent, System.currentTimeMillis());
    }
}
