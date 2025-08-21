package org.openhab.core.ai.model.monitoring;

import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.base.AbstractStatistics;

/**
 * Statistics for model integration operations.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ModelIntegrationStatistics extends AbstractStatistics implements CountsMetrics {

    private final long totalIntegrations;
    private final long successfulIntegrations;
    private final long failedIntegrations;
    private final long totalProcessingTime;
    private final double averageIntegrationTime;
    private final @Nullable Instant lastIntegrationTime;

    /**
     * Create a new ModelIntegrationStatistics instance.
     */
    public ModelIntegrationStatistics(String id, Instant timestamp, String domain, @Nullable String source,
            @Nullable String description, @Nullable Map<String, Object> data, long totalCount, long successCount,
            long failureCount, @Nullable Instant collectionStartTime, @Nullable Instant collectionEndTime,
            @Nullable Map<String, Double> additionalMeasures, long totalIntegrations, long successfulIntegrations,
            long failedIntegrations, long totalProcessingTime, double averageIntegrationTime,
            @Nullable Instant lastIntegrationTime) {
        super(id, timestamp, domain, source, description, data, totalCount, successCount, failureCount,
                collectionStartTime, collectionEndTime, additionalMeasures);
        this.totalIntegrations = totalIntegrations;
        this.successfulIntegrations = successfulIntegrations;
        this.failedIntegrations = failedIntegrations;
        this.totalProcessingTime = totalProcessingTime;
        this.averageIntegrationTime = averageIntegrationTime;
        this.lastIntegrationTime = lastIntegrationTime;
    }

    /**
     * Get the total number of integrations.
     * 
     * @return total integrations
     */
    public long getTotalIntegrations() {
        return totalIntegrations;
    }

    /**
     * Get the number of successful integrations.
     * 
     * @return successful integrations
     */
    public long getSuccessfulIntegrations() {
        return successfulIntegrations;
    }

    /**
     * Get the number of failed integrations.
     * 
     * @return failed integrations
     */
    public long getFailedIntegrations() {
        return failedIntegrations;
    }

    /**
     * Get the total processing time in milliseconds.
     * 
     * @return total processing time
     */
    public long getTotalProcessingTime() {
        return totalProcessingTime;
    }

    /**
     * Get the average integration time in milliseconds.
     * 
     * @return average integration time
     */
    public double getAverageIntegrationTime() {
        return averageIntegrationTime;
    }

    /**
     * Get the time of the last integration.
     * 
     * @return last integration time, or null if no integrations
     */
    public @Nullable Instant getLastIntegrationTime() {
        return lastIntegrationTime;
    }

    @Override
    public long total() {
        return getTotalCount();
    }

    @Override
    public long success() {
        return getSuccessCount();
    }

    @Override
    public long failure() {
        return getFailureCount();
    }

    /**
     * Builder for ModelIntegrationStatistics.
     */
    public static final class Builder {
        private String id;
        private Instant timestamp = Instant.now();
        private String domain = "model";
        private @Nullable String source;
        private @Nullable String description;
        private @Nullable Map<String, Object> data;
        private long totalCount = 0;
        private long successCount = 0;
        private long failureCount = 0;
        private @Nullable Instant collectionStartTime;
        private @Nullable Instant collectionEndTime;
        private @Nullable Map<String, Double> additionalMeasures;
        private long totalIntegrations = 0;
        private long successfulIntegrations = 0;
        private long failedIntegrations = 0;
        private long totalProcessingTime = 0;
        private double averageIntegrationTime = 0.0;
        private @Nullable Instant lastIntegrationTime;

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

        public Builder withTotalCount(long totalCount) {
            this.totalCount = totalCount;
            return this;
        }

        public Builder withSuccessCount(long successCount) {
            this.successCount = successCount;
            return this;
        }

        public Builder withFailureCount(long failureCount) {
            this.failureCount = failureCount;
            return this;
        }

        public Builder withCollectionStartTime(@Nullable Instant collectionStartTime) {
            this.collectionStartTime = collectionStartTime;
            return this;
        }

        public Builder withCollectionEndTime(@Nullable Instant collectionEndTime) {
            this.collectionEndTime = collectionEndTime;
            return this;
        }

        public Builder withAdditionalMeasures(@Nullable Map<String, Double> additionalMeasures) {
            this.additionalMeasures = additionalMeasures;
            return this;
        }

        public Builder withTotalIntegrations(long totalIntegrations) {
            this.totalIntegrations = totalIntegrations;
            return this;
        }

        public Builder withSuccessfulIntegrations(long successfulIntegrations) {
            this.successfulIntegrations = successfulIntegrations;
            return this;
        }

        public Builder withFailedIntegrations(long failedIntegrations) {
            this.failedIntegrations = failedIntegrations;
            return this;
        }

        public Builder withTotalProcessingTime(long totalProcessingTime) {
            this.totalProcessingTime = totalProcessingTime;
            return this;
        }

        public Builder withAverageIntegrationTime(double averageIntegrationTime) {
            this.averageIntegrationTime = averageIntegrationTime;
            return this;
        }

        public Builder withLastIntegrationTime(@Nullable Instant lastIntegrationTime) {
            this.lastIntegrationTime = lastIntegrationTime;
            return this;
        }

        public ModelIntegrationStatistics build() {
            return new ModelIntegrationStatistics(id, timestamp, domain, source, description, data, totalCount,
                    successCount, failureCount, collectionStartTime, collectionEndTime, additionalMeasures,
                    totalIntegrations, successfulIntegrations, failedIntegrations, totalProcessingTime,
                    averageIntegrationTime, lastIntegrationTime);
        }
    }

    /**
     * Create a builder for ModelIntegrationStatistics.
     * 
     * @param id the unique identifier
     * @return a new builder instance
     */
    public static Builder builder(String id) {
        return new Builder(id);
    }
}
