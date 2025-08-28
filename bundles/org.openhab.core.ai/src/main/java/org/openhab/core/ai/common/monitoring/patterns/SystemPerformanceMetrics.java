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
 * Static utility class for recording system performance metrics.
 * 
 * <p>This class provides static methods for recording various system performance operations
 * including message latency, throughput, bandwidth, and general performance measurements.
 * All methods follow the static utility pattern with MetricsService as the first parameter.
 * 
 * <p>Example usage:
 * <pre>{@code
 * // Record message latency
 * SystemPerformanceMetrics.recordMessageLatency(metricsService, "api-request", 
 *     Duration.ofMillis(150), "endpoint-123", true);
 * 
 * // Record throughput
 * SystemPerformanceMetrics.recordThroughput(metricsService, "data-processing", 
 *     1000, Duration.ofSeconds(1), 0.95);
 * }</pre>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public final class SystemPerformanceMetrics {

    /**
     * Private constructor to prevent instantiation.
     */
    private SystemPerformanceMetrics() {
        // Utility class
    }

    /**
     * Record message latency metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param messageType the type of message (api-request, database-query, file-operation, etc.)
     * @param latency the latency of the message processing
     * @param endpoint the endpoint or service handling the message
     * @param success whether the message processing was successful
     */
    public static void recordMessageLatency(MetricsService metricsService, String messageType, Duration latency,
            @Nullable String endpoint, boolean success) {
        try {
            var operation = metricsService.recordOperation("system-performance", "message-latency")
                    .withSuccess(success)
                    .withDuration(latency.toNanos())
                    .withData("messageType", messageType)
                    .withData("latencyMs", latency.toMillis());

            if (endpoint != null) {
                operation.withData("endpoint", endpoint);
            }

            operation.record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting performance monitoring
        }
    }

    /**
     * Record throughput metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param operationType the type of operation (data-processing, api-calls, file-operations, etc.)
     * @param throughput the throughput value (operations per time unit)
     * @param measurementDuration the duration over which throughput was measured
     * @param successRate the success rate of the operations (0.0 to 1.0)
     */
    public static void recordThroughput(MetricsService metricsService, String operationType, double throughput,
            Duration measurementDuration, double successRate) {
        try {
            metricsService.recordOperation("system-performance", "throughput")
                    .withSuccess(true)
                    .withDuration(measurementDuration.toNanos())
                    .withData("operationType", operationType)
                    .withData("throughput", throughput)
                    .withData("measurementDurationMs", measurementDuration.toMillis())
                    .withData("successRate", successRate)
                    .withData("effectiveThroughput", throughput * successRate)
                    .record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting performance monitoring
        }
    }

    /**
     * Record bandwidth metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param bandwidthType the type of bandwidth (network, disk, memory, etc.)
     * @param bandwidth the bandwidth value in bytes per second
     * @param duration the duration of the bandwidth measurement
     * @param utilization the utilization percentage of the bandwidth (0.0 to 1.0)
     * @param direction the direction of the bandwidth usage (inbound, outbound, bidirectional)
     */
    public static void recordBandwidth(MetricsService metricsService, String bandwidthType, long bandwidth,
            Duration duration, double utilization, String direction) {
        try {
            metricsService.recordOperation("system-performance", "bandwidth")
                    .withSuccess(true)
                    .withDuration(duration.toNanos())
                    .withData("bandwidthType", bandwidthType)
                    .withData("bandwidth", bandwidth)
                    .withData("bandwidthMbps", bandwidth / (1024.0 * 1024.0))
                    .withData("durationMs", duration.toMillis())
                    .withData("utilization", utilization)
                    .withData("direction", direction)
                    .withData("effectiveBandwidth", bandwidth * utilization)
                    .record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting performance monitoring
        }
    }

    /**
     * Record general performance metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param performanceMetric the type of performance metric being measured
     * @param value the performance value
     * @param duration the measurement duration
     * @param unit the unit of measurement (ms, ops/sec, bytes/sec, etc.)
     * @param context additional context data for the performance metric
     */
    public static void recordPerformanceMetric(MetricsService metricsService, String performanceMetric, double value,
            Duration duration, @Nullable String unit, Map<String, Object> context) {
        try {
            var operation = metricsService.recordOperation("system-performance", "metric")
                    .withSuccess(true)
                    .withDuration(duration.toNanos())
                    .withData("performanceMetric", performanceMetric)
                    .withData("value", value)
                    .withData("durationMs", duration.toMillis());

            if (unit != null) {
                operation.withData("unit", unit);
            }

            // Add context data
            context.forEach(operation::withData);

            operation.record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting performance monitoring
        }
    }

    /**
     * Record performance benchmark metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param benchmarkType the type of benchmark (load-test, stress-test, endurance-test, etc.)
     * @param success whether the benchmark completed successfully
     * @param duration the duration of the benchmark
     * @param benchmarkScore the benchmark score or result
     * @param baselineScore the baseline score for comparison
     * @param context additional context data about the benchmark
     */
    public static void recordPerformanceBenchmark(MetricsService metricsService, String benchmarkType, boolean success,
            Duration duration, double benchmarkScore, @Nullable Double baselineScore, Map<String, Object> context) {
        try {
            var operation = metricsService.recordOperation("system-performance", "benchmark")
                    .withSuccess(success)
                    .withDuration(duration.toNanos())
                    .withData("benchmarkType", benchmarkType)
                    .withData("benchmarkScore", benchmarkScore)
                    .withData("durationMs", duration.toMillis());

            if (baselineScore != null) {
                operation.withData("baselineScore", baselineScore);
                operation.withData("improvement", benchmarkScore - baselineScore);
                operation.withData("improvementPercentage", ((benchmarkScore - baselineScore) / baselineScore) * 100.0);
            }

            // Add context data
            context.forEach(operation::withData);

            operation.record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting performance monitoring
        }
    }

    /**
     * Record performance degradation metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param degradationType the type of performance degradation (latency-increase, throughput-decrease, etc.)
     * @param severity the severity of the degradation (low, medium, high, critical)
     * @param duration the duration of the degradation measurement
     * @param degradationAmount the amount of degradation (percentage or absolute value)
     * @param context additional context data about the degradation
     */
    public static void recordPerformanceDegradation(MetricsService metricsService, String degradationType, String severity,
            Duration duration, double degradationAmount, Map<String, Object> context) {
        try {
            var operation = metricsService.recordOperation("system-performance", "degradation")
                    .withSuccess(false) // Degradation is generally considered a failure
                    .withDuration(duration.toNanos())
                    .withData("degradationType", degradationType)
                    .withData("severity", severity)
                    .withData("degradationAmount", degradationAmount)
                    .withData("durationMs", duration.toMillis());

            // Add context data
            context.forEach(operation::withData);

            operation.record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting performance monitoring
        }
    }
}
