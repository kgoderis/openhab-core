package org.openhab.core.ai.common.monitoring.registry;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.Health;
import org.openhab.core.ai.common.monitoring.api.MetricKey;
import org.openhab.core.ai.common.monitoring.api.Metrics;
import org.openhab.core.ai.common.monitoring.api.Monitoring;
import org.openhab.core.ai.common.monitoring.api.Statistics;
import org.openhab.core.ai.common.monitoring.collector.MetricsCollector;

/**
 * Unified registry for managing all types of monitoring data.
 * 
 * <p>
 * This interface provides a single point of access for all monitoring data types:
 * - Performance data (metrics, timing, throughput)
 * - Statistics data (aggregated counts, trends)
 * - Health data (status, alerts, circuit breakers)
 * </p>
 * 
 * <p>
 * The registry enforces consistent taxonomy, enables enumeration for exporters,
 * supports aggregation and lifecycle management, and provides thread-safe access
 * to all monitoring data.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface MonitoringRegistry {

    // ===== Performance Data Management =====

    /**
     * Register metrics data for the given key.
     * 
     * @param key metric key
     * @param data metrics data to register
     */
    void registerMetrics(MetricKey key, Metrics data);

    /**
     * Get metrics data for the given key.
     * 
     * @param key metric key
     * @return metrics data, or null if not found
     */
    @org.eclipse.jdt.annotation.Nullable
    Metrics getMetrics(MetricKey key);

    /**
     * Get all metrics data.
     * 
     * @return map of keys to metrics data
     */
    Map<MetricKey, Metrics> getAllMetrics();

    /**
     * Get metrics data by domain.
     * 
     * @param domain domain to filter by
     * @return list of metrics data for the domain
     */
    List<Metrics> getMetricsByDomain(String domain);

    // ===== Statistics Data Management =====

    /**
     * Register statistics data for the given key.
     * 
     * @param key metric key
     * @param data statistics data to register
     */
    void registerStatistics(MetricKey key, Statistics data);

    /**
     * Get statistics data for the given key.
     * 
     * @param key metric key
     * @return statistics data, or null if not found
     */
    @org.eclipse.jdt.annotation.Nullable
    Statistics getStatistics(MetricKey key);

    /**
     * Get all statistics data.
     * 
     * @return map of keys to statistics data
     */
    Map<MetricKey, Statistics> getAllStatistics();

    /**
     * Get statistics data by domain.
     * 
     * @param domain domain to filter by
     * @return list of statistics data for the domain
     */
    List<Statistics> getStatisticsByDomain(String domain);

    // ===== Health Data Management =====

    /**
     * Register health data for the given key.
     * 
     * @param key metric key
     * @param data health data to register
     */
    void registerHealth(MetricKey key, Health data);

    /**
     * Get health data for the given key.
     * 
     * @param key metric key
     * @return health data, or null if not found
     */
    @org.eclipse.jdt.annotation.Nullable
    Health getHealth(MetricKey key);

    /**
     * Get all health data.
     * 
     * @return map of keys to health data
     */
    Map<MetricKey, Health> getAllHealth();

    /**
     * Get health data by domain.
     * 
     * @param domain domain to filter by
     * @return list of health data for the domain
     */
    List<Health> getHealthByDomain(String domain);

    // ===== Generic Monitoring Data Management =====

    /**
     * Register any type of monitoring data for the given key.
     * 
     * @param key metric key
     * @param data monitoring data to register
     */
    void registerMonitoring(MetricKey key, Monitoring data);

    /**
     * Get monitoring data for the given key.
     * 
     * @param key metric key
     * @return monitoring data, or null if not found
     */
    @org.eclipse.jdt.annotation.Nullable
    Monitoring getMonitoring(MetricKey key);

    /**
     * Get all monitoring data.
     * 
     * @return map of keys to monitoring data
     */
    Map<MetricKey, Monitoring> getAllMonitoring();

    /**
     * Get monitoring data by domain.
     * 
     * @param domain domain to filter by
     * @return list of monitoring data for the domain
     */
    List<Monitoring> getMonitoringByDomain(String domain);

    /**
     * Get monitoring data by type.
     * 
     * @param <T> monitoring data type
     * @param type monitoring data class
     * @return list of monitoring data of the specified type
     */
    <T extends Monitoring> List<T> getMonitoringByType(Class<T> type);

    // ===== Registry Management =====

    /**
     * Get all registered metric keys.
     * 
     * @return collection of metric keys
     */
    Collection<MetricKey> getAllKeys();

    /**
     * Get metric keys by domain.
     * 
     * @param domain domain to filter by
     * @return collection of metric keys for the domain
     */
    Collection<MetricKey> getKeysByDomain(String domain);

    /**
     * Check if a key is registered.
     * 
     * @param key metric key to check
     * @return true if the key is registered
     */
    boolean isRegistered(MetricKey key);

    /**
     * Remove monitoring data for the given key.
     * 
     * @param key metric key to remove
     */
    void remove(MetricKey key);

    /**
     * Clear all monitoring data.
     */
    void clear();

    /**
     * Get the total number of registered entries.
     * 
     * @return total number of entries
     */
    int size();

    /**
     * Check if the registry is empty.
     * 
     * @return true if the registry is empty
     */
    boolean isEmpty();

    // ===== Collector Management =====

    /**
     * Get a unified metrics collector for the given key.
     * 
     * @param key metric key
     * @return unified metrics collector
     */
    MetricsCollector metricsCollector(MetricKey key);

    /**
     * Reset all collectors for the given key.
     * 
     * @param key metric key
     */
    void reset(MetricKey key);

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
     * @return list of snapshots of the specified type
     */
    <S extends org.openhab.core.ai.common.monitoring.api.MetricsSnapshot> List<S> snapshots(Class<S> snapshotType);

    /**
     * Get metrics snapshots that implement the Metrics interface.
     * 
     * @return list of metrics snapshots
     */
    List<Metrics> getMetricsSnapshots();

    /**
     * Get statistics snapshots that implement the Statistics interface.
     * 
     * @return list of statistics snapshots
     */
    List<Statistics> getStatisticsSnapshots();

    /**
     * Get health snapshots that implement the Health interface.
     * 
     * @return list of health snapshots
     */
    List<Health> getHealthSnapshots();
}
