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
 * Static utility class for recording model client metrics.
 * 
 * <p>This class provides static methods for recording various model client operations
 * including model completion, requests, responses, and error handling.
 * All methods follow the static utility pattern with MetricsService as the first parameter.
 * 
 * <p>Example usage:
 * <pre>{@code
 * // Record model completion
 * ModelClientMetrics.recordModelCompletion(metricsService, "gpt-4", "text-completion", 
 *     true, Duration.ofSeconds(2), 1500, 0.05);
 * 
 * // Record model request
 * ModelClientMetrics.recordModelRequest(metricsService, "claude-3", "chat-request", 
 *     true, Duration.ofMillis(1500), 1024, "req-123");
 * }</pre>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public final class ModelClientMetrics {

    /**
     * Private constructor to prevent instantiation.
     */
    private ModelClientMetrics() {
        // Utility class
    }

    /**
     * Record model completion metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param modelId the unique identifier of the model
     * @param completionType the type of completion (text-completion, chat-completion, code-completion, etc.)
     * @param success whether the completion was successful
     * @param duration the duration of the completion process
     * @param tokenCount the number of tokens used in the completion
     * @param cost the cost of the completion operation
     */
    public static void recordModelCompletion(MetricsService metricsService, String modelId, String completionType,
            boolean success, Duration duration, int tokenCount, double cost) {
        try {
            metricsService.recordOperation("model", "completion")
                    .withSuccess(success)
                    .withDuration(duration.toNanos())
                    .withData("modelId", modelId)
                    .withData("completionType", completionType)
                    .withData("tokenCount", tokenCount)
                    .withData("cost", cost)
                    .record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting model operations
        }
    }

    /**
     * Record model request metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param modelId the unique identifier of the model
     * @param requestType the type of request (chat, completion, embedding, etc.)
     * @param success whether the request was successful
     * @param duration the duration of the request processing
     * @param inputSize the size of the input data in bytes
     * @param requestId the unique identifier of the request
     */
    public static void recordModelRequest(MetricsService metricsService, String modelId, String requestType,
            boolean success, Duration duration, long inputSize, @Nullable String requestId) {
        try {
            var operation = metricsService.recordOperation("model", "request")
                    .withSuccess(success)
                    .withDuration(duration.toNanos())
                    .withData("modelId", modelId)
                    .withData("requestType", requestType)
                    .withData("inputSize", inputSize);

            if (requestId != null) {
                operation.withData("requestId", requestId);
            }

            operation.record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting model operations
        }
    }

    /**
     * Record model response metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param modelId the unique identifier of the model
     * @param responseType the type of response (text, json, embedding, etc.)
     * @param success whether the response was successful
     * @param duration the duration of the response generation
     * @param outputSize the size of the output data in bytes
     * @param responseId the unique identifier of the response
     */
    public static void recordModelResponse(MetricsService metricsService, String modelId, String responseType,
            boolean success, Duration duration, long outputSize, @Nullable String responseId) {
        try {
            var operation = metricsService.recordOperation("model", "response")
                    .withSuccess(success)
                    .withDuration(duration.toNanos())
                    .withData("modelId", modelId)
                    .withData("responseType", responseType)
                    .withData("outputSize", outputSize);

            if (responseId != null) {
                operation.withData("responseId", responseId);
            }

            operation.record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting model operations
        }
    }

    /**
     * Record model error metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param modelId the unique identifier of the model
     * @param errorType the type of error (timeout, rate-limit, invalid-request, etc.)
     * @param errorCode the error code returned by the model service
     * @param duration the duration before the error occurred
     * @param errorMessage the error message
     * @param retryCount the number of retries attempted
     */
    public static void recordModelError(MetricsService metricsService, String modelId, String errorType,
            @Nullable String errorCode, Duration duration, @Nullable String errorMessage, int retryCount) {
        try {
            var operation = metricsService.recordOperation("model", "error")
                    .withSuccess(false)
                    .withDuration(duration.toNanos())
                    .withData("modelId", modelId)
                    .withData("errorType", errorType)
                    .withData("retryCount", retryCount);

            if (errorCode != null) {
                operation.withData("errorCode", errorCode);
            }

            if (errorMessage != null) {
                operation.withData("errorMessage", errorMessage);
            }

            operation.record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting model operations
        }
    }

    /**
     * Record model performance metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param modelId the unique identifier of the model
     * @param performanceMetric the specific performance metric being measured
     * @param value the performance value
     * @param duration the measurement duration
     * @param context additional context data for the performance metric
     */
    public static void recordModelPerformance(MetricsService metricsService, String modelId, String performanceMetric,
            double value, Duration duration, Map<String, Object> context) {
        try {
            var operation = metricsService.recordOperation("model", "performance")
                    .withSuccess(true)
                    .withDuration(duration.toNanos())
                    .withData("modelId", modelId)
                    .withData("performanceMetric", performanceMetric)
                    .withData("value", value);

            // Add context data
            context.forEach(operation::withData);

            operation.record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting model operations
        }
    }

    /**
     * Record model usage metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param modelId the unique identifier of the model
     * @param usageType the type of usage (inference, training, fine-tuning, etc.)
     * @param success whether the usage was successful
     * @param duration the duration of the usage
     * @param usageCount the number of usage operations
     * @param userId the identifier of the user making the request
     */
    public static void recordModelUsage(MetricsService metricsService, String modelId, String usageType,
            boolean success, Duration duration, int usageCount, @Nullable String userId) {
        try {
            var operation = metricsService.recordOperation("model", "usage")
                    .withSuccess(success)
                    .withDuration(duration.toNanos())
                    .withData("modelId", modelId)
                    .withData("usageType", usageType)
                    .withData("usageCount", usageCount);

            if (userId != null) {
                operation.withData("userId", userId);
            }

            operation.record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting model operations
        }
    }
}
