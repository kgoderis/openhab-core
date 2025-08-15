package org.openhab.core.ai.agent.execution.api;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public interface SkillTestResult {
    boolean isSuccessful();

    String getMessage();

    long getExecutionTime();

    Map<String, Object> getTestData();
}
