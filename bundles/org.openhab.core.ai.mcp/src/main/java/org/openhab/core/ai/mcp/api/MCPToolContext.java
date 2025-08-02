package org.openhab.core.ai.mcp.api;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Context information for MCP tool execution.
 * 
 * @author AI Assistant
 * @since 1.0.0
 */
@NonNullByDefault
public class MCPToolContext {
    private final Map<String, Object> properties;

    public MCPToolContext() {
        this.properties = new HashMap<>();
    }

    public @Nullable Object getProperty(String key) {
        return properties.get(key);
    }

    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }
}
