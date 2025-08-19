package org.openhab.core.ai.common.adapter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Base adapter for prompt-related operations.
 * 
 * This adapter provides common functionality for prompt adapters including
 * prompt generation, content management, and prompt operations.
 * 
 * @param <T> The prompt type
 * @param <C> The prompt context type
 * @param <R> The prompt result type
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public abstract class PromptAdapter<T, C, R> extends BaseAdapter<T, C, R> {

    private final Map<String, String> promptCache = new ConcurrentHashMap<>();

    /**
     * Create a new prompt adapter.
     * 
     * @param refreshIntervalMs Refresh interval in milliseconds
     */
    protected PromptAdapter(long refreshIntervalMs) {
        super(refreshIntervalMs);
    }

    /**
     * Create a prompt entity from the given identifier and context.
     * 
     * @param identifier the prompt identifier
     * @param context the prompt context
     * @return the created prompt entity, or null if creation fails
     */
    public abstract @Nullable T createEntity(String identifier, C context);

    /**
     * Get the content of a prompt.
     * 
     * @param identifier the prompt identifier
     * @param context the prompt context
     * @return the prompt content, or null if not available
     */
    public abstract @Nullable String getContent(String identifier, C context);

    /**
     * Check if a prompt is writable.
     * 
     * @param identifier the prompt identifier
     * @param context the prompt context
     * @return true if the prompt is writable
     */
    public abstract boolean isWritable(String identifier, C context);

    /**
     * Write content to a prompt.
     * 
     * @param identifier the prompt identifier
     * @param content the content to write
     * @param context the prompt context
     * @return true if the write operation was successful
     */
    public abstract boolean writeContent(String identifier, @Nullable String content, C context);

    /**
     * Check if a prompt exists.
     * 
     * @param identifier the prompt identifier
     * @param context the prompt context
     * @return true if the prompt exists
     */
    public abstract boolean exists(String identifier, C context);

    /**
     * Execute an operation on a prompt.
     * 
     * @param identifier the prompt identifier
     * @param operation the operation to execute
     * @param parameters the operation parameters
     * @param context the prompt context
     * @return the operation result
     */
    public abstract R execute(String identifier, String operation, Map<String, Object> parameters, C context);

    /**
     * Generate a prompt for the given identifier and context.
     * 
     * @param identifier the prompt identifier
     * @param context the prompt context
     * @return the generated prompt content
     */
    public abstract String generatePrompt(String identifier, C context);

    /**
     * Get a cached prompt for the given key.
     * 
     * @param key the cache key
     * @return the cached prompt, or null if not found
     */
    protected @Nullable String getCachedPrompt(String key) {
        return promptCache.get(key);
    }

    /**
     * Set a cached prompt for the given key.
     * 
     * @param key the cache key
     * @param prompt the prompt to cache
     */
    protected void setCachedPrompt(String key, String prompt) {
        promptCache.put(key, prompt);
    }

    /**
     * Remove a cached prompt for the given key.
     * 
     * @param key the cache key
     */
    protected void removeCachedPrompt(String key) {
        promptCache.remove(key);
    }

    /**
     * Clear all cached prompts.
     */
    protected void clearPromptCache() {
        promptCache.clear();
    }

    @Override
    protected void doCleanup() {
        clearPromptCache();
    }
}
