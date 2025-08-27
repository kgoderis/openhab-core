package org.openhab.core.ai.transport;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.service.snapshot.GenericMetricsSnapshot;

/**
 * Servlet information container.
 * 
 * This class contains information about a registered servlet including
 * metadata, request counts, error counts, health status, and timing information.
 * Originally extracted from ServletLifecycleManager as an inner class.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ServletInfo {
    private final String servletId;
    private final String servletName;
    private final String servletPattern;
    private final String protocol;
    private final long registrationTime;
    // Performance monitoring - migrated to MetricsService
    // private final AtomicLong requestCount = new AtomicLong(0);
    // private final AtomicLong errorCount = new AtomicLong(0);
    private final AtomicBoolean isHealthy = new AtomicBoolean(true);
    private volatile long lastRequestTime = 0;
    private volatile long lastErrorTime = 0;

    private MetricsService metricsService;

    public void setMetricsService(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    public ServletInfo(String servletId, String servletName, String servletPattern, String protocol) {
        this.servletId = servletId;
        this.servletName = servletName;
        this.servletPattern = servletPattern;
        this.protocol = protocol;
        this.registrationTime = System.currentTimeMillis();
    }

    public String getServletId() {
        return servletId;
    }

    public String getServletName() {
        return servletName;
    }

    public String getServletPattern() {
        return servletPattern;
    }

    public String getProtocol() {
        return protocol;
    }

    public long getRegistrationTime() {
        return registrationTime;
    }

    public long getRequestCount() {
        if (metricsService != null) {
            try {
                GenericMetricsSnapshot snapshot = metricsService.getSnapshot("servlet", "request");
                return snapshot.getMetricAsLong("total_count");
            } catch (Exception e) {
                // Fallback to default value if MetricsService fails
                return 0L;
            }
        }
        return 0L;
    }

    public long getErrorCount() {
        if (metricsService != null) {
            try {
                GenericMetricsSnapshot snapshot = metricsService.getSnapshot("servlet", "error");
                return snapshot.getMetricAsLong("total_count");
            } catch (Exception e) {
                // Fallback to default value if MetricsService fails
                return 0L;
            }
        }
        return 0L;
    }

    public boolean isHealthy() {
        return isHealthy.get();
    }

    public long getLastRequestTime() {
        return lastRequestTime;
    }

    public long getLastErrorTime() {
        return lastErrorTime;
    }

    public void incrementRequestCount() {
        if (metricsService != null) {
            try {
                metricsService.recordOperation("servlet", "request")
                    .withSuccess(true)
                    .withDuration(0L)
                    .withData("servletId", servletId)
                    .withData("servletName", servletName)
                    .withData("protocol", protocol)
                    .record();
            } catch (Exception e) {
                // Fallback to local logging if MetricsService fails
                System.err.println("Failed to record servlet request metrics for servlet " + servletId + ": " + e.getMessage());
                // Graceful degradation: continue with request processing even if metrics recording fails
            }
        }
        lastRequestTime = System.currentTimeMillis();
    }

    public void incrementErrorCount() {
        if (metricsService != null) {
            try {
                metricsService.recordOperation("servlet", "error")
                    .withSuccess(false)
                    .withDuration(0L)
                    .withData("servletId", servletId)
                    .withData("servletName", servletName)
                    .withData("protocol", protocol)
                    .record();
            } catch (Exception e) {
                // Fallback to local logging if MetricsService fails
                System.err.println("Failed to record servlet error metrics: " + e.getMessage());
            }
        }
        lastErrorTime = System.currentTimeMillis();
    }

    public void setHealthy(boolean healthy) {
        isHealthy.set(healthy);
    }

    /**
     * Reset the servlet statistics.
     */
    public void resetStatistics() {
        // requestCount.set(0); // Removed AtomicLong
        // errorCount.set(0); // Removed AtomicLong
        lastRequestTime = 0;
        lastErrorTime = 0;
    }

    @Override
    public String toString() {
        return String.format(
                "ServletInfo{servletId='%s', servletName='%s', protocol='%s', healthy=%s, requests=%d, errors=%d}",
                servletId, servletName, protocol, isHealthy.get(), getRequestCount(), getErrorCount());
    }
}
