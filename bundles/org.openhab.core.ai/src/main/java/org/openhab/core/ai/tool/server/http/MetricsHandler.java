package org.openhab.core.ai.tool.server.http;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.tool.error.ErrorRecoveryStatistics;
import org.openhab.core.ai.tool.security.api.SecurityStatistics;
import org.openhab.core.ai.tool.server.DefaultToolServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final AtomicLong totalRequests;
    private final AtomicLong totalErrors;

    public MetricsHandler(DefaultToolServer serverInstance, long startTime, AtomicLong totalRequests,
            AtomicLong totalErrors) {
        this.serverInstance = serverInstance;
        this.startTime = startTime;
        this.totalRequests = totalRequests;
        this.totalErrors = totalErrors;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            totalRequests.incrementAndGet();

            long uptimeSeconds = (System.currentTimeMillis() - startTime) / 1000;

            StringBuilder response = new StringBuilder();
            response.append("# HELP mcp_requests_total Total number of requests\n");
            response.append("# TYPE mcp_requests_total counter\n");
            response.append("mcp_requests_total ").append(totalRequests.get()).append("\n");
            response.append("# HELP mcp_errors_total Total number of errors\n");
            response.append("# TYPE mcp_errors_total counter\n");
            response.append("mcp_errors_total ").append(totalErrors.get()).append("\n");
            response.append("# HELP mcp_uptime_seconds Uptime in seconds\n");
            response.append("# TYPE mcp_uptime_seconds gauge\n");
            response.append("mcp_uptime_seconds ").append(uptimeSeconds).append("\n");
            response.append("# HELP mcp_server_healthy Server health status\n");
            response.append("# TYPE mcp_server_healthy gauge\n");
            response.append("mcp_server_healthy ").append(serverInstance.isHealthy() ? 1 : 0).append("\n");

            if (serverInstance.isSecurityEnabled()) {
                SecurityStatistics securityStats = serverInstance.getSecurityStatistics();
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
                    response.append("# HELP mcp_errors_total_count Total number of errors\n");
                    response.append("# TYPE mcp_errors_total_count counter\n");
                    response.append("mcp_errors_total_count ").append(errorStats.getTotalErrors()).append("\n");
                    response.append("# HELP mcp_recoveries_total Total number of successful recoveries\n");
                    response.append("# TYPE mcp_recoveries_total counter\n");
                    response.append("mcp_recoveries_total ").append(errorStats.getTotalRecoveries()).append("\n");
                    response.append("# HELP mcp_fallbacks_total Total number of fallbacks\n");
                    response.append("# TYPE mcp_fallbacks_total counter\n");
                    response.append("mcp_fallbacks_total ").append(errorStats.getTotalFallbacks()).append("\n");
                    double recoveryRate = errorStats.getTotalErrors() > 0
                            ? (double) errorStats.getTotalRecoveries() / errorStats.getTotalErrors()
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

        } catch (Exception e) {
            totalErrors.incrementAndGet();
            logger.error("Error handling metrics request", e);
            exchange.sendResponseHeaders(500, 0);
            exchange.close();
        }
    }
}
