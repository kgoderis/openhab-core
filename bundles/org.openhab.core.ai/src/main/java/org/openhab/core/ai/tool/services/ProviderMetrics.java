package org.openhab.core.ai.tool.services;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.model.api.ModelProviderType;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pure metrics recording helper for provider operations.
 * 
 * <p>
 * This class provides lightweight metrics recording for provider operations:
 * - Provider execution recording via MetricsService
 * - Error occurrence recording
 * - Health check recording
 * </p>
 * 
 * <p>
 * All statistics retrieval should be done directly through MetricsService
 * using appropriate MetricKeys. This class only handles recording operations.
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
        if (provider == null || executionTime < 0) {
            return; // Silently skip invalid input
        }

        MetricsService metrics = metricsService;
        if (metrics != null) {
            try {
                metrics.recordOperation("provider", "execution", success, java.time.Duration.ofMillis(executionTime));
            } catch (Exception e) {
                logger.debug("Failed to record provider execution metrics: {}", e.getMessage());
            }
        }
    }

    /**
     * Record provider error using the new monitoring framework
     */
    public void recordProviderError(ModelProviderType provider, String errorType) {
        if (provider == null) {
            return; // Silently skip invalid input
        }

        MetricsService metrics = metricsService;
        if (metrics != null) {
            try {
                metrics.recordOperation("provider", "error", false, java.time.Duration.ofMillis(0));
            } catch (Exception e) {
                logger.debug("Failed to record provider error metrics: {}", e.getMessage());
            }
        }
    }

    /**
     * Record provider health check using the new monitoring framework
     */
    public void recordProviderHealthCheck(ModelProviderType provider, boolean healthy, long responseTime) {
        if (provider == null || responseTime < 0) {
            return; // Silently skip invalid input
        }

        MetricsService metrics = metricsService;
        if (metrics != null) {
            try {
                metrics.recordOperation("provider", "health-check", healthy, java.time.Duration.ofMillis(responseTime));
            } catch (Exception e) {
                logger.debug("Failed to record provider health check metrics: {}", e.getMessage());
            }
        }
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
