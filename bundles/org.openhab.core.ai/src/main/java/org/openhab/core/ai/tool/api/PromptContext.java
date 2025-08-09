package org.openhab.core.ai.tool.api;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Prompt Context for MCP Prompts
 *
 * This class defines execution context for MCP prompts
 * including request properties and metadata.
 *
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public class PromptContext {

    private final Map<String, Object> properties;

    /**
     * Create a new PromptContext instance
     */
    public PromptContext() {
        this.properties = new HashMap<>();
    }

    /**
     * Set a property in the context
     *
     * @param key The property key
     * @param value The property value
     */
    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }

    /**
     * Get a property from the context
     *
     * @param key The property key
     * @return The property value or null if not found
     */
    @SuppressWarnings("unchecked")
    public <T> T getProperty(String key) {
        return (T) properties.get(key);
    }

    /**
     * Get all properties
     *
     * @return The properties map
     */
    public Map<String, Object> getProperties() {
        return new HashMap<>(properties);
    }

    /**
     * Check if a property exists
     *
     * @param key The property key
     * @return true if the property exists, false otherwise
     */
    public boolean hasProperty(String key) {
        return properties.containsKey(key);
    }

    /**
     * Remove a property from the context
     *
     * @param key The property key to remove
     */
    public void removeProperty(String key) {
        properties.remove(key);
    }

    /**
     * Clear all properties
     */
    public void clearProperties() {
        properties.clear();
    }
}
