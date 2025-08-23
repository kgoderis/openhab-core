package org.openhab.core.ai.events;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.MetricKeys;
import org.openhab.core.ai.common.monitoring.collector.ExecutionMetricsCollector;
import org.openhab.core.ai.common.monitoring.registry.MonitoringRegistry;
import org.openhab.core.ai.events.monitoring.EventProcessingStatistics;
import org.openhab.core.ai.reasoning.input.AutonomousReasoningInputManager;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Event Processing Analytics and Optimization
 * 
 * <p>
 * This component provides comprehensive analytics and optimization for the event processing pipeline:
 * - Performance metrics collection and analysis
 * - Bottleneck detection and optimization recommendations
 * - Resource utilization monitoring and optimization
 * - Quality metrics and improvement suggestions
 * - Predictive analytics for capacity planning
 * - Real-time optimization and tuning
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 */
@Component(service = EventProcessingAnalytics.class)
@NonNullByDefault
public class EventProcessingAnalytics {

    private static final Logger logger = LoggerFactory.getLogger(EventProcessingAnalytics.class);

    // Configuration
    private static final Duration DEFAULT_METRICS_WINDOW = Duration.ofMinutes(15);
    private static final int DEFAULT_MAX_METRICS_HISTORY = 1000;
    private static final double DEFAULT_PERFORMANCE_THRESHOLD = 0.8;
    private static final double DEFAULT_QUALITY_THRESHOLD = 0.7;

    // Metrics collection
    private final Map<String, PerformanceMetric> performanceMetrics = new ConcurrentHashMap<>();
    private final Map<String, QualityMetric> qualityMetrics = new ConcurrentHashMap<>();
    private final Map<String, ResourceMetric> resourceMetrics = new ConcurrentHashMap<>();
    private final List<AnalyticsEvent> analyticsEvents = new ArrayList<>();

    // Threading
    private final ExecutorService analyticsExecutor = Executors.newFixedThreadPool(2);
    private volatile boolean isRunning = false;

    // Dependencies
    @Reference
    private @Nullable EventSystemIntegration eventSystemIntegration;

    @Reference
    private @Nullable LogIngestionPipeline logIngestionPipeline;

    @Reference
    private @Nullable AutonomousReasoningInputManager inputManager;

    @Reference
    private @Nullable EventLogCorrelationEngine correlationEngine;

    // NEW: Monitoring registry for centralized metrics collection
    @Reference
    private @Nullable MonitoringRegistry monitoringRegistry;

    // Configuration
    private Duration metricsWindow = DEFAULT_METRICS_WINDOW;
    private int maxMetricsHistory = DEFAULT_MAX_METRICS_HISTORY;
    private double performanceThreshold = DEFAULT_PERFORMANCE_THRESHOLD;
    private double qualityThreshold = DEFAULT_QUALITY_THRESHOLD;
    private boolean enablePerformanceMonitoring = true;
    private boolean enableQualityMonitoring = true;
    private boolean enableResourceMonitoring = true;
    private boolean enablePredictiveAnalytics = true;

    @Activate
    public void activate() {
        logger.debug("Event Processing Analytics activated");
        startAnalytics();
    }

    @Deactivate
    public void deactivate() {
        logger.debug("Event Processing Analytics deactivated");
        stopAnalytics();
    }

    /**
     * Start analytics processing
     */
    public void startAnalytics() {
        if (isRunning) {
            logger.warn("Event Processing Analytics is already running");
            return;
        }

        isRunning = true;
        logger.info("Starting Event Processing Analytics");

        try {
            // Start analytics processing
            startAnalyticsProcessing();

            logger.info("Event Processing Analytics started successfully");
        } catch (Exception e) {
            logger.error("Failed to start Event Processing Analytics", e);
            isRunning = false;
        }
    }

