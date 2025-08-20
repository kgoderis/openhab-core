package org.openhab.core.ai.agent.api;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.action.ActionRegistry;
import org.openhab.core.ai.action.api.ActionResult;
import org.openhab.core.ai.common.context.ExecutionContext;
import org.openhab.core.ai.common.context.ReasoningContext;
import org.openhab.core.ai.model.api.ModelClient;
import org.openhab.core.ai.reasoning.api.MultiStepReasoningResult;
import org.openhab.core.ai.reasoning.engine.MultiStepReasoningEngine;

/**
 * Intelligent Agent Interface
 * 
 * <p>
 * This interface extends BaseAutonomousAgent to provide intelligent reasoning capabilities:
 * - Integration with MultiStepReasoningEngine for decision making
 * - Action execution through ActionRegistry
 * - ModelClient integration for LLM-based reasoning
 * - Context-aware action planning and execution
 * - Learning and adaptation capabilities
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public interface IntelligentAgent {

    /**
     * Execute an intelligent action with reasoning
     * 
     * @param actionName the high-level action name
     * @param parameters the action parameters
     * @return the action result after reasoning and execution
     */
    CompletableFuture<ActionResult> executeIntelligentAction(String actionName, Map<String, Object> parameters);

    /**
     * Plan actions using reasoning engine
     * 
     * @param goal the goal to achieve
     * @param context the current context
     * @return list of planned actions
     */
    CompletableFuture<List<ExecutionContext>> planActions(String goal, Map<String, Object> context);

    /**
     * Execute a reasoning session
     * 
     * @param reasoningContext the reasoning context
     * @return the reasoning result
     */
    CompletableFuture<MultiStepReasoningResult> reason(ReasoningContext reasoningContext);

    /**
     * Learn from action results
     * 
     * @param actionName the action name
     * @param parameters the action parameters
     * @param result the action result
     * @param success whether the action was successful
     */
    void learnFromAction(String actionName, Map<String, Object> parameters, ActionResult result, boolean success);

    /**
     * Get the reasoning engine
     * 
     * @return the reasoning engine
     */
    MultiStepReasoningEngine getReasoningEngine();

    /**
     * Get the action registry
     * 
     * @return the action registry
     */
    ActionRegistry getActionRegistry();

    /**
     * Get the model client
     * 
     * @return the model client
     */
    ModelClient getModelClient();

    /**
     * Update agent knowledge and context
     * 
     * @param knowledge the new knowledge to incorporate
     */
    void updateKnowledge(Map<String, Object> knowledge);

    /**
     * Get agent capabilities
     * 
     * @return the agent capabilities
     */
    Map<String, Object> getCapabilities();

    /**
     * Check if agent can handle a specific action
     * 
     * @param actionName the action name
     * @return true if the agent can handle the action
     */
    boolean canHandleAction(String actionName);

    /**
     * Get agent specialization
     * 
     * @return the agent specialization (e.g., "energy", "security", "comfort")
     */
    String getSpecialization();
}
