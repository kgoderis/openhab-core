package org.openhab.core.ai.common.metrics.registry;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.metrics.api.MetricKey;
import org.openhab.core.ai.common.metrics.api.MetricsSnapshot;
import org.openhab.core.ai.common.metrics.collector.ExecutionMetricsCollector;
import org.osgi.service.component.annotations.Component;

/**
 * Default implementation of MetricsRegistry.
 * 
 * <p>
 * This implementation provides thread-safe metrics collection using
 * ConcurrentHashMap for storing collectors.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@Component(service = MetricsRegistry.class)
@NonNullByDefault
public final class DefaultMetricsRegistry implements MetricsRegistry {

    private final ConcurrentMap<String, ExecutionMetricsCollector> collectors = new ConcurrentHashMap<>();

    @Override
    public ExecutionMetricsCollector executionCollector(MetricKey key) {
        return collectors.computeIfAbsent(key.id(), k -> new ExecutionMetricsCollector());
    }

    @Override
    public Collection<MetricKey> keys() {
        // This is a simplified implementation - in a real implementation,
        // we would need to maintain a separate collection of keys
        return List.of();
    }

    @Override
    public <S extends MetricsSnapshot> List<S> snapshots(Class<S> snapshotType) {
        // This is a simplified implementation - in a real implementation,
        // we would need to collect snapshots from all collectors
        return List.of();
    }

    @Override
    public void reset(MetricKey key) {
        collectors.remove(key.id());
    }
}
