package org.openhab.core.ai.common.adapter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Base adapter for completion-related operations.
 * 
 * This adapter provides common functionality for completion adapters including
 * suggestion generation, content management, and completion operations.
 * 
 * @param <T> The completion type
 * @param <C> The completion context type
 * @param <R> The completion result type
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public abstract class CompletionAdapter<T, C, R> extends BaseAdapter<T, C, R> {

    private final Map<String, List<String>> completionCache = new ConcurrentHashMap<>();

    /**
     * Create a new completion adapter.
     * 
     * @param refreshIntervalMs Refresh interval in milliseconds
     */
    protected CompletionAdapter(long refreshIntervalMs) {
        super(refreshIntervalMs);
    }

    /**
     * Create a completion entity from the given identifier and context.
     * 
     * @param identifier the completion identifier
     * @param context the completion context
     * @return the created completion entity, or null if creation fails
     */
    public abstract @Nullable T createEntity(String identifier, C context);

    /**
     * Get the content of a completion.
     * 
     * @param identifier the completion identifier
     * @param context the completion context
     * @return the completion content, or null if not available
     */
    public abstract @Nullable String getContent(String identifier, C context);

    /**
     * Check if a completion is writable.
     * 
     * @param identifier the completion identifier
     * @param context the completion context
     * @return true if the completion is writable
     */
    public abstract boolean isWritable(String identifier, C context);

    /**
     * Write content to a completion.
     * 
     * @param identifier the completion identifier
     * @param content the content to write
     * @param context the completion context
     * @return true if the write operation was successful
     */
    public abstract boolean writeContent(String identifier, @Nullable String content, C context);

    /**
     * Check if a completion exists.
     * 
     * @param identifier the completion identifier
     * @param context the completion context
     * @return true if the completion exists
     */
    public abstract boolean exists(String identifier, C context);

    /**
     * Execute an operation on a completion.
     * 
     * @param identifier the completion identifier
     * @param operation the operation to execute
     * @param parameters the operation parameters
     * @param context the completion context
     * @return the operation result
     */
    public abstract R execute(String identifier, String operation, Map<String, Object> parameters, C context);

    /**
     * Get suggestions for the given identifier and context.
     * 
     * @param identifier the completion identifier
     * @param context the completion context
     * @return the list of suggestions
     */
    public abstract List<String> getSuggestions(String identifier, C context);

    /**
     * Get a cached completion list for the given key.
     * 
     * @param key the cache key
     * @return the cached completion list, or null if not found
     */
    protected @Nullable List<String> getCachedCompletions(String key) {
        return completionCache.get(key);
    }

    /**
     * Set a cached completion list for the given key.
     * 
     * @param key the cache key
     * @param completions the completion list to cache
     */
    protected void setCachedCompletions(String key, List<String> completions) {
        completionCache.put(key, completions);
    }

    /**
     * Remove a cached completion list for the given key.
     * 
     * @param key the cache key
     */
    protected void removeCachedCompletions(String key) {
        completionCache.remove(key);
    }

    /**
     * Clear all cached completions.
     */
    protected void clearCompletionCache() {
        completionCache.clear();
    }

    @Override
    protected void doCleanup() {
        clearCompletionCache();
    }
}
