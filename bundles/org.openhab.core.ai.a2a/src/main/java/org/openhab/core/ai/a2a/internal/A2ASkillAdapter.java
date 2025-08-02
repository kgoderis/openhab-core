package org.openhab.core.ai.a2a.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.a2a.api.skill.A2ASkillException;
import org.openhab.core.ai.a2a.api.skill.A2ASkillResult;
import org.openhab.core.ai.common.action.AIActionContext;
import org.openhab.core.ai.common.action.AIActionResult;
import org.openhab.core.ai.common.api.action.AIAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.a2a.spec.Message;

/**
 * Skill adapter for A2A operations.
 * 
 * @author AI Assistant
 * @since 1.0.0
 */
@NonNullByDefault
public class A2ASkillAdapter {

    private static final Logger logger = LoggerFactory.getLogger(A2ASkillAdapter.class);

    private final String skillId;
    private final AIAction action;

    /**
     * Create a new A2A skill adapter.
     * 
     * @param skillId the skill ID
     * @param action the AIAction to adapt
     */
    public A2ASkillAdapter(String skillId, AIAction action) {
        this.skillId = skillId;
        this.action = action;
        logger.debug("Created A2A skill adapter for skill: {}", skillId);
    }

    /**
     * Execute the skill with the given A2A SDK message.
     * 
     * @param sdkMessage the A2A SDK message containing parameters
     * @return the skill execution result
     * @throws A2ASkillException if execution fails
     */
    public A2ASkillResult execute(Message sdkMessage) throws A2ASkillException {
        long startTime = System.currentTimeMillis();

        try {
            logger.debug("Executing A2A skill {} with SDK message", skillId);

            // Extract parameters from A2A SDK message
            Map<String, Object> parameters = extractParameters(sdkMessage);

            // Create AIAction context from A2A SDK message
            AIActionContext aiContext = createAIActionContext(sdkMessage);

            // Execute the AIAction asynchronously
            CompletableFuture<AIActionResult> future = action.executeAsync(parameters, aiContext);

            // Wait for completion and convert result
            AIActionResult aiResult = future.get();
            long executionTime = System.currentTimeMillis() - startTime;

            if (aiResult.isSuccess()) {
                // Convert AIActionResult data to Map<String, Object>
                Map<String, Object> resultData = convertToMap(aiResult.getData());
                return A2ASkillResult.success(resultData, executionTime);
            } else {
                return A2ASkillResult.failure(aiResult.getMessage(), "EXECUTION_FAILED", executionTime);
            }

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("Error executing skill: {}", skillId, e);
            return A2ASkillResult.failure("Error executing skill: " + e.getMessage(), "EXECUTION_ERROR", executionTime);
        }
    }

    /**
     * Extract parameters from an A2A SDK message.
     * 
     * @param sdkMessage the A2A SDK message
     * @return the extracted parameters
     */
    private Map<String, Object> extractParameters(Message sdkMessage) {
        Map<String, Object> parameters = new HashMap<>();

        // Extract parameters from message parts (following A2AAgentExecutor pattern)
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
     * @param message the A2A message
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
     * Create an AIAction context from an A2A SDK message.
     * 
     * @param sdkMessage the A2A SDK message
     * @return the AIAction context
     */
    private AIActionContext createAIActionContext(Message sdkMessage) {
        // Extract information from message metadata
        Map<String, Object> metadata = sdkMessage.getMetadata();
        String senderId = (String) metadata.get("senderId");
        String receiverId = (String) metadata.get("receiverId");
        String priority = (String) metadata.get("priority");
        String correlationId = (String) metadata.get("correlationId");

        // Create context with A2A-specific information
        return AIActionContext.builder().protocol("a2a").clientId(senderId != null ? senderId : "a2a-client")
                .sessionId("a2a-session-" + System.currentTimeMillis())
                .correlationId(correlationId != null ? correlationId : "msg-" + System.currentTimeMillis())
                .priority(priority != null ? priority : "normal").build();
    }

    /**
     * Convert AIActionResult data to Map<String, Object>.
     * 
     * @param data the AIActionResult data
     * @return the converted map
     */
    private Map<String, Object> convertToMap(Object data) {
        if (data instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> dataMap = (Map<String, Object>) data;
            return dataMap;
        } else {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("result", data);
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
     * Get the underlying AIAction.
     * 
     * @return the AIAction
     */
    public AIAction getAction() {
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
        return "A2ASkillAdapter{" + "skillId='" + skillId + '\'' + ", actionName='" + action.getActionName() + '\''
                + '}';
    }
}
