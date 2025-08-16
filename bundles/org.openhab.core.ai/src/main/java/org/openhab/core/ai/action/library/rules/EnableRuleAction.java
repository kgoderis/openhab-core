package org.openhab.core.ai.action.library.rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.openhab.core.ai.action.api.Action;
import org.openhab.core.ai.action.api.ActionContext;
import org.openhab.core.ai.action.api.ActionException;
import org.openhab.core.ai.action.api.ActionMetadata;
import org.openhab.core.ai.action.api.ActionResult;
import org.openhab.core.ai.action.api.ActionValidationResult;
import org.openhab.core.automation.Rule;
import org.openhab.core.automation.RuleManager;
import org.openhab.core.automation.RuleRegistry;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action for enabling openHAB Rules.
 * This action enables rules to be executed when their triggers are activated.
 * 
 * 
 */
@Component(service = Action.class, immediate = true)
public class EnableRuleAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(EnableRuleAction.class);

    @Reference
    private RuleRegistry ruleRegistry;

    @Reference
    private RuleManager ruleManager;

    @Override
    public String getActionId() {
        return "openhab.rules.enable";
    }

    @Override
    public String getActionName() {
        return "Enable Rule";
    }

    @Override
    public String getDescription() {
        return "Enables an openHAB Rule to be executed when its triggers are activated";
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
        properties.put("ruleUID", Map.of("type", "string", "description", "The UID of the rule to enable"));
        properties.put("validateBeforeEnable",
                Map.of("type", "boolean", "description", "Validate rule before enabling", "default", true));
        properties.put("enableDependencies",
                Map.of("type", "boolean", "description", "Enable dependent rules as well", "default", false));

        schema.put("properties", properties);
        schema.put("required", List.of("ruleUID"));
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
        properties.put("enabled", Map.of("type", "boolean", "description", "Whether the rule was enabled"));
        properties.put("previousStatus", Map.of("type", "string", "description", "Previous status of the rule"));
        properties.put("validationPassed", Map.of("type", "boolean", "description", "Whether validation passed"));
        properties.put("dependenciesEnabled",
                Map.of("type", "integer", "description", "Number of dependencies enabled"));
        properties.put("error", Map.of("type", "string", "description", "Error message if operation failed"));
        properties.put("notFound", Map.of("type", "boolean", "description", "Whether the rule was not found"));
        properties.put("warnings",
                Map.of("type", "array", "items", Map.of("type", "string"), "description", "Warning messages"));

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public ActionValidationResult validateParameters(Map<String, Object> parameters) {
        if (parameters == null) {
            return ActionValidationResult.invalid(List.of("Parameters cannot be null"));
        }

        String ruleUID = (String) parameters.get("ruleUID");
        if (ruleUID == null || ruleUID.trim().isEmpty()) {
            return ActionValidationResult.invalid(List.of("ruleUID is required and cannot be empty"));
        }

        return ActionValidationResult.valid(parameters);
    }

    @Override
    public ActionResult execute(Map<String, Object> parameters, ActionContext context) throws ActionException {
        try {
            String ruleUID = (String) parameters.get("ruleUID");
            boolean validateBeforeEnable = (Boolean) parameters.getOrDefault("validateBeforeEnable", true);
            boolean enableDependencies = (Boolean) parameters.getOrDefault("enableDependencies", false);

            logger.debug("Enabling rule with UID: {}", ruleUID);

            Rule rule = ruleRegistry.get(ruleUID);
            if (rule == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("notFound", true);
                result.put("error", "Rule not found: " + ruleUID);
                return ActionResult.success(result, System.currentTimeMillis());
            }

            // Real implementation using RuleRegistry
            List<String> warnings = new ArrayList<>();
            boolean validationPassed = true;
            int dependenciesEnabled = 0;
            String previousStatus = "UNKNOWN";

            // Validate the rule if requested
            if (validateBeforeEnable) {
                try {
                    // Basic validation - check if rule has required components
                    if (rule.getTriggers().isEmpty()) {
                        validationPassed = false;
                        warnings.add("Rule has no triggers");
                    }
                    if (rule.getActions().isEmpty()) {
                        validationPassed = false;
                        warnings.add("Rule has no actions");
                    }

                    if (validationPassed) {
                        logger.debug("Rule validation passed for: {}", ruleUID);
                    } else {
                        logger.warn("Rule validation failed for: {}", ruleUID);
                    }
                } catch (Exception e) {
                    logger.warn("Error during rule validation for {}: {}", ruleUID, e.getMessage());
                    validationPassed = false;
                    warnings.add("Validation error: " + e.getMessage());
                }
            }

            // Enable dependencies if requested
            if (enableDependencies) {
                try {
                    // Note: Dependency enabling would require additional analysis
                    // For now, we'll just log that dependency enabling was requested
                    logger.debug("Dependency enabling requested for rule: {}", ruleUID);
                    dependenciesEnabled = 0; // Would need to implement dependency analysis
                    warnings.add("Dependency enabling is logged but not implemented");
                } catch (Exception e) {
                    logger.warn("Failed to enable dependencies for rule {}: {}", ruleUID, e.getMessage());
                    warnings.add("Dependency enabling failed: " + e.getMessage());
                }
            }

            // Enable the rule using RuleManager
            try {
                ruleManager.setEnabled(ruleUID, true);
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("ruleUID", ruleUID);
                result.put("enabled", true);
                result.put("previousStatus", previousStatus);
                result.put("validationPassed", validationPassed);
                result.put("dependenciesEnabled", dependenciesEnabled);
                result.put("warnings", warnings);
                result.put("message", "Rule enabled");
                return ActionResult.success(result, System.currentTimeMillis());
            } catch (Exception e) {
                logger.error("Failed to enable rule {}: {}", ruleUID, e.getMessage(), e);

                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("ruleUID", ruleUID);
                result.put("enabled", false);
                result.put("previousStatus", previousStatus);
                result.put("validationPassed", validationPassed);
                result.put("dependenciesEnabled", dependenciesEnabled);
                result.put("warnings", warnings);
                result.put("error", "Failed to enable rule: " + e.getMessage());

                return ActionResult.success(result, System.currentTimeMillis());
            }

        } catch (Exception e) {
            logger.error("Error enabling rule: {}", e.getMessage(), e);
            throw new ActionException(getActionId(), "Failed to enable rule: " + e.getMessage(), e);
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
                .tags(List.of("rules", "enable", "automation")).build();
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
        logger.debug("Initializing EnableRuleAction");
    }

    @Override
    public void cleanup() {
        logger.debug("Cleaning up EnableRuleAction");
    }

    @Override
    public boolean isReady() {
        return ruleRegistry != null;
    }
}
