package org.openhab.core.ai.common.monitoring.service.statistics;

import java.time.Duration;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.OrchestrationMetrics;
import org.openhab.core.ai.common.monitoring.api.PercentileMetrics;
import org.openhab.core.ai.common.monitoring.api.TrendMetrics;
import org.openhab.core.ai.common.monitoring.service.snapshot.OrchestrationSnapshot;

/**
 * Statistics class for orchestration metrics.
 * 
 * <p>
 * This class provides comprehensive orchestration statistics including
 * trend analysis, percentile calculations, and orchestration metrics.
 * It aggregates multiple OrchestrationSnapshot instances to provide
 * historical and statistical analysis of orchestration patterns.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record OrchestrationStatistics(List<OrchestrationSnapshot> snapshots, Duration timeRange,
        long timestampMs) implements StatisticsSnapshot, TrendMetrics, PercentileMetrics, OrchestrationMetrics {

    // TrendMetrics implementation
    @Override
    public double trendPercentage() {
        if (snapshots.size() < 2) {
            return 0.0;
        }
        // Calculate percentage change from first to last snapshot
        double firstValue = snapshots.get(0).workflowExecutionRate();
        double lastValue = snapshots.get(snapshots.size() - 1).workflowExecutionRate();
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

        List<Double> values = snapshots.stream().mapToDouble(s -> s.workflowExecutionRate()).sorted().boxed().toList();

        int index = (int) Math.ceil((percentile / 100.0) * values.size()) - 1;
        index = Math.max(0, Math.min(index, values.size() - 1));
        return values.get(index);
    }

    // OrchestrationMetrics implementation
    @Override
    public double workflowExecutionRate() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.workflowExecutionRate()).average().orElse(0.0);
    }

    @Override
    public double coordinationEfficiency() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.coordinationEfficiency()).average().orElse(0.0);
    }

    @Override
    public double resourceAllocationEfficiency() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.resourceAllocationEfficiency()).average().orElse(0.0);
    }

    @Override
    public double orchestrationComplexity() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.orchestrationComplexity()).average().orElse(0.0);
    }

    @Override
    public double workflowCompletionRate() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.workflowCompletionRate()).average().orElse(0.0);
    }

    @Override
    public double averageWorkflowDuration() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.averageWorkflowDuration()).average().orElse(0.0);
    }

    @Override
    public double orchestrationThroughput() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.orchestrationThroughput()).average().orElse(0.0);
    }

    @Override
    public double resourceUtilization() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.resourceUtilization()).average().orElse(0.0);
    }

    @Override
    public double orchestrationErrorRate() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.orchestrationErrorRate()).average().orElse(0.0);
    }

    @Override
    public long workflowQueueDepth() {
        if (snapshots.isEmpty()) {
            return 0L;
        }
        return (long) snapshots.stream().mapToLong(s -> s.workflowQueueDepth()).average().orElse(0.0);
    }
}
