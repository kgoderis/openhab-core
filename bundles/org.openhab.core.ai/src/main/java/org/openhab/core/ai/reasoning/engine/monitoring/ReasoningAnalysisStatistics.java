package org.openhab.core.ai.reasoning.engine.monitoring;

import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.base.AbstractMetrics;

/**
 * Statistics for the reasoning analysis service that extends the unified monitoring framework.
 * 
 * <p>
 * This class provides comprehensive statistics for reasoning analysis operations including
 * analysis counts, performance metrics, and type distribution. It implements both
 * CountsMetrics and LatencyMetrics capability interfaces.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ReasoningAnalysisStatistics extends AbstractMetrics implements CountsMetrics, LatencyMetrics {

    private final Map<String, Integer> analysisTypeCount;
    private final double averageAnalysisQuality;
    private final double averageAnalysisConfidence;
    private final long totalPatternsIdentified;
    private final long totalAnomaliesDetected;
    private final long totalRecommendationsGenerated;
    private final double averageAnalysisTimeMs;
    private final double peakAnalysisThroughput;

    /**
     * Create a new ReasoningAnalysisStatistics instance.
     * 
     * @param id unique identifier for this statistics instance
     * @param timestamp timestamp when statistics were collected
     * @param totalAnalysesPerformed total number of analyses performed
     * @param successfulAnalyses number of successful analyses
     * @param failedAnalyses number of failed analyses
     * @param totalAnalysisTimeNanos total analysis time in nanoseconds
     * @param averageResponseTime average response time in milliseconds
     * @param lastOperationTime timestamp of last operation
     * @param analysisTypeCount count of analyses by type
     * @param averageAnalysisQuality average quality score of analyses
     * @param averageAnalysisConfidence average confidence score of analyses
     * @param totalPatternsIdentified total patterns identified
     * @param totalAnomaliesDetected total anomalies detected
     * @param totalRecommendationsGenerated total recommendations generated
     * @param averageAnalysisTimeMs average analysis time in milliseconds
     * @param peakAnalysisThroughput peak analysis throughput
     * @param data additional monitoring data
     */
    public ReasoningAnalysisStatistics(String id, Instant timestamp, long totalAnalysesPerformed,
            long successfulAnalyses, long failedAnalyses, long totalAnalysisTimeNanos, double averageResponseTime,
            @Nullable Instant lastOperationTime, Map<String, Integer> analysisTypeCount, double averageAnalysisQuality,
            double averageAnalysisConfidence, long totalPatternsIdentified, long totalAnomaliesDetected,
            long totalRecommendationsGenerated, double averageAnalysisTimeMs, double peakAnalysisThroughput,
            @Nullable Map<String, Object> data) {
        super(id, timestamp, "reasoning", "analysis-service", "Reasoning analysis service statistics", data,
                totalAnalysesPerformed, successfulAnalyses, failedAnalyses, totalAnalysisTimeNanos, averageResponseTime,
                lastOperationTime);
        this.analysisTypeCount = Map.copyOf(analysisTypeCount);
        this.averageAnalysisQuality = averageAnalysisQuality;
        this.averageAnalysisConfidence = averageAnalysisConfidence;
        this.totalPatternsIdentified = totalPatternsIdentified;
        this.totalAnomaliesDetected = totalAnomaliesDetected;
        this.totalRecommendationsGenerated = totalRecommendationsGenerated;
        this.averageAnalysisTimeMs = averageAnalysisTimeMs;
        this.peakAnalysisThroughput = peakAnalysisThroughput;
    }

    /**
     * Create a new ReasoningAnalysisStatistics instance with current timestamp.
     * 
     * @param id unique identifier for this statistics instance
     * @param totalAnalysesPerformed total number of analyses performed
     * @param successfulAnalyses number of successful analyses
     * @param failedAnalyses number of failed analyses
     * @param totalAnalysisTimeNanos total analysis time in nanoseconds
     * @param averageResponseTime average response time in milliseconds
     * @param analysisTypeCount count of analyses by type
     * @param averageAnalysisQuality average quality score of analyses
     * @param averageAnalysisConfidence average confidence score of analyses
     * @param totalPatternsIdentified total patterns identified
     * @param totalAnomaliesDetected total anomalies detected
     * @param totalRecommendationsGenerated total recommendations generated
     * @param averageAnalysisTimeMs average analysis time in milliseconds
     * @param peakAnalysisThroughput peak analysis throughput
     */
    public ReasoningAnalysisStatistics(String id, long totalAnalysesPerformed, long successfulAnalyses,
            long failedAnalyses, long totalAnalysisTimeNanos, double averageResponseTime,
            Map<String, Integer> analysisTypeCount, double averageAnalysisQuality, double averageAnalysisConfidence,
            long totalPatternsIdentified, long totalAnomaliesDetected, long totalRecommendationsGenerated,
            double averageAnalysisTimeMs, double peakAnalysisThroughput) {
        this(id, Instant.now(), totalAnalysesPerformed, successfulAnalyses, failedAnalyses, totalAnalysisTimeNanos,
                averageResponseTime, null, analysisTypeCount, averageAnalysisQuality, averageAnalysisConfidence,
                totalPatternsIdentified, totalAnomaliesDetected, totalRecommendationsGenerated, averageAnalysisTimeMs,
                peakAnalysisThroughput, null);
    }

    // CountsMetrics interface implementation
    @Override
    public long total() {
        return getTotalOperations();
    }

    @Override
    public long success() {
        return getSuccessfulOperations();
    }

    @Override
    public long failure() {
        return getFailedOperations();
    }

    // LatencyMetrics interface implementation
    @Override
    public long totalDurationNanos() {
        return getTotalProcessingTime();
    }

    // Analysis-specific getters
    public Map<String, Integer> getAnalysisTypeCount() {
        return analysisTypeCount;
    }

    public double getAverageAnalysisQuality() {
        return averageAnalysisQuality;
    }

    public double getAverageAnalysisConfidence() {
        return averageAnalysisConfidence;
    }

    public long getTotalPatternsIdentified() {
        return totalPatternsIdentified;
    }

    public long getTotalAnomaliesDetected() {
        return totalAnomaliesDetected;
    }

    public long getTotalRecommendationsGenerated() {
        return totalRecommendationsGenerated;
    }

    public double getAverageAnalysisTimeMs() {
        return averageAnalysisTimeMs;
    }

    public double getPeakAnalysisThroughput() {
        return peakAnalysisThroughput;
    }

    /**
     * Get the analysis efficiency score.
     * 
     * @return efficiency score between 0.0 and 1.0
     */
    public double getAnalysisEfficiency() {
        double successRate = successRate();
        double qualityScore = averageAnalysisQuality;
        double confidenceScore = averageAnalysisConfidence;
        double throughputScore = peakAnalysisThroughput > 0 ? Math.min(1.0, peakAnalysisThroughput / 100.0) : 0.0;

        return (successRate * 0.3) + (qualityScore * 0.3) + (confidenceScore * 0.2) + (throughputScore * 0.2);
    }

    /**
     * Get the analysis insights rate.
     * 
     * @return insights rate as a percentage
     */
    public double getAnalysisInsightsRate() {
        long totalInsights = totalPatternsIdentified + totalAnomaliesDetected + totalRecommendationsGenerated;
        return getTotalOperations() > 0 ? (double) totalInsights / getTotalOperations() * 100.0 : 0.0;
    }
}
