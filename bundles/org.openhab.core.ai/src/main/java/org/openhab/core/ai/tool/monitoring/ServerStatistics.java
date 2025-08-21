package org.openhab.core.ai.tool.monitoring;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Server statistics for transport layer.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ServerStatistics {
    private final String serverId;
    private final String serverName;
    private final String serverType;
    private final String protocol;
    private final long totalRequests;
    private final long successfulRequests;
    private final long failedRequests;
    private final long totalResponseTime;
    private final double averageResponseTime;
    private final int activeConnections;
    private final boolean healthy;

    public ServerStatistics(String serverId, String serverName, String serverType, String protocol, long totalRequests,
            long successfulRequests, long failedRequests, long totalResponseTime, double averageResponseTime,
            int activeConnections, boolean healthy) {
        this.serverId = serverId;
        this.serverName = serverName;
        this.serverType = serverType;
        this.protocol = protocol;
        this.totalRequests = totalRequests;
        this.successfulRequests = successfulRequests;
        this.failedRequests = failedRequests;
        this.totalResponseTime = totalResponseTime;
        this.averageResponseTime = averageResponseTime;
        this.activeConnections = activeConnections;
        this.healthy = healthy;
    }

    public String getServerId() {
        return serverId;
    }

    public String getServerName() {
        return serverName;
    }

    public String getServerType() {
        return serverType;
    }

    public String getProtocol() {
        return protocol;
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

    public boolean isHealthy() {
        return healthy;
    }

    public static Builder builder(String serverId) {
        return new Builder(serverId);
    }

    public static final class Builder {
        private String serverId;
        private String serverName = "";
        private String serverType = "";
        private String protocol = "";
        private long totalRequests = 0;
        private long successfulRequests = 0;
        private long failedRequests = 0;
        private long totalResponseTime = 0;
        private double averageResponseTime = 0.0;
        private int activeConnections = 0;
        private boolean healthy = true;

        public Builder(String serverId) {
            this.serverId = serverId;
        }

        public Builder withServerId(String serverId) {
            this.serverId = serverId;
            return this;
        }

        public Builder withServerName(String serverName) {
            this.serverName = serverName;
            return this;
        }

        public Builder withServerType(String serverType) {
            this.serverType = serverType;
            return this;
        }

        public Builder withProtocol(String protocol) {
            this.protocol = protocol;
            return this;
        }

        public Builder withTotalRequests(long totalRequests) {
            this.totalRequests = totalRequests;
            return this;
        }

        public Builder withSuccessfulRequests(long successfulRequests) {
            this.successfulRequests = successfulRequests;
            return this;
        }

        public Builder withFailedRequests(long failedRequests) {
            this.failedRequests = failedRequests;
            return this;
        }

        public Builder withTotalResponseTime(long totalResponseTime) {
            this.totalResponseTime = totalResponseTime;
            return this;
        }

        public Builder withAverageResponseTime(double averageResponseTime) {
            this.averageResponseTime = averageResponseTime;
            return this;
        }

        public Builder withActiveConnections(int activeConnections) {
            this.activeConnections = activeConnections;
            return this;
        }

        public Builder withHealthy(boolean healthy) {
            this.healthy = healthy;
            return this;
        }

        public ServerStatistics build() {
            return new ServerStatistics(serverId, serverName, serverType, protocol, totalRequests, successfulRequests,
                    failedRequests, totalResponseTime, averageResponseTime, activeConnections, healthy);
        }
    }
}