    /**
     * Stop analytics processing
     */
    public void stopAnalytics() {
        if (!isRunning) {
            return;
        }

        isRunning = false;
        logger.info("Stopping Event Processing Analytics");

        // Shutdown executor
        analyticsExecutor.shutdown();

        // Clear metrics
        performanceMetrics.clear();
        qualityMetrics.clear();
        resourceMetrics.clear();
        analyticsEvents.clear();

        logger.info("Event Processing Analytics stopped");
    }

    /**
     * Record performance metric using the new monitoring framework
     */
    public void recordPerformanceMetric(String component, String operation, Duration duration, boolean success) {
        if (!enablePerformanceMonitoring) {
            return;
        }

        analyticsExecutor.submit(() -> {
            try {
                // Use centralized monitoring registry
                if (monitoringRegistry != null) {
                    ExecutionMetricsCollector collector = monitoringRegistry
                            .executionCollector(MetricKeys.action(component + "." + operation));
                    collector.recordExecution(success, duration.toNanos());
                }

                // Legacy local metrics for backward compatibility
                String metricId = generateMetricId(component, operation);
                PerformanceMetric metric = performanceMetrics.computeIfAbsent(metricId,
                        k -> new PerformanceMetric(component, operation));

                metric.recordExecution(duration, success);

                // Check for performance issues
                if (metric.getAverageDuration().compareTo(Duration.ofSeconds(1)) > 0) {
                    recordAnalyticsEvent(AnalyticsEventType.PERFORMANCE_WARNING,
                            "Slow performance detected for " + component + "." + operation, metric);
                }

            } catch (Exception e) {
                logger.error("Error recording performance metric", e);
            }
        });
    }

    /**
     * Record quality metric
     */
    public void recordQualityMetric(String component, String operation, double quality, String details) {
        if (!enableQualityMonitoring) {
            return;
        }

        analyticsExecutor.submit(() -> {
            try {
                String metricId = generateMetricId(component, operation);
                QualityMetric metric = qualityMetrics.computeIfAbsent(metricId,
                        k -> new QualityMetric(component, operation));

                metric.recordQuality(quality, details);

                // Check for quality issues
                if (quality < qualityThreshold) {
                    recordAnalyticsEvent(AnalyticsEventType.QUALITY_WARNING,
                            "Low quality detected for " + component + "." + operation, metric);
                }

            } catch (Exception e) {
                logger.error("Error recording quality metric", e);
            }
        });
    }

    /**
     * Record resource metric
     */
    public void recordResourceMetric(String resource, String operation, double utilization, String details) {
        if (!enableResourceMonitoring) {
            return;
        }

        analyticsExecutor.submit(() -> {
            try {
                String metricId = generateMetricId(resource, operation);
                ResourceMetric metric = resourceMetrics.computeIfAbsent(metricId,
                        k -> new ResourceMetric(resource, operation));

                metric.recordUtilization(utilization, details);

                // Check for resource issues
                if (utilization > 0.9) {
                    recordAnalyticsEvent(AnalyticsEventType.RESOURCE_WARNING,
                            "High resource utilization detected for " + resource, metric);
                }

            } catch (Exception e) {
                logger.error("Error recording resource metric", e);
            }
        });
    }

    /**
     * Record error using the new monitoring framework
     */
    public void recordError(String component, String operation, String error, @Nullable Throwable exception) {
        // Record error using monitoring registry
        if (monitoringRegistry != null) {
            ExecutionMetricsCollector collector = monitoringRegistry
                    .executionCollector(MetricKeys.action(component + "." + operation));
            collector.recordExecution(false, 0L);
        }

        analyticsExecutor.submit(() -> {
            try {
                recordAnalyticsEvent(AnalyticsEventType.ERROR, "Error in " + component + "." + operation + ": " + error,
                        null);

                // Record performance impact
                recordPerformanceMetric(component, operation, Duration.ofMillis(0), false);

            } catch (Exception e) {
                logger.error("Error recording error metric", e);
            }
        });
    }

