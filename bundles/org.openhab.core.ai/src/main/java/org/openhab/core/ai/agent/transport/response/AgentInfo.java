package org.openhab.core.ai.agent.transport.response;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Response information for agent REST operations.
 * 
 * This class encapsulates response information for agent REST endpoints,
 * providing status and message details for client consumption.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class AgentInfo {
    private final String status;
    private final String message;

    /**
     * Create a new agent info response.
     * 
     * @param status the status of the operation
     * @param message the response message
     */
    public AgentInfo(String status, String message) {
        this.status = status;
        this.message = message;
    }

    /**
     * Get the status of the operation.
     * 
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Get the response message.
     * 
     * @return the message
     */
    public String getMessage() {
        return message;
    }
}
