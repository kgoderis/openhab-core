package org.openhab.core.ai.common.metrics.api;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Interface for identifying metric streams.
 * 
 * <p>
 * This interface provides a way to identify metric streams with bounded labels
 * to control cardinality.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface MetricKey {

    /**
     * Get the kind of metric.
     * 
     * @return metric kind
     */
    String kind();

    /**
     * Get the labels for this metric.
     * 
     * @return map of label key-value pairs
     */
    Map<String, String> labels();

    /**
     * Get a unique identifier for this metric key.
     * 
     * @return unique identifier
     */
    default String id() {
        return kind() + "|" + labels().entrySet().stream().sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey() + "=" + e.getValue()).reduce((a, b) -> a + "," + b).orElse("");
    }
}
