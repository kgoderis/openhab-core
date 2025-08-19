package org.openhab.core.ai.action.api;

/**
 * Action-specific context keys for type-safe access to execution context values.
 * 
 * <p>
 * This enum provides documented, type-safe keys for action-related protocol context
 * fields that are not already covered by ExecutionContext fields. It eliminates
 * string-based field access while maintaining the flexibility of the general value
 * map approach.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
public enum ActionKeys {

    /**
     * Action name identifier (e.g., "openhab.items.get", "openhab.things.status")
     */
    ACTION_NAME("action"),

    /**
     * Action identifier for unique action instances
     */
    ACTION_ID("actionId"),

    /**
     * Action type or category
     */
    ACTION_TYPE("actionType"),

    /**
     * Action version for compatibility
     */
    ACTION_VERSION("actionVersion"),

    /**
     * Action retry count
     */
    RETRY_COUNT("retryCount"),

    /**
     * Action execution mode
     */
    EXECUTION_MODE("executionMode"),

    /**
     * Action parameters map
     */
    PARAMETERS("parameters"),

    /**
     * Action arguments map (alternative to parameters)
     */
    ARGUMENTS("arguments");

    private final String key;

    ActionKeys(String key) {
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
