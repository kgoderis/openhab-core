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
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.reasoning.AutonomousReasoningInputManager;
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

    // Performance monitoring
    private final AtomicLong totalEventsProcessed = new AtomicLong(0);
    private final AtomicLong totalProcessingTime = new AtomicLong(0);
    private final AtomicLong totalErrors = new AtomicLong(0);
    private final AtomicLong totalWarnings = new AtomicLong(0);

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
     * Record performance metric
     */
    public void recordPerformanceMetric(String component, String operation, Duration duration, boolean success) {
        if (!enablePerformanceMonitoring) {
            return;
        }

        analyticsExecutor.submit(() -> {
            try {
                String metricId = generateMetricId(component, operation);
                PerformanceMetric metric = performanceMetrics.computeIfAbsent(metricId,
                        k -> new PerformanceMetric(component, operation));

                metric.recordExecution(duration, success);
                totalEventsProcessed.incrementAndGet();
                totalProcessingTime.addAndGet(duration.toMillis());

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
     * Record error
     */
    public void recordError(String component, String operation, String error, @Nullable Throwable exception) {
        totalErrors.incrementAndGet();

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
     * Record warning
     */
    public void recordWarning(String component, String operation, String warning) {
        totalWarnings.incrementAndGet();

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
     * Get performance analytics
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

                return new PerformanceAnalytics(overallPerformance, metrics, bottlenecks, recommendations,
                        totalEventsProcessed.get(), totalProcessingTime.get(), Instant.now());

            } catch (Exception e) {
                logger.error("Error generating performance analytics", e);
                return new PerformanceAnalytics(0.0, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                        totalEventsProcessed.get(), totalProcessingTime.get(), Instant.now());
            }
        }, analyticsExecutor);
    }

    /**
     * Get quality analytics
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

                return new QualityAnalytics(overallQuality, metrics, qualityIssues, recommendations, totalErrors.get(),
                        totalWarnings.get(), Instant.now());

            } catch (Exception e) {
                logger.error("Error generating quality analytics", e);
                return new QualityAnalytics(0.0, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                        totalErrors.get(), totalWarnings.get(), Instant.now());
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
        return Math.min(1.0, totalEventsProcessed.get() / 1000.0);
    }

    private double calculatePredictedPerformance() {
        // Simple prediction based on current performance
        return Math.max(0.0, 1.0 - (totalErrors.get() / Math.max(1, totalEventsProcessed.get())));
    }

    private double calculatePredictedQuality() {
        // Simple prediction based on current quality
        return Math.max(0.0, 1.0 - (totalWarnings.get() / Math.max(1, totalEventsProcessed.get())));
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

    // Data classes
    public static class PerformanceMetric {
        private final String component;
        private final String operation;
        private final List<Duration> durations = new ArrayList<>();
        private final List<Boolean> successes = new ArrayList<>();
        private final List<Instant> timestamps = new ArrayList<>();

        public PerformanceMetric(String component, String operation) {
            this.component = component;
            this.operation = operation;
        }

        public void recordExecution(Duration duration, boolean success) {
            durations.add(duration);
            successes.add(success);
            timestamps.add(Instant.now());

            // Keep only recent metrics
            if (durations.size() > 100) {
                durations.remove(0);
                successes.remove(0);
                timestamps.remove(0);
            }
        }

        public String getComponent() {
            return component;
        }

        public String getOperation() {
            return operation;
        }

        public Duration getAverageDuration() {
            if (durations.isEmpty()) {
                return Duration.ZERO;
            }
            long totalMillis = durations.stream().mapToLong(Duration::toMillis).sum();
            return Duration.ofMillis(totalMillis / durations.size());
        }

        public double getSuccessRate() {
            if (successes.isEmpty()) {
                return 1.0;
            }
            long successCount = successes.stream().filter(s -> s).count();
            return (double) successCount / successes.size();
        }

        public List<Duration> getDurations() {
            return new ArrayList<>(durations);
        }

        public List<Boolean> getSuccesses() {
            return new ArrayList<>(successes);
        }

        public List<Instant> getTimestamps() {
            return new ArrayList<>(timestamps);
        }
    }

    public static class QualityMetric {
        private final String component;
        private final String operation;
        private final List<Double> qualities = new ArrayList<>();
        private final List<String> details = new ArrayList<>();
        private final List<Instant> timestamps = new ArrayList<>();

        public QualityMetric(String component, String operation) {
            this.component = component;
            this.operation = operation;
        }

        public void recordQuality(double quality, String details) {
            qualities.add(quality);
            this.details.add(details);
            timestamps.add(Instant.now());

            // Keep only recent metrics
            if (qualities.size() > 100) {
                qualities.remove(0);
                this.details.remove(0);
                timestamps.remove(0);
            }
        }

        public String getComponent() {
            return component;
        }

        public String getOperation() {
            return operation;
        }

        public double getAverageQuality() {
            if (qualities.isEmpty()) {
                return 1.0;
            }
            return qualities.stream().mapToDouble(Double::doubleValue).average().orElse(1.0);
        }

        public String getLastDetails() {
            return details.isEmpty() ? "" : details.get(details.size() - 1);
        }

        public List<Double> getQualities() {
            return new ArrayList<>(qualities);
        }

        public List<String> getDetails() {
            return new ArrayList<>(details);
        }

        public List<Instant> getTimestamps() {
            return new ArrayList<>(timestamps);
        }
    }

    public static class ResourceMetric {
        private final String resource;
        private final String operation;
        private final List<Double> utilizations = new ArrayList<>();
        private final List<String> details = new ArrayList<>();
        private final List<Instant> timestamps = new ArrayList<>();

        public ResourceMetric(String resource, String operation) {
            this.resource = resource;
            this.operation = operation;
        }

        public void recordUtilization(double utilization, String details) {
            utilizations.add(utilization);
            this.details.add(details);
            timestamps.add(Instant.now());

            // Keep only recent metrics
            if (utilizations.size() > 100) {
                utilizations.remove(0);
                this.details.remove(0);
                timestamps.remove(0);
            }
        }

        public String getResource() {
            return resource;
        }

        public String getOperation() {
            return operation;
        }

        public double getAverageUtilization() {
            if (utilizations.isEmpty()) {
                return 0.0;
            }
            return utilizations.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        }

        public String getLastDetails() {
            return details.isEmpty() ? "" : details.get(details.size() - 1);
        }

        public List<Double> getUtilizations() {
            return new ArrayList<>(utilizations);
        }

        public List<String> getDetails() {
            return new ArrayList<>(details);
        }

        public List<Instant> getTimestamps() {
            return new ArrayList<>(timestamps);
        }
    }

    public static class AnalyticsEvent {
        private final String id;
        private final AnalyticsEventType type;
        private final String message;
        private final @Nullable Object data;
        private final Instant timestamp;

        public AnalyticsEvent(String id, AnalyticsEventType type, String message, @Nullable Object data,
                Instant timestamp) {
            this.id = id;
            this.type = type;
            this.message = message;
            this.data = data;
            this.timestamp = timestamp;
        }

        public String getId() {
            return id;
        }

        public AnalyticsEventType getType() {
            return type;
        }

        public String getMessage() {
            return message;
        }

        public @Nullable Object getData() {
            return data;
        }

        public Instant getTimestamp() {
            return timestamp;
        }
    }

    public enum AnalyticsEventType {
        PERFORMANCE_WARNING,
        QUALITY_WARNING,
        RESOURCE_WARNING,
        ERROR,
        WARNING,
        INFO
    }

    // Analytics result classes
    public static class PerformanceAnalytics {
        private final double overallPerformance;
        private final List<PerformanceMetric> metrics;
        private final List<PerformanceBottleneck> bottlenecks;
        private final List<OptimizationRecommendation> recommendations;
        private final long totalEventsProcessed;
        private final long totalProcessingTime;
        private final Instant timestamp;

        public PerformanceAnalytics(double overallPerformance, List<PerformanceMetric> metrics,
                List<PerformanceBottleneck> bottlenecks, List<OptimizationRecommendation> recommendations,
                long totalEventsProcessed, long totalProcessingTime, Instant timestamp) {
            this.overallPerformance = overallPerformance;
            this.metrics = metrics;
            this.bottlenecks = bottlenecks;
            this.recommendations = recommendations;
            this.totalEventsProcessed = totalEventsProcessed;
            this.totalProcessingTime = totalProcessingTime;
            this.timestamp = timestamp;
        }

        public double getOverallPerformance() {
            return overallPerformance;
        }

        public List<PerformanceMetric> getMetrics() {
            return metrics;
        }

        public List<PerformanceBottleneck> getBottlenecks() {
            return bottlenecks;
        }

        public List<OptimizationRecommendation> getRecommendations() {
            return recommendations;
        }

        public long getTotalEventsProcessed() {
            return totalEventsProcessed;
        }

        public long getTotalProcessingTime() {
            return totalProcessingTime;
        }

        public Instant getTimestamp() {
            return timestamp;
        }
    }

    public static class QualityAnalytics {
        private final double overallQuality;
        private final List<QualityMetric> metrics;
        private final List<QualityIssue> issues;
        private final List<QualityImprovementRecommendation> recommendations;
        private final long totalErrors;
        private final long totalWarnings;
        private final Instant timestamp;

        public QualityAnalytics(double overallQuality, List<QualityMetric> metrics, List<QualityIssue> issues,
                List<QualityImprovementRecommendation> recommendations, long totalErrors, long totalWarnings,
                Instant timestamp) {
            this.overallQuality = overallQuality;
            this.metrics = metrics;
            this.issues = issues;
            this.recommendations = recommendations;
            this.totalErrors = totalErrors;
            this.totalWarnings = totalWarnings;
            this.timestamp = timestamp;
        }

        public double getOverallQuality() {
            return overallQuality;
        }

        public List<QualityMetric> getMetrics() {
            return metrics;
        }

        public List<QualityIssue> getIssues() {
            return issues;
        }

        public List<QualityImprovementRecommendation> getRecommendations() {
            return recommendations;
        }

        public long getTotalErrors() {
            return totalErrors;
        }

        public long getTotalWarnings() {
            return totalWarnings;
        }

        public Instant getTimestamp() {
            return timestamp;
        }
    }

    public static class ResourceAnalytics {
        private final double overallUtilization;
        private final List<ResourceMetric> metrics;
        private final List<ResourceIssue> issues;
        private final List<ResourceOptimizationRecommendation> recommendations;
        private final Instant timestamp;

        public ResourceAnalytics(double overallUtilization, List<ResourceMetric> metrics, List<ResourceIssue> issues,
                List<ResourceOptimizationRecommendation> recommendations, Instant timestamp) {
            this.overallUtilization = overallUtilization;
            this.metrics = metrics;
            this.issues = issues;
            this.recommendations = recommendations;
            this.timestamp = timestamp;
        }

        public double getOverallUtilization() {
            return overallUtilization;
        }

        public List<ResourceMetric> getMetrics() {
            return metrics;
        }

        public List<ResourceIssue> getIssues() {
            return issues;
        }

        public List<ResourceOptimizationRecommendation> getRecommendations() {
            return recommendations;
        }

        public Instant getTimestamp() {
            return timestamp;
        }
    }

    public static class PredictiveAnalytics {
        private final double predictedLoad;
        private final double predictedPerformance;
        private final double predictedQuality;
        private final List<PredictionAlert> alerts;
        private final Instant timestamp;

        public PredictiveAnalytics(double predictedLoad, double predictedPerformance, double predictedQuality,
                List<PredictionAlert> alerts, Instant timestamp) {
            this.predictedLoad = predictedLoad;
            this.predictedPerformance = predictedPerformance;
            this.predictedQuality = predictedQuality;
            this.alerts = alerts;
            this.timestamp = timestamp;
        }

        public double getPredictedLoad() {
            return predictedLoad;
        }

        public double getPredictedPerformance() {
            return predictedPerformance;
        }

        public double getPredictedQuality() {
            return predictedQuality;
        }

        public List<PredictionAlert> getAlerts() {
            return alerts;
        }

        public Instant getTimestamp() {
            return timestamp;
        }
    }

    public static class ComprehensiveAnalyticsReport {
        private final double systemHealthScore;
        private final @Nullable PerformanceAnalytics performanceAnalytics;
        private final @Nullable QualityAnalytics qualityAnalytics;
        private final @Nullable ResourceAnalytics resourceAnalytics;
        private final @Nullable PredictiveAnalytics predictiveAnalytics;
        private final List<AnalyticsEvent> events;
        private final Instant timestamp;

        public ComprehensiveAnalyticsReport(double systemHealthScore,
                @Nullable PerformanceAnalytics performanceAnalytics, @Nullable QualityAnalytics qualityAnalytics,
                @Nullable ResourceAnalytics resourceAnalytics, @Nullable PredictiveAnalytics predictiveAnalytics,
                List<AnalyticsEvent> events, Instant timestamp) {
            this.systemHealthScore = systemHealthScore;
            this.performanceAnalytics = performanceAnalytics;
            this.qualityAnalytics = qualityAnalytics;
            this.resourceAnalytics = resourceAnalytics;
            this.predictiveAnalytics = predictiveAnalytics;
            this.events = events;
            this.timestamp = timestamp;
        }

        public double getSystemHealthScore() {
            return systemHealthScore;
        }

        public @Nullable PerformanceAnalytics getPerformanceAnalytics() {
            return performanceAnalytics;
        }

        public @Nullable QualityAnalytics getQualityAnalytics() {
            return qualityAnalytics;
        }

        public @Nullable ResourceAnalytics getResourceAnalytics() {
            return resourceAnalytics;
        }

        public @Nullable PredictiveAnalytics getPredictiveAnalytics() {
            return predictiveAnalytics;
        }

        public List<AnalyticsEvent> getEvents() {
            return events;
        }

        public Instant getTimestamp() {
            return timestamp;
        }
    }

    // Issue and recommendation classes
    public static class PerformanceBottleneck {
        private final String component;
        private final String operation;
        private final Duration averageDuration;
        private final double successRate;
        private final String recommendation;

        public PerformanceBottleneck(String component, String operation, Duration averageDuration, double successRate,
                String recommendation) {
            this.component = component;
            this.operation = operation;
            this.averageDuration = averageDuration;
            this.successRate = successRate;
            this.recommendation = recommendation;
        }

        public String getComponent() {
            return component;
        }

        public String getOperation() {
            return operation;
        }

        public Duration getAverageDuration() {
            return averageDuration;
        }

        public double getSuccessRate() {
            return successRate;
        }

        public String getRecommendation() {
            return recommendation;
        }
    }

    public static class QualityIssue {
        private final String component;
        private final String operation;
        private final double quality;
        private final String details;
        private final String recommendation;

        public QualityIssue(String component, String operation, double quality, String details, String recommendation) {
            this.component = component;
            this.operation = operation;
            this.quality = quality;
            this.details = details;
            this.recommendation = recommendation;
        }

        public String getComponent() {
            return component;
        }

        public String getOperation() {
            return operation;
        }

        public double getQuality() {
            return quality;
        }

        public String getDetails() {
            return details;
        }

        public String getRecommendation() {
            return recommendation;
        }
    }

    public static class ResourceIssue {
        private final String resource;
        private final String operation;
        private final double utilization;
        private final String details;
        private final String recommendation;

        public ResourceIssue(String resource, String operation, double utilization, String details,
                String recommendation) {
            this.resource = resource;
            this.operation = operation;
            this.utilization = utilization;
            this.details = details;
            this.recommendation = recommendation;
        }

        public String getResource() {
            return resource;
        }

        public String getOperation() {
            return operation;
        }

        public double getUtilization() {
            return utilization;
        }

        public String getDetails() {
            return details;
        }

        public String getRecommendation() {
            return recommendation;
        }
    }

    // Extracted: org.openhab.core.ai.events.OptimizationRecommendation

    public static class QualityImprovementRecommendation {
        private final String component;
        private final String operation;
        private final String recommendation;
        private final double priority;

        public QualityImprovementRecommendation(String component, String operation, String recommendation,
                double priority) {
            this.component = component;
            this.operation = operation;
            this.recommendation = recommendation;
            this.priority = priority;
        }

        public String getComponent() {
            return component;
        }

        public String getOperation() {
            return operation;
        }

        public String getRecommendation() {
            return recommendation;
        }

        public double getPriority() {
            return priority;
        }
    }

    public static class ResourceOptimizationRecommendation {
        private final String resource;
        private final String operation;
        private final String recommendation;
        private final double priority;

        public ResourceOptimizationRecommendation(String resource, String operation, String recommendation,
                double priority) {
            this.resource = resource;
            this.operation = operation;
            this.recommendation = recommendation;
            this.priority = priority;
        }

        public String getResource() {
            return resource;
        }

        public String getOperation() {
            return operation;
        }

        public String getRecommendation() {
            return recommendation;
        }

        public double getPriority() {
            return priority;
        }
    }

    public static class PredictionAlert {
        private final String type;
        private final String message;
        private final double value;

        public PredictionAlert(String type, String message, double value) {
            this.type = type;
            this.message = message;
            this.value = value;
        }

        public String getType() {
            return type;
        }

        public String getMessage() {
            return message;
        }

        public double getValue() {
            return value;
        }
    }

    // Extracted: org.openhab.core.ai.events.OptimizationType
}
