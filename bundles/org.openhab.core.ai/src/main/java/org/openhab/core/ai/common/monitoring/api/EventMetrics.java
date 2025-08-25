package org.openhab.core.ai.common.monitoring.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Capability interface for event processing metrics.
 * 
 * <p>
 * This interface provides event processing-specific functionality including
 * event processing rates, event queue metrics, event correlation efficiency,
 * and event handling performance metrics.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface EventMetrics {

    /**
     * Get the event processing rate in events per second.
     * 
     * @return event processing rate
     */
    double eventProcessingRate();

    /**
     * Get the event queue depth.
     * 
     * @return queue depth
     */
    long eventQueueDepth();

    /**
     * Get the event correlation efficiency (0-100).
     * 
     * @return correlation efficiency score
     */
    double eventCorrelationEfficiency();

    /**
     * Get the event processing latency in milliseconds.
     * 
     * @return processing latency
     */
    double eventProcessingLatency();

    /**
     * Get the event error rate as a percentage.
     * 
     * @return error rate between 0.0 and 100.0
     */
    double eventErrorRate();

    /**
     * Get the event throughput in events per minute.
     * 
     * @return event throughput
     */
    double eventThroughput();

    /**
     * Get the event handling success rate as a percentage.
     * 
     * @return handling success rate between 0.0 and 100.0
     */
    double eventHandlingSuccessRate();

    /**
     * Get the event filtering efficiency (0-100).
     * 
     * @return filtering efficiency score
     */
    double eventFilteringEfficiency();

    /**
     * Get the event routing accuracy as a percentage.
     * 
     * @return routing accuracy between 0.0 and 100.0
     */
    double eventRoutingAccuracy();

    /**
     * Get the event priority handling efficiency (0-100).
     * 
     * @return priority handling efficiency score
     */
    double eventPriorityHandlingEfficiency();
}
