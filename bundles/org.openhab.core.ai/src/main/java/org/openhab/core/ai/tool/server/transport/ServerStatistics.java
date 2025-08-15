package org.openhab.core.ai.tool.server.transport;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Server statistics for servlet transports (Tool and Agent).
 *
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public class ServerStatistics {
    private final long totalRequests;
    private final long successfulRequests;
    private final long failedRequests;
    private final long totalResponseTime;
    private final double averageResponseTime;
    private final int activeConnections;

    public ServerStatistics(long totalRequests, long successfulRequests, long failedRequests, long totalResponseTime,
            double averageResponseTime, int activeConnections) {
        this.totalRequests = totalRequests;
        this.successfulRequests = successfulRequests;
        this.failedRequests = failedRequests;
        this.totalResponseTime = totalResponseTime;
        this.averageResponseTime = averageResponseTime;
        this.activeConnections = activeConnections;
    }

    public long getTotalRequests() {
        return totalRequests;
    }

    public long getSuccessfulRequests() {
        return successfulRequests;
    }

    public long getFailedRequests() {
        return failedRequests;
    }

    public long getTotalResponseTime() {
        return totalResponseTime;
    }

    public double getAverageResponseTime() {
        return averageResponseTime;
    }

    public int getActiveConnections() {
        return activeConnections;
    }
}
