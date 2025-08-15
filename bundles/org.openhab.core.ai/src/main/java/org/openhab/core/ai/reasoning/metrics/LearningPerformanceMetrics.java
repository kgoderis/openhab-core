package org.openhab.core.ai.reasoning.metrics;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class LearningPerformanceMetrics {
    private final long totalLearningEvents;
    private final long totalPatternRecognitions;
    private final long totalFeedbackIntegrations;
    private final long totalStrategyAdaptations;
    private final int userPreferenceCount;
    private final int behaviorPatternCount;
    private final int feedbackHistoryCount;
    private final int adaptiveStrategyCount;

    LearningPerformanceMetrics(LearningPerformanceMetricsBuilder builder) {
        this.totalLearningEvents = builder.totalLearningEvents;
        this.totalPatternRecognitions = builder.totalPatternRecognitions;
        this.totalFeedbackIntegrations = builder.totalFeedbackIntegrations;
        this.totalStrategyAdaptations = builder.totalStrategyAdaptations;
        this.userPreferenceCount = builder.userPreferenceCount;
        this.behaviorPatternCount = builder.behaviorPatternCount;
        this.feedbackHistoryCount = builder.feedbackHistoryCount;
        this.adaptiveStrategyCount = builder.adaptiveStrategyCount;
    }

    public long getTotalLearningEvents() {
        return totalLearningEvents;
    }

    public long getTotalPatternRecognitions() {
        return totalPatternRecognitions;
    }

    public long getTotalFeedbackIntegrations() {
        return totalFeedbackIntegrations;
    }

    public long getTotalStrategyAdaptations() {
        return totalStrategyAdaptations;
    }

    public int getUserPreferenceCount() {
        return userPreferenceCount;
    }

    public int getBehaviorPatternCount() {
        return behaviorPatternCount;
    }

    public int getFeedbackHistoryCount() {
        return feedbackHistoryCount;
    }

    public int getAdaptiveStrategyCount() {
        return adaptiveStrategyCount;
    }

    public static LearningPerformanceMetricsBuilder builder() {
        return new LearningPerformanceMetricsBuilder();
    }
}
