package org.openhab.core.ai.agent.api;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class ModelHealthStatusBuilder {
    ModelHealthStatus.HealthState overallHealth = ModelHealthStatus.HealthState.UNKNOWN;
    boolean primaryModelAvailable = false;
    boolean fallbackModelAvailable = false;
    double errorRate = 0.0;
    double responseTimeMs = 0.0;
    long totalRequests = 0;
    long failedRequests = 0;
    String lastError = "";
    Instant lastHealthCheck = Instant.now();
    Instant lastSuccessfulRequest = Instant.now();
    Instant lastFailedRequest = Instant.now();

    public ModelHealthStatusBuilder overallHealth(ModelHealthStatus.HealthState overallHealth) { this.overallHealth = overallHealth; return this; }
    public ModelHealthStatusBuilder primaryModelAvailable(boolean primaryModelAvailable) { this.primaryModelAvailable = primaryModelAvailable; return this; }
    public ModelHealthStatusBuilder fallbackModelAvailable(boolean fallbackModelAvailable) { this.fallbackModelAvailable = fallbackModelAvailable; return this; }
    public ModelHealthStatusBuilder errorRate(double errorRate) { this.errorRate = errorRate; return this; }
    public ModelHealthStatusBuilder responseTimeMs(double responseTimeMs) { this.responseTimeMs = responseTimeMs; return this; }
    public ModelHealthStatusBuilder totalRequests(long totalRequests) { this.totalRequests = totalRequests; return this; }
    public ModelHealthStatusBuilder failedRequests(long failedRequests) { this.failedRequests = failedRequests; return this; }
    public ModelHealthStatusBuilder lastError(String lastError) { this.lastError = lastError; return this; }
    public ModelHealthStatusBuilder lastHealthCheck(Instant lastHealthCheck) { this.lastHealthCheck = lastHealthCheck; return this; }
    public ModelHealthStatusBuilder lastSuccessfulRequest(Instant lastSuccessfulRequest) { this.lastSuccessfulRequest = lastSuccessfulRequest; return this; }
    public ModelHealthStatusBuilder lastFailedRequest(Instant lastFailedRequest) { this.lastFailedRequest = lastFailedRequest; return this; }

    public ModelHealthStatus build() { return new ModelHealthStatus(this); }
}


