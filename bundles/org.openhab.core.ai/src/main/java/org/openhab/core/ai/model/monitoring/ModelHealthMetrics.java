package org.openhab.core.ai.model.monitoring;

import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.Health;
import org.openhab.core.ai.common.monitoring.api.MonitoringType;
import org.openhab.core.ai.common.monitoring.base.AbstractMonitoring;

/**
 * Health metrics for model clients and providers.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ModelHealthMetrics extends AbstractMonitoring implements Health {

    private final HealthStatus status;
    private final @Nullable String statusMessage;
    private final @Nullable Map<String, Object> healthIndicators;
    private final boolean available;
    private final long averageResponseTimeMs;
    private final double successRate;
    private final int errorCount;
    private final @Nullable String lastError;
    private final @Nullable Instant lastErrorTime;

    /**
     * Create a new ModelHealthMetrics instance.
     */
    public ModelHealthMetrics(String id, Instant timestamp, String domain, @Nullable String source,
            @Nullable String description, @Nullable Map<String, Object> data, HealthStatus status,
            @Nullable String statusMessage, @Nullable Map<String, Object> healthIndicators, boolean available,
            long averageResponseTimeMs, double successRate, int errorCount, @Nullable String lastError,
            @Nullable Instant lastErrorTime) {
        super(id, timestamp, domain, source, description, data);
        this.status = status;
        this.statusMessage = statusMessage;
        this.healthIndicators = healthIndicators;
        this.available = available;
        this.averageResponseTimeMs = averageResponseTimeMs;
        this.successRate = successRate;
        this.errorCount = errorCount;
        this.lastError = lastError;
        this.lastErrorTime = lastErrorTime;
    }

    @Override
    public MonitoringType getType() {
        return MonitoringType.HEALTH;
    }

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

    /**
     * Check if the model is available.
     * 
     * @return true if available
     */
    public boolean isAvailable() {
        return available;
    }

    /**
     * Get the average response time in milliseconds.
     * 
     * @return Average response time in ms
     */
    public long getAverageResponseTimeMs() {
        return averageResponseTimeMs;
    }

    /**
     * Get the success rate as a percentage (0.0 to 1.0).
     * 
     * @return Success rate
     */
    public double getSuccessRate() {
        return successRate;
    }

    /**
     * Get the number of errors encountered.
     * 
     * @return Error count
     */
    public int getErrorCount() {
        return errorCount;
    }

    /**
     * Get the last error message.
     * 
     * @return Last error message, or null if no errors
     */
    public @Nullable String getLastError() {
        return lastError;
    }

    /**
     * Get the time of the last error.
     * 
     * @return Last error time, or null if no errors
     */
    public @Nullable Instant getLastErrorTime() {
        return lastErrorTime;
    }

    /**
     * Builder for ModelHealthMetrics.
     */
    public static final class Builder {
        private String id;
        private Instant timestamp = Instant.now();
        private String domain = "model";
        private @Nullable String source;
        private @Nullable String description;
        private @Nullable Map<String, Object> data;
        private HealthStatus status = HealthStatus.UNKNOWN;
        private @Nullable String statusMessage;
        private @Nullable Map<String, Object> healthIndicators;
        private boolean available = false;
        private long averageResponseTimeMs = 0;
        private double successRate = 0.0;
        private int errorCount = 0;
        private @Nullable String lastError;
        private @Nullable Instant lastErrorTime;

        public Builder(String id) {
            this.id = id;
        }

        public Builder withTimestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder withDomain(String domain) {
            this.domain = domain;
            return this;
        }

        public Builder withSource(@Nullable String source) {
            this.source = source;
            return this;
        }

        public Builder withDescription(@Nullable String description) {
            this.description = description;
            return this;
        }

        public Builder withData(@Nullable Map<String, Object> data) {
            this.data = data;
            return this;
        }

        public Builder withStatus(HealthStatus status) {
            this.status = status;
            return this;
        }

        public Builder withStatusMessage(@Nullable String statusMessage) {
            this.statusMessage = statusMessage;
            return this;
        }

        public Builder withHealthIndicators(@Nullable Map<String, Object> healthIndicators) {
            this.healthIndicators = healthIndicators;
            return this;
        }

        public Builder withAvailable(boolean available) {
            this.available = available;
            return this;
        }

        public Builder withAverageResponseTimeMs(long averageResponseTimeMs) {
            this.averageResponseTimeMs = averageResponseTimeMs;
            return this;
        }

        public Builder withSuccessRate(double successRate) {
            this.successRate = successRate;
            return this;
        }

        public Builder withErrorCount(int errorCount) {
            this.errorCount = errorCount;
            return this;
        }

        public Builder withLastError(@Nullable String lastError) {
            this.lastError = lastError;
            return this;
        }

        public Builder withLastErrorTime(@Nullable Instant lastErrorTime) {
            this.lastErrorTime = lastErrorTime;
            return this;
        }

        public ModelHealthMetrics build() {
            return new ModelHealthMetrics(id, timestamp, domain, source, description, data, status, statusMessage,
                    healthIndicators, available, averageResponseTimeMs, successRate, errorCount, lastError,
                    lastErrorTime);
        }
    }

    /**
     * Create a builder for ModelHealthMetrics.
     * 
     * @param id the unique identifier
     * @return a new builder instance
     */
    public static Builder builder(String id) {
        return new Builder(id);
    }
}
