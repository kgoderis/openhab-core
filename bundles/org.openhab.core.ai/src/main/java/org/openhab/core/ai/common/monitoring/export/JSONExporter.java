package org.openhab.core.ai.common.monitoring.export;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.MetricsExporter;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.service.snapshot.DomainAggregatedSnapshot;
import org.openhab.core.ai.common.monitoring.service.snapshot.GenericMetricsSnapshot;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * JSON metrics exporter implementation.
 * 
 * <p>
 * This exporter formats metrics data as JSON, making it suitable for REST APIs
 * and web applications that need structured metrics data.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@Component(service = MetricsExporter.class, property = { "type=json" })
@NonNullByDefault
public class JSONExporter implements MetricsExporter {

    private static final Logger logger = LoggerFactory.getLogger(JSONExporter.class);
    private final Gson gson = new GsonBuilder().setPrettyPrinting()
            .registerTypeAdapter(Instant.class, new com.google.gson.JsonSerializer<Instant>() {
                @Override
                public com.google.gson.JsonElement serialize(Instant src, java.lang.reflect.Type typeOfSrc,
                        com.google.gson.JsonSerializationContext context) {
                    return new com.google.gson.JsonPrimitive(src.toEpochMilli());
                }
            }).create();

    @Override
    public String exportPrometheus(MetricsService metricsService) {
        logger.debug("JSONExporter does not support Prometheus export, delegating to PrometheusExporter");
        // This exporter specializes in JSON format, Prometheus should be handled by PrometheusExporter
        return "";
    }

    @Override
    public String exportJSON(MetricsService metricsService) {
        logger.debug("Exporting metrics to JSON format");

        try {
            Map<String, Object> exportData = new HashMap<>();
            exportData.put("timestamp", Instant.now().toEpochMilli());
            exportData.put("version", "1.0");

            // Export individual snapshots
            List<GenericMetricsSnapshot> snapshots = metricsService.getAllSnapshots(GenericMetricsSnapshot.class);
            List<Map<String, Object>> snapshotData = new ArrayList<>();

            for (GenericMetricsSnapshot snapshot : snapshots) {
                Map<String, Object> snapshotMap = new HashMap<>();
                snapshotMap.put("domain", snapshot.getDomain());
                snapshotMap.put("operation", snapshot.getOperation());
                snapshotMap.put("timestamp", snapshot.getTimestamp().toEpochMilli());
                snapshotMap.put("total", snapshot.total());
                snapshotMap.put("success", snapshot.success());
                snapshotMap.put("failure", snapshot.failure());
                snapshotMap.put("totalDurationNanos", snapshot.totalDurationNanos());
                snapshotMap.put("successRate",
                        snapshot.total() > 0 ? (snapshot.success() * 100.0) / snapshot.total() : 0.0);
                snapshotMap.put("averageDurationMs",
                        snapshot.total() > 0 ? (snapshot.totalDurationNanos() / 1_000_000.0) / snapshot.total() : 0.0);
                snapshotMap.put("data", snapshot.getMetrics());
                snapshotData.add(snapshotMap);
            }

            exportData.put("snapshots", snapshotData);
            exportData.put("snapshotCount", snapshotData.size());

            // Export domain aggregated snapshots
            List<Map<String, Object>> domainData = new ArrayList<>();
            // Get unique domains from snapshots
            List<String> domains = snapshots.stream().map(GenericMetricsSnapshot::getDomain).distinct().toList();

            for (String domain : domains) {
                DomainAggregatedSnapshot domainSnapshot = metricsService.getDomainAggregatedSnapshot(domain);
                Map<String, Object> domainMap = new HashMap<>();
                domainMap.put("domain", domainSnapshot.domain());
                domainMap.put("totalOperations", domainSnapshot.totalOperations());
                domainMap.put("successfulOperations", domainSnapshot.successfulOperations());
                domainMap.put("failedOperations", domainSnapshot.failedOperations());
                domainMap.put("totalDurationNanos", domainSnapshot.totalDurationNanos());
                domainMap.put("averageSuccessRate", domainSnapshot.averageSuccessRate());
                domainMap.put("successRate", domainSnapshot.getSuccessRate());
                domainMap.put("averageDurationMs", domainSnapshot.getAverageDurationMs());
                domainMap.put("operationsPerSecond", domainSnapshot.getOperationsPerSecond());
                domainMap.put("timestamp", domainSnapshot.timestamp().toEpochMilli());
                domainData.add(domainMap);
            }

            exportData.put("domains", domainData);
            exportData.put("domainCount", domainData.size());

            return gson.toJson(exportData);

        } catch (Exception e) {
            logger.warn("Failed to export metrics to JSON format", e);
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("error", "Failed to export metrics");
            errorData.put("timestamp", Instant.now().toEpochMilli());
            return gson.toJson(errorData);
        }
    }

    @Override
    public void exportJMX(MetricsService metricsService) {
        logger.debug("JSONExporter does not support JMX export, delegating to JMXExporter");
        // This exporter specializes in JSON format, JMX should be handled by JMXExporter
    }
}
