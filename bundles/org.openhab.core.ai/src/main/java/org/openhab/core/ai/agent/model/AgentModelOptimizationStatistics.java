package org.openhab.core.ai.agent.model;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.osgi.service.component.annotations.Reference;

/**
 * Statistics for agent model optimizations.
 * 
 * <p>
 * This class provides statistics about model optimizations including counts,
 * average improvements, and status distributions.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public final class AgentModelOptimizationStatistics {

    @Reference
    private @Nullable MetricsService metricsService;

    private final int totalOptimizations;
    private final Map<AgentModelOptimizationStatus, Long> statusCounts;
    private final double averageImprovement;
    private final @Nullable Instant lastOptimizationTime;
    private final Map<String, Object> metadata;

    private AgentModelOptimizationStatistics(Builder b) {
        this.totalOptimizations = b.totalOptimizations;
        this.statusCounts = Map.copyOf(b.statusCounts);
        this.averageImprovement = b.averageImprovement;
        this.lastOptimizationTime = b.lastOptimizationTime;
        this.metadata = Map.copyOf(b.metadata);
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public int getTotalOptimizations() {
        return totalOptimizations;
    }

    public Map<AgentModelOptimizationStatus, Long> getStatusCounts() {
        return statusCounts;
    }

    public double getAverageImprovement() {
        return averageImprovement;
    }

    public @Nullable Instant getLastOptimizationTime() {
        return lastOptimizationTime;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public long getStatusCount(AgentModelOptimizationStatus status) {
        return statusCounts.getOrDefault(status, 0L);
    }

    public long getSuccessfulOptimizations() {
        return statusCounts.entrySet().stream().filter(entry -> entry.getKey().isSuccessful())
                .mapToLong(Map.Entry::getValue).sum();
    }

    public long getOptimizationsWithImprovement() {
        return statusCounts.entrySet().stream().filter(entry -> entry.getKey().hasImprovement())
                .mapToLong(Map.Entry::getValue).sum();
    }

    /**
     * Record optimization statistics using MetricsService.
     */
    public void recordOptimizationStatistics() {
        MetricsService metrics = metricsService;
        if (metrics != null) {
            long startTime = System.currentTimeMillis();
            boolean success = getSuccessfulOptimizations() > 0;
            long duration = System.currentTimeMillis() - startTime;

            metrics.recordOperation("agent-model", "optimization-statistics", success,
                    java.time.Duration.ofMillis(duration));
        }
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        AgentModelOptimizationStatistics other = (AgentModelOptimizationStatistics) obj;
        return totalOptimizations == other.totalOptimizations && Objects.equals(statusCounts, other.statusCounts)
                && Double.compare(averageImprovement, other.averageImprovement) == 0
                && Objects.equals(lastOptimizationTime, other.lastOptimizationTime)
                && Objects.equals(metadata, other.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalOptimizations, statusCounts, averageImprovement, lastOptimizationTime, metadata);
    }

    @Override
    public String toString() {
        return "AgentModelOptimizationStatistics{" + "totalOptimizations=" + totalOptimizations + ", statusCounts="
                + statusCounts + ", averageImprovement=" + averageImprovement + ", lastOptimizationTime="
                + lastOptimizationTime + '}';
    }

    public static final class Builder {
        private int totalOptimizations = 0;
        private Map<AgentModelOptimizationStatus, Long> statusCounts = Map.of();
        private double averageImprovement = 0.0;
        private @Nullable Instant lastOptimizationTime;
        private Map<String, Object> metadata = Map.of();

        public Builder() {
        }

        public Builder(AgentModelOptimizationStatistics source) {
            this.totalOptimizations = source.totalOptimizations;
            this.statusCounts = source.statusCounts;
            this.averageImprovement = source.averageImprovement;
            this.lastOptimizationTime = source.lastOptimizationTime;
            this.metadata = source.metadata;
        }

        public Builder withTotalOptimizations(int totalOptimizations) {
            this.totalOptimizations = totalOptimizations;
            return this;
        }

        public Builder withStatusCounts(Map<AgentModelOptimizationStatus, Long> statusCounts) {
            this.statusCounts = Objects.requireNonNull(statusCounts, "statusCounts");
            return this;
        }

        public Builder withAverageImprovement(double averageImprovement) {
            this.averageImprovement = averageImprovement;
            return this;
        }

        public Builder withLastOptimizationTime(@Nullable Instant lastOptimizationTime) {
            this.lastOptimizationTime = lastOptimizationTime;
            return this;
        }

        public Builder withMetadata(Map<String, Object> metadata) {
            this.metadata = Objects.requireNonNull(metadata, "metadata");
            return this;
        }

        public AgentModelOptimizationStatistics build() {
            if (totalOptimizations < 0) {
                throw new IllegalArgumentException("totalOptimizations must be non-negative");
            }
            if (averageImprovement < 0.0 || averageImprovement > 1.0) {
                throw new IllegalArgumentException("averageImprovement must be between 0.0 and 1.0");
            }
            return new AgentModelOptimizationStatistics(this);
        }
    }
}
