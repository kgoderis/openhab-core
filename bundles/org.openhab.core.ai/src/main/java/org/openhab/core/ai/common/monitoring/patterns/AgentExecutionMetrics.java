/*
 * Copyright (c) 2010-2024 openHAB e.V. and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 */
package org.openhab.core.ai.common.monitoring.patterns;

import java.time.Duration;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.MetricsService;

/**
 * Static utility class for recording agent execution metrics.
 * 
 * <p>This class provides static methods for recording various agent execution operations
 * including task execution, skill execution, task assignment, and lifecycle events.
 * All methods follow the static utility pattern with MetricsService as the first parameter.
 * 
 * <p>Example usage:
 * <pre>{@code
 * // Record agent task execution
 * AgentExecutionMetrics.recordAgentExecution(metricsService, "agent-123", "communication", 
 *     true, Duration.ofMillis(150), 0.95, 0.1);
 * 
 * // Record skill execution
 * AgentExecutionMetrics.recordAgentSkillExecution(metricsService, "skill-456", "data-analysis", 
 *     Duration.ofMillis(200), 1024, true, null);
 * }</pre>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public final class AgentExecutionMetrics {

    /**
     * Private constructor to prevent instantiation.
     */
    private AgentExecutionMetrics() {
        // Utility class
    }

    /**
     * Record agent task execution metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param agentId the unique identifier of the agent
     * @param taskType the type of task being executed
     * @param success whether the task execution was successful
     * @param duration the duration of the task execution
     * @param decisionAccuracy the accuracy of agent decisions (0.0 to 1.0)
     * @param learningRate the learning rate applied during execution (0.0 to 1.0)
     */
    public static void recordAgentExecution(MetricsService metricsService, String agentId, String taskType,
            boolean success, Duration duration, double decisionAccuracy, double learningRate) {
        try {
            metricsService.recordOperation("agent", "execution")
                    .withSuccess(success)
                    .withDuration(duration.toNanos())
                    .withData("agentId", agentId)
                    .withData("taskType", taskType)
                    .withData("decisionAccuracy", decisionAccuracy)
                    .withData("learningRate", learningRate)
                    .record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting agent execution
            // Logger would be injected in a real implementation
        }
    }

    /**
     * Record agent skill execution metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param skillId the unique identifier of the skill
     * @param skillName the name of the skill being executed
     * @param executionTime the time taken to execute the skill
     * @param resultSize the size of the execution result in bytes
     * @param success whether the skill execution was successful
     * @param errorType the type of error if execution failed, null if successful
     */
    public static void recordAgentSkillExecution(MetricsService metricsService, String skillId, String skillName,
            Duration executionTime, long resultSize, boolean success, @Nullable String errorType) {
        try {
            var operation = metricsService.recordOperation("agent", "skill-execution")
                    .withSuccess(success)
                    .withDuration(executionTime.toNanos())
                    .withData("skillId", skillId)
                    .withData("skillName", skillName)
                    .withData("resultSize", resultSize);

            if (errorType != null) {
                operation.withData("errorType", errorType);
            }

            operation.record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting agent execution
        }
    }

    /**
     * Record agent task assignment metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param taskId the unique identifier of the task
     * @param agentId the unique identifier of the agent
     * @param assignmentTime the time taken to assign the task
     * @param priority the priority level of the task (1-10)
     * @param resourceAllocated the amount of resources allocated for the task
     */
    public static void recordAgentTaskAssignment(MetricsService metricsService, String taskId, String agentId,
            Duration assignmentTime, int priority, long resourceAllocated) {
        try {
            metricsService.recordOperation("agent", "task-assignment")
                    .withSuccess(true)
                    .withDuration(assignmentTime.toNanos())
                    .withData("taskId", taskId)
                    .withData("agentId", agentId)
                    .withData("priority", priority)
                    .withData("resourceAllocated", resourceAllocated)
                    .record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting agent execution
        }
    }

    /**
     * Record agent lifecycle event metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param agentId the unique identifier of the agent
     * @param lifecycleEvent the type of lifecycle event (start, stop, pause, resume, etc.)
     * @param eventTime the time when the event occurred
     * @param agentAge the age of the agent in milliseconds
     * @param state the current state of the agent
     */
    public static void recordAgentLifecycle(MetricsService metricsService, String agentId, String lifecycleEvent,
            Duration eventTime, long agentAge, String state) {
        try {
            metricsService.recordOperation("agent", "lifecycle")
                    .withSuccess(true)
                    .withDuration(eventTime.toNanos())
                    .withData("agentId", agentId)
                    .withData("lifecycleEvent", lifecycleEvent)
                    .withData("agentAge", agentAge)
                    .withData("state", state)
                    .record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting agent execution
        }
    }

    /**
     * Record agent performance metrics with additional context data.
     * 
     * @param metricsService the metrics service to record with
     * @param agentId the unique identifier of the agent
     * @param performanceMetric the type of performance metric
     * @param value the performance value
     * @param duration the measurement duration
     * @param context additional context data for the performance metric
     */
    public static void recordAgentPerformance(MetricsService metricsService, String agentId, String performanceMetric,
            double value, Duration duration, Map<String, Object> context) {
        try {
            var operation = metricsService.recordOperation("agent", "performance")
                    .withSuccess(true)
                    .withDuration(duration.toNanos())
                    .withData("agentId", agentId)
                    .withData("performanceMetric", performanceMetric)
                    .withData("value", value);

            // Add context data
            context.forEach(operation::withData);

            operation.record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting agent execution
        }
    }
}
