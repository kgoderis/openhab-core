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
 * Static utility class for recording event processing metrics.
 * 
 * <p>This class provides static methods for recording various event processing operations
 * including event processing, correlation, log ingestion, and filtering.
 * All methods follow the static utility pattern with MetricsService as the first parameter.
 * 
 * <p>Example usage:
 * <pre>{@code
 * // Record event processing
 * EventProcessingMetrics.recordEventProcessing(metricsService, "user-action", 
 *     true, Duration.ofMillis(10), 1024, 3);
 * 
 * // Record event correlation
 * EventProcessingMetrics.recordEventCorrelation(metricsService, "corr-123", 
 *     true, Duration.ofMillis(25), 5, 0.95);
 * }</pre>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public final class EventProcessingMetrics {

    /**
     * Private constructor to prevent instantiation.
     */
    private EventProcessingMetrics() {
        // Utility class
    }

    /**
     * Record event processing metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param eventType the type of event being processed
     * @param success whether the event processing was successful
     * @param duration the duration of the event processing
     * @param eventSize the size of the event data in bytes
     * @param processingSteps the number of processing steps performed
     */
    public static void recordEventProcessing(MetricsService metricsService, String eventType, boolean success,
            Duration duration, long eventSize, int processingSteps) {
        try {
            metricsService.recordOperation("event-processing", "process")
                    .withSuccess(success)
                    .withDuration(duration.toNanos())
                    .withData("eventType", eventType)
                    .withData("eventSize", eventSize)
                    .withData("processingSteps", processingSteps)
                    .record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting event processing
        }
    }

    /**
     * Record event correlation metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param correlationId the unique identifier of the correlation
     * @param success whether the event correlation was successful
     * @param duration the duration of the correlation process
     * @param correlatedEvents the number of events that were correlated
     * @param correlationAccuracy the accuracy of the correlation (0.0 to 1.0)
     */
    public static void recordEventCorrelation(MetricsService metricsService, String correlationId, boolean success,
            Duration duration, int correlatedEvents, double correlationAccuracy) {
        try {
            metricsService.recordOperation("event-processing", "correlation")
                    .withSuccess(success)
                    .withDuration(duration.toNanos())
                    .withData("correlationId", correlationId)
                    .withData("correlatedEvents", correlatedEvents)
                    .withData("correlationAccuracy", correlationAccuracy)
                    .record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting event processing
        }
    }

    /**
     * Record log ingestion metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param ingestionType the type of log ingestion (file, stream, batch, etc.)
     * @param success whether the log ingestion was successful
     * @param duration the duration of the ingestion process
     * @param logSize the size of the ingested logs in bytes
     * @param logCount the number of log entries ingested
     * @param ingestionRate the rate of ingestion (logs per second)
     */
    public static void recordLogIngestion(MetricsService metricsService, String ingestionType, boolean success,
            Duration duration, long logSize, int logCount, double ingestionRate) {
        try {
            metricsService.recordOperation("event-processing", "log-ingestion")
                    .withSuccess(success)
                    .withDuration(duration.toNanos())
                    .withData("ingestionType", ingestionType)
                    .withData("logSize", logSize)
                    .withData("logCount", logCount)
                    .withData("ingestionRate", ingestionRate)
                    .record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting event processing
        }
    }

    /**
     * Record event filtering metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param filterType the type of event filter (content, source, severity, etc.)
     * @param success whether the event filtering was successful
     * @param duration the duration of the filtering process
     * @param filteredEvents the number of events that were filtered
     * @param filterAccuracy the accuracy of the filtering (0.0 to 1.0)
     */
    public static void recordEventFiltering(MetricsService metricsService, String filterType, boolean success,
            Duration duration, int filteredEvents, double filterAccuracy) {
        try {
            metricsService.recordOperation("event-processing", "filtering")
                    .withSuccess(success)
                    .withDuration(duration.toNanos())
                    .withData("filterType", filterType)
                    .withData("filteredEvents", filteredEvents)
                    .withData("filterAccuracy", filterAccuracy)
                    .record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting event processing
        }
    }

    /**
     * Record event aggregation metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param aggregationType the type of event aggregation (time-based, count-based, etc.)
     * @param success whether the event aggregation was successful
     * @param duration the duration of the aggregation process
     * @param inputEvents the number of input events
     * @param outputEvents the number of output aggregated events
     * @param aggregationRatio the ratio of input to output events
     */
    public static void recordEventAggregation(MetricsService metricsService, String aggregationType, boolean success,
            Duration duration, int inputEvents, int outputEvents, double aggregationRatio) {
        try {
            metricsService.recordOperation("event-processing", "aggregation")
                    .withSuccess(success)
                    .withDuration(duration.toNanos())
                    .withData("aggregationType", aggregationType)
                    .withData("inputEvents", inputEvents)
                    .withData("outputEvents", outputEvents)
                    .withData("aggregationRatio", aggregationRatio)
                    .record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting event processing
        }
    }

    /**
     * Record event transformation metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param transformationType the type of event transformation (format, schema, enrichment, etc.)
     * @param success whether the event transformation was successful
     * @param duration the duration of the transformation process
     * @param inputSize the size of the input event data
     * @param outputSize the size of the output event data
     * @param context additional context data about the transformation
     */
    public static void recordEventTransformation(MetricsService metricsService, String transformationType,
            boolean success, Duration duration, long inputSize, long outputSize, Map<String, Object> context) {
        try {
            var operation = metricsService.recordOperation("event-processing", "transformation")
                    .withSuccess(success)
                    .withDuration(duration.toNanos())
                    .withData("transformationType", transformationType)
                    .withData("inputSize", inputSize)
                    .withData("outputSize", outputSize)
                    .withData("sizeRatio", outputSize / (double) inputSize);

            // Add context data
            context.forEach(operation::withData);

            operation.record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting event processing
        }
    }
}
