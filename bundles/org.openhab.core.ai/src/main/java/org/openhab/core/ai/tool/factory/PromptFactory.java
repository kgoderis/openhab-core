package org.openhab.core.ai.tool.factory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.factory.api.PromptFactoryMethod;
import org.openhab.core.ai.tool.prompts.BasePrompt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for managing MCP Prompt objects with caching and lifecycle management.
 *
 * This factory provides centralized creation, caching, and lifecycle management
 * for BasePrompt objects, ensuring efficient resource usage and proper cleanup.
 *
 * Author: Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public class PromptFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(PromptFactory.class);
    private final Map<String, BasePrompt> activePrompts = new ConcurrentHashMap<>();
    private static final long DEFAULT_REFRESH_INTERVAL_MS = 5 * 60 * 1000;

    /**
     * Create a prompt with default refresh interval.
     */
    public @Nullable BasePrompt createPrompt(String promptName, PromptFactoryMethod factory) {
        return createPrompt(promptName, factory, DEFAULT_REFRESH_INTERVAL_MS);
    }

    /**
     * Create a prompt with custom refresh interval.
     */
    public @Nullable BasePrompt createPrompt(String promptName, PromptFactoryMethod factory, long refreshIntervalMs) {
        BasePrompt existing = activePrompts.get(promptName);
        if (existing != null && existing.isValid() && !existing.needsRefresh()) {
            LOGGER.debug("Using existing valid prompt: {}", promptName);
            return existing;
        }

        try {
            BasePrompt prompt = factory.create(promptName, refreshIntervalMs);
            if (prompt != null) {
                activePrompts.put(promptName, prompt);
                LOGGER.debug("Created new prompt: {}", promptName);
                return prompt;
            }
        } catch (Exception e) {
            LOGGER.error("Error creating prompt: {}", promptName, e);
        }
        return null;
    }

    /**
     * Get an existing prompt by name.
     */
    public @Nullable BasePrompt getPrompt(String promptName) {
        return activePrompts.get(promptName);
    }

    /**
     * Check if a valid prompt exists.
     */
    public boolean hasValidPrompt(String promptName) {
        BasePrompt prompt = activePrompts.get(promptName);
        return prompt != null && prompt.isValid() && !prompt.needsRefresh();
    }

    /**
     * Refresh a specific prompt.
     */
    public boolean refreshPrompt(String promptName) {
        BasePrompt prompt = activePrompts.get(promptName);
        if (prompt != null) {
            prompt.refresh();
            LOGGER.debug("Refreshed prompt: {}", promptName);
            return true;
        }
        return false;
    }

    /**
     * Close and remove a prompt.
     */
    public boolean removePrompt(String promptName) {
        BasePrompt prompt = activePrompts.remove(promptName);
        if (prompt != null) {
            try {
                prompt.close();
            } catch (Exception e) {
                LOGGER.warn("Error closing prompt: {}", promptName, e);
            }
            LOGGER.debug("Removed prompt: {}", promptName);
            return true;
        }
        return false;
    }

    /**
     * Close all prompts.
     */
    public void closeAll() {
        activePrompts.forEach((name, prompt) -> {
            try {
                prompt.close();
            } catch (Exception e) {
                LOGGER.warn("Error closing prompt: {}", name, e);
            }
        });
        activePrompts.clear();
    }
}
