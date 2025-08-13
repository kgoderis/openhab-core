package org.openhab.core.ai.tool.monitoring.api;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.model.api.ModelProviderType;
import org.openhab.core.ai.tool.monitoring.HealthCheckResult;

/**
 * System Health Monitor Interface
 * 
 * <p>
 * This interface defines the contract for system health monitoring implementations that provide:
 * - Health checking for tool providers and services
 * - Performance metrics collection and analysis
 * - Circuit breaker pattern implementation
 * - Failure detection and alerting
 * - Recovery mechanisms and automatic healing
 * - Health reporting and status aggregation
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface SystemHealthMonitor {

    /**
     * Check if a provider is healthy
     * 
     * @param provider the provider to check
     * @return true if the provider is healthy
     */
    boolean isProviderHealthy(ModelProviderType provider);

    /**
     * Check if a service is healthy
     * 
     * @param serviceName the service name to check
     * @return true if the service is healthy
     */
    boolean isServiceHealthy(String serviceName);

    /**
     * Record a successful operation for a provider
     * 
     * @param provider the provider
     * @param responseTime the response time in milliseconds
     */
    void recordSuccess(ModelProviderType provider, long responseTime);

    /**
     * Record a failed operation for a provider
     * 
     * @param provider the provider
     * @param error the error that occurred
     */
    void recordFailure(ModelProviderType provider, Exception error);

    /**
     * Record a successful operation for a service
     * 
     * @param serviceName the service name
     * @param responseTime the response time in milliseconds
     */
    void recordServiceSuccess(String serviceName, long responseTime);

    /**
     * Record a failed operation for a service
     * 
     * @param serviceName the service name
     * @param error the error that occurred
     */
    void recordServiceFailure(String serviceName, Exception error);

    /**
     * Perform a health check for a provider
     * 
     * @param provider the provider to check
     * @return health check result
     */
    CompletableFuture<org.openhab.core.ai.tool.monitoring.HealthCheckResult> performHealthCheck(
            ModelProviderType provider);

    /**
     * Perform a health check for a service
     * 
     * @param serviceName the service name to check
     * @return health check result
     */
    CompletableFuture<HealthCheckResult> performServiceHealthCheck(String serviceName);

    /**
     * Get health metrics for a service
     * 
     * @param serviceName the service name
     * @return service health metrics
     */
    org.openhab.core.ai.tool.monitoring.ServiceHealthMetrics getServiceHealthMetrics(String serviceName);

    /**
     * Force recovery for a provider
     * 
     * @param provider the provider to recover
     */
    void forceProviderRecovery(ModelProviderType provider);

    /**
     * Force recovery for a service
     * 
     * @param serviceName the service name to recover
     */
    void forceServiceRecovery(String serviceName);

    /**
     * Reset health state for a provider
     * 
     * @param provider the provider to reset
     */
    void resetProviderHealth(ModelProviderType provider);

    /**
     * Reset health state for a service
     * 
     * @param serviceName the service name to reset
     */
    void resetServiceHealth(String serviceName);

    /**
     * Set failure threshold
     * 
     * @param threshold the failure threshold
     */
    void setFailureThreshold(int threshold);

    /**
     * Set recovery timeout
     * 
     * @param timeout the recovery timeout
     */
    void setRecoveryTimeout(Duration timeout);

    /**
     * Set health check interval
     * 
     * @param interval the health check interval
     */
    void setHealthCheckInterval(Duration interval);

    /**
     * Set maximum response time
     * 
     * @param maxResponseTime the maximum response time in milliseconds
     */
    void setMaxResponseTime(long maxResponseTime);

    /**
     * Set minimum success rate
     * 
     * @param minSuccessRate the minimum success rate (0.0 to 1.0)
     */
    void setMinSuccessRate(double minSuccessRate);

    /**
     * Set monitoring enabled
     * 
     * @param enabled true to enable monitoring
     */
    void setMonitoringEnabled(boolean enabled);

    /**
     * Set auto recovery enabled
     * 
     * @param enabled true to enable auto recovery
     */
    void setAutoRecoveryEnabled(boolean enabled);

    /**
     * Record specification execution
     * 
     * @param specificationId the specification ID
     * @param responseTime the response time in milliseconds
     * @param success true if the execution was successful
     */
    void recordSpecificationExecution(String specificationId, long responseTime, boolean success);

    /**
     * Get specification metrics
     * 
     * @param specificationId the specification ID
     * @return specification performance metrics
     */
    org.openhab.core.ai.tool.monitoring.SpecificationPerformanceMetrics getSpecificationMetrics(String specificationId);

    /**
     * Get all specification metrics
     * 
     * @return map of specification performance metrics
     */
    Map<String, org.openhab.core.ai.tool.monitoring.SpecificationPerformanceMetrics> getAllSpecificationMetrics();

    /**
     * Get specification alerts
     * 
     * @param specificationId the specification ID
     * @return list of performance alerts
     */
    List<org.openhab.core.ai.tool.monitoring.PerformanceAlert> getSpecificationAlerts(String specificationId);

    /**
     * Get specification optimizations
     * 
     * @param specificationId the specification ID
     * @return list of performance optimizations
     */
    List<org.openhab.core.ai.tool.monitoring.PerformanceOptimization> getSpecificationOptimizations(
            String specificationId);

    /**
     * Get system health status
     * 
     * @return system health status
     */
    org.openhab.core.ai.tool.monitoring.SystemHealthStatus getSystemHealthStatus();

    /**
     * Get provider health metrics
     * 
     * @param provider the provider
     * @return provider health metrics
     */
    org.openhab.core.ai.tool.monitoring.ProviderHealthMetrics getProviderHealthMetrics(ModelProviderType provider);

    /**
     * Check if monitoring is enabled
     * 
     * @return true if monitoring is enabled
     */
    boolean isMonitoringEnabled();

    /**
     * Check if auto recovery is enabled
     * 
     * @return true if auto recovery is enabled
     */
    boolean isAutoRecoveryEnabled();

    /**
     * Get failure threshold
     * 
     * @return failure threshold
     */
    int getFailureThreshold();

    /**
     * Get recovery timeout
     * 
     * @return recovery timeout
     */
    Duration getRecoveryTimeout();

    /**
     * Get health check interval
     * 
     * @return health check interval
     */
    Duration getHealthCheckInterval();

    /**
     * Get maximum response time
     * 
     * @return maximum response time in milliseconds
     */
    long getMaxResponseTime();

    /**
     * Get minimum success rate
     * 
     * @return minimum success rate (0.0 to 1.0)
     */
    double getMinSuccessRate();
}
