package org.openhab.core.ai.tool.monitoring;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.HealthStatus;
import org.openhab.core.ai.common.monitoring.api.MetricKeys;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.service.snapshot.UnifiedMetricsSnapshot;
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
    private final AtomicReference<CircuitBreakerState> circuitBreakerState = new AtomicReference<>(
            CircuitBreakerState.CLOSED);
    private final AtomicReference<Instant> lastFailureTime = new AtomicReference<>();
    private final AtomicReference<Instant> lastHealthCheck = new AtomicReference<>();
    private final MetricsService metricsService;

    ProviderHealthState(ModelProviderType provider, MetricsService metricsService) {
        this.provider = provider;
        this.metricsService = metricsService;
    }

    void recordSuccess(long responseTime) {
        // Get current snapshot to reset consecutive failures
        UnifiedMetricsSnapshot snapshot = metricsService.getSnapshot(MetricKeys.provider(provider.name()), UnifiedMetricsSnapshot.class);
        int currentConsecutiveFailures = 0; // Reset to 0 on success
        
        // Record success metrics using MetricsService
        metricsService.recordOperation("provider", "request")
            .withSuccess(true)
            .withDuration(responseTime)
            .withData("provider", provider.name())
            .withData("responseTimeMs", responseTime)
            .withData("consecutiveFailures", currentConsecutiveFailures)
            .record();

        if (circuitBreakerState.get() == CircuitBreakerState.HALF_OPEN) {
            circuitBreakerState.set(CircuitBreakerState.CLOSED);
        }
    }

    void recordFailure(Exception error) {
        // Get current snapshot to increment consecutive failures
        UnifiedMetricsSnapshot snapshot = metricsService.getSnapshot(MetricKeys.provider(provider.name()), UnifiedMetricsSnapshot.class);
        int currentConsecutiveFailures = 0;
        
        if (snapshot != null && snapshot.healthIndicators() != null) {
            Object consecutiveFailuresObj = snapshot.healthIndicators().get("consecutiveFailures");
            currentConsecutiveFailures = consecutiveFailuresObj instanceof Number ? ((Number) consecutiveFailuresObj).intValue() : 0;
        }
        
        // Increment consecutive failures
        int newConsecutiveFailures = currentConsecutiveFailures + 1;
        
        // Record failure metrics using MetricsService
        metricsService.recordOperation("provider", "request")
            .withSuccess(false)
            .withDuration(0L)
            .withData("provider", provider.name())
            .withData("exceptionType", error.getClass().getSimpleName())
            .withData("errorMessage", error.getMessage() != null ? error.getMessage() : "Unknown error")
            .withData("consecutiveFailures", newConsecutiveFailures)
            .record();

        lastFailureTime.set(Instant.now());

        // Check circuit breaker state based on consecutive failures
        if (newConsecutiveFailures >= 5 && circuitBreakerState.get() == CircuitBreakerState.CLOSED) {
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
        circuitBreakerState.set(CircuitBreakerState.CLOSED);
        lastFailureTime.set(Instant.now());
    }

    boolean isHealthy() {
        if (circuitBreakerState.get() == CircuitBreakerState.OPEN) {
            return false;
        }
        return getSuccessRate() >= 0.8 && getAverageResponseTime() <= 5000;
    }

    double getSuccessRate() {
        UnifiedMetricsSnapshot snapshot = metricsService.getSnapshot(MetricKeys.provider(provider.name()), UnifiedMetricsSnapshot.class);
        if (snapshot != null) {
            return snapshot.successRate();
        }
        return 0.0;
    }

    double getAverageResponseTime() {
        UnifiedMetricsSnapshot snapshot = metricsService.getSnapshot(MetricKeys.provider(provider.name()), UnifiedMetricsSnapshot.class);
        if (snapshot != null) {
            return snapshot.averageMs(snapshot.total());
        }
        return 0.0;
    }

    long getFailedRequestsCount() {
        UnifiedMetricsSnapshot snapshot = metricsService.getSnapshot(MetricKeys.provider(provider.name()), UnifiedMetricsSnapshot.class);
        return snapshot != null ? snapshot.failure() : 0;
    }

    long getTotalRequests() {
        UnifiedMetricsSnapshot snapshot = metricsService.getSnapshot(MetricKeys.provider(provider.name()), UnifiedMetricsSnapshot.class);
        return snapshot != null ? snapshot.total() : 0;
    }

    long getConsecutiveFailures() {
        UnifiedMetricsSnapshot snapshot = metricsService.getSnapshot(MetricKeys.provider(provider.name()), UnifiedMetricsSnapshot.class);
        if (snapshot != null && snapshot.healthIndicators() != null) {
            Object consecutiveFailuresObj = snapshot.healthIndicators().get("consecutiveFailures");
            return consecutiveFailuresObj instanceof Number ? ((Number) consecutiveFailuresObj).longValue() : 0;
        }
        return 0;
    }

    CircuitBreakerState getCircuitBreakerState() {
        return circuitBreakerState.get();
    }

    Instant getLastHealthCheck() {
        return lastHealthCheck.get();
    }
}
