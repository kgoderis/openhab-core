package org.openhab.core.ai.tool.factory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.dto.AbstractCompletion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for managing MCP Completion objects with caching and lifecycle management.
 * 
 * This factory provides centralized creation, caching, and lifecycle management
 * for AbstractCompletion objects, ensuring efficient resource usage and proper cleanup.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class CompletionFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompletionFactory.class);
    private final Map<String, AbstractCompletion> activeCompletions = new ConcurrentHashMap<>();
    private static final long DEFAULT_REFRESH_INTERVAL_MS = 5 * 60 * 1000;

    /**
     * Create a completion with default refresh interval.
     *
     * @param completionName the name of the completion
     * @param factory the factory method to create the completion
     * @return the created completion or existing valid completion
     */
    public @Nullable AbstractCompletion createCompletion(String completionName, CompletionFactoryMethod factory) {
        return createCompletion(completionName, factory, DEFAULT_REFRESH_INTERVAL_MS);
    }

    /**
     * Create a completion with custom refresh interval.
     *
     * @param completionName the name of the completion
     * @param factory the factory method to create the completion
     * @param refreshIntervalMs the refresh interval in milliseconds
     * @return the created completion or existing valid completion
     */
    public @Nullable AbstractCompletion createCompletion(String completionName, CompletionFactoryMethod factory,
            long refreshIntervalMs) {
        // Check if we already have a valid completion
        AbstractCompletion existing = activeCompletions.get(completionName);
        if (existing != null && existing.isValid() && !existing.needsRefresh()) {
            LOGGER.debug("Using existing valid completion: {}", completionName);
            return existing;
        }

        // Create new completion
        try {
            AbstractCompletion completion = factory.create(completionName, refreshIntervalMs);
            if (completion != null) {
                activeCompletions.put(completionName, completion);
                LOGGER.debug("Created new completion: {}", completionName);
                return completion;
            }
        } catch (Exception e) {
            LOGGER.error("Error creating completion: {}", completionName, e);
        }

        return null;
    }

    /**
     * Get an existing completion by name.
     *
     * @param completionName the name of the completion
     * @return the completion or null if not found
     */
    public @Nullable AbstractCompletion getCompletion(String completionName) {
        return activeCompletions.get(completionName);
    }

    /**
     * Check if a valid completion exists.
     *
     * @param completionName the name of the completion
     * @return true if a valid completion exists
     */
    public boolean hasValidCompletion(String completionName) {
        AbstractCompletion completion = activeCompletions.get(completionName);
        return completion != null && completion.isValid() && !completion.needsRefresh();
    }

    /**
     * Refresh a specific completion.
     *
     * @param completionName the name of the completion to refresh
     * @return true if refresh was successful
     */
    public boolean refreshCompletion(String completionName) {
        AbstractCompletion completion = activeCompletions.get(completionName);
        if (completion != null) {
            try {
                completion.refresh();
                LOGGER.debug("Refreshed completion: {}", completionName);
                return true;
            } catch (Exception e) {
                LOGGER.error("Error refreshing completion: {}", completionName, e);
                completion.markInvalid();
            }
        }
        return false;
    }

    /**
     * Refresh all active completions.
     *
     * @return the number of completions successfully refreshed
     */
    public int refreshAllCompletions() {
        int refreshedCount = 0;

        for (Map.Entry<String, AbstractCompletion> entry : activeCompletions.entrySet()) {
            String completionName = entry.getKey();
            AbstractCompletion completion = entry.getValue();

            try {
                completion.refresh();
                refreshedCount++;
                LOGGER.debug("Refreshed completion: {}", completionName);
            } catch (Exception e) {
                LOGGER.error("Error refreshing completion: {}", completionName, e);
                completion.markInvalid();
            }
        }

        return refreshedCount;
    }

    /**
     * Remove a completion from the factory.
     *
     * @param completionName the name of the completion to remove
     * @return true if the completion was removed
     */
    public boolean removeCompletion(String completionName) {
        AbstractCompletion completion = activeCompletions.remove(completionName);
        if (completion != null) {
            try {
                completion.close();
                LOGGER.debug("Removed completion: {}", completionName);
                return true;
            } catch (Exception e) {
                LOGGER.error("Error closing completion: {}", completionName, e);
            }
        }
        return false;
    }

    /**
     * Clean up all completions and clear the factory.
     */
    public void cleanup() {
        LOGGER.debug("Cleaning up CompletionFactory with {} active completions", activeCompletions.size());

        for (Map.Entry<String, AbstractCompletion> entry : activeCompletions.entrySet()) {
            String completionName = entry.getKey();
            AbstractCompletion completion = entry.getValue();

            try {
                completion.close();
                LOGGER.debug("Closed completion: {}", completionName);
            } catch (Exception e) {
                LOGGER.error("Error closing completion: {}", completionName, e);
            }
        }

        activeCompletions.clear();
        LOGGER.debug("CompletionFactory cleanup completed");
    }

    /**
     * Get the number of active completions.
     *
     * @return the number of active completions
     */
    public int getCompletionCount() {
        return activeCompletions.size();
    }

    /**
     * Get all active completions.
     *
     * @return a copy of all active completions
     */
    public Map<String, AbstractCompletion> getAllCompletions() {
        return new ConcurrentHashMap<>(activeCompletions);
    }

    /**
     * Functional interface for creating completions.
     */
    @FunctionalInterface
    public interface CompletionFactoryMethod {
        @Nullable
        AbstractCompletion create(String completionName, long refreshIntervalMs);
    }
}
