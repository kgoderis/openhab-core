package org.openhab.core.ai.action.library.channels;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.link.ItemChannelLink;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * AI Action for listing openHAB Channels with comprehensive filtering and metadata.
 * 
 * 
 */
@NonNullByDefault
@Component(service = Action.class, immediate = true)
public class ListChannelsAction implements Action {

    private static final String ACTION_ID = "openhab.channels.list";
    private static final String ACTION_NAME = "List Channels";
    private static final String DESCRIPTION = "Lists openHAB Channels with comprehensive filtering, sorting, and metadata options including channel types, linked items, and configuration";
    private static final String CATEGORY = "channels";
    private static final String VERSION = "1.0.0";

    @Reference
    private @Nullable ThingRegistry thingRegistry;

    @Reference
    private @Nullable ItemChannelLinkRegistry itemChannelLinkRegistry;

    @Reference
    private @Nullable ChannelTypeRegistry channelTypeRegistry;

    @Override
    public String getActionId() {
        return ACTION_ID;
    }

    @Override
    public String getActionName() {
        return ACTION_NAME;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public String getCategory() {
        return CATEGORY;
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    public Map<String, Object> getParameterSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("thingUID",
                Map.of("type", "string", "description", "Filter by Thing UID (exact match or prefix)"));
        properties.put("channelType", Map.of("type", "string", "description", "Filter by channel type UID"));
        properties.put("acceptedItemType",
                Map.of("type", "string", "description", "Filter by accepted item type (e.g., Switch, Number, String)"));
        properties.put("kind",
                Map.of("type", "string", "enum", List.of("STATE", "TRIGGER"), "description", "Filter by channel kind"));
        properties.put("binding",
                Map.of("type", "string", "description", "Filter by binding (e.g., hue, zwave, mqtt)"));
        properties.put("linkedOnly", Map.of("type", "boolean", "description",
                "Show only channels that are linked to items", "default", false));
        properties.put("unlinkedOnly", Map.of("type", "boolean", "description",
                "Show only channels that are NOT linked to items", "default", false));
        properties.put("includeConfiguration",
                Map.of("type", "boolean", "description", "Include channel configuration details", "default", false));
        properties.put("includeChannelType", Map.of("type", "boolean", "description",
                "Include detailed channel type information", "default", false));
        properties.put("includeLinkedItems",
                Map.of("type", "boolean", "description", "Include list of linked items", "default", true));
        properties.put("sortBy",
                Map.of("type", "string", "enum",
                        List.of("uid", "label", "thingUID", "type", "kind", "acceptedItemType", "binding"),
                        "description", "Sort channels by specified field", "default", "uid"));
        properties.put("sortOrder", Map.of("type", "string", "enum", List.of("asc", "desc"), "description",
                "Sort order", "default", "asc"));
        properties.put("limit", Map.of("type", "integer", "minimum", 1, "maximum", 1000, "description",
                "Maximum number of channels to return", "default", 100));
        properties.put("offset", Map.of("type", "integer", "minimum", 0, "description",
                "Number of channels to skip (pagination)", "default", 0));

        schema.put("properties", properties);
        schema.put("additionalProperties", false);
        return schema;
    }

    @Override
    public ActionValidationResult validateParameters(Map<String, Object> parameters) {
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

        Boolean linkedOnly = (Boolean) parameters.get("linkedOnly");
        Boolean unlinkedOnly = (Boolean) parameters.get("unlinkedOnly");
        if (Boolean.TRUE.equals(linkedOnly) && Boolean.TRUE.equals(unlinkedOnly)) {
            return ActionValidationResult.invalid(List.of("linkedOnly and unlinkedOnly cannot both be true"));
        }

        return ActionValidationResult.valid(parameters);
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("timestamp", Map.of("type", "string", "description", "Timestamp of the operation"));
        properties.put("channels", Map.of("type", "array", "description", "List of channels"));
        properties.put("totalCount", Map.of("type", "integer", "description", "Total number of channels"));
        properties.put("returnedCount", Map.of("type", "integer", "description", "Number of channels returned"));
        properties.put("offset", Map.of("type", "integer", "description", "Pagination offset"));
        properties.put("limit", Map.of("type", "integer", "description", "Pagination limit"));
        properties.put("hasMore", Map.of("type", "boolean", "description", "Whether more channels are available"));
        properties.put("summary", Map.of("type", "object", "description", "Summary statistics"));

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public ActionResult execute(Map<String, Object> parameters, ActionContext context) throws ActionException {
        long startTime = System.currentTimeMillis();

        try {
            String thingUID = (String) parameters.get("thingUID");
            String channelType = (String) parameters.get("channelType");
            String acceptedItemType = (String) parameters.get("acceptedItemType");
            String kind = (String) parameters.get("kind");
            String binding = (String) parameters.get("binding");
            Boolean linkedOnly = (Boolean) parameters.getOrDefault("linkedOnly", false);
            Boolean unlinkedOnly = (Boolean) parameters.getOrDefault("unlinkedOnly", false);
            boolean includeConfiguration = (Boolean) parameters.getOrDefault("includeConfiguration", false);
            boolean includeChannelType = (Boolean) parameters.getOrDefault("includeChannelType", false);
            boolean includeLinkedItems = (Boolean) parameters.getOrDefault("includeLinkedItems", true);
            String sortBy = (String) parameters.getOrDefault("sortBy", "uid");
            String sortOrder = (String) parameters.getOrDefault("sortOrder", "asc");
            int limit = (Integer) parameters.getOrDefault("limit", 100);
            int offset = (Integer) parameters.getOrDefault("offset", 0);

            Map<String, Object> result = listChannels(thingUID, channelType, acceptedItemType, kind, binding,
                    linkedOnly, unlinkedOnly, includeConfiguration, includeChannelType, includeLinkedItems, sortBy,
                    sortOrder, limit, offset);

            long executionTime = System.currentTimeMillis() - startTime;
            return ActionResult.success(result, executionTime);

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            throw new ActionException(ACTION_ID, "Failed to list Channels: " + e.getMessage(), e);
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
        return ActionMetadata.builder().description(DESCRIPTION).version(VERSION).author("openHAB")
                .tags(List.of("channels", "listing", "filtering")).build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("channel_listing", true);
        capabilities.put("filtering", true);
        capabilities.put("sorting", true);
        capabilities.put("pagination", true);
        capabilities.put("metadata_access", true);
        return capabilities;
    }

    @Override
    public void initialize(ActionContext context) {
        // No initialization required
    }

    @Override
    public void cleanup() {
        // No cleanup required
    }

    @Override
    public boolean isReady() {
        return thingRegistry != null && itemChannelLinkRegistry != null && channelTypeRegistry != null;
    }

    private Map<String, Object> listChannels(String thingUID, String channelType, String acceptedItemType, String kind,
            String binding, Boolean linkedOnly, Boolean unlinkedOnly, boolean includeConfiguration,
            boolean includeChannelType, boolean includeLinkedItems, String sortBy, String sortOrder, int limit,
            int offset) throws ActionException {

        Map<String, Object> result = new HashMap<>();
        result.put("timestamp", Instant.now().toString());

        if (thingRegistry == null) {
            throw new ActionException(ACTION_ID, "ThingRegistry service not available", "SERVICE_UNAVAILABLE");
        }

        // Get all Things and extract their channels
        Collection<Thing> allThings = thingRegistry.getAll();
        List<Map<String, Object>> channels = new ArrayList<>();

        for (Thing thing : allThings) {
            for (Channel channel : thing.getChannels()) {
                Map<String, Object> channelMap = convertChannelToMap(channel, thing, includeConfiguration,
                        includeChannelType, includeLinkedItems);
                channels.add(channelMap);
            }
        }

        // Apply filters
        List<Map<String, Object>> filteredChannels = applyFilters(channels, thingUID, channelType, acceptedItemType,
                kind, binding, linkedOnly, unlinkedOnly);

        // Sort results
        sortChannels(filteredChannels, sortBy, sortOrder);

        // Apply pagination
        int totalCount = filteredChannels.size();
        List<Map<String, Object>> paginatedChannels = applyPagination(filteredChannels, limit, offset);

        result.put("channels", paginatedChannels);
        result.put("totalCount", totalCount);
        result.put("returnedCount", paginatedChannels.size());
        result.put("offset", offset);
        result.put("limit", limit);
        result.put("hasMore", offset + paginatedChannels.size() < totalCount);

        // Add summary statistics
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalChannels", totalCount);
        summary.put("kindBreakdown", getKindBreakdown(filteredChannels));
        summary.put("typeBreakdown", getTypeBreakdown(filteredChannels));
        summary.put("bindingBreakdown", getBindingBreakdown(filteredChannels));
        summary.put("linkedChannels",
                filteredChannels.stream().mapToInt(c -> (Boolean) c.getOrDefault("isLinked", false) ? 1 : 0).sum());
        result.put("summary", summary);

        return result;
    }

    private Map<String, Object> convertChannelToMap(Channel channel, Thing thing, boolean includeConfiguration,
            boolean includeChannelType, boolean includeLinkedItems) {
        Map<String, Object> channelMap = new HashMap<>();

        ChannelUID channelUID = channel.getUID();
        channelMap.put("uid", channelUID.getAsString());
        channelMap.put("id", channelUID.getId());
        String channelLabel = "";
        if (channel.getLabel() != null) {
            channelLabel = channel.getLabel();
        }
        channelMap.put("label", channelLabel);

        String channelDescription = "";
        if (channel.getDescription() != null) {
            channelDescription = channel.getDescription();
        }
        channelMap.put("description", channelDescription);

        String channelKind = "";
        if (channel.getKind() != null) {
            channelKind = channel.getKind().toString();
        }
        channelMap.put("kind", channelKind);

        String channelAcceptedItemType = "";
        if (channel.getAcceptedItemType() != null) {
            channelAcceptedItemType = channel.getAcceptedItemType();
        }
        channelMap.put("acceptedItemType", channelAcceptedItemType);

        // Thing information
        channelMap.put("thingUID", thing.getUID().getAsString());

        String thingLabel = "";
        if (thing.getLabel() != null) {
            thingLabel = thing.getLabel();
        }
        channelMap.put("thingLabel", thingLabel);

        String bindingId = "";
        if (thing.getUID() != null && thing.getUID().getBindingId() != null) {
            bindingId = thing.getUID().getBindingId();
        }
        channelMap.put("binding", bindingId);

        // Channel type information
        if (channel.getChannelTypeUID() != null) {
            channelMap.put("channelTypeUID", channel.getChannelTypeUID().getAsString());
        }

        // Configuration
        if (includeConfiguration) {
            Configuration config = channel.getConfiguration();
            Map<String, Object> configMap = new HashMap<>();
            config.getProperties().forEach((key, value) -> configMap.put(key, value));
            channelMap.put("configuration", configMap);
        }

        // Detailed channel type information
        if (includeChannelType && channel.getChannelTypeUID() != null && channelTypeRegistry != null) {
            ChannelType channelType = channelTypeRegistry.getChannelType(channel.getChannelTypeUID());
            if (channelType != null) {
                Map<String, Object> typeInfo = new HashMap<>();
                String typeLabel = "";
                if (channelType.getLabel() != null) {
                    typeLabel = channelType.getLabel();
                }
                typeInfo.put("label", typeLabel);

                String typeDescription = "";
                if (channelType.getDescription() != null) {
                    typeDescription = channelType.getDescription();
                }
                typeInfo.put("description", typeDescription);

                String typeCategory = "";
                if (channelType.getCategory() != null) {
                    typeCategory = channelType.getCategory();
                }
                typeInfo.put("category", typeCategory);

                String typeItemType = "";
                if (channelType.getItemType() != null) {
                    typeItemType = channelType.getItemType();
                }
                typeInfo.put("itemType", typeItemType);

                String typeKind = "";
                if (channelType.getKind() != null) {
                    typeKind = channelType.getKind().toString();
                }
                typeInfo.put("kind", typeKind);
                channelMap.put("channelTypeInfo", typeInfo);
            }
        }

        // Linked items information
        boolean isLinked = false;
        if (includeLinkedItems && itemChannelLinkRegistry != null) {
            Set<ItemChannelLink> links = itemChannelLinkRegistry.getLinks(channelUID);
            if (!links.isEmpty()) {
                isLinked = true;
                List<Map<String, Object>> linkedItems = links.stream().map(link -> {
                    Map<String, Object> linkMap = new HashMap<>();
                    linkMap.put("itemName", link.getItemName());
                    linkMap.put("configuration", link.getConfiguration().getProperties());
                    return linkMap;
                }).collect(Collectors.toList());
                channelMap.put("linkedItems", linkedItems);
            }
        } else if (itemChannelLinkRegistry != null) {
            isLinked = !itemChannelLinkRegistry.getLinks(channelUID).isEmpty();
        }
        channelMap.put("isLinked", isLinked);

        return channelMap;
    }

    private List<Map<String, Object>> applyFilters(List<Map<String, Object>> channels, String thingUID,
            String channelType, String acceptedItemType, String kind, String binding, Boolean linkedOnly,
            Boolean unlinkedOnly) {

        return channels.stream().filter(channel -> {
            // Thing UID filter
            if (thingUID != null) {
                String channelThingUID = (String) channel.get("thingUID");
                if (!channelThingUID.equals(thingUID) && !channelThingUID.startsWith(thingUID + ":")) {
                    return false;
                }
            }

            // Channel type filter
            if (channelType != null) {
                String channelTypeUID = (String) channel.get("channelTypeUID");
                if (channelTypeUID == null || !channelTypeUID.equals(channelType)) {
                    return false;
                }
            }

            // Accepted item type filter
            if (acceptedItemType != null) {
                String channelAcceptedItemType = (String) channel.get("acceptedItemType");
                if (!acceptedItemType.equals(channelAcceptedItemType)) {
                    return false;
                }
            }

            // Kind filter
            if (kind != null) {
                String channelKind = (String) channel.get("kind");
                if (!kind.equals(channelKind)) {
                    return false;
                }
            }

            // Binding filter
            if (binding != null) {
                String channelBinding = (String) channel.get("binding");
                if (!binding.equals(channelBinding)) {
                    return false;
                }
            }

            // Linked filter
            if (Boolean.TRUE.equals(linkedOnly)) {
                Boolean isLinked = (Boolean) channel.get("isLinked");
                if (!Boolean.TRUE.equals(isLinked)) {
                    return false;
                }
            }

            if (Boolean.TRUE.equals(unlinkedOnly)) {
                Boolean isLinked = (Boolean) channel.get("isLinked");
                if (Boolean.TRUE.equals(isLinked)) {
                    return false;
                }
            }

            return true;
        }).collect(Collectors.toList());
    }

    private void sortChannels(List<Map<String, Object>> channels, String sortBy, String sortOrder) {
        boolean ascending = "asc".equals(sortOrder);

        channels.sort((c1, c2) -> {
            Object v1 = c1.get(sortBy);
            Object v2 = c2.get(sortBy);

            if (v1 == null && v2 == null)
                return 0;
            if (v1 == null)
                return ascending ? -1 : 1;
            if (v2 == null)
                return ascending ? 1 : -1;

            int comparison;
            if (v1 instanceof String && v2 instanceof String) {
                comparison = ((String) v1).compareToIgnoreCase((String) v2);
            } else if (v1 instanceof Boolean && v2 instanceof Boolean) {
                comparison = Boolean.compare((Boolean) v1, (Boolean) v2);
            } else {
                comparison = v1.toString().compareToIgnoreCase(v2.toString());
            }

            return ascending ? comparison : -comparison;
        });
    }

    private List<Map<String, Object>> applyPagination(List<Map<String, Object>> channels, int limit, int offset) {
        int startIndex = Math.min(offset, channels.size());
        int endIndex = Math.min(offset + limit, channels.size());
        return channels.subList(startIndex, endIndex);
    }

    private Map<String, Integer> getKindBreakdown(List<Map<String, Object>> channels) {
        Map<String, Integer> breakdown = new HashMap<>();
        for (Map<String, Object> channel : channels) {
            String kind = (String) channel.get("kind");
            breakdown.merge(kind, 1, Integer::sum);
        }
        return breakdown;
    }

    private Map<String, Integer> getTypeBreakdown(List<Map<String, Object>> channels) {
        Map<String, Integer> breakdown = new HashMap<>();
        for (Map<String, Object> channel : channels) {
            String acceptedItemType = (String) channel.get("acceptedItemType");
            if (acceptedItemType != null) {
                breakdown.merge(acceptedItemType, 1, Integer::sum);
            }
        }
        return breakdown;
    }

    private Map<String, Integer> getBindingBreakdown(List<Map<String, Object>> channels) {
        Map<String, Integer> breakdown = new HashMap<>();
        for (Map<String, Object> channel : channels) {
            String binding = (String) channel.get("binding");
            breakdown.merge(binding, 1, Integer::sum);
        }
        return breakdown;
    }
}
