package org.openhab.core.ai.common.monitoring.service.statistics;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.BusinessMetrics;
import org.openhab.core.ai.common.monitoring.api.EfficiencyMetrics;
import org.openhab.core.ai.common.monitoring.api.PercentileMetrics;
import org.openhab.core.ai.common.monitoring.api.TrendMetrics;
import org.openhab.core.ai.common.monitoring.service.snapshot.ToolFileReadSnapshot;

/**
 * Statistics for tool file read operations with capability interfaces.
 * 
 * <p>
 * This class provides comprehensive statistics for tool file read operations:
 * - Basic counting metrics (total, success, failure)
 * - Latency metrics (duration, averages, percentiles)
 * - Business metrics (cost efficiency, resource utilization)
 * - Efficiency metrics (resource, time, energy efficiency)
 * - Trend analysis (trend percentage, direction, change rate)
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record ToolFileReadStatistics(String toolId, Duration timeRange, long totalOperations, long successfulOperations,
        long failedOperations, long totalDurationNanos, long totalFileSize, double averageFileSize,
        double averageDurationMs, double successRate, double throughputPerSecond, double resourceEfficiency,
        double timeEfficiency, double energyEfficiency, double costEfficiency, double resourceUtilization,
        double throughputEfficiency, double trendPercentage, String trendDirection, double changeRate,
        double percentile50, double percentile90, double percentile95, double percentile99,
        List<ToolFileReadSnapshot> snapshots)
        implements
            StatisticsSnapshot,
            BusinessMetrics,
            EfficiencyMetrics,
            TrendMetrics,
            PercentileMetrics {

    public ToolFileReadStatistics {
        if (toolId == null || toolId.isBlank()) {
            throw new IllegalArgumentException("toolId must not be null or blank");
        }
        if (timeRange == null || timeRange.isNegative() || timeRange.isZero()) {
            throw new IllegalArgumentException("timeRange must be positive");
        }
        if (totalOperations < 0) {
            throw new IllegalArgumentException("totalOperations must be >= 0");
        }
        if (successfulOperations < 0 || successfulOperations > totalOperations) {
            throw new IllegalArgumentException("successfulOperations must be >= 0 and <= totalOperations");
        }
        if (failedOperations < 0 || failedOperations > totalOperations) {
            throw new IllegalArgumentException("failedOperations must be >= 0 and <= totalOperations");
        }
        if (totalDurationNanos < 0) {
            throw new IllegalArgumentException("totalDurationNanos must be >= 0");
        }
        if (totalFileSize < 0) {
            throw new IllegalArgumentException("totalFileSize must be >= 0");
        }
        if (averageFileSize < 0) {
            throw new IllegalArgumentException("averageFileSize must be >= 0");
        }
        if (averageDurationMs < 0) {
            throw new IllegalArgumentException("averageDurationMs must be >= 0");
        }
        if (successRate < 0.0 || successRate > 100.0) {
            throw new IllegalArgumentException("successRate must be between 0.0 and 100.0");
        }
        if (throughputPerSecond < 0) {
            throw new IllegalArgumentException("throughputPerSecond must be >= 0");
        }
        if (resourceEfficiency < 0.0 || resourceEfficiency > 1.0) {
            throw new IllegalArgumentException("resourceEfficiency must be between 0.0 and 1.0");
        }
        if (timeEfficiency < 0.0 || timeEfficiency > 1.0) {
            throw new IllegalArgumentException("timeEfficiency must be between 0.0 and 1.0");
        }
        if (energyEfficiency < 0.0 || energyEfficiency > 1.0) {
            throw new IllegalArgumentException("energyEfficiency must be between 0.0 and 1.0");
        }
        if (costEfficiency < 0.0 || costEfficiency > 1.0) {
            throw new IllegalArgumentException("costEfficiency must be between 0.0 and 1.0");
        }
        if (resourceUtilization < 0.0 || resourceUtilization > 100.0) {
            throw new IllegalArgumentException("resourceUtilization must be between 0.0 and 100.0");
        }
        if (throughputEfficiency < 0.0 || throughputEfficiency > 1.0) {
            throw new IllegalArgumentException("throughputEfficiency must be between 0.0 and 1.0");
        }
        if (percentile50 < 0) {
            throw new IllegalArgumentException("percentile50 must be >= 0");
        }
        if (percentile90 < 0) {
            throw new IllegalArgumentException("percentile90 must be >= 0");
        }
        if (percentile95 < 0) {
            throw new IllegalArgumentException("percentile95 must be >= 0");
        }
        if (percentile99 < 0) {
            throw new IllegalArgumentException("percentile99 must be >= 0");
        }
        snapshots = List.copyOf(snapshots);
    }

    // BusinessMetrics implementation
    public double costEfficiency() {
        return costEfficiency;
    }

    public double resourceUtilization() {
        return resourceUtilization;
    }

    public double throughputEfficiency() {
        return throughputEfficiency;
    }

    // EfficiencyMetrics implementation
    public double resourceEfficiency() {
        return resourceEfficiency;
    }

    public double timeEfficiency() {
        return timeEfficiency;
    }

    public double energyEfficiency() {
        return energyEfficiency;
    }

    // TrendMetrics implementation
    public double trendPercentage() {
        return trendPercentage;
    }

    public String trendDirection() {
        return trendDirection;
    }

    public double changeRate() {
        return changeRate;
    }

    // PercentileMetrics implementation
    public double percentile50() {
        return percentile50;
    }

    public double percentile90() {
        return percentile90;
    }

    public double percentile95() {
        return percentile95;
    }

    public double percentile99() {
        return percentile99;
    }

    /**
     * Create statistics from a list of snapshots.
     * 
     * @param toolId the tool identifier
     * @param timeRange the time range for statistics
     * @param snapshots the list of snapshots
     * @return tool file read statistics
     */
    public static ToolFileReadStatistics fromSnapshots(String toolId, Duration timeRange,
            List<ToolFileReadSnapshot> snapshots) {
        if (snapshots.isEmpty()) {
            return new ToolFileReadStatistics(toolId, timeRange, 0, 0, 0, 0, 0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                    0.0, 0.0, 0.0, "stable", 0.0, 0.0, 0.0, 0.0, 0.0, snapshots);
        }

        long total = snapshots.stream().mapToLong(ToolFileReadSnapshot::total).sum();
        long success = snapshots.stream().mapToLong(ToolFileReadSnapshot::success).sum();
        long failure = snapshots.stream().mapToLong(ToolFileReadSnapshot::failure).sum();
        long totalDuration = snapshots.stream().mapToLong(ToolFileReadSnapshot::totalDurationNanos).sum();
        long totalFileSize = snapshots.stream().mapToLong(ToolFileReadSnapshot::totalBytesRead).sum();

        double averageFileSize = total > 0 ? (double) totalFileSize / total : 0.0;
        double averageDurationMs = total > 0 ? (double) totalDuration / total / 1_000_000.0 : 0.0;
        double successRate = total > 0 ? (success * 100.0) / total : 0.0;
        double throughputPerSecond = timeRange.toSeconds() > 0 ? (double) total / timeRange.toSeconds() : 0.0;

        // Calculate percentiles from duration data
        List<Long> durations = snapshots.stream()
                .flatMap(s -> Stream.generate(() -> s.totalDurationNanos()).limit(s.total())).sorted()
                .collect(Collectors.toList());

        double percentile50 = calculatePercentile(durations, 50);
        double percentile90 = calculatePercentile(durations, 90);
        double percentile95 = calculatePercentile(durations, 95);
        double percentile99 = calculatePercentile(durations, 99);

        // Calculate efficiency metrics (simplified calculations)
        double resourceEfficiency = total > 0 ? Math.min(1.0, (double) success / total) : 0.0;
        double timeEfficiency = total > 0 ? Math.min(1.0, 1000.0 / averageDurationMs) : 0.0;
        double energyEfficiency = total > 0 ? Math.min(1.0, (double) success / total) : 0.0;
        double costEfficiency = total > 0 ? Math.min(1.0, (double) success / total) : 0.0;
        double resourceUtilization = total > 0 ? Math.min(100.0, (double) total / timeRange.toSeconds()) : 0.0;
        double throughputEfficiency = total > 0 ? Math.min(1.0, throughputPerSecond / 100.0) : 0.0;

        // Calculate trend metrics (simplified)
        double trendPercentage = 0.0;
        String trendDirection = "stable";
        double changeRate = 0.0;

        if (snapshots.size() > 1) {
            ToolFileReadSnapshot first = snapshots.get(0);
            ToolFileReadSnapshot last = snapshots.get(snapshots.size() - 1);

            if (first.total() > 0 && last.total() > 0) {
                double firstRate = (double) first.success() / first.total();
                double lastRate = (double) last.success() / last.total();
                trendPercentage = ((lastRate - firstRate) / firstRate) * 100.0;

                if (trendPercentage > 5.0) {
                    trendDirection = "increasing";
                } else if (trendPercentage < -5.0) {
                    trendDirection = "decreasing";
                } else {
                    trendDirection = "stable";
                }

                changeRate = trendPercentage / timeRange.toDays();
            }
        }

        return new ToolFileReadStatistics(toolId, timeRange, total, success, failure, totalDuration, totalFileSize,
                averageFileSize, averageDurationMs, successRate, throughputPerSecond, resourceEfficiency,
                timeEfficiency, energyEfficiency, costEfficiency, resourceUtilization, throughputEfficiency,
                trendPercentage, trendDirection, changeRate, percentile50, percentile90, percentile95, percentile99,
                snapshots);
    }

    private static double calculatePercentile(List<Long> values, int percentile) {
        if (values.isEmpty()) {
            return 0.0;
        }

        int index = (int) Math.ceil((percentile / 100.0) * values.size()) - 1;
        index = Math.max(0, Math.min(index, values.size() - 1));
        return values.get(index) / 1_000_000.0; // Convert to milliseconds
    }
}
