package org.openhab.core.ai.common.monitoring.service.statistics;

import java.time.Duration;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.CommunicationMetrics;
import org.openhab.core.ai.common.monitoring.api.PercentileMetrics;
import org.openhab.core.ai.common.monitoring.api.TrendMetrics;
import org.openhab.core.ai.common.monitoring.service.snapshot.CommunicationSnapshot;

/**
 * Statistics class for communication metrics.
 * 
 * <p>
 * This class provides comprehensive communication statistics including
 * trend analysis, percentile calculations, and communication metrics.
 * It aggregates multiple CommunicationSnapshot instances to provide
 * historical and statistical analysis of communication patterns.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record CommunicationStatistics(List<CommunicationSnapshot> snapshots, Duration timeRange,
        long timestampMs) implements StatisticsSnapshot, TrendMetrics, PercentileMetrics, CommunicationMetrics {

    // TrendMetrics implementation
    @Override
    public double trendPercentage() {
        if (snapshots.size() < 2) {
            return 0.0;
        }
        // Calculate percentage change from first to last snapshot
        double firstValue = snapshots.get(0).communicationSuccessRate();
        double lastValue = snapshots.get(snapshots.size() - 1).communicationSuccessRate();
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

        List<Double> values = snapshots.stream().mapToDouble(s -> s.communicationSuccessRate()).sorted().boxed()
                .toList();

        int index = (int) Math.ceil((percentile / 100.0) * values.size()) - 1;
        index = Math.max(0, Math.min(index, values.size() - 1));
        return values.get(index);
    }

    // CommunicationMetrics implementation
    @Override
    public double communicationSuccessRate() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.communicationSuccessRate()).average().orElse(0.0);
    }

    @Override
    public double messageThroughput() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.messageThroughput()).average().orElse(0.0);
    }

    @Override
    public double communicationLatency() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.communicationLatency()).average().orElse(0.0);
    }

    @Override
    public double communicationReliability() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.communicationReliability()).average().orElse(0.0);
    }

    @Override
    public double messageDeliveryRate() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.messageDeliveryRate()).average().orElse(0.0);
    }

    @Override
    public double communicationErrorRate() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.communicationErrorRate()).average().orElse(0.0);
    }

    @Override
    public long messageQueueDepth() {
        if (snapshots.isEmpty()) {
            return 0L;
        }
        return (long) snapshots.stream().mapToLong(s -> s.messageQueueDepth()).average().orElse(0.0);
    }

    @Override
    public double bandwidthUtilization() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.bandwidthUtilization()).average().orElse(0.0);
    }

    @Override
    public double communicationRetryRate() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.communicationRetryRate()).average().orElse(0.0);
    }

    @Override
    public double communicationTimeoutRate() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.communicationTimeoutRate()).average().orElse(0.0);
    }
}
