package org.openhab.core.ai.tool.server.transport;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Server statistics for the MCP servlet.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class ServerStatistics {
    private final long totalRequests;
    private final long successfulRequests;
    private final long failedRequests;
    private final long totalResponseTime;
    private final double averageResponseTime;
    private final int activeConnections;

    /**
     * Create a new server statistics instance.
     * 
     * @param totalRequests the total number of requests
     * @param successfulRequests the number of successful requests
     * @param failedRequests the number of failed requests
     * @param totalResponseTime the total response time in milliseconds
     * @param averageResponseTime the average response time in milliseconds
     * @param activeConnections the number of active connections
     */
    public ServerStatistics(long totalRequests, long successfulRequests, long failedRequests, long totalResponseTime,
            double averageResponseTime, int activeConnections) {
        this.totalRequests = totalRequests;
        this.successfulRequests = successfulRequests;
        this.failedRequests = failedRequests;
        this.totalResponseTime = totalResponseTime;
        this.averageResponseTime = averageResponseTime;
        this.activeConnections = activeConnections;
    }

    /**
     * Get the total number of requests.
     * 
     * @return the total requests
     */
    public long getTotalRequests() {
        return totalRequests;
    }

    /**
     * Get the number of successful requests.
     * 
     * @return the successful requests
     */
    public long getSuccessfulRequests() {
        return successfulRequests;
    }

    /**
     * Get the number of failed requests.
     * 
     * @return the failed requests
     */
    public long getFailedRequests() {
        return failedRequests;
    }

    /**
     * Get the total response time in milliseconds.
     * 
     * @return the total response time
     */
    public long getTotalResponseTime() {
        return totalResponseTime;
    }

    /**
     * Get the average response time in milliseconds.
     * 
     * @return the average response time
     */
    public double getAverageResponseTime() {
        return averageResponseTime;
    }

    /**
     * Get the number of active connections.
     * 
     * @return the active connections
     */
    public int getActiveConnections() {
        return activeConnections;
    }

    /**
     * Get the success rate as a percentage.
     * 
     * @return the success rate (0.0 to 100.0)
     */
    public double getSuccessRate() {
        if (totalRequests == 0) {
            return 0.0;
        }
        return (double) successfulRequests / totalRequests * 100.0;
    }

    @Override
    public String toString() {
        return "ServerStatistics{" + "totalRequests=" + totalRequests + ", successfulRequests=" + successfulRequests
                + ", failedRequests=" + failedRequests + ", totalResponseTime=" + totalResponseTime
                + ", averageResponseTime=" + averageResponseTime + ", activeConnections=" + activeConnections
                + ", successRate=" + getSuccessRate() + "%}";
    }
}
