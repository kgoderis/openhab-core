package org.openhab.core.ai.reasoning.api;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.model.api.IntelligentToolClient;
import org.openhab.core.ai.model.ModelParameters;
import org.openhab.core.ai.model.ModelResponse;

/**
 * Interface for reasoning strategies.
 *
 * This interface defines the contract for different reasoning execution strategies
 * that can be used by the ReasoningOrchestrationService to execute reasoning steps
 * in various ways (sequential, parallel, adaptive, etc.).
 *
 * Author: Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public interface ReasoningStrategy {

    /**
     * Executes reasoning steps according to the strategy.
     * 
     * @param client The intelligent tool client
     * @param steps The reasoning steps
     * @param context The reasoning context
     * @param params Configuration parameters
     * @return A CompletableFuture containing the results
     */
    CompletableFuture<List<ModelResponse>> execute(IntelligentToolClient client, List<ReasoningPlanStep> steps,
            ReasoningContext context, ModelParameters params);
}
