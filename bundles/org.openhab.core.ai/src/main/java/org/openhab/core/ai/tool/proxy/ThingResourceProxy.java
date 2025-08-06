package org.openhab.core.ai.tool.proxy;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.dto.AbstractResource;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingUID;

/**
 * Encapsulated resource proxy for openHAB things.
 * 
 * This class extends AbstractResource to provide proper encapsulation,
 * lifecycle management, and state caching for openHAB things.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ThingResourceProxy extends AbstractResource {

    private final ThingRegistry thingRegistry;
    private final String thingUID;
    private volatile @Nullable Thing cachedThing;
    private volatile @Nullable String cachedContent;

    /**
     * Create a new ThingResourceProxy.
     *
     * @param thingRegistry the thing registry
     * @param thingUID the UID of the thing
     * @param refreshIntervalMs the refresh interval in milliseconds
     */
    public ThingResourceProxy(ThingRegistry thingRegistry, String thingUID, long refreshIntervalMs) {
        super("openhab://things/" + thingUID, "Thing: " + thingUID, "Resource adapter for openHAB thing: " + thingUID,
                "application/json", createMetadata(thingUID), refreshIntervalMs);

        this.thingRegistry = thingRegistry;
        this.thingUID = thingUID;
    }

    /**
     * Create metadata for the thing.
     *
     * @param thingUID the thing UID
     * @return the metadata map
     */
    private static Map<String, Object> createMetadata(String thingUID) {
        Map<String, Object> metadata = new ConcurrentHashMap<>();
        metadata.put("type", "openhab-thing");
        metadata.put("thingUID", thingUID);
        metadata.put("uri", "openhab://things/" + thingUID);
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
        // Things are generally not directly writable through this interface
        // Configuration changes would go through the ThingRegistry
        return false;
    }

    @Override
    public boolean writeContent(@Nullable String content) {
        if (!isWritable()) {
            LOGGER.warn("Thing is not writable: {}", thingUID);
            return false;
        }

        // Things are not directly writable through this interface
        LOGGER.warn("Writing content to things is not supported: {}", thingUID);
        return false;
    }

    @Override
    public boolean exists() {
        if (needsRefresh()) {
            refresh();
        }
        return cachedThing != null;
    }

    @Override
    public void refresh() {
        try {
            ThingUID uid = new ThingUID(thingUID);
            Thing thing = thingRegistry.get(uid);
            if (thing == null) {
                LOGGER.debug("Thing not found during refresh: {}", thingUID);
                cachedThing = null;
                cachedContent = null;
                markInvalid();
                return;
            }

            cachedThing = thing;
            cachedContent = createThingJson(thing);

            // Update metadata with current thing information
            metadata.put("thingTypeUID", thing.getThingTypeUID().toString());
            metadata.put("label", thing.getLabel());
            metadata.put("location", thing.getLocation());
            metadata.put("properties", thing.getProperties());
            metadata.put("status", thing.getStatus().toString());

            updateRefreshTime();
            LOGGER.debug("Refreshed thing resource: {}", thingUID);
        } catch (Exception e) {
            LOGGER.error("Error refreshing thing resource: {}", thingUID, e);
            markInvalid();
        }
    }

    @Override
    public void close() {
        LOGGER.debug("Closing thing resource: {}", thingUID);
        cachedThing = null;
        cachedContent = null;
        markInvalid();
    }

    /**
     * Create JSON representation of thing state and metadata.
     *
     * @param thing the thing to convert to JSON
     * @return the JSON string
     */
    private String createThingJson(Thing thing) {
        StringBuilder content = new StringBuilder();
        content.append("{");
        content.append("\"uid\":\"").append(thing.getUID().toString()).append("\",");
        content.append("\"thingTypeUID\":\"").append(thing.getThingTypeUID().toString()).append("\",");
        content.append("\"status\":\"").append(thing.getStatus().toString()).append("\",");

        if (thing.getLabel() != null) {
            content.append("\"label\":\"").append(thing.getLabel()).append("\",");
        }

        if (thing.getLocation() != null) {
            content.append("\"location\":\"").append(thing.getLocation()).append("\",");
        }

        content.append("\"properties\":{");
        boolean first = true;
        for (Map.Entry<String, String> entry : thing.getProperties().entrySet()) {
            if (!first) {
                content.append(",");
            }
            content.append("\"").append(entry.getKey()).append("\":\"").append(entry.getValue()).append("\"");
            first = false;
        }
        content.append("}");
        content.append("}");

        return content.toString();
    }

    /**
     * Get the underlying thing.
     *
     * @return the thing or null if not available
     */
    public @Nullable Thing getThing() {
        if (needsRefresh()) {
            refresh();
        }
        return cachedThing;
    }

    /**
     * Get the thing UID.
     *
     * @return the thing UID
     */
    public String getThingUID() {
        return thingUID;
    }
}
