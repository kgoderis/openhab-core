package org.openhab.core.ai.common.monitoring.api;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.service.snapshot.GenericMetricsSnapshot;

/**
 * Utility class for building standardized JSON responses from MetricsService snapshots.
 * 
 * <p>
 * This class provides a consistent JSON structure for all metrics endpoints following
 * the standardized format defined in the metrics plan. It transforms MetricsService
 * snapshots into a uniform response format that includes:
 * - Standard metadata (timestamp, domain, operation)
 * - Structured metrics (counts, latency, domain-specific)
 * - Consistent field naming and data types
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class MetricsResponseBuilder {

    private MetricsResponseBuilder() {
        // Utility class - prevent instantiation
    }

    /**
     * Build a standardized metrics response from a single snapshot.
     * 
     * @param snapshot the metrics snapshot
     * @return standardized JSON response map
     */
    public static Map<String, Object> buildResponse(MetricsSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot cannot be null");

        Map<String, Object> response = new HashMap<>();

        // Standard metadata
        response.put("timestamp", Instant.ofEpochMilli(snapshot.getTimestampMs()).toString());

        // Extract domain and operation from snapshot
        String domain = extractDomain(snapshot);
        String operation = extractOperation(snapshot);

        response.put("domain", domain);
        response.put("operation", operation);

        // Build structured metrics
        Map<String, Object> metrics = buildMetricsStructure(snapshot);
        response.put("metrics", metrics);

        return response;
    }

    /**
     * Build a standardized metrics response from multiple snapshots.
     * 
     * @param snapshots list of metrics snapshots
     * @return standardized JSON response map with aggregated data
     */
    public static Map<String, Object> buildResponse(List<MetricsSnapshot> snapshots) {
        Objects.requireNonNull(snapshots, "snapshots cannot be null");

        if (snapshots.isEmpty()) {
            return buildEmptyResponse();
        }

        Map<String, Object> response = new HashMap<>();

        // Use the most recent timestamp
        long latestTimestamp = snapshots.stream().mapToLong(MetricsSnapshot::getTimestampMs).max()
                .orElse(System.currentTimeMillis());

        response.put("timestamp", Instant.ofEpochMilli(latestTimestamp).toString());

        // Aggregate domain and operation information
        String aggregatedDomain = aggregateDomains(snapshots);
        String aggregatedOperation = aggregateOperations(snapshots);

        response.put("domain", aggregatedDomain);
        response.put("operation", aggregatedOperation);

        // Build aggregated metrics structure
        Map<String, Object> metrics = buildAggregatedMetricsStructure(snapshots);
        response.put("metrics", metrics);

        return response;
    }

    /**
     * Build a standardized metrics response for a specific domain and operation.
     * 
     * @param domain the operation domain
     * @param operation the operation name
     * @param snapshot the metrics snapshot
     * @return standardized JSON response map
     */
    public static Map<String, Object> buildResponse(String domain, String operation, MetricsSnapshot snapshot) {
        Objects.requireNonNull(domain, "domain cannot be null");
        Objects.requireNonNull(operation, "operation cannot be null");
        Objects.requireNonNull(snapshot, "snapshot cannot be null");

        Map<String, Object> response = new HashMap<>();

        response.put("timestamp", Instant.ofEpochMilli(snapshot.getTimestampMs()).toString());
        response.put("domain", domain);
        response.put("operation", operation);

        Map<String, Object> metrics = buildMetricsStructure(snapshot);
        response.put("metrics", metrics);

        return response;
    }

    /**
     * Build an empty response for cases with no data.
     * 
     * @return empty standardized JSON response map
     */
    public static Map<String, Object> buildEmptyResponse() {
        Map<String, Object> response = new HashMap<>();

        response.put("timestamp", Instant.now().toString());
        response.put("domain", "unknown");
        response.put("operation", "none");

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("counts", Map.of("total", 0L, "success", 0L, "failure", 0L, "successRate", 0.0));
        metrics.put("latency", Map.of("averageMs", 0.0, "minMs", 0.0, "maxMs", 0.0, "totalDurationNanos", 0L));
        metrics.put("domain_specific", Map.of());

        response.put("metrics", metrics);

        return response;
    }

    // ===== Private Helper Methods =====

    /**
     * Build the structured metrics object from a single snapshot.
     */
    private static Map<String, Object> buildMetricsStructure(MetricsSnapshot snapshot) {
        Map<String, Object> metrics = new HashMap<>();

        // Build counts section
        Map<String, Object> counts = buildCountsSection(snapshot);
        metrics.put("counts", counts);

        // Build latency section
        Map<String, Object> latency = buildLatencySection(snapshot);
        metrics.put("latency", latency);

        // Build domain-specific section
        Map<String, Object> domainSpecific = buildDomainSpecificSection(snapshot);
        metrics.put("domain_specific", domainSpecific);

        return metrics;
    }

    /**
     * Build the aggregated metrics structure from multiple snapshots.
     */
    private static Map<String, Object> buildAggregatedMetricsStructure(List<MetricsSnapshot> snapshots) {
        Map<String, Object> metrics = new HashMap<>();

        // Aggregate counts across all snapshots
        Map<String, Object> counts = buildAggregatedCountsSection(snapshots);
        metrics.put("counts", counts);

        // Aggregate latency across all snapshots
        Map<String, Object> latency = buildAggregatedLatencySection(snapshots);
        metrics.put("latency", latency);

        // Aggregate domain-specific metrics
        Map<String, Object> domainSpecific = buildAggregatedDomainSpecificSection(snapshots);
        metrics.put("domain_specific", domainSpecific);

        return metrics;
    }

    /**
     * Build the counts section from a snapshot.
     */
    private static Map<String, Object> buildCountsSection(MetricsSnapshot snapshot) {
        Map<String, Object> counts = new HashMap<>();

        if (snapshot instanceof GenericMetricsSnapshot) {
            GenericMetricsSnapshot generic = (GenericMetricsSnapshot) snapshot;

            long total = generic.getTotal();
            long success = generic.getSuccess();
            long failure = generic.getFailure();
            double successRate = generic.getSuccessRate();

            counts.put("total", total);
            counts.put("success", success);
            counts.put("failure", failure);
            counts.put("successRate", successRate);
        } else if (snapshot instanceof CountsMetrics) {
            CountsMetrics countsMetrics = (CountsMetrics) snapshot;

            counts.put("total", countsMetrics.total());
            counts.put("success", countsMetrics.success());
            counts.put("failure", countsMetrics.failure());
            counts.put("successRate", countsMetrics.successRate());
        } else {
            // Fallback for unknown snapshot types
            counts.put("total", 0L);
            counts.put("success", 0L);
            counts.put("failure", 0L);
            counts.put("successRate", 0.0);
        }

        return counts;
    }

    /**
     * Build the latency section from a snapshot.
     */
    private static Map<String, Object> buildLatencySection(MetricsSnapshot snapshot) {
        Map<String, Object> latency = new HashMap<>();

        if (snapshot instanceof GenericMetricsSnapshot) {
            GenericMetricsSnapshot generic = (GenericMetricsSnapshot) snapshot;

            long totalDurationNanos = generic.getTotalDurationNanos();
            double averageMs = generic.getAverageMs();

            latency.put("averageMs", averageMs);
            latency.put("minMs", generic.getDouble("minMs", 0.0));
            latency.put("maxMs", generic.getDouble("maxMs", 0.0));
            latency.put("totalDurationNanos", totalDurationNanos);
        } else if (snapshot instanceof LatencyMetrics) {
            LatencyMetrics latencyMetrics = (LatencyMetrics) snapshot;

            // For LatencyMetrics, we need to get total count from CountsMetrics if available
            long totalCount = 1L; // Default to 1 if no count information available
            if (snapshot instanceof CountsMetrics) {
                totalCount = ((CountsMetrics) snapshot).total();
            }

            latency.put("averageMs", latencyMetrics.averageMs(totalCount));
            latency.put("minMs", 0.0); // LatencyMetrics doesn't provide min/max
            latency.put("maxMs", 0.0);
            latency.put("totalDurationNanos", latencyMetrics.totalDurationNanos());
        } else {
            // Fallback for unknown snapshot types
            latency.put("averageMs", 0.0);
            latency.put("minMs", 0.0);
            latency.put("maxMs", 0.0);
            latency.put("totalDurationNanos", 0L);
        }

        return latency;
    }

    /**
     * Build the domain-specific section from a snapshot.
     */
    private static Map<String, Object> buildDomainSpecificSection(MetricsSnapshot snapshot) {
        Map<String, Object> domainSpecific = new HashMap<>();

        if (snapshot instanceof GenericMetricsSnapshot) {
            GenericMetricsSnapshot generic = (GenericMetricsSnapshot) snapshot;

            // Add model-specific metrics if available
            if (generic.hasModelMetrics()) {
                domainSpecific.put("tokensPerSecond", generic.getDouble("tokensPerSecond", 0.0));
                domainSpecific.put("costPerRequest", generic.getCost());
                domainSpecific.put("averageTokensPerRequest", generic.getTokens());
            }

            // Add any other domain-specific metrics
            Map<String, Object> allMetrics = generic.getMetrics();
            for (Map.Entry<String, Object> entry : allMetrics.entrySet()) {
                String key = entry.getKey();
                if (!isStandardMetric(key)) {
                    domainSpecific.put(key, entry.getValue());
                }
            }
        } else if (snapshot instanceof ModelMetrics) {
            ModelMetrics modelMetrics = (ModelMetrics) snapshot;

            domainSpecific.put("tokensPerSecond", modelMetrics.tokensPerSecond());
            domainSpecific.put("costPerRequest", modelMetrics.costPerRequest());
            domainSpecific.put("averageTokensPerRequest", modelMetrics.averageTokensPerRequest());
        }

        return domainSpecific;
    }

    /**
     * Build aggregated counts section from multiple snapshots.
     */
    private static Map<String, Object> buildAggregatedCountsSection(List<MetricsSnapshot> snapshots) {
        long totalTotal = 0L;
        long totalSuccess = 0L;
        long totalFailure = 0L;

        for (MetricsSnapshot snapshot : snapshots) {
            if (snapshot instanceof GenericMetricsSnapshot) {
                GenericMetricsSnapshot generic = (GenericMetricsSnapshot) snapshot;
                totalTotal += generic.getTotal();
                totalSuccess += generic.getSuccess();
                totalFailure += generic.getFailure();
            } else if (snapshot instanceof CountsMetrics) {
                CountsMetrics countsMetrics = (CountsMetrics) snapshot;
                totalTotal += countsMetrics.total();
                totalSuccess += countsMetrics.success();
                totalFailure += countsMetrics.failure();
            }
        }

        double successRate = totalTotal > 0 ? (totalSuccess * 100.0) / totalTotal : 0.0;

        return Map.of("total", totalTotal, "success", totalSuccess, "failure", totalFailure, "successRate",
                successRate);
    }

    /**
     * Build aggregated latency section from multiple snapshots.
     */
    private static Map<String, Object> buildAggregatedLatencySection(List<MetricsSnapshot> snapshots) {
        long totalDurationNanos = 0L;
        double minMs = Double.MAX_VALUE;
        double maxMs = 0.0;
        long totalCount = 0L;

        for (MetricsSnapshot snapshot : snapshots) {
            if (snapshot instanceof GenericMetricsSnapshot) {
                GenericMetricsSnapshot generic = (GenericMetricsSnapshot) snapshot;
                totalDurationNanos += generic.getTotalDurationNanos();
                minMs = Math.min(minMs, generic.getDouble("minMs", Double.MAX_VALUE));
                maxMs = Math.max(maxMs, generic.getDouble("maxMs", 0.0));
                totalCount += generic.getTotal();
            } else if (snapshot instanceof LatencyMetrics) {
                LatencyMetrics latencyMetrics = (LatencyMetrics) snapshot;
                totalDurationNanos += latencyMetrics.totalDurationNanos();
                // LatencyMetrics doesn't provide min/max, so we can't update them
                totalCount += 1; // Assume one operation per snapshot
            }
        }

        double averageMs = totalCount > 0 ? totalDurationNanos / (totalCount * 1_000_000.0) : 0.0;

        if (minMs == Double.MAX_VALUE) {
            minMs = 0.0;
        }

        return Map.of("averageMs", averageMs, "minMs", minMs, "maxMs", maxMs, "totalDurationNanos", totalDurationNanos);
    }

    /**
     * Build aggregated domain-specific section from multiple snapshots.
     */
    private static Map<String, Object> buildAggregatedDomainSpecificSection(List<MetricsSnapshot> snapshots) {
        Map<String, Object> domainSpecific = new HashMap<>();

        double totalTokensPerSecond = 0.0;
        double totalCostPerRequest = 0.0;
        long totalAverageTokensPerRequest = 0L;
        int modelMetricsCount = 0;

        for (MetricsSnapshot snapshot : snapshots) {
            if (snapshot instanceof GenericMetricsSnapshot) {
                GenericMetricsSnapshot generic = (GenericMetricsSnapshot) snapshot;

                if (generic.hasModelMetrics()) {
                    totalTokensPerSecond += generic.getDouble("tokensPerSecond", 0.0);
                    totalCostPerRequest += generic.getCost();
                    totalAverageTokensPerRequest += generic.getTokens();
                    modelMetricsCount++;
                }
            } else if (snapshot instanceof ModelMetrics) {
                ModelMetrics modelMetrics = (ModelMetrics) snapshot;
                totalTokensPerSecond += modelMetrics.tokensPerSecond();
                totalCostPerRequest += modelMetrics.costPerRequest();
                totalAverageTokensPerRequest += modelMetrics.averageTokensPerRequest();
                modelMetricsCount++;
            }
        }

        if (modelMetricsCount > 0) {
            domainSpecific.put("tokensPerSecond", totalTokensPerSecond / modelMetricsCount);
            domainSpecific.put("costPerRequest", totalCostPerRequest / modelMetricsCount);
            domainSpecific.put("averageTokensPerRequest", totalAverageTokensPerRequest / modelMetricsCount);
        }

        return domainSpecific;
    }

    /**
     * Extract domain from snapshot.
     */
    private static String extractDomain(MetricsSnapshot snapshot) {
        if (snapshot instanceof GenericMetricsSnapshot) {
            return ((GenericMetricsSnapshot) snapshot).getDomain();
        }
        return "unknown";
    }

    /**
     * Extract operation from snapshot.
     */
    private static String extractOperation(MetricsSnapshot snapshot) {
        if (snapshot instanceof GenericMetricsSnapshot) {
            return ((GenericMetricsSnapshot) snapshot).getOperation();
        }
        return "unknown";
    }

    /**
     * Aggregate domains from multiple snapshots.
     */
    private static String aggregateDomains(List<MetricsSnapshot> snapshots) {
        return snapshots.stream().map(MetricsResponseBuilder::extractDomain).distinct().reduce((a, b) -> a + "," + b)
                .orElse("unknown");
    }

    /**
     * Aggregate operations from multiple snapshots.
     */
    private static String aggregateOperations(List<MetricsSnapshot> snapshots) {
        return snapshots.stream().map(MetricsResponseBuilder::extractOperation).distinct().reduce((a, b) -> a + "," + b)
                .orElse("unknown");
    }

    /**
     * Check if a metric key is a standard metric that should not be included in domain-specific section.
     */
    private static boolean isStandardMetric(String key) {
        return key.equals("total") || key.equals("success") || key.equals("failure") || key.equals("totalDurationNanos")
                || key.equals("minMs") || key.equals("maxMs") || key.equals("tokens") || key.equals("cost")
                || key.equals("throughput") || key.equals("healthStatus") || key.equals("lastCheckTime")
                || key.equals("integrationSuccessRate") || key.equals("lastIntegrationTime");
    }
}
