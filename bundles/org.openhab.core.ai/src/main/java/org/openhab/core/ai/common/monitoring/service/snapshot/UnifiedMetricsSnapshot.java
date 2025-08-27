package org.openhab.core.ai.common.monitoring.service.snapshot;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.Counts;
import org.openhab.core.ai.common.monitoring.api.HealthMetrics;
import org.openhab.core.ai.common.monitoring.api.HealthStatus;
import org.openhab.core.ai.common.monitoring.api.PerformanceMetrics;
import org.openhab.core.ai.common.monitoring.api.StatisticsMetrics;
import org.openhab.core.ai.common.monitoring.api.Timing;
import org.openhab.core.ai.common.monitoring.api.UnifiedMetrics;

/**
 * Unified metrics snapshot that implements all monitoring capabilities.
 * 
 * <p>
 * This class provides a complete replacement for the Monitoring hierarchy,
 * implementing all capability interfaces in a single, unified approach.
 * It serves as the foundation for migrating from the old Monitoring system
 * to the new Metrics system.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class UnifiedMetricsSnapshot implements UnifiedMetrics {

    private final String id;
    private final String domain;
    private final String operation;
    private final @Nullable String source;
    private final long timestampMs;
    private final Map<String, Object> rawData;
    private final Counts counts;
    private final Timing timing;
    private final HealthStatus healthStatus;
    private final @Nullable String statusMessage;
    private final @Nullable Map<String, Object> healthIndicators;
    private final @Nullable Instant monitoringStartTime;
    private final @Nullable Instant monitoringEndTime;

    // ===== Constructors =====

    /**
     * Create a new unified metrics snapshot.
     * 
     * @param id the unique identifier
     * @param domain the domain name
     * @param operation the operation name
     * @param source the source component (optional)
     * @param rawData the raw metrics data
     * @param counts the counts data
     * @param timing the timing data
     * @param healthStatus the health status
     * @param statusMessage the status message (optional)
     * @param healthIndicators the health indicators (optional)
     * @param monitoringStartTime the monitoring start time (optional)
     * @param monitoringEndTime the monitoring end time (optional)
     */
    public UnifiedMetricsSnapshot(
            String id,
            String domain,
            String operation,
            @Nullable String source,
            Map<String, Object> rawData,
            Counts counts,
            Timing timing,
            HealthStatus healthStatus,
            @Nullable String statusMessage,
            @Nullable Map<String, Object> healthIndicators,
            @Nullable Instant monitoringStartTime,
            @Nullable Instant monitoringEndTime) {
        
        this.id = id;
        this.domain = domain;
        this.operation = operation;
        this.source = source;
        this.timestampMs = System.currentTimeMillis();
        this.rawData = new HashMap<>(rawData); // Defensive copy
        this.counts = counts;
        this.timing = timing;
        this.healthStatus = healthStatus;
        this.statusMessage = statusMessage;
        this.healthIndicators = healthIndicators != null ? new HashMap<>(healthIndicators) : null;
        this.monitoringStartTime = monitoringStartTime;
        this.monitoringEndTime = monitoringEndTime;
    }

    // ===== UnifiedMetrics Implementation =====

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDomain() {
        return domain;
    }

    @Override
    public String getOperation() {
        return operation;
    }

    @Override
    public @Nullable String getSource() {
        return source;
    }

    @Override
    public @Nullable Map<String, Object> getRawData() {
        return rawData != null ? new HashMap<>(rawData) : null;
    }

    @Override
    public long getTimestampMs() {
        return timestampMs;
    }

    // ===== CountsMetrics Implementation =====

    @Override
    public long total() {
        return counts.total();
    }

    @Override
    public long success() {
        return counts.success();
    }

    @Override
    public long failure() {
        return counts.failure();
    }

    @Override
    public double successRate() {
        long total = counts.total();
        if (total == 0) {
            return 0.0;
        }
        return (double) counts.success() / total * 100.0;
    }

    // ===== LatencyMetrics Implementation =====

    @Override
    public long totalDurationNanos() {
        return timing.totalDurationNanos();
    }

    @Override
    public double averageMs(long total) {
        if (total == 0) {
            return 0.0;
        }
        return (double) timing.totalDurationNanos() / total / 1_000_000.0;
    }

    // ===== HealthMetrics Implementation =====

    @Override
    public HealthStatus healthStatus() {
        return healthStatus;
    }

    @Override
    public @Nullable String statusMessage() {
        return statusMessage;
    }

    @Override
    public @Nullable Map<String, Object> healthIndicators() {
        return healthIndicators != null ? new HashMap<>(healthIndicators) : null;
    }

    // ===== PerformanceMetrics Implementation =====

    @Override
    public long totalOperations() {
        return counts.total();
    }

    @Override
    public long successfulOperations() {
        return counts.success();
    }

    @Override
    public long failedOperations() {
        return counts.failure();
    }

    @Override
    public long totalProcessingTimeNanos() {
        return timing.totalDurationNanos();
    }

    @Override
    public @Nullable Instant monitoringStartTime() {
        return monitoringStartTime;
    }

    @Override
    public @Nullable Instant monitoringEndTime() {
        return monitoringEndTime;
    }

    // ===== StatisticsMetrics Implementation =====

    @Override
    public long totalCount() {
        return counts.total();
    }

    @Override
    public long successCount() {
        return counts.success();
    }

    @Override
    public long failureCount() {
        return counts.failure();
    }

    @Override
    public @Nullable Instant collectionStartTime() {
        return monitoringStartTime;
    }

    @Override
    public @Nullable Instant collectionEndTime() {
        return monitoringEndTime;
    }

    // ===== Builder Pattern =====

    /**
     * Builder for UnifiedMetricsSnapshot.
     */
    public static final class Builder {
        private String id;
        private String domain;
        private String operation;
        private @Nullable String source;
        private Map<String, Object> rawData = new HashMap<>();
        private Counts counts = new Counts(0L, 0L, 0L);
        private Timing timing = new Timing(0L);
        private HealthStatus healthStatus = HealthStatus.UNKNOWN;
        private @Nullable String statusMessage;
        private @Nullable Map<String, Object> healthIndicators;
        private @Nullable Instant monitoringStartTime;
        private @Nullable Instant monitoringEndTime;

        public Builder(String id, String domain, String operation) {
            this.id = id;
            this.domain = domain;
            this.operation = operation;
        }

        public Builder withSource(@Nullable String source) {
            this.source = source;
            return this;
        }

        public Builder withRawData(Map<String, Object> rawData) {
            this.rawData = new HashMap<>(rawData);
            return this;
        }

        public Builder withCounts(Counts counts) {
            this.counts = counts;
            return this;
        }

        public Builder withTiming(Timing timing) {
            this.timing = timing;
            return this;
        }

        public Builder withHealthStatus(HealthStatus healthStatus) {
            this.healthStatus = healthStatus;
            return this;
        }

        public Builder withStatusMessage(@Nullable String statusMessage) {
            this.statusMessage = statusMessage;
            return this;
        }

        public Builder withHealthIndicators(@Nullable Map<String, Object> healthIndicators) {
            this.healthIndicators = healthIndicators != null ? new HashMap<>(healthIndicators) : null;
            return this;
        }

        public Builder withMonitoringStartTime(@Nullable Instant monitoringStartTime) {
            this.monitoringStartTime = monitoringStartTime;
            return this;
        }

        public Builder withMonitoringEndTime(@Nullable Instant monitoringEndTime) {
            this.monitoringEndTime = monitoringEndTime;
            return this;
        }

        public UnifiedMetricsSnapshot build() {
            return new UnifiedMetricsSnapshot(
                id, domain, operation, source, rawData, counts, timing,
                healthStatus, statusMessage, healthIndicators,
                monitoringStartTime, monitoringEndTime);
        }
    }

    /**
     * Create a new builder for UnifiedMetricsSnapshot.
     * 
     * @param id the unique identifier
     * @param domain the domain name
     * @param operation the operation name
     * @return the builder
     */
    public static Builder builder(String id, String domain, String operation) {
        return new Builder(id, domain, operation);
    }
}
