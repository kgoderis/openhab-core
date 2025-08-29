package org.openhab.core.ai.tool.server.http;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;


import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.MetricsResponseBuilder;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.api.MetricsSnapshot;
import org.openhab.core.ai.common.monitoring.patterns.SystemPerformanceMetrics;
import org.openhab.core.ai.common.monitoring.service.snapshot.GenericMetricsSnapshot;
import org.openhab.core.ai.common.monitoring.service.statistics.ErrorRecoveryStatistics;
import org.openhab.core.ai.common.security.ToolSecurityStatistics;
import org.openhab.core.ai.tool.server.DefaultToolServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * HTTP handler that serves Prometheus-style metrics for the MCP Tool server.
 *
 * <p>
 * Extracted from the inner class in {@code ToolMetricsEndpoint} to a top-level
 * class for clarity and reuse.
 * </p>
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class MetricsHandler implements HttpHandler {

    private static final Logger logger = LoggerFactory.getLogger(MetricsHandler.class);

    private final DefaultToolServer serverInstance;
    private final long startTime;
    private final @Nullable MetricsService metricsService;

    public MetricsHandler(DefaultToolServer serverInstance, long startTime, @Nullable MetricsService metricsService) {
        this.serverInstance = serverInstance;
        this.startTime = startTime;
        this.metricsService = metricsService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        long startTime = System.currentTimeMillis();
        
        try {
            // Check if client wants JSON format (standardized) or Prometheus format
            String acceptHeader = exchange.getRequestHeaders().getFirst("Accept");
            boolean wantsJson = acceptHeader != null && acceptHeader.contains("application/json");

            if (wantsJson) {
                handleJsonMetrics(exchange);
            } else {
                handlePrometheusMetrics(exchange);
            }
            
            // Replace totalRequests.incrementAndGet()
            Duration responseTime = Duration.ofMillis(System.currentTimeMillis() - startTime);
            recordHttpRequest("metrics-request", true, responseTime);

        } catch (Exception e) {
            logger.error("Error handling metrics request", e);
            exchange.sendResponseHeaders(500, 0);
            exchange.close();
            
            // Replace totalErrors.incrementAndGet()
            recordHttpError("metrics-handler-error", e.getMessage());
        }
    }

    /**
     * Handle metrics request with standardized JSON response format.
     */
    private void handleJsonMetrics(HttpExchange exchange) throws IOException {
        Map<String, Object> response;

        if (metricsService != null) {
            // Get metrics from MetricsService
            List<MetricsSnapshot> snapshots = metricsService.getAllSnapshots(MetricsSnapshot.class);
            if (snapshots.isEmpty()) {
                // Create a snapshot from legacy data if no MetricsService data available
                GenericMetricsSnapshot legacySnapshot = createLegacySnapshot();
                response = MetricsResponseBuilder.buildResponse(legacySnapshot);
            } else {
                response = MetricsResponseBuilder.buildResponse(snapshots);
            }
        } else {
            // Fallback to legacy data when MetricsService is not available
            GenericMetricsSnapshot legacySnapshot = createLegacySnapshot();
            response = MetricsResponseBuilder.buildResponse(legacySnapshot);
        }

        // Convert to JSON
        ObjectMapper mapper = new ObjectMapper();
        String jsonResponse = mapper.writeValueAsString(response);
        byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);

        var headers = exchange.getResponseHeaders();
        if (headers != null) {
            headers.add("Content-Type", "application/json; charset=utf-8");
        }
        exchange.sendResponseHeaders(200, responseBytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            if (os != null) {
                os.write(responseBytes);
            }
        }
    }

    /**
     * Handle metrics request with Prometheus format (legacy support).
     */
    private void handlePrometheusMetrics(HttpExchange exchange) throws IOException {
        long uptimeSeconds = (System.currentTimeMillis() - startTime) / 1000;

        StringBuilder response = new StringBuilder();
        response.append("# HELP mcp_requests_total Total number of requests\n");
        response.append("# TYPE mcp_requests_total counter\n");
        response.append("mcp_requests_total ").append(0).append("\n");
        response.append("# HELP mcp_errors_total Total number of errors\n");
        response.append("# TYPE mcp_errors_total counter\n");
        response.append("mcp_errors_total ").append(0).append("\n");
        response.append("# HELP mcp_uptime_seconds Uptime in seconds\n");
        response.append("# TYPE mcp_uptime_seconds gauge\n");
        response.append("mcp_uptime_seconds ").append(uptimeSeconds).append("\n");
        response.append("# HELP mcp_server_healthy Server health status\n");
        response.append("# TYPE mcp_server_healthy gauge\n");
        response.append("mcp_server_healthy ").append(serverInstance.isHealthy() ? 1 : 0).append("\n");

        if (serverInstance.isSecurityEnabled()) {
            ToolSecurityStatistics securityStats = serverInstance.getSecurityStatistics();
            if (securityStats != null) {
                response.append("# HELP mcp_security_total_requests Total number of security requests\n");
                response.append("# TYPE mcp_security_total_requests counter\n");
                response.append("mcp_security_total_requests ").append(securityStats.getTotalAccessAttempts())
                        .append("\n");
                response.append("# HELP mcp_security_allowed_requests Number of allowed requests\n");
                response.append("# TYPE mcp_security_allowed_requests counter\n");
                response.append("mcp_security_allowed_requests ").append(securityStats.getAllowedAccessAttempts())
                        .append("\n");
                response.append("# HELP mcp_security_denied_requests Number of denied requests\n");
                response.append("# TYPE mcp_security_denied_requests counter\n");
                response.append("mcp_security_denied_requests ").append(securityStats.getDeniedAccessAttempts())
                        .append("\n");
            }
        }

        if (serverInstance.isErrorRecoveryEnabled()) {
            ErrorRecoveryStatistics errorStats = serverInstance.getErrorRecoveryStatistics();
            if (errorStats != null) {
                response.append("# HELP mcp_recovery_attempts_total Total number of recovery attempts\n");
                response.append("# TYPE mcp_recovery_attempts_total counter\n");
                response.append("mcp_recovery_attempts_total ").append(errorStats.getTotalRecoveryAttempts())
                        .append("\n");
                response.append("# HELP mcp_successful_recoveries_total Total number of successful recoveries\n");
                response.append("# TYPE mcp_successful_recoveries_total counter\n");
                response.append("mcp_successful_recoveries_total ").append(errorStats.getSuccessfulRecoveries())
                        .append("\n");
                response.append("# HELP mcp_failed_recoveries_total Total number of failed recoveries\n");
                response.append("# TYPE mcp_failed_recoveries_total counter\n");
                response.append("mcp_failed_recoveries_total ").append(errorStats.getFailedRecoveries()).append("\n");
                double recoveryRate = errorStats.getTotalRecoveryAttempts() > 0
                        ? (double) errorStats.getSuccessfulRecoveries() / errorStats.getTotalRecoveryAttempts()
                        : 0.0;
                response.append("# HELP mcp_recovery_rate Recovery rate\n");
                response.append("# TYPE mcp_recovery_rate gauge\n");
                response.append("mcp_recovery_rate ").append(recoveryRate).append("\n");
            }
        }

        byte[] responseBytes = response.toString().getBytes(StandardCharsets.UTF_8);
        var headers = exchange.getResponseHeaders();
        if (headers != null) {
            headers.add("Content-Type", "text/plain; version=0.0.4; charset=utf-8");
        }
        exchange.sendResponseHeaders(200, responseBytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            if (os != null) {
                os.write(responseBytes);
            }
        }
    }

    /**
     * Create a legacy snapshot from current server state when MetricsService data is not available.
     */
    private GenericMetricsSnapshot createLegacySnapshot() {
        long uptimeSeconds = (System.currentTimeMillis() - startTime) / 1000;

        return GenericMetricsSnapshot.builder("tool-server", "metrics-endpoint")
                .withCounts(0, 0).withLatency(0L) // No timing data available in legacy mode
                .withMetric("uptimeSeconds", uptimeSeconds).withMetric("serverHealthy", serverInstance.isHealthy())
                .withMetric("securityEnabled", serverInstance.isSecurityEnabled())
                .withMetric("errorRecoveryEnabled", serverInstance.isErrorRecoveryEnabled()).build();
    }
    
    // Metrics recording methods - replacing removed AtomicLong fields using SystemPerformanceMetrics pattern
    
    /**
     * Record HTTP request - replaces totalRequests.incrementAndGet()
     */
    private void recordHttpRequest(String requestType, boolean success, Duration responseTime) {
        try {
            MetricsService metrics = metricsService;
            if (metrics != null) {
                // Use SystemPerformanceMetrics pattern for HTTP requests
                SystemPerformanceMetrics.recordMessageLatency(metrics, "http-request", responseTime, success, "metrics-handler");
            }
        } catch (Exception e) {
            logger.warn("Failed to record HTTP request metric for type {}: {}", requestType, e.getMessage());
        }
    }
    
    /**
     * Record HTTP error - replaces totalErrors.incrementAndGet()
     */
    private void recordHttpError(String errorType, String errorMessage) {
        try {
            MetricsService metrics = metricsService;
            if (metrics != null) {
                // Use SystemPerformanceMetrics pattern for HTTP errors
                SystemPerformanceMetrics.recordPerformanceDegradation(metrics, "http-error", "medium", errorMessage);
            }
        } catch (Exception e) {
            logger.warn("Failed to record HTTP error metric for type {}: {}", errorType, e.getMessage());
        }
    }
}
