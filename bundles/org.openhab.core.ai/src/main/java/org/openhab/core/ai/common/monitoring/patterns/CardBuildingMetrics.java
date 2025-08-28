package org.openhab.core.ai.common.monitoring.patterns;

import java.time.Duration;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.utils.SystemMetricsCollector;

/**
 * Utility class for recording agent card building metrics using the enhanced MetricsService.
 * 
 * <p>
 * This class provides methods to record comprehensive metrics for agent card generation
 * including generation steps, validation, success rates, and performance characteristics.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class CardBuildingMetrics {

    private final MetricsService metricsService;

    public CardBuildingMetrics(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    /**
     * Record card generation metrics.
     * 
     * @param cardId the unique card identifier
     * @param cardType the type of card being generated
     * @param agentId the ID of the agent for which the card is generated
     * @param generationTime the time taken to generate the card
     * @param cardSize the size of the generated card
     * @param success whether the generation was successful
     */
    public void recordCardGeneration(String cardId, String cardType, String agentId, Duration generationTime,
            long cardSize, boolean success) {
        metricsService.recordOperation("card", "generation").withSuccess(success).withDuration(generationTime.toNanos())
                .withData("cardId", cardId).withData("cardType", cardType).withData("agentId", agentId)
                .withData("cardSize", cardSize)
                // Performance Metrics Enhancements
                .withTimingContext(7, 0, cardSize) // High complexity for card generation
                .withResourceUtilization(SystemMetricsCollector.getCurrentMemoryUsage(),
                        SystemMetricsCollector.getCurrentCpuUsage(), SystemMetricsCollector.estimateDiskIOTime())
                .withConcurrencyMetrics(SystemMetricsCollector.getActiveOperationsCount(),
                        SystemMetricsCollector.getCurrentQueueSize(),
                        (int) SystemMetricsCollector.getTotalContentionCount())
                .withQualityMetrics(success ? null : "card-generation-failure", success ? null : 3, 0, false)
                .withUserExperienceMetrics(generationTime.toMillis(), generationTime.toMillis() / 2, success ? 4 : 2) // User
                                                                                                                      // satisfaction
                                                                                                                      // based
                                                                                                                      // on
                                                                                                                      // success
                .withSystemHealthCorrelation(SystemMetricsCollector.calculateSystemHealthScore(),
                        SystemMetricsCollector.getActiveAlertsCount(),
                        SystemMetricsCollector.calculateResourceAvailability(),
                        SystemMetricsCollector.getSystemLoadAverage())
                .record();
    }

    /**
     * Record card validation metrics.
     * 
     * @param cardId the unique card identifier
     * @param validationRules the number of validation rules applied
     * @param validationTime the time taken for validation
     * @param validationErrors the number of validation errors
     * @param validationWarnings the number of validation warnings
     * @param validationSuccess whether validation passed
     */
    public void recordCardValidation(String cardId, int validationRules, Duration validationTime, int validationErrors,
            int validationWarnings, boolean validationSuccess) {
        metricsService.recordOperation("card", "validation").withSuccess(validationSuccess)
                .withDuration(validationTime.toNanos()).withData("cardId", cardId)
                .withData("validationRules", validationRules).withData("validationErrors", validationErrors)
                .withData("validationWarnings", validationWarnings)
                // Performance Metrics Enhancements
                .withTimingContext(6, validationRules * 50L, 0) // High complexity for validation
                .withResourceUtilization(SystemMetricsCollector.getCurrentMemoryUsage(),
                        SystemMetricsCollector.getCurrentCpuUsage(), 0)
                .withConcurrencyMetrics(SystemMetricsCollector.getActiveOperationsCount(),
                        SystemMetricsCollector.getCurrentQueueSize(), 0)
                .withQualityMetrics(validationSuccess ? null : "validation-errors", validationSuccess ? null : 2, 0,
                        false)
                .withUserExperienceMetrics(validationTime.toMillis(), 0, validationSuccess ? 4 : 2)
                .withSystemHealthCorrelation(SystemMetricsCollector.calculateSystemHealthScore(),
                        SystemMetricsCollector.getActiveAlertsCount(),
                        SystemMetricsCollector.calculateResourceAvailability(),
                        SystemMetricsCollector.getSystemLoadAverage())
                .record();
    }

    /**
     * Record card rendering metrics.
     * 
     * @param cardId the unique card identifier
     * @param renderType the type of rendering (html, json, xml, etc.)
     * @param renderTime the time taken to render the card
     * @param outputSize the size of the rendered output
     * @param renderSuccess whether rendering was successful
     * @param templateUsed the template used for rendering
     */
    public void recordCardRendering(String cardId, String renderType, Duration renderTime, long outputSize,
            boolean renderSuccess, String templateUsed) {
        metricsService.recordOperation("card", "rendering").withSuccess(renderSuccess)
                .withDuration(renderTime.toNanos()).withData("cardId", cardId).withData("renderType", renderType)
                .withData("outputSize", outputSize).withData("templateUsed", templateUsed)
                // Performance Metrics Enhancements
                .withTimingContext(5, 0, outputSize) // Moderate complexity for rendering
                .withResourceUtilization(SystemMetricsCollector.getCurrentMemoryUsage(),
                        SystemMetricsCollector.getCurrentCpuUsage(), SystemMetricsCollector.estimateDiskIOTime())
                .withConcurrencyMetrics(SystemMetricsCollector.getActiveOperationsCount(),
                        SystemMetricsCollector.getCurrentQueueSize(),
                        (int) SystemMetricsCollector.getTotalContentionCount())
                .withQualityMetrics(renderSuccess ? null : "rendering-failure", renderSuccess ? null : 2, 0, false)
                .withUserExperienceMetrics(renderTime.toMillis(), 0, renderSuccess ? 4 : 2)
                .withSystemHealthCorrelation(SystemMetricsCollector.calculateSystemHealthScore(),
                        SystemMetricsCollector.getActiveAlertsCount(),
                        SystemMetricsCollector.calculateResourceAvailability(),
                        SystemMetricsCollector.getSystemLoadAverage())
                .record();
    }

    /**
     * Record card caching metrics.
     * 
     * @param cardId the unique card identifier
     * @param cacheOperation the type of cache operation (hit, miss, store, evict)
     * @param cacheTime the time taken for the cache operation
     * @param cacheSize the size of the cached card
     * @param cacheSuccess whether the cache operation was successful
     */
    public void recordCardCaching(String cardId, String cacheOperation, Duration cacheTime, long cacheSize,
            boolean cacheSuccess) {
        boolean success = cacheTime.toNanos() < 100_000; // Cache operations should be very fast (< 0.1ms)

        metricsService.recordOperation("card", "cache-" + cacheOperation).withSuccess(success)
                .withDuration(cacheTime.toNanos()).withData("cardId", cardId).withData("cacheOperation", cacheOperation)
                .withData("cacheSize", cacheSize)
                // Performance Metrics Enhancements
                .withTimingContext(2, 0, cacheSize) // Simple cache operation
                .withResourceUtilization(SystemMetricsCollector.getCurrentMemoryUsage(),
                        SystemMetricsCollector.getCurrentCpuUsage(), 0)
                .withConcurrencyMetrics(SystemMetricsCollector.getActiveOperationsCount(),
                        SystemMetricsCollector.getCurrentQueueSize(), 0)
                .withQualityMetrics(success ? null : "cache-operation-slow", success ? null : 1, 0, false)
                .withUserExperienceMetrics(cacheTime.toNanos() / 1_000_000, 0, success ? 5 : 3) // Cache should be very
                                                                                                // fast
                .withSystemHealthCorrelation(SystemMetricsCollector.calculateSystemHealthScore(),
                        SystemMetricsCollector.getActiveAlertsCount(),
                        SystemMetricsCollector.calculateResourceAvailability(),
                        SystemMetricsCollector.getSystemLoadAverage())
                .record();
    }

    /**
     * Record card update metrics.
     * 
     * @param cardId the unique card identifier
     * @param updateType the type of update (content, metadata, structure)
     * @param updateTime the time taken for the update
     * @param changesCount the number of changes made
     * @param updateSuccess whether the update was successful
     * @param rollbackRequired whether rollback is required
     */
    public void recordCardUpdate(String cardId, String updateType, Duration updateTime, int changesCount,
            boolean updateSuccess, boolean rollbackRequired) {
        metricsService.recordOperation("card", "update").withSuccess(updateSuccess).withDuration(updateTime.toNanos())
                .withData("cardId", cardId).withData("updateType", updateType).withData("changesCount", changesCount)
                .withData("rollbackRequired", rollbackRequired)
                // Performance Metrics Enhancements
                .withTimingContext(6, changesCount * 100L, changesCount * 50L) // High complexity for updates
                .withResourceUtilization(SystemMetricsCollector.getCurrentMemoryUsage(),
                        SystemMetricsCollector.getCurrentCpuUsage(), SystemMetricsCollector.estimateDiskIOTime())
                .withConcurrencyMetrics(SystemMetricsCollector.getActiveOperationsCount(),
                        SystemMetricsCollector.getCurrentQueueSize(),
                        (int) SystemMetricsCollector.getTotalContentionCount())
                .withQualityMetrics(updateSuccess ? null : "update-failure", updateSuccess ? null : 3, 0,
                        rollbackRequired)
                .withUserExperienceMetrics(updateTime.toMillis(), 0, updateSuccess ? 4 : 2)
                .withSystemHealthCorrelation(SystemMetricsCollector.calculateSystemHealthScore(),
                        SystemMetricsCollector.getActiveAlertsCount(),
                        SystemMetricsCollector.calculateResourceAvailability(),
                        SystemMetricsCollector.getSystemLoadAverage())
                .record();
    }

    /**
     * Record card performance metrics.
     * 
     * @param cardId the unique card identifier
     * @param performanceMetric the type of performance metric
     * @param metricValue the value of the performance metric
     * @param measurementTime the time when the measurement was taken
     * @param performanceScore the overall performance score (1-10)
     */
    public void recordCardPerformance(String cardId, String performanceMetric, double metricValue,
            Duration measurementTime, int performanceScore) {
        boolean success = performanceScore >= 7; // Consider successful if performance score >= 7

        metricsService.recordOperation("card", "performance").withSuccess(success)
                .withDuration(measurementTime.toNanos()).withData("cardId", cardId)
                .withData("performanceMetric", performanceMetric).withData("metricValue", metricValue)
                .withData("performanceScore", performanceScore)
                // Performance Metrics Enhancements
                .withTimingContext(4, 0, 0) // Simple performance measurement
                .withResourceUtilization(SystemMetricsCollector.getCurrentMemoryUsage(),
                        SystemMetricsCollector.getCurrentCpuUsage(), 0)
                .withConcurrencyMetrics(SystemMetricsCollector.getActiveOperationsCount(),
                        SystemMetricsCollector.getCurrentQueueSize(), 0)
                .withQualityMetrics(success ? null : "low-performance", success ? null : 2, 0, false)
                .withUserExperienceMetrics(measurementTime.toMillis(), 0, performanceScore)
                .withSystemHealthCorrelation(SystemMetricsCollector.calculateSystemHealthScore(),
                        SystemMetricsCollector.getActiveAlertsCount(),
                        SystemMetricsCollector.calculateResourceAvailability(),
                        SystemMetricsCollector.getSystemLoadAverage())
                .record();
    }

    /**
     * Record card lifecycle metrics.
     * 
     * @param cardId the unique card identifier
     * @param lifecycleEvent the lifecycle event (created, activated, deactivated, deleted)
     * @param eventTime the time when the event occurred
     * @param cardAge the age of the card in milliseconds
     * @param usageCount the number of times the card has been used
     */
    public void recordCardLifecycle(String cardId, String lifecycleEvent, Duration eventTime, long cardAge,
            int usageCount) {
        boolean success = true; // Lifecycle events are generally successful

        metricsService.recordOperation("card", "lifecycle-" + lifecycleEvent).withSuccess(success)
                .withDuration(eventTime.toNanos()).withData("cardId", cardId).withData("lifecycleEvent", lifecycleEvent)
                .withData("cardAge", cardAge).withData("usageCount", usageCount)
                // Performance Metrics Enhancements
                .withTimingContext(3, 0, 0) // Simple lifecycle event
                .withResourceUtilization(SystemMetricsCollector.getCurrentMemoryUsage(),
                        SystemMetricsCollector.getCurrentCpuUsage(), 0)
                .withConcurrencyMetrics(SystemMetricsCollector.getActiveOperationsCount(),
                        SystemMetricsCollector.getCurrentQueueSize(), 0)
                .withQualityMetrics(null, null, 0, false) // No errors in lifecycle events
                .withUserExperienceMetrics(eventTime.toMillis(), 0, 4) // Good user experience for lifecycle
                .withSystemHealthCorrelation(SystemMetricsCollector.calculateSystemHealthScore(),
                        SystemMetricsCollector.getActiveAlertsCount(),
                        SystemMetricsCollector.calculateResourceAvailability(),
                        SystemMetricsCollector.getSystemLoadAverage())
                .record();
    }
}
