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
 * Static utility class for recording tool validation metrics.
 * 
 * <p>This class provides static methods for recording various tool validation operations
 * including filter validation, validation rules, and batch validation.
 * All methods follow the static utility pattern with MetricsService as the first parameter.
 * 
 * <p>Example usage:
 * <pre>{@code
 * // Record filter validation
 * ToolValidationMetrics.recordFilterValidation(metricsService, "filter-123", "input-validation", 
 *     true, Duration.ofMillis(10), "VALID");
 * 
 * // Record validation rule
 * ToolValidationMetrics.recordValidationRule(metricsService, "rule-456", "format-check", 
 *     true, Duration.ofMillis(5), 1024, 0);
 * }</pre>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public final class ToolValidationMetrics {

    /**
     * Private constructor to prevent instantiation.
     */
    private ToolValidationMetrics() {
        // Utility class
    }

    /**
     * Record filter validation metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param filterId the unique identifier of the filter
     * @param validationType the type of validation being performed
     * @param success whether the validation was successful
     * @param duration the duration of the validation process
     * @param validationResult the result of the validation (VALID, INVALID, WARNING, etc.)
     */
    public static void recordFilterValidation(MetricsService metricsService, String filterId, String validationType,
            boolean success, Duration duration, String validationResult) {
        try {
            metricsService.recordOperation("filter-validator", "validation")
                    .withSuccess(success)
                    .withDuration(duration.toNanos())
                    .withData("filterId", filterId)
                    .withData("validationType", validationType)
                    .withData("validationResult", validationResult)
                    .record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting validation
        }
    }

    /**
     * Record validation rule metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param ruleId the unique identifier of the validation rule
     * @param ruleType the type of validation rule
     * @param success whether the rule validation was successful
     * @param duration the duration of the rule validation
     * @param inputSize the size of the input data being validated
     * @param errorCount the number of validation errors found
     */
    public static void recordValidationRule(MetricsService metricsService, String ruleId, String ruleType,
            boolean success, Duration duration, long inputSize, int errorCount) {
        try {
            metricsService.recordOperation("validation-rule", "effectiveness")
                    .withSuccess(success)
                    .withDuration(duration.toNanos())
                    .withData("ruleId", ruleId)
                    .withData("ruleType", ruleType)
                    .withData("inputSize", inputSize)
                    .withData("errorCount", errorCount)
                    .record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting validation
        }
    }

    /**
     * Record validation batch metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param batchId the unique identifier of the validation batch
     * @param ruleCount the number of rules in the batch
     * @param success whether the batch validation was successful
     * @param duration the duration of the batch validation
     * @param passedCount the number of items that passed validation
     * @param failedCount the number of items that failed validation
     */
    public static void recordValidationBatch(MetricsService metricsService, String batchId, int ruleCount,
            boolean success, Duration duration, int passedCount, int failedCount) {
        try {
            metricsService.recordOperation("validation-rule", "batch")
                    .withSuccess(success)
                    .withDuration(duration.toNanos())
                    .withData("batchId", batchId)
                    .withData("ruleCount", ruleCount)
                    .withData("passedCount", passedCount)
                    .withData("failedCount", failedCount)
                    .record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting validation
        }
    }

    /**
     * Record validation performance metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param validationType the type of validation being performed
     * @param performanceMetric the specific performance metric being measured
     * @param value the performance value
     * @param duration the measurement duration
     * @param context additional context data for the performance metric
     */
    public static void recordValidationPerformance(MetricsService metricsService, String validationType,
            String performanceMetric, double value, Duration duration, Map<String, Object> context) {
        try {
            var operation = metricsService.recordOperation("validation-rule", "performance")
                    .withSuccess(true)
                    .withDuration(duration.toNanos())
                    .withData("validationType", validationType)
                    .withData("performanceMetric", performanceMetric)
                    .withData("value", value);

            // Add context data
            context.forEach(operation::withData);

            operation.record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting validation
        }
    }

    /**
     * Record validation error metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param validationType the type of validation that failed
     * @param errorType the type of error that occurred
     * @param errorMessage the error message
     * @param duration the time taken before the error occurred
     * @param context additional context data about the error
     */
    public static void recordValidationError(MetricsService metricsService, String validationType, String errorType,
            @Nullable String errorMessage, Duration duration, Map<String, Object> context) {
        try {
            var operation = metricsService.recordOperation("validation-rule", "error")
                    .withSuccess(false)
                    .withDuration(duration.toNanos())
                    .withData("validationType", validationType)
                    .withData("errorType", errorType);

            if (errorMessage != null) {
                operation.withData("errorMessage", errorMessage);
            }

            // Add context data
            context.forEach(operation::withData);

            operation.record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting validation
        }
    }

    /**
     * Record validation optimization metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param ruleId the unique identifier of the rule being optimized
     * @param optimizationType the type of optimization performed
     * @param beforePerformance the performance before optimization
     * @param afterPerformance the performance after optimization
     * @param optimizationTime the time taken to perform the optimization
     */
    public static void recordValidationOptimization(MetricsService metricsService, String ruleId,
            String optimizationType, double beforePerformance, double afterPerformance, Duration optimizationTime) {
        try {
            metricsService.recordOperation("validation-rule", "optimization")
                    .withSuccess(true)
                    .withDuration(optimizationTime.toNanos())
                    .withData("ruleId", ruleId)
                    .withData("optimizationType", optimizationType)
                    .withData("beforePerformance", beforePerformance)
                    .withData("afterPerformance", afterPerformance)
                    .withData("improvement", afterPerformance - beforePerformance)
                    .record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting validation optimization
        }
    }
}
