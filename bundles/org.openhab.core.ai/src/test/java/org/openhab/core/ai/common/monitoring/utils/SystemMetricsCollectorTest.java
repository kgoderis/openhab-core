package org.openhab.core.ai.common.monitoring.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Test class for SystemMetricsCollector utility.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
class SystemMetricsCollectorTest {

    @Test
    void testGetCurrentMemoryUsage() {
        long memoryUsage = SystemMetricsCollector.getCurrentMemoryUsage();
        assertTrue(memoryUsage >= 0, "Memory usage should be non-negative");
    }

    @Test
    void testGetCurrentCpuUsage() {
        double cpuUsage = SystemMetricsCollector.getCurrentCpuUsage();
        assertTrue(cpuUsage >= 0, "CPU usage should be non-negative");
        // Note: CPU usage can be > 100% on multi-core systems, so we don't test upper bound
    }

    @Test
    void testGetSystemLoadAverage() {
        double loadAverage = SystemMetricsCollector.getSystemLoadAverage();
        assertTrue(loadAverage >= 0, "System load average should be non-negative");
    }

    @Test
    void testGetActiveThreadCount() {
        int threadCount = SystemMetricsCollector.getActiveThreadCount();
        assertTrue(threadCount > 0, "Active thread count should be positive");
    }

    @Test
    void testActiveOperationsCounter() {
        // Test initial state
        int initialCount = SystemMetricsCollector.getActiveOperationsCount();

        // Test increment
        int afterIncrement = SystemMetricsCollector.incrementActiveOperations();
        assertEquals(initialCount + 1, afterIncrement, "Increment should increase count by 1");

        // Test decrement
        int afterDecrement = SystemMetricsCollector.decrementActiveOperations();
        assertEquals(initialCount, afterDecrement, "Decrement should decrease count by 1");
    }

    @Test
    void testQueueSizeOperations() {
        // Test set queue size
        SystemMetricsCollector.setQueueSize(10);
        assertEquals(10, SystemMetricsCollector.getCurrentQueueSize(), "Queue size should be set correctly");

        // Test negative queue size (should be clamped to 0)
        SystemMetricsCollector.setQueueSize(-5);
        assertEquals(0, SystemMetricsCollector.getCurrentQueueSize(), "Negative queue size should be clamped to 0");

        // Test increment
        int afterIncrement = SystemMetricsCollector.incrementQueueSize();
        assertEquals(1, afterIncrement, "Increment should increase queue size by 1");

        // Test decrement
        int afterDecrement = SystemMetricsCollector.decrementQueueSize();
        assertEquals(0, afterDecrement, "Decrement should decrease queue size by 1");
    }

    @Test
    void testContentionCount() {
        long initialCount = SystemMetricsCollector.getTotalContentionCount();

        // Test increment
        long afterIncrement = SystemMetricsCollector.incrementContentionCount();
        assertEquals(initialCount + 1, afterIncrement, "Increment should increase contention count by 1");
    }

    @Test
    void testCalculateSystemHealthScore() {
        double healthScore = SystemMetricsCollector.calculateSystemHealthScore();
        assertTrue(healthScore >= 0 && healthScore <= 100,
                "System health score should be between 0 and 100, got: " + healthScore);
    }

    @Test
    void testCalculateResourceAvailability() {
        double availability = SystemMetricsCollector.calculateResourceAvailability();
        assertTrue(availability >= 0 && availability <= 100,
                "Resource availability should be between 0 and 100, got: " + availability);
    }

    @Test
    void testEstimateDiskIOTime() {
        long diskIOTime = SystemMetricsCollector.estimateDiskIOTime();
        assertTrue(diskIOTime >= 0, "Disk I/O time should be non-negative");
    }

    @Test
    void testGetActiveAlertsCount() {
        int alertsCount = SystemMetricsCollector.getActiveAlertsCount();
        assertTrue(alertsCount >= 0, "Active alerts count should be non-negative");
    }

    @Test
    void testConcurrentOperations() {
        // Test multiple concurrent operations
        int initialCount = SystemMetricsCollector.getActiveOperationsCount();

        // Simulate multiple operations starting
        SystemMetricsCollector.incrementActiveOperations();
        SystemMetricsCollector.incrementActiveOperations();
        SystemMetricsCollector.incrementActiveOperations();

        int afterIncrements = SystemMetricsCollector.getActiveOperationsCount();
        assertEquals(initialCount + 3, afterIncrements, "Should have 3 more active operations");

        // Simulate operations completing
        SystemMetricsCollector.decrementActiveOperations();
        SystemMetricsCollector.decrementActiveOperations();

        int afterDecrements = SystemMetricsCollector.getActiveOperationsCount();
        assertEquals(initialCount + 1, afterDecrements, "Should have 1 more active operation");
    }

    @Test
    void testQueueSizeBoundaryConditions() {
        // Test boundary conditions for queue size
        SystemMetricsCollector.setQueueSize(0);
        assertEquals(0, SystemMetricsCollector.getCurrentQueueSize());

        // Test decrementing from 0 (should not go negative)
        int result = SystemMetricsCollector.decrementQueueSize();
        assertEquals(0, result, "Queue size should not go below 0");
    }
}
