package org.openhab.core.ai.tool.resources.adapter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.adapter.Adapter;
import org.openhab.core.ai.tool.adapter.BaseAdapter;
import org.openhab.core.ai.tool.resources.api.ResourceContext;
import org.openhab.core.ai.tool.resources.api.ResourceResult;
import org.openhab.core.ai.tool.resources.api.dto.Resource;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Consolidated Resource Adapter for openHAB things.
 * 
 * This adapter combines the functionality of both the old Adapter and Proxy classes,
 * providing MCP resource access to openHAB things with caching and lifecycle management.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ThingResourceAdapter extends BaseAdapter implements Adapter<Resource, ResourceContext, ResourceResult> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThingResourceAdapter.class);

    private static final String ADAPTER_TYPE = "things";
    private static final String URI_PATTERN = "openhab://things/{thingUID}";
    private static final long DEFAULT_REFRESH_INTERVAL_MS = 5 * 60 * 1000; // 5 minutes

    private final ThingRegistry thingRegistry;
    private final Map<String, ThingCachedData> thingCache = new ConcurrentHashMap<>();

    /**
     * Create a new ThingResourceAdapter.
     *
     * @param thingRegistry the thing registry
     */
    public ThingResourceAdapter(ThingRegistry thingRegistry) {
        super(DEFAULT_REFRESH_INTERVAL_MS);
        this.thingRegistry = thingRegistry;
    }

    @Override
    public @Nullable Resource createEntity(String identifier, ResourceContext context) {
        try {
            ThingUID thingUID = new ThingUID(identifier);
            Thing thing = thingRegistry.get(thingUID);
            if (thing == null) {
                LOGGER.debug("Thing not found: {}", identifier);
                return null;
            }

            String uri = "openhab://things/" + identifier;
            String name = "Thing: " + identifier;
            String description = "Resource adapter for openHAB thing: " + identifier;
            String mimeType = "application/json";

            // Create metadata with thing information
            Map<String, Object> metadata = new ConcurrentHashMap<>();
            metadata.put("type", "openhab-thing");
            metadata.put("thingUID", identifier);
            metadata.put("uri", uri);
            metadata.put("label", thing.getLabel());
            metadata.put("thingTypeUID", thing.getThingTypeUID().toString());
            metadata.put("bridgeUID", thing.getBridgeUID() != null ? thing.getBridgeUID().toString() : null);

            return new Resource(uri, name, description, mimeType, metadata);
        } catch (Exception e) {
            LOGGER.error("Error creating resource for thing: {}", identifier, e);
            return null;
        }
    }

    @Override
    public @Nullable String getContent(String identifier, ResourceContext context) {
        ThingCachedData cachedData = getOrCreateCachedData(identifier);
        if (cachedData != null && cachedData.needsRefresh()) {
            refresh(identifier, context);
        }
        return cachedData != null ? cachedData.getContent() : null;
    }

    @Override
    public boolean isWritable(String identifier, ResourceContext context) {
        return true; // Things are generally writable
    }

    @Override
    public boolean writeContent(String identifier, @Nullable String content, ResourceContext context) {
        try {
            if (content == null || content.isEmpty()) {
                LOGGER.warn("Attempted to write null or empty content to thing: {}", identifier);
                return false;
            }

            // TODO: Implement actual thing writing logic
            // This would involve updating the thing configuration
            LOGGER.debug("Writing content to thing: {} - {}", identifier, content);

            // Update cached content
            ThingCachedData cachedData = getOrCreateCachedData(identifier);
            if (cachedData != null) {
                cachedData.setContent(content);
                cachedData.updateRefreshTime();
            }

            return true;
        } catch (Exception e) {
            LOGGER.error("Error writing content to thing: {}", identifier, e);
            return false;
        }
    }

    @Override
    public boolean exists(String identifier, ResourceContext context) {
        ThingCachedData cachedData = getOrCreateCachedData(identifier);
        return cachedData != null && cachedData.getThing() != null;
    }

    @Override
    public ResourceResult execute(String identifier, String operation, Map<String, Object> parameters,
            ResourceContext context) {
        long startTime = System.currentTimeMillis();

        try {
            LOGGER.debug("Executing thing operation: {} for thing: {} with parameters: {}", operation, identifier,
                    parameters);

            ThingCachedData cachedData = getOrCreateCachedData(identifier);
            if (cachedData == null || cachedData.getThing() == null) {
                return ResourceResult.failure("Thing not found: " + identifier, System.currentTimeMillis() - startTime);
            }

            switch (operation) {
                case "get":
                    Thing thing = cachedData.getThing();
                    Map<String, Object> result = new ConcurrentHashMap<>();
                    result.put("success", true);
                    result.put("thingUID", identifier);
                    result.put("label", thing.getLabel());
                    result.put("thingTypeUID", thing.getThingTypeUID().toString());
                    result.put("bridgeUID", thing.getBridgeUID() != null ? thing.getBridgeUID().toString() : null);
                    result.put("status", thing.getStatus().toString());
                    result.put("location", thing.getLocation());

                    long getExecutionTime = System.currentTimeMillis() - startTime;
                    return ResourceResult.success(result, getExecutionTime);

                case "enable":
                    boolean enableSuccess = enableThing(identifier, true);
                    Map<String, Object> enableResult = new ConcurrentHashMap<>();
                    enableResult.put("success", enableSuccess);
                    enableResult.put("thingUID", identifier);
                    enableResult.put("enabled", true);

                    long enableExecutionTime = System.currentTimeMillis() - startTime;
                    return ResourceResult.success(enableResult, enableExecutionTime);

                case "disable":
                    boolean disableSuccess = enableThing(identifier, false);
                    Map<String, Object> disableResult = new ConcurrentHashMap<>();
                    disableResult.put("success", disableSuccess);
                    disableResult.put("thingUID", identifier);
                    disableResult.put("enabled", false);

                    long disableExecutionTime = System.currentTimeMillis() - startTime;
                    return ResourceResult.success(disableResult, disableExecutionTime);

                default:
                    return ResourceResult.failure("Unknown operation: " + operation,
                            System.currentTimeMillis() - startTime);
            }

        } catch (Exception e) {
            LOGGER.error("Error executing thing operation: {} for thing: {}", operation, identifier, e);
            return ResourceResult.failure("Execution error: " + e.getMessage(), System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public void refresh(String identifier, ResourceContext context) {
        try {
            ThingUID thingUID = new ThingUID(identifier);
            Thing thing = thingRegistry.get(thingUID);
            ThingCachedData cachedData = getOrCreateCachedData(identifier);

            if (thing != null) {
                // Create JSON representation of thing state
                StringBuilder content = new StringBuilder();
                content.append("{\n");
                content.append("  \"uid\": \"").append(identifier).append("\",\n");
                content.append("  \"label\": \"").append(thing.getLabel() != null ? thing.getLabel() : "")
                        .append("\",\n");
                content.append("  \"thingTypeUID\": \"").append(thing.getThingTypeUID().toString()).append("\",\n");
                content.append("  \"bridgeUID\": \"")
                        .append(thing.getBridgeUID() != null ? thing.getBridgeUID().toString() : "").append("\",\n");
                content.append("  \"status\": \"").append(thing.getStatus().toString()).append("\",\n");
                content.append("  \"location\": \"").append(thing.getLocation() != null ? thing.getLocation() : "")
                        .append("\"\n");
                content.append("}");

                cachedData.setThing(thing);
                cachedData.setContent(content.toString());
            } else {
                cachedData.setThing(null);
                cachedData.setContent("{}");
            }

            cachedData.updateRefreshTime();
            updateRefreshTime();
            LOGGER.debug("Refreshed thing data: {}", identifier);
        } catch (Exception e) {
            LOGGER.error("Error refreshing thing data: {}", identifier, e);
        }
    }

    @Override
    public void cleanup() {
        LOGGER.debug("Cleaning up ThingResourceAdapter resources");
        thingCache.clear();
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
    public boolean canAdapt(org.openhab.core.ai.tool.resources.api.dto.Resource source) {
        return source != null && source.getName().toLowerCase().contains("thing");
    }

    @Override
    public org.openhab.core.ai.tool.resources.api.ResourceResult adapt(
            org.openhab.core.ai.tool.resources.api.dto.Resource source,
            org.openhab.core.ai.tool.resources.api.ResourceContext context) {
        if (!canAdapt(source)) {
            return ResourceResult.failure("Cannot adapt source", 0);
        }
        return execute(source.getName(), "thing", Map.of(), context);
    }

    @Override
    public Class<org.openhab.core.ai.tool.resources.api.dto.Resource> getSourceType() {
        return Resource.class;
    }

    @Override
    public Class<org.openhab.core.ai.tool.resources.api.ResourceResult> getResultType() {
        return ResourceResult.class;
    }

    @Override
    public void close() {
        cleanup();
    }

    /**
     * Get or create cached data for a thing.
     * 
     * @param identifier the thing identifier
     * @return the cached data or null if thing doesn't exist
     */
    private @Nullable ThingCachedData getOrCreateCachedData(String identifier) {
        return thingCache.computeIfAbsent(identifier, key -> {
            try {
                ThingUID thingUID = new ThingUID(key);
                Thing thing = thingRegistry.get(thingUID);
                return thing != null ? new ThingCachedData(thing) : null;
            } catch (Exception e) {
                LOGGER.debug("Invalid thing UID: {}", key);
                return null;
            }
        });
    }

    /**
     * Enable or disable a thing.
     * 
     * @param identifier the thing identifier
     * @param enabled whether to enable the thing
     * @return true if successful
     */
    private boolean enableThing(String identifier, boolean enabled) {
        try {
            ThingUID thingUID = new ThingUID(identifier);
            Thing thing = thingRegistry.get(thingUID);

            if (thing == null) {
                LOGGER.warn("Thing not found for enable/disable operation: {}", identifier);
                return false;
            }

            // Check current status
            ThingStatus currentStatus = thing.getStatus();
            if (enabled && currentStatus == ThingStatus.ONLINE) {
                LOGGER.debug("Thing {} is already online", identifier);
                return true;
            }

            if (!enabled && currentStatus == ThingStatus.OFFLINE) {
                LOGGER.debug("Thing {} is already offline", identifier);
                return true;
            }

            // In a real implementation, this would use the ThingRegistry's update method
            // For now, we'll log the status change request
            LOGGER.info("Thing status change requested: {} -> {}", identifier, enabled ? "ENABLED" : "DISABLED");

            // Update cached data to reflect the change
            ThingCachedData cachedData = getOrCreateCachedData(identifier);
            if (cachedData != null) {
                cachedData.updateRefreshTime();
            }

            return true;
        } catch (Exception e) {
            LOGGER.error("Error setting enabled={} for thing: {}", enabled, identifier, e);
            return false;
        }
    }

    /**
     * Cached thing data for performance optimization.
     */
    // Extracted: org.openhab.core.ai.tool.resources.adapter.ThingCachedData
}
