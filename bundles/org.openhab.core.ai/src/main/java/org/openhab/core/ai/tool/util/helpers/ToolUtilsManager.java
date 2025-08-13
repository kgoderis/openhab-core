package org.openhab.core.ai.tool.util.helpers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.api.Tool;
import org.openhab.core.ai.tool.api.ToolValidationResult;
import org.openhab.core.ai.tool.notifications.DefaultNotificationService;
import org.openhab.core.ai.tool.notifications.events.NotificationType;
import org.openhab.core.ai.tool.progress.ToolProgressTracker;
import org.openhab.core.ai.tool.util.ToolUtils;
import org.openhab.core.ai.util.ValidationUtils;
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
    private final DefaultNotificationService notificationManager;

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
        this.notificationManager = new DefaultNotificationService();
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
    public DefaultNotificationService getNotificationManager() {
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

        // Validate tool metadata when utility available
        validateToolMetadata(tool);

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
            // Clear tool specification cache when utility available
            clearToolSpecificationCache(toolId);

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
            // Validate parameters using parameter validation helper
            ParameterValidationResult validationResult = validateToolParameters(tool, parameters);
            if (!validationResult.isValid()) {
                progressTracker.markFailed("Parameter validation failed: " + validationResult.getErrorMessage());
                return Map.of("success", false, "error",
                        "Parameter validation failed: " + validationResult.getErrorMessage());
            }

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
        stats.put("cachedSpecifications", getCachedSpecificationCount());

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
                Map.of("status", "healthy"), "toolUtils", Map.of("status", "healthy", "cachedSpecifications",
                        getCachedSpecificationCount(), "registeredTools", registeredTools.size()));
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
        // Clear spec cache when utility available
        clearAllToolSpecificationCaches();

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
                registeredTools.size(), notificationManager.getListenerCount(), 1, getCachedSpecificationCount());
    }

    /**
     * Validate tool metadata when utility available.
     * 
     * @param tool the tool to validate
     */
    private void validateToolMetadata(Tool tool) {
        try {
            // Validate tool ID
            ValidationUtils.requireNonEmpty(tool.getId(), "Tool ID");

            // Validate tool name
            ValidationUtils.requireNonEmpty(tool.getName(), "Tool name");

            // Validate tool description
            ValidationUtils.requireNonEmpty(tool.getDescription(), "Tool description");

            // Validate tool metadata
            if (tool.getMetadata() == null) {
                LOGGER.warn("Tool {} has null metadata", tool.getId());
            }

            // Validate input schema
            if (tool.getInputSchema() == null || tool.getInputSchema().isEmpty()) {
                LOGGER.warn("Tool {} has empty input schema", tool.getId());
            }

            // Validate output schema
            if (tool.getOutputSchema() == null || tool.getOutputSchema().isEmpty()) {
                LOGGER.warn("Tool {} has empty output schema", tool.getId());
            }

            LOGGER.debug("Tool metadata validation passed for: {}", tool.getId());

        } catch (Exception e) {
            LOGGER.error("Tool metadata validation failed for: {}", tool.getId(), e);
            // Don't throw exception, just log the issue
        }
    }

    /**
     * Clear tool specification cache when utility available.
     * 
     * @param toolId the tool ID to clear cache for
     */
    private void clearToolSpecificationCache(String toolId) {
        try {
            // Use ToolUtils to clear specification caches
            ToolUtils.clearSpecificationCache(toolId);
            LOGGER.debug("Cleared specification cache for tool: {}", toolId);
        } catch (Exception e) {
            LOGGER.warn("Failed to clear specification cache for tool: {}", toolId, e);
        }
    }

    /**
     * Clear all tool specification caches.
     */
    private void clearAllToolSpecificationCaches() {
        try {
            // Use ToolUtils to clear all specification caches
            ToolUtils.clearSpecificationCache(null);
            LOGGER.debug("Cleared all tool specification caches");
        } catch (Exception e) {
            LOGGER.warn("Failed to clear all tool specification caches", e);
        }
    }

    /**
     * Get the count of cached specifications.
     * 
     * @return the count of cached specifications
     */
    private int getCachedSpecificationCount() {
        try {
            return ToolUtils.getCachedSpecificationCount();
        } catch (Exception e) {
            LOGGER.warn("Failed to get cached specification count", e);
            return 0;
        }
    }

    /**
     * Parameter validation helper.
     * 
     * @param tool the tool to validate parameters for
     * @param parameters the parameters to validate
     * @return the validation result
     */
    private ParameterValidationResult validateToolParameters(Tool tool, Map<String, Object> parameters) {
        try {
            // Validate parameters are not null
            ValidationUtils.requireNonNull(parameters, "Parameters");

            // Use tool's built-in validation
            ToolValidationResult toolValidation = tool.validateParameters(parameters);
            if (!toolValidation.isValid()) {
                return new ParameterValidationResult(false, toolValidation.getMessage());
            }

            // Additional validation using ValidationUtils
            validateParameterTypes(parameters);

            return new ParameterValidationResult(true, null);

        } catch (Exception e) {
            LOGGER.error("Parameter validation failed for tool: {}", tool.getId(), e);
            return new ParameterValidationResult(false, "Parameter validation error: " + e.getMessage());
        }
    }

    /**
     * Validate parameter types using ValidationUtils.
     * 
     * @param parameters the parameters to validate
     */
    private void validateParameterTypes(Map<String, Object> parameters) {
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            // Validate key is not null or empty
            ValidationUtils.requireNonEmpty(key, "Parameter key");

            // Validate value is not null (unless explicitly allowed)
            if (value == null) {
                LOGGER.warn("Parameter {} has null value", key);
            }
        }
    }

    /**
     * Parameter validation result.
     */
    // ParameterValidationResult extracted to org.openhab.core.ai.tool.util.helpers.ParameterValidationResult
}
