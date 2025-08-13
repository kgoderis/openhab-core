package org.openhab.core.ai.tool.monitoring;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Service health state tracker used by the system health monitor.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
final class ServiceHealthState {
    private final String serviceName;
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong successfulRequests = new AtomicLong(0);
    private final AtomicLong failedRequests = new AtomicLong(0);
    private final AtomicLong totalResponseTime = new AtomicLong(0);
    private final AtomicReference<Instant> lastHealthCheck = new AtomicReference<>();

    ServiceHealthState(String serviceName) {
        this.serviceName = serviceName;
    }

    void recordSuccess(long responseTime) {
        totalRequests.incrementAndGet();
        successfulRequests.incrementAndGet();
        totalResponseTime.addAndGet(responseTime);
    }

    void recordFailure(Exception error) {
        totalRequests.incrementAndGet();
        failedRequests.incrementAndGet();
    }

    void updateFromHealthCheck(boolean isHealthy, long responseTime) {
        lastHealthCheck.set(Instant.now());
    }

    void forceRecovery() {
        failedRequests.set(0);
    }

    boolean isHealthy() {
        return getSuccessRate() >= 0.8 && getAverageResponseTime() <= 5000;
    }

    ServiceHealthMetrics getHealthMetrics() {
        return new ServiceHealthMetrics(totalRequests.get(), successfulRequests.get(), failedRequests.get(),
                totalResponseTime.get(), getSuccessRate(), getAverageResponseTime(), isHealthy());
    }

    double getSuccessRate() {
        long total = totalRequests.get();
        return total > 0 ? (double) successfulRequests.get() / total : 0.0;
    }

    double getAverageResponseTime() {
        long total = totalRequests.get();
        return total > 0 ? (double) totalResponseTime.get() / total : 0.0;
    }

    Instant getLastHealthCheck() { return lastHealthCheck.get(); }
}


