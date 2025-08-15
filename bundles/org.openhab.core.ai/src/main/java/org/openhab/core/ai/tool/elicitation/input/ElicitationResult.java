package org.openhab.core.ai.tool.elicitation.input;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Elicitation result data for MCP Client Features.
 * 
 * This represents the result of an elicitation request.
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public class ElicitationResult {

    private final String requestId;
    private final ElicitationStatus status;
    private final String message;
    private final @Nullable Object response;
    private final long timestamp;

    public ElicitationResult(String requestId, ElicitationStatus status, String message, @Nullable Object response,
            long timestamp) {
        this.requestId = requestId;
        this.status = status;
        this.message = message;
        this.response = response;
        this.timestamp = timestamp;
    }

    /**
     * Get the request ID.
     * 
     * @return the request ID
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * Get the result status.
     * 
     * @return the status
     */
    public ElicitationStatus getStatus() {
        return status;
    }

    /**
     * Get the result message.
     * 
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Get the user response.
     * 
     * @return the response or null if not available
     */
    public @Nullable Object getResponse() {
        return response;
    }

    /**
     * Get the result timestamp.
     * 
     * @return the timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "ElicitationResult{requestId='" + requestId + "', status=" + status + ", message='" + message + "'}";
    }

    // ElicitationStatus extracted to org.openhab.core.ai.tool.elicitation.input.ElicitationStatus
}
