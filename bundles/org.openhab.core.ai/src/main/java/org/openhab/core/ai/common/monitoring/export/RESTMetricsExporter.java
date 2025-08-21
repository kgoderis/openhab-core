package org.openhab.core.ai.common.monitoring.export;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.Health;
import org.openhab.core.ai.common.monitoring.api.Metrics;
import org.openhab.core.ai.common.monitoring.api.Statistics;
import org.openhab.core.ai.common.monitoring.registry.MonitoringRegistry;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * REST metrics exporter for exposing monitoring data via HTTP endpoints.
 * 
 * This exporter provides JSON serialization of all snapshot types and REST endpoints
 * for accessing performance, statistics, and health metrics data.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
@Component(service = RESTMetricsExporter.class)
public class RESTMetricsExporter {

    @Reference
    private @Nullable MonitoringRegistry monitoringRegistry;

    private final Map<String, Object> responseCache = new ConcurrentHashMap<>();
    private static final long CACHE_TTL_MS = 5000; // 5 seconds cache
    private volatile long lastCacheUpdate = 0;

    /**
     * Get all performance metrics as JSON-serializable data.
     * 
     * @return map containing all performance metrics data
     */
    public Map<String, Object> getPerformanceMetrics() {
        MonitoringRegistry registry = monitoringRegistry;
        if (registry == null) {
            return Map.of("error", "Monitoring registry not available");
        }

        List<Metrics> metrics = registry.getMetricsSnapshots();
        return Map.of("timestamp", System.currentTimeMillis(), "count", metrics.size(), "metrics",
                metrics.stream().map(this::serializeMetrics).collect(Collectors.toList()));
    }

    /**
     * Get all statistics data as JSON-serializable data.
     * 
     * @return map containing all statistics data
     */
    public Map<String, Object> getStatisticsData() {
        MonitoringRegistry registry = monitoringRegistry;
        if (registry == null) {
            return Map.of("error", "Monitoring registry not available");
        }

        List<Statistics> statistics = registry.getStatisticsSnapshots();
        return Map.of("timestamp", System.currentTimeMillis(), "count", statistics.size(), "statistics",
                statistics.stream().map(this::serializeStatistics).collect(Collectors.toList()));
    }

    /**
     * Get all health data as JSON-serializable data.
     * 
     * @return map containing all health data
     */
    public Map<String, Object> getHealthData() {
        MonitoringRegistry registry = monitoringRegistry;
        if (registry == null) {
            return Map.of("error", "Monitoring registry not available");
        }

        List<Health> healthData = registry.getHealthSnapshots();
        return Map.of("timestamp", System.currentTimeMillis(), "count", healthData.size(), "health",
                healthData.stream().map(this::serializeHealth).collect(Collectors.toList()));
    }

    /**
     * Get aggregated metrics summary.
     * 
     * @return map containing aggregated metrics summary
     */
    public Map<String, Object> getMetricsSummary() {
        MonitoringRegistry registry = monitoringRegistry;
        if (registry == null) {
            return Map.of("error", "Monitoring registry not available");
        }

        long currentTime = System.currentTimeMillis();

        // Check cache
        if (currentTime - lastCacheUpdate < CACHE_TTL_MS && !responseCache.isEmpty()) {
            return responseCache;
        }

        List<Metrics> metrics = registry.getMetricsSnapshots();
        List<Statistics> statistics = registry.getStatisticsSnapshots();
        List<Health> healthData = registry.getHealthSnapshots();

        Map<String, Object> summary = Map.of("timestamp", currentTime, "totalMetrics", metrics.size(),
                "totalStatistics", statistics.size(), "totalHealth", healthData.size(), "activeCollectors",
                registry.keys().size(), "systemStatus", calculateSystemStatus(healthData));

        // Update cache
        responseCache.clear();
        responseCache.putAll(summary);
        lastCacheUpdate = currentTime;

        return summary;
    }

    /**
     * Get metrics filtered by domain.
     * 
     * @param domain the domain to filter by
     * @return filtered metrics data
     */
    public Map<String, Object> getMetricsByDomain(String domain) {
        MonitoringRegistry registry = monitoringRegistry;
        if (registry == null) {
            return Map.of("error", "Monitoring registry not available");
        }

        List<Metrics> filteredMetrics = registry.getMetricsSnapshots().stream()
                .filter(metric -> domain.equals(metric.getDomain())).collect(Collectors.toList());

        return Map.of("timestamp", System.currentTimeMillis(), "domain", domain, "count", filteredMetrics.size(),
                "metrics", filteredMetrics.stream().map(this::serializeMetrics).collect(Collectors.toList()));
    }

