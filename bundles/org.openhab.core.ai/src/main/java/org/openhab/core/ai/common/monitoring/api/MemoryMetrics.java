package org.openhab.core.ai.common.monitoring.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Capability interface for memory usage metrics.
 * 
 * <p>
 * This interface provides functionality for tracking memory usage including
 * current usage, peak usage, and utilization percentages.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface MemoryMetrics {

    /**
     * Get the current memory usage in bytes.
     * 
     * @return current memory usage in bytes
     */
    long memoryUsageBytes();

    /**
     * Get the peak memory usage in bytes.
     * 
     * @return peak memory usage in bytes
     */
    long peakMemoryUsageBytes();

    /**
     * Get the memory utilization as a percentage.
     * 
     * @return memory utilization percentage (0.0 to 100.0)
     */
    double memoryUtilizationPercentage();

    /**
     * Get the available memory in bytes.
     * 
     * @return available memory in bytes
     */
    default long availableMemoryBytes() {
        return Runtime.getRuntime().totalMemory() - memoryUsageBytes();
    }

    /**
     * Check if memory usage is high (above 80% utilization).
     * 
     * @return true if memory utilization is above 80%
     */
    default boolean isHighMemoryUsage() {
        return memoryUtilizationPercentage() > 80.0;
    }

    /**
     * Check if memory usage is critical (above 95% utilization).
     * 
     * @return true if memory utilization is above 95%
     */
    default boolean isCriticalMemoryUsage() {
        return memoryUtilizationPercentage() > 95.0;
    }
}
