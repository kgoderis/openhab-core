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

    private ModelHealthStatus(Builder builder) {
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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private HealthState overallHealth = HealthState.UNKNOWN;
        private boolean primaryModelAvailable = false;
        private boolean fallbackModelAvailable = false;
        private double errorRate = 0.0;
        private double responseTimeMs = 0.0;
        private long totalRequests = 0;
        private long failedRequests = 0;
        private String lastError = "";
        private Instant lastHealthCheck = Instant.now();
        private Instant lastSuccessfulRequest = Instant.now();
        private Instant lastFailedRequest = Instant.now();

        public Builder overallHealth(HealthState overallHealth) {
            this.overallHealth = overallHealth;
            return this;
        }

        public Builder primaryModelAvailable(boolean primaryModelAvailable) {
            this.primaryModelAvailable = primaryModelAvailable;
            return this;
        }

        public Builder fallbackModelAvailable(boolean fallbackModelAvailable) {
            this.fallbackModelAvailable = fallbackModelAvailable;
            return this;
        }

        public Builder errorRate(double errorRate) {
            this.errorRate = errorRate;
            return this;
        }

        public Builder responseTimeMs(double responseTimeMs) {
            this.responseTimeMs = responseTimeMs;
            return this;
        }

        public Builder totalRequests(long totalRequests) {
            this.totalRequests = totalRequests;
            return this;
        }

        public Builder failedRequests(long failedRequests) {
            this.failedRequests = failedRequests;
            return this;
        }

        public Builder lastError(String lastError) {
            this.lastError = lastError;
            return this;
        }

        public Builder lastHealthCheck(Instant lastHealthCheck) {
            this.lastHealthCheck = lastHealthCheck;
            return this;
        }

        public Builder lastSuccessfulRequest(Instant lastSuccessfulRequest) {
            this.lastSuccessfulRequest = lastSuccessfulRequest;
            return this;
        }

        public Builder lastFailedRequest(Instant lastFailedRequest) {
            this.lastFailedRequest = lastFailedRequest;
            return this;
        }

        public ModelHealthStatus build() {
            return new ModelHealthStatus(this);
        }
    }
}
