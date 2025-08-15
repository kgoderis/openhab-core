package org.openhab.core.ai.transport;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;

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
public class LifecycleStatistics {
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
}
