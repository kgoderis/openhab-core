package org.openhab.core.ai.agent.execution;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.agent.execution.api.AgentSkillException;
import org.openhab.core.ai.agent.execution.api.AgentSkillResult;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.service.statistics.ExecutionStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.a2a.spec.Message;

/**
 * Skill Execution and Metrics Management.
 * 
 * <p>
 * <strong>Primary Responsibility:</strong> Skill Execution and Metrics Management
 * </p>
 * 
 * <p>
 * <strong>Concerns:</strong>
 * <ul>
 * <li><strong>Skill Execution:</strong> Executes skills through their adapters</li>
 * <li><strong>Execution Tracking:</strong> Tracks execution metrics and statistics</li>
 * <li><strong>Error Handling:</strong> Handles execution errors and exceptions</li>
 * <li><strong>Result Processing:</strong> Processes and formats execution results</li>
 * <li><strong>Retry Logic:</strong> Implements retry mechanisms for failed executions</li>
 * <li><strong>Performance Monitoring:</strong> Monitors execution performance</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>What this class DOES:</strong>
 * <ul>
 * <li>Executes skills via {@link #executeSkill(String, Message)}</li>
 * <li>Implements retry logic via {@link #executeSkillWithRetry(String, Message, int)}</li>
 * <li>Tracks execution metrics and statistics</li>
 * <li>Handles execution errors and exceptions</li>
 * <li>Processes and formats execution results</li>
 * <li>Monitors execution performance</li>
 * <li>Delegates skill lookup to {@link AgentSkillRegistry}</li>
 * <li>Delegates actual execution to {@link AgentSkillAdapter}</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>What this class DOES NOT do:</strong>
 * <ul>
 * <li>❌ Register skills (delegates to AgentSkillRegistry)</li>
 * <li>❌ Handle protocol communication (delegates to AgentProtocolHandler)</li>
 * <li>❌ Manage task lifecycle (delegates to AgentTaskManager)</li>
 * <li>❌ Handle authentication (delegates to security manager)</li>
 * <li>❌ Convert parameters (delegates to AgentSkillManagerImpl)</li>
 * <li>❌ Manage skill registry (delegates to AgentSkillRegistry)</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>Boundary Conditions:</strong>
 * <ul>
 * <li>Only executes skills and tracks metrics</li>
 * <li>Delegates skill lookup to AgentSkillRegistry</li>
 * <li>Delegates actual execution to AgentSkillAdapter</li>
 * <li>Does not handle parameter conversion</li>
 * <li>Does not register skills</li>
 * <li>Does not handle protocol communication</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>Dependencies:</strong>
 * <ul>
 * <li>{@link AgentSkillRegistry}: For skill adapter lookup</li>
 * <li>{@link AgentSkillAdapter}: For actual skill execution</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>Architecture Layer:</strong> Skill Management Layer
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class AgentSkillExecutor {

    private static final Logger logger = LoggerFactory.getLogger(AgentSkillExecutor.class);

    private final AtomicReference<@Nullable AgentSkillRegistry> skillRegistry = new AtomicReference<>();
    private @Nullable MetricsService metricsService;

    /**
     * Set the skill registry reference.
     * 
     * @param registry the skill registry
     */
    public void setSkillRegistry(@Nullable AgentSkillRegistry registry) {
        this.skillRegistry.set(registry);
    }

    /**
     * Set the metrics service reference.
     * 
     * @param metricsService the metrics service
     */
    public void setMetricsService(MetricsService metricsService) {
        this.metricsService = metricsService;
        logger.debug("Metrics service set for AgentSkillExecutor");
    }

    /**
     * Execute a skill with the given message.
     * 
     * @param skillId the skill ID to execute
     * @param message the message containing execution parameters
     * @return the execution result
     * @throws AgentSkillException if execution fails
     */
    public AgentSkillResult executeSkill(String skillId, Message message) throws AgentSkillException {
        long startTime = System.currentTimeMillis();
        long startTimeNanos = System.nanoTime();

        logger.debug("Executing Agent skill: {}", skillId);

        try {
            // Get skill registry
            AgentSkillRegistry registry = skillRegistry.get();
            if (registry == null) {
                String errorMsg = "Skill registry not available";
                logger.error(errorMsg);
                recordMetrics("agent-skill", "execution", false, System.nanoTime() - startTimeNanos);
                return AgentSkillResult.failure(errorMsg, "REGISTRY_UNAVAILABLE",
                        System.currentTimeMillis() - startTime);
            }

            // Get skill adapter
            AgentSkillAdapter adapter = registry.getSkillAdapter(skillId);
            if (adapter == null) {
                String errorMsg = "Skill not found: " + skillId;
                logger.warn(errorMsg);
                recordMetrics("agent-skill", "execution", false, System.nanoTime() - startTimeNanos);
                return AgentSkillResult.failure(errorMsg, "SKILL_NOT_FOUND", System.currentTimeMillis() - startTime);
            }

            // Execute the skill
            Object result = adapter.execute(message);
            long executionTime = System.currentTimeMillis() - startTime;
            long durationNanos = System.nanoTime() - startTimeNanos;

            // Track successful execution
            recordMetrics("agent-skill", "execution", true, durationNanos);

            logger.debug("Skill execution completed successfully: {} in {}ms", skillId, executionTime);

            // Convert result to AgentSkillResult
            if (result instanceof AgentSkillResult) {
                return (AgentSkillResult) result;
            } else {
                // Convert result to Map format for AgentSkillResult
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("result", result);
                resultMap.put("skillId", skillId);
                return AgentSkillResult.success(resultMap, executionTime);
            }

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            long durationNanos = System.nanoTime() - startTimeNanos;

            recordMetrics("agent-skill", "execution", false, durationNanos);

            logger.error("Skill execution failed: {}", skillId, e);
            return AgentSkillResult.failure("Skill execution failed: " + e.getMessage(), "EXECUTION_ERROR",
                    executionTime);
        }
    }

    /**
     * Execute a skill with retry logic.
     * 
     * @param skillId the skill ID to execute
     * @param message the message containing execution parameters
     * @param maxRetries the maximum number of retries
     * @return the execution result
     * @throws AgentSkillException if execution fails after all retries
     */
    public AgentSkillResult executeSkillWithRetry(String skillId, Message message, int maxRetries)
            throws AgentSkillException {

        AgentSkillResult lastResult = null;
        Exception lastException = null;

        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                lastResult = executeSkill(skillId, message);

                // If successful, return immediately
                if (lastResult.isSuccess()) {
                    return lastResult;
                }

                // If not successful but not an exception, return the result
                if (attempt == maxRetries) {
                    return lastResult;
                }

            } catch (Exception e) {
                lastException = e;
                logger.warn("Skill execution attempt {} failed for skill {}: {}", attempt + 1, skillId, e.getMessage());

                if (attempt == maxRetries) {
                    throw new AgentSkillException("Skill execution failed after " + maxRetries + " retries",
                            lastException);
                }

                // Wait before retry (exponential backoff)
                try {
                    Thread.sleep((long) Math.pow(2, attempt) * 100);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new AgentSkillException("Skill execution interrupted", ie);
                }
            }
        }

        // This should never be reached, but just in case
        if (lastException != null) {
            throw new AgentSkillException("Skill execution failed after " + maxRetries + " retries", lastException);
        }

        return lastResult != null ? lastResult
                : AgentSkillResult.failure("Unknown execution failure", "UNKNOWN_ERROR", 0);
    }

    /**
     * Get execution statistics.
     * 
     * @return the execution statistics
     */
    public ExecutionStatistics getExecutionStatistics() {
        MetricsService metrics = metricsService;
        if (metrics != null) {
            try {
                var snapshot = metrics.getDomainAggregatedSnapshot("agent-skill");
                return ExecutionStatistics.fromExecutionData(snapshot.totalOperations(),
                        snapshot.totalOperations() - snapshot.failedOperations(), snapshot.failedOperations(),
                        snapshot.totalDurationNanos(), Duration.ofDays(1) // Default time range
                );
            } catch (Exception e) {
                logger.warn("Error retrieving metrics for agent-skill: {}", e.getMessage());
            }
        } else {
            logger.warn("MetricsService not available, returning empty statistics");
        }

        // Return empty statistics if MetricsService is not available
        return ExecutionStatistics.fromExecutionData(0L, 0L, 0L, 0L, Duration.ofDays(1));
    }

    /**
     * Record metrics for an operation.
     * 
     * @param domain the operation domain
     * @param operation the operation name
     * @param success whether the operation was successful
     * @param durationNanos the operation duration in nanoseconds
     */
    private void recordMetrics(String domain, String operation, boolean success, long durationNanos) {
        MetricsService metrics = metricsService;
        if (metrics != null) {
            metrics.recordOperation(domain, operation, success, java.time.Duration.ofNanos(durationNanos));
        } else {
            logger.warn("MetricsService not available, cannot record metrics for operation: {} - {}", domain,
                    operation);
        }
    }

    /**
     * Reset execution statistics.
     */
    public void resetExecutionStatistics() {
        // Reset is handled by the MetricsService, no local counters to reset
        logger.info("Execution statistics reset requested - handled by MetricsService");
    }

    // Inner class extracted to top-level: org.openhab.core.ai.agent.execution.ExecutionStatistics
}
