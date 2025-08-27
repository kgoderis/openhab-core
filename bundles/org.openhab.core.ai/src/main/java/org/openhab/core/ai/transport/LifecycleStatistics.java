package org.openhab.core.ai.transport;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.LifecycleMetrics;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.api.MetricsSnapshot;
import org.osgi.service.component.annotations.Reference;

/**
 * Lifecycle statistics container.
 * 
 * This class contains comprehensive statistics about the servlet lifecycle manager
 * including activity status, servlet counts, request/error metrics, uptime,
 * performance metrics, and health status information.
 * Originally extracted from ServletLifecycleManager as an inner class.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class LifecycleStatistics implements MetricsSnapshot, CountsMetrics, LatencyMetrics, LifecycleMetrics {

    @Reference
    private @Nullable MetricsService metricsService;
    private final boolean active;
    private final int servletCount;
    private final long totalRequests;
    private final long totalErrors;
    private final long uptimeMs;
    private final double errorRate;
    private final double requestsPerMinute;
    private final boolean allServletsHealthy;
    private final Map<String, ServletInfo> servlets;

    public LifecycleStatistics(boolean active, int servletCount, long totalRequests, long totalErrors, long uptimeMs,
            double errorRate, double requestsPerMinute, boolean allServletsHealthy, Map<String, ServletInfo> servlets) {
        this.active = active;
        this.servletCount = servletCount;
        this.totalRequests = totalRequests;
        this.totalErrors = totalErrors;
        this.uptimeMs = uptimeMs;
        this.errorRate = errorRate;
        this.requestsPerMinute = requestsPerMinute;
        this.allServletsHealthy = allServletsHealthy;
        this.servlets = servlets;
    }

    public boolean isActive() {
        return active;
    }

    public int getServletCount() {
        return servletCount;
    }

    public long getTotalRequests() {
        return totalRequests;
    }

    public long getTotalErrors() {
        return totalErrors;
    }

    public long getUptimeMs() {
        return uptimeMs;
    }

    public double getErrorRate() {
        return errorRate;
    }

    public double getRequestsPerMinute() {
        return requestsPerMinute;
    }

    public boolean isAllServletsHealthy() {
        return allServletsHealthy;
    }

    public Map<String, ServletInfo> getServlets() {
        return new ConcurrentHashMap<>(servlets);
    }

    @Override
    public String toString() {
        return String.format(
                "LifecycleStatistics{active=%s, servletCount=%d, totalRequests=%d, totalErrors=%d, uptimeMs=%d, errorRate=%.2f%%, requestsPerMinute=%.2f, allServletsHealthy=%s}",
                active, servletCount, totalRequests, totalErrors, uptimeMs, errorRate * 100, requestsPerMinute,
                allServletsHealthy);
    }

    // MetricsSnapshot implementation
    @Override
    public long getTimestampMs() {
        return System.currentTimeMillis();
    }

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

    @Override
    public double successRate() {
        if (totalRequests == 0) {
            return 0.0;
        }
        return (success() * 100.0) / totalRequests;
    }

    // LatencyMetrics implementation
    @Override
    public long totalDurationNanos() {
        // Convert uptime to nanoseconds for latency metrics
        return uptimeMs * 1_000_000L;
    }

    @Override
    public double averageMs(long total) {
        if (total == 0) {
            return 0.0;
        }
        return totalDurationNanos() / (total * 1_000_000.0);
    }

    // LifecycleMetrics implementation
    @Override
    public double uptime() {
        return uptimeMs / (1000.0 * 60.0 * 60.0); // Convert to hours
    }

    @Override
    public double startupTime() {
        // For lifecycle statistics, we consider the uptime as the startup time
        return uptimeMs;
    }

    @Override
    public double shutdownTime() {
        // Not tracked in current implementation
        return 0.0;
    }

    @Override
    public double restartFrequency() {
        // Not tracked in current implementation
        return 0.0;
    }

    @Override
    public double healthScore() {
        // Calculate health score based on error rate and servlet health
        double errorScore = (1.0 - errorRate) * 100.0;
        double healthScore = allServletsHealthy ? 100.0 : 50.0;
        return (errorScore + healthScore) / 2.0;
    }

    /**
     * Record lifecycle statistics using MetricsService.
     */
    public void recordLifecycleStatistics() {
        MetricsService metrics = metricsService;
        if (metrics != null) {
            long startTime = System.currentTimeMillis();
            boolean success = allServletsHealthy && errorRate < 0.1; // Consider healthy if error rate < 10%
            long duration = System.currentTimeMillis() - startTime;

            metrics.recordOperation("lifecycle", "statistics", success, java.time.Duration.ofMillis(duration));
        }
    }
}
