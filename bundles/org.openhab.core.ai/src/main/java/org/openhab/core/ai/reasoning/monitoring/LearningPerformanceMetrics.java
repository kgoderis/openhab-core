package org.openhab.core.ai.reasoning.monitoring;

import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.base.AbstractMetrics;

/**
 * Consolidated learning performance metrics that extends the unified monitoring framework.
 * 
 * <p>
 * This class provides comprehensive performance metrics for learning operations including
 * pattern recognition, feedback integration, strategy adaptations, and learning efficiency.
 * It implements CountsMetrics and LatencyMetrics capability interfaces.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class LearningPerformanceMetrics extends AbstractMetrics implements CountsMetrics, LatencyMetrics {

    private final long totalPatternRecognitions;
    private final long totalFeedbackIntegrations;
    private final long totalStrategyAdaptations;
    private final int userPreferenceCount;
    private final int behaviorPatternCount;
    private final int feedbackHistoryCount;
    private final int adaptiveStrategyCount;

    /**
     * Create a new LearningPerformanceMetrics instance.
     * 
     * @param id unique identifier for this metrics instance
     * @param timestamp timestamp when metrics were collected
     * @param totalOperations total number of learning events
     * @param successfulOperations number of successful learning events
     * @param failedOperations number of failed learning events
     * @param totalProcessingTime total processing time in nanoseconds
     * @param averageResponseTime average response time in milliseconds
     * @param lastOperationTime timestamp of last operation
     * @param totalPatternRecognitions total number of pattern recognitions
     * @param totalFeedbackIntegrations total number of feedback integrations
     * @param totalStrategyAdaptations total number of strategy adaptations
     * @param userPreferenceCount number of user preferences
     * @param behaviorPatternCount number of behavior patterns
     * @param feedbackHistoryCount number of feedback history entries
     * @param adaptiveStrategyCount number of adaptive strategies
     * @param data additional monitoring data
     */
    public LearningPerformanceMetrics(String id, Instant timestamp, long totalOperations, long successfulOperations,
            long failedOperations, long totalProcessingTime, double averageResponseTime,
            @Nullable Instant lastOperationTime, long totalPatternRecognitions, long totalFeedbackIntegrations,
            long totalStrategyAdaptations, int userPreferenceCount, int behaviorPatternCount, int feedbackHistoryCount,
            int adaptiveStrategyCount, @Nullable Map<String, Object> data) {
        super(id, timestamp, "reasoning", "learning-performance", "Learning performance metrics", data, totalOperations,
                successfulOperations, failedOperations, totalProcessingTime, averageResponseTime, lastOperationTime);
        this.totalPatternRecognitions = totalPatternRecognitions;
        this.totalFeedbackIntegrations = totalFeedbackIntegrations;
        this.totalStrategyAdaptations = totalStrategyAdaptations;
        this.userPreferenceCount = userPreferenceCount;
        this.behaviorPatternCount = behaviorPatternCount;
        this.feedbackHistoryCount = feedbackHistoryCount;
        this.adaptiveStrategyCount = adaptiveStrategyCount;
    }

    /**
     * Create a new LearningPerformanceMetrics instance with current timestamp.
     * 
     * @param id unique identifier for this metrics instance
     * @param totalOperations total number of learning events
     * @param successfulOperations number of successful learning events
     * @param failedOperations number of failed learning events
     * @param totalProcessingTime total processing time in nanoseconds
     * @param averageResponseTime average response time in milliseconds
     * @param totalPatternRecognitions total number of pattern recognitions
     * @param totalFeedbackIntegrations total number of feedback integrations
     * @param totalStrategyAdaptations total number of strategy adaptations
     * @param userPreferenceCount number of user preferences
     * @param behaviorPatternCount number of behavior patterns
     * @param feedbackHistoryCount number of feedback history entries
     * @param adaptiveStrategyCount number of adaptive strategies
     */
    public LearningPerformanceMetrics(String id, long totalOperations, long successfulOperations, long failedOperations,
            long totalProcessingTime, double averageResponseTime, long totalPatternRecognitions,
            long totalFeedbackIntegrations, long totalStrategyAdaptations, int userPreferenceCount,
            int behaviorPatternCount, int feedbackHistoryCount, int adaptiveStrategyCount) {
        this(id, Instant.now(), totalOperations, successfulOperations, failedOperations, totalProcessingTime,
                averageResponseTime, null, totalPatternRecognitions, totalFeedbackIntegrations,
                totalStrategyAdaptations, userPreferenceCount, behaviorPatternCount, feedbackHistoryCount,
                adaptiveStrategyCount, null);
    }

    /**
     * Get the total number of pattern recognitions.
     * 
     * @return total pattern recognitions
     */
    public long getTotalPatternRecognitions() {
        return totalPatternRecognitions;
    }

    /**
     * Get the total number of feedback integrations.
     * 
     * @return total feedback integrations
     */
    public long getTotalFeedbackIntegrations() {
        return totalFeedbackIntegrations;
    }

    /**
     * Get the total number of strategy adaptations.
     * 
     * @return total strategy adaptations
     */
    public long getTotalStrategyAdaptations() {
        return totalStrategyAdaptations;
    }

    /**
     * Get the number of user preferences.
     * 
     * @return user preference count
     */
    public int getUserPreferenceCount() {
        return userPreferenceCount;
    }

    /**
     * Get the number of behavior patterns.
     * 
     * @return behavior pattern count
     */
    public int getBehaviorPatternCount() {
        return behaviorPatternCount;
    }

    /**
     * Get the number of feedback history entries.
     * 
     * @return feedback history count
     */
    public int getFeedbackHistoryCount() {
        return feedbackHistoryCount;
    }

    /**
     * Get the number of adaptive strategies.
     * 
     * @return adaptive strategy count
     */
    public int getAdaptiveStrategyCount() {
        return adaptiveStrategyCount;
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

    /**
     * Get the learning efficiency score.
     * 
     * @return learning efficiency score between 0.0 and 1.0
     */
    public double getLearningEfficiency() {
        double successRate = successRate();
        double patternRecognitionRate = total() > 0 ? (double) totalPatternRecognitions / total() : 0.0;
        double feedbackIntegrationRate = total() > 0 ? (double) totalFeedbackIntegrations / total() : 0.0;
        double strategyAdaptationRate = total() > 0 ? (double) totalStrategyAdaptations / total() : 0.0;
        double latencyScore = getAverageResponseTime() < 2000 ? 1.0
                : getAverageResponseTime() < 5000 ? 0.8 : getAverageResponseTime() < 10000 ? 0.6 : 0.4;

        return (successRate * 0.3) + (patternRecognitionRate * 0.2) + (feedbackIntegrationRate * 0.2)
                + (strategyAdaptationRate * 0.2) + (latencyScore * 0.1);
    }

    /**
     * Get the pattern recognition success rate.
     * 
     * @return pattern recognition success rate as a percentage
     */
    public double getPatternRecognitionRate() {
        return total() > 0 ? (double) totalPatternRecognitions / total() * 100.0 : 0.0;
    }

    /**
     * Get the feedback integration rate.
     * 
     * @return feedback integration rate as a percentage
     */
    public double getFeedbackIntegrationRate() {
        return total() > 0 ? (double) totalFeedbackIntegrations / total() * 100.0 : 0.0;
    }

    /**
     * Get the strategy adaptation rate.
     * 
     * @return strategy adaptation rate as a percentage
     */
    public double getStrategyAdaptationRate() {
        return total() > 0 ? (double) totalStrategyAdaptations / total() * 100.0 : 0.0;
    }

    /**
     * Check if learning is performing well (high success rate and good adaptation).
     * 
     * @return true if learning is performing well
     */
    public boolean isPerformingWell() {
        return successRate() > 0.8 && getPatternRecognitionRate() > 50.0 && getStrategyAdaptationRate() > 30.0;
    }

    /**
     * Check if the system is actively learning (high pattern recognition and feedback integration).
     * 
     * @return true if the system is actively learning
     */
    public boolean isActivelyLearning() {
        return getPatternRecognitionRate() > 70.0 && getFeedbackIntegrationRate() > 60.0;
    }
}
