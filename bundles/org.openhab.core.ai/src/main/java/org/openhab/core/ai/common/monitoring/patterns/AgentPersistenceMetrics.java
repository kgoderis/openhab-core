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
 * Static utility class for recording agent persistence metrics.
 * 
 * <p>This class provides static methods for recording various agent persistence operations
 * including task management and OpenHAB integration operations.
 * All methods follow the static utility pattern with MetricsService as the first parameter.
 * 
 * <p>Example usage:
 * <pre>{@code
 * // Record agent task management
 * AgentPersistenceMetrics.recordAgentTaskManagement(metricsService, "task-123", "create", 
 *     true, Duration.ofMillis(100), 5);
 * 
 * // Record OpenHAB integration
 * AgentPersistenceMetrics.recordAgentOpenHABIntegration(metricsService, "item-update", 
 *     true, Duration.ofMillis(50), 10);
 * }</pre>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public final class AgentPersistenceMetrics {

    /**
     * Private constructor to prevent instantiation.
     */
    private AgentPersistenceMetrics() {
        // Utility class
    }

    /**
     * Record agent task management metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param taskId the unique identifier of the task
     * @param operation the type of task management operation (create, update, delete, etc.)
     * @param success whether the operation was successful
     * @param duration the duration of the operation
     * @param taskCount the number of tasks involved in the operation
     */
    public static void recordAgentTaskManagement(MetricsService metricsService, String taskId, String operation,
            boolean success, Duration duration, int taskCount) {
        try {
            metricsService.recordOperation("agent-persistence", "task-management")
                    .withSuccess(success)
                    .withDuration(duration.toNanos())
                    .withData("taskId", taskId)
                    .withData("operation", operation)
                    .withData("taskCount", taskCount)
                    .record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting agent persistence
        }
    }

    /**
     * Record agent OpenHAB integration metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param integrationType the type of OpenHAB integration (item-update, thing-config, rule-execution, etc.)
     * @param success whether the integration operation was successful
     * @param duration the duration of the integration operation
     * @param itemCount the number of OpenHAB items involved in the operation
     */
    public static void recordAgentOpenHABIntegration(MetricsService metricsService, String integrationType,
            boolean success, Duration duration, int itemCount) {
        try {
            metricsService.recordOperation("agent-persistence", "openhab-integration")
                    .withSuccess(success)
                    .withDuration(duration.toNanos())
                    .withData("integrationType", integrationType)
                    .withData("itemCount", itemCount)
                    .record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting agent persistence
        }
    }

    /**
     * Record agent state persistence metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param agentId the unique identifier of the agent
     * @param stateType the type of state being persisted (memory, context, configuration, etc.)
     * @param success whether the state persistence was successful
     * @param duration the duration of the persistence operation
     * @param stateSize the size of the state data in bytes
     */
    public static void recordAgentStatePersistence(MetricsService metricsService, String agentId, String stateType,
            boolean success, Duration duration, long stateSize) {
        try {
            metricsService.recordOperation("agent-persistence", "state-persistence")
                    .withSuccess(success)
                    .withDuration(duration.toNanos())
                    .withData("agentId", agentId)
                    .withData("stateType", stateType)
                    .withData("stateSize", stateSize)
                    .record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting agent persistence
        }
    }

    /**
     * Record agent data synchronization metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param syncType the type of synchronization (full, incremental, selective)
     * @param success whether the synchronization was successful
     * @param duration the duration of the synchronization operation
     * @param recordsSynced the number of records synchronized
     * @param syncDirection the direction of synchronization (inbound, outbound, bidirectional)
     */
    public static void recordAgentDataSynchronization(MetricsService metricsService, String syncType, boolean success,
            Duration duration, int recordsSynced, String syncDirection) {
        try {
            metricsService.recordOperation("agent-persistence", "data-synchronization")
                    .withSuccess(success)
                    .withDuration(duration.toNanos())
                    .withData("syncType", syncType)
                    .withData("recordsSynced", recordsSynced)
                    .withData("syncDirection", syncDirection)
                    .record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting agent persistence
        }
    }

    /**
     * Record agent persistence performance metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param persistenceType the type of persistence operation
     * @param performanceMetric the specific performance metric being measured
     * @param value the performance value
     * @param duration the measurement duration
     * @param context additional context data for the performance metric
     */
    public static void recordAgentPersistencePerformance(MetricsService metricsService, String persistenceType,
            String performanceMetric, double value, Duration duration, Map<String, Object> context) {
        try {
            var operation = metricsService.recordOperation("agent-persistence", "performance")
                    .withSuccess(true)
                    .withDuration(duration.toNanos())
                    .withData("persistenceType", persistenceType)
                    .withData("performanceMetric", performanceMetric)
                    .withData("value", value);

            // Add context data
            context.forEach(operation::withData);

            operation.record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting agent persistence
        }
    }

    /**
     * Record agent persistence error metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param persistenceType the type of persistence operation that failed
     * @param errorType the type of error that occurred
     * @param errorMessage the error message
     * @param duration the time taken before the error occurred
     * @param context additional context data about the error
     */
    public static void recordAgentPersistenceError(MetricsService metricsService, String persistenceType,
            String errorType, @Nullable String errorMessage, Duration duration, Map<String, Object> context) {
        try {
            var operation = metricsService.recordOperation("agent-persistence", "error")
                    .withSuccess(false)
                    .withDuration(duration.toNanos())
                    .withData("persistenceType", persistenceType)
                    .withData("errorType", errorType);

            if (errorMessage != null) {
                operation.withData("errorMessage", errorMessage);
            }

            // Add context data
            context.forEach(operation::withData);

            operation.record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting agent persistence
        }
    }
}
