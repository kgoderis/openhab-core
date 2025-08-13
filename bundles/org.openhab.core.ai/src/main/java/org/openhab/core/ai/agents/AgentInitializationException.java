package org.openhab.core.ai.agents;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Exception thrown when an agent fails to initialize.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class AgentInitializationException extends RuntimeException {
    public AgentInitializationException(String message, Throwable cause) {
        super(message, cause);
    }
}


