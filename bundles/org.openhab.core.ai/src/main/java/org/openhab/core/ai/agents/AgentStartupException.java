package org.openhab.core.ai.agents;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Exception thrown when an agent fails to start or stop properly.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class AgentStartupException extends RuntimeException {
    public AgentStartupException(String message, Throwable cause) {
        super(message, cause);
    }
}
