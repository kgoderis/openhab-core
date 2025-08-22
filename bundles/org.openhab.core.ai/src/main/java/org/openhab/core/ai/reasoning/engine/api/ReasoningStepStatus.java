package org.openhab.core.ai.reasoning.engine.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Status of a reasoning step in the reasoning process.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public enum ReasoningStepStatus {
    /**
     * Step is waiting to be executed
     */
    PENDING,

    /**
     * Step is currently being executed
     */
    IN_PROGRESS,

    /**
     * Step has completed successfully
     */
    COMPLETED,

    /**
     * Step has failed with an error
     */
    FAILED,

    /**
     * Step has been cancelled
     */
    CANCELLED,

    /**
     * Step is being retried after a failure
     */
    RETRYING,

    /**
     * Step has been skipped
     */
    SKIPPED,

    /**
     * Step is waiting for dependencies
     */
    BLOCKED,

    /**
     * Step has been validated
     */
    VALIDATED,

    /**
     * Step has been rejected during validation
     */
    REJECTED
}
