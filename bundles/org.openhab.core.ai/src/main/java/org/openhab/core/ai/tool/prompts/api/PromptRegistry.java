package org.openhab.core.ai.tool.prompts.api;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.prompts.api.dto.Prompt;

import io.modelcontextprotocol.server.McpServerFeatures;

/**
 * Registry for MCP Prompts.
 *
 * This interface manages the registration and discovery of MCP prompts,
 * providing access to prompt specifications for the MCP server.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface PromptRegistry {

    /**
     * Register a prompt.
     *
     * @param prompt the prompt to register
     */
    void registerPrompt(Prompt prompt);

    /**
     * Unregister a prompt.
     *
     * @param name the prompt name to unregister
     */
    void unregisterPrompt(String name);

    /**
     * Get a prompt by name.
     *
     * @param name the prompt name
     * @return the prompt or null if not found
     */
    @Nullable
    Prompt getPrompt(String name);

    /**
     * Get all prompts.
     *
     * @return all registered prompts
     */
    Map<String, Prompt> getAllPrompts();

    /**
     * Get lightweight prompt descriptors suitable for list responses.
     * Each map should contain at least: name, description, arguments[] (name, description, required).
     *
     * @return array of serializable prompt descriptors
     */
    Map<String, Object>[] getPromptDescriptors();

    /**
     * Get the number of registered prompts.
     *
     * @return the number of prompts
     */
    int getPromptCount();

    /**
     * Check if a prompt is registered.
     *
     * @param name the prompt name
     * @return true if the prompt is registered
     */
    boolean isPromptRegistered(String name);

    /**
     * Get sync prompt specifications.
     *
     * @return sync prompt specifications
     */
    McpServerFeatures.SyncPromptSpecification[] getSyncPromptSpecifications();

    /**
     * Get async prompt specifications.
     *
     * @return async prompt specifications
     */
    McpServerFeatures.AsyncPromptSpecification[] getAsyncPromptSpecifications();
}
