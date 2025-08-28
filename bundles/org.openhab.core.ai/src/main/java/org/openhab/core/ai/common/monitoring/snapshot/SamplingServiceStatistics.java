/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.core.ai.common.monitoring.snapshot;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.PercentileMetrics;
import org.openhab.core.ai.common.monitoring.api.SamplingMetrics;
import org.openhab.core.ai.common.monitoring.api.TrendMetrics;

/**
 * Immutable statistics for sampling service metrics analysis.
 * 
 * This class provides trend analysis, percentile calculations, and sampling
 * metrics for human-in-the-loop sampling operations over a specified time range.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public record SamplingServiceStatistics(List<SamplingServiceSnapshot> snapshots, Duration timeRange,
        long timestampMs) implements TrendMetrics, PercentileMetrics, SamplingMetrics {

    public SamplingServiceStatistics {
        Objects.requireNonNull(snapshots, "snapshots");
        Objects.requireNonNull(timeRange, "timeRange");
    }

    // Helper method for percentile calculations
    private double percentile(double p) {
        if (snapshots.isEmpty()) {
            return 0.0;
        }

        // Calculate percentile of approval rates
        double[] values = snapshots.stream().mapToDouble(SamplingServiceSnapshot::approvalRate).sorted().toArray();

        return calculatePercentile(values, p);
    }

    @Override
    public double percentile50() {
        return percentile(50.0);
    }

    @Override
    public double percentile90() {
        return percentile(90.0);
    }

    @Override
    public double percentile95() {
        return percentile(95.0);
    }

    @Override
    public double percentile99() {
        return percentile(99.0);
    }

    @Override
    public double trendPercentage() {
        if (snapshots.size() < 2) {
            return 0.0;
        }

        // Calculate percentage change from first to last snapshot for total requests
        double firstValue = snapshots.get(0).totalRequests();
        double lastValue = snapshots.get(snapshots.size() - 1).totalRequests();

        if (firstValue == 0) {
            return lastValue > 0 ? 100.0 : 0.0;
        }

        return ((lastValue - firstValue) / firstValue) * 100.0;
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
        if (snapshots.size() < 2 || timeRange.isZero()) {
            return 0.0;
        }

        double firstValue = snapshots.get(0).totalRequests();
        double lastValue = snapshots.get(snapshots.size() - 1).totalRequests();
        double timeRangeSeconds = timeRange.toSeconds();

        return timeRangeSeconds > 0 ? (lastValue - firstValue) / timeRangeSeconds : 0.0;
    }

    // SamplingMetrics implementation - provide aggregate values across snapshots
    @Override
    public long totalSamplesGenerated() {
        return snapshots.stream().mapToLong(SamplingServiceSnapshot::totalSamplesGenerated).sum();
    }

    @Override
    public long cacheHits() {
        return snapshots.stream().mapToLong(SamplingServiceSnapshot::cacheHits).sum();
    }

    @Override
    public long cacheMisses() {
        return snapshots.stream().mapToLong(SamplingServiceSnapshot::cacheMisses).sum();
    }

    @Override
    public int cacheSize() {
        if (snapshots.isEmpty()) {
            return 0;
        }
        // Return the most recent cache size
        return snapshots.get(snapshots.size() - 1).cacheSize();
    }

    @Override
    public String modelType() {
        if (snapshots.isEmpty()) {
            return "";
        }
        // Return the most recent model type
        return snapshots.get(snapshots.size() - 1).modelType();
    }

    @Override
    public String modelVersion() {
        if (snapshots.isEmpty()) {
            return "";
        }
        // Return the most recent model version
        return snapshots.get(snapshots.size() - 1).modelVersion();
    }

    @Override
    public long total() {
        return snapshots.stream().mapToLong(SamplingServiceSnapshot::total).sum();
    }

    // Sampling service specific aggregated metrics

    /**
     * Get the total number of approved requests across all snapshots.
     * 
     * @return total approved requests
     */
    public long totalApprovedRequests() {
        return snapshots.stream().mapToLong(SamplingServiceSnapshot::approvedRequests).sum();
    }

    /**
     * Get the total number of rejected requests across all snapshots.
     * 
     * @return total rejected requests
     */
    public long totalRejectedRequests() {
        return snapshots.stream().mapToLong(SamplingServiceSnapshot::rejectedRequests).sum();
    }

    /**
     * Get the current number of pending requests.
     * 
     * @return current pending requests
     */
    public long currentPendingRequests() {
        if (snapshots.isEmpty()) {
            return 0;
        }
        // Return the most recent pending count
        return snapshots.get(snapshots.size() - 1).pendingRequests();
    }

    /**
     * Get the total number of requests across all snapshots.
     * 
     * @return total requests
     */
    public long totalRequests() {
        return totalApprovedRequests() + totalRejectedRequests() + currentPendingRequests();
    }

    /**
     * Calculate the overall approval rate across the time period.
     * 
     * @return overall approval rate between 0.0 and 1.0
     */
    public double overallApprovalRate() {
        long totalProcessed = totalApprovedRequests() + totalRejectedRequests();
        return totalProcessed > 0 ? (double) totalApprovedRequests() / totalProcessed : 0.0;
    }

    /**
     * Calculate the overall approval rate as a percentage.
     * 
     * @return overall approval rate percentage between 0.0 and 100.0
     */
    public double overallApprovalRatePercentage() {
        return overallApprovalRate() * 100.0;
    }

    /**
     * Calculate the average approval rate over the time range.
     * 
     * @return average approval rate as percentage (0.0 to 100.0)
     */
    public double averageApprovalRatePercentage() {
        return snapshots.stream().mapToDouble(SamplingServiceSnapshot::approvalRatePercentage).average().orElse(0.0);
    }

    /**
     * Calculate the average cache efficiency over the time range.
     * 
     * @return average cache efficiency as percentage (0.0 to 100.0)
     */
    public double averageCacheEfficiency() {
        return snapshots.stream().mapToDouble(SamplingServiceSnapshot::cacheEfficiency).average().orElse(0.0);
    }

    /**
     * Calculate the trend in approval rate.
     * 
     * @return trend percentage for approval rate
     */
    public double approvalRateTrend() {
        if (snapshots.size() < 2) {
            return 0.0;
        }

        double firstRate = snapshots.get(0).approvalRatePercentage();
        double lastRate = snapshots.get(snapshots.size() - 1).approvalRatePercentage();

        if (firstRate == 0) {
            return lastRate > 0 ? 100.0 : 0.0;
        }

        return ((lastRate - firstRate) / firstRate) * 100.0;
    }

    /**
     * Calculate the trend in cache hit rate.
     * 
     * @return trend percentage for cache hit rate
     */
    public double cacheHitRateTrend() {
        if (snapshots.size() < 2) {
            return 0.0;
        }

        double firstRate = snapshots.get(0).cacheHitRate();
        double lastRate = snapshots.get(snapshots.size() - 1).cacheHitRate();

        if (firstRate == 0) {
            return lastRate > 0 ? 100.0 : 0.0;
        }

        return ((lastRate - firstRate) / firstRate) * 100.0;
    }

    /**
     * Calculate the average requests processed per second.
     * 
     * @return requests per second, or 0.0 if no time range
     */
    public double requestsPerSecond() {
        if (timeRange.isZero()) {
            return 0.0;
        }

        long totalProcessed = totalApprovedRequests() + totalRejectedRequests();
        return (double) totalProcessed / timeRange.toSeconds();
    }

    /**
     * Calculate the average response time across all snapshots.
     * 
     * @return average response time in milliseconds
     */
    public double averageResponseTimeMs() {
        return snapshots.stream().mapToDouble(SamplingServiceSnapshot::averageMs).average().orElse(0.0);
    }

    /**
     * Calculate the peak approval rate in the time range.
     * 
     * @return peak approval rate as percentage (0.0 to 100.0)
     */
    public double peakApprovalRatePercentage() {
        return snapshots.stream().mapToDouble(SamplingServiceSnapshot::approvalRatePercentage).max().orElse(0.0);
    }

    /**
     * Calculate the minimum approval rate in the time range.
     * 
     * @return minimum approval rate as percentage (0.0 to 100.0)
     */
    public double minimumApprovalRatePercentage() {
        return snapshots.stream().mapToDouble(SamplingServiceSnapshot::approvalRatePercentage).min().orElse(0.0);
    }

    /**
     * Calculate the peak efficiency score in the time range.
     * 
     * @return peak efficiency score (0.0 to 1.0)
     */
    public double peakEfficiencyScore() {
        return snapshots.stream().mapToDouble(SamplingServiceSnapshot::efficiencyScore).max().orElse(0.0);
    }

    /**
     * Calculate the average efficiency score over the time range.
     * 
     * @return average efficiency score (0.0 to 1.0)
     */
    public double averageEfficiencyScore() {
        return snapshots.stream().mapToDouble(SamplingServiceSnapshot::efficiencyScore).average().orElse(0.0);
    }

    /**
     * Check if the sampling service performance is improving over time.
     * 
     * @return true if approval rate trend and efficiency trend are positive
     */
    public boolean isImproving() {
        return approvalRateTrend() > 0.0 && averageEfficiencyScore() > 0.7;
    }

    /**
     * Get performance summary for the time period.
     * 
     * @return performance summary string
     */
    public String getPerformanceSummary() {
        return String.format(
                "SamplingService[%d total requests, %.1f%% approval rate, %.1fms avg response, %.1f%% efficiency, trend: %s]",
                totalRequests(), overallApprovalRatePercentage(), averageResponseTimeMs(),
                averageEfficiencyScore() * 100.0, trendDirection());
    }

    // Helper methods for statistical calculations
    private double calculatePercentile(double[] values, double p) {
        if (values.length == 0)
            return 0.0;

        int n = values.length;
        double index = (p / 100.0) * (n - 1);
        int lowerIndex = (int) Math.floor(index);
        int upperIndex = Math.min(lowerIndex + 1, n - 1);

        if (lowerIndex == upperIndex) {
            return values[lowerIndex];
        }

        double weight = index - lowerIndex;
        return values[lowerIndex] * (1 - weight) + values[upperIndex] * weight;
    }
}
