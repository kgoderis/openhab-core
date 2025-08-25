package org.openhab.core.ai.common.monitoring.service.statistics;

import java.time.Duration;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.MemoryMetrics;
import org.openhab.core.ai.common.monitoring.api.PercentileMetrics;
import org.openhab.core.ai.common.monitoring.api.TrendMetrics;
import org.openhab.core.ai.common.monitoring.service.snapshot.MemoryUsageSnapshot;

/**
 * Statistics class for memory usage metrics.
 * 
 * <p>
 * This class provides comprehensive memory usage statistics including
 * trend analysis, percentile calculations, and resource utilization metrics.
 * It aggregates multiple MemoryUsageSnapshot instances to provide
 * historical and statistical analysis of memory patterns.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record MemoryUsageStatistics(List<MemoryUsageSnapshot> snapshots, Duration timeRange,
        long timestampMs) implements StatisticsSnapshot, TrendMetrics, PercentileMetrics, MemoryMetrics {

    // TrendMetrics implementation
    @Override
    public double trendPercentage() {
        if (snapshots.size() < 2) {
            return 0.0;
        }
        // Calculate percentage change from first to last snapshot
        double firstValue = snapshots.get(0).totalMemoryUsage();
        double lastValue = snapshots.get(snapshots.size() - 1).totalMemoryUsage();
        return firstValue > 0 ? ((lastValue - firstValue) / firstValue) * 100.0 : 0.0;
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
        // Calculate change rate per time unit
        double totalChange = trendPercentage();
        long timeSpanMs = snapshots.get(snapshots.size() - 1).timestampMs() - snapshots.get(0).timestampMs();
        return timeSpanMs > 0 ? totalChange / (timeSpanMs / 1000.0) : 0.0;
    }

    // PercentileMetrics implementation
    @Override
    public double percentile50() {
        return calculatePercentile(50.0);
    }

    @Override
    public double percentile90() {
        return calculatePercentile(90.0);
    }

    @Override
    public double percentile95() {
        return calculatePercentile(95.0);
    }

    @Override
    public double percentile99() {
        return calculatePercentile(99.0);
    }

    private double calculatePercentile(double percentile) {
        if (snapshots.isEmpty()) {
            return 0.0;
        }

        List<Double> values = snapshots.stream().mapToDouble(s -> s.totalMemoryUsage()).sorted().boxed().toList();

        int index = (int) Math.ceil((percentile / 100.0) * values.size()) - 1;
        index = Math.max(0, Math.min(index, values.size() - 1));
        return values.get(index);
    }

    // MemoryMetrics implementation
    @Override
    public double heapMemoryUsage() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.heapMemoryUsage()).average().orElse(0.0);
    }

    @Override
    public double nonHeapMemoryUsage() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.nonHeapMemoryUsage()).average().orElse(0.0);
    }

    @Override
    public double totalMemoryUsage() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.totalMemoryUsage()).average().orElse(0.0);
    }

    @Override
    public double memoryAllocationRate() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.memoryAllocationRate()).average().orElse(0.0);
    }

    @Override
    public double garbageCollectionFrequency() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.garbageCollectionFrequency()).average().orElse(0.0);
    }

    @Override
    public double memoryEfficiency() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.memoryEfficiency()).average().orElse(0.0);
    }

    @Override
    public double memoryFragmentation() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.memoryFragmentation()).average().orElse(0.0);
    }

    @Override
    public long availableMemory() {
        if (snapshots.isEmpty()) {
            return 0L;
        }
        return (long) snapshots.stream().mapToLong(s -> s.availableMemory()).average().orElse(0.0);
    }

    @Override
    public long totalMemory() {
        if (snapshots.isEmpty()) {
            return 0L;
        }
        return (long) snapshots.stream().mapToLong(s -> s.totalMemory()).average().orElse(0.0);
    }

    @Override
    public long usedMemory() {
        if (snapshots.isEmpty()) {
            return 0L;
        }
        return (long) snapshots.stream().mapToLong(s -> s.usedMemory()).average().orElse(0.0);
    }
}
