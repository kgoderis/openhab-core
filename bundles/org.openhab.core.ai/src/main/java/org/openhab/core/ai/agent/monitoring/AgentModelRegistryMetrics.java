package org.openhab.core.ai.agent.monitoring;

import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.MetricKeys;
import org.openhab.core.ai.common.monitoring.collector.MetricsCollector;
import org.openhab.core.ai.common.monitoring.registry.MonitoringRegistry;
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
    private @Nullable MonitoringRegistry monitoringRegistry;

    /**
     * Record model registration using the new monitoring framework
     */
    public void recordModelRegistration(String modelId, ModelProviderType provider, long duration, boolean success) {
        try {
            // Use centralized monitoring registry
            if (monitoringRegistry != null) {
                MetricsCollector collector = monitoringRegistry
                        .metricsCollector(MetricKeys.action("model-registration"));
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
                MetricsCollector collector = monitoringRegistry
                        .metricsCollector(MetricKeys.action("model-unregistration"));
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
                MetricsCollector collector = monitoringRegistry.metricsCollector(MetricKeys.action("model-retrieval"));
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
                MetricsCollector collector = monitoringRegistry.metricsCollector(MetricKeys.action("model-validation"));
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
    public Map<String, Object> getStatistics() {
        Map<String, Object> statistics = new java.util.HashMap<>();

        if (monitoringRegistry != null) {
            // Get statistics from monitoring registry
            MetricsCollector registrationCollector = monitoringRegistry
                    .metricsCollector(MetricKeys.action("model-registration"));
            MetricsCollector unregistrationCollector = monitoringRegistry
                    .metricsCollector(MetricKeys.action("model-unregistration"));
            MetricsCollector retrievalCollector = monitoringRegistry
                    .metricsCollector(MetricKeys.action("model-retrieval"));
            MetricsCollector validationCollector = monitoringRegistry
                    .metricsCollector(MetricKeys.action("model-validation"));

            var registrationSnapshot = registrationCollector.executionSnapshot();
            var unregistrationSnapshot = unregistrationCollector.executionSnapshot();
            var retrievalSnapshot = retrievalCollector.executionSnapshot();
            var validationSnapshot = validationCollector.executionSnapshot();

            statistics.put("totalRegistrations", registrationSnapshot.total());
            statistics.put("successfulRegistrations", registrationSnapshot.success());
            statistics.put("failedRegistrations", registrationSnapshot.failure());
            statistics.put("totalUnregistrations", unregistrationSnapshot.total());
            statistics.put("successfulUnregistrations", unregistrationSnapshot.success());
            statistics.put("failedUnregistrations", unregistrationSnapshot.failure());
            statistics.put("totalRetrievals", retrievalSnapshot.total());
            statistics.put("successfulRetrievals", retrievalSnapshot.success());
            statistics.put("failedRetrievals", retrievalSnapshot.failure());
            statistics.put("totalValidations", validationSnapshot.total());
            statistics.put("successfulValidations", validationSnapshot.success());
            statistics.put("failedValidations", validationSnapshot.failure());
            statistics.put("timestamp", Instant.now());
        } else {
            // Fallback to basic statistics
            statistics.put("totalRegistrations", 0);
            statistics.put("successfulRegistrations", 0);
            statistics.put("failedRegistrations", 0);
            statistics.put("totalUnregistrations", 0);
            statistics.put("successfulUnregistrations", 0);
            statistics.put("failedUnregistrations", 0);
            statistics.put("totalRetrievals", 0);
            statistics.put("successfulRetrievals", 0);
            statistics.put("failedRetrievals", 0);
            statistics.put("totalValidations", 0);
            statistics.put("successfulValidations", 0);
            statistics.put("failedValidations", 0);
            statistics.put("timestamp", Instant.now());
        }

        return statistics;
    }
}
