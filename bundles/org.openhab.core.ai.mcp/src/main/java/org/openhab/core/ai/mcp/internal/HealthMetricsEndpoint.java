package org.openhab.core.ai.mcp.internal;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Health metrics endpoint for MCP operations.
 * 
 * @author AI Assistant
 * @since 1.0.0
 */
@NonNullByDefault
public class HealthMetricsEndpoint {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(HealthMetricsEndpoint.class);

    private final MCPServer serverInstance;
    private final MCPServerConfiguration config;
    private final com.sun.net.httpserver.HttpServer httpServer;
    private final java.util.concurrent.ScheduledExecutorService executor;

    // Metrics counters
    private final java.util.concurrent.atomic.AtomicLong totalRequests = new java.util.concurrent.atomic.AtomicLong(0);
    private final java.util.concurrent.atomic.AtomicLong totalErrors = new java.util.concurrent.atomic.AtomicLong(0);
    private final long startTime = System.currentTimeMillis();

    public HealthMetricsEndpoint(MCPServer serverInstance, MCPServerConfiguration config) throws IOException {
        this.serverInstance = serverInstance;
        this.config = config;

        // Create HTTP server on port 8080 (configurable)
        int port = 8080; // TODO: Make configurable
        this.httpServer = com.sun.net.httpserver.HttpServer.create(new java.net.InetSocketAddress(port), 0);

        // Set up endpoints
        if (config.isEnableHealthChecks()) {
            httpServer.createContext("/health", new HealthHandler());
            logger.info("Health endpoint enabled at /health");
        }

        if (config.isEnableMetrics()) {
            httpServer.createContext("/metrics", new MetricsHandler());
            logger.info("Metrics endpoint enabled at /metrics");
        }

        // Create executor for background tasks
        this.executor = java.util.concurrent.Executors.newScheduledThreadPool(1);

        // Start health check scheduler if enabled
        if (config.isEnableHealthChecks()) {
            executor.scheduleAtFixedRate(this::performHealthCheck, config.getHealthCheckInterval(),
                    config.getHealthCheckInterval(), java.util.concurrent.TimeUnit.MILLISECONDS);
        }
    }

    public void start() {
        httpServer.start();
        logger.info("Health/Metrics endpoint started on port 8080");
    }

    public void stop() {
        httpServer.stop(0);
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        logger.info("Health/Metrics endpoint stopped");
    }

    private void performHealthCheck() {
        try {
            boolean healthy = serverInstance.isHealthy();
            if (!healthy) {
                logger.warn("Health check failed: server is not healthy");
            }
        } catch (Exception e) {
            logger.error("Health check failed with exception", e);
        }
    }

