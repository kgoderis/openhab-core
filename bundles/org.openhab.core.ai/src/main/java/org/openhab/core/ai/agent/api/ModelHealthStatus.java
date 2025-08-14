package org.openhab.core.ai.agent.api;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Model health status for the integration system
 * 
 * <p>
 * This class tracks:
 * - Overall system health status
 * - Model availability and responsiveness
 * - Error rates and types
 * - Performance metrics
 * - Resource utilization
 * - Last health check time
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public class ModelHealthStatus {

    public enum HealthState {
        HEALTHY,
        DEGRADED,
        UNHEALTHY,
        UNKNOWN
    }

    private final HealthState overallHealth;
    private final boolean primaryModelAvailable;
    private final boolean fallbackModelAvailable;
    private final double errorRate;
    private final double responseTimeMs;
    private final long totalRequests;
    private final long failedRequests;
    private final String lastError;
    private final Instant lastHealthCheck;
    private final Instant lastSuccessfulRequest;
    private final Instant lastFailedRequest;

    ModelHealthStatus(ModelHealthStatusBuilder builder) {
        this.overallHealth = builder.overallHealth;
        this.primaryModelAvailable = builder.primaryModelAvailable;
        this.fallbackModelAvailable = builder.fallbackModelAvailable;
        this.errorRate = builder.errorRate;
        this.responseTimeMs = builder.responseTimeMs;
        this.totalRequests = builder.totalRequests;
        this.failedRequests = builder.failedRequests;
        this.lastError = builder.lastError;
        this.lastHealthCheck = builder.lastHealthCheck;
        this.lastSuccessfulRequest = builder.lastSuccessfulRequest;
        this.lastFailedRequest = builder.lastFailedRequest;
    }

    public HealthState getOverallHealth() {
        return overallHealth;
    }

    public boolean isPrimaryModelAvailable() {
        return primaryModelAvailable;
    }

    public boolean isFallbackModelAvailable() {
        return fallbackModelAvailable;
    }

    public double getErrorRate() {
        return errorRate;
    }

    public double getResponseTimeMs() {
        return responseTimeMs;
    }

    public long getTotalRequests() {
        return totalRequests;
    }

    public long getFailedRequests() {
        return failedRequests;
    }

    public String getLastError() {
        return lastError;
    }

    public Instant getLastHealthCheck() {
        return lastHealthCheck;
    }

    public Instant getLastSuccessfulRequest() {
        return lastSuccessfulRequest;
    }

    public Instant getLastFailedRequest() {
        return lastFailedRequest;
    }

    public boolean isHealthy() {
        return overallHealth == HealthState.HEALTHY;
    }

    public boolean isDegraded() {
        return overallHealth == HealthState.DEGRADED;
    }

    public boolean isUnhealthy() {
        return overallHealth == HealthState.UNHEALTHY;
    }

    public static ModelHealthStatusBuilder builder() { return new ModelHealthStatusBuilder(); }
}
