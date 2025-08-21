package org.openhab.core.ai.agent.monitoring;

import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.agent.core.AgentState;
import org.openhab.core.ai.common.monitoring.api.CircuitBreakerMetrics;
import org.openhab.core.ai.common.monitoring.api.Health;
import org.openhab.core.ai.common.monitoring.base.AbstractMonitoring;

/**
 * Consolidated agent health metrics that extends the unified monitoring framework.
 * 
 * <p>
 * This class provides comprehensive health metrics for agent operations including
 * health status, circuit breaker state, and health indicators. It implements
 * the Health and CircuitBreakerMetrics capability interfaces.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class AgentHealthMetrics extends AbstractMonitoring implements Health, CircuitBreakerMetrics {

    private final HealthStatus status;
    private final @Nullable String statusMessage;
    private final @Nullable Map<String, Object> healthIndicators;
    private final String circuitBreakerState;
    private final boolean isOpen;
    private final long failureCount;
    private final long lastFailureTime;
    private final String agentId;
    private final AgentState agentState;

    /**
     * Create a new AgentHealthMetrics instance.
     * 
     * @param id unique identifier for this metrics instance
     * @param timestamp timestamp when metrics were collected
     * @param agentId the agent identifier
     * @param agentState the current agent state
     * @param status current health status
     * @param statusMessage optional status message
     * @param healthIndicators health indicators map
     * @param circuitBreakerState current circuit breaker state
     * @param isOpen whether circuit breaker is open
     * @param failureCount number of failures
     * @param lastFailureTime timestamp of last failure
     * @param data additional monitoring data
     */
    public AgentHealthMetrics(String id, Instant timestamp, String agentId, AgentState agentState, HealthStatus status,
            @Nullable String statusMessage, @Nullable Map<String, Object> healthIndicators, String circuitBreakerState,
            boolean isOpen, long failureCount, long lastFailureTime, @Nullable Map<String, Object> data) {
        super(id, timestamp, "agent", "agent-health", "Agent health metrics", data);
        this.agentId = agentId;
        this.agentState = agentState;
        this.status = status;
        this.statusMessage = statusMessage;
        this.healthIndicators = healthIndicators;
        this.circuitBreakerState = circuitBreakerState;
        this.isOpen = isOpen;
        this.failureCount = failureCount;
        this.lastFailureTime = lastFailureTime;
    }

    /**
     * Create a new AgentHealthMetrics instance with current timestamp.
     * 
     * @param id unique identifier for this metrics instance
     * @param agentId the agent identifier
     * @param agentState the current agent state
     * @param status current health status
     * @param circuitBreakerState current circuit breaker state
     * @param isOpen whether circuit breaker is open
     * @param failureCount number of failures
     */
    public AgentHealthMetrics(String id, String agentId, AgentState agentState, HealthStatus status,
            String circuitBreakerState, boolean isOpen, long failureCount) {
        this(id, Instant.now(), agentId, agentState, status, null, null, circuitBreakerState, isOpen, failureCount, 0L,
                null);
    }

    /**
     * Get the agent identifier.
     * 
     * @return the agent identifier
     */
    public String getAgentId() {
        return agentId;
    }

    /**
     * Get the current agent state.
     * 
     * @return the agent state
     */
    public AgentState getAgentState() {
        return agentState;
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
     * Check if the agent is currently healthy.
     * 
     * @return true if agent is healthy, false otherwise
     */
    public boolean isAgentHealthy() {
        return isHealthy() && !isOpen();
    }

    /**
     * Get the overall agent health score.
     * 
     * @return health score between 0.0 and 1.0
     */
    public double getAgentHealthScore() {
        double baseScore = isHealthy() ? 1.0 : isDegraded() ? 0.5 : 0.0;
        double circuitBreakerPenalty = isOpen() ? 0.3 : 0.0;
        double stateBonus = agentState.isActive() ? 0.1 : 0.0;

        return Math.min(1.0, Math.max(0.0, baseScore - circuitBreakerPenalty + stateBonus));
    }

    /**
     * Check if the agent is in a good operational state.
     * 
     * @return true if agent is in good operational state
     */
    public boolean isOperational() {
        return isAgentHealthy() && agentState.isActive();
    }
}
