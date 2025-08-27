package org.openhab.core.ai.tool.monitoring.health;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.api.MetricKeys;
import org.openhab.core.ai.common.monitoring.api.MetricKey;
import org.openhab.core.ai.common.monitoring.snapshot.ExecutionMetricsSnapshot;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of SystemCheck.
 * 
 * This class provides comprehensive health check capabilities including:
 * - Health check logic with multiple check types
 * - Health check dependencies for complex scenarios
 * - Performance monitoring and metrics collection
 * - Versioning support for health checks
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class DefaultSystemCheck implements SystemCheck {

    private static final Logger logger = LoggerFactory.getLogger(DefaultSystemCheck.class);

    private final String healthCheckId;
    private final String healthCheckName;
    private final String healthCheckDescription;
    private final String category;
    private final int priority;
    private final AtomicReference<Boolean> enabled = new AtomicReference<>(true);
    private final Map<String, Object> configuration = new ConcurrentHashMap<>();

    // Performance monitoring - using centralized MetricsService instead of AtomicLong counters
    private final AtomicReference<Long> lastCheckTime = new AtomicReference<>(0L);
    private final AtomicReference<Long> minCheckTime = new AtomicReference<>(Long.MAX_VALUE);
    private final AtomicReference<Long> maxCheckTime = new AtomicReference<>(0L);

    // Health check dependencies
    private final List<SystemCheck> dependencies = new ArrayList<>();
    private final AtomicInteger dependencyDepth = new AtomicInteger(0);

    // Versioning
    private final String version;
    private final AtomicReference<String> currentVersion = new AtomicReference<>();
    private final Map<String, String> versionHistory = new ConcurrentHashMap<>();

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    private volatile @Nullable MetricsService metricsService;

    /**
     * Create a new default system check.
     * 
     * @param healthCheckId the health check ID
     * @param healthCheckName the health check name
     * @param healthCheckDescription the health check description
     * @param category the health check category
     * @param priority the health check priority
     * @param version the health check version
     */
    public DefaultSystemCheck(String healthCheckId, String healthCheckName, String healthCheckDescription,
            String category, int priority, String version) {
        this.healthCheckId = healthCheckId;
        this.healthCheckName = healthCheckName;
        this.healthCheckDescription = healthCheckDescription;
        this.category = category;
        this.priority = priority;
        this.version = version;
        this.currentVersion.set(version);

        // Initialize default configuration
        configuration.put("timeoutMs", 5000);
        configuration.put("retryAttempts", 3);
        configuration.put("retryDelayMs", 1000);
        configuration.put("enableDependencies", true);
        configuration.put("maxDependencyDepth", 3);
        configuration.put("enablePerformanceMonitoring", true);
        configuration.put("enableVersioning", true);
    }

    @Override
    public String getHealthCheckId() {
        return healthCheckId;
    }

    @Override
    public String getHealthCheckName() {
        return healthCheckName;
    }

    @Override
    public String getHealthCheckDescription() {
        return healthCheckDescription;
    }

    @Override
    public String getCategory() {
        return category;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public boolean isEnabled() {
        return enabled.get();
    }

    @Override
    public SystemCheckResult performCheck() {
        // Implement health check logic
        long startTime = System.currentTimeMillis();

        try {
            logger.debug("Starting health check: {}", healthCheckName);

            // Check dependencies first
            boolean enableDependencies = (Boolean) configuration.getOrDefault("enableDependencies", true);
            int maxDependencyDepth = (Integer) configuration.getOrDefault("maxDependencyDepth", 3);

            if (enableDependencies && dependencyDepth.get() < maxDependencyDepth && !dependencies.isEmpty()) {
                return performDependencyCheck();
            }

            // Execute primary health check logic
            SystemCheckResult result = executePrimaryCheck();

            // Record performance metrics using centralized MetricsService
            long checkTime = System.currentTimeMillis() - startTime;
            recordCheckMetrics(result, checkTime);

            return result;

        } catch (Exception e) {
            logger.error("Error during health check execution: {}", e.getMessage(), e);
            
            // Record failure metrics using centralized MetricsService
            long checkTime = System.currentTimeMillis() - startTime;
            if (metricsService != null) {
                try {
                    metricsService.recordOperation("system-check", "health-check")
                        .withSuccess(false)
                        .withDuration(Duration.ofMillis(checkTime).toNanos())
                        .withData("healthCheckId", healthCheckId)
                        .withData("healthCheckName", healthCheckName)
                        .withData("category", category)
                        .withData("error", e.getMessage())
                        .record();
                } catch (Exception metricsError) {
                    logger.warn("Failed to record health check failure metrics", metricsError);
                }
            }

            return new SystemCheckResult(false, "FAILED", "Health check execution failed: " + e.getMessage(),
                    Map.of("error", e.getMessage(), "check", healthCheckName), System.currentTimeMillis());
        }
    }

    @Override
    public Map<String, Object> getConfiguration() {
        return new HashMap<>(configuration);
    }

    @Override
    public void updateConfiguration(Map<String, Object> configuration) {
        this.configuration.putAll(configuration);
        logger.debug("Configuration updated for health check: {}", healthCheckName);
    }

    /**
     * Add support for health check dependencies
     * 
     * @param dependency the dependency to add
     */
    public void addDependency(SystemCheck dependency) {
        dependencies.add(dependency);
        logger.debug("Added dependency: {} to {}", dependency.getHealthCheckName(), healthCheckName);
    }

    /**
     * Remove a dependency
     * 
     * @param dependency the dependency to remove
     */
    public void removeDependency(SystemCheck dependency) {
        dependencies.remove(dependency);
        logger.debug("Removed dependency: {} from {}", dependency.getHealthCheckName(), healthCheckName);
    }

    /**
     * Get all dependencies
     * 
     * @return list of dependencies
     */
    public List<SystemCheck> getDependencies() {
        return new ArrayList<>(dependencies);
    }

    /**
     * Implement health check performance monitoring
     * 
     * @return performance metrics
     */
    public Map<String, Object> getPerformanceMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        if (metricsService != null) {
            try {
                // Get health check metrics from centralized MetricsService
                MetricKey healthCheckKey = MetricKeys.execution("health-check");
                ExecutionMetricsSnapshot healthCheckSnapshot = metricsService.getSnapshot(healthCheckKey, ExecutionMetricsSnapshot.class);
                
                if (healthCheckSnapshot != null) {
                    metrics.put("totalChecks", healthCheckSnapshot.total());
                    metrics.put("successfulChecks", healthCheckSnapshot.success());
                    metrics.put("failedChecks", healthCheckSnapshot.failure());
                    metrics.put("successRate", healthCheckSnapshot.successRate());
                    metrics.put("averageCheckTimeMs", healthCheckSnapshot.averageMs());
                } else {
                    // Fallback to default values if no metrics available
                    metrics.put("totalChecks", 0L);
                    metrics.put("successfulChecks", 0L);
                    metrics.put("failedChecks", 0L);
                    metrics.put("successRate", 0.0);
                    metrics.put("averageCheckTimeMs", 0.0);
                }
                
                // Add local timing metrics
                metrics.put("minCheckTimeMs", minCheckTime.get() == Long.MAX_VALUE ? 0 : minCheckTime.get());
                metrics.put("maxCheckTimeMs", maxCheckTime.get());
                metrics.put("lastCheckTimeMs", lastCheckTime.get());
                metrics.put("currentDependencyDepth", dependencyDepth.get());
                metrics.put("dependenciesCount", dependencies.size());
                
            } catch (Exception e) {
                logger.warn("Failed to retrieve performance metrics from MetricsService", e);
                // Fallback to default values
                metrics.put("totalChecks", 0L);
                metrics.put("successfulChecks", 0L);
                metrics.put("failedChecks", 0L);
                metrics.put("successRate", 0.0);
                metrics.put("averageCheckTimeMs", 0.0);
                metrics.put("minCheckTimeMs", 0L);
                metrics.put("maxCheckTimeMs", 0L);
                metrics.put("lastCheckTimeMs", 0L);
                metrics.put("currentDependencyDepth", dependencyDepth.get());
                metrics.put("dependenciesCount", dependencies.size());
            }
        } else {
            // No MetricsService available, return default values
            metrics.put("totalChecks", 0L);
            metrics.put("successfulChecks", 0L);
            metrics.put("failedChecks", 0L);
            metrics.put("successRate", 0.0);
            metrics.put("averageCheckTimeMs", 0.0);
            metrics.put("minCheckTimeMs", 0L);
            metrics.put("maxCheckTimeMs", 0L);
            metrics.put("lastCheckTimeMs", 0L);
            metrics.put("currentDependencyDepth", dependencyDepth.get());
            metrics.put("dependenciesCount", dependencies.size());
        }

        return metrics;
    }

    /**
     * Add support for health check versioning
     * 
     * @param newVersion the new version
     * @param description the version description
     */
    public void updateVersion(String newVersion, String description) {
        String oldVersion = currentVersion.get();
        versionHistory.put(oldVersion, description);
        currentVersion.set(newVersion);
        logger.info("Updated health check version from {} to {}: {}", oldVersion, newVersion, description);
    }

    /**
     * Get version information
     * 
     * @return version information
     */
    public Map<String, Object> getVersionInfo() {
        Map<String, Object> versionInfo = new HashMap<>();
        versionInfo.put("currentVersion", currentVersion.get());
        versionInfo.put("originalVersion", version);
        versionInfo.put("versionHistory", new HashMap<>(versionHistory));
        return versionInfo;
    }

    /**
     * Execute primary health check logic
     * 
     * @return health check result
     */
    private SystemCheckResult executePrimaryCheck() {
        int timeoutMs = (Integer) configuration.getOrDefault("timeoutMs", 5000);
        int retryAttempts = (Integer) configuration.getOrDefault("retryAttempts", 3);
        long retryDelayMs = (Long) configuration.getOrDefault("retryDelayMs", 1000L);

        logger.debug("Executing primary health check: {}", healthCheckName);

        for (int attempt = 1; attempt <= retryAttempts; attempt++) {
            try {
                // Simulate health check execution
                Thread.sleep(100); // Simulate check time

                // Check if system is healthy based on category
                boolean isHealthy = performCategorySpecificCheck();

                if (isHealthy) {
                    return new SystemCheckResult(true, "HEALTHY", "Health check passed on attempt " + attempt,
                            Map.of("attempt", attempt, "category", category, "check", healthCheckName),
                            System.currentTimeMillis());
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return new SystemCheckResult(false, "FAILED", "Health check interrupted",
                        Map.of("error", e.getMessage(), "check", healthCheckName), System.currentTimeMillis());
            } catch (Exception e) {
                logger.warn("Health check attempt {} failed: {}", attempt, e.getMessage());
            }
        }

        return new SystemCheckResult(false, "FAILED", "Health check failed after " + retryAttempts + " attempts",
                Map.of("attempts", retryAttempts, "category", category, "check", healthCheckName),
                System.currentTimeMillis());
    }

    /**
     * Perform dependency check
     * 
     * @return health check result
     */
    private SystemCheckResult performDependencyCheck() {
        dependencyDepth.incrementAndGet();

        logger.debug("Performing dependency check, depth: {}", dependencyDepth.get());

        try {
            // Check all dependencies first
            for (SystemCheck dependency : dependencies) {
                if (dependency.isEnabled()) {
                    SystemCheckResult dependencyResult = dependency.performCheck();
                    if (!dependencyResult.isHealthy()) {
                        return new SystemCheckResult(false, "FAILED",
                                "Dependency check failed: " + dependency.getHealthCheckName(),
                                Map.of("failedDependency", dependency.getHealthCheckName(), "check", healthCheckName),
                                System.currentTimeMillis());
                    }
                }
            }

            // If all dependencies passed, perform primary check
            return executePrimaryCheck();

        } finally {
            dependencyDepth.decrementAndGet();
        }
    }

    /**
     * Perform category-specific health check
     * 
     * @return true if healthy
     */
    private boolean performCategorySpecificCheck() {
        switch (category.toLowerCase()) {
            case "memory":
                return checkMemoryHealth();
            case "cpu":
                return checkCpuHealth();
            case "disk":
                return checkDiskHealth();
            case "network":
                return checkNetworkHealth();
            case "database":
                return checkDatabaseHealth();
            case "service":
                return checkServiceHealth();
            default:
                return checkGenericHealth();
        }
    }

    /**
     * Check memory health
     * 
     * @return true if healthy
     */
    private boolean checkMemoryHealth() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        double memoryUsage = (double) usedMemory / totalMemory;

        return memoryUsage < 0.9; // Consider healthy if less than 90% used
    }

    /**
     * Check CPU health
     * 
     * @return true if healthy
     */
    private boolean checkCpuHealth() {
        // Simple CPU health check - in a real implementation, this would use JMX or similar
        return true; // Placeholder
    }

    /**
     * Check disk health
     * 
     * @return true if healthy
     */
    private boolean checkDiskHealth() {
        // Simple disk health check - in a real implementation, this would check disk space
        return true; // Placeholder
    }

    /**
     * Check network health
     * 
     * @return true if healthy
     */
    private boolean checkNetworkHealth() {
        // Simple network health check - in a real implementation, this would ping or check connectivity
        return true; // Placeholder
    }

    /**
     * Check database health
     * 
     * @return true if healthy
     */
    private boolean checkDatabaseHealth() {
        // Simple database health check - in a real implementation, this would check DB connectivity
        return true; // Placeholder
    }

    /**
     * Check service health
     * 
     * @return true if healthy
     */
    private boolean checkServiceHealth() {
        // Simple service health check - in a real implementation, this would check service status
        return true; // Placeholder
    }

    /**
     * Check generic health
     * 
     * @return true if healthy
     */
    private boolean checkGenericHealth() {
        // Generic health check - always return true for now
        return true;
    }

    /**
     * Record check metrics using centralized MetricsService
     * 
     * @param result the check result
     * @param checkTime the check time in milliseconds
     */
    private void recordCheckMetrics(SystemCheckResult result, long checkTime) {
        // Update local timing metrics
        lastCheckTime.set(checkTime);
        minCheckTime.updateAndGet(current -> Math.min(current, checkTime));
        maxCheckTime.updateAndGet(current -> Math.max(current, checkTime));

        // Record metrics using centralized MetricsService
        if (metricsService != null) {
            try {
                metricsService.recordOperation("system-check", "health-check")
                    .withSuccess(result.isHealthy())
                    .withDuration(Duration.ofMillis(checkTime).toNanos())
                    .withData("healthCheckId", healthCheckId)
                    .withData("healthCheckName", healthCheckName)
                    .withData("category", category)
                    .withData("priority", priority)
                    .withData("checkTimeMs", checkTime)
                    .withData("dependenciesCount", dependencies.size())
                    .withData("dependencyDepth", dependencyDepth.get())
                    .record();
            } catch (Exception e) {
                logger.warn("Failed to record health check metrics", e);
            }
        }

        logger.debug("Recorded check metrics - Check: {}, Success: {}, Time: {}ms", healthCheckName, result.isHealthy(),
                checkTime);
    }
}
