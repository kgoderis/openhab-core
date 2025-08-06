package org.openhab.core.ai.api.tool;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.dto.Resource;

import io.modelcontextprotocol.server.McpServerFeatures;

/**
 * Registry for MCP Resources.
 *
 * This interface manages the registration and discovery of MCP resources,
 * providing access to resource specifications for the MCP server.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface ResourceRegistry {

    /**
     * Register a resource.
     *
     * @param resource the resource to register
     */
    void registerResource(Resource resource);

    /**
     * Unregister a resource.
     *
     * @param uri the resource URI to unregister
     */
    void unregisterResource(String uri);

    /**
     * Get a resource by URI.
     *
     * @param uri the resource URI
     * @return the resource or null if not found
     */
    @Nullable
    Resource getResource(String uri);

    /**
     * Get all resources.
     *
     * @return all registered resources
     */
    Map<String, Resource> getAllResources();

    /**
     * Get the number of registered resources.
     *
     * @return the number of resources
     */
    int getResourceCount();

    /**
     * Check if a resource is registered.
     *
     * @param uri the resource URI
     * @return true if the resource is registered
     */
    boolean isResourceRegistered(String uri);

    /**
     * Get sync resource specifications.
     *
     * @return sync resource specifications
     */
    McpServerFeatures.SyncResourceSpecification[] getSyncResourceSpecifications();

    /**
     * Get async resource specifications.
     *
     * @return async resource specifications
     */
    McpServerFeatures.AsyncResourceSpecification[] getAsyncResourceSpecifications();
}
