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
 * Static utility class for recording system health metrics.
 * 
 * <p>This class provides static methods for recording various system health operations
 * including system health checks, provider health, service health, and thresholds.
 * All methods follow the static utility pattern with MetricsService as the first parameter.
 * 
 * <p>Example usage:
 * <pre>{@code
 * // Record system health check
 * SystemHealthMetrics.recordSystemHealthCheck(metricsService, "memory-check", 
 *     true, Duration.ofMillis(50), 0.85, "Memory usage normal");
 * 
 * // Record provider health
 * SystemHealthMetrics.recordProviderHealth(metricsService, "provider-123", 
 *     "healthy", Duration.ofSeconds(1), Map.of("responseTime", 100), "api");
 * }</pre>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public final class SystemHealthMetrics {

    /**
     * Private constructor to prevent instantiation.
     */
    private SystemHealthMetrics() {
        // Utility class
    }

    /**
     * Record system health check metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param checkType the type of health check (memory-check, cpu-check, disk-check, etc.)
     * @param success whether the health check was successful
     * @param duration the duration of the health check
     * @param healthScore the health score (0.0 to 1.0)
     * @param checkDetails additional details about the health check
     */
    public static void recordSystemHealthCheck(MetricsService metricsService, String checkType, boolean success,
            Duration duration, double healthScore, @Nullable String checkDetails) {
        try {
            var operation = metricsService.recordOperation("system-health-monitor", "health-check")
                    .withSuccess(success)
                    .withDuration(duration.toNanos())
                    .withData("checkType", checkType)
                    .withData("healthScore", healthScore);

            if (checkDetails != null) {
                operation.withData("checkDetails", checkDetails);
            }

            operation.record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting health monitoring
        }
    }

    /**
     * Record provider health metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param providerId the unique identifier of the provider
     * @param healthStatus the health status (healthy, unhealthy, degraded, unknown)
     * @param duration the duration of the health check
     * @param healthMetrics additional health metrics
     * @param providerType the type of provider (api, database, service, etc.)
     */
    public static void recordProviderHealth(MetricsService metricsService, String providerId, String healthStatus,
            Duration duration, Map<String, Object> healthMetrics, String providerType) {
        try {
            var operation = metricsService.recordOperation("system-health-monitor", "provider-health")
                    .withSuccess("healthy".equals(healthStatus))
                    .withDuration(duration.toNanos())
                    .withData("providerId", providerId)
                    .withData("healthStatus", healthStatus)
                    .withData("providerType", providerType);

            // Add health metrics
            healthMetrics.forEach(operation::withData);

            operation.record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting health monitoring
        }
    }

    /**
     * Record service health metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param serviceId the unique identifier of the service
     * @param healthStatus the health status (healthy, unhealthy, degraded, unknown)
     * @param duration the duration of the health check
     * @param serviceMetrics additional service metrics
     * @param serviceType the type of service (web, api, database, cache, etc.)
     */
    public static void recordServiceHealth(MetricsService metricsService, String serviceId, String healthStatus,
            Duration duration, Map<String, Object> serviceMetrics, String serviceType) {
        try {
            var operation = metricsService.recordOperation("system-health-monitor", "service-health")
                    .withSuccess("healthy".equals(healthStatus))
                    .withDuration(duration.toNanos())
                    .withData("serviceId", serviceId)
                    .withData("healthStatus", healthStatus)
                    .withData("serviceType", serviceType);

            // Add service metrics
            serviceMetrics.forEach(operation::withData);

            operation.record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting health monitoring
        }
    }

    /**
     * Record health threshold metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param thresholdType the type of threshold (cpu-usage, memory-usage, disk-usage, etc.)
     * @param thresholdValue the threshold value
     * @param currentValue the current value
     * @param duration the duration of the threshold check
     * @param alertLevel the alert level (info, warning, critical, emergency)
     */
    public static void recordHealthThreshold(MetricsService metricsService, String thresholdType, double thresholdValue,
            double currentValue, Duration duration, String alertLevel) {
        try {
            boolean thresholdExceeded = currentValue > thresholdValue;
            
            metricsService.recordOperation("system-health-monitor", "threshold")
                    .withSuccess(!thresholdExceeded)
                    .withDuration(duration.toNanos())
                    .withData("thresholdType", thresholdType)
                    .withData("thresholdValue", thresholdValue)
                    .withData("currentValue", currentValue)
                    .withData("alertLevel", alertLevel)
                    .withData("thresholdExceeded", thresholdExceeded)
                    .withData("excessAmount", Math.max(0, currentValue - thresholdValue))
                    .record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting health monitoring
        }
    }

    /**
     * Record health alert metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param alertType the type of health alert (threshold-exceeded, service-down, etc.)
     * @param severity the severity of the alert (low, medium, high, critical)
     * @param duration the duration of the alert condition
     * @param alertMessage the alert message
     * @param context additional context data about the alert
     */
    public static void recordHealthAlert(MetricsService metricsService, String alertType, String severity,
            Duration duration, @Nullable String alertMessage, Map<String, Object> context) {
        try {
            var operation = metricsService.recordOperation("system-health-monitor", "alert")
                    .withSuccess(false) // Alerts are generally considered failures
                    .withDuration(duration.toNanos())
                    .withData("alertType", alertType)
                    .withData("severity", severity);

            if (alertMessage != null) {
                operation.withData("alertMessage", alertMessage);
            }

            // Add context data
            context.forEach(operation::withData);

            operation.record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting health monitoring
        }
    }

    /**
     * Record health recovery metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param recoveryType the type of recovery (automatic, manual, service-restart, etc.)
     * @param success whether the recovery was successful
     * @param duration the duration of the recovery process
     * @param recoveryTime the time taken to recover
     * @param context additional context data about the recovery
     */
    public static void recordHealthRecovery(MetricsService metricsService, String recoveryType, boolean success,
            Duration duration, Duration recoveryTime, Map<String, Object> context) {
        try {
            var operation = metricsService.recordOperation("system-health-monitor", "recovery")
                    .withSuccess(success)
                    .withDuration(duration.toNanos())
                    .withData("recoveryType", recoveryType)
                    .withData("recoveryTime", recoveryTime.toNanos());

            // Add context data
            context.forEach(operation::withData);

            operation.record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting health monitoring
        }
    }
}
