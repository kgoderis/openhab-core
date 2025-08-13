package org.openhab.core.ai.tool.server.http;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.tool.server.DefaultToolServer;
import org.openhab.core.ai.tool.server.ServerConfiguration;

/**
 * HTTP handler that serves JSON health information for the MCP Tool server.
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
public final class HealthHandler implements com.sun.net.httpserver.HttpHandler {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(HealthHandler.class);

    private final DefaultToolServer serverInstance;
    private final ServerConfiguration config;
    private final long startTime;
    private final AtomicLong totalRequests;
    private final AtomicLong totalErrors;

    public HealthHandler(DefaultToolServer serverInstance, ServerConfiguration config, long startTime,
            AtomicLong totalRequests, AtomicLong totalErrors) {
        this.serverInstance = serverInstance;
        this.config = config;
        this.startTime = startTime;
        this.totalRequests = totalRequests;
        this.totalErrors = totalErrors;
    }

    @Override
    public void handle(com.sun.net.httpserver.HttpExchange exchange) throws IOException {
        try {
            totalRequests.incrementAndGet();

            boolean healthy = serverInstance.isHealthy();
            org.openhab.core.ai.tool.server.TransportHealthInfo transportHealth = serverInstance.getTransportHealth();

            StringBuilder response = new StringBuilder();
            response.append("{\n");
            response.append("  \"status\": \"").append(healthy ? "UP" : "DOWN").append("\",\n");
            response.append("  \"transport\": {\n");
            response.append("    \"type\": \"").append(transportHealth.getTransportType()).append("\",\n");
            response.append("    \"healthy\": ").append(transportHealth.isHealthy()).append(",\n");
            response.append("    \"uptime\": ").append(transportHealth.getUptime()).append(",\n");
            response.append("    \"lastError\": ").append(
                    transportHealth.getLastError() != null ? "\"" + transportHealth.getLastError() + "\"" : "null")
                    .append("\n");
            response.append("  },\n");
            response.append("  \"uptime\": ").append(System.currentTimeMillis() - startTime).append(",\n");
            response.append("  \"version\": \"").append(config.getServerVersion()).append("\",\n");

            if (serverInstance.isSecurityEnabled()) {
                org.openhab.core.ai.tool.security.SecurityStatistics securityStats = serverInstance
                        .getSecurityStatistics();
                if (securityStats != null) {
                    response.append("  \"security\": {\n");
                    response.append("    \"totalRequests\": ").append(securityStats.getTotalAccessAttempts())
                            .append(",\n");
                    response.append("    \"allowedRequests\": ").append(securityStats.getAllowedAccessAttempts())
                            .append(",\n");
                    response.append("    \"deniedRequests\": ").append(securityStats.getDeniedAccessAttempts())
                            .append("\n");
                    response.append("  },\n");
                }
            }

            if (serverInstance.isErrorRecoveryEnabled()) {
                org.openhab.core.ai.tool.error.ErrorRecoveryStatistics errorStats = serverInstance
                        .getErrorRecoveryStatistics();
                if (errorStats != null) {
                    response.append("  \"errorRecovery\": {\n");
                    response.append("    \"totalErrors\": ").append(errorStats.getTotalErrors()).append(",\n");
                    response.append("    \"totalRecoveries\": ").append(errorStats.getTotalRecoveries()).append(",\n");
                    response.append("    \"totalFallbacks\": ").append(errorStats.getTotalFallbacks()).append(",\n");
                    response.append("    \"totalFailures\": ").append(errorStats.getTotalFailures()).append("\n");
                    response.append("  },\n");
                }
            }

            response.append("  \"timestamp\": \"").append(java.time.Instant.now()).append("\"\n");
            response.append("}");

            byte[] responseBytes = response.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
            var headers = exchange.getResponseHeaders();
            if (headers != null) {
                headers.add("Content-Type", "application/json");
            }
            exchange.sendResponseHeaders(healthy ? 200 : 503, responseBytes.length);

            try (java.io.OutputStream os = exchange.getResponseBody()) {
                if (os != null) {
                    os.write(responseBytes);
                }
            }

        } catch (Exception e) {
            totalErrors.incrementAndGet();
            logger.error("Error handling health check request", e);
            String errorResponse = "{\"error\": \"Internal server error\"}";
            byte[] responseBytes = errorResponse.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            var headers = exchange.getResponseHeaders();
            if (headers != null) {
                headers.add("Content-Type", "application/json");
            }
            exchange.sendResponseHeaders(500, responseBytes.length);
            try (java.io.OutputStream os = exchange.getResponseBody()) {
                if (os != null) {
                    os.write(responseBytes);
                }
            }
        }
    }
}
