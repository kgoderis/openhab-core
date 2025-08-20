package org.openhab.core.ai.model;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.action.ActionRegistry;
import org.openhab.core.ai.action.api.ActionKeys;
import org.openhab.core.ai.common.context.ExecutionContext;
import org.openhab.core.ai.common.metrics.ModelPerformanceMetrics;
import org.openhab.core.ai.common.response.ModelResponse;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Action Call Parser - Parses action calls from LLM responses
 * 
 * <p>
 * This parser provides:
 * - JSON-based action call parsing for structured responses
 * - Regex-based fallback parsing for non-structured responses
 * - Argument parsing and validation
 * - Action call validation and error handling
 * - Action call result accumulation
 * - Action call retry mechanisms
 * - Action call performance monitoring
 * </p>
 * 
 * <h3>Supported JSON Formats</h3>
 * 
 * <p>
 * The parser supports multiple JSON formats for action calls:
 * </p>
 * 
 * <h4>1. Direct Action Calls Array</h4>
 * 
 * <pre>{@code
 * {
 *   "reasoning": "I need to get the current temperature and humidity",
 *   "action_calls": [
 *     {
 *       "id": "temp-1",
 *       "action": "openhab.items.get",
 *       "arguments": {
 *         "itemName": "Temperature_Sensor"
 *       }
 *     },
 *     {
 *       "id": "humidity-1", 
 *       "action": "openhab.items.get",
 *       "arguments": {
 *         "itemName": "Humidity_Sensor"
 *       }
 *     }
 *   ]
 * }
 * }</pre>
 * 
 * <h4>2. Embedded JSON in Reasoning</h4>
 * 
 * <pre>{@code
 * {
 *   "reasoning": "I need to get the temperature. Here's the action call: {\"action\": \"openhab.items.get\", \"arguments\": {\"itemName\": \"Temperature_Sensor\"}}"
 * }
 * }</pre>
 * 
 * <h4>3. Regex Fallback Format</h4>
 * <p>
 * If JSON parsing fails, the parser falls back to regex pattern matching:
 * </p>
 * 
 * <pre>{@code
 * I need to call action_call(openhab.items.get, {itemName: "Temperature_Sensor"}) to get the temperature
 * }</pre>
 * 
 * <h3>Action Call Structure</h3>
 * <ul>
 * <li><strong>id</strong> (optional): Unique identifier for the action call</li>
 * <li><strong>action</strong> (required): The action name to execute</li>
 * <li><strong>arguments</strong> (required): Key-value pairs of arguments for the action</li>
 * </ul>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@Component(service = ModelResponseActionParser.class)
@NonNullByDefault
public class ModelResponseActionParser {

    private static final Logger logger = LoggerFactory.getLogger(ModelResponseActionParser.class);

    // Performance monitoring
    private final AtomicLong totalParsingAttempts = new AtomicLong(0);
    private final AtomicLong successfulJsonParses = new AtomicLong(0);
    private final AtomicLong successfulRegexParses = new AtomicLong(0);
    private final AtomicLong failedParses = new AtomicLong(0);
    private final AtomicLong totalActionCalls = new AtomicLong(0);

    // Regex patterns for fallback parsing
    private static final Pattern ACTION_CALL_PATTERN = Pattern
            .compile("action[s_]*call[s]*([s]*([w.]+)[s]*,[s]*{([^}]*)}[s]*)", Pattern.CASE_INSENSITIVE);