    /**
     * Record warning using the new monitoring framework
     */
    public void recordWarning(String component, String operation, String warning) {
        // Record warning using monitoring registry
        if (monitoringRegistry != null) {
            ExecutionMetricsCollector collector = monitoringRegistry
                    .executionCollector(MetricKeys.action(component + "." + operation));
            // Warning doesn't affect success/failure count, just record for tracking
        }

        analyticsExecutor.submit(() -> {
            try {
                recordAnalyticsEvent(AnalyticsEventType.WARNING,
                        "Warning in " + component + "." + operation + ": " + warning, null);

            } catch (Exception e) {
                logger.error("Error recording warning metric", e);
            }
        });
    }

    /**
     * Get performance analytics using the new monitoring framework
     */
    public CompletableFuture<PerformanceAnalytics> getPerformanceAnalytics() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<PerformanceMetric> metrics = new ArrayList<>(performanceMetrics.values());

                // Calculate overall performance
                double overallPerformance = calculateOverallPerformance(metrics);

                // Identify bottlenecks
                List<PerformanceBottleneck> bottlenecks = identifyBottlenecks(metrics);

                // Generate optimization recommendations
                List<OptimizationRecommendation> recommendations = generateOptimizationRecommendations(metrics,
                        bottlenecks);

                // Get statistics from monitoring registry
                long totalEvents = 0;
                long totalProcessingTime = 0;

                if (monitoringRegistry != null) {
                    ExecutionMetricsCollector collector = monitoringRegistry
                            .executionCollector(MetricKeys.events("processing"));
                    var snapshot = collector.snapshot();
                    totalEvents = snapshot.total();
                    totalProcessingTime = snapshot.totalDurationNanos() / 1_000_000; // Convert to milliseconds
                }

