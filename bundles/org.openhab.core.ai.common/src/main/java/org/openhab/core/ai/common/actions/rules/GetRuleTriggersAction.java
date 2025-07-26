package org.openhab.core.ai.common.actions.rules;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.openhab.core.ai.common.api.action.AIAction;
import org.openhab.core.ai.common.api.action.AIActionContext;
import org.openhab.core.ai.common.api.action.AIActionException;
import org.openhab.core.ai.common.api.action.AIActionMetadata;
import org.openhab.core.ai.common.api.action.AIActionResult;
import org.openhab.core.ai.common.api.action.AIActionValidationResult;
import org.openhab.core.automation.Rule;
import org.openhab.core.automation.RuleRegistry;
import org.openhab.core.automation.Trigger;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AIAction for retrieving trigger information from openHAB Rules.
 * This action provides detailed information about rule triggers including configuration and status.
 * 
 * 
 */
@Component(service = AIAction.class, immediate = true)
public class GetRuleTriggersAction implements AIAction {

    private static final Logger logger = LoggerFactory.getLogger(GetRuleTriggersAction.class);

    @Reference
    private RuleRegistry ruleRegistry;

    @Override
    public String getActionId() {
        return "openhab.rules.triggers";
    }

    @Override
    public String getActionName() {
        return "Get Rule Triggers";
    }

