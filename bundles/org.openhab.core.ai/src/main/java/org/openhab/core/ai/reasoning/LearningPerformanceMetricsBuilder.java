package org.openhab.core.ai.reasoning;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class LearningPerformanceMetricsBuilder {
    long totalLearningEvents;
    long totalPatternRecognitions;
    long totalFeedbackIntegrations;
    long totalStrategyAdaptations;
    int userPreferenceCount;
    int behaviorPatternCount;
    int feedbackHistoryCount;
    int adaptiveStrategyCount;

    public LearningPerformanceMetricsBuilder totalLearningEvents(long v) { this.totalLearningEvents = v; return this; }
    public LearningPerformanceMetricsBuilder totalPatternRecognitions(long v) { this.totalPatternRecognitions = v; return this; }
    public LearningPerformanceMetricsBuilder totalFeedbackIntegrations(long v) { this.totalFeedbackIntegrations = v; return this; }
    public LearningPerformanceMetricsBuilder totalStrategyAdaptations(long v) { this.totalStrategyAdaptations = v; return this; }
    public LearningPerformanceMetricsBuilder userPreferenceCount(int v) { this.userPreferenceCount = v; return this; }
    public LearningPerformanceMetricsBuilder behaviorPatternCount(int v) { this.behaviorPatternCount = v; return this; }
    public LearningPerformanceMetricsBuilder feedbackHistoryCount(int v) { this.feedbackHistoryCount = v; return this; }
    public LearningPerformanceMetricsBuilder adaptiveStrategyCount(int v) { this.adaptiveStrategyCount = v; return this; }
    public LearningPerformanceMetrics build() { return new LearningPerformanceMetrics(this); }
}


