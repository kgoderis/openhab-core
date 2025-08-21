package org.openhab.core.ai.common.monitoring.registry;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.Health;
import org.openhab.core.ai.common.monitoring.api.MetricKey;
import org.openhab.core.ai.common.monitoring.api.MetricKeys;
import org.openhab.core.ai.common.monitoring.api.Metrics;
import org.openhab.core.ai.common.monitoring.api.Monitoring;
import org.openhab.core.ai.common.monitoring.api.Statistics;
import org.openhab.core.ai.common.monitoring.collector.ExecutionMetricsCollector;
import org.openhab.core.ai.common.monitoring.collector.ProviderHealthCollector;
import org.openhab.core.ai.common.monitoring.snapshot.ExecutionMetricsSnapshot;
import org.osgi.service.component.annotations.Component;

/**
 * Default implementation of MonitoringRegistry.
 * 
 * <p>
 * This implementation provides thread-safe access to all monitoring data types
 * using ConcurrentHashMap for storing data. It supports:
 * - Performance data (metrics, timing, throughput)
 * - Statistics data (aggregated counts, trends)
 * - Health data (status, alerts, circuit breakers)
 * </p>
 * 
 * <p>
 * The registry maintains separate collections for each data type while providing
 * unified access methods. All operations are thread-safe and support concurrent
 * access patterns.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@Component(service = MonitoringRegistry.class)
@NonNullByDefault
public final class DefaultMonitoringRegistry implements MonitoringRegistry {

    // Separate collections for each data type for efficient access
    private final ConcurrentMap<String, Metrics> metricsData = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Statistics> statisticsData = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Health> healthData = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Monitoring> allMonitoringData = new ConcurrentHashMap<>();

    // Collector management for thread-safe metrics collection
    private final ConcurrentMap<String, ExecutionMetricsCollector> executionCollectors = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, ProviderHealthCollector> healthCollectors = new ConcurrentHashMap<>();

    // ===== Metrics Management =====

    @Override
    public void registerMetrics(MetricKey key, Metrics data) {
        String keyId = key.id();
        metricsData.put(keyId, data);
        allMonitoringData.put(keyId, data);
    }

    @Override
    public @org.eclipse.jdt.annotation.Nullable Metrics getMetrics(MetricKey key) {
        return metricsData.get(key.id());
    }

    @Override
    public Map<MetricKey, Metrics> getAllMetrics() {
        return metricsData.entrySet().stream().collect(
                Collectors.toConcurrentMap(entry -> createMetricKeyFromId(entry.getKey()), Map.Entry::getValue));
    }

    @Override
    public List<Metrics> getMetricsByDomain(String domain) {
        return metricsData.values().stream().filter(data -> domain.equals(data.getDomain()))
                .collect(Collectors.toList());
    }

    // ===== Statistics Management =====

    @Override
    public void registerStatistics(MetricKey key, Statistics data) {
        String keyId = key.id();
        statisticsData.put(keyId, data);
        allMonitoringData.put(keyId, data);
    }

    @Override
    public @org.eclipse.jdt.annotation.Nullable Statistics getStatistics(MetricKey key) {
        return statisticsData.get(key.id());
    }

    @Override
    public Map<MetricKey, Statistics> getAllStatistics() {
        return statisticsData.entrySet().stream().collect(
                Collectors.toConcurrentMap(entry -> createMetricKeyFromId(entry.getKey()), Map.Entry::getValue));
    }

    @Override
    public List<Statistics> getStatisticsByDomain(String domain) {
        return statisticsData.values().stream().filter(data -> domain.equals(data.getDomain()))
                .collect(Collectors.toList());
    }

    // ===== Health Management =====

    @Override
    public void registerHealth(MetricKey key, Health data) {
        String keyId = key.id();
        healthData.put(keyId, data);
        allMonitoringData.put(keyId, data);
    }

    @Override
    public @org.eclipse.jdt.annotation.Nullable Health getHealth(MetricKey key) {
        return healthData.get(key.id());
    }

    @Override
    public Map<MetricKey, Health> getAllHealth() {
        return healthData.entrySet().stream().collect(
                Collectors.toConcurrentMap(entry -> createMetricKeyFromId(entry.getKey()), Map.Entry::getValue));
    }

    @Override
    public List<Health> getHealthByDomain(String domain) {
        return healthData.values().stream().filter(data -> domain.equals(data.getDomain()))
                .collect(Collectors.toList());
    }

    // ===== Generic Monitoring Management =====

    @Override
    public void registerMonitoring(MetricKey key, Monitoring data) {
        String keyId = key.id();
        allMonitoringData.put(keyId, data);

        // Also register in the appropriate type-specific collection
        if (data instanceof Metrics) {
            metricsData.put(keyId, (Metrics) data);
        } else if (data instanceof Statistics) {
            statisticsData.put(keyId, (Statistics) data);
        } else if (data instanceof Health) {
            healthData.put(keyId, (Health) data);
        }
    }

    @Override
    public @org.eclipse.jdt.annotation.Nullable Monitoring getMonitoring(MetricKey key) {
        return allMonitoringData.get(key.id());
    }

    @Override
    public Map<MetricKey, Monitoring> getAllMonitoring() {
        return allMonitoringData.entrySet().stream().collect(
                Collectors.toConcurrentMap(entry -> createMetricKeyFromId(entry.getKey()), Map.Entry::getValue));
    }

    @Override
    public List<Monitoring> getMonitoringByDomain(String domain) {
        return allMonitoringData.values().stream().filter(data -> domain.equals(data.getDomain()))
                .collect(Collectors.toList());
    }

    @Override
    public <T extends Monitoring> List<T> getMonitoringByType(Class<T> type) {
        return allMonitoringData.values().stream().filter(type::isInstance).map(type::cast)
                .collect(Collectors.toList());
    }

    // ===== Registry Management =====

    @Override
    public Collection<MetricKey> getAllKeys() {
        return allMonitoringData.keySet().stream().map(this::createMetricKeyFromId).collect(Collectors.toList());
    }

    @Override
    public Collection<MetricKey> getKeysByDomain(String domain) {
        return allMonitoringData.entrySet().stream().filter(entry -> domain.equals(entry.getValue().getDomain()))
                .map(entry -> createMetricKeyFromId(entry.getKey())).collect(Collectors.toList());
    }

    @Override
    public boolean isRegistered(MetricKey key) {
        return allMonitoringData.containsKey(key.id());
    }

    @Override
    public void remove(MetricKey key) {
        String keyId = key.id();
        allMonitoringData.remove(keyId);
        metricsData.remove(keyId);
        statisticsData.remove(keyId);
        healthData.remove(keyId);
    }

    @Override
    public void clear() {
        allMonitoringData.clear();
        metricsData.clear();
        statisticsData.clear();
        healthData.clear();
    }

    @Override
    public int size() {
        return allMonitoringData.size();
    }

    @Override
    public boolean isEmpty() {
        return allMonitoringData.isEmpty();
    }

    // ===== Collector Management =====

    @Override
    public ExecutionMetricsCollector executionCollector(MetricKey key) {
        return executionCollectors.computeIfAbsent(key.id(), k -> new ExecutionMetricsCollector());
    }

    @Override
    public ProviderHealthCollector healthCollector(MetricKey key) {
        return healthCollectors.computeIfAbsent(key.id(), k -> new ProviderHealthCollector());
    }

    @Override
    public void reset(MetricKey key) {
        String keyId = key.id();
        ExecutionMetricsCollector execCollector = executionCollectors.get(keyId);
        if (execCollector != null) {
            execCollector.reset();
        }
        ProviderHealthCollector healthCollector = healthCollectors.get(keyId);
        if (healthCollector != null) {
            healthCollector.reset();
        }
    }

    /**
     * Reset all collectors and clear registry data.
     */
    public void resetAll() {
        executionCollectors.values().forEach(ExecutionMetricsCollector::reset);
        healthCollectors.values().forEach(ProviderHealthCollector::reset);
        metricsData.clear();
        statisticsData.clear();
        healthData.clear();
        allMonitoringData.clear();
    }

    /**
     * Get aggregated metrics across all execution collectors.
     * 
     * @return aggregated execution metrics snapshot
     */
    public ExecutionMetricsSnapshot getAggregatedExecutionMetrics() {
        long totalOps = 0;
        long successOps = 0;
        long totalDurationNanos = 0;

        for (ExecutionMetricsCollector collector : executionCollectors.values()) {
            if (collector.hasData()) {
                totalOps += collector.getCurrentTotal();
                successOps += collector.getCurrentSuccess();
                ExecutionMetricsSnapshot snapshot = collector.snapshot();
                totalDurationNanos += snapshot.totalDurationNanos();
            }
        }

        return new ExecutionMetricsSnapshot(
                new org.openhab.core.ai.common.monitoring.api.Counts(totalOps, successOps,
                        Math.max(0, totalOps - successOps)),
                new org.openhab.core.ai.common.monitoring.api.Timing(totalDurationNanos), System.currentTimeMillis());
    }

    /**
     * Get count of active collectors (with data).
     * 
     * @return count of collectors that have recorded data
     */
    public int getActiveCollectorCount() {
        int count = 0;
        for (ExecutionMetricsCollector collector : executionCollectors.values()) {
            if (collector.hasData()) {
                count++;
            }
        }
        for (ProviderHealthCollector collector : healthCollectors.values()) {
            if (collector.hasData()) {
                count++;
            }
        }
        return count;
    }

    @Override
    public Collection<MetricKey> keys() {
        return getAllKeys();
    }

    @Override
    public <S extends org.openhab.core.ai.common.monitoring.api.MetricsSnapshot> List<S> snapshots(
            Class<S> snapshotType) {
        // This is a simplified implementation - in a real implementation,
        // we would collect snapshots from all collectors and filter by type
        return List.of(); // Placeholder implementation
    }

    @Override
    public List<Metrics> getMetricsSnapshots() {
        return executionCollectors.values().stream().map(ExecutionMetricsCollector::snapshot)
                .filter(snapshot -> snapshot instanceof Metrics).map(snapshot -> (Metrics) snapshot)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<Statistics> getStatisticsSnapshots() {
        // Placeholder implementation - would collect from statistics collectors
        return List.of();
    }

    @Override
    public List<Health> getHealthSnapshots() {
        return healthCollectors.values().stream().map(ProviderHealthCollector::snapshot)
                .filter(snapshot -> snapshot instanceof Health).map(snapshot -> (Health) snapshot)
                .collect(java.util.stream.Collectors.toList());
    }

    // ===== Helper Methods =====

    /**
     * Create a MetricKey from a key ID string.
     * 
     * <p>
     * This is a simplified implementation. In a real implementation, we would
     * need to parse the key ID back into a proper MetricKey object with
     * kind and labels.
     * </p>
     * 
     * @param keyId the key ID string
     * @return a MetricKey object
     */
    private MetricKey createMetricKeyFromId(String keyId) {
        // This is a simplified implementation - in a real implementation,
        // we would need to parse the key ID back into a proper MetricKey
        return new MetricKeys.SimpleMetricKey("unknown", Map.of("id", keyId));
    }
}
