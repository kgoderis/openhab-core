package org.openhab.core.ai.reasoning.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class SecurityRequest {
    private final String requestId;
    private final String agentId;
    private final String modelId;
    private final String taskType;
    private final String prompt;
    private final String authenticationToken;
    private final long timestamp;

    public SecurityRequest(String requestId, String agentId, String modelId, String taskType, String prompt,
            String authenticationToken, long timestamp) {
        this.requestId = requestId;
        this.agentId = agentId;
        this.modelId = modelId;
        this.taskType = taskType;
        this.prompt = prompt;
        this.authenticationToken = authenticationToken;
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

    public long getTimestamp() {
        return timestamp;
    }
}
