package org.openhab.core.ai.tool.resources.monitoring;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.PercentileMetrics;
import org.openhab.core.ai.common.monitoring.api.ResourceMetrics;
import org.openhab.core.ai.common.monitoring.api.TrendMetrics;
import org.openhab.core.ai.common.monitoring.service.statistics.StatisticsSnapshot;

/**
 * Resource Manager statistics providing trend analysis, percentiles, and resource utilization insights.
 * 
 * <p>
 * This immutable statistics snapshot provides computed insights from ResourceManager
 * metrics data over a time range, including trends, percentile analysis, and resource
 * utilization patterns.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record ResourceManagerStatistics(double trendPercentage, String trendDirection, double changeRate,
        double percentile50ResponseTime, double percentile90ResponseTime, double percentile95ResponseTime,
        double percentile99ResponseTime, long avgMemoryUsageBytes, long maxMemoryBytes, double avgCpuUsagePercentage,
        long avgDiskUsageBytes, long totalDiskBytes, long avgNetworkBytesReceived, long avgNetworkBytesSent,
        int avgActiveThreads, int maxThreads, long timeRangeStartMs,
        long timeRangeEndMs) implements StatisticsSnapshot, TrendMetrics, PercentileMetrics, ResourceMetrics {

    /**
     * Create a new ResourceManagerStatistics.
     * 
     * @param trendPercentage trend percentage (positive for increasing, negative for decreasing)
     * @param trendDirection trend direction ("increasing", "decreasing", or "stable")
     * @param changeRate change rate per time unit
     * @param percentile50ResponseTime 50th percentile response time in milliseconds
     * @param percentile90ResponseTime 90th percentile response time in milliseconds
     * @param percentile95ResponseTime 95th percentile response time in milliseconds
     * @param percentile99ResponseTime 99th percentile response time in milliseconds
     * @param avgMemoryUsageBytes average memory usage in bytes
     * @param maxMemoryBytes maximum memory available in bytes
     * @param avgCpuUsagePercentage average CPU usage percentage
     * @param avgDiskUsageBytes average disk usage in bytes
     * @param totalDiskBytes total disk space in bytes
     * @param avgNetworkBytesReceived average network bytes received
     * @param avgNetworkBytesSent average network bytes sent
     * @param avgActiveThreads average number of active threads
     * @param maxThreads maximum number of threads
     * @param timeRangeStartMs start of time range in milliseconds
     * @param timeRangeEndMs end of time range in milliseconds
     */
    public ResourceManagerStatistics {
        // Validation
        if (!isValidTrendDirection(trendDirection)) {
            throw new IllegalArgumentException("trendDirection must be 'increasing', 'decreasing', or 'stable'");
        }
        if (percentile50ResponseTime < 0) {
            throw new IllegalArgumentException("percentile50ResponseTime must not be negative");
        }
        if (percentile90ResponseTime < percentile50ResponseTime) {
            throw new IllegalArgumentException(
                    "percentile90ResponseTime must not be less than percentile50ResponseTime");
        }
        if (percentile95ResponseTime < percentile90ResponseTime) {
            throw new IllegalArgumentException(
                    "percentile95ResponseTime must not be less than percentile90ResponseTime");
        }
        if (percentile99ResponseTime < percentile95ResponseTime) {
            throw new IllegalArgumentException(
                    "percentile99ResponseTime must not be less than percentile95ResponseTime");
        }
        if (avgMemoryUsageBytes < 0) {
            throw new IllegalArgumentException("avgMemoryUsageBytes must not be negative");
        }
        if (maxMemoryBytes < 0) {
            throw new IllegalArgumentException("maxMemoryBytes must not be negative");
        }
        if (avgCpuUsagePercentage < 0.0 || avgCpuUsagePercentage > 100.0) {
            throw new IllegalArgumentException("avgCpuUsagePercentage must be between 0.0 and 100.0");
        }
        if (avgDiskUsageBytes < 0) {
            throw new IllegalArgumentException("avgDiskUsageBytes must not be negative");
        }
        if (totalDiskBytes < 0) {
            throw new IllegalArgumentException("totalDiskBytes must not be negative");
        }
        if (avgNetworkBytesReceived < 0) {
            throw new IllegalArgumentException("avgNetworkBytesReceived must not be negative");
        }
        if (avgNetworkBytesSent < 0) {
            throw new IllegalArgumentException("avgNetworkBytesSent must not be negative");
        }
        if (avgActiveThreads < 0) {
            throw new IllegalArgumentException("avgActiveThreads must not be negative");
        }
        if (maxThreads < 0) {
            throw new IllegalArgumentException("maxThreads must not be negative");
        }
        if (timeRangeStartMs > timeRangeEndMs) {
            throw new IllegalArgumentException("timeRangeStartMs must not be greater than timeRangeEndMs");
        }
    }

    // TrendMetrics implementation
    @Override
    public double trendPercentage() {
        return trendPercentage;
    }

    @Override
    public String trendDirection() {
        return trendDirection;
    }

    @Override
    public double changeRate() {
        return changeRate;
    }

    // PercentileMetrics implementation
    @Override
    public double percentile50() {
        return percentile50ResponseTime;
    }

    @Override
    public double percentile90() {
        return percentile90ResponseTime;
    }

    @Override
    public double percentile95() {
        return percentile95ResponseTime;
    }

    @Override
    public double percentile99() {
        return percentile99ResponseTime;
    }

    // ResourceMetrics implementation
    @Override
    public long memoryUsageBytes() {
        return avgMemoryUsageBytes;
    }

    @Override
    public long maxMemoryBytes() {
        return maxMemoryBytes;
    }

    @Override
    public double cpuUsagePercentage() {
        return avgCpuUsagePercentage;
    }

    @Override
    public long diskUsageBytes() {
        return avgDiskUsageBytes;
    }

    @Override
    public long totalDiskBytes() {
        return totalDiskBytes;
    }

    @Override
    public long networkBytesReceived() {
        return avgNetworkBytesReceived;
    }

    @Override
    public long networkBytesSent() {
        return avgNetworkBytesSent;
    }

    @Override
    public int activeThreads() {
        return avgActiveThreads;
    }

    @Override
    public int maxThreads() {
        return maxThreads;
    }

    /**
     * Get the time range duration in milliseconds.
     * 
     * @return time range duration in milliseconds
     */
    public long getTimeRangeDurationMs() {
        return timeRangeEndMs - timeRangeStartMs;
    }

    /**
     * Get the start of the time range.
     * 
     * @return start time in milliseconds
     */
    public long getTimeRangeStartMs() {
        return timeRangeStartMs;
    }

    /**
     * Get the end of the time range.
     * 
     * @return end time in milliseconds
     */
    public long getTimeRangeEndMs() {
        return timeRangeEndMs;
    }

    /**
     * Check if the trend is positive (increasing).
     * 
     * @return true if trend is increasing
     */
    public boolean isPositiveTrend() {
        return "increasing".equals(trendDirection);
    }

    /**
     * Check if the trend is negative (decreasing).
     * 
     * @return true if trend is decreasing
     */
    public boolean isNegativeTrend() {
        return "decreasing".equals(trendDirection);
    }

    /**
     * Check if the trend is stable.
     * 
     * @return true if trend is stable
     */
    public boolean isStableTrend() {
        return "stable".equals(trendDirection);
    }

    /**
     * Get the response time spread (99th percentile - 50th percentile).
     * 
     * @return response time spread in milliseconds
     */
    public double getResponseTimeSpread() {
        return percentile99ResponseTime - percentile50ResponseTime;
    }

    /**
     * Check if response times are consistent (low spread).
     * 
     * @return true if response time spread is less than 2x the median
     */
    public boolean hasConsistentResponseTimes() {
        return getResponseTimeSpread() < (percentile50ResponseTime * 2);
    }

    private static boolean isValidTrendDirection(String direction) {
        return "increasing".equals(direction) || "decreasing".equals(direction) || "stable".equals(direction);
    }

    /**
     * Create an empty ResourceManagerStatistics for when no data is available.
     * 
     * @param timeRangeStartMs start of time range
     * @param timeRangeEndMs end of time range
     * @return empty statistics
     */
    public static ResourceManagerStatistics empty(long timeRangeStartMs, long timeRangeEndMs) {
        return new ResourceManagerStatistics(0.0, // trendPercentage
                "stable", // trendDirection
                0.0, // changeRate
                0.0, // percentile50ResponseTime
                0.0, // percentile90ResponseTime
                0.0, // percentile95ResponseTime
                0.0, // percentile99ResponseTime
                0L, // avgMemoryUsageBytes
                0L, // maxMemoryBytes
                0.0, // avgCpuUsagePercentage
                0L, // avgDiskUsageBytes
                0L, // totalDiskBytes
                0L, // avgNetworkBytesReceived
                0L, // avgNetworkBytesSent
                0, // avgActiveThreads
                0, // maxThreads
                timeRangeStartMs, timeRangeEndMs);
    }
}
