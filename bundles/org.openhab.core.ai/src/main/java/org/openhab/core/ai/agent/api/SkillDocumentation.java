package org.openhab.core.ai.agent.api;

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


