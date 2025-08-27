package org.openhab.core.ai.common.monitoring.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Capability interface for server metrics.
 * 
 * <p>
 * This interface provides server-specific functionality including
 * request rates, response rates, connection utilization, and session metrics.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface ServerMetrics {

    /**
     * Get the total number of requests.
     * 
     * @return total requests
     */
    long totalRequests();

    /**
     * Get the total number of responses.
     * 
     * @return total responses
     */
    long totalResponses();

    /**
     * Get the total number of connections.
     * 
     * @return total connections
     */
    long totalConnections();

    /**
     * Get the total number of sessions.
     * 
     * @return total sessions
     */
    long totalSessions();

    /**
     * Get the total number of endpoints.
     * 
     * @return total endpoints
     */
    long totalEndpoints();

    /**
     * Get the request rate in requests per second.
     * 
     * @return request rate
     */
    double requestRate();

    /**
     * Get the response rate in responses per second.
     * 
     * @return response rate
     */
    double responseRate();

    /**
     * Get the connection utilization as a percentage.
     * 
     * @return connection utilization between 0.0 and 100.0
     */
    double connectionUtilization();

    /**
     * Get the session utilization as a percentage.
     * 
     * @return session utilization between 0.0 and 100.0
     */
    double sessionUtilization();
}
