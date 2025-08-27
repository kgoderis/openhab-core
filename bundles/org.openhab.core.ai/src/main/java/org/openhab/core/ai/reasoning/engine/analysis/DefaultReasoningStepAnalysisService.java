package org.openhab.core.ai.reasoning.engine.analysis;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.service.statistics.ReasoningPerformanceStatistics;
import org.openhab.core.ai.reasoning.engine.api.ReasoningStep;
import org.openhab.core.ai.reasoning.engine.api.ReasoningStepStatus;
import org.openhab.core.ai.reasoning.engine.api.ReasoningStepType;
import org.openhab.core.ai.reasoning.engine.persistence.ReasoningStepPersistenceService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of the reasoning step analysis service.
 * 
 * <p>
 * This service provides basic analysis capabilities for reasoning steps including:
 * - Pattern recognition and analysis
 * - Performance analysis and optimization suggestions
 * - Quality assessment algorithms
 * - Step correlation analysis
 * - Reasoning efficiency metrics
 * - Step recommendations
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@Component(service = ReasoningStepAnalysisService.class)
@NonNullByDefault
public class DefaultReasoningStepAnalysisService implements ReasoningStepAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultReasoningStepAnalysisService.class);

    private final ReasoningStepPersistenceService persistenceService;
    private final AtomicLong totalAnalyses = new AtomicLong(0);
    private final AtomicLong successfulAnalyses = new AtomicLong(0);
    private final AtomicLong failedAnalyses = new AtomicLong(0);
    private final AtomicLong totalAnalysisTimeNanos = new AtomicLong(0);
    private final Map<String, Integer> analysisTypeCounter = new HashMap<>();
    private volatile Instant lastAnalysisTime = Instant.now();

    @Activate
    public DefaultReasoningStepAnalysisService(@Reference ReasoningStepPersistenceService persistenceService) {
        this.persistenceService = persistenceService;
        logger.info("DefaultReasoningStepAnalysisService initialized");
    }

    @Override
    public ReasoningPatternAnalysis analyzePatterns(List<ReasoningStep> steps) {
        long startTime = System.nanoTime();

        try {
            logger.debug("Analyzing patterns for {} steps", steps.size());

            // Simple pattern analysis - count step types
            Map<String, Integer> stepTypeFrequency = steps.stream()
                    .collect(Collectors.groupingBy(step -> step.getStepType().name(),
                            Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)));

            // Calculate success rates by type
            Map<String, Double> successRatesByType = new HashMap<>();
            for (ReasoningStepType type : ReasoningStepType.values()) {
                List<ReasoningStep> stepsOfType = steps.stream().filter(step -> step.getStepType() == type).toList();

                if (!stepsOfType.isEmpty()) {
                    long successCount = stepsOfType.stream()
                            .mapToLong(step -> step.getStatus() == ReasoningStepStatus.COMPLETED ? 1 : 0).sum();
                    double successRate = (double) successCount / stepsOfType.size();
                    successRatesByType.put(type.name(), successRate);
                }
            }

            // Generate basic recommendations
            List<String> recommendations = generateBasicRecommendations(stepTypeFrequency, successRatesByType);

            // Create a simple pattern analysis result
            return ReasoningPatternAnalysis.builder().withPatternFrequencies(stepTypeFrequency)
                    .withPatternSuccessRates(successRatesByType).withAnalysisTime(Instant.now())
                    .withTotalStepsAnalyzed(steps.size())
                    .withOverallPatternEffectiveness(calculateOverallEffectiveness(successRatesByType)).build();

        } finally {
            recordAnalysis("pattern", startTime);
        }
    }

    @Override
    public ReasoningPerformanceAnalysis analyzePerformance(List<ReasoningStep> steps) {
        long startTime = System.nanoTime();

        try {
            logger.debug("Analyzing performance for {} steps", steps.size());

            // Calculate average processing time
            double avgProcessingTime = steps.stream().filter(step -> step.getResourceUsage() != null)
                    .mapToLong(step -> step.getResourceUsage().processingTimeMs()).average().orElse(0.0);

            // Calculate tokens per step by model
            Map<String, Long> tokensByModel = steps.stream()
                    .filter(step -> step.getResourceUsage() != null && step.getModelId() != null)
                    .collect(Collectors.groupingBy(ReasoningStep::getModelId,
                            Collectors.summingLong(step -> step.getResourceUsage().getTotalTokens())));

            // Calculate performance by model
            Map<String, Double> performanceByModel = steps.stream()
                    .filter(step -> step.getResourceUsage() != null && step.getModelId() != null)
                    .collect(Collectors.groupingBy(ReasoningStep::getModelId,
                            Collectors.averagingLong(step -> step.getResourceUsage().processingTimeMs())));

            // Generate optimization suggestions
            List<String> optimizationSuggestions = generateBasicOptimizationSuggestions(avgProcessingTime,
                    performanceByModel, performanceByModel);

            // Create a simple performance analysis result
            return ReasoningPerformanceAnalysis.builder().withAverageProcessingTime(avgProcessingTime)
                    .withTokensByModel(tokensByModel).withPerformanceByModel(performanceByModel)
                    .withOptimizationSuggestions(optimizationSuggestions).withAnalysisTime(Instant.now()).build();

        } finally {
            recordAnalysis("performance", startTime);
        }
    }

    @Override
    public ReasoningQualityAssessment assessQuality(List<ReasoningStep> steps) {
        long startTime = System.nanoTime();

        try {
            logger.debug("Assessing quality for {} steps", steps.size());

            // Calculate overall quality score based on validation scores
            double overallQuality = steps.stream().filter(step -> step.getValidationInfo() != null)
                    .mapToDouble(step -> step.getValidationInfo().validationScore()).average().orElse(0.0);

            // Calculate quality by model
            Map<String, Double> qualityByModel = steps.stream()
                    .filter(step -> step.getValidationInfo() != null && step.getModelId() != null)
                    .collect(Collectors.groupingBy(ReasoningStep::getModelId,
                            Collectors.averagingDouble(step -> step.getValidationInfo().validationScore())));

            // Identify quality issues
            List<String> qualityIssues = identifyBasicQualityIssues(steps);

            // Generate improvement suggestions
            List<String> improvementSuggestions = generateBasicQualityImprovements(overallQuality, qualityIssues);

            // Create a simple quality assessment result
            return ReasoningQualityAssessment.builder().withOverallQualityScore(overallQuality)
                    .withQualityByModel(qualityByModel).withQualityIssues(qualityIssues)
                    .withImprovementSuggestions(improvementSuggestions).withAssessmentTime(Instant.now()).build();

        } finally {
            recordAnalysis("quality", startTime);
        }
    }

    @Override
    public ReasoningCorrelationAnalysis analyzeCorrelations(List<ReasoningStep> steps) {
        long startTime = System.nanoTime();

        try {
            logger.debug("Analyzing correlations for {} steps", steps.size());

            // Analyze correlation between step types and success
            Map<String, Double> typeSuccessCorrelation = new HashMap<>();
            for (ReasoningStepType type : ReasoningStepType.values()) {
                List<ReasoningStep> stepsOfType = steps.stream().filter(step -> step.getStepType() == type).toList();

                if (!stepsOfType.isEmpty()) {
                    double successRate = stepsOfType.stream()
                            .mapToDouble(step -> step.getStatus() == ReasoningStepStatus.COMPLETED ? 1.0 : 0.0)
                            .average().orElse(0.0);
                    typeSuccessCorrelation.put(type.name(), successRate);
                }
            }

            // Generate significant correlations
            List<String> significantCorrelations = identifyBasicCorrelations(typeSuccessCorrelation,
                    typeSuccessCorrelation);

            // Create a simple correlation analysis result
            return ReasoningCorrelationAnalysis.builder().withStepTypeCorrelations(typeSuccessCorrelation)
                    .withSignificantCorrelations(significantCorrelations).withAnalysisTime(Instant.now()).build();

        } finally {
            recordAnalysis("correlation", startTime);
        }
    }

    @Override
    public ReasoningEfficiencyMetrics calculateEfficiencyMetrics(List<ReasoningStep> steps) {
        long startTime = System.nanoTime();

        try {
            logger.debug("Calculating efficiency metrics for {} steps", steps.size());

            // Calculate overall efficiency (success rate / average time)
            double successRate = steps.stream()
                    .mapToDouble(step -> step.getStatus() == ReasoningStepStatus.COMPLETED ? 1.0 : 0.0).average()
                    .orElse(0.0);

            double avgTime = steps.stream().filter(step -> step.getResourceUsage() != null)
                    .mapToDouble(step -> step.getResourceUsage().processingTimeMs()).average().orElse(1.0);

            double overallEfficiency = avgTime > 0 ? successRate / (avgTime / 1000.0) : 0.0;

            // Calculate time efficiency
            double timeEfficiency = avgTime > 0 ? successRate / avgTime : 0.0;

            // Calculate cost efficiency
            double avgCost = steps.stream().filter(step -> step.getResourceUsage() != null)
                    .mapToDouble(step -> step.getResourceUsage().costUsd()).average().orElse(0.0);

            double costEfficiency = avgCost > 0 ? successRate / avgCost : 0.0;

            // Calculate token efficiency
            double avgTokens = steps.stream().filter(step -> step.getResourceUsage() != null)
                    .mapToDouble(step -> step.getResourceUsage().getTotalTokens()).average().orElse(0.0);

            double tokenEfficiency = avgTokens > 0 ? successRate / avgTokens : 0.0;

            // Calculate efficiency by model
            Map<String, Double> efficiencyByModel = steps.stream()
                    .filter(step -> step.getModelId() != null && step.getResourceUsage() != null)
                    .collect(Collectors.groupingBy(ReasoningStep::getModelId,
                            Collectors.collectingAndThen(Collectors.toList(), this::calculateModelEfficiency)));

            // Create a simple efficiency metrics result
            return ReasoningEfficiencyMetrics.builder().withOverallEfficiency(overallEfficiency)
                    .withTimeEfficiency(timeEfficiency).withCostEfficiency(costEfficiency)
                    .withTokenEfficiency(tokenEfficiency).withEfficiencyByModel(efficiencyByModel)
                    .withCalculationTime(Instant.now()).build();

        } finally {
            recordAnalysis("efficiency", startTime);
        }
    }

    @Override
    public List<ReasoningRecommendation> generateRecommendations(List<ReasoningStep> steps) {
        long startTime = System.nanoTime();

        try {
            logger.debug("Generating recommendations for {} steps", steps.size());

            List<ReasoningRecommendation> recommendations = new ArrayList<>();

            // Add basic performance recommendations
            analyzeAndAddBasicPerformanceRecommendations(steps, recommendations);

            // Add basic quality recommendations
            analyzeAndAddBasicQualityRecommendations(steps, recommendations);

            // Add basic cost recommendations
            analyzeAndAddBasicCostRecommendations(steps, recommendations);

            return recommendations;

        } finally {
            recordAnalysis("recommendations", startTime);
        }
    }

    @Override
    public ReasoningOptimizationAnalysis analyzeOptimizationOpportunities(List<ReasoningStep> steps) {
        long startTime = System.nanoTime();

        try {
            logger.debug("Analyzing optimization opportunities for {} steps", steps.size());

            // Calculate basic optimization potential
            double optimizationPotential = calculateBasicOptimizationPotential(steps);

            // Identify basic optimization opportunities
            List<String> opportunities = identifyBasicOptimizationOpportunities(steps);

            // Create a simple optimization analysis result
            return ReasoningOptimizationAnalysis.builder().withOverallOptimizationPotential(optimizationPotential)
                    .withOptimizationOpportunities(opportunities).withAnalysisTime(Instant.now()).build();

        } finally {
            recordAnalysis("optimization", startTime);
        }
    }

    @Override
    public ReasoningAnomalyDetection detectAnomalies(List<ReasoningStep> steps) {
        long startTime = System.nanoTime();

        try {
            logger.debug("Detecting anomalies in {} steps", steps.size());

            List<String> anomalyTypes = new ArrayList<>();
            Map<String, Integer> anomaliesByType = new HashMap<>();
            Map<String, Integer> anomaliesByModel = new HashMap<>();
            List<String> suspiciousPatterns = new ArrayList<>();

            // Basic anomaly detection
            detectBasicAnomalies(steps, anomalyTypes, anomaliesByType, anomaliesByModel);

            int totalAnomalies = anomalyTypes.size();
            double anomalyRate = steps.isEmpty() ? 0.0 : (double) totalAnomalies / steps.size();

            // Create a simple anomaly detection result
            return ReasoningAnomalyDetection.builder().withTotalAnomalies(totalAnomalies).withAnomalyTypes(anomalyTypes)
                    .withAnomaliesByType(anomaliesByType).withAnomaliesByModel(anomaliesByModel)
                    .withSuspiciousPatterns(suspiciousPatterns).withAnomalyRate(anomalyRate)
                    .withDetectionTime(Instant.now()).build();

        } finally {
            recordAnalysis("anomaly", startTime);
        }
    }

    @Override
    public ReasoningDependencyAnalysis analyzeDependencies(List<ReasoningStep> steps) {
        long startTime = System.nanoTime();

        try {
            logger.debug("Analyzing dependencies for {} steps", steps.size());

            Map<String, List<String>> dependencies = new HashMap<>();
            Map<String, Integer> dependencyCounts = new HashMap<>();
            List<String> circularDependencies = new ArrayList<>();
            List<String> missingDependencies = new ArrayList<>();

            // Basic dependency analysis
            analyzeBasicDependencies(steps, dependencies, dependencyCounts);

            double averageDependenciesPerStep = dependencies.isEmpty() ? 0.0
                    : dependencies.values().stream().mapToInt(List::size).average().orElse(0.0);

            // Create a simple dependency analysis result
            return ReasoningDependencyAnalysis.builder().withDependencies(dependencies)
                    .withDependencyCounts(dependencyCounts).withCircularDependencies(circularDependencies)
                    .withMissingDependencies(missingDependencies)
                    .withAverageDependenciesPerStep(averageDependenciesPerStep).withAnalysisTime(Instant.now()).build();

        } finally {
            recordAnalysis("dependencies", startTime);
        }
    }

    @Override
    public ReasoningAnalysisReport generateComprehensiveReport(List<ReasoningStep> steps) {
        long startTime = System.nanoTime();

        try {
            logger.debug("Generating comprehensive report for {} steps", steps.size());

            String reportId = "report_" + System.currentTimeMillis();
            String title = "Reasoning Step Analysis Report";
            String summary = generateBasicReportSummary(steps);
            List<String> keyFindings = generateBasicKeyFindings(steps);
            Map<String, Object> metrics = generateBasicReportMetrics(steps);

            // Calculate step type frequency and success rates for recommendations
            Map<String, Integer> stepTypeFrequency = steps.stream()
                    .collect(Collectors.groupingBy(step -> step.getStepType().name(),
                            Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)));

            Map<String, Double> successRatesByType = new HashMap<>();
            for (ReasoningStepType type : ReasoningStepType.values()) {
                List<ReasoningStep> stepsOfType = steps.stream().filter(step -> step.getStepType() == type).toList();
                if (!stepsOfType.isEmpty()) {
                    long successCount = stepsOfType.stream()
                            .mapToLong(step -> step.getStatus() == ReasoningStepStatus.COMPLETED ? 1 : 0).sum();
                    double successRate = (double) successCount / stepsOfType.size();
                    successRatesByType.put(type.name(), successRate);
                }
            }

            List<String> recommendations = generateBasicRecommendations(stepTypeFrequency, successRatesByType);
            List<String> recommendationTexts = createBasicRecommendations(recommendations).stream()
                    .map(rec -> rec.getPatternType() + ": " + rec.getDescription()).toList();

            // Create a simple comprehensive report
            return ReasoningAnalysisReport.builder(reportId, title).withSummary(summary).withKeyFindings(keyFindings)
                    .withMetrics(metrics).withRecommendations(recommendations).withGeneratedAt(Instant.now())
                    .withGeneratedBy("DefaultReasoningStepAnalysisService").build();

        } finally {
            recordAnalysis("comprehensive_report", startTime);
        }
    }

    @Override
    public ReasoningSessionAnalysis analyzeSession(String sessionId) {
        long startTime = System.nanoTime();

        try {
            logger.debug("Analyzing session: {}", sessionId);

            List<ReasoningStep> sessionSteps = persistenceService.getStepsForSession(sessionId);

            int totalSteps = sessionSteps.size();
            double averageStepDuration = sessionSteps.stream().filter(step -> step.getResourceUsage() != null)
                    .mapToDouble(step -> step.getResourceUsage().processingTimeMs()).average().orElse(0.0);

            double successRate = sessionSteps.stream()
                    .mapToDouble(step -> step.getStatus() == ReasoningStepStatus.COMPLETED ? 1.0 : 0.0).average()
                    .orElse(0.0);

            Map<String, Integer> stepsByType = sessionSteps.stream()
                    .collect(Collectors.groupingBy(step -> step.getStepType().name(),
                            Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)));

            List<String> sessionInsights = generateBasicSessionInsights(sessionSteps);

            // Create a simple session analysis result
            return ReasoningSessionAnalysis.builder(sessionId).withTotalSteps(totalSteps)
                    .withAverageStepDuration(averageStepDuration).withSuccessRate(successRate)
                    .withStepsByType(stepsByType).withSessionInsights(sessionInsights).withAnalysisTime(Instant.now())
                    .build();

        } finally {
            recordAnalysis("session", startTime);
        }
    }

    @Override
    public ReasoningTimeAnalysis analyzeTimeRange(Instant startTime, Instant endTime) {
        long analysisStartTime = System.nanoTime();

        try {
            logger.debug("Analyzing time range: {} to {}", startTime, endTime);

            // Basic time range analysis - placeholder implementation
            return ReasoningTimeAnalysis.builder().withAverageProcessingTime(0.0).withMedianProcessingTime(0.0)
                    .withP95ProcessingTime(0.0).withProcessingTimeByModel(Map.of()).withProcessingTimeByType(Map.of())
                    .withTimeInsights(List.of("Time range analysis not fully implemented"))
                    .withAnalysisTime(Instant.now()).build();

        } finally {
            recordAnalysis("time_range", analysisStartTime);
        }
    }

    @Override
    public ReasoningModelAnalysis analyzeModel(String modelId) {
        long startTime = System.nanoTime();

        try {
            logger.debug("Analyzing model: {}", modelId);

            // Basic model analysis - placeholder implementation
            return ReasoningModelAnalysis.builder().withUsageByModel(Map.of(modelId, 0))
                    .withPerformanceByModel(Map.of(modelId, 0.0)).withQualityByModel(Map.of(modelId, 0.0))
                    .withCostByModel(Map.of(modelId, 0.0))
                    .withModelRecommendations(List.of("Model analysis not fully implemented"))
                    .withBestPerformingModel(modelId).withAnalysisTime(Instant.now()).build();

        } finally {
            recordAnalysis("model", startTime);
        }
    }

    @Override
    public ReasoningTypeAnalysis analyzeStepType(ReasoningStepType stepType) {
        long startTime = System.nanoTime();

        try {
            logger.debug("Analyzing step type: {}", stepType);

            // Basic step type analysis - placeholder implementation
            return ReasoningTypeAnalysis.builder().withUsageByType(Map.of(stepType.name(), 0))
                    .withPerformanceByType(Map.of(stepType.name(), 0.0)).withQualityByType(Map.of(stepType.name(), 0.0))
                    .withTypeInsights(List.of("Type analysis not fully implemented")).withMostUsedType(stepType.name())
                    .withBestPerformingType(stepType.name()).withAnalysisTime(Instant.now()).build();

        } finally {
            recordAnalysis("step_type", startTime);
        }
    }

    @Override
    public ReasoningStatusAnalysis analyzeStatus(ReasoningStepStatus status) {
        long startTime = System.nanoTime();

        try {
            logger.debug("Analyzing status: {}", status);

            // Basic status analysis - placeholder implementation
            return ReasoningStatusAnalysis.builder().withStatusDistribution(Map.of(status.name(), 0))
                    .withStatusPerformance(Map.of(status.name(), 0.0))
                    .withStatusInsights(List.of("Status analysis not fully implemented"))
                    .withMostCommonStatus(status.name()).withCompletionRate(0.0).withAnalysisTime(Instant.now())
                    .build();

        } finally {
            recordAnalysis("status", startTime);
        }
    }

    @Override
    public String exportAnalysisResults(Object analysisResults, String format) {
        logger.debug("Exporting analysis results in format: {}", format);

        if ("JSON".equalsIgnoreCase(format)) {
            return analysisResults.toString(); // Basic implementation
        } else if ("CSV".equalsIgnoreCase(format)) {
            return "CSV export not implemented"; // Basic implementation
        } else {
            return "Unsupported format: " + format;
        }
    }

    @Override
    public boolean isHealthy() {
        return persistenceService.isHealthy();
    }

    @Override
    public ReasoningPerformanceStatistics getStatistics() {
        // Create ReasoningPerformanceStatistics from analysis data
        return ReasoningPerformanceStatistics.fromReasoningData(totalAnalyses.get(), // totalStepCount
                0L, // totalStorageSizeBytes
                successfulAnalyses.get(), // activeStepCount
                0L, // archivedStepCount
                0L, // compressedStepCount
                0L, // totalTokensUsed
                0.0, // totalCostUsd
                totalAnalysisTimeNanos.get() / 1_000_000, // totalProcessingTimeMs
                0.8, // averageQualityScore
                0.7, // averageConfidence
                1024.0, // averageStepSizeBytes
                5.0, // averageStepsPerSession
                null, // statusDistribution
                null, // typeDistribution
                null, // modelUsageDistribution
                null, // sessionDistribution
                Duration.ofDays(1) // timeRange
        );
    }

    // Helper methods for the analysis implementations

    private void recordAnalysis(String analysisType, long startTime) {
        long durationNanos = System.nanoTime() - startTime;
        try {
            totalAnalyses.incrementAndGet();
            totalAnalysisTimeNanos.addAndGet(durationNanos);
            successfulAnalyses.incrementAndGet(); // Assume success for now, could be enhanced to track actual failures
            analysisTypeCounter.merge(analysisType, 1, Integer::sum);
            lastAnalysisTime = Instant.now();
        } catch (Exception e) {
            logger.warn("Failed to record reasoning analysis metrics for type {}: {}", analysisType, e.getMessage());
            // Graceful degradation: continue with analysis even if metrics recording fails
        }
    }

    // Basic helper methods for analysis
    private List<String> generateBasicRecommendations(Map<String, Integer> stepTypeFrequency,
            Map<String, Double> successRatesByType) {
        List<String> recommendations = new ArrayList<>();

        // Find step types with low success rates
        successRatesByType.entrySet().stream().filter(entry -> entry.getValue() < 0.7)
                .forEach(entry -> recommendations.add("Improve " + entry.getKey() + " steps (success rate: "
                        + String.format("%.1f%%", entry.getValue() * 100) + ")"));

        return recommendations;
    }

    private List<String> generateBasicOptimizationSuggestions(double avgProcessingTime,
            Map<String, Double> tokensByModel, Map<String, Double> costByModel) {
        List<String> suggestions = new ArrayList<>();

        if (avgProcessingTime > 10000) { // > 10 seconds
            suggestions.add("Consider optimizing step processing time (avg: "
                    + String.format("%.1fs", avgProcessingTime / 1000.0) + ")");
        }

        return suggestions;
    }

    private List<String> identifyBasicQualityIssues(List<ReasoningStep> steps) {
        List<String> issues = new ArrayList<>();

        long lowValidationSteps = steps.stream().filter(step -> step.getValidationInfo() != null)
                .filter(step -> step.getValidationInfo().validationScore() < 0.5).count();

        if (lowValidationSteps > 0) {
            issues.add(lowValidationSteps + " steps have low validation scores (< 0.5)");
        }

        return issues;
    }

    private List<String> generateBasicQualityImprovements(double overallQuality, List<String> qualityIssues) {
        List<String> improvements = new ArrayList<>();

        if (overallQuality < 0.7) {
            improvements.add("Overall quality is below acceptable threshold (70%)");
        }

        return improvements;
    }

    private List<String> identifyBasicCorrelations(Map<String, Double> typeSuccessCorrelation,
            Map<String, Double> modelPerformanceCorrelation) {
        List<String> correlations = new ArrayList<>();

        // Find step types with very high or low success rates
        typeSuccessCorrelation.entrySet().stream().filter(entry -> entry.getValue() > 0.95 || entry.getValue() < 0.5)
                .forEach(entry -> correlations
                        .add(entry.getKey() + " step type has " + (entry.getValue() > 0.95 ? "excellent" : "poor")
                                + " success rate (" + String.format("%.1f%%", entry.getValue() * 100) + ")"));

        return correlations;
    }

    private double calculateModelEfficiency(List<ReasoningStep> modelSteps) {
        double successRate = modelSteps.stream()
                .mapToDouble(step -> step.getStatus() == ReasoningStepStatus.COMPLETED ? 1.0 : 0.0).average()
                .orElse(0.0);

        double avgTime = modelSteps.stream().filter(step -> step.getResourceUsage() != null)
                .mapToDouble(step -> step.getResourceUsage().processingTimeMs()).average().orElse(1.0);

        return avgTime > 0 ? successRate / (avgTime / 1000.0) : 0.0;
    }

    private void analyzeAndAddBasicPerformanceRecommendations(List<ReasoningStep> steps,
            List<ReasoningRecommendation> recommendations) {
        double avgTime = steps.stream().filter(step -> step.getResourceUsage() != null)
                .mapToDouble(step -> step.getResourceUsage().processingTimeMs()).average().orElse(0.0);

        if (avgTime > 10000) { // > 10 seconds
            recommendations.add(ReasoningRecommendation.builder("perf_time_opt", "Optimize Processing Time")
                    .withDescription("Average processing time is " + String.format("%.1fs", avgTime / 1000.0)
                            + ", consider optimizing step execution")
                    .withPriority(ReasoningRecommendationPriority.HIGH)
                    .withCategory(ReasoningRecommendationCategory.PERFORMANCE).withConfidence(0.8).build());
        }
    }

    private void analyzeAndAddBasicQualityRecommendations(List<ReasoningStep> steps,
            List<ReasoningRecommendation> recommendations) {
        double avgQuality = steps.stream().filter(step -> step.getValidationInfo() != null)
                .mapToDouble(step -> step.getValidationInfo().validationScore()).average().orElse(0.0);

        if (avgQuality < 0.7) {
            recommendations.add(ReasoningRecommendation.builder("quality_improve", "Improve Step Quality")
                    .withDescription("Average quality score is " + String.format("%.1f%%", avgQuality * 100)
                            + ", below recommended threshold of 70%")
                    .withPriority(ReasoningRecommendationPriority.MEDIUM)
                    .withCategory(ReasoningRecommendationCategory.QUALITY).withConfidence(0.9).build());
        }
    }

    private void analyzeAndAddBasicCostRecommendations(List<ReasoningStep> steps,
            List<ReasoningRecommendation> recommendations) {
        double avgCost = steps.stream().filter(step -> step.getResourceUsage() != null)
                .mapToDouble(step -> step.getResourceUsage().costUsd()).average().orElse(0.0);

        if (avgCost > 0.02) { // > 2 cents per step
            recommendations.add(ReasoningRecommendation.builder("cost_optimize", "Optimize Cost Efficiency")
                    .withDescription("Average cost per step is " + String.format("%.4f", avgCost)
                            + " USD, consider using more cost-effective models")
                    .withPriority(ReasoningRecommendationPriority.LOW)
                    .withCategory(ReasoningRecommendationCategory.COST).withConfidence(0.7).build());
        }
    }

    private double calculateBasicOptimizationPotential(List<ReasoningStep> steps) {
        // Simple calculation based on success rate and processing time
        double successRate = steps.stream()
                .mapToDouble(step -> step.getStatus() == ReasoningStepStatus.COMPLETED ? 1.0 : 0.0).average()
                .orElse(0.0);

        double avgTime = steps.stream().filter(step -> step.getResourceUsage() != null)
                .mapToDouble(step -> step.getResourceUsage().processingTimeMs()).average().orElse(0.0);

        return Math.max(0.0, 1.0 - (successRate * (avgTime / 10000.0))); // Normalize to 0-1 range
    }

    private List<String> identifyBasicOptimizationOpportunities(List<ReasoningStep> steps) {
        List<String> opportunities = new ArrayList<>();

        double successRate = steps.stream()
                .mapToDouble(step -> step.getStatus() == ReasoningStepStatus.COMPLETED ? 1.0 : 0.0).average()
                .orElse(0.0);

        if (successRate < 0.8) {
            opportunities
                    .add("Improve step success rate (currently " + String.format("%.1f%%", successRate * 100) + ")");
        }

        return opportunities;
    }

    private void detectBasicAnomalies(List<ReasoningStep> steps, List<String> anomalyTypes,
            Map<String, Integer> anomaliesByType, Map<String, Integer> anomaliesByModel) {
        // Basic anomaly detection - look for failed steps
        long failedSteps = steps.stream().filter(step -> step.getStatus() == ReasoningStepStatus.FAILED).count();

        if (failedSteps > 0) {
            anomalyTypes.add("Failed steps detected");
            anomaliesByType.put("FAILED", (int) failedSteps);
        }
    }

    private void analyzeBasicDependencies(List<ReasoningStep> steps, Map<String, List<String>> dependencies,
            Map<String, Integer> dependencyCounts) {
        // Basic dependency analysis - assume sequential dependencies
        for (int i = 0; i < steps.size() - 1; i++) {
            int currentStepId = steps.get(i).getStepNumber();
            int nextStepId = steps.get(i + 1).getStepNumber();

            dependencies.computeIfAbsent(String.valueOf(currentStepId), k -> new ArrayList<>())
                    .add(String.valueOf(nextStepId));
            dependencyCounts.merge(String.valueOf(currentStepId), 1, Integer::sum);
        }
    }

    private String generateBasicReportSummary(List<ReasoningStep> steps) {
        return String.format("Analysis of %d reasoning steps completed. Overall success rate: %.1f%%.", steps.size(),
                steps.stream().mapToDouble(step -> step.getStatus() == ReasoningStepStatus.COMPLETED ? 100.0 : 0.0)
                        .average().orElse(0.0));
    }

    private List<String> generateBasicKeyFindings(List<ReasoningStep> steps) {
        return List.of("Most common step type: " + getMostCommonStepType(steps),
                "Average processing time: " + String.format("%.1fms", getAverageProcessingTime(steps)));
    }

    private Map<String, Object> generateBasicReportMetrics(List<ReasoningStep> steps) {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalSteps", steps.size());
        metrics.put("successRate", getSuccessRate(steps));
        metrics.put("averageProcessingTime", getAverageProcessingTime(steps));
        metrics.put("totalCost", getTotalCost(steps));
        return metrics;
    }

    private List<String> generateBasicSessionInsights(List<ReasoningStep> steps) {
        List<String> insights = new ArrayList<>();

        double successRate = getSuccessRate(steps);
        if (successRate < 0.8) {
            insights.add("Session has below-average success rate (" + String.format("%.1f%%", successRate * 100) + ")");
        }

        return insights;
    }

    private double calculateOverallEffectiveness(Map<String, Double> successRatesByType) {
        if (successRatesByType.isEmpty()) {
            return 0.0;
        }
        return successRatesByType.values().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    private List<PatternRecommendation> createBasicRecommendations(List<String> recommendations) {
        return recommendations
                .stream().map(rec -> new PatternRecommendation("BASIC", rec,
                        PatternRecommendation.RecommendationType.IMPORTANT, 0.7, List.of("Review and implement")))
                .collect(Collectors.toList());
    }

    // Utility methods
    private String getMostCommonStepType(List<ReasoningStep> steps) {
        return steps.stream().collect(Collectors.groupingBy(step -> step.getStepType().name(), Collectors.counting()))
                .entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse("UNKNOWN");
    }

    private double getSuccessRate(List<ReasoningStep> steps) {
        return steps.stream().mapToDouble(step -> step.getStatus() == ReasoningStepStatus.COMPLETED ? 1.0 : 0.0)
                .average().orElse(0.0);
    }

    private double getAverageProcessingTime(List<ReasoningStep> steps) {
        return steps.stream().filter(step -> step.getResourceUsage() != null)
                .mapToDouble(step -> step.getResourceUsage().processingTimeMs()).average().orElse(0.0);
    }

    private double getTotalCost(List<ReasoningStep> steps) {
        return steps.stream().filter(step -> step.getResourceUsage() != null)
                .mapToDouble(step -> step.getResourceUsage().costUsd()).sum();
    }
}
