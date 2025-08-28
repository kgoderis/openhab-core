package org.openhab.core.ai.common.monitoring.service.snapshot;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.MetricsSnapshot;

/**
 * Snapshot record for HTTP transport metrics.
 * 
 * <p>
 * This record provides HTTP transport-specific metrics including request counts,
 * error rates, bandwidth utilization, and uptime information. It implements
 * CountsMetrics and LatencyMetrics capability interfaces.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record HttpTransportSnapshot(String providerId, String providerName, boolean running, long uptime,
        long totalRequests, long totalErrors, long totalBytesTransferred, long totalDurationNanos,
        long timestampMs) implements MetricsSnapshot, CountsMetrics, LatencyMetrics {

    // CountsMetrics implementation
    @Override
    public long total() {
        return totalRequests;
    }

    @Override
    public long success() {
        return totalRequests - totalErrors;
    }

    @Override
    public long failure() {
        return totalErrors;
    }

    // LatencyMetrics implementation
    @Override
    public long totalDurationNanos() {
        return totalDurationNanos;
    }

    /**
     * Calculate requests per second based on uptime.
     * 
     * @return requests per second
     */
    public double requestsPerSecond() {
        return uptime > 0 ? (double) totalRequests / (uptime / 1000.0) : 0.0;
    }

    /**
     * Calculate error rate as a percentage.
     * 
     * @return error rate between 0.0 and 1.0
     */
    public double errorRate() {
        return totalRequests > 0 ? (double) totalErrors / totalRequests : 0.0;
    }

    /**
     * Calculate bandwidth in bytes per second based on uptime.
     * 
     * @return bandwidth in bytes per second
     */
    public double bandwidthBytesPerSecond() {
        return uptime > 0 ? (double) totalBytesTransferred / (uptime / 1000.0) : 0.0;
    }

    /**
     * Get average bytes per request.
     * 
     * @return average bytes per request
     */
    public double averageBytesPerRequest() {
        return totalRequests > 0 ? (double) totalBytesTransferred / totalRequests : 0.0;
    }

    /**
     * Check if transport is performing well.
     * 
     * @return true if error rate is below 5%
     */
    public boolean isHealthy() {
        return errorRate() < 0.05;
    }

    /**
     * Get uptime in hours.
     * 
     * @return uptime in hours
     */
    public double uptimeHours() {
        return uptime / (1000.0 * 60.0 * 60.0);
    }

    /**
     * Get total bytes transferred in megabytes.
     * 
     * @return total bytes in MB
     */
    public double totalBytesMB() {
        return totalBytesTransferred / (1024.0 * 1024.0);
    }
}
