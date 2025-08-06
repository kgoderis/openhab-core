package org.openhab.core.ai.tool.registry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.api.tool.ResourceRegistry;
import org.openhab.core.ai.tool.dto.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.modelcontextprotocol.server.McpServerFeatures;

/**
 * Implementation of ResourceRegistry for MCP Resources.
 *
 * This class manages the registration and discovery of MCP resources,
 * providing access to resource specifications for the MCP server.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ResourceRegistryImpl implements ResourceRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceRegistryImpl.class);

    /** Map of resources by URI. */
    private final Map<String, Resource> resources = new ConcurrentHashMap<>();

    @Override
    public void registerResource(final Resource resource) {
        String uri = resource.getUri();
        resources.put(uri, resource);
        LOGGER.debug("Registered resource: {}", uri);
    }

    @Override
    public void unregisterResource(final String uri) {
        resources.remove(uri);
        LOGGER.debug("Unregistered resource: {}", uri);
    }

    @Override
    public @Nullable Resource getResource(final String uri) {
        return resources.get(uri);
    }

    @Override
    public Map<String, Resource> getAllResources() {
        return new ConcurrentHashMap<>(resources);
    }

    @Override
    public int getResourceCount() {
        return resources.size();
    }

    @Override
    public boolean isResourceRegistered(final String uri) {
        return resources.containsKey(uri);
    }

    @Override
    public McpServerFeatures.SyncResourceSpecification[] getSyncResourceSpecifications() {
        // TODO: Implement actual MCP resource specification creation
        // For now, return empty array until MCP SDK integration is properly implemented
        LOGGER.debug("Returning empty sync resource specifications (not yet implemented)");
        return new McpServerFeatures.SyncResourceSpecification[0];
    }

    @Override
    public McpServerFeatures.AsyncResourceSpecification[] getAsyncResourceSpecifications() {
        // TODO: Implement actual MCP resource specification creation
        // For now, return empty array until MCP SDK integration is properly implemented
        LOGGER.debug("Returning empty async resource specifications (not yet implemented)");
        return new McpServerFeatures.AsyncResourceSpecification[0];
    }
}
