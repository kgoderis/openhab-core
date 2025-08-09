package org.openhab.core.ai.tool.resources.adapter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.adapter.BaseAdapter;
import org.openhab.core.ai.tool.api.Adapter;
import org.openhab.core.ai.tool.api.ResourceContext;
import org.openhab.core.ai.tool.api.ResourceResult;
import org.openhab.core.ai.tool.resources.dto.Resource;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Consolidated Resource Adapter for openHAB items.
 * 
 * This adapter combines the functionality of both the old Adapter and Proxy classes,
 * providing MCP resource access to openHAB items with caching and lifecycle management.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ItemResourceAdapter extends BaseAdapter implements Adapter<Resource, ResourceContext, ResourceResult> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ItemResourceAdapter.class);

    private static final String ADAPTER_TYPE = "items";
    private static final String URI_PATTERN = "openhab://items/{itemName}";
    private static final long DEFAULT_REFRESH_INTERVAL_MS = 5 * 60 * 1000; // 5 minutes

    private final ItemRegistry itemRegistry;
    private final Map<String, CachedItemData> itemCache = new ConcurrentHashMap<>();

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
        CachedItemData cachedData = getOrCreateCachedData(identifier);
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

            // TODO: Implement actual item state writing logic
            // This would involve updating the item state via ItemRegistry
            LOGGER.debug("Writing content to item: {} - {}", identifier, content);

            // Update cached content
            CachedItemData cachedData = getOrCreateCachedData(identifier);
            if (cachedData != null) {
                cachedData.setContent(content);
                cachedData.updateRefreshTime();
            }

            return true;
        } catch (Exception e) {
            LOGGER.error("Error writing content to item: {}", identifier, e);
            return false;
        }
    }

    @Override
    public boolean exists(String identifier, ResourceContext context) {
        CachedItemData cachedData = getOrCreateCachedData(identifier);
        return cachedData != null && cachedData.getItem() != null;
    }

    @Override
    public ResourceResult execute(String identifier, String operation, Map<String, Object> parameters,
            ResourceContext context) {
        long startTime = System.currentTimeMillis();

        try {
            LOGGER.debug("Executing item operation: {} for item: {} with parameters: {}", operation, identifier,
                    parameters);

            CachedItemData cachedData = getOrCreateCachedData(identifier);
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
            CachedItemData cachedData = getOrCreateCachedData(identifier);

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
    public void close() {
        cleanup();
    }

    /**
     * Get or create cached data for an item.
     * 
     * @param identifier the item identifier
     * @return the cached data or null if item doesn't exist
     */
    private @Nullable CachedItemData getOrCreateCachedData(String identifier) {
        return itemCache.computeIfAbsent(identifier, key -> {
            Item item = itemRegistry.get(key);
            return item != null ? new CachedItemData(item) : null;
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
            // TODO: Implement actual state setting logic
            // This would involve updating the item state via ItemRegistry
            LOGGER.debug("Setting state {} for item: {}", state, identifier);
            return true;
        } catch (Exception e) {
            LOGGER.error("Error setting state {} for item: {}", state, identifier, e);
            return false;
        }
    }

    /**
     * Cached item data for performance optimization.
     */
    private static class CachedItemData {
        private volatile @Nullable Item item;
        private volatile @Nullable String content;
        private volatile long lastRefreshTime = 0;
        private final long refreshIntervalMs = 5 * 60 * 1000; // 5 minutes

        public CachedItemData(Item item) {
            this.item = item;
        }

        public @Nullable Item getItem() {
            return item;
        }

        public void setItem(@Nullable Item item) {
            this.item = item;
        }

        public @Nullable String getContent() {
            return content;
        }

        public void setContent(@Nullable String content) {
            this.content = content;
        }

        public boolean needsRefresh() {
            return System.currentTimeMillis() - lastRefreshTime > refreshIntervalMs;
        }

        public void updateRefreshTime() {
            lastRefreshTime = System.currentTimeMillis();
        }
    }
}
