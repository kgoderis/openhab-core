package org.openhab.core.ai.common.monitoring.api;

import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Factory for creating common metric keys with predefined capabilities.
 * 
 * <p>
 * This class provides factory methods for creating MetricKey instances
 * with appropriate capabilities for different metric types.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class MetricKeys {

    private MetricKeys() {
        // Utility class - prevent instantiation
    }

    /**
     * Simple implementation of MetricKey with capabilities.
     */
    public record SimpleMetricKey(String kind, Map<String, String> labels, Set<String> capabilities) 
            implements MetricKey {}

    // ===== Basic Metric Keys =====

    /**
     * Create a key for general execution metrics.
     * 
     * @param operation the operation name
     * @return the metric key
     */
    public static MetricKey execution(String operation) {
        return new SimpleMetricKey("execution", Map.of("operation", operation), 
            Set.of("counts", "latency"));
    }

    /**
     * Create a key for provider metrics.
     * 
     * @param provider the provider name
     * @return the metric key
     */
    public static MetricKey provider(String provider) {
        return new SimpleMetricKey("provider", Map.of("name", provider), 
            Set.of("counts", "latency", "health"));
    }

    /**
     * Create a key for tool metrics.
     * 
     * @param toolName the tool name
     * @return the metric key
     */
    public static MetricKey tool(String toolName) {
        return new SimpleMetricKey("tool", Map.of("name", toolName), 
            Set.of("counts", "latency", "tool"));
    }

    /**
     * Create a key for action metrics.
     * 
     * @param actionName the action name
     * @return the metric key
     */
    public static MetricKey action(String actionName) {
        return new SimpleMetricKey("action", Map.of("name", actionName), 
            Set.of("counts", "latency", "action"));
    }

    // ===== Domain-Specific Metric Keys =====

    /**
     * Create a key for model completion metrics.
     * 
     * @param modelId the model identifier
     * @return the metric key
     */
    public static MetricKey modelCompletion(String modelId) {
        return new SimpleMetricKey("model", Map.of("modelId", modelId), 
            Set.of("counts", "latency", "model", "model-stats"));
    }

    /**
     * Create a key for tool execution metrics.
     * 
     * @param toolId the tool identifier
     * @return the metric key
     */
    public static MetricKey toolExecution(String toolId) {
        return new SimpleMetricKey("tool", Map.of("toolId", toolId), 
            Set.of("counts", "latency", "tool", "tool-stats"));
    }

    /**
     * Create a key for action execution metrics.
     * 
     * @param actionId the action identifier
     * @return the metric key
     */
    public static MetricKey actionExecution(String actionId) {
        return new SimpleMetricKey("action", Map.of("actionId", actionId), 
            Set.of("counts", "latency", "action", "action-stats"));
    }

    /**
     * Create a key for agent task metrics.
     * 
     * @param agentId the agent identifier
     * @return the metric key
     */
    public static MetricKey agentTask(String agentId) {
        return new SimpleMetricKey("agent", Map.of("agentId", agentId), 
            Set.of("counts", "latency", "agent", "agent-stats", "behavior-stats", "persistence-stats", "reasoning-stats", "coordination-stats", "optimization-stats"));
    }

    /**
     * Create a key for agent model metrics.
     * 
     * @param modelId the model identifier
     * @return the metric key
     */
    public static MetricKey agentModel(String modelId) {
        return new SimpleMetricKey("agent-model", Map.of("modelId", modelId), 
            Set.of("counts", "latency", "model", "agent", "agent-stats"));
    }

    /**
     * Create a key for tool file read metrics.
     * 
     * @param toolId the tool identifier
     * @return the metric key
     */
    public static MetricKey toolFileRead(String toolId) {
        return new SimpleMetricKey("tool-file", Map.of("toolId", toolId), 
            Set.of("counts", "latency", "tool", "file", "tool-stats"));
    }

    /**
     * Create a key for communication metrics.
     * 
     * @param communicationId the communication identifier
     * @return the metric key
     */
    public static MetricKey communication(String communicationId) {
        return new SimpleMetricKey("communication", Map.of("communicationId", communicationId), 
            Set.of("counts", "latency", "communication"));
    }

    /**
     * Create a key for error recovery metrics.
     * 
     * @return the metric key
     */
    public static MetricKey errorRecovery() {
        return new SimpleMetricKey("error-recovery", Map.of(), 
            Set.of("counts", "latency", "error", "error-stats"));
    }

    /**
     * Create a key for security monitoring metrics.
     * 
     * @param securityId the security identifier
     * @return the metric key
     */
    public static MetricKey securityMonitoring(String securityId) {
        return new SimpleMetricKey("security", Map.of("securityId", securityId), 
            Set.of("counts", "latency", "security", "security-stats"));
    }

    /**
     * Create a key for monitoring operations.
     * 
     * @param monitoringId the monitoring identifier
     * @return the metric key
     */
    public static MetricKey monitoring(String monitoringId) {
        return new SimpleMetricKey("monitoring", Map.of("monitoringId", monitoringId), 
            Set.of("counts", "latency", "monitoring", "monitoring-stats"));
    }

    /**
     * Create a key for domain aggregated metrics.
     * 
     * @param domain the domain name
     * @return the metric key
     */
    public static MetricKey domainAggregated(String domain) {
        return new SimpleMetricKey("domain", Map.of("domain", domain), 
            Set.of("counts", "latency", "aggregation"));
    }

    // ===== Custom Metric Keys =====

    /**
     * Create a custom metric key with specified capabilities.
     * 
     * @param kind the metric kind
     * @param labels the labels map
     * @param capabilities the supported capabilities
     * @return the metric key
     */
    public static MetricKey custom(String kind, Map<String, String> labels, Set<String> capabilities) {
        return new SimpleMetricKey(kind, labels, capabilities);
    }

    // ===== Domain-Specific Health Metric Keys =====

    /**
     * Create a key for model health metrics.
     * 
     * @param modelId the model identifier
     * @return the metric key
     */
    public static MetricKey modelHealth(String modelId) {
        return new SimpleMetricKey("model-health", Map.of("modelId", modelId), 
            Set.of("counts", "latency", "health"));
    }

    /**
     * Create a key for tool health metrics.
     * 
     * @param toolId the tool identifier
     * @return the metric key
     */
    public static MetricKey toolHealth(String toolId) {
        return new SimpleMetricKey("tool-health", Map.of("toolId", toolId), 
            Set.of("counts", "latency", "health", "circuit-breaker"));
    }

    /**
     * Create a key for provider health metrics.
     * 
     * @param providerId the provider identifier
     * @return the metric key
     */
    public static MetricKey providerHealth(String providerId) {
        return new SimpleMetricKey("provider-health", Map.of("providerId", providerId), 
            Set.of("counts", "latency", "health", "circuit-breaker"));
    }

    /**
     * Create a key for agent health metrics.
     * 
     * @param agentId the agent identifier
     * @return the metric key
     */
    public static MetricKey agentHealth(String agentId) {
        return new SimpleMetricKey("agent-health", Map.of("agentId", agentId), 
            Set.of("counts", "latency", "health", "circuit-breaker"));
    }

    /**
     * Create a key for agent dialogue statistics.
     * 
     * @param agentId the agent identifier
     * @return the metric key
     */
    public static MetricKey agentDialogue(String agentId) {
        return new SimpleMetricKey("agent-dialogue", Map.of("agentId", agentId), 
            Set.of("counts", "latency", "statistics"));
    }
}
