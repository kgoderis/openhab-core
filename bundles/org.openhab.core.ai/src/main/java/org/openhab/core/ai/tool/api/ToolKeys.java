package org.openhab.core.ai.tool.api;

/**
 * Tool-specific context keys for type-safe access to execution context values.
 * 
 * <p>
 * This enum provides documented, type-safe keys for tool-related protocol context
 * fields that are not already covered by ExecutionContext fields. It eliminates
 * string-based field access while maintaining the flexibility of the general value
 * map approach.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
public enum ToolKeys {

    /**
     * Tool name identifier (e.g., "file_search", "codebase_search")
     */
    TOOL_NAME("tool"),

    /**
     * Tool identifier for unique tool instances
     */
    TOOL_ID("toolId"),

    /**
     * Tool type or category
     */
    TOOL_TYPE("toolType"),

    /**
     * Tool version for compatibility
     */
    TOOL_VERSION("toolVersion"),

    /**
     * Tool provider identifier
     */
    PROVIDER("provider"),

    /**
     * Tool capabilities list
     */
    CAPABILITIES("capabilities");

    private final String key;

    ToolKeys(String key) {
        this.key = key;
    }

    /**
     * Get the string key value.
     * 
     * @return the key string
     */
    public String getKey() {
        return key;
    }

    @Override
    public String toString() {
        return key;
    }
}
