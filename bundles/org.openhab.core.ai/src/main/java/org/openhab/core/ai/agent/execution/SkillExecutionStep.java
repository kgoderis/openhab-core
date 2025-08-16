package org.openhab.core.ai.agent.execution;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public interface SkillExecutionStep {
    String getSkillName();

    Map<String, Object> getParameters();

    List<String> getDependencies();

    int getOrder();

    boolean isRequired();
}
