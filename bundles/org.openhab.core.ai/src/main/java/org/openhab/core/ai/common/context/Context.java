package org.openhab.core.ai.common.context;

import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Base interface for all context types in the openHAB AI system.
 * 
 * <p>
 * This interface provides a unified foundation for all context objects:
 * - Common context identification and metadata
 * - Type-safe context retrieval
 * - Immutable context access patterns
 * - Context lifecycle management
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface Context {

    /**
     * Get the unique context identifier.
     * 
     * @return the context ID
     */
    String getContextId();

    /**
     * Get the context type/category.
     * 
     * @return the context type
     */
    String getContextType();

    /**
     * Get when this context was created.
     * 
     * @return the creation timestamp
     */
    Instant getCreatedAt();

    /**
     * Get when this context was last modified.
     * 
     * @return the last modification timestamp
     */
    Instant getLastModifiedAt();

    /**
     * Get the context version.
     * 
     * @return the context version
     */
    String getVersion();

    /**
     * Get a specific context value by key.
     * 
     * @param key the context key
     * @return the context value, or null if not found
     */
    @Nullable
    Object getValue(String key);

    /**
     * Get a typed context value by key.
     * 
     * @param <T> the expected type
     * @param key the context key
     * @param type the expected type class
     * @return the typed context value, or null if not found or wrong type
     */
    <T> @Nullable T getValue(String key, Class<T> type);

    /**
     * Check if the context contains a specific key.
     * 
     * @param key the key to check
     * @return true if the key exists
     */
    boolean hasValue(String key);

    /**
     * Get all context values as an immutable map.
     * 
     * @return immutable map of all context values
     */
    Map<String, Object> getAllValues();

    /**
     * Get context metadata as an immutable map.
     * 
     * @return immutable map of context metadata
     */
    Map<String, Object> getMetadata();

    /**
     * Get a specific metadata value by key.
     * 
     * @param key the metadata key
     * @return the metadata value, or null if not found
     */
    @Nullable
    Object getMetadata(String key);

    /**
     * Check if the context contains specific metadata.
     * 
     * @param key the metadata key to check
     * @return true if the metadata key exists
     */
    boolean hasMetadata(String key);

    /**
     * Get the context size (number of values).
     * 
     * @return the number of context values
     */
    int size();

    /**
     * Check if the context is empty.
     * 
     * @return true if the context has no values
     */
    boolean isEmpty();
}
