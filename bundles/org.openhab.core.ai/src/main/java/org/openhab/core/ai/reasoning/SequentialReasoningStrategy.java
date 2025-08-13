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
 * Sequential reasoning strategy.
 *
 * Delegates to the service's sequential execution helper.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public class SequentialReasoningStrategy implements ReasoningStrategy {
    private final ReasoningOrchestrationService service;

    public SequentialReasoningStrategy(ReasoningOrchestrationService service) {
        this.service = service;
    }

    @Override
    public CompletableFuture<List<ModelResponse>> execute(IntelligentToolClient client, List<ReasoningPlanStep> steps,
            ReasoningContext context, ModelParameters params) {
        return service.executeStepsSequential(client, steps, context, params);
    }
}


