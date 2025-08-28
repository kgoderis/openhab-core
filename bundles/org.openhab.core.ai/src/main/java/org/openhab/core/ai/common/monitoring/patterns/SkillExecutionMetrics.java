package org.openhab.core.ai.common.monitoring.patterns;

import java.time.Duration;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.utils.SystemMetricsCollector;

/**
 * Static utility class for recording skill execution metrics using the enhanced MetricsService.
 * 
 * <p>
 * This class provides static methods to record comprehensive metrics for skill invocation,
 * execution success/failure, and performance characteristics with full context.
 * All methods require a MetricsService instance as the first parameter.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class SkillExecutionMetrics {

    private SkillExecutionMetrics() {
        // Utility class - prevent instantiation
    }

    /**
     * Record skill invocation metrics.
     * 
     * @param metricsService the metrics service instance
     * @param skillId the unique skill identifier
     * @param skillName the name of the skill
     * @param invocationTime the time taken to invoke the skill
     * @param inputParameters the input parameters passed to the skill
     * @param agentId the ID of the agent invoking the skill
     */
    public static void recordSkillInvocation(MetricsService metricsService, String skillId, String skillName, Duration invocationTime,
            Map<String, Object> inputParameters, String agentId) {
        boolean success = invocationTime.toMillis() < 1000; // Consider successful if under 1 second

        long inputSize = inputParameters.values().stream().mapToLong(v -> v.toString().length()).sum();

        metricsService.recordOperation("skill", "invocation").withSuccess(success)
                .withDuration(invocationTime.toNanos()).withData("skillId", skillId).withData("skillName", skillName)
                .withData("agentId", agentId).withData("inputParameterCount", inputParameters.size())
                .withData("inputSize", inputSize)
                // Performance Metrics Enhancements
                .withTimingContext(5, inputSize, 0) // Moderate complexity for skill invocation
                .withResourceUtilization(SystemMetricsCollector.getCurrentMemoryUsage(),
                        SystemMetricsCollector.getCurrentCpuUsage(), 0)
                .withConcurrencyMetrics(SystemMetricsCollector.getActiveOperationsCount(),
                        SystemMetricsCollector.getCurrentQueueSize(), 0)
                .withQualityMetrics(success ? null : "invocation-timeout", success ? null : 2, 0, false)
                .withUserExperienceMetrics(invocationTime.toMillis(), 0, success ? 4 : 2)
                .withSystemHealthCorrelation(SystemMetricsCollector.calculateSystemHealthScore(),
                        SystemMetricsCollector.getActiveAlertsCount(),
                        SystemMetricsCollector.calculateResourceAvailability(),
                        SystemMetricsCollector.getSystemLoadAverage())
                .record();
    }

    /**
     * Record skill execution success metrics.
     * 
     * @param metricsService the metrics service instance
     * @param skillId the unique skill identifier
     * @param skillName the name of the skill
     * @param executionTime the total execution time
     * @param resultSize the size of the result data
     * @param agentId the ID of the agent that executed the skill
     * @param executionContext additional context about the execution
     */
    public static void recordSkillExecutionSuccess(MetricsService metricsService, String skillId, String skillName, Duration executionTime, long resultSize,
            String agentId, String executionContext) {
        metricsService.recordOperation("skill", "execution-success").withSuccess(true)
                .withDuration(executionTime.toNanos()).withData("skillId", skillId).withData("skillName", skillName)
                .withData("agentId", agentId).withData("resultSize", resultSize)
                .withData("executionContext", executionContext)
                // Performance Metrics Enhancements
                .withTimingContext(7, 0, resultSize) // High complexity with result data
                .withResourceUtilization(SystemMetricsCollector.getCurrentMemoryUsage(),
                        SystemMetricsCollector.getCurrentCpuUsage(), SystemMetricsCollector.estimateDiskIOTime())
                .withConcurrencyMetrics(SystemMetricsCollector.getActiveOperationsCount(),
                        SystemMetricsCollector.getCurrentQueueSize(),
                        (int) SystemMetricsCollector.getTotalContentionCount())
                .withQualityMetrics(null, null, 0, false) // No errors in successful execution
                .withUserExperienceMetrics(executionTime.toMillis(), executionTime.toMillis() / 2, 5) // High
                                                                                                      // satisfaction
                .withSystemHealthCorrelation(SystemMetricsCollector.calculateSystemHealthScore(),
                        SystemMetricsCollector.getActiveAlertsCount(),
                        SystemMetricsCollector.calculateResourceAvailability(),
                        SystemMetricsCollector.getSystemLoadAverage())
                .record();
    }

    /**
     * Record skill execution failure metrics.
     * 
     * @param metricsService the metrics service instance
     * @param skillId the unique skill identifier
     * @param skillName the name of the skill
     * @param executionTime the time taken before failure
     * @param errorType the type of error that occurred
     * @param errorMessage the detailed error message
     * @param agentId the ID of the agent that executed the skill
     * @param retryCount the number of retries attempted
     */
    public static void recordSkillExecutionFailure(MetricsService metricsService, String skillId, String skillName, Duration executionTime, String errorType,
            String errorMessage, String agentId, int retryCount) {
        metricsService.recordOperation("skill", "execution-failure").withSuccess(false)
                .withDuration(executionTime.toNanos()).withData("skillId", skillId).withData("skillName", skillName)
                .withData("agentId", agentId).withData("errorType", errorType).withData("errorMessage", errorMessage)
                .withData("retryCount", retryCount)
                // Performance Metrics Enhancements
                .withTimingContext(6, 0, 0) // Moderate complexity for failure handling
                .withResourceUtilization(SystemMetricsCollector.getCurrentMemoryUsage(),
                        SystemMetricsCollector.getCurrentCpuUsage(), SystemMetricsCollector.estimateDiskIOTime())
                .withConcurrencyMetrics(SystemMetricsCollector.getActiveOperationsCount(),
                        SystemMetricsCollector.getCurrentQueueSize(),
                        (int) SystemMetricsCollector.getTotalContentionCount())
                .withQualityMetrics(errorType, 4, retryCount, false) // High severity for execution failures
                .withUserExperienceMetrics(executionTime.toMillis(), 0, 1) // Poor user experience
                .withSystemHealthCorrelation(SystemMetricsCollector.calculateSystemHealthScore(),
                        SystemMetricsCollector.getActiveAlertsCount(),
                        SystemMetricsCollector.calculateResourceAvailability(),
                        SystemMetricsCollector.getSystemLoadAverage())
                .record();
    }

    /**
     * Record skill performance metrics.
     * 
     * @param metricsService the metrics service instance
     * @param skillId the unique skill identifier
     * @param skillName the name of the skill
     * @param averageExecutionTime the average execution time over multiple runs
     * @param executionCount the number of times the skill has been executed
     * @param successRate the success rate percentage (0-100)
     * @param throughput the throughput (executions per minute)
     */
    public static void recordSkillPerformance(MetricsService metricsService, String skillId, String skillName, Duration averageExecutionTime,
            int executionCount, double successRate, double throughput) {
        boolean success = successRate >= 95.0; // Consider successful if 95%+ success rate

        metricsService.recordOperation("skill", "performance").withSuccess(success)
                .withDuration(averageExecutionTime.toNanos()).withData("skillId", skillId)
                .withData("skillName", skillName).withData("executionCount", executionCount)
                .withData("successRate", successRate).withData("throughput", throughput)
                // Performance Metrics Enhancements
                .withTimingContext(8, executionCount * 100L, 0) // High complexity for performance analysis
                .withResourceUtilization(SystemMetricsCollector.getCurrentMemoryUsage(),
                        SystemMetricsCollector.getCurrentCpuUsage(), SystemMetricsCollector.estimateDiskIOTime())
                .withConcurrencyMetrics(SystemMetricsCollector.getActiveOperationsCount(),
                        SystemMetricsCollector.getCurrentQueueSize(),
                        (int) SystemMetricsCollector.getTotalContentionCount())
                .withQualityMetrics(success ? null : "low-performance", success ? null : 2, 0, false)
                .withUserExperienceMetrics(averageExecutionTime.toMillis(), 0, success ? 4 : 2)
                .withSystemHealthCorrelation(SystemMetricsCollector.calculateSystemHealthScore(),
                        SystemMetricsCollector.getActiveAlertsCount(),
                        SystemMetricsCollector.calculateResourceAvailability(),
                        SystemMetricsCollector.getSystemLoadAverage())
                .record();
    }

    /**
     * Record skill composition metrics.
     * 
     * @param metricsService the metrics service instance
     * @param compositionId the unique composition identifier
     * @param skillCount the number of skills in the composition
     * @param totalExecutionTime the total time for all skills
     * @param successfulSkills the number of skills that executed successfully
     * @param failedSkills the number of skills that failed
     * @param compositionType the type of composition (sequential, parallel, etc.)
     */
    public static void recordSkillComposition(MetricsService metricsService, String compositionId, int skillCount, Duration totalExecutionTime,
            int successfulSkills, int failedSkills, String compositionType) {
        boolean success = failedSkills == 0; // Composition is successful if no skills failed

        metricsService.recordOperation("skill", "composition").withSuccess(success)
                .withDuration(totalExecutionTime.toNanos()).withData("compositionId", compositionId)
                .withData("skillCount", skillCount).withData("successfulSkills", successfulSkills)
                .withData("failedSkills", failedSkills).withData("compositionType", compositionType)
                // Performance Metrics Enhancements
                .withTimingContext(9, skillCount * 200L, successfulSkills * 100L) // Very high complexity
                .withResourceUtilization(SystemMetricsCollector.getCurrentMemoryUsage(),
                        SystemMetricsCollector.getCurrentCpuUsage(), SystemMetricsCollector.estimateDiskIOTime())
                .withConcurrencyMetrics(SystemMetricsCollector.getActiveOperationsCount(),
                        SystemMetricsCollector.getCurrentQueueSize(),
                        (int) SystemMetricsCollector.getTotalContentionCount())
                .withQualityMetrics(success ? null : "composition-failure", success ? null : 3, 0, false)
                .withUserExperienceMetrics(totalExecutionTime.toMillis(), totalExecutionTime.toMillis() / 3,
                        success ? 4 : 2)
                .withSystemHealthCorrelation(SystemMetricsCollector.calculateSystemHealthScore(),
                        SystemMetricsCollector.getActiveAlertsCount(),
                        SystemMetricsCollector.calculateResourceAvailability(),
                        SystemMetricsCollector.getSystemLoadAverage())
                .record();
    }

    /**
     * Record skill learning metrics.
     * 
     * @param metricsService the metrics service instance
     * @param skillId the unique skill identifier
     * @param skillName the name of the skill
     * @param learningTime the time taken for learning/adaptation
     * @param learningType the type of learning (reinforcement, supervised, etc.)
     * @param improvementScore the improvement score (0-100)
     * @param trainingDataSize the size of training data used
     */
    public static void recordSkillLearning(MetricsService metricsService, String skillId, String skillName, Duration learningTime, String learningType,
            double improvementScore, long trainingDataSize) {
        boolean success = improvementScore > 0; // Success if there's any improvement

        metricsService.recordOperation("skill", "learning").withSuccess(success).withDuration(learningTime.toNanos())
                .withData("skillId", skillId).withData("skillName", skillName).withData("learningType", learningType)
                .withData("improvementScore", improvementScore).withData("trainingDataSize", trainingDataSize)
                // Performance Metrics Enhancements
                .withTimingContext(8, trainingDataSize, 0) // High complexity for learning
                .withResourceUtilization(SystemMetricsCollector.getCurrentMemoryUsage(),
                        SystemMetricsCollector.getCurrentCpuUsage(), SystemMetricsCollector.estimateDiskIOTime())
                .withConcurrencyMetrics(SystemMetricsCollector.getActiveOperationsCount(),
                        SystemMetricsCollector.getCurrentQueueSize(),
                        (int) SystemMetricsCollector.getTotalContentionCount())
                .withQualityMetrics(success ? null : "learning-failure", success ? null : 2, 0, false)
                .withUserExperienceMetrics(learningTime.toMillis(), 0, success ? 4 : 2)
                .withSystemHealthCorrelation(SystemMetricsCollector.calculateSystemHealthScore(),
                        SystemMetricsCollector.getActiveAlertsCount(),
                        SystemMetricsCollector.calculateResourceAvailability(),
                        SystemMetricsCollector.getSystemLoadAverage())
                .record();
    }
}
