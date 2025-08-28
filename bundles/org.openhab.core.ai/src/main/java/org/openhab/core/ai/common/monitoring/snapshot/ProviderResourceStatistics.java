/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.core.ai.common.monitoring.snapshot;

import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.PercentileMetrics;
import org.openhab.core.ai.common.monitoring.api.ResourceMetrics;
import org.openhab.core.ai.common.monitoring.api.TrendMetrics;

/**
 * Immutable statistics for provider resource usage trends and patterns.
 * 
 * <p>
 * This class provides statistical analysis of provider resource usage over time,
 * including trend analysis, percentile calculations, and resource utilization patterns.
 * It enables performance monitoring and capacity planning for AI provider resources.
 * </p>
 * 
 * <h3>Statistical Analysis Features:</h3>
 * <ul>
 * <li>Request rate trends and capacity analysis</li>
 * <li>Response time percentiles and distribution</li>
 * <li>Resource usage patterns and peak detection</li>
 * <li>Provider efficiency and reliability metrics</li>
 * <li>Capacity planning and scaling recommendations</li>
 * <li>Historical performance comparison</li>
 * </ul>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record ProviderResourceStatistics(long startTimeMs, long endTimeMs, String providerName,
        List<Double> requestRateHistory, List<Double> responseTimeHistory, List<Double> memoryUsageHistory,
        List<Double> cpuUsageHistory, List<Double> diskUsageHistory, List<Double> networkThroughputHistory,
        List<Double> threadUsageHistory, double peakMemoryUsage, double peakCpuUsage, double peakDiskUsage,
        double peakNetworkThroughput, int peakThreadCount, double averageResourceUtilization, long totalDataProcessed,
        long totalNetworkTraffic) implements TrendMetrics, PercentileMetrics, ResourceMetrics {

    /**
     * Create a new ProviderResourceStatistics.
     * 
     * @param startTimeMs the start time in milliseconds
     * @param endTimeMs the end time in milliseconds
     * @param providerName the provider name
     * @param requestRateHistory the request rate history
     * @param responseTimeHistory the response time history
     * @param memoryUsageHistory the memory usage history
     * @param cpuUsageHistory the CPU usage history
     * @param diskUsageHistory the disk usage history
     * @param networkThroughputHistory the network throughput history
     * @param threadUsageHistory the thread usage history
     * @param peakMemoryUsage the peak memory usage percentage
     * @param peakCpuUsage the peak CPU usage percentage
     * @param peakDiskUsage the peak disk usage percentage
     * @param peakNetworkThroughput the peak network throughput
     * @param peakThreadCount the peak thread count
     * @param averageResourceUtilization the average resource utilization
     * @param totalDataProcessed the total data processed in bytes
     * @param totalNetworkTraffic the total network traffic in bytes
     */
    public ProviderResourceStatistics {
        Objects.requireNonNull(providerName, "providerName cannot be null");
        Objects.requireNonNull(requestRateHistory, "requestRateHistory cannot be null");
        Objects.requireNonNull(responseTimeHistory, "responseTimeHistory cannot be null");
        Objects.requireNonNull(memoryUsageHistory, "memoryUsageHistory cannot be null");
        Objects.requireNonNull(cpuUsageHistory, "cpuUsageHistory cannot be null");
        Objects.requireNonNull(diskUsageHistory, "diskUsageHistory cannot be null");
        Objects.requireNonNull(networkThroughputHistory, "networkThroughputHistory cannot be null");
        Objects.requireNonNull(threadUsageHistory, "threadUsageHistory cannot be null");

        if (startTimeMs < 0) {
            throw new IllegalArgumentException("startTimeMs must be non-negative");
        }
        if (endTimeMs < startTimeMs) {
            throw new IllegalArgumentException("endTimeMs must be >= startTimeMs");
        }
        if (peakMemoryUsage < 0.0 || peakMemoryUsage > 100.0) {
            throw new IllegalArgumentException("peakMemoryUsage must be between 0.0 and 100.0");
        }
        if (peakCpuUsage < 0.0 || peakCpuUsage > 100.0) {
            throw new IllegalArgumentException("peakCpuUsage must be between 0.0 and 100.0");
        }
        if (peakDiskUsage < 0.0 || peakDiskUsage > 100.0) {
            throw new IllegalArgumentException("peakDiskUsage must be between 0.0 and 100.0");
        }
        if (peakNetworkThroughput < 0.0) {
            throw new IllegalArgumentException("peakNetworkThroughput must be non-negative");
        }
        if (peakThreadCount < 0) {
            throw new IllegalArgumentException("peakThreadCount must be non-negative");
        }
        if (averageResourceUtilization < 0.0 || averageResourceUtilization > 100.0) {
            throw new IllegalArgumentException("averageResourceUtilization must be between 0.0 and 100.0");
        }
        if (totalDataProcessed < 0) {
            throw new IllegalArgumentException("totalDataProcessed must be non-negative");
        }
        if (totalNetworkTraffic < 0) {
            throw new IllegalArgumentException("totalNetworkTraffic must be non-negative");
        }

        // Make defensive copies of lists
        requestRateHistory = List.copyOf(requestRateHistory);
        responseTimeHistory = List.copyOf(responseTimeHistory);
        memoryUsageHistory = List.copyOf(memoryUsageHistory);
        cpuUsageHistory = List.copyOf(cpuUsageHistory);
        diskUsageHistory = List.copyOf(diskUsageHistory);
        networkThroughputHistory = List.copyOf(networkThroughputHistory);
        threadUsageHistory = List.copyOf(threadUsageHistory);
    }

    // TrendMetrics implementation
    @Override
    public double trendPercentage() {
        return getProviderEfficiencyTrend();
    }

    @Override
    public String trendDirection() {
        double trend = trendPercentage();
        if (trend > 5.0) {
            return "increasing";
        } else if (trend < -5.0) {
            return "decreasing";
        } else {
            return "stable";
        }
    }

    @Override
    public double changeRate() {
        if (requestRateHistory.size() < 2) {
            return 0.0;
        }
        // Calculate average change per time period
        double totalChange = 0.0;
        for (int i = 1; i < requestRateHistory.size(); i++) {
            totalChange += Math.abs(requestRateHistory.get(i) - requestRateHistory.get(i - 1));
        }
        return totalChange / (requestRateHistory.size() - 1);
    }

    // PercentileMetrics implementation
    @Override
    public double percentile50() {
        return calculatePercentile(responseTimeHistory, 50.0);
    }

    @Override
    public double percentile90() {
        return calculatePercentile(responseTimeHistory, 90.0);
    }

    @Override
    public double percentile95() {
        return calculatePercentile(responseTimeHistory, 95.0);
    }

    @Override
    public double percentile99() {
        return calculatePercentile(responseTimeHistory, 99.0);
    }

    // ResourceMetrics implementation
    @Override
    public long memoryUsageBytes() {
        // Return peak memory usage as representative value
        return Math.round(peakMemoryUsage * maxMemoryBytes() / 100.0);
    }

    @Override
    public long maxMemoryBytes() {
        // Use system memory as a reasonable default
        return Runtime.getRuntime().maxMemory();
    }

    @Override
    public double cpuUsagePercentage() {
        return peakCpuUsage;
    }

    @Override
    public long diskUsageBytes() {
        // Return peak disk usage as representative value
        return Math.round(peakDiskUsage * totalDiskBytes() / 100.0);
    }

    @Override
    public long totalDiskBytes() {
        // Use total disk space as a reasonable default (simplified)
        return 1024L * 1024L * 1024L * 100L; // 100GB default
    }

    @Override
    public long networkBytesReceived() {
        return totalNetworkTraffic / 2; // Assume balanced receive/send
    }

    @Override
    public long networkBytesSent() {
        return totalNetworkTraffic / 2; // Assume balanced receive/send
    }

    @Override
    public int activeThreads() {
        return peakThreadCount;
    }

    @Override
    public int maxThreads() {
        return Math.max(peakThreadCount, Runtime.getRuntime().availableProcessors() * 4); // Reasonable default
    }

    // Provider-specific methods

    /**
     * Get the provider name.
     * 
     * @return the provider name
     */
    public String getProviderName() {
        return providerName;
    }

    /**
     * Get the peak memory usage percentage.
     * 
     * @return the peak memory usage percentage
     */
    public double getPeakMemoryUsage() {
        return peakMemoryUsage;
    }

    /**
     * Get the peak CPU usage percentage.
     * 
     * @return the peak CPU usage percentage
     */
    public double getPeakCpuUsage() {
        return peakCpuUsage;
    }

    /**
     * Get the peak disk usage percentage.
     * 
     * @return the peak disk usage percentage
     */
    public double getPeakDiskUsage() {
        return peakDiskUsage;
    }

    /**
     * Get the peak network throughput.
     * 
     * @return the peak network throughput in bytes per second
     */
    public double getPeakNetworkThroughput() {
        return peakNetworkThroughput;
    }

    /**
     * Get the peak thread count.
     * 
     * @return the peak thread count
     */
    public int getPeakThreadCount() {
        return peakThreadCount;
    }

    /**
     * Get the average resource utilization.
     * 
     * @return the average resource utilization percentage
     */
    public double getAverageResourceUtilization() {
        return averageResourceUtilization;
    }

    /**
     * Get the total data processed.
     * 
     * @return the total data processed in bytes
     */
    public long getTotalDataProcessed() {
        return totalDataProcessed;
    }

    /**
     * Get the total network traffic.
     * 
     * @return the total network traffic in bytes
     */
    public long getTotalNetworkTraffic() {
        return totalNetworkTraffic;
    }

    /**
     * Get the provider efficiency trend.
     * 
     * @return efficiency trend (positive = improving, negative = degrading)
     */
    public double getProviderEfficiencyTrend() {
        if (requestRateHistory.size() < 2) {
            return 0.0;
        }

        double recent = requestRateHistory
                .subList(Math.max(0, requestRateHistory.size() - 5), requestRateHistory.size()).stream()
                .mapToDouble(Double::doubleValue).average().orElse(0.0);
        double earlier = requestRateHistory.subList(0, Math.min(5, requestRateHistory.size())).stream()
                .mapToDouble(Double::doubleValue).average().orElse(0.0);

        return earlier > 0 ? (recent - earlier) / earlier * 100.0 : 0.0;
    }

    /**
     * Get the capacity recommendation.
     * 
     * @return capacity recommendation (percentage change needed)
     */
    public double getCapacityRecommendation() {
        double maxUtilization = Math.max(Math.max(peakMemoryUsage, peakCpuUsage),
                Math.max(peakDiskUsage, threadUsagePercentage()));

        if (maxUtilization > 90.0) {
            return 50.0; // Recommend 50% increase
        } else if (maxUtilization > 80.0) {
            return 25.0; // Recommend 25% increase
        } else if (maxUtilization < 30.0) {
            return -25.0; // Recommend 25% decrease
        }
        return 0.0; // No change needed
    }

    /**
     * Check if this provider shows concerning performance patterns.
     * 
     * @return true if performance patterns are concerning
     */
    public boolean hasPerformanceConcerns() {
        return peakMemoryUsage > 90.0 || peakCpuUsage > 90.0 || peakDiskUsage > 90.0
                || getProviderEfficiencyTrend() < -20.0;
    }

    /**
     * Get memory usage trend.
     * 
     * @return memory usage trend direction
     */
    public double getMemoryUsageTrend() {
        return calculateTrend(memoryUsageHistory);
    }

    /**
     * Get CPU usage trend.
     * 
     * @return CPU usage trend direction
     */
    public double getCpuUsageTrend() {
        return calculateTrend(cpuUsageHistory);
    }

    /**
     * Get network throughput trend.
     * 
     * @return network throughput trend direction
     */
    public double getNetworkThroughputTrend() {
        return calculateTrend(networkThroughputHistory);
    }

    private double calculateTrend(List<Double> history) {
        if (history.size() < 2) {
            return 0.0;
        }

        double recent = history.subList(Math.max(0, history.size() - 5), history.size()).stream()
                .mapToDouble(Double::doubleValue).average().orElse(0.0);
        double earlier = history.subList(0, Math.min(5, history.size())).stream().mapToDouble(Double::doubleValue)
                .average().orElse(0.0);

        return earlier > 0 ? (recent - earlier) / earlier * 100.0 : 0.0;
    }

    /**
     * Create an empty ProviderResourceStatistics for a time range.
     * 
     * @param providerName the provider name
     * @param startTimeMs the start time in milliseconds
     * @param endTimeMs the end time in milliseconds
     * @return empty statistics instance
     */
    public static ProviderResourceStatistics empty(String providerName, long startTimeMs, long endTimeMs) {
        return new ProviderResourceStatistics(startTimeMs, endTimeMs, providerName, List.of(), List.of(), List.of(),
                List.of(), List.of(), List.of(), List.of(), 0.0, 0.0, 0.0, 0.0, 0, 0.0, 0L, 0L);
    }

    /**
     * Create a Builder for constructing ProviderResourceStatistics instances.
     * 
     * @param providerName the provider name
     * @param startTimeMs the start time
     * @param endTimeMs the end time
     * @return a new Builder instance
     */
    public static Builder builder(String providerName, long startTimeMs, long endTimeMs) {
        return new Builder(providerName, startTimeMs, endTimeMs);
    }

    /**
     * Builder for creating ProviderResourceStatistics instances.
     */
    public static final class Builder {
        private final String providerName;
        private final long startTimeMs;
        private final long endTimeMs;
        private List<Double> requestRateHistory = List.of();
        private List<Double> responseTimeHistory = List.of();
        private List<Double> memoryUsageHistory = List.of();
        private List<Double> cpuUsageHistory = List.of();
        private List<Double> diskUsageHistory = List.of();
        private List<Double> networkThroughputHistory = List.of();
        private List<Double> threadUsageHistory = List.of();
        private double peakMemoryUsage = 0.0;
        private double peakCpuUsage = 0.0;
        private double peakDiskUsage = 0.0;
        private double peakNetworkThroughput = 0.0;
        private int peakThreadCount = 0;
        private double averageResourceUtilization = 0.0;
        private long totalDataProcessed = 0L;
        private long totalNetworkTraffic = 0L;

        private Builder(String providerName, long startTimeMs, long endTimeMs) {
            this.providerName = Objects.requireNonNull(providerName, "providerName cannot be null");
            this.startTimeMs = startTimeMs;
            this.endTimeMs = endTimeMs;
        }

        public Builder withRequestRateHistory(List<Double> requestRateHistory) {
            this.requestRateHistory = Objects.requireNonNull(requestRateHistory, "requestRateHistory cannot be null");
            return this;
        }

        public Builder withResponseTimeHistory(List<Double> responseTimeHistory) {
            this.responseTimeHistory = Objects.requireNonNull(responseTimeHistory,
                    "responseTimeHistory cannot be null");
            return this;
        }

        public Builder withMemoryUsageHistory(List<Double> memoryUsageHistory) {
            this.memoryUsageHistory = Objects.requireNonNull(memoryUsageHistory, "memoryUsageHistory cannot be null");
            return this;
        }

        public Builder withCpuUsageHistory(List<Double> cpuUsageHistory) {
            this.cpuUsageHistory = Objects.requireNonNull(cpuUsageHistory, "cpuUsageHistory cannot be null");
            return this;
        }

        public Builder withDiskUsageHistory(List<Double> diskUsageHistory) {
            this.diskUsageHistory = Objects.requireNonNull(diskUsageHistory, "diskUsageHistory cannot be null");
            return this;
        }

        public Builder withNetworkThroughputHistory(List<Double> networkThroughputHistory) {
            this.networkThroughputHistory = Objects.requireNonNull(networkThroughputHistory,
                    "networkThroughputHistory cannot be null");
            return this;
        }

        public Builder withThreadUsageHistory(List<Double> threadUsageHistory) {
            this.threadUsageHistory = Objects.requireNonNull(threadUsageHistory, "threadUsageHistory cannot be null");
            return this;
        }

        public Builder withPeakUsage(double peakMemoryUsage, double peakCpuUsage, double peakDiskUsage,
                double peakNetworkThroughput, int peakThreadCount) {
            this.peakMemoryUsage = peakMemoryUsage;
            this.peakCpuUsage = peakCpuUsage;
            this.peakDiskUsage = peakDiskUsage;
            this.peakNetworkThroughput = peakNetworkThroughput;
            this.peakThreadCount = peakThreadCount;
            return this;
        }

        public Builder withAverageResourceUtilization(double averageResourceUtilization) {
            this.averageResourceUtilization = averageResourceUtilization;
            return this;
        }

        public Builder withTotalData(long totalDataProcessed, long totalNetworkTraffic) {
            this.totalDataProcessed = totalDataProcessed;
            this.totalNetworkTraffic = totalNetworkTraffic;
            return this;
        }

        public ProviderResourceStatistics build() {
            return new ProviderResourceStatistics(startTimeMs, endTimeMs, providerName, requestRateHistory,
                    responseTimeHistory, memoryUsageHistory, cpuUsageHistory, diskUsageHistory,
                    networkThroughputHistory, threadUsageHistory, peakMemoryUsage, peakCpuUsage, peakDiskUsage,
                    peakNetworkThroughput, peakThreadCount, averageResourceUtilization, totalDataProcessed,
                    totalNetworkTraffic);
        }
    }

    /**
     * Calculate percentile value from a list of values.
     * 
     * @param values the list of values
     * @param percentile the percentile to calculate (0-100)
     * @return the percentile value
     */
    private double calculatePercentile(List<Double> values, double percentile) {
        if (values.isEmpty()) {
            return 0.0;
        }

        List<Double> sorted = values.stream().sorted().toList();
        int index = (int) Math.ceil(percentile / 100.0 * sorted.size()) - 1;
        index = Math.max(0, Math.min(index, sorted.size() - 1));
        return sorted.get(index);
    }
}
