package org.openhab.core.ai.agent.planning;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.context.ExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Optimizer for agent model action plans.
 * 
 * <p>
 * This class provides optimization capabilities for action plans including
 * dependency optimization, parallel execution optimization, and resource optimization.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class AgentModelActionPlanOptimizer {

    private final Logger logger = LoggerFactory.getLogger(AgentModelActionPlanOptimizer.class);

    /**
     * Optimize an action plan.
     * 
     * @param plan the action plan to optimize
     * @param context the execution context
     * @return optimized action plan
     */
    public AgentModelActionPlan optimize(AgentModelActionPlan plan, ExecutionContext context) {
        try {
            logger.debug("Optimizing action plan: {}", plan.getPlanId());

            // Create a copy of the plan for optimization
            AgentModelActionPlan optimizedPlan = plan.toBuilder().build();

            // 1. Optimize action ordering for dependencies
            optimizedPlan = optimizeActionOrdering(optimizedPlan);

            // 2. Optimize for parallel execution
            optimizedPlan = optimizeParallelExecution(optimizedPlan);

            // 3. Optimize resource usage
            optimizedPlan = optimizeResourceUsage(optimizedPlan, context);

            // 4. Recalculate estimated duration
            optimizedPlan = recalculateDuration(optimizedPlan);

            logger.debug("Action plan optimization completed: {}", optimizedPlan.getPlanId());

            return optimizedPlan;

        } catch (Exception e) {
            logger.error("Error during action plan optimization: {}", plan.getPlanId(), e);
            // Return original plan if optimization fails
            return plan;
        }
    }

    /**
     * Optimize action ordering based on dependencies.
     */
    private AgentModelActionPlan optimizeActionOrdering(AgentModelActionPlan plan) {
        List<AgentModelAction> actions = new ArrayList<>(plan.getActions());

        // Sort actions by dependencies (topological sort)
        actions.sort(new DependencyComparator());

        return plan.toBuilder().withActions(actions).build();
    }

    /**
     * Optimize for parallel execution where possible.
     */
    private AgentModelActionPlan optimizeParallelExecution(AgentModelActionPlan plan) {
        List<AgentModelAction> optimizedActions = new ArrayList<>();
        Map<String, List<String>> dependencyGroups = new HashMap<>();

        // Group actions by their dependency chains
        for (AgentModelAction action : plan.getActions()) {
            String dependencyKey = String.join(",", action.getDependencies());
            dependencyGroups.computeIfAbsent(dependencyKey, k -> new ArrayList<>()).add(action.getActionId());
        }

        // Reorder actions to maximize parallel execution
        List<AgentModelAction> reorderedActions = new ArrayList<>();
        for (AgentModelAction action : plan.getActions()) {
            // Find the best position for this action
            int bestPosition = findBestPosition(action, reorderedActions);
            reorderedActions.add(bestPosition, action);
        }

        return plan.toBuilder().withActions(reorderedActions).build();
    }

    /**
     * Find the best position for an action to maximize parallel execution.
     */
    private int findBestPosition(AgentModelAction action, List<AgentModelAction> existingActions) {
        // Simple heuristic: place actions with similar dependencies together
        for (int i = 0; i < existingActions.size(); i++) {
            AgentModelAction existing = existingActions.get(i);
            if (haveSimilarDependencies(action, existing)) {
                return i + 1; // Place after similar action
            }
        }
        return existingActions.size(); // Place at the end
    }

    /**
     * Check if two actions have similar dependencies.
     */
    private boolean haveSimilarDependencies(AgentModelAction action1, AgentModelAction action2) {
        List<String> deps1 = action1.getDependencies();
        List<String> deps2 = action2.getDependencies();

        if (deps1.size() != deps2.size()) {
            return false;
        }

        return deps1.containsAll(deps2) && deps2.containsAll(deps1);
    }

    /**
     * Optimize resource usage based on execution context.
     */
    private AgentModelActionPlan optimizeResourceUsage(AgentModelActionPlan plan, ExecutionContext context) {
        List<AgentModelAction> optimizedActions = new ArrayList<>();
        Map<String, Object> contextValues = context.getAllValues();

        for (AgentModelAction action : plan.getActions()) {
            AgentModelAction optimizedAction = optimizeActionResources(action, contextValues);
            optimizedActions.add(optimizedAction);
        }

        return plan.toBuilder().withActions(optimizedActions).build();
    }

    /**
     * Optimize resources for a single action.
     */
    private AgentModelAction optimizeActionResources(AgentModelAction action, Map<String, Object> contextValues) {
        Map<String, Object> optimizedParameters = new HashMap<>(action.getParameters());

        // Check if action requires specific resources that are available
        if (optimizedParameters.containsKey("requiredResource")) {
            String requiredResource = optimizedParameters.get("requiredResource").toString();
            if (contextValues.containsKey(requiredResource)) {
                // Resource is available, optimize parameters
                optimizedParameters.put("resourceAvailable", true);
                optimizedParameters.put("resourceValue", contextValues.get(requiredResource));
            } else {
                // Resource not available, add warning
                optimizedParameters.put("resourceWarning", "Required resource not available: " + requiredResource);
            }
        }

        return action.toBuilder().withParameters(optimizedParameters).build();
    }

    /**
     * Recalculate estimated duration after optimization.
     */
    private AgentModelActionPlan recalculateDuration(AgentModelActionPlan plan) {
        long totalDuration = 0;
        long maxParallelDuration = 0;
        Map<String, Long> dependencyCompletionTimes = new HashMap<>();

        for (AgentModelAction action : plan.getActions()) {
            // Calculate when this action can start (after dependencies complete)
            long startTime = 0;
            for (String dependencyId : action.getDependencies()) {
                Long dependencyTime = dependencyCompletionTimes.get(dependencyId);
                if (dependencyTime != null) {
                    startTime = Math.max(startTime, dependencyTime);
                }
            }

            // Calculate completion time for this action
            long completionTime = startTime + action.getEstimatedDurationMs();
            dependencyCompletionTimes.put(action.getActionId(), completionTime);

            // Update total duration
            totalDuration = Math.max(totalDuration, completionTime);

            // Update max parallel duration (simplified calculation)
            maxParallelDuration += action.getEstimatedDurationMs();
        }

        // Use the smaller of total duration or parallel duration
        long optimizedDuration = Math.min(totalDuration, maxParallelDuration);

        return plan.toBuilder().withEstimatedTotalDurationMs(optimizedDuration).build();
    }

    /**
     * Comparator for sorting actions by dependencies.
     */
    private static class DependencyComparator implements Comparator<AgentModelAction> {
        @Override
        public int compare(AgentModelAction action1, AgentModelAction action2) {
            // Actions with fewer dependencies come first
            int deps1 = action1.getDependencies().size();
            int deps2 = action2.getDependencies().size();

            if (deps1 != deps2) {
                return Integer.compare(deps1, deps2);
            }

            // If same number of dependencies, sort by priority
            return action1.getPriority().compareTo(action2.getPriority());
        }
    }

    /**
     * Get optimization statistics for a plan.
     * 
     * @param originalPlan the original plan
     * @param optimizedPlan the optimized plan
     * @return optimization statistics
     */
    public Map<String, Object> getOptimizationStats(AgentModelActionPlan originalPlan,
            AgentModelActionPlan optimizedPlan) {
        Map<String, Object> stats = new HashMap<>();

        long durationImprovement = originalPlan.getEstimatedTotalDurationMs()
                - optimizedPlan.getEstimatedTotalDurationMs();
        double improvementPercentage = originalPlan.getEstimatedTotalDurationMs() > 0
                ? (double) durationImprovement / originalPlan.getEstimatedTotalDurationMs() * 100.0
                : 0.0;

        stats.put("originalDuration", originalPlan.getEstimatedTotalDurationMs());
        stats.put("optimizedDuration", optimizedPlan.getEstimatedTotalDurationMs());
        stats.put("durationImprovement", durationImprovement);
        stats.put("improvementPercentage", improvementPercentage);
        stats.put("originalActionCount", originalPlan.getActions().size());
        stats.put("optimizedActionCount", optimizedPlan.getActions().size());
        stats.put("optimizationTimestamp", System.currentTimeMillis());

        return stats;
    }
}
