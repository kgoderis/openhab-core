package org.openhab.core.ai.action.library.rules;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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
 * Action for listing openHAB Rules with comprehensive filtering and metadata.
 * This action provides detailed rule information with filtering, sorting, and metadata options.
 * 
 * 
 */
@Component(service = Action.class, immediate = true)
public class ListRulesAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(ListRulesAction.class);

    @Reference
    private RuleRegistry ruleRegistry;

    @Reference
    private RuleManager ruleManager;

    @Override
    public String getActionId() {
        return "openhab.rules.list";
    }

    @Override
    public String getActionName() {
        return "List Rules";
    }

    @Override
    public String getDescription() {
        return "Lists openHAB Rules with comprehensive filtering, sorting, and metadata options including triggers, conditions, and actions";
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
        properties.put("status", Map.of("type", "string", "enum", List.of("ENABLED", "DISABLED", "all"), "description",
                "Filter by Rule status", "default", "all"));
        properties.put("tag", Map.of("type", "string", "description", "Filter by tag"));
        properties.put("type", Map.of("type", "string", "description", "Filter by rule type"));
        properties.put("includeTriggers",
                Map.of("type", "boolean", "description", "Include trigger information", "default", false));
        properties.put("includeConditions",
                Map.of("type", "boolean", "description", "Include condition information", "default", false));
        properties.put("includeActions",
                Map.of("type", "boolean", "description", "Include action information", "default", false));
        properties.put("includeConfiguration",
                Map.of("type", "boolean", "description", "Include configuration details", "default", false));
        properties.put("sortBy", Map.of("type", "string", "enum", List.of("name", "uid", "status", "type"),
                "description", "Sort results by field", "default", "name"));
        properties.put("sortOrder", Map.of("type", "string", "enum", List.of("asc", "desc"), "description",
                "Sort order", "default", "asc"));
        properties.put("limit", Map.of("type", "integer", "minimum", 1, "maximum", 1000, "description",
                "Maximum number of Rules to return", "default", 100));
        properties.put("offset", Map.of("type", "integer", "minimum", 0, "description",
                "Number of Rules to skip for pagination", "default", 0));

        schema.put("properties", properties);
        schema.put("additionalProperties", false);
        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("success", Map.of("type", "boolean", "description", "Whether the operation was successful"));
        properties.put("rules", Map.of("type", "array", "description", "List of rules"));
        properties.put("totalCount", Map.of("type", "integer", "description", "Total number of rules returned"));
        properties.put("totalAvailable", Map.of("type", "integer", "description", "Total number of rules available"));
        properties.put("filteredBy", Map.of("type", "object", "description", "Applied filters"));
        properties.put("statusBreakdown", Map.of("type", "object", "description", "Breakdown by status"));
        properties.put("typeBreakdown", Map.of("type", "object", "description", "Breakdown by type"));
        properties.put("pagination", Map.of("type", "object", "description", "Pagination information"));
        properties.put("error", Map.of("type", "string", "description", "Error message if operation failed"));

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public ActionValidationResult validateParameters(Map<String, Object> parameters) {
        if (parameters == null) {
            return ActionValidationResult.invalid(java.util.List.of("Parameters cannot be null"));
        }

        // Validate limit
        Object limitObj = parameters.get("limit");
        if (limitObj != null) {
            try {
                int limit = Integer.parseInt(limitObj.toString());
                if (limit < 1 || limit > 1000) {
                    return ActionValidationResult.invalid(java.util.List.of("limit must be between 1 and 1000"));
                }
            } catch (NumberFormatException e) {
                return ActionValidationResult.invalid(java.util.List.of("limit must be a valid integer"));
            }
        }

        // Validate offset
        Object offsetObj = parameters.get("offset");
        if (offsetObj != null) {
            try {
                int offset = Integer.parseInt(offsetObj.toString());
                if (offset < 0) {
                    return ActionValidationResult.invalid(java.util.List.of("offset must be non-negative"));
                }
            } catch (NumberFormatException e) {
                return ActionValidationResult.invalid(java.util.List.of("offset must be a valid integer"));
            }
        }

        return ActionValidationResult.valid(parameters);
    }

    @Override
    public ActionResult execute(Map<String, Object> parameters, ActionContext context) throws ActionException {
        try {
            String status = (String) parameters.getOrDefault("status", "all");
            String tag = (String) parameters.get("tag");
            String type = (String) parameters.get("type");
            boolean includeTriggers = (Boolean) parameters.getOrDefault("includeTriggers", false);
            boolean includeConditions = (Boolean) parameters.getOrDefault("includeConditions", false);
            boolean includeActions = (Boolean) parameters.getOrDefault("includeActions", false);
            boolean includeConfiguration = (Boolean) parameters.getOrDefault("includeConfiguration", false);
            String sortBy = (String) parameters.getOrDefault("sortBy", "name");
            String sortOrder = (String) parameters.getOrDefault("sortOrder", "asc");
            int limit = Integer.parseInt(parameters.getOrDefault("limit", 100).toString());
            int offset = Integer.parseInt(parameters.getOrDefault("offset", 0).toString());

            logger.debug("Listing rules with filters: status={}, tag={}, type={}, limit={}, offset={}", status, tag,
                    type, limit, offset);

            Map<String, Object> result = listRules(status, tag, type, includeTriggers, includeConditions,
                    includeActions, includeConfiguration, sortBy, sortOrder, limit, offset);

            result.put("success", true);
            return ActionResult.success(result, System.currentTimeMillis());

        } catch (Exception e) {
            logger.error("Error listing rules: {}", e.getMessage(), e);
            throw new ActionException(getActionId(), "Failed to list rules: " + e.getMessage(), e);
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
                .tags(java.util.List.of("rules", "list", "filter", "sort")).build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("supportsAsync", true);
        capabilities.put("supportsValidation", true);
        capabilities.put("supportsFiltering", true);
        capabilities.put("supportsSorting", true);
        capabilities.put("supportsPagination", true);
        return capabilities;
    }

    @Override
    public void initialize(ActionContext context) {
        logger.debug("Initializing ListRulesAction");
    }

    @Override
    public void cleanup() {
        logger.debug("Cleaning up ListRulesAction");
    }

    @Override
    public boolean isReady() {
        return ruleRegistry != null && ruleManager != null;
    }

    /**
     * List rules with the specified filters and options.
     */
    private Map<String, Object> listRules(String status, String tag, String type, boolean includeTriggers,
            boolean includeConditions, boolean includeActions, boolean includeConfiguration, String sortBy,
            String sortOrder, int limit, int offset) throws ActionException {

        if (ruleRegistry == null) {
            throw new ActionException(getActionId(), "RuleRegistry not available");
        }

        Collection<Rule> allRules = ruleRegistry.getAll();
        List<Map<String, Object>> ruleMaps = allRules.stream().map(rule -> convertRuleToMap(rule, includeTriggers,
                includeConditions, includeActions, includeConfiguration)).collect(Collectors.toList());

        // Apply filters
        List<Map<String, Object>> filteredRules = applyFilters(ruleMaps, status, tag, type);

        // Sort rules
        sortRules(filteredRules, sortBy, sortOrder);

        // Apply pagination
        List<Map<String, Object>> paginatedRules = applyPagination(filteredRules, limit, offset);

        // Build result
        Map<String, Object> result = new HashMap<>();
        result.put("rules", paginatedRules);
        result.put("totalCount", paginatedRules.size());
        result.put("totalAvailable", filteredRules.size());
        result.put("filteredBy", Map.of("status", status != null ? status : "all", "tag", tag != null ? tag : "none",
                "type", type != null ? type : "none"));
        result.put("statusBreakdown", getStatusBreakdown(filteredRules));
        result.put("typeBreakdown", getTypeBreakdown(filteredRules));
        result.put("pagination",
                Map.of("limit", limit, "offset", offset, "hasMore", offset + limit < filteredRules.size()));

        return result;
    }

    /**
     * Convert a Rule to a Map representation with real status information.
     */
    private Map<String, Object> convertRuleToMap(Rule rule, boolean includeTriggers, boolean includeConditions,
            boolean includeActions, boolean includeConfiguration) {

        Map<String, Object> ruleMap = new HashMap<>();
        ruleMap.put("uid", rule.getUID());
        ruleMap.put("name", rule.getName());
        ruleMap.put("description", rule.getDescription() != null ? rule.getDescription() : "");
        ruleMap.put("tags", rule.getTags());

        // Real status information using RuleManager
        boolean isEnabled = ruleManager != null ? ruleManager.isEnabled(rule.getUID()) : true;
        String status = isEnabled ? "ENABLED" : "DISABLED";

        ruleMap.put("status", status);
        ruleMap.put("enabled", isEnabled);
        ruleMap.put("type", rule.getTemplateUID() != null ? rule.getTemplateUID() : "core");

        if (includeTriggers) {
            List<Map<String, Object>> triggerMaps = rule.getTriggers().stream().map(trigger -> {
                Map<String, Object> triggerMap = new HashMap<>();
                triggerMap.put("id", trigger.getId());
                triggerMap.put("typeUID", trigger.getTypeUID());
                triggerMap.put("label", trigger.getLabel() != null ? trigger.getLabel() : "");
                triggerMap.put("description", trigger.getDescription() != null ? trigger.getDescription() : "");
                if (includeConfiguration) {
                    triggerMap.put("configuration", trigger.getConfiguration().getProperties());
                }
                return triggerMap;
            }).collect(Collectors.toList());
            ruleMap.put("triggers", triggerMaps);
        }

        if (includeConditions) {
            List<Map<String, Object>> conditionMaps = rule.getConditions().stream().map(condition -> {
                Map<String, Object> conditionMap = new HashMap<>();
                conditionMap.put("id", condition.getId());
                conditionMap.put("typeUID", condition.getTypeUID());
                conditionMap.put("label", condition.getLabel() != null ? condition.getLabel() : "");
                conditionMap.put("description", condition.getDescription() != null ? condition.getDescription() : "");
                if (includeConfiguration) {
                    conditionMap.put("configuration", condition.getConfiguration().getProperties());
                }
                return conditionMap;
            }).collect(Collectors.toList());
            ruleMap.put("conditions", conditionMaps);
        }

        if (includeActions) {
            List<Map<String, Object>> actionMaps = rule.getActions().stream().map(action -> {
                Map<String, Object> actionMap = new HashMap<>();
                actionMap.put("id", action.getId());
                actionMap.put("typeUID", action.getTypeUID());
                actionMap.put("label", action.getLabel() != null ? action.getLabel() : "");
                actionMap.put("description", action.getDescription() != null ? action.getDescription() : "");
                if (includeConfiguration) {
                    actionMap.put("configuration", action.getConfiguration().getProperties());
                }
                return actionMap;
            }).collect(Collectors.toList());
            ruleMap.put("actions", actionMaps);
        }

        if (includeConfiguration) {
            ruleMap.put("configuration", rule.getConfiguration().getProperties());
        }

        return ruleMap;
    }

    /**
     * Apply filters to the list of rules.
     */
    private List<Map<String, Object>> applyFilters(List<Map<String, Object>> rules, String status, String tag,
            String type) {
        return rules.stream().filter(rule -> {
            // Status filter
            if (status != null && !"all".equals(status)) {
                String ruleStatus = (String) rule.get("status");
                if (!status.equals(ruleStatus)) {
                    return false;
                }
            }

            // Tag filter
            if (tag != null && !tag.trim().isEmpty()) {
                @SuppressWarnings("unchecked")
                List<String> ruleTags = (List<String>) rule.get("tags");
                if (ruleTags == null || !ruleTags.contains(tag)) {
                    return false;
                }
            }

            // Type filter
            if (type != null && !type.trim().isEmpty()) {
                String ruleType = (String) rule.get("type");
                if (!type.equals(ruleType)) {
                    return false;
                }
            }

            return true;
        }).collect(Collectors.toList());
    }

    /**
     * Sort rules by the specified field and order.
     */
    private void sortRules(List<Map<String, Object>> rules, String sortBy, String sortOrder) {
        rules.sort((rule1, rule2) -> {
            Object value1 = rule1.get(sortBy);
            Object value2 = rule2.get(sortBy);

            if (value1 == null && value2 == null) {
                return 0;
            }
            if (value1 == null) {
                return sortOrder.equals("asc") ? -1 : 1;
            }
            if (value2 == null) {
                return sortOrder.equals("asc") ? 1 : -1;
            }

            int comparison = value1.toString().compareToIgnoreCase(value2.toString());
            return sortOrder.equals("asc") ? comparison : -comparison;
        });
    }

    /**
     * Apply pagination to the list of rules.
     */
    private List<Map<String, Object>> applyPagination(List<Map<String, Object>> rules, int limit, int offset) {
        int startIndex = Math.min(offset, rules.size());
        int endIndex = Math.min(startIndex + limit, rules.size());
        return rules.subList(startIndex, endIndex);
    }

    /**
     * Get breakdown of rules by status.
     */
    private Map<String, Integer> getStatusBreakdown(List<Map<String, Object>> rules) {
        return rules.stream().collect(Collectors.groupingBy(rule -> (String) rule.get("status"),
                Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));
    }

    /**
     * Get breakdown of rules by type.
     */
    private Map<String, Integer> getTypeBreakdown(List<Map<String, Object>> rules) {
        return rules.stream().collect(Collectors.groupingBy(rule -> (String) rule.get("type"),
                Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));
    }
}
