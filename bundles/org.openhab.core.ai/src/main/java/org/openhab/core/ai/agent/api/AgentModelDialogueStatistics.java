package org.openhab.core.ai.agent.api;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.Counts;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.MetricsSnapshot;
import org.openhab.core.ai.common.monitoring.api.MonitoringType;
import org.openhab.core.ai.common.monitoring.api.Statistics;

/**
 * Statistics snapshot for dialogue management
 * 
 * <p>
 * This record provides:
 * - Immutable snapshot of dialogue statistics
 * - Statistical analysis of dialogue patterns and trends
 * - Capability interfaces for different statistics access patterns
 * - Thread-safe and suitable for concurrent use
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record AgentModelDialogueStatistics(Counts counts, long timestampMs, @Nullable Instant collectionStartTime,
        @Nullable Instant collectionEndTime, long totalSessionCount, double averageSessionDuration,
        double averageMessagesPerSession) implements MetricsSnapshot, CountsMetrics, Statistics {

    /**
     * Validation constructor for the record.
     */
    public AgentModelDialogueStatistics {
        Objects.requireNonNull(counts, "counts must not be null");
        if (timestampMs < 0) {
            throw new IllegalArgumentException("timestampMs must be non-negative");
        }
        if (totalSessionCount < 0) {
            throw new IllegalArgumentException("totalSessionCount must be non-negative");
        }
        if (averageSessionDuration < 0.0) {
            throw new IllegalArgumentException("averageSessionDuration must be non-negative");
        }
        if (averageMessagesPerSession < 0.0) {
            throw new IllegalArgumentException("averageMessagesPerSession must be non-negative");
        }
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

    // ===== Statistics Interface Implementation =====

    @Override
    public String getId() {
        return "dialogue-statistics";
    }

    @Override
    public Instant getTimestamp() {
        return Instant.ofEpochMilli(timestampMs);
    }

    @Override
    public MonitoringType getType() {
        return MonitoringType.STATISTICS;
    }

    @Override
    public Map<String, Object> getData() {
        Map<String, Object> data = new HashMap<>();
        data.put("totalCount", getTotalCount());
        data.put("successCount", getSuccessCount());
        data.put("failureCount", getFailureCount());
        data.put("successRate", getSuccessRate());
        data.put("failureRate", getFailureRate());
        data.put("totalSessionCount", totalSessionCount);
        data.put("averageSessionDuration", averageSessionDuration);
        data.put("averageMessagesPerSession", averageMessagesPerSession);
        data.put("collectionDurationMs", getCollectionDurationMs());
        data.put("itemsPerSecond", getItemsPerSecond());
        return data;
    }

    @Override
    public String getDomain() {
        return "dialogue";
    }

    @Override
    public String getSource() {
        return "dialogue-statistics-collector";
    }

    @Override
    public long getTotalCount() {
        return total();
    }

    @Override
    public long getSuccessCount() {
        return success();
    }

    @Override
    public long getFailureCount() {
        return failure();
    }

    @Override
    public @Nullable Instant getCollectionStartTime() {
        return collectionStartTime;
    }

    @Override
    public @Nullable Instant getCollectionEndTime() {
        return collectionEndTime;
    }

    @Override
    public Map<String, Double> getAdditionalMeasures() {
        Map<String, Double> measures = new HashMap<>();
        measures.put("totalSessionCount", (double) totalSessionCount);
        measures.put("averageSessionDuration", averageSessionDuration);
        measures.put("averageMessagesPerSession", averageMessagesPerSession);
        measures.put("sessionsPerHour", getSessionsPerHour());
        measures.put("completionRate", getCompletionRate());
        return measures;
    }

    // ===== Dialogue Specific Statistics =====

    /**
     * Calculate sessions per hour based on collection period.
     * 
     * @return sessions per hour, or 0.0 if no collection period
     */
    public double getSessionsPerHour() {
        long durationMs = getCollectionDurationMs();
        if (durationMs == 0) {
            return 0.0;
        }
        double durationHours = durationMs / 3_600_000.0;
        return totalSessionCount / durationHours;
    }

    /**
     * Calculate completion rate (successful dialogues / total sessions).
     * 
     * @return completion rate (0.0-1.0)
     */
    public double getCompletionRate() {
        if (totalSessionCount == 0) {
            return 0.0;
        }
        return (double) success() / totalSessionCount;
    }

    /**
     * Calculate average dialogue efficiency score.
     * 
     * @return efficiency score combining success rate and session metrics
     */
    public double getEfficiencyScore() {
        if (totalSessionCount == 0) {
            return 0.0;
        }
        double successRatio = getSuccessRate() / 100.0;
        double sessionQuality = Math.min(1.0, averageMessagesPerSession / 10.0); // Normalize to 0-1
        double durationQuality = Math.max(0.0, 1.0 - (averageSessionDuration / 300.0)); // Penalty for long sessions
        return (successRatio + sessionQuality + durationQuality) / 3.0;
    }

    /**
     * Check if the dialogue statistics represent a healthy state.
     * 
     * @return true if success rate and completion rate are high
     */
    public boolean isHealthy() {
        return getSuccessRate() >= 85.0 && getCompletionRate() >= 0.8;
    }

    // ===== Builder Pattern =====

    /**
     * Builder for AgentModelDialogueStatistics.
     */
    public static final class Builder {
        private Counts counts = new Counts(0, 0, 0);
        private long timestampMs = System.currentTimeMillis();
        private @Nullable Instant collectionStartTime = null;
        private @Nullable Instant collectionEndTime = null;
        private long totalSessionCount = 0;
        private double averageSessionDuration = 0.0;
        private double averageMessagesPerSession = 0.0;

        public Builder() {
            // Default constructor
        }

        public Builder(AgentModelDialogueStatistics source) {
            this.counts = source.counts;
            this.timestampMs = source.timestampMs;
            this.collectionStartTime = source.collectionStartTime;
            this.collectionEndTime = source.collectionEndTime;
            this.totalSessionCount = source.totalSessionCount;
            this.averageSessionDuration = source.averageSessionDuration;
            this.averageMessagesPerSession = source.averageMessagesPerSession;
        }

        public Builder withCounts(Counts counts) {
            this.counts = Objects.requireNonNull(counts, "counts");
            return this;
        }

        public Builder withTimestampMs(long timestampMs) {
            this.timestampMs = timestampMs;
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

        public Builder withTotalSessionCount(long totalSessionCount) {
            this.totalSessionCount = totalSessionCount;
            return this;
        }

        public Builder withAverageSessionDuration(double averageSessionDuration) {
            this.averageSessionDuration = averageSessionDuration;
            return this;
        }

        public Builder withAverageMessagesPerSession(double averageMessagesPerSession) {
            this.averageMessagesPerSession = averageMessagesPerSession;
            return this;
        }

        public Builder withTotal(long total) {
            this.counts = new Counts(total, counts.success(), counts.failure());
            return this;
        }

        public Builder withSuccess(long success) {
            this.counts = new Counts(counts.total(), success, counts.failure());
            return this;
        }

        public Builder withFailure(long failure) {
            this.counts = new Counts(counts.total(), counts.success(), failure);
            return this;
        }

        public AgentModelDialogueStatistics build() {
            validate();
            return new AgentModelDialogueStatistics(counts, timestampMs, collectionStartTime, collectionEndTime,
                    totalSessionCount, averageSessionDuration, averageMessagesPerSession);
        }

        private void validate() {
            if (counts.total() < 0) {
                throw new IllegalArgumentException("total count must be non-negative");
            }
            if (counts.success() < 0) {
                throw new IllegalArgumentException("success count must be non-negative");
            }
            if (counts.failure() < 0) {
                throw new IllegalArgumentException("failure count must be non-negative");
            }
            if (totalSessionCount < 0) {
                throw new IllegalArgumentException("total session count must be non-negative");
            }
            if (averageSessionDuration < 0.0) {
                throw new IllegalArgumentException("average session duration must be non-negative");
            }
            if (averageMessagesPerSession < 0.0) {
                throw new IllegalArgumentException("average messages per session must be non-negative");
            }
        }
    }

    /**
     * Create a new builder for AgentModelDialogueStatistics.
     *
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Create a new builder for AgentModelDialogueStatistics from an existing instance.
     *
     * @return a new builder instance initialized with this instance's values
     */
    public Builder toBuilder() {
        return new Builder(this);
    }

    // ===== Utility Methods =====

    /**
     * Create a concise summary string for logging.
     * 
     * @return concise summary string
     */
    public String toSummary() {
        return String.format("DialogueStats[%d sessions, %.1f%% success, %.1f avg msgs, %.1fs avg duration]",
                totalSessionCount, getSuccessRate(), averageMessagesPerSession, averageSessionDuration);
    }
}
