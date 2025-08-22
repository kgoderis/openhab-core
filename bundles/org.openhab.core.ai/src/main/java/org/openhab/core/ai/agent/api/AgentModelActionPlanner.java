package org.openhab.core.ai.agent.api;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.context.ReasoningContext;

/**
 * Model-based action planner for autonomous agents
 * 
 * <p>
 * This interface provides:
 * - Model-based action planning using LLM reasoning
 * - Action plan generation and optimization
 * - Plan validation and safety checks
 * - Plan execution coordination and monitoring
 * - Plan rollback and recovery mechanisms
 * - Plan performance monitoring and optimization
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface AgentModelActionPlanner {

    /**
     * Create an action plan using model-based reasoning
     * 
     * @param goal the goal to achieve
     * @param context the current context
     * @param constraints the planning constraints
     * @return CompletableFuture with the action plan
     */
    CompletableFuture<AgentModelActionPlan> createPlan(String goal, Map<String, Object> context,
            @Nullable Map<String, Object> constraints);

    /**
     * Create an action plan with reasoning context
     * 
     * @param reasoningContext the reasoning context
     * @param constraints the planning constraints
     * @return CompletableFuture with the action plan
     */
    CompletableFuture<AgentModelActionPlan> createPlan(ReasoningContext reasoningContext,
            @Nullable Map<String, Object> constraints);

    /**
     * Optimize an existing action plan
     * 
     * @param plan the action plan to optimize
     * @param optimizationHints hints for optimization
     * @return CompletableFuture with the optimized plan
     */
    CompletableFuture<AgentModelActionPlan> optimizePlan(AgentModelActionPlan plan,
            @Nullable Map<String, Object> optimizationHints);

    /**
     * Validate an action plan
     * 
     * @param plan the action plan to validate
     * @return validation result
     */
    AgentModelActionPlanValidationResult validatePlan(AgentModelActionPlan plan);

    /**
     * Execute an action plan with monitoring
     * 
     * @param plan the action plan to execute
     * @param executionContext the execution context
     * @return CompletableFuture with execution results
     */
    CompletableFuture<AgentModelActionPlanExecutionResult> executePlan(AgentModelActionPlan plan,
            @Nullable Map<String, Object> executionContext);

    /**
     * Rollback an action plan execution
     * 
     * @param plan the action plan to rollback
     * @param executionResult the execution result to rollback
     * @return CompletableFuture with rollback results
     */
    CompletableFuture<AgentModelActionPlanRollbackResult> rollbackPlan(AgentModelActionPlan plan,
            AgentModelActionPlanExecutionResult executionResult);

    /**
     * Get planning performance metrics
     * 
     * @return planning performance metrics
     */
    AgentModelActionPlanPerformanceMetrics getPerformanceMetrics();

    /**
     * Get available planning strategies
     * 
     * @return list of available planning strategies
     */
    List<String> getAvailableStrategies();

    /**
     * Set planning strategy
     * 
     * @param strategy the planning strategy to use
     */
    void setPlanningStrategy(String strategy);

    /**
     * Get current planning strategy
     * 
     * @return current planning strategy
     */
    String getCurrentStrategy();
}