                return new PerformanceAnalytics(overallPerformance, metrics, bottlenecks, recommendations, totalEvents,
                        totalProcessingTime, Instant.now());

            } catch (Exception e) {
                logger.error("Error generating performance analytics", e);
                return new PerformanceAnalytics(0.0, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), 0, 0,
                        Instant.now());
            }
        }, analyticsExecutor);
    }

    /**
     * Get quality analytics using the new monitoring framework
     */
    public CompletableFuture<QualityAnalytics> getQualityAnalytics() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<QualityMetric> metrics = new ArrayList<>(qualityMetrics.values());

                // Calculate overall quality
                double overallQuality = calculateOverallQuality(metrics);

                // Identify quality issues
                List<QualityIssue> qualityIssues = identifyQualityIssues(metrics);

                // Generate quality improvement recommendations
                List<QualityImprovementRecommendation> recommendations = generateQualityImprovementRecommendations(
                        metrics, qualityIssues);

                // Get error/warning counts from monitoring registry
                long totalErrors = 0;
                long totalWarnings = 0;

                if (monitoringRegistry != null) {
                    ExecutionMetricsCollector collector = monitoringRegistry
                            .executionCollector(MetricKeys.events("processing"));
                    var snapshot = collector.snapshot();
                    totalErrors = snapshot.failure();
                    // Warnings are not tracked in the basic metrics, would need separate tracking
                }

                return new QualityAnalytics(overallQuality, metrics, qualityIssues, recommendations, totalErrors,
                        totalWarnings, Instant.now());

            } catch (Exception e) {
                logger.error("Error generating quality analytics", e);
                return new QualityAnalytics(0.0, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), 0, 0,
                        Instant.now());
            }
        }, analyticsExecutor);
    }

    /**
     * Get resource analytics
     */
    public CompletableFuture<ResourceAnalytics> getResourceAnalytics() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<ResourceMetric> metrics = new ArrayList<>(resourceMetrics.values());

                // Calculate overall resource utilization
                double overallUtilization = calculateOverallUtilization(metrics);

                // Identify resource issues
                List<ResourceIssue> resourceIssues = identifyResourceIssues(metrics);

                // Generate resource optimization recommendations
                List<ResourceOptimizationRecommendation> recommendations = generateResourceOptimizationRecommendations(
                        metrics, resourceIssues);

                return new ResourceAnalytics(overallUtilization, metrics, resourceIssues, recommendations,
                        Instant.now());

            } catch (Exception e) {
                logger.error("Error generating resource analytics", e);
                return new ResourceAnalytics(0.0, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                        Instant.now());
            }
        }, analyticsExecutor);
    }

    /**
     * Get comprehensive analytics report
     */
    public CompletableFuture<ComprehensiveAnalyticsReport> getComprehensiveAnalyticsReport() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                PerformanceAnalytics performanceAnalytics = getPerformanceAnalytics().get();
                QualityAnalytics qualityAnalytics = getQualityAnalytics().get();
                ResourceAnalytics resourceAnalytics = getResourceAnalytics().get();

                // Generate predictive analytics
                PredictiveAnalytics predictiveAnalytics = generatePredictiveAnalytics();

                // Generate system health score
                double systemHealthScore = calculateSystemHealthScore(performanceAnalytics, qualityAnalytics,
                        resourceAnalytics);

                return new ComprehensiveAnalyticsReport(systemHealthScore, performanceAnalytics, qualityAnalytics,
                        resourceAnalytics, predictiveAnalytics, analyticsEvents, Instant.now());

            } catch (Exception e) {
                logger.error("Error generating comprehensive analytics report", e);
                return new ComprehensiveAnalyticsReport(0.0, null, null, null, null, new ArrayList<>(), Instant.now());
            }
        }, analyticsExecutor);
    }

    /**
     * Get event processing statistics using the new monitoring framework
     */
    public EventProcessingStatistics getStatistics() {
        if (monitoringRegistry != null) {
            // Get statistics from centralized registry
            ExecutionMetricsCollector collector = monitoringRegistry
                    .executionCollector(MetricKeys.events("processing"));
            var snapshot = collector.snapshot();

            return new EventProcessingStatistics("event-processing-stats", Instant.now(), snapshot.total(),
                    snapshot.success(), snapshot.failure(), 0, // filteredEvents
                    0, // enrichedEvents
                    0, // routedEvents
                    0, // persistedEvents
                    0, // reasoningTriggers
                    true, // eventProcessingEnabled
                    true, // eventPersistenceEnabled
                    true, // reasoningIntegrationEnabled
                    Duration.ofHours(24), // eventRetentionPeriod
                    false, // replayInProgress
                    null // data
            );
        }

        // Fallback to legacy calculation
        return calculateLegacyStatistics();
    }

    /**
     * Legacy statistics calculation for backward compatibility
     */
    private EventProcessingStatistics calculateLegacyStatistics() {
        long totalEvents = 0;
        long successfulEvents = 0;
        long failedEvents = 0;

        for (PerformanceMetric metric : performanceMetrics.values()) {
            totalEvents += metric.getSuccesses().size();
            successfulEvents += metric.getSuccesses().stream().filter(s -> s).count();
            failedEvents += metric.getSuccesses().stream().filter(s -> !s).count();
        }

        return new EventProcessingStatistics("event-processing-stats", Instant.now(), totalEvents, successfulEvents,
                failedEvents, 0, // filteredEvents
                0, // enrichedEvents
                0, // routedEvents
                0, // persistedEvents
                0, // reasoningTriggers
                true, // eventProcessingEnabled
                true, // eventPersistenceEnabled
                true, // reasoningIntegrationEnabled
                Duration.ofHours(24), // eventRetentionPeriod
                false, // replayInProgress
                null // data
        );
    }

    /**
     * Calculate overall performance score
     */
    private double calculateOverallPerformance(List<PerformanceMetric> metrics) {
        if (metrics.isEmpty()) {
            return 0.0;
        }

        double totalScore = 0.0;
        int count = 0;

        for (PerformanceMetric metric : metrics) {
            double successRate = metric.getSuccessRate();
            double avgDuration = metric.getAverageDuration().toMillis();

            // Normalize duration (shorter is better)
            double normalizedDuration = Math.max(0.0, 1.0 - (avgDuration / 1000.0)); // 1 second baseline

            double score = (successRate + normalizedDuration) / 2.0;
            totalScore += score;
            count++;
        }

        return count > 0 ? totalScore / count : 0.0;
    }

    /**
     * Calculate overall quality score
     */
    private double calculateOverallQuality(List<QualityMetric> metrics) {
        if (metrics.isEmpty()) {
            return 0.0;
        }

        double totalQuality = 0.0;
        int count = 0;

        for (QualityMetric metric : metrics) {
            totalQuality += metric.getAverageQuality();
            count++;
        }

        return count > 0 ? totalQuality / count : 0.0;
    }

    /**
     * Calculate overall resource utilization
     */
    private double calculateOverallUtilization(List<ResourceMetric> metrics) {
        if (metrics.isEmpty()) {
            return 0.0;
        }

        double totalUtilization = 0.0;
        int count = 0;

        for (ResourceMetric metric : metrics) {
            totalUtilization += metric.getAverageUtilization();
            count++;
        }

        return count > 0 ? totalUtilization / count : 0.0;
    }

    /**
     * Identify performance bottlenecks
     */
    private List<PerformanceBottleneck> identifyBottlenecks(List<PerformanceMetric> metrics) {
        List<PerformanceBottleneck> bottlenecks = new ArrayList<>();

        for (PerformanceMetric metric : metrics) {
            if (metric.getAverageDuration().compareTo(Duration.ofSeconds(1)) > 0 || metric.getSuccessRate() < 0.8) {

                bottlenecks.add(new PerformanceBottleneck(metric.getComponent(), metric.getOperation(),
                        metric.getAverageDuration(), metric.getSuccessRate(),
                        generateBottleneckRecommendation(metric)));
            }
        }

        return bottlenecks;
    }

    /**
     * Identify quality issues
     */
    private List<QualityIssue> identifyQualityIssues(List<QualityMetric> metrics) {
        List<QualityIssue> issues = new ArrayList<>();

        for (QualityMetric metric : metrics) {
            if (metric.getAverageQuality() < qualityThreshold) {
                issues.add(new QualityIssue(metric.getComponent(), metric.getOperation(), metric.getAverageQuality(),
                        metric.getLastDetails(), generateQualityIssueRecommendation(metric)));
            }
        }

        return issues;
    }

    /**
     * Identify resource issues
     */
    private List<ResourceIssue> identifyResourceIssues(List<ResourceMetric> metrics) {
        List<ResourceIssue> issues = new ArrayList<>();

        for (ResourceMetric metric : metrics) {
            if (metric.getAverageUtilization() > 0.9) {
                issues.add(
                        new ResourceIssue(metric.getResource(), metric.getOperation(), metric.getAverageUtilization(),
                                metric.getLastDetails(), generateResourceIssueRecommendation(metric)));
            }
        }

        return issues;
    }

    /**
     * Generate optimization recommendations
     */
    private List<OptimizationRecommendation> generateOptimizationRecommendations(List<PerformanceMetric> metrics,
            List<PerformanceBottleneck> bottlenecks) {
        List<OptimizationRecommendation> recommendations = new ArrayList<>();

        for (PerformanceBottleneck bottleneck : bottlenecks) {
            recommendations.add(new OptimizationRecommendation(bottleneck.getComponent(), bottleneck.getOperation(),
                    OptimizationType.PERFORMANCE, bottleneck.getRecommendation(),
                    calculateOptimizationPriority(bottleneck)));
        }

        return recommendations;
    }

    /**
     * Generate quality improvement recommendations
     */
    private List<QualityImprovementRecommendation> generateQualityImprovementRecommendations(
            List<QualityMetric> metrics, List<QualityIssue> issues) {
        List<QualityImprovementRecommendation> recommendations = new ArrayList<>();

        for (QualityIssue issue : issues) {
            recommendations.add(new QualityImprovementRecommendation(issue.getComponent(), issue.getOperation(),
                    issue.getRecommendation(), calculateQualityImprovementPriority(issue)));
        }

        return recommendations;
    }

    /**
     * Generate resource optimization recommendations
     */
    private List<ResourceOptimizationRecommendation> generateResourceOptimizationRecommendations(
            List<ResourceMetric> metrics, List<ResourceIssue> issues) {
        List<ResourceOptimizationRecommendation> recommendations = new ArrayList<>();

        for (ResourceIssue issue : issues) {
            recommendations.add(new ResourceOptimizationRecommendation(issue.getResource(), issue.getOperation(),
                    issue.getRecommendation(), calculateResourceOptimizationPriority(issue)));
        }

        return recommendations;
    }

    /**
     * Generate predictive analytics
     */
    private PredictiveAnalytics generatePredictiveAnalytics() {
        if (!enablePredictiveAnalytics) {
            return new PredictiveAnalytics(0.0, 0.0, 0.0, new ArrayList<>(), Instant.now());
        }

        try {
            // Simple predictive analytics based on current trends
            double predictedLoad = calculatePredictedLoad();
            double predictedPerformance = calculatePredictedPerformance();
            double predictedQuality = calculatePredictedQuality();

            List<PredictionAlert> alerts = generatePredictionAlerts(predictedLoad, predictedPerformance,
                    predictedQuality);

            return new PredictiveAnalytics(predictedLoad, predictedPerformance, predictedQuality, alerts,
                    Instant.now());

        } catch (Exception e) {
            logger.error("Error generating predictive analytics", e);
            return new PredictiveAnalytics(0.0, 0.0, 0.0, new ArrayList<>(), Instant.now());
        }
    }

    /**
     * Calculate system health score
     */
    private double calculateSystemHealthScore(PerformanceAnalytics performanceAnalytics,
            QualityAnalytics qualityAnalytics, ResourceAnalytics resourceAnalytics) {
        double performanceScore = performanceAnalytics.getOverallPerformance();
        double qualityScore = qualityAnalytics.getOverallQuality();
        double resourceScore = 1.0 - resourceAnalytics.getOverallUtilization(); // Lower utilization is better

        // Weighted average
        return (performanceScore * 0.4 + qualityScore * 0.4 + resourceScore * 0.2);
    }

    /**
     * Record analytics event
     */
    private void recordAnalyticsEvent(AnalyticsEventType type, String message, @Nullable Object data) {
        AnalyticsEvent event = new AnalyticsEvent(generateEventId(), type, message, data, Instant.now());

        analyticsEvents.add(event);

        // Limit events history
        if (analyticsEvents.size() > maxMetricsHistory) {
            analyticsEvents.remove(0);
        }
    }

    /**
     * Start analytics processing
     */
    private void startAnalyticsProcessing() {
        // Start background analytics tasks
        analyticsExecutor.submit(this::processAnalytics);
    }

    /**
     * Process analytics
     */
    private void processAnalytics() {
        while (isRunning) {
            try {
                // Process analytics periodically
                Thread.sleep(60000); // Check every minute
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                logger.error("Error in analytics processing", e);
            }
        }
    }

    // Helper methods for recommendations and calculations
    private String generateMetricId(String component, String operation) {
        return component + "." + operation;
    }

    private String generateEventId() {
        return "analytics_event_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId();
    }

    private String generateBottleneckRecommendation(PerformanceMetric metric) {
        return "Consider optimizing " + metric.getComponent() + "." + metric.getOperation() + " (avg duration: "
                + metric.getAverageDuration().toMillis() + "ms)";
    }

    private String generateQualityIssueRecommendation(QualityMetric metric) {
        return "Improve quality for " + metric.getComponent() + "." + metric.getOperation() + " (current quality: "
                + String.format("%.2f", metric.getAverageQuality()) + ")";
    }

    private String generateResourceIssueRecommendation(ResourceMetric metric) {
        return "Optimize resource usage for " + metric.getResource() + "." + metric.getOperation()
                + " (current utilization: " + String.format("%.2f", metric.getAverageUtilization()) + ")";
    }

    private double calculateOptimizationPriority(PerformanceBottleneck bottleneck) {
        return (1.0 - bottleneck.getSuccessRate()) * 0.6 + (bottleneck.getAverageDuration().toMillis() / 1000.0) * 0.4;
    }

    private double calculateQualityImprovementPriority(QualityIssue issue) {
        return 1.0 - issue.getQuality();
    }

    private double calculateResourceOptimizationPriority(ResourceIssue issue) {
        return issue.getUtilization();
    }

    private double calculatePredictedLoad() {
        // Simple prediction based on current trends
        if (monitoringRegistry != null) {
            ExecutionMetricsCollector collector = monitoringRegistry
                    .executionCollector(MetricKeys.events("processing"));
            var snapshot = collector.snapshot();
            return Math.min(1.0, snapshot.total() / 1000.0);
        }
        return 0.0;
    }

    private double calculatePredictedPerformance() {
        // Simple prediction based on current performance
        if (monitoringRegistry != null) {
            ExecutionMetricsCollector collector = monitoringRegistry
                    .executionCollector(MetricKeys.events("processing"));
            var snapshot = collector.snapshot();
            return Math.max(0.0, 1.0 - (snapshot.failure() / Math.max(1, snapshot.total())));
        }
        return 0.0;
    }

    private double calculatePredictedQuality() {
        // Simple prediction based on current quality
        if (monitoringRegistry != null) {
            ExecutionMetricsCollector collector = monitoringRegistry
                    .executionCollector(MetricKeys.events("processing"));
            var snapshot = collector.snapshot();
            // Warnings are not tracked in basic metrics, use success rate as quality proxy
            return snapshot.successRate();
        }
        return 0.0;
    }

    private List<PredictionAlert> generatePredictionAlerts(double predictedLoad, double predictedPerformance,
            double predictedQuality) {
        List<PredictionAlert> alerts = new ArrayList<>();

        if (predictedLoad > 0.8) {
            alerts.add(new PredictionAlert("HIGH_LOAD", "Predicted high load", predictedLoad));
        }

        if (predictedPerformance < 0.7) {
            alerts.add(new PredictionAlert("LOW_PERFORMANCE", "Predicted low performance", predictedPerformance));
        }

        if (predictedQuality < 0.7) {
            alerts.add(new PredictionAlert("LOW_QUALITY", "Predicted low quality", predictedQuality));
        }

        return alerts;
    }

    // Configuration methods
    public void setMetricsWindow(Duration metricsWindow) {
        this.metricsWindow = metricsWindow;
    }

    public void setMaxMetricsHistory(int maxMetricsHistory) {
        this.maxMetricsHistory = maxMetricsHistory;
    }

    public void setPerformanceThreshold(double performanceThreshold) {
        this.performanceThreshold = performanceThreshold;
    }

    public void setQualityThreshold(double qualityThreshold) {
        this.qualityThreshold = qualityThreshold;
    }

    public void setEnablePerformanceMonitoring(boolean enablePerformanceMonitoring) {
        this.enablePerformanceMonitoring = enablePerformanceMonitoring;
    }

    public void setEnableQualityMonitoring(boolean enableQualityMonitoring) {
        this.enableQualityMonitoring = enableQualityMonitoring;
    }

    public void setEnableResourceMonitoring(boolean enableResourceMonitoring) {
        this.enableResourceMonitoring = enableResourceMonitoring;
    }

    public void setEnablePredictiveAnalytics(boolean enablePredictiveAnalytics) {
        this.enablePredictiveAnalytics = enablePredictiveAnalytics;
    }

    // Inner classes removed in favor of top-level types in org.openhab.core.ai.events
}
