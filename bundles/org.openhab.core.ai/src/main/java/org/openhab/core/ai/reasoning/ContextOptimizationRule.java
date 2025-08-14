package org.openhab.core.ai.reasoning;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
@FunctionalInterface
public interface ContextOptimizationRule {
    List<String> optimize(AgentModelContext context);
}


