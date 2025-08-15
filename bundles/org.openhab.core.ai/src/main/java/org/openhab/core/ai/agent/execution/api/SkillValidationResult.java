package org.openhab.core.ai.agent.execution.api;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public interface SkillValidationResult {
    boolean isValid();

    String getMessage();

    List<String> getErrors();
}
