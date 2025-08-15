package org.openhab.core.ai.reasoning.strategies.execution;

import java.util.ArrayList;
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
 * Adaptive reasoning strategy.
 *
 * Uses parallel execution for independent steps and sequential execution for
 * dependent steps.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public class AdaptiveReasoningStrategy implements ReasoningStrategy {
    private final org.openhab.core.ai.reasoning.engine.ReasoningOrchestrationService service;

    public AdaptiveReasoningStrategy(org.openhab.core.ai.reasoning.engine.ReasoningOrchestrationService service) {
        this.service = service;
    }

    @Override
    public CompletableFuture<List<ModelResponse>> execute(IntelligentToolClient client, List<ReasoningPlanStep> steps,
            ReasoningContext context, ModelParameters params) {
        List<ReasoningPlanStep> independentSteps = new ArrayList<>();
        List<ReasoningPlanStep> dependentSteps = new ArrayList<>();

        for (ReasoningPlanStep step : steps) {
            if (step.getDependencies().isEmpty()) {
                independentSteps.add(step);
            } else {
                dependentSteps.add(step);
            }
        }

        CompletableFuture<List<ModelResponse>> independentResults = service.executeStepsParallel(client,
                independentSteps, context, params);

        return independentResults.thenCompose(results -> {
            if (dependentSteps.isEmpty()) {
                return CompletableFuture.completedFuture(results);
            }
            return service.executeStepsSequential(client, dependentSteps, context, params)
                    .thenApply(dependentResults -> {
                        results.addAll(dependentResults);
                        return results;
                    });
        });
    }
}
