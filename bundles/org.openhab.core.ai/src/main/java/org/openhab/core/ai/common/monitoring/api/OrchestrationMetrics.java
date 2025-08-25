package org.openhab.core.ai.common.monitoring.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Capability interface for orchestration metrics.
 * 
 * <p>
 * This interface provides orchestration-specific functionality including
 * workflow execution rates, coordination efficiency, resource allocation,
 * and orchestration complexity metrics.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface OrchestrationMetrics {

    /**
     * Get the workflow execution success rate as a percentage.
     * 
     * @return execution success rate between 0.0 and 100.0
     */
    double workflowExecutionRate();

    /**
     * Get the orchestration coordination efficiency (0-100).
     * 
     * @return coordination efficiency score
     */
    double coordinationEfficiency();

    /**
     * Get the resource allocation efficiency (0-100).
     * 
     * @return resource allocation efficiency
     */
    double resourceAllocationEfficiency();

    /**
     * Get the orchestration complexity score (0-100).
     * 
     * @return complexity score
     */
    double orchestrationComplexity();

    /**
     * Get the workflow completion rate as a percentage.
     * 
     * @return completion rate between 0.0 and 100.0
     */
    double workflowCompletionRate();

    /**
     * Get the average workflow duration in milliseconds.
     * 
     * @return average workflow duration
     */
    double averageWorkflowDuration();

    /**
     * Get the orchestration throughput in workflows per minute.
     * 
     * @return orchestration throughput
     */
    double orchestrationThroughput();

    /**
     * Get the resource utilization rate as a percentage.
     * 
     * @return resource utilization between 0.0 and 100.0
     */
    double resourceUtilization();

    /**
     * Get the orchestration error rate as a percentage.
     * 
     * @return error rate between 0.0 and 100.0
     */
    double orchestrationErrorRate();

    /**
     * Get the workflow queue depth.
     * 
     * @return queue depth
     */
    long workflowQueueDepth();
}
