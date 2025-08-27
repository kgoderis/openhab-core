package org.openhab.core.ai.common.monitoring.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.Counts;
import org.openhab.core.ai.common.monitoring.api.MetricKey;
import org.openhab.core.ai.common.monitoring.api.MetricKeys;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.api.MetricsSnapshot;
import org.openhab.core.ai.common.monitoring.api.Timing;
import org.openhab.core.ai.common.monitoring.collector.MetricsCollector;
import org.openhab.core.ai.common.monitoring.registry.MetricsRegistry;
import org.openhab.core.ai.common.monitoring.service.factory.SnapshotFactory;
import org.openhab.core.ai.common.monitoring.service.factory.StatisticsFactory;
import org.openhab.core.ai.common.monitoring.service.snapshot.ActionExecutionSnapshot;
import org.openhab.core.ai.common.monitoring.service.snapshot.AgentModelSnapshot;
import org.openhab.core.ai.common.monitoring.service.snapshot.AgentTaskSnapshot;
import org.openhab.core.ai.common.monitoring.service.snapshot.CommunicationSnapshot;
import org.openhab.core.ai.common.monitoring.service.snapshot.DomainAggregatedSnapshot;
import org.openhab.core.ai.common.monitoring.service.snapshot.ErrorRecoverySnapshot;
import org.openhab.core.ai.common.monitoring.service.snapshot.GenericMetricsSnapshot;
import org.openhab.core.ai.common.monitoring.service.snapshot.ModelCompletionSnapshot;
import org.openhab.core.ai.common.monitoring.service.snapshot.SecurityMonitoringSnapshot;
import org.openhab.core.ai.common.monitoring.service.snapshot.ToolExecutionSnapshot;
import org.openhab.core.ai.common.monitoring.service.snapshot.ToolFileReadSnapshot;
import org.openhab.core.ai.common.monitoring.snapshot.ExecutionMetricsSnapshot;

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
    private @Nullable MetricsRegistry monitoringRegistry;

    // ===== Unified Generic Retrieval with Type Safety =====

    @Override
    public <T extends MetricsSnapshot> T getSnapshot(MetricKey key, Class<T> snapshotType) {
        logger.debug("Getting snapshot for key: {} type: {}", key.id(), snapshotType.getSimpleName());

        MetricsRegistry registry = monitoringRegistry;
        if (registry == null) {
            logger.warn("Monitoring registry not available for snapshot: {}:{}", key.id(), snapshotType.getSimpleName());
            return SnapshotFactory.createEmptySnapshot(snapshotType, key);
        }

        try {
            MetricsCollector collector = registry.getCollector(key);
            return SnapshotFactory.createSnapshot(collector, snapshotType, key);
        } catch (Exception e) {
            logger.warn("Failed to get snapshot for key: {} type: {}", key.id(), snapshotType.getSimpleName(), e);
            return SnapshotFactory.createEmptySnapshot(snapshotType, key);
        }
    }

    @Override
    public MetricsSnapshot getSnapshot(MetricKey key) {
        // Default to generic snapshot
        return getSnapshot(key, GenericMetricsSnapshot.class);
    }

    @Override
    public <T extends MetricsSnapshot> List<T> getSnapshotsByCapability(Class<T> capabilityType) {
        logger.debug("Getting snapshots by capability: {}", capabilityType.getSimpleName());

        MetricsRegistry registry = monitoringRegistry;
        if (registry == null) {
            logger.warn("Monitoring registry not available for capability snapshots: {}", capabilityType.getSimpleName());
            return List.of();
        }

        List<T> snapshots = new ArrayList<>();
        
        // Get all available keys and filter by capability
        Collection<MetricKey> allKeys = registry.getAllKeys();
        for (MetricKey key : allKeys) {
            try {
                MetricsCollector collector = registry.getCollector(key);
                GenericMetricsSnapshot genericSnapshot = SnapshotFactory.createSnapshot(
                    collector, GenericMetricsSnapshot.class, key);
                
                // Check if the snapshot supports the requested capability
                if (genericSnapshot.hasCapability(capabilityType)) {
                    T typedSnapshot = genericSnapshot.as(capabilityType);
                    snapshots.add(typedSnapshot);
                }
            } catch (Exception e) {
                logger.warn("Failed to process snapshot for key: {}", key.id(), e);
            }
        }

        return snapshots;
    }

    @Override
    public <T extends MetricsSnapshot> List<T> getSnapshotsByDomain(String domain, Class<T> snapshotType) {
        logger.debug("Getting snapshots by domain: {} type: {}", domain, snapshotType.getSimpleName());

        MetricsRegistry registry = monitoringRegistry;
        if (registry == null) {
            logger.warn("Monitoring registry not available for domain snapshots: {}:{}", domain, snapshotType.getSimpleName());
            return List.of();
        }

        List<T> snapshots = new ArrayList<>();
        
        // Get all available keys and filter by domain
        Collection<MetricKey> allKeys = registry.getAllKeys();
        for (MetricKey key : allKeys) {
            if (domain.equals(key.labels().get("domain"))) {
                try {
                    MetricsCollector collector = registry.getCollector(key);
                    T snapshot = SnapshotFactory.createSnapshot(collector, snapshotType, key);
                    snapshots.add(snapshot);
                } catch (Exception e) {
                    logger.warn("Failed to process snapshot for key: {}", key.id(), e);
                }
            }
        }

        return snapshots;
    }

    // ===== Recording Methods =====

    @Override
    public void recordOperation(String domain, String operation, boolean success, Duration duration) {
        try {
            MetricsRegistry registry = monitoringRegistry;
            if (registry != null) {
                // Create a metric key for the registry
                MetricKey metricKey = new MetricKeys.SimpleMetricKey(domain + "." + operation,
                        Map.of("domain", domain, "operation", operation), Set.of("counts", "latency"));
                MetricsCollector collector = registry.getCollector(metricKey);
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

        MetricsRegistry registry = monitoringRegistry;
        if (registry != null) {
            try {
                MetricKey metricKey = new MetricKeys.SimpleMetricKey(domain + "." + operation,
                        Map.of("domain", domain, "operation", operation), Set.of("counts", "latency"));
                MetricsCollector collector = registry.getCollector(metricKey);
                collector.recordExecution(success, duration.toNanos());

                // Record data if provided
                if (!data.isEmpty()) {
                    collector.recordExtendedData(data);
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

        MetricsRegistry registry = monitoringRegistry;
        if (registry != null) {
            try {
                MetricKey metricKey = new MetricKeys.SimpleMetricKey("model." + modelId + ".completion",
                        Map.of("domain", "model", "operation", modelId + ":completion"), Set.of("counts", "latency", "model"));
                MetricsCollector collector = registry.getCollector(metricKey);
                collector.recordExecution(success, duration.toNanos());
                collector.recordExtendedData("inputTokens", inputTokens);
                collector.recordExtendedData("outputTokens", outputTokens);
                collector.recordExtendedData("cost", cost);
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

        MetricsRegistry registry = monitoringRegistry;
        if (registry != null) {
            try {
                MetricKey metricKey = new MetricKeys.SimpleMetricKey("tool." + toolId + ".file_read",
                        Map.of("domain", "tool", "operation", toolId + ":file_read"), Set.of("counts", "latency", "file"));
                MetricsCollector collector = registry.getCollector(metricKey);
                collector.recordExecution(success, duration.toNanos());
                collector.recordExtendedData("fileSize", fileSize);
                collector.recordExtendedData("fileType", fileType);
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

        MetricsRegistry registry = monitoringRegistry;
        if (registry != null) {
            try {
                MetricKey metricKey = new MetricKeys.SimpleMetricKey("agent." + agentId + ".task",
                        Map.of("domain", "agent", "operation", agentId + ":task"), Set.of("counts", "latency", "agent"));
                MetricsCollector collector = registry.getCollector(metricKey);
                collector.recordExecution(success, duration.toNanos());
                collector.recordExtendedData("taskType", taskType);
                collector.recordExtendedData("decisionAccuracy", decisionAccuracy);
                collector.recordExtendedData("learningRate", learningRate);
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

        MetricsRegistry registry = monitoringRegistry;
        if (registry != null) {
            try {
                MetricKey metricKey = new MetricKeys.SimpleMetricKey("monitoring." + monitoringId,
                        Map.of("domain", "monitoring", "operation", monitoringId), Set.of("counts", "latency", "monitoring"));
                MetricsCollector collector = registry.getCollector(metricKey);
                collector.recordExecution(success, duration.toNanos());
                collector.recordExtendedData("metricsCollected", metricsCollected);
                collector.recordExtendedData("alertsGenerated", alertsGenerated);
                collector.recordExtendedData("metricType", metricType);
                collector.recordExtendedData("alertSeverity", alertSeverity);
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

        MetricsRegistry registry = monitoringRegistry;
        if (registry != null) {
            try {
                MetricKey metricKey = new MetricKeys.SimpleMetricKey("error-recovery." + errorType,
                        Map.of("domain", "error-recovery", "operation", errorType), Set.of("counts", "latency", "error"));
                MetricsCollector collector = registry.getCollector(metricKey);
                collector.recordExecution(success, duration.toNanos());
                collector.recordExtendedData("recoveryStrategy", recoveryStrategy);
                collector.recordExtendedData("fallbackUsed", fallbackUsed);
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
    public GenericMetricsSnapshot getSnapshot(String domain, String operation) {
        logger.debug("Getting generic snapshot for domain: {} operation: {}", domain, operation);

        MetricsRegistry registry = monitoringRegistry;
        if (registry == null) {
            logger.warn("Monitoring registry not available for generic snapshot: {}:{}", domain, operation);
            return GenericMetricsSnapshot.builder(domain, operation)
                    .withMetric("total_count", 0L)
                    .withMetric("success_count", 0L)
                    .withMetric("failure_count", 0L)
                    .withMetric("total_duration_ms", 0L)
                    .withMetric("success_rate", 0.0)
                    .build();
        }

        try {
            MetricKey metricKey = new MetricKeys.SimpleMetricKey(domain + "." + operation,
                    Map.of("domain", domain, "operation", operation),
                    Set.of("counts", "latency"));
            return getSnapshot(metricKey, GenericMetricsSnapshot.class);
        } catch (Exception e) {
            logger.warn("Failed to get generic snapshot for: {}:{}", domain, operation, e);
            return GenericMetricsSnapshot.builder(domain, operation)
                    .withMetric("total_count", 0L)
                    .withMetric("success_count", 0L)
                    .withMetric("failure_count", 0L)
                    .withMetric("total_duration_ms", 0L)
                    .withMetric("success_rate", 0.0)
                    .build();
        }
    }

    // ===== Generic Statistics Retrieval =====

    @Override
    public <T extends org.openhab.core.ai.common.monitoring.service.statistics.StatisticsSnapshot> T getStatistics(MetricKey key, Class<T> statisticsType, Duration timeRange) {
        // Get snapshots for the key
        List<MetricsSnapshot> snapshots = getAllSnapshots(MetricsSnapshot.class);
        
        // Filter snapshots for the specific key
        List<MetricsSnapshot> keySnapshots = snapshots.stream()
            .filter(snapshot -> {
                // This is a simplified filter - in a real implementation, you'd match by key
                // TODO: Implement proper key matching
                return true; // For now, return all snapshots
            })
            .collect(Collectors.toList());
        
        return StatisticsFactory.createStatistics(keySnapshots, statisticsType, key, timeRange);
    }

    @Override
    public <T extends org.openhab.core.ai.common.monitoring.service.statistics.StatisticsSnapshot> List<T> getStatisticsByCapability(Class<T> capabilityType, Duration timeRange) {
        List<T> statistics = new ArrayList<>();
        Collection<MetricKey> keys = monitoringRegistry.getAllKeys();
        
        for (MetricKey key : keys) {
            try {
                T statistic = getStatistics(key, capabilityType, timeRange);
                statistics.add(statistic);
            } catch (IllegalArgumentException e) {
                // Skip keys that don't support this capability
                logger.debug("Skipping key {} for capability {}: {}", key.id(), capabilityType.getSimpleName(), e.getMessage());
            }
        }
        
        return statistics;
    }

    @Override
    public <T extends org.openhab.core.ai.common.monitoring.service.statistics.StatisticsSnapshot> List<T> getStatisticsByDomain(String domain, Class<T> statisticsType, Duration timeRange) {
        List<T> statistics = new ArrayList<>();
        Collection<MetricKey> keys = monitoringRegistry.getKeysByDomain(domain);
        
        for (MetricKey key : keys) {
            try {
                T statistic = getStatistics(key, statisticsType, timeRange);
                statistics.add(statistic);
            } catch (IllegalArgumentException e) {
                // Skip keys that don't support this statistics type
                logger.debug("Skipping key {} for domain {}: {}", key.id(), domain, e.getMessage());
            }
        }
        
        return statistics;
    }
}
