package org.openhab.core.ai.tool.logging;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.tool.server.TransportType;
import org.osgi.service.component.annotations.Component;
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

    // Performance tracking
    private final AtomicLong totalToolExecutions = new AtomicLong(0);
    private final AtomicLong totalToolExecutionTime = new AtomicLong(0);
    private final AtomicLong totalServerRequests = new AtomicLong(0);

    /**
     * Log tool execution with enhanced details.
     * 
     * @param toolId Tool identifier
     * @param parameters Tool parameters
     * @param executionTime Execution time in milliseconds
     * @param success Whether execution was successful
     * @param result Execution result or error message
     */
    public void logToolExecution(String toolId, Map<String, Object> parameters, long executionTime, boolean success,
            String result) {

        // Update performance metrics
        totalToolExecutions.incrementAndGet();
        totalToolExecutionTime.addAndGet(executionTime);

        if (success) {
            logger.info("MCP Tool executed: id={}, duration={}ms, result={}", toolId, executionTime, result);
        } else {
            logger.error("MCP Tool failed: id={}, duration={}ms, error={}", toolId, executionTime, result);
        }

        // Log detailed parameters for debugging
        if (logger.isDebugEnabled()) {
            logger.debug("MCP Tool parameters: id={}, params={}", toolId, parameters);
        }
    }

    /**
     * Log server lifecycle events.
     * 
     * @param serverId Server identifier
     * @param action Lifecycle action (created, started, stopped, etc.)
     * @param transportType Transport type (STDIO, SSE, etc.)
     * @param success Whether action was successful
     * @param details Additional details or error message
     */
    public void logServerLifecycle(String serverId, String action, String transportType, boolean success,
            String details) {

        if (success) {
            logger.info("MCP Server {}: action={}, transport={}, details={}", serverId, action, transportType, details);
        } else {
            logger.error("MCP Server {}: action={}, transport={}, error={}", serverId, action, transportType, details);
        }
    }

    /**
     * Log transport health status.
     * 
     * @param serverId Server identifier
     * @param transportType Transport type
     * @param healthy Whether transport is healthy
     * @param error Error message if unhealthy
     * @param uptime Transport uptime in milliseconds
     */
    public void logTransportHealth(String serverId, TransportType transportType, boolean healthy, String error,
            long uptime) {

        if (healthy) {
            logger.info("MCP Transport healthy: server={}, transport={}, uptime={}ms", serverId, transportType, uptime);
        } else {
            logger.warn("MCP Transport unhealthy: server={}, transport={}, error={}, uptime={}ms", serverId,
                    transportType, error, uptime);
        }
    }

    /**
     * Log security events and violations.
     * 
     * @param clientId Client identifier
     * @param eventType Type of security event
     * @param details Event details
     * @param success Whether the security check passed
     * @param serverId Server identifier
     */
    public void logSecurityEvent(String clientId, String eventType, String details, boolean success, String serverId) {

        if (success) {
            logger.info("MCP Security event: client={}, type={}, server={}, details={}", clientId, eventType, serverId,
                    details);
        } else {
            logger.warn("MCP Security violation: client={}, type={}, server={}, details={}", clientId, eventType,
                    serverId, details);
        }
    }

    /**
     * Log performance metrics.
     * 
     * @param serverId Server identifier
     * @param requestCount Total request count
     * @param totalExecutionTime Total execution time in milliseconds
     * @param avgExecutionTime Average execution time in milliseconds
     * @param activeConnections Number of active connections
     */
    public void logPerformanceMetrics(String serverId, long requestCount, long totalExecutionTime,
            double avgExecutionTime, int activeConnections) {

        logger.info("MCP Performance: server={}, requests={}, avgTime={:.2f}ms, connections={}", serverId, requestCount,
                avgExecutionTime, activeConnections);
    }

    /**
     * Log tool registry events.
     * 
     * @param action Registry action (discovered, registered, unregistered)
     * @param toolId Tool identifier
     * @param toolCount Total number of tools
     * @param success Whether action was successful
     */
    public void logToolRegistryEvent(String action, String toolId, int toolCount, boolean success) {

        if (success) {
            logger.info("MCP Tool Registry: action={}, tool={}, totalTools={}", action, toolId, toolCount);
        } else {
            logger.error("MCP Tool Registry: action={}, tool={}, totalTools={}, failed", action, toolId, toolCount);
        }
    }

    /**
     * Log connection events.
     * 
     * @param clientId Client identifier
     * @param transportType Transport type
     * @param action Connection action (connected, disconnected, rejected)
     * @param success Whether action was successful
     * @param details Additional details
     */
    public void logConnectionEvent(String clientId, String transportType, String action, boolean success,
            String details) {

        if (success) {
            logger.info("MCP Connection: client={}, transport={}, action={}, details={}", clientId, transportType,
                    action, details);
        } else {
            logger.warn("MCP Connection: client={}, transport={}, action={}, error={}", clientId, transportType, action,
                    details);
        }
    }

    /**
     * Log error recovery events.
     * 
     * @param errorType Type of error
     * @param errorMessage Error message
     * @param recoveryAction Recovery action taken
     * @param success Whether recovery was successful
     * @param serverId Server identifier
     */
    public void logErrorRecovery(String errorType, String errorMessage, String recoveryAction, boolean success,
            String serverId) {

        if (success) {
            logger.info("MCP Error Recovery: type={}, action={}, server={}, message={}", errorType, recoveryAction,
                    serverId, errorMessage);
        } else {
            logger.error("MCP Error Recovery Failed: type={}, action={}, server={}, message={}", errorType,
                    recoveryAction, serverId, errorMessage);
        }
    }

    /**
     * Log configuration events.
     * 
     * @param serverId Server identifier
     * @param configType Configuration type
     * @param action Configuration action (loaded, saved, updated, validated)
     * @param success Whether action was successful
     * @param details Additional details
     */
    public void logConfigurationEvent(String serverId, String configType, String action, boolean success,
            String details) {

        if (success) {
            logger.info("MCP Configuration: server={}, type={}, action={}, details={}", serverId, configType, action,
                    details);
        } else {
            logger.error("MCP Configuration: server={}, type={}, action={}, error={}", serverId, configType, action,
                    details);
        }
    }

    /**
     * Get current performance statistics.
     * 
     * @return Map containing performance statistics
     */
    public Map<String, Object> getPerformanceStatistics() {
        long totalExecutions = totalToolExecutions.get();
        long totalTime = totalToolExecutionTime.get();
        double avgExecutionTime = totalExecutions > 0 ? (double) totalTime / totalExecutions : 0.0;

        return Map.of("totalToolExecutions", totalExecutions, "totalToolExecutionTime", totalTime,
                "averageToolExecutionTime", avgExecutionTime, "totalServerRequests", totalServerRequests.get());
    }

    /**
     * Reset performance statistics.
     */
    public void resetPerformanceStatistics() {
        totalToolExecutions.set(0);
        totalToolExecutionTime.set(0);
        totalServerRequests.set(0);
        logger.info("MCP Performance statistics reset");
    }

    /**
     * Log a general MCP event with structured format.
     * 
     * @param component Component name (server, tool, transport, etc.)
     * @param event Event name
     * @param level Log level (INFO, WARN, ERROR, DEBUG)
     * @param details Event details
     */
    public void logMCPEvent(String component, String event, String level, Map<String, Object> details) {
        String detailsStr = details != null ? details.toString() : "{}";

        switch (level.toUpperCase()) {
            case "DEBUG":
                logger.debug("MCP {} {}: {}", component, event, detailsStr);
                break;
            case "INFO":
                logger.info("MCP {} {}: {}", component, event, detailsStr);
                break;
            case "WARN":
                logger.warn("MCP {} {}: {}", component, event, detailsStr);
                break;
            case "ERROR":
                logger.error("MCP {} {}: {}", component, event, detailsStr);
                break;
            default:
                logger.info("MCP {} {}: {}", component, event, detailsStr);
        }
    }
}