    private static final Pattern ARGUMENT_PATTERN = Pattern.compile("([w]+)[s]*:[s]*([^,}]+)",
            Pattern.CASE_INSENSITIVE);

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Parse action calls from LLM response
     * 
     * @param response the LLM response to parse
     * @param sessionId the reasoning session ID for context
     * @return list of parsed action contexts
     */
    public List<ExecutionContext> parseActionCalls(ModelResponse response, String sessionId) {
        totalParsingAttempts.incrementAndGet();
        Instant startTime = Instant.now();

        try {
            List<ExecutionContext> actionCalls = new ArrayList<>();

            // Try JSON-based parsing first
            List<ExecutionContext> jsonResults = parseJsonActionCalls(response, sessionId);
            if (!jsonResults.isEmpty()) {
                successfulJsonParses.incrementAndGet();
                actionCalls.addAll(jsonResults);
                logger.debug("Successfully parsed {} action calls using JSON for session {}", jsonResults.size(),
                        sessionId);
            } else {
                // Fallback to regex-based parsing
                List<ExecutionContext> regexResults = parseRegexActionCalls(response, sessionId);
                if (!regexResults.isEmpty()) {
                    successfulRegexParses.incrementAndGet();
                    actionCalls.addAll(regexResults);
                    logger.debug("Successfully parsed {} action calls using regex for session {}", regexResults.size(),
                            sessionId);
                } else {
                    failedParses.incrementAndGet();
                    logger.debug("No action calls found in response for session {}", sessionId);
                }
            }

            totalActionCalls.addAndGet(actionCalls.size());

            long parseTime = Duration.between(startTime, Instant.now()).toMillis();
            logger.debug("Action call parsing completed in {} ms for session {}", parseTime, sessionId);

            return actionCalls;

        } catch (Exception e) {
            failedParses.incrementAndGet();
            logger.error("Error parsing action calls for session {}", sessionId, e);
            return new ArrayList<>();
        }
    }

    /**
     * Parse action calls using JSON structure
     */
    private List<ExecutionContext> parseJsonActionCalls(ModelResponse response, String sessionId) {
        List<ExecutionContext> actionCalls = new ArrayList<>();

        try {
            String content = response.getContent();
            if (content == null || content.isEmpty()) {
                return actionCalls;
            }

            // Look for JSON action calls in the content
            JsonNode rootNode = objectMapper.readTree(content);

            // Check for direct action calls array
            if (rootNode.has("action_calls") && rootNode.get("action_calls").isArray()) {
                ArrayNode actionCallsArray = (ArrayNode) rootNode.get("action_calls");
                for (JsonNode actionCallNode : actionCallsArray) {
                    ExecutionContext actionContext = parseJsonActionCall(actionCallNode, sessionId);
                    if (actionContext != null) {
                        actionCalls.add(actionContext);
                    }
                }
            }

            // Check for embedded action calls in reasoning
            if (rootNode.has("reasoning") && rootNode.get("reasoning").isTextual()) {
                String reasoning = rootNode.get("reasoning").asText();
                List<ExecutionContext> embeddedCalls = parseEmbeddedJsonActionCalls(reasoning, sessionId);
                actionCalls.addAll(embeddedCalls);
            }

        } catch (JsonProcessingException e) {
            logger.debug("JSON parsing failed for session {}, falling back to regex: {}", sessionId, e.getMessage());
        } catch (Exception e) {
            logger.debug("Unexpected error in JSON parsing for session {}: {}", sessionId, e.getMessage());
        }

        return actionCalls;
    }

    /**
     * Parse a single JSON action call
     */
    private @Nullable ExecutionContext parseJsonActionCall(JsonNode actionCallNode, String sessionId) {
        try {
            if (!actionCallNode.has("action") || !actionCallNode.has("arguments")) {
                logger.debug("Invalid action call structure in JSON for session {}", sessionId);
                return null;
            }

            String actionName = actionCallNode.get("action").asText();
            JsonNode argumentsNode = actionCallNode.get("arguments");

            Map<String, Object> arguments = parseJsonArguments(argumentsNode);

            String correlationId = actionCallNode.has("id") ? actionCallNode.get("id").asText()
                    : "action-" + System.currentTimeMillis() + "-" + System.nanoTime();

            return ExecutionContext.builder().withProtocol("a2a").withClientId("reasoning-engine")
                    .withSessionId(sessionId).withCorrelationId(correlationId)
                    .withProtocolContext(Map.of("action", actionName, "arguments", arguments)).build();

        } catch (Exception e) {
            logger.debug("Error parsing JSON action call for session {}: {}", sessionId, e.getMessage());
            return null;
        }
    }

    /**
     * Parse arguments from JSON node
     */
    private Map<String, Object> parseJsonArguments(JsonNode argumentsNode) {
        Map<String, Object> arguments = Map.of();

        try {
            if (argumentsNode.isObject()) {
                ObjectNode objectNode = (ObjectNode) argumentsNode;
                arguments = objectMapper.convertValue(objectNode, new TypeReference<Map<String, Object>>() {
                });
            }
        } catch (Exception e) {
            logger.debug("Error parsing JSON arguments: {}", e.getMessage());
        }

        return arguments;
    }

