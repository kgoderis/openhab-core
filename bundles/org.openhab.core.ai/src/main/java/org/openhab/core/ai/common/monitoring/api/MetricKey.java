package org.openhab.core.ai.common.monitoring.api;

import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Interface for identifying metric streams with bounded labels and capabilities.
 * 
 * <p>
 * MetricKey provides a way to uniquely identify metric streams and declare
 * what capabilities they support. This enables type-safe snapshot creation
 * and validation of compatible snapshot types.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface MetricKey {
    
    /**
     * Get the kind/type of metric this key represents.
     * 
     * @return the metric kind
     */
    String kind();
    
    /**
     * Get the labels associated with this metric key.
     * Labels provide additional context and filtering capabilities.
     * 
     * @return the labels map
     */
    Map<String, String> labels();
    
    /**
     * Get the capabilities this metric key supports.
     * Capabilities determine what types of snapshots can be created from this key.
     * 
     * @return the set of supported capabilities
     */
    Set<String> capabilities();
    
    /**
     * Get a unique identifier for this metric key.
     * 
     * @return the unique identifier
     */
    default String id() {
        return kind() + "|" + labels().entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(e -> e.getKey() + "=" + e.getValue())
            .reduce((a, b) -> a + "," + b).orElse("");
    }
}
