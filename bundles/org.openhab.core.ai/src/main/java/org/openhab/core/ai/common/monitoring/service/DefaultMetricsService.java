package org.openhab.core.ai.common.monitoring.service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.MetricKey;
import org.openhab.core.ai.common.monitoring.api.MetricKeys;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.api.MetricsSnapshot;
import org.openhab.core.ai.common.monitoring.collector.MetricsCollector;
import org.openhab.core.ai.common.monitoring.registry.MetricsRegistry;
import org.openhab.core.ai.common.monitoring.service.factory.SnapshotFactory;
import org.openhab.core.ai.common.monitoring.service.factory.StatisticsFactory;
import org.openhab.core.ai.common.monitoring.service.snapshot.DomainAggregatedSnapshot;
import org.openhab.core.ai.common.monitoring.service.snapshot.GenericMetricsSnapshot;
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
            logger.warn("Monitoring registry not available for snapshot: {}:{}", key.id(),
                    snapshotType.getSimpleName());
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
            logger.warn("Monitoring registry not available for capability snapshots: {}",
                    capabilityType.getSimpleName());
            return List.of();
        }

        List<T> snapshots = new ArrayList<>();

        // Get all available keys and filter by capability
        Collection<MetricKey> allKeys = registry.getAllKeys();
        for (MetricKey key : allKeys) {
            try {
                MetricsCollector collector = registry.getCollector(key);
                GenericMetricsSnapshot genericSnapshot = SnapshotFactory.createSnapshot(collector,
                        GenericMetricsSnapshot.class, key);

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
            logger.warn("Monitoring registry not available for domain snapshots: {}:{}", domain,
                    snapshotType.getSimpleName());
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
            return GenericMetricsSnapshot.builder(domain, operation).withMetric("total_count", 0L)
                    .withMetric("success_count", 0L).withMetric("failure_count", 0L).withMetric("total_duration_ms", 0L)
                    .withMetric("success_rate", 0.0).build();
        }

        try {
            MetricKey metricKey = new MetricKeys.SimpleMetricKey(domain + "." + operation,
                    Map.of("domain", domain, "operation", operation), Set.of("counts", "latency"));
            return getSnapshot(metricKey, GenericMetricsSnapshot.class);
        } catch (Exception e) {
            logger.warn("Failed to get generic snapshot for: {}:{}", domain, operation, e);
            return GenericMetricsSnapshot.builder(domain, operation).withMetric("total_count", 0L)
                    .withMetric("success_count", 0L).withMetric("failure_count", 0L).withMetric("total_duration_ms", 0L)
                    .withMetric("success_rate", 0.0).build();
        }
    }

    // ===== Generic Statistics Retrieval =====

    @Override
    public <T extends org.openhab.core.ai.common.monitoring.service.statistics.StatisticsSnapshot> T getStatistics(
            MetricKey key, Class<T> statisticsType, Duration timeRange) {
        // Get snapshots for the key
        List<MetricsSnapshot> snapshots = getAllSnapshots(MetricsSnapshot.class);

        // Filter snapshots for the specific key
        List<MetricsSnapshot> keySnapshots = snapshots.stream().filter(snapshot -> {
            // This is a simplified filter - in a real implementation, you'd match by key
            // TODO: Implement proper key matching
            return true; // For now, return all snapshots
        }).collect(Collectors.toList());

        return StatisticsFactory.createStatistics(keySnapshots, statisticsType, key, timeRange);
    }

    @Override
    public <T extends org.openhab.core.ai.common.monitoring.service.statistics.StatisticsSnapshot> List<T> getStatisticsByCapability(
            Class<T> capabilityType, Duration timeRange) {
        List<T> statistics = new ArrayList<>();
        Collection<MetricKey> keys = monitoringRegistry.getAllKeys();

        for (MetricKey key : keys) {
            try {
                T statistic = getStatistics(key, capabilityType, timeRange);
                statistics.add(statistic);
            } catch (IllegalArgumentException e) {
                // Skip keys that don't support this capability
                logger.debug("Skipping key {} for capability {}: {}", key.id(), capabilityType.getSimpleName(),
                        e.getMessage());
            }
        }

        return statistics;
    }

    @Override
    public <T extends org.openhab.core.ai.common.monitoring.service.statistics.StatisticsSnapshot> List<T> getStatisticsByDomain(
            String domain, Class<T> statisticsType, Duration timeRange) {
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

    // ===== Phase 4: Advanced Features - Aggregation and Filtering =====

    @Override
    public <T extends MetricsSnapshot> List<T> getAggregatedSnapshots(String domain, Class<T> snapshotType) {
        logger.debug("Getting aggregated snapshots for domain: {} type: {}", domain, snapshotType.getSimpleName());

        MetricsRegistry registry = monitoringRegistry;
        if (registry == null) {
            logger.warn("Monitoring registry not available for aggregated snapshots: {}:{}", domain,
                    snapshotType.getSimpleName());
            return List.of();
        }

        List<T> aggregatedSnapshots = new ArrayList<>();

        try {
            // Get all keys for the domain
            Collection<MetricKey> domainKeys = registry.getKeysByDomain(domain);

            // Group snapshots by operation type for aggregation
            Map<String, List<MetricsSnapshot>> operationGroups = new java.util.HashMap<>();

            for (MetricKey key : domainKeys) {
                try {
                    MetricsCollector collector = registry.getCollector(key);
                    GenericMetricsSnapshot snapshot = SnapshotFactory.createSnapshot(collector,
                            GenericMetricsSnapshot.class, key);

                    String operation = key.labels().get("operation");
                    if (operation != null) {
                        operationGroups.computeIfAbsent(operation, k -> new ArrayList<>()).add(snapshot);
                    }
                } catch (Exception e) {
                    logger.warn("Failed to process snapshot for key: {}", key.id(), e);
                }
            }

            // Create aggregated snapshots for each operation group
            for (Map.Entry<String, List<MetricsSnapshot>> entry : operationGroups.entrySet()) {
                String operation = entry.getKey();
                List<MetricsSnapshot> snapshots = entry.getValue();

                if (!snapshots.isEmpty()) {
                    // Aggregate the snapshots
                    T aggregatedSnapshot = createAggregatedSnapshot(domain, operation, snapshots, snapshotType);
                    if (aggregatedSnapshot != null) {
                        aggregatedSnapshots.add(aggregatedSnapshot);
                    }
                }
            }

        } catch (Exception e) {
            logger.warn("Failed to get aggregated snapshots for domain: {}", domain, e);
        }

        return aggregatedSnapshots;
    }

    @Override
    public <T extends MetricsSnapshot> List<T> getSnapshotsInRange(String domain, String operation,
            Class<T> snapshotType, Instant start, Instant end) {
        logger.debug("Getting snapshots in range for domain: {} operation: {} type: {} from {} to {}", domain,
                operation, snapshotType.getSimpleName(), start, end);

        MetricsRegistry registry = monitoringRegistry;
        if (registry == null) {
            logger.warn("Monitoring registry not available for time range snapshots: {}:{}", domain, operation);
            return List.of();
        }

        List<T> rangeSnapshots = new ArrayList<>();

        try {
            // Get all keys for the domain and operation
            Collection<MetricKey> allKeys = registry.getAllKeys();

            for (MetricKey key : allKeys) {
                if (domain.equals(key.labels().get("domain"))
                        && (operation == null || operation.equals(key.labels().get("operation")))) {

                    try {
                        MetricsCollector collector = registry.getCollector(key);
                        GenericMetricsSnapshot snapshot = SnapshotFactory.createSnapshot(collector,
                                GenericMetricsSnapshot.class, key);

                        // Check if snapshot timestamp is within range
                        if (snapshot.getTimestamp().isAfter(start) && snapshot.getTimestamp().isBefore(end)) {
                            T typedSnapshot = snapshot.as(snapshotType);
                            rangeSnapshots.add(typedSnapshot);
                        }
                    } catch (Exception e) {
                        logger.warn("Failed to process snapshot for key: {}", key.id(), e);
                    }
                }
            }

        } catch (Exception e) {
            logger.warn("Failed to get snapshots in range for domain: {} operation: {}", domain, operation, e);
        }

        return rangeSnapshots;
    }

    @Override
    public DomainAggregatedSnapshot getDomainAggregatedSnapshot(String domain) {
        logger.debug("Getting domain aggregated snapshot for domain: {}", domain);

        MetricsRegistry registry = monitoringRegistry;
        if (registry == null) {
            logger.warn("Monitoring registry not available for domain aggregated snapshot: {}", domain);
            return DomainAggregatedSnapshot.empty(domain);
        }

        try {
            // Get all keys for the domain
            Collection<MetricKey> domainKeys = registry.getKeysByDomain(domain);

            long totalOperations = 0L;
            long successfulOperations = 0L;
            long failedOperations = 0L;
            long totalDurationNanos = 0L;
            List<MetricsSnapshot> operationSnapshots = new ArrayList<>();

            for (MetricKey key : domainKeys) {
                try {
                    MetricsCollector collector = registry.getCollector(key);
                    GenericMetricsSnapshot snapshot = SnapshotFactory.createSnapshot(collector,
                            GenericMetricsSnapshot.class, key);

                    // Aggregate the metrics
                    totalOperations += snapshot.total();
                    successfulOperations += snapshot.success();
                    failedOperations += snapshot.failure();
                    totalDurationNanos += snapshot.totalDurationNanos();
                    operationSnapshots.add(snapshot);

                } catch (Exception e) {
                    logger.warn("Failed to process snapshot for key: {}", key.id(), e);
                }
            }

            // Calculate average success rate
            double averageSuccessRate = totalOperations > 0 ? (successfulOperations * 100.0) / totalOperations : 0.0;

            return new DomainAggregatedSnapshot(domain, totalOperations, successfulOperations, failedOperations,
                    totalDurationNanos, averageSuccessRate, operationSnapshots, Instant.now());

        } catch (Exception e) {
            logger.warn("Failed to get domain aggregated snapshot for domain: {}", domain, e);
            return DomainAggregatedSnapshot.empty(domain);
        }
    }

    // ===== Private Helper Methods =====

    /**
     * Create an aggregated snapshot from a list of snapshots.
     * 
     * @param <T> the snapshot type
     * @param domain the domain
     * @param operation the operation
     * @param snapshots the snapshots to aggregate
     * @param snapshotType the target snapshot type
     * @return aggregated snapshot or null if aggregation fails
     */
    @SuppressWarnings("unchecked")
    private <T extends MetricsSnapshot> T createAggregatedSnapshot(String domain, String operation,
            List<MetricsSnapshot> snapshots, Class<T> snapshotType) {
        try {
            // Aggregate basic metrics
            long totalOperations = snapshots.stream().filter(s -> s instanceof GenericMetricsSnapshot)
                    .mapToLong(s -> ((GenericMetricsSnapshot) s).total()).sum();
            long successfulOperations = snapshots.stream().filter(s -> s instanceof GenericMetricsSnapshot)
                    .mapToLong(s -> ((GenericMetricsSnapshot) s).success()).sum();
            long failedOperations = snapshots.stream().filter(s -> s instanceof GenericMetricsSnapshot)
                    .mapToLong(s -> ((GenericMetricsSnapshot) s).failure()).sum();
            long totalDurationNanos = snapshots.stream().filter(s -> s instanceof GenericMetricsSnapshot)
                    .mapToLong(s -> ((GenericMetricsSnapshot) s).totalDurationNanos()).sum();

            // Create a generic aggregated snapshot
            GenericMetricsSnapshot aggregated = GenericMetricsSnapshot.builder(domain, operation)
                    .withMetric("total_count", totalOperations).withMetric("success_count", successfulOperations)
                    .withMetric("failure_count", failedOperations)
                    .withMetric("total_duration_nanos", totalDurationNanos).withMetric("success_rate",
                            totalOperations > 0 ? (successfulOperations * 100.0) / totalOperations : 0.0)
                    .build();

            // Convert to the requested type if possible
            if (snapshotType == GenericMetricsSnapshot.class) {
                return (T) aggregated;
            } else {
                return aggregated.as(snapshotType);
            }
        } catch (Exception e) {
            logger.warn("Failed to create aggregated snapshot for domain: {} operation: {}", domain, operation, e);
            return null;
        }
    }
}
