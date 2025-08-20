package org.openhab.core.ai.common.statistics;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Server Statistics implementation for MCP servlet and other server components.
 *
 * <p>
 * This class provides comprehensive statistics for server operations,
 * including request counts, response times, connection status, and health metrics.
 * </p>
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class ServerStatistics extends BaseStatistics {

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
    private final boolean isHealthy;
    private final long lastRequestTime;
    private final long lastErrorTime;
    private final @Nullable String lastError;

    private ServerStatistics(String id, @Nullable Instant timestamp, Map<String, Object> metrics, String serverId,
            String serverName, String serverType, String protocol, long totalRequests, long successfulRequests,
            long failedRequests, long totalResponseTime, double averageResponseTime, int activeConnections,
            boolean isHealthy, long lastRequestTime, long lastErrorTime, @Nullable String lastError) {
        super(id, timestamp, StatisticsType.SERVER, metrics);
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
        this.isHealthy = isHealthy;
        this.lastRequestTime = lastRequestTime;
        this.lastErrorTime = lastErrorTime;
        this.lastError = lastError;
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
        return isHealthy;
    }

    public long getLastRequestTime() {
        return lastRequestTime;
    }

    public long getLastErrorTime() {
        return lastErrorTime;
    }

    public @Nullable String getLastError() {
        return lastError;
    }

    @Override
    public long getTotalCount() {
        return totalRequests;
    }

    @Override
    public long getSuccessCount() {
        return successfulRequests;
    }

    @Override
    public long getFailureCount() {
        return failedRequests;
    }

    @Override
    public double getSuccessRate() {
        if (totalRequests == 0) {
            return 0.0;
        }
        return (double) successfulRequests / totalRequests * 100.0;
    }

    @Override
    public String toString() {
        return String.format(
                "ServerStatistics{serverId='%s', serverName='%s', serverType='%s', protocol='%s', "
                        + "totalRequests=%d, successfulRequests=%d, failedRequests=%d, totalResponseTime=%d, "
                        + "averageResponseTime=%.2f, activeConnections=%d, isHealthy=%s, successRate=%.2f%%}",
                serverId, serverName, serverType, protocol, totalRequests, successfulRequests, failedRequests,
                totalResponseTime, averageResponseTime, activeConnections, isHealthy, getSuccessRate());
    }

    /**
     * Builder for ServerStatistics.
     */
    public static final class Builder {

        private String id;
        private @Nullable Instant timestamp;
        private final Map<String, Object> metrics = new HashMap<>();
        private String serverId = "";
        private String serverName = "";
        private String serverType = "";
        private String protocol = "";
        private long totalRequests = 0L;
        private long successfulRequests = 0L;
        private long failedRequests = 0L;
        private long totalResponseTime = 0L;
        private double averageResponseTime = 0.0;
        private int activeConnections = 0;
        private boolean isHealthy = true;
        private long lastRequestTime = 0L;
        private long lastErrorTime = 0L;
        private @Nullable String lastError = null;

        public Builder(String id) {
            this.id = Objects.requireNonNull(id, "id");
            this.timestamp = Instant.now();
        }

        public Builder(ServerStatistics source) {
            this.id = source.getId();
            this.timestamp = source.getTimestamp();
            this.serverId = source.serverId;
            this.serverName = source.serverName;
            this.serverType = source.serverType;
            this.protocol = source.protocol;
            this.totalRequests = source.totalRequests;
            this.successfulRequests = source.successfulRequests;
            this.failedRequests = source.failedRequests;
            this.totalResponseTime = source.totalResponseTime;
            this.averageResponseTime = source.averageResponseTime;
            this.activeConnections = source.activeConnections;
            this.isHealthy = source.isHealthy;
            this.lastRequestTime = source.lastRequestTime;
            this.lastErrorTime = source.lastErrorTime;
            this.lastError = source.lastError;
            this.metrics.putAll(source.getMetrics());
        }

        public Builder withServerId(String serverId) {
            this.serverId = Objects.requireNonNull(serverId, "serverId");
            return this;
        }

        public Builder withServerName(String serverName) {
            this.serverName = Objects.requireNonNull(serverName, "serverName");
            return this;
        }

        public Builder withServerType(String serverType) {
            this.serverType = Objects.requireNonNull(serverType, "serverType");
            return this;
        }

        public Builder withProtocol(String protocol) {
            this.protocol = Objects.requireNonNull(protocol, "protocol");
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

        public Builder withHealthy(boolean isHealthy) {
            this.isHealthy = isHealthy;
            return this;
        }

        public Builder withLastRequestTime(long lastRequestTime) {
            this.lastRequestTime = lastRequestTime;
            return this;
        }

        public Builder withLastErrorTime(long lastErrorTime) {
            this.lastErrorTime = lastErrorTime;
            return this;
        }

        public Builder withLastError(@Nullable String lastError) {
            this.lastError = lastError;
            return this;
        }

        public Builder withMetric(String key, Object value) {
            this.metrics.put(Objects.requireNonNull(key, "key"), value);
            return this;
        }

        public ServerStatistics build() {
            if (serverId.isBlank()) {
                throw new IllegalArgumentException("serverId must not be blank");
            }
            if (serverName.isBlank()) {
                throw new IllegalArgumentException("serverName must not be blank");
            }
            if (serverType.isBlank()) {
                throw new IllegalArgumentException("serverType must not be blank");
            }
            if (protocol.isBlank()) {
                throw new IllegalArgumentException("protocol must not be blank");
            }
            if (totalRequests < 0) {
                throw new IllegalArgumentException("totalRequests must be >= 0");
            }
            if (successfulRequests < 0) {
                throw new IllegalArgumentException("successfulRequests must be >= 0");
            }
            if (failedRequests < 0) {
                throw new IllegalArgumentException("failedRequests must be >= 0");
            }
            if (totalResponseTime < 0) {
                throw new IllegalArgumentException("totalResponseTime must be >= 0");
            }
            if (averageResponseTime < 0) {
                throw new IllegalArgumentException("averageResponseTime must be >= 0");
            }
            if (activeConnections < 0) {
                throw new IllegalArgumentException("activeConnections must be >= 0");
            }

            return new ServerStatistics(id, timestamp, new HashMap<>(metrics), serverId, serverName, serverType,
                    protocol, totalRequests, successfulRequests, failedRequests, totalResponseTime, averageResponseTime,
                    activeConnections, isHealthy, lastRequestTime, lastErrorTime, lastError);
        }
    }

    /**
     * Create a new builder instance.
     *
     * @param id the statistics identifier
     * @return a new builder
     */
    public static Builder builder(String id) {
        return new Builder(id);
    }

    /**
     * Create a new builder instance from an existing ServerStatistics object.
     *
     * @param source the source statistics
     * @return a new builder
     */
    public static Builder builder(ServerStatistics source) {
        return new Builder(source);
    }
}
