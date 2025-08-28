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
 * Immutable statistics for sampling model metrics analysis.
 * 
 * This class provides trend analysis, percentile calculations, and sampling
 * metrics for sampling model operations over a specified time range.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public record SamplingModelStatistics(List<SamplingModelSnapshot> snapshots, Duration timeRange,
        long timestampMs) implements TrendMetrics, PercentileMetrics, SamplingMetrics {

    public SamplingModelStatistics {
        Objects.requireNonNull(snapshots, "snapshots");
        Objects.requireNonNull(timeRange, "timeRange");
    }

    // Helper method for percentile calculations
    private double percentile(double p) {
        if (snapshots.isEmpty()) {
            return 0.0;
        }

        // Calculate percentile of cache hit rates
        double[] values = snapshots.stream().mapToDouble(SamplingModelSnapshot::cacheHitRate).sorted().toArray();

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

        // Calculate percentage change from first to last snapshot
        double firstValue = snapshots.get(0).total();
        double lastValue = snapshots.get(snapshots.size() - 1).total();

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

        double firstValue = snapshots.get(0).total();
        double lastValue = snapshots.get(snapshots.size() - 1).total();
        double timeRangeSeconds = timeRange.toSeconds();

        return timeRangeSeconds > 0 ? (lastValue - firstValue) / timeRangeSeconds : 0.0;
    }

    // SamplingMetrics implementation - provide aggregate values across snapshots
    @Override
    public long totalSamplesGenerated() {
        return snapshots.stream().mapToLong(SamplingModelSnapshot::totalSamplesGenerated).sum();
    }

    @Override
    public long cacheHits() {
        return snapshots.stream().mapToLong(SamplingModelSnapshot::cacheHits).sum();
    }

    @Override
    public long cacheMisses() {
        return snapshots.stream().mapToLong(SamplingModelSnapshot::cacheMisses).sum();
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
        return snapshots.stream().mapToLong(SamplingModelSnapshot::total).sum();
    }

    /**
     * Calculate the average cache efficiency over the time range.
     * 
     * @return average cache efficiency as percentage (0.0 to 100.0)
     */
    public double averageCacheEfficiency() {
        return snapshots.stream().mapToDouble(SamplingModelSnapshot::cacheEfficiency).average().orElse(0.0);
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
     * Calculate the average samples generated per second.
     * 
     * @return samples per second, or 0.0 if no time range
     */
    public double samplesPerSecond() {
        if (timeRange.isZero()) {
            return 0.0;
        }

        return (double) totalSamplesGenerated() / timeRange.toSeconds();
    }

    /**
     * Calculate the peak cache efficiency in the time range.
     * 
     * @return peak cache efficiency as percentage (0.0 to 100.0)
     */
    public double peakCacheEfficiency() {
        return snapshots.stream().mapToDouble(SamplingModelSnapshot::cacheEfficiency).max().orElse(0.0);
    }

    /**
     * Calculate the minimum cache efficiency in the time range.
     * 
     * @return minimum cache efficiency as percentage (0.0 to 100.0)
     */
    public double minimumCacheEfficiency() {
        return snapshots.stream().mapToDouble(SamplingModelSnapshot::cacheEfficiency).min().orElse(0.0);
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
