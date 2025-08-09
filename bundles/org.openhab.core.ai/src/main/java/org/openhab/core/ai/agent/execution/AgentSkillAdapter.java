package org.openhab.core.ai.agent.execution;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.action.ActionContext;
import org.openhab.core.ai.action.ActionResult;
import org.openhab.core.ai.action.api.Action;
import org.openhab.core.ai.agent.api.AgentSkillException;
import org.openhab.core.ai.agent.api.AgentSkillResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.a2a.spec.Message;

/**
 * Protocol Adaptation and Action Bridging.
 * 
 * <p>
 * <strong>Primary Responsibility:</strong> Protocol Adaptation and Action Bridging
 * </p>
 * 
 * <p>
 * <strong>Concerns:</strong>
 * <ul>
 * <li><strong>Protocol Adaptation:</strong> Adapts A2A protocol to Action execution</li>
 * <li><strong>Parameter Extraction:</strong> Extracts parameters from A2A Message objects</li>
 * <li><strong>Context Creation:</strong> Creates ActionContext from A2A Message</li>
 * <li><strong>Result Conversion:</strong> Converts Action results to AgentSkillResult</li>
 * <li><strong>Error Adaptation:</strong> Adapts Action errors to skill errors</li>
 * <li><strong>Protocol Bridging:</strong> Bridges A2A protocol to Action protocol</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>What this class DOES:</strong>
 * <ul>
 * <li>Adapts A2A protocol to Action execution via {@link #execute(Message)}</li>
 * <li>Extracts parameters from A2A Message objects</li>
 * <li>Creates ActionContext from A2A Message</li>
 * <li>Converts Action results to AgentSkillResult format</li>
 * <li>Adapts Action errors to skill errors</li>
 * <li>Bridges A2A protocol to Action protocol</li>
 * <li>Provides skill metadata (name, description, category)</li>
 * <li>Delegates actual execution to {@link Action#executeAsync}</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>What this class DOES NOT do:</strong>
 * <ul>
 * <li>❌ Register skills (delegates to AgentSkillRegistry)</li>
 * <li>❌ Handle protocol communication (delegates to AgentProtocolHandler)</li>
 * <li>❌ Manage task lifecycle (delegates to AgentTaskManager)</li>
 * <li>❌ Handle authentication (delegates to security manager)</li>
 * <li>❌ Track execution metrics (delegates to AgentSkillExecutor)</li>
 * <li>❌ Manage skill lifecycle (delegates to AgentSkillRegistry)</li>
 * <li>❌ Execute business logic (delegates to Action.executeAsync)</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>Boundary Conditions:</strong>
 * <ul>
 * <li>Only adapts A2A protocol to Action protocol</li>
 * <li>Does not execute business logic</li>
 * <li>Does not manage skill registration</li>
 * <li>Does not handle protocol communication</li>
 * <li>Does not manage task lifecycle</li>
 * <li>Does not track execution metrics</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>Dependencies:</strong>
 * <ul>
 * <li>{@link Action}: For actual execution</li>
 * <li>A2A SDK classes: For Message handling</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>Architecture Layer:</strong> Skill Adaptation Layer
 * </p>
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
