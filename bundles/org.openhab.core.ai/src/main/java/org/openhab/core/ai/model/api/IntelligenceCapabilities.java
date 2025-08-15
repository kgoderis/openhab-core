package org.openhab.core.ai.model.api;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public interface IntelligenceCapabilities {
    boolean supportsMultiStepReasoning();

    boolean supportsContextMemory();

    boolean supportsAutonomousBehavior();

    boolean supportsLearningAndAdaptation();

    boolean supportsSafetyConstraints();

    int getMaxReasoningSteps();

    long getMaxContextMemorySize();

    List<String> getSupportedReasoningStrategies();
}
