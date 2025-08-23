package org.openhab.core.ai.tool.services;

import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.MetricKeys;
import org.openhab.core.ai.common.monitoring.collector.ExecutionMetricsCollector;
import org.openhab.core.ai.common.monitoring.registry.MonitoringRegistry;
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
    private @Nullable MonitoringRegistry monitoringRegistry;

    /**
     * Record tool execution using the new monitoring framework
     */
    public void recordToolExecution(String toolName, long executionTime, boolean success) {
        try {
            // Use centralized monitoring registry
            if (monitoringRegistry != null) {
                ExecutionMetricsCollector collector = monitoringRegistry.executionCollector(MetricKeys.tool(toolName));
                collector.recordExecution(success, executionTime * 1_000_000L); // Convert to nanoseconds
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
            if (monitoringRegistry != null) {
                ExecutionMetricsCollector collector = monitoringRegistry.executionCollector(MetricKeys.tool(toolName));
                collector.recordExecution(false, 0L);
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

        if (monitoringRegistry != null) {
            // Get statistics from monitoring registry for specific tool
            ExecutionMetricsCollector collector = monitoringRegistry.executionCollector(MetricKeys.tool(toolName));
            var snapshot = collector.snapshot();

            statistics.put("toolName", toolName);
            statistics.put("totalExecutions", snapshot.total());
            statistics.put("successfulExecutions", snapshot.success());
            statistics.put("failedExecutions", snapshot.failure());
            statistics.put("totalExecutionTime", snapshot.totalDurationNanos() / 1_000_000); // Convert from nanoseconds
            statistics.put("averageExecutionTime",
                    snapshot.totalDurationNanos() / Math.max(1, snapshot.total()) / 1_000_000); // Convert from
                                                                                                // nanoseconds
            statistics.put("successRate", snapshot.successRate());
            statistics.put("timestamp", Instant.now());
        } else {
            // Fallback to basic statistics
            statistics.put("toolName", toolName);
            statistics.put("totalExecutions", 0);
            statistics.put("successfulExecutions", 0);
            statistics.put("failedExecutions", 0);
            statistics.put("totalExecutionTime", 0);
            statistics.put("averageExecutionTime", 0);
            statistics.put("successRate", 0.0);
            statistics.put("timestamp", Instant.now());
        }

        return statistics;
    }

    /**
     * Get overall tool statistics using the new monitoring framework
     */
    public Map<String, Object> getOverallStatistics() {
        Map<String, Object> statistics = new java.util.HashMap<>();

        if (monitoringRegistry != null) {
            // Get statistics from monitoring registry for all tools
            ExecutionMetricsCollector collector = monitoringRegistry
                    .executionCollector(MetricKeys.action("tool-execution"));
            var snapshot = collector.snapshot();

            statistics.put("totalToolExecutions", snapshot.total());
            statistics.put("successfulToolExecutions", snapshot.success());
            statistics.put("failedToolExecutions", snapshot.failure());
            statistics.put("totalExecutionTime", snapshot.totalDurationNanos() / 1_000_000); // Convert from nanoseconds
            statistics.put("averageExecutionTime",
                    snapshot.totalDurationNanos() / Math.max(1, snapshot.total()) / 1_000_000); // Convert from
                                                                                                // nanoseconds
            statistics.put("overallSuccessRate", snapshot.successRate());
            statistics.put("timestamp", Instant.now());
        } else {
            // Fallback to basic statistics
            statistics.put("totalToolExecutions", 0);
            statistics.put("successfulToolExecutions", 0);
            statistics.put("failedToolExecutions", 0);
            statistics.put("totalExecutionTime", 0);
            statistics.put("averageExecutionTime", 0);
            statistics.put("overallSuccessRate", 0.0);
            statistics.put("timestamp", Instant.now());
        }

        return statistics;
    }

    /**
     * Reset tool metrics for a specific tool
     */
    public void resetToolMetrics(String toolName) {
        if (monitoringRegistry != null) {
            monitoringRegistry.reset(MetricKeys.tool(toolName));
            logger.info("Tool metrics reset for tool: {}", toolName);
        }
    }

    /**
     * Reset all tool metrics
     */
    public void resetAllMetrics() {
        if (monitoringRegistry != null) {
            monitoringRegistry.reset(MetricKeys.action("tool-execution"));
            logger.info("All tool metrics reset");
        }
    }
}
