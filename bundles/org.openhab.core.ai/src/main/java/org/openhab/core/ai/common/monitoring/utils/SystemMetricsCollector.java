package org.openhab.core.ai.common.monitoring.utils;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.atomic.AtomicInteger;


import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Utility class for collecting system metrics for performance monitoring.
 * 
 * <p>
 * This class provides methods to collect various system metrics that can be
 * used with the enhanced OperationRecorder performance metrics.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class SystemMetricsCollector {

    private static final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    private static final OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
    private static final ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

    // Thread-safe counters for concurrency metrics
    private static final AtomicInteger activeOperations = new AtomicInteger(0);
    private static final AtomicInteger queueSize = new AtomicInteger(0);
    // private static final AtomicLong contentionCount = new AtomicLong(0); // Migrated to MetricsService

    /**
     * Get current memory usage in bytes.
     * 
     * @return the current memory usage in bytes
     */
    public static long getCurrentMemoryUsage() {
        return memoryBean.getHeapMemoryUsage().getUsed();
    }

    /**
     * Get current CPU usage percentage.
     * 
     * @return the current CPU usage percentage (0-100)
     */
    public static double getCurrentCpuUsage() {
        // Note: This is a simplified calculation. In production, you might want
        // to use a more sophisticated approach like calculating CPU usage over time
        return osBean.getSystemLoadAverage() * 100.0;
    }

    /**
     * Get system load average.
     * 
     * @return the system load average
     */
    public static double getSystemLoadAverage() {
        return osBean.getSystemLoadAverage();
    }

    /**
     * Get current number of active threads.
     * 
     * @return the current number of active threads
     */
    public static int getActiveThreadCount() {
        return threadBean.getThreadCount();
    }

    /**
     * Get current number of active operations.
     * 
     * @return the current number of active operations
     */
    public static int getActiveOperationsCount() {
        return activeOperations.get();
    }

    /**
     * Get current queue size.
     * 
     * @return the current queue size
     */
    public static int getCurrentQueueSize() {
        return queueSize.get();
    }

    /**
     * Get total contention count.
     * 
     * @return the total contention count
     */
    public static long getTotalContentionCount() {
        return 0; // Migrated to MetricsService
    }

    /**
     * Increment the active operations counter.
     * 
     * @return the new count
     */
    public static int incrementActiveOperations() {
        return activeOperations.incrementAndGet();
    }

    /**
     * Decrement the active operations counter.
     * 
     * @return the new count
     */
    public static int decrementActiveOperations() {
        return activeOperations.decrementAndGet();
    }

    /**
     * Set the queue size.
     * 
     * @param size the new queue size
     */
    public static void setQueueSize(int size) {
        queueSize.set(Math.max(0, size));
    }

    /**
     * Increment the queue size.
     * 
     * @return the new queue size
     */
    public static int incrementQueueSize() {
        return queueSize.incrementAndGet();
    }

    /**
     * Decrement the queue size.
     * 
     * @return the new queue size
     */
    public static int decrementQueueSize() {
        return queueSize.decrementAndGet();
    }

    /**
     * Increment the contention count.
     * 
     * @return the new contention count
     */
    public static long incrementContentionCount() {
        return 0; // Migrated to MetricsService
    }

    /**
     * Calculate system health score based on various metrics.
     * 
     * @return the system health score (0-100)
     */
    public static double calculateSystemHealthScore() {
        double memoryUsage = (double) getCurrentMemoryUsage() / memoryBean.getHeapMemoryUsage().getMax();
        double cpuUsage = getCurrentCpuUsage() / 100.0;
        double loadAverage = getSystemLoadAverage() / osBean.getAvailableProcessors();

        // Calculate health score (higher is better)
        double healthScore = 100.0;

        // Penalize high memory usage
        if (memoryUsage > 0.8) {
            healthScore -= (memoryUsage - 0.8) * 50;
        }

        // Penalize high CPU usage
        if (cpuUsage > 0.8) {
            healthScore -= (cpuUsage - 0.8) * 30;
        }

        // Penalize high load average
        if (loadAverage > 1.0) {
            healthScore -= (loadAverage - 1.0) * 20;
        }

        return Math.max(0, Math.min(100, healthScore));
    }

    /**
     * Calculate resource availability percentage.
     * 
     * @return the resource availability percentage (0-100)
     */
    public static double calculateResourceAvailability() {
        double memoryUsage = (double) getCurrentMemoryUsage() / memoryBean.getHeapMemoryUsage().getMax();
        double cpuUsage = getCurrentCpuUsage() / 100.0;

        // Calculate availability (higher is better)
        double memoryAvailability = (1.0 - memoryUsage) * 100;
        double cpuAvailability = (1.0 - cpuUsage) * 100;

        // Return the minimum of the two (bottleneck approach)
        return Math.min(memoryAvailability, cpuAvailability);
    }

    /**
     * Estimate disk I/O time based on system activity.
     * 
     * @return estimated disk I/O time in milliseconds
     */
    public static long estimateDiskIOTime() {
        // This is a simplified estimation. In production, you might want to
        // use more sophisticated monitoring tools or JMX beans
        double loadAverage = getSystemLoadAverage();
        if (loadAverage > 2.0) {
            return (long) ((loadAverage - 2.0) * 50); // Estimate higher I/O with higher load
        }
        return 0;
    }

    /**
     * Get the number of active system alerts (simplified).
     * 
     * @return the number of active system alerts
     */
    public static int getActiveAlertsCount() {
        int alerts = 0;

        // Check memory usage
        double memoryUsage = (double) getCurrentMemoryUsage() / memoryBean.getHeapMemoryUsage().getMax();
        if (memoryUsage > 0.9) {
            alerts++;
        }

        // Check CPU usage
        double cpuUsage = getCurrentCpuUsage();
        if (cpuUsage > 90) {
            alerts++;
        }

        // Check load average
        double loadAverage = getSystemLoadAverage();
        if (loadAverage > osBean.getAvailableProcessors() * 2) {
            alerts++;
        }

        return alerts;
    }
}
