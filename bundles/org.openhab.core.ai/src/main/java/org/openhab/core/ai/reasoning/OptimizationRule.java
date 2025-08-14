package org.openhab.core.ai.reasoning;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

@FunctionalInterface
@NonNullByDefault
public interface OptimizationRule {
    List<String> optimize(AgentModelPrompt prompt);
}


