package org.openhab.core.ai.tool.services;

import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.MetricKeys;
import org.openhab.core.ai.common.monitoring.collector.ExecutionMetricsCollector;
import org.openhab.core.ai.common.monitoring.registry.MonitoringRegistry;
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
    private @Nullable MonitoringRegistry monitoringRegistry;

    /**
     * Record provider execution using the new monitoring framework
     */
    public void recordProviderExecution(ModelProviderType provider, long executionTime, boolean success) {
        try {
            // Use centralized monitoring registry
            if (monitoringRegistry != null) {
                ExecutionMetricsCollector collector = monitoringRegistry
                        .executionCollector(MetricKeys.provider(provider.name()));
                collector.recordExecution(success, executionTime * 1_000_000L); // Convert to nanoseconds
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
            if (monitoringRegistry != null) {
                ExecutionMetricsCollector collector = monitoringRegistry
                        .executionCollector(MetricKeys.provider(provider.name()));
                collector.recordExecution(false, 0L);
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
            if (monitoringRegistry != null) {
                ExecutionMetricsCollector collector = monitoringRegistry
                        .executionCollector(MetricKeys.action("provider-health-" + provider.name()));
                collector.recordExecution(healthy, responseTime * 1_000_000L); // Convert to nanoseconds
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
     * Get provider statistics using the new monitoring framework
     */
    public Map<String, Object> getProviderStatistics(ModelProviderType provider) {
        Map<String, Object> statistics = new java.util.HashMap<>();

        if (monitoringRegistry != null) {
            // Get statistics from monitoring registry for specific provider
            ExecutionMetricsCollector collector = monitoringRegistry
                    .executionCollector(MetricKeys.provider(provider.name()));
            var snapshot = collector.snapshot();

            statistics.put("provider", provider.name());
            statistics.put("totalExecutions", snapshot.total());
            statistics.put("successfulExecutions", snapshot.success());
            statistics.put("failedExecutions", snapshot.failure());
            statistics.put("totalExecutionTime", snapshot.totalDurationNanos() / 1_000_000); // Convert from nanoseconds
            statistics.put("averageExecutionTime",
                    snapshot.totalDurationNanos() / Math.max(1, snapshot.total()) / 1_000_000); // Convert from
                                                                                                // nanoseconds
            statistics.put("successRate", snapshot.successRate());
            statistics.put("timestamp", Instant.now());
        } else {
            // Fallback to basic statistics
            statistics.put("provider", provider.name());
            statistics.put("totalExecutions", 0);
            statistics.put("successfulExecutions", 0);
            statistics.put("failedExecutions", 0);
            statistics.put("totalExecutionTime", 0);
            statistics.put("averageExecutionTime", 0);
            statistics.put("successRate", 0.0);
            statistics.put("timestamp", Instant.now());
        }

        return statistics;
    }

    /**
     * Get overall provider statistics using the new monitoring framework
     */
    public Map<String, Object> getOverallStatistics() {
        Map<String, Object> statistics = new java.util.HashMap<>();

        if (monitoringRegistry != null) {
            // Get statistics from monitoring registry for all providers
            ExecutionMetricsCollector collector = monitoringRegistry
                    .executionCollector(MetricKeys.action("provider-execution"));
            var snapshot = collector.snapshot();

            statistics.put("totalProviderExecutions", snapshot.total());
            statistics.put("successfulProviderExecutions", snapshot.success());
            statistics.put("failedProviderExecutions", snapshot.failure());
            statistics.put("totalExecutionTime", snapshot.totalDurationNanos() / 1_000_000); // Convert from nanoseconds
            statistics.put("averageExecutionTime",
                    snapshot.totalDurationNanos() / Math.max(1, snapshot.total()) / 1_000_000); // Convert from
                                                                                                // nanoseconds
            statistics.put("overallSuccessRate", snapshot.successRate());
            statistics.put("timestamp", Instant.now());
        } else {
            // Fallback to basic statistics
            statistics.put("totalProviderExecutions", 0);
            statistics.put("successfulProviderExecutions", 0);
            statistics.put("failedProviderExecutions", 0);
            statistics.put("totalExecutionTime", 0);
            statistics.put("averageExecutionTime", 0);
            statistics.put("overallSuccessRate", 0.0);
            statistics.put("timestamp", Instant.now());
        }

        return statistics;
    }

    /**
     * Reset provider metrics for a specific provider
     */
    public void resetProviderMetrics(ModelProviderType provider) {
        if (monitoringRegistry != null) {
            monitoringRegistry.reset(MetricKeys.provider(provider.name()));
            logger.info("Provider metrics reset for provider: {}", provider);
        }
    }

    /**
     * Reset all provider metrics
     */
    public void resetAllMetrics() {
        if (monitoringRegistry != null) {
            monitoringRegistry.reset(MetricKeys.action("provider-execution"));
            logger.info("All provider metrics reset");
        }
    }
}
