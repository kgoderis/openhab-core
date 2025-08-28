package org.openhab.core.ai.common.monitoring.registry;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.MetricKey;
import org.openhab.core.ai.common.monitoring.api.MetricKeys;
import org.openhab.core.ai.common.monitoring.collector.MetricsCollector;
import org.osgi.service.component.annotations.Component;

/**
 * Default implementation of MetricsRegistry.
 * 
 * <p>
 * This implementation provides thread-safe access to metrics collectors
 * using ConcurrentHashMap for storing data. It supports:
 * - Real-time data collection via collectors
 * - Thread-safe collector management
 * - Efficient key-based access patterns
 * </p>
 * 
 * <p>
 * The registry maintains collections for metrics collectors while providing
 * unified access methods. All operations are thread-safe and support concurrent
 * access patterns.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@Component(service = MetricsRegistry.class)
@NonNullByDefault
public final class DefaultMetricsRegistry implements MetricsRegistry {

    // Unified collector management for thread-safe metrics collection
    private final ConcurrentMap<String, MetricsCollector> metricsCollectors = new ConcurrentHashMap<>();

    // ===== Registry Management =====

    @Override
    public Collection<MetricKey> getAllKeys() {
        return metricsCollectors.keySet().stream().map(this::createMetricKeyFromId).collect(Collectors.toList());
    }

    @Override
    public Collection<MetricKey> getKeysByDomain(String domain) {
        return metricsCollectors.entrySet().stream().filter(entry -> {
            MetricsCollector collector = entry.getValue();
            // Extract domain from collector data if available
            // For now, return all keys since we don't store domain separately
            return true;
        }).map(entry -> createMetricKeyFromId(entry.getKey())).collect(Collectors.toList());
    }

    @Override
    public boolean isRegistered(MetricKey key) {
        return metricsCollectors.containsKey(key.id());
    }

    @Override
    public void removeKey(MetricKey key) {
        String keyId = key.id();
        metricsCollectors.remove(keyId);
    }

    @Override
    public int size() {
        return metricsCollectors.size();
    }

    @Override
    public boolean isEmpty() {
        return metricsCollectors.isEmpty();
    }

    // ===== Collector Management =====

    @Override
    public MetricsCollector getCollector(MetricKey key) {
        return metricsCollectors.computeIfAbsent(key.id(), k -> new MetricsCollector());
    }

    @Override
    public void resetCollector(MetricKey key) {
        String keyId = key.id();
        MetricsCollector collector = metricsCollectors.get(keyId);
        if (collector != null) {
            collector.reset();
        }
    }

    @Override
    public void resetAllCollectors() {
        metricsCollectors.values().forEach(MetricsCollector::reset);
        metricsCollectors.clear();
    }

    /**
     * Get count of active collectors (with data).
     * 
     * @return count of collectors that have recorded data
     */
    public int getActiveCollectorCount() {
        int count = 0;
        for (MetricsCollector collector : metricsCollectors.values()) {
            if (collector.hasData()) {
                count++;
            }
        }
        return count;
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
        return new MetricKeys.SimpleMetricKey("unknown", Map.of("id", keyId), Set.of());
    }
}
