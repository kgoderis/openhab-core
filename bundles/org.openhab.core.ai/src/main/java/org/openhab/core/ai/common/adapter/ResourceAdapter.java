package org.openhab.core.ai.common.adapter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Base adapter for resource-related operations.
 * 
 * This adapter provides common functionality for resource adapters including
 * content management, writability checks, and resource operations.
 * 
 * @param <T> The resource type
 * @param <C> The resource context type
 * @param <R> The resource result type
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public abstract class ResourceAdapter<T, C, R> extends BaseAdapter<T, C, R> {

    private final Map<String, Object> resourceCache = new ConcurrentHashMap<>();

    /**
     * Create a new resource adapter.
     * 
     * @param refreshIntervalMs Refresh interval in milliseconds
     */
    protected ResourceAdapter(long refreshIntervalMs) {
        super(refreshIntervalMs);
    }

    /**
     * Create a resource entity from the given identifier and context.
     * 
     * @param identifier the resource identifier
     * @param context the resource context
     * @return the created resource entity, or null if creation fails
     */
    public abstract @Nullable T createEntity(String identifier, C context);

    /**
     * Get the content of a resource.
     * 
     * @param identifier the resource identifier
     * @param context the resource context
     * @return the resource content, or null if not available
     */
    public abstract @Nullable String getContent(String identifier, C context);

    /**
     * Check if a resource is writable.
     * 
     * @param identifier the resource identifier
     * @param context the resource context
     * @return true if the resource is writable
     */
    public abstract boolean isWritable(String identifier, C context);

    /**
     * Write content to a resource.
     * 
     * @param identifier the resource identifier
     * @param content the content to write
     * @param context the resource context
     * @return true if the write operation was successful
     */
    public abstract boolean writeContent(String identifier, @Nullable String content, C context);

    /**
     * Check if a resource exists.
     * 
     * @param identifier the resource identifier
     * @param context the resource context
     * @return true if the resource exists
     */
    public abstract boolean exists(String identifier, C context);

    /**
     * Execute an operation on a resource.
     * 
     * @param identifier the resource identifier
     * @param operation the operation to execute
     * @param parameters the operation parameters
     * @param context the resource context
     * @return the operation result
     */
    public abstract R execute(String identifier, String operation, Map<String, Object> parameters, C context);

    /**
     * Get a cached value for the given key.
     * 
     * @param key the cache key
     * @return the cached value, or null if not found
     */
    protected @Nullable Object getCachedValue(String key) {
        return resourceCache.get(key);
    }

    /**
     * Set a cached value for the given key.
     * 
     * @param key the cache key
     * @param value the value to cache
     */
    protected void setCachedValue(String key, Object value) {
        resourceCache.put(key, value);
    }

    /**
     * Remove a cached value for the given key.
     * 
     * @param key the cache key
     */
    protected void removeCachedValue(String key) {
        resourceCache.remove(key);
    }

    /**
     * Clear all cached values.
     */
    protected void clearCache() {
        resourceCache.clear();
    }

    @Override
    protected void doCleanup() {
        clearCache();
    }
}
