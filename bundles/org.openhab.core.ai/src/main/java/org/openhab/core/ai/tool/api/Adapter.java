package org.openhab.core.ai.tool.api;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Generic Adapter interface for MCP protocol entities.
 * 
 * This interface provides a unified way to adapt between MCP protocol entities
 * (Resource, Prompt, Completion) and their underlying OpenHAB implementations.
 * 
 * @param <T> The MCP data model type (Resource, Prompt, Completion)
 * @param <C> The Context type for execution context
 * @param <R> The Result type for operation results
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface Adapter<T, C, R> {

    /**
     * Create an MCP entity from OpenHAB data.
     * 
     * @param identifier The identifier (item name, rule UID, etc.)
     * @param context The execution context
     * @return The MCP entity or null if creation fails
     */
    @Nullable
    T createEntity(String identifier, C context);

    /**
     * Get content from the underlying OpenHAB entity.
     * 
     * @param identifier The identifier (item name, rule UID, etc.)
     * @param context The execution context
     * @return The content as a string or null if not available
     */
    @Nullable
    String getContent(String identifier, C context);

    /**
     * Check if the entity is writable.
     * 
     * @param identifier The identifier (item name, rule UID, etc.)
     * @param context The execution context
     * @return true if the entity is writable
     */
    boolean isWritable(String identifier, C context);

    /**
     * Write content to the underlying OpenHAB entity.
     * 
     * @param identifier The identifier (item name, rule UID, etc.)
     * @param content The content to write
     * @param context The execution context
     * @return true if successful
     */
    boolean writeContent(String identifier, @Nullable String content, C context);

    /**
     * Check if the entity exists.
     * 
     * @param identifier The identifier (item name, rule UID, etc.)
     * @param context The execution context
     * @return true if the entity exists
     */
    boolean exists(String identifier, C context);

    /**
     * Execute an operation on the entity.
     * 
     * @param identifier The identifier (item name, rule UID, etc.)
     * @param operation The operation to perform
     * @param parameters The operation parameters
     * @param context The execution context
     * @return The operation result
     */
    R execute(String identifier, String operation, Map<String, Object> parameters, C context);

    /**
     * Refresh the cached data for the entity.
     * 
     * @param identifier The identifier (item name, rule UID, etc.)
     * @param context The execution context
     */
    void refresh(String identifier, C context);

    /**
     * Clean up resources managed by this adapter.
     */
    void cleanup();

    /**
     * Get the adapter type identifier.
     * 
     * @return The adapter type (e.g., "items", "rules", "things")
     */
    String getAdapterType();

    /**
     * Get the URI pattern for this adapter.
     * 
     * @return The URI pattern (e.g., "openhab://items/{itemName}")
     */
    String getUriPattern();
}
