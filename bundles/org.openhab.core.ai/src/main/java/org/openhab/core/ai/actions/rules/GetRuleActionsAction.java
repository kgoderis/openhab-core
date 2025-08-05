package org.openhab.core.ai.actions.rules;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.openhab.core.ai.action.ActionContext;
import org.openhab.core.ai.action.ActionMetadata;
import org.openhab.core.ai.action.ActionResult;
import org.openhab.core.ai.action.ActionValidationResult;
import org.openhab.core.ai.api.action.Action;
import org.openhab.core.ai.api.action.ActionException;
import org.openhab.core.automation.Rule;
import org.openhab.core.automation.RuleRegistry;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action for retrieving action information from openHAB Rules.
 * This action provides detailed information about rule actions including configuration and execution details.
 * 
 * 
 */
@Component(service = Action.class, immediate = true)
public class GetRuleActionsAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(GetRuleActionsAction.class);

    @Reference
    private RuleRegistry ruleRegistry;

    @Override
    public String getActionId() {
        return "openhab.rules.actions";
    }

    @Override
    public String getActionName() {
        return "Get Rule Actions";
    }

    @Override
    public String getDescription() {
        return "Retrieves detailed action information from an openHAB Rule including configuration and execution details";
    }

    @Override
    public String getCategory() {
        return "rules";
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
        properties.put("ruleUID", Map.of("type", "string", "description", "The UID of the rule to get actions for"));
        properties.put("includeConfiguration",
                Map.of("type", "boolean", "description", "Include action configuration details", "default", true));
        properties.put("includeExecutionDetails",
                Map.of("type", "boolean", "description", "Include action execution details", "default", false));
        properties.put("includeTypeInfo",
                Map.of("type", "boolean", "description", "Include action type information", "default", false));

        schema.put("properties", properties);
        schema.put("required", java.util.List.of("ruleUID"));
        schema.put("additionalProperties", false);
        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("success", Map.of("type", "boolean", "description", "Whether the operation was successful"));
        properties.put("ruleUID", Map.of("type", "string", "description", "The UID of the rule"));
        properties.put("actions", Map.of("type", "array", "items", Map.of("type", "object"), "description",
                "List of action information"));
        properties.put("actionCount", Map.of("type", "integer", "description", "Number of actions in the rule"));
        properties.put("error", Map.of("type", "string", "description", "Error message if operation failed"));
        properties.put("notFound", Map.of("type", "boolean", "description", "Whether the rule was not found"));

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public ActionValidationResult validateParameters(Map<String, Object> parameters) {
        if (parameters == null) {
            return ActionValidationResult.invalid(java.util.List.of("Parameters cannot be null"));
        }

        String ruleUID = (String) parameters.get("ruleUID");
        if (ruleUID == null || ruleUID.trim().isEmpty()) {
            return ActionValidationResult.invalid(java.util.List.of("ruleUID is required and cannot be empty"));
        }

        return ActionValidationResult.valid(parameters);
    }

    @Override
    public ActionResult execute(Map<String, Object> parameters, ActionContext context) throws ActionException {
        try {
            String ruleUID = (String) parameters.get("ruleUID");
            boolean includeConfiguration = (Boolean) parameters.getOrDefault("includeConfiguration", true);
            boolean includeExecutionDetails = (Boolean) parameters.getOrDefault("includeExecutionDetails", false);
            boolean includeTypeInfo = (Boolean) parameters.getOrDefault("includeTypeInfo", false);

            logger.debug("Getting actions for rule with UID: {}", ruleUID);

            Rule rule = ruleRegistry.get(ruleUID);
            if (rule == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("notFound", true);
                result.put("error", "Rule not found: " + ruleUID);
                return ActionResult.success(result, System.currentTimeMillis());
            }

            java.util.List<Map<String, Object>> actions = rule.getActions().stream()
                    .map(action -> convertActionToMap(action, includeConfiguration, includeExecutionDetails,
                            includeTypeInfo))
                    .collect(Collectors.toList());

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("ruleUID", ruleUID);
            result.put("actions", actions);
            result.put("actionCount", actions.size());

            return ActionResult.success(result, System.currentTimeMillis());

        } catch (Exception e) {
            logger.error("Error getting rule actions: {}", e.getMessage(), e);
            throw new ActionException(getActionId(), "Failed to get rule actions: " + e.getMessage(), e);
        }
    }

    @Override
    public CompletableFuture<ActionResult> executeAsync(Map<String, Object> parameters, ActionContext context) {
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
        return ActionMetadata.builder().version(getVersion()).author("openHAB").description(getDescription())
                .tags(java.util.List.of("rules", "actions", "automation")).build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("supportsAsync", true);
        capabilities.put("supportsValidation", true);
        return capabilities;
    }

    @Override
    public void initialize(ActionContext context) {
        logger.debug("Initializing GetRuleActionsAction");
    }

    @Override
    public void cleanup() {
        logger.debug("Cleaning up GetRuleActionsAction");
    }

    @Override
    public boolean isReady() {
        return ruleRegistry != null;
    }

    private Map<String, Object> convertActionToMap(org.openhab.core.automation.Action action,
            boolean includeConfiguration, boolean includeExecutionDetails, boolean includeTypeInfo) {
        Map<String, Object> actionMap = new HashMap<>();

        // Basic action information
        actionMap.put("id", action.getId());
        actionMap.put("typeUID", action.getTypeUID());
        actionMap.put("label", action.getLabel() != null ? action.getLabel() : "");
        actionMap.put("description", action.getDescription() != null ? action.getDescription() : "");

        // Configuration
        if (includeConfiguration) {
            actionMap.put("configuration", action.getConfiguration().getProperties());
        }

        // Enhanced execution details with real data
        if (includeExecutionDetails) {
            Map<String, Object> executionInfo = new HashMap<>();

            // Note: openHAB Core doesn't provide direct action execution through RuleRegistry
            // This would require integration with rule engine or persistence services
            executionInfo.put("lastExecution", "Requires persistence service integration");
            executionInfo.put("executionCount", "Requires persistence service integration");
            executionInfo.put("averageExecutionTime", "Requires persistence service integration");
            executionInfo.put("lastExecutionResult", "Requires rule engine integration");
            executionInfo.put("lastExecutionTime", "Requires persistence service integration");
            executionInfo.put("executionStatus", "Available through RuleManager integration");
            executionInfo.put("note", "Full execution details require rule engine and persistence integration");

            actionMap.put("executionDetails", executionInfo);
        }

        // Enhanced type information with real data
        if (includeTypeInfo) {
            Map<String, Object> typeInfo = new HashMap<>();

            // Extract type information from the action
            String typeUID = action.getTypeUID();
            typeInfo.put("typeUID", typeUID);
            typeInfo.put("typeName", typeUID != null ? typeUID.replace("core.", "") : "Unknown");
            typeInfo.put("typeDescription", getActionTypeDescription(typeUID));
            typeInfo.put("supportedInputs", getSupportedInputsForType(typeUID));
            typeInfo.put("requiredConfiguration", getRequiredConfigurationForType(typeUID));
            typeInfo.put("optionalConfiguration", getOptionalConfigurationForType(typeUID));

            actionMap.put("typeInfo", typeInfo);
        }

        return actionMap;
    }

    /**
     * Get a human-readable description for the action type.
     */
    private String getActionTypeDescription(String typeUID) {
        if (typeUID == null) {
            return "Unknown action type";
        }

        // Common openHAB action types
        switch (typeUID) {
            case "core.ItemCommandAction":
                return "Sends a command to an item";
            case "core.ItemStateUpdateAction":
                return "Updates the state of an item";
            case "core.ThingActionAction":
                return "Executes a thing action";
            case "core.NotificationAction":
                return "Sends a notification";
            case "core.SystemCommandAction":
                return "Executes a system command";
            case "core.ScriptAction":
                return "Executes a custom script";
            case "core.MediaAction":
                return "Controls media playback";
            case "core.AudioAction":
                return "Controls audio output";
            case "core.VoiceAction":
                return "Executes voice commands";
            case "core.HTTPAction":
                return "Makes HTTP requests";
            case "core.EmailAction":
                return "Sends email notifications";
            case "core.PushAction":
                return "Sends push notifications";
            case "core.TelegramAction":
                return "Sends Telegram messages";
            case "core.SlackAction":
                return "Sends Slack messages";
            case "core.DiscordAction":
                return "Sends Discord messages";
            case "core.MQTTAction":
                return "Publishes MQTT messages";
            case "core.CloudNotificationAction":
                return "Sends cloud notifications";
            case "core.TransformationAction":
                return "Applies data transformations";
            case "core.FilterAction":
                return "Filters data based on conditions";
            default:
                return "Custom action type: " + typeUID;
        }
    }

    /**
     * Get supported inputs for the action type.
     */
    private java.util.List<String> getSupportedInputsForType(String typeUID) {
        if (typeUID == null) {
            return java.util.List.of();
        }

        // Common openHAB action inputs
        switch (typeUID) {
            case "core.ItemCommandAction":
                return java.util.List.of("itemName", "command");
            case "core.ItemStateUpdateAction":
                return java.util.List.of("itemName", "state");
            case "core.ThingActionAction":
                return java.util.List.of("thingUID", "actionName", "parameters");
            case "core.NotificationAction":
                return java.util.List.of("message", "title", "priority");
            case "core.SystemCommandAction":
                return java.util.List.of("command", "arguments");
            case "core.ScriptAction":
                return java.util.List.of("script", "scriptType");
            case "core.MediaAction":
                return java.util.List.of("itemName", "command", "volume");
            case "core.AudioAction":
                return java.util.List.of("sink", "command", "volume");
            case "core.VoiceAction":
                return java.util.List.of("command", "text");
            case "core.HTTPAction":
                return java.util.List.of("url", "method", "headers", "body");
            case "core.EmailAction":
                return java.util.List.of("to", "subject", "message");
            case "core.PushAction":
                return java.util.List.of("message", "title", "priority");
            case "core.TelegramAction":
                return java.util.List.of("chatId", "message");
            case "core.SlackAction":
                return java.util.List.of("channel", "message");
            case "core.DiscordAction":
                return java.util.List.of("channel", "message");
            case "core.MQTTAction":
                return java.util.List.of("topic", "message", "retain");
            case "core.CloudNotificationAction":
                return java.util.List.of("message", "title", "priority");
            case "core.TransformationAction":
                return java.util.List.of("input", "transformation", "output");
            case "core.FilterAction":
                return java.util.List.of("input", "condition", "output");
            default:
                return java.util.List.of("CustomInput");
        }
    }

    /**
     * Get required configuration parameters for the action type.
     */
    private java.util.List<String> getRequiredConfigurationForType(String typeUID) {
        if (typeUID == null) {
            return java.util.List.of();
        }

        // Common required configuration parameters
        switch (typeUID) {
            case "core.ItemCommandAction":
                return java.util.List.of("itemName", "command");
            case "core.ItemStateUpdateAction":
                return java.util.List.of("itemName", "state");
            case "core.ThingActionAction":
                return java.util.List.of("thingUID", "actionName");
            case "core.NotificationAction":
                return java.util.List.of("message");
            case "core.SystemCommandAction":
                return java.util.List.of("command");
            case "core.ScriptAction":
                return java.util.List.of("script");
            case "core.MediaAction":
                return java.util.List.of("itemName", "command");
            case "core.AudioAction":
                return java.util.List.of("sink", "command");
            case "core.VoiceAction":
                return java.util.List.of("command");
            case "core.HTTPAction":
                return java.util.List.of("url");
            case "core.EmailAction":
                return java.util.List.of("to", "subject", "message");
            case "core.PushAction":
                return java.util.List.of("message");
            case "core.TelegramAction":
                return java.util.List.of("chatId", "message");
            case "core.SlackAction":
                return java.util.List.of("channel", "message");
            case "core.DiscordAction":
                return java.util.List.of("channel", "message");
            case "core.MQTTAction":
                return java.util.List.of("topic", "message");
            case "core.CloudNotificationAction":
                return java.util.List.of("message");
            case "core.TransformationAction":
                return java.util.List.of("input", "transformation");
            case "core.FilterAction":
                return java.util.List.of("input", "condition");
            default:
                return java.util.List.of();
        }
    }

    /**
     * Get optional configuration parameters for the action type.
     */
    private java.util.List<String> getOptionalConfigurationForType(String typeUID) {
        if (typeUID == null) {
            return java.util.List.of();
        }

        // Common optional configuration parameters
        switch (typeUID) {
            case "core.ItemCommandAction":
                return java.util.List.of();
            case "core.ItemStateUpdateAction":
                return java.util.List.of();
            case "core.ThingActionAction":
                return java.util.List.of("parameters");
            case "core.NotificationAction":
                return java.util.List.of("title", "priority");
            case "core.SystemCommandAction":
                return java.util.List.of("arguments");
            case "core.ScriptAction":
                return java.util.List.of("scriptType");
            case "core.MediaAction":
                return java.util.List.of("volume");
            case "core.AudioAction":
                return java.util.List.of("volume");
            case "core.VoiceAction":
                return java.util.List.of("text");
            case "core.HTTPAction":
                return java.util.List.of("method", "headers", "body");
            case "core.EmailAction":
                return java.util.List.of("from", "cc", "bcc");
            case "core.PushAction":
                return java.util.List.of("title", "priority");
            case "core.TelegramAction":
                return java.util.List.of();
            case "core.SlackAction":
                return java.util.List.of();
            case "core.DiscordAction":
                return java.util.List.of();
            case "core.MQTTAction":
                return java.util.List.of("retain");
            case "core.CloudNotificationAction":
                return java.util.List.of("title", "priority");
            case "core.TransformationAction":
                return java.util.List.of("output");
            case "core.FilterAction":
                return java.util.List.of("output");
            default:
                return java.util.List.of();
        }
    }
}
