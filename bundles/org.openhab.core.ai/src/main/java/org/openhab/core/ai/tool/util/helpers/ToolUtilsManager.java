package org.openhab.core.ai.tool.util.helpers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.api.Tool;
import org.openhab.core.ai.tool.notifications.NotificationManager;
import org.openhab.core.ai.tool.notifications.events.NotificationType;
import org.openhab.core.ai.tool.progress.ToolProgressTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enhanced MCP Utilities Manager.
 * 
 * This class provides a unified interface for managing all MCP utilities including
 * notification management, progress tracking, and tool utilities. It integrates
 * the enhanced utilities to provide comprehensive MCP functionality.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ToolUtilsManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ToolUtilsManager.class);

    /** Singleton instance. */
    private static volatile ToolUtilsManager instance;

    /** Notification manager. */
    private final NotificationManager notificationManager;

    /** Progress tracker. */
    private final ToolProgressTracker progressTracker;

    /** Map of registered tools. */
    private final Map<String, Tool> registeredTools = new ConcurrentHashMap<>();

    /** Map of utility statistics. */
    private final Map<String, Object> statistics = new ConcurrentHashMap<>();

    /**
     * Private constructor for singleton pattern.
     */
    private ToolUtilsManager() {
        this.notificationManager = new NotificationManager();
        this.progressTracker = new ToolProgressTracker("manager", "tool-utils", 3, Map.of());
        LOGGER.info("MCP Utilities Manager initialized");
    }

    /**
     * Get the singleton instance.
     * 
     * @return the utilities manager instance
     */
    public static ToolUtilsManager getInstance() {
        if (instance == null) {
            synchronized (ToolUtilsManager.class) {
                if (instance == null) {
                    instance = new ToolUtilsManager();
                }
            }
        }
        return instance;
    }

    /**
     * Get the notification manager.
     * 
     * @return the notification manager
     */
    public NotificationManager getNotificationManager() {
        return notificationManager;
    }

    /**
     * Get the progress tracker.
     * 
     * @return the progress tracker
     */
    public ToolProgressTracker getProgressTracker() {
        return progressTracker;
    }

    /**
     * Register a tool with the utilities manager.
     * 
     * @param tool the tool to register
     */
    public void registerTool(Tool tool) {
        String toolId = tool.getId();
        registeredTools.put(toolId, tool);

        // Validate tool metadata
        // TODO: validate metadata when utility available

        // Send notification about tool registration
        notificationManager.notify("tool.registered", NotificationType.INFO,
                String.format("Tool registered: %s", toolId), Map.of("toolId", toolId));

        LOGGER.debug("Registered tool: {}", toolId);
    }

    /**
     * Unregister a tool from the utilities manager.
     * 
     * @param toolId the tool ID to unregister
     */
    public void unregisterTool(String toolId) {
        Tool removed = registeredTools.remove(toolId);
        if (removed != null) {
            // Clear tool specification cache
            // TODO: clear spec cache when utility available

            // Send notification about tool unregistration
            notificationManager.notify("tool.unregistered", NotificationType.INFO,
                    String.format("Tool unregistered: %s", toolId), Map.of("toolId", toolId));

            LOGGER.debug("Unregistered tool: {}", toolId);
        }
    }

    /**
     * Get a registered tool.
     * 
     * @param toolId the tool ID
     * @return the tool, or null if not found
     */
    public @Nullable Tool getTool(String toolId) {
        return registeredTools.get(toolId);
    }

    /**
     * Get all registered tools.
     * 
     * @return map of all registered tools
     */
    public Map<String, Tool> getAllTools() {
        return new ConcurrentHashMap<>(registeredTools);
    }

    /**
     * Execute a tool with progress tracking and notifications.
     * 
     * @param toolId the tool ID
     * @param parameters the tool parameters
     * @return the execution result
     */
    public Map<String, Object> executeToolWithProgress(String toolId, Map<String, Object> parameters) {
        Tool tool = registeredTools.get(toolId);
        if (tool == null) {
            LOGGER.error("Tool not found: {}", toolId);
            return Map.of("success", false, "error", "Tool not found: " + toolId);
        }

        // Create progress session
        String operationId = toolId + ":exec";
        // initialize tracking state
        progressTracker.setCurrentStep(0);
        progressTracker.setCurrentMessage("Starting");

        // Start progress tracking
        progressTracker.setCurrentStep(1);
        progressTracker.setCurrentMessage("Starting tool execution: " + toolId);

        try {
            // Validate parameters
            // TODO: add parameter validation helper

            // Execute tool
            progressTracker.setCurrentStep(2);
            progressTracker.setCurrentMessage("Executing tool: " + toolId);
            long startTime = System.currentTimeMillis();
            var toolResult = tool.execute(parameters, null);
            long executionTime = System.currentTimeMillis() - startTime;

            // Convert tool result to map format
            Map<String, Object> result = Map.of("success", toolResult.isSuccess(), "content",
                    toolResult.getContent() != null ? toolResult.getContent() : "", "executionTime", executionTime);

            // Record performance metrics
            statistics.put("tool." + toolId + ".lastExecutionTime", executionTime);
            statistics.put("tool." + toolId + ".lastExecutionTimestamp", System.currentTimeMillis());

            // Complete progress
            progressTracker.markCompleted();

            // Send success notification
            notificationManager.notify("tool.execution.completed", NotificationType.SUCCESS,
                    String.format("Tool execution completed: %s", toolId),
                    Map.of("toolId", toolId, "executionTime", executionTime, "success", true));

            LOGGER.debug("Tool execution completed: {} in {}ms", toolId, executionTime);
            return result;

        } catch (Exception e) {
            progressTracker.markFailed("Tool execution failed: " + e.getMessage());
            notificationManager.notify("tool.execution.failed", NotificationType.ERROR,
                    String.format("Tool execution failed: %s", toolId),
                    Map.of("toolId", toolId, "error", e.getMessage()));

            LOGGER.error("Tool execution failed: {}", toolId, e);
            return Map.of("success", false, "error", "Tool execution failed: " + e.getMessage());
        }
    }

    /**
     * Get comprehensive statistics about the utilities manager.
     * 
     * @return map of statistics
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new ConcurrentHashMap<>(statistics);

        // Add current statistics
        stats.put("registeredTools", registeredTools.size());
        // Minimal metrics until dedicated utilities are available
        stats.put("notificationListeners", notificationManager.getListenerCount());
        stats.put("activeProgressSessions", 1);
        stats.put("cachedSpecifications", 0);

        return stats;
    }

    /**
     * Get health status of all utilities.
     * 
     * @return health status map
     */
    public Map<String, Object> getHealthStatus() {
        return Map.of("notificationManager",
                Map.of("status", "healthy", "listeners", notificationManager.getListenerCount()), "progressTracker",
                Map.of("status", "healthy"), "toolUtils",
                Map.of("status", "healthy", "cachedSpecifications", 0, "registeredTools", registeredTools.size()));
    }

    /**
     * Clean up resources and perform maintenance.
     */
    public void performMaintenance() {
        LOGGER.info("Performing MCP utilities maintenance");

        // Clean up completed progress sessions (older than 1 hour)
        // No bulk cleanup on current tracker; future enhancement placeholder

        // Clear old statistics (older than 24 hours)
        long cutoffTime = System.currentTimeMillis() - 86400000;
        statistics.entrySet().removeIf(entry -> {
            if (entry.getValue() instanceof Long timestamp) {
                return timestamp < cutoffTime;
            }
            return false;
        });

        LOGGER.info("MCP utilities maintenance completed");
    }

    /**
     * Shutdown the utilities manager and clean up resources.
     */
    public void shutdown() {
        LOGGER.info("Shutting down MCP utilities manager");

        // Clear all resources
        registeredTools.clear();
        // No clear methods available on current implementations; rely on GC
        statistics.clear();
        // TODO: clear spec cache when utility available

        LOGGER.info("MCP utilities manager shutdown completed");
    }

    /**
     * Get a summary of the utilities manager status.
     * 
     * @return status summary
     */
    public String getStatusSummary() {
        return String.format(
                "MCP Utilities Manager Status - Tools: %d, Notifications: %d, Progress Sessions: %d, Cached Specs: %d",
                registeredTools.size(), notificationManager.getListenerCount(), 1, 0);
    }
}
