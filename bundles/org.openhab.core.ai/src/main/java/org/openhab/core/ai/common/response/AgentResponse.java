package org.openhab.core.ai.common.response;

import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Response implementation for agent responses.
 * 
 * This class represents responses from autonomous agents,
 * implementing the unified Response interface.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class AgentResponse implements Response<Object> {

    private final String id;
    private final @Nullable Object data;
    private final long timestamp;
    private final String agentId;
    private final String action;
    private final long processingTimeMs;
    private final Map<String, Object> metadata;
    private final @Nullable String errorMessage;
    private final String responseType;

    /**
     * Create a new AgentResponse.
     * 
     * @param id the response ID
     * @param data the response data
     * @param timestamp the response timestamp
     * @param agentId the agent ID
     * @param action the action performed
     * @param processingTimeMs the processing time in milliseconds
     * @param metadata additional metadata
     * @param errorMessage the error message if any
     * @param responseType the type of response
     */
    public AgentResponse(String id, @Nullable Object data, long timestamp, String agentId, String action,
            long processingTimeMs, Map<String, Object> metadata, @Nullable String errorMessage, String responseType) {
        this.id = Objects.requireNonNull(id, "id");
        this.data = data;
        this.timestamp = timestamp;
        this.agentId = Objects.requireNonNull(agentId, "agentId");
        this.action = Objects.requireNonNull(action, "action");
        this.processingTimeMs = processingTimeMs;
        this.metadata = Objects.requireNonNull(metadata, "metadata");
        this.errorMessage = errorMessage;
        this.responseType = Objects.requireNonNull(responseType, "responseType");
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isSuccess() {
        return errorMessage == null;
    }

    @Override
    public @Nullable Object getData() {
        return data;
    }

    @Override
    public @Nullable String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Get the agent ID that generated this response.
     * 
     * @return the agent ID
     */
    public String getAgentId() {
        return agentId;
    }

    /**
     * Get the action that was performed.
     * 
     * @return the action name
     */
    public String getAction() {
        return action;
    }

    /**
     * Get the processing time in milliseconds.
     * 
     * @return the processing time
     */
    public long getProcessingTimeMs() {
        return processingTimeMs;
    }

    /**
     * Get additional metadata for this response.
     * 
     * @return the metadata map
     */
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    /**
     * Get the type of response.
     * 
     * @return the response type
     */
    public String getResponseType() {
        return responseType;
    }

    /**
     * Create a successful response.
     * 
     * @param data the response data
     * @param agentId the agent ID
     * @param action the action performed
     * @return a successful AgentResponse
     */
    public static AgentResponse success(Object data, String agentId, String action) {
        return new AgentResponse(generateId(), data, System.currentTimeMillis(), agentId, action, 0, Map.of(), null,
                "SUCCESS");
    }

    /**
     * Create an error response.
     * 
     * @param errorMessage the error message
     * @param agentId the agent ID
     * @param action the action attempted
     * @return an error AgentResponse
     */
    public static AgentResponse error(String errorMessage, String agentId, String action) {
        return new AgentResponse(generateId(), null, System.currentTimeMillis(), agentId, action, 0, Map.of(),
                errorMessage, "ERROR");
    }

    /**
     * Create a response with processing time.
     * 
     * @param data the response data
     * @param agentId the agent ID
     * @param action the action performed
     * @param processingTimeMs the processing time in milliseconds
     * @return an AgentResponse with processing time
     */
    public static AgentResponse success(Object data, String agentId, String action, long processingTimeMs) {
        return new AgentResponse(generateId(), data, System.currentTimeMillis(), agentId, action, processingTimeMs,
                Map.of(), null, "SUCCESS");
    }

    /**
     * Create a notification response.
     * 
     * @param message the notification message
     * @param agentId the agent ID
     * @param action the action performed
     * @return a notification AgentResponse
     */
    public static AgentResponse notification(String message, String agentId, String action) {
        return new AgentResponse(generateId(), message, System.currentTimeMillis(), agentId, action, 0, Map.of(), null,
                "NOTIFICATION");
    }

    private static String generateId() {
        return "agent-response-" + System.currentTimeMillis() + "-" + System.nanoTime();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        AgentResponse other = (AgentResponse) obj;
        return Objects.equals(id, other.id) && Objects.equals(data, other.data) && timestamp == other.timestamp
                && Objects.equals(agentId, other.agentId) && Objects.equals(action, other.action)
                && processingTimeMs == other.processingTimeMs && Objects.equals(metadata, other.metadata)
                && Objects.equals(errorMessage, other.errorMessage) && Objects.equals(responseType, other.responseType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, data, timestamp, agentId, action, processingTimeMs, metadata, errorMessage,
                responseType);
    }

    @Override
    public String toString() {
        return "AgentResponse{" + "id='" + id + '\'' + ", data=" + data + ", timestamp=" + timestamp + ", agentId='"
                + agentId + '\'' + ", action='" + action + '\'' + ", processingTimeMs=" + processingTimeMs
                + ", metadata=" + metadata + ", errorMessage='" + errorMessage + '\'' + ", responseType='"
                + responseType + '\'' + '}';
    }
}
