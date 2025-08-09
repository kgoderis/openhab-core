package org.openhab.core.ai.action.library.items;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.action.ActionContext;
import org.openhab.core.ai.action.ActionMetadata;
import org.openhab.core.ai.action.ActionResult;
import org.openhab.core.ai.action.ActionValidationResult;
import org.openhab.core.ai.action.api.Action;
import org.openhab.core.ai.action.api.ActionException;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.Metadata;
import org.openhab.core.items.MetadataKey;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.thing.link.ItemChannelLink;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.core.types.State;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action for listing openHAB Items with comprehensive filtering and metadata.
 * This action provides detailed item information with filtering, sorting, and metadata options.
 * 
 * 
 */
@Component(service = Action.class, immediate = true)
@NonNullByDefault
public class ListItemsAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(ListItemsAction.class);

    @Reference
    private @Nullable ItemRegistry itemRegistry;

    @Reference
    private @Nullable ItemChannelLinkRegistry itemChannelLinkRegistry;

    @Reference
    private @Nullable MetadataRegistry metadataRegistry;

    @Override
    public String getActionId() {
        return "openhab.items.list";
    }

    @Override
    public String getActionName() {
        return "List Items";
    }

    @Override
    public String getDescription() {
        return "Lists openHAB Items with comprehensive filtering, sorting, and metadata options including state, type, tags, groups, and channel links";
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
        properties.put("type",
                Map.of("type", "string", "enum",
                        List.of("Switch", "Dimmer", "Color", "String", "Number", "DateTime", "Contact", "Rollershutter",
                                "Player", "Location", "Image", "Group", "all"),
                        "description", "Filter by Item type", "default", "all"));
        properties.put("state", Map.of("type", "string", "description", "Filter by current state value"));
        properties.put("group", Map.of("type", "string", "description", "Filter by group membership"));
        properties.put("tag", Map.of("type", "string", "description", "Filter by tag"));
        properties.put("category", Map.of("type", "string", "description", "Filter by category"));
        properties.put("hasChannelLink",
                Map.of("type", "boolean", "description", "Filter Items that have channel links"));
        properties.put("includeMetadata",
                Map.of("type", "boolean", "description", "Include Item metadata", "default", false));
        properties.put("includeChannelLinks",
                Map.of("type", "boolean", "description", "Include channel link information", "default", false));
        properties.put("includeGroups",
                Map.of("type", "boolean", "description", "Include group membership information", "default", false));
        properties.put("includeState",
                Map.of("type", "boolean", "description", "Include current state information", "default", true));
        properties.put("sortBy",
                Map.of("type", "string", "enum", List.of("name", "label", "type", "state", "lastUpdate"), "description",
                        "Sort results by field", "default", "name"));
        properties.put("sortOrder", Map.of("type", "string", "enum", List.of("asc", "desc"), "description",
                "Sort order", "default", "asc"));
        properties.put("limit", Map.of("type", "integer", "minimum", 1, "maximum", 1000, "description",
                "Maximum number of Items to return", "default", 100));
        properties.put("offset", Map.of("type", "integer", "minimum", 0, "description",
                "Number of Items to skip for pagination", "default", 0));

        schema.put("properties", properties);
        schema.put("additionalProperties", false);
        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        return Map.of("type", "object", "properties",
                Map.of("items",
                        Map.of("type", "array", "description", "List of items matching the filter criteria", "items",
                                Map.of("type", "object", "properties", Map.of("name", Map.of("type", "string"), "type",
                                        Map.of("type", "string"), "label", Map.of("type", "string"), "category",
                                        Map.of("type", "string"), "state", Map.of("type", "string"), "tags",
                                        Map.of("type", "array", "items", Map.of("type", "string")), "groups",
                                        Map.of("type", "array", "items", Map.of("type", "string")), "channelLinks",
                                        Map.of("type", "array", "items", Map.of("type", "object")), "metadata",
                                        Map.of("type", "object")))),
                        "totalCount", Map.of("type", "integer", "description", "Total number of items returned"),
                        "filteredBy", Map.of("type", "object", "description", "Filter criteria applied"),
                        "typeBreakdown", Map.of("type", "object", "description", "Breakdown of items by type"),
                        "stateBreakdown", Map.of("type", "object", "description", "Breakdown of items by state")));
    }

    @Override
    public ActionValidationResult validateParameters(Map<String, Object> parameters) {
        String type = (String) parameters.getOrDefault("type", "all");
        List<String> validTypes = List.of("Switch", "Dimmer", "Color", "String", "Number", "DateTime", "Contact",
                "Rollershutter", "Player", "Location", "Image", "Group", "all");

        if (!validTypes.contains(type)) {
            return ActionValidationResult
                    .invalid(List.of("Invalid type: " + type + ". Must be one of: " + String.join(", ", validTypes)));
        }

        String sortBy = (String) parameters.getOrDefault("sortBy", "name");
        List<String> validSortBy = List.of("name", "label", "type", "state", "lastUpdate");
        if (!validSortBy.contains(sortBy)) {
            return ActionValidationResult.invalid(
                    List.of("Invalid sortBy: " + sortBy + ". Must be one of: " + String.join(", ", validSortBy)));
        }

        String sortOrder = (String) parameters.getOrDefault("sortOrder", "asc");
        if (!"asc".equals(sortOrder) && !"desc".equals(sortOrder)) {
            return ActionValidationResult
                    .invalid(List.of("Invalid sortOrder: " + sortOrder + ". Must be 'asc' or 'desc'"));
        }

        Object limit = parameters.get("limit");
        if (limit != null) {
            if (!(limit instanceof Integer) || (Integer) limit < 1 || (Integer) limit > 1000) {
                return ActionValidationResult.invalid(List.of("limit must be an integer between 1 and 1000"));
            }
        }

        Object offset = parameters.get("offset");
        if (offset != null) {
            if (!(offset instanceof Integer) || (Integer) offset < 0) {
                return ActionValidationResult.invalid(List.of("offset must be a non-negative integer"));
            }
        }

        return ActionValidationResult.valid(parameters);
    }

    @Override
    public ActionResult execute(Map<String, Object> parameters, ActionContext context) throws ActionException {
        long startTime = System.currentTimeMillis();

        try {
            String type = (String) parameters.getOrDefault("type", "all");
            String state = (String) parameters.get("state");
            String group = (String) parameters.get("group");
            String tag = (String) parameters.get("tag");
            String category = (String) parameters.get("category");
            Boolean hasChannelLink = (Boolean) parameters.get("hasChannelLink");
            boolean includeMetadata = (Boolean) parameters.getOrDefault("includeMetadata", false);
            boolean includeChannelLinks = (Boolean) parameters.getOrDefault("includeChannelLinks", false);
            boolean includeGroups = (Boolean) parameters.getOrDefault("includeGroups", false);
            boolean includeState = (Boolean) parameters.getOrDefault("includeState", true);
            String sortBy = (String) parameters.getOrDefault("sortBy", "name");
            String sortOrder = (String) parameters.getOrDefault("sortOrder", "asc");
            int limit = (Integer) parameters.getOrDefault("limit", 100);
            int offset = (Integer) parameters.getOrDefault("offset", 0);

            Map<String, Object> result = listItems(type, state, group, tag, category, hasChannelLink, includeMetadata,
                    includeChannelLinks, includeGroups, includeState, sortBy, sortOrder, limit, offset);

            long executionTime = System.currentTimeMillis() - startTime;

            logger.debug("ListItemsAction executed in {}ms, returned {} items", executionTime,
                    ((List<?>) result.get("items")).size());

            return ActionResult.success(result, executionTime);

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("Error executing ListItemsAction", e);
            throw new ActionException(getActionId(), "Failed to list Items: " + e.getMessage(), e);
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
                .description("List openHAB items with comprehensive filtering and metadata options")
                .tags(List.of("items", "list", "filter", "metadata", "sorting"))
                .documentation(
                        "Lists openHAB Items with comprehensive filtering, sorting, and metadata options including state, type, tags, groups, and channel links")
                .examples(List.of("List all items: {}", "List only switches: {\"type\": \"Switch\"}",
                        "List items with 'light' in name: {\"tag\": \"light\"}",
                        "List items with metadata: {\"includeMetadata\": true}",
                        "List items with channel links: {\"includeChannelLinks\": true}",
                        "Sort by state: {\"sortBy\": \"state\", \"sortOrder\": \"desc\"}",
                        "Paginated results: {\"limit\": 50, \"offset\": 100}"))
                .build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        return Map.of("filtering", true, "sorting", true, "pagination", true, "metadata", true, "channelLinks", true,
                "groups", true, "state", true, "async", true);
    }

    @Override
    public void initialize(ActionContext context) {
        logger.debug("ListItemsAction initialized for protocol: {}", context.getProtocol());
    }

    @Override
    public void cleanup() {
        logger.debug("ListItemsAction cleanup completed");
    }

    @Override
    public boolean isReady() {
        return itemRegistry != null && itemChannelLinkRegistry != null && metadataRegistry != null;
    }

    /**
     * List items with the specified filters and options.
     */
    private Map<String, Object> listItems(String type, String state, String group, String tag, String category,
            Boolean hasChannelLink, boolean includeMetadata, boolean includeChannelLinks, boolean includeGroups,
            boolean includeState, String sortBy, String sortOrder, int limit, int offset) throws ActionException {

        if (itemRegistry == null) {
            throw new ActionException(getActionId(), "ItemRegistry not available");
        }

        Collection<Item> allItems = itemRegistry.getAll();
        List<Map<String, Object>> itemMaps = allItems.stream()
                .map(item -> convertItemToMap(item, includeMetadata, includeChannelLinks, includeGroups, includeState))
                .collect(Collectors.toList());

        // Apply filters
        List<Map<String, Object>> filteredItems = applyFilters(itemMaps, type, state, group, tag, category,
                hasChannelLink);

        // Sort items
        sortItems(filteredItems, sortBy, sortOrder);

        // Apply pagination
        List<Map<String, Object>> paginatedItems = applyPagination(filteredItems, limit, offset);

        // Build result
        Map<String, Object> result = new HashMap<>();
        result.put("items", paginatedItems);
        result.put("totalCount", paginatedItems.size());
        result.put("totalAvailable", filteredItems.size());
        result.put("filteredBy",
                Map.of("type", type != null ? type : "all", "state", state != null ? state : "none", "group",
                        group != null ? group : "none", "tag", tag != null ? tag : "none", "category",
                        category != null ? category : "none", "hasChannelLink",
                        hasChannelLink != null ? hasChannelLink : "none"));
        result.put("typeBreakdown", getTypeBreakdown(filteredItems));
        result.put("stateBreakdown", getStateBreakdown(filteredItems));
        result.put("pagination",
                Map.of("limit", limit, "offset", offset, "hasMore", offset + limit < filteredItems.size()));

        return result;
    }

    /**
     * Convert an Item to a Map representation.
     */
    private Map<String, Object> convertItemToMap(Item item, boolean includeMetadata, boolean includeChannelLinks,
            boolean includeGroups, boolean includeState) {

        Map<String, Object> itemMap = new HashMap<>();
        itemMap.put("name", item.getName());
        itemMap.put("type", item.getType());
        String label = item.getLabel() != null ? item.getLabel() : "";
        itemMap.put("label", label);
        String category = item.getCategory() != null ? item.getCategory() : "";
        itemMap.put("category", category);

        if (includeState) {
            State currentState = item.getState();
            itemMap.put("state", currentState != null ? currentState.toString() : "NULL");
            itemMap.put("stateType", currentState != null ? currentState.getClass().getSimpleName() : "NULL");
        }

        if (includeGroups) {
            List<String> groupNames = item.getGroupNames();
            itemMap.put("groups", groupNames);
        }

        Set<String> tags = item.getTags();
        itemMap.put("tags", new HashSet<>(tags));

        if (includeChannelLinks && itemChannelLinkRegistry != null) {
            Set<ItemChannelLink> links = itemChannelLinkRegistry.getLinks(item.getName());
            if (!links.isEmpty()) {
                List<Map<String, Object>> linkMaps = links.stream().map(link -> {
                    Map<String, Object> linkMap = new HashMap<>();
                    linkMap.put("channelUID", link.getLinkedUID().getAsString());
                    linkMap.put("configuration", link.getConfiguration().getProperties());
                    return linkMap;
                }).collect(Collectors.toList());
                itemMap.put("channelLinks", linkMaps);
            }
        }

        if (includeMetadata && metadataRegistry != null) {
            Map<String, Object> metadataMap = new HashMap<>();
            for (String namespace : metadataRegistry.getAllNamespaces(item.getName())) {
                MetadataKey key = new MetadataKey(namespace, item.getName());
                Metadata metadata = metadataRegistry.get(key);
                if (metadata != null) {
                    Map<String, Object> metaMap = new HashMap<>();
                    metaMap.put("value", metadata.getValue());
                    metaMap.put("configuration", metadata.getConfiguration());
                    metadataMap.put(namespace, metaMap);
                }
            }
            itemMap.put("metadata", metadataMap);
        }

        return itemMap;
    }

    /**
     * Apply filters to the list of items.
     */
    private List<Map<String, Object>> applyFilters(List<Map<String, Object>> items, String type, String state,
            String group, String tag, String category, Boolean hasChannelLink) {

        return items.stream().filter(item -> {
            // Type filter
            if (type != null && !"all".equals(type) && !type.equals(item.get("type"))) {
                return false;
            }

            // State filter
            if (state != null && !state.equals(item.get("state"))) {
                return false;
            }

            // Group filter
            if (group != null) {
                @SuppressWarnings("unchecked")
                Set<String> groups = (Set<String>) item.get("groups");
                if (groups == null || !groups.contains(group)) {
                    return false;
                }
            }

            // Tag filter
            if (tag != null) {
                @SuppressWarnings("unchecked")
                Set<String> tags = (Set<String>) item.get("tags");
                if (tags == null || !tags.contains(tag)) {
                    return false;
                }
            }

            // Category filter
            if (category != null && !category.equals(item.get("category"))) {
                return false;
            }

            // Channel link filter
            if (hasChannelLink != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> channelLinks = (List<Map<String, Object>>) item.get("channelLinks");
                boolean hasLinks = channelLinks != null && !channelLinks.isEmpty();
                if (hasChannelLink != hasLinks) {
                    return false;
                }
            }

            return true;
        }).collect(Collectors.toList());
    }

    /**
     * Sort items by the specified field and order.
     */
    private void sortItems(List<Map<String, Object>> items, String sortBy, String sortOrder) {
        items.sort((a, b) -> {
            Object aValue = a.get(sortBy);
            Object bValue = b.get(sortBy);

            if (aValue == null && bValue == null)
                return 0;
            if (aValue == null)
                return sortOrder.equals("asc") ? -1 : 1;
            if (bValue == null)
                return sortOrder.equals("asc") ? 1 : -1;

            int comparison = aValue.toString().compareToIgnoreCase(bValue.toString());
            return sortOrder.equals("asc") ? comparison : -comparison;
        });
    }

    /**
     * Apply pagination to the list of items.
     */
    private List<Map<String, Object>> applyPagination(List<Map<String, Object>> items, int limit, int offset) {
        int start = Math.min(offset, items.size());
        int end = Math.min(start + limit, items.size());
        return items.subList(start, end);
    }

    /**
     * Get breakdown of items by type.
     */
    private Map<String, Integer> getTypeBreakdown(List<Map<String, Object>> items) {
        return items.stream().collect(Collectors.groupingBy(item -> (String) item.get("type"),
                Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));
    }

    /**
     * Get breakdown of items by state.
     */
    private Map<String, Integer> getStateBreakdown(List<Map<String, Object>> items) {
        return items.stream().filter(item -> item.get("state") != null)
                .collect(Collectors.groupingBy(item -> (String) item.get("state"),
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));
    }
}
