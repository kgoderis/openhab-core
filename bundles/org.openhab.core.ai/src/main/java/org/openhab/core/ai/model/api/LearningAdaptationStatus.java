package org.openhab.core.ai.model.api;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public interface LearningAdaptationStatus {
    boolean isLearningActive();

    double getLearningRate();

    long getLearningIterations();

    long getLastLearningUpdate();

    Map<String, Double> getLearningMetrics();
}
