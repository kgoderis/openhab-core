package org.openhab.core.ai.common.monitoring.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Capability interface for resource monitoring metrics.
 * 
 * <p>
 * This interface provides functionality for monitoring system resources
 * including memory, CPU, disk, and network usage.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface ResourceMetrics {

    /**
     * Get memory usage in bytes.
     * 
     * @return memory usage in bytes
     */
    long memoryUsageBytes();

    /**
     * Get maximum memory in bytes.
     * 
     * @return maximum memory in bytes
     */
    long maxMemoryBytes();

    /**
     * Get CPU usage percentage.
     * 
     * @return CPU usage percentage (0.0-100.0)
     */
    double cpuUsagePercentage();

    /**
     * Get disk usage in bytes.
     * 
     * @return disk usage in bytes
     */
    long diskUsageBytes();

    /**
     * Get total disk space in bytes.
     * 
     * @return total disk space in bytes
     */
    long totalDiskBytes();

    /**
     * Get network bytes received.
     * 
     * @return network bytes received
     */
    long networkBytesReceived();

    /**
     * Get network bytes sent.
     * 
     * @return network bytes sent
     */
    long networkBytesSent();

    /**
     * Get the number of active threads.
     * 
     * @return number of active threads
     */
    int activeThreads();

    /**
     * Get the maximum number of threads.
     * 
     * @return maximum number of threads
     */
    int maxThreads();

    /**
     * Get memory usage percentage.
     * 
     * @return memory usage percentage (0.0-100.0)
     */
    default double memoryUsagePercentage() {
        return maxMemoryBytes() > 0 ? (memoryUsageBytes() * 100.0) / maxMemoryBytes() : 0.0;
    }

    /**
     * Get disk usage percentage.
     * 
     * @return disk usage percentage (0.0-100.0)
     */
    default double diskUsagePercentage() {
        return totalDiskBytes() > 0 ? (diskUsageBytes() * 100.0) / totalDiskBytes() : 0.0;
    }

    /**
     * Get thread usage percentage.
     * 
     * @return thread usage percentage (0.0-100.0)
     */
    default double threadUsagePercentage() {
        return maxThreads() > 0 ? (activeThreads() * 100.0) / maxThreads() : 0.0;
    }

    /**
     * Check if memory usage is high.
     * 
     * @return true if memory usage > 80%, false otherwise
     */
    default boolean isMemoryUsageHigh() {
        return memoryUsagePercentage() > 80.0;
    }

    /**
     * Check if CPU usage is high.
     * 
     * @return true if CPU usage > 80%, false otherwise
     */
    default boolean isCpuUsageHigh() {
        return cpuUsagePercentage() > 80.0;
    }

    /**
     * Check if disk usage is high.
     * 
     * @return true if disk usage > 80%, false otherwise
     */
    default boolean isDiskUsageHigh() {
        return diskUsagePercentage() > 80.0;
    }

    /**
     * Check if thread usage is high.
     * 
     * @return true if thread usage > 80%, false otherwise
     */
    default boolean isThreadUsageHigh() {
        return threadUsagePercentage() > 80.0;
    }

    /**
     * Get total network throughput in bytes per second.
     * 
     * @return network throughput in bytes per second
     */
    default long networkThroughputBytesPerSecond() {
        return networkBytesReceived() + networkBytesSent();
    }

    /**
     * Get available memory in bytes.
     * 
     * @return available memory in bytes
     */
    default long availableMemoryBytes() {
        return maxMemoryBytes() - memoryUsageBytes();
    }

    /**
     * Get available disk space in bytes.
     * 
     * @return available disk space in bytes
     */
    default long availableDiskBytes() {
        return totalDiskBytes() - diskUsageBytes();
    }
}
