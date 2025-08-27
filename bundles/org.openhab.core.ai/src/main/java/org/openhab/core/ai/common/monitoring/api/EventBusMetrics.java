package org.openhab.core.ai.common.monitoring.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Capability interface for event bus metrics.
 * 
 * <p>
 * This interface provides event bus-specific functionality including
 * event counts, subscription metrics, publisher metrics, and event bus
 * performance indicators.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface EventBusMetrics {

    /**
     * Get the total number of events processed.
     * 
     * @return total events
     */
    long totalEvents();

    /**
     * Get the total number of subscriptions.
     * 
     * @return total subscriptions
     */
    long totalSubscriptions();

    /**
     * Get the total number of event schemas.
     * 
     * @return total schemas
     */
    long totalSchemas();

    /**
     * Get the total number of publishers.
     * 
     * @return total publishers
     */
    long totalPublishers();

    /**
     * Get the total number of subscribers.
     * 
     * @return total subscribers
     */
    long totalSubscribers();

    /**
     * Get the event processing rate in events per second.
     * 
     * @return events per second
     */
    double eventsPerSecond();

    /**
     * Get the average number of events per operation.
     * 
     * @return average events per operation
     */
    double averageEventsPerOperation();

    /**
     * Get the subscription utilization rate (0-100).
     * 
     * @return subscription utilization percentage
     */
    double subscriptionUtilization();

    /**
     * Get the publisher utilization rate (0-100).
     * 
     * @return publisher utilization percentage
     */
    double publisherUtilization();
}
