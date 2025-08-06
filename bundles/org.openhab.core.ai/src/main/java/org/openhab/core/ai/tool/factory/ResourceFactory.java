package org.openhab.core.ai.tool.factory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.dto.AbstractResource;
import org.openhab.core.ai.tool.dto.Resource;
import org.openhab.core.ai.tool.proxy.ConfigurationResourceProxy;
import org.openhab.core.ai.tool.proxy.ItemResourceProxy;
import org.openhab.core.ai.tool.proxy.RuleResourceProxy;
import org.openhab.core.ai.tool.proxy.ThingResourceProxy;
import org.openhab.core.automation.RuleRegistry;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.thing.ThingRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for creating and managing MCP Resources with lifecycle management.
 * 
 * This factory provides centralized resource creation, caching, and cleanup
 * for the MCP resource system.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ResourceFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceFactory.class);

    /** Map of active resources by URI. */
    private final Map<String, AbstractResource> activeResources = new ConcurrentHashMap<>();

    /** Default refresh interval in milliseconds (5 minutes). */
    private static final long DEFAULT_REFRESH_INTERVAL_MS = 5 * 60 * 1000;

    /**
     * Create an AbstractResource from a Resource DTO.
     * This method attempts to resolve the resource based on its URI.
     *
     * @param resource the resource DTO
     * @param context the context containing required registries
     * @return the AbstractResource or null if resolution fails
     */
    public @Nullable AbstractResource fromResource(Resource resource, Map<String, Object> context) {
        String uri = resource.getUri();

        try {
            if (uri.startsWith("openhab://items/")) {
                String itemName = uri.substring("openhab://items/".length());
                ItemRegistry itemRegistry = (ItemRegistry) context.get("itemRegistry");
                if (itemRegistry != null) {
                    return createResource(uri, (resourceUri, refreshIntervalMs) -> new ItemResourceProxy(itemRegistry,
                            itemName, refreshIntervalMs));
                }
            } else if (uri.startsWith("openhab://things/")) {
                String thingUID = uri.substring("openhab://things/".length());
                ThingRegistry thingRegistry = (ThingRegistry) context.get("thingRegistry");
                if (thingRegistry != null) {
                    return createResource(uri, (resourceUri, refreshIntervalMs) -> new ThingResourceProxy(thingRegistry,
                            thingUID, refreshIntervalMs));
                }
            } else if (uri.startsWith("openhab://rules/")) {
                String ruleUID = uri.substring("openhab://rules/".length());
                RuleRegistry ruleRegistry = (RuleRegistry) context.get("ruleRegistry");
                if (ruleRegistry != null) {
                    return createResource(uri, (resourceUri, refreshIntervalMs) -> new RuleResourceProxy(ruleRegistry,
                            ruleUID, refreshIntervalMs));
                }
            } else if (uri.startsWith("openhab://config/")) {
                String configId = uri.substring("openhab://config/".length());
                // For configuration resources, we need a Configuration object from context
                Configuration configuration = (Configuration) context.get("configuration");
                if (configuration != null) {
                    return createResource(uri,
                            (resourceUri, refreshIntervalMs) -> new ConfigurationResourceProxy(configId, configuration,
                                    refreshIntervalMs));
                } else {
                    LOGGER.warn("Configuration object not found in context for: {}", uri);
                }
            }

            LOGGER.warn("Unknown URI scheme for resource: {}", uri);

        } catch (Exception e) {
            LOGGER.error("Error creating AbstractResource from Resource DTO: {}", uri, e);
        }

        return null;
    }

    /**
     * Create an AbstractResource from a Resource DTO with default refresh interval.
     *
     * @param resource the resource DTO
     * @param context the context containing required registries
     * @return the AbstractResource or null if resolution fails
     */
    public @Nullable AbstractResource fromResource(Resource resource, Map<String, Object> context,
            long refreshIntervalMs) {
        String uri = resource.getUri();

        try {
            if (uri.startsWith("openhab://items/")) {
                String itemName = uri.substring("openhab://items/".length());
                ItemRegistry itemRegistry = (ItemRegistry) context.get("itemRegistry");
                if (itemRegistry != null) {
                    return createResource(uri,
                            (resourceUri, interval) -> new ItemResourceProxy(itemRegistry, itemName, refreshIntervalMs),
                            refreshIntervalMs);
                }
            } else if (uri.startsWith("openhab://things/")) {
                String thingUID = uri.substring("openhab://things/".length());
                ThingRegistry thingRegistry = (ThingRegistry) context.get("thingRegistry");
                if (thingRegistry != null) {
                    return createResource(uri, (resourceUri, interval) -> new ThingResourceProxy(thingRegistry,
                            thingUID, refreshIntervalMs), refreshIntervalMs);
                }
            } else if (uri.startsWith("openhab://rules/")) {
                String ruleUID = uri.substring("openhab://rules/".length());
                RuleRegistry ruleRegistry = (RuleRegistry) context.get("ruleRegistry");
                if (ruleRegistry != null) {
                    return createResource(uri,
                            (resourceUri, interval) -> new RuleResourceProxy(ruleRegistry, ruleUID, refreshIntervalMs),
                            refreshIntervalMs);
                }
            } else if (uri.startsWith("openhab://config/")) {
                String configId = uri.substring("openhab://config/".length());
                // For configuration resources, we need a Configuration object from context
                Configuration configuration = (Configuration) context.get("configuration");
                if (configuration != null) {
                    return createResource(uri, (resourceUri, interval) -> new ConfigurationResourceProxy(configId,
                            configuration, refreshIntervalMs), refreshIntervalMs);
                } else {
                    LOGGER.warn("Configuration object not found in context for: {}", uri);
                }
            }

            LOGGER.warn("Unknown URI scheme for resource: {}", uri);

        } catch (Exception e) {
            LOGGER.error("Error creating AbstractResource from Resource DTO: {}", uri, e);
        }

        return null;
    }

    /**
     * Create a resource using the provided factory method.
     * 
     * @param uri the resource URI
     * @param factory the factory method to create the resource
     * @return the created resource or existing valid resource
     */
    public @Nullable AbstractResource createResource(String uri, ResourceFactoryMethod factory) {
        return createResource(uri, factory, DEFAULT_REFRESH_INTERVAL_MS);
    }

    /**
     * Create a resource using the provided factory method with custom refresh interval.
     * 
     * @param uri the resource URI
     * @param factory the factory method to create the resource
     * @param refreshIntervalMs the refresh interval in milliseconds
     * @return the created resource or existing valid resource
     */
    public @Nullable AbstractResource createResource(String uri, ResourceFactoryMethod factory,
            long refreshIntervalMs) {
        // Check if we already have a valid resource
        AbstractResource existing = activeResources.get(uri);
        if (existing != null && existing.isValid() && !existing.needsRefresh()) {
            LOGGER.debug("Returning existing valid resource: {}", uri);
            return existing;
        }

        // Create new resource
        AbstractResource resource = factory.create(uri, refreshIntervalMs);
        if (resource != null) {
            activeResources.put(uri, resource);
            LOGGER.debug("Created new resource: {}", uri);
        } else {
            LOGGER.warn("Failed to create resource: {}", uri);
        }

        return resource;
    }

    /**
     * Get an existing resource by URI.
     * 
     * @param uri the resource URI
     * @return the resource or null if not found
     */
    public @Nullable AbstractResource getResource(String uri) {
        return activeResources.get(uri);
    }

    /**
     * Check if a resource exists and is valid.
     * 
     * @param uri the resource URI
     * @return true if the resource exists and is valid
     */
    public boolean hasValidResource(String uri) {
        AbstractResource resource = activeResources.get(uri);
        return resource != null && resource.isValid();
    }

    /**
     * Refresh a specific resource.
     * 
     * @param uri the resource URI
     * @return true if the resource was refreshed successfully
     */
    public boolean refreshResource(String uri) {
        AbstractResource resource = activeResources.get(uri);
        if (resource != null) {
            try {
                resource.refresh();
                LOGGER.debug("Refreshed resource: {}", uri);
                return true;
            } catch (Exception e) {
                LOGGER.error("Error refreshing resource: {}", uri, e);
                resource.markInvalid();
            }
        }
        return false;
    }

    /**
     * Refresh all resources that need refreshing.
     * 
     * @return the number of resources refreshed
     */
    public int refreshAllResources() {
        int refreshed = 0;
        for (Map.Entry<String, AbstractResource> entry : activeResources.entrySet()) {
            String uri = entry.getKey();
            AbstractResource resource = entry.getValue();

            if (resource.needsRefresh()) {
                if (refreshResource(uri)) {
                    refreshed++;
                }
            }
        }
        LOGGER.debug("Refreshed {} resources", refreshed);
        return refreshed;
    }

    /**
     * Remove a resource from the factory.
     * 
     * @param uri the resource URI
     * @return true if the resource was removed
     */
    public boolean removeResource(String uri) {
        AbstractResource resource = activeResources.remove(uri);
        if (resource != null) {
            try {
                resource.close();
                LOGGER.debug("Removed and closed resource: {}", uri);
                return true;
            } catch (Exception e) {
                LOGGER.error("Error closing resource: {}", uri, e);
            }
        }
        return false;
    }

    /**
     * Clean up all resources.
     */
    public void cleanup() {
        LOGGER.info("Cleaning up {} resources", activeResources.size());

        for (Map.Entry<String, AbstractResource> entry : activeResources.entrySet()) {
            String uri = entry.getKey();
            AbstractResource resource = entry.getValue();

            try {
                resource.close();
                LOGGER.debug("Closed resource: {}", uri);
            } catch (Exception e) {
                LOGGER.error("Error closing resource: {}", uri, e);
            }
        }

        activeResources.clear();
        LOGGER.info("Resource cleanup completed");
    }

    /**
     * Get the number of active resources.
     * 
     * @return the number of active resources
     */
    public int getResourceCount() {
        return activeResources.size();
    }

    /**
     * Get all active resources.
     * 
     * @return a copy of all active resources
     */
    public Map<String, AbstractResource> getAllResources() {
        return new ConcurrentHashMap<>(activeResources);
    }

    /**
     * Functional interface for resource creation.
     */
    @FunctionalInterface
    public interface ResourceFactoryMethod {
        /**
         * Create a resource.
         * 
         * @param uri the resource URI
         * @param refreshIntervalMs the refresh interval in milliseconds
         * @return the created resource or null if creation failed
         */
        @Nullable
        AbstractResource create(String uri, long refreshIntervalMs);
    }
}
