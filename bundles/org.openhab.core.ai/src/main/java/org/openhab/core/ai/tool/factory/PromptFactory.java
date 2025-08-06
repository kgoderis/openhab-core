package org.openhab.core.ai.tool.factory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.dto.AbstractPrompt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for managing MCP Prompt objects with caching and lifecycle management.
 * 
 * This factory provides centralized creation, caching, and lifecycle management
 * for AbstractPrompt objects, ensuring efficient resource usage and proper cleanup.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class PromptFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(PromptFactory.class);
    private final Map<String, AbstractPrompt> activePrompts = new ConcurrentHashMap<>();
    private static final long DEFAULT_REFRESH_INTERVAL_MS = 5 * 60 * 1000; // 5 minutes

    /**
     * Create a prompt with default refresh interval.
     *
     * @param promptName the name of the prompt
     * @param factory the factory method to create the prompt
     * @return the created prompt or existing valid prompt
     */
    public @Nullable AbstractPrompt createPrompt(String promptName, PromptFactoryMethod factory) {
        return createPrompt(promptName, factory, DEFAULT_REFRESH_INTERVAL_MS);
    }

    /**
     * Create a prompt with custom refresh interval.
     *
     * @param promptName the name of the prompt
     * @param factory the factory method to create the prompt
     * @param refreshIntervalMs the refresh interval in milliseconds
     * @return the created prompt or existing valid prompt
     */
    public @Nullable AbstractPrompt createPrompt(String promptName, PromptFactoryMethod factory,
            long refreshIntervalMs) {
        // Check if we already have a valid prompt
        AbstractPrompt existing = activePrompts.get(promptName);
        if (existing != null && existing.isValid() && !existing.needsRefresh()) {
            LOGGER.debug("Using existing valid prompt: {}", promptName);
            return existing;
        }

        // Create new prompt
        try {
            AbstractPrompt newPrompt = factory.create(promptName, refreshIntervalMs);
            if (newPrompt != null) {
                activePrompts.put(promptName, newPrompt);
                LOGGER.debug("Created new prompt: {}", promptName);
                return newPrompt;
            }
        } catch (Exception e) {
            LOGGER.error("Error creating prompt: {}", promptName, e);
        }

        return null;
    }

    /**
     * Get an existing prompt by name.
     *
     * @param promptName the name of the prompt
     * @return the prompt or null if not found
     */
    public @Nullable AbstractPrompt getPrompt(String promptName) {
        return activePrompts.get(promptName);
    }

    /**
     * Check if a valid prompt exists.
     *
     * @param promptName the name of the prompt
     * @return true if a valid prompt exists
     */
    public boolean hasValidPrompt(String promptName) {
        AbstractPrompt prompt = activePrompts.get(promptName);
        return prompt != null && prompt.isValid() && !prompt.needsRefresh();
    }

    /**
     * Refresh a specific prompt.
     *
     * @param promptName the name of the prompt to refresh
     * @return true if the prompt was refreshed successfully
     */
    public boolean refreshPrompt(String promptName) {
        AbstractPrompt prompt = activePrompts.get(promptName);
        if (prompt != null) {
            try {
                prompt.refresh();
                LOGGER.debug("Refreshed prompt: {}", promptName);
                return true;
            } catch (Exception e) {
                LOGGER.error("Error refreshing prompt: {}", promptName, e);
                prompt.markInvalid();
            }
        }
        return false;
    }

    /**
     * Refresh all prompts that need refreshing.
     *
     * @return the number of prompts that were refreshed
     */
    public int refreshAllPrompts() {
        int refreshedCount = 0;

        for (Map.Entry<String, AbstractPrompt> entry : activePrompts.entrySet()) {
            String promptName = entry.getKey();
            AbstractPrompt prompt = entry.getValue();

            if (prompt.needsRefresh()) {
                if (refreshPrompt(promptName)) {
                    refreshedCount++;
                }
            }
        }

        LOGGER.debug("Refreshed {} prompts", refreshedCount);
        return refreshedCount;
    }

    /**
     * Remove a prompt from the factory.
     *
     * @param promptName the name of the prompt to remove
     * @return true if the prompt was removed
     */
    public boolean removePrompt(String promptName) {
        AbstractPrompt prompt = activePrompts.remove(promptName);
        if (prompt != null) {
            try {
                prompt.close();
                LOGGER.debug("Removed prompt: {}", promptName);
                return true;
            } catch (Exception e) {
                LOGGER.error("Error closing prompt: {}", promptName, e);
            }
        }
        return false;
    }

    /**
     * Clean up all prompts managed by this factory.
     */
    public void cleanup() {
        LOGGER.info("Cleaning up {} prompts", activePrompts.size());

        for (Map.Entry<String, AbstractPrompt> entry : activePrompts.entrySet()) {
            String promptName = entry.getKey();
            AbstractPrompt prompt = entry.getValue();

            try {
                prompt.close();
                LOGGER.debug("Closed prompt: {}", promptName);
            } catch (Exception e) {
                LOGGER.error("Error closing prompt: {}", promptName, e);
            }
        }

        activePrompts.clear();
        LOGGER.info("Prompt cleanup completed");
    }

    /**
     * Get the number of active prompts.
     *
     * @return the number of active prompts
     */
    public int getPromptCount() {
        return activePrompts.size();
    }

    /**
     * Get all active prompts.
     *
     * @return a copy of the active prompts map
     */
    public Map<String, AbstractPrompt> getAllPrompts() {
        return new ConcurrentHashMap<>(activePrompts);
    }

    /**
     * Functional interface for creating prompts.
     */
    @FunctionalInterface
    public interface PromptFactoryMethod {
        /**
         * Create a prompt.
         *
         * @param promptName the name of the prompt
         * @param refreshIntervalMs the refresh interval in milliseconds
         * @return the created prompt or null if creation failed
         */
        @Nullable
        AbstractPrompt create(String promptName, long refreshIntervalMs);
    }
}
