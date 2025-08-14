package org.openhab.core.ai.agent.execution;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public interface SkillCompositionResult {
    boolean isSuccess();
    List<SkillCompositionStrategy.SkillExecutionStep> getExecutionPlan();
    String getErrorMessage();
    long getEstimatedExecutionTime();
}


