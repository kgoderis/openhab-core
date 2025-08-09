package org.openhab.core.ai.agent.api;

import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.action.ActionContext;
import org.openhab.core.ai.action.ActionResult;

/**
 * Agent Action Delegation Service - Defines the contract for agent-based action delegation
 * 
 * <p>
 * This service provides:
 * - Action delegation to available agents for all LLM types (local and remote)
 * - Service availability checking
 * - Agent count monitoring
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface AgentActionDelegationService {

    /**
     * Delegate an action to an appropriate agent for execution
     * 
     * @param actionContext the action context to delegate
     * @return CompletableFuture with the action result
     */
    CompletableFuture<ActionResult> delegateAction(ActionContext actionContext);

    /**
     * Get the service status
     * 
     * @return true if the service is available and ready
     */
    boolean isAvailable();

    /**
     * Get the number of available agents
     * 
     * @return the number of agents available for delegation
     */
    int getAvailableAgentCount();
}
