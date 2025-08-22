package org.openhab.core.ai.agent.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Action plan execution states
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public enum AgentModelActionPlanState {

    /**
     * Plan has been created but not yet validated
     */
    CREATED,

    /**
     * Plan is being validated
     */
    VALIDATING,

    /**
     * Plan is ready for execution
     */
    READY,

    /**
     * Plan is currently executing
     */
    EXECUTING,

    /**
     * Plan execution has been paused
     */
    PAUSED,

    /**
     * Plan execution has been cancelled
     */
    CANCELLED,

    /**
     * Plan execution has completed successfully
     */
    COMPLETED,

    /**
     * Plan execution has failed
     */
    FAILED,

    /**
     * Plan execution has been rolled back
     */
    ROLLED_BACK,

    /**
     * Plan rollback has failed
     */
    ROLLBACK_FAILED
}
