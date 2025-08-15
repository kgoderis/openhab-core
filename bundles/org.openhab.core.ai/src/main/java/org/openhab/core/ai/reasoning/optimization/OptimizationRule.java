package org.openhab.core.ai.reasoning.optimization;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.reasoning.prompts.AgentModelPrompt;

@FunctionalInterface
@NonNullByDefault
public interface OptimizationRule {
    List<String> optimize(AgentModelPrompt prompt);
}
