package org.openhab.core.ai.common.statistics;

import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Service statistics for tracking service performance and health.
 * 
 * This class provides comprehensive metrics for service monitoring including
 * request counts, error rates, health status, and timing information.
 * This unified class replaces the duplicate HybridServiceMetrics implementations.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ServiceStatistics extends BaseStatistics {
    private final String serviceId;
    private final String serviceName;
    private final String serviceType;
    private final long requestCount;
    private final long errorCount;
    private final boolean isHealthy;
    private final long lastRequestTime;
    private final long lastErrorTime;
    private final long registrationTime;

    private ServiceStatistics(Builder builder) {
        super(builder.id != null ? builder.id : "service-" + builder.serviceId, StatisticsType.MONITORING,
                builder.metrics);
        this.serviceId = Objects.requireNonNull(builder.serviceId, "serviceId");
        this.serviceName = Objects.requireNonNull(builder.serviceName, "serviceName");
        this.serviceType = Objects.requireNonNull(builder.serviceType, "serviceType");
        this.requestCount = builder.requestCount;
        this.errorCount = builder.errorCount;
        this.isHealthy = builder.isHealthy;
        this.lastRequestTime = builder.lastRequestTime;
        this.lastErrorTime = builder.lastErrorTime;
        this.registrationTime = builder.registrationTime;
    }

    /**
     * Get the service ID.
     * 
     * @return service ID
     */
    public String getServiceId() {
        return serviceId;
    }

    /**
     * Get the service name.
     * 
     * @return service name
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * Get the service type.
     * 
     * @return service type
     */
    public String getServiceType() {
        return serviceType;
    }

    /**
     * Get the total request count.
     * 
     * @return request count
     */
    public long getRequestCount() {
        return requestCount;
    }

    /**
     * Get the total error count.
     * 
     * @return error count
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
     * @return last request time
     */
    public long getLastRequestTime() {
        return lastRequestTime;
    }

    /**
     * Get the last error time.
     * 
     * @return last error time
     */
    public long getLastErrorTime() {
        return lastErrorTime;
    }

    /**
     * Get the registration time.
     * 
     * @return registration time
     */
    public long getRegistrationTime() {
        return registrationTime;
    }

    /**
     * Calculate success rate.
     * 
     * @return success rate as a percentage (0.0 to 100.0)
     */
    public double getSuccessRate() {
        return requestCount > 0 ? (double) (requestCount - errorCount) / requestCount * 100.0 : 0.0;
    }

    /**
     * Create a new builder.
     * 
     * @return builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ServiceStatistics other = (ServiceStatistics) obj;
        return requestCount == other.requestCount && errorCount == other.errorCount && isHealthy == other.isHealthy
                && lastRequestTime == other.lastRequestTime && lastErrorTime == other.lastErrorTime
                && registrationTime == other.registrationTime && Objects.equals(serviceId, other.serviceId)
                && Objects.equals(serviceName, other.serviceName) && Objects.equals(serviceType, other.serviceType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceId, serviceName, serviceType, requestCount, errorCount, isHealthy, lastRequestTime,
                lastErrorTime, registrationTime);
    }

    @Override
    public String toString() {
        return "ServiceStatistics{" + "serviceId='" + serviceId + '\'' + ", serviceName='" + serviceName + '\''
                + ", serviceType='" + serviceType + '\'' + ", requestCount=" + requestCount + ", errorCount="
                + errorCount + ", isHealthy=" + isHealthy + ", lastRequestTime=" + lastRequestTime + ", lastErrorTime="
                + lastErrorTime + ", registrationTime=" + registrationTime + '}';
    }

    /**
     * Builder for ServiceStatistics.
     */
    public static final class Builder {
        private @Nullable String id;
        private String serviceId = "";
        private String serviceName = "";
        private String serviceType = "";
        private long requestCount = 0;
        private long errorCount = 0;
        private boolean isHealthy = true;
        private long lastRequestTime = 0;
        private long lastErrorTime = 0;
        private long registrationTime = 0;
        private Map<String, Object> metrics = Map.of();

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withServiceId(String serviceId) {
            this.serviceId = serviceId;
            return this;
        }

        public Builder withServiceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        public Builder withServiceType(String serviceType) {
            this.serviceType = serviceType;
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
            this.metrics = metrics;
            return this;
        }

        public ServiceStatistics build() {
            if (serviceId.isEmpty()) {
                throw new IllegalArgumentException("serviceId cannot be empty");
            }
            if (serviceName.isEmpty()) {
                throw new IllegalArgumentException("serviceName cannot be empty");
            }
            if (serviceType.isEmpty()) {
                throw new IllegalArgumentException("serviceType cannot be empty");
            }
            if (requestCount < 0) {
                throw new IllegalArgumentException("requestCount cannot be negative");
            }
            if (errorCount < 0) {
                throw new IllegalArgumentException("errorCount cannot be negative");
            }
            if (errorCount > requestCount) {
                throw new IllegalArgumentException("errorCount cannot be greater than requestCount");
            }
            return new ServiceStatistics(this);
        }
    }
}
