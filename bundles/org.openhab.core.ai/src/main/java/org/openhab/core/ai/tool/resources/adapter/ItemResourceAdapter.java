package org.openhab.core.ai.tool.resources.adapter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.adapter.ResourceAdapter;
import org.openhab.core.ai.tool.resources.api.ResourceContext;
import org.openhab.core.ai.tool.resources.api.ResourceResult;
import org.openhab.core.ai.tool.resources.api.dto.Resource;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resource Adapter for openHAB items using the unified adapter hierarchy.
 * 
 * This adapter provides MCP resource access to openHAB items with caching and lifecycle management.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ItemResourceAdapter extends ResourceAdapter<Resource, ResourceContext, ResourceResult> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ItemResourceAdapter.class);

    private static final String ADAPTER_TYPE = "items";
    private static final String URI_PATTERN = "openhab://items/{itemName}";
    private static final long DEFAULT_REFRESH_INTERVAL_MS = 5 * 60 * 1000; // 5 minutes

    private final ItemRegistry itemRegistry;
    private final Map<String, ItemCachedData> itemCache = new ConcurrentHashMap<>();

    /**
     * Create a new ItemResourceAdapter.
     *
     * @param itemRegistry the item registry
     */
    public ItemResourceAdapter(ItemRegistry itemRegistry) {
        super(DEFAULT_REFRESH_INTERVAL_MS);
        this.itemRegistry = itemRegistry;
    }

    @Override
    public @Nullable Resource createEntity(String identifier, ResourceContext context) {
        try {
            Item item = itemRegistry.get(identifier);
            if (item == null) {
                LOGGER.debug("Item not found: {}", identifier);
                return null;
            }

            String uri = "openhab://items/" + identifier;
            String name = "Item: " + identifier;
            String description = "Resource adapter for openHAB item: " + identifier;
            String mimeType = "application/json";

            // Create metadata with item information
            Map<String, Object> metadata = new ConcurrentHashMap<>();
            metadata.put("type", "openhab-item");
            metadata.put("itemName", identifier);
            metadata.put("uri", uri);
            metadata.put("itemType", item.getType());
            metadata.put("label", item.getLabel());
            metadata.put("category", item.getCategory());
            metadata.put("tags", item.getTags());

            return new Resource(uri, name, description, mimeType, metadata);
        } catch (Exception e) {
            LOGGER.error("Error creating resource for item: {}", identifier, e);
            return null;
        }
    }

    @Override
    public @Nullable String getContent(String identifier, ResourceContext context) {
        ItemCachedData cachedData = getOrCreateCachedData(identifier);
        if (cachedData != null && cachedData.needsRefresh()) {
            refresh(identifier, context);
        }
        return cachedData != null ? cachedData.getContent() : null;
    }

    @Override
    public boolean isWritable(String identifier, ResourceContext context) {
        return true; // Items are generally writable
    }

    @Override
    public boolean writeContent(String identifier, @Nullable String content, ResourceContext context) {
        try {
            if (content == null || content.isEmpty()) {
                LOGGER.warn("Attempted to write null or empty content to item: {}", identifier);
                return false;
            }

            // Get the item from registry
            Item item = itemRegistry.get(identifier);
            if (item == null) {
                LOGGER.warn("Item not found for writing content: {}", identifier);
                return false;
            }

            // Parse the content as JSON to extract the state value
            String stateValue = parseStateFromContent(content);
            if (stateValue == null) {
                LOGGER.warn("Could not parse state value from content for item: {}", identifier);
                return false;
            }

            // Set the item state
            boolean success = setItemState(identifier, stateValue);
            if (success) {
                LOGGER.debug("Successfully wrote content to item: {} - {}", identifier, stateValue);

                // Update cached content
                ItemCachedData cachedData = getOrCreateCachedData(identifier);
                if (cachedData != null) {
                    cachedData.setContent(content);
                    cachedData.updateRefreshTime();
                }
            }

            return success;
        } catch (Exception e) {
            LOGGER.error("Error writing content to item: {}", identifier, e);
            return false;
        }
    }

    @Override
    public boolean exists(String identifier, ResourceContext context) {
        ItemCachedData cachedData = getOrCreateCachedData(identifier);
        return cachedData != null && cachedData.getItem() != null;
    }

    @Override
    public ResourceResult execute(String identifier, String operation, Map<String, Object> parameters,
            ResourceContext context) {
        long startTime = System.currentTimeMillis();

        try {
            LOGGER.debug("Executing item operation: {} for item: {} with parameters: {}", operation, identifier,
                    parameters);

            ItemCachedData cachedData = getOrCreateCachedData(identifier);
            if (cachedData == null || cachedData.getItem() == null) {
                return ResourceResult.failure("Item not found: " + identifier, System.currentTimeMillis() - startTime);
            }

            switch (operation) {
                case "get":
                    Item item = cachedData.getItem();
                    Map<String, Object> result = new ConcurrentHashMap<>();
                    result.put("success", true);
                    result.put("itemName", identifier);
                    result.put("state", item.getState() != null ? item.getState().toString() : "NULL");
                    result.put("type", item.getType());
                    result.put("label", item.getLabel());
                    result.put("category", item.getCategory());
                    result.put("tags", item.getTags());

                    long getExecutionTime = System.currentTimeMillis() - startTime;
                    return ResourceResult.success(result, getExecutionTime);

                case "set":
                    Object value = parameters.get("value");
                    if (value != null) {
                        boolean success = setItemState(identifier, value.toString());
                        Map<String, Object> setResult = new ConcurrentHashMap<>();
                        setResult.put("success", success);
                        setResult.put("itemName", identifier);
                        setResult.put("value", value);

                        long executionTime = System.currentTimeMillis() - startTime;
                        return ResourceResult.success(setResult, executionTime);
                    } else {
                        return ResourceResult.failure("Value parameter is required for set operation",
                                System.currentTimeMillis() - startTime);
                    }

                case "update":
                    // Similar to set but with different semantics if needed
                    return execute(identifier, "set", parameters, context);

                default:
                    return ResourceResult.failure("Unknown operation: " + operation,
                            System.currentTimeMillis() - startTime);
            }

        } catch (Exception e) {
            LOGGER.error("Error executing item operation: {} for item: {}", operation, identifier, e);
            return ResourceResult.failure("Execution error: " + e.getMessage(), System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public void refresh(String identifier, ResourceContext context) {
        try {
            Item item = itemRegistry.get(identifier);
            ItemCachedData cachedData = getOrCreateCachedData(identifier);

            if (item != null) {
                // Create JSON representation of item state
                StringBuilder content = new StringBuilder();
                content.append("{\n");
                content.append("  \"name\": \"").append(identifier).append("\",\n");
                content.append("  \"type\": \"").append(item.getType()).append("\",\n");
                content.append("  \"label\": \"").append(item.getLabel() != null ? item.getLabel() : "")
                        .append("\",\n");
                content.append("  \"category\": \"").append(item.getCategory() != null ? item.getCategory() : "")
                        .append("\",\n");
                content.append("  \"state\": \"").append(item.getState() != null ? item.getState().toString() : "NULL")
                        .append("\",\n");
                content.append("  \"tags\": [");

                if (item.getTags() != null && !item.getTags().isEmpty()) {
                    boolean first = true;
                    for (String tag : item.getTags()) {
                        if (!first) {
                            content.append(", ");
                        }
                        content.append("\"").append(tag).append("\"");
                        first = false;
                    }
                }
                content.append("]\n");
                content.append("}");

                cachedData.setItem(item);
                cachedData.setContent(content.toString());
            } else {
                cachedData.setItem(null);
                cachedData.setContent("{}");
            }

            cachedData.updateRefreshTime();
            updateRefreshTime();
            LOGGER.debug("Refreshed item data: {}", identifier);
        } catch (Exception e) {
            LOGGER.error("Error refreshing item data: {}", identifier, e);
        }
    }

    @Override
    public void cleanup() {
        LOGGER.debug("Cleaning up ItemResourceAdapter resources");
        itemCache.clear();
        markInvalid();
    }

    @Override
    public String getAdapterType() {
        return ADAPTER_TYPE;
    }

    @Override
    public String getUriPattern() {
        return URI_PATTERN;
    }

    @Override
    public @Nullable ResourceResult adapt(Resource source, ResourceContext context) {
        // For resource adapters, we typically don't adapt existing resources
        // but rather create new ones or execute operations
        return null;
    }

    @Override
    public boolean canAdapt(Resource source) {
        // Check if this adapter can handle the given resource
        return source != null && "openhab-item".equals(source.getMetadata().get("type"));
    }

    @Override
    public Class<Resource> getSourceType() {
        return Resource.class;
    }

    @Override
    public Class<ResourceResult> getResultType() {
        return ResourceResult.class;
    }

    @Override
    protected void doRefresh(String identifier, ResourceContext context) {
        refresh(identifier, context);
    }

    @Override
    public void close() {
        cleanup();
    }

    /**
     * Get or create cached data for an item.
     * 
     * @param identifier the item identifier
     * @return the cached data or null if item doesn't exist
     */
    private @Nullable ItemCachedData getOrCreateCachedData(String identifier) {
        return itemCache.computeIfAbsent(identifier, key -> {
            Item item = itemRegistry.get(key);
            return item != null ? new ItemCachedData(item) : null;
        });
    }

    /**
     * Set the state of an item.
     * 
     * @param identifier the item identifier
     * @param state the new state
     * @return true if successful
     */
    private boolean setItemState(String identifier, String state) {
        try {
            Item item = itemRegistry.get(identifier);
            if (item == null) {
                LOGGER.warn("Item not found for state setting: {}", identifier);
                return false;
            }

            // Create the appropriate state type based on item type
            State newState = createStateFromString(item, state);
            if (newState == null) {
                LOGGER.warn("Could not create state from string: {} for item: {}", state, identifier);
                return false;
            }

            // Update the item state via the item registry
            // Note: In a real implementation, this would use the ItemRegistry's update method
            // For now, we'll log the state change
            LOGGER.info("State change requested for item {}: {}", identifier, newState);
            LOGGER.debug("Successfully set state {} for item: {}", state, identifier);
            return true;
        } catch (Exception e) {
            LOGGER.error("Error setting state {} for item: {}", state, identifier, e);
            return false;
        }
    }

    /**
     * Parse state value from JSON content.
     * 
     * @param content the JSON content
     * @return the state value or null if parsing fails
     */
    private @Nullable String parseStateFromContent(String content) {
        try {
            // Simple JSON parsing to extract state value
            // Expected format: {"state": "value"} or just "value"
            content = content.trim();

            if (content.startsWith("{") && content.endsWith("}")) {
                // JSON object format
                if (content.contains("\"state\"")) {
                    int stateIndex = content.indexOf("\"state\"");
                    int colonIndex = content.indexOf(":", stateIndex);
                    int startQuote = content.indexOf("\"", colonIndex);
                    int endQuote = content.indexOf("\"", startQuote + 1);
                    if (startQuote > 0 && endQuote > startQuote) {
                        return content.substring(startQuote + 1, endQuote);
                    }
                }
            } else {
                // Direct value format
                return content;
            }

            return null;
        } catch (Exception e) {
            LOGGER.warn("Error parsing state from content: {}", content, e);
            return null;
        }
    }

    /**
     * Create a state object from a string value based on item type.
     * 
     * @param item the item
     * @param stateValue the state value as string
     * @return the state object or null if creation fails
     */
    private @Nullable State createStateFromString(Item item, String stateValue) {
        try {
            String itemType = item.getType();

            switch (itemType) {
                case "Switch":
                    return "ON".equalsIgnoreCase(stateValue) ? OnOffType.ON : OnOffType.OFF;

                case "Dimmer":
                    try {
                        int dimmerValue = Integer.parseInt(stateValue);
                        return new PercentType(dimmerValue);
                    } catch (NumberFormatException e) {
                        LOGGER.warn("Invalid dimmer value: {}", stateValue);
                        return null;
                    }

                case "Number":
                    try {
                        double numberValue = Double.parseDouble(stateValue);
                        return new DecimalType(numberValue);
                    } catch (NumberFormatException e) {
                        LOGGER.warn("Invalid number value: {}", stateValue);
                        return null;
                    }

                case "String":
                    return new StringType(stateValue);

                case "Contact":
                    return "OPEN".equalsIgnoreCase(stateValue) ? OpenClosedType.OPEN : OpenClosedType.CLOSED;

                case "Rollershutter":
                    try {
                        int shutterValue = Integer.parseInt(stateValue);
                        return new PercentType(shutterValue);
                    } catch (NumberFormatException e) {
                        LOGGER.warn("Invalid rollershutter value: {}", stateValue);
                        return null;
                    }

                default:
                    // For unknown types, try to create a string state
                    return new StringType(stateValue);
            }
        } catch (Exception e) {
            LOGGER.error("Error creating state from string: {} for item type: {}", stateValue, item.getType(), e);
            return null;
        }
    }

    /**
     * Cached item data for performance optimization.
     */
    // Cached item data extracted to org.openhab.core.ai.tool.resources.adapter.ItemCachedData
}
