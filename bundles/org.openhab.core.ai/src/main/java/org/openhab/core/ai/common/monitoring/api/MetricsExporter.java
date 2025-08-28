package org.openhab.core.ai.common.monitoring.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Interface for exporting metrics data in various formats.
 * 
 * <p>
 * This interface provides methods for exporting metrics data to different formats
 * such as Prometheus, JSON, and JMX. Implementations should handle the specific
 * formatting requirements for each export format.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface MetricsExporter {

    /**
     * Export metrics to Prometheus format.
     * 
     * @param metricsService the metrics service to export from
     * @return Prometheus-formatted metrics string
     */
    String exportPrometheus(MetricsService metricsService);

    /**
     * Export metrics to JSON format.
     * 
     * @param metricsService the metrics service to export from
     * @return JSON-formatted metrics string
     */
    String exportJSON(MetricsService metricsService);

    /**
     * Export metrics to JMX.
     * 
     * @param metricsService the metrics service to export from
     */
    void exportJMX(MetricsService metricsService);
}
