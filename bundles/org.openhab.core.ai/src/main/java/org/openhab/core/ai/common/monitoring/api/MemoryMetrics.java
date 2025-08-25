package org.openhab.core.ai.common.monitoring.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Capability interface for memory-specific metrics.
 * 
 * <p>
 * This interface provides memory-specific functionality including heap usage,
 * non-heap usage, memory allocation rates, garbage collection metrics, and
 * memory efficiency indicators.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface MemoryMetrics {

    /**
     * Get the heap memory usage as a percentage.
     * 
     * @return heap memory usage between 0.0 and 100.0
     */
    double heapMemoryUsage();

    /**
     * Get the non-heap memory usage as a percentage.
     * 
     * @return non-heap memory usage between 0.0 and 100.0
     */
    double nonHeapMemoryUsage();

    /**
     * Get the total memory usage as a percentage.
     * 
     * @return total memory usage between 0.0 and 100.0
     */
    double totalMemoryUsage();

    /**
     * Get the memory allocation rate in bytes per second.
     * 
     * @return memory allocation rate
     */
    double memoryAllocationRate();

    /**
     * Get the garbage collection frequency per minute.
     * 
     * @return garbage collection frequency
     */
    double garbageCollectionFrequency();

    /**
     * Get the memory efficiency score (0-100).
     * 
     * @return memory efficiency score
     */
    double memoryEfficiency();

    /**
     * Get the memory fragmentation level (0-100).
     * 
     * @return memory fragmentation level
     */
    double memoryFragmentation();

    /**
     * Get the available memory in bytes.
     * 
     * @return available memory in bytes
     */
    long availableMemory();

    /**
     * Get the total memory in bytes.
     * 
     * @return total memory in bytes
     */
    long totalMemory();

    /**
     * Get the used memory in bytes.
     * 
     * @return used memory in bytes
     */
    long usedMemory();
}
