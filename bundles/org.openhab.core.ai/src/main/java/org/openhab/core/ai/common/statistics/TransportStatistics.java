package org.openhab.core.ai.common.statistics;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.builder.AbstractBuilder;

/**
 * Unified Transport Statistics implementation.
 *
 * <p>
 * This class provides comprehensive statistics for transport operations,
 * including servlet information, request counts, error rates, and health status.
 * </p>
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class TransportStatistics extends BaseStatistics {

    private final String transportId;
    private final String transportName;
    private final String transportType;
    private final String protocol;
    private final long registrationTime;
    private final long requestCount;
    private final long errorCount;
    private final boolean isHealthy;
    private final long lastRequestTime;
    private final long lastErrorTime;
    private final long totalResponseTimeMs;
    private final long averageResponseTimeMs;
    private final @Nullable String lastError;

    private TransportStatistics(Builder builder) {
        super(builder.id, builder.timestamp, StatisticsType.TRANSPORT, builder.metrics);
        this.transportId = builder.transportId;
        this.transportName = builder.transportName;
        this.transportType = builder.transportType;
        this.protocol = builder.protocol;
        this.registrationTime = builder.registrationTime;
        this.requestCount = builder.requestCount;
        this.errorCount = builder.errorCount;
        this.isHealthy = builder.isHealthy;
        this.lastRequestTime = builder.lastRequestTime;
        this.lastErrorTime = builder.lastErrorTime;
        this.totalResponseTimeMs = builder.totalResponseTimeMs;
        this.averageResponseTimeMs = builder.averageResponseTimeMs;
        this.lastError = builder.lastError;
    }

    public String getTransportId() {
        return transportId;
    }

    public String getTransportName() {
        return transportName;
    }

    public String getTransportType() {
        return transportType;
    }

    public String getProtocol() {
        return protocol;
    }

    public long getRegistrationTime() {
        return registrationTime;
    }

    public long getRequestCount() {
        return requestCount;
    }

    public long getErrorCount() {
        return errorCount;
    }

    public boolean isHealthy() {
        return isHealthy;
    }

    public long getLastRequestTime() {
        return lastRequestTime;
    }

    public long getLastErrorTime() {
        return lastErrorTime;
    }

    public long getTotalResponseTimeMs() {
        return totalResponseTimeMs;
    }

    public long getAverageResponseTimeMs() {
        return averageResponseTimeMs;
    }

    public @Nullable String getLastError() {
        return lastError;
    }

    public double getSuccessRate() {
        return requestCount > 0 ? (double) (requestCount - errorCount) / requestCount * 100.0 : 0.0;
    }

    public double getErrorRate() {
        return requestCount > 0 ? (double) errorCount / requestCount * 100.0 : 0.0;
    }

    public long getUptimeMs() {
        return System.currentTimeMillis() - registrationTime;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj) || getClass() != obj.getClass()) {
            return false;
        }
        TransportStatistics other = (TransportStatistics) obj;
        return Objects.equals(transportId, other.transportId) && Objects.equals(transportName, other.transportName)
                && Objects.equals(transportType, other.transportType) && Objects.equals(protocol, other.protocol)
                && registrationTime == other.registrationTime && requestCount == other.requestCount
                && errorCount == other.errorCount && isHealthy == other.isHealthy
                && lastRequestTime == other.lastRequestTime && lastErrorTime == other.lastErrorTime
                && totalResponseTimeMs == other.totalResponseTimeMs
                && averageResponseTimeMs == other.averageResponseTimeMs && Objects.equals(lastError, other.lastError);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), transportId, transportName, transportType, protocol, registrationTime,
                requestCount, errorCount, isHealthy, lastRequestTime, lastErrorTime, totalResponseTimeMs,
                averageResponseTimeMs, lastError);
    }

    @Override
    public String toString() {
        return String.format(
                "TransportStatistics{id='%s', transportId='%s', transportName='%s', protocol='%s', healthy=%s, requests=%d, errors=%d, successRate=%.2f%%}",
                getId(), transportId, transportName, protocol, isHealthy, requestCount, errorCount, getSuccessRate());
    }

    public static final class Builder extends AbstractBuilder<TransportStatistics> {
        private String id = "";
        private @Nullable Instant timestamp;
        private final Map<String, Object> metrics = new HashMap<>();
        private String transportId = "";
        private String transportName = "";
        private String transportType = "";
        private String protocol = "";
        private long registrationTime = 0;
        private long requestCount = 0;
        private long errorCount = 0;
        private boolean isHealthy = true;
        private long lastRequestTime = 0;
        private long lastErrorTime = 0;
        private long totalResponseTimeMs = 0;
        private long averageResponseTimeMs = 0;
        private @Nullable String lastError;

        public Builder() {
        }

        public Builder(TransportStatistics source) {
            this.id = source.getId();
            this.timestamp = source.getTimestamp();
            this.metrics.putAll(source.getMetrics());
            this.transportId = source.transportId;
            this.transportName = source.transportName;
            this.transportType = source.transportType;
            this.protocol = source.protocol;
            this.registrationTime = source.registrationTime;
            this.requestCount = source.requestCount;
            this.errorCount = source.errorCount;
            this.isHealthy = source.isHealthy;
            this.lastRequestTime = source.lastRequestTime;
            this.lastErrorTime = source.lastErrorTime;
            this.totalResponseTimeMs = source.totalResponseTimeMs;
            this.averageResponseTimeMs = source.averageResponseTimeMs;
            this.lastError = source.lastError;
        }

        public Builder withId(String id) {
            this.id = Objects.requireNonNull(id, "id");
            return this;
        }

        public Builder withTimestamp(@Nullable Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder withTransportId(String transportId) {
            this.transportId = Objects.requireNonNull(transportId, "transportId");
            return this;
        }

        public Builder withTransportName(String transportName) {
            this.transportName = Objects.requireNonNull(transportName, "transportName");
            return this;
        }

        public Builder withTransportType(String transportType) {
            this.transportType = Objects.requireNonNull(transportType, "transportType");
            return this;
        }

        public Builder withProtocol(String protocol) {
            this.protocol = Objects.requireNonNull(protocol, "protocol");
            return this;
        }

        public Builder withRegistrationTime(long registrationTime) {
            this.registrationTime = registrationTime;
            return this;
        }

        public Builder withRequestCount(long requestCount) {
            this.requestCount = requestCount;
            return this;
        }

        public Builder withErrorCount(long errorCount) {
            this.errorCount = errorCount;
            return this;
        }

        public Builder withHealthy(boolean isHealthy) {
            this.isHealthy = isHealthy;
            return this;
        }

        public Builder withLastRequestTime(long lastRequestTime) {
            this.lastRequestTime = lastRequestTime;
            return this;
        }

        public Builder withLastErrorTime(long lastErrorTime) {
            this.lastErrorTime = lastErrorTime;
            return this;
        }

        public Builder withTotalResponseTimeMs(long totalResponseTimeMs) {
            this.totalResponseTimeMs = totalResponseTimeMs;
            return this;
        }

        public Builder withAverageResponseTimeMs(long averageResponseTimeMs) {
            this.averageResponseTimeMs = averageResponseTimeMs;
            return this;
        }

        public Builder withLastError(@Nullable String lastError) {
            this.lastError = lastError;
            return this;
        }

        @Override
        public TransportStatistics build() {
            validate();
            populateMetrics();
            return new TransportStatistics(this);
        }

        @Override
        protected void validate() {
            if (id.trim().isEmpty()) {
                addValidationError("id cannot be blank");
            }
            if (transportId.trim().isEmpty()) {
                addValidationError("transportId cannot be blank");
            }
            if (transportName.trim().isEmpty()) {
                addValidationError("transportName cannot be blank");
            }
            if (transportType.trim().isEmpty()) {
                addValidationError("transportType cannot be blank");
            }
            if (protocol.trim().isEmpty()) {
                addValidationError("protocol cannot be blank");
            }
            if (registrationTime < 0) {
                addValidationError("registrationTime must be non-negative");
            }
            if (requestCount < 0) {
                addValidationError("requestCount must be non-negative");
            }
            if (errorCount < 0) {
                addValidationError("errorCount must be non-negative");
            }
            if (errorCount > requestCount) {
                addValidationError("errorCount cannot exceed requestCount");
            }
            if (lastRequestTime < 0) {
                addValidationError("lastRequestTime must be non-negative");
            }
            if (lastErrorTime < 0) {
                addValidationError("lastErrorTime must be non-negative");
            }
            if (totalResponseTimeMs < 0) {
                addValidationError("totalResponseTimeMs must be non-negative");
            }
            if (averageResponseTimeMs < 0) {
                addValidationError("averageResponseTimeMs must be non-negative");
            }
        }

        @Override
        protected void doReset() {
            id = "";
            timestamp = null;
            metrics.clear();
            transportId = "";
            transportName = "";
            transportType = "";
            protocol = "";
            registrationTime = 0;
            requestCount = 0;
            errorCount = 0;
            isHealthy = true;
            lastRequestTime = 0;
            lastErrorTime = 0;
            totalResponseTimeMs = 0;
            averageResponseTimeMs = 0;
            lastError = null;
        }

        private void populateMetrics() {
            metrics.put("transportId", transportId);
            metrics.put("transportName", transportName);
            metrics.put("transportType", transportType);
            metrics.put("protocol", protocol);
            metrics.put("registrationTime", registrationTime);
            metrics.put("requestCount", requestCount);
            metrics.put("errorCount", errorCount);
            metrics.put("isHealthy", isHealthy);
            metrics.put("lastRequestTime", lastRequestTime);
            metrics.put("lastErrorTime", lastErrorTime);
            metrics.put("totalResponseTimeMs", totalResponseTimeMs);
            metrics.put("averageResponseTimeMs", averageResponseTimeMs);
            metrics.put("lastError", lastError);
            metrics.put("successRate",
                    requestCount > 0 ? (double) (requestCount - errorCount) / requestCount * 100.0 : 0.0);
            metrics.put("errorRate", requestCount > 0 ? (double) errorCount / requestCount * 100.0 : 0.0);
            metrics.put("uptimeMs", System.currentTimeMillis() - registrationTime);
        }
    }
}
