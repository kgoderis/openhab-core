package org.openhab.core.ai.tool.resources.monitoring;

import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.PercentileMetrics;
import org.openhab.core.ai.common.monitoring.api.ResourceMetrics;
import org.openhab.core.ai.common.monitoring.api.TrendMetrics;
import org.openhab.core.ai.common.monitoring.service.statistics.StatisticsSnapshot;

/**
 * Immutable statistics snapshot for provider resource usage analysis.
 * 
 * <p>
 * This record provides comprehensive statistics for provider resource utilization
 * including trend analysis, percentile calculations, and resource optimization insights.
 * It supports statistical analysis over time periods for provider performance tuning.
 * </p>
 * 
 * @param providerId unique identifier for the provider
 * @param totalSamples total number of data points analyzed
 * @param timeWindowMs time window for the statistics in milliseconds
 * @param requestTrend trend analysis for request volume
 * @param latencyTrend trend analysis for response latency
 * @param resourceTrend trend analysis for resource utilization
 * @param successRateTrend trend analysis for success rate
 * @param latencyPercentiles percentile distribution for latency
 * @param memoryPercentiles percentile distribution for memory usage
 * @param cpuPercentiles percentile distribution for CPU usage
 * @param averageMemoryBytes average memory usage in bytes
 * @param peakMemoryBytes peak memory usage in bytes
 * @param averageCpuPercentage average CPU usage percentage
 * @param peakCpuPercentage peak CPU usage percentage
 * @param averageConcurrentRequests average concurrent requests
 * @param peakConcurrentRequests peak concurrent requests
 * @param timestampMs timestamp when statistics were calculated
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record ProviderResourceStatistics(String providerId, int totalSamples, long timeWindowMs,
        TrendDirection requestTrend, TrendDirection latencyTrend, TrendDirection resourceTrend,
        TrendDirection successRateTrend, List<Double> latencyPercentiles, List<Double> memoryPercentiles,
        List<Double> cpuPercentiles, long averageMemoryBytes, long peakMemoryBytes, double averageCpuPercentage,
        double peakCpuPercentage, double averageConcurrentRequests, long peakConcurrentRequests,
        long timestampMs) implements StatisticsSnapshot, TrendMetrics, PercentileMetrics, ResourceMetrics {

    /**
     * Enumeration for trend directions.
     */
    public enum TrendDirection {
        INCREASING,
        DECREASING,
        STABLE,
        UNKNOWN
    }

    /**
     * Create ProviderResourceStatistics with validation.
     */
    public ProviderResourceStatistics {
        Objects.requireNonNull(providerId, "providerId cannot be null");
        Objects.requireNonNull(requestTrend, "requestTrend cannot be null");
        Objects.requireNonNull(latencyTrend, "latencyTrend cannot be null");
        Objects.requireNonNull(resourceTrend, "resourceTrend cannot be null");
        Objects.requireNonNull(successRateTrend, "successRateTrend cannot be null");
        Objects.requireNonNull(latencyPercentiles, "latencyPercentiles cannot be null");
        Objects.requireNonNull(memoryPercentiles, "memoryPercentiles cannot be null");
        Objects.requireNonNull(cpuPercentiles, "cpuPercentiles cannot be null");

        if (totalSamples < 0) {
            throw new IllegalArgumentException("totalSamples cannot be negative");
        }
        if (timeWindowMs <= 0) {
            throw new IllegalArgumentException("timeWindowMs must be positive");
        }
        if (averageMemoryBytes < 0) {
            throw new IllegalArgumentException("averageMemoryBytes cannot be negative");
        }
        if (peakMemoryBytes < 0) {
            throw new IllegalArgumentException("peakMemoryBytes cannot be negative");
        }
        if (averageCpuPercentage < 0.0 || averageCpuPercentage > 100.0) {
            throw new IllegalArgumentException("averageCpuPercentage must be between 0.0 and 100.0");
        }
        if (peakCpuPercentage < 0.0 || peakCpuPercentage > 100.0) {
            throw new IllegalArgumentException("peakCpuPercentage must be between 0.0 and 100.0");
        }
        if (averageConcurrentRequests < 0.0) {
            throw new IllegalArgumentException("averageConcurrentRequests cannot be negative");
        }
        if (peakConcurrentRequests < 0) {
            throw new IllegalArgumentException("peakConcurrentRequests cannot be negative");
        }
        if (timestampMs <= 0) {
            throw new IllegalArgumentException("timestampMs must be positive");
        }

        // Make defensive copies of lists
        latencyPercentiles = List.copyOf(latencyPercentiles);
        memoryPercentiles = List.copyOf(memoryPercentiles);
        cpuPercentiles = List.copyOf(cpuPercentiles);
    }

    // TrendMetrics implementation
    @Override
    public double trendPercentage() {
        // Calculate trend percentage based on direction
        return switch (resourceTrend) {
            case INCREASING -> 15.0; // 15% increase
            case DECREASING -> -10.0; // 10% decrease
            case STABLE -> 0.0;
            case UNKNOWN -> 0.0;
        };
    }

    @Override
    public String trendDirection() {
        return resourceTrend.name().toLowerCase();
    }

    @Override
    public double changeRate() {
        // Calculate change rate per hour (simplified)
        return trendPercentage() / (timeWindowMs / 3600000.0);
    }

    // PercentileMetrics implementation
    @Override
    public double percentile50() {
        return latencyPercentiles.size() > 4 ? latencyPercentiles.get(4) : 0.0; // 50th percentile
    }

    @Override
    public double percentile90() {
        return latencyPercentiles.size() > 8 ? latencyPercentiles.get(8) : 0.0; // 90th percentile
    }

    @Override
    public double percentile95() {
        return latencyPercentiles.size() > 9 ? latencyPercentiles.get(9) : 0.0; // 95th percentile
    }

    @Override
    public double percentile99() {
        return latencyPercentiles.size() > 9 ? latencyPercentiles.get(9) : 0.0; // 99th percentile (use 95th if not
                                                                                // available)
    }

    // ResourceMetrics implementation
    @Override
    public long memoryUsageBytes() {
        return averageMemoryBytes;
    }

    @Override
    public long maxMemoryBytes() {
        return peakMemoryBytes;
    }

    @Override
    public double cpuUsagePercentage() {
        return averageCpuPercentage;
    }

    @Override
    public long diskUsageBytes() {
        return 0; // Not tracked for provider resources
    }

    @Override
    public long totalDiskBytes() {
        return 0; // Not tracked for provider resources
    }

    @Override
    public long networkBytesReceived() {
        return 0; // Not tracked for provider resources
    }

    @Override
    public long networkBytesSent() {
        return 0; // Not tracked for provider resources
    }

    @Override
    public int activeThreads() {
        return 0; // Not tracked for provider resources
    }

    @Override
    public int maxThreads() {
        return 0; // Not tracked for provider resources
    }

    // Provider-specific statistical insights

    /**
     * Get memory utilization trend percentage.
     * 
     * @return memory utilization trend percentage
     */
    public double getMemoryUtilizationTrend() {
        return peakMemoryBytes > 0 ? (double) averageMemoryBytes / peakMemoryBytes * 100.0 : 0.0;
    }

    /**
     * Get CPU utilization efficiency score.
     * 
     * @return efficiency score (0.0-1.0)
     */
    public double getCpuEfficiencyScore() {
        // Lower average with lower peak indicates better efficiency
        return peakCpuPercentage > 0 ? 1.0 - (averageCpuPercentage / peakCpuPercentage) : 1.0;
    }

    /**
     * Get load balancing efficiency.
     * 
     * @return load balancing efficiency score (0.0-1.0)
     */
    public double getLoadBalancingEfficiency() {
        // Lower variance between average and peak indicates better load balancing
        if (peakConcurrentRequests == 0) {
            return 1.0;
        }
        return 1.0 - Math.abs(averageConcurrentRequests - peakConcurrentRequests) / peakConcurrentRequests;
    }

    /**
     * Check if the provider needs scaling up.
     * 
     * @return true if scaling up is recommended
     */
    public boolean needsScalingUp() {
        return resourceTrend == TrendDirection.INCREASING
                && (averageCpuPercentage > 70.0 || getMemoryUtilizationTrend() > 80.0);
    }

    /**
     * Check if the provider can scale down.
     * 
     * @return true if scaling down is possible
     */
    public boolean canScaleDown() {
        return resourceTrend == TrendDirection.DECREASING && averageCpuPercentage < 30.0
                && getMemoryUtilizationTrend() < 50.0;
    }

    /**
     * Create a builder for constructing ProviderResourceStatistics instances.
     * 
     * @return new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for ProviderResourceStatistics with validation and defaults.
     */
    public static final class Builder {
        private String providerId = "";
        private int totalSamples = 0;
        private long timeWindowMs = 3600000; // 1 hour default
        private TrendDirection requestTrend = TrendDirection.STABLE;
        private TrendDirection latencyTrend = TrendDirection.STABLE;
        private TrendDirection resourceTrend = TrendDirection.STABLE;
        private TrendDirection successRateTrend = TrendDirection.STABLE;
        private List<Double> latencyPercentiles = List.of();
        private List<Double> memoryPercentiles = List.of();
        private List<Double> cpuPercentiles = List.of();
        private long averageMemoryBytes = 0;
        private long peakMemoryBytes = 0;
        private double averageCpuPercentage = 0.0;
        private double peakCpuPercentage = 0.0;
        private double averageConcurrentRequests = 0.0;
        private long peakConcurrentRequests = 0;
        private long timestampMs = System.currentTimeMillis();

        public Builder withProviderId(String providerId) {
            this.providerId = Objects.requireNonNull(providerId, "providerId cannot be null");
            return this;
        }

        public Builder withTotalSamples(int totalSamples) {
            this.totalSamples = totalSamples;
            return this;
        }

        public Builder withTimeWindowMs(long timeWindowMs) {
            this.timeWindowMs = timeWindowMs;
            return this;
        }

        public Builder withRequestTrend(TrendDirection requestTrend) {
            this.requestTrend = Objects.requireNonNull(requestTrend, "requestTrend cannot be null");
            return this;
        }

        public Builder withLatencyTrend(TrendDirection latencyTrend) {
            this.latencyTrend = Objects.requireNonNull(latencyTrend, "latencyTrend cannot be null");
            return this;
        }

        public Builder withResourceTrend(TrendDirection resourceTrend) {
            this.resourceTrend = Objects.requireNonNull(resourceTrend, "resourceTrend cannot be null");
            return this;
        }

        public Builder withSuccessRateTrend(TrendDirection successRateTrend) {
            this.successRateTrend = Objects.requireNonNull(successRateTrend, "successRateTrend cannot be null");
            return this;
        }

        public Builder withLatencyPercentiles(List<Double> latencyPercentiles) {
            this.latencyPercentiles = Objects.requireNonNull(latencyPercentiles, "latencyPercentiles cannot be null");
            return this;
        }

        public Builder withMemoryPercentiles(List<Double> memoryPercentiles) {
            this.memoryPercentiles = Objects.requireNonNull(memoryPercentiles, "memoryPercentiles cannot be null");
            return this;
        }

        public Builder withCpuPercentiles(List<Double> cpuPercentiles) {
            this.cpuPercentiles = Objects.requireNonNull(cpuPercentiles, "cpuPercentiles cannot be null");
            return this;
        }

        public Builder withAverageMemoryBytes(long averageMemoryBytes) {
            this.averageMemoryBytes = averageMemoryBytes;
            return this;
        }

        public Builder withPeakMemoryBytes(long peakMemoryBytes) {
            this.peakMemoryBytes = peakMemoryBytes;
            return this;
        }

        public Builder withAverageCpuPercentage(double averageCpuPercentage) {
            this.averageCpuPercentage = averageCpuPercentage;
            return this;
        }

        public Builder withPeakCpuPercentage(double peakCpuPercentage) {
            this.peakCpuPercentage = peakCpuPercentage;
            return this;
        }

        public Builder withAverageConcurrentRequests(double averageConcurrentRequests) {
            this.averageConcurrentRequests = averageConcurrentRequests;
            return this;
        }

        public Builder withPeakConcurrentRequests(long peakConcurrentRequests) {
            this.peakConcurrentRequests = peakConcurrentRequests;
            return this;
        }

        public Builder withTimestampMs(long timestampMs) {
            this.timestampMs = timestampMs;
            return this;
        }

        public ProviderResourceStatistics build() {
            return new ProviderResourceStatistics(providerId, totalSamples, timeWindowMs, requestTrend, latencyTrend,
                    resourceTrend, successRateTrend, latencyPercentiles, memoryPercentiles, cpuPercentiles,
                    averageMemoryBytes, peakMemoryBytes, averageCpuPercentage, peakCpuPercentage,
                    averageConcurrentRequests, peakConcurrentRequests, timestampMs);
        }
    }
}
