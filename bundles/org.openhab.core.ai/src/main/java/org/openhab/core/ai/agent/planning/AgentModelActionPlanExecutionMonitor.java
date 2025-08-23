package org.openhab.core.ai.agent.planning;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Monitor for action plan execution progress and events.
 * 
 * This interface provides callbacks for monitoring the execution of action plans,
 * allowing external components to track progress, handle events, and respond to
 * execution state changes.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public interface AgentModelActionPlanExecutionMonitor {

    /**
     * Called when plan execution starts.
     * 
     * @param plan the action plan being executed
     */
    void onPlanExecutionStarted(AgentModelActionPlan plan);

    /**
     * Called when plan execution completes successfully.
     * 
     * @param plan the action plan that was executed
     * @param result the execution result
     */
    void onPlanExecutionCompleted(AgentModelActionPlan plan, AgentModelActionPlanExecutionResult result);

    /**
     * Called when plan execution fails.
     * 
     * @param plan the action plan that failed
     * @param exception the exception that caused the failure
     */
    void onPlanExecutionFailed(AgentModelActionPlan plan, Exception exception);

    /**
     * Called when individual action execution starts.
     * 
     * @param action the action being executed
     */
    void onActionExecutionStarted(AgentModelAction action);

    /**
     * Called when individual action execution completes successfully.
     * 
     * @param action the action that was executed
     * @param result the action execution result
     */
    void onActionExecutionCompleted(AgentModelAction action, AgentModelActionExecutionResult result);

    /**
     * Called when individual action execution fails.
     * 
     * @param action the action that failed
     * @param result the action execution result with error details
     */
    void onActionExecutionFailed(AgentModelAction action, AgentModelActionExecutionResult result);

    /**
     * Called when action execution is skipped.
     * 
     * @param action the action that was skipped
     * @param reason the reason for skipping
     */
    void onActionExecutionSkipped(AgentModelAction action, String reason);

    /**
     * Called when execution progress is updated.
     * 
     * @param plan the action plan being executed
     * @param completedActions the number of completed actions
     * @param totalActions the total number of actions
     * @param currentAction the currently executing action, or null if none
     */
    void onExecutionProgressUpdated(AgentModelActionPlan plan, int completedActions, int totalActions,
            @Nullable AgentModelAction currentAction);
}
