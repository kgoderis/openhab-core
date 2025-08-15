package org.openhab.core.ai.model.api;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public interface ReasoningPerformanceMetrics {
    double getAverageReasoningTime();

    long getTotalReasoningOperations();

    double getSuccessRate();

    double getAverageReasoningSteps();

    Map<String, Double> getStrategyMetrics();
}
