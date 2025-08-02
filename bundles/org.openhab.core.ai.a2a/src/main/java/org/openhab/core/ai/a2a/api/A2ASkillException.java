package org.openhab.core.ai.a2a.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Exception thrown by A2A skills during execution.
 * 
 * @author AI Assistant
 * @since 1.0.0
 */
@NonNullByDefault
public class A2ASkillException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Create a new A2A skill exception with a message.
     * 
     * @param message the error message
     */
    public A2ASkillException(String message) {
        super(message);
    }

    /**
     * Create a new A2A skill exception with a message and cause.
     * 
     * @param message the error message
     * @param cause the cause of the exception
     */
    public A2ASkillException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Create a new A2A skill exception with a cause.
     * 
     * @param cause the cause of the exception
     */
    public A2ASkillException(Throwable cause) {
        super(cause);
    }
}
