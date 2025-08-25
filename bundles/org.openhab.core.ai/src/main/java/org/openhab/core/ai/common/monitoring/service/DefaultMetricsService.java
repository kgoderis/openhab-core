package org.openhab.core.ai.common.monitoring.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.Counts;
import org.openhab.core.ai.common.monitoring.api.MetricKey;
import org.openhab.core.ai.common.monitoring.api.MetricKeys;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.api.MetricsSnapshot;
import org.openhab.core.ai.common.monitoring.api.Timing;
import org.openhab.core.ai.common.monitoring.collector.MetricsCollector;
import org.openhab.core.ai.common.monitoring.registry.MonitoringRegistry;
import org.openhab.core.ai.common.monitoring.service.snapshot.ActionExecutionSnapshot;
import org.openhab.core.ai.common.monitoring.service.snapshot.AgentModelSnapshot;
import org.openhab.core.ai.common.monitoring.service.snapshot.AgentTaskSnapshot;
import org.openhab.core.ai.common.monitoring.service.snapshot.CommunicationSnapshot;
import org.openhab.core.ai.common.monitoring.service.snapshot.DomainAggregatedSnapshot;
import org.openhab.core.ai.common.monitoring.service.snapshot.ErrorRecoverySnapshot;
import org.openhab.core.ai.common.monitoring.service.snapshot.ModelCompletionSnapshot;
import org.openhab.core.ai.common.monitoring.service.snapshot.SecurityMonitoringSnapshot;
import org.openhab.core.ai.common.monitoring.service.snapshot.ToolExecutionSnapshot;
import org.openhab.core.ai.common.monitoring.service.snapshot.ToolFileReadSnapshot;
import org.openhab.core.ai.common.monitoring.service.statistics.AgentBehaviorStatistics;
import org.openhab.core.ai.common.monitoring.service.statistics.AgentPersistenceStatistics;
import org.openhab.core.ai.common.monitoring.service.statistics.CoordinationStatistics;
import org.openhab.core.ai.common.monitoring.service.statistics.ErrorRecoveryStatistics;
import org.openhab.core.ai.common.monitoring.service.statistics.ModelCompletionStatistics;
import org.openhab.core.ai.common.monitoring.service.statistics.OptimizationStatistics;
import org.openhab.core.ai.common.monitoring.service.statistics.ReasoningPerformanceStatistics;
import org.openhab.core.ai.common.monitoring.service.statistics.SecurityMonitoringStatistics;
import org.openhab.core.ai.common.monitoring.service.statistics.ToolFileReadStatistics;
import org.openhab.core.ai.common.monitoring.snapshot.ExecutionMetricsSnapshot;
import org.openhab.core.ai.common.security.MessageSecurityStatistics;
import org.openhab.core.ai.tool.monitoring.MonitoringStatistics;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the centralized Metrics Service.
 * 
 * <p>
 * This implementation provides:
 * - Basic operation recording with success/failure and timing
 * - Domain-specific convenience methods for models, tools, and agents
 * - Snapshot retrieval for current metrics state
 * - Statistics computation for historical insights
 * - Integration with the existing MonitoringRegistry
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@Component(service = MetricsService.class)
@NonNullByDefault
public class DefaultMetricsService implements MetricsService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultMetricsService.class);

    // Reference to the monitoring registry for unified metrics collection
    @Reference
    private @Nullable MonitoringRegistry monitoringRegistry;

    @Override
    public void recordOperation(String domain, String operation, boolean success, Duration duration) {
        try {
            MonitoringRegistry registry = monitoringRegistry;
            if (registry != null) {
                // Create a metric key for the registry
                MetricKey metricKey = new MetricKeys.SimpleMetricKey(domain + "." + operation,
                        Map.of("domain", domain, "operation", operation));
                MetricsCollector collector = registry.metricsCollector(metricKey);
                collector.recordExecution(success, duration.toNanos());
                logger.debug("Recorded operation in unified collector: {} (success={}, duration={}ns)", metricKey.id(),
                        success, duration.toNanos());
            } else {
                logger.warn("Monitoring registry not available for operation: {}:{}", domain, operation);
            }
        } catch (Exception e) {
            logger.warn("Failed to record operation: {}:{}", domain, operation, e);
        }
    }

    @Override
    public OperationRecorder recordOperation(String domain, String operation) {
        return new OperationRecorder(this, domain, operation);
    }

    @Override
    public void recordOperationWithData(String domain, String operation, boolean success, Duration duration,
            Map<String, Object> data) {
        logger.debug("Recording operation with data: {}:{} (success={}, duration={}, dataSize={})", domain, operation,
                success, duration, data.size());

        MonitoringRegistry registry = monitoringRegistry;
        if (registry != null) {
            try {
                MetricKey metricKey = new MetricKeys.SimpleMetricKey(domain + "." + operation,
                        Map.of("domain", domain, "operation", operation));
                MetricsCollector collector = registry.metricsCollector(metricKey);
                collector.recordExecution(success, duration.toNanos());

                // Record data if provided
                if (!data.isEmpty()) {
                    collector.recordData(data);
                    logger.debug("Recorded data: {}", data);
                }

                logger.debug("Recorded operation with data in unified collector: {} (success={}, duration={}ns)",
                        metricKey.id(), success, duration.toNanos());
            } catch (Exception e) {
                logger.warn("Failed to record operation with data: {}:{}", domain, operation, e);
            }
        } else {
            logger.warn("Monitoring registry not available for operation with data: {}:{}", domain, operation);
        }
    }

    @Override
    public void recordModelCompletion(String modelId, boolean success, Duration duration, int inputTokens,
            int outputTokens, double cost) {
        logger.debug("Recording model completion: {} (success={}, duration={}, input={}, output={}, cost={})", modelId,
                success, duration, inputTokens, outputTokens, cost);

        MonitoringRegistry registry = monitoringRegistry;
        if (registry != null) {
            try {
                MetricKey metricKey = new MetricKeys.SimpleMetricKey("model." + modelId + ".completion",
                        Map.of("domain", "model", "operation", modelId + ":completion"));
                MetricsCollector collector = registry.metricsCollector(metricKey);
                collector.recordExecution(success, duration.toNanos());
                collector.recordData("inputTokens", inputTokens);
                collector.recordData("outputTokens", outputTokens);
                collector.recordData("cost", cost);
                logger.debug(
                        "Recorded model completion with domain data in unified collector: {} (success={}, duration={}ns)",
                        metricKey.id(), success, duration.toNanos());
            } catch (Exception e) {
                logger.warn("Failed to record model completion: {}:{}", modelId, modelId + ":completion", e);
            }
        } else {
            logger.warn("Monitoring registry not available for model completion: {}", modelId);
        }
    }

    @Override
    public void recordToolFileRead(String toolId, boolean success, Duration duration, long fileSize, String fileType) {
        logger.debug("Recording tool file read: {} (success={}, duration={}, fileSize={}, fileType={})", toolId,
                success, duration, fileSize, fileType);

        MonitoringRegistry registry = monitoringRegistry;
        if (registry != null) {
            try {
                MetricKey metricKey = new MetricKeys.SimpleMetricKey("tool." + toolId + ".file_read",
                        Map.of("domain", "tool", "operation", toolId + ":file_read"));
                MetricsCollector collector = registry.metricsCollector(metricKey);
                collector.recordExecution(success, duration.toNanos());
                collector.recordData("fileSize", fileSize);
                collector.recordData("fileType", fileType);
                logger.debug(
                        "Recorded tool file read with domain data in unified collector: {} (success={}, duration={}ns)",
                        metricKey.id(), success, duration.toNanos());
            } catch (Exception e) {
                logger.warn("Failed to record tool file read: {}:{}", toolId, toolId + ":file_read", e);
            }
        } else {
            logger.warn("Monitoring registry not available for tool file read: {}", toolId);
        }
    }

    @Override
    public void recordAgentTask(String agentId, boolean success, Duration duration, String taskType,
            double decisionAccuracy, double learningRate) {
        logger.debug("Recording agent task: {} (success={}, duration={}, taskType={}, accuracy={}, learning={})",
                agentId, success, duration, taskType, decisionAccuracy, learningRate);

        MonitoringRegistry registry = monitoringRegistry;
        if (registry != null) {
            try {
                MetricKey metricKey = new MetricKeys.SimpleMetricKey("agent." + agentId + ".task",
                        Map.of("domain", "agent", "operation", agentId + ":task"));
                MetricsCollector collector = registry.metricsCollector(metricKey);
                collector.recordExecution(success, duration.toNanos());
                collector.recordData("taskType", taskType);
                collector.recordData("decisionAccuracy", decisionAccuracy);
                collector.recordData("learningRate", learningRate);
                logger.debug(
                        "Recorded agent task with domain data in unified collector: {} (success={}, duration={}ns)",
                        metricKey.id(), success, duration.toNanos());
            } catch (Exception e) {
                logger.warn("Failed to record agent task: {}:{}", agentId, agentId + ":task", e);
            }
        } else {
            logger.warn("Monitoring registry not available for agent task: {}", agentId);
        }
    }

    @Override
    public void recordMonitoringOperation(String monitoringId, boolean success, Duration duration, int metricsCollected,
            int alertsGenerated, String metricType, String alertSeverity) {
        logger.debug(
                "Recording monitoring operation: {} (success={}, duration={}, metrics={}, alerts={}, type={}, severity={})",
                monitoringId, success, duration, metricsCollected, alertsGenerated, metricType, alertSeverity);

        MonitoringRegistry registry = monitoringRegistry;
        if (registry != null) {
            try {
                MetricKey metricKey = new MetricKeys.SimpleMetricKey("monitoring." + monitoringId,
                        Map.of("domain", "monitoring", "operation", monitoringId));
                MetricsCollector collector = registry.metricsCollector(metricKey);
                collector.recordExecution(success, duration.toNanos());
                collector.recordData("metricsCollected", metricsCollected);
                collector.recordData("alertsGenerated", alertsGenerated);
                collector.recordData("metricType", metricType);
                collector.recordData("alertSeverity", alertSeverity);
                logger.debug(
                        "Recorded monitoring operation with domain data in unified collector: {} (success={}, duration={}ns)",
                        metricKey.id(), success, duration.toNanos());
            } catch (Exception e) {
                logger.warn("Failed to record monitoring operation: {}:{}", monitoringId, monitoringId, e);
            }
        } else {
            logger.warn("Monitoring registry not available for monitoring operation: {}", monitoringId);
        }
    }

    @Override
    public void recordErrorRecovery(String errorType, boolean success, Duration duration, String recoveryStrategy,
            boolean fallbackUsed) {
        logger.debug("Recording error recovery: {} (success={}, duration={}, strategy={}, fallback={})", errorType,
                success, duration, recoveryStrategy, fallbackUsed);

        MonitoringRegistry registry = monitoringRegistry;
        if (registry != null) {
            try {
                MetricKey metricKey = new MetricKeys.SimpleMetricKey("error-recovery." + errorType,
                        Map.of("domain", "error-recovery", "operation", errorType));
                MetricsCollector collector = registry.metricsCollector(metricKey);
                collector.recordExecution(success, duration.toNanos());
                collector.recordData("recoveryStrategy", recoveryStrategy);
                collector.recordData("fallbackUsed", fallbackUsed);
                logger.debug(
                        "Recorded error recovery with domain data in unified collector: {} (success={}, duration={}ns)",
                        metricKey.id(), success, duration.toNanos());
            } catch (Exception e) {
                logger.warn("Failed to record error recovery: {}:{}", errorType, errorType, e);
            }
        } else {
            logger.warn("Monitoring registry not available for error recovery: {}", errorType);
        }
    }

    @Override
    public ModelCompletionSnapshot getModelCompletionSnapshot(String modelId) {
        logger.debug("Getting model completion snapshot for: {}", modelId);

        MonitoringRegistry registry = monitoringRegistry;
        if (registry == null) {
            logger.warn("Monitoring registry not available for model completion snapshot: {}", modelId);
            return new ModelCompletionSnapshot(new Counts(0, 0, 0), new Timing(0), System.currentTimeMillis(), 0, 0.0);
        }

        try {
            MetricKey metricKey = new MetricKeys.SimpleMetricKey("model." + modelId + ".completion",
                    Map.of("domain", "model", "operation", modelId + ":completion"));
            MetricsCollector collector = registry.metricsCollector(metricKey);
            ExecutionMetricsSnapshot snapshot = collector.executionSnapshot();

            // Extract data from the collector
            long totalTokens = collector.getData("inputTokens", Integer.class) != null
                    ? collector.getData("inputTokens", Integer.class)
                    : 0L;
            totalTokens += collector.getData("outputTokens", Integer.class) != null
                    ? collector.getData("outputTokens", Integer.class)
                    : 0L;

            double totalCost = collector.getData("cost", Double.class) != null ? collector.getData("cost", Double.class)
                    : 0.0;

            return new ModelCompletionSnapshot(snapshot.counts(), snapshot.timing(), snapshot.timestampMs(),
                    totalTokens, totalCost);
        } catch (Exception e) {
            logger.warn("Failed to get model completion snapshot for: {}", modelId, e);
            return new ModelCompletionSnapshot(new Counts(0, 0, 0), new Timing(0), System.currentTimeMillis(), 0, 0.0);
        }
    }

    @Override
    public ToolFileReadSnapshot getToolFileReadSnapshot(String toolId) {
        logger.debug("Getting tool file read snapshot for: {}", toolId);

        MonitoringRegistry registry = monitoringRegistry;
        if (registry == null) {
            logger.warn("Monitoring registry not available for tool file read snapshot: {}", toolId);
            return new ToolFileReadSnapshot(new Counts(0, 0, 0), new Timing(0), System.currentTimeMillis(), 0, 0);
        }

        try {
            MetricKey metricKey = new MetricKeys.SimpleMetricKey("tool." + toolId + ".file_read",
                    Map.of("domain", "tool", "operation", toolId + ":file_read"));
            MetricsCollector collector = registry.metricsCollector(metricKey);
            ExecutionMetricsSnapshot snapshot = collector.executionSnapshot();

            // Extract data from the collector
            long totalBytesRead = collector.getData("fileSize", Long.class) != null
                    ? collector.getData("fileSize", Long.class)
                    : 0L;

            long totalFilesRead = snapshot.success(); // Assume one file per successful operation

            return new ToolFileReadSnapshot(snapshot.counts(), snapshot.timing(), snapshot.timestampMs(),
                    totalBytesRead, totalFilesRead);
        } catch (Exception e) {
            logger.warn("Failed to get tool file read snapshot for: {}", toolId, e);
            return new ToolFileReadSnapshot(new Counts(0, 0, 0), new Timing(0), System.currentTimeMillis(), 0, 0);
        }
    }

    @Override
    public AgentTaskSnapshot getAgentTaskSnapshot(String agentId) {
        logger.debug("Getting agent task snapshot for: {}", agentId);

        MonitoringRegistry registry = monitoringRegistry;
        if (registry == null) {
            logger.warn("Monitoring registry not available for agent task snapshot: {}", agentId);
            return new AgentTaskSnapshot(new Counts(0, 0, 0), new Timing(0), System.currentTimeMillis(), 0.0, 0.0);
        }

        try {
            MetricKey metricKey = new MetricKeys.SimpleMetricKey("agent." + agentId + ".task",
                    Map.of("domain", "agent", "operation", agentId + ":task"));
            MetricsCollector collector = registry.metricsCollector(metricKey);
            ExecutionMetricsSnapshot snapshot = collector.executionSnapshot();

            // Extract data from the collector
            double avgDecisionAccuracy = collector.getData("decisionAccuracy", Double.class) != null
                    ? collector.getData("decisionAccuracy", Double.class)
                    : 0.0;

            double avgLearningRate = collector.getData("learningRate", Double.class) != null
                    ? collector.getData("learningRate", Double.class)
                    : 0.0;

            return new AgentTaskSnapshot(snapshot.counts(), snapshot.timing(), snapshot.timestampMs(),
                    avgDecisionAccuracy, avgLearningRate);
        } catch (Exception e) {
            logger.warn("Failed to get agent task snapshot for: {}", agentId, e);
            return new AgentTaskSnapshot(new Counts(0, 0, 0), new Timing(0), System.currentTimeMillis(), 0.0, 0.0);
        }
    }

    @Override
    public ModelCompletionStatistics getModelCompletionStatistics(String modelId, Duration timeRange) {
        logger.debug("Getting model completion statistics for: {} (timeRange={})", modelId, timeRange);

        // For now, return statistics based on current snapshot since historical data is not yet stored
        // In a future enhancement, the MonitoringRegistry could be extended to support historical snapshots
        ModelCompletionSnapshot currentSnapshot = getModelCompletionSnapshot(modelId);
        List<ModelCompletionSnapshot> snapshots = List.of(currentSnapshot);

        return new ModelCompletionStatistics(snapshots, timeRange, System.currentTimeMillis());
    }

    @Override
    public ToolFileReadStatistics getToolFileReadStatistics(String toolId, Duration timeRange) {
        logger.debug("Getting tool file read statistics for: {} (timeRange={})", toolId, timeRange);

        // For now, return statistics based on current snapshot since historical data is not yet stored
        // In a future enhancement, the MonitoringRegistry could be extended to support historical snapshots
        ToolFileReadSnapshot currentSnapshot = getToolFileReadSnapshot(toolId);
        List<ToolFileReadSnapshot> snapshots = List.of(currentSnapshot);

        return ToolFileReadStatistics.fromSnapshots(toolId, timeRange, snapshots);
    }

    @Override
    public AgentBehaviorStatistics getAgentBehaviorStatistics(String agentId, Duration timeRange) {
        logger.debug("Getting agent behavior statistics for: {} (timeRange={})", agentId, timeRange);

        // For now, return statistics based on current snapshot since historical data is not yet stored
        // In a future enhancement, the MonitoringRegistry could be extended to support historical snapshots
        AgentTaskSnapshot currentSnapshot = getAgentTaskSnapshot(agentId);
        List<AgentTaskSnapshot> snapshots = List.of(currentSnapshot);

        return AgentBehaviorStatistics.fromSnapshots(snapshots, timeRange);
    }

    @Override
    public AgentPersistenceStatistics getAgentPersistenceStatistics(String agentId, Duration timeRange) {
        logger.debug("Getting agent persistence statistics for: {} (timeRange={})", agentId, timeRange);

        // For now, return statistics based on current snapshot since historical data is not yet stored
        // In a future enhancement, the MonitoringRegistry could be extended to support historical snapshots
        DomainAggregatedSnapshot currentSnapshot = getDomainAggregatedSnapshot("agent-persistence");
        List<DomainAggregatedSnapshot> snapshots = List.of(currentSnapshot);

        return AgentPersistenceStatistics.fromSnapshots(snapshots, timeRange);
    }

    @Override
    public MonitoringStatistics getMonitoringStatistics(String monitoringId, Duration timeRange) {
        logger.debug("Getting monitoring statistics for: {} (timeRange={})", monitoringId, timeRange);

        // For now, return statistics based on current snapshot since historical data is not yet stored
        // In a future enhancement, the MonitoringRegistry could be extended to support historical snapshots
        MonitoringRegistry registry = monitoringRegistry;
        if (registry == null) {
            logger.warn("Monitoring registry not available for monitoring statistics: {}", monitoringId);
            return MonitoringStatistics.empty(timeRange);
        }

        try {
            MetricKey metricKey = new MetricKeys.SimpleMetricKey("monitoring." + monitoringId,
                    Map.of("domain", "monitoring", "operation", monitoringId));
            MetricsCollector collector = registry.metricsCollector(metricKey);
            ExecutionMetricsSnapshot snapshot = collector.executionSnapshot();

            // Extract monitoring-specific data from the collector
            int totalMetricsCollected = collector.getData("metricsCollected", Integer.class) != null
                    ? collector.getData("metricsCollected", Integer.class)
                    : 0;
            int totalAlertsGenerated = collector.getData("alertsGenerated", Integer.class) != null
                    ? collector.getData("alertsGenerated", Integer.class)
                    : 0;
            String metricType = collector.getData("metricType", String.class) != null
                    ? collector.getData("metricType", String.class)
                    : "unknown";
            String alertSeverity = collector.getData("alertSeverity", String.class) != null
                    ? collector.getData("alertSeverity", String.class)
                    : "unknown";

            // Create metrics by type and alerts by severity maps
            Map<String, Long> metricsByType = Map.of(metricType, (long) totalMetricsCollected);
            Map<String, Long> alertsBySeverity = Map.of(alertSeverity, (long) totalAlertsGenerated);

            return new MonitoringStatistics(totalMetricsCollected, totalAlertsGenerated, metricsByType,
                    alertsBySeverity, snapshot.timestampMs(), timeRange, System.currentTimeMillis());
        } catch (Exception e) {
            logger.warn("Failed to get monitoring statistics for: {}", monitoringId, e);
            return MonitoringStatistics.empty(timeRange);
        }
    }

    @Override
    public <T extends MetricsSnapshot> List<T> getAllSnapshots(Class<T> snapshotType) {
        logger.debug("Getting all snapshots of type: {}", snapshotType.getSimpleName());

        List<T> snapshots = new ArrayList<>();

        // For now, return empty list since we don't have a way to enumerate all available metrics
        // In a future enhancement, the MonitoringRegistry could be extended to support enumeration
        // of all available metric keys and their corresponding snapshots
        logger.debug("Snapshot enumeration not yet supported in unified collector architecture");

        return snapshots;
    }

    @Override
    public DomainAggregatedSnapshot getDomainAggregatedSnapshot(String domain) {
        logger.debug("Getting domain aggregated snapshot for: {}", domain);

        MonitoringRegistry registry = monitoringRegistry;
        if (registry == null) {
            logger.warn("Monitoring registry not available for domain aggregated snapshot: {}", domain);
            return DomainAggregatedSnapshot.empty(domain);
        }

        try {
            // Get all available metric keys from the registry
            Collection<MetricKey> allKeys = registry.getAllKeys();

            // Filter keys that belong to the requested domain
            List<MetricKey> domainKeys = allKeys.stream()
                    .filter(key -> key.labels().containsKey("domain") && domain.equals(key.labels().get("domain")))
                    .toList();

            if (domainKeys.isEmpty()) {
                logger.debug("No metrics found for domain: {}", domain);
                return DomainAggregatedSnapshot.empty(domain);
            }

            // Aggregate metrics across all keys in the domain
            long totalOperations = 0;
            long failedOperations = 0;
            long totalDurationNanos = 0;
            long operationCount = 0;
            List<org.openhab.core.ai.common.monitoring.api.MetricsSnapshot> operationSnapshots = new ArrayList<>();

            for (MetricKey key : domainKeys) {
                try {
                    MetricsCollector collector = registry.metricsCollector(key);
                    ExecutionMetricsSnapshot snapshot = collector.executionSnapshot();

                    totalOperations += snapshot.total();
                    failedOperations += snapshot.failure();
                    totalDurationNanos += snapshot.totalDurationNanos();
                    operationCount++;
                    operationSnapshots.add(snapshot);

                    logger.debug("Aggregated metrics for key {}: total={}, failed={}, duration={}ns", key.id(),
                            snapshot.total(), snapshot.failure(), snapshot.totalDurationNanos());
                } catch (Exception e) {
                    logger.warn("Failed to get metrics for key: {}", key.id(), e);
                }
            }

            // Calculate success rate
            double averageSuccessRate = totalOperations > 0
                    ? (double) (totalOperations - failedOperations) / totalOperations
                    : 0.0;

            // Create aggregated snapshot
            DomainAggregatedSnapshot snapshot = new DomainAggregatedSnapshot(domain, totalOperations,
                    totalOperations - failedOperations, // successful operations
                    failedOperations, totalDurationNanos, averageSuccessRate, operationSnapshots,
                    java.time.Instant.now());

            logger.debug(
                    "Created domain aggregated snapshot for {}: total={}, failed={}, successRate={}%, avgDuration={}ns",
                    domain, totalOperations, failedOperations, averageSuccessRate * 100,
                    totalOperations > 0 ? totalDurationNanos / totalOperations : 0);

            return snapshot;
        } catch (Exception e) {
            logger.warn("Failed to get domain aggregated snapshot for: {}", domain, e);
            return DomainAggregatedSnapshot.empty(domain);
        }
    }

    @Override
    public AgentModelSnapshot getAgentModelSnapshot(String modelId) {
        logger.debug("Getting agent model snapshot for modelId: {}", modelId);

        MonitoringRegistry registry = monitoringRegistry;
        if (registry == null) {
            logger.warn("Monitoring registry not available for agent model snapshot: {}", modelId);
            return AgentModelSnapshot.empty(modelId, "unknown");
        }

        try {
            MetricKey metricKey = new MetricKeys.SimpleMetricKey("agent-model." + modelId,
                    Map.of("domain", "agent-model", "operation", modelId));
            MetricsCollector collector = registry.metricsCollector(metricKey);
            ExecutionMetricsSnapshot snapshot = collector.executionSnapshot();

            // Extract domain-specific data from the collector
            String modelType = collector.getData("modelType", String.class) != null
                    ? collector.getData("modelType", String.class)
                    : "unknown";
            double accuracy = collector.getData("accuracy", Double.class) != null
                    ? collector.getData("accuracy", Double.class)
                    : 0.0;
            double performance = collector.getData("performance", Double.class) != null
                    ? collector.getData("performance", Double.class)
                    : 0.0;

            Map<String, Long> operationCounts = new HashMap<>();
            operationCounts.put("model-operation", snapshot.total());

            Map<String, Long> errorCounts = new HashMap<>();
            errorCounts.put("model-error", snapshot.failure());

            return new AgentModelSnapshot(snapshot.counts(), snapshot.timing(), snapshot.timestampMs(), modelId,
                    modelType, accuracy, performance, operationCounts, errorCounts, null, null);
        } catch (Exception e) {
            logger.warn("Failed to get agent model snapshot for: {}", modelId, e);
            return AgentModelSnapshot.empty(modelId, "unknown");
        }
    }

    @Override
    public ActionExecutionSnapshot getActionExecutionSnapshot(String actionId) {
        logger.debug("Getting action execution snapshot for actionId: {}", actionId);

        MonitoringRegistry registry = monitoringRegistry;
        if (registry == null) {
            logger.warn("Monitoring registry not available for action execution snapshot: {}", actionId);
            return ActionExecutionSnapshot.empty(actionId);
        }

        try {
            MetricKey metricKey = new MetricKeys.SimpleMetricKey("action." + actionId,
                    Map.of("domain", "action", "operation", actionId));
            MetricsCollector collector = registry.metricsCollector(metricKey);
            ExecutionMetricsSnapshot snapshot = collector.executionSnapshot();

            // Extract domain-specific data from the collector
            long inputDataSize = collector.getData("inputDataSize", Long.class) != null
                    ? collector.getData("inputDataSize", Long.class)
                    : 0L;
            long outputDataSize = collector.getData("outputDataSize", Long.class) != null
                    ? collector.getData("outputDataSize", Long.class)
                    : 0L;
            int priority = collector.getData("priority", Integer.class) != null
                    ? collector.getData("priority", Integer.class)
                    : 10;

            return new ActionExecutionSnapshot(snapshot.counts(), snapshot.timing(), snapshot.timestampMs(), actionId,
                    inputDataSize, outputDataSize, snapshot.failure(), priority, null);
        } catch (Exception e) {
            logger.warn("Failed to get action execution snapshot for: {}", actionId, e);
            return ActionExecutionSnapshot.empty(actionId);
        }
    }

    @Override
    public ToolExecutionSnapshot getToolExecutionSnapshot(String toolId) {
        logger.debug("Getting tool execution snapshot for toolId: {}", toolId);

        MonitoringRegistry registry = monitoringRegistry;
        if (registry == null) {
            logger.warn("Monitoring registry not available for tool execution snapshot: {}", toolId);
            return ToolExecutionSnapshot.empty(toolId);
        }

        try {
            MetricKey metricKey = new MetricKeys.SimpleMetricKey("tool." + toolId,
                    Map.of("domain", "tool", "operation", toolId));
            MetricsCollector collector = registry.metricsCollector(metricKey);
            ExecutionMetricsSnapshot snapshot = collector.executionSnapshot();

            // Extract domain-specific data from the collector
            double accuracy = collector.getData("accuracy", Double.class) != null
                    ? collector.getData("accuracy", Double.class)
                    : 0.0;
            double reliability = collector.getData("reliability", Double.class) != null
                    ? collector.getData("reliability", Double.class)
                    : 0.0;
            long dataProcessed = collector.getData("dataProcessed", Long.class) != null
                    ? collector.getData("dataProcessed", Long.class)
                    : 0L;

            return new ToolExecutionSnapshot(snapshot.counts(), snapshot.timing(), snapshot.timestampMs(), toolId,
                    accuracy, reliability, dataProcessed);
        } catch (Exception e) {
            logger.warn("Failed to get tool execution snapshot for: {}", toolId, e);
            return ToolExecutionSnapshot.empty(toolId);
        }
    }

    @Override
    public CommunicationSnapshot getCommunicationSnapshot(String communicationId) {
        logger.debug("Getting communication snapshot for communicationId: {}", communicationId);

        MonitoringRegistry registry = monitoringRegistry;
        if (registry == null) {
            logger.warn("Monitoring registry not available for communication snapshot: {}", communicationId);
            return CommunicationSnapshot.empty(communicationId);
        }

        try {
            MetricKey metricKey = new MetricKeys.SimpleMetricKey("communication." + communicationId,
                    Map.of("domain", "communication", "operation", communicationId));
            MetricsCollector collector = registry.metricsCollector(metricKey);
            ExecutionMetricsSnapshot snapshot = collector.executionSnapshot();

            // Extract domain-specific data from the collector
            String protocol = collector.getData("protocol", String.class) != null
                    ? collector.getData("protocol", String.class)
                    : "sync";
            long messagesSent = collector.getData("messagesSent", Long.class) != null
                    ? collector.getData("messagesSent", Long.class)
                    : snapshot.total();
            long messagesReceived = collector.getData("messagesReceived", Long.class) != null
                    ? collector.getData("messagesReceived", Long.class)
                    : snapshot.success();
            double bandwidth = collector.getData("bandwidth", Double.class) != null
                    ? collector.getData("bandwidth", Double.class)
                    : 1024.0;

            return new CommunicationSnapshot(snapshot.total(), snapshot.success(), snapshot.failure(),
                    snapshot.totalDurationNanos(), snapshot.successRate(), 0.0, snapshot.averageMs(snapshot.total()),
                    0.0, 0.0, 0.0, 0L, bandwidth, 0.0, 0.0, snapshot.timestampMs());
        } catch (Exception e) {
            logger.warn("Failed to get communication snapshot for: {}", communicationId, e);
            return CommunicationSnapshot.empty(communicationId);
        }
    }

    @Override
    public ErrorRecoverySnapshot getErrorRecoverySnapshot(String errorType) {
        logger.debug("Getting error recovery snapshot for: {}", errorType);

        MonitoringRegistry registry = monitoringRegistry;
        if (registry == null) {
            logger.warn("Monitoring registry not available for error recovery snapshot: {}", errorType);
            return ErrorRecoverySnapshot.empty();
        }

        try {
            MetricKey metricKey = new MetricKeys.SimpleMetricKey("error-recovery." + errorType,
                    Map.of("domain", "error-recovery", "operation", errorType));
            MetricsCollector collector = registry.metricsCollector(metricKey);
            ExecutionMetricsSnapshot snapshot = collector.executionSnapshot();

            // Extract data from the collector
            String recoveryStrategy = collector.getData("recoveryStrategy", String.class) != null
                    ? collector.getData("recoveryStrategy", String.class)
                    : "unknown";
            boolean fallbackUsed = collector.getData("fallbackUsed", Boolean.class) != null
                    ? collector.getData("fallbackUsed", Boolean.class)
                    : false;

            // Create error counts by type and recovery counts by strategy maps
            Map<String, Long> errorCountsByType = Map.of(errorType, snapshot.total());
            Map<String, Long> recoveryCountsByStrategy = Map.of(recoveryStrategy, snapshot.success());

            return new ErrorRecoverySnapshot(snapshot.counts(), snapshot.timing(), snapshot.timestampMs(),
                    snapshot.total(), snapshot.success(), fallbackUsed ? snapshot.success() : 0L, snapshot.failure(),
                    snapshot.total(), errorCountsByType, recoveryCountsByStrategy);
        } catch (Exception e) {
            logger.warn("Failed to get error recovery snapshot for: {}", errorType, e);
            return ErrorRecoverySnapshot.empty();
        }
    }

    @Override
    public ErrorRecoveryStatistics getErrorRecoveryStatistics(String errorType, Duration timeRange) {
        logger.debug("Getting error recovery statistics for: {} (timeRange={})", errorType, timeRange);

        // For now, return statistics based on current snapshot since historical data is not yet stored
        // In a future enhancement, the MonitoringRegistry could be extended to support historical snapshots
        ErrorRecoverySnapshot currentSnapshot = getErrorRecoverySnapshot(errorType);
        List<ErrorRecoverySnapshot> snapshots = List.of(currentSnapshot);

        return ErrorRecoveryStatistics.fromSnapshots(snapshots, timeRange);
    }

    @Override
    public ReasoningPerformanceStatistics getReasoningPerformanceStatistics(String agentId, Duration timeRange) {
        logger.debug("Getting reasoning performance statistics for: {} (timeRange={})", agentId, timeRange);

        // For now, return statistics based on current snapshot since historical data is not yet stored
        // In a future enhancement, the MonitoringRegistry could be extended to support historical snapshots
        MonitoringRegistry registry = monitoringRegistry;
        if (registry == null) {
            logger.warn("Monitoring registry not available for reasoning performance statistics: {}", agentId);
            return ReasoningPerformanceStatistics.fromReasoningData(0L, 0L, 0L, 0L, 0L, 0L, 0.0, 0L, 0.0, 0.0, 0.0, 0.0,
                    Map.of(), Map.of(), Map.of(), Map.of(), timeRange);
        }

        try {
            MetricKey metricKey = new MetricKeys.SimpleMetricKey("reasoning." + agentId,
                    Map.of("domain", "reasoning", "operation", agentId));
            MetricsCollector collector = registry.metricsCollector(metricKey);
            ExecutionMetricsSnapshot snapshot = collector.executionSnapshot();

            // Extract reasoning-specific data from the collector
            long totalStepCount = collector.getData("totalStepCount", Long.class) != null
                    ? collector.getData("totalStepCount", Long.class)
                    : snapshot.total();
            long totalStorageSizeBytes = collector.getData("totalStorageSizeBytes", Long.class) != null
                    ? collector.getData("totalStorageSizeBytes", Long.class)
                    : 0L;
            long activeStepCount = collector.getData("activeStepCount", Long.class) != null
                    ? collector.getData("activeStepCount", Long.class)
                    : snapshot.success();
            long archivedStepCount = collector.getData("archivedStepCount", Long.class) != null
                    ? collector.getData("archivedStepCount", Long.class)
                    : 0L;
            long compressedStepCount = collector.getData("compressedStepCount", Long.class) != null
                    ? collector.getData("compressedStepCount", Long.class)
                    : 0L;
            long totalTokensUsed = collector.getData("totalTokensUsed", Long.class) != null
                    ? collector.getData("totalTokensUsed", Long.class)
                    : 0L;
            double totalCostUsd = collector.getData("totalCostUsd", Double.class) != null
                    ? collector.getData("totalCostUsd", Double.class)
                    : 0.0;
            long totalProcessingTimeMs = snapshot.totalDurationNanos() / 1_000_000; // Convert to milliseconds
            double averageQualityScore = collector.getData("averageQualityScore", Double.class) != null
                    ? collector.getData("averageQualityScore", Double.class)
                    : 85.0; // Default quality score
            double averageConfidence = collector.getData("averageConfidence", Double.class) != null
                    ? collector.getData("averageConfidence", Double.class)
                    : 0.8; // Default confidence
            double averageStepSizeBytes = totalStepCount > 0 ? (double) totalStorageSizeBytes / totalStepCount : 0.0;
            double averageStepsPerSession = collector.getData("averageStepsPerSession", Double.class) != null
                    ? collector.getData("averageStepsPerSession", Double.class)
                    : 5.0; // Default steps per session

            // Create distribution maps (simplified for now)
            Map<String, Long> statusDistribution = Map.of("completed", snapshot.success(), "failed",
                    snapshot.failure());
            Map<String, Long> typeDistribution = Map.of("reasoning", totalStepCount);
            Map<String, Long> modelUsageDistribution = Map.of("default", totalStepCount);
            Map<String, Long> sessionDistribution = Map.of("active", activeStepCount, "archived", archivedStepCount);

            return ReasoningPerformanceStatistics.fromReasoningData(totalStepCount, totalStorageSizeBytes,
                    activeStepCount, archivedStepCount, compressedStepCount, totalTokensUsed, totalCostUsd,
                    totalProcessingTimeMs, averageQualityScore, averageConfidence, averageStepSizeBytes,
                    averageStepsPerSession, statusDistribution, typeDistribution, modelUsageDistribution,
                    sessionDistribution, timeRange);
        } catch (Exception e) {
            logger.warn("Failed to get reasoning performance statistics for: {}", agentId, e);
            return ReasoningPerformanceStatistics.fromReasoningData(0L, 0L, 0L, 0L, 0L, 0L, 0.0, 0L, 0.0, 0.0, 0.0, 0.0,
                    Map.of(), Map.of(), Map.of(), Map.of(), timeRange);
        }
    }

    @Override
    public SecurityMonitoringSnapshot getSecurityMonitoringSnapshot(String securityId) {
        logger.debug("Getting security monitoring snapshot for: {}", securityId);

        MonitoringRegistry registry = monitoringRegistry;
        if (registry == null) {
            logger.warn("Monitoring registry not available for security monitoring snapshot: {}", securityId);
            return SecurityMonitoringSnapshot.empty(securityId);
        }

        try {
            MetricKey metricKey = new MetricKeys.SimpleMetricKey("security." + securityId,
                    Map.of("domain", "security", "operation", securityId));
            MetricsCollector collector = registry.metricsCollector(metricKey);
            ExecutionMetricsSnapshot snapshot = collector.executionSnapshot();

            // Extract security-specific data from the collector
            long securityViolations = collector.getData("securityViolations", Long.class) != null
                    ? collector.getData("securityViolations", Long.class)
                    : 0L;
            long activeClients = collector.getData("activeClients", Long.class) != null
                    ? collector.getData("activeClients", Long.class)
                    : snapshot.success();
            long blockedClients = collector.getData("blockedClients", Long.class) != null
                    ? collector.getData("blockedClients", Long.class)
                    : snapshot.failure();
            boolean authenticationEnabled = collector.getData("authenticationEnabled", Boolean.class) != null
                    ? collector.getData("authenticationEnabled", Boolean.class)
                    : true;
            boolean requestValidationEnabled = collector.getData("requestValidationEnabled", Boolean.class) != null
                    ? collector.getData("requestValidationEnabled", Boolean.class)
                    : true;
            int maxConnections = collector.getData("maxConnections", Integer.class) != null
                    ? collector.getData("maxConnections", Integer.class)
                    : 100;
            int requestRateLimit = collector.getData("requestRateLimit", Integer.class) != null
                    ? collector.getData("requestRateLimit", Integer.class)
                    : 60;
            int sessionTimeoutMinutes = collector.getData("sessionTimeoutMinutes", Integer.class) != null
                    ? collector.getData("sessionTimeoutMinutes", Integer.class)
                    : 30;

            return new SecurityMonitoringSnapshot(
                    new SecurityMonitoringSnapshot.Counts(snapshot.total(), snapshot.success(), snapshot.failure()),
                    new SecurityMonitoringSnapshot.Timing(snapshot.totalDurationNanos()), snapshot.timestampMs(),
                    securityViolations, (int) activeClients, (int) blockedClients, authenticationEnabled,
                    requestValidationEnabled, maxConnections, requestRateLimit, sessionTimeoutMinutes);
        } catch (Exception e) {
            logger.warn("Failed to get security monitoring snapshot for: {}", securityId, e);
            return SecurityMonitoringSnapshot.empty(securityId);
        }
    }

    @Override
    public SecurityMonitoringStatistics getSecurityMonitoringStatistics(String securityId, Duration timeRange) {
        logger.debug("Getting security monitoring statistics for: {} (timeRange={})", securityId, timeRange);

        // For now, return statistics based on current snapshot since historical data is not yet stored
        // In a future enhancement, the MonitoringRegistry could be extended to support historical snapshots
        SecurityMonitoringSnapshot currentSnapshot = getSecurityMonitoringSnapshot(securityId);
        List<SecurityMonitoringSnapshot> snapshots = List.of(currentSnapshot);

        return new SecurityMonitoringStatistics(snapshots, timeRange, System.currentTimeMillis());
    }

    @Override
    public CoordinationStatistics getCoordinationStatistics(String agentId, Duration timeRange) {
        logger.debug("Getting coordination statistics for: {} (timeRange={})", agentId, timeRange);

        MonitoringRegistry registry = monitoringRegistry;
        if (registry == null) {
            logger.warn("Monitoring registry not available for coordination statistics: {}", agentId);
            return CoordinationStatistics.fromSnapshots(List.of(), timeRange);
        }

        try {
            // Get coordination-related metrics from the registry
            // For now, return empty statistics since we need to implement proper snapshot collection
            // This will be enhanced when CoordinationSnapshot collection is implemented
            return CoordinationStatistics.fromSnapshots(List.of(), timeRange);
        } catch (Exception e) {
            logger.warn("Failed to get coordination statistics for: {}", agentId, e);
            return CoordinationStatistics.fromSnapshots(List.of(), timeRange);
        }
    }

    @Override
    public org.openhab.core.ai.agent.collaboration.coordination.CoordinationStatistics getAgentCoordinationStatistics(
            String agentId, Duration timeRange) {
        logger.debug("Getting agent coordination statistics for: {} (timeRange={})", agentId, timeRange);

        MonitoringRegistry registry = monitoringRegistry;
        if (registry == null) {
            logger.warn("Monitoring registry not available for agent coordination statistics: {}", agentId);
            return new org.openhab.core.ai.agent.collaboration.coordination.CoordinationStatistics(0L, 0L, 0L, 0L, 0, 0,
                    0, 0);
        }

        try {
            // Get coordination-related metrics from the registry
            // For now, return empty statistics since we need to implement proper metric collection
            // This will be enhanced when coordination metrics are properly recorded
            return new org.openhab.core.ai.agent.collaboration.coordination.CoordinationStatistics(0L, 0L, 0L, 0L, 0, 0,
                    0, 0);
        } catch (Exception e) {
            logger.warn("Failed to get agent coordination statistics for: {}", agentId, e);
            return new org.openhab.core.ai.agent.collaboration.coordination.CoordinationStatistics(0L, 0L, 0L, 0L, 0, 0,
                    0, 0);
        }
    }

    @Override
    public OptimizationStatistics getOptimizationStatistics(String optimizationId, Duration timeRange) {
        logger.debug("Getting optimization statistics for: {} (timeRange={})", optimizationId, timeRange);

        // For now, return empty statistics since we need to implement proper snapshot collection
        // This will be enhanced when OptimizationSnapshot collection is implemented
        return OptimizationStatistics.fromSnapshots(List.of(), timeRange);
    }

    @Override
    public MessageSecurityStatistics getMessageSecurityStatistics(String securityId, Duration timeRange) {
        logger.debug("Getting message security statistics for: {} (timeRange={})", securityId, timeRange);

        // For now, return empty statistics since we need to implement proper snapshot collection
        // This will be enhanced when MessageSecuritySnapshot collection is implemented
        return MessageSecurityStatistics.empty(timeRange);
    }
}
