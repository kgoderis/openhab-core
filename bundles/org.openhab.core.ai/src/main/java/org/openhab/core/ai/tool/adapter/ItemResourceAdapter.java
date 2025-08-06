package org.openhab.core.ai.tool.adapter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.dto.AbstractResource;
import org.openhab.core.ai.tool.dto.Resource;
import org.openhab.core.ai.tool.factory.ResourceFactory;
import org.openhab.core.ai.tool.proxy.ItemResourceProxy;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resource adapter for openHAB items.
 * 
 * This adapter provides MCP resource access to openHAB items, allowing
 * reading and writing of item states and metadata.
 * 
 * Updated to use encapsulated architecture internally while maintaining
 * the same public interface for backward compatibility.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ItemResourceAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ItemResourceAdapter.class);

    private final ItemRegistry itemRegistry;
    private final ResourceFactory resourceFactory;

    /**
     * Create a new ItemResourceAdapter.
     *
     * @param itemRegistry the item registry
     */
    public ItemResourceAdapter(ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
        this.resourceFactory = new ResourceFactory();
    }

    /**
     * Create a resource for an openHAB item.
     *
     * @param itemName the name of the item
     * @return the resource or null if the item doesn't exist
     */
    public @Nullable Resource createItemResource(String itemName) {
        try {
            Item item = itemRegistry.get(itemName);
            if (item == null) {
                LOGGER.debug("Item not found: {}", itemName);
                return null;
            }

            String uri = "openhab://items/" + itemName;
            String name = "Item: " + itemName;
            String description = "Resource adapter for openHAB item: " + itemName;
            String mimeType = "application/json";

            // Create metadata with item information
            Map<String, Object> metadata = new ConcurrentHashMap<>();
            metadata.put("type", "openhab-item");
            metadata.put("itemName", itemName);
            metadata.put("uri", uri);
            metadata.put("itemType", item.getType());
            metadata.put("label", item.getLabel());
            metadata.put("category", item.getCategory());
            metadata.put("tags", item.getTags());

            return new Resource(uri, name, description, mimeType, metadata);
        } catch (Exception e) {
            LOGGER.error("Error creating resource for item: {}", itemName, e);
            return null;
        }
    }

    /**
     * Get resource content for an item.
     *
     * @param itemName the name of the item
     * @return the item content as a string or null if the item doesn't exist
     */
    public @Nullable String getItemContent(String itemName) {
        AbstractResource resource = getOrCreateEncapsulatedResource(itemName);
        return resource != null ? resource.getContent() : null;
    }

    /**
     * Check if an item is writable.
     *
     * @param itemName the name of the item
     * @return true if the item is writable
     */
    public boolean isItemWritable(String itemName) {
        AbstractResource resource = getOrCreateEncapsulatedResource(itemName);
        return resource != null && resource.isWritable();
    }

    /**
     * Write content to an item.
     *
     * @param itemName the name of the item
     * @param content the content to write
     * @return true if successful
     */
    public boolean writeItemContent(String itemName, @Nullable String content) {
        AbstractResource resource = getOrCreateEncapsulatedResource(itemName);
        return resource != null && resource.writeContent(content);
    }

    /**
     * Check if an item exists.
     *
     * @param itemName the name of the item
     * @return true if the item exists
     */
    public boolean itemExists(String itemName) {
        AbstractResource resource = getOrCreateEncapsulatedResource(itemName);
        return resource != null && resource.exists();
    }

    /**
     * Extract state value from JSON content.
     * This is a simplified implementation - in practice, use a proper JSON parser.
     *
     * @param content the JSON content
     * @return the state value or null if not found
     */
    public @Nullable String extractStateFromJson(String content) {
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
     * Get or create an encapsulated resource for the item.
     *
     * @param itemName the name of the item
     * @return the encapsulated resource or null if the item doesn't exist
     */
    private @Nullable AbstractResource getOrCreateEncapsulatedResource(String itemName) {
        String uri = "openhab://items/" + itemName;

        return resourceFactory.createResource(uri, (resourceUri, refreshIntervalMs) -> {
            try {
                // Check if item exists before creating proxy
                if (itemRegistry.get(itemName) == null) {
                    LOGGER.debug("Item not found: {}", itemName);
                    return null;
                }

                return new ItemResourceProxy(itemRegistry, itemName, refreshIntervalMs);
            } catch (Exception e) {
                LOGGER.error("Error creating resource for item: {}", itemName, e);
                return null;
            }
        });
    }

    /**
     * Clean up resources managed by this adapter.
     */
    public void cleanup() {
        LOGGER.debug("Cleaning up ItemResourceAdapter resources");
        resourceFactory.cleanup();
    }
}
