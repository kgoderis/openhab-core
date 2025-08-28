package org.openhab.core.ai.common.monitoring.patterns;

import java.time.Duration;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.utils.SystemMetricsCollector;

/**
 * Static utility class for recording validation rule execution metrics using the enhanced MetricsService.
 * 
 * <p>
 * This class provides static methods to record comprehensive metrics for validation rule execution
 * including rule evaluation, success/failure rates, and performance characteristics.
 * All methods require a MetricsService instance as the first parameter.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class ValidationRuleMetrics {

    private ValidationRuleMetrics() {
        // Utility class - prevent instantiation
    }

    /**
     * Record validation rule execution metrics.
     * 
     * @param metricsService the metrics service instance
     * @param ruleId the unique rule identifier
     * @param ruleType the type of validation rule
     * @param executionTime the time taken to execute the rule
     * @param validationResult whether the validation passed
     * @param inputDataSize the size of input data being validated
     * @param errorMessage the error message if validation failed
     */
    public static void recordValidationRuleExecution(MetricsService metricsService, String ruleId, String ruleType, Duration executionTime,
            boolean validationResult, long inputDataSize, String errorMessage) {
        metricsService.recordOperation("validation", "rule-execution").withSuccess(validationResult)
                .withDuration(executionTime.toNanos()).withData("ruleId", ruleId).withData("ruleType", ruleType)
                .withData("inputDataSize", inputDataSize).withData("errorMessage", errorMessage)
                // Performance Metrics Enhancements
                .withTimingContext(4, inputDataSize, 0) // Moderate complexity validation
                .withResourceUtilization(SystemMetricsCollector.getCurrentMemoryUsage(),
                        SystemMetricsCollector.getCurrentCpuUsage(), 0) // Minimal resource usage
                .withConcurrencyMetrics(SystemMetricsCollector.getActiveOperationsCount(),
                        SystemMetricsCollector.getCurrentQueueSize(), 0)
                .withQualityMetrics(validationResult ? null : "validation-failure", validationResult ? null : 2, 0,
                        false)
                .withUserExperienceMetrics(executionTime.toMillis(), 0, validationResult ? 4 : 2)
                .withSystemHealthCorrelation(SystemMetricsCollector.calculateSystemHealthScore(),
                        SystemMetricsCollector.getActiveAlertsCount(),
                        SystemMetricsCollector.calculateResourceAvailability(),
                        SystemMetricsCollector.getSystemLoadAverage())
                .record();
    }

    /**
     * Record validation rule batch execution metrics.
     * 
     * @param metricsService the metrics service instance
     * @param batchId the unique batch identifier
     * @param ruleCount the number of rules in the batch
     * @param executionTime the total time taken to execute all rules
     * @param passedCount the number of rules that passed
     * @param failedCount the number of rules that failed
     * @param totalInputSize the total size of input data
     */
    public static void recordValidationRuleBatch(MetricsService metricsService, String batchId, int ruleCount, Duration executionTime, int passedCount,
            int failedCount, long totalInputSize) {
        boolean success = failedCount == 0; // Batch is successful if no rules failed

        metricsService.recordOperation("validation", "rule-batch").withSuccess(success)
                .withDuration(executionTime.toNanos()).withData("batchId", batchId).withData("ruleCount", ruleCount)
                .withData("passedCount", passedCount).withData("failedCount", failedCount)
                .withData("totalInputSize", totalInputSize)
                // Performance Metrics Enhancements
                .withTimingContext(6, totalInputSize, ruleCount * 100L) // Complex batch operation
                .withResourceUtilization(SystemMetricsCollector.getCurrentMemoryUsage(),
                        SystemMetricsCollector.getCurrentCpuUsage(), SystemMetricsCollector.estimateDiskIOTime())
                .withConcurrencyMetrics(SystemMetricsCollector.getActiveOperationsCount(),
                        SystemMetricsCollector.getCurrentQueueSize(),
                        (int) SystemMetricsCollector.getTotalContentionCount())
                .withQualityMetrics(success ? null : "batch-validation-failure", success ? null : 3, 0, false)
                .withUserExperienceMetrics(executionTime.toMillis(), 0, success ? 4 : 2)
                .withSystemHealthCorrelation(SystemMetricsCollector.calculateSystemHealthScore(),
                        SystemMetricsCollector.getActiveAlertsCount(),
                        SystemMetricsCollector.calculateResourceAvailability(),
                        SystemMetricsCollector.getSystemLoadAverage())
                .record();
    }

    /**
     * Record validation rule performance metrics.
     * 
     * @param metricsService the metrics service instance
     * @param ruleId the unique rule identifier
     * @param averageExecutionTime the average execution time over multiple runs
     * @param executionCount the number of times the rule has been executed
     * @param successRate the success rate percentage (0-100)
     * @param performanceScore the performance score (1-10)
     */
    public static void recordValidationRulePerformance(MetricsService metricsService, String ruleId, Duration averageExecutionTime, int executionCount,
            double successRate, int performanceScore) {
        boolean success = successRate >= 90.0; // Consider successful if 90%+ success rate

        metricsService.recordOperation("validation", "rule-performance").withSuccess(success)
                .withDuration(averageExecutionTime.toNanos()).withData("ruleId", ruleId)
                .withData("executionCount", executionCount).withData("successRate", successRate)
                .withData("performanceScore", performanceScore)
                // Performance Metrics Enhancements
                .withTimingContext(performanceScore, executionCount * 50L, 0) // Complexity based on performance score
                .withResourceUtilization(SystemMetricsCollector.getCurrentMemoryUsage(),
                        SystemMetricsCollector.getCurrentCpuUsage(), 0)
                .withConcurrencyMetrics(SystemMetricsCollector.getActiveOperationsCount(),
                        SystemMetricsCollector.getCurrentQueueSize(), 0)
                .withQualityMetrics(success ? null : "low-performance", success ? null : 2, 0, false)
                .withUserExperienceMetrics(averageExecutionTime.toMillis(), 0, performanceScore)
                .withSystemHealthCorrelation(SystemMetricsCollector.calculateSystemHealthScore(),
                        SystemMetricsCollector.getActiveAlertsCount(),
                        SystemMetricsCollector.calculateResourceAvailability(),
                        SystemMetricsCollector.getSystemLoadAverage())
                .record();
    }

    /**
     * Record validation rule error metrics.
     * 
     * @param metricsService the metrics service instance
     * @param ruleId the unique rule identifier
     * @param errorType the type of error that occurred
     * @param errorSeverity the severity level (1-5)
     * @param errorContext additional context about the error
     * @param recoveryTime the time taken to recover from the error
     */
    public static void recordValidationRuleError(MetricsService metricsService, String ruleId, String errorType, int errorSeverity, String errorContext,
            Duration recoveryTime) {
        metricsService.recordOperation("validation", "rule-error").withSuccess(false)
                .withDuration(recoveryTime.toNanos()).withData("ruleId", ruleId).withData("errorType", errorType)
                .withData("errorSeverity", errorSeverity).withData("errorContext", errorContext)
                // Performance Metrics Enhancements
                .withTimingContext(5, 0, 0) // Moderate complexity for error handling
                .withResourceUtilization(SystemMetricsCollector.getCurrentMemoryUsage(),
                        SystemMetricsCollector.getCurrentCpuUsage(), SystemMetricsCollector.estimateDiskIOTime())
                .withConcurrencyMetrics(SystemMetricsCollector.getActiveOperationsCount(),
                        SystemMetricsCollector.getCurrentQueueSize(),
                        (int) SystemMetricsCollector.getTotalContentionCount())
                .withQualityMetrics(errorType, errorSeverity, 0, false)
                .withUserExperienceMetrics(recoveryTime.toMillis(), 0, 1) // Poor user experience
                .withSystemHealthCorrelation(SystemMetricsCollector.calculateSystemHealthScore(),
                        SystemMetricsCollector.getActiveAlertsCount(),
                        SystemMetricsCollector.calculateResourceAvailability(),
                        SystemMetricsCollector.getSystemLoadAverage())
                .record();
    }

    /**
     * Record validation rule optimization metrics.
     * 
     * @param metricsService the metrics service instance
     * @param ruleId the unique rule identifier
     * @param optimizationType the type of optimization applied
     * @param beforePerformance the performance before optimization
     * @param afterPerformance the performance after optimization
     * @param optimizationTime the time taken to apply the optimization
     */
    public static void recordValidationRuleOptimization(MetricsService metricsService, String ruleId, String optimizationType, double beforePerformance,
            double afterPerformance, Duration optimizationTime) {
        boolean success = afterPerformance > beforePerformance; // Success if performance improved

        metricsService.recordOperation("validation", "rule-optimization").withSuccess(success)
                .withDuration(optimizationTime.toNanos()).withData("ruleId", ruleId)
                .withData("optimizationType", optimizationType).withData("beforePerformance", beforePerformance)
                .withData("afterPerformance", afterPerformance)
                // Performance Metrics Enhancements
                .withTimingContext(7, 0, 0) // High complexity for optimization
                .withResourceUtilization(SystemMetricsCollector.getCurrentMemoryUsage(),
                        SystemMetricsCollector.getCurrentCpuUsage(), SystemMetricsCollector.estimateDiskIOTime())
                .withConcurrencyMetrics(SystemMetricsCollector.getActiveOperationsCount(),
                        SystemMetricsCollector.getCurrentQueueSize(),
                        (int) SystemMetricsCollector.getTotalContentionCount())
                .withQualityMetrics(success ? null : "optimization-failure", success ? null : 2, 0, false)
                .withUserExperienceMetrics(optimizationTime.toMillis(), 0, success ? 5 : 3)
                .withSystemHealthCorrelation(SystemMetricsCollector.calculateSystemHealthScore(),
                        SystemMetricsCollector.getActiveAlertsCount(),
                        SystemMetricsCollector.calculateResourceAvailability(),
                        SystemMetricsCollector.getSystemLoadAverage())
                .record();
    }

    // Convenience methods for common use cases

    /**
     * Record validation rule execution metrics with default parameters.
     * 
     * @param metricsService the metrics service instance
     * @param ruleId the unique rule identifier
     * @param ruleType the type of validation rule
     * @param validationResult whether the validation passed
     */
    public static void recordValidationRuleExecution(MetricsService metricsService, String ruleId, String ruleType, boolean validationResult) {
        recordValidationRuleExecution(metricsService, ruleId, ruleType, Duration.ZERO, validationResult, 0, null);
    }

    /**
     * Record validation rule batch execution metrics with default parameters.
     * 
     * @param metricsService the metrics service instance
     * @param batchId the unique batch identifier
     * @param ruleCount the number of rules in the batch
     * @param passedCount the number of rules that passed
     * @param failedCount the number of rules that failed
     */
    public static void recordValidationRuleBatch(MetricsService metricsService, String batchId, int ruleCount, int passedCount, int failedCount) {
        recordValidationRuleBatch(metricsService, batchId, ruleCount, Duration.ZERO, passedCount, failedCount, 0);
    }

    /**
     * Record validation rule error metrics with default parameters.
     * 
     * @param metricsService the metrics service instance
     * @param ruleId the unique rule identifier
     * @param errorType the type of error that occurred
     * @param errorSeverity the severity level (1-5)
     */
    public static void recordValidationRuleError(MetricsService metricsService, String ruleId, String errorType, int errorSeverity) {
        recordValidationRuleError(metricsService, ruleId, errorType, errorSeverity, null, Duration.ZERO);
    }
}
