package org.openhab.core.ai.agent.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Exception thrown by A2A skills during execution.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class AgentSkillException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Create a new A2A skill exception with a message.
     * 
     * @param message the error message
     */
    public AgentSkillException(String message) {
        super(message);
    }

    /**
     * Create a new A2A skill exception with a message and cause.
     * 
     * @param message the error message
     * @param cause the cause of the exception
     */
    public AgentSkillException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Create a new A2A skill exception with a cause.
     * 
     * @param cause the cause of the exception
     */
    public AgentSkillException(Throwable cause) {
        super(cause);
    }
}
