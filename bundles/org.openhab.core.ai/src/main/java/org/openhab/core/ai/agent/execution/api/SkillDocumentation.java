package org.openhab.core.ai.agent.execution.api;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public interface SkillDocumentation {
    String getDescription();

    String getUsage();

    List<String> getParameters();

    List<String> getExamples();

    String getVersion();
}
