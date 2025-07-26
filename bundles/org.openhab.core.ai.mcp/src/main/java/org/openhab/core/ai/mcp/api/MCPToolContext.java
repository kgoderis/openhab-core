package org.openhab.core.ai.mcp.api;

import java.util.HashMap;
import java.util.Map;

public class MCPToolContext {
    private final Map<String, Object> properties;

    public MCPToolContext() {
        this.properties = new HashMap<>();
    }

    public Object getProperty(String key) {
        return properties.get(key);
    }

    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }
}
