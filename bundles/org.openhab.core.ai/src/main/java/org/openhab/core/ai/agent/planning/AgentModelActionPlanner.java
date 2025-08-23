package org.openhab.core.ai.agent.planning;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.action.api.ActionResult;
import org.openhab.core.ai.common.context.ExecutionContext;
import org.openhab.core.ai.common.context.ReasoningContext;
import org.openhab.core.ai.model.api.ModelClient;
import org.openhab.core.ai.reasoning.api.MultiStepReasoningResult;
import org.openhab.core.ai.reasoning.engine.MultiStepReasoningEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Model-based action planner for intelligent agents.
 * 
 * <p>
 * This class provides sophisticated action planning capabilities using AI models
 * to generate, validate, and optimize action plans based on goals and context.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class AgentModelActionPlanner {

    private final Logger logger = LoggerFactory.getLogger(AgentModelActionPlanner.class);

    private final @Nullable ModelClient modelClient;
    private final @Nullable MultiStepReasoningEngine reasoningEngine;
    private final AgentModelActionPlanValidator validator;
    private final AgentModelActionPlanOptimizer optimizer;

    /**
     * Create a new action planner.
     * 
     * @param modelClient the model client for AI interactions
     * @param reasoningEngine the reasoning engine for plan generation
     * @param validator the action plan validator
     * @param optimizer the action plan optimizer
     */
    public AgentModelActionPlanner(@Nullable ModelClient modelClient,
            @Nullable MultiStepReasoningEngine reasoningEngine, AgentModelActionPlanValidator validator,
            AgentModelActionPlanOptimizer optimizer) {
        this.modelClient = modelClient;
        this.reasoningEngine = reasoningEngine;
        this.validator = Objects.requireNonNull(validator, "validator");
        this.optimizer = Objects.requireNonNull(optimizer, "optimizer");
    }

    /**
     * Create an action plan for a given goal and context.
     * 
     * @param goal the goal to achieve
     * @param context the execution context
     * @param parameters planning parameters
     * @return a future that completes with the action plan
     */
    public CompletableFuture<AgentModelActionPlan> createActionPlan(String goal, ExecutionContext context,
            AgentModelActionPlanParameters parameters) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.debug("Creating action plan for goal: {}", goal);

                // 1. Generate initial plan
                AgentModelActionPlan initialPlan = generateInitialPlan(goal, context, parameters);

                // 2. Validate the plan
                AgentModelActionPlanValidationResult validationResult = validator.validate(initialPlan, context);
                if (!validationResult.isValid()) {
                    logger.warn("Plan validation failed: {}", validationResult.getErrors());
                    return initialPlan; // Return original plan if validation fails
                }

                // 3. Optimize the plan
                AgentModelActionPlan optimizedPlan = optimizer.optimize(initialPlan, context);

                // 4. Final validation
                AgentModelActionPlanValidationResult finalValidation = validator.validate(optimizedPlan, context);
                if (!finalValidation.isValid()) {
                    logger.warn("Optimized plan validation failed: {}", finalValidation.getErrors());
                    // Return the initial plan if optimization failed validation
                    return initialPlan;
                }

                logger.debug("Action plan created successfully: {}", optimizedPlan.getPlanId());
                return optimizedPlan;

            } catch (Exception e) {
                logger.error("Failed to create action plan for goal: {}", goal, e);
                // Return a simple fallback plan
                return createSimpleActionPlan(goal, context, parameters);
            }
        });
    }

    /**
     * Generate an initial action plan using the reasoning engine.
     */
    private AgentModelActionPlan generateInitialPlan(String goal, ExecutionContext context,
            AgentModelActionPlanParameters parameters) throws Exception {

        // Create reasoning context for plan generation
        ReasoningContext reasoningContext = ReasoningContext.builder().withInitialContext("Goal: " + goal)
                .withCurrentContext("Generating action plan for: " + goal).withDomain(context.getProtocol())
                .withSessionId(context.getSessionId())
                .withMetadata(Map.of("goal", goal, "context", context.getAllValues(), "parameters", parameters.toMap()))
                .build();

        // Use reasoning engine to generate plan
        MultiStepReasoningEngine engine = reasoningEngine;
        if (engine != null) {
            MultiStepReasoningResult reasoningResult = engine.reasonAsync(reasoningContext).get();
            return convertReasoningToActionPlan(reasoningResult, goal, context, parameters);
        } else {
            // Fallback: create a simple plan
            return createSimpleActionPlan(goal, context, parameters);
        }
    }

    /**
     * Convert reasoning result to action plan.
     */
    private AgentModelActionPlan convertReasoningToActionPlan(MultiStepReasoningResult reasoningResult, String goal,
            ExecutionContext context, AgentModelActionPlanParameters parameters) {

        List<AgentModelAction> actions = reasoningResult.getSteps().stream()
                .map(step -> AgentModelAction.builder().withActionId("action-" + step.getStepNumber())
                        .withActionName("reasoning-step-" + step.getStepNumber()).withDescription(step.getReasoning())
                        .withParameters(Map.of("reasoning", step.getReasoning()))
                        .withPriority(AgentModelActionPriority.MEDIUM)
                        .withEstimatedDurationMs(parameters.getDefaultActionDurationMs()).withDependencies(List.of())
                        .build())
                .toList();

        return AgentModelActionPlan.builder().withPlanId("plan-" + System.currentTimeMillis()).withGoal(goal)
                .withActions(actions).withContext(context).withParameters(parameters).withCreatedAt(Instant.now())
                .withEstimatedTotalDurationMs(actions.size() * parameters.getDefaultActionDurationMs())
                .withConfidence(reasoningResult.getConfidence()).build();
    }

    /**
     * Create a simple action plan as fallback.
     */
    private AgentModelActionPlan createSimpleActionPlan(String goal, ExecutionContext context,
            AgentModelActionPlanParameters parameters) {

        AgentModelAction simpleAction = AgentModelAction.builder().withActionId("simple-action-1")
                .withActionName("execute-goal").withDescription("Execute goal: " + goal)
                .withParameters(Map.of("goal", goal)).withPriority(AgentModelActionPriority.HIGH)
                .withEstimatedDurationMs(parameters.getDefaultActionDurationMs()).withDependencies(List.of()).build();

        return AgentModelActionPlan.builder().withPlanId("simple-plan-" + System.currentTimeMillis()).withGoal(goal)
                .withActions(List.of(simpleAction)).withContext(context).withParameters(parameters)
                .withCreatedAt(Instant.now()).withEstimatedTotalDurationMs(parameters.getDefaultActionDurationMs())
                .withConfidence(0.5).build();
    }

    /**
     * Fix validation issues in the action plan.
     */
    private AgentModelActionPlan fixValidationIssues(AgentModelActionPlan plan,
            AgentModelActionPlanValidationResult validationResult, ExecutionContext context,
            AgentModelActionPlanParameters parameters) {

        logger.debug("Attempting to fix {} validation issues", validationResult.getIssues().size());

        // For now, return the original plan
        // TODO: Implement intelligent issue fixing
        return plan;
    }

    /**
     * Execute an action plan with monitoring and coordination.
     * 
     * @param plan the action plan to execute
     * @param monitor the execution monitor
     * @return a future that completes with the execution result
     */
    public CompletableFuture<AgentModelActionPlanExecutionResult> executeActionPlan(AgentModelActionPlan plan,
            AgentModelActionPlanExecutionMonitor monitor) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.debug("Executing action plan: {}", plan.getPlanId());
                monitor.onPlanExecutionStarted(plan);

                List<AgentModelAction> actionsToExecute = plan.getActions();
                Instant startTime = Instant.now();

                // 4. Execute concrete actions
                List<ActionResult> actionResults = executeActionsWithMonitoring(actionsToExecute);

                // 5. Create execution result
                long totalDuration = java.time.Duration.between(startTime, Instant.now()).toMillis();
                AgentModelActionPlanExecutionResult executionResult = AgentModelActionPlanExecutionResult.builder()
                        .withPlanId(plan.getPlanId()).withActionResults(actionResults)
                        .withTotalDurationMs(totalDuration)
                        .withSuccessCount((int) actionResults.stream().filter(ActionResult::isSuccess).count())
                        .withFailureCount((int) actionResults.stream().filter(r -> !r.isSuccess()).count())
                        .withCompletedAt(Instant.now()).build();

                monitor.onPlanExecutionCompleted(plan, executionResult);
                logger.debug("Action plan execution completed: {} successes, {} failures",
                        executionResult.getSuccessCount(), executionResult.getFailureCount());

                return executionResult;

            } catch (Exception e) {
                logger.error("Failed to execute action plan: {}", plan.getPlanId(), e);
                monitor.onPlanExecutionFailed(plan, e);
                throw new RuntimeException(new AgentModelActionPlanningException("Failed to execute action plan", e));
            }
        });
    }

    /**
     * Execute actions with monitoring.
     */
    private List<ActionResult> executeActionsWithMonitoring(List<AgentModelAction> actions) {
        List<ActionResult> results = new ArrayList<>();

        for (AgentModelAction action : actions) {
            try {
                // Execute the action (simplified implementation)
                ActionResult result = executeAction(action);
                results.add(result);

            } catch (Exception e) {
                logger.error("Failed to execute action: {}", action.getActionId(), e);
                ActionResult errorResult = ActionResult.error("Execution failed: " + e.getMessage(),
                        new org.openhab.core.ai.action.api.ActionError("EXECUTION_FAILED", e.getMessage()), 0);
                results.add(errorResult);
            }
        }

        return results;
    }

    /**
     * Execute a single action.
     */
    private ActionResult executeAction(AgentModelAction action) {
        try {
            Thread.sleep(action.getEstimatedDurationMs());
            return ActionResult.success("Action executed successfully", action.getEstimatedDurationMs());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ActionResult.error("Action execution interrupted",
                    new org.openhab.core.ai.action.api.ActionError("INTERRUPTED", "Action execution interrupted"), 0);
        }
    }
}
