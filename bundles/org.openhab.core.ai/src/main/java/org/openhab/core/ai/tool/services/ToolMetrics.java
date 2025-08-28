package org.openhab.core.ai.tool.services;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pure metrics recording helper for tool execution operations.
 * 
 * <p>
 * This class provides lightweight metrics recording for tool operations:
 * - Tool execution recording via MetricsService
 * - Error occurrence recording
 * </p>
 * 
 * <p>
 * All statistics retrieval should be done directly through MetricsService
 * using appropriate MetricKeys. This class only handles recording operations.
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
        if (toolName == null || toolName.trim().isEmpty()) {
            return; // Silently skip invalid input
        }

        MetricsService metrics = metricsService;
        if (metrics != null) {
            try {
                metrics.recordOperation("tool", "execution", success, java.time.Duration.ofMillis(executionTime));
            } catch (Exception e) {
                logger.debug("Failed to record tool execution metrics: {}", e.getMessage());
            }
        }
    }

    /**
     * Record tool error using the new monitoring framework
     */
    public void recordToolError(String toolName, String errorType) {
        if (toolName == null || toolName.trim().isEmpty()) {
            return; // Silently skip invalid input
        }

        MetricsService metrics = metricsService;
        if (metrics != null) {
            try {
                metrics.recordOperation("tool", "error", false, java.time.Duration.ofMillis(0));
            } catch (Exception e) {
                logger.debug("Failed to record tool error metrics: {}", e.getMessage());
            }
        }
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
