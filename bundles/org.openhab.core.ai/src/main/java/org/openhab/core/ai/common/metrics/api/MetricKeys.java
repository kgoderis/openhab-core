package org.openhab.core.ai.common.metrics.api;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Utility class for creating common metric keys.
 * 
 * <p>
 * This class provides factory methods for creating metric keys for common
 * use cases.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class MetricKeys {

    private MetricKeys() {
        // Utility class
    }

    /**
     * Create a metric key for a provider.
     * 
     * @param provider provider name
     * @return metric key
     */
    public static MetricKey provider(String provider) {
        return new SimpleMetricKey("provider", Map.of("name", provider));
    }

    /**
     * Create a metric key for a tool.
     * 
     * @param toolName tool name
     * @return metric key
     */
    public static MetricKey tool(String toolName) {
        return new SimpleMetricKey("tool", Map.of("name", toolName));
    }

    /**
     * Create a metric key for an action.
     * 
     * @param actionName action name
     * @return metric key
     */
    public static MetricKey action(String actionName) {
        return new SimpleMetricKey("action", Map.of("name", actionName));
    }

    /**
     * Create a metric key for an agent.
     * 
     * @param agentName agent name
     * @return metric key
     */
    public static MetricKey agent(String agentName) {
        return new SimpleMetricKey("agent", Map.of("name", agentName));
    }

    /**
     * Create a metric key for delegation.
     * 
     * @param delegationType delegation type
     * @return metric key
     */
    public static MetricKey delegation(String delegationType) {
        return new SimpleMetricKey("delegation", Map.of("type", delegationType));
    }

    /**
     * Simple implementation of MetricKey.
     */
    public record SimpleMetricKey(String kind, Map<String, String> labels) implements MetricKey {
        // Implementation is complete with the record
    }
}
