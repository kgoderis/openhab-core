package org.openhab.core.ai.tool.server;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.tool.config.ToolServerConfiguration;
import org.openhab.core.ai.tool.server.http.HealthHandler;
import org.openhab.core.ai.tool.server.http.MetricsHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.net.httpserver.HttpServer;

/**
 * HTTP endpoint for tool server health and metrics.
 * 
 * <p>
 * This class provides HTTP endpoints for monitoring tool server health and
 * metrics, including health checks and metrics collection.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ToolMetricsEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(ToolMetricsEndpoint.class);

    private final DefaultToolServer serverInstance;
    private final ToolServerConfiguration config;
    private final HttpServer httpServer;
    private final ScheduledExecutorService executor;

    // Metrics service for centralized metrics collection
    private @Nullable MetricsService metricsService;

    // Start time for uptime calculation
    private final long startTime = System.currentTimeMillis();

    public ToolMetricsEndpoint(DefaultToolServer serverInstance, ToolServerConfiguration config) throws IOException {
        this.serverInstance = serverInstance;
        this.config = config;

        // Create HTTP server on configurable port
        // Use SSE port from config or default to 8080
        int port = config.getSsePort();
        this.httpServer = HttpServer.create(new InetSocketAddress(port), 0);

        // Set up endpoints - always enable health and metrics for now
        httpServer.createContext("/health", new HealthHandler(serverInstance, config, startTime, metricsService));
        logger.info("Health endpoint enabled at /health");

        httpServer.createContext("/metrics", new MetricsHandler(serverInstance, startTime, metricsService));
        logger.info("Metrics endpoint enabled at /metrics");

        // Create executor for background tasks
        this.executor = Executors.newScheduledThreadPool(1);

        // Start health check scheduler
        executor.scheduleAtFixedRate(this::performHealthCheck, 30000, // 30 seconds
                30000, TimeUnit.MILLISECONDS);
    }

    public void start() {
        httpServer.start();
        logger.info("Health/Metrics endpoint started on port 8080");
    }

    public void stop() {
        httpServer.stop(0);
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        logger.info("Health/Metrics endpoint stopped");
    }

    /**
     * Set the metrics service for centralized metrics recording.
     * 
     * @param metricsService the metrics service to use
     */
    public void setMetricsService(@Nullable MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    /**
     * Record metrics for an operation.
     * 
     * @param operation the operation name
     * @param success whether the operation was successful
     * @param durationNanos the operation duration in nanoseconds
     */
    private void recordMetrics(String operation, boolean success, long durationNanos) {
        MetricsService metrics = metricsService;
        if (metrics != null) {
            try {
                metrics.recordOperation("tool-metrics", operation, success, Duration.ofNanos(durationNanos));
            } catch (Exception e) {
                logger.debug("Failed to record metrics for {}.{}: {}", "tool-metrics", operation, e.getMessage());
            }
        } else {
            logger.debug("MetricsService not available, cannot record metrics for operation: {}", operation);
        }
    }

    private void performHealthCheck() {
        try {
            boolean healthy = serverInstance.isHealthy();
            if (!healthy) {
                logger.warn("Health check failed: server is not healthy");
            }
            recordMetrics("health-check", healthy, 0);
        } catch (Exception e) {
            logger.error("Health check failed with exception", e);
            recordMetrics("health-check", false, 0);
        }
    }

    /**
     * Get basic health status.
     * 
     * @return Health status as a map
     */
    public Map<String, Object> getHealthStatus() {
        Map<String, Object> status = new HashMap<>();

        // Basic health checks
        status.put("serverRunning", serverInstance.isRunning());
        status.put("transportHealthy", serverInstance.isHealthy());
        status.put("uptime", System.currentTimeMillis() - startTime);

        // Record health check metrics
        boolean healthy = serverInstance.isRunning() && serverInstance.isHealthy();
        recordMetrics("health-status", healthy, 0);

        return status;
    }

    /**
     * Get performance metrics.
     * 
     * @return Performance metrics as a map
     */
    public Map<String, Object> getPerformanceMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        // Request metrics - get from MetricsService if available
        MetricsService metricsService = this.metricsService;
        if (metricsService != null) {
            try {
                var snapshot = metricsService.getDomainAggregatedSnapshot("tool-metrics");
                metrics.put("totalRequests", snapshot.totalOperations());
                metrics.put("totalErrors", snapshot.failedOperations());
                metrics.put("successfulRequests", snapshot.totalOperations() - snapshot.failedOperations());
                metrics.put("errorRate",
                        snapshot.totalOperations() > 0
                                ? (double) snapshot.failedOperations() / snapshot.totalOperations()
                                : 0.0);

                // Uptime metrics
                long uptime = System.currentTimeMillis() - startTime;
                metrics.put("uptime", uptime);
                metrics.put("requestsPerSecond",
                        uptime > 0 ? (double) snapshot.totalOperations() / (uptime / 1000.0) : 0.0);
            } catch (Exception e) {
                logger.warn("Error retrieving metrics for tool-metrics: {}", e.getMessage());
                // Fallback to legacy counters
                // These counters are no longer maintained in this class, so this fallback is not applicable.
                // The original code had a fallback to totalRequests/totalErrors, but they are removed.
                // The original code also had a fallback for uptime/requestsPerSecond, but they are not in the new code.
                // So, we just log the error and return empty metrics.
                logger.warn("Error retrieving metrics for tool-metrics: {}", e.getMessage());
            }
        } else {
            // Fallback to legacy counters
            // These counters are no longer maintained in this class, so this fallback is not applicable.
            // The original code had a fallback to totalRequests/totalErrors, but they are removed.
            // The original code also had a fallback for uptime/requestsPerSecond, but they are not in the new code.
            // So, we just log the error and return empty metrics.
            logger.warn("MetricsService not available, cannot get performance metrics.");
        }

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
    public Map<String, Object> getSystemResources() {
        Map<String, Object> resources = new HashMap<>();

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
            File file = new File(".");
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
    private double calculateHealthScore(Map<String, Object> status) {
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
            MetricsService metrics = metricsService;
            if (metrics != null) {
                try {
                    var snapshot = metrics.getDomainAggregatedSnapshot("tool-metrics");
                    return (double) snapshot.totalOperations() / (timeWindow / 1000.0);
                } catch (Exception e) {
                    logger.warn("Error retrieving metrics for tool-metrics: {}", e.getMessage());
                    // Fallback to legacy counter
                    return 0.0; // No legacy counter available
                }
            } else {
                // Fallback to legacy counter
                return 0.0; // No legacy counter available
            }
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
        MetricsService metrics = metricsService;
        if (metrics != null) {
            try {
                var snapshot = metrics.getDomainAggregatedSnapshot("tool-metrics");
                return Math.min(snapshot.totalOperations() / 100.0, 10.0); // Placeholder calculation
            } catch (Exception e) {
                logger.warn("Error retrieving metrics for tool-metrics: {}", e.getMessage());
                // Fallback to legacy counter
                return Math.min(0.0, 10.0); // No legacy counter available
            }
        } else {
            // Fallback to legacy counter
            return Math.min(0.0, 10.0); // No legacy counter available
        }
    }

    /**
     * Get peak concurrent requests.
     * 
     * @return Peak concurrent requests
     */
    private double getPeakConcurrentRequests() {
        // Simplified implementation - in a real system, this would track peak concurrent requests
        MetricsService metrics = metricsService;
        if (metrics != null) {
            try {
                var snapshot = metrics.getDomainAggregatedSnapshot("tool-metrics");
                return Math.min(snapshot.totalOperations() / 50.0, 20.0); // Placeholder calculation
            } catch (Exception e) {
                logger.warn("Error retrieving metrics for tool-metrics: {}", e.getMessage());
                // Fallback to legacy counter
                return Math.min(0.0, 20.0); // No legacy counter available
            }
        } else {
            // Fallback to legacy counter
            return Math.min(0.0, 20.0); // No legacy counter available
        }
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
            File file = new File(".");
            return file.getFreeSpace();
        } catch (Exception e) {
            logger.warn("Could not get disk free space", e);
            return 0L;
        }
    }
}
