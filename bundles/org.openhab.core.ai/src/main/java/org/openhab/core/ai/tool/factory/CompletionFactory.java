package org.openhab.core.ai.tool.factory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.completions.BaseCompletion;
import org.openhab.core.ai.tool.factory.api.CompletionFactoryMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for managing MCP Completion objects with caching and lifecycle management.
 *
 * This factory provides centralized creation, caching, and lifecycle management
 * for BaseCompletion objects, ensuring efficient resource usage and proper cleanup.
 *
 * Author: Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public class CompletionFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompletionFactory.class);
    private final Map<String, BaseCompletion> activeCompletions = new ConcurrentHashMap<>();
    private static final long DEFAULT_REFRESH_INTERVAL_MS = 5 * 60 * 1000;

    /**
     * Create a completion with default refresh interval.
     */
    public @Nullable BaseCompletion createCompletion(String completionName, CompletionFactoryMethod factory) {
        return createCompletion(completionName, factory, DEFAULT_REFRESH_INTERVAL_MS);
    }

    /**
     * Create a completion with custom refresh interval.
     */
    public @Nullable BaseCompletion createCompletion(String completionName, CompletionFactoryMethod factory,
            long refreshIntervalMs) {
        BaseCompletion existing = activeCompletions.get(completionName);
        if (existing != null && existing.isValid() && !existing.needsRefresh()) {
            LOGGER.debug("Using existing valid completion: {}", completionName);
            return existing;
        }

        try {
            BaseCompletion completion = factory.create(completionName, refreshIntervalMs);
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
     */
    public @Nullable BaseCompletion getCompletion(String completionName) {
        return activeCompletions.get(completionName);
    }

    /**
     * Check if a valid completion exists.
     */
    public boolean hasValidCompletion(String completionName) {
        BaseCompletion completion = activeCompletions.get(completionName);
        return completion != null && completion.isValid() && !completion.needsRefresh();
    }

    /**
     * Refresh a specific completion.
     */
    public boolean refreshCompletion(String completionName) {
        BaseCompletion completion = activeCompletions.get(completionName);
        if (completion != null) {
            completion.refresh();
            LOGGER.debug("Refreshed completion: {}", completionName);
            return true;
        }
        return false;
    }

    /**
     * Close and remove a completion.
     */
    public boolean removeCompletion(String completionName) {
        BaseCompletion completion = activeCompletions.remove(completionName);
        if (completion != null) {
            try {
                completion.close();
            } catch (Exception e) {
                LOGGER.warn("Error closing completion: {}", completionName, e);
            }
            LOGGER.debug("Removed completion: {}", completionName);
            return true;
        }
        return false;
    }

    /**
     * Close all completions.
     */
    public void closeAll() {
        activeCompletions.forEach((name, completion) -> {
            try {
                completion.close();
            } catch (Exception e) {
                LOGGER.warn("Error closing completion: {}", name, e);
            }
        });
        activeCompletions.clear();
    }
}
