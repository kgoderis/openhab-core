package org.openhab.core.ai.actions.items;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.action.ActionContext;
import org.openhab.core.ai.action.ActionMetadata;
import org.openhab.core.ai.action.ActionResult;
import org.openhab.core.ai.action.ActionValidationResult;
import org.openhab.core.ai.api.action.Action;
import org.openhab.core.ai.api.action.ActionException;
import org.openhab.core.items.GroupItem;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.MetadataRegistry;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action for searching items in openHAB.
 * 
 * This action provides functionality to search for
 * items using various criteria.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class SearchItemsAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(SearchItemsAction.class);

    @Reference
    private @Nullable ItemRegistry itemRegistry;

    @Reference
    private @Nullable MetadataRegistry metadataRegistry;

    @Override
    public String getActionId() {
        return "openhab.items.search";
    }

    @Override
    public String getActionName() {
        return "Search Items";
    }

    @Override
    public String getDescription() {
        return "Search for items based on various criteria";
    }

    @Override
    public String getCategory() {
        return "items";
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
        properties.put("namePattern", Map.of("type", "string", "description",
                "Pattern to match item names (supports wildcards)", "required", false));
        properties.put("itemType",
                Map.of("type", "string", "description", "Filter by specific item type", "required", false));
        properties.put("tag",
                Map.of("type", "string", "description", "Filter by metadata tag/namespace", "required", false));
        properties.put("group",
                Map.of("type", "string", "description", "Filter by group membership", "required", false));
        properties.put("category",
                Map.of("type", "string", "description", "Filter by item category", "required", false));
        properties.put("includeGroups",
                Map.of("type", "boolean", "description", "Include group items in results", "default", true));
        properties.put("includeDetails",
                Map.of("type", "boolean", "description", "Include detailed item information", "default", false));
        properties.put("maxResults",
                Map.of("type", "integer", "description", "Maximum number of results to return", "default", 100));
        properties.put("caseSensitive",
                Map.of("type", "boolean", "description", "Use case-sensitive matching", "default", false));

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("searchCriteria", Map.of("type", "object"));
        properties.put("results", Map.of("type", "array"));
        properties.put("totalResults", Map.of("type", "integer"));
        properties.put("maxResults", Map.of("type", "integer"));
        properties.put("success", Map.of("type", "boolean"));
        properties.put("timestamp", Map.of("type", "number"));

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public ActionValidationResult validateParameters(Map<String, Object> parameters) {
        if (parameters == null) {
            return ActionValidationResult.invalid(List.of("Parameters cannot be null"));
        }

        // Validate maxResults if provided
        Object maxResultsObj = parameters.get("maxResults");
        if (maxResultsObj != null) {
            if (!(maxResultsObj instanceof Integer)) {
                return ActionValidationResult.invalid(List.of("maxResults must be an integer"));
            }
            Integer maxResults = (Integer) maxResultsObj;
            if (maxResults <= 0 || maxResults > 1000) {
                return ActionValidationResult.invalid(List.of("maxResults must be between 1 and 1000"));
            }
        }

        return ActionValidationResult.valid(parameters);
    }

    @Override
    public ActionResult execute(Map<String, Object> parameters, ActionContext context) throws ActionException {
        long executionStartTime = System.currentTimeMillis();

        try {
            String namePattern = (String) parameters.get("namePattern");
            String itemType = (String) parameters.get("itemType");
            String tag = (String) parameters.get("tag");
            String group = (String) parameters.get("group");
            String category = (String) parameters.get("category");
            Boolean includeGroups = (Boolean) parameters.getOrDefault("includeGroups", true);
            Boolean includeDetails = (Boolean) parameters.getOrDefault("includeDetails", false);
            Integer maxResults = (Integer) parameters.getOrDefault("maxResults", 100);
            Boolean caseSensitive = (Boolean) parameters.getOrDefault("caseSensitive", false);

            logger.debug("Searching items with criteria: namePattern={}, itemType={}, tag={}, group={}", namePattern,
                    itemType, tag, group);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("timestamp", System.currentTimeMillis());
            result.put("maxResults", maxResults);

            // Store search criteria
            Map<String, Object> searchCriteria = new HashMap<>();
            searchCriteria.put("namePattern", namePattern);
            searchCriteria.put("itemType", itemType);
            searchCriteria.put("tag", tag);
            searchCriteria.put("group", group);
            searchCriteria.put("category", category);
            searchCriteria.put("includeGroups", includeGroups);
            searchCriteria.put("caseSensitive", caseSensitive);
            result.put("searchCriteria", searchCriteria);

            // Start with all items
            List<Item> allItems = itemRegistry.getAll().stream()
                    .filter(item -> includeGroups || !(item instanceof GroupItem)).collect(Collectors.toList());

            // Apply filters
            List<Item> filteredItems = allItems.stream()
                    .filter(item -> matchesNamePattern(item, namePattern, caseSensitive))
                    .filter(item -> matchesItemType(item, itemType)).filter(item -> matchesTag(item, tag))
                    .filter(item -> matchesGroup(item, group)).filter(item -> matchesCategory(item, category))
                    .limit(maxResults).collect(Collectors.toList());

            // Convert to result format
            List<Map<String, Object>> results = filteredItems.stream()
                    .map(item -> createItemResult(item, includeDetails)).collect(Collectors.toList());

            result.put("results", results);
            result.put("totalResults", results.size());

            long executionTime = System.currentTimeMillis() - executionStartTime;
            logger.debug("Search completed with {} results in {}ms", results.size(), executionTime);

            return ActionResult.success(result, executionTime);

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - executionStartTime;
            logger.error("Error searching items", e);
            throw new ActionException(getActionId(), "Failed to search items: " + e.getMessage(), e);
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
        return ActionMetadata.builder().version(getVersion()).author("openHAB")
                .description("Search for openHAB items based on various criteria")
                .tags(List.of("items", "search", "filter", "query"))
                .documentation(
                        "Searches for openHAB items based on name patterns, types, tags, groups, and categories. Supports wildcard matching and detailed results.")
                .examples(List.of("Search by name: {\"namePattern\": \"*Light*\"}",
                        "Search by type: {\"itemType\": \"Switch\"}", "Search by tag: {\"tag\": \"semantics\"}",
                        "Search by group: {\"group\": \"LivingRoom\"}",
                        "Complex search: {\"namePattern\": \"*Light*\", \"itemType\": \"Switch\", \"tag\": \"semantics\", \"includeDetails\": true}"))
                .build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("async", true);
        capabilities.put("validation", true);
        capabilities.put("nameSearch", true);
        capabilities.put("typeFilter", true);
        capabilities.put("tagFilter", true);
        capabilities.put("groupFilter", true);
        capabilities.put("categoryFilter", true);
        capabilities.put("wildcardMatching", true);
        capabilities.put("caseSensitive", true);
        capabilities.put("resultLimiting", true);
        return capabilities;
    }

    @Override
    public void initialize(ActionContext context) {
        // No initialization needed
    }

    @Override
    public void cleanup() {
        // No cleanup needed
    }

    @Override
    public boolean isReady() {
        return itemRegistry != null;
    }

    /**
     * Check if item matches name pattern
     */
    private boolean matchesNamePattern(Item item, String namePattern, boolean caseSensitive) {
        if (namePattern == null || namePattern.trim().isEmpty()) {
            return true;
        }

        String itemName = item.getName();
        String pattern = namePattern.trim();

        if (!caseSensitive) {
            itemName = itemName.toLowerCase();
            pattern = pattern.toLowerCase();
        }

        // Simple wildcard matching
        if (pattern.contains("*")) {
            String regex = pattern.replace("*", ".*");
            return itemName.matches(regex);
        } else {
            return itemName.contains(pattern);
        }
    }

    /**
     * Check if item matches item type
     */
    private boolean matchesItemType(Item item, String itemType) {
        if (itemType == null || itemType.trim().isEmpty()) {
            return true;
        }

        return item.getType().equals(itemType.trim());
    }

    /**
     * Check if item matches tag
     */
    private boolean matchesTag(Item item, String tag) {
        if (tag == null || tag.trim().isEmpty() || metadataRegistry == null) {
            return true;
        }

        return metadataRegistry.getAll().stream()
                .anyMatch(metadata -> metadata.getUID().getItemName().equals(item.getName())
                        && metadata.getUID().getNamespace().equals(tag.trim()));
    }

    /**
     * Check if item matches group
     */
    private boolean matchesGroup(Item item, String group) {
        if (group == null || group.trim().isEmpty()) {
            return true;
        }

        return itemRegistry.getAll().stream().filter(groupItem -> groupItem instanceof GroupItem)
                .map(groupItem -> (GroupItem) groupItem).anyMatch(
                        groupItem -> groupItem.getName().equals(group.trim()) && groupItem.getMembers().contains(item));
    }

    /**
     * Check if item matches category
     */
    private boolean matchesCategory(Item item, String category) {
        if (category == null || category.trim().isEmpty()) {
            return true;
        }

        String itemCategory = item.getCategory();
        return itemCategory != null && itemCategory.equals(category.trim());
    }

    /**
     * Create item result map
     */
    private Map<String, Object> createItemResult(Item item, boolean includeDetails) {
        Map<String, Object> itemResult = new HashMap<>();
        itemResult.put("name", item.getName());
        itemResult.put("type", item.getType());
        String itemLabel = "";
        if (item.getLabel() != null) {
            itemLabel = item.getLabel();
        }
        itemResult.put("label", itemLabel);

        String itemCategory = "";
        if (item.getCategory() != null) {
            itemCategory = item.getCategory();
        }
        itemResult.put("category", itemCategory);
        itemResult.put("state", item.getState() != null ? item.getState().toString() : "NULL");

        if (includeDetails) {
            Map<String, Object> details = new HashMap<>();
            details.put("lastStateChange",
                    item.getLastStateChange() != null ? item.getLastStateChange().toString() : "NULL");
            details.put("isGroup", item instanceof GroupItem);

            if (item instanceof GroupItem groupItem) {
                details.put("memberCount", groupItem.getMembers().size());
                String baseItemType = "";
                if (groupItem.getBaseItem() != null) {
                    baseItemType = groupItem.getBaseItem().getType();
                }
                details.put("baseItemType", baseItemType);
            }

            itemResult.put("details", details);
        }

        return itemResult;
    }
}
