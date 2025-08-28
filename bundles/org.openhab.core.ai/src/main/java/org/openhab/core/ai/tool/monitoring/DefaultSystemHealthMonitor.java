package org.openhab.core.ai.tool.monitoring;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.HealthMetrics;
import org.openhab.core.ai.common.monitoring.api.HealthStatus;
import org.openhab.core.ai.common.monitoring.api.MetricKeys;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.model.api.ModelProviderType;
import org.openhab.core.ai.tool.monitoring.api.SystemHealthMonitor;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default System Health Monitor - Provides comprehensive health monitoring and circuit breaker functionality
 * 
 * <p>
 * This service provides:
 * - Health checking for tool providers and services
 * - Performance metrics collection and analysis
 * - Circuit breaker pattern implementation
 * - Failure detection and alerting
 * - Recovery mechanisms and automatic healing
 * - Health reporting and status aggregation
 * </p>
 * 
 * <h3>Monitoring Flow</h3>
 * 
 * <pre>{@code
 * Health Check → Metrics Collection → Failure Detection → Circuit Breaker → Recovery
 *      ↓              ↓                   ↓                   ↓              ↓
 * Status Update → Performance Analysis → Alert Generation → State Management → Healing
 * }</pre>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@Component(service = SystemHealthMonitor.class)
@NonNullByDefault
public class DefaultSystemHealthMonitor implements SystemHealthMonitor {

    private static final Logger logger = LoggerFactory.getLogger(DefaultSystemHealthMonitor.class);

    // Health state tracking
    private final ConcurrentHashMap<ModelProviderType, ProviderHealthState> providerHealthStates = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ServiceHealthState> serviceHealthStates = new ConcurrentHashMap<>();

    // Circuit breaker configuration - using simple variables instead of AtomicReference
    private volatile int failureThreshold = 5;
    private volatile Duration recoveryTimeout = Duration.ofMinutes(5);
    private volatile Duration healthCheckInterval = Duration.ofSeconds(30);

    // Performance thresholds - using simple variables instead of AtomicReference
    private volatile long maxResponseTime = 5000L; // 5 seconds
    private volatile double minSuccessRate = 0.8; // 80%

    // Monitoring state - using simple variables instead of AtomicReference
    private volatile boolean monitoringEnabled = true;
    private volatile boolean autoRecoveryEnabled = true;

    // Performance monitoring for specifications - using simple Map for now
    private final ConcurrentHashMap<String, SpecificationPerformanceMetrics> specificationMetrics = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, PerformanceAlert> performanceAlerts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, PerformanceOptimization> performanceOptimizations = new ConcurrentHashMap<>();

    // Performance thresholds for specifications - using simple variables instead of AtomicReference
    private volatile long maxSpecificationResponseTime = 3000L; // 3 seconds
    private volatile double minSpecificationSuccessRate = 0.9; // 90%
    private volatile int maxSpecificationThroughput = 100; // 100 req/sec

    @Reference
    private MetricsService metricsService;

    /**
     * Check if a provider is healthy
     * 
     * @param provider the provider to check
     * @return true if the provider is healthy
     */
    public boolean isProviderHealthy(ModelProviderType provider) {
        ProviderHealthState state = providerHealthStates.get(provider);
        if (state == null) {
            // If no state exists, consider it healthy and create initial state
            state = new ProviderHealthState(provider, metricsService);
            providerHealthStates.put(provider, state);
        }
        return state.isHealthy();
    }

    /**
     * Check if a service is healthy
     * 
     * @param serviceName the service name to check
     * @return true if the service is healthy
     */
    public boolean isServiceHealthy(String serviceName) {
        ServiceHealthState state = serviceHealthStates.get(serviceName);
        if (state == null) {
            // If no state exists, consider it healthy and create initial state
            state = new ServiceHealthState(serviceName, metricsService);
            serviceHealthStates.put(serviceName, state);
        }
        return state.isHealthy();
    }

    /**
     * Record a successful operation
     * 
     * @param provider the provider that handled the operation
     * @param responseTime the response time in milliseconds
     */
    public void recordSuccess(ModelProviderType provider, long responseTime) {
        ProviderHealthState state = getOrCreateProviderState(provider);
        state.recordSuccess(responseTime);
        logger.debug("Recorded success for provider {}: {}ms", provider, responseTime);
    }

    /**
     * Record a failed operation
     * 
     * @param provider the provider that failed
     * @param error the error that occurred
     */
    public void recordFailure(ModelProviderType provider, Exception error) {
        ProviderHealthState state = getOrCreateProviderState(provider);
        state.recordFailure(error);
        logger.warn("Recorded failure for provider {}: {}", provider, error.getMessage());
    }

