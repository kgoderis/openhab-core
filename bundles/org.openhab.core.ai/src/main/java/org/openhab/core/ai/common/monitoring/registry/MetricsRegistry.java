package org.openhab.core.ai.common.monitoring.registry;

import java.util.Collection;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.MetricKey;
import org.openhab.core.ai.common.monitoring.collector.MetricsCollector;

/**
 * Registry for managing metrics collectors in the openHAB AI system.
 * 
 * <p>
 * This registry provides centralized management of MetricsCollector instances
 * by MetricKey, enabling efficient collection and retrieval of metrics data
 * across the system.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface MetricsRegistry {

    // ===== Collector Management =====

    /**
     * Get all registered metric keys.
     * 
     * @return collection of all registered metric keys
     */
    Collection<MetricKey> getAllKeys();

    /**
     * Get metric keys by domain.
     * 
     * @param domain the domain name
     * @return collection of metric keys for the domain
     */
    Collection<MetricKey> getKeysByDomain(String domain);

    /**
     * Check if a metric key is registered.
     * 
     * @param key the metric key to check
     * @return true if registered, false otherwise
     */
    boolean isRegistered(MetricKey key);

    /**
     * Remove a metric key and its associated collector.
     * 
     * @param key the metric key to remove
     */
    void removeKey(MetricKey key);

    /**
     * Get the number of registered keys.
     * 
     * @return the number of registered keys
     */
    int size();

    /**
     * Check if the registry is empty.
     * 
     * @return true if empty, false otherwise
     */
    boolean isEmpty();

    /**
     * Get a collector for the specified metric key.
     * Creates a new collector if one doesn't exist.
     * 
     * @param key the metric key
     * @return the metrics collector
     */
    MetricsCollector getCollector(MetricKey key);

    /**
     * Reset the collector for a specific metric key.
     * 
     * @param key the metric key
     */
    void resetCollector(MetricKey key);

    /**
     * Reset all collectors and clear all data.
     */
    void resetAllCollectors();
}
