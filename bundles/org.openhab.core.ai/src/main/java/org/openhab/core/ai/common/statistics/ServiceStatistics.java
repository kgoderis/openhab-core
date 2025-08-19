package org.openhab.core.ai.common.statistics;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.builder.AbstractBuilder;

/**
 * Unified statistics for service operations in the openHAB AI system.
 *
 * <p>
 * This class provides comprehensive statistics for service operations including
 * request counts, error counts, health status, and timing information.
 * </p>
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class ServiceStatistics extends BaseStatistics {

    private final String serviceId;
    private final String serviceName;
    private final String serviceType;
    private final long requestCount;
    private final long errorCount;
    private final boolean isHealthy;
    private final long lastRequestTime;
    private final long lastErrorTime;
    private final long registrationTime;

    /**
     * Create a new ServiceStatistics instance.
     *
     * @param id the statistics identifier
     * @param timestamp the collection timestamp
     * @param serviceId the service identifier
     * @param serviceName the service name
     * @param serviceType the service type
     * @param requestCount the total request count
     * @param errorCount the total error count
     * @param isHealthy the health status
     * @param lastRequestTime the last request time
     * @param lastErrorTime the last error time
     * @param registrationTime the registration time
     * @param metrics additional metrics
     */
    public ServiceStatistics(String id, @Nullable Instant timestamp, String serviceId, String serviceName,
            String serviceType, long requestCount, long errorCount, boolean isHealthy, long lastRequestTime,
            long lastErrorTime, long registrationTime, Map<String, Object> metrics) {
        super(id, timestamp, StatisticsType.MONITORING, metrics);
        this.serviceId = Objects.requireNonNull(serviceId, "serviceId");
        this.serviceName = Objects.requireNonNull(serviceName, "serviceName");
        this.serviceType = Objects.requireNonNull(serviceType, "serviceType");
        this.requestCount = requestCount;
        this.errorCount = errorCount;
        this.isHealthy = isHealthy;
        this.lastRequestTime = lastRequestTime;
        this.lastErrorTime = lastErrorTime;
        this.registrationTime = registrationTime;
    }

    /**
     * Get the service identifier.
     *
     * @return the service identifier
     */
    public String getServiceId() {
        return serviceId;
    }

    /**
     * Get the service name.
     *
     * @return the service name
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * Get the service type.
     *
     * @return the service type
     */
    public String getServiceType() {
        return serviceType;
    }

    /**
     * Get the total request count.
     *
     * @return the request count
     */
    public long getRequestCount() {
        return requestCount;
    }

    /**
     * Get the total error count.
     *
     * @return the error count
     */
    public long getErrorCount() {
        return errorCount;
    }

    /**
     * Check if the service is healthy.
     *
     * @return true if healthy
     */
    public boolean isHealthy() {
        return isHealthy;
    }

    /**
     * Get the last request time.
     *
     * @return the last request time
     */
    public long getLastRequestTime() {
        return lastRequestTime;
    }

    /**
     * Get the last error time.
     *
     * @return the last error time
     */
    public long getLastErrorTime() {
        return lastErrorTime;
    }

    /**
     * Get the registration time.
     *
     * @return the registration time
     */
    public long getRegistrationTime() {
        return registrationTime;
    }

    /**
     * Calculate the success rate.
     *
     * @return the success rate (0.0 to 100.0)
     */
    public double getSuccessRate() {
        if (requestCount == 0) {
            return 0.0;
        }
        return (double) (requestCount - errorCount) / requestCount * 100.0;
    }

    /**
     * Create a new builder for ServiceStatistics.
     *
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for ServiceStatistics.
     */
    public static final class Builder extends AbstractBuilder<ServiceStatistics> {

        private @Nullable String id;
        private @Nullable Instant timestamp;
        private String serviceId = "";
        private String serviceName = "";
        private String serviceType = "unknown";
        private long requestCount = 0L;
        private long errorCount = 0L;
        private boolean isHealthy = true;
        private long lastRequestTime = 0L;
        private long lastErrorTime = 0L;
        private long registrationTime = System.currentTimeMillis();
        private Map<String, Object> metrics = Map.of();

        public Builder withId(String id) {
            this.id = Objects.requireNonNull(id, "id");
            return this;
        }

        public Builder withTimestamp(@Nullable Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder withServiceId(String serviceId) {
            this.serviceId = Objects.requireNonNull(serviceId, "serviceId");
            return this;
        }

        public Builder withServiceName(String serviceName) {
            this.serviceName = Objects.requireNonNull(serviceName, "serviceName");
            return this;
        }

        public Builder withServiceType(String serviceType) {
            this.serviceType = Objects.requireNonNull(serviceType, "serviceType");
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

        public Builder withRegistrationTime(long registrationTime) {
            this.registrationTime = registrationTime;
            return this;
        }

        public Builder withMetrics(Map<String, Object> metrics) {
            this.metrics = Objects.requireNonNull(metrics, "metrics");
            return this;
        }

        @Override
        protected void validate() {
            validateRequiredString(serviceId, "serviceId");
            validateRequiredString(serviceName, "serviceName");
            validateRequiredString(serviceType, "serviceType");
            if (requestCount < 0) {
                addValidationError("requestCount must be >= 0");
            }
            if (errorCount < 0) {
                addValidationError("errorCount must be >= 0");
            }
            if (errorCount > requestCount) {
                addValidationError("errorCount cannot exceed requestCount");
            }
        }

        @Override
        protected void doReset() {
            id = null;
            timestamp = null;
            serviceId = "";
            serviceName = "";
            serviceType = "unknown";
            requestCount = 0L;
            errorCount = 0L;
            isHealthy = true;
            lastRequestTime = 0L;
            lastErrorTime = 0L;
            registrationTime = System.currentTimeMillis();
            metrics = Map.of();
        }

        @Override
        public ServiceStatistics build() {
            if (!isValid()) {
                throw new IllegalArgumentException("Invalid ServiceStatisticsBuilder state: " + getValidationErrors());
            }
            String resolvedId = id != null ? id
                    : ("service-stats-" + System.currentTimeMillis() + "-" + System.nanoTime());
            return new ServiceStatistics(resolvedId, timestamp, serviceId, serviceName, serviceType, requestCount,
                    errorCount, isHealthy, lastRequestTime, lastErrorTime, registrationTime, metrics);
        }
    }
}
