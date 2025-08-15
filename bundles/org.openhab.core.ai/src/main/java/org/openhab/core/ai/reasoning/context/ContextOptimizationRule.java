package org.openhab.core.ai.reasoning.context;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.agent.api.AgentModelContext;

@NonNullByDefault
@FunctionalInterface
public interface ContextOptimizationRule {
    List<String> optimize(AgentModelContext context);
}
