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
import java.util.Map;
import java.util.Set;
import org.openhab.core.ai.common.monitoring.api.MetricKey;
import org.openhab.core.ai.common.monitoring.api.MetricKeys;
import org.openhab.core.ai.tool.config.ToolServerConfiguration;
import org.openhab.core.ai.tool.server.http.HealthHandler;
import org.openhab.core.ai.tool.server.http.MetricsHandler;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
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
@Component(service = ToolMetricsEndpoint.class, immediate = true)
public class ToolMetricsEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(ToolMetricsEndpoint.class);

    // OSGi service references
    @Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC)
    private @Nullable DefaultToolServer serverInstance;

    @Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC)
    private @Nullable ToolServerConfiguration config;

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    private @Nullable MetricsService metricsService;

    // Instance fields
    private @Nullable HttpServer httpServer;
    private @Nullable ScheduledExecutorService executor;

    // Start time for uptime calculation
    private final long startTime = System.currentTimeMillis();

    @Activate
    protected void activate() {
        logger.info("Activating ToolMetricsEndpoint");
        
        try {
            // Validate required dependencies
            if (serverInstance == null) {
                throw new IllegalStateException("DefaultToolServer not available");
            }
            if (config == null) {
                throw new IllegalStateException("ToolServerConfiguration not available");
            }

            // Create HTTP server on configurable port
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

            // Start HTTP server
            httpServer.start();
            logger.info("ToolMetricsEndpoint activated on port {}", port);
            
        } catch (Exception e) {
            logger.error("Failed to activate ToolMetricsEndpoint", e);
            throw new RuntimeException("Failed to activate ToolMetricsEndpoint", e);
        }
    }

    @Deactivate
    protected void deactivate() {
        logger.info("Deactivating ToolMetricsEndpoint");
        
        try {
            // Stop HTTP server
            if (httpServer != null) {
                httpServer.stop(0);
                logger.debug("HTTP server stopped");
            }
            
            // Shutdown executor gracefully
            if (executor != null) {
                executor.shutdown();
                try {
                    if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                        executor.shutdownNow();
                        logger.warn("Executor did not terminate gracefully, forced shutdown");
                    }
                } catch (InterruptedException e) {
                    executor.shutdownNow();
                    Thread.currentThread().interrupt();
                    logger.warn("Executor shutdown interrupted", e);
                }
            }
            
            logger.info("ToolMetricsEndpoint deactivated successfully");
            
        } catch (Exception e) {
            logger.error("Error during ToolMetricsEndpoint deactivation", e);
        }
    }

    @Modified
    protected void modified() {
        logger.info("ToolMetricsEndpoint configuration modified");
        
        try {
            // Validate new configuration
            if (config == null) {
                logger.warn("Configuration not available during modification");
                return;
            }
            
            // Check if port has changed
            int newPort = config.getSsePort();
            if (httpServer != null && httpServer.getAddress().getPort() != newPort) {
                logger.info("Port configuration changed from {} to {}, restarting HTTP server", 
                    httpServer.getAddress().getPort(), newPort);
                
                // Restart HTTP server with new port
                httpServer.stop(0);
                httpServer = HttpServer.create(new InetSocketAddress(newPort), 0);
                
                // Recreate endpoints with new configuration
                httpServer.createContext("/health", new HealthHandler(serverInstance, config, startTime, metricsService));
                httpServer.createContext("/metrics", new MetricsHandler(serverInstance, startTime, metricsService));
                
                httpServer.start();
                logger.info("HTTP server restarted on new port {}", newPort);
            } else {
                logger.debug("Configuration modified but no restart required");
            }
            
        } catch (Exception e) {
            logger.error("Error handling configuration modification", e);
        }
    }

    /**
     * Unset method for MetricsService (required for dynamic reference).
     * 
     * @param metricsService the metrics service being unset
     */
    public void unsetMetricsService(@Nullable MetricsService metricsService) {
        this.metricsService = null;
        logger.debug("MetricsService unset for ToolMetricsEndpoint");
    }

    /**
     * Record metrics for an operation with graceful degradation.
     * 
     * @param operation the operation name
     * @param success whether the operation was successful
     * @param durationNanos the operation duration in nanoseconds
     */
    private void recordMetrics(String operation, boolean success, long durationNanos) {
        try {
            if (metricsService != null) {
                metricsService.recordOperation("tool-metrics", operation)
                    .withSuccess(success)
                    .withDuration(durationNanos)
                    .withData("operation", operation)
                    .withData("durationNanos", durationNanos)
                    .record();
            } else {
                logger.debug("MetricsService not available, skipping metrics recording for operation: {}", operation);
            }
        } catch (Exception e) {
            logger.warn("Failed to record metrics for {}.{}: {}", "tool-metrics", operation, e.getMessage());
        }
    }

    private void performHealthCheck() {
        try {
            if (serverInstance == null) {
                logger.warn("DefaultToolServer not available for health check");
                return;
            }
            
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

        try {
            if (serverInstance == null) {
                logger.warn("DefaultToolServer not available for health status");
                status.put("serverRunning", false);
                status.put("transportHealthy", false);
                status.put("uptime", System.currentTimeMillis() - startTime);
                return status;
            }

            // Basic health checks
            status.put("serverRunning", serverInstance.isRunning());
            status.put("transportHealthy", serverInstance.isHealthy());
            status.put("uptime", System.currentTimeMillis() - startTime);

            // Record health check metrics
            boolean healthy = serverInstance.isRunning() && serverInstance.isHealthy();
            recordMetrics("health-status", healthy, 0);
            
        } catch (Exception e) {
            logger.error("Error getting health status", e);
            status.put("serverRunning", false);
            status.put("transportHealthy", false);
            status.put("uptime", System.currentTimeMillis() - startTime);
            recordMetrics("health-status", false, 0);
        }

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
        if (metricsService != null) {
            try {
                MetricKey toolMetricsKey = MetricKeys.custom("tool-metrics", Map.of(), Set.of("counts", "latency"));
                var snapshot = metricsService.getSnapshot(toolMetricsKey, org.openhab.core.ai.common.monitoring.service.snapshot.GenericMetricsSnapshot.class);
                
                if (snapshot != null) {
                    long totalRequests = snapshot.getLong("total");
                    long totalErrors = snapshot.getLong("failure");
                    metrics.put("totalRequests", totalRequests);
                    metrics.put("totalErrors", totalErrors);
                    metrics.put("successfulRequests", totalRequests - totalErrors);
                    metrics.put("errorRate",
                            totalRequests > 0
                                    ? (double) totalErrors / totalRequests
                                    : 0.0);
                }

                // Uptime metrics
                long uptime = System.currentTimeMillis() - startTime;
                metrics.put("uptime", uptime);
                
                // Calculate requests per second using the totalRequests from above
                if (snapshot != null) {
                    long totalRequests = snapshot.getLong("total");
                    metrics.put("requestsPerSecond",
                            uptime > 0 ? (double) totalRequests / (uptime / 1000.0) : 0.0);
                } else {
                    metrics.put("requestsPerSecond", 0.0);
                }
            } catch (Exception e) {
                logger.warn("Error retrieving metrics for tool-metrics: {}", e.getMessage());
            }
        } else {
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
            if (metricsService != null) {
                try {
                    MetricKey toolMetricsKey = MetricKeys.custom("tool-metrics", Map.of(), Set.of("counts", "latency"));
                    var snapshot = metricsService.getSnapshot(toolMetricsKey, org.openhab.core.ai.common.monitoring.service.snapshot.GenericMetricsSnapshot.class);
                    return snapshot != null ? (double) snapshot.getLong("total") / (timeWindow / 1000.0) : 0.0;
                } catch (Exception e) {
                    logger.warn("Error retrieving metrics for tool-metrics: {}", e.getMessage());
                    return 0.0;
                }
            } else {
                return 0.0;
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
        if (metricsService != null) {
            try {
                MetricKey toolMetricsKey = MetricKeys.custom("tool-metrics", Map.of(), Set.of("counts", "latency"));
                var snapshot = metricsService.getSnapshot(toolMetricsKey, org.openhab.core.ai.common.monitoring.service.snapshot.GenericMetricsSnapshot.class);
                return snapshot != null ? Math.min(snapshot.getLong("total") / 100.0, 10.0) : 0.0; // Placeholder calculation
            } catch (Exception e) {
                logger.warn("Error retrieving metrics for tool-metrics: {}", e.getMessage());
                return Math.min(0.0, 10.0);
            }
        } else {
            return Math.min(0.0, 10.0);
        }
    }

    /**
     * Get peak concurrent requests.
     * 
     * @return Peak concurrent requests
     */
    private double getPeakConcurrentRequests() {
        // Simplified implementation - in a real system, this would track peak concurrent requests
        if (metricsService != null) {
            try {
                MetricKey toolMetricsKey = MetricKeys.custom("tool-metrics", Map.of(), Set.of("counts", "latency"));
                var snapshot = metricsService.getSnapshot(toolMetricsKey, org.openhab.core.ai.common.monitoring.service.snapshot.GenericMetricsSnapshot.class);
                return snapshot != null ? Math.min(snapshot.getLong("total") / 50.0, 20.0) : 0.0; // Placeholder calculation
            } catch (Exception e) {
                logger.warn("Error retrieving metrics for tool-metrics: {}", e.getMessage());
                return Math.min(0.0, 20.0);
            }
        } else {
            return Math.min(0.0, 20.0);
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
