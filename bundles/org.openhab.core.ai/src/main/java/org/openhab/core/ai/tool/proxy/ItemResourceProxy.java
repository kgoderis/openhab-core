package org.openhab.core.ai.tool.proxy;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.dto.AbstractResource;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;

/**
 * Encapsulated resource proxy for openHAB items.
 * 
 * This class extends AbstractResource to provide proper encapsulation,
 * lifecycle management, and state caching for openHAB items.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ItemResourceProxy extends AbstractResource {

    private final ItemRegistry itemRegistry;
    private final String itemName;
    private volatile @Nullable Item cachedItem;
    private volatile @Nullable String cachedContent;

    /**
     * Create a new ItemResourceProxy.
     *
     * @param itemRegistry the item registry
     * @param itemName the name of the item
     * @param refreshIntervalMs the refresh interval in milliseconds
     */
    public ItemResourceProxy(ItemRegistry itemRegistry, String itemName, long refreshIntervalMs) {
        super("openhab://items/" + itemName, "Item: " + itemName, "Resource adapter for openHAB item: " + itemName,
                "application/json", createMetadata(itemName), refreshIntervalMs);

        this.itemRegistry = itemRegistry;
        this.itemName = itemName;
    }

    /**
     * Create metadata for the item.
     *
     * @param itemName the item name
     * @return the metadata map
     */
    private static Map<String, Object> createMetadata(String itemName) {
        Map<String, Object> metadata = new ConcurrentHashMap<>();
        metadata.put("type", "openhab-item");
        metadata.put("itemName", itemName);
        metadata.put("uri", "openhab://items/" + itemName);
        return metadata;
    }

    @Override
    public @Nullable String getContent() {
        if (needsRefresh()) {
            refresh();
        }
        return cachedContent;
    }

    @Override
    public boolean isWritable() {
        if (needsRefresh()) {
            refresh();
        }
        return cachedItem != null && cachedItem.getAcceptedCommandTypes().size() > 0;
    }

    @Override
    public boolean writeContent(@Nullable String content) {
        if (!isWritable()) {
            LOGGER.warn("Item is not writable: {}", itemName);
            return false;
        }

        if (content == null || content.trim().isEmpty()) {
            LOGGER.warn("Content is null or empty for item: {}", itemName);
            return false;
        }

        try {
            // Parse JSON content and update item state
            if (content.contains("\"state\":")) {
                String stateValue = extractStateFromJson(content);
                if (stateValue != null) {
                    // Use the item registry to send command
                    // Note: This would need to be implemented with proper openHAB command handling
                    LOGGER.info("Would update item {} state to: {}", itemName, stateValue);

                    // Refresh the cache after write
                    refresh();
                    return true;
                }
            }

            LOGGER.warn("Invalid content format for item: {}", itemName);
            return false;
        } catch (Exception e) {
            LOGGER.error("Error writing content to item: {}", itemName, e);
            return false;
        }
    }

    @Override
    public boolean exists() {
        if (needsRefresh()) {
            refresh();
        }
        return cachedItem != null;
    }

    @Override
    public void refresh() {
        try {
            Item item = itemRegistry.get(itemName);
            if (item == null) {
                LOGGER.debug("Item not found during refresh: {}", itemName);
                cachedItem = null;
                cachedContent = null;
                markInvalid();
                return;
            }

            cachedItem = item;
            cachedContent = createItemJson(item);

            // Update metadata with current item information
            metadata.put("itemType", item.getType());
            metadata.put("label", item.getLabel());
            metadata.put("category", item.getCategory());
            metadata.put("tags", item.getTags());

            updateRefreshTime();
            LOGGER.debug("Refreshed item resource: {}", itemName);
        } catch (Exception e) {
            LOGGER.error("Error refreshing item resource: {}", itemName, e);
            markInvalid();
        }
    }

    @Override
    public void close() {
        LOGGER.debug("Closing item resource: {}", itemName);
        cachedItem = null;
        cachedContent = null;
        markInvalid();
    }

    /**
     * Create JSON representation of item state and metadata.
     *
     * @param item the item to convert to JSON
     * @return the JSON string
     */
    private String createItemJson(Item item) {
        StringBuilder content = new StringBuilder();
        content.append("{");
        content.append("\"name\":\"").append(item.getName()).append("\",");
        content.append("\"type\":\"").append(item.getType()).append("\",");
        content.append("\"state\":\"").append(item.getState().toString()).append("\",");

        if (item.getLabel() != null) {
            content.append("\"label\":\"").append(item.getLabel()).append("\",");
        }

        if (item.getCategory() != null) {
            content.append("\"category\":\"").append(item.getCategory()).append("\",");
        }

        content.append("\"tags\":[").append(String.join(",", item.getTags())).append("]");
        content.append("}");

        return content.toString();
    }

    /**
     * Extract state value from JSON content.
     * This is a simplified implementation - in practice, use a proper JSON parser.
     *
     * @param content the JSON content
     * @return the state value or null if not found
     */
    private @Nullable String extractStateFromJson(String content) {
        try {
            int stateIndex = content.indexOf("\"state\":");
            if (stateIndex >= 0) {
                int startQuote = content.indexOf("\"", stateIndex + 8);
                if (startQuote >= 0) {
                    int endQuote = content.indexOf("\"", startQuote + 1);
                    if (endQuote >= 0) {
                        return content.substring(startQuote + 1, endQuote);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error extracting state from JSON: {}", content, e);
        }
        return null;
    }

    /**
     * Get the underlying item.
     *
     * @return the item or null if not available
     */
    public @Nullable Item getItem() {
        if (needsRefresh()) {
            refresh();
        }
        return cachedItem;
    }

    /**
     * Get the item name.
     *
     * @return the item name
     */
    public String getItemName() {
        return itemName;
    }
}
