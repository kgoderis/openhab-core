package org.openhab.core.ai.common.monitoring.api;

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
     * Create a metric key for reasoning operations.
     * 
     * @param reasoningType reasoning type
     * @return metric key
     */
    public static MetricKey reasoning(String reasoningType) {
        return new SimpleMetricKey("reasoning", Map.of("type", reasoningType));
    }

    /**
     * Create a metric key for events.
     * 
     * @param eventType event type
     * @return metric key
     */
    public static MetricKey events(String eventType) {
        return new SimpleMetricKey("events", Map.of("type", eventType));
    }

    /**
     * Create a metric key for model operations.
     * 
     * @param modelType model type
     * @return metric key
     */
    public static MetricKey model(String modelType) {
        return new SimpleMetricKey("model", Map.of("type", modelType));
    }

    /**
     * Create a metric key for validation operations.
     * 
     * @param validationType validation type
     * @return metric key
     */
    public static MetricKey validation(String validationType) {
        return new SimpleMetricKey("validation", Map.of("type", validationType));
    }

    /**
     * Create a metric key for security operations.
     * 
     * @param securityType security type
     * @return metric key
     */
    public static MetricKey security(String securityType) {
        return new SimpleMetricKey("security", Map.of("type", securityType));
    }

    /**
     * Simple implementation of MetricKey with validation.
     */
    public record SimpleMetricKey(String kind, Map<String, String> labels) implements MetricKey {

        /**
         * Create a new SimpleMetricKey with validation.
         * 
         * @param kind the metric kind
         * @param labels the metric labels
         */
        public SimpleMetricKey {
            if (kind == null || kind.isBlank()) {
                throw new IllegalArgumentException("kind must not be null or blank");
            }
            if (labels == null) {
                throw new IllegalArgumentException("labels must not be null");
            }
            if (labels.size() > 10) {
                throw new IllegalArgumentException("labels size must not exceed 10 to prevent cardinality explosion");
            }
            for (Map.Entry<String, String> entry : labels.entrySet()) {
                if (entry.getKey() == null || entry.getKey().isBlank()) {
                    throw new IllegalArgumentException("label key must not be null or blank");
                }
                if (entry.getValue() == null || entry.getValue().isBlank()) {
                    throw new IllegalArgumentException("label value must not be null or blank");
                }
                if (entry.getKey().length() > 50) {
                    throw new IllegalArgumentException("label key length must not exceed 50 characters");
                }
                if (entry.getValue().length() > 100) {
                    throw new IllegalArgumentException("label value length must not exceed 100 characters");
                }
            }
        }
    }
}
