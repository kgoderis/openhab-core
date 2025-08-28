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
import org.openhab.core.ai.common.monitoring.service.snapshot.MessageLatencySnapshot;

/**
 * Statistics for message latency metrics over time.
 * 
 * <p>
 * This class provides statistical analysis of message latency performance including
 * trend analysis, percentile calculations, and communication metrics. It aggregates
 * multiple MessageLatencySnapshot instances to provide comprehensive statistical insights.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class MessageLatencyStatistics implements StatisticsSnapshot, CountsMetrics, LatencyMetrics,
        CommunicationMetrics, TrendMetrics, PercentileMetrics {

    private final List<MessageLatencySnapshot> snapshots;
    private final String agentId;
    private final long timestampMs;

    /**
     * Create message latency statistics from multiple snapshots.
     * 
     * @param snapshots list of message latency snapshots
     * @param agentId agent identifier
     */
    public MessageLatencyStatistics(List<MessageLatencySnapshot> snapshots, String agentId) {
        this.snapshots = List.copyOf(snapshots);
        this.agentId = agentId;
        this.timestampMs = System.currentTimeMillis();
    }

    /**
     * Create empty message latency statistics.
     * 
     * @param agentId agent identifier
     * @return empty statistics
     */
    public static MessageLatencyStatistics empty(String agentId) {
        return new MessageLatencyStatistics(List.of(), agentId);
    }

    // CountsMetrics implementation
    @Override
    public long total() {
        return snapshots.stream().mapToLong(MessageLatencySnapshot::total).sum();
    }

    @Override
    public long success() {
        return snapshots.stream().mapToLong(MessageLatencySnapshot::success).sum();
    }

    @Override
    public long failure() {
        return snapshots.stream().mapToLong(MessageLatencySnapshot::failure).sum();
    }

    // LatencyMetrics implementation
    @Override
    public long totalDurationNanos() {
        return snapshots.stream().mapToLong(MessageLatencySnapshot::totalDurationNanos).sum();
    }

    // CommunicationMetrics implementation
    @Override
    public double communicationSuccessRate() {
        long totalCount = total();
        return totalCount > 0 ? (double) success() / totalCount * 100.0 : 0.0;
    }

    @Override
    public double messageThroughput() {
        return snapshots.stream().mapToDouble(MessageLatencySnapshot::messageThroughput).average().orElse(0.0);
    }

    @Override
    public double communicationLatency() {
        return snapshots.stream().mapToDouble(MessageLatencySnapshot::communicationLatency).average().orElse(0.0);
    }

    @Override
    public double communicationReliability() {
        return snapshots.stream().mapToDouble(MessageLatencySnapshot::communicationReliability).average().orElse(0.0);
    }

    @Override
    public double messageDeliveryRate() {
        return communicationSuccessRate();
    }

    @Override
    public double communicationErrorRate() {
        long totalCount = total();
        return totalCount > 0 ? (double) failure() / totalCount * 100.0 : 0.0;
    }

    @Override
    public long messageQueueDepth() {
        return snapshots.stream().mapToLong(MessageLatencySnapshot::messageQueueDepth).max().orElse(0L);
    }

    @Override
    public double bandwidthUtilization() {
        return snapshots.stream().mapToDouble(MessageLatencySnapshot::bandwidthUtilization).average().orElse(0.0);
    }

    @Override
    public double communicationRetryRate() {
        return snapshots.stream().mapToDouble(MessageLatencySnapshot::communicationRetryRate).average().orElse(0.0);
    }

    @Override
    public double communicationTimeoutRate() {
        return snapshots.stream().mapToDouble(MessageLatencySnapshot::communicationTimeoutRate).average().orElse(0.0);
    }

    // TrendMetrics implementation
    @Override
    public double trendPercentage() {
        if (snapshots.size() < 2) {
            return 0.0;
        }

        // Calculate percentage change from first to last snapshot
        double firstValue = snapshots.get(0).communicationLatency();
        double lastValue = snapshots.get(snapshots.size() - 1).communicationLatency();

        if (firstValue == 0.0) {
            return lastValue > 0 ? 100.0 : 0.0;
        }

        return ((lastValue - firstValue) / firstValue) * 100.0;
    }

    @Override
    public String trendDirection() {
        double percentage = trendPercentage();
        if (Math.abs(percentage) < 1.0) {
            return "stable";
        }
        return percentage > 0 ? "increasing" : "decreasing";
    }

    @Override
    public double changeRate() {
        if (snapshots.size() < 2) {
            return 0.0;
        }

        // Calculate change rate per time unit (milliseconds)
        long timeSpan = snapshots.get(snapshots.size() - 1).timestampMs() - snapshots.get(0).timestampMs();
        if (timeSpan <= 0) {
            return 0.0;
        }

        double valueChange = snapshots.get(snapshots.size() - 1).communicationLatency()
                - snapshots.get(0).communicationLatency();
        return valueChange / timeSpan; // Change per millisecond
    }

    // PercentileMetrics implementation
    @Override
    public double percentile50() {
        return calculatePercentile(
                snapshots.stream().mapToDouble(MessageLatencySnapshot::communicationLatency).sorted().toArray(), 0.50);
    }

    public double percentile75() {
        return calculatePercentile(
                snapshots.stream().mapToDouble(MessageLatencySnapshot::communicationLatency).sorted().toArray(), 0.75);
    }

    @Override
    public double percentile90() {
        return calculatePercentile(
                snapshots.stream().mapToDouble(MessageLatencySnapshot::communicationLatency).sorted().toArray(), 0.90);
    }

    @Override
    public double percentile95() {
        return calculatePercentile(
                snapshots.stream().mapToDouble(MessageLatencySnapshot::communicationLatency).sorted().toArray(), 0.95);
    }

    @Override
    public double percentile99() {
        return calculatePercentile(
                snapshots.stream().mapToDouble(MessageLatencySnapshot::communicationLatency).sorted().toArray(), 0.99);
    }

    public double interQuartileRange() {
        return percentile75() - percentile25();
    }

    public double percentile25() {
        return calculatePercentile(
                snapshots.stream().mapToDouble(MessageLatencySnapshot::communicationLatency).sorted().toArray(), 0.25);
    }

    /**
     * Get the agent identifier.
     * 
     * @return agent ID
     */
    public String getAgentId() {
        return agentId;
    }

    /**
     * Get the timestamp when these statistics were calculated.
     * 
     * @return timestamp in milliseconds
     */
    public long getTimestampMs() {
        return timestampMs;
    }

    /**
     * Get the number of snapshots used for these statistics.
     * 
     * @return snapshot count
     */
    public int getSnapshotCount() {
        return snapshots.size();
    }

    /**
     * Get the time range covered by these statistics.
     * 
     * @return duration covered
     */
    public Duration getTimeRangeCovered() {
        if (snapshots.size() < 2) {
            return Duration.ZERO;
        }

        long minTime = snapshots.stream().mapToLong(MessageLatencySnapshot::timestampMs).min().orElse(0L);
        long maxTime = snapshots.stream().mapToLong(MessageLatencySnapshot::timestampMs).max().orElse(0L);
        return Duration.ofMillis(maxTime - minTime);
    }

    /**
     * Get peak message throughput across all snapshots.
     * 
     * @return peak throughput
     */
    public double getPeakThroughput() {
        return snapshots.stream().mapToDouble(MessageLatencySnapshot::messageThroughput).max().orElse(0.0);
    }

    /**
     * Get minimum communication latency across all snapshots.
     * 
     * @return minimum latency
     */
    public double getMinimumLatency() {
        return snapshots.stream().mapToDouble(MessageLatencySnapshot::communicationLatency).min().orElse(0.0);
    }

    /**
     * Get maximum communication latency across all snapshots.
     * 
     * @return maximum latency
     */
    public double getMaximumLatency() {
        return snapshots.stream().mapToDouble(MessageLatencySnapshot::communicationLatency).max().orElse(0.0);
    }

    /**
     * Get snapshots that are considered outliers based on latency.
     * 
     * @return list of outlier snapshots
     */
    public List<MessageLatencySnapshot> getLatencyOutliers() {
        double q1 = percentile25();
        double q3 = percentile75();
        double iqr = q3 - q1;
        double lowerBound = q1 - 1.5 * iqr;
        double upperBound = q3 + 1.5 * iqr;

        return snapshots.stream()
                .filter(s -> s.communicationLatency() < lowerBound || s.communicationLatency() > upperBound)
                .collect(Collectors.toList());
    }

    // Helper methods for statistical calculations
    private double calculateLinearRegressionSlope(double[] values) {
        int n = values.length;
        double sumX = 0, sumY = 0, sumXY = 0, sumXX = 0;

        for (int i = 0; i < n; i++) {
            sumX += i;
            sumY += values[i];
            sumXY += i * values[i];
            sumXX += i * i;
        }

        return (n * sumXY - sumX * sumY) / (n * sumXX - sumX * sumX);
    }

    private double calculateRSquared(double[] values) {
        if (values.length < 2) {
            return 0.0;
        }

        double mean = java.util.Arrays.stream(values).average().orElse(0.0);
        double totalSumSquares = java.util.Arrays.stream(values).map(v -> Math.pow(v - mean, 2)).sum();

        double slope = calculateLinearRegressionSlope(values);
        double intercept = mean - slope * (values.length - 1) / 2.0;

        double residualSumSquares = 0.0;
        for (int i = 0; i < values.length; i++) {
            double predicted = slope * i + intercept;
            residualSumSquares += Math.pow(values[i] - predicted, 2);
        }

        return totalSumSquares > 0 ? 1.0 - (residualSumSquares / totalSumSquares) : 0.0;
    }

    private double calculatePercentile(double[] sortedValues, double percentile) {
        if (sortedValues.length == 0) {
            return 0.0;
        }
        if (sortedValues.length == 1) {
            return sortedValues[0];
        }

        double index = percentile * (sortedValues.length - 1);
        int lowerIndex = (int) Math.floor(index);
        int upperIndex = (int) Math.ceil(index);

        if (lowerIndex == upperIndex) {
            return sortedValues[lowerIndex];
        }

        double weight = index - lowerIndex;
        return sortedValues[lowerIndex] * (1.0 - weight) + sortedValues[upperIndex] * weight;
    }
}
