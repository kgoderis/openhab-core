package org.openhab.core.ai.common.monitoring.service.statistics;

import java.time.Duration;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.api.PercentileMetrics;
import org.openhab.core.ai.common.monitoring.api.TransportMetrics;
import org.openhab.core.ai.common.monitoring.api.TrendMetrics;
import org.openhab.core.ai.common.monitoring.service.snapshot.TransportSnapshot;
import org.osgi.service.component.annotations.Reference;

/**
 * Statistics class for transport metrics.
 * 
 * <p>
 * This class provides comprehensive transport statistics including
 * trend analysis, percentile calculations, and transport metrics.
 * It aggregates multiple TransportSnapshot instances to provide
 * historical and statistical analysis of transport patterns.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record TransportStatistics(List<TransportSnapshot> snapshots, Duration timeRange,
        long timestampMs) implements StatisticsSnapshot, TrendMetrics, PercentileMetrics, TransportMetrics {

    @Reference
    private static @Nullable MetricsService metricsService;

    // TrendMetrics implementation
    @Override
    public double trendPercentage() {
        if (snapshots.size() < 2) {
            return 0.0;
        }
        // Calculate percentage change from first to last snapshot
        double firstValue = snapshots.get(0).transportReliability();
        double lastValue = snapshots.get(snapshots.size() - 1).transportReliability();
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

        List<Double> values = snapshots.stream().mapToDouble(s -> s.transportReliability()).sorted().boxed().toList();

        int index = (int) Math.ceil((percentile / 100.0) * values.size()) - 1;
        index = Math.max(0, Math.min(index, values.size() - 1));
        return values.get(index);
    }

    // TransportMetrics implementation
    @Override
    public double transportReliability() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.transportReliability()).average().orElse(0.0);
    }

    @Override
    public double transportThroughput() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.transportThroughput()).average().orElse(0.0);
    }

    @Override
    public double transportLatency() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.transportLatency()).average().orElse(0.0);
    }

    @Override
    public double transportEfficiency() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.transportEfficiency()).average().orElse(0.0);
    }

    @Override
    public double transportErrorRate() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.transportErrorRate()).average().orElse(0.0);
    }

    @Override
    public double connectionSuccessRate() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.connectionSuccessRate()).average().orElse(0.0);
    }

    @Override
    public double packetLossRate() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.packetLossRate()).average().orElse(0.0);
    }

    @Override
    public double transportBandwidthUtilization() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.transportBandwidthUtilization()).average().orElse(0.0);
    }

    @Override
    public double transportRetryRate() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.transportRetryRate()).average().orElse(0.0);
    }

    @Override
    public double transportTimeoutRate() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.transportTimeoutRate()).average().orElse(0.0);
    }

    /**
     * Create statistics from a list of snapshots.
     * 
     * @param snapshots the list of snapshots to aggregate
     * @param timeRange the time range for the statistics
     * @return transport statistics
     */
    public static TransportStatistics fromSnapshots(List<TransportSnapshot> snapshots, Duration timeRange) {
        return new TransportStatistics(snapshots, timeRange, System.currentTimeMillis());
    }

    /**
     * Record transport statistics using MetricsService.
     */
    public static void recordTransportStatistics(boolean success, long durationMs) {
        MetricsService metrics = metricsService;
        if (metrics != null) {
            metrics.recordOperation("transport", "statistics", success, java.time.Duration.ofMillis(durationMs));
        }
    }
}
