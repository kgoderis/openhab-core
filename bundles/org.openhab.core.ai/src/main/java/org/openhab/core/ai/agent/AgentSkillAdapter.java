package org.openhab.core.ai.agent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.action.ActionContext;
import org.openhab.core.ai.action.ActionResult;
import org.openhab.core.ai.api.action.Action;
import org.openhab.core.ai.api.agent.AgentSkillException;
import org.openhab.core.ai.api.agent.AgentSkillResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.a2a.spec.Message;

/**
 * Skill adapter for Agent operations.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class AgentSkillAdapter {

    private static final Logger logger = LoggerFactory.getLogger(AgentSkillAdapter.class);

    private final String skillId;
    private final Action action;

    /**
     * Create a new Agent skill adapter.
     * 
     * @param skillId the skill ID
     * @param action the Action to adapt
     */
    public AgentSkillAdapter(String skillId, Action action) {
        this.skillId = skillId;
        this.action = action;
        logger.debug("Created Agent skill adapter for skill: {}", skillId);
    }

    /**
     * Execute the skill with the given Agent SDK message.
     * 
     * @param sdkMessage the Agent SDK message containing parameters
     * @return the skill execution result
     * @throws AgentSkillException if execution fails
     */
    public AgentSkillResult execute(Message sdkMessage) throws AgentSkillException {
        long startTime = System.currentTimeMillis();

        try {
            logger.debug("Executing Agent skill {} with SDK message", skillId);

            // Extract parameters from Agent SDK message
            Map<String, Object> parameters = extractParameters(sdkMessage);

            // Create Action context from Agent SDK message
            ActionContext aiContext = createActionContext(sdkMessage);

            // Execute the Action asynchronously
            CompletableFuture<ActionResult> future = action.executeAsync(parameters, aiContext);

            // Wait for completion and convert result
            ActionResult aiResult = future.get();
            long executionTime = System.currentTimeMillis() - startTime;

            if (aiResult.isSuccess()) {
                // Convert ActionResult data to Map<String, Object>
                Map<String, Object> resultData = convertToMap(aiResult.getData());
                return AgentSkillResult.success(resultData, executionTime);
            } else {
                return AgentSkillResult.failure(aiResult.getMessage(), "EXECUTION_FAILED", executionTime);
            }

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("Error executing skill: {}", skillId, e);
            return AgentSkillResult.failure("Error executing skill: " + e.getMessage(), "EXECUTION_ERROR",
                    executionTime);
        }
    }

    /**
     * Extract parameters from an Agent SDK message.
     * 
     * @param sdkMessage the Agent SDK message
     * @return the extracted parameters
     */
    private Map<String, Object> extractParameters(Message sdkMessage) {
        Map<String, Object> parameters = new HashMap<>();

        // Extract parameters from message parts (following AgentAgentExecutor pattern)
        String content = extractTextContent(sdkMessage);

        // Simple parsing - extract parameters from content
        // Format: "actionId param1=value1 param2=value2"
        String[] parts = content.split(" ");

        for (int i = 1; i < parts.length; i++) {
            String part = parts[i];
            String[] keyValue = part.split("=", 2);
            if (keyValue.length == 2) {
                parameters.put(keyValue[0], keyValue[1]);
            }
        }

        // Add message metadata
        Map<String, Object> metadata = sdkMessage.getMetadata();
        if (metadata != null) {
            parameters.putAll(metadata);
        }

        return parameters;
    }

    /**
     * Extract text content from message parts.
     * 
     * @param message the Agent message
     * @return text content
     */
    private String extractTextContent(Message message) {
        StringBuilder content = new StringBuilder();

        for (io.a2a.spec.Part<?> part : message.getParts()) {
            if (part instanceof io.a2a.spec.TextPart textPart) {
                if (content.length() > 0) {
                    content.append(" ");
                }
                content.append(textPart.getText());
            }
        }

        return content.toString();
    }

    /**
     * Create an Action context from an Agent SDK message.
     * 
     * @param sdkMessage the Agent SDK message
     * @return the Action context
     */
    private ActionContext createActionContext(Message sdkMessage) {
        // Extract information from message metadata
        Map<String, Object> metadata = sdkMessage.getMetadata();
        String senderId = (String) metadata.get("senderId");
        String receiverId = (String) metadata.get("receiverId");
        String priority = (String) metadata.get("priority");
        String correlationId = (String) metadata.get("correlationId");

        // Create context with Agent-specific information
        return ActionContext.builder().protocol("a2a").clientId(senderId != null ? senderId : "a2a-client")
                .sessionId("a2a-session-" + System.currentTimeMillis())
                .correlationId(correlationId != null ? correlationId : "msg-" + System.currentTimeMillis())
                .priority(priority != null ? priority : "normal").build();
    }

    /**
     * Convert ActionResult data to Map<String, Object>.
     * 
     * @param data the ActionResult data
     * @return the converted map
     */
    private Map<String, Object> convertToMap(@Nullable Object data) {
        if (data instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> dataMap = (Map<String, Object>) data;
            return dataMap;
        } else {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("result", data != null ? data : "null");
            return resultMap;
        }
    }

    /**
     * Get the skill ID.
     * 
     * @return the skill ID
     */
    public String getSkillId() {
        return skillId;
    }

    /**
     * Get the underlying Action.
     * 
     * @return the Action
     */
    public Action getAction() {
        return action;
    }

    /**
     * Get the skill name.
     * 
     * @return the skill name
     */
    public String getSkillName() {
        return action.getActionName();
    }

    /**
     * Get the skill description.
     * 
     * @return the skill description
     */
    public String getSkillDescription() {
        return action.getDescription();
    }

    /**
     * Get the skill category.
     * 
     * @return the skill category
     */
    public String getSkillCategory() {
        return action.getCategory();
    }

    @Override
    public String toString() {
        return "AgentSkillAdapter{" + "skillId='" + skillId + '\'' + ", actionName='" + action.getActionName() + '\''
                + '}';
    }
}
