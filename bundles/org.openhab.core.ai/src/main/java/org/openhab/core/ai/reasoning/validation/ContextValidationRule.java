package org.openhab.core.ai.reasoning.validation;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.agent.api.AgentModelContext;

@NonNullByDefault
@FunctionalInterface
public interface ContextValidationRule {
    List<String> validate(AgentModelContext context);
}
