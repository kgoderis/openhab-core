package org.openhab.core.ai.tool.logging;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
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
    private @Nullable MetricsService metricsService;

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

        // Use centralized metrics service
        if (metricsService != null) {
            metricsService.recordOperation("tool", toolId, success, java.time.Duration.ofMillis(executionTime));
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

        // Use centralized metrics service
        if (metricsService != null) {
            metricsService.recordOperation("server", requestType, success, java.time.Duration.ofMillis(processingTime));
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

        // Use centralized metrics service
        if (metricsService != null) {
            metricsService.recordOperation("transport", "health-check", "UP".equals(healthStatus),
                    java.time.Duration.ZERO);
        }

        // Structured logging
        logger.info("Transport health check - Type: {}, Status: {}", transportType, healthStatus);
    }

    /**
     * Log security event using the new monitoring framework.
     * 
     * @param eventType Type of security event
     * @param severity Event severity
     * @param details Event details
     */
    public void logSecurityEvent(String eventType, String severity, @Nullable Map<String, Object> details) {

        // Use centralized metrics service
        if (metricsService != null) {
            metricsService.recordOperation("security", eventType, true, java.time.Duration.ZERO);
        }

        // Structured logging
        logger.info("Security event - Type: {}, Severity: {}", eventType, severity);
    }

    /**
     * Log performance metric using the new monitoring framework.
     * 
     * @param component Component name
     * @param operation Operation name
     * @param duration Duration in milliseconds
     * @param success Whether operation was successful
     */
    public void logPerformanceMetric(String component, String operation, long duration, boolean success) {

        // Use centralized metrics service
        if (metricsService != null) {
            metricsService.recordOperation("performance", component + "." + operation, success,
                    java.time.Duration.ofMillis(duration));
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
