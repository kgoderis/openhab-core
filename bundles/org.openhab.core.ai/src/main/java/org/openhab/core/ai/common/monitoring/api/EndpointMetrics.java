package org.openhab.core.ai.common.monitoring.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Capability interface for endpoint-specific metrics.
 * 
 * <p>
 * This interface provides endpoint-specific functionality including response times,
 * availability, error rates, throughput, and concurrency metrics.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface EndpointMetrics {

    /**
     * Get the average response time in milliseconds.
     * 
     * @return response time in milliseconds
     */
    double responseTime();

    /**
     * Get the availability as a percentage.
     * 
     * @return availability between 0.0 and 100.0
     */
    double availability();

    /**
     * Get the error rate as a percentage.
     * 
     * @return error rate between 0.0 and 100.0
     */
    double errorRate();

    /**
     * Get the throughput in operations per hour.
     * 
     * @return throughput
     */
    double throughput();

    /**
     * Get the concurrency in operations per second.
     * 
     * @return concurrency
     */
    double concurrency();
}
