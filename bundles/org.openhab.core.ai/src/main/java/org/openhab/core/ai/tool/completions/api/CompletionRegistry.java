package org.openhab.core.ai.tool.completions.api;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.completions.api.dto.Completion;

import io.modelcontextprotocol.server.McpServerFeatures;

/**
 * Registry for MCP Completions.
 *
 * This interface manages the registration and discovery of MCP completions,
 * providing access to completion specifications for the MCP server.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface CompletionRegistry {

    /**
     * Register a completion.
     *
     * @param completion the completion to register
     */
    void registerCompletion(Completion completion);

    /**
     * Unregister a completion.
     *
     * @param promptReference the prompt reference to unregister
     */
    void unregisterCompletion(String promptReference);

    /**
     * Get a completion by prompt reference.
     *
     * @param promptReference the prompt reference
     * @return the completion or null if not found
     */
    @Nullable
    Completion getCompletion(String promptReference);

    /**
     * Get all completions.
     *
     * @return all registered completions
     */
    Map<String, Completion> getAllCompletions();

    /**
     * Get lightweight completion descriptors suitable for list responses.
     * Each map should contain at least: promptReference, description, suggestions, total, hasMore.
     *
     * @return array of serializable completion descriptors
     */
    Map<String, Object>[] getCompletionDescriptors();

    /**
     * Get the number of registered completions.
     *
     * @return the number of completions
     */
    int getCompletionCount();

    /**
     * Check if a completion is registered.
     *
     * @param promptReference the prompt reference
     * @return true if the completion is registered
     */
    boolean isCompletionRegistered(String promptReference);

    /**
     * Get sync completion specifications.
     *
     * @return sync completion specifications
     */
    McpServerFeatures.SyncCompletionSpecification[] getSyncCompletionSpecifications();

    /**
     * Get async completion specifications.
     *
     * @return async completion specifications
     */
    McpServerFeatures.AsyncCompletionSpecification[] getAsyncCompletionSpecifications();
}
