package org.openhab.core.ai.agent.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Action step execution states
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public enum AgentModelActionStepState {

    /**
     * Step has been created but not yet validated
     */
    CREATED,

    /**
     * Step is being validated
     */
    VALIDATING,

    /**
     * Step is ready for execution
     */
    READY,

    /**
     * Step is currently executing
     */
    EXECUTING,

    /**
     * Step execution has been paused
     */
    PAUSED,

    /**
     * Step execution has been cancelled
     */
    CANCELLED,

    /**
     * Step execution has completed successfully
     */
    COMPLETED,

    /**
     * Step execution has failed
     */
    FAILED,

    /**
     * Step execution has been skipped
     */
    SKIPPED,

    /**
     * Step execution has been rolled back
     */
    ROLLED_BACK
}
