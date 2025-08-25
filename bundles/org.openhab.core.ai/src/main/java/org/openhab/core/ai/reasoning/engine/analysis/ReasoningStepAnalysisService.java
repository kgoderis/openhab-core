package org.openhab.core.ai.reasoning.engine.analysis;

import java.time.Instant;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.reasoning.engine.api.ReasoningStep;
import org.openhab.core.ai.reasoning.engine.api.ReasoningStepStatus;
import org.openhab.core.ai.reasoning.engine.api.ReasoningStepType;

/**
 * Service for analyzing reasoning steps and providing practical insights.
 * 
 * <p>
 * This service provides practical analysis capabilities for reasoning steps including:
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
@NonNullByDefault
public interface ReasoningStepAnalysisService {

    /**
     * Analyze reasoning patterns in a set of steps.
     * 
     * @param steps the reasoning steps to analyze
     * @return analysis results with identified patterns
     */
    ReasoningPatternAnalysis analyzePatterns(List<ReasoningStep> steps);

    /**
     * Analyze performance characteristics of reasoning steps.
     * 
     * @param steps the reasoning steps to analyze
     * @return performance analysis with optimization suggestions
     */
    ReasoningPerformanceAnalysis analyzePerformance(List<ReasoningStep> steps);

    /**
     * Assess the quality of reasoning steps.
     * 
     * @param steps the reasoning steps to assess
     * @return quality assessment results
     */
    ReasoningQualityAssessment assessQuality(List<ReasoningStep> steps);

    /**
     * Analyze correlations between reasoning steps.
     * 
     * @param steps the reasoning steps to analyze
     * @return correlation analysis results
     */
    ReasoningCorrelationAnalysis analyzeCorrelations(List<ReasoningStep> steps);

    /**
     * Calculate reasoning efficiency metrics.
     * 
     * @param steps the reasoning steps to analyze
     * @return efficiency metrics
     */
    ReasoningEfficiencyMetrics calculateEfficiencyMetrics(List<ReasoningStep> steps);

    /**
     * Generate recommendations for improving reasoning.
     * 
     * @param steps the reasoning steps to analyze
     * @return list of recommendations
     */
    List<ReasoningRecommendation> generateRecommendations(List<ReasoningStep> steps);

    /**
     * Analyze reasoning step sequences for optimization opportunities.
     * 
     * @param steps the reasoning steps to analyze
     * @return optimization analysis results
     */
    ReasoningOptimizationAnalysis analyzeOptimizationOpportunities(List<ReasoningStep> steps);

    /**
     * Detect anomalies in reasoning steps.
     * 
     * @param steps the reasoning steps to analyze
     * @return anomaly detection results
     */
    ReasoningAnomalyDetection detectAnomalies(List<ReasoningStep> steps);

    /**
     * Analyze reasoning step dependencies and relationships.
     * 
     * @param steps the reasoning steps to analyze
     * @return dependency analysis results
     */
    ReasoningDependencyAnalysis analyzeDependencies(List<ReasoningStep> steps);

    /**
     * Generate a comprehensive analysis report.
     * 
     * @param steps the reasoning steps to analyze
     * @return comprehensive analysis report
     */
    ReasoningAnalysisReport generateComprehensiveReport(List<ReasoningStep> steps);

    /**
     * Analyze reasoning steps for a specific session.
     * 
     * @param sessionId the session ID to analyze
     * @return session-specific analysis results
     */
    ReasoningSessionAnalysis analyzeSession(String sessionId);

    /**
     * Analyze reasoning steps within a time range.
     * 
     * @param startTime the start time (inclusive)
     * @param endTime the end time (inclusive)
     * @return time-based analysis results
     */
    ReasoningTimeAnalysis analyzeTimeRange(Instant startTime, Instant endTime);

    /**
     * Analyze reasoning steps by model.
     * 
     * @param modelId the model ID to analyze
     * @return model-specific analysis results
     */
    ReasoningModelAnalysis analyzeModel(String modelId);

    /**
     * Analyze reasoning steps by type.
     * 
     * @param stepType the step type to analyze
     * @return type-specific analysis results
     */
    ReasoningTypeAnalysis analyzeStepType(ReasoningStepType stepType);

    /**
     * Analyze reasoning steps by status.
     * 
     * @param status the status to analyze
     * @return status-specific analysis results
     */
    ReasoningStatusAnalysis analyzeStatus(ReasoningStepStatus status);

    /**
     * Export analysis results to various formats.
     * 
     * @param analysisResults the analysis results to export
     * @param format the export format (JSON, CSV, etc.)
     * @return exported data as string
     */
    String exportAnalysisResults(Object analysisResults, String format);

    /**
     * Check if the analysis service is healthy.
     * 
     * @return true if the service is healthy
     */
    boolean isHealthy();

    /**
     * Get analysis service statistics.
     * 
     * @return service statistics
     */
    Object getStatistics();
}
