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
import org.openhab.core.ai.common.monitoring.api.MetricKeys;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.service.snapshot.UnifiedMetricsSnapshot;
import org.openhab.core.ai.model.api.ModelProviderType;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tool Health Monitor - Provides comprehensive health monitoring and circuit breaker functionality
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
@Component(service = ToolHealthMonitor.class)
@NonNullByDefault
public class ToolHealthMonitor {

    private static final Logger logger = LoggerFactory.getLogger(ToolHealthMonitor.class);

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

    // Performance monitoring for specifications
    private final ConcurrentHashMap<String, SpecificationPerformanceMetrics> specificationMetrics = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, PerformanceAlert> performanceAlerts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, PerformanceOptimization> performanceOptimizations = new ConcurrentHashMap<>();

    // Performance thresholds for specifications - using simple variables instead of AtomicReference
    private volatile long maxSpecificationResponseTime = 3000L; // 3 seconds
    private volatile double minSpecificationSuccessRate = 0.9; // 90%
    private volatile int maxSpecificationThroughput = 100; // 100 req/sec

    // NEW: Centralized MetricsService for monitoring operations
    @Reference
    private @Nullable MetricsService metricsService;

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

        // NEW: Record monitoring operation using centralized MetricsService
        if (metricsService != null) {
            try {
                metricsService.recordOperation("monitoring", "provider-" + provider.name()).withSuccess(true)
                        .withDuration(Duration.ofMillis(responseTime).toNanos()).withData("metricsCollected", 1)
                        .withData("alertsGenerated", 0).withData("metricType", "provider-health")
                        .withData("alertSeverity", "none").record();
            } catch (Exception e) {
                logger.warn("Failed to record monitoring operation for provider: {}", provider, e);
            }
        }

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

