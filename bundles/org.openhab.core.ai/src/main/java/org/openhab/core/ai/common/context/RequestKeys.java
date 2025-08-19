package org.openhab.core.ai.common.context;

/**
 * Request-specific context keys for type-safe access to execution context values.
 * 
 * <p>
 * This enum provides documented, type-safe keys for request-related protocol context
 * fields that are not already covered by ExecutionContext fields. It eliminates
 * string-based field access while maintaining the flexibility of the general value
 * map approach.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
public enum RequestKeys {

    /**
     * Request identifier for correlation
     */
    REQUEST_ID("requestId"),

    /**
     * Protocol version
     */
    PROTOCOL_VERSION("protocolVersion"),

    /**
     * Timestamp for request timing
     */
    TIMESTAMP("timestamp");

    private final String key;

    RequestKeys(String key) {
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
