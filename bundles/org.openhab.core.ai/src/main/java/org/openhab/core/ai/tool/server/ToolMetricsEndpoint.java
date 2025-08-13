package org.openhab.core.ai.tool.server;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Health metrics endpoint for MCP operations.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ToolMetricsEndpoint {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ToolMetricsEndpoint.class);

    private final DefaultToolServer serverInstance;
    private final ServerConfiguration config;
    private final com.sun.net.httpserver.HttpServer httpServer;
    private final java.util.concurrent.ScheduledExecutorService executor;

    // Metrics counters
    private final java.util.concurrent.atomic.AtomicLong totalRequests = new java.util.concurrent.atomic.AtomicLong(0);
    private final java.util.concurrent.atomic.AtomicLong totalErrors = new java.util.concurrent.atomic.AtomicLong(0);
    private final long startTime = System.currentTimeMillis();

    public ToolMetricsEndpoint(DefaultToolServer serverInstance, ServerConfiguration config) throws IOException {
        this.serverInstance = serverInstance;
        this.config = config;

        // Create HTTP server on configurable port
        // Use SSE port from config or default to 8080
        int port = config.getSsePort();
        this.httpServer = com.sun.net.httpserver.HttpServer.create(new java.net.InetSocketAddress(port), 0);

        // Set up endpoints - always enable health and metrics for now
        httpServer.createContext("/health", new org.openhab.core.ai.tool.server.http.HealthHandler(serverInstance,
                config, startTime, totalRequests, totalErrors));
        logger.info("Health endpoint enabled at /health");

        httpServer.createContext("/metrics", new org.openhab.core.ai.tool.server.http.MetricsHandler(serverInstance,
                startTime, totalRequests, totalErrors));
        logger.info("Metrics endpoint enabled at /metrics");

        // Create executor for background tasks
        this.executor = java.util.concurrent.Executors.newScheduledThreadPool(1);

        // Start health check scheduler
        executor.scheduleAtFixedRate(this::performHealthCheck, 30000, // 30 seconds
                30000, java.util.concurrent.TimeUnit.MILLISECONDS);
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