        // NEW: Record monitoring operation using centralized MetricsService
        if (metricsService != null) {
            try {
                metricsService.recordOperation("monitoring", "provider-" + provider.name()).withSuccess(false)
                        .withDuration(Duration.ofMillis(0).toNanos()).withData("metricsCollected", 0)
                        .withData("alertsGenerated", 1).withData("metricType", "provider-health")
                        .withData("alertSeverity", "error").record();
            } catch (Exception e) {
                logger.warn("Failed to record monitoring operation for provider: {}", provider, e);
            }
        }

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
    public CompletableFuture<HealthCheckResult> performProviderHealthCheck(ModelProviderType provider) {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            boolean isHealthy = false;
            Exception error = null;

            try {
                // Perform comprehensive health check
                isHealthy = performProviderHealthCheckLogic(provider);

                // NEW: Record monitoring operation using centralized MetricsService
                if (metricsService != null) {
                    try {
                        long responseTime = System.currentTimeMillis() - startTime;
                        metricsService.recordOperation("monitoring", "provider-health-check-" + provider.name())
                                .withSuccess(isHealthy).withDuration(Duration.ofMillis(responseTime).toNanos())
                                .withData("metricsCollected", isHealthy ? 1 : 0)
                                .withData("alertsGenerated", isHealthy ? 0 : 1).withData("metricType", "health-check")
                                .withData("alertSeverity", isHealthy ? "none" : "warning").record();
                    } catch (Exception e) {
                        logger.warn("Failed to record monitoring operation for provider health check: {}", provider, e);
                    }
                }

                return new HealthCheckResult(provider.name(), isHealthy, System.currentTimeMillis() - startTime, null);
            } catch (Exception e) {
                error = e;
                logger.error("Provider health check failed for {}", provider, e);

                // NEW: Record monitoring operation for failed health check
                if (metricsService != null) {
                    try {
                        metricsService.recordOperation("monitoring", "provider-health-check-" + provider.name())
                                .withSuccess(false)
                                .withDuration(Duration.ofMillis(System.currentTimeMillis() - startTime).toNanos())
                                .withData("metricsCollected", 0).withData("alertsGenerated", 1)
                                .withData("metricType", "health-check").withData("alertSeverity", "error").record();
                    } catch (Exception ex) {
                        logger.warn("Failed to record monitoring operation for failed health check: {}", provider, ex);
                    }
                }

                return new HealthCheckResult(provider.name(), false, System.currentTimeMillis() - startTime, e);
            }
        });
    }

    /**
     * Perform a health check on a service
     * 
     * @param serviceName the service to check
     * @return CompletableFuture with health check result
     */
    public CompletableFuture<HealthCheckResult> performServiceHealthCheck(String serviceName) {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            boolean isHealthy = false;
            Exception error = null;

            try {
                // TODO: Implement actual service health check logic
                isHealthy = performServiceHealthCheckLogic(serviceName);
                long responseTime = System.currentTimeMillis() - startTime;

                HealthCheckResult result = new HealthCheckResult(serviceName, isHealthy, responseTime, null);

                // Update health state
                ServiceHealthState state = getOrCreateServiceState(serviceName);
                state.updateFromHealthCheck(isHealthy, responseTime);

                // NEW: Record monitoring operation using centralized MetricsService
                if (metricsService != null) {
                    try {
                        metricsService.recordOperation("monitoring", "service-health-check-" + serviceName)
                                .withSuccess(isHealthy).withDuration(Duration.ofMillis(responseTime).toNanos())
                                .withData("metricsCollected", isHealthy ? 1 : 0)
                                .withData("alertsGenerated", isHealthy ? 0 : 1).withData("metricType", "health-check")
                                .withData("alertSeverity", isHealthy ? "none" : "warning").record();
                    } catch (Exception e) {
                        logger.warn("Failed to record monitoring operation for service health check: {}", serviceName,
                                e);
                    }
                }

                return result;
            } catch (Exception e) {
                error = e;
                logger.error("Service health check failed for {}", serviceName, e);

                // NEW: Record monitoring operation for failed health check
                if (metricsService != null) {
                    try {
                        metricsService.recordOperation("monitoring", "service-health-check-" + serviceName)
                                .withSuccess(false)
                                .withDuration(Duration.ofMillis(System.currentTimeMillis() - startTime).toNanos())
                                .withData("metricsCollected", 0).withData("alertsGenerated", 1)
                                .withData("metricType", "health-check").withData("alertSeverity", "error").record();
                    } catch (Exception ex) {
                        logger.warn("Failed to record monitoring operation for failed service health check: {}",
                                serviceName, ex);
                    }
                }

                return new HealthCheckResult(serviceName, false, System.currentTimeMillis() - startTime, e);
            }
        });
    }

    /**
     * Get health metrics for a provider
     * 
     * @param provider the provider
     * @return provider health metrics
     */
    public HealthMetrics getProviderHealthMetrics(ModelProviderType provider) {
        ProviderHealthState state = getOrCreateProviderState(provider);

        // Record health check operation
        if (metricsService != null) {
            try {
                metricsService.recordOperation("tool", "provider-health-check").withSuccess(state.isHealthy())
                        .withDuration(50)
                        .withData(Map.of("provider", provider.name(), "circuitBreakerState",
                                state.getCircuitBreakerState().name(), "isOpen",
                                state.getCircuitBreakerState() == CircuitBreakerState.OPEN, "failureCount",
                                state.getFailedRequestsCount(), "totalRequests", state.getTotalRequests()))
                        .record();

                // Return health metrics from service
                return metricsService.getSnapshot(MetricKeys.providerHealth(provider.name()),
                        UnifiedMetricsSnapshot.class);
            } catch (Exception e) {
                logger.warn("Failed to record or retrieve provider health check metrics for {}: {}", provider.name(),
                        e.getMessage());
                // Fall through to return null as fallback
            }
        }
        return null;
    }

    /**
     * Get health metrics for a service
     * 
     * @param serviceName the service name
     * @return service health metrics
     */
    public HealthMetrics getServiceHealthMetrics(String serviceName) {
        ServiceHealthState state = getOrCreateServiceState(serviceName);

        // Record health check operation
        if (metricsService != null) {
            try {
                metricsService.recordOperation("service", "health-check").withSuccess(state.isHealthy())
                        .withDuration(50).withData(Map.of("serviceName", serviceName)).record();

                // Return health metrics from service
                return (HealthMetrics) metricsService.getSnapshot(MetricKeys.provider(serviceName));
            } catch (Exception e) {
                logger.warn("Failed to record or retrieve service health check metrics for {}: {}", serviceName,
                        e.getMessage());
                // Fall through to return null as fallback
            }
        }
        return null;
    }

    /**
     * Get overall system health status
     * 
     * @return system health status
     */
    public SystemHealthStatus getSystemHealthStatus() {
        Map<ModelProviderType, HealthMetrics> providerMetrics = new ConcurrentHashMap<>();
        Map<String, HealthMetrics> serviceMetrics = new ConcurrentHashMap<>();

        // Collect provider metrics
        for (ModelProviderType provider : ModelProviderType.values()) {
            if (providerHealthStates.containsKey(provider)) {
                providerMetrics.put(provider, getProviderHealthMetrics(provider));
            }
        }

        // Collect service metrics
        for (String serviceName : serviceHealthStates.keySet()) {
            serviceMetrics.put(serviceName, getServiceHealthMetrics(serviceName));
        }

        return new SystemHealthStatus(providerMetrics, serviceMetrics);
    }

    /**
     * Get monitoring statistics for the health monitor
     * 
     * @param timeRange the time range for statistics
     * @return monitoring statistics
     */
    public MonitoringStatistics getMonitoringStatistics(Duration timeRange) {
        if (metricsService != null) {
            try {
                // Record monitoring operation using centralized MetricsService
                // Record the monitoring operation
                metricsService.recordOperation("tool", "health-monitor-get-statistics", true, Duration.ofMillis(50));

                // Use simplified MonitoringStatistics - StatisticsFactory already supports this
                // TODO: Collect proper snapshots from MetricsService for StatisticsFactory usage
                return MonitoringStatistics.empty(timeRange);
            } catch (Exception e) {
                logger.warn("Failed to get monitoring statistics from MetricsService", e);

                // Record failure operation (with additional try-catch to prevent recursion)
                try {
                    metricsService.recordOperation("tool", "health-monitor-get-statistics", false,
                            Duration.ofMillis(50));
                } catch (Exception recordError) {
                    logger.debug("Failed to record failure operation: {}", recordError.getMessage());
                    // Graceful degradation - ignore recording errors during error handling
                }
            }
        }

        // Fallback to empty statistics if MetricsService is not available
        return MonitoringStatistics.empty(timeRange);
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

            // NEW: Record monitoring operation for recovery
            if (metricsService != null) {
                try {
                    metricsService.recordOperation("monitoring", "provider-recovery-" + provider.name())
                            .withSuccess(true).withDuration(Duration.ofMillis(0).toNanos())
                            .withData("metricsCollected", 1).withData("alertsGenerated", 0)
                            .withData("metricType", "recovery").withData("alertSeverity", "none").record();
                } catch (Exception e) {
                    logger.warn("Failed to record monitoring operation for provider recovery: {}", provider, e);
                }
            }

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

            // NEW: Record monitoring operation for recovery
            if (metricsService != null) {
                try {
                    metricsService.recordOperation("monitoring", "service-recovery-" + serviceName).withSuccess(true)
                            .withDuration(Duration.ofMillis(0).toNanos()).withData("metricsCollected", 1)
                            .withData("alertsGenerated", 0).withData("metricType", "recovery")
                            .withData("alertSeverity", "none").record();
                } catch (Exception e) {
                    logger.warn("Failed to record monitoring operation for service recovery: {}", serviceName, e);
                }
            }

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

        // NEW: Record monitoring operation for reset
        if (metricsService != null) {
            try {
                metricsService.recordOperation("monitoring", "provider-reset-" + provider.name()).withSuccess(true)
                        .withDuration(Duration.ofMillis(0).toNanos()).withData("metricsCollected", 1)
                        .withData("alertsGenerated", 0).withData("metricType", "reset")
                        .withData("alertSeverity", "none").record();
            } catch (Exception e) {
                logger.warn("Failed to record monitoring operation for provider reset: {}", provider, e);
            }
        }

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

        // Record configuration change using centralized MetricsService
        if (metricsService != null) {
            try {
                metricsService.recordOperation("tool-health-monitor", "configuration").withSuccess(true)
                        .withDuration(0L).withData("configType", "failureThreshold").withData("threshold", threshold)
                        .record();
            } catch (Exception e) {
                logger.warn("Failed to record configuration change metric for failureThreshold: {}", e.getMessage());
            }
        }
    }

    public void setRecoveryTimeout(Duration timeout) {
        this.recoveryTimeout = timeout;

        // Record configuration change using centralized MetricsService
        if (metricsService != null) {
            try {
                metricsService.recordOperation("tool-health-monitor", "configuration").withSuccess(true)
                        .withDuration(0L).withData("configType", "recoveryTimeout")
                        .withData("timeoutMs", timeout.toMillis()).record();
            } catch (Exception e) {
                logger.warn("Failed to record configuration change metric for recoveryTimeout: {}", e.getMessage());
            }
        }
    }

    public void setHealthCheckInterval(Duration interval) {
        this.healthCheckInterval = interval;

        // Record configuration change using centralized MetricsService
        if (metricsService != null) {
            try {
                metricsService.recordOperation("tool-health-monitor", "configuration").withSuccess(true)
                        .withDuration(0L).withData("configType", "healthCheckInterval")
                        .withData("intervalMs", interval.toMillis()).record();
            } catch (Exception e) {
                logger.warn("Failed to record configuration change metric for healthCheckInterval: {}", e.getMessage());
            }
        }
    }

    public void setMaxResponseTime(long maxResponseTime) {
        this.maxResponseTime = maxResponseTime;

        // Record configuration change using centralized MetricsService
        if (metricsService != null) {
            try {
                metricsService.recordOperation("tool-health-monitor", "configuration").withSuccess(true)
                        .withDuration(0L).withData("configType", "maxResponseTime")
                        .withData("responseTimeMs", maxResponseTime).record();
            } catch (Exception e) {
                logger.warn("Failed to record configuration change metric for maxResponseTime: {}", e.getMessage());
            }
        }
    }

    public void setMinSuccessRate(double minSuccessRate) {
        this.minSuccessRate = minSuccessRate;

        // Record configuration change using centralized MetricsService
        if (metricsService != null) {
            try {
                metricsService.recordOperation("tool-health-monitor", "configuration").withSuccess(true)
                        .withDuration(0L).withData("configType", "minSuccessRate")
                        .withData("successRate", minSuccessRate).record();
            } catch (Exception e) {
                logger.warn("Failed to record configuration change metric for minSuccessRate: {}", e.getMessage());
            }
        }
    }

    public void setMonitoringEnabled(boolean enabled) {
        this.monitoringEnabled = enabled;

        // Record configuration change using centralized MetricsService
        if (metricsService != null) {
            try {
                metricsService.recordOperation("tool-health-monitor", "configuration").withSuccess(true)
                        .withDuration(0L).withData("configType", "monitoringEnabled").withData("enabled", enabled)
                        .record();
            } catch (Exception e) {
                logger.warn("Failed to record configuration change metric for monitoringEnabled: {}", e.getMessage());
            }
        }
    }

    public void setAutoRecoveryEnabled(boolean enabled) {
        this.autoRecoveryEnabled = enabled;

        // Record configuration change using centralized MetricsService
        if (metricsService != null) {
            try {
                metricsService.recordOperation("tool-health-monitor", "configuration").withSuccess(true)
                        .withDuration(0L).withData("configType", "autoRecoveryEnabled").withData("enabled", enabled)
                        .record();
            } catch (Exception e) {
                logger.warn("Failed to record configuration change metric for autoRecoveryEnabled: {}", e.getMessage());
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
        SpecificationPerformanceMetrics metrics = specificationMetrics.computeIfAbsent(specificationId,
                id -> new SpecificationPerformanceMetrics(id, 0, 0, 0, 0, 0.0, 0.0, 0, Instant.now()));

        long totalRequests = metrics.totalRequests() + 1;
        long successfulRequests = metrics.successfulRequests() + (success ? 1 : 0);
        long failedRequests = metrics.failedRequests() + (success ? 0 : 1);
        long totalResponseTime = metrics.totalResponseTime() + responseTime;
        double averageResponseTime = (double) totalResponseTime / totalRequests;
        double successRate = (double) successfulRequests / totalRequests;

        SpecificationPerformanceMetrics updatedMetrics = new SpecificationPerformanceMetrics(specificationId,
                totalRequests, successfulRequests, failedRequests, totalResponseTime, averageResponseTime, successRate,
                calculateThroughput(specificationId), Instant.now());

        specificationMetrics.put(specificationId, updatedMetrics);

        // Check for performance alerts
        checkPerformanceAlerts(specificationId, updatedMetrics);

        // Generate optimization suggestions
        generateOptimizationSuggestions(specificationId, updatedMetrics);
    }

    /**
     * Get performance metrics for a specification
     * 
     * @param specificationId the specification ID
     * @return performance metrics
     */
    public SpecificationPerformanceMetrics getSpecificationMetrics(String specificationId) {
        return specificationMetrics.getOrDefault(specificationId,
                new SpecificationPerformanceMetrics(specificationId, 0, 0, 0, 0, 0.0, 0.0, 0, Instant.now()));
    }

    /**
     * Get all specification performance metrics
     * 
     * @return map of specification ID to performance metrics
     */
    public Map<String, SpecificationPerformanceMetrics> getAllSpecificationMetrics() {
        return new ConcurrentHashMap<>(specificationMetrics);
    }

    /**
     * Get performance alerts for a specification
     * 
     * @param specificationId the specification ID
     * @return list of performance alerts
     */
    public List<PerformanceAlert> getSpecificationAlerts(String specificationId) {
        return performanceAlerts.values().stream().filter(alert -> alert.getSpecificationId().equals(specificationId))
                .collect(Collectors.toList());
    }

    /**
     * Get performance optimizations for a specification
     * 
     * @param specificationId the specification ID
     * @return list of performance optimizations
     */
    public List<PerformanceOptimization> getSpecificationOptimizations(String specificationId) {
        return performanceOptimizations.values().stream()
                .filter(opt -> opt.getSpecificationId().equals(specificationId)).collect(Collectors.toList());
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

    // Implement comprehensive tool health check logic
    private boolean performComprehensiveToolHealthCheck(ModelProviderType provider) {
        try {
            logger.debug("Performing comprehensive tool health check for provider: {}", provider);

            // Get provider state
            ProviderHealthState state = getOrCreateProviderState(provider);

            // 1. Check if provider has recent activity
            Instant lastHealthCheck = state.getLastHealthCheck();
            if (lastHealthCheck != null) {
                Duration timeSinceLastCheck = Duration.between(lastHealthCheck, Instant.now());
                if (timeSinceLastCheck.toMinutes() > 10) {
                    logger.warn("Tool provider {} has not been checked recently", provider);
                    return false;
                }
            }

            // 2. Check success rate
            double successRate = state.getSuccessRate();
            double minSuccessRate = this.minSuccessRate;
            if (successRate < minSuccessRate) {
                logger.warn("Tool provider {} success rate {} is below threshold {}", provider, successRate,
                        minSuccessRate);
                return false;
            }

            // 3. Check response time
            double avgResponseTime = state.getAverageResponseTime();
            long maxResponseTime = this.maxResponseTime;
            if (avgResponseTime > maxResponseTime) {
                logger.warn("Tool provider {} average response time {}ms exceeds threshold {}ms", provider,
                        avgResponseTime, maxResponseTime);
                return false;
            }

            // 4. Check circuit breaker state
            CircuitBreakerState circuitBreakerState = state.getCircuitBreakerState();
            if (circuitBreakerState == CircuitBreakerState.OPEN) {
                logger.warn("Tool provider {} circuit breaker is OPEN", provider);
                return false;
            }

            // 5. Check consecutive failures
            long consecutiveFailures = state.getConsecutiveFailures();
            int failureThreshold = this.failureThreshold;
            if (consecutiveFailures >= failureThreshold) {
                logger.warn("Tool provider {} has {} consecutive failures, exceeding threshold {}", provider,
                        consecutiveFailures, failureThreshold);
                return false;
            }

            // 6. Check tool-specific health indicators
            boolean toolSpecificHealth = checkToolSpecificHealth(provider);
            if (!toolSpecificHealth) {
                logger.warn("Tool provider {} failed tool-specific health checks", provider);
                return false;
            }

            // 7. Check tool availability
            boolean toolAvailable = checkToolAvailability(provider);
            if (!toolAvailable) {
                logger.warn("Tool provider {} has availability issues", provider);
                return false;
            }

            logger.debug("Tool provider {} comprehensive health check passed", provider);
            return true;

        } catch (Exception e) {
            logger.error("Error during comprehensive tool health check for {}: {}", provider, e.getMessage(), e);
            return false;
        }
    }

    // Placeholder health check implementations
    private boolean performProviderHealthCheckLogic(ModelProviderType provider) {
        // TODO: Implement actual provider health check
        // This could involve making a test request to the provider
        return true; // Placeholder
    }

    private long measureHealthCheckResponseTime(ModelProviderType provider) {
        // TODO: Implement actual response time measurement
        return 100; // Placeholder
    }

    private boolean performServiceHealthCheckLogic(String serviceName) {
        // TODO: Implement actual service health check
        return true; // Placeholder
    }

    private long measureServiceHealthCheckResponseTime(String serviceName) {
        // TODO: Implement actual service response time measurement
        return 100; // Placeholder
    }

    // Check tool-specific health indicators
    private boolean checkToolSpecificHealth(ModelProviderType provider) {
        try {
            switch (provider) {
                case OPENAI:
                    return checkOpenAIToolHealth();
                case ANTHROPIC:
                    return checkAnthropicToolHealth();
                case GOOGLE:
                    return checkGoogleToolHealth();
                case AZURE:
                    return checkAzureToolHealth();
                case OLLAMA:
                    return checkOllamaToolHealth();
                case LOCALAI:
                    return checkLocalAIToolHealth();
                case VLLM:
                    return checkVLLMToolHealth();
                case LMSTUDIO:
                    return checkLMStudioToolHealth();
                default:
                    logger.warn("Unknown tool provider type: {}", provider);
                    return false;
            }
        } catch (Exception e) {
            logger.error("Error checking tool-specific health for {}: {}", provider, e.getMessage());
            return false;
        }
    }

    // Check tool availability
    private boolean checkToolAvailability(ModelProviderType provider) {
        try {
            // Check if tool registry has tools for this provider
            boolean hasTools = checkToolRegistryAvailability(provider);
            if (!hasTools) {
                logger.warn("No tools available for provider {}", provider);
                return false;
            }

            // Check if tool execution service is available
            boolean executionServiceAvailable = checkToolExecutionServiceAvailability(provider);
            if (!executionServiceAvailable) {
                logger.warn("Tool execution service not available for provider {}", provider);
                return false;
            }

            logger.debug("Tool availability check passed for provider {}", provider);
            return true;

        } catch (Exception e) {
            logger.error("Error checking tool availability for {}: {}", provider, e.getMessage());
            return false;
        }
    }

    // Tool-specific health check methods
    private boolean checkOpenAIToolHealth() {
        // Check OpenAI tool-specific health indicators
        return true; // Placeholder - implement actual OpenAI tool health checks
    }

    private boolean checkAnthropicToolHealth() {
        // Check Anthropic tool-specific health indicators
        return true; // Placeholder - implement actual Anthropic tool health checks
    }

    private boolean checkGoogleToolHealth() {
        // Check Google tool-specific health indicators
        return true; // Placeholder - implement actual Google tool health checks
    }

    private boolean checkAzureToolHealth() {
        // Check Azure tool-specific health indicators
        return true; // Placeholder - implement actual Azure tool health checks
    }

    private boolean checkOllamaToolHealth() {
        // Check Ollama tool-specific health indicators
        return true; // Placeholder - implement actual Ollama tool health checks
    }

    private boolean checkLocalAIToolHealth() {
        // Check LocalAI tool-specific health indicators
        return true; // Placeholder - implement actual LocalAI tool health checks
    }

    private boolean checkVLLMToolHealth() {
        // Check vLLM tool-specific health indicators
        return true; // Placeholder - implement actual vLLM tool health checks
    }

    private boolean checkLMStudioToolHealth() {
        // Check LM Studio tool-specific health indicators
        return true; // Placeholder - implement actual LM Studio tool health checks
    }

    // Tool availability check methods
    private boolean checkToolRegistryAvailability(ModelProviderType provider) {
        // Check if tool registry has tools for this provider
        // In a real implementation, this would check the actual tool registry
        return true; // Placeholder - implement actual tool registry availability check
    }

    private boolean checkToolExecutionServiceAvailability(ModelProviderType provider) {
        // Check if tool execution service is available for this provider
        // In a real implementation, this would check the actual execution service
        return true; // Placeholder - implement actual tool execution service availability check
    }

    // Record class for specification performance monitoring (kept local to this monitor)
    public record SpecificationPerformanceMetrics(String specificationId, long totalRequests, long successfulRequests,
            long failedRequests, long totalResponseTime, double averageResponseTime, double successRate,
            int currentThroughput, Instant lastUpdated) {
    }

    // ProviderHealthState and ServiceHealthState are top-level classes in this package
}
