package org.openhab.core.ai.reasoning.strategies;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.model.ModelParameters;
import org.openhab.core.ai.model.ModelResponse;
import org.openhab.core.ai.model.api.IntelligentToolClient;
import org.openhab.core.ai.reasoning.api.ReasoningContext;
import org.openhab.core.ai.reasoning.engine.api.ReasoningPlanStep;
import org.openhab.core.ai.reasoning.strategies.api.ReasoningStrategy;

/**
 * Sequential reasoning strategy.
 *
 * Delegates to the service's sequential execution helper.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public class SequentialReasoningStrategy implements ReasoningStrategy {
    private final org.openhab.core.ai.reasoning.engine.ReasoningOrchestrationService service;

    public SequentialReasoningStrategy(org.openhab.core.ai.reasoning.engine.ReasoningOrchestrationService service) {
        this.service = service;
    }

    @Override
    public CompletableFuture<List<ModelResponse>> execute(IntelligentToolClient client, List<ReasoningPlanStep> steps,
            ReasoningContext context, ModelParameters params) {
        return service.executeStepsSequential(client, steps, context, params);
    }
}
