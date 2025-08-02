package org.openhab.core.ai.common.api.action;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.action.AIActionContext;
import org.openhab.core.ai.common.action.AIActionMetadata;
import org.openhab.core.ai.common.action.AIActionResult;
import org.openhab.core.ai.common.action.AIActionValidationResult;

/**
 * Unified interface for AI actions that can be executed by both MCP and A2A protocols.
 * This serves as the common foundation for all AI capabilities in openHAB.
 * 
 * 
 */
@NonNullByDefault
public interface AIAction {

    // ===== CORE IDENTIFICATION =====

    /**
     * Get the unique identifier for this action.
     * Format: "protocol.category.action" (e.g., "openhab.items.list", "openhab.security.arm")
     * 
     * @return the unique action identifier
     */
    String getActionId();

    /**
     * Get the human-readable name for this action.
     * 
     * @return the action name
     */
    String getActionName();

    /**
     * Get a detailed description of what this action does.
     * 
     * @return the action description
     */
    String getDescription();

    /**
     * Get the action category (e.g., "items", "things", "security", "automation").
     * 
     * @return the action category
     */
    String getCategory();

    /**
     * Get the action version.
     * 
     * @return the action version
     */
    String getVersion();

    // ===== SCHEMA AND VALIDATION =====

    /**
     * Get the JSON schema for action parameters.
     * 
     * @return the parameter schema as a Map
     */
    Map<String, Object> getParameterSchema();

    /**
     * Validate action parameters before execution.
     * 
     * @param parameters the parameters to validate
     * @return validation result
     */
    AIActionValidationResult validateParameters(Map<String, Object> parameters);

    /**
     * Get the return schema for action results.
     * 
     * @return the return schema as a Map
     */
    Map<String, Object> getReturnSchema();

    // ===== EXECUTION =====

    /**
     * Execute the action synchronously.
     * This is the primary execution method for MCP tools.
     * 
     * @param parameters the action parameters
     * @param context the execution context
     * @return the action result
     * @throws AIActionException if execution fails
     */
    AIActionResult execute(Map<String, Object> parameters, AIActionContext context) throws AIActionException;

    /**
     * Execute the action asynchronously.
     * This is the primary execution method for A2A skills.
     * 
     * @param parameters the action parameters
     * @param context the execution context
     * @return a CompletableFuture with the action result
     */
    CompletableFuture<AIActionResult> executeAsync(Map<String, Object> parameters, AIActionContext context);

    // ===== METADATA =====

    /**
     * Get comprehensive metadata about this action.
     * 
     * @return the action metadata
     */
    AIActionMetadata getMetadata();

    /**
     * Get action capabilities and features.
     * 
     * @return the action capabilities
     */
    Map<String, Object> getCapabilities();

    // ===== LIFECYCLE =====

    /**
     * Initialize the action with context.
     * Called when the action is first created or when context changes.
     * 
     * @param context the initialization context
     */
    void initialize(AIActionContext context);

    /**
     * Clean up resources when the action is no longer needed.
     */
    void cleanup();

    /**
     * Check if the action is ready for execution.
     * 
     * @return true if the action is ready, false otherwise
     */
    boolean isReady();
}
