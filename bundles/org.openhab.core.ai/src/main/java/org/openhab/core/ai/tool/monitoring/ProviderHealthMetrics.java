package org.openhab.core.ai.tool.monitoring;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Provider-level health metrics including request counts and circuit breaker state.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ProviderHealthMetrics {
    private final long totalRequests;
    private final long successfulRequests;
    private final long failedRequests;
    private final long totalResponseTime;
    private final double successRate;
    private final double averageResponseTime;
    private final CircuitBreakerState circuitBreakerState;
    private final boolean healthy;

    public ProviderHealthMetrics(long totalRequests, long successfulRequests, long failedRequests,
            long totalResponseTime, double successRate, double averageResponseTime,
            CircuitBreakerState circuitBreakerState, boolean healthy) {
        this.totalRequests = totalRequests;
        this.successfulRequests = successfulRequests;
        this.failedRequests = failedRequests;
        this.totalResponseTime = totalResponseTime;
        this.successRate = successRate;
        this.averageResponseTime = averageResponseTime;
        this.circuitBreakerState = circuitBreakerState;
        this.healthy = healthy;
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

    public double getSuccessRate() {
        return successRate;
    }

    public double getAverageResponseTime() {
        return averageResponseTime;
    }

    public CircuitBreakerState getCircuitBreakerState() {
        return circuitBreakerState;
    }

    public boolean isHealthy() {
        return healthy;
    }
}
