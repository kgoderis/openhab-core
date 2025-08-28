package org.openhab.core.ai.common.monitoring.service.statistics;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.CommunicationMetrics;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.PercentileMetrics;
import org.openhab.core.ai.common.monitoring.api.TrendMetrics;
import org.openhab.core.ai.common.monitoring.service.snapshot.BandwidthSnapshot;

/**
 * Statistics for bandwidth metrics over time.
 * 
 * <p>
 * This class provides statistical analysis of bandwidth performance including
 * trend analysis, percentile calculations, and efficiency metrics. It aggregates
 * multiple BandwidthSnapshot instances to provide insights over time periods.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record BandwidthStatistics(List<BandwidthSnapshot> snapshots, Duration timeRange, long timestampMs)
        implements
            StatisticsSnapshot,
            CountsMetrics,
            LatencyMetrics,
            TrendMetrics,
            PercentileMetrics,
            CommunicationMetrics {

    // CountsMetrics implementation
    @Override
    public long total() {
        return snapshots.stream().mapToLong(BandwidthSnapshot::total).sum();
    }

    @Override
    public long success() {
        return snapshots.stream().mapToLong(BandwidthSnapshot::success).sum();
    }

    @Override
    public long failure() {
        return snapshots.stream().mapToLong(BandwidthSnapshot::failure).sum();
    }

    @Override
    public double successRate() {
        long totalCount = total();
        if (totalCount == 0) {
            return 0.0;
        }
        return (success() * 100.0) / totalCount;
    }

    // LatencyMetrics implementation
    @Override
    public long totalDurationNanos() {
        return snapshots.stream().mapToLong(BandwidthSnapshot::totalDurationNanos).sum();
    }

    @Override
    public double averageMs(long total) {
        if (total == 0 || totalDurationNanos() == 0) {
            return 0.0;
        }
        return totalDurationNanos() / (total * 1_000_000.0);
    }

    // TrendMetrics implementation
    @Override
    public double trendPercentage() {
        if (snapshots.size() < 2) {
            return 0.0;
        }

        // Calculate trend based on bandwidth utilization over time
        double firstHalf = calculateAverageBandwidthUtilization(0, snapshots.size() / 2);
        double secondHalf = calculateAverageBandwidthUtilization(snapshots.size() / 2, snapshots.size());

        if (firstHalf == 0) {
            return 0.0;
        }
        return ((secondHalf - firstHalf) / firstHalf) * 100.0;
    }

    @Override
    public String trendDirection() {
        double trend = trendPercentage();
        if (trend > 1.0) {
            return "increasing";
        }
        if (trend < -1.0) {
            return "decreasing";
        }
        return "stable";
    }

    @Override
    public double changeRate() {
        if (timeRange.toDays() == 0) {
            return 0.0;
        }
        return trendPercentage() / timeRange.toDays();
    }

    // PercentileMetrics implementation
    @Override
    public double percentile50() {
        return calculateBandwidthPercentile(0.5);
    }

    @Override
    public double percentile90() {
        return calculateBandwidthPercentile(0.9);
    }

    @Override
    public double percentile95() {
        return calculateBandwidthPercentile(0.95);
    }

    @Override
    public double percentile99() {
        return calculateBandwidthPercentile(0.99);
    }

    // CommunicationMetrics implementation
    @Override
    public double communicationSuccessRate() {
        return successRate();
    }

    @Override
    public double messageThroughput() {
        if (snapshots.isEmpty() || timeRange.toNanos() == 0) {
            return 0.0;
        }
        double durationSeconds = timeRange.toNanos() / 1_000_000_000.0;
        return total() / durationSeconds;
    }

    @Override
    public double communicationLatency() {
        return averageMs(total());
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
        long totalCount = total();
        if (totalCount == 0) {
            return 0.0;
        }
        return (failure() * 100.0) / totalCount;
    }

    @Override
    public long messageQueueDepth() {
        if (snapshots.isEmpty()) {
            return 0L;
        }
        return snapshots.stream().mapToLong(BandwidthSnapshot::messageQueueDepth).sum() / snapshots.size();
    }

    @Override
    public double bandwidthUtilization() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(BandwidthSnapshot::getBandwidthUtilizationPercent).average().orElse(0.0);
    }

    @Override
    public double communicationRetryRate() {
        long totalCount = total();
        if (totalCount == 0) {
            return 0.0;
        }
        return (failure() * 100.0) / totalCount;
    }

    @Override
    public double communicationTimeoutRate() {
        return communicationRetryRate(); // Same as retry rate for bandwidth
    }

    // Bandwidth-specific statistical methods

    /**
     * Get the average bandwidth across all snapshots.
     * 
     * @return average bandwidth in bytes per second
     */
    public double getAverageBandwidth() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(BandwidthSnapshot::getAverageBandwidthBytesPerSecond).average()
                .orElse(0.0);
    }

    /**
     * Get the peak bandwidth across all snapshots.
     * 
     * @return peak bandwidth in bytes per second
     */
    public double getPeakBandwidth() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(BandwidthSnapshot::getPeakBandwidthBytesPerSecond).max().orElse(0.0);
    }

    /**
     * Get the total bytes transferred across all snapshots.
     * 
     * @return total bytes transferred
     */
    public long getTotalBytesTransferred() {
        return snapshots.stream().mapToLong(BandwidthSnapshot::getTotalBytesTransferred).sum();
    }

    /**
     * Get the bandwidth efficiency (utilization relative to peak).
     * 
     * @return bandwidth efficiency percentage
     */
    public double getBandwidthEfficiency() {
        double peak = getPeakBandwidth();
        double average = getAverageBandwidth();
        if (peak == 0) {
            return 0.0;
        }
        return (average / peak) * 100.0;
    }

    // Helper methods

    private double calculateAverageBandwidthUtilization(int start, int end) {
        return snapshots.subList(start, end).stream().mapToDouble(BandwidthSnapshot::getBandwidthUtilizationPercent)
                .average().orElse(0.0);
    }

    private double calculateBandwidthPercentile(double percentile) {
        List<Double> bandwidths = snapshots.stream().map(BandwidthSnapshot::getAverageBandwidthBytesPerSecond).sorted()
                .collect(Collectors.toList());

        if (bandwidths.isEmpty()) {
            return 0.0;
        }

        int index = (int) Math.ceil(percentile * bandwidths.size()) - 1;
        return bandwidths.get(Math.max(0, index));
    }

    /**
     * Create bandwidth statistics from snapshots.
     * 
     * @param snapshots the bandwidth snapshots
     * @param timeRange the time range for the statistics
     * @return bandwidth statistics
     */
    public static BandwidthStatistics fromSnapshots(List<BandwidthSnapshot> snapshots, Duration timeRange) {
        return new BandwidthStatistics(List.copyOf(snapshots), timeRange, System.currentTimeMillis());
    }

    /**
     * Create empty bandwidth statistics.
     * 
     * @param timeRange the time range for the statistics
     * @return empty bandwidth statistics
     */
    public static BandwidthStatistics empty(Duration timeRange) {
        return new BandwidthStatistics(List.of(), timeRange, System.currentTimeMillis());
    }
}
