package org.openhab.core.ai.tool.monitoring;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * System health metrics for monitoring system performance and status.
 * 
 * <p>
 * This class provides comprehensive metrics about system health including
 * CPU usage, memory usage, disk usage, connection counts, and response times.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class SystemHealthMetrics {
    private final double cpuUsage;
    private final double memoryUsage;
    private final double diskUsage;
    private final long activeConnections;
    private final long totalRequests;
    private final double responseTime;
    private final Instant timestamp;

    /**
     * Constructor for SystemHealthMetrics.
     * 
     * @param cpuUsage CPU usage percentage
     * @param memoryUsage memory usage percentage
     * @param diskUsage disk usage percentage
     * @param activeConnections number of active connections
     * @param totalRequests total number of requests
     * @param responseTime average response time in milliseconds
     * @param timestamp timestamp of the metrics
     */
    public SystemHealthMetrics(double cpuUsage, double memoryUsage, double diskUsage, long activeConnections,
            long totalRequests, double responseTime, Instant timestamp) {
        this.cpuUsage = cpuUsage;
        this.memoryUsage = memoryUsage;
        this.diskUsage = diskUsage;
        this.activeConnections = activeConnections;
        this.totalRequests = totalRequests;
        this.responseTime = responseTime;
        this.timestamp = timestamp;
    }

    /**
     * Get CPU usage percentage.
     * 
     * @return CPU usage
     */
    public double getCpuUsage() {
        return cpuUsage;
    }

    /**
     * Get memory usage percentage.
     * 
     * @return memory usage
     */
    public double getMemoryUsage() {
        return memoryUsage;
    }

    /**
     * Get disk usage percentage.
     * 
     * @return disk usage
     */
    public double getDiskUsage() {
        return diskUsage;
    }

    /**
     * Get number of active connections.
     * 
     * @return active connections
     */
    public long getActiveConnections() {
        return activeConnections;
    }

    /**
     * Get total number of requests.
     * 
     * @return total requests
     */
    public long getTotalRequests() {
        return totalRequests;
    }

    /**
     * Get average response time in milliseconds.
     * 
     * @return response time
     */
    public double getResponseTime() {
        return responseTime;
    }

    /**
     * Get timestamp of the metrics.
     * 
     * @return timestamp
     */
    public Instant getTimestamp() {
        return timestamp;
    }
}