    /**
     * Get paginated metrics data.
     * 
     * @param page the page number (0-based)
     * @param size the page size
     * @return paginated metrics data
     */
    public Map<String, Object> getPaginatedMetrics(int page, int size) {
        MonitoringRegistry registry = monitoringRegistry;
        if (registry == null) {
            return Map.of("error", "Monitoring registry not available");
        }

        List<Metrics> allMetrics = registry.getMetricsSnapshots();
        int total = allMetrics.size();
        int start = page * size;
        int end = Math.min(start + size, total);

        List<Metrics> pageMetrics = allMetrics.subList(start, end);

        return Map.of("timestamp", System.currentTimeMillis(), "page", page, "size", size, "total", total, "totalPages",
                (total + size - 1) / size, "hasNext", end < total, "hasPrevious", page > 0, "metrics",
                pageMetrics.stream().map(this::serializeMetrics).collect(Collectors.toList()));
    }

    // ===== Private Helper Methods =====

    /**
     * Serialize metrics data for JSON export.
     * 
     * @param metrics the metrics to serialize
     * @return serialized metrics data
     */
    private Map<String, Object> serializeMetrics(Metrics metrics) {
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("id", metrics.getId());
        result.put("timestamp", metrics.getTimestamp().toEpochMilli());
        result.put("type", metrics.getType().name());
        result.put("domain", metrics.getDomain());
        result.put("source", metrics.getSource());
        result.put("totalOperations", metrics.getTotalOperations());
        result.put("successfulOperations", metrics.getSuccessfulOperations());
        result.put("failedOperations", metrics.getFailedOperations());
        result.put("totalProcessingTime", metrics.getTotalProcessingTime());
        result.put("averageResponseTime", metrics.getAverageResponseTime());
        result.put("lastOperationTime", metrics.getLastOperationTime());
        result.put("data", metrics.getData());
        return result;
    }

    /**
     * Serialize statistics data for JSON export.
     * 
     * @param statistics the statistics to serialize
     * @return serialized statistics data
     */
    private Map<String, Object> serializeStatistics(Statistics statistics) {
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("id", statistics.getId());
        result.put("timestamp", statistics.getTimestamp().toEpochMilli());
        result.put("type", statistics.getType().name());
        result.put("domain", statistics.getDomain());
        result.put("source", statistics.getSource());
        result.put("totalCount", statistics.getTotalCount());
        result.put("successCount", statistics.getSuccessCount());
        result.put("failureCount", statistics.getFailureCount());
        result.put("successRate", statistics.getSuccessRate());
        result.put("data", statistics.getData());
        return result;
    }

    /**
     * Serialize health data for JSON export.
     * 
     * @param health the health data to serialize
     * @return serialized health data
     */
    private Map<String, Object> serializeHealth(Health health) {
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("id", health.getId());
        result.put("timestamp", health.getTimestamp().toEpochMilli());
        result.put("type", health.getType().name());
        result.put("domain", health.getDomain());
        result.put("source", health.getSource());
        result.put("status", health.getStatus().name());
        result.put("statusMessage", health.getStatusMessage());
        result.put("healthIndicators", health.getHealthIndicators());
        result.put("data", health.getData());
        return result;
    }

    /**
     * Calculate overall system status based on health data.
     * 
     * @param healthData list of health snapshots
     * @return overall system status
     */
    private String calculateSystemStatus(List<Health> healthData) {
        if (healthData.isEmpty()) {
            return "UNKNOWN";
        }

        long healthyCount = healthData.stream().mapToLong(h -> "HEALTHY".equals(h.getStatus().name()) ? 1 : 0).sum();

        double healthRatio = (double) healthyCount / healthData.size();

        if (healthRatio >= 0.9) {
            return "HEALTHY";
        } else if (healthRatio >= 0.7) {
            return "DEGRADED";
        } else if (healthRatio >= 0.3) {
            return "WARNING";
        } else {
            return "CRITICAL";
        }
    }

    /**
     * Clear the response cache.
     */
    public void clearCache() {
        responseCache.clear();
        lastCacheUpdate = 0;
    }
}
