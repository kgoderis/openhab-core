package org.openhab.core.ai.common.monitoring.patterns;

import java.time.Duration;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.utils.SystemMetricsCollector;

/**
 * Utility class for recording task lifecycle metrics using the enhanced MetricsService.
 * 
 * <p>
 * This class provides methods to record comprehensive metrics for task lifecycle events
 * including creation, activation, completion, and cancellation with full performance context.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class TaskLifecycleMetrics {

    private final MetricsService metricsService;

    public TaskLifecycleMetrics(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    /**
     * Record task creation metrics.
     * 
     * @param taskId the unique task identifier
     * @param taskType the type of task being created
     * @param priority the task priority level
     * @param estimatedDuration the estimated task duration
     * @param creationTime the time taken to create the task
     */
    public void recordTaskCreation(String taskId, String taskType, String priority, Duration estimatedDuration,
            Duration creationTime) {
        metricsService.recordOperation("task", "creation").withSuccess(true).withDuration(creationTime.toNanos())
                .withData("taskId", taskId).withData("taskType", taskType).withData("priority", priority)
                .withData("estimatedDuration", estimatedDuration.toMillis())
                // Performance Metrics Enhancements
                .withTimingContext(3, taskId.length() + taskType.length(), 0) // Simple creation operation
                .withResourceUtilization(SystemMetricsCollector.getCurrentMemoryUsage(),
                        SystemMetricsCollector.getCurrentCpuUsage(), 0) // Minimal resource usage
                .withConcurrencyMetrics(SystemMetricsCollector.getActiveOperationsCount(),
                        SystemMetricsCollector.getCurrentQueueSize(), 0) // Current system state
                .withQualityMetrics(null, null, 0, false) // No errors in creation
                .withUserExperienceMetrics(creationTime.toMillis(), 0, null) // No user interaction yet
                .withSystemHealthCorrelation(SystemMetricsCollector.calculateSystemHealthScore(),
                        SystemMetricsCollector.getActiveAlertsCount(),
                        SystemMetricsCollector.calculateResourceAvailability(),
                        SystemMetricsCollector.getSystemLoadAverage())
                .record();
    }

    /**
     * Record task activation metrics.
     * 
     * @param taskId the unique task identifier
     * @param taskType the type of task being activated
     * @param activationTime the time taken to activate the task
     * @param queueWaitTime the time the task waited in queue
     * @param resourceAllocated the amount of resources allocated
     */
    public void recordTaskActivation(String taskId, String taskType, Duration activationTime, Duration queueWaitTime,
            long resourceAllocated) {
        boolean success = activationTime.toMillis() < 5000; // Consider activation successful if under 5 seconds

        metricsService.recordOperation("task", "activation").withSuccess(success).withDuration(activationTime.toNanos())
                .withData("taskId", taskId).withData("taskType", taskType)
                .withData("queueWaitTime", queueWaitTime.toMillis()).withData("resourceAllocated", resourceAllocated)
                // Performance Metrics Enhancements
                .withTimingContext(5, resourceAllocated, resourceAllocated / 2) // Moderate complexity
                .withResourceUtilization(resourceAllocated, SystemMetricsCollector.getCurrentCpuUsage(),
                        SystemMetricsCollector.estimateDiskIOTime())
                .withConcurrencyMetrics(SystemMetricsCollector.getActiveOperationsCount(),
                        SystemMetricsCollector.getCurrentQueueSize(),
                        (int) SystemMetricsCollector.getTotalContentionCount())
                .withQualityMetrics(success ? null : "activation-timeout", success ? null : 2, 0, false)
                .withUserExperienceMetrics(activationTime.toMillis() + queueWaitTime.toMillis(), 0, null)
                .withSystemHealthCorrelation(SystemMetricsCollector.calculateSystemHealthScore(),
                        SystemMetricsCollector.getActiveAlertsCount(),
                        SystemMetricsCollector.calculateResourceAvailability(),
                        SystemMetricsCollector.getSystemLoadAverage())
                .record();
    }

    /**
     * Record task completion metrics.
     * 
     * @param taskId the unique task identifier
     * @param taskType the type of task completed
     * @param executionTime the total execution time
     * @param resultSize the size of the result data
     * @param success whether the task completed successfully
     * @param errorMessage the error message if failed
     */
    public void recordTaskCompletion(String taskId, String taskType, Duration executionTime, long resultSize,
            boolean success, String errorMessage) {
        metricsService.recordOperation("task", "completion").withSuccess(success).withDuration(executionTime.toNanos())
                .withData("taskId", taskId).withData("taskType", taskType).withData("resultSize", resultSize)
                .withData("errorMessage", errorMessage)
                // Performance Metrics Enhancements
                .withTimingContext(7, 0, resultSize) // Complex operation with result data
                .withResourceUtilization(SystemMetricsCollector.getCurrentMemoryUsage(),
                        SystemMetricsCollector.getCurrentCpuUsage(), SystemMetricsCollector.estimateDiskIOTime())
                .withConcurrencyMetrics(SystemMetricsCollector.getActiveOperationsCount(),
                        SystemMetricsCollector.getCurrentQueueSize(),
                        (int) SystemMetricsCollector.getTotalContentionCount())
                .withQualityMetrics(success ? null : "execution-failure", success ? null : 3, 0, false)
                .withUserExperienceMetrics(executionTime.toMillis(), executionTime.toMillis() / 2, success ? 4 : 2) // User
                                                                                                                    // satisfaction
                                                                                                                    // based
                                                                                                                    // on
                                                                                                                    // success
                .withSystemHealthCorrelation(SystemMetricsCollector.calculateSystemHealthScore(),
                        SystemMetricsCollector.getActiveAlertsCount(),
                        SystemMetricsCollector.calculateResourceAvailability(),
                        SystemMetricsCollector.getSystemLoadAverage())
                .record();
    }

    /**
     * Record task cancellation metrics.
     * 
     * @param taskId the unique task identifier
     * @param taskType the type of task cancelled
     * @param cancellationTime the time taken to cancel the task
     * @param executionProgress the progress made before cancellation (0-100)
     * @param reason the reason for cancellation
     */
    public void recordTaskCancellation(String taskId, String taskType, Duration cancellationTime,
            double executionProgress, String reason) {
        metricsService.recordOperation("task", "cancellation").withSuccess(true)
                .withDuration(cancellationTime.toNanos()).withData("taskId", taskId).withData("taskType", taskType)
                .withData("executionProgress", executionProgress).withData("reason", reason)
                // Performance Metrics Enhancements
                .withTimingContext(4, 0, 0) // Simple cancellation operation
                .withResourceUtilization(SystemMetricsCollector.getCurrentMemoryUsage(),
                        SystemMetricsCollector.getCurrentCpuUsage(), 0)
                .withConcurrencyMetrics(SystemMetricsCollector.getActiveOperationsCount(),
                        SystemMetricsCollector.getCurrentQueueSize(), 0)
                .withQualityMetrics("cancellation", 1, 0, false) // Cancellation is a controlled event
                .withUserExperienceMetrics(cancellationTime.toMillis(), 0, 3) // Neutral user satisfaction
                .withSystemHealthCorrelation(SystemMetricsCollector.calculateSystemHealthScore(),
                        SystemMetricsCollector.getActiveAlertsCount(),
                        SystemMetricsCollector.calculateResourceAvailability(),
                        SystemMetricsCollector.getSystemLoadAverage())
                .record();
    }

    /**
     * Record task failure metrics.
     * 
     * @param taskId the unique task identifier
     * @param taskType the type of task that failed
     * @param failureTime the time when the failure occurred
     * @param errorCategory the category of error
     * @param retryCount the number of retries attempted
     * @param fallbackUsed whether a fallback mechanism was used
     */
    public void recordTaskFailure(String taskId, String taskType, Duration failureTime, String errorCategory,
            int retryCount, boolean fallbackUsed) {
        metricsService.recordOperation("task", "failure").withSuccess(false).withDuration(failureTime.toNanos())
                .withData("taskId", taskId).withData("taskType", taskType).withData("errorCategory", errorCategory)
                .withData("retryCount", retryCount).withData("fallbackUsed", fallbackUsed)
                // Performance Metrics Enhancements
                .withTimingContext(6, 0, 0) // Moderate complexity for failure handling
                .withResourceUtilization(SystemMetricsCollector.getCurrentMemoryUsage(),
                        SystemMetricsCollector.getCurrentCpuUsage(), SystemMetricsCollector.estimateDiskIOTime())
                .withConcurrencyMetrics(SystemMetricsCollector.getActiveOperationsCount(),
                        SystemMetricsCollector.getCurrentQueueSize(),
                        (int) SystemMetricsCollector.getTotalContentionCount())
                .withQualityMetrics(errorCategory, 4, retryCount, fallbackUsed) // High severity for failures
                .withUserExperienceMetrics(failureTime.toMillis(), 0, 1) // Poor user experience
                .withSystemHealthCorrelation(SystemMetricsCollector.calculateSystemHealthScore(),
                        SystemMetricsCollector.getActiveAlertsCount(),
                        SystemMetricsCollector.calculateResourceAvailability(),
                        SystemMetricsCollector.getSystemLoadAverage())
                .record();
    }
}
