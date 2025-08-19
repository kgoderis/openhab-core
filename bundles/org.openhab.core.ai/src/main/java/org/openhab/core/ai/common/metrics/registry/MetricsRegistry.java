package org.openhab.core.ai.common.metrics.registry;

import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.metrics.api.MetricKey;
import org.openhab.core.ai.common.metrics.api.MetricsSnapshot;
import org.openhab.core.ai.common.metrics.collector.ExecutionMetricsCollector;

/**
 * Registry for managing metrics collectors.
 * 
 * <p>
 * This interface provides a single source of truth for collectors, enforces
 * taxonomy, enables enumeration for exporters, and supports aggregation and
 * lifecycle management.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface MetricsRegistry {

    /**
     * Get an execution metrics collector for the given key.
     * 
     * @param key metric key
     * @return execution metrics collector
     */
    ExecutionMetricsCollector executionCollector(MetricKey key);

    /**
     * Get all registered metric keys.
     * 
     * @return collection of metric keys
     */
    Collection<MetricKey> keys();

    /**
     * Get snapshots of a specific type.
     * 
     * @param <S> snapshot type
     * @param snapshotType snapshot class
     * @return list of snapshots
     */
    <S extends MetricsSnapshot> List<S> snapshots(Class<S> snapshotType);

    /**
     * Reset metrics for the given key.
     * 
     * @param key metric key
     */
    void reset(MetricKey key);
}
