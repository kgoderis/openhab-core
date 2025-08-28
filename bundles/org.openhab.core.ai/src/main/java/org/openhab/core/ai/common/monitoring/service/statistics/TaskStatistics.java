package org.openhab.core.ai.common.monitoring.service.statistics;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.PercentileMetrics;
import org.openhab.core.ai.common.monitoring.api.TrendMetrics;
import org.openhab.core.ai.common.monitoring.service.snapshot.TaskSnapshot;

/**
 * Statistics class for task lifecycle metrics.
 * 
 * <p>
 * This class provides comprehensive task lifecycle statistics including
 * execution patterns, performance metrics, state distribution, and efficiency analysis.
 * It aggregates multiple TaskSnapshot instances to provide
 * historical and statistical analysis of task behavior.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record TaskStatistics(List<TaskSnapshot> snapshots, Duration timeRange, long timestampMs)
        implements
            StatisticsSnapshot,
            CountsMetrics,
            LatencyMetrics,
            TrendMetrics,
            PercentileMetrics {

    // CountsMetrics implementation (aggregated across all snapshots)
    @Override
    public long total() {
        return snapshots.stream().mapToLong(TaskSnapshot::total).sum();
    }

    @Override
    public long success() {
        return snapshots.stream().mapToLong(TaskSnapshot::success).sum();
    }

    @Override
    public long failure() {
        return snapshots.stream().mapToLong(TaskSnapshot::failure).sum();
    }

    // LatencyMetrics implementation
    @Override
    public long totalDurationNanos() {
        return snapshots.stream().mapToLong(TaskSnapshot::totalDurationNanos).sum();
    }

    // TrendMetrics implementation
    @Override
    public double trendPercentage() {
        if (snapshots.size() < 2) {
            return 0.0;
        }
        // Calculate success rate trend from first to last snapshot
        double firstSuccessRate = snapshots.get(0).successRate();
        double lastSuccessRate = snapshots.get(snapshots.size() - 1).successRate();
        return firstSuccessRate > 0 ? ((lastSuccessRate - firstSuccessRate) / firstSuccessRate) * 100.0 : 0.0;
    }

    @Override
    public String trendDirection() {
        double percentage = trendPercentage();
        if (percentage > 5.0) {
            return "improving";
        } else if (percentage < -5.0) {
            return "declining";
        } else {
            return "stable";
        }
    }

    @Override
    public double changeRate() {
        if (snapshots.size() < 2) {
            return 0.0;
        }
        // Calculate change rate in executions per second
        long totalExecutions = total();
        long timeSpanMs = snapshots.get(snapshots.size() - 1).timestampMs() - snapshots.get(0).timestampMs();
        return timeSpanMs > 0 ? (double) totalExecutions / (timeSpanMs / 1000.0) : 0.0;
    }

    // PercentileMetrics implementation
    @Override
    public double percentile50() {
        return calculatePercentile(0.5);
    }

    @Override
    public double percentile90() {
        return calculatePercentile(0.9);
    }

    @Override
    public double percentile95() {
        return calculatePercentile(0.95);
    }

    @Override
    public double percentile99() {
        return calculatePercentile(0.99);
    }

    private double calculatePercentile(double percentile) {
        if (snapshots.isEmpty()) {
            return 0.0;
        }

        List<Double> executionTimes = snapshots.stream().mapToDouble(s -> s.averageExecutionTimeMs()).sorted().boxed()
                .toList();

        if (executionTimes.isEmpty()) {
            return 0.0;
        }

        int index = (int) Math.ceil(percentile * executionTimes.size()) - 1;
        index = Math.max(0, Math.min(index, executionTimes.size() - 1));
        return executionTimes.get(index);
    }

    /**
     * Get total retry count across all snapshots.
     * 
     * @return total retry count
     */
    public long totalRetries() {
        return snapshots.stream().mapToLong(TaskSnapshot::retryCount).sum();
    }

    /**
     * Get average success rate across all snapshots.
     * 
     * @return average success rate
     */
    public double averageSuccessRate() {
        return snapshots.stream().mapToDouble(TaskSnapshot::successRate).average().orElse(0.0);
    }

    /**
     * Get average retry rate across all snapshots.
     * 
     * @return average retry rate
     */
    public double averageRetryRate() {
        return snapshots.stream().mapToDouble(TaskSnapshot::retryRate).average().orElse(0.0);
    }

    /**
     * Get average execution time across all snapshots.
     * 
     * @return average execution time in milliseconds
     */
    public double averageExecutionTime() {
        return snapshots.stream().mapToDouble(TaskSnapshot::averageExecutionTimeMs).average().orElse(0.0);
    }

    /**
     * Get maximum execution time across all snapshots.
     * 
     * @return maximum execution time in milliseconds
     */
    public double maxExecutionTime() {
        return snapshots.stream().mapToDouble(TaskSnapshot::maxExecutionTimeMs).max().orElse(0.0);
    }

    /**
     * Get minimum execution time across all snapshots.
     * 
     * @return minimum execution time in milliseconds
     */
    public double minExecutionTime() {
        return snapshots.stream().mapToDouble(TaskSnapshot::minExecutionTimeMs).min().orElse(0.0);
    }

    /**
     * Get task state distribution.
     * 
     * @return map of task states to their counts
     */
    public Map<String, Long> taskStateDistribution() {
        return snapshots.stream().collect(Collectors.groupingBy(TaskSnapshot::taskState, Collectors.counting()));
    }

    /**
     * Get task type distribution.
     * 
     * @return map of task types to their counts
     */
    public Map<String, Long> taskTypeDistribution() {
        return snapshots.stream().collect(Collectors.groupingBy(TaskSnapshot::taskType, Collectors.counting()));
    }

    /**
     * Get count of active tasks.
     * 
     * @return number of active tasks
     */
    public long activeTaskCount() {
        return snapshots.stream().mapToLong(s -> s.isActive() ? 1 : 0).sum();
    }

    /**
     * Get count of completed tasks.
     * 
     * @return number of completed tasks
     */
    public long completedTaskCount() {
        return snapshots.stream().mapToLong(s -> s.isCompleted() ? 1 : 0).sum();
    }

    /**
     * Get count of failed tasks.
     * 
     * @return number of failed tasks
     */
    public long failedTaskCount() {
        return snapshots.stream().mapToLong(s -> s.isFailed() ? 1 : 0).sum();
    }

    /**
     * Get count of healthy tasks (good performance).
     * 
     * @return number of healthy tasks
     */
    public long healthyTaskCount() {
        return snapshots.stream().mapToLong(s -> s.isHealthy() ? 1 : 0).sum();
    }

    /**
     * Get average efficiency score across all snapshots.
     * 
     * @return average efficiency score
     */
    public double averageEfficiencyScore() {
        return snapshots.stream().mapToDouble(TaskSnapshot::efficiencyScore).average().orElse(0.0);
    }

    /**
     * Get unique task count.
     * 
     * @return number of unique tasks
     */
    public long uniqueTaskCount() {
        return snapshots.stream().map(TaskSnapshot::taskId).distinct().count();
    }

    /**
     * Get unique task type count.
     * 
     * @return number of unique task types
     */
    public long uniqueTaskTypeCount() {
        return snapshots.stream().map(TaskSnapshot::taskType).distinct().count();
    }

    /**
     * Check if overall task performance is healthy.
     * 
     * @return true if average success rate > 80% and average efficiency > 0.7
     */
    public boolean isOverallHealthy() {
        return averageSuccessRate() > 0.8 && averageEfficiencyScore() > 0.7;
    }

    /**
     * Get most common task type.
     * 
     * @return most frequently executed task type
     */
    public String mostCommonTaskType() {
        return taskTypeDistribution().entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey)
                .orElse("unknown");
    }

    /**
     * Get most common task state.
     * 
     * @return most frequently occurring task state
     */
    public String mostCommonTaskState() {
        return taskStateDistribution().entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey)
                .orElse("unknown");
    }
}
