package org.openhab.core.ai.common.monitoring.snapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.Health;
import org.openhab.core.ai.common.monitoring.api.Health.HealthStatus;
import org.openhab.core.ai.common.monitoring.api.MetricsSnapshot;
import org.openhab.core.ai.common.monitoring.api.MonitoringType;

/**
 * Immutable snapshot of provider health metrics.
 * 
 * This record provides a thread-safe snapshot of provider health metrics at a specific
 * point in time. It implements multiple capability interfaces for different types of
 * health monitoring access.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public record ProviderHealthSnapshot(HealthStatus status, String statusMessage, long totalOperations,
        long successfulOperations, long failedOperations, double successRate, long lastFailureTime,
        long lastSuccessTime, String lastError, long timestampMs) implements MetricsSnapshot, Health, CountsMetrics {

    public ProviderHealthSnapshot {
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(statusMessage, "statusMessage");
        Objects.requireNonNull(lastError, "lastError");
    }

    @Override
    public String getId() {
        return "provider-health";
    }

    @Override
    public java.time.Instant getTimestamp() {
        return java.time.Instant.ofEpochMilli(timestampMs);
    }

    @Override
    public MonitoringType getType() {
        return MonitoringType.HEALTH;
    }

    @Override
    public String getDomain() {
        return "provider";
    }

    @Override
    public String getSource() {
        return "health-collector";
    }

    @Override
    public Map<String, Object> getData() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", status.name());
        data.put("statusMessage", statusMessage);
        data.put("totalOperations", totalOperations);
        data.put("successfulOperations", successfulOperations);
        data.put("failedOperations", failedOperations);
        data.put("successRate", successRate);
        data.put("lastFailureTime", lastFailureTime);
        data.put("lastSuccessTime", lastSuccessTime);
        data.put("lastError", lastError);
        return data;
    }

    @Override
    public HealthStatus getStatus() {
        return status;
    }

    @Override
    public String getStatusMessage() {
        return statusMessage;
    }

    @Override
    public Map<String, Object> getHealthIndicators() {
        Map<String, Object> indicators = new HashMap<>();
        indicators.put("successRate", successRate);
        indicators.put("totalOperations", totalOperations);
        indicators.put("lastFailureTime", lastFailureTime);
        indicators.put("lastSuccessTime", lastSuccessTime);
        indicators.put("lastError", lastError);
        return indicators;
    }

    @Override
    public long total() {
        return totalOperations;
    }

    @Override
    public long success() {
        return successfulOperations;
    }

    @Override
    public long failure() {
        return failedOperations;
    }

    @Override
    public double successRate() {
        return successRate;
    }

    // ===== Advanced Health Metrics =====

    /**
     * Calculate uptime percentage based on success counts.
     * 
     * @return uptime percentage (0.0-100.0)
     */
    public double uptimePercent() {
        if (totalOperations == 0) {
            return 100.0; // No operations means fully available
        }
        return (successfulOperations * 100.0) / totalOperations;
    }

    /**
     * Get error rate as percentage.
     * 
     * @return error rate percentage (0.0-100.0)
     */
    public double errorRatePercent() {
        if (totalOperations == 0) {
            return 0.0;
        }
        return (failedOperations * 100.0) / totalOperations;
    }

    /**
     * Check if the provider is experiencing high error rates.
     * 
     * @return true if error rate exceeds 5%
     */
    public boolean hasHighErrorRate() {
        return errorRatePercent() > 5.0;
    }

    /**
     * Get time since last failure in milliseconds.
     * 
     * @return milliseconds since last failure, or -1 if no failures recorded
     */
    public long timeSinceLastFailureMs() {
        if (lastFailureTime == 0) {
            return -1;
        }
        return timestampMs - lastFailureTime;
    }

    /**
     * Get time since last success in milliseconds.
     * 
     * @return milliseconds since last success, or -1 if no successes recorded
     */
    public long timeSinceLastSuccessMs() {
        if (lastSuccessTime == 0) {
            return -1;
        }
        return timestampMs - lastSuccessTime;
    }

    /**
     * Calculate overall health score (0.0-1.0).
     * 
     * @return health score combining various health indicators
     */
    public double healthScore() {
        double uptimeScore = uptimePercent() / 100.0;

        // Reduce score based on time since last success
        double recentActivityScore = 1.0;
        long timeSinceSuccess = timeSinceLastSuccessMs();
        if (timeSinceSuccess > 300_000) { // 5 minutes
            recentActivityScore = Math.max(0.0, 1.0 - (timeSinceSuccess - 300_000) / 600_000.0); // Linear decay over 10
                                                                                                 // minutes
        }

        return (uptimeScore + recentActivityScore) / 2.0;
    }

    /**
     * Determine overall health status based on metrics.
     * 
     * @return calculated health status
     */
    public HealthStatus calculateHealthStatus() {
        double score = healthScore();
        if (score >= 0.9) {
            return HealthStatus.HEALTHY;
        } else if (score >= 0.7) {
            return HealthStatus.DEGRADED;
        } else if (score >= 0.3) {
            return HealthStatus.UNHEALTHY;
        } else {
            return HealthStatus.OFFLINE;
        }
    }

    /**
     * Check if immediate attention is required.
     * 
     * @return true if health score is critically low
     */
    public boolean requiresImmediateAttention() {
        return healthScore() < 0.3;
    }

    /**
     * Check if the provider is healthy based on multiple criteria.
     * 
     * @return true if uptime is above 95% and no high error rates
     */
    public boolean isHealthy() {
        return uptimePercent() >= 95.0 && !hasHighErrorRate();
    }

    /**
     * Builder for ProviderHealthSnapshot.
     */
    public static final class Builder {
        private HealthStatus status = HealthStatus.UNKNOWN;
        private String statusMessage = "";
        private long totalOperations = 0;
        private long successfulOperations = 0;
        private long failedOperations = 0;
        private double successRate = 0.0;
        private long lastFailureTime = 0L;
        private long lastSuccessTime = 0L;
        private String lastError = "";
        private long timestampMs = System.currentTimeMillis();

        public Builder() {
            // Default constructor
        }

        public Builder(ProviderHealthSnapshot source) {
            this.status = source.status;
            this.statusMessage = source.statusMessage;
            this.totalOperations = source.totalOperations;
            this.successfulOperations = source.successfulOperations;
            this.failedOperations = source.failedOperations;
            this.successRate = source.successRate;
            this.lastFailureTime = source.lastFailureTime;
            this.lastSuccessTime = source.lastSuccessTime;
            this.lastError = source.lastError;
            this.timestampMs = source.timestampMs;
        }

        public Builder withStatus(HealthStatus status) {
            this.status = Objects.requireNonNull(status, "status");
            return this;
        }

        public Builder withStatusMessage(String statusMessage) {
            this.statusMessage = Objects.requireNonNull(statusMessage, "statusMessage");
            return this;
        }

        public Builder withTotalOperations(long totalOperations) {
            this.totalOperations = totalOperations;
            return this;
        }

        public Builder withSuccessfulOperations(long successfulOperations) {
            this.successfulOperations = successfulOperations;
            return this;
        }

        public Builder withFailedOperations(long failedOperations) {
            this.failedOperations = failedOperations;
            return this;
        }

        public Builder withSuccessRate(double successRate) {
            this.successRate = successRate;
            return this;
        }

        public Builder withLastFailureTime(long lastFailureTime) {
            this.lastFailureTime = lastFailureTime;
            return this;
        }

        public Builder withLastSuccessTime(long lastSuccessTime) {
            this.lastSuccessTime = lastSuccessTime;
            return this;
        }

        public Builder withLastError(String lastError) {
            this.lastError = Objects.requireNonNull(lastError, "lastError");
            return this;
        }

        public Builder withTimestampMs(long timestampMs) {
            this.timestampMs = timestampMs;
            return this;
        }

        public ProviderHealthSnapshot build() {
            return new ProviderHealthSnapshot(status, statusMessage, totalOperations, successfulOperations,
                    failedOperations, successRate, lastFailureTime, lastSuccessTime, lastError, timestampMs);
        }
    }

    // ===== Static Factory Methods =====

    /**
     * Create a new builder for ProviderHealthSnapshot.
     *
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Create a new builder for ProviderHealthSnapshot from an existing instance.
     *
     * @param source the source instance to copy from
     * @return a new builder instance initialized with the source's values
     */
    public static Builder builder(ProviderHealthSnapshot source) {
        return new Builder(source);
    }

    // ===== Object Contract Methods =====

    @Override
    public String toString() {
        return new StringBuilder("ProviderHealthSnapshot{").append("status=").append(status).append(", uptime=")
                .append(String.format("%.2f%%", uptimePercent())).append(", errorRate=")
                .append(String.format("%.2f%%", errorRatePercent())).append(", totalOps=").append(totalOperations)
                .append(", healthScore=").append(String.format("%.3f", healthScore())).append(", requiresAttention=")
                .append(requiresImmediateAttention()).append(", lastError='").append(lastError).append("'")
                .append(", timestamp=").append(getTimestamp()).append("}").toString();
    }

    /**
     * Create a concise summary string for logging.
     * 
     * @return concise summary string
     */
    public String toSummary() {
        return String.format("Health[%s, %.1f%% up, %.1f%% err, score=%.2f%s]", status, uptimePercent(),
                errorRatePercent(), healthScore(), requiresImmediateAttention() ? " ⚠" : "");
    }
}
