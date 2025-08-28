package org.openhab.core.ai.agent.monitoring;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.MetricKeys;
import org.openhab.core.ai.common.monitoring.collector.MetricsCollector;
import org.openhab.core.ai.common.monitoring.registry.MetricsRegistry;
import org.openhab.core.ai.model.api.ModelProviderType;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Metrics for agent model registry operations.
 * 
 * <p>
 * This class provides comprehensive metrics for agent model registry operations:
 * - Model registration and unregistration statistics
 * - Model retrieval and validation metrics
 * - Performance timing and processing metrics
 * - Error tracking and failure analysis
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@Component(service = AgentModelRegistryMetrics.class)
@NonNullByDefault
public class AgentModelRegistryMetrics {

    private static final Logger logger = LoggerFactory.getLogger(AgentModelRegistryMetrics.class);

    // NEW: Monitoring registry for centralized metrics collection
    @Reference
    private @Nullable MetricsRegistry monitoringRegistry;

    /**
     * Record model registration using the new monitoring framework
     */
    public void recordModelRegistration(String modelId, ModelProviderType provider, long duration, boolean success) {
        try {
            // Use centralized monitoring registry
            if (monitoringRegistry != null) {
                MetricsCollector collector = monitoringRegistry.getCollector(MetricKeys.action("model-registration"));
                collector.recordExecution(success, duration * 1_000_000L); // Convert to nanoseconds
            }

            // Log the operation
            if (success) {
                logger.debug("Model registration successful - Model: {}, Provider: {}, Duration: {}ms", modelId,
                        provider, duration);
            } else {
                logger.warn("Model registration failed - Model: {}, Provider: {}, Duration: {}ms", modelId, provider,
                        duration);
            }

        } catch (Exception e) {
            logger.error("Error recording model registration metrics for model: {}", modelId, e);
        }
    }

    /**
     * Record model unregistration using the new monitoring framework
     */
    public void recordModelUnregistration(String modelId, long duration, boolean success) {
        try {
            // Use centralized monitoring registry
            if (monitoringRegistry != null) {
                MetricsCollector collector = monitoringRegistry.getCollector(MetricKeys.action("model-unregistration"));
                collector.recordExecution(success, duration * 1_000_000L); // Convert to nanoseconds
            }

            // Log the operation
            if (success) {
                logger.debug("Model unregistration successful - Model: {}, Duration: {}ms", modelId, duration);
            } else {
                logger.warn("Model unregistration failed - Model: {}, Duration: {}ms", modelId, duration);
            }

        } catch (Exception e) {
            logger.error("Error recording model unregistration metrics for model: {}", modelId, e);
        }
    }

    /**
     * Record model retrieval using the new monitoring framework
     */
    public void recordModelRetrieval(String modelId, long duration, boolean success) {
        try {
            // Use centralized monitoring registry
            if (monitoringRegistry != null) {
                MetricsCollector collector = monitoringRegistry.getCollector(MetricKeys.action("model-retrieval"));
                collector.recordExecution(success, duration * 1_000_000L); // Convert to nanoseconds
            }

            // Log the operation
            if (success) {
                logger.debug("Model retrieval successful - Model: {}, Duration: {}ms", modelId, duration);
            } else {
                logger.warn("Model retrieval failed - Model: {}, Duration: {}ms", modelId, duration);
            }

        } catch (Exception e) {
            logger.error("Error recording model retrieval metrics for model: {}", modelId, e);
        }
    }

    /**
     * Record model validation using the new monitoring framework
     */
    public void recordModelValidation(String modelId, long duration, boolean success) {
        try {
            // Use centralized monitoring registry
            if (monitoringRegistry != null) {
                MetricsCollector collector = monitoringRegistry.getCollector(MetricKeys.action("model-validation"));
                collector.recordExecution(success, duration * 1_000_000L); // Convert to nanoseconds
            }

            // Log the operation
            if (success) {
                logger.debug("Model validation successful - Model: {}, Duration: {}ms", modelId, duration);
            } else {
                logger.warn("Model validation failed - Model: {}, Duration: {}ms", modelId, duration);
            }

        } catch (Exception e) {
            logger.error("Error recording model validation metrics for model: {}", modelId, e);
        }
    }

    /**
     * Get model registry statistics using the new monitoring framework
     */
    // Eliminated getStatistics() proxy method
    // Consumers should access snapshots directly via MetricsService:
    // Use injected MetricsService to get snapshots:
    // - MetricKeys.action("model-registration") for registration snapshots
    // - MetricKeys.action("model-unregistration") for unregistration snapshots
    // - MetricKeys.action("model-retrieval") for retrieval snapshots
    // - MetricKeys.action("model-validation") for validation snapshots

    // The statistics transformation logic above can be moved to a static utility method if needed
}
