package org.openhab.core.ai.common.actions.things;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.openhab.core.ai.common.api.action.AIAction;
import org.openhab.core.ai.common.api.action.AIActionContext;
import org.openhab.core.ai.common.api.action.AIActionException;
import org.openhab.core.ai.common.api.action.AIActionMetadata;
import org.openhab.core.ai.common.api.action.AIActionResult;
import org.openhab.core.ai.common.api.action.AIActionValidationResult;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.type.ThingTypeRegistry;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AIAction for listing openHAB Things with comprehensive filtering and metadata.
 * This action provides detailed thing information with filtering, sorting, and metadata options.
 * 
 * 
 */
@Component(service = AIAction.class, immediate = true)
public class ListThingsAction implements AIAction {

    private static final Logger logger = LoggerFactory.getLogger(ListThingsAction.class);

    @Reference
    private ThingRegistry thingRegistry;

    @Reference
    private ThingTypeRegistry thingTypeRegistry;

    @Override
    public String getActionId() {
        return "openhab.things.list";
    }

    @Override
    public String getActionName() {
        return "List Things";
    }

    @Override
    public String getDescription() {
        return "Lists openHAB Things with comprehensive filtering, sorting, and metadata options including status, configuration, and channel information";
    }

    @Override
    public String getCategory() {
        return "things";
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
        properties.put("status",
                Map.of("type", "string", "enum",
                        List.of("ONLINE", "OFFLINE", "UNINITIALIZED", "INITIALIZING", "REMOVING", "REMOVED", "all"),
                        "description", "Filter by Thing status", "default", "all"));
        properties.put("binding",
                Map.of("type", "string", "description", "Filter by binding type (e.g., 'zwave', 'hue')"));
        properties.put("thingType", Map.of("type", "string", "description", "Filter by specific Thing type"));
        properties.put("bridgeUID",
                Map.of("type", "string", "description", "Filter by bridge UID (child Things of specific bridge)"));
        properties.put("location", Map.of("type", "string", "description", "Filter by location"));
        properties.put("enabled", Map.of("type", "boolean", "description", "Filter by enabled status"));
        properties.put("includeChannels",
                Map.of("type", "boolean", "description", "Include channel information", "default", false));
        properties.put("includeConfiguration",
                Map.of("type", "boolean", "description", "Include configuration details", "default", false));
        properties.put("includeProperties",
                Map.of("type", "boolean", "description", "Include properties information", "default", false));
        properties.put("sortBy",
                Map.of("type", "string", "enum", List.of("uid", "label", "status", "thingType", "binding"),
                        "description", "Sort results by field", "default", "uid"));
        properties.put("sortOrder", Map.of("type", "string", "enum", List.of("asc", "desc"), "description",
                "Sort order", "default", "asc"));
        properties.put("limit", Map.of("type", "integer", "minimum", 1, "maximum", 1000, "description",
                "Maximum number of Things to return", "default", 100));
        properties.put("offset", Map.of("type", "integer", "minimum", 0, "description",
                "Number of Things to skip for pagination", "default", 0));

        schema.put("properties", properties);
        schema.put("additionalProperties", false);
        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();

        Map<String, Object> thingsProperty = new HashMap<>();
        thingsProperty.put("type", "array");
        thingsProperty.put("description", "List of things matching the filter criteria");

        Map<String, Object> itemsSchema = new HashMap<>();
        itemsSchema.put("type", "object");

        Map<String, Object> itemProperties = new HashMap<>();
        itemProperties.put("uid", Map.of("type", "string"));
        itemProperties.put("thingTypeUID", Map.of("type", "string"));
        itemProperties.put("label", Map.of("type", "string"));
        itemProperties.put("status", Map.of("type", "string"));
        itemProperties.put("statusInfo", Map.of("type", "object"));
        itemProperties.put("bridgeUID", Map.of("type", "string"));
        itemProperties.put("location", Map.of("type", "string"));
        itemProperties.put("enabled", Map.of("type", "boolean"));
        itemProperties.put("channels", Map.of("type", "array", "items", Map.of("type", "object")));
        itemProperties.put("configuration", Map.of("type", "object"));
        itemProperties.put("properties", Map.of("type", "object"));

        itemsSchema.put("properties", itemProperties);
        thingsProperty.put("items", itemsSchema);
        properties.put("things", thingsProperty);

        properties.put("totalCount", Map.of("type", "integer", "description", "Total number of things returned"));
        properties.put("filteredBy", Map.of("type", "object", "description", "Filter criteria applied"));
        properties.put("statusBreakdown", Map.of("type", "object", "description", "Breakdown of things by status"));
        properties.put("bindingBreakdown", Map.of("type", "object", "description", "Breakdown of things by binding"));

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public AIActionValidationResult validateParameters(Map<String, Object> parameters) {
        String status = (String) parameters.getOrDefault("status", "all");
        List<String> validStatuses = List.of("ONLINE", "OFFLINE", "UNINITIALIZED", "INITIALIZING", "REMOVING",
                "REMOVED", "all");

        if (!validStatuses.contains(status)) {
            return AIActionValidationResult.invalid(
                    List.of("Invalid status: " + status + ". Must be one of: " + String.join(", ", validStatuses)));
        }

        String sortBy = (String) parameters.getOrDefault("sortBy", "uid");
        List<String> validSortBy = List.of("uid", "label", "status", "thingType", "binding");
        if (!validSortBy.contains(sortBy)) {
            return AIActionValidationResult.invalid(
                    List.of("Invalid sortBy: " + sortBy + ". Must be one of: " + String.join(", ", validSortBy)));
        }

        String sortOrder = (String) parameters.getOrDefault("sortOrder", "asc");
        if (!"asc".equals(sortOrder) && !"desc".equals(sortOrder)) {
            return AIActionValidationResult
                    .invalid(List.of("Invalid sortOrder: " + sortOrder + ". Must be 'asc' or 'desc'"));
        }

        Object limit = parameters.get("limit");
        if (limit != null) {
            if (!(limit instanceof Integer) || (Integer) limit < 1 || (Integer) limit > 1000) {
                return AIActionValidationResult.invalid(List.of("limit must be an integer between 1 and 1000"));
            }
        }

        Object offset = parameters.get("offset");
        if (offset != null) {
            if (!(offset instanceof Integer) || (Integer) offset < 0) {
                return AIActionValidationResult.invalid(List.of("offset must be a non-negative integer"));
            }
        }

        return AIActionValidationResult.valid(parameters);
    }

    @Override
    public AIActionResult execute(Map<String, Object> parameters, AIActionContext context) throws AIActionException {
        long startTime = System.currentTimeMillis();

        try {
            String status = (String) parameters.getOrDefault("status", "all");
            String binding = (String) parameters.get("binding");
            String thingType = (String) parameters.get("thingType");
            String bridgeUID = (String) parameters.get("bridgeUID");
            String location = (String) parameters.get("location");
            Boolean enabled = (Boolean) parameters.get("enabled");
            boolean includeChannels = (Boolean) parameters.getOrDefault("includeChannels", false);
            boolean includeConfiguration = (Boolean) parameters.getOrDefault("includeConfiguration", false);
            boolean includeProperties = (Boolean) parameters.getOrDefault("includeProperties", false);
            String sortBy = (String) parameters.getOrDefault("sortBy", "uid");
            String sortOrder = (String) parameters.getOrDefault("sortOrder", "asc");
            int limit = (Integer) parameters.getOrDefault("limit", 100);
            int offset = (Integer) parameters.getOrDefault("offset", 0);

            Map<String, Object> result = listThings(status, binding, thingType, bridgeUID, location, enabled,
                    includeChannels, includeConfiguration, includeProperties, sortBy, sortOrder, limit, offset);

            long executionTime = System.currentTimeMillis() - startTime;

            logger.debug("ListThingsAction executed in {}ms, returned {} things", executionTime,
                    ((List<?>) result.get("things")).size());

            return AIActionResult.success(result, executionTime);

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("Error executing ListThingsAction", e);
            throw new AIActionException(getActionId(), "Failed to list Things: " + e.getMessage(), e);
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
        return AIActionMetadata.builder().version(getVersion()).author("openHAB")
                .description("List openHAB things with comprehensive filtering and metadata options")
                .tags(List.of("things", "list", "filter", "metadata", "sorting"))
                .documentation(
                        "Lists openHAB Things with comprehensive filtering, sorting, and metadata options including status, configuration, and channel information")
                .examples(List.of("List all things: {}", "List only online things: {\"status\": \"ONLINE\"}",
                        "List things by binding: {\"binding\": \"zwave\"}",
                        "List things with channels: {\"includeChannels\": true}",
                        "List things with configuration: {\"includeConfiguration\": true}",
                        "Sort by status: {\"sortBy\": \"status\", \"sortOrder\": \"desc\"}",
                        "Paginated results: {\"limit\": 50, \"offset\": 100}"))
                .build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        return Map.of("filtering", true, "sorting", true, "pagination", true, "metadata", true, "channels", true,
                "configuration", true, "properties", true, "status", true, "async", true);
    }

    @Override
    public void initialize(AIActionContext context) {
        logger.debug("ListThingsAction initialized for protocol: {}", context.getProtocol());
    }

    @Override
    public void cleanup() {
        logger.debug("ListThingsAction cleanup completed");
    }

    @Override
    public boolean isReady() {
        return thingRegistry != null && thingTypeRegistry != null;
    }

    /**
     * List things with the specified filters and options.
     */
    private Map<String, Object> listThings(String status, String binding, String thingType, String bridgeUID,
            String location, Boolean enabled, boolean includeChannels, boolean includeConfiguration,
            boolean includeProperties, String sortBy, String sortOrder, int limit, int offset)
            throws AIActionException {

        if (thingRegistry == null) {
            throw new AIActionException(getActionId(), "ThingRegistry not available");
        }

        Collection<Thing> allThings = thingRegistry.getAll();
        List<Map<String, Object>> thingMaps = allThings.stream()
                .map(thing -> convertThingToMap(thing, includeChannels, includeConfiguration, includeProperties))
                .collect(Collectors.toList());

        // Apply filters
        List<Map<String, Object>> filteredThings = applyFilters(thingMaps, status, binding, thingType, bridgeUID,
                location, enabled);

        // Sort things
        sortThings(filteredThings, sortBy, sortOrder);

        // Apply pagination
        List<Map<String, Object>> paginatedThings = applyPagination(filteredThings, limit, offset);

        // Build result
        Map<String, Object> result = new HashMap<>();
        result.put("things", paginatedThings);
        result.put("totalCount", paginatedThings.size());
        result.put("totalAvailable", filteredThings.size());
        result.put("filteredBy",
                Map.of("status", status != null ? status : "all", "binding", binding != null ? binding : "none",
                        "thingType", thingType != null ? thingType : "none", "bridgeUID",
                        bridgeUID != null ? bridgeUID : "none", "location", location != null ? location : "none",
                        "enabled", enabled != null ? enabled : "none"));
        result.put("statusBreakdown", getStatusBreakdown(filteredThings));
        result.put("bindingBreakdown", getBindingBreakdown(filteredThings));
        result.put("pagination",
                Map.of("limit", limit, "offset", offset, "hasMore", offset + limit < filteredThings.size()));

        return result;
    }

    /**
     * Convert a Thing to a Map representation.
     */
    private Map<String, Object> convertThingToMap(Thing thing, boolean includeChannels, boolean includeConfiguration,
            boolean includeProperties) {

        Map<String, Object> thingMap = new HashMap<>();
        thingMap.put("uid", thing.getUID().toString());
        thingMap.put("thingTypeUID", thing.getThingTypeUID().toString());
        thingMap.put("label", thing.getLabel() != null ? thing.getLabel() : "");
        thingMap.put("status", thing.getStatus().toString());
        thingMap.put("statusInfo",
                Map.of("status", thing.getStatusInfo().getStatus().toString(), "statusDetail",
                        thing.getStatusInfo().getStatusDetail().toString(), "description",
                        thing.getStatusInfo().getDescription() != null ? thing.getStatusInfo().getDescription() : ""));

        ThingUID bridgeUID = thing.getBridgeUID();
        thingMap.put("bridgeUID", bridgeUID != null ? bridgeUID.toString() : null);
        thingMap.put("location", thing.getLocation() != null ? thing.getLocation() : "");
        thingMap.put("enabled", thing.isEnabled());

        if (includeChannels) {
            List<Map<String, Object>> channelMaps = thing.getChannels().stream().map(this::convertChannelToMap)
                    .collect(Collectors.toList());
            thingMap.put("channels", channelMaps);
        }

        if (includeConfiguration) {
            Configuration config = thing.getConfiguration();
            thingMap.put("configuration", config != null ? config.getProperties() : Map.of());
        }

        if (includeProperties) {
            thingMap.put("properties", thing.getProperties());
        }

        return thingMap;
    }

    /**
     * Convert a Channel to a Map representation.
     */
    private Map<String, Object> convertChannelToMap(Channel channel) {
        Map<String, Object> channelMap = new HashMap<>();
        channelMap.put("uid", channel.getUID().toString());
        channelMap.put("id", channel.getUID().getId());
        channelMap.put("label", channel.getLabel() != null ? channel.getLabel() : "");
        channelMap.put("description", channel.getDescription() != null ? channel.getDescription() : "");
        channelMap.put("type", channel.getChannelTypeUID() != null ? channel.getChannelTypeUID().toString() : null);
        channelMap.put("kind", channel.getKind().toString());
        channelMap.put("defaultTags", channel.getDefaultTags());

        Configuration config = channel.getConfiguration();
        channelMap.put("configuration", config != null ? config.getProperties() : Map.of());

        return channelMap;
    }

    /**
     * Apply filters to the list of things.
     */
    private List<Map<String, Object>> applyFilters(List<Map<String, Object>> things, String status, String binding,
            String thingType, String bridgeUID, String location, Boolean enabled) {

        return things.stream().filter(thing -> {
            // Status filter
            if (status != null && !"all".equals(status) && !status.equals(thing.get("status"))) {
                return false;
            }

            // Binding filter
            if (binding != null) {
                String thingTypeUID = (String) thing.get("thingTypeUID");
                if (thingTypeUID == null || !thingTypeUID.startsWith(binding + ":")) {
                    return false;
                }
            }

            // Thing type filter
            if (thingType != null && !thingType.equals(thing.get("thingTypeUID"))) {
                return false;
            }

            // Bridge UID filter
            if (bridgeUID != null && !bridgeUID.equals(thing.get("bridgeUID"))) {
                return false;
            }

            // Location filter
            if (location != null && !location.equals(thing.get("location"))) {
                return false;
            }

            // Enabled filter
            if (enabled != null && !enabled.equals(thing.get("enabled"))) {
                return false;
            }

            return true;
        }).collect(Collectors.toList());
    }

    /**
     * Sort things by the specified field and order.
     */
    private void sortThings(List<Map<String, Object>> things, String sortBy, String sortOrder) {
        things.sort((a, b) -> {
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
     * Apply pagination to the list of things.
     */
    private List<Map<String, Object>> applyPagination(List<Map<String, Object>> things, int limit, int offset) {
        int start = Math.min(offset, things.size());
        int end = Math.min(start + limit, things.size());
        return things.subList(start, end);
    }

    /**
     * Get breakdown of things by status.
     */
    private Map<String, Integer> getStatusBreakdown(List<Map<String, Object>> things) {
        return things.stream().collect(Collectors.groupingBy(thing -> (String) thing.get("status"),
                Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));
    }

    /**
     * Get breakdown of things by binding.
     */
    private Map<String, Integer> getBindingBreakdown(List<Map<String, Object>> things) {
        return things.stream().map(thing -> {
            String thingTypeUID = (String) thing.get("thingTypeUID");
            return thingTypeUID != null ? thingTypeUID.split(":")[0] : "unknown";
        }).collect(Collectors.groupingBy(binding -> binding,
                Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));
    }
}
