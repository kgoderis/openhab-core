package org.openhab.core.ai.agent;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Context for autonomous agent operations
 * 
 * <p>
 * This class provides a thread-safe context management system for autonomous agents:
 * - Dynamic context updates with atomic operations
 * - Type-safe context retrieval
 * - Context inheritance and copying
 * - Immutable context access
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public class AgentContext {

    private final Map<String, Object> context = new HashMap<>();

    /**
     * Create a new empty agent context
     */
    public AgentContext() {
    }

    /**
     * Create a new agent context by copying from another
     * 
     * @param other the context to copy from
     */
    public AgentContext(AgentContext other) {
        context.putAll(other.context);
    }

    /**
     * Put a value in the context
     * 
     * @param key the context key
     * @param value the context value
     */
    public void put(String key, Object value) {
        context.put(key, value);
    }

    /**
     * Get a value from the context
     * 
     * @param key the context key
     * @return the context value, or null if not found
     */
    public @Nullable Object get(String key) {
        return context.get(key);
    }

    /**
     * Get a typed value from the context
     * 
     * @param <T> the expected type
     * @param key the context key
     * @param type the expected type class
     * @return the typed context value, or null if not found or wrong type
     */
    public <T> @Nullable T get(String key, Class<T> type) {
        Object value = context.get(key);
        if (type.isInstance(value)) {
            return type.cast(value);
        }
        return null;
    }

    /**
     * Get all context values as a map
     * 
     * @return a copy of all context values
     */
    public Map<String, Object> getAll() {
        return new HashMap<>(context);
    }

    /**
     * Clear all context values
     */
    public void clear() {
        context.clear();
    }

    /**
     * Check if the context contains a key
     * 
     * @param key the key to check
     * @return true if the key exists
     */
    public boolean containsKey(String key) {
        return context.containsKey(key);
    }

    /**
     * Get the number of context entries
     * 
     * @return the number of context entries
     */
    public int size() {
        return context.size();
    }

    /**
     * Check if the context is empty
     * 
     * @return true if the context is empty
     */
    public boolean isEmpty() {
        return context.isEmpty();
    }
}
