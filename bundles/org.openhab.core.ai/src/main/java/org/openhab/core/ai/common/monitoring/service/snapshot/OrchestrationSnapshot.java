package org.openhab.core.ai.common.monitoring.service.snapshot;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.OrchestrationMetrics;
import org.openhab.core.ai.common.monitoring.service.statistics.StatisticsSnapshot;

/**
 * Snapshot class for orchestration metrics.
 * 
 * <p>
 * This class provides orchestration metrics including workflow execution rates,
 * coordination efficiency, resource allocation, and orchestration complexity metrics.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record OrchestrationSnapshot(long total, long success, long failure, long totalDurationNanos,
        double workflowExecutionRate, double coordinationEfficiency, double resourceAllocationEfficiency,
        double orchestrationComplexity, double workflowCompletionRate, double averageWorkflowDuration,
        double orchestrationThroughput, double resourceUtilization, double orchestrationErrorRate,
        long workflowQueueDepth,
        long timestampMs) implements StatisticsSnapshot, CountsMetrics, LatencyMetrics, OrchestrationMetrics {

    /**
     * Create an orchestration snapshot.
     * 
     * @param total total workflows
     * @param success successful workflows
     * @param failure failed workflows
     * @param totalDurationNanos total duration in nanoseconds
     * @param workflowExecutionRate workflow execution rate percentage
     * @param coordinationEfficiency coordination efficiency score
     * @param resourceAllocationEfficiency resource allocation efficiency score
     * @param orchestrationComplexity orchestration complexity score
     * @param workflowCompletionRate workflow completion rate percentage
     * @param averageWorkflowDuration average workflow duration in milliseconds
     * @param orchestrationThroughput orchestration throughput in workflows per minute
     * @param resourceUtilization resource utilization percentage
     * @param orchestrationErrorRate orchestration error rate percentage
     * @param workflowQueueDepth workflow queue depth
     * @param timestampMs timestamp in milliseconds
     */
    public OrchestrationSnapshot {
        // Validation
        if (total < 0) {
            throw new IllegalArgumentException("total must be non-negative");
        }
        if (success < 0) {
            throw new IllegalArgumentException("success must be non-negative");
        }
        if (failure < 0) {
            throw new IllegalArgumentException("failure must be non-negative");
        }
        if (totalDurationNanos < 0) {
            throw new IllegalArgumentException("totalDurationNanos must be non-negative");
        }
        if (workflowExecutionRate < 0.0 || workflowExecutionRate > 100.0) {
            throw new IllegalArgumentException("workflowExecutionRate must be between 0.0 and 100.0");
        }
        if (coordinationEfficiency < 0.0 || coordinationEfficiency > 100.0) {
            throw new IllegalArgumentException("coordinationEfficiency must be between 0.0 and 100.0");
        }
        if (resourceAllocationEfficiency < 0.0 || resourceAllocationEfficiency > 100.0) {
            throw new IllegalArgumentException("resourceAllocationEfficiency must be between 0.0 and 100.0");
        }
        if (orchestrationComplexity < 0.0 || orchestrationComplexity > 100.0) {
            throw new IllegalArgumentException("orchestrationComplexity must be between 0.0 and 100.0");
        }
        if (workflowCompletionRate < 0.0 || workflowCompletionRate > 100.0) {
            throw new IllegalArgumentException("workflowCompletionRate must be between 0.0 and 100.0");
        }
        if (averageWorkflowDuration < 0.0) {
            throw new IllegalArgumentException("averageWorkflowDuration must be non-negative");
        }
        if (orchestrationThroughput < 0.0) {
            throw new IllegalArgumentException("orchestrationThroughput must be non-negative");
        }
        if (resourceUtilization < 0.0 || resourceUtilization > 100.0) {
            throw new IllegalArgumentException("resourceUtilization must be between 0.0 and 100.0");
        }
        if (orchestrationErrorRate < 0.0 || orchestrationErrorRate > 100.0) {
            throw new IllegalArgumentException("orchestrationErrorRate must be between 0.0 and 100.0");
        }
        if (workflowQueueDepth < 0) {
            throw new IllegalArgumentException("workflowQueueDepth must be non-negative");
        }
        if (timestampMs < 0) {
            throw new IllegalArgumentException("timestampMs must be non-negative");
        }
    }

    /**
     * Create an orchestration snapshot from basic metrics.
     * 
     * @param total total workflows
     * @param success successful workflows
     * @param failure failed workflows
     * @param totalDurationNanos total duration in nanoseconds
     * @param workflowExecutionRate workflow execution rate percentage
     * @param coordinationEfficiency coordination efficiency score
     * @param resourceAllocationEfficiency resource allocation efficiency score
     * @param orchestrationComplexity orchestration complexity score
     * @param workflowCompletionRate workflow completion rate percentage
     * @param averageWorkflowDuration average workflow duration in milliseconds
     * @param orchestrationThroughput orchestration throughput in workflows per minute
     * @param resourceUtilization resource utilization percentage
     * @param orchestrationErrorRate orchestration error rate percentage
     * @param workflowQueueDepth workflow queue depth
     * @return orchestration snapshot
     */
    public static OrchestrationSnapshot of(long total, long success, long failure, long totalDurationNanos,
            double workflowExecutionRate, double coordinationEfficiency, double resourceAllocationEfficiency,
            double orchestrationComplexity, double workflowCompletionRate, double averageWorkflowDuration,
            double orchestrationThroughput, double resourceUtilization, double orchestrationErrorRate,
            long workflowQueueDepth) {
        return new OrchestrationSnapshot(total, success, failure, totalDurationNanos, workflowExecutionRate,
                coordinationEfficiency, resourceAllocationEfficiency, orchestrationComplexity, workflowCompletionRate,
                averageWorkflowDuration, orchestrationThroughput, resourceUtilization, orchestrationErrorRate,
                workflowQueueDepth, System.currentTimeMillis());
    }

    /**
     * Create an empty orchestration snapshot.
     * 
     * @return empty orchestration snapshot
     */
    public static OrchestrationSnapshot empty() {
        return new OrchestrationSnapshot(0, 0, 0, 0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0,
                System.currentTimeMillis());
    }

    // CountsMetrics implementation
    @Override
    public long total() {
        return total;
    }

    @Override
    public long success() {
        return success;
    }

    @Override
    public long failure() {
        return failure;
    }

    // LatencyMetrics implementation
    @Override
    public long totalDurationNanos() {
        return totalDurationNanos;
    }

    // OrchestrationMetrics implementation
    @Override
    public double workflowExecutionRate() {
        return workflowExecutionRate;
    }

    @Override
    public double coordinationEfficiency() {
        return coordinationEfficiency;
    }

    @Override
    public double resourceAllocationEfficiency() {
        return resourceAllocationEfficiency;
    }

    @Override
    public double orchestrationComplexity() {
        return orchestrationComplexity;
    }

    @Override
    public double workflowCompletionRate() {
        return workflowCompletionRate;
    }

    @Override
    public double averageWorkflowDuration() {
        return averageWorkflowDuration;
    }

    @Override
    public double orchestrationThroughput() {
        return orchestrationThroughput;
    }

    @Override
    public double resourceUtilization() {
        return resourceUtilization;
    }

    @Override
    public double orchestrationErrorRate() {
        return orchestrationErrorRate;
    }

    @Override
    public long workflowQueueDepth() {
        return workflowQueueDepth;
    }
}
