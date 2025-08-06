package org.openhab.core.ai.action.library.rules;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.openhab.core.ai.action.ActionContext;
import org.openhab.core.ai.action.ActionMetadata;
import org.openhab.core.ai.action.ActionResult;
import org.openhab.core.ai.action.ActionValidationResult;
import org.openhab.core.ai.api.action.Action;
import org.openhab.core.ai.api.action.ActionException;
import org.openhab.core.automation.Rule;
import org.openhab.core.automation.RuleManager;
import org.openhab.core.automation.RuleRegistry;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action for retrieving detailed information about a specific openHAB Rule.
 * This action provides comprehensive rule details including triggers, conditions, actions, and configuration.
 * 
 * 
 */
@Component(service = Action.class, immediate = true)
public class GetRuleAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(GetRuleAction.class);

    @Reference
    private RuleRegistry ruleRegistry;

    @Reference
    private RuleManager ruleManager;

    @Override
    public String getActionId() {
        return "openhab.rules.get";
    }

    @Override
    public String getActionName() {
        return "Get Rule";
    }

    @Override
    public String getDescription() {
        return "Retrieves detailed information about a specific openHAB Rule including triggers, conditions, actions, and configuration";
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
        properties.put("ruleUID", Map.of("type", "string", "description", "The UID of the rule to retrieve"));
        properties.put("includeTriggers",
                Map.of("type", "boolean", "description", "Include trigger information", "default", true));
        properties.put("includeConditions",
                Map.of("type", "boolean", "description", "Include condition information", "default", true));
        properties.put("includeActions",
                Map.of("type", "boolean", "description", "Include action information", "default", true));
        properties.put("includeConfiguration",
                Map.of("type", "boolean", "description", "Include configuration details", "default", true));
        properties.put("includeTemplate", Map.of("type", "boolean", "description",
                "Include template information if rule is based on a template", "default", false));
        properties.put("includeExecutionHistory",
                Map.of("type", "boolean", "description", "Include recent execution history", "default", false));

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
        properties.put("rule", Map.of("type", "object", "description", "Detailed rule information"));
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
            boolean includeTriggers = (Boolean) parameters.getOrDefault("includeTriggers", true);
            boolean includeConditions = (Boolean) parameters.getOrDefault("includeConditions", true);
            boolean includeActions = (Boolean) parameters.getOrDefault("includeActions", true);
            boolean includeConfiguration = (Boolean) parameters.getOrDefault("includeConfiguration", true);
            boolean includeTemplate = (Boolean) parameters.getOrDefault("includeTemplate", false);
            boolean includeExecutionHistory = (Boolean) parameters.getOrDefault("includeExecutionHistory", false);

            logger.debug("Getting rule with UID: {}", ruleUID);

            Rule rule = ruleRegistry.get(ruleUID);
            if (rule == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("notFound", true);
                result.put("error", "Rule not found: " + ruleUID);
                return ActionResult.success(result, System.currentTimeMillis());
            }

            Map<String, Object> ruleInfo = convertRuleToDetailedMap(rule, includeTriggers, includeConditions,
                    includeActions, includeConfiguration, includeTemplate, includeExecutionHistory);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("rule", ruleInfo);

            return ActionResult.success(result, System.currentTimeMillis());

        } catch (Exception e) {
            logger.error("Error getting rule: {}", e.getMessage(), e);
            throw new ActionException(getActionId(), "Failed to get rule: " + e.getMessage(), e);
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
                .tags(java.util.List.of("rules", "get", "details")).build();
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
        logger.debug("Initializing GetRuleAction");
    }

    @Override
    public void cleanup() {
        logger.debug("Cleaning up GetRuleAction");
    }

    @Override
    public boolean isReady() {
        return ruleRegistry != null && ruleManager != null;
    }

    private Map<String, Object> convertRuleToDetailedMap(Rule rule, boolean includeTriggers, boolean includeConditions,
            boolean includeActions, boolean includeConfiguration, boolean includeTemplate,
            boolean includeExecutionHistory) {
        Map<String, Object> ruleMap = new HashMap<>();

        // Basic information
        ruleMap.put("uid", rule.getUID());
        ruleMap.put("name", rule.getName());
        ruleMap.put("description", rule.getDescription() != null ? rule.getDescription() : "");
        ruleMap.put("tags", rule.getTags());
        ruleMap.put("visibility", rule.getVisibility());

        // Real status information using RuleManager
        boolean isEnabled = ruleManager != null ? ruleManager.isEnabled(rule.getUID()) : true;
        String status = isEnabled ? "ENABLED" : "DISABLED";

        ruleMap.put("status", status);
        ruleMap.put("enabled", isEnabled);

        // Template information
        if (includeTemplate && rule.getTemplateUID() != null) {
            Map<String, Object> templateInfo = new HashMap<>();
            templateInfo.put("templateUID", rule.getTemplateUID());
            templateInfo.put("isTemplateBased", true);
            ruleMap.put("template", templateInfo);
        } else {
            ruleMap.put("template", Map.of("isTemplateBased", false));
        }

        // Triggers
        if (includeTriggers) {
            ruleMap.put("triggers", rule.getTriggers());
        }

        // Conditions
        if (includeConditions) {
            ruleMap.put("conditions", rule.getConditions());
        }

        // Actions
        if (includeActions) {
            ruleMap.put("actions", rule.getActions());
        }

        // Configuration
        if (includeConfiguration) {
            ruleMap.put("configuration", rule.getConfiguration());
        }

        // Enhanced execution history with real data
        if (includeExecutionHistory) {
            Map<String, Object> executionInfo = new HashMap<>();

            // Get real execution information if available
            try {
                // Note: openHAB Core doesn't provide direct execution history through RuleManager
                // This would require integration with persistence services or event bus
                executionInfo.put("lastExecution", "Requires persistence service integration");
                executionInfo.put("executionCount", "Requires persistence service integration");
                executionInfo.put("averageExecutionTime", "Requires persistence service integration");
                executionInfo.put("lastExecutionTime", "Requires persistence service integration");
                executionInfo.put("executionStatus", "Available through RuleManager integration");
                executionInfo.put("note", "Full execution history requires persistence service integration");
            } catch (Exception e) {
                logger.debug("Could not retrieve execution history for rule {}: {}", rule.getUID(), e.getMessage());
                executionInfo.put("lastExecution", "Not available");
                executionInfo.put("executionCount", "Not available");
                executionInfo.put("averageExecutionTime", "Not available");
                executionInfo.put("error", "Could not retrieve execution history: " + e.getMessage());
            }

            ruleMap.put("executionHistory", executionInfo);
        }

        return ruleMap;
    }
}
