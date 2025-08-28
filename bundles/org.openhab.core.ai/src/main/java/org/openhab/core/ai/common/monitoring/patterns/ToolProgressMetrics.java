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
import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.MetricsService;

/**
 * Static utility class for recording tool progress metrics.
 * 
 * <p>This class provides static methods for recording various tool progress operations
 * including progress tracking, operation lifecycle, and progress updates.
 * All methods follow the static utility pattern with MetricsService as the first parameter.
 * 
 * <p>Example usage:
 * <pre>{@code
 * // Record progress tracking
 * ToolProgressMetrics.recordProgressTracking(metricsService, "op-123", "start", 
 *     true, Duration.ofMillis(0), Map.of("totalSteps", 10));
 * 
 * // Record operation start
 * ToolProgressMetrics.recordOperationStart(metricsService, "op-456", "data-processing", 
 *     Instant.now(), 5, Map.of("inputSize", 1024));
 * }</pre>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public final class ToolProgressMetrics {

    /**
     * Private constructor to prevent instantiation.
     */
    private ToolProgressMetrics() {
        // Utility class
    }

    /**
     * Record progress tracking metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param operationId the unique identifier of the operation
     * @param operation the type of progress operation (start, update, complete, cancel, etc.)
     * @param success whether the progress operation was successful
     * @param duration the duration of the progress operation
     * @param progressData additional progress data as key-value pairs
     */
    public static void recordProgressTracking(MetricsService metricsService, String operationId, String operation,
            boolean success, Duration duration, Map<String, Object> progressData) {
        try {
            var operationBuilder = metricsService.recordOperation("progress_tracking", operation)
                    .withSuccess(success)
                    .withDuration(duration.toNanos())
                    .withData("operationId", operationId);

            // Add progress data
            progressData.forEach(operationBuilder::withData);

            operationBuilder.record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting progress tracking
        }
    }

    /**
     * Record operation start metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param operationId the unique identifier of the operation
     * @param operationType the type of operation being started
     * @param startTime the time when the operation started
     * @param totalSteps the total number of steps in the operation
     * @param context additional context data for the operation
     */
    public static void recordOperationStart(MetricsService metricsService, String operationId, String operationType,
            Instant startTime, int totalSteps, Map<String, Object> context) {
        try {
            var operationBuilder = metricsService.recordOperation("progress_tracking", "start")
                    .withSuccess(true)
                    .withDuration(Duration.ofNanos(0L).toNanos())
                    .withData("operationId", operationId)
                    .withData("operationType", operationType)
                    .withData("startTime", startTime.toEpochMilli())
                    .withData("totalSteps", totalSteps);

            // Add context data
            context.forEach(operationBuilder::withData);

            operationBuilder.record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting operation tracking
        }
    }

    /**
     * Record operation completion metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param operationId the unique identifier of the operation
     * @param operationType the type of operation that completed
     * @param completionTime the time when the operation completed
     * @param success whether the operation completed successfully
     * @param resultSize the size of the operation result in bytes
     */
    public static void recordOperationComplete(MetricsService metricsService, String operationId, String operationType,
            Instant completionTime, boolean success, long resultSize) {
        try {
            metricsService.recordOperation("progress_tracking", "complete")
                    .withSuccess(success)
                    .withDuration(Duration.ofNanos(0L).toNanos())
                    .withData("operationId", operationId)
                    .withData("operationType", operationType)
                    .withData("completionTime", completionTime.toEpochMilli())
                    .withData("resultSize", resultSize)
                    .record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting operation tracking
        }
    }

    /**
     * Record operation update metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param operationId the unique identifier of the operation
     * @param currentStep the current step number in the operation
     * @param totalSteps the total number of steps in the operation
     * @param progress the progress percentage (0.0 to 1.0)
     * @param message the progress message or status update
     */
    public static void recordOperationUpdate(MetricsService metricsService, String operationId, int currentStep,
            int totalSteps, double progress, @Nullable String message) {
        try {
            var operationBuilder = metricsService.recordOperation("progress_tracking", "update")
                    .withSuccess(true)
                    .withDuration(Duration.ofNanos(0L).toNanos())
                    .withData("operationId", operationId)
                    .withData("currentStep", currentStep)
                    .withData("totalSteps", totalSteps)
                    .withData("progress", progress);

            if (message != null) {
                operationBuilder.withData("message", message);
            }

            operationBuilder.record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting operation tracking
        }
    }

    /**
     * Record operation cancellation metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param operationId the unique identifier of the operation
     * @param operationType the type of operation that was cancelled
     * @param cancellationTime the time when the operation was cancelled
     * @param progressAtCancellation the progress percentage when cancelled (0.0 to 1.0)
     * @param reason the reason for cancellation
     */
    public static void recordOperationCancellation(MetricsService metricsService, String operationId,
            String operationType, Instant cancellationTime, double progressAtCancellation, @Nullable String reason) {
        try {
            var operationBuilder = metricsService.recordOperation("progress_tracking", "cancel")
                    .withSuccess(true)
                    .withDuration(Duration.ofNanos(0L).toNanos())
                    .withData("operationId", operationId)
                    .withData("operationType", operationType)
                    .withData("cancellationTime", cancellationTime.toEpochMilli())
                    .withData("progressAtCancellation", progressAtCancellation);

            if (reason != null) {
                operationBuilder.withData("reason", reason);
            }

            operationBuilder.record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting operation tracking
        }
    }

    /**
     * Record operation error metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param operationId the unique identifier of the operation
     * @param operationType the type of operation that failed
     * @param errorType the type of error that occurred
     * @param errorMessage the error message
     * @param progressAtError the progress percentage when error occurred (0.0 to 1.0)
     * @param context additional context data about the error
     */
    public static void recordOperationError(MetricsService metricsService, String operationId, String operationType,
            String errorType, @Nullable String errorMessage, double progressAtError, Map<String, Object> context) {
        try {
            var operationBuilder = metricsService.recordOperation("progress_tracking", "error")
                    .withSuccess(false)
                    .withDuration(Duration.ofNanos(0L).toNanos())
                    .withData("operationId", operationId)
                    .withData("operationType", operationType)
                    .withData("errorType", errorType)
                    .withData("progressAtError", progressAtError);

            if (errorMessage != null) {
                operationBuilder.withData("errorMessage", errorMessage);
            }

            // Add context data
            context.forEach(operationBuilder::withData);

            operationBuilder.record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting operation tracking
        }
    }
}
