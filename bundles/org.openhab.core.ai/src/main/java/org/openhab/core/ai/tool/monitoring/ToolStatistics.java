package org.openhab.core.ai.tool.monitoring;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.base.AbstractStatistics;

/**
 * Consolidated tool statistics that extends the unified monitoring framework.
 * 
 * <p>
 * This class provides comprehensive statistics for tool operations including
 * execution counts, success rates, and performance metrics. It implements
 * the CountsMetrics capability interface for basic counting functionality.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ToolStatistics extends AbstractStatistics implements CountsMetrics {

    /**
     * Create a new ToolStatistics instance.
     * 
     * @param id unique identifier for this statistics instance
     * @param timestamp timestamp when statistics were collected
     * @param totalCount total number of tool executions
     * @param successCount number of successful executions
     * @param failureCount number of failed executions
     * @param collectionStartTime when collection started
     * @param collectionEndTime when collection ended
     * @param additionalMeasures additional statistical measures
     * @param data additional monitoring data
     */
    public ToolStatistics(String id, Instant timestamp, long totalCount, long successCount, long failureCount,
            @Nullable Instant collectionStartTime, @Nullable Instant collectionEndTime,
            @Nullable Map<String, Double> additionalMeasures, @Nullable Map<String, Object> data) {
        super(id, timestamp, "tool", "tool-statistics", "Tool execution statistics", data, totalCount, successCount,
                failureCount, collectionStartTime, collectionEndTime, additionalMeasures);
    }

    /**
     * Create a new ToolStatistics instance with current timestamp.
     * 
     * @param id unique identifier for this statistics instance
     * @param totalCount total number of tool executions
     * @param successCount number of successful executions
     * @param failureCount number of failed executions
     */
    public ToolStatistics(String id, long totalCount, long successCount, long failureCount) {
        this(id, Instant.now(), totalCount, successCount, failureCount, null, null, null, null);
    }

    // CountsMetrics interface implementation
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
     * Get the average execution time from additional measures.
     * 
     * @return average execution time in milliseconds, or 0.0 if not available
     */
    public double getAverageExecutionTime() {
        Map<String, Double> measures = getAdditionalMeasures();
        return measures != null ? measures.getOrDefault("averageExecutionTime", 0.0) : 0.0;
    }

    /**
     * Get the total execution time from additional measures.
     * 
     * @return total execution time in milliseconds, or 0.0 if not available
     */
    public double getTotalExecutionTime() {
        Map<String, Double> measures = getAdditionalMeasures();
        return measures != null ? measures.getOrDefault("totalExecutionTime", 0.0) : 0.0;
    }

    /**
     * Check if there are critical tool issues (high failure rate).
     *
     * @return true if there are critical tool issues
     */
    public boolean hasCriticalIssues() {
        return successRate() < 0.8;
    }

    /**
     * Builder for ToolStatistics.
     */
    public static final class Builder {
        private String id = "";
        private long totalOperations = 0;
        private long successfulOperations = 0;
        private long failedOperations = 0;
        private @Nullable Map<String, Object> data;

        public Builder() {
            // Default constructor
        }

        public Builder(ToolStatistics source) {
            this.id = source.getId();
            this.totalOperations = source.getTotalCount();
            this.successfulOperations = source.getSuccessCount();
            this.failedOperations = source.getFailureCount();
            this.data = source.getData();
        }

        public Builder withId(String id) {
            this.id = Objects.requireNonNull(id, "id");
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

        public Builder withData(@Nullable Map<String, Object> data) {
            this.data = data;
            return this;
        }

        public ToolStatistics build() {
            validate();
            return new ToolStatistics(id, Instant.now(), totalOperations, successfulOperations, failedOperations, null,
                    null, null, data);
        }

        private void validate() {
            if (id.isBlank()) {
                throw new IllegalArgumentException("id must not be blank");
            }
            if (totalOperations < 0) {
                throw new IllegalArgumentException("totalOperations must be non-negative");
            }
            if (successfulOperations < 0) {
                throw new IllegalArgumentException("successfulOperations must be non-negative");
            }
            if (failedOperations < 0) {
                throw new IllegalArgumentException("failedOperations must be non-negative");
            }
            if (totalOperations != successfulOperations + failedOperations) {
                throw new IllegalArgumentException(
                        "totalOperations must equal successfulOperations + failedOperations");
            }
        }
    }

    /**
     * Create a new builder for ToolStatistics.
     *
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Create a new builder for ToolStatistics from an existing instance.
     *
     * @return a new builder instance initialized with this instance's values
     */
    public Builder toBuilder() {
        return new Builder(this);
    }
}
