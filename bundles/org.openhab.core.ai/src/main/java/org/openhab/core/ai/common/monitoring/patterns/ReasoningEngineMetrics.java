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
 * Static utility class for recording reasoning engine metrics.
 * 
 * <p>This class provides static methods for recording various reasoning engine operations
 * including agent reasoning, reasoning steps, cache operations, and analysis.
 * All methods follow the static utility pattern with MetricsService as the first parameter.
 * 
 * <p>Example usage:
 * <pre>{@code
 * // Record agent reasoning
 * ReasoningEngineMetrics.recordAgentReasoning(metricsService, "agent-123", "decision-making", 
 *     true, Duration.ofMillis(200), 5, 0.95);
 * 
 * // Record reasoning step
 * ReasoningEngineMetrics.recordReasoningStep(metricsService, "step-456", "analysis", 
 *     true, Duration.ofMillis(50), 0.8, "context-789");
 * }</pre>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public final class ReasoningEngineMetrics {

    /**
     * Private constructor to prevent instantiation.
     */
    private ReasoningEngineMetrics() {
        // Utility class
    }

    /**
     * Record agent reasoning metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param agentId the unique identifier of the agent
     * @param reasoningType the type of reasoning (decision-making, problem-solving, planning, etc.)
     * @param success whether the reasoning was successful
     * @param duration the duration of the reasoning process
     * @param reasoningSteps the number of reasoning steps performed
     * @param confidence the confidence level of the reasoning result (0.0 to 1.0)
     */
    public static void recordAgentReasoning(MetricsService metricsService, String agentId, String reasoningType,
            boolean success, Duration duration, int reasoningSteps, double confidence) {
        try {
            metricsService.recordOperation("reasoning-engine", "agent-reasoning")
                    .withSuccess(success)
                    .withDuration(duration.toNanos())
                    .withData("agentId", agentId)
                    .withData("reasoningType", reasoningType)
                    .withData("reasoningSteps", reasoningSteps)
                    .withData("confidence", confidence)
                    .withData("averageStepTime", duration.toNanos() / (double) reasoningSteps)
                    .record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting reasoning operations
        }
    }

    /**
     * Record reasoning step metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param stepId the unique identifier of the reasoning step
     * @param stepType the type of reasoning step (analysis, synthesis, evaluation, etc.)
     * @param success whether the reasoning step was successful
     * @param duration the duration of the reasoning step
     * @param stepConfidence the confidence level of the step result (0.0 to 1.0)
     * @param contextId the identifier of the reasoning context
     */
    public static void recordReasoningStep(MetricsService metricsService, String stepId, String stepType,
            boolean success, Duration duration, double stepConfidence, @Nullable String contextId) {
        try {
            var operation = metricsService.recordOperation("reasoning-engine", "reasoning-step")
                    .withSuccess(success)
                    .withDuration(duration.toNanos())
                    .withData("stepId", stepId)
                    .withData("stepType", stepType)
                    .withData("stepConfidence", stepConfidence);

            if (contextId != null) {
                operation.withData("contextId", contextId);
            }

            operation.record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting reasoning operations
        }
    }

    /**
     * Record reasoning cache operations.
     * 
     * @param metricsService the metrics service to record with
     * @param cacheOperation the type of cache operation (get, put, evict, clear, etc.)
     * @param success whether the cache operation was successful
     * @param duration the duration of the cache operation
     * @param cacheSize the current size of the cache
     * @param hitRate the cache hit rate (0.0 to 1.0)
     * @param cacheKey the key involved in the cache operation
     */
    public static void recordReasoningCacheOperation(MetricsService metricsService, String cacheOperation, boolean success,
            Duration duration, int cacheSize, double hitRate, @Nullable String cacheKey) {
        try {
            var operation = metricsService.recordOperation("reasoning-engine", "cache-operation")
                    .withSuccess(success)
                    .withDuration(duration.toNanos())
                    .withData("cacheOperation", cacheOperation)
                    .withData("cacheSize", cacheSize)
                    .withData("hitRate", hitRate);

            if (cacheKey != null) {
                operation.withData("cacheKey", cacheKey);
            }

            operation.record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting reasoning operations
        }
    }

    /**
     * Record reasoning analysis metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param analysisType the type of analysis (pattern-recognition, trend-analysis, anomaly-detection, etc.)
     * @param success whether the analysis was successful
     * @param duration the duration of the analysis
     * @param analysisAccuracy the accuracy of the analysis (0.0 to 1.0)
     * @param dataPoints the number of data points analyzed
     * @param context additional context data for the analysis
     */
    public static void recordReasoningAnalysis(MetricsService metricsService, String analysisType, boolean success,
            Duration duration, double analysisAccuracy, int dataPoints, Map<String, Object> context) {
        try {
            var operation = metricsService.recordOperation("reasoning-engine", "analysis")
                    .withSuccess(success)
                    .withDuration(duration.toNanos())
                    .withData("analysisType", analysisType)
                    .withData("analysisAccuracy", analysisAccuracy)
                    .withData("dataPoints", dataPoints)
                    .withData("analysisRate", dataPoints / (duration.toSeconds() + 1.0));

            // Add context data
            context.forEach(operation::withData);

            operation.record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting reasoning operations
        }
    }

    /**
     * Record reasoning optimization metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param optimizationType the type of optimization (algorithm-optimization, cache-optimization, etc.)
     * @param success whether the optimization was successful
     * @param duration the duration of the optimization process
     * @param beforePerformance the performance before optimization
     * @param afterPerformance the performance after optimization
     * @param context additional context data about the optimization
     */
    public static void recordReasoningOptimization(MetricsService metricsService, String optimizationType, boolean success,
            Duration duration, double beforePerformance, double afterPerformance, Map<String, Object> context) {
        try {
            var operation = metricsService.recordOperation("reasoning-engine", "optimization")
                    .withSuccess(success)
                    .withDuration(duration.toNanos())
                    .withData("optimizationType", optimizationType)
                    .withData("beforePerformance", beforePerformance)
                    .withData("afterPerformance", afterPerformance)
                    .withData("improvement", afterPerformance - beforePerformance)
                    .withData("improvementPercentage", ((afterPerformance - beforePerformance) / beforePerformance) * 100.0);

            // Add context data
            context.forEach(operation::withData);

            operation.record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting reasoning operations
        }
    }

    /**
     * Record reasoning error metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param errorType the type of reasoning error (logic-error, timeout, resource-exhaustion, etc.)
     * @param errorMessage the error message
     * @param duration the time taken before the error occurred
     * @param reasoningContext the context in which the error occurred
     * @param context additional context data about the error
     */
    public static void recordReasoningError(MetricsService metricsService, String errorType, @Nullable String errorMessage,
            Duration duration, @Nullable String reasoningContext, Map<String, Object> context) {
        try {
            var operation = metricsService.recordOperation("reasoning-engine", "error")
                    .withSuccess(false)
                    .withDuration(duration.toNanos())
                    .withData("errorType", errorType);

            if (errorMessage != null) {
                operation.withData("errorMessage", errorMessage);
            }

            if (reasoningContext != null) {
                operation.withData("reasoningContext", reasoningContext);
            }

            // Add context data
            context.forEach(operation::withData);

            operation.record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting reasoning operations
        }
    }
}
