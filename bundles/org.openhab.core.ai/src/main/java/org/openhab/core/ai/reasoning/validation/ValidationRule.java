package org.openhab.core.ai.reasoning.validation;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Validation Rule interface extracted from AgentModelDecisionValidator.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
@FunctionalInterface
public interface ValidationRule {
    List<String> validate(org.openhab.core.ai.reasoning.results.DecisionResult decision,
            org.openhab.core.ai.reasoning.decision.DecisionContext context);
}