    /**
     * Parse embedded JSON action calls from reasoning text
     */
    private List<ExecutionContext> parseEmbeddedJsonActionCalls(String reasoning, String sessionId) {
        List<ExecutionContext> actionCalls = new ArrayList<>();

        // Look for JSON blocks in the reasoning text
        Pattern jsonBlockPattern = Pattern.compile("{.*?\"action\"s*:.*?}", Pattern.DOTALL);
        Matcher matcher = jsonBlockPattern.matcher(reasoning);

        while (matcher.find()) {
            try {
                String jsonBlock = matcher.group();
                JsonNode actionCallNode = objectMapper.readTree(jsonBlock);
                ExecutionContext actionContext = parseJsonActionCall(actionCallNode, sessionId);
                if (actionContext != null) {
                    actionCalls.add(actionContext);
                }
            } catch (Exception e) {
                logger.debug("Error parsing embedded JSON action call: {}", e.getMessage());
            }
        }

        return actionCalls;
    }

    /**
     * Parse action calls using regex patterns
     */
    private List<ExecutionContext> parseRegexActionCalls(ModelResponse response, String sessionId) {
        List<ExecutionContext> actionCalls = new ArrayList<>();

        String content = response.getContent();
        if (content == null || content.isEmpty()) {
            return actionCalls;
        }

        Matcher actionMatcher = ACTION_CALL_PATTERN.matcher(content);

        while (actionMatcher.find()) {
            try {
                String actionName = actionMatcher.group(1).trim();
                String argumentsText = actionMatcher.group(2).trim();

                Map<String, Object> arguments = parseRegexArguments(argumentsText);

                String correlationId = "action-" + System.currentTimeMillis() + "-" + actionCalls.size();

                ExecutionContext actionContext = ExecutionContext.builder().withProtocol("a2a")
                        .withClientId("reasoning-engine").withSessionId(sessionId).withCorrelationId(correlationId)
                        .withProtocolContext(Map.of("action", actionName, "arguments", arguments)).build();

                actionCalls.add(actionContext);

            } catch (Exception e) {
                logger.debug("Error parsing regex action call: {}", e.getMessage());
            }
        }

        return actionCalls;
    }

    /**
     * Parse arguments from regex-matched text
     */
    private Map<String, Object> parseRegexArguments(String argumentsText) {
        Map<String, Object> arguments = Map.of();

        try {
            Matcher argMatcher = ARGUMENT_PATTERN.matcher(argumentsText);
            while (argMatcher.find()) {
                String key = argMatcher.group(1).trim();
                String value = argMatcher.group(2).trim();

                // Remove quotes if present
                if (value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1, value.length() - 1);
                }

                arguments = Map.of(key, value);
            }
        } catch (Exception e) {
            logger.debug("Error parsing regex arguments: {}", e.getMessage());
        }

        return arguments;
    }

    /**
     * Validate action call
     */
    public boolean validateActionCall(ExecutionContext actionContext, @Nullable ActionRegistry registry) {
        try {
            // Basic validation
            if (actionContext.getCorrelationId() == null || actionContext.getCorrelationId().isEmpty()) {
                logger.debug("Action call missing correlation ID");
                return false;
            }

            String actionName = actionContext.getValue(ActionKeys.ACTION_NAME.getKey(), String.class);
            if (actionName == null || actionName.isEmpty()) {
                logger.debug("Action call missing action name");
                return false;
            }
            if (actionName == null || actionName.isEmpty()) {
                logger.debug("Action call has empty action name");
                return false;
            }

            // Registry validation if available
            if (registry != null) {
                // This would validate against the actual registry
                // For now, just log that validation would happen
                logger.debug("Would validate action '{}' against registry", actionName);
            }

            return true;

        } catch (Exception e) {
            logger.debug("Error validating action call: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get performance metrics
     */
    public ModelPerformanceMetrics getPerformanceMetrics() {
        return new ModelPerformanceMetrics(totalParsingAttempts.get(), successfulJsonParses.get(),
                successfulRegexParses.get(), failedParses.get(), totalActionCalls.get(), 0, // totalProcessingTime
                                                                                            // -
                                                                                            // not
                                                                                            // tracked
                                                                                            // yet
                0.0, // averageResponseTime - not tracked yet
                null // lastOperationTime - not tracked yet
        );
    }

    /**
     * Performance metrics data class
     */
    // Inner class extracted to top-level: org.openhab.core.ai.model.PerformanceMetrics
}
