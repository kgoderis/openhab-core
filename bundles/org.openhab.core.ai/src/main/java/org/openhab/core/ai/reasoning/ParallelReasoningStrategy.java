package org.openhab.core.ai.reasoning;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.model.api.IntelligentToolClient;
import org.openhab.core.ai.model.ModelParameters;
import org.openhab.core.ai.model.ModelResponse;
import org.openhab.core.ai.reasoning.api.ReasoningContext;
import org.openhab.core.ai.reasoning.api.ReasoningPlanStep;
import org.openhab.core.ai.reasoning.api.ReasoningStrategy;

/**
 * Parallel reasoning strategy.
 *
 * Delegates to the service's parallel execution helper.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public class ParallelReasoningStrategy implements ReasoningStrategy {
    private final ReasoningOrchestrationService service;

    public ParallelReasoningStrategy(ReasoningOrchestrationService service) {
        this.service = service;
    }

    @Override
    public CompletableFuture<List<ModelResponse>> execute(IntelligentToolClient client, List<ReasoningPlanStep> steps,
            ReasoningContext context, ModelParameters params) {
        return service.executeStepsParallel(client, steps, context, params);
    }
}


