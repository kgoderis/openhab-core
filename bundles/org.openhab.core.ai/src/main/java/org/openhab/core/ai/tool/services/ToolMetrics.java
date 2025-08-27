package org.openhab.core.ai.tool.services;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import java.util.Map;
import java.util.Set;
import org.openhab.core.ai.common.monitoring.api.MetricKey;
import org.openhab.core.ai.common.monitoring.api.MetricKeys;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Metrics for tool execution operations using the new monitoring framework.
 * 
 * <p>
 * This class provides comprehensive metrics for tool execution operations:
 * - Tool execution statistics
 * - Performance timing and processing metrics
 * - Error tracking and failure analysis
 * - Success rate calculations
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@Component(service = ToolMetrics.class)
@NonNullByDefault
public class ToolMetrics {

    private static final Logger logger = LoggerFactory.getLogger(ToolMetrics.class);

    // NEW: Monitoring registry for centralized metrics collection
    @Reference
    private @Nullable MetricsService metricsService;

    /**
     * Record tool execution using the new monitoring framework
     */
    public void recordToolExecution(String toolName, long executionTime, boolean success) {
        try {
            // Use centralized monitoring registry
            MetricsService metrics = metricsService;
            if (metrics != null) {
                metrics.recordOperation("tool", "execution", success, java.time.Duration.ofMillis(executionTime));
            }

            // Log the operation
            if (success) {
                logger.debug("Tool execution successful - Tool: {}, Duration: {}ms", toolName, executionTime);
            } else {
                logger.warn("Tool execution failed - Tool: {}, Duration: {}ms", toolName, executionTime);
            }

        } catch (Exception e) {
            logger.error("Error recording tool execution metrics for tool: {}", toolName, e);
        }
    }

    /**
     * Record tool error using the new monitoring framework
     */
    public void recordToolError(String toolName, String errorType) {
        try {
            // Use centralized monitoring registry
            MetricsService metrics = metricsService;
            if (metrics != null) {
                metrics.recordOperation("tool", "error", false, java.time.Duration.ofMillis(0));
            }

            // Log the error
            logger.warn("Tool error recorded - Tool: {}, Error: {}", toolName, errorType);

        } catch (Exception e) {
            logger.error("Error recording tool error metrics for tool: {}", toolName, e);
        }
    }

    /**
     * Get tool execution statistics using the new monitoring framework
     */
    public Map<String, Object> getToolStatistics(String toolName) {
        Map<String, Object> statistics = new java.util.HashMap<>();

        MetricsService metrics = metricsService;
        if (metrics != null) {
            try {
                // Get statistics from metrics service for specific tool
                MetricKey toolKey = MetricKeys.custom("tool", Map.of("toolName", toolName), Set.of("counts", "latency"));
                var snapshot = metrics.getSnapshot(toolKey, org.openhab.core.ai.common.monitoring.service.snapshot.GenericMetricsSnapshot.class);

                if (snapshot != null) {
                    long totalOperations = snapshot.getLong("total");
                    long successfulOperations = snapshot.getLong("success");
                    long failedOperations = snapshot.getLong("failure");
                    long totalDurationNanos = snapshot.getLong("totalDurationNanos");
                    
                    statistics.put("toolName", toolName);
                    statistics.put("totalExecutions", totalOperations);
                    statistics.put("successfulExecutions", successfulOperations);
                    statistics.put("failedExecutions", failedOperations);
                    statistics.put("totalExecutionTimeMs", totalDurationNanos / 1_000_000); // Convert from nanoseconds
                    statistics.put("averageExecutionTimeMs",
                            totalOperations > 0
                                    ? totalDurationNanos / (totalOperations * 1_000_000)
                                    : 0);
                    statistics.put("successRate", totalOperations > 0 ? (double) successfulOperations / totalOperations : 0.0);
                }
                statistics.put("lastUpdated", System.currentTimeMillis());
            } catch (Exception e) {
                logger.debug("Failed to get tool statistics: {}", e.getMessage());
            }
        }

        return statistics;
    }

    /**
     * Get overall tool execution statistics using the new monitoring framework
     */
    public Map<String, Object> getAllToolStatistics() {
        Map<String, Object> statistics = new java.util.HashMap<>();

        MetricsService metrics = metricsService;
        if (metrics != null) {
            try {
                // Get statistics from metrics service for all tools
                MetricKey toolKey = MetricKeys.custom("tool", Map.of(), Set.of("counts", "latency"));
                var snapshot = metrics.getSnapshot(toolKey, org.openhab.core.ai.common.monitoring.service.snapshot.GenericMetricsSnapshot.class);
                
                if (snapshot != null) {
                    long totalOperations = snapshot.getLong("total");
                    long successfulOperations = snapshot.getLong("success");
                    long failedOperations = snapshot.getLong("failure");
                    long totalDurationNanos = snapshot.getLong("totalDurationNanos");
                    
                    statistics.put("totalToolExecutions", totalOperations);
                    statistics.put("successfulToolExecutions", successfulOperations);
                    statistics.put("failedToolExecutions", failedOperations);
                    statistics.put("totalExecutionTimeMs", totalDurationNanos / 1_000_000); // Convert from nanoseconds
                    statistics.put("averageExecutionTimeMs",
                            totalOperations > 0
                                    ? totalDurationNanos / (totalOperations * 1_000_000)
                                    : 0);
                    statistics.put("successRate", totalOperations > 0 ? (double) successfulOperations / totalOperations : 0.0);
                    statistics.put("lastUpdated", System.currentTimeMillis());
                }
            } catch (Exception e) {
                logger.debug("Failed to get all tool statistics: {}", e.getMessage());
            }
        }

        return statistics;
    }

    /**
     * Reset tool metrics for a specific tool.
     * 
     * @param toolName the tool name
     */
    public void resetToolMetrics(String toolName) {
        // Reset functionality is not available in MetricsService
        logger.info("Tool metrics reset not supported for tool: {}", toolName);
    }

    /**
     * Reset all tool metrics.
     */
    public void resetAllMetrics() {
        // Reset functionality is not available in MetricsService
        logger.info("All tool metrics reset not supported");
    }
}
