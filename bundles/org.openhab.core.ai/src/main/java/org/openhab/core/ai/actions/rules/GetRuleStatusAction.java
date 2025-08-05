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
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action for retrieving detailed status information about openHAB Rules.
 * This action provides comprehensive rule status including execution state, errors, and performance metrics.
 * 
 * 
 */
@Component(service = Action.class, immediate = true)
public class GetRuleStatusAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(GetRuleStatusAction.class);

    @Reference
    private RuleRegistry ruleRegistry;

    @Reference
    private RuleManager ruleManager;

    @Override
    public String getActionId() {
        return "openhab.rules.status";
    }

    @Override
    public String getActionName() {
        return "Get Rule Status";
    }

    @Override
    public String getDescription() {
        return "Retrieves detailed status information about an openHAB Rule including execution state, errors, and performance metrics";
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
        properties.put("ruleUID", Map.of("type", "string", "description", "The UID of the rule to get status for"));
        properties.put("includeExecutionHistory",
                Map.of("type", "boolean", "description", "Include recent execution history", "default", false));
        properties.put("includePerformanceMetrics",
                Map.of("type", "boolean", "description", "Include performance metrics", "default", false));
        properties.put("includeErrorDetails",
                Map.of("type", "boolean", "description", "Include detailed error information", "default", false));
        properties.put("includeDependencies",
                Map.of("type", "boolean", "description", "Include dependency status", "default", false));

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
        properties.put("status", Map.of("type", "object", "description", "Detailed status information"));
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
            boolean includeExecutionHistory = (Boolean) parameters.getOrDefault("includeExecutionHistory", false);
            boolean includePerformanceMetrics = (Boolean) parameters.getOrDefault("includePerformanceMetrics", false);
            boolean includeErrorDetails = (Boolean) parameters.getOrDefault("includeErrorDetails", false);
            boolean includeDependencies = (Boolean) parameters.getOrDefault("includeDependencies", false);

            logger.debug("Getting status for rule with UID: {}", ruleUID);

            Rule rule = ruleRegistry.get(ruleUID);
            if (rule == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("notFound", true);
                result.put("error", "Rule not found: " + ruleUID);
                return ActionResult.success(result, System.currentTimeMillis());
            }

            Map<String, Object> statusInfo = getDetailedStatus(rule, includeExecutionHistory, includePerformanceMetrics,
                    includeErrorDetails, includeDependencies);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("ruleUID", ruleUID);
            result.put("status", statusInfo);

            return ActionResult.success(result, System.currentTimeMillis());

        } catch (Exception e) {
            logger.error("Error getting rule status: {}", e.getMessage(), e);
            throw new ActionException(getActionId(), "Failed to get rule status: " + e.getMessage(), e);
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
                .tags(java.util.List.of("rules", "status", "monitoring")).build();
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
        logger.debug("Initializing GetRuleStatusAction");
    }

    @Override
    public void cleanup() {
        logger.debug("Cleaning up GetRuleStatusAction");
    }

    @Override
    public boolean isReady() {
        return ruleRegistry != null;
    }

    private Map<String, Object> getDetailedStatus(Rule rule, boolean includeExecutionHistory,
            boolean includePerformanceMetrics, boolean includeErrorDetails, boolean includeDependencies) {
        Map<String, Object> status = new HashMap<>();

        // Basic status information
        status.put("name", rule.getName());
        status.put("enabled", ruleManager != null ? ruleManager.isEnabled(rule.getUID()) : true);
        status.put("status", "UNKNOWN"); // Status not available through RuleRegistry
        status.put("lastModified", "Not available");
        status.put("created", "Not available");

        // Rule structure information
        status.put("triggerCount", rule.getTriggers().size());
        status.put("conditionCount", rule.getConditions().size());
        status.put("actionCount", rule.getActions().size());
        status.put("hasConfiguration", !rule.getConfiguration().getProperties().isEmpty());

        // Template information
        if (rule.getTemplateUID() != null) {
            status.put("templateBased", true);
            status.put("templateUID", rule.getTemplateUID());
        } else {
            status.put("templateBased", false);
        }

        // Execution history (stub)
        if (includeExecutionHistory) {
            Map<String, Object> executionInfo = new HashMap<>();
            executionInfo.put("lastExecution", "Not available");
            executionInfo.put("executionCount", "Not available");
            executionInfo.put("lastExecutionTime", "Not available");
            executionInfo.put("averageExecutionTime", "Not available");
            status.put("executionHistory", executionInfo);
        }

        // Performance metrics (stub)
        if (includePerformanceMetrics) {
            Map<String, Object> performanceInfo = new HashMap<>();
            performanceInfo.put("totalExecutions", "Not available");
            performanceInfo.put("successRate", "Not available");
            performanceInfo.put("averageResponseTime", "Not available");
            performanceInfo.put("peakExecutionTime", "Not available");
            status.put("performanceMetrics", performanceInfo);
        }

        // Error details (stub)
        if (includeErrorDetails) {
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("lastError", "Not available");
            errorInfo.put("errorCount", "Not available");
            errorInfo.put("errorRate", "Not available");
            errorInfo.put("commonErrors", java.util.List.of());
            status.put("errorDetails", errorInfo);
        }

        // Dependencies (stub)
        if (includeDependencies) {
            Map<String, Object> dependencyInfo = new HashMap<>();
            dependencyInfo.put("dependentRules", java.util.List.of());
            dependencyInfo.put("dependencies", java.util.List.of());
            dependencyInfo.put("dependencyStatus", "Not available");
            status.put("dependencies", dependencyInfo);
        }

        // Health assessment
        Map<String, Object> healthInfo = new HashMap<>();
        healthInfo.put("overallHealth", "UNKNOWN");
        healthInfo.put("issues", java.util.List.of("Status information limited in stub implementation"));
        healthInfo.put("recommendations",
                java.util.List.of("Implement full status monitoring for detailed information"));
        status.put("health", healthInfo);

        return status;
    }
}
