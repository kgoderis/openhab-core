package org.openhab.core.ai.common.statistics;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Base implementation of Statistics interface.
 * 
 * <p>
 * This class provides common functionality for all statistics implementations,
 * including basic properties, metric management, and utility methods.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public abstract class BaseStatistics implements Statistics {

    private final String id;
    private final @Nullable Instant timestamp;
    private final StatisticsType type;
    private final Map<String, Object> metrics;

    /**
     * Protected constructor for subclasses.
     * 
     * @param id the statistics identifier
     * @param timestamp the collection timestamp
     * @param type the statistics type
     * @param metrics the metrics map
     */
    protected BaseStatistics(String id, @Nullable Instant timestamp, StatisticsType type, Map<String, Object> metrics) {
        this.id = Objects.requireNonNull(id, "id");
        this.timestamp = timestamp;
        this.type = Objects.requireNonNull(type, "type");
        this.metrics = new HashMap<>(Objects.requireNonNull(metrics, "metrics"));
    }

    /**
     * Protected constructor for subclasses with default timestamp.
     * 
     * @param id the statistics identifier
     * @param type the statistics type
     * @param metrics the metrics map
     */
    protected BaseStatistics(String id, StatisticsType type, Map<String, Object> metrics) {
        this(id, Instant.now(), type, metrics);
    }

    /**
     * Protected constructor for subclasses with empty metrics.
     * 
     * @param id the statistics identifier
     * @param type the statistics type
     */
    protected BaseStatistics(String id, StatisticsType type) {
        this(id, type, new HashMap<>());
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public @Nullable Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public StatisticsType getType() {
        return type;
    }

    @Override
    public Map<String, Object> getMetrics() {
        return new HashMap<>(metrics);
    }

    @Override
    public @Nullable Object getMetric(String key) {
        return metrics.get(key);
    }

    @Override
    public boolean hasMetric(String key) {
        return metrics.containsKey(key);
    }

    /**
     * Add a metric to this statistics object.
     * 
     * @param key the metric key
     * @param value the metric value
     */
    protected void addMetric(String key, Object value) {
        metrics.put(Objects.requireNonNull(key, "key"), value);
    }

    /**
     * Remove a metric from this statistics object.
     * 
     * @param key the metric key
     */
    protected void removeMetric(String key) {
        metrics.remove(key);
    }

    /**
     * Clear all metrics from this statistics object.
     */
    protected void clearMetrics() {
        metrics.clear();
    }

    /**
     * Get the number of metrics in this statistics object.
     * 
     * @return the number of metrics
     */
    public int getMetricCount() {
        return metrics.size();
    }

    /**
     * Check if this statistics object has any metrics.
     * 
     * @return true if there are metrics
     */
    public boolean hasMetrics() {
        return !metrics.isEmpty();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        BaseStatistics other = (BaseStatistics) obj;
        return Objects.equals(id, other.id) && Objects.equals(timestamp, other.timestamp) && type == other.type
                && Objects.equals(metrics, other.metrics);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, timestamp, type, metrics);
    }

    @Override
    public String toString() {
        return String.format("BaseStatistics{id='%s', type=%s, timestamp=%s, metricCount=%d}", id, type, timestamp,
                getMetricCount());
    }
}
