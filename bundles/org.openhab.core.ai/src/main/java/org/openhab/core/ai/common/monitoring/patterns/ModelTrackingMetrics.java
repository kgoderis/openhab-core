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
 * Static utility class for recording model tracking metrics.
 * 
 * <p>This class provides static methods for recording various model tracking operations
 * including model usage, performance, and resource usage.
 * All methods follow the static utility pattern with MetricsService as the first parameter.
 * 
 * <p>Example usage:
 * <pre>{@code
 * // Record model usage
 * ModelTrackingMetrics.recordModelUsage(metricsService, "gpt-4", "inference", 
 *     true, Duration.ofSeconds(1), 5, "user-123");
 * 
 * // Record model performance
 * ModelTrackingMetrics.recordModelPerformance(metricsService, "claude-3", "latency", 
 *     150.5, Duration.ofMinutes(1), Map.of("context", "chat"));
 * }</pre>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public final class ModelTrackingMetrics {

    /**
     * Private constructor to prevent instantiation.
     */
    private ModelTrackingMetrics() {
        // Utility class
    }

    /**
     * Record model usage tracking metrics.
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
            // Log error but don't throw to avoid disrupting model tracking
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
            // Log error but don't throw to avoid disrupting model tracking
        }
    }

    /**
     * Record model resource usage metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param modelId the unique identifier of the model
     * @param resourceType the type of resource being used (CPU, memory, GPU, network, etc.)
     * @param usage the amount of resource used
     * @param duration the duration of resource usage
     * @param resourceLimit the limit of the resource
     */
    public static void recordModelResourceUsage(MetricsService metricsService, String modelId, String resourceType,
            double usage, Duration duration, @Nullable Double resourceLimit) {
        try {
            var operation = metricsService.recordOperation("model", "resource-usage")
                    .withSuccess(true)
                    .withDuration(duration.toNanos())
                    .withData("modelId", modelId)
                    .withData("resourceType", resourceType)
                    .withData("usage", usage);

            if (resourceLimit != null) {
                operation.withData("resourceLimit", resourceLimit);
                operation.withData("usagePercentage", (usage / resourceLimit) * 100.0);
            }

            operation.record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting model tracking
        }
    }

    /**
     * Record model availability metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param modelId the unique identifier of the model
     * @param availabilityStatus the availability status (available, unavailable, degraded, etc.)
     * @param duration the duration of the availability check
     * @param responseTime the response time of the availability check
     * @param context additional context data about the availability
     */
    public static void recordModelAvailability(MetricsService metricsService, String modelId, String availabilityStatus,
            Duration duration, Duration responseTime, Map<String, Object> context) {
        try {
            var operation = metricsService.recordOperation("model", "availability")
                    .withSuccess("available".equals(availabilityStatus))
                    .withDuration(duration.toNanos())
                    .withData("modelId", modelId)
                    .withData("availabilityStatus", availabilityStatus)
                    .withData("responseTime", responseTime.toNanos());

            // Add context data
            context.forEach(operation::withData);

            operation.record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting model tracking
        }
    }

    /**
     * Record model quality metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param modelId the unique identifier of the model
     * @param qualityMetric the type of quality metric (accuracy, precision, recall, etc.)
     * @param value the quality value
     * @param duration the measurement duration
     * @param dataset the dataset used for quality measurement
     * @param context additional context data for the quality metric
     */
    public static void recordModelQuality(MetricsService metricsService, String modelId, String qualityMetric,
            double value, Duration duration, @Nullable String dataset, Map<String, Object> context) {
        try {
            var operation = metricsService.recordOperation("model", "quality")
                    .withSuccess(true)
                    .withDuration(duration.toNanos())
                    .withData("modelId", modelId)
                    .withData("qualityMetric", qualityMetric)
                    .withData("value", value);

            if (dataset != null) {
                operation.withData("dataset", dataset);
            }

            // Add context data
            context.forEach(operation::withData);

            operation.record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting model tracking
        }
    }

    /**
     * Record model cost tracking metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param modelId the unique identifier of the model
     * @param costType the type of cost (inference, training, storage, etc.)
     * @param cost the cost amount
     * @param duration the duration for which the cost was incurred
     * @param currency the currency of the cost
     * @param context additional context data about the cost
     */
    public static void recordModelCost(MetricsService metricsService, String modelId, String costType, double cost,
            Duration duration, @Nullable String currency, Map<String, Object> context) {
        try {
            var operation = metricsService.recordOperation("model", "cost")
                    .withSuccess(true)
                    .withDuration(duration.toNanos())
                    .withData("modelId", modelId)
                    .withData("costType", costType)
                    .withData("cost", cost);

            if (currency != null) {
                operation.withData("currency", currency);
            }

            // Add context data
            context.forEach(operation::withData);

            operation.record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting model tracking
        }
    }
}
