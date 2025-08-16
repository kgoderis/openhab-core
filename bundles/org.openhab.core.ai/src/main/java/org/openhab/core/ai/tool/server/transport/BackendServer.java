package org.openhab.core.ai.tool.server.transport;

import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Backend server information for load balancing.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class BackendServer {
    private final String url;
    private final AtomicLong requestCount = new AtomicLong(0);
    private final AtomicLong errorCount = new AtomicLong(0);
    private final AtomicLong responseTimeSum = new AtomicLong(0);
    private volatile boolean healthy = true;
    volatile long lastHealthCheck = 0;

    public BackendServer(String url) {
        this.url = url;
    }

    String getUrl() {
        return url;
    }

    long getRequestCount() {
        return requestCount.get();
    }

    long getErrorCount() {
        return errorCount.get();
    }

    double getAverageResponseTime() {
        long count = requestCount.get();
        return count > 0 ? (double) responseTimeSum.get() / count : 0.0;
    }

    boolean isHealthy() {
        return healthy;
    }

    void setHealthy(boolean healthy) {
        this.healthy = healthy;
    }

    void recordRequest(long responseTime) {
        requestCount.incrementAndGet();
        responseTimeSum.addAndGet(responseTime);
    }

    void recordError() {
        errorCount.incrementAndGet();
    }

    double getErrorRate() {
        long total = requestCount.get();
        return total > 0 ? (double) errorCount.get() / total : 0.0;
    }
}
