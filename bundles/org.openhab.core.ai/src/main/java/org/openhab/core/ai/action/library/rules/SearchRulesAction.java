package org.openhab.core.ai.action.library.rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.openhab.core.ai.action.ActionContext;
import org.openhab.core.ai.action.ActionMetadata;
import org.openhab.core.ai.action.ActionResult;
import org.openhab.core.ai.action.ActionValidationResult;
import org.openhab.core.ai.action.api.Action;
import org.openhab.core.ai.action.api.ActionException;
import org.openhab.core.automation.Rule;
import org.openhab.core.automation.RuleManager;
import org.openhab.core.automation.RuleRegistry;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action for searching openHAB Rules based on various criteria.
 * This action provides advanced search capabilities with filtering, sorting, and pagination.
 * 
 * Uses real RuleRegistry to perform comprehensive rule searches with advanced filtering.
 */
@Component(service = Action.class, immediate = true)
public class SearchRulesAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(SearchRulesAction.class);

    @Reference
    private RuleRegistry ruleRegistry;

    @Reference
    private RuleManager ruleManager;

    @Override
    public String getActionId() {
        return "openhab.rules.search";
    }

    @Override
    public String getActionName() {
        return "Search Rules";
    }

    @Override
    public String getDescription() {
        return "Searches for openHAB Rules based on various criteria including name, tags, type, and content";
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
        properties.put("query", Map.of("type", "string", "description", "Search query string"));
        properties.put("namePattern",
                Map.of("type", "string", "description", "Pattern to match rule names (supports wildcards)"));
        properties.put("tag", Map.of("type", "string", "description", "Filter by specific tag"));
        properties.put("type", Map.of("type", "string", "description", "Filter by rule type"));
        properties.put("templateUID", Map.of("type", "string", "description", "Filter by template UID"));
        properties.put("hasTriggers", Map.of("type", "boolean", "description", "Filter rules that have triggers"));
        properties.put("hasConditions", Map.of("type", "boolean", "description", "Filter rules that have conditions"));
        properties.put("hasActions", Map.of("type", "boolean", "description", "Filter rules that have actions"));
        properties.put("enabled", Map.of("type", "boolean", "description", "Filter by enabled status"));
        properties.put("caseSensitive",
                Map.of("type", "boolean", "description", "Case sensitive search", "default", false));
        properties.put("sortBy", Map.of("type", "string", "enum", List.of("name", "uid", "type", "created"),
                "description", "Sort results by field", "default", "name"));
        properties.put("sortOrder", Map.of("type", "string", "enum", List.of("asc", "desc"), "description",
                "Sort order", "default", "asc"));
        properties.put("limit", Map.of("type", "integer", "minimum", 1, "maximum", 1000, "description",
                "Maximum number of results", "default", 100));
        properties.put("offset",
                Map.of("type", "integer", "minimum", 0, "description", "Number of results to skip", "default", 0));

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
        properties.put("results",
                Map.of("type", "array", "items", Map.of("type", "object"), "description", "List of matching rules"));
        properties.put("totalCount", Map.of("type", "integer", "description", "Total number of matching rules"));
        properties.put("resultCount", Map.of("type", "integer", "description", "Number of results returned"));
        properties.put("searchCriteria", Map.of("type", "object", "description", "Search criteria used"));
        properties.put("error", Map.of("type", "string", "description", "Error message if operation failed"));

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public ActionValidationResult validateParameters(Map<String, Object> parameters) {
        try {
            // Validate limit
            Object limitObj = parameters.get("limit");
            if (limitObj != null) {
                int limit = (Integer) limitObj;
                if (limit < 1 || limit > 1000) {
                    return ActionValidationResult.invalid(List.of("limit must be between 1 and 1000"));
                }
            }

            // Validate offset
            Object offsetObj = parameters.get("offset");
            if (offsetObj != null) {
                int offset = (Integer) offsetObj;
                if (offset < 0) {
                    return ActionValidationResult.invalid(List.of("offset must be non-negative"));
                }
            }

            // Validate sortBy
            String sortBy = (String) parameters.get("sortBy");
            if (sortBy != null && !List.of("name", "uid", "type", "created").contains(sortBy)) {
                return ActionValidationResult.invalid(List.of("Invalid sortBy value: " + sortBy));
            }

            // Validate sortOrder
            String sortOrder = (String) parameters.get("sortOrder");
            if (sortOrder != null && !List.of("asc", "desc").contains(sortOrder)) {
                return ActionValidationResult.invalid(List.of("Invalid sortOrder value: " + sortOrder));
            }

            return ActionValidationResult.valid(parameters);

        } catch (Exception e) {
            return ActionValidationResult.invalid(List.of("Parameter validation failed: " + e.getMessage()));
        }
    }

    @Override
    public ActionResult execute(Map<String, Object> parameters, ActionContext context) throws ActionException {
        long executionStartTime = System.currentTimeMillis();

        try {
            String query = (String) parameters.get("query");
            String namePattern = (String) parameters.get("namePattern");
            String tag = (String) parameters.get("tag");
            String type = (String) parameters.get("type");
            String templateUID = (String) parameters.get("templateUID");
            Boolean hasTriggers = (Boolean) parameters.get("hasTriggers");
            Boolean hasConditions = (Boolean) parameters.get("hasConditions");
            Boolean hasActions = (Boolean) parameters.get("hasActions");
            Boolean enabled = (Boolean) parameters.get("enabled");
            Boolean caseSensitive = (Boolean) parameters.getOrDefault("caseSensitive", false);
            String sortBy = (String) parameters.getOrDefault("sortBy", "name");
            String sortOrder = (String) parameters.getOrDefault("sortOrder", "asc");
            Integer limit = (Integer) parameters.getOrDefault("limit", 100);
            Integer offset = (Integer) parameters.getOrDefault("offset", 0);

            logger.debug(
                    "Searching rules with query: '{}', namePattern: '{}', tag: '{}', type: '{}', limit: {}, offset: {}",
                    query, namePattern, tag, type, limit, offset);

            // Get all rules from registry
            List<Rule> allRules = new ArrayList<>(ruleRegistry.getAll());

            // Apply filters
            List<Rule> filteredRules = applyFilters(allRules, query, namePattern, tag, type, templateUID, hasTriggers,
                    hasConditions, hasActions, enabled, caseSensitive);

            // Sort rules
            sortRules(filteredRules, sortBy, sortOrder);

            // Apply pagination
            List<Rule> paginatedRules = applyPagination(filteredRules, limit, offset);

            // Convert to result format
            List<Map<String, Object>> results = paginatedRules.stream().map(this::convertRuleToSearchResult)
                    .collect(Collectors.toList());

            // Create search criteria summary
            Map<String, Object> searchCriteria = new HashMap<>();
            searchCriteria.put("query", query);
            searchCriteria.put("namePattern", namePattern);
            searchCriteria.put("tag", tag);
            searchCriteria.put("type", type);
            searchCriteria.put("templateUID", templateUID);
            searchCriteria.put("hasTriggers", hasTriggers);
            searchCriteria.put("hasConditions", hasConditions);
            searchCriteria.put("hasActions", hasActions);
            searchCriteria.put("enabled", enabled);
            searchCriteria.put("caseSensitive", caseSensitive);
            searchCriteria.put("sortBy", sortBy);
            searchCriteria.put("sortOrder", sortOrder);
            searchCriteria.put("limit", limit);
            searchCriteria.put("offset", offset);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("results", results);
            result.put("totalCount", filteredRules.size());
            result.put("resultCount", results.size());
            result.put("searchCriteria", searchCriteria);

            long executionTime = System.currentTimeMillis() - executionStartTime;
            logger.debug("Found {} rules matching criteria, returned {} results in {}ms", filteredRules.size(),
                    results.size(), executionTime);

            return ActionResult.success(result, executionTime);

        } catch (Exception e) {
            logger.error("Error searching rules: {}", e.getMessage(), e);
            throw new ActionException(getActionId(), "Failed to search rules: " + e.getMessage(), e);
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
                .tags(List.of("rules", "search", "filtering")).build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("supportsAsync", true);
        capabilities.put("supportsValidation", true);
        capabilities.put("supportsPagination", true);
        capabilities.put("supportsSorting", true);
        capabilities.put("supportsFiltering", true);
        return capabilities;
    }

    @Override
    public void initialize(ActionContext context) {
        logger.debug("Initializing SearchRulesAction");
    }

    @Override
    public void cleanup() {
        logger.debug("Cleaning up SearchRulesAction");
    }

    @Override
    public boolean isReady() {
        return ruleRegistry != null && ruleManager != null;
    }

    /**
     * Apply all filters to the list of rules.
     */
    private List<Rule> applyFilters(List<Rule> rules, String query, String namePattern, String tag, String type,
            String templateUID, Boolean hasTriggers, Boolean hasConditions, Boolean hasActions, Boolean enabled,
            boolean caseSensitive) {

        return rules.stream().filter(rule -> {
            // Query filter
            if (query != null && !query.trim().isEmpty()) {
                if (!matchesQuery(rule, query, caseSensitive)) {
                    return false;
                }
            }

            // Name pattern filter
            if (namePattern != null && !namePattern.trim().isEmpty()) {
                if (!matchesNamePattern(rule, namePattern, caseSensitive)) {
                    return false;
                }
            }

            // Tag filter
            if (tag != null && !tag.trim().isEmpty()) {
                if (!matchesTag(rule, tag)) {
                    return false;
                }
            }

            // Type filter
            if (type != null && !type.trim().isEmpty()) {
                if (!matchesType(rule, type)) {
                    return false;
                }
            }

            // Template UID filter
            if (templateUID != null && !templateUID.trim().isEmpty()) {
                if (!matchesTemplateUID(rule, templateUID)) {
                    return false;
                }
            }

            // Has triggers filter
            if (hasTriggers != null) {
                if (!matchesHasTriggers(rule, hasTriggers)) {
                    return false;
                }
            }

            // Has conditions filter
            if (hasConditions != null) {
                if (!matchesHasConditions(rule, hasConditions)) {
                    return false;
                }
            }

            // Has actions filter
            if (hasActions != null) {
                if (!matchesHasActions(rule, hasActions)) {
                    return false;
                }
            }

            // Enabled filter
            if (enabled != null) {
                if (!matchesEnabled(rule, enabled)) {
                    return false;
                }
            }

            return true;
        }).collect(Collectors.toList());
    }

    /**
     * Check if rule matches the search query.
     */
    private boolean matchesQuery(Rule rule, String query, boolean caseSensitive) {
        String searchQuery = caseSensitive ? query : query.toLowerCase();

        // Search in name
        String ruleName = rule.getName();
        if (ruleName != null) {
            String nameToSearch = caseSensitive ? ruleName : ruleName.toLowerCase();
            if (nameToSearch.contains(searchQuery)) {
                return true;
            }
        }

        // Search in description
        String description = rule.getDescription();
        if (description != null) {
            String descToSearch = caseSensitive ? description : description.toLowerCase();
            if (descToSearch.contains(searchQuery)) {
                return true;
            }
        }

        // Search in UID
        String uid = rule.getUID();
        if (uid != null) {
            String uidToSearch = caseSensitive ? uid : uid.toLowerCase();
            if (uidToSearch.contains(searchQuery)) {
                return true;
            }
        }

        // Search in tags
        if (rule.getTags() != null) {
            for (String tag : rule.getTags()) {
                String tagToSearch = caseSensitive ? tag : tag.toLowerCase();
                if (tagToSearch.contains(searchQuery)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Check if rule name matches the pattern.
     */
    private boolean matchesNamePattern(Rule rule, String namePattern, boolean caseSensitive) {
        String ruleName = rule.getName();
        if (ruleName == null) {
            return false;
        }

        String pattern = caseSensitive ? namePattern : namePattern.toLowerCase();
        String name = caseSensitive ? ruleName : ruleName.toLowerCase();

        // Simple wildcard matching (* and ?)
        return matchesWildcardPattern(name, pattern);
    }

    /**
     * Simple wildcard pattern matching.
     */
    private boolean matchesWildcardPattern(String text, String pattern) {
        // Convert wildcard pattern to regex
        String regex = pattern.replace("*", ".*").replace("?", ".");
        return text.matches(regex);
    }

    /**
     * Check if rule has the specified tag.
     */
    private boolean matchesTag(Rule rule, String tag) {
        return rule.getTags() != null && rule.getTags().contains(tag);
    }

    /**
     * Check if rule matches the type.
     */
    private boolean matchesType(Rule rule, String type) {
        String ruleType = rule.getTemplateUID() != null ? rule.getTemplateUID() : "core";
        return ruleType.equals(type);
    }

    /**
     * Check if rule matches the template UID.
     */
    private boolean matchesTemplateUID(Rule rule, String templateUID) {
        return templateUID.equals(rule.getTemplateUID());
    }

    /**
     * Check if rule has triggers.
     */
    private boolean matchesHasTriggers(Rule rule, Boolean hasTriggers) {
        boolean ruleHasTriggers = rule.getTriggers() != null && !rule.getTriggers().isEmpty();
        return ruleHasTriggers == hasTriggers;
    }

    /**
     * Check if rule has conditions.
     */
    private boolean matchesHasConditions(Rule rule, Boolean hasConditions) {
        boolean ruleHasConditions = rule.getConditions() != null && !rule.getConditions().isEmpty();
        return ruleHasConditions == hasConditions;
    }

    /**
     * Check if rule has actions.
     */
    private boolean matchesHasActions(Rule rule, Boolean hasActions) {
        boolean ruleHasActions = rule.getActions() != null && !rule.getActions().isEmpty();
        return ruleHasActions == hasActions;
    }

    /**
     * Check if rule matches the enabled status.
     */
    private boolean matchesEnabled(Rule rule, Boolean enabled) {
        boolean ruleEnabled = ruleManager != null ? ruleManager.isEnabled(rule.getUID()) : true;
        return ruleEnabled == enabled;
    }

    /**
     * Sort rules by the specified field and order.
     */
    private void sortRules(List<Rule> rules, String sortBy, String sortOrder) {
        rules.sort((rule1, rule2) -> {
            Object value1 = getSortValue(rule1, sortBy);
            Object value2 = getSortValue(rule2, sortBy);

            int comparison = 0;
            if (value1 == null && value2 == null) {
                comparison = 0;
            } else if (value1 == null) {
                comparison = -1;
            } else if (value2 == null) {
                comparison = 1;
            } else if (value1 instanceof Comparable && value2 instanceof Comparable) {
                @SuppressWarnings("unchecked")
                Comparable<Object> comp1 = (Comparable<Object>) value1;
                comparison = comp1.compareTo(value2);
            } else {
                comparison = value1.toString().compareTo(value2.toString());
            }

            return "desc".equals(sortOrder) ? -comparison : comparison;
        });
    }

    /**
     * Get the sort value for a rule based on the sort field.
     */
    private Object getSortValue(Rule rule, String sortBy) {
        switch (sortBy) {
            case "name":
                return rule.getName();
            case "uid":
                return rule.getUID();
            case "type":
                return rule.getTemplateUID() != null ? rule.getTemplateUID() : "core";
            case "created":
                // Note: Rule doesn't have a creation timestamp, so we'll use a default
                return 0L;
            default:
                return rule.getName();
        }
    }

    /**
     * Apply pagination to the list of rules.
     */
    private List<Rule> applyPagination(List<Rule> rules, int limit, int offset) {
        int startIndex = Math.min(offset, rules.size());
        int endIndex = Math.min(startIndex + limit, rules.size());

        if (startIndex >= rules.size()) {
            return new ArrayList<>();
        }

        return rules.subList(startIndex, endIndex);
    }

    /**
     * Convert a rule to search result format.
     */
    private Map<String, Object> convertRuleToSearchResult(Rule rule) {
        Map<String, Object> result = new HashMap<>();
        result.put("uid", rule.getUID());
        result.put("name", rule.getName());
        result.put("description", rule.getDescription() != null ? rule.getDescription() : "");
        result.put("tags", rule.getTags());
        result.put("type", rule.getTemplateUID() != null ? rule.getTemplateUID() : "core");
        result.put("visibility", rule.getVisibility());

        // Real status information using RuleManager
        boolean isEnabled = ruleManager != null ? ruleManager.isEnabled(rule.getUID()) : true;
        String status = isEnabled ? "ENABLED" : "DISABLED";
        result.put("status", status);
        result.put("enabled", isEnabled);

        // Module counts
        result.put("triggerCount", rule.getTriggers() != null ? rule.getTriggers().size() : 0);
        result.put("conditionCount", rule.getConditions() != null ? rule.getConditions().size() : 0);
        result.put("actionCount", rule.getActions() != null ? rule.getActions().size() : 0);

        // Configuration info
        result.put("hasConfiguration",
                rule.getConfiguration() != null && !rule.getConfiguration().getProperties().isEmpty());

        return result;
    }
}
