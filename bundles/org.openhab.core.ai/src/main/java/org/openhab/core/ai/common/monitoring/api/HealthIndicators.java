package org.openhab.core.ai.common.monitoring.api;

import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Immutable record for health indicators.
 * 
 * <p>
 * This record provides a thread-safe way to share health indicator metrics
 * between collectors and snapshots.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public record HealthIndicators(Map<String, Object> indicators) {

    /**
     * Create a new HealthIndicators record.
     * 
     * @param indicators map of health indicators
     */
    public HealthIndicators {
        indicators = Map.copyOf(Objects.requireNonNull(indicators, "indicators"));
    }

    /**
     * Create a new HealthIndicators record with a single indicator.
     * 
     * @param key the indicator key
     * @param value the indicator value
     */
    public HealthIndicators(String key, Object value) {
        this(Map.of(Objects.requireNonNull(key, "key"), Objects.requireNonNull(value, "value")));
    }

    /**
     * Create an empty HealthIndicators record.
     * 
     * @return empty health indicators
     */
    public static HealthIndicators empty() {
        return new HealthIndicators(Map.of());
    }

    /**
     * Get an indicator value by key.
     * 
     * @param key the indicator key
     * @return the indicator value, or null if not found
     */
    public Object get(String key) {
        return indicators.get(key);
    }

    /**
     * Check if an indicator exists.
     * 
     * @param key the indicator key
     * @return true if the indicator exists, false otherwise
     */
    public boolean has(String key) {
        return indicators.containsKey(key);
    }

    /**
     * Get the number of indicators.
     * 
     * @return the number of indicators
     */
    public int size() {
        return indicators.size();
    }

    /**
     * Check if there are no indicators.
     * 
     * @return true if there are no indicators, false otherwise
     */
    public boolean isEmpty() {
        return indicators.isEmpty();
    }
}
