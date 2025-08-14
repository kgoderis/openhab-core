package org.openhab.core.ai.agent.execution;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public interface SkillExecutionStep {
    String getSkillName();
    java.util.Map<String, Object> getParameters();
    List<String> getDependencies();
    int getOrder();
    boolean isRequired();
}


