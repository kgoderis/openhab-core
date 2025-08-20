/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.core.ai.agent.infrastructure.security;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.security.SecurityRequest;

/**
 * Agent-specific security request for validation operations.
 * 
 * <p>
 * This class extends the common SecurityRequest with agent-specific fields
 * that are relevant for agent security validation operations.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public final class AgentSecurityRequest extends SecurityRequest {
    private final String agentId;
    private final String modelId;
    private final String taskType;
    private final @Nullable String prompt;

    /**
     * Create a new agent security request.
     * 
     * @param requestId the unique request identifier
     * @param agentId the agent identifier
     * @param modelId the model identifier
     * @param taskType the type of task being performed
     * @param prompt the prompt or content being processed (may be null)
     * @param authenticationToken the authentication token (may be null)
     * @param timestamp the request timestamp
     */
    public AgentSecurityRequest(String requestId, String agentId, String modelId, String taskType,
            @Nullable String prompt, @Nullable String authenticationToken, long timestamp) {
        super(requestId, authenticationToken, timestamp, buildAgentMetadata(agentId, modelId, taskType, prompt));
        this.agentId = agentId;
        this.modelId = modelId;
        this.taskType = taskType;
        this.prompt = prompt;
    }

    /**
     * Get the agent identifier.
     * 
     * @return the agent ID
     */
    public String getAgentId() {
        return agentId;
    }

    /**
     * Get the model identifier.
     * 
     * @return the model ID
     */
    public String getModelId() {
        return modelId;
    }

    /**
     * Get the task type.
     * 
     * @return the task type
     */
    public String getTaskType() {
        return taskType;
    }

    /**
     * Get the prompt or content.
     * 
     * @return the prompt, or null if not provided
     */
    public @Nullable String getPrompt() {
        return prompt;
    }

    /**
     * Build agent-specific metadata for the base SecurityRequest.
     * 
     * @param agentId the agent ID
     * @param modelId the model ID
     * @param taskType the task type
     * @param prompt the prompt
     * @return metadata map
     */
    private static Map<String, Object> buildAgentMetadata(String agentId, String modelId, String taskType,
            @Nullable String prompt) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("agentId", agentId);
        metadata.put("modelId", modelId);
        metadata.put("taskType", taskType);
        if (prompt != null) {
            metadata.put("prompt", prompt);
        }
        return metadata;
    }

    @Override
    public String toString() {
        return String.format("AgentSecurityRequest{requestId='%s', agentId='%s', modelId='%s', taskType='%s'}",
                getRequestId(), agentId, modelId, taskType);
    }
}
