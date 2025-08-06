package org.openhab.core.ai.tool.adapter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.dto.AbstractResource;
import org.openhab.core.ai.tool.dto.Resource;
import org.openhab.core.ai.tool.factory.ResourceFactory;
import org.openhab.core.ai.tool.proxy.ThingResourceProxy;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resource adapter for openHAB things.
 * 
 * This adapter provides MCP resource access to openHAB things, allowing
 * reading and writing of thing states and metadata.
 * 
 * Updated to use encapsulated architecture internally while maintaining
 * the same public interface for backward compatibility.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ThingResourceAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThingResourceAdapter.class);

    private final ThingRegistry thingRegistry;
    private final ResourceFactory resourceFactory;

    /**
     * Create a new ThingResourceAdapter.
     *
     * @param thingRegistry the thing registry
     */
    public ThingResourceAdapter(ThingRegistry thingRegistry) {
        this.thingRegistry = thingRegistry;
        this.resourceFactory = new ResourceFactory();
    }

    /**
     * Create a resource for an openHAB thing.
     *
     * @param thingUID the UID of the thing
     * @return the resource or null if the thing doesn't exist
     */
    public @Nullable Resource createThingResource(String thingUID) {
        try {
            ThingUID uid = new ThingUID(thingUID);
            Thing thing = thingRegistry.get(uid);
            if (thing == null) {
                LOGGER.debug("Thing not found: {}", thingUID);
                return null;
            }

            String uri = "openhab://things/" + thingUID;
            String name = "Thing: " + thing.getLabel();
            String description = "Resource adapter for openHAB thing: " + thing.getLabel();
            String mimeType = "application/json";

            // Create metadata with thing information
            Map<String, Object> metadata = new ConcurrentHashMap<>();
            metadata.put("type", "openhab-thing");
            metadata.put("thingUID", thingUID);
            metadata.put("uri", uri);
            metadata.put("thingType", thing.getThingTypeUID().toString());
            metadata.put("label", thing.getLabel());
            metadata.put("location", thing.getLocation());
            metadata.put("status", thing.getStatus().toString());

            return new Resource(uri, name, description, mimeType, metadata);
        } catch (Exception e) {
            LOGGER.error("Error creating resource for thing: {}", thingUID, e);
            return null;
        }
    }

    /**
     * Get resource content for a thing.
     *
     * @param thingUID the UID of the thing
     * @return the thing content as a string or null if the thing doesn't exist
     */
    public @Nullable String getThingContent(String thingUID) {
        AbstractResource resource = getOrCreateEncapsulatedResource(thingUID);
        return resource != null ? resource.getContent() : null;
    }

    /**
     * Check if a thing is writable.
     *
     * @param thingUID the UID of the thing
     * @return true if the thing is writable
     */
    public boolean isThingWritable(String thingUID) {
        AbstractResource resource = getOrCreateEncapsulatedResource(thingUID);
        return resource != null && resource.isWritable();
    }

    /**
     * Write content to a thing.
     *
     * @param thingUID the UID of the thing
     * @param content the content to write
     * @return true if successful
     */
    public boolean writeThingContent(String thingUID, @Nullable String content) {
        AbstractResource resource = getOrCreateEncapsulatedResource(thingUID);
        return resource != null && resource.writeContent(content);
    }

    /**
     * Check if a thing exists.
     *
     * @param thingUID the UID of the thing
     * @return true if the thing exists
     */
    public boolean thingExists(String thingUID) {
        AbstractResource resource = getOrCreateEncapsulatedResource(thingUID);
        return resource != null && resource.exists();
    }

    /**
     * Get or create an encapsulated resource for the thing.
     *
     * @param thingUID the UID of the thing
     * @return the encapsulated resource or null if the thing doesn't exist
     */
    private @Nullable AbstractResource getOrCreateEncapsulatedResource(String thingUID) {
        String uri = "openhab://things/" + thingUID;

        return resourceFactory.createResource(uri, (resourceUri, refreshIntervalMs) -> {
            try {
                ThingUID uid = new ThingUID(thingUID);
                if (thingRegistry.get(uid) == null) {
                    LOGGER.debug("Thing not found: {}", thingUID);
                    return null;
                }

                return new ThingResourceProxy(thingRegistry, thingUID, refreshIntervalMs);
            } catch (Exception e) {
                LOGGER.error("Error creating resource for thing: {}", thingUID, e);
                return null;
            }
        });
    }

    /**
     * Clean up resources managed by this adapter.
     */
    public void cleanup() {
        LOGGER.debug("Cleaning up ThingResourceAdapter resources");
        resourceFactory.cleanup();
    }
}
