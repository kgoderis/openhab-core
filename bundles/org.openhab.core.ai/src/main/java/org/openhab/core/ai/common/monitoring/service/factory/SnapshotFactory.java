package org.openhab.core.ai.common.monitoring.service.factory;

import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.Counts;
import org.openhab.core.ai.common.monitoring.api.MetricKey;
import org.openhab.core.ai.common.monitoring.api.MetricsSnapshot;
import org.openhab.core.ai.common.monitoring.api.Timing;
import org.openhab.core.ai.common.monitoring.collector.MetricsCollector;
import org.openhab.core.ai.common.monitoring.service.snapshot.ActionExecutionSnapshot;
import org.openhab.core.ai.common.monitoring.service.snapshot.AgentModelSnapshot;
import org.openhab.core.ai.common.monitoring.service.snapshot.AgentTaskSnapshot;
import org.openhab.core.ai.common.monitoring.service.snapshot.DomainAggregatedSnapshot;
import org.openhab.core.ai.common.monitoring.service.snapshot.ErrorRecoverySnapshot;
import org.openhab.core.ai.common.monitoring.service.snapshot.GenericMetricsSnapshot;
import org.openhab.core.ai.common.monitoring.service.snapshot.ModelCompletionSnapshot;
import org.openhab.core.ai.common.monitoring.service.snapshot.SecurityMonitoringSnapshot;
import org.openhab.core.ai.common.monitoring.service.snapshot.ToolExecutionSnapshot;
import org.openhab.core.ai.common.monitoring.service.snapshot.ToolFileReadSnapshot;
import org.openhab.core.ai.common.monitoring.snapshot.ExecutionMetricsSnapshot;
import org.openhab.core.ai.common.security.MessageSecurityStatistics;
import org.openhab.core.ai.tool.monitoring.MonitoringStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for creating typed snapshots from raw collector data.
 * 
 * <p>
 * This factory provides a centralized way to create any type of snapshot
 * from a MetricsCollector and MetricKey. It handles the extraction of
 * raw data and construction of appropriate snapshot types.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class SnapshotFactory {

    private static final Logger logger = LoggerFactory.getLogger(SnapshotFactory.class);

    private SnapshotFactory() {
        // Utility class - prevent instantiation
    }

    /**
     * Create a typed snapshot from collector data.
     * 
     * @param <T> the snapshot type
     * @param collector the metrics collector
     * @param snapshotType the class of the snapshot type
     * @param key the metric key
     * @return the created snapshot
     * @throws IllegalArgumentException if the snapshot type is not supported
     */
    @SuppressWarnings("unchecked")
    public static <T extends MetricsSnapshot> T createSnapshot(MetricsCollector collector, Class<T> snapshotType,
            MetricKey key) {

        Objects.requireNonNull(collector, "collector");
        Objects.requireNonNull(snapshotType, "snapshotType");
        Objects.requireNonNull(key, "key");

        // Create base execution snapshot from collector data
        ExecutionMetricsSnapshot baseSnapshot = createExecutionSnapshot(collector);

        // Route to appropriate snapshot creation method
        if (snapshotType == ExecutionMetricsSnapshot.class) {
            return (T) baseSnapshot;
        } else if (snapshotType == GenericMetricsSnapshot.class) {
            return (T) createGenericSnapshot(collector, key);
        } else if (snapshotType == ModelCompletionSnapshot.class) {
            return (T) createModelCompletionSnapshot(collector, baseSnapshot);
        } else if (snapshotType == ToolFileReadSnapshot.class) {
            return (T) createToolFileReadSnapshot(collector, baseSnapshot);
        } else if (snapshotType == AgentTaskSnapshot.class) {
            return (T) createAgentTaskSnapshot(collector, baseSnapshot);
        } else if (snapshotType == AgentModelSnapshot.class) {
            return (T) createAgentModelSnapshot(collector, baseSnapshot, key);
        } else if (snapshotType == ActionExecutionSnapshot.class) {
            return (T) createActionExecutionSnapshot(collector, baseSnapshot, key);
        } else if (snapshotType == ToolExecutionSnapshot.class) {
            return (T) createToolExecutionSnapshot(collector, baseSnapshot, key);
        } else if (snapshotType == ErrorRecoverySnapshot.class) {
            return (T) createErrorRecoverySnapshot(collector, baseSnapshot, key);
        } else if (snapshotType == SecurityMonitoringSnapshot.class) {
            return (T) createSecurityMonitoringSnapshot(collector, baseSnapshot, key);
        } else if (snapshotType == DomainAggregatedSnapshot.class) {
            return (T) createDomainAggregatedSnapshot(collector, baseSnapshot, key);
        } else if (snapshotType == MonitoringStatistics.class) {
            return (T) createMonitoringStatistics(collector, baseSnapshot);
        } else if (snapshotType == MessageSecurityStatistics.class) {
            return (T) createMessageSecurityStatistics(collector, baseSnapshot);
        } else {
            throw new IllegalArgumentException("Unsupported snapshot type: " + snapshotType.getSimpleName());
        }
    }

    /**
     * Create an empty snapshot of the specified type.
     * 
     * @param <T> the snapshot type
     * @param snapshotType the class of the snapshot type
     * @param key the metric key (for domain-specific snapshots)
     * @return an empty snapshot
     */
    @SuppressWarnings("unchecked")
    public static <T extends MetricsSnapshot> T createEmptySnapshot(Class<T> snapshotType, MetricKey key) {
        Objects.requireNonNull(snapshotType, "snapshotType");

        if (snapshotType == ExecutionMetricsSnapshot.class) {
            return (T) ExecutionMetricsSnapshot.builder().build();
        } else if (snapshotType == GenericMetricsSnapshot.class) {
            String domain = key.labels().getOrDefault("domain", "unknown");
            String operation = key.labels().getOrDefault("operation", "unknown");
            return (T) GenericMetricsSnapshot.builder(domain, operation).build();
        } else if (snapshotType == ModelCompletionSnapshot.class) {
            return (T) new ModelCompletionSnapshot(new Counts(0, 0, 0), new Timing(0), System.currentTimeMillis(), 0,
                    0.0);
        } else if (snapshotType == ToolFileReadSnapshot.class) {
            return (T) new ToolFileReadSnapshot(new Counts(0, 0, 0), new Timing(0), System.currentTimeMillis(), 0, 0);
        } else if (snapshotType == AgentTaskSnapshot.class) {
            return (T) new AgentTaskSnapshot(new Counts(0, 0, 0), new Timing(0), System.currentTimeMillis(), 0.0, 0.0);
        } else if (snapshotType == AgentModelSnapshot.class) {
            String modelId = extractModelId(key);
            return (T) AgentModelSnapshot.empty(modelId, "unknown");
        } else if (snapshotType == ActionExecutionSnapshot.class) {
            String actionId = extractActionId(key);
            return (T) ActionExecutionSnapshot.empty(actionId);
        } else if (snapshotType == ToolExecutionSnapshot.class) {
            String toolId = extractToolId(key);
            return (T) ToolExecutionSnapshot.empty(toolId);
        } else if (snapshotType == ErrorRecoverySnapshot.class) {
            return (T) ErrorRecoverySnapshot.empty();
        } else if (snapshotType == SecurityMonitoringSnapshot.class) {
            String securityId = extractSecurityId(key);
            return (T) SecurityMonitoringSnapshot.empty(securityId);
        } else if (snapshotType == DomainAggregatedSnapshot.class) {
            String domain = key.labels().getOrDefault("domain", "unknown");
            return (T) DomainAggregatedSnapshot.empty(domain);
        } else if (snapshotType == MonitoringStatistics.class) {
            return (T) MonitoringStatistics.empty(java.time.Duration.ZERO);
        } else if (snapshotType == MessageSecurityStatistics.class) {
            return (T) MessageSecurityStatistics.empty(java.time.Duration.ZERO);
        } else {
            throw new IllegalArgumentException("Unsupported snapshot type: " + snapshotType.getSimpleName());
        }
    }

    // ===== Private Creation Methods =====

    /**
     * Create base execution snapshot from collector data.
     */
    private static ExecutionMetricsSnapshot createExecutionSnapshot(MetricsCollector collector) {
        return ExecutionMetricsSnapshot.builder().withTotal(collector.getTotal()).withSuccess(collector.getSuccess())
                .withFailure(collector.getFailure()).withTotalDurationNanos(collector.getTotalDurationNanos())
                .withHealthStatus(collector.getHealthStatus()).withStatusMessage(collector.getStatusMessage())
                .withLastFailureTime(collector.getLastFailureTime()).withLastSuccessTime(collector.getLastSuccessTime())
                .withLastError(collector.getLastError()).withConsecutiveFailures(collector.getConsecutiveFailures())
                .build();
    }

    /**
     * Create generic metrics snapshot with all collector data.
     */
    private static GenericMetricsSnapshot createGenericSnapshot(MetricsCollector collector, MetricKey key) {
        String domain = key.labels().getOrDefault("domain", "unknown");
        String operation = key.labels().getOrDefault("operation", "unknown");

        GenericMetricsSnapshot.Builder builder = GenericMetricsSnapshot.builder(domain, operation)
                .withMetric("total_count", collector.getTotal()).withMetric("success_count", collector.getSuccess())
                .withMetric("failure_count", collector.getFailure())
                .withMetric("total_duration_ms", collector.getTotalDurationNanos() / 1_000_000)
                .withMetric("average_duration_ms",
                        collector.getTotal() > 0
                                ? (collector.getTotalDurationNanos() / collector.getTotal()) / 1_000_000
                                : 0)
                .withMetric("success_rate", collector.getSuccessRate())
                .withMetric("failure_rate", 1.0 - collector.getSuccessRate())
                .withMetric("health_status", collector.getHealthStatus().name())
                .withMetric("status_message", collector.getStatusMessage())
                .withMetric("consecutive_failures", collector.getConsecutiveFailures())
                .withMetric("last_failure_time_ms", collector.getLastFailureTime())
                .withMetric("last_success_time_ms", collector.getLastSuccessTime())
                .withMetric("last_error", collector.getLastError());

        // Add all extended data
        Map<String, Object> extendedData = collector.getExtendedData();
        if (!extendedData.isEmpty()) {
            builder.withMetrics(extendedData);
        }

        return builder.build();
    }

    /**
     * Create model completion snapshot.
     */
    private static ModelCompletionSnapshot createModelCompletionSnapshot(MetricsCollector collector,
            ExecutionMetricsSnapshot baseSnapshot) {

        // Extract model-specific data
        Integer inputTokens = collector.getExtendedData("inputTokens", Integer.class);
        Integer outputTokens = collector.getExtendedData("outputTokens", Integer.class);
        Double cost = collector.getExtendedData("cost", Double.class);

        long totalTokens = (inputTokens != null ? inputTokens : 0) + (outputTokens != null ? outputTokens : 0);
        double totalCost = cost != null ? cost : 0.0;

        return new ModelCompletionSnapshot(baseSnapshot.counts(), baseSnapshot.timing(), baseSnapshot.getTimestampMs(),
                totalTokens, totalCost);
    }

    /**
     * Create tool file read snapshot.
     */
    private static ToolFileReadSnapshot createToolFileReadSnapshot(MetricsCollector collector,
            ExecutionMetricsSnapshot baseSnapshot) {

        // Extract file-specific data
        Long fileSize = collector.getExtendedData("fileSize", Long.class);
        String fileType = collector.getExtendedData("fileType", String.class);

        long totalBytesRead = fileSize != null ? fileSize : 0L;
        long totalFilesRead = baseSnapshot.success(); // Assume one file per successful operation

        return new ToolFileReadSnapshot(baseSnapshot.counts(), baseSnapshot.timing(), baseSnapshot.getTimestampMs(),
                totalBytesRead, totalFilesRead);
    }

    /**
     * Create agent task snapshot.
     */
    private static AgentTaskSnapshot createAgentTaskSnapshot(MetricsCollector collector,
            ExecutionMetricsSnapshot baseSnapshot) {

        // Extract agent-specific data
        Double decisionAccuracy = collector.getExtendedData("decisionAccuracy", Double.class);
        Double learningRate = collector.getExtendedData("learningRate", Double.class);

        double avgDecisionAccuracy = decisionAccuracy != null ? decisionAccuracy : 0.0;
        double avgLearningRate = learningRate != null ? learningRate : 0.0;

        return new AgentTaskSnapshot(baseSnapshot.counts(), baseSnapshot.timing(), baseSnapshot.getTimestampMs(),
                avgDecisionAccuracy, avgLearningRate);
    }

    /**
     * Create agent model snapshot.
     */
    private static AgentModelSnapshot createAgentModelSnapshot(MetricsCollector collector,
            ExecutionMetricsSnapshot baseSnapshot, MetricKey key) {

        String modelId = extractModelId(key);

        // Extract model-specific data
        String modelType = collector.getExtendedData("modelType", String.class);
        Double accuracy = collector.getExtendedData("accuracy", Double.class);
        Double performance = collector.getExtendedData("performance", Double.class);

        modelType = modelType != null ? modelType : "unknown";
        double avgAccuracy = accuracy != null ? accuracy : 0.0;
        double avgPerformance = performance != null ? performance : 0.0;

        Map<String, Long> operationCounts = Map.of("model-operation", baseSnapshot.total());
        Map<String, Long> errorCounts = Map.of("model-error", baseSnapshot.failure());

        return new AgentModelSnapshot(baseSnapshot.counts(), baseSnapshot.timing(), baseSnapshot.getTimestampMs(),
                modelId, modelType, avgAccuracy, avgPerformance, operationCounts, errorCounts, null, null);
    }

    /**
     * Create action execution snapshot.
     */
    private static ActionExecutionSnapshot createActionExecutionSnapshot(MetricsCollector collector,
            ExecutionMetricsSnapshot baseSnapshot, MetricKey key) {

        String actionId = extractActionId(key);

        // Extract action-specific data
        Long inputDataSize = collector.getExtendedData("inputDataSize", Long.class);
        Long outputDataSize = collector.getExtendedData("outputDataSize", Long.class);
        Integer priority = collector.getExtendedData("priority", Integer.class);

        long inputSize = inputDataSize != null ? inputDataSize : 0L;
        long outputSize = outputDataSize != null ? outputDataSize : 0L;
        int actionPriority = priority != null ? priority : 10;

        return new ActionExecutionSnapshot(baseSnapshot.counts(), baseSnapshot.timing(), baseSnapshot.getTimestampMs(),
                actionId, inputSize, outputSize, baseSnapshot.failure(), actionPriority, null);
    }

    /**
     * Create tool execution snapshot.
     */
    private static ToolExecutionSnapshot createToolExecutionSnapshot(MetricsCollector collector,
            ExecutionMetricsSnapshot baseSnapshot, MetricKey key) {

        String toolId = extractToolId(key);

        // Extract tool-specific data
        Double accuracy = collector.getExtendedData("accuracy", Double.class);
        Double reliability = collector.getExtendedData("reliability", Double.class);
        Long dataProcessed = collector.getExtendedData("dataProcessed", Long.class);

        double toolAccuracy = accuracy != null ? accuracy : 0.0;
        double toolReliability = reliability != null ? reliability : 0.0;
        long processedData = dataProcessed != null ? dataProcessed : 0L;

        return new ToolExecutionSnapshot(baseSnapshot.counts(), baseSnapshot.timing(), baseSnapshot.getTimestampMs(),
                toolId, toolAccuracy, toolReliability, processedData);
    }

    /**
     * Create error recovery snapshot.
     */
    private static ErrorRecoverySnapshot createErrorRecoverySnapshot(MetricsCollector collector,
            ExecutionMetricsSnapshot baseSnapshot, MetricKey key) {

        // Extract error recovery-specific data
        String recoveryStrategy = collector.getExtendedData("recoveryStrategy", String.class);
        Boolean fallbackUsed = collector.getExtendedData("fallbackUsed", Boolean.class);

        String strategy = recoveryStrategy != null ? recoveryStrategy : "unknown";
        boolean fallback = fallbackUsed != null ? fallbackUsed : false;

        // Create error counts by type and recovery counts by strategy maps
        Map<String, Long> errorCountsByType = Map.of("error", baseSnapshot.total());
        Map<String, Long> recoveryCountsByStrategy = Map.of(strategy, baseSnapshot.success());

        return new ErrorRecoverySnapshot(baseSnapshot.counts(), baseSnapshot.timing(), baseSnapshot.getTimestampMs(),
                baseSnapshot.total(), baseSnapshot.success(), fallback ? baseSnapshot.success() : 0L,
                baseSnapshot.failure(), baseSnapshot.total(), errorCountsByType, recoveryCountsByStrategy);
    }

    /**
     * Create security monitoring snapshot.
     */
    private static SecurityMonitoringSnapshot createSecurityMonitoringSnapshot(MetricsCollector collector,
            ExecutionMetricsSnapshot baseSnapshot, MetricKey key) {

        String securityId = extractSecurityId(key);

        // Extract security-specific data
        Long securityViolations = collector.getExtendedData("securityViolations", Long.class);
        Long activeClients = collector.getExtendedData("activeClients", Long.class);
        Long blockedClients = collector.getExtendedData("blockedClients", Long.class);
        Boolean authenticationEnabled = collector.getExtendedData("authenticationEnabled", Boolean.class);
        Boolean requestValidationEnabled = collector.getExtendedData("requestValidationEnabled", Boolean.class);
        Integer maxConnections = collector.getExtendedData("maxConnections", Integer.class);
        Integer requestRateLimit = collector.getExtendedData("requestRateLimit", Integer.class);
        Integer sessionTimeoutMinutes = collector.getExtendedData("sessionTimeoutMinutes", Integer.class);

        long violations = securityViolations != null ? securityViolations : 0L;
        int active = activeClients != null ? activeClients.intValue() : (int) baseSnapshot.success();
        int blocked = blockedClients != null ? blockedClients.intValue() : (int) baseSnapshot.failure();
        boolean authEnabled = authenticationEnabled != null ? authenticationEnabled : true;
        boolean validationEnabled = requestValidationEnabled != null ? requestValidationEnabled : true;
        int maxConn = maxConnections != null ? maxConnections : 100;
        int rateLimit = requestRateLimit != null ? requestRateLimit : 60;
        int timeout = sessionTimeoutMinutes != null ? sessionTimeoutMinutes : 30;

        return new SecurityMonitoringSnapshot(
                new SecurityMonitoringSnapshot.Counts(baseSnapshot.total(), baseSnapshot.success(),
                        baseSnapshot.failure()),
                new SecurityMonitoringSnapshot.Timing(baseSnapshot.totalDurationNanos()), baseSnapshot.getTimestampMs(),
                violations, active, blocked, authEnabled, validationEnabled, maxConn, rateLimit, timeout);
    }

    /**
     * Create domain aggregated snapshot.
     */
    private static DomainAggregatedSnapshot createDomainAggregatedSnapshot(MetricsCollector collector,
            ExecutionMetricsSnapshot baseSnapshot, MetricKey key) {

        String domain = key.labels().getOrDefault("domain", "unknown");

        // For domain aggregation, we'd typically need multiple collectors
        // This is a simplified version that just uses the single collector
        double averageSuccessRate = baseSnapshot.total() > 0 ? (double) baseSnapshot.success() / baseSnapshot.total()
                : 0.0;

        return new DomainAggregatedSnapshot(domain, baseSnapshot.total(), baseSnapshot.success(),
                baseSnapshot.failure(), baseSnapshot.totalDurationNanos(), averageSuccessRate,
                java.util.List.of(baseSnapshot), java.time.Instant.now());
    }

    /**
     * Create monitoring statistics.
     */
    private static MonitoringStatistics createMonitoringStatistics(MetricsCollector collector,
            ExecutionMetricsSnapshot baseSnapshot) {

        // Extract monitoring-specific data
        Integer metricsCollected = collector.getExtendedData("metricsCollected", Integer.class);
        Integer alertsGenerated = collector.getExtendedData("alertsGenerated", Integer.class);
        String metricType = collector.getExtendedData("metricType", String.class);
        String alertSeverity = collector.getExtendedData("alertSeverity", String.class);

        int totalMetrics = metricsCollected != null ? metricsCollected : 0;
        int totalAlerts = alertsGenerated != null ? alertsGenerated : 0;
        String type = metricType != null ? metricType : "unknown";
        String severity = alertSeverity != null ? alertSeverity : "unknown";

        // Create metrics by type and alerts by severity maps
        Map<String, Long> metricsByType = Map.of(type, (long) totalMetrics);
        Map<String, Long> alertsBySeverity = Map.of(severity, (long) totalAlerts);

        return new MonitoringStatistics(totalMetrics, totalAlerts, metricsByType, alertsBySeverity,
                baseSnapshot.getTimestampMs(), java.time.Duration.ZERO, System.currentTimeMillis());
    }

    /**
     * Create message security statistics.
     */
    private static MessageSecurityStatistics createMessageSecurityStatistics(MetricsCollector collector,
            ExecutionMetricsSnapshot baseSnapshot) {

        // For now, return empty statistics since we need more specific data
        return MessageSecurityStatistics.empty(java.time.Duration.ZERO);
    }

    // ===== Helper Methods for ID Extraction =====

    private static String extractModelId(MetricKey key) {
        return key.labels().getOrDefault("modelId", "unknown");
    }

    private static String extractActionId(MetricKey key) {
        return key.labels().getOrDefault("actionId", "unknown");
    }

    private static String extractToolId(MetricKey key) {
        return key.labels().getOrDefault("toolId", "unknown");
    }

    private static String extractSecurityId(MetricKey key) {
        return key.labels().getOrDefault("securityId", "unknown");
    }
}
