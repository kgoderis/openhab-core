package org.openhab.core.ai.common.monitoring.examples;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.patterns.TaskLifecycleMetrics;
import org.openhab.core.ai.common.monitoring.patterns.ValidationRuleMetrics;
import org.openhab.core.ai.common.monitoring.patterns.SkillExecutionMetrics;
import org.openhab.core.ai.common.monitoring.patterns.AuditEventMetrics;
import org.openhab.core.ai.common.monitoring.patterns.ConfigurationOperationMetrics;
import org.openhab.core.ai.common.monitoring.patterns.CardBuildingMetrics;

/**
 * Example demonstrating the enhanced performance metrics capabilities.
 * 
 * <p>
 * This class shows how to use the new performance metrics enhancements
 * in the OperationRecorder for comprehensive operation monitoring.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class PerformanceMetricsExample {

    private final MetricsService metricsService;

    public PerformanceMetricsExample(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    /**
     * Example: Model completion with full performance metrics.
     */
    public void recordModelCompletionWithFullMetrics() {
        // Simulate operation data
        String modelId = "gpt-4";
        boolean success = ThreadLocalRandom.current().nextBoolean();
        Duration duration = Duration.ofMillis(ThreadLocalRandom.current().nextLong(100, 2000));
        int inputTokens = ThreadLocalRandom.current().nextInt(100, 5000);
        int outputTokens = ThreadLocalRandom.current().nextInt(50, 1000);
        double cost = inputTokens * 0.00003 + outputTokens * 0.00006;

        // Record with comprehensive performance metrics
        metricsService.recordOperation("model", "completion").withSuccess(success).withDuration(duration.toNanos())
                .withData("modelId", modelId).withData("inputTokens", inputTokens)
                .withData("outputTokens", outputTokens).withData("cost", cost)
                // Performance Metrics Enhancements
                .withTimingContext(7, inputTokens * 4L, outputTokens * 4L) // Estimate 4 bytes per token
                .withResourceUtilization(ThreadLocalRandom.current().nextLong(50_000_000, 200_000_000), // 50-200MB
                        ThreadLocalRandom.current().nextDouble(10, 80), // 10-80% CPU
                        ThreadLocalRandom.current().nextLong(0, 100) // 0-100ms disk I/O
                ).withConcurrencyMetrics(ThreadLocalRandom.current().nextInt(1, 10), // 1-9 concurrent operations
                        ThreadLocalRandom.current().nextInt(0, 50), // 0-49 queue size
                        ThreadLocalRandom.current().nextInt(0, 5) // 0-4 contention events
                ).withQualityMetrics(success ? null : "timeout", // Error category if failed
                        success ? null : 3, // Error severity if failed
                        ThreadLocalRandom.current().nextInt(0, 3), // 0-2 retries
                        !success && ThreadLocalRandom.current().nextBoolean() // Fallback used
                ).withUserExperienceMetrics(duration.toMillis() + ThreadLocalRandom.current().nextLong(0, 500), // User
                                                                                                                // perceived
                                                                                                                // latency
                        ThreadLocalRandom.current().nextLong(1000, 10000), // User interaction time
                        success ? ThreadLocalRandom.current().nextInt(3, 6) : ThreadLocalRandom.current().nextInt(1, 4) // Satisfaction
                                                                                                                        // score
                ).withSystemHealthCorrelation(ThreadLocalRandom.current().nextDouble(70, 100), // System health score
                        ThreadLocalRandom.current().nextInt(0, 3), // Active alerts
                        ThreadLocalRandom.current().nextDouble(80, 100), // Resource availability
                        ThreadLocalRandom.current().nextDouble(0.5, 2.0) // System load average
                ).record();
    }

    /**
     * Example: Tool execution with performance metrics.
     */
    public void recordToolExecutionWithMetrics() {
        String toolName = "file-reader";
        boolean success = ThreadLocalRandom.current().nextBoolean();
        Duration duration = Duration.ofMillis(ThreadLocalRandom.current().nextLong(10, 500));
        long fileSize = ThreadLocalRandom.current().nextLong(1024, 10_000_000); // 1KB to 10MB

        metricsService.recordOperation("tool", "file-read").withSuccess(success).withDuration(duration.toNanos())
                .withData("toolName", toolName).withData("fileSize", fileSize).withData("fileType", "yaml")
                // Performance Metrics Enhancements
                .withTimingContext(3, fileSize, fileSize / 10) // Simple operation, output smaller than input
                .withResourceUtilization(fileSize / 2, // Memory usage roughly half of file size
                        ThreadLocalRandom.current().nextDouble(5, 30), // Low CPU usage
                        ThreadLocalRandom.current().nextLong(0, 50) // Minimal disk I/O
                ).withConcurrencyMetrics(ThreadLocalRandom.current().nextInt(1, 5), // Few concurrent operations
                        ThreadLocalRandom.current().nextInt(0, 10), // Small queue
                        0 // No contention for file operations
                ).withQualityMetrics(success ? null : "file-not-found", success ? null : 2, 0, // No retries for file
                                                                                               // operations
                        false // No fallback
                ).withUserExperienceMetrics(duration.toMillis(), // User perceived latency same as operation time
                        ThreadLocalRandom.current().nextLong(500, 2000), // Quick user interaction
                        success ? 5 : 2 // High satisfaction if successful, low if failed
                ).withSystemHealthCorrelation(ThreadLocalRandom.current().nextDouble(85, 100), // Good system health
                        0, // No alerts
                        ThreadLocalRandom.current().nextDouble(90, 100), // High resource availability
                        ThreadLocalRandom.current().nextDouble(0.1, 1.0) // Low system load
                ).record();
    }

    /**
     * Example: Agent task execution with performance metrics.
     */
    public void recordAgentTaskWithMetrics() {
        String agentId = "energy-optimizer";
        String taskType = "energy-analysis";
        boolean success = ThreadLocalRandom.current().nextBoolean();
        Duration duration = Duration.ofMillis(ThreadLocalRandom.current().nextLong(500, 5000));
        double decisionAccuracy = ThreadLocalRandom.current().nextDouble(0.7, 1.0);
        double learningRate = ThreadLocalRandom.current().nextDouble(0.01, 0.1);

        metricsService.recordOperation("agent", "task-execution").withSuccess(success).withDuration(duration.toNanos())
                .withData("agentId", agentId).withData("taskType", taskType)
                .withData("decisionAccuracy", decisionAccuracy).withData("learningRate", learningRate)
                // Performance Metrics Enhancements
                .withTimingContext(8, 10000, 5000) // Complex operation, moderate data sizes
                .withResourceUtilization(ThreadLocalRandom.current().nextLong(100_000_000, 500_000_000), // 100-500MB
                        ThreadLocalRandom.current().nextDouble(20, 60), // Moderate CPU usage
                        ThreadLocalRandom.current().nextLong(0, 200) // Some disk I/O
                ).withConcurrencyMetrics(ThreadLocalRandom.current().nextInt(2, 8), // Multiple concurrent operations
                        ThreadLocalRandom.current().nextInt(5, 25), // Moderate queue size
                        ThreadLocalRandom.current().nextInt(0, 3) // Some contention
                )
                .withQualityMetrics(success ? null : "resource-exhaustion", success ? null : 4,
                        ThreadLocalRandom.current().nextInt(0, 2), // 0-1 retries
                        !success && ThreadLocalRandom.current().nextBoolean() // Fallback sometimes used
                ).withUserExperienceMetrics(duration.toMillis() + ThreadLocalRandom.current().nextLong(0, 1000), // User
                                                                                                                 // perceived
                                                                                                                 // latency
                        ThreadLocalRandom.current().nextLong(2000, 15000), // Longer user interaction
                        success ? ThreadLocalRandom.current().nextInt(4, 6) : ThreadLocalRandom.current().nextInt(1, 3) // Satisfaction
                                                                                                                        // based
                                                                                                                        // on
                                                                                                                        // success
                ).withSystemHealthCorrelation(ThreadLocalRandom.current().nextDouble(60, 95), // Variable system health
                        ThreadLocalRandom.current().nextInt(0, 2), // Few alerts
                        ThreadLocalRandom.current().nextDouble(70, 95), // Variable resource availability
                        ThreadLocalRandom.current().nextDouble(0.5, 2.5) // Variable system load
                ).record();
    }

    /**
     * Example: Monitoring operation with performance metrics.
     */
    public void recordMonitoringOperationWithMetrics() {
        String operationType = "system-health-check";
        boolean success = ThreadLocalRandom.current().nextBoolean();
        Duration duration = Duration.ofMillis(ThreadLocalRandom.current().nextLong(50, 1000));
        int metricsCollected = ThreadLocalRandom.current().nextInt(10, 100);
        int alertsGenerated = success ? 0 : ThreadLocalRandom.current().nextInt(1, 5);

        metricsService.recordOperation("monitoring", "system-health-check").withSuccess(success)
                .withDuration(duration.toNanos()).withData("operationType", operationType)
                .withData("metricsCollected", metricsCollected).withData("alertsGenerated", alertsGenerated)
                .withData("metricType", "system-monitoring").withData("alertSeverity", success ? "none" : "warning")
                // Performance Metrics Enhancements
                .withTimingContext(4, metricsCollected * 100L, alertsGenerated * 50L) // Simple operation, small data
                .withResourceUtilization(ThreadLocalRandom.current().nextLong(10_000_000, 50_000_000), // 10-50MB
                        ThreadLocalRandom.current().nextDouble(5, 25), // Low CPU usage
                        ThreadLocalRandom.current().nextLong(0, 20) // Minimal disk I/O
                ).withConcurrencyMetrics(ThreadLocalRandom.current().nextInt(1, 3), // Few concurrent operations
                        ThreadLocalRandom.current().nextInt(0, 5), // Small queue
                        0 // No contention for monitoring
                ).withQualityMetrics(success ? null : "monitoring-failure", success ? null : 2, 0, // No retries for
                                                                                                   // monitoring
                        false // No fallback
                ).withUserExperienceMetrics(duration.toMillis(), // User perceived latency same as operation time
                        ThreadLocalRandom.current().nextLong(100, 1000), // Quick user interaction
                        success ? 4 : 2 // Good satisfaction if successful, poor if failed
                ).withSystemHealthCorrelation(ThreadLocalRandom.current().nextDouble(80, 100), // Good system health
                        alertsGenerated, // Alerts match generated alerts
                        ThreadLocalRandom.current().nextDouble(85, 100), // High resource availability
                        ThreadLocalRandom.current().nextDouble(0.2, 1.5) // Low to moderate system load
                ).record();
    }

    /**
     * Example: Error recovery operation with performance metrics.
     */
    public void recordErrorRecoveryWithMetrics() {
        String errorType = "connection-timeout";
        boolean success = ThreadLocalRandom.current().nextBoolean();
        Duration duration = Duration.ofMillis(ThreadLocalRandom.current().nextLong(100, 2000));
        String recoveryStrategy = success ? "retry-with-backoff" : "fallback-to-cache";
        boolean fallbackUsed = !success;

        metricsService.recordOperation("error-recovery", errorType).withSuccess(success)
                .withDuration(duration.toNanos()).withData("errorType", errorType)
                .withData("recoveryStrategy", recoveryStrategy).withData("fallbackUsed", fallbackUsed)
                // Performance Metrics Enhancements
                .withTimingContext(6, 1000, 500) // Moderate complexity, small data
                .withResourceUtilization(ThreadLocalRandom.current().nextLong(20_000_000, 100_000_000), // 20-100MB
                        ThreadLocalRandom.current().nextDouble(10, 40), // Moderate CPU usage
                        ThreadLocalRandom.current().nextLong(0, 100) // Some disk I/O
                ).withConcurrencyMetrics(ThreadLocalRandom.current().nextInt(1, 4), // Few concurrent operations
                        ThreadLocalRandom.current().nextInt(0, 8), // Small queue
                        ThreadLocalRandom.current().nextInt(0, 2) // Minimal contention
                ).withQualityMetrics(errorType, // Error category is the error type
                        3, // Moderate severity
                        ThreadLocalRandom.current().nextInt(1, 4), // 1-3 retries
                        fallbackUsed // Fallback usage matches success
                ).withUserExperienceMetrics(duration.toMillis() + ThreadLocalRandom.current().nextLong(0, 200), // User
                                                                                                                // perceived
                                                                                                                // latency
                        ThreadLocalRandom.current().nextLong(500, 3000), // User interaction time
                        success ? ThreadLocalRandom.current().nextInt(3, 5) : ThreadLocalRandom.current().nextInt(1, 3) // Satisfaction
                                                                                                                        // based
                                                                                                                        // on
                                                                                                                        // success
                ).withSystemHealthCorrelation(ThreadLocalRandom.current().nextDouble(50, 90), // Variable system health
                        ThreadLocalRandom.current().nextInt(1, 4), // Some alerts
                        ThreadLocalRandom.current().nextDouble(60, 90), // Variable resource availability
                        ThreadLocalRandom.current().nextDouble(1.0, 3.0) // Higher system load during recovery
                ).record();
    }

    /**
     * Example: Task lifecycle metrics using static pattern methods.
     */
    public void recordTaskLifecycleWithStaticMethods() {
        String taskId = "task-" + ThreadLocalRandom.current().nextInt(1000, 9999);
        String taskType = "data-processing";
        String priority = ThreadLocalRandom.current().nextBoolean() ? "high" : "medium";
        Duration estimatedDuration = Duration.ofMillis(ThreadLocalRandom.current().nextLong(1000, 10000));
        Duration creationTime = Duration.ofMillis(ThreadLocalRandom.current().nextLong(10, 100));
        Duration activationTime = Duration.ofMillis(ThreadLocalRandom.current().nextLong(50, 500));
        Duration executionTime = Duration.ofMillis(ThreadLocalRandom.current().nextLong(500, 5000));
        boolean success = ThreadLocalRandom.current().nextBoolean();
        long resultSize = ThreadLocalRandom.current().nextLong(1024, 1024 * 1024); // 1KB to 1MB
        String errorMessage = success ? null : "Processing failed due to invalid input";

        // Record task creation
        TaskLifecycleMetrics.recordTaskCreation(metricsService, taskId, taskType, priority, estimatedDuration, creationTime);

        // Record task activation
        TaskLifecycleMetrics.recordTaskActivation(metricsService, taskId, taskType, activationTime, 
                Duration.ofMillis(ThreadLocalRandom.current().nextLong(0, 100)), 
                ThreadLocalRandom.current().nextLong(10_000_000, 50_000_000)); // 10-50MB allocated

        // Record task completion
        TaskLifecycleMetrics.recordTaskCompletion(metricsService, taskId, taskType, executionTime, resultSize, success, errorMessage);
    }

    /**
     * Example: Validation rule metrics using static pattern methods.
     */
    public void recordValidationRuleWithStaticMethods() {
        String ruleId = "rule-" + ThreadLocalRandom.current().nextInt(100, 999);
        String ruleType = "data-validation";
        Duration executionTime = Duration.ofMillis(ThreadLocalRandom.current().nextLong(10, 500));
        boolean validationResult = ThreadLocalRandom.current().nextBoolean();
        int inputDataSize = ThreadLocalRandom.current().nextInt(100, 10000);
        String errorMessage = validationResult ? null : "Validation failed: Invalid data format";

        // Record validation rule execution
        ValidationRuleMetrics.recordValidationRuleExecution(metricsService, ruleId, ruleType, executionTime, 
                validationResult, inputDataSize, errorMessage);

        // Record validation rule error if validation failed
        if (!validationResult) {
            ValidationRuleMetrics.recordValidationRuleError(metricsService, ruleId, ruleType, 
                    ThreadLocalRandom.current().nextInt(1, 5));
        }
    }

    /**
     * Example: Skill execution metrics using static pattern methods.
     */
    public void recordSkillExecutionWithStaticMethods() {
        String skillId = "skill-" + ThreadLocalRandom.current().nextInt(100, 999);
        String skillType = "data-transformation";
        boolean success = ThreadLocalRandom.current().nextBoolean();
        Duration executionTime = Duration.ofMillis(ThreadLocalRandom.current().nextLong(100, 2000));
        int complexity = ThreadLocalRandom.current().nextInt(1, 10);
        String context = "batch-processing";

        // Record skill invocation
        Map<String, Object> inputParameters = Map.of("complexity", complexity, "context", context);
        SkillExecutionMetrics.recordSkillInvocation(metricsService, skillId, skillType, executionTime, 
                inputParameters, "agent-" + ThreadLocalRandom.current().nextInt(100, 999));

        // Record skill execution result
        String agentId = "agent-" + ThreadLocalRandom.current().nextInt(100, 999);
        if (success) {
            SkillExecutionMetrics.recordSkillExecutionSuccess(metricsService, skillId, skillType, executionTime, 
                    ThreadLocalRandom.current().nextLong(1024, 10240), agentId, context);
        } else {
            SkillExecutionMetrics.recordSkillExecutionFailure(metricsService, skillId, skillType, executionTime, 
                    "execution-error", "Skill execution failed due to invalid input", agentId, 0);
        }
    }

    /**
     * Example: Audit event metrics using static pattern methods.
     */
    public void recordAuditEventWithStaticMethods() {
        String eventType = "user-action";
        String category = "authentication";
        String severity = ThreadLocalRandom.current().nextBoolean() ? "info" : "warning";
        String eventId = "event-" + ThreadLocalRandom.current().nextInt(1000, 9999);
        boolean success = ThreadLocalRandom.current().nextBoolean();
        String eventData = "User login attempt from IP: 192.168.1.100";

        // Record audit event
        Map<String, Object> eventDataMap = Map.of("eventData", eventData, "success", success);
        int severityLevel = "warning".equals(severity) ? 2 : 1;
        AuditEventMetrics.recordAuditEvent(metricsService, eventId, category, eventType, severityLevel, 
                "admin", eventDataMap, Duration.ofMillis(ThreadLocalRandom.current().nextLong(1, 50)));

        // Record security audit
        AuditEventMetrics.recordSecurityAuditEvent(metricsService, eventId, "login-attempt", severityLevel, 
                "192.168.1.100", "admin", success ? "login-success" : "login-failed", 
                Duration.ofMillis(ThreadLocalRandom.current().nextLong(1, 20)));
    }

    /**
     * Example: Configuration operation metrics using static pattern methods.
     */
    public void recordConfigurationOperationWithStaticMethods() {
        String cacheType = "configuration-cache";
        String operation = "get";
        boolean success = ThreadLocalRandom.current().nextBoolean();
        Duration responseTime = Duration.ofMillis(ThreadLocalRandom.current().nextLong(1, 100));
        long cacheSize = ThreadLocalRandom.current().nextLong(1024 * 1024, 100 * 1024 * 1024); // 1MB to 100MB

        // Record cache operation
        String cacheKey = "config-" + ThreadLocalRandom.current().nextInt(100, 999);
        ConfigurationOperationMetrics.recordCacheOperation(metricsService, cacheType, operation, cacheKey, 
                cacheSize, responseTime);

        // Record file operation
        String configType = "application-config";
        Duration fileOperationTime = Duration.ofMillis(ThreadLocalRandom.current().nextLong(10, 500));
        long configSize = ThreadLocalRandom.current().nextLong(1024, 1024 * 1024); // 1KB to 1MB

        ConfigurationOperationMetrics.recordFileOperation(metricsService, configType, "read", configSize, 
                fileOperationTime, success);
    }

    /**
     * Example: Card building metrics using static pattern methods.
     */
    public void recordCardBuildingWithStaticMethods() {
        String cardType = "agent-card";
        boolean success = ThreadLocalRandom.current().nextBoolean();
        Duration generationTime = Duration.ofMillis(ThreadLocalRandom.current().nextLong(100, 2000));
        int stepCount = ThreadLocalRandom.current().nextInt(3, 10);
        int validationCount = ThreadLocalRandom.current().nextInt(1, 5);

        // Record card generation
        String cardId = "card-" + ThreadLocalRandom.current().nextInt(1000, 9999);
        String agentId = "agent-" + ThreadLocalRandom.current().nextInt(100, 999);
        long cardSize = ThreadLocalRandom.current().nextLong(1024, 10 * 1024); // 1KB to 10KB
        
        CardBuildingMetrics.recordCardGeneration(metricsService, cardId, cardType, agentId, generationTime, 
                cardSize, success);

        // Record card validation
        Duration validationTime = Duration.ofMillis(ThreadLocalRandom.current().nextLong(10, 200));
        int validationErrors = success ? 0 : ThreadLocalRandom.current().nextInt(1, 3);

        CardBuildingMetrics.recordCardValidation(metricsService, cardId, validationErrors, validationTime, 
                validationCount, stepCount, success);
    }
}
