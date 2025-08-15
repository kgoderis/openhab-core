package org.openhab.core.ai.reasoning.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Model Request for security validation and processing.
 *
 * Immutable request metadata including agent, model and task information.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ModelRequest {
    private final String requestId;
    private final String agentId;
    private final String modelId;
    private final String taskType;
    private final String prompt;
    private final String authenticationToken;
    private final Map<String, Object> parameters;
    private final long timestamp;

    public ModelRequest(String requestId, String agentId, String modelId, String taskType, String prompt,
            String authenticationToken, Map<String, Object> parameters, long timestamp) {
        this.requestId = requestId;
        this.agentId = agentId;
        this.modelId = modelId;
        this.taskType = taskType;
        this.prompt = prompt;
        this.authenticationToken = authenticationToken;
        this.parameters = new ConcurrentHashMap<>(parameters);
        this.timestamp = timestamp;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getAgentId() {
        return agentId;
    }

    public String getModelId() {
        return modelId;
    }

    public String getTaskType() {
        return taskType;
    }

    public String getPrompt() {
        return prompt;
    }

    public String getAuthenticationToken() {
        return authenticationToken;
    }

    public Map<String, Object> getParameters() {
        return new ConcurrentHashMap<>(parameters);
    }

    public long getTimestamp() {
        return timestamp;
    }
}
