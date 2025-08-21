package org.openhab.core.ai.tool.monitoring;

import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.CircuitBreakerMetrics;
import org.openhab.core.ai.common.monitoring.api.Health;
import org.openhab.core.ai.common.monitoring.base.AbstractMonitoring;

/**
 * Consolidated tool health metrics that extends the unified monitoring framework.
 * 
 * <p>
 * This class provides comprehensive health metrics for tool operations including
 * health status, circuit breaker state, and health indicators. It implements
 * the CircuitBreakerMetrics capability interface.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ToolHealthMetrics extends AbstractMonitoring implements Health, CircuitBreakerMetrics {

    private final HealthStatus status;
    private final @Nullable String statusMessage;
    private final @Nullable Map<String, Object> healthIndicators;
    private final String circuitBreakerState;
    private final boolean isOpen;
    private final long failureCount;
    private final long lastFailureTime;

    /**
     * Create a new ToolHealthMetrics instance.
     * 
     * @param id unique identifier for this metrics instance
     * @param timestamp timestamp when metrics were collected
     * @param status current health status
     * @param statusMessage optional status message
     * @param healthIndicators health indicators map
     * @param circuitBreakerState current circuit breaker state
     * @param isOpen whether circuit breaker is open
     * @param failureCount number of failures
     * @param lastFailureTime timestamp of last failure
     * @param data additional monitoring data
     */
    public ToolHealthMetrics(String id, Instant timestamp, HealthStatus status, @Nullable String statusMessage,
            @Nullable Map<String, Object> healthIndicators, String circuitBreakerState, boolean isOpen,
            long failureCount, long lastFailureTime, @Nullable Map<String, Object> data) {
        super(id, timestamp, "tool", "tool-health", "Tool health metrics", data);
        this.status = status;
        this.statusMessage = statusMessage;
        this.healthIndicators = healthIndicators;
        this.circuitBreakerState = circuitBreakerState;
        this.isOpen = isOpen;
        this.failureCount = failureCount;
        this.lastFailureTime = lastFailureTime;
    }

    /**
     * Create a new ToolHealthMetrics instance with current timestamp.
     * 
     * @param id unique identifier for this metrics instance
     * @param status current health status
     * @param circuitBreakerState current circuit breaker state
     * @param isOpen whether circuit breaker is open
     * @param failureCount number of failures
     */
    public ToolHealthMetrics(String id, HealthStatus status, String circuitBreakerState, boolean isOpen,
            long failureCount) {
        this(id, Instant.now(), status, null, null, circuitBreakerState, isOpen, failureCount, 0L, null);
    }

    // Health interface implementation
    @Override
    public HealthStatus getStatus() {
        return status;
    }

    @Override
    public @Nullable String getStatusMessage() {
        return statusMessage;
    }

    @Override
    public @Nullable Map<String, Object> getHealthIndicators() {
        return healthIndicators;
    }

    // CircuitBreakerMetrics interface implementation
    @Override
    public String breakerState() {
        return circuitBreakerState;
    }

    @Override
    public boolean isOpen() {
        return isOpen;
    }

    @Override
    public long failureCount() {
        return failureCount;
    }

    @Override
    public long lastFailureTime() {
        return lastFailureTime;
    }

    /**
     * Check if the tool is currently healthy.
     * 
     * @return true if tool is healthy, false otherwise
     */
    public boolean isToolHealthy() {
        return isHealthy() && !isOpen();
    }

    /**
     * Get the overall health score.
     * 
     * @return health score between 0.0 and 1.0
     */
    public double getHealthScore() {
        double baseScore = isHealthy() ? 1.0 : isDegraded() ? 0.5 : 0.0;
        double circuitBreakerPenalty = isOpen() ? 0.3 : 0.0;
        return Math.max(0.0, baseScore - circuitBreakerPenalty);
    }
}
