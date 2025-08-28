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
 * Static utility class for recording agent communication metrics.
 * 
 * <p>This class provides static methods for recording various agent communication operations
 * including conversations, messaging, and event bus operations.
 * All methods follow the static utility pattern with MetricsService as the first parameter.
 * 
 * <p>Example usage:
 * <pre>{@code
 * // Record agent conversation
 * AgentCommunicationMetrics.recordAgentConversation(metricsService, "conv-123", "agent-456", 
 *     15, Duration.ofMinutes(5), true);
 * 
 * // Record agent messaging
 * AgentCommunicationMetrics.recordAgentMessaging(metricsService, "msg-789", "agent-456", "agent-789", 
 *     "request", 1024, Duration.ofMillis(50));
 * }</pre>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public final class AgentCommunicationMetrics {

    /**
     * Private constructor to prevent instantiation.
     */
    private AgentCommunicationMetrics() {
        // Utility class
    }

    /**
     * Record agent conversation metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param conversationId the unique identifier of the conversation
     * @param agentId the unique identifier of the agent
     * @param messageCount the number of messages in the conversation
     * @param duration the duration of the conversation
     * @param success whether the conversation was successful
     */
    public static void recordAgentConversation(MetricsService metricsService, String conversationId, String agentId,
            int messageCount, Duration duration, boolean success) {
        try {
            metricsService.recordOperation("agent-conversation", "operation")
                    .withSuccess(success)
                    .withDuration(duration.toNanos())
                    .withData("conversationId", conversationId)
                    .withData("agentId", agentId)
                    .withData("messageCount", messageCount)
                    .record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting agent communication
        }
    }

    /**
     * Record agent messaging metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param messageId the unique identifier of the message
     * @param senderId the unique identifier of the sender agent
     * @param receiverId the unique identifier of the receiver agent
     * @param messageType the type of message (request, response, notification, etc.)
     * @param size the size of the message in bytes
     * @param latency the latency of the message transmission
     */
    public static void recordAgentMessaging(MetricsService metricsService, String messageId, String senderId,
            String receiverId, String messageType, long size, Duration latency) {
        try {
            metricsService.recordOperation("agent-messaging", "operation")
                    .withSuccess(true)
                    .withDuration(latency.toNanos())
                    .withData("messageId", messageId)
                    .withData("senderId", senderId)
                    .withData("receiverId", receiverId)
                    .withData("messageType", messageType)
                    .withData("size", size)
                    .record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting agent messaging
        }
    }

    /**
     * Record agent event bus metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param eventId the unique identifier of the event
     * @param eventType the type of event
     * @param publisherId the unique identifier of the event publisher
     * @param subscriberCount the number of subscribers to the event
     * @param processingTime the time taken to process the event
     */
    public static void recordAgentEventBus(MetricsService metricsService, String eventId, String eventType,
            String publisherId, int subscriberCount, Duration processingTime) {
        try {
            metricsService.recordOperation("agent-event-bus", "operation")
                    .withSuccess(true)
                    .withDuration(processingTime.toNanos())
                    .withData("eventId", eventId)
                    .withData("eventType", eventType)
                    .withData("publisherId", publisherId)
                    .withData("subscriberCount", subscriberCount)
                    .record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting event bus operations
        }
    }

    /**
     * Record agent communication performance metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param communicationType the type of communication (conversation, messaging, event)
     * @param performanceMetric the specific performance metric being measured
     * @param value the performance value
     * @param duration the measurement duration
     * @param context additional context data for the performance metric
     */
    public static void recordAgentCommunicationPerformance(MetricsService metricsService, String communicationType,
            String performanceMetric, double value, Duration duration, Map<String, Object> context) {
        try {
            var operation = metricsService.recordOperation("agent-communication", "performance")
                    .withSuccess(true)
                    .withDuration(duration.toNanos())
                    .withData("communicationType", communicationType)
                    .withData("performanceMetric", performanceMetric)
                    .withData("value", value);

            // Add context data
            context.forEach(operation::withData);

            operation.record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting agent communication
        }
    }

    /**
     * Record agent communication error metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param communicationType the type of communication that failed
     * @param errorType the type of error that occurred
     * @param errorMessage the error message
     * @param duration the time taken before the error occurred
     * @param context additional context data about the error
     */
    public static void recordAgentCommunicationError(MetricsService metricsService, String communicationType,
            String errorType, @Nullable String errorMessage, Duration duration, Map<String, Object> context) {
        try {
            var operation = metricsService.recordOperation("agent-communication", "error")
                    .withSuccess(false)
                    .withDuration(duration.toNanos())
                    .withData("communicationType", communicationType)
                    .withData("errorType", errorType);

            if (errorMessage != null) {
                operation.withData("errorMessage", errorMessage);
            }

            // Add context data
            context.forEach(operation::withData);

            operation.record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting agent communication
        }
    }
}
