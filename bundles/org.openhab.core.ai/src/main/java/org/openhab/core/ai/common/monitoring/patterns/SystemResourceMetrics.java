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
 * Static utility class for recording system resource metrics.
 * 
 * <p>This class provides static methods for recording various system resource operations
 * including resource management, concurrent requests, resource usage, and allocation.
 * All methods follow the static utility pattern with MetricsService as the first parameter.
 * 
 * <p>Example usage:
 * <pre>{@code
 * // Record resource management
 * SystemResourceMetrics.recordResourceManagement(metricsService, "memory", "allocate", 
 *     true, Duration.ofMillis(5), 1024, 2048);
 * 
 * // Record concurrent requests
 * SystemResourceMetrics.recordConcurrentRequests(metricsService, "api", 10, 5, 
 *     Duration.ofSeconds(1), 0.8);
 * }</pre>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public final class SystemResourceMetrics {

    /**
     * Private constructor to prevent instantiation.
     */
    private SystemResourceMetrics() {
        // Utility class
    }

    /**
     * Record resource management metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param resourceType the type of resource being managed (memory, cpu, disk, network, etc.)
     * @param operation the type of resource operation (allocate, deallocate, resize, etc.)
     * @param success whether the resource operation was successful
     * @param duration the duration of the resource operation
     * @param resourceAmount the amount of resource involved in the operation
     * @param totalCapacity the total capacity of the resource
     */
    public static void recordResourceManagement(MetricsService metricsService, String resourceType, String operation,
            boolean success, Duration duration, long resourceAmount, long totalCapacity) {
        try {
            metricsService.recordOperation("system-resource", "management")
                    .withSuccess(success)
                    .withDuration(duration.toNanos())
                    .withData("resourceType", resourceType)
                    .withData("operation", operation)
                    .withData("resourceAmount", resourceAmount)
                    .withData("totalCapacity", totalCapacity)
                    .withData("utilizationPercentage", (resourceAmount / (double) totalCapacity) * 100.0)
                    .record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting resource management
        }
    }

    /**
     * Record concurrent request metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param requestType the type of concurrent requests (api, database, file, etc.)
     * @param activeRequests the number of currently active requests
     * @param maxConcurrentRequests the maximum number of concurrent requests allowed
     * @param duration the duration of the concurrent request measurement
     * @param utilizationRate the utilization rate of concurrent requests (0.0 to 1.0)
     */
    public static void recordConcurrentRequests(MetricsService metricsService, String requestType, int activeRequests,
            int maxConcurrentRequests, Duration duration, double utilizationRate) {
        try {
            metricsService.recordOperation("system-resource", "concurrent-requests")
                    .withSuccess(true)
                    .withDuration(duration.toNanos())
                    .withData("requestType", requestType)
                    .withData("activeRequests", activeRequests)
                    .withData("maxConcurrentRequests", maxConcurrentRequests)
                    .withData("utilizationRate", utilizationRate)
                    .withData("availableCapacity", maxConcurrentRequests - activeRequests)
                    .record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting resource monitoring
        }
    }

    /**
     * Record resource usage metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param resourceType the type of resource being used
     * @param usageAmount the amount of resource currently being used
     * @param capacity the total capacity of the resource
     * @param duration the duration of the usage measurement
     * @param usageTrend the trend of resource usage (increasing, decreasing, stable)
     */
    public static void recordResourceUsage(MetricsService metricsService, String resourceType, long usageAmount,
            long capacity, Duration duration, String usageTrend) {
        try {
            double utilizationPercentage = (usageAmount / (double) capacity) * 100.0;
            
            metricsService.recordOperation("system-resource", "usage")
                    .withSuccess(true)
                    .withDuration(duration.toNanos())
                    .withData("resourceType", resourceType)
                    .withData("usageAmount", usageAmount)
                    .withData("capacity", capacity)
                    .withData("utilizationPercentage", utilizationPercentage)
                    .withData("usageTrend", usageTrend)
                    .withData("availableAmount", capacity - usageAmount)
                    .record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting resource monitoring
        }
    }

    /**
     * Record resource allocation metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param allocationType the type of resource allocation (static, dynamic, on-demand, etc.)
     * @param success whether the resource allocation was successful
     * @param duration the duration of the allocation process
     * @param allocatedAmount the amount of resource allocated
     * @param requestedAmount the amount of resource requested
     * @param allocationEfficiency the efficiency of the allocation (0.0 to 1.0)
     */
    public static void recordResourceAllocation(MetricsService metricsService, String allocationType, boolean success,
            Duration duration, long allocatedAmount, long requestedAmount, double allocationEfficiency) {
        try {
            metricsService.recordOperation("system-resource", "allocation")
                    .withSuccess(success)
                    .withDuration(duration.toNanos())
                    .withData("allocationType", allocationType)
                    .withData("allocatedAmount", allocatedAmount)
                    .withData("requestedAmount", requestedAmount)
                    .withData("allocationEfficiency", allocationEfficiency)
                    .withData("allocationRatio", allocatedAmount / (double) requestedAmount)
                    .record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting resource allocation
        }
    }

    /**
     * Record resource contention metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param resourceType the type of resource experiencing contention
     * @param contentionLevel the level of contention (low, medium, high, critical)
     * @param duration the duration of the contention measurement
     * @param waitingRequests the number of requests waiting for the resource
     * @param contentionReason the reason for the contention
     */
    public static void recordResourceContention(MetricsService metricsService, String resourceType, String contentionLevel,
            Duration duration, int waitingRequests, @Nullable String contentionReason) {
        try {
            var operation = metricsService.recordOperation("system-resource", "contention")
                    .withSuccess("low".equals(contentionLevel))
                    .withDuration(duration.toNanos())
                    .withData("resourceType", resourceType)
                    .withData("contentionLevel", contentionLevel)
                    .withData("waitingRequests", waitingRequests);

            if (contentionReason != null) {
                operation.withData("contentionReason", contentionReason);
            }

            operation.record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting resource monitoring
        }
    }

    /**
     * Record resource optimization metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param optimizationType the type of resource optimization performed
     * @param success whether the optimization was successful
     * @param duration the duration of the optimization process
     * @param beforeUtilization the resource utilization before optimization
     * @param afterUtilization the resource utilization after optimization
     * @param context additional context data about the optimization
     */
    public static void recordResourceOptimization(MetricsService metricsService, String optimizationType, boolean success,
            Duration duration, double beforeUtilization, double afterUtilization, Map<String, Object> context) {
        try {
            var operation = metricsService.recordOperation("system-resource", "optimization")
                    .withSuccess(success)
                    .withDuration(duration.toNanos())
                    .withData("optimizationType", optimizationType)
                    .withData("beforeUtilization", beforeUtilization)
                    .withData("afterUtilization", afterUtilization)
                    .withData("improvement", afterUtilization - beforeUtilization)
                    .withData("improvementPercentage", ((afterUtilization - beforeUtilization) / beforeUtilization) * 100.0);

            // Add context data
            context.forEach(operation::withData);

            operation.record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting resource optimization
        }
    }
}