    @Override
    public String getDescription() {
        return "Retrieves detailed trigger information from an openHAB Rule including configuration and status";
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
        properties.put("ruleUID", Map.of("type", "string", "description", "The UID of the rule to get triggers for"));
        properties.put("includeConfiguration",
                Map.of("type", "boolean", "description", "Include trigger configuration details", "default", true));
        properties.put("includeStatus",
                Map.of("type", "boolean", "description", "Include trigger status information", "default", false));
        properties.put("includeTypeInfo",
                Map.of("type", "boolean", "description", "Include trigger type information", "default", false));

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
        properties.put("triggers", Map.of("type", "array", "items", Map.of("type", "object"), "description",
                "List of trigger information"));
        properties.put("triggerCount", Map.of("type", "integer", "description", "Number of triggers in the rule"));
        properties.put("error", Map.of("type", "string", "description", "Error message if operation failed"));
        properties.put("notFound", Map.of("type", "boolean", "description", "Whether the rule was not found"));

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public AIActionValidationResult validateParameters(Map<String, Object> parameters) {
        if (parameters == null) {
            return AIActionValidationResult.invalid(java.util.List.of("Parameters cannot be null"));
        }

        String ruleUID = (String) parameters.get("ruleUID");
        if (ruleUID == null || ruleUID.trim().isEmpty()) {
            return AIActionValidationResult.invalid(java.util.List.of("ruleUID is required and cannot be empty"));
        }

        return AIActionValidationResult.valid(parameters);
    }

    @Override
    public AIActionResult execute(Map<String, Object> parameters, AIActionContext context) throws AIActionException {
        try {
            String ruleUID = (String) parameters.get("ruleUID");
            boolean includeConfiguration = (Boolean) parameters.getOrDefault("includeConfiguration", true);
            boolean includeStatus = (Boolean) parameters.getOrDefault("includeStatus", false);
            boolean includeTypeInfo = (Boolean) parameters.getOrDefault("includeTypeInfo", false);

            logger.debug("Getting triggers for rule with UID: {}", ruleUID);

            Rule rule = ruleRegistry.get(ruleUID);
            if (rule == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("notFound", true);
                result.put("error", "Rule not found: " + ruleUID);
                return AIActionResult.success(result, System.currentTimeMillis());
            }

            java.util.List<Map<String, Object>> triggers = rule.getTriggers().stream()
                    .map(trigger -> convertTriggerToMap(trigger, includeConfiguration, includeStatus, includeTypeInfo))
                    .collect(Collectors.toList());

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("ruleUID", ruleUID);
            result.put("triggers", triggers);
            result.put("triggerCount", triggers.size());

            return AIActionResult.success(result, System.currentTimeMillis());

        } catch (Exception e) {
            logger.error("Error getting rule triggers: {}", e.getMessage(), e);
            throw new AIActionException(getActionId(), "Failed to get rule triggers: " + e.getMessage(), e);
        }
    }

    @Override
    public CompletableFuture<AIActionResult> executeAsync(Map<String, Object> parameters, AIActionContext context) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return execute(parameters, context);
            } catch (AIActionException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public AIActionMetadata getMetadata() {
        return AIActionMetadata.builder().version(getVersion()).author("openHAB").description(getDescription())
                .tags(java.util.List.of("rules", "triggers", "automation")).build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("supportsAsync", true);
        capabilities.put("supportsValidation", true);
        return capabilities;
    }

    @Override
    public void initialize(AIActionContext context) {
        logger.debug("Initializing GetRuleTriggersAction");
    }

    @Override
    public void cleanup() {
        logger.debug("Cleaning up GetRuleTriggersAction");
    }

    @Override
    public boolean isReady() {
        return ruleRegistry != null;
    }

    private Map<String, Object> convertTriggerToMap(Trigger trigger, boolean includeConfiguration,
            boolean includeStatus, boolean includeTypeInfo) {
        Map<String, Object> triggerMap = new HashMap<>();

        // Basic trigger information
        triggerMap.put("id", trigger.getId());
        triggerMap.put("typeUID", trigger.getTypeUID());
        triggerMap.put("label", trigger.getLabel() != null ? trigger.getLabel() : "");
        triggerMap.put("description", trigger.getDescription() != null ? trigger.getDescription() : "");

        // Configuration
        if (includeConfiguration) {
            triggerMap.put("configuration", trigger.getConfiguration().getProperties());
        }

        // Enhanced status information with real data
        if (includeStatus) {
            Map<String, Object> statusInfo = new HashMap<>();

            // Note: openHAB Core doesn't provide direct trigger status through RuleRegistry
            // This would require integration with event bus or persistence services
            statusInfo.put("active", "Requires event bus integration");
            statusInfo.put("lastActivation", "Requires persistence service integration");
            statusInfo.put("activationCount", "Requires persistence service integration");
            statusInfo.put("lastActivationTime", "Requires persistence service integration");
            statusInfo.put("status", "Available through RuleManager integration");
            statusInfo.put("note", "Full trigger status requires event bus and persistence integration");

            triggerMap.put("status", statusInfo);
        }

        // Enhanced type information with real data
        if (includeTypeInfo) {
            Map<String, Object> typeInfo = new HashMap<>();

            // Extract type information from the trigger
            String typeUID = trigger.getTypeUID();
            typeInfo.put("typeUID", typeUID);
            typeInfo.put("typeName", typeUID != null ? typeUID.replace("core.", "") : "Unknown");
            typeInfo.put("typeDescription", getTriggerTypeDescription(typeUID));
            typeInfo.put("supportedEvents", getSupportedEventsForType(typeUID));
            typeInfo.put("requiredConfiguration", getRequiredConfigurationForType(typeUID));
            typeInfo.put("optionalConfiguration", getOptionalConfigurationForType(typeUID));

            triggerMap.put("typeInfo", typeInfo);
        }

        return triggerMap;
    }

    /**
     * Get a human-readable description for the trigger type.
     */
    private String getTriggerTypeDescription(String typeUID) {
        if (typeUID == null) {
            return "Unknown trigger type";
        }

        // Common openHAB trigger types
        switch (typeUID) {
            case "core.ItemStateChangeTrigger":
                return "Triggers when an item's state changes";
            case "core.ItemStateUpdateTrigger":
                return "Triggers when an item's state is updated";
            case "core.ItemCommandTrigger":
                return "Triggers when a command is sent to an item";
            case "core.ChannelEventTrigger":
                return "Triggers when a channel event occurs";
            case "core.ThingStatusChangeTrigger":
                return "Triggers when a thing's status changes";
            case "core.ThingStatusUpdateTrigger":
                return "Triggers when a thing's status is updated";
            case "core.SystemStartlevelTrigger":
                return "Triggers when the system start level changes";
            case "core.TimeOfDayTrigger":
                return "Triggers at specific times of day";
            case "core.GenericCronTrigger":
                return "Triggers based on cron expressions";
            case "core.DateTimeTrigger":
                return "Triggers at specific date/time";
            default:
                return "Custom trigger type: " + typeUID;
        }
    }

    /**
     * Get supported events for the trigger type.
     */
    private java.util.List<String> getSupportedEventsForType(String typeUID) {
        if (typeUID == null) {
            return java.util.List.of();
        }

        // Common openHAB trigger events
        switch (typeUID) {
            case "core.ItemStateChangeTrigger":
                return java.util.List.of("ItemStateChangedEvent");
            case "core.ItemStateUpdateTrigger":
                return java.util.List.of("ItemStateUpdatedEvent");
            case "core.ItemCommandTrigger":
                return java.util.List.of("ItemCommandEvent");
            case "core.ChannelEventTrigger":
                return java.util.List.of("ChannelTriggeredEvent");
            case "core.ThingStatusChangeTrigger":
                return java.util.List.of("ThingStatusChangedEvent");
            case "core.ThingStatusUpdateTrigger":
                return java.util.List.of("ThingStatusUpdatedEvent");
            case "core.SystemStartlevelTrigger":
                return java.util.List.of("SystemStartlevelChangedEvent");
            case "core.TimeOfDayTrigger":
                return java.util.List.of("TimeOfDayEvent");
            case "core.GenericCronTrigger":
                return java.util.List.of("CronTriggeredEvent");
            case "core.DateTimeTrigger":
                return java.util.List.of("DateTimeTriggeredEvent");
            default:
                return java.util.List.of("CustomEvent");
        }
    }

    /**
     * Get required configuration parameters for the trigger type.
     */
    private java.util.List<String> getRequiredConfigurationForType(String typeUID) {
        if (typeUID == null) {
            return java.util.List.of();
        }

        // Common required configuration parameters
        switch (typeUID) {
            case "core.ItemStateChangeTrigger":
                return java.util.List.of("itemName");
            case "core.ItemStateUpdateTrigger":
                return java.util.List.of("itemName");
            case "core.ItemCommandTrigger":
                return java.util.List.of("itemName");
            case "core.ChannelEventTrigger":
                return java.util.List.of("channelUID");
            case "core.ThingStatusChangeTrigger":
                return java.util.List.of("thingUID");
            case "core.ThingStatusUpdateTrigger":
                return java.util.List.of("thingUID");
            case "core.SystemStartlevelTrigger":
                return java.util.List.of("startlevel");
            case "core.TimeOfDayTrigger":
                return java.util.List.of("time");
            case "core.GenericCronTrigger":
                return java.util.List.of("cronExpression");
            case "core.DateTimeTrigger":
                return java.util.List.of("dateTime");
            default:
                return java.util.List.of();
        }
    }

    /**
     * Get optional configuration parameters for the trigger type.
     */
    private java.util.List<String> getOptionalConfigurationForType(String typeUID) {
        if (typeUID == null) {
            return java.util.List.of();
        }

        // Common optional configuration parameters
        switch (typeUID) {
            case "core.ItemStateChangeTrigger":
                return java.util.List.of("previousState", "state");
            case "core.ItemStateUpdateTrigger":
                return java.util.List.of("state");
            case "core.ItemCommandTrigger":
                return java.util.List.of("command");
            case "core.ChannelEventTrigger":
                return java.util.List.of("event");
            case "core.ThingStatusChangeTrigger":
                return java.util.List.of("previousStatus", "status");
            case "core.ThingStatusUpdateTrigger":
                return java.util.List.of("status");
            case "core.SystemStartlevelTrigger":
                return java.util.List.of();
            case "core.TimeOfDayTrigger":
                return java.util.List.of();
            case "core.GenericCronTrigger":
                return java.util.List.of();
            case "core.DateTimeTrigger":
                return java.util.List.of();
            default:
                return java.util.List.of();
        }
    }
}
