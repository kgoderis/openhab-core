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
 * Action for performing bulk operations on multiple openHAB Rules.
 * This action supports operations like enable, disable, delete, and validation across multiple rules.
 * 
 * 
 */
@Component(service = Action.class, immediate = true)
public class BulkRuleOperationsAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(BulkRuleOperationsAction.class);

    @Reference
    private RuleRegistry ruleRegistry;

    @Reference
    private RuleManager ruleManager;

    @Override
    public String getActionId() {
        return "openhab.rules.bulk";
    }

    @Override
    public String getActionName() {
        return "Bulk Rule Operations";
    }

    @Override
    public String getDescription() {
        return "Performs bulk operations on multiple openHAB Rules including enable, disable, delete, and validation";
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
        properties.put("operation", Map.of("type", "string", "enum", List.of("enable", "disable", "delete", "validate"),
                "description", "Type of bulk operation to perform"));
        properties.put("ruleUIDs", Map.of("type", "array", "items", Map.of("type", "string"), "description",
                "List of rule UIDs to operate on"));
        properties.put("query",
                Map.of("type", "string", "description", "Search query to find rules (alternative to ruleUIDs)"));
        properties.put("tag", Map.of("type", "string", "description", "Filter rules by tag (alternative to ruleUIDs)"));
        properties.put("continueOnError", Map.of("type", "boolean", "description",
                "Continue processing if individual operations fail", "default", true));
        properties.put("validateBeforeExecute", Map.of("type", "boolean", "description",
                "Validate rules before performing operations", "default", true));
        properties.put("dryRun", Map.of("type", "boolean", "description",
                "Simulate operations without actually executing them", "default", false));

        schema.put("properties", properties);
        schema.put("required", List.of("operation"));
        schema.put("additionalProperties", false);
        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("success", Map.of("type", "boolean", "description", "Whether the operation was successful"));
        properties.put("operation", Map.of("type", "string", "description", "The operation that was performed"));
        properties.put("totalRules", Map.of("type", "integer", "description", "Total number of rules processed"));
        properties.put("successfulOperations",
                Map.of("type", "integer", "description", "Number of successful operations"));
        properties.put("failedOperations", Map.of("type", "integer", "description", "Number of failed operations"));
        properties.put("results", Map.of("type", "array", "items", Map.of("type", "object"), "description",
                "Detailed results for each rule"));
        properties.put("summary", Map.of("type", "object", "description", "Summary of the bulk operation"));
        properties.put("error", Map.of("type", "string", "description", "Error message if operation failed"));

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public ActionValidationResult validateParameters(Map<String, Object> parameters) {
        if (parameters == null) {
            return ActionValidationResult.invalid(List.of("Parameters cannot be null"));
        }

        String operation = (String) parameters.get("operation");
        if (operation == null || operation.trim().isEmpty()) {
            return ActionValidationResult.invalid(List.of("operation is required"));
        }

        List<String> validOperations = List.of("enable", "disable", "delete", "validate");
        if (!validOperations.contains(operation)) {
            return ActionValidationResult
                    .invalid(List.of("operation must be one of: " + String.join(", ", validOperations)));
        }

        @SuppressWarnings("unchecked")
        List<String> ruleUIDs = (List<String>) parameters.get("ruleUIDs");
        String query = (String) parameters.get("query");
        String tag = (String) parameters.get("tag");

        if ((ruleUIDs == null || ruleUIDs.isEmpty()) && (query == null || query.trim().isEmpty())
                && (tag == null || tag.trim().isEmpty())) {
            return ActionValidationResult.invalid(List.of("Either ruleUIDs, query, or tag must be provided"));
        }

        return ActionValidationResult.valid(parameters);
    }

    @Override
    public ActionResult execute(Map<String, Object> parameters, ActionContext context) throws ActionException {
        try {
            String operation = (String) parameters.get("operation");
            @SuppressWarnings("unchecked")
            List<String> ruleUIDs = (List<String>) parameters.get("ruleUIDs");
            String query = (String) parameters.get("query");
            String tag = (String) parameters.get("tag");
            boolean continueOnError = (Boolean) parameters.getOrDefault("continueOnError", true);
            boolean validateBeforeExecute = (Boolean) parameters.getOrDefault("validateBeforeExecute", true);
            boolean dryRun = (Boolean) parameters.getOrDefault("dryRun", false);

            logger.debug("Performing bulk operation: {} on rules", operation);

            // Determine which rules to operate on
            List<String> targetRuleUIDs = determineTargetRules(ruleUIDs, query, tag);

            if (targetRuleUIDs.isEmpty()) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("error", "No rules found matching the criteria");
                return ActionResult.success(result, System.currentTimeMillis());
            }

            // Perform bulk operation
            Map<String, Object> operationResult = performBulkOperation(operation, targetRuleUIDs, continueOnError,
                    validateBeforeExecute, dryRun);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("operation", operation);
            result.putAll(operationResult);
            result.put("note", "This is a stub implementation - actual bulk operations require proper rule management");

            return ActionResult.success(result, System.currentTimeMillis());

        } catch (Exception e) {
            logger.error("Error performing bulk rule operation: {}", e.getMessage(), e);
            throw new ActionException(getActionId(), "Failed to perform bulk rule operation: " + e.getMessage(), e);
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
                .tags(List.of("rules", "bulk", "operations")).build();
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
        logger.debug("Initializing BulkRuleOperationsAction");
    }

    @Override
    public void cleanup() {
        logger.debug("Cleaning up BulkRuleOperationsAction");
    }

    @Override
    public boolean isReady() {
        return ruleRegistry != null;
    }

    private List<String> determineTargetRules(List<String> ruleUIDs, String query, String tag) {
        if (ruleUIDs != null && !ruleUIDs.isEmpty()) {
            return ruleUIDs;
        }

        List<String> targetUIDs = new ArrayList<>();
        List<Rule> allRules = new ArrayList<>(ruleRegistry.getAll());

        for (Rule rule : allRules) {
            boolean include = true;

            if (query != null && !query.trim().isEmpty()) {
                String ruleName = rule.getName().toLowerCase();
                String ruleDescription = rule.getDescription() != null ? rule.getDescription().toLowerCase() : "";
                String searchQuery = query.toLowerCase();
                include = ruleName.contains(searchQuery) || ruleDescription.contains(searchQuery);
            }

            if (include && tag != null && !tag.trim().isEmpty()) {
                include = rule.getTags().contains(tag);
            }

            if (include) {
                targetUIDs.add(rule.getUID());
            }
        }

        return targetUIDs;
    }

    private Map<String, Object> performBulkOperation(String operation, List<String> ruleUIDs, boolean continueOnError,
            boolean validateBeforeExecute, boolean dryRun) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> individualResults = new ArrayList<>();
        int successfulOperations = 0;
        int failedOperations = 0;
        for (String ruleUID : ruleUIDs) {
            Map<String, Object> individualResult = new HashMap<>();
            individualResult.put("ruleUID", ruleUID);
            try {
                Rule rule = ruleRegistry.get(ruleUID);
                if (rule == null) {
                    individualResult.put("success", false);
                    individualResult.put("error", "Rule not found");
                    failedOperations++;
                } else {
                    individualResult.put("ruleName", rule.getName());
                    if (dryRun) {
                        individualResult.put("success", true);
                        individualResult.put("message", "Operation would be performed (dry run)");
                        successfulOperations++;
                    } else {
                        if (operation.equals("enable")) {
                            ruleManager.setEnabled(ruleUID, true);
                            individualResult.put("success", true);
                            individualResult.put("message", "Rule enabled");
                            successfulOperations++;
                        } else if (operation.equals("disable")) {
                            ruleManager.setEnabled(ruleUID, false);
                            individualResult.put("success", true);
                            individualResult.put("message", "Rule disabled");
                            successfulOperations++;
                        } else {
                            individualResult.put("success", false);
                            individualResult.put("error", "Bulk operation not implemented in stub");
                            individualResult.put("message", "This is a stub implementation");
                            failedOperations++;
                        }
                    }
                }
            } catch (Exception e) {
                individualResult.put("success", false);
                individualResult.put("error", e.getMessage());
                failedOperations++;
                if (!continueOnError) {
                    throw new RuntimeException("Bulk operation failed on rule " + ruleUID + ": " + e.getMessage(), e);
                }
            }
            individualResults.add(individualResult);
        }
        result.put("totalRules", ruleUIDs.size());
        result.put("successfulOperations", successfulOperations);
        result.put("failedOperations", failedOperations);
        result.put("results", individualResults);
        Map<String, Object> summary = new HashMap<>();
        summary.put("operation", operation);
        summary.put("dryRun", dryRun);
        summary.put("successRate", ruleUIDs.size() > 0 ? (double) successfulOperations / ruleUIDs.size() * 100 : 0.0);
        summary.put("continueOnError", continueOnError);
        summary.put("validateBeforeExecute", validateBeforeExecute);
        result.put("summary", summary);
        return result;
    }
}
