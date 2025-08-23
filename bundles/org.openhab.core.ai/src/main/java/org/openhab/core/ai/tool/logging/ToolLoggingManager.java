package org.openhab.core.ai.tool.logging;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.MetricKeys;
import org.openhab.core.ai.common.monitoring.collector.ExecutionMetricsCollector;
import org.openhab.core.ai.common.monitoring.registry.MonitoringRegistry;
import org.openhab.core.ai.common.transport.TransportType;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enhanced logging manager for MCP bundle with structured logging capabilities.
 * 
 * This component provides centralized, structured logging for all MCP operations
 * including tool execution, server lifecycle, transport health, security events,
 * and performance metrics.
 * 
 * 
 */
@Component(service = ToolLoggingManager.class)
@NonNullByDefault
public class ToolLoggingManager {

    private static final Logger logger = LoggerFactory.getLogger(ToolLoggingManager.class);

    // NEW: Monitoring registry for centralized metrics collection
    @Reference
    private @Nullable MonitoringRegistry monitoringRegistry;

    /**
     * Log tool execution with enhanced details using the new monitoring framework.
     * 
     * @param toolId Tool identifier
     * @param parameters Tool parameters
     * @param executionTime Execution time in milliseconds
     * @param success Whether execution was successful
     * @param result Execution result or error message
     */
    public void logToolExecution(String toolId, Map<String, Object> parameters, long executionTime, boolean success,
            @Nullable String result) {

        // Use centralized monitoring registry
        if (monitoringRegistry != null) {
            ExecutionMetricsCollector collector = monitoringRegistry.executionCollector(MetricKeys.tool(toolId));
            collector.recordExecution(success, executionTime * 1_000_000L); // Convert to nanoseconds
        }

        // Structured logging
        if (success) {
            logger.info("Tool execution successful - Tool: {}, Duration: {}ms, Result: {}", toolId, executionTime,
                    result != null ? result.substring(0, Math.min(result.length(), 100)) + "..." : "null");
        } else {
            logger.error("Tool execution failed - Tool: {}, Duration: {}ms, Error: {}", toolId, executionTime,
                    result != null ? result : "Unknown error");
        }
    }

    /**
     * Log server request with enhanced details using the new monitoring framework.
     * 
     * @param requestType Type of request
     * @param transportType Transport type used
     * @param processingTime Processing time in milliseconds
     * @param success Whether request was successful
     * @param details Additional request details
     */
    public void logServerRequest(String requestType, TransportType transportType, long processingTime, boolean success,
            @Nullable Map<String, Object> details) {

        // Use centralized monitoring registry
        if (monitoringRegistry != null) {
            ExecutionMetricsCollector collector = monitoringRegistry
                    .executionCollector(MetricKeys.action("server-" + requestType));
            collector.recordExecution(success, processingTime * 1_000_000L); // Convert to nanoseconds
        }

        // Structured logging
        if (success) {
            logger.info("Server request successful - Type: {}, Transport: {}, Duration: {}ms", requestType,
                    transportType, processingTime);
        } else {
            logger.error("Server request failed - Type: {}, Transport: {}, Duration: {}ms", requestType, transportType,
                    processingTime);
        }
    }

    /**
     * Log transport health event using the new monitoring framework.
     * 
     * @param transportType Transport type
     * @param healthStatus Health status
     * @param details Health details
     */
    public void logTransportHealth(TransportType transportType, String healthStatus,
            @Nullable Map<String, Object> details) {

        // Use centralized monitoring registry
        if (monitoringRegistry != null) {
            ExecutionMetricsCollector collector = monitoringRegistry
                    .executionCollector(MetricKeys.action("transport-health"));
            // Record health check as successful execution
            collector.recordExecution("UP".equals(healthStatus), 0L);
        }

        // Structured logging
        logger.info("Transport health check - Type: {}, Status: {}, Details: {}", transportType, healthStatus, details);
    }

    /**
     * Log security event using the new monitoring framework.
     * 
     * @param eventType Security event type
     * @param severity Event severity
     * @param details Security event details
     */
    public void logSecurityEvent(String eventType, String severity, @Nullable Map<String, Object> details) {

        // Use centralized monitoring registry
        if (monitoringRegistry != null) {
            ExecutionMetricsCollector collector = monitoringRegistry
                    .executionCollector(MetricKeys.action("security-" + eventType));
            // Record security event as successful execution
            collector.recordExecution(true, 0L);
        }

        // Structured logging
        switch (severity.toUpperCase()) {
            case "CRITICAL":
                logger.error("Security event - Type: {}, Severity: {}, Details: {}", eventType, severity, details);
                break;
            case "HIGH":
                logger.warn("Security event - Type: {}, Severity: {}, Details: {}", eventType, severity, details);
                break;
            case "MEDIUM":
                logger.info("Security event - Type: {}, Severity: {}, Details: {}", eventType, severity, details);
                break;
            case "LOW":
                logger.debug("Security event - Type: {}, Severity: {}, Details: {}", eventType, severity, details);
                break;
            default:
                logger.info("Security event - Type: {}, Severity: {}, Details: {}", eventType, severity, details);
        }
    }

    /**
     * Log performance metrics using the new monitoring framework.
     * 
     * @param component Component name
     * @param operation Operation name
     * @param duration Duration in milliseconds
     * @param success Whether operation was successful
     */
    public void logPerformanceMetrics(String component, String operation, long duration, boolean success) {

        // Use centralized monitoring registry
        if (monitoringRegistry != null) {
            ExecutionMetricsCollector collector = monitoringRegistry
                    .executionCollector(MetricKeys.action(component + "." + operation));
            collector.recordExecution(success, duration * 1_000_000L); // Convert to nanoseconds
        }

        // Structured logging
        if (success) {
            logger.debug("Performance metric - Component: {}, Operation: {}, Duration: {}ms", component, operation,
                    duration);
        } else {
            logger.warn("Performance metric - Component: {}, Operation: {}, Duration: {}ms (FAILED)", component,
                    operation, duration);
        }
    }
}
