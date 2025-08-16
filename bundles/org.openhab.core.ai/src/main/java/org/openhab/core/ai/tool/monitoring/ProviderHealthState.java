package org.openhab.core.ai.tool.monitoring;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.model.api.ModelProviderType;

/**
 * Provider health state tracker used by the system health monitor.
 *
 * <p>
 * Tracks request counters, response times, failures, last health check
 * and circuit breaker status for a single provider.
 * </p>
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
final class ProviderHealthState {
    private final ModelProviderType provider;
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong successfulRequests = new AtomicLong(0);
    private final AtomicLong failedRequests = new AtomicLong(0);
    private final AtomicLong totalResponseTime = new AtomicLong(0);
    private final AtomicLong consecutiveFailures = new AtomicLong(0);
    private final AtomicReference<CircuitBreakerState> circuitBreakerState = new AtomicReference<>(
            CircuitBreakerState.CLOSED);
    private final AtomicReference<Instant> lastFailureTime = new AtomicReference<>();
    private final AtomicReference<Instant> lastHealthCheck = new AtomicReference<>();

    ProviderHealthState(ModelProviderType provider) {
        this.provider = provider;
    }

    void recordSuccess(long responseTime) {
        totalRequests.incrementAndGet();
        successfulRequests.incrementAndGet();
        totalResponseTime.addAndGet(responseTime);
        consecutiveFailures.set(0);

        if (circuitBreakerState.get() == CircuitBreakerState.HALF_OPEN) {
            circuitBreakerState.set(CircuitBreakerState.CLOSED);
        }
    }

    void recordFailure(Exception error) {
        totalRequests.incrementAndGet();
        failedRequests.incrementAndGet();
        consecutiveFailures.incrementAndGet();
        lastFailureTime.set(Instant.now());

        if (consecutiveFailures.get() >= 5 && circuitBreakerState.get() == CircuitBreakerState.CLOSED) {
            circuitBreakerState.set(CircuitBreakerState.OPEN);
        }
    }

    void updateFromHealthCheck(HealthCheckResult result) {
        lastHealthCheck.set(Instant.now());
        if (result.isHealthy() && circuitBreakerState.get() == CircuitBreakerState.OPEN) {
            circuitBreakerState.set(CircuitBreakerState.HALF_OPEN);
        }
    }

    void forceRecovery() {
        consecutiveFailures.set(0);
        circuitBreakerState.set(CircuitBreakerState.CLOSED);
        lastFailureTime.set(Instant.now());
    }

    boolean isHealthy() {
        if (circuitBreakerState.get() == CircuitBreakerState.OPEN) {
            return false;
        }
        return getSuccessRate() >= 0.8 && getAverageResponseTime() <= 5000;
    }

    ProviderHealthMetrics getHealthMetrics() {
        return new ProviderHealthMetrics(totalRequests.get(), successfulRequests.get(), failedRequests.get(),
                totalResponseTime.get(), getSuccessRate(), getAverageResponseTime(), circuitBreakerState.get(),
                isHealthy());
    }

    double getSuccessRate() {
        long total = totalRequests.get();
        return total > 0 ? (double) successfulRequests.get() / total : 0.0;
    }

    double getAverageResponseTime() {
        long total = totalRequests.get();
        return total > 0 ? (double) totalResponseTime.get() / total : 0.0;
    }

    long getFailedRequestsCount() {
        return failedRequests.get();
    }

    long getConsecutiveFailures() {
        return consecutiveFailures.get();
    }

    CircuitBreakerState getCircuitBreakerState() {
        return circuitBreakerState.get();
    }

    Instant getLastHealthCheck() {
        return lastHealthCheck.get();
    }
}
