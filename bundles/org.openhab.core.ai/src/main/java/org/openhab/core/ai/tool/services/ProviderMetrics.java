package org.openhab.core.ai.tool.services;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.model.api.ModelProviderType;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Metrics for provider operations using the new monitoring framework.
 * 
 * <p>
 * This class provides comprehensive metrics for provider operations:
 * - Provider execution statistics
 * - Performance timing and processing metrics
 * - Error tracking and failure analysis
 * - Success rate calculations
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@Component(service = ProviderMetrics.class)
@NonNullByDefault
public class ProviderMetrics {

    private static final Logger logger = LoggerFactory.getLogger(ProviderMetrics.class);

    // NEW: Monitoring registry for centralized metrics collection
    @Reference
    private @Nullable MetricsService metricsService;

    /**
     * Record provider execution using the new monitoring framework
     */
    public void recordProviderExecution(ModelProviderType provider, long executionTime, boolean success) {
        try {
            // Use centralized monitoring registry
            MetricsService metrics = metricsService;
            if (metrics != null) {
                metrics.recordOperation("provider", "execution", success, java.time.Duration.ofMillis(executionTime));
            }

            // Log the operation
            if (success) {
                logger.debug("Provider execution successful - Provider: {}, Duration: {}ms", provider, executionTime);
            } else {
                logger.warn("Provider execution failed - Provider: {}, Duration: {}ms", provider, executionTime);
            }

        } catch (Exception e) {
            logger.error("Error recording provider execution metrics for provider: {}", provider, e);
        }
    }

    /**
     * Record provider error using the new monitoring framework
     */
    public void recordProviderError(ModelProviderType provider, String errorType) {
        try {
            // Use centralized monitoring registry
            MetricsService metrics = metricsService;
            if (metrics != null) {
                metrics.recordOperation("provider", "error", false, java.time.Duration.ofMillis(0));
            }

            // Log the error
            logger.warn("Provider error recorded - Provider: {}, Error: {}", provider, errorType);

        } catch (Exception e) {
            logger.error("Error recording provider error metrics for provider: {}", provider, e);
        }
    }

    /**
     * Record provider health check using the new monitoring framework
     */
    public void recordProviderHealthCheck(ModelProviderType provider, boolean healthy, long responseTime) {
        try {
            // Use centralized monitoring registry
            MetricsService metrics = metricsService;
            if (metrics != null) {
                metrics.recordOperation("provider", "health-check", healthy, java.time.Duration.ofMillis(responseTime));
            }

            // Log the health check
            if (healthy) {
                logger.debug("Provider health check successful - Provider: {}, Response time: {}ms", provider,
                        responseTime);
            } else {
                logger.warn("Provider health check failed - Provider: {}, Response time: {}ms", provider, responseTime);
            }

        } catch (Exception e) {
            logger.error("Error recording provider health check metrics for provider: {}", provider, e);
        }
    }

    /**
     * Get provider execution statistics using the new monitoring framework
     */
    public Map<String, Object> getProviderStatistics(ModelProviderType provider) {
        Map<String, Object> statistics = new java.util.HashMap<>();

        MetricsService metrics = metricsService;
        if (metrics != null) {
            try {
                // Get statistics from metrics service for specific provider
                var snapshot = metrics.getDomainAggregatedSnapshot("provider");

                statistics.put("provider", provider.name());
                statistics.put("totalExecutions", snapshot.totalOperations());
                statistics.put("successfulExecutions", snapshot.successfulOperations());
                statistics.put("failedExecutions", snapshot.failedOperations());
                statistics.put("totalExecutionTimeMs", snapshot.totalDurationNanos() / 1_000_000); // Convert from
                                                                                                   // nanoseconds
                statistics.put("averageExecutionTimeMs",
                        snapshot.totalOperations() > 0
                                ? snapshot.totalDurationNanos() / (snapshot.totalOperations() * 1_000_000)
                                : 0);
                statistics.put("successRate", snapshot.getSuccessRate());
                statistics.put("lastUpdated", System.currentTimeMillis());
            } catch (Exception e) {
                logger.debug("Failed to get provider statistics: {}", e.getMessage());
            }
        }

        return statistics;
    }

    /**
     * Get overall provider execution statistics using the new monitoring framework
     */
    public Map<String, Object> getAllProviderStatistics() {
        Map<String, Object> statistics = new java.util.HashMap<>();

        MetricsService metrics = metricsService;
        if (metrics != null) {
            try {
                // Get statistics from metrics service for all providers
                var snapshot = metrics.getDomainAggregatedSnapshot("provider");

                statistics.put("totalProviderExecutions", snapshot.totalOperations());
                statistics.put("successfulProviderExecutions", snapshot.successfulOperations());
                statistics.put("failedProviderExecutions", snapshot.failedOperations());
                statistics.put("totalExecutionTimeMs", snapshot.totalDurationNanos() / 1_000_000); // Convert from
                                                                                                   // nanoseconds
                statistics.put("averageExecutionTimeMs",
                        snapshot.totalOperations() > 0
                                ? snapshot.totalDurationNanos() / (snapshot.totalOperations() * 1_000_000)
                                : 0);
                statistics.put("successRate", snapshot.getSuccessRate());
                statistics.put("lastUpdated", System.currentTimeMillis());
            } catch (Exception e) {
                logger.debug("Failed to get all provider statistics: {}", e.getMessage());
            }
        }

        return statistics;
    }

    /**
     * Reset provider metrics for a specific provider.
     * 
     * @param provider the provider type
     */
    public void resetProviderMetrics(ModelProviderType provider) {
        // Reset functionality is not available in MetricsService
        logger.info("Provider metrics reset not supported for provider: {}", provider);
    }

    /**
     * Reset all provider metrics.
     */
    public void resetAllMetrics() {
        // Reset functionality is not available in MetricsService
        logger.info("All provider metrics reset not supported");
    }
}
