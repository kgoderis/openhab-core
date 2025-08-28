package org.openhab.core.ai.tool.server;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.MetricKey;
import org.openhab.core.ai.common.monitoring.api.MetricKeys;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
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

    // Request counters for HTTP handlers (legacy support)
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong totalErrors = new AtomicLong(0);

    @Activate
    protected void activate() {
        logger.info("Activating ToolMetricsEndpoint");

        try {
            // Validate required dependencies
            if (serverInstance == null) {
                logger.error("ToolMetricsEndpoint activation failed: DefaultToolServer not available");
                recordMetrics("endpoint-activation", false, 0);
                throw new IllegalStateException("DefaultToolServer not available");
            }
            if (config == null) {
                logger.error("ToolMetricsEndpoint activation failed: ToolServerConfiguration not available");
                recordMetrics("endpoint-activation", false, 0);
                throw new IllegalStateException("ToolServerConfiguration not available");
            }

            // Create HTTP server on configurable port
            int port = config.getSsePort();
            if (port <= 0 || port > 65535) {
                logger.error("ToolMetricsEndpoint activation failed: Invalid port configuration: {}", port);
                recordMetrics("endpoint-activation", false, 0);
                throw new IllegalArgumentException("Invalid port configuration: " + port);
            }

            this.httpServer = HttpServer.create(new InetSocketAddress(port), 0);

            // Set up endpoints - always enable health and metrics for now
            try {
                httpServer.createContext("/health", new HealthHandler(serverInstance, config, startTime, totalRequests,
                        totalErrors, metricsService));
                logger.info("Health endpoint enabled at /health");
            } catch (Exception e) {
                logger.error("Failed to create health endpoint: {}", e.getMessage(), e);
                recordMetrics("endpoint-creation", false, 0);
                throw e;
            }

            try {
                httpServer.createContext("/metrics",
                        new MetricsHandler(serverInstance, startTime, totalRequests, totalErrors, metricsService));
                logger.info("Metrics endpoint enabled at /metrics");
            } catch (Exception e) {
                logger.error("Failed to create metrics endpoint: {}", e.getMessage(), e);
                recordMetrics("endpoint-creation", false, 0);
                throw e;
            }

            // Create executor for background tasks
            this.executor = Executors.newScheduledThreadPool(1);

            // Start health check scheduler
            executor.scheduleAtFixedRate(this::performHealthCheck, 30000, // 30 seconds
                    30000, TimeUnit.MILLISECONDS);

            // Start HTTP server
            httpServer.start();
            logger.info("ToolMetricsEndpoint activated on port {}", port);
            recordMetrics("endpoint-activation", true, 0);

        } catch (Exception e) {
            logger.error("Failed to activate ToolMetricsEndpoint: {}", e.getMessage(), e);
            recordMetrics("endpoint-activation", false, 0);
            // Graceful degradation - ensure partial cleanup before rethrowing
            if (executor != null) {
                try {
                    executor.shutdown();
                } catch (Exception cleanupError) {
                    logger.warn("Error during activation cleanup: {}", cleanupError.getMessage());
                }
            }
            throw new RuntimeException("Failed to activate ToolMetricsEndpoint: " + e.getMessage(), e);
        }
    }

    @Deactivate
    protected void deactivate() {
        logger.info("Deactivating ToolMetricsEndpoint");

        try {
            // Stop HTTP server
            if (httpServer != null) {
                try {
                    httpServer.stop(0);
                    logger.debug("HTTP server stopped");
                    recordMetrics("endpoint-deactivation", true, 0);
                } catch (Exception e) {
                    logger.error("Error stopping HTTP server during deactivation: {}", e.getMessage(), e);
                    recordMetrics("endpoint-deactivation", false, 0);
                }
            }

            // Shutdown executor gracefully
            if (executor != null) {
                try {
                    executor.shutdown();
                    if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                        executor.shutdownNow();
                        logger.warn("Executor did not terminate gracefully, forced shutdown");
                        recordMetrics("executor-shutdown", false, 0);
                    } else {
                        recordMetrics("executor-shutdown", true, 0);
                    }
                } catch (InterruptedException e) {
                    executor.shutdownNow();
                    Thread.currentThread().interrupt();
                    logger.warn("Executor shutdown interrupted: {}", e.getMessage(), e);
                    recordMetrics("executor-shutdown", false, 0);
                } catch (Exception e) {
                    logger.error("Error shutting down executor: {}", e.getMessage(), e);
                    recordMetrics("executor-shutdown", false, 0);
                }
            }

            logger.info("ToolMetricsEndpoint deactivated successfully");

        } catch (Exception e) {
            logger.error("Error during ToolMetricsEndpoint deactivation: {}", e.getMessage(), e);
            recordMetrics("endpoint-deactivation", false, 0);
        }
    }

    @Modified
    protected void modified() {
        logger.info("ToolMetricsEndpoint configuration modified");

        try {
            // Validate new configuration
            if (config == null) {
                logger.warn("Configuration not available during modification");
                recordMetrics("config-modification", false, 0);
                return;
            }

            // Check if port has changed
            int newPort = config.getSsePort();
            if (newPort <= 0 || newPort > 65535) {
                logger.error("Invalid port configuration during modification: {}", newPort);
                recordMetrics("config-modification", false, 0);
                return;
            }

            if (httpServer != null && httpServer.getAddress().getPort() != newPort) {
                logger.info("Port configuration changed from {} to {}, restarting HTTP server",
                        httpServer.getAddress().getPort(), newPort);

                try {
                    // Restart HTTP server with new port
                    httpServer.stop(0);
                    httpServer = HttpServer.create(new InetSocketAddress(newPort), 0);

                    // Recreate endpoints with new configuration
                    try {
                        httpServer.createContext("/health", new HealthHandler(serverInstance, config, startTime,
                                totalRequests, totalErrors, metricsService));
                    } catch (Exception e) {
                        logger.error("Failed to recreate health endpoint during modification: {}", e.getMessage(), e);
                        recordMetrics("config-modification", false, 0);
                        throw e;
                    }

                    try {
                        httpServer.createContext("/metrics", new MetricsHandler(serverInstance, startTime,
                                totalRequests, totalErrors, metricsService));
                    } catch (Exception e) {
                        logger.error("Failed to recreate metrics endpoint during modification: {}", e.getMessage(), e);
                        recordMetrics("config-modification", false, 0);
                        throw e;
                    }

                    httpServer.start();
                    logger.info("HTTP server restarted on new port {}", newPort);
                    recordMetrics("config-modification", true, 0);
                } catch (Exception e) {
                    logger.error("Failed to restart HTTP server on new port {}: {}", newPort, e.getMessage(), e);
                    recordMetrics("config-modification", false, 0);
                    throw e;
                }
            } else {
                logger.debug("Configuration modified but no restart required");
                recordMetrics("config-modification", true, 0);
            }

        } catch (Exception e) {
            logger.error("Error handling configuration modification: {}", e.getMessage(), e);
            recordMetrics("config-modification", false, 0);
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
                metricsService.recordOperation("tool-metrics", operation).withSuccess(success)
                        .withDuration(durationNanos).withData("operation", operation)
                        .withData("durationNanos", durationNanos).record();
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

        try {
            // Request metrics - get from MetricsService if available
            if (metricsService != null) {
                try {
                    MetricKey toolMetricsKey = MetricKeys.custom("tool-metrics", Map.of(), Set.of("counts", "latency"));
                    var snapshot = metricsService.getSnapshot(toolMetricsKey,
                            org.openhab.core.ai.common.monitoring.service.snapshot.GenericMetricsSnapshot.class);

                    if (snapshot != null) {
                        long totalRequests = snapshot.getLong("total");
                        long totalErrors = snapshot.getLong("failure");
                        metrics.put("totalRequests", totalRequests);
                        metrics.put("totalErrors", totalErrors);
                        metrics.put("successfulRequests", totalRequests - totalErrors);
                        metrics.put("errorRate", totalRequests > 0 ? (double) totalErrors / totalRequests : 0.0);

                        // Uptime metrics
                        long uptime = System.currentTimeMillis() - startTime;
                        metrics.put("uptime", uptime);

                        // Calculate requests per second using the totalRequests from above
                        metrics.put("requestsPerSecond", uptime > 0 ? (double) totalRequests / (uptime / 1000.0) : 0.0);
                    } else {
                        logger.debug("No metrics snapshot available for tool-metrics");
                        // Set default values when no metrics are available
                        metrics.put("totalRequests", 0);
                        metrics.put("totalErrors", 0);
                        metrics.put("successfulRequests", 0);
                        metrics.put("errorRate", 0.0);
                        metrics.put("uptime", System.currentTimeMillis() - startTime);
                        metrics.put("requestsPerSecond", 0.0);
                    }
                } catch (Exception e) {
                    logger.warn("Error retrieving metrics for tool-metrics: {}", e.getMessage());
                    metrics.put("error", "Failed to retrieve performance metrics: " + e.getMessage());
                    recordMetrics("performance-metrics-retrieval", false, 0);
                }
            } else {
                logger.debug("MetricsService not available, returning basic performance metrics");
                // Set basic metrics when MetricsService is not available
                metrics.put("totalRequests", 0);
                metrics.put("totalErrors", 0);
                metrics.put("successfulRequests", 0);
                metrics.put("errorRate", 0.0);
                metrics.put("uptime", System.currentTimeMillis() - startTime);
                metrics.put("requestsPerSecond", 0.0);
                metrics.put("note", "MetricsService not available");
            }

            // System metrics
            try {
                Runtime runtime = Runtime.getRuntime();
                double memoryUsage = (double) (runtime.totalMemory() - runtime.freeMemory()) / runtime.totalMemory();
                int threadCount = Thread.activeCount();

                metrics.put("memoryUsage", memoryUsage);
                metrics.put("threadCount", threadCount);

                recordMetrics("performance-metrics-retrieval", true, 0);
            } catch (Exception e) {
                logger.warn("Error retrieving system metrics: {}", e.getMessage());
                metrics.put("memoryUsage", 0.0);
                metrics.put("threadCount", 0);
                recordMetrics("performance-metrics-retrieval", false, 0);
            }

        } catch (Exception e) {
            logger.error("Unexpected error in getPerformanceMetrics: {}", e.getMessage(), e);
            metrics.put("error", "Unexpected error retrieving performance metrics: " + e.getMessage());
            recordMetrics("performance-metrics-retrieval", false, 0);
        }

        return metrics;
    }

    /**
     * Get system resources.
     * 
     * @return System resources as a map
     */
    public Map<String, Object> getSystemResources() {
        Map<String, Object> resources = new HashMap<>();

        try {
            // Memory information
            try {
                Runtime runtime = Runtime.getRuntime();
                long totalMemory = runtime.totalMemory();
                long freeMemory = runtime.freeMemory();
                long usedMemory = totalMemory - freeMemory;

                resources.put("totalMemory", totalMemory);
                resources.put("usedMemory", usedMemory);
                resources.put("freeMemory", freeMemory);
                resources.put("memoryUsagePercentage", totalMemory > 0 ? (double) usedMemory / totalMemory * 100 : 0.0);
                resources.put("availableProcessors", runtime.availableProcessors());

                recordMetrics("system-memory-retrieval", true, 0);
            } catch (Exception e) {
                logger.warn("Error retrieving memory information: {}", e.getMessage(), e);
                resources.put("totalMemory", 0L);
                resources.put("usedMemory", 0L);
                resources.put("freeMemory", 0L);
                resources.put("memoryUsagePercentage", 0.0);
                resources.put("availableProcessors", 0);
                recordMetrics("system-memory-retrieval", false, 0);
            }

            // Thread information
            try {
                int threadCount = Thread.activeCount();
                resources.put("threadCount", threadCount);
                recordMetrics("system-thread-retrieval", true, 0);
            } catch (Exception e) {
                logger.warn("Error retrieving thread information: {}", e.getMessage(), e);
                resources.put("threadCount", 0);
                recordMetrics("system-thread-retrieval", false, 0);
            }

            // Disk information
            try {
                File file = new File(".");
                long diskFreeSpace = file.getFreeSpace();
                long diskTotalSpace = file.getTotalSpace();
                long diskUsableSpace = file.getUsableSpace();

                resources.put("diskFreeSpace", diskFreeSpace);
                resources.put("diskTotalSpace", diskTotalSpace);
                resources.put("diskUsableSpace", diskUsableSpace);
                resources.put("diskUsagePercentage",
                        diskTotalSpace > 0 ? (double) (diskTotalSpace - diskFreeSpace) / diskTotalSpace * 100 : 0.0);

                recordMetrics("system-disk-retrieval", true, 0);
            } catch (Exception e) {
                logger.warn("Error retrieving disk information: {}", e.getMessage(), e);
                resources.put("diskFreeSpace", 0L);
                resources.put("diskTotalSpace", 0L);
                resources.put("diskUsableSpace", 0L);
                resources.put("diskUsagePercentage", 0.0);
                recordMetrics("system-disk-retrieval", false, 0);
            }

        } catch (Exception e) {
            logger.error("Unexpected error in getSystemResources: {}", e.getMessage(), e);
            resources.put("error", "Unexpected error retrieving system resources: " + e.getMessage());
            recordMetrics("system-resources-retrieval", false, 0);
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
        try {
            long currentTime = System.currentTimeMillis();
            long timeWindow = currentTime - startTime;

            if (timeWindow > 0) {
                if (metricsService != null) {
                    try {
                        MetricKey toolMetricsKey = MetricKeys.custom("tool-metrics", Map.of(),
                                Set.of("counts", "latency"));
                        var snapshot = metricsService.getSnapshot(toolMetricsKey,
                                org.openhab.core.ai.common.monitoring.service.snapshot.GenericMetricsSnapshot.class);
                        double result = snapshot != null ? (double) snapshot.getLong("total") / (timeWindow / 1000.0)
                                : 0.0;
                        recordMetrics("requests-per-second-calculation", true, 0);
                        return result;
                    } catch (Exception e) {
                        logger.warn("Error retrieving metrics for requests per second calculation: {}", e.getMessage());
                        recordMetrics("requests-per-second-calculation", false, 0);
                        return 0.0;
                    }
                } else {
                    logger.debug("MetricsService not available for requests per second calculation");
                    return 0.0;
                }
            }
            return 0.0;
        } catch (Exception e) {
            logger.error("Unexpected error in calculateRequestsPerSecond: {}", e.getMessage(), e);
            recordMetrics("requests-per-second-calculation", false, 0);
            return 0.0;
        }
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
                var snapshot = metricsService.getSnapshot(toolMetricsKey,
                        org.openhab.core.ai.common.monitoring.service.snapshot.GenericMetricsSnapshot.class);
                return snapshot != null ? Math.min(snapshot.getLong("total") / 100.0, 10.0) : 0.0; // Placeholder
                                                                                                   // calculation
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
                var snapshot = metricsService.getSnapshot(toolMetricsKey,
                        org.openhab.core.ai.common.monitoring.service.snapshot.GenericMetricsSnapshot.class);
                return snapshot != null ? Math.min(snapshot.getLong("total") / 50.0, 20.0) : 0.0; // Placeholder
                                                                                                  // calculation
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