    private class HealthHandler implements com.sun.net.httpserver.HttpHandler {
        @Override
        public void handle(com.sun.net.httpserver.@Nullable HttpExchange exchange) throws IOException {
            try {
                totalRequests.incrementAndGet();

                boolean healthy = serverInstance.isHealthy();
                MCPServer.TransportHealthInfo transportHealth = serverInstance.getTransportHealth();

                // Build enhanced health response
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

                // Add security information if available
                if (serverInstance.isSecurityEnabled()) {
                    MCPSecurityManager.SecurityStatistics securityStats = serverInstance.getSecurityStatistics();
                    if (securityStats != null) {
                        response.append("  \"security\": {\n");
                        response.append("    \"enabled\": ").append(securityStats.isAuthenticationEnabled())
                                .append(",\n");
                        response.append("    \"activeClients\": ").append(securityStats.getActiveClients())
                                .append(",\n");
                        response.append("    \"blockedClients\": ").append(securityStats.getBlockedClients())
                                .append(",\n");
                        response.append("    \"failedAttempts\": ").append(securityStats.getFailedAttempts())
                                .append(",\n");
                        response.append("    \"requestValidation\": ")
                                .append(securityStats.isRequestValidationEnabled()).append("\n");
                        response.append("  },\n");
                    }
                }

                // Add error recovery information if available
                if (serverInstance.isErrorRecoveryEnabled()) {
                    MCPErrorRecoveryManager.ErrorRecoveryStatistics errorStats = serverInstance
                            .getErrorRecoveryStatistics();
                    if (errorStats != null) {
                        response.append("  \"errorRecovery\": {\n");
                        response.append("    \"totalErrors\": ").append(errorStats.getTotalErrors()).append(",\n");
                        response.append("    \"totalRecoveries\": ").append(errorStats.getTotalRecoveries())
                                .append(",\n");
                        response.append("    \"totalFallbacks\": ").append(errorStats.getTotalFallbacks())
                                .append(",\n");
                        response.append("    \"recoveryRate\": ")
                                .append(String.format("%.2f", errorStats.getRecoveryRate())).append(",\n");
                        response.append("    \"activeErrorTypes\": ").append(errorStats.getActiveErrorTypes())
                                .append(",\n");
                        response.append("    \"activeCircuitBreakers\": ").append(errorStats.getActiveCircuitBreakers())
                                .append("\n");
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

    private class MetricsHandler implements com.sun.net.httpserver.HttpHandler {
        @Override
        public void handle(com.sun.net.httpserver.@Nullable HttpExchange exchange) throws IOException {
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

                // Add security metrics if available
                if (serverInstance.isSecurityEnabled()) {
                    MCPSecurityManager.SecurityStatistics securityStats = serverInstance.getSecurityStatistics();
                    if (securityStats != null) {
                        response.append("# HELP mcp_security_active_clients Number of active clients\n");
                        response.append("# TYPE mcp_security_active_clients gauge\n");
                        response.append("mcp_security_active_clients ").append(securityStats.getActiveClients())
                                .append("\n");
                        response.append("# HELP mcp_security_blocked_clients Number of blocked clients\n");
                        response.append("# TYPE mcp_security_blocked_clients gauge\n");
                        response.append("mcp_security_blocked_clients ").append(securityStats.getBlockedClients())
                                .append("\n");
                        response.append(
                                "# HELP mcp_security_failed_attempts Number of failed authentication attempts\n");
                        response.append("# TYPE mcp_security_failed_attempts gauge\n");
                        response.append("mcp_security_failed_attempts ").append(securityStats.getFailedAttempts())
                                .append("\n");
                        response.append("# HELP mcp_security_enabled Security features enabled\n");
                        response.append("# TYPE mcp_security_enabled gauge\n");
                        response.append("mcp_security_enabled ").append(securityStats.isAuthenticationEnabled() ? 1 : 0)
                                .append("\n");
                    }
                }

                // Add error recovery metrics if available
                if (serverInstance.isErrorRecoveryEnabled()) {
                    MCPErrorRecoveryManager.ErrorRecoveryStatistics errorStats = serverInstance
                            .getErrorRecoveryStatistics();
                    if (errorStats != null) {
                        response.append("# HELP mcp_errors_total_count Total number of errors\n");
                        response.append("# TYPE mcp_errors_total_count counter\n");
                        response.append("mcp_errors_total_count ").append(errorStats.getTotalErrors()).append("\n");
                        response.append("# HELP mcp_recoveries_total Total number of successful recoveries\n");
                        response.append("# TYPE mcp_recoveries_total counter\n");
                        response.append("mcp_recoveries_total ").append(errorStats.getTotalRecoveries()).append("\n");
                        response.append("# HELP mcp_fallbacks_total Total number of fallback actions\n");
                        response.append("# TYPE mcp_fallbacks_total counter\n");
                        response.append("mcp_fallbacks_total ").append(errorStats.getTotalFallbacks()).append("\n");
                        response.append("# HELP mcp_recovery_rate Error recovery rate\n");
                        response.append("# TYPE mcp_recovery_rate gauge\n");
                        response.append("mcp_recovery_rate ").append(errorStats.getRecoveryRate()).append("\n");
                        response.append("# HELP mcp_active_error_types Number of active error types\n");
                        response.append("# TYPE mcp_active_error_types gauge\n");
                        response.append("mcp_active_error_types ").append(errorStats.getActiveErrorTypes())
                                .append("\n");
                        response.append("# HELP mcp_active_circuit_breakers Number of active circuit breakers\n");
                        response.append("# TYPE mcp_active_circuit_breakers gauge\n");
                        response.append("mcp_active_circuit_breakers ").append(errorStats.getActiveCircuitBreakers())
                                .append("\n");
                    }
                }

                byte[] responseBytes = response.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
                var headers = exchange.getResponseHeaders();
                if (headers != null) {
                    headers.add("Content-Type", "text/plain; version=0.0.4; charset=utf-8");
                }
                exchange.sendResponseHeaders(200, responseBytes.length);

                try (java.io.OutputStream os = exchange.getResponseBody()) {
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

    /**
     * Get basic health status.
     * 
     * @return Health status as a map
     */
    public java.util.Map<String, Object> getHealthStatus() {
        java.util.Map<String, Object> status = new java.util.HashMap<>();

        // Basic health checks
        status.put("serverRunning", serverInstance.isRunning());
        status.put("transportHealthy", serverInstance.isHealthy());
        status.put("uptime", System.currentTimeMillis() - startTime);

        // Performance metrics
        status.put("totalRequests", totalRequests.get());
        status.put("totalErrors", totalErrors.get());
        status.put("errorRate", totalRequests.get() > 0 ? (double) totalErrors.get() / totalRequests.get() : 0.0);

        // System metrics
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        status.put("memoryUsage", (double) usedMemory / totalMemory);
        status.put("totalMemory", totalMemory);
        status.put("usedMemory", usedMemory);
        status.put("freeMemory", freeMemory);
        status.put("availableProcessors", runtime.availableProcessors());
        status.put("threadCount", Thread.activeCount());

        // Calculate health score
        double healthScore = calculateHealthScore(status);
        status.put("healthScore", healthScore);

        return status;
    }

    /**
     * Get performance metrics.
     * 
     * @return Performance metrics as a map
     */
    public java.util.Map<String, Object> getPerformanceMetrics() {
        java.util.Map<String, Object> metrics = new java.util.HashMap<>();

        // Request metrics
        metrics.put("totalRequests", totalRequests.get());
        metrics.put("totalErrors", totalErrors.get());
        metrics.put("successfulRequests", totalRequests.get() - totalErrors.get());
        metrics.put("errorRate", totalRequests.get() > 0 ? (double) totalErrors.get() / totalRequests.get() : 0.0);

        // Uptime metrics
        long uptime = System.currentTimeMillis() - startTime;
        metrics.put("uptime", uptime);
        metrics.put("requestsPerSecond", uptime > 0 ? (double) totalRequests.get() / (uptime / 1000.0) : 0.0);

        // System metrics
        Runtime runtime = Runtime.getRuntime();
        metrics.put("memoryUsage", (double) (runtime.totalMemory() - runtime.freeMemory()) / runtime.totalMemory());
        metrics.put("threadCount", Thread.activeCount());

        return metrics;
    }

    /**
     * Get system resources.
     * 
     * @return System resources as a map
     */
    public java.util.Map<String, Object> getSystemResources() {
        java.util.Map<String, Object> resources = new java.util.HashMap<>();

        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        resources.put("totalMemory", totalMemory);
        resources.put("usedMemory", usedMemory);
        resources.put("freeMemory", freeMemory);
        resources.put("memoryUsagePercentage", (double) usedMemory / totalMemory * 100);
        resources.put("availableProcessors", runtime.availableProcessors());
        resources.put("threadCount", Thread.activeCount());

        // Disk information
        try {
            java.io.File file = new java.io.File(".");
            resources.put("diskFreeSpace", file.getFreeSpace());
            resources.put("diskTotalSpace", file.getTotalSpace());
            resources.put("diskUsableSpace", file.getUsableSpace());
        } catch (Exception e) {
            logger.warn("Could not get disk information", e);
            resources.put("diskFreeSpace", 0L);
            resources.put("diskTotalSpace", 0L);
            resources.put("diskUsableSpace", 0L);
        }

        return resources;
    }

    /**
     * Calculate health score based on metrics.
     * 
     * @param status Health status map
     * @return Health score (0-100)
     */
    private double calculateHealthScore(java.util.Map<String, Object> status) {
        double score = 100.0;

        // Deduct points for various issues
        Boolean serverRunning = (Boolean) status.get("serverRunning");
        if (serverRunning != null && !serverRunning) {
            score -= 50.0;
        }

        Boolean transportHealthy = (Boolean) status.get("transportHealthy");
        if (transportHealthy != null && !transportHealthy) {
            score -= 30.0;
        }

        Double errorRate = (Double) status.get("errorRate");
        if (errorRate != null && errorRate > 0.1) { // More than 10% error rate
            score -= 20.0;
        }

        Double memoryUsage = (Double) status.get("memoryUsage");
        if (memoryUsage != null && memoryUsage > 0.9) { // More than 90% memory usage
            score -= 15.0;
        }

        return Math.max(0.0, score);
    }

    /**
     * Calculate requests per second.
     * 
     * @return Requests per second
     */
    private double calculateRequestsPerSecond() {
        long currentTime = System.currentTimeMillis();
        long timeWindow = currentTime - startTime;

        if (timeWindow > 0) {
            return (double) totalRequests.get() / (timeWindow / 1000.0);
        }

        return 0.0;
    }

    /**
     * Get average concurrent requests.
     * 
     * @return Average concurrent requests
     */
    private double getAverageConcurrentRequests() {
        // Simplified implementation - in a real system, this would track concurrent requests
        return Math.min(totalRequests.get() / 100.0, 10.0); // Placeholder calculation
    }

    /**
     * Get peak concurrent requests.
     * 
     * @return Peak concurrent requests
     */
    private double getPeakConcurrentRequests() {
        // Simplified implementation - in a real system, this would track peak concurrent requests
        return Math.min(totalRequests.get() / 50.0, 20.0); // Placeholder calculation
    }

    /**
     * Get average tool execution time.
     * 
     * @return Average tool execution time in milliseconds
     */
    private double getAverageToolExecutionTime() {
        // Simplified implementation - in a real system, this would track tool execution time
        return 100.0; // Placeholder value
    }

    /**
     * Get tool success rate.
     * 
     * @return Tool success rate (0.0-1.0)
     */
    private double getToolSuccessRate() {
        // Simplified implementation - in a real system, this would track tool success rate
        return 0.95; // Placeholder value
    }

    /**
     * Get thread count.
     * 
     * @return Current thread count
     */
    private int getThreadCount() {
        return Thread.activeCount();
    }

    /**
     * Get peak thread count.
     * 
     * @return Peak thread count
     */
    private int getPeakThreadCount() {
        // Simplified implementation - in a real system, this would track peak thread count
        return Thread.activeCount() * 2; // Placeholder calculation
    }

    /**
     * Get disk free space.
     * 
     * @return Disk free space in bytes
     */
    private long getDiskFreeSpace() {
        try {
            java.io.File file = new java.io.File(".");
            return file.getFreeSpace();
        } catch (Exception e) {
            logger.warn("Could not get disk free space", e);
            return 0L;
        }
    }
}
