package org.openhab.core.ai.agent.planning;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Exception thrown during action planning operations.
 * 
 * This exception is used to indicate errors that occur during the planning,
 * validation, optimization, or execution of action plans.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public class AgentModelActionPlanningException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Create a new exception with a message.
     * 
     * @param message the error message
     */
    public AgentModelActionPlanningException(String message) {
        super(message);
    }

    /**
     * Create a new exception with a message and cause.
     * 
     * @param message the error message
     * @param cause the cause of the exception
     */
    public AgentModelActionPlanningException(String message, @Nullable Throwable cause) {
        super(message, cause);
    }

    /**
     * Create a new exception with a cause.
     * 
     * @param cause the cause of the exception
     */
    public AgentModelActionPlanningException(@Nullable Throwable cause) {
        super(cause);
    }
}
