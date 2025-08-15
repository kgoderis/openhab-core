package org.openhab.core.ai.reasoning.prompts;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
@FunctionalInterface
public interface PromptValidationRule {
    List<String> validate(AgentModelPrompt prompt);
}