    /**
     * Record a successful service operation
     * 
     * @param serviceName the service name
     * @param responseTime the response time in milliseconds
     */
    public void recordServiceSuccess(String serviceName, long responseTime) {
        ServiceHealthState state = getOrCreateServiceState(serviceName);
        state.recordSuccess(responseTime);
        logger.debug("Recorded service success for {}: {}ms", serviceName, responseTime);
    }

    /**
     * Record a failed service operation
     * 
     * @param serviceName the service name
     * @param error the error that occurred
     */
    public void recordServiceFailure(String serviceName, Exception error) {
        ServiceHealthState state = getOrCreateServiceState(serviceName);
        state.recordFailure(error);
        logger.warn("Recorded service failure for {}: {}", serviceName, error.getMessage());
    }

    /**
     * Perform a health check on a provider
     * 
     * @param provider the provider to check
     * @return CompletableFuture with health check result
     */
    public CompletableFuture<HealthCheckResult> performHealthCheck(ModelProviderType provider) {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            try {
                logger.debug("Starting health check for provider: {}", provider);

                // Perform comprehensive health check
                boolean isHealthy = performComprehensiveProviderHealthCheck(provider);
                long responseTime = measureHealthCheckResponseTime(provider);

                HealthCheckResult result = new HealthCheckResult(provider, isHealthy, responseTime, null);

                // Update health state
                ProviderHealthState state = getOrCreateProviderState(provider);
                state.updateFromHealthCheck(result);

                // Record health check operation using MetricsService
                if (metricsService != null) {
                    try {
                        metricsService.recordOperation("system-health-monitor", "provider-health-check", isHealthy,
                                Duration.ofMillis(System.currentTimeMillis() - startTime));
                    } catch (Exception e) {
                        logger.warn("Failed to record health check metrics for provider: {}", provider, e);
                    }
                }

                logger.debug("Health check completed for provider {}: healthy={}, responseTime={}ms", provider,
                        isHealthy, responseTime);

                return result;
            } catch (Exception e) {
                // Record failed health check operation
                if (metricsService != null) {
                    try {
                        metricsService.recordOperation("system-health-monitor", "provider-health-check", false,
                                Duration.ofMillis(System.currentTimeMillis() - startTime));
                    } catch (Exception ex) {
                        logger.warn("Failed to record failed health check metrics for provider: {}", provider, ex);
                    }
                }

                logger.error("Health check failed for provider {}", provider, e);
                return new HealthCheckResult(provider, false, 0, e);
            }
        });
    }

    /**
     * Perform a health check on a service
     * 
     * @param serviceName the service to check
     * @return CompletableFuture with health check result
     */
    @Override
    public CompletableFuture<HealthCheckResult> performServiceHealthCheck(String serviceName) {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            try {
                // TODO: Implement actual service health check logic
                boolean isHealthy = performServiceHealthCheckLogic(serviceName);
                long responseTime = measureServiceHealthCheckResponseTime(serviceName);

                HealthCheckResult result = new HealthCheckResult(serviceName, isHealthy, responseTime, null);

                // Update health state
                ServiceHealthState state = getOrCreateServiceState(serviceName);
                state.updateFromHealthCheck(isHealthy, responseTime);

                // Record service health check operation using MetricsService
                if (metricsService != null) {
                    try {
                        metricsService.recordOperation("system-health-monitor", "service-health-check", isHealthy,
                                Duration.ofMillis(System.currentTimeMillis() - startTime));
                    } catch (Exception e) {
                        logger.warn("Failed to record service health check metrics for service: {}", serviceName, e);
                    }
                }

                return result;
            } catch (Exception e) {
                // Record failed service health check operation
                if (metricsService != null) {
                    try {
                        metricsService.recordOperation("system-health-monitor", "service-health-check", false,
                                Duration.ofMillis(System.currentTimeMillis() - startTime));
                    } catch (Exception ex) {
                        logger.warn("Failed to record failed service health check metrics for service: {}", serviceName,
                                ex);
                    }
                }

                logger.error("Service health check failed for {}", serviceName, e);
                return new HealthCheckResult(serviceName, false, 0, e);
            }
        });
    }

    /**
     * Get health metrics for a provider
     * 
     * @param provider the provider
     * @return provider health metrics
     */
    @Override
    public HealthMetrics getProviderHealthMetrics(ModelProviderType provider) {
        ProviderHealthState state = getOrCreateProviderState(provider);

        // Record health check operation using MetricsService
        if (metricsService != null) {
            try {
                metricsService.recordOperation("system-health-monitor", "provider-health-check", state.isHealthy(),
                        Duration.ofMillis(50));
            } catch (Exception e) {
                logger.warn("Failed to record provider health metrics for provider: {}", provider, e);
            }
        }

        // Return health metrics from service
        try {
            return (HealthMetrics) metricsService.getSnapshot(MetricKeys.provider(provider.name()));
        } catch (Exception e) {
            logger.warn("Failed to get provider health metrics snapshot for provider: {}", provider, e);
            // Return a default/empty health metrics if service is unavailable
            return new DefaultHealthMetrics();
        }
    }

    /**
     * Get health metrics for a service
     * 
     * @param serviceName the service name
     * @return service health metrics
     */
    @Override
    public HealthMetrics getServiceHealthMetrics(String serviceName) {
        ServiceHealthState state = getOrCreateServiceState(serviceName);

        // Record health check operation using MetricsService
        if (metricsService != null) {
            try {
                metricsService.recordOperation("system-health-monitor", "service-health-check", state.isHealthy(),
                        Duration.ofMillis(50));
            } catch (Exception e) {
                logger.warn("Failed to record service health metrics for service: {}", serviceName, e);
            }
        }

        // Return health metrics from service
        try {
            return (HealthMetrics) metricsService.getSnapshot(MetricKeys.provider(serviceName));
        } catch (Exception e) {
            logger.warn("Failed to get service health metrics snapshot for service: {}", serviceName, e);
            // Return a default/empty health metrics if service is unavailable
            return new DefaultHealthMetrics();
        }
    }

    /**
     * Get overall system health status
     * 
     * @return system health status
     */
    @Override
    public SystemHealthStatus getSystemHealthStatus() {
        Map<ModelProviderType, HealthMetrics> providerMetrics = new ConcurrentHashMap<>();
        Map<String, HealthMetrics> serviceMetrics = new ConcurrentHashMap<>();

        for (ModelProviderType provider : ModelProviderType.values()) {
            if (providerHealthStates.containsKey(provider)) {
                providerMetrics.put(provider, getProviderHealthMetrics(provider));
            }
        }

        for (String serviceName : serviceHealthStates.keySet()) {
            serviceMetrics.put(serviceName, getServiceHealthMetrics(serviceName));
        }

        return new SystemHealthStatus(providerMetrics, serviceMetrics);
    }

    /**
     * Force recovery of a provider
     * 
     * @param provider the provider to recover
     */
    public void forceProviderRecovery(ModelProviderType provider) {
        ProviderHealthState state = providerHealthStates.get(provider);
        if (state != null) {
            state.forceRecovery();
            logger.info("Forced recovery for provider {}", provider);
        }
    }

    /**
     * Force recovery of a service
     * 
     * @param serviceName the service to recover
     */
    public void forceServiceRecovery(String serviceName) {
        ServiceHealthState state = serviceHealthStates.get(serviceName);
        if (state != null) {
            state.forceRecovery();
            logger.info("Forced recovery for service {}", serviceName);
        }
    }

    /**
     * Reset health monitoring for a provider
     * 
     * @param provider the provider to reset
     */
    public void resetProviderHealth(ModelProviderType provider) {
        providerHealthStates.remove(provider);
        logger.info("Reset health monitoring for provider {}", provider);
    }

    /**
     * Reset health monitoring for a service
     * 
     * @param serviceName the service to reset
     */
    public void resetServiceHealth(String serviceName) {
        serviceHealthStates.remove(serviceName);
        logger.info("Reset health monitoring for service {}", serviceName);
    }

    // Configuration methods - now using MetricsService for tracking
    public void setFailureThreshold(int threshold) {
        this.failureThreshold = threshold;
        if (metricsService != null) {
            try {
                metricsService.recordOperation("system-health-monitor", "configuration", true, Duration.ofMillis(1));
            } catch (Exception e) {
                logger.warn("Failed to record configuration change for failure threshold", e);
            }
        }
    }

    public void setRecoveryTimeout(Duration timeout) {
        this.recoveryTimeout = timeout;
        if (metricsService != null) {
            try {
                metricsService.recordOperation("system-health-monitor", "configuration", true, Duration.ofMillis(1));
            } catch (Exception e) {
                logger.warn("Failed to record configuration change for recovery timeout", e);
            }
        }
    }

    public void setHealthCheckInterval(Duration interval) {
        this.healthCheckInterval = interval;
        if (metricsService != null) {
            try {
                metricsService.recordOperation("system-health-monitor", "configuration", true, Duration.ofMillis(1));
            } catch (Exception e) {
                logger.warn("Failed to record configuration change for health check interval", e);
            }
        }
    }

    public void setMaxResponseTime(long maxResponseTime) {
        this.maxResponseTime = maxResponseTime;
        if (metricsService != null) {
            try {
                metricsService.recordOperation("system-health-monitor", "configuration", true, Duration.ofMillis(1));
            } catch (Exception e) {
                logger.warn("Failed to record configuration change for max response time", e);
            }
        }
    }

    public void setMinSuccessRate(double minSuccessRate) {
        this.minSuccessRate = minSuccessRate;
        if (metricsService != null) {
            try {
                metricsService.recordOperation("system-health-monitor", "configuration", true, Duration.ofMillis(1));
            } catch (Exception e) {
                logger.warn("Failed to record configuration change for min success rate", e);
            }
        }
    }

    public void setMonitoringEnabled(boolean enabled) {
        this.monitoringEnabled = enabled;
        if (metricsService != null) {
            try {
                metricsService.recordOperation("system-health-monitor", "configuration", true, Duration.ofMillis(1));
            } catch (Exception e) {
                logger.warn("Failed to record configuration change for monitoring enabled", e);
            }
        }
    }

    public void setAutoRecoveryEnabled(boolean enabled) {
        this.autoRecoveryEnabled = enabled;
        if (metricsService != null) {
            try {
                metricsService.recordOperation("system-health-monitor", "configuration", true, Duration.ofMillis(1));
            } catch (Exception e) {
                logger.warn("Failed to record configuration change for auto recovery enabled", e);
            }
        }
    }

    // Specification Performance Monitoring Methods

    /**
     * Record specification execution metrics
     * 
     * @param specificationId the specification ID
     * @param responseTime the response time in milliseconds
     * @param success true if execution was successful
     */
    public void recordSpecificationExecution(String specificationId, long responseTime, boolean success) {
        // Get or create metrics for the specification
        SpecificationPerformanceMetrics existingMetrics = specificationMetrics.get(specificationId);
        if (existingMetrics == null) {
            existingMetrics = new SpecificationPerformanceMetrics(specificationId, 0, 0, 0, 0, 0.0, 0.0, 0,
                    Instant.now());
        }

        long totalRequests = existingMetrics.totalRequests() + 1;
        long successfulRequests = existingMetrics.successfulRequests() + (success ? 1 : 0);
        long failedRequests = existingMetrics.failedRequests() + (success ? 0 : 1);
        long totalResponseTime = existingMetrics.totalResponseTime() + responseTime;
        double averageResponseTime = (double) totalResponseTime / totalRequests;
        double successRate = (double) successfulRequests / totalRequests;

        SpecificationPerformanceMetrics updatedMetrics = new SpecificationPerformanceMetrics(specificationId,
                totalRequests, successfulRequests, failedRequests, totalResponseTime, averageResponseTime, successRate,
                (int) calculateThroughput(specificationId), Instant.now());

        specificationMetrics.put(specificationId, updatedMetrics);
    }

    /**
     * Get performance metrics for a specification
     * 
     * @param specificationId the specification ID
     * @return performance metrics
     */
    @Override
    public SpecificationPerformanceMetrics getSpecificationMetrics(String specificationId) {
        SpecificationPerformanceMetrics m = specificationMetrics.getOrDefault(specificationId,
                new SpecificationPerformanceMetrics(specificationId, 0, 0, 0, 0, 0.0, 0.0, 0, Instant.now()));
        return new SpecificationPerformanceMetrics(m.specificationId(), m.totalRequests(), m.successfulRequests(),
                m.failedRequests(), m.totalResponseTime(), m.averageResponseTime(), m.successRate(),
                (int) calculateThroughput(specificationId), m.lastUpdated());
    }

    /**
     * Get all specification performance metrics
     * 
     * @return map of specification ID to performance metrics as Object
     */
    @Override
    public Map<String, Object> getAllSpecificationMetrics() {
        Map<String, Object> result = new ConcurrentHashMap<>();
        for (Map.Entry<String, SpecificationPerformanceMetrics> entry : specificationMetrics.entrySet()) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    /**
     * Get performance alerts for a specification
     * 
     * @param specificationId the specification ID
     * @return list of performance alerts
     */
    @Override
    public List<PerformanceAlert> getSpecificationAlerts(String specificationId) {
        return performanceAlerts.values().stream().filter(alert -> alert.specificationId().equals(specificationId))
                .map(a -> new PerformanceAlert(a.alertId(), a.specificationId(), a.type(), a.message(), a.severity(),
                        a.timestamp()))
                .collect(Collectors.toList());
    }

    /**
     * Get performance optimizations for a specification
     * 
     * @param specificationId the specification ID
     * @return list of performance optimizations
     */
    @Override
    public List<PerformanceOptimization> getSpecificationOptimizations(String specificationId) {
        return performanceOptimizations.values().stream().filter(opt -> opt.specificationId().equals(specificationId))
                .map(o -> new PerformanceOptimization(o.optimizationId(), o.specificationId(), o.type(),
                        o.description(), o.impact(), o.timestamp()))
                .collect(Collectors.toList());
    }

    /**
     * Get overall specification performance report
     * 
     * @return performance report
     */
    public SpecificationPerformanceReport getSpecificationPerformanceReport() {
        List<SpecificationPerformanceMetrics> allMetrics = new ArrayList<>(specificationMetrics.values());
        List<PerformanceAlert> allAlerts = new ArrayList<>(performanceAlerts.values());
        List<PerformanceOptimization> allOptimizations = new ArrayList<>(performanceOptimizations.values());

        double overallSuccessRate = allMetrics.stream().mapToDouble(SpecificationPerformanceMetrics::successRate)
                .average().orElse(0.0);

        double overallAverageResponseTime = allMetrics.stream()
                .mapToDouble(SpecificationPerformanceMetrics::averageResponseTime).average().orElse(0.0);

        return new SpecificationPerformanceReport(allMetrics, allAlerts, allOptimizations, overallSuccessRate,
                overallAverageResponseTime);
    }

    // Helper methods for performance monitoring
    private int calculateThroughput(String specificationId) {
        // Simple throughput calculation based on recent requests
        // In a real implementation, this would use a sliding window
        SpecificationPerformanceMetrics metrics = specificationMetrics.get(specificationId);
        if (metrics == null) {
            return 0;
        }

        // Calculate requests per second based on last minute
        long requestsInLastMinute = metrics.totalRequests() % 60; // Simplified calculation
        return (int) requestsInLastMinute;
    }

    private void checkPerformanceAlerts(String specificationId, SpecificationPerformanceMetrics metrics) {
        // Check response time threshold
        if (metrics.averageResponseTime() > maxSpecificationResponseTime) {
            createPerformanceAlert(specificationId, "HIGH_RESPONSE_TIME",
                    "Average response time exceeds threshold: " + metrics.averageResponseTime() + "ms", "WARNING");
        }

        // Check success rate threshold
        if (metrics.successRate() < minSpecificationSuccessRate) {
            createPerformanceAlert(specificationId, "LOW_SUCCESS_RATE",
                    "Success rate below threshold: " + (metrics.successRate() * 100) + "%", "ERROR");
        }

        // Check throughput threshold
        if (metrics.currentThroughput() > maxSpecificationThroughput) {
            createPerformanceAlert(specificationId, "HIGH_THROUGHPUT",
                    "Throughput exceeds threshold: " + metrics.currentThroughput() + " req/sec", "INFO");
        }
    }

    private void generateOptimizationSuggestions(String specificationId, SpecificationPerformanceMetrics metrics) {
        // Generate optimization suggestions based on performance patterns
        if (metrics.averageResponseTime() > 2000) {
            createPerformanceOptimization(specificationId, "CACHING",
                    "Consider implementing caching for frequently accessed data",
                    "Could reduce response time by 50-80%");
        }

        if (metrics.successRate() < 0.95) {
            createPerformanceOptimization(specificationId, "ERROR_HANDLING",
                    "Improve error handling and retry mechanisms", "Could improve success rate by 5-10%");
        }

        if (metrics.currentThroughput() > 50) {
            createPerformanceOptimization(specificationId, "LOAD_BALANCING",
                    "Consider load balancing for high-throughput scenarios", "Could improve throughput by 20-40%");
        }
    }

    private void createPerformanceAlert(String specificationId, String type, String message, String severity) {
        String alertId = "alert_" + specificationId + "_" + System.currentTimeMillis();
        PerformanceAlert alert = new PerformanceAlert(alertId, specificationId, type, message, severity, Instant.now());
        performanceAlerts.put(alertId, alert);
        logger.warn("Performance alert for specification {}: {} - {}", specificationId, type, message);
    }

    private void createPerformanceOptimization(String specificationId, String type, String description, String impact) {
        String optimizationId = "opt_" + specificationId + "_" + System.currentTimeMillis();
        PerformanceOptimization optimization = new PerformanceOptimization(optimizationId, specificationId, type,
                description, impact, Instant.now());
        performanceOptimizations.put(optimizationId, optimization);
        logger.info("Performance optimization suggestion for specification {}: {} - {}", specificationId, type,
                description);
    }

    // Performance report data class
    public record SpecificationPerformanceReport(List<SpecificationPerformanceMetrics> metrics,
            List<PerformanceAlert> alerts, List<PerformanceOptimization> optimizations, double overallSuccessRate,
            double overallAverageResponseTime) {
    }

    // Helper methods
    private ProviderHealthState getOrCreateProviderState(ModelProviderType provider) {
        return providerHealthStates.computeIfAbsent(provider, p -> new ProviderHealthState(p, metricsService));
    }

    private ServiceHealthState getOrCreateServiceState(String serviceName) {
        ServiceHealthState state = serviceHealthStates.computeIfAbsent(serviceName,
                name -> new ServiceHealthState(name, metricsService));
        if (state == null) {
            state = new ServiceHealthState(serviceName, metricsService);
            serviceHealthStates.put(serviceName, state);
        }
        return state;
    }

    // Implement comprehensive health check logic
    private boolean performComprehensiveProviderHealthCheck(ModelProviderType provider) {
        try {
            logger.debug("Performing comprehensive health check for provider: {}", provider);

            // Get provider state
            ProviderHealthState state = getOrCreateProviderState(provider);

            // 1. Check if provider has recent activity
            Instant lastHealthCheck = state.getLastHealthCheck();
            if (lastHealthCheck != null) {
                Duration timeSinceLastCheck = Duration.between(lastHealthCheck, Instant.now());
                if (timeSinceLastCheck.toMinutes() > 10) {
                    logger.warn("Provider {} has not been checked recently", provider);
                    return false;
                }
            }

            // 2. Check success rate
            double successRate = state.getSuccessRate();
            double minSuccessRate = this.minSuccessRate;
            if (successRate < minSuccessRate) {
                logger.warn("Provider {} success rate {} is below threshold {}", provider, successRate, minSuccessRate);
                return false;
            }

            // 3. Check response time
            double avgResponseTime = state.getAverageResponseTime();
            long maxResponseTime = this.maxResponseTime;
            if (avgResponseTime > maxResponseTime) {
                logger.warn("Provider {} average response time {}ms exceeds threshold {}ms", provider, avgResponseTime,
                        maxResponseTime);
                return false;
            }

            // 4. Check circuit breaker state
            CircuitBreakerState circuitBreakerState = state.getCircuitBreakerState();
            if (circuitBreakerState == CircuitBreakerState.OPEN) {
                logger.warn("Provider {} circuit breaker is OPEN", provider);
                return false;
            }

            // 5. Check consecutive failures
            long consecutiveFailures = state.getConsecutiveFailures();
            int failureThreshold = this.failureThreshold;
            if (consecutiveFailures >= failureThreshold) {
                logger.warn("Provider {} has {} consecutive failures, exceeding threshold {}", provider,
                        consecutiveFailures, failureThreshold);
                return false;
            }

            // 6. Check provider-specific health indicators
            boolean providerSpecificHealth = checkProviderSpecificHealth(provider);
            if (!providerSpecificHealth) {
                logger.warn("Provider {} failed provider-specific health checks", provider);
                return false;
            }

            // 7. Check resource availability
            boolean resourcesAvailable = checkProviderResources(provider);
            if (!resourcesAvailable) {
                logger.warn("Provider {} has insufficient resources", provider);
                return false;
            }

            logger.debug("Provider {} comprehensive health check passed", provider);
            return true;

        } catch (Exception e) {
            logger.error("Error during comprehensive provider health check for {}: {}", provider, e.getMessage(), e);
            return false;
        }
    }

    // Implement actual health check logic
    private boolean performProviderHealthCheck(ModelProviderType provider) {
        // Implement actual provider health check
        try {
            logger.debug("Performing health check for provider: {}", provider);

            // Get provider state
            ProviderHealthState state = getOrCreateProviderState(provider);

            // Check if provider has recent activity
            Instant lastHealthCheck = state.getLastHealthCheck();
            if (lastHealthCheck != null) {
                Duration timeSinceLastCheck = Duration.between(lastHealthCheck, Instant.now());
                if (timeSinceLastCheck.toMinutes() > 10) {
                    logger.warn("Provider {} has not been checked recently", provider);
                    return false;
                }
            }

            // Check success rate
            double successRate = state.getSuccessRate();
            double minSuccessRate = this.minSuccessRate;
            if (successRate < minSuccessRate) {
                logger.warn("Provider {} success rate {} is below threshold {}", provider, successRate, minSuccessRate);
                return false;
            }

            // Check response time
            double avgResponseTime = state.getAverageResponseTime();
            long maxResponseTime = this.maxResponseTime;
            if (avgResponseTime > maxResponseTime) {
                logger.warn("Provider {} average response time {}ms exceeds threshold {}ms", provider, avgResponseTime,
                        maxResponseTime);
                return false;
            }

            // Check circuit breaker state
            CircuitBreakerState circuitBreakerState = state.getCircuitBreakerState();
            if (circuitBreakerState == CircuitBreakerState.OPEN) {
                logger.warn("Provider {} circuit breaker is OPEN", provider);
                return false;
            }

            // Check consecutive failures
            long consecutiveFailures = state.getConsecutiveFailures();
            int failureThreshold = this.failureThreshold;
            if (consecutiveFailures >= failureThreshold) {
                logger.warn("Provider {} has {} consecutive failures, exceeding threshold {}", provider,
                        consecutiveFailures, failureThreshold);
                return false;
            }

            logger.debug("Provider {} health check passed", provider);
            return true;

        } catch (Exception e) {
            logger.error("Error during provider health check for {}: {}", provider, e.getMessage(), e);
            return false;
        }
    }

    private long measureHealthCheckResponseTime(ModelProviderType provider) {
        // Implement actual response time measurement
        long startTime = System.currentTimeMillis();

        try {
            // Simulate health check request to provider
            // In a real implementation, this would make an actual request to the provider
            // Execute health check and compute response time
            performProviderHealthCheck(provider);
            long responseTime = System.currentTimeMillis() - startTime;
            logger.debug("Provider {} health check response time: {}ms", provider, responseTime);

            return responseTime;

        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            logger.error("Provider {} health check failed after {}ms: {}", provider, responseTime, e.getMessage());
            return responseTime;
        }
    }

    private boolean performServiceHealthCheckLogic(String serviceName) {
        // Implement actual service health check logic
        try {
            logger.debug("Performing health check for service: {}", serviceName);

            // Get service state
            ServiceHealthState state = getOrCreateServiceState(serviceName);

            // Check if service has recent activity
            Instant lastHealthCheck = state.getLastHealthCheck();
            if (lastHealthCheck != null) {
                Duration timeSinceLastCheck = Duration.between(lastHealthCheck, Instant.now());
                if (timeSinceLastCheck.toMinutes() > 10) {
                    logger.warn("Service {} has not been checked recently", serviceName);
                    return false;
                }
            }

            // Check success rate
            double successRate = state.getSuccessRate();
            double minSuccessRate = this.minSuccessRate;
            if (successRate < minSuccessRate) {
                logger.warn("Service {} success rate {} is below threshold {}", serviceName, successRate,
                        minSuccessRate);
                return false;
            }

            // Check response time
            double avgResponseTime = state.getAverageResponseTime();
            long maxResponseTime = this.maxResponseTime;
            if (avgResponseTime > maxResponseTime) {
                logger.warn("Service {} average response time {}ms exceeds threshold {}ms", serviceName,
                        avgResponseTime, maxResponseTime);
                return false;
            }

            // Check total requests (ensure service is being used)
            // Note: For now we assume services with no recorded activity are still healthy
            // In a real implementation, this would check if the service has processed any requests
            logger.debug("Service {} health check passed basic validation", serviceName);

            logger.debug("Service {} health check passed", serviceName);
            return true;

        } catch (Exception e) {
            logger.error("Error during service health check for {}: {}", serviceName, e.getMessage(), e);
            return false;
        }
    }

    private long measureServiceHealthCheckResponseTime(String serviceName) {
        // Implement actual service response time measurement
        long startTime = System.currentTimeMillis();

        try {
            // Simulate health check request to service
            // In a real implementation, this would make an actual request to the service
            // Execute health check and compute response time
            performServiceHealthCheckLogic(serviceName);
            long responseTime = System.currentTimeMillis() - startTime;
            logger.debug("Service {} health check response time: {}ms", serviceName, responseTime);

            return responseTime;

        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            logger.error("Service {} health check failed after {}ms: {}", serviceName, responseTime, e.getMessage());
            return responseTime;
        }
    }

    // Check provider-specific health indicators
    private boolean checkProviderSpecificHealth(ModelProviderType provider) {
        try {
            switch (provider) {
                case OPENAI:
                    return checkOpenAIHealth();
                case ANTHROPIC:
                    return checkAnthropicHealth();
                case GOOGLE:
                    return checkGoogleHealth();
                case AZURE:
                    return checkAzureOpenAIHealth();
                case OLLAMA:
                    return checkOllamaHealth();
                case LOCALAI:
                    return checkLocalAIHealth();
                case VLLM:
                    return checkVLLMHealth();
                case LMSTUDIO:
                    return checkLMStudioHealth();
                default:
                    logger.warn("Unknown provider type: {}", provider);
                    return false;
            }
        } catch (Exception e) {
            logger.error("Error checking provider-specific health for {}: {}", provider, e.getMessage());
            return false;
        }
    }

    // Check provider resource availability
    private boolean checkProviderResources(ModelProviderType provider) {
        try {
            // Check system resources
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = maxMemory - freeMemory;
            double memoryUsage = (double) usedMemory / maxMemory;

            // Check if memory usage is acceptable (less than 90%)
            if (memoryUsage > 0.9) {
                logger.warn("High memory usage detected: {}%", String.format("%.1f", memoryUsage * 100));
                return false;
            }

            // Check available processors
            int availableProcessors = runtime.availableProcessors();
            if (availableProcessors < 2) {
                logger.warn("Insufficient processors available: {}", availableProcessors);
                return false;
            }

            logger.debug("Resource check passed for provider {}: memory={}%, processors={}", provider,
                    String.format("%.1f", memoryUsage * 100), availableProcessors);
            return true;

        } catch (Exception e) {
            logger.error("Error checking provider resources for {}: {}", provider, e.getMessage());
            return false;
        }
    }

    // Provider-specific health check methods
    private boolean checkOpenAIHealth() {
        // Check OpenAI-specific health indicators
        // In a real implementation, this would check API quotas, rate limits, etc.
        return true; // Placeholder - implement actual OpenAI health checks
    }

    private boolean checkAnthropicHealth() {
        // Check Anthropic-specific health indicators
        return true; // Placeholder - implement actual Anthropic health checks
    }

    private boolean checkGoogleHealth() {
        // Check Google-specific health indicators
        return true; // Placeholder - implement actual Google health checks
    }

    private boolean checkAzureOpenAIHealth() {
        // Check Azure OpenAI-specific health indicators
        return true; // Placeholder - implement actual Azure OpenAI health checks
    }

    private boolean checkOllamaHealth() {
        // Check Ollama-specific health indicators
        return true; // Placeholder - implement actual Ollama health checks
    }

    private boolean checkLocalAIHealth() {
        // Check LocalAI-specific health indicators
        return true; // Placeholder - implement actual LocalAI health checks
    }

    private boolean checkVLLMHealth() {
        // Check vLLM-specific health indicators
        return true; // Placeholder - implement actual vLLM health checks
    }

    private boolean checkLMStudioHealth() {
        // Check LM Studio-specific health indicators
        return true; // Placeholder - implement actual LM Studio health checks
    }

    // HealthCheckResult extracted to org.openhab.core.ai.tool.monitoring.HealthCheckResult

    // SystemHealthStatus extracted to org.openhab.core.ai.tool.monitoring.SystemHealthStatus

    // ProviderHealthMetrics extracted to org.openhab.core.ai.tool.monitoring.ProviderHealthMetrics

    // ServiceHealthMetrics extracted to org.openhab.core.ai.tool.monitoring.ServiceHealthMetrics

    // Record classes for specification performance monitoring
    // Records and enums moved to top-level classes:
    // - SpecificationPerformanceMetrics (record) is local to this class and still referenced directly above
    // - PerformanceAlert -> org.openhab.core.ai.tool.monitoring.PerformanceAlert
    // - PerformanceOptimization -> org.openhab.core.ai.tool.monitoring.PerformanceOptimization
    // - CircuitBreakerState -> org.openhab.core.ai.tool.monitoring.CircuitBreakerState

    // Extracted: org.openhab.core.ai.tool.monitoring.ProviderHealthState

    // Extracted: org.openhab.core.ai.tool.monitoring.ServiceHealthState

    // Implementation of missing interface methods
    @Override
    public boolean isMonitoringEnabled() {
        return monitoringEnabled;
    }

    @Override
    public boolean isAutoRecoveryEnabled() {
        return autoRecoveryEnabled;
    }

    @Override
    public int getFailureThreshold() {
        return failureThreshold;
    }

    @Override
    public Duration getRecoveryTimeout() {
        return recoveryTimeout;
    }

    @Override
    public Duration getHealthCheckInterval() {
        return healthCheckInterval;
    }

    @Override
    public long getMaxResponseTime() {
        return maxResponseTime;
    }

    @Override
    public double getMinSuccessRate() {
        return minSuccessRate;
    }

    /**
     * Simple default implementation of HealthMetrics for fallback purposes.
     */
    private static class DefaultHealthMetrics implements HealthMetrics {
        @Override
        public HealthStatus healthStatus() {
            return HealthStatus.UNKNOWN;
        }

        @Override
        public @Nullable String statusMessage() {
            return "Health metrics unavailable";
        }

        @Override
        public @Nullable Map<String, Object> healthIndicators() {
            return Map.of("status", "unavailable");
        }
    }
}
