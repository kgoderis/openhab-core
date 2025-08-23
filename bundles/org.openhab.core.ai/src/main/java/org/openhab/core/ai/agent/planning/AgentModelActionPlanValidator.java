package org.openhab.core.ai.agent.planning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.context.ExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validator for agent model action plans.
 * 
 * <p>
 * This class provides comprehensive validation for action plans including
 * action dependencies, parameter validation, and plan structure validation.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class AgentModelActionPlanValidator {

    private final Logger logger = LoggerFactory.getLogger(AgentModelActionPlanValidator.class);

    /**
     * Validate an action plan.
     * 
     * @param plan the action plan to validate
     * @param context the execution context
     * @return validation result
     */
    public AgentModelActionPlanValidationResult validate(AgentModelActionPlan plan, ExecutionContext context) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        Map<String, Object> details = new HashMap<>();

        try {
            logger.debug("Validating action plan: {}", plan.getPlanId());

            // 1. Validate plan structure
            validatePlanStructure(plan, errors, warnings, details);

            // 2. Validate action dependencies
            validateActionDependencies(plan, errors, warnings, details);

            // 3. Validate action parameters
            validateActionParameters(plan, errors, warnings, details);

            // 4. Validate plan constraints
            validatePlanConstraints(plan, context, errors, warnings, details);

            boolean isValid = errors.isEmpty();
            details.put("planId", plan.getPlanId());
            details.put("totalActions", plan.getActions().size());
            details.put("validationTimestamp", System.currentTimeMillis());

            logger.debug("Action plan validation completed: valid={}, errors={}, warnings={}", isValid, errors.size(),
                    warnings.size());

            return new AgentModelActionPlanValidationResult(isValid, errors, warnings, details);

        } catch (Exception e) {
            logger.error("Error during action plan validation: {}", plan.getPlanId(), e);
            errors.add("Validation error: " + e.getMessage());
            return new AgentModelActionPlanValidationResult(false, errors, warnings, details);
        }
    }

    /**
     * Validate the basic structure of the action plan.
     */
    private void validatePlanStructure(AgentModelActionPlan plan, List<String> errors, List<String> warnings,
            Map<String, Object> details) {

        // Check if plan has a goal
        if (plan.getGoal() == null || plan.getGoal().trim().isEmpty()) {
            errors.add("Action plan must have a non-empty goal");
        }

        // Check if plan has actions
        if (plan.getActions().isEmpty()) {
            errors.add("Action plan must contain at least one action");
        }

        // Check plan confidence
        if (plan.getConfidence() < 0.0 || plan.getConfidence() > 1.0) {
            errors.add("Plan confidence must be between 0.0 and 1.0");
        }

        // Check estimated duration
        if (plan.getEstimatedTotalDurationMs() < 0) {
            errors.add("Estimated total duration must be non-negative");
        }

        // Check for duplicate action IDs
        List<String> actionIds = plan.getActions().stream().map(AgentModelAction::getActionId).toList();
        List<String> duplicateIds = actionIds.stream().filter(id -> actionIds.stream().filter(id::equals).count() > 1)
                .distinct().toList();

        if (!duplicateIds.isEmpty()) {
            errors.add("Duplicate action IDs found: " + duplicateIds);
        }

        details.put("structureValid", errors.stream().noneMatch(e -> e.contains("structure")));
    }

    /**
     * Validate action dependencies.
     */
    private void validateActionDependencies(AgentModelActionPlan plan, List<String> errors, List<String> warnings,
            Map<String, Object> details) {

        List<String> actionIds = plan.getActions().stream().map(AgentModelAction::getActionId).toList();

        List<String> dependencyErrors = new ArrayList<>();
        List<String> dependencyWarnings = new ArrayList<>();

        for (AgentModelAction action : plan.getActions()) {
            for (String dependencyId : action.getDependencies()) {
                if (!actionIds.contains(dependencyId)) {
                    dependencyErrors
                            .add("Action " + action.getActionId() + " depends on non-existent action: " + dependencyId);
                }
            }
        }

        // Check for circular dependencies (simplified check)
        if (hasCircularDependencies(plan)) {
            dependencyErrors.add("Circular dependencies detected in action plan");
        }

        errors.addAll(dependencyErrors);
        warnings.addAll(dependencyWarnings);
        details.put("dependenciesValid", dependencyErrors.isEmpty());
    }

    /**
     * Check for circular dependencies in the action plan.
     */
    private boolean hasCircularDependencies(AgentModelActionPlan plan) {
        // Simplified circular dependency detection
        // In a full implementation, this would use a proper graph algorithm
        Map<String, List<String>> dependencyGraph = new HashMap<>();

        for (AgentModelAction action : plan.getActions()) {
            dependencyGraph.put(action.getActionId(), new ArrayList<>(action.getDependencies()));
        }

        // Check for self-dependencies
        for (AgentModelAction action : plan.getActions()) {
            if (action.getDependencies().contains(action.getActionId())) {
                return true;
            }
        }

        // For now, return false (no circular dependencies detected)
        // TODO: Implement proper cycle detection algorithm
        return false;
    }

    /**
     * Validate action parameters.
     */
    private void validateActionParameters(AgentModelActionPlan plan, List<String> errors, List<String> warnings,
            Map<String, Object> details) {

        List<String> parameterErrors = new ArrayList<>();
        List<String> parameterWarnings = new ArrayList<>();

        for (AgentModelAction action : plan.getActions()) {
            // Check if action has required parameters
            if (action.getParameters().isEmpty()) {
                parameterWarnings.add("Action " + action.getActionId() + " has no parameters");
            }

            // Check estimated duration
            if (action.getEstimatedDurationMs() < 0) {
                parameterErrors.add("Action " + action.getActionId() + " has negative estimated duration");
            }

            // Check action name
            if (action.getActionName() == null || action.getActionName().trim().isEmpty()) {
                parameterErrors.add("Action " + action.getActionId() + " has empty action name");
            }

            // Check description
            if (action.getDescription() == null || action.getDescription().trim().isEmpty()) {
                parameterWarnings.add("Action " + action.getActionId() + " has no description");
            }
        }

        errors.addAll(parameterErrors);
        warnings.addAll(parameterWarnings);
        details.put("parametersValid", parameterErrors.isEmpty());
    }

    /**
     * Validate plan constraints against execution context.
     */
    private void validatePlanConstraints(AgentModelActionPlan plan, ExecutionContext context, List<String> errors,
            List<String> warnings, Map<String, Object> details) {

        List<String> constraintErrors = new ArrayList<>();
        List<String> constraintWarnings = new ArrayList<>();

        // Check if plan parameters allow the plan structure
        AgentModelActionPlanParameters params = plan.getParameters();

        if (plan.getActions().size() > params.getMaxActionsPerPlan()) {
            constraintErrors.add("Plan contains " + plan.getActions() + " actions, but maximum allowed is "
                    + params.getMaxActionsPerPlan());
        }

        if (plan.getConfidence() < params.getMinConfidenceThreshold()) {
            constraintWarnings.add("Plan confidence (" + plan.getConfidence() + ") is below threshold ("
                    + params.getMinConfidenceThreshold() + ")");
        }

        // Check estimated duration against reasonable limits
        long maxReasonableDuration = 300000L; // 5 minutes
        if (plan.getEstimatedTotalDurationMs() > maxReasonableDuration) {
            constraintWarnings.add(
                    "Plan estimated duration (" + plan.getEstimatedTotalDurationMs() + "ms) is very long (>5 minutes)");
        }

        errors.addAll(constraintErrors);
        warnings.addAll(constraintWarnings);
        details.put("constraintsValid", constraintErrors.isEmpty());
    }
}
