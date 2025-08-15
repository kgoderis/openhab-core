package org.openhab.core.ai.agent.execution.api;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public interface SkillExample {
    String getName();

    String getDescription();

    Map<String, Object> getParameters();

    Object getExpectedResult();
}
