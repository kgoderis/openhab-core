package org.openhab.core.ai.action.library.discovery;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.action.api.Action;
import org.openhab.core.ai.action.api.ActionException;
import org.openhab.core.ai.action.api.ActionMetadata;
import org.openhab.core.ai.action.api.ActionResult;
import org.openhab.core.ai.action.api.ActionValidationResult;
import org.openhab.core.ai.common.context.ExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base discovery action for openHAB.
 * 
 * This action provides basic discovery functionality
 * for managing device discovery processes.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class DiscoveryAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(DiscoveryAction.class);
    private static final String ACTION_ID = "openhab.discovery.manage";
    private static final String ACTION_NAME = "Discovery Management";

    @Override
    public String getActionId() {
        return ACTION_ID;
    }

    @Override
    public String getActionName() {
        return ACTION_NAME;
    }

    @Override
    public String getDescription() {
        return "Basic discovery operations for openHAB - simplified version for current build";
    }

    @Override
    public String getCategory() {
        return "discovery";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public Map<String, Object> getParameterSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("action", Map.of("type", "string", "enum", List.of("status", "info"), "description",
                "Discovery action to perform"));

        schema.put("properties", properties);
        schema.put("required", List.of("action"));
        schema.put("additionalProperties", false);
        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("action", Map.of("type", "string", "description", "The action that was performed"));
        properties.put("timestamp", Map.of("type", "string", "description", "ISO timestamp of the operation"));
        properties.put("success", Map.of("type", "boolean", "description", "Whether the operation was successful"));
        properties.put("message", Map.of("type", "string", "description", "Human-readable message about the result"));

        schema.put("properties", properties);
        schema.put("required", List.of("action", "timestamp", "success", "message"));
        return schema;
    }

    @Override
    public ActionValidationResult validateParameters(Map<String, Object> parameters) {
        if (parameters == null) {
            return ActionValidationResult.invalid(List.of("Parameters cannot be null"));
        }

        String action = (String) parameters.get("action");
        if (action == null) {
            return ActionValidationResult.invalid(List.of("Missing required parameter: action"));
        }

        List<String> validActions = List.of("status", "info");
        if (!validActions.contains(action)) {
            return ActionValidationResult.invalid(List.of("Invalid action. Must be one of: " + validActions));
        }

        return ActionValidationResult.valid(Map.of());
    }

    @Override
    public ActionResult execute(Map<String, Object> parameters, ExecutionContext context) throws ActionException {
        long startTime = System.currentTimeMillis();
        logger.debug("Executing discovery action with parameters: {}", parameters);

        try {
            String action = (String) parameters.get("action");
            Map<String, Object> result = switch (action) {
                case "status" -> getDiscoveryStatus();
                case "info" -> getDiscoveryInfo();
                default -> throw new ActionException(ACTION_ID, "Unknown action: " + action);
            };

            long executionTime = System.currentTimeMillis() - startTime;
            logger.debug("Discovery action completed in {}ms", executionTime);

            return ActionResult.success(result, executionTime);

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("Failed to execute discovery operation", e);
            throw new ActionException(ACTION_ID, "Failed to execute discovery operation: " + e.getMessage(), e);
        }
    }

    @Override
    public CompletableFuture<ActionResult> executeAsync(Map<String, Object> parameters, ExecutionContext context) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return execute(parameters, context);
            } catch (ActionException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public ActionMetadata getMetadata() {
        return ActionMetadata.builder().withVersion(getVersion()).withAuthor("openHAB")
                .withDescription("Basic discovery operations for openHAB")
                .withTags(List.of("discovery", "devices", "inventory"))
                .withDocumentation("Provides basic discovery status and information for openHAB devices")
                .withExamples(List.of("{\"action\": \"status\"} - Get discovery service status",
                        "{\"action\": \"info\"} - Get discovery information and capabilities"))
                .build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        return Map.of("filtering", false, "sorting", false, "pagination", false, "metadata", true, "async", true);
    }

    @Override
    public void initialize(ExecutionContext context) {
        logger.debug("DiscoveryAction initialized for protocol: {}", context.getProtocol());
    }

    @Override
    public void cleanup() {
        logger.debug("DiscoveryAction cleanup completed");
    }

    @Override
    public boolean isReady() {
        return true; // No external dependencies required
    }

    private Map<String, Object> getDiscoveryStatus() {
        Map<String, Object> result = new HashMap<>();
        result.put("action", "status");
        result.put("timestamp", Instant.now().toString());
        result.put("success", true);
        result.put("discoveryAvailable", false);
        result.put("message", "Discovery APIs not available in current openHAB version");
        result.put("note", "Full discovery functionality will be available when discovery APIs are included");
        return result;
    }

    private Map<String, Object> getDiscoveryInfo() {
        Map<String, Object> result = new HashMap<>();
        result.put("action", "info");
        result.put("timestamp", Instant.now().toString());
        result.put("success", true);
        result.put("discoveryServices", List.of());
        result.put("discoveryResults", List.of());
        result.put("message", "Discovery functionality is currently disabled");
        result.put("alternativeApproach", "Use Thing management tools to manually configure devices");
        result.put("supportedActions", List.of("status", "info"));
        return result;
    }
}
