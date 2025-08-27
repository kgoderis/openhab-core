package org.openhab.core.ai.tool.server.transport;

import java.time.Duration;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.service.snapshot.GenericMetricsSnapshot;

/**
 * Backend server information for load balancing.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class BackendServer {
    private final String url;
    // Performance monitoring - migrated to MetricsService
    // private final AtomicLong requestCount = new AtomicLong(0);
    // private final AtomicLong errorCount = new AtomicLong(0);
    // private final AtomicLong responseTimeSum = new AtomicLong(0);
    private volatile boolean healthy = true;
    volatile long lastHealthCheck = 0;

    private MetricsService metricsService;

    public void setMetricsService(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    public BackendServer(String url) {
        this.url = url;
    }

    String getUrl() {
        return url;
    }

    long getRequestCount() {
        if (metricsService != null) {
            try {
                GenericMetricsSnapshot snapshot = metricsService.getSnapshot("backend_server", "request");
                return snapshot.getMetricAsLong("total_count");
            } catch (Exception e) {
                // Fallback to default value if MetricsService fails
                return 0L;
            }
        }
        return 0L;
    }

    long getErrorCount() {
        if (metricsService != null) {
            try {
                GenericMetricsSnapshot snapshot = metricsService.getSnapshot("backend_server", "error");
                return snapshot.getMetricAsLong("total_count");
            } catch (Exception e) {
                // Fallback to default value if MetricsService fails
                return 0L;
            }
        }
        return 0L;
    }

    double getAverageResponseTime() {
        if (metricsService != null) {
            try {
                GenericMetricsSnapshot snapshot = metricsService.getSnapshot("backend_server", "request");
                return snapshot.getMetricAsDouble("average_duration_ms");
            } catch (Exception e) {
                // Fallback to default value if MetricsService fails
                return 0.0;
            }
        }
        return 0.0;
    }

    boolean isHealthy() {
        return healthy;
    }

    void setHealthy(boolean healthy) {
        this.healthy = healthy;
    }

    void recordRequest(long responseTime) {
        if (metricsService != null) {
            try {
                metricsService.recordOperation("backend_server", "request")
                    .withSuccess(true)
                    .withDuration(Duration.ofMillis(responseTime).toNanos())
                    .withData("url", url)
                    .withData("responseTimeMs", responseTime)
                    .record();
            } catch (Exception e) {
                // Fallback to local logging if MetricsService fails
                System.err.println("Failed to record backend server request metrics for URL " + url + ": " + e.getMessage());
                // Graceful degradation: continue with request processing even if metrics recording fails
            }
        }
    }

    void recordError() {
        if (metricsService != null) {
            try {
                metricsService.recordOperation("backend_server", "error")
                    .withSuccess(false)
                    .withDuration(0L)
                    .withData("url", url)
                    .record();
            } catch (Exception e) {
                // Fallback to local logging if MetricsService fails
                System.err.println("Failed to record backend server error metrics: " + e.getMessage());
            }
        }
    }

    double getErrorRate() {
        if (metricsService != null) {
            try {
                GenericMetricsSnapshot errorSnapshot = metricsService.getSnapshot("backend_server", "error");
                GenericMetricsSnapshot requestSnapshot = metricsService.getSnapshot("backend_server", "request");
                
                long errorCount = errorSnapshot.getMetricAsLong("total_count");
                long requestCount = requestSnapshot.getMetricAsLong("total_count");
                
                if (requestCount > 0) {
                    return (double) errorCount / requestCount * 100.0;
                }
                return 0.0;
            } catch (Exception e) {
                // Fallback to default value if MetricsService fails
                return 0.0;
            }
        }
        return 0.0;
    }
}
