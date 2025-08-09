package org.openhab.core.ai.tool.monitoring;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.model.api.ModelProviderType;
import org.osgi.service.component.annotations.Component;
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

    // Circuit breaker configuration
    private final AtomicReference<Integer> failureThreshold = new AtomicReference<>(5);
    private final AtomicReference<Duration> recoveryTimeout = new AtomicReference<>(Duration.ofMinutes(5));
    private final AtomicReference<Duration> healthCheckInterval = new AtomicReference<>(Duration.ofSeconds(30));

    // Performance thresholds
    private final AtomicReference<Long> maxResponseTime = new AtomicReference<>(5000L); // 5 seconds
    private final AtomicReference<Double> minSuccessRate = new AtomicReference<>(0.8); // 80%

    // Monitoring state
    private final AtomicReference<Boolean> monitoringEnabled = new AtomicReference<>(true);
    private final AtomicReference<Boolean> autoRecoveryEnabled = new AtomicReference<>(true);

    // Performance monitoring for specifications
    private final ConcurrentHashMap<String, SpecificationPerformanceMetrics> specificationMetrics = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, PerformanceAlert> performanceAlerts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, PerformanceOptimization> performanceOptimizations = new ConcurrentHashMap<>();

    // Performance thresholds for specifications
    private final AtomicReference<Long> maxSpecificationResponseTime = new AtomicReference<>(3000L); // 3 seconds
    private final AtomicReference<Double> minSpecificationSuccessRate = new AtomicReference<>(0.9); // 90%
    private final AtomicReference<Integer> maxSpecificationThroughput = new AtomicReference<>(100); // 100 req/sec

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
            state = new ProviderHealthState(provider);
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
            state = new ServiceHealthState(serviceName);
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
            try {
                // TODO: Implement actual health check logic
                // This is a placeholder implementation
                boolean isHealthy = performProviderHealthCheck(provider);
                long responseTime = measureHealthCheckResponseTime(provider);

                HealthCheckResult result = new HealthCheckResult(provider, isHealthy, responseTime, null);

                // Update health state
                ProviderHealthState state = getOrCreateProviderState(provider);
                state.updateFromHealthCheck(result);

                return result;
            } catch (Exception e) {
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
    public CompletableFuture<HealthCheckResult> performServiceHealthCheck(String serviceName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // TODO: Implement actual service health check logic
                boolean isHealthy = performServiceHealthCheckLogic(serviceName);
                long responseTime = measureServiceHealthCheckResponseTime(serviceName);

                HealthCheckResult result = new HealthCheckResult(serviceName, isHealthy, responseTime, null);

                // Update health state
                ServiceHealthState state = getOrCreateServiceState(serviceName);
                state.updateFromHealthCheck(result);

                return result;
            } catch (Exception e) {
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
    public ProviderHealthMetrics getProviderHealthMetrics(ModelProviderType provider) {
        ProviderHealthState state = getOrCreateProviderState(provider);
        return state.getHealthMetrics();
    }

    /**
     * Get health metrics for a service
     * 
     * @param serviceName the service name
     * @return service health metrics
     */
    public ServiceHealthMetrics getServiceHealthMetrics(String serviceName) {
        ServiceHealthState state = getOrCreateServiceState(serviceName);
        return state.getHealthMetrics();
    }

    /**
     * Get overall system health status
     * 
     * @return system health status
     */
    public SystemHealthStatus getSystemHealthStatus() {
        Map<ModelProviderType, ProviderHealthMetrics> providerMetrics = new ConcurrentHashMap<>();
        Map<String, ServiceHealthMetrics> serviceMetrics = new ConcurrentHashMap<>();

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

    // Configuration methods
    public void setFailureThreshold(int threshold) {
        failureThreshold.set(threshold);
    }

    public void setRecoveryTimeout(Duration timeout) {
        recoveryTimeout.set(timeout);
    }

    public void setHealthCheckInterval(Duration interval) {
        healthCheckInterval.set(interval);
    }

    public void setMaxResponseTime(long maxResponseTime) {
        this.maxResponseTime.set(maxResponseTime);
    }

    public void setMinSuccessRate(double minSuccessRate) {
        this.minSuccessRate.set(minSuccessRate);
    }

    public void setMonitoringEnabled(boolean enabled) {
        monitoringEnabled.set(enabled);
    }

    public void setAutoRecoveryEnabled(boolean enabled) {
        autoRecoveryEnabled.set(enabled);
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
        return performanceAlerts.values().stream().filter(alert -> alert.specificationId().equals(specificationId))
                .collect(Collectors.toList());
    }

    /**
     * Get performance optimizations for a specification
     * 
     * @param specificationId the specification ID
     * @return list of performance optimizations
     */
    public List<PerformanceOptimization> getSpecificationOptimizations(String specificationId) {
        return performanceOptimizations.values().stream().filter(opt -> opt.specificationId().equals(specificationId))
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
        if (metrics.averageResponseTime() > maxSpecificationResponseTime.get()) {
            createPerformanceAlert(specificationId, "HIGH_RESPONSE_TIME",
                    "Average response time exceeds threshold: " + metrics.averageResponseTime() + "ms", "WARNING");
        }

        // Check success rate threshold
        if (metrics.successRate() < minSpecificationSuccessRate.get()) {
            createPerformanceAlert(specificationId, "LOW_SUCCESS_RATE",
                    "Success rate below threshold: " + (metrics.successRate() * 100) + "%", "ERROR");
        }

        // Check throughput threshold
        if (metrics.currentThroughput() > maxSpecificationThroughput.get()) {
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
        ProviderHealthState state = providerHealthStates.computeIfAbsent(provider, ProviderHealthState::new);
        if (state == null) {
            state = new ProviderHealthState(provider);
            providerHealthStates.put(provider, state);
        }
        return state;
    }

    private ServiceHealthState getOrCreateServiceState(String serviceName) {
        ServiceHealthState state = serviceHealthStates.computeIfAbsent(serviceName, ServiceHealthState::new);
        if (state == null) {
            state = new ServiceHealthState(serviceName);
            serviceHealthStates.put(serviceName, state);
        }
        return state;
    }

    // Placeholder health check implementations
    private boolean performProviderHealthCheck(ModelProviderType provider) {
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

    // Inner classes
    public static class HealthCheckResult {
        private final String target;
        private final boolean healthy;
        private final long responseTime;
        private final @Nullable Exception error;

        public HealthCheckResult(String target, boolean healthy, long responseTime, @Nullable Exception error) {
            this.target = target;
            this.healthy = healthy;
            this.responseTime = responseTime;
            this.error = error;
        }

        public HealthCheckResult(ModelProviderType provider, boolean healthy, long responseTime,
                @Nullable Exception error) {
            this.target = provider.name();
            this.healthy = healthy;
            this.responseTime = responseTime;
            this.error = error;
        }

        public String getTarget() {
            return target;
        }

        public boolean isHealthy() {
            return healthy;
        }

        public long getResponseTime() {
            return responseTime;
        }

        public @Nullable Exception getError() {
            return error;
        }
    }

    public static class SystemHealthStatus {
        private final Map<ModelProviderType, ProviderHealthMetrics> providerMetrics;
        private final Map<String, ServiceHealthMetrics> serviceMetrics;

        public SystemHealthStatus(Map<ModelProviderType, ProviderHealthMetrics> providerMetrics,
                Map<String, ServiceHealthMetrics> serviceMetrics) {
            this.providerMetrics = providerMetrics;
            this.serviceMetrics = serviceMetrics;
        }

        public Map<ModelProviderType, ProviderHealthMetrics> getProviderMetrics() {
            return providerMetrics;
        }

        public Map<String, ServiceHealthMetrics> getServiceMetrics() {
            return serviceMetrics;
        }

        public boolean isSystemHealthy() {
            return providerMetrics.values().stream().allMatch(ProviderHealthMetrics::isHealthy)
                    && serviceMetrics.values().stream().allMatch(ServiceHealthMetrics::isHealthy);
        }
    }

    public static class ProviderHealthMetrics {
        private final long totalRequests;
        private final long successfulRequests;
        private final long failedRequests;
        private final long totalResponseTime;
        private final double successRate;
        private final double averageResponseTime;
        private final CircuitBreakerState circuitBreakerState;
        private final boolean healthy;

        public ProviderHealthMetrics(long totalRequests, long successfulRequests, long failedRequests,
                long totalResponseTime, double successRate, double averageResponseTime,
                CircuitBreakerState circuitBreakerState, boolean healthy) {
            this.totalRequests = totalRequests;
            this.successfulRequests = successfulRequests;
            this.failedRequests = failedRequests;
            this.totalResponseTime = totalResponseTime;
            this.successRate = successRate;
            this.averageResponseTime = averageResponseTime;
            this.circuitBreakerState = circuitBreakerState;
            this.healthy = healthy;
        }

        public long getTotalRequests() {
            return totalRequests;
        }

        public long getSuccessfulRequests() {
            return successfulRequests;
        }

        public long getFailedRequests() {
            return failedRequests;
        }

        public long getTotalResponseTime() {
            return totalResponseTime;
        }

        public double getSuccessRate() {
            return successRate;
        }

        public double getAverageResponseTime() {
            return averageResponseTime;
        }

        public CircuitBreakerState getCircuitBreakerState() {
            return circuitBreakerState;
        }

        public boolean isHealthy() {
            return healthy;
        }
    }

    public static class ServiceHealthMetrics {
        private final long totalRequests;
        private final long successfulRequests;
        private final long failedRequests;
        private final long totalResponseTime;
        private final double successRate;
        private final double averageResponseTime;
        private final boolean healthy;

        public ServiceHealthMetrics(long totalRequests, long successfulRequests, long failedRequests,
                long totalResponseTime, double successRate, double averageResponseTime, boolean healthy) {
            this.totalRequests = totalRequests;
            this.successfulRequests = successfulRequests;
            this.failedRequests = failedRequests;
            this.totalResponseTime = totalResponseTime;
            this.successRate = successRate;
            this.averageResponseTime = averageResponseTime;
            this.healthy = healthy;
        }

        public long getTotalRequests() {
            return totalRequests;
        }

        public long getSuccessfulRequests() {
            return successfulRequests;
        }

        public long getFailedRequests() {
            return failedRequests;
        }

        public long getTotalResponseTime() {
            return totalResponseTime;
        }

        public double getSuccessRate() {
            return successRate;
        }

        public double getAverageResponseTime() {
            return averageResponseTime;
        }

        public boolean isHealthy() {
            return healthy;
        }
    }

    // Record classes for specification performance monitoring
    public record SpecificationPerformanceMetrics(String specificationId, long totalRequests, long successfulRequests,
            long failedRequests, long totalResponseTime, double averageResponseTime, double successRate,
            int currentThroughput, Instant lastUpdated) {
    }

    public record PerformanceAlert(String alertId, String specificationId, String type, String message, String severity,
            Instant timestamp) {
    }

    public record PerformanceOptimization(String optimizationId, String specificationId, String type,
            String description, String impact, Instant timestamp) {
    }

    public enum CircuitBreakerState {
        CLOSED, // Normal operation
        OPEN, // Circuit is open, requests are failing
        HALF_OPEN // Testing if service has recovered
    }

    private static class ProviderHealthState {
        private final ModelProviderType provider;
        private final AtomicLong totalRequests = new AtomicLong(0);
        private final AtomicLong successfulRequests = new AtomicLong(0);
        private final AtomicLong failedRequests = new AtomicLong(0);
        private final AtomicLong totalResponseTime = new AtomicLong(0);
        private final AtomicLong consecutiveFailures = new AtomicLong(0);
        private final AtomicReference<CircuitBreakerState> circuitBreakerState = new AtomicReference<>(
                CircuitBreakerState.CLOSED);
        private final AtomicReference<Instant> lastFailureTime = new AtomicReference<>();
        private final AtomicReference<Instant> lastHealthCheck = new AtomicReference<>();

        public ProviderHealthState(ModelProviderType provider) {
            this.provider = provider;
        }

        public void recordSuccess(long responseTime) {
            totalRequests.incrementAndGet();
            successfulRequests.incrementAndGet();
            totalResponseTime.addAndGet(responseTime);
            consecutiveFailures.set(0);

            // If circuit breaker is half-open and we get a success, close it
            if (circuitBreakerState.get() == CircuitBreakerState.HALF_OPEN) {
                circuitBreakerState.set(CircuitBreakerState.CLOSED);
            }
        }

        public void recordFailure(Exception error) {
            totalRequests.incrementAndGet();
            failedRequests.incrementAndGet();
            consecutiveFailures.incrementAndGet();
            lastFailureTime.set(Instant.now());

            // Check if we should open the circuit breaker
            if (consecutiveFailures.get() >= 5 && circuitBreakerState.get() == CircuitBreakerState.CLOSED) {
                circuitBreakerState.set(CircuitBreakerState.OPEN);
            }
        }

        public void updateFromHealthCheck(HealthCheckResult result) {
            lastHealthCheck.set(Instant.now());

            if (result.isHealthy() && circuitBreakerState.get() == CircuitBreakerState.OPEN) {
                circuitBreakerState.set(CircuitBreakerState.HALF_OPEN);
            }
        }

        public void forceRecovery() {
            consecutiveFailures.set(0);
            circuitBreakerState.set(CircuitBreakerState.CLOSED);
            lastFailureTime.set(Instant.now());
        }

        public boolean isHealthy() {
            if (circuitBreakerState.get() == CircuitBreakerState.OPEN) {
                return false;
            }

            double successRate = getSuccessRate();
            double avgResponseTime = getAverageResponseTime();

            return successRate >= 0.8 && avgResponseTime <= 5000; // 80% success rate, 5s max response time
        }

        public ProviderHealthMetrics getHealthMetrics() {
            return new ProviderHealthMetrics(totalRequests.get(), successfulRequests.get(), failedRequests.get(),
                    totalResponseTime.get(), getSuccessRate(), getAverageResponseTime(), circuitBreakerState.get(),
                    isHealthy());
        }

        private double getSuccessRate() {
            long total = totalRequests.get();
            return total > 0 ? (double) successfulRequests.get() / total : 0.0;
        }

        private double getAverageResponseTime() {
            long total = totalRequests.get();
            return total > 0 ? (double) totalResponseTime.get() / total : 0.0;
        }
    }

    private static class ServiceHealthState {
        private final String serviceName;
        private final AtomicLong totalRequests = new AtomicLong(0);
        private final AtomicLong successfulRequests = new AtomicLong(0);
        private final AtomicLong failedRequests = new AtomicLong(0);
        private final AtomicLong totalResponseTime = new AtomicLong(0);
        private final AtomicReference<Instant> lastHealthCheck = new AtomicReference<>();

        public ServiceHealthState(String serviceName) {
            this.serviceName = serviceName;
        }

        public void recordSuccess(long responseTime) {
            totalRequests.incrementAndGet();
            successfulRequests.incrementAndGet();
            totalResponseTime.addAndGet(responseTime);
        }

        public void recordFailure(Exception error) {
            totalRequests.incrementAndGet();
            failedRequests.incrementAndGet();
        }

        public void updateFromHealthCheck(HealthCheckResult result) {
            lastHealthCheck.set(Instant.now());
        }

        public void forceRecovery() {
            // Reset failure counters
            failedRequests.set(0);
        }

        public boolean isHealthy() {
            double successRate = getSuccessRate();
            double avgResponseTime = getAverageResponseTime();

            return successRate >= 0.8 && avgResponseTime <= 5000; // 80% success rate, 5s max response time
        }

        public ServiceHealthMetrics getHealthMetrics() {
            return new ServiceHealthMetrics(totalRequests.get(), successfulRequests.get(), failedRequests.get(),
                    totalResponseTime.get(), getSuccessRate(), getAverageResponseTime(), isHealthy());
        }

        private double getSuccessRate() {
            long total = totalRequests.get();
            return total > 0 ? (double) successfulRequests.get() / total : 0.0;
        }

        private double getAverageResponseTime() {
            long total = totalRequests.get();
            return total > 0 ? (double) totalResponseTime.get() / total : 0.0;
        }
    }
}
