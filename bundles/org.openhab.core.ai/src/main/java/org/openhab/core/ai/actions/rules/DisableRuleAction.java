package org.openhab.core.ai.actions.rules;

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
import org.openhab.core.persistence.PersistenceService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action for disabling openHAB Rules.
 * This action disables rules to prevent them from being executed.
 * 
 * 
 */
@Component(service = Action.class, immediate = true)
public class DisableRuleAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(DisableRuleAction.class);

    @Reference
    private RuleRegistry ruleRegistry;

    @Reference
    private PersistenceService persistenceService;

    @Reference
    private RuleManager ruleManager;

    @Override
    public String getActionId() {
        return "openhab.rules.disable";
    }

    @Override
    public String getActionName() {
        return "Disable Rule";
    }

    @Override
    public String getDescription() {
        return "Disables an openHAB Rule to prevent it from being executed";
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
        properties.put("ruleUID", Map.of("type", "string", "description", "The UID of the rule to disable"));
        properties.put("disableDependencies",
                Map.of("type", "boolean", "description", "Disable dependent rules as well", "default", false));
        properties.put("temporary", Map.of("type", "boolean", "description",
                "Temporary disable (can be re-enabled easily)", "default", false));

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
        properties.put("disabled", Map.of("type", "boolean", "description", "Whether the rule was disabled"));
        properties.put("previousStatus", Map.of("type", "string", "description", "Previous status of the rule"));
        properties.put("dependenciesDisabled",
                Map.of("type", "integer", "description", "Number of dependencies disabled"));
        properties.put("temporary", Map.of("type", "boolean", "description", "Whether this is a temporary disable"));
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
            boolean disableDependencies = (Boolean) parameters.getOrDefault("disableDependencies", false);
            boolean temporary = (Boolean) parameters.getOrDefault("temporary", false);

            logger.debug("Disabling rule with UID: {}", ruleUID);

            Rule rule = ruleRegistry.get(ruleUID);
            if (rule == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("notFound", true);
                result.put("error", "Rule not found: " + ruleUID);
                return ActionResult.success(result, System.currentTimeMillis());
            }

            // Real implementation using RuleRegistry
            java.util.List<String> warnings = new java.util.ArrayList<>();
            int dependenciesDisabled = 0;
            String previousStatus = "UNKNOWN";

            // Disable dependencies if requested
            if (disableDependencies) {
                try {
                    // Note: Dependency disabling would require additional analysis
                    // For now, we'll just log that dependency disabling was requested
                    logger.debug("Dependency disabling requested for rule: {}", ruleUID);
                    dependenciesDisabled = 0; // Would need to implement dependency analysis
                    warnings.add("Dependency disabling is logged but not implemented");
                } catch (Exception e) {
                    logger.warn("Failed to disable dependencies for rule {}: {}", ruleUID, e.getMessage());
                    warnings.add("Dependency disabling failed: " + e.getMessage());
                }
            }

            // Handle temporary disable
            if (temporary) {
                try {
                    // Note: Temporary disable would require additional state management
                    // For now, we'll just log that temporary disable was requested
                    logger.debug("Temporary disable requested for rule: {}", ruleUID);
                    warnings.add("Temporary disable is logged but not implemented");
                } catch (Exception e) {
                    logger.warn("Failed to temporarily disable rule {}: {}", ruleUID, e.getMessage());
                    warnings.add("Temporary disable failed: " + e.getMessage());
                }
            }

            // Disable the rule using RuleManager
            try {
                ruleManager.setEnabled(ruleUID, false);
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("ruleUID", ruleUID);
                result.put("disabled", true);
                result.put("previousStatus", previousStatus);
                result.put("dependenciesDisabled", dependenciesDisabled);
                result.put("temporary", temporary);
                result.put("warnings", warnings);
                result.put("message", "Rule disabled");
                return ActionResult.success(result, System.currentTimeMillis());
            } catch (Exception e) {
                logger.error("Failed to disable rule {}: {}", ruleUID, e.getMessage(), e);

                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("ruleUID", ruleUID);
                result.put("disabled", false);
                result.put("previousStatus", previousStatus);
                result.put("dependenciesDisabled", dependenciesDisabled);
                result.put("temporary", temporary);
                result.put("warnings", warnings);
                result.put("error", "Failed to disable rule: " + e.getMessage());

                return ActionResult.success(result, System.currentTimeMillis());
            }

        } catch (Exception e) {
            logger.error("Error disabling rule: {}", e.getMessage(), e);
            throw new ActionException(getActionId(), "Failed to disable rule: " + e.getMessage(), e);
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
                .tags(java.util.List.of("rules", "disable", "automation")).build();
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
        logger.debug("Initializing DisableRuleAction");
    }

    @Override
    public void cleanup() {
        logger.debug("Cleaning up DisableRuleAction");
    }

    @Override
    public boolean isReady() {
        return ruleRegistry != null;
    }
}
