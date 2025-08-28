package org.openhab.core.ai.common.monitoring.export;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.MetricsExporter;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.service.snapshot.GenericMetricsSnapshot;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Prometheus metrics exporter implementation.
 * 
 * <p>
 * This exporter formats metrics data according to the Prometheus exposition format,
 * making it suitable for ingestion by Prometheus monitoring systems.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@Component(service = MetricsExporter.class, property = { "type=prometheus" })
@NonNullByDefault
public class PrometheusExporter implements MetricsExporter {

    private static final Logger logger = LoggerFactory.getLogger(PrometheusExporter.class);

    @Override
    public String exportPrometheus(MetricsService metricsService) {
        logger.debug("Exporting metrics to Prometheus format");

        StringBuilder prometheusOutput = new StringBuilder();

        try {
            // Add header comment
            prometheusOutput.append("# HELP openhab_ai_operations_total Total number of operations\n");
            prometheusOutput.append("# TYPE openhab_ai_operations_total counter\n");
            prometheusOutput
                    .append("# HELP openhab_ai_operations_successful_total Total number of successful operations\n");
            prometheusOutput.append("# TYPE openhab_ai_operations_successful_total counter\n");
            prometheusOutput.append("# HELP openhab_ai_operations_failed_total Total number of failed operations\n");
            prometheusOutput.append("# TYPE openhab_ai_operations_failed_total counter\n");
            prometheusOutput
                    .append("# HELP openhab_ai_operation_duration_seconds Total operation duration in seconds\n");
            prometheusOutput.append("# TYPE openhab_ai_operation_duration_seconds counter\n");
            prometheusOutput
                    .append("# HELP openhab_ai_operation_duration_seconds_avg Average operation duration in seconds\n");
            prometheusOutput.append("# TYPE openhab_ai_operation_duration_seconds_avg gauge\n");

            // Get all snapshots and convert to Prometheus format
            List<GenericMetricsSnapshot> snapshots = metricsService.getAllSnapshots(GenericMetricsSnapshot.class);

            for (GenericMetricsSnapshot snapshot : snapshots) {
                String labels = formatLabels(snapshot.domain(), snapshot.operation());

                // Export basic metrics
                prometheusOutput
                        .append(String.format("openhab_ai_operations_total{%s} %d\n", labels, snapshot.total()));
                prometheusOutput.append(
                        String.format("openhab_ai_operations_successful_total{%s} %d\n", labels, snapshot.success()));
                prometheusOutput.append(
                        String.format("openhab_ai_operations_failed_total{%s} %d\n", labels, snapshot.failure()));

                // Export duration metrics
                double durationSeconds = snapshot.totalDurationNanos() / 1_000_000_000.0;
                prometheusOutput.append(
                        String.format("openhab_ai_operation_duration_seconds{%s} %.6f\n", labels, durationSeconds));

                if (snapshot.total() > 0) {
                    double avgDurationSeconds = durationSeconds / snapshot.total();
                    prometheusOutput.append(String.format("openhab_ai_operation_duration_seconds_avg{%s} %.6f\n",
                            labels, avgDurationSeconds));
                }
            }

        } catch (Exception e) {
            logger.warn("Failed to export metrics to Prometheus format", e);
            prometheusOutput.append("# ERROR: Failed to export metrics\n");
        }

        return prometheusOutput.toString();
    }

    @Override
    public String exportJSON(MetricsService metricsService) {
        logger.debug("PrometheusExporter does not support JSON export, delegating to JSONExporter");
        // This exporter specializes in Prometheus format, JSON should be handled by JSONExporter
        return "{}";
    }

    @Override
    public void exportJMX(MetricsService metricsService) {
        logger.debug("PrometheusExporter does not support JMX export, delegating to JMXExporter");
        // This exporter specializes in Prometheus format, JMX should be handled by JMXExporter
    }

    /**
     * Format labels for Prometheus metrics.
     * 
     * @param domain the domain
     * @param operation the operation
     * @return formatted labels string
     */
    private String formatLabels(String domain, String operation) {
        return String.format("domain=\"%s\",operation=\"%s\"", escapeLabelValue(domain), escapeLabelValue(operation));
    }

    /**
     * Escape label values for Prometheus format.
     * 
     * @param value the value to escape
     * @return escaped value
     */
    private String escapeLabelValue(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}
