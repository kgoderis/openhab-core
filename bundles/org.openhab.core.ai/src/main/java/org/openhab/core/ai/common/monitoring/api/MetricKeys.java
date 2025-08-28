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
    public record SimpleMetricKey(String kind, Map<String, String> labels,
            Set<String> capabilities) implements MetricKey {
    }

    // ===== Basic Metric Keys =====

    /**
     * Create a key for general execution metrics.
     * 
     * @param operation the operation name
     * @return the metric key
     */
    public static MetricKey execution(String operation) {
        return new SimpleMetricKey("execution", Map.of("operation", operation), Set.of("counts", "latency"));
    }

    /**
     * Create a key for provider metrics.
     * 
     * @param provider the provider name
     * @return the metric key
     */
    public static MetricKey provider(String provider) {
        return new SimpleMetricKey("provider", Map.of("name", provider), Set.of("counts", "latency", "health"));
    }

    /**
     * Create a key for tool metrics.
     * 
     * @param toolName the tool name
     * @return the metric key
     */
    public static MetricKey tool(String toolName) {
        return new SimpleMetricKey("tool", Map.of("name", toolName), Set.of("counts", "latency", "tool"));
    }

    /**
     * Create a key for action metrics.
     * 
     * @param actionName the action name
     * @return the metric key
     */
    public static MetricKey action(String actionName) {
        return new SimpleMetricKey("action", Map.of("name", actionName), Set.of("counts", "latency", "action"));
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
        return new SimpleMetricKey("tool", Map.of("toolId", toolId), Set.of("counts", "latency", "tool", "tool-stats"));
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
                Set.of("counts", "latency", "agent", "agent-stats", "behavior-stats", "persistence-stats",
                        "reasoning-stats", "coordination-stats", "optimization-stats"));
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
        return new SimpleMetricKey("error-recovery", Map.of(), Set.of("counts", "latency", "error", "error-stats"));
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
        return new SimpleMetricKey("domain", Map.of("domain", domain), Set.of("counts", "latency", "aggregation"));
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
        return new SimpleMetricKey("model-health", Map.of("modelId", modelId), Set.of("counts", "latency", "health"));
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

    // ===== Cache Metric Keys =====

    /**
     * Create a standardized key for cache hit metrics.
     * 
     * @param cacheName the cache name
     * @return the metric key
     */
    public static MetricKey cacheHit(String cacheName) {
        return new SimpleMetricKey("cache-hit", Map.of("cacheName", cacheName),
                Set.of("counts", "hit-rate", "cache-stats"));
    }

    /**
     * Create a standardized key for cache miss metrics.
     * 
     * @param cacheName the cache name
     * @return the metric key
     */
    public static MetricKey cacheMiss(String cacheName) {
        return new SimpleMetricKey("cache-miss", Map.of("cacheName", cacheName),
                Set.of("counts", "hit-rate", "cache-stats"));
    }

    /**
     * Create a standardized key for cache eviction metrics.
     * 
     * @param cacheName the cache name
     * @return the metric key
     */
    public static MetricKey cacheEviction(String cacheName) {
        return new SimpleMetricKey("cache-eviction", Map.of("cacheName", cacheName), Set.of("counts", "cache-stats"));
    }

    /**
     * Create a standardized key for cache size metrics.
     * 
     * @param cacheName the cache name
     * @return the metric key
     */
    public static MetricKey cacheSize(String cacheName) {
        return new SimpleMetricKey("cache-size", Map.of("cacheName", cacheName), Set.of("cache-stats"));
    }

    /**
     * Create a standardized key for cache utilization metrics.
     * 
     * @param cacheName the cache name
     * @return the metric key
     */
    public static MetricKey cacheUtilization(String cacheName) {
        return new SimpleMetricKey("cache-utilization", Map.of("cacheName", cacheName), Set.of("cache-stats"));
    }

    /**
     * Create a standardized key for general cache operations.
     * 
     * @param cacheName the cache name
     * @param operation the cache operation (e.g., "lookup", "store", "clear")
     * @return the metric key
     */
    public static MetricKey cacheOperation(String cacheName, String operation) {
        return new SimpleMetricKey("cache-operation", Map.of("cacheName", cacheName, "operation", operation),
                Set.of("counts", "latency", "cache-stats"));
    }

    // ===== Security Metric Keys =====

    /**
     * Create a standardized key for tool security metrics.
     * 
     * @param component the security component (e.g., "tool-access", "authentication")
     * @return the metric key
     */
    public static MetricKey toolSecurity(String component) {
        return new SimpleMetricKey("tool-security", Map.of("component", component),
                Set.of("counts", "security", "security-stats"));
    }

    /**
     * Create a standardized key for authentication metrics.
     * 
     * @param authType the authentication type (e.g., "jwt", "api-key", "oauth")
     * @return the metric key
     */
    public static MetricKey authentication(String authType) {
        return new SimpleMetricKey("authentication", Map.of("type", authType),
                Set.of("counts", "security", "security-stats"));
    }

    /**
     * Create a standardized key for authorization metrics.
     * 
     * @param resource the protected resource
     * @return the metric key
     */
    public static MetricKey authorization(String resource) {
        return new SimpleMetricKey("authorization", Map.of("resource", resource),
                Set.of("counts", "security", "security-stats"));
    }

    /**
     * Create a standardized key for security violation metrics.
     * 
     * @param violationType the type of security violation
     * @return the metric key
     */
    public static MetricKey securityViolation(String violationType) {
        return new SimpleMetricKey("security-violation", Map.of("type", violationType),
                Set.of("counts", "security", "security-stats"));
    }

    /**
     * Create a standardized key for rate limiting metrics.
     * 
     * @param limitType the type of rate limit (e.g., "per-user", "per-ip", "global")
     * @return the metric key
     */
    public static MetricKey rateLimiting(String limitType) {
        return new SimpleMetricKey("rate-limiting", Map.of("type", limitType),
                Set.of("counts", "security", "security-stats"));
    }

    /**
     * Create a standardized key for message security metrics.
     * 
     * @param operation the security operation (e.g., "encrypt", "decrypt", "sign", "verify")
     * @return the metric key
     */
    public static MetricKey messageSecurity(String operation) {
        return new SimpleMetricKey("message-security", Map.of("operation", operation),
                Set.of("counts", "latency", "security", "security-stats"));
    }

    // ===== Transport Metric Keys =====

    /**
     * Create a standardized key for HTTP transport metrics.
     * 
     * @param providerId the transport provider ID
     * @return the metric key
     */
    public static MetricKey httpTransport(String providerId) {
        return new SimpleMetricKey("http-transport", Map.of("providerId", providerId),
                Set.of("counts", "latency", "transport-stats"));
    }

    /**
     * Create a standardized key for transport request metrics.
     * 
     * @param providerId the transport provider ID
     * @param requestType the type of request (e.g., "stats", "health", "api")
     * @return the metric key
     */
    public static MetricKey transportRequest(String providerId, String requestType) {
        return new SimpleMetricKey("transport-request", Map.of("providerId", providerId, "requestType", requestType),
                Set.of("counts", "latency", "transport-stats"));
    }

    /**
     * Create a standardized key for transport bandwidth metrics.
     * 
     * @param providerId the transport provider ID
     * @return the metric key
     */
    public static MetricKey transportBandwidth(String providerId) {
        return new SimpleMetricKey("transport-bandwidth", Map.of("providerId", providerId), Set.of("transport-stats"));
    }

    /**
     * Create a standardized key for transport error metrics.
     * 
     * @param providerId the transport provider ID
     * @param errorType the type of error (e.g., "timeout", "connection", "protocol")
     * @return the metric key
     */
    public static MetricKey transportError(String providerId, String errorType) {
        return new SimpleMetricKey("transport-error", Map.of("providerId", providerId, "errorType", errorType),
                Set.of("counts", "transport-stats"));
    }

    /**
     * Create a standardized key for transport uptime metrics.
     * 
     * @param providerId the transport provider ID
     * @return the metric key
     */
    public static MetricKey transportUptime(String providerId) {
        return new SimpleMetricKey("transport-uptime", Map.of("providerId", providerId), Set.of("transport-stats"));
    }

    // ===== Task Metric Keys =====

    /**
     * Create a standardized key for task execution metrics.
     * 
     * @param taskId the task ID
     * @param taskType the type of task
     * @return the metric key
     */
    public static MetricKey taskExecution(String taskId, String taskType) {
        return new SimpleMetricKey("task-execution", Map.of("taskId", taskId, "taskType", taskType),
                Set.of("counts", "latency", "task-stats"));
    }

    /**
     * Create a standardized key for task lifecycle metrics.
     * 
     * @param taskId the task ID
     * @param taskState the current task state
     * @return the metric key
     */
    public static MetricKey taskLifecycle(String taskId, String taskState) {
        return new SimpleMetricKey("task-lifecycle", Map.of("taskId", taskId, "taskState", taskState),
                Set.of("counts", "task-stats"));
    }

    /**
     * Create a standardized key for task performance metrics.
     * 
     * @param taskType the type of task
     * @return the metric key
     */
    public static MetricKey taskPerformance(String taskType) {
        return new SimpleMetricKey("task-performance", Map.of("taskType", taskType),
                Set.of("counts", "latency", "task-stats"));
    }

    /**
     * Create a standardized key for task retry metrics.
     * 
     * @param taskId the task ID
     * @param retryReason the reason for retry
     * @return the metric key
     */
    public static MetricKey taskRetry(String taskId, String retryReason) {
        return new SimpleMetricKey("task-retry", Map.of("taskId", taskId, "retryReason", retryReason),
                Set.of("counts", "task-stats"));
    }

    /**
     * Create a standardized key for task orchestration metrics.
     * 
     * @param orchestrationId the orchestration ID
     * @return the metric key
     */
    public static MetricKey taskOrchestration(String orchestrationId) {
        return new SimpleMetricKey("task-orchestration", Map.of("orchestrationId", orchestrationId),
                Set.of("counts", "latency", "task-stats"));
    }

    /**
     * Create a standardized key for task efficiency metrics.
     * 
     * @param taskType the type of task
     * @return the metric key
     */
    public static MetricKey taskEfficiency(String taskType) {
        return new SimpleMetricKey("task-efficiency", Map.of("taskType", taskType), Set.of("task-stats"));
    }

    // ===== OpenHAB Persistence Metric Keys =====

    /**
     * Create a standardized key for openHAB item state persistence metrics.
     * 
     * @param serviceId the persistence service ID
     * @param itemName the item name
     * @return the metric key
     */
    public static MetricKey openHABItemStatePersistence(String serviceId, String itemName) {
        return new SimpleMetricKey("openhab-item-state-persistence",
                Map.of("serviceId", serviceId, "itemName", itemName),
                Set.of("counts", "latency", "openhab-persistence-stats"));
    }

    /**
     * Create a standardized key for openHAB persistence query metrics.
     * 
     * @param serviceId the persistence service ID
     * @param queryType the type of query (e.g., "historical", "latest", "since")
     * @return the metric key
     */
    public static MetricKey openHABPersistenceQuery(String serviceId, String queryType) {
        return new SimpleMetricKey("openhab-persistence-query", Map.of("serviceId", serviceId, "queryType", queryType),
                Set.of("counts", "latency", "openhab-persistence-stats"));
    }

    /**
     * Create a standardized key for openHAB persistence backup metrics.
     * 
     * @param serviceId the persistence service ID
     * @param backupType the type of backup (e.g., "full", "incremental", "scheduled")
     * @return the metric key
     */
    public static MetricKey openHABPersistenceBackup(String serviceId, String backupType) {
        return new SimpleMetricKey("openhab-persistence-backup",
                Map.of("serviceId", serviceId, "backupType", backupType),
                Set.of("counts", "latency", "openhab-persistence-stats"));
    }

    /**
     * Create a standardized key for openHAB persistence restore metrics.
     * 
     * @param serviceId the persistence service ID
     * @param restoreType the type of restore (e.g., "full", "partial", "emergency")
     * @return the metric key
     */
    public static MetricKey openHABPersistenceRestore(String serviceId, String restoreType) {
        return new SimpleMetricKey("openhab-persistence-restore",
                Map.of("serviceId", serviceId, "restoreType", restoreType),
                Set.of("counts", "latency", "openhab-persistence-stats"));
    }

    /**
     * Create a standardized key for openHAB persistence service health metrics.
     * 
     * @param serviceId the persistence service ID
     * @return the metric key
     */
    public static MetricKey openHABPersistenceServiceHealth(String serviceId) {
        return new SimpleMetricKey("openhab-persistence-service-health", Map.of("serviceId", serviceId),
                Set.of("openhab-persistence-stats"));
    }

    /**
     * Create a standardized key for openHAB persistence data size metrics.
     * 
     * @param serviceId the persistence service ID
     * @return the metric key
     */
    public static MetricKey openHABPersistenceDataSize(String serviceId) {
        return new SimpleMetricKey("openhab-persistence-data-size", Map.of("serviceId", serviceId),
                Set.of("openhab-persistence-stats"));
    }

    /**
     * Create a standardized key for openHAB persistence throughput metrics.
     * 
     * @param serviceId the persistence service ID
     * @return the metric key
     */
    public static MetricKey openHABPersistenceThroughput(String serviceId) {
        return new SimpleMetricKey("openhab-persistence-throughput", Map.of("serviceId", serviceId),
                Set.of("openhab-persistence-stats"));
    }

    // ===== Enhanced Metrics Recording Patterns =====

    /**
     * Create a standardized key for task lifecycle metrics.
     * 
     * @param taskId the task ID
     * @param lifecycleEvent the lifecycle event (e.g., "created", "activated", "completed", "cancelled")
     * @return the metric key
     */
    public static MetricKey taskLifecycleEvent(String taskId, String lifecycleEvent) {
        return new SimpleMetricKey("task-lifecycle-event", Map.of("taskId", taskId, "event", lifecycleEvent),
                Set.of("counts", "latency", "task-stats"));
    }

    /**
     * Create a standardized key for validation rule execution metrics.
     * 
     * @param ruleId the validation rule ID
     * @param ruleType the type of validation rule
     * @return the metric key
     */
    public static MetricKey validationRuleExecution(String ruleId, String ruleType) {
        return new SimpleMetricKey("validation-rule-execution", Map.of("ruleId", ruleId, "ruleType", ruleType),
                Set.of("counts", "latency", "validation-stats"));
    }

    /**
     * Create a standardized key for skill execution metrics.
     * 
     * @param skillId the skill ID
     * @param skillType the type of skill
     * @return the metric key
     */
    public static MetricKey skillExecution(String skillId, String skillType) {
        return new SimpleMetricKey("skill-execution", Map.of("skillId", skillId, "skillType", skillType),
                Set.of("counts", "latency", "skill-stats"));
    }

    /**
     * Create a standardized key for audit event metrics.
     * 
     * @param eventCategory the audit event category
     * @param severityLevel the severity level (e.g., "low", "medium", "high", "critical")
     * @return the metric key
     */
    public static MetricKey auditEvent(String eventCategory, String severityLevel) {
        return new SimpleMetricKey("audit-event", Map.of("category", eventCategory, "severity", severityLevel),
                Set.of("counts", "audit-stats"));
    }

    /**
     * Create a standardized key for configuration operation metrics.
     * 
     * @param operationType the configuration operation type (e.g., "cache-hit", "cache-miss", "reload",
     *            "file-operation")
     * @param configType the configuration type
     * @return the metric key
     */
    public static MetricKey configurationOperation(String operationType, String configType) {
        return new SimpleMetricKey("configuration-operation",
                Map.of("operationType", operationType, "configType", configType),
                Set.of("counts", "latency", "configuration-stats"));
    }

    /**
     * Create a standardized key for agent card building metrics.
     * 
     * @param cardType the type of agent card
     * @param buildStep the card building step (e.g., "generation", "validation", "success")
     * @return the metric key
     */
    public static MetricKey agentCardBuilding(String cardType, String buildStep) {
        return new SimpleMetricKey("agent-card-building", Map.of("cardType", cardType, "buildStep", buildStep),
                Set.of("counts", "latency", "card-stats"));
    }

    // ===== Business Logic Value Capture =====

    /**
     * Create a standardized key for task executor assignment metrics.
     * 
     * @param executorId the executor ID
     * @param taskType the type of task being assigned
     * @return the metric key
     */
    public static MetricKey taskExecutorAssignment(String executorId, String taskType) {
        return new SimpleMetricKey("task-executor-assignment", Map.of("executorId", executorId, "taskType", taskType),
                Set.of("counts", "assignment-stats"));
    }

    /**
     * Create a standardized key for validation rule effectiveness metrics.
     * 
     * @param ruleId the validation rule ID
     * @param effectiveness the effectiveness level (e.g., "high", "medium", "low")
     * @return the metric key
     */
    public static MetricKey validationRuleEffectiveness(String ruleId, String effectiveness) {
        return new SimpleMetricKey("validation-rule-effectiveness",
                Map.of("ruleId", ruleId, "effectiveness", effectiveness), Set.of("counts", "effectiveness-stats"));
    }

    /**
     * Create a standardized key for skill usage pattern metrics.
     * 
     * @param skillId the skill ID
     * @param usagePattern the usage pattern (e.g., "standalone", "chained", "parallel")
     * @return the metric key
     */
    public static MetricKey skillUsagePattern(String skillId, String usagePattern) {
        return new SimpleMetricKey("skill-usage-pattern", Map.of("skillId", skillId, "usagePattern", usagePattern),
                Set.of("counts", "usage-stats"));
    }

    /**
     * Create a standardized key for audit event pattern metrics.
     * 
     * @param patternType the audit event pattern type
     * @param sequenceLength the sequence length
     * @return the metric key
     */
    public static MetricKey auditEventPattern(String patternType, String sequenceLength) {
        return new SimpleMetricKey("audit-event-pattern",
                Map.of("patternType", patternType, "sequenceLength", sequenceLength),
                Set.of("counts", "pattern-stats"));
    }

    /**
     * Create a standardized key for configuration change metrics.
     * 
     * @param configType the configuration type
     * @param changeType the type of change (e.g., "value-change", "structure-change", "addition", "removal")
     * @return the metric key
     */
    public static MetricKey configurationChange(String configType, String changeType) {
        return new SimpleMetricKey("configuration-change", Map.of("configType", configType, "changeType", changeType),
                Set.of("counts", "change-stats"));
    }

    /**
     * Create a standardized key for notification effectiveness metrics.
     * 
     * @param notificationType the type of notification
     * @param effectiveness the effectiveness level (e.g., "delivered", "read", "acted-upon")
     * @return the metric key
     */
    public static MetricKey notificationEffectiveness(String notificationType, String effectiveness) {
        return new SimpleMetricKey("notification-effectiveness",
                Map.of("notificationType", notificationType, "effectiveness", effectiveness),
                Set.of("counts", "notification-stats"));
    }

    // ===== Performance Metrics Enhancement =====

    /**
     * Create a standardized key for operation timing context metrics.
     * 
     * @param operationType the operation type
     * @param complexity the operation complexity level
     * @return the metric key
     */
    public static MetricKey operationTimingContext(String operationType, String complexity) {
        return new SimpleMetricKey("operation-timing-context",
                Map.of("operationType", operationType, "complexity", complexity),
                Set.of("counts", "latency", "timing-stats"));
    }

    /**
     * Create a standardized key for resource utilization context metrics.
     * 
     * @param resourceType the resource type (e.g., "memory", "cpu", "disk", "network")
     * @param utilizationLevel the utilization level (e.g., "low", "medium", "high", "critical")
     * @return the metric key
     */
    public static MetricKey resourceUtilizationContext(String resourceType, String utilizationLevel) {
        return new SimpleMetricKey("resource-utilization-context",
                Map.of("resourceType", resourceType, "utilizationLevel", utilizationLevel),
                Set.of("counts", "resource-stats"));
    }

    /**
     * Create a standardized key for concurrency metrics.
     * 
     * @param operationType the operation type
     * @param concurrencyLevel the concurrency level (e.g., "low", "medium", "high")
     * @return the metric key
     */
    public static MetricKey concurrencyMetrics(String operationType, String concurrencyLevel) {
        return new SimpleMetricKey("concurrency-metrics",
                Map.of("operationType", operationType, "concurrencyLevel", concurrencyLevel),
                Set.of("counts", "concurrency-stats"));
    }

    /**
     * Create a standardized key for quality metrics.
     * 
     * @param operationType the operation type
     * @param qualityLevel the quality level (e.g., "excellent", "good", "fair", "poor")
     * @return the metric key
     */
    public static MetricKey qualityMetrics(String operationType, String qualityLevel) {
        return new SimpleMetricKey("quality-metrics",
                Map.of("operationType", operationType, "qualityLevel", qualityLevel),
                Set.of("counts", "quality-stats"));
    }

    /**
     * Create a standardized key for user experience metrics.
     * 
     * @param experienceType the experience type (e.g., "response-time", "interaction", "satisfaction")
     * @param experienceLevel the experience level (e.g., "excellent", "good", "fair", "poor")
     * @return the metric key
     */
    public static MetricKey userExperienceMetrics(String experienceType, String experienceLevel) {
        return new SimpleMetricKey("user-experience-metrics",
                Map.of("experienceType", experienceType, "experienceLevel", experienceLevel),
                Set.of("counts", "latency", "experience-stats"));
    }

    /**
     * Create a standardized key for system health correlation metrics.
     * 
     * @param healthIndicator the health indicator
     * @param correlationLevel the correlation level (e.g., "strong", "moderate", "weak")
     * @return the metric key
     */
    public static MetricKey systemHealthCorrelation(String healthIndicator, String correlationLevel) {
        return new SimpleMetricKey("system-health-correlation",
                Map.of("healthIndicator", healthIndicator, "correlationLevel", correlationLevel),
                Set.of("counts", "correlation-stats"));
    }

    // ===== Audit Metric Keys =====

    /**
     * Create a standardized key for authentication attempt metrics.
     * 
     * @param authMethod the authentication method
     * @return the metric key
     */
    public static MetricKey authenticationAttempt(String authMethod) {
        return new SimpleMetricKey("authentication-attempt", Map.of("authMethod", authMethod),
                Set.of("counts", "latency", "audit-stats"));
    }

    /**
     * Create a standardized key for authorization decision metrics.
     * 
     * @param resourceType the type of resource being accessed
     * @return the metric key
     */
    public static MetricKey authorizationDecision(String resourceType) {
        return new SimpleMetricKey("authorization-decision", Map.of("resourceType", resourceType),
                Set.of("counts", "audit-stats"));
    }

    /**
     * Create a standardized key for audit system health metrics.
     * 
     * @param auditSystem the audit system identifier
     * @return the metric key
     */
    public static MetricKey auditSystemHealth(String auditSystem) {
        return new SimpleMetricKey("audit-system-health", Map.of("auditSystem", auditSystem), Set.of("audit-stats"));
    }

    // ===== Notification Metric Keys =====

    /**
     * Create a standardized key for notification delivery metrics.
     * 
     * @param notificationType the type of notification
     * @param eventCategory the event category
     * @return the metric key
     */
    public static MetricKey notificationDelivery(String notificationType, String eventCategory) {
        return new SimpleMetricKey("notification-delivery",
                Map.of("notificationType", notificationType, "eventCategory", eventCategory),
                Set.of("counts", "latency", "notification-stats"));
    }

    /**
     * Create a standardized key for notification listener metrics.
     * 
     * @param listenerType the type of listener
     * @return the metric key
     */
    public static MetricKey notificationListener(String listenerType) {
        return new SimpleMetricKey("notification-listener", Map.of("listenerType", listenerType),
                Set.of("counts", "notification-stats"));
    }

    /**
     * Create a standardized key for notification queue metrics.
     * 
     * @param queueName the queue name
     * @return the metric key
     */
    public static MetricKey notificationQueue(String queueName) {
        return new SimpleMetricKey("notification-queue", Map.of("queueName", queueName),
                Set.of("counts", "notification-stats"));
    }

    /**
     * Create a standardized key for notification service health metrics.
     * 
     * @param serviceId the notification service ID
     * @return the metric key
     */
    public static MetricKey notificationServiceHealth(String serviceId) {
        return new SimpleMetricKey("notification-service-health", Map.of("serviceId", serviceId),
                Set.of("notification-stats"));
    }

    /**
     * Create a standardized key for notification throughput metrics.
     * 
     * @param serviceId the notification service ID
     * @return the metric key
     */
    public static MetricKey notificationThroughput(String serviceId) {
        return new SimpleMetricKey("notification-throughput", Map.of("serviceId", serviceId),
                Set.of("notification-stats"));
    }

    // ===== Card Building Metric Keys =====

    /**
     * Create a standardized key for agent card building metrics.
     * 
     * @param agentId the agent ID
     * @param cardType the type of card being built
     * @return the metric key
     */
    public static MetricKey cardBuilding(String agentId, String cardType) {
        return new SimpleMetricKey("card-building", Map.of("agentId", agentId, "cardType", cardType),
                Set.of("counts", "latency", "card-building-stats"));
    }

    /**
     * Create a standardized key for template processing metrics.
     * 
     * @param templateType the type of template
     * @return the metric key
     */
    public static MetricKey templateProcessing(String templateType) {
        return new SimpleMetricKey("template-processing", Map.of("templateType", templateType),
                Set.of("counts", "latency", "card-building-stats"));
    }

    /**
     * Create a standardized key for skill discovery metrics.
     * 
     * @param agentId the agent ID
     * @return the metric key
     */
    public static MetricKey skillDiscovery(String agentId) {
        return new SimpleMetricKey("skill-discovery", Map.of("agentId", agentId),
                Set.of("counts", "latency", "card-building-stats"));
    }

    /**
     * Create a standardized key for card validation metrics.
     * 
     * @param validationType the type of validation
     * @return the metric key
     */
    public static MetricKey cardValidation(String validationType) {
        return new SimpleMetricKey("card-validation", Map.of("validationType", validationType),
                Set.of("counts", "latency", "card-building-stats"));
    }

    /**
     * Create a standardized key for card building cache metrics.
     * 
     * @param cacheType the type of cache
     * @return the metric key
     */
    public static MetricKey cardBuildingCache(String cacheType) {
        return new SimpleMetricKey("card-building-cache", Map.of("cacheType", cacheType),
                Set.of("counts", "card-building-stats"));
    }

    /**
     * Create a standardized key for card building efficiency metrics.
     * 
     * @param agentId the agent ID
     * @return the metric key
     */
    public static MetricKey cardBuildingEfficiency(String agentId) {
        return new SimpleMetricKey("card-building-efficiency", Map.of("agentId", agentId),
                Set.of("card-building-stats"));
    }

    // ===== Enhanced Statistics Metric Keys =====

    /**
     * Create a standardized key for time-series statistics.
     * 
     * @param domain the domain for time-series analysis
     * @param operation the operation being analyzed
     * @return the metric key
     */
    public static MetricKey timeSeriesStatistics(String domain, String operation) {
        return new SimpleMetricKey("time-series-stats", Map.of("domain", domain, "operation", operation),
                Set.of("time-series", "trends", "percentiles"));
    }

    /**
     * Create a standardized key for cross-domain aggregated statistics.
     * 
     * @param aggregationType the type of cross-domain aggregation
     * @param domains the domains being aggregated (comma-separated)
     * @return the metric key
     */
    public static MetricKey crossDomainStatistics(String aggregationType, String domains) {
        return new SimpleMetricKey("cross-domain-stats", Map.of("aggregationType", aggregationType, "domains", domains),
                Set.of("cross-domain", "aggregated", "multi-domain"));
    }

    /**
     * Create a standardized key for business logic statistics.
     * 
     * @param businessDomain the business domain
     * @param logicType the type of business logic
     * @return the metric key
     */
    public static MetricKey businessLogicStatistics(String businessDomain, String logicType) {
        return new SimpleMetricKey("business-logic-stats",
                Map.of("businessDomain", businessDomain, "logicType", logicType),
                Set.of("business-logic", "domain-specific", "calculated"));
    }

    /**
     * Create a standardized key for enhanced percentile statistics.
     * 
     * @param domain the domain for percentile analysis
     * @param metricType the type of metric being analyzed
     * @return the metric key
     */
    public static MetricKey enhancedPercentileStatistics(String domain, String metricType) {
        return new SimpleMetricKey("enhanced-percentile-stats", Map.of("domain", domain, "metricType", metricType),
                Set.of("percentiles", "statistical", "advanced"));
    }

    /**
     * Create a standardized key for multi-source statistics.
     * 
     * @param sourceType the type of source aggregation
     * @param sourceCount the number of sources
     * @return the metric key
     */
    public static MetricKey multiSourceStatistics(String sourceType, int sourceCount) {
        return new SimpleMetricKey("multi-source-stats",
                Map.of("sourceType", sourceType, "sourceCount", String.valueOf(sourceCount)),
                Set.of("multi-source", "aggregated", "combined"));
    }

    /**
     * Create a standardized key for trend analysis statistics.
     * 
     * @param trendType the type of trend analysis
     * @param timeWindow the time window for trend analysis
     * @return the metric key
     */
    public static MetricKey trendAnalysisStatistics(String trendType, String timeWindow) {
        return new SimpleMetricKey("trend-analysis-stats", Map.of("trendType", trendType, "timeWindow", timeWindow),
                Set.of("trends", "analysis", "time-series"));
    }

    /**
     * Create a standardized key for correlation statistics.
     * 
     * @param correlationType the type of correlation analysis
     * @param variables the variables being correlated (comma-separated)
     * @return the metric key
     */
    public static MetricKey correlationStatistics(String correlationType, String variables) {
        return new SimpleMetricKey("correlation-stats",
                Map.of("correlationType", correlationType, "variables", variables),
                Set.of("correlation", "statistical", "relationships"));
    }

    /**
     * Create a standardized key for anomaly detection statistics.
     * 
     * @param domain the domain for anomaly detection
     * @param detectionMethod the method used for anomaly detection
     * @return the metric key
     */
    public static MetricKey anomalyDetectionStatistics(String domain, String detectionMethod) {
        return new SimpleMetricKey("anomaly-detection-stats",
                Map.of("domain", domain, "detectionMethod", detectionMethod),
                Set.of("anomaly", "detection", "outliers"));
    }

    /**
     * Create a standardized key for predictive statistics.
     * 
     * @param predictionType the type of prediction
     * @param horizon the prediction horizon
     * @return the metric key
     */
    public static MetricKey predictiveStatistics(String predictionType, String horizon) {
        return new SimpleMetricKey("predictive-stats", Map.of("predictionType", predictionType, "horizon", horizon),
                Set.of("predictive", "forecasting", "future"));
    }

    /**
     * Create a standardized key for composite statistics.
     * 
     * @param compositeType the type of composite analysis
     * @param components the components being combined (comma-separated)
     * @return the metric key
     */
    public static MetricKey compositeStatistics(String compositeType, String components) {
        return new SimpleMetricKey("composite-stats", Map.of("compositeType", compositeType, "components", components),
                Set.of("composite", "combined", "multi-dimensional"));
    }
}
