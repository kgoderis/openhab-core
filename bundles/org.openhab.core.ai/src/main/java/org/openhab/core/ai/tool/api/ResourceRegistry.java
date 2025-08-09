package org.openhab.core.ai.tool.api;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.resources.dto.Resource;
import org.openhab.core.ai.tool.resources.specification.ResourceSpecification;

import io.modelcontextprotocol.server.McpServerFeatures;

/**
 * Resource Registry Interface for MCP Resources
 * 
 * This interface defines the contract for managing MCP resource specifications
 * including Items, Things, Rules, and Configuration resources.
 * 
 * This is the consolidated ResourceRegistry interface that combines functionality
 * from both the api.tool and resource.api packages.
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public interface ResourceRegistry {

    /**
     * Get all synchronous resource specifications
     * 
     * @return Array of sync resource specifications
     */
    Object[] getSyncResourceSpecifications();

    /**
     * Get all asynchronous resource specifications
     * 
     * @return Array of async resource specifications
     */
    Object[] getAsyncResourceSpecifications();

    /**
     * Get MCP sync resource specifications
     * 
     * @return MCP sync resource specifications
     */
    McpServerFeatures.SyncResourceSpecification[] getMcpSyncResourceSpecifications();

    /**
     * Get MCP async resource specifications
     * 
     * @return MCP async resource specifications
     */
    McpServerFeatures.AsyncResourceSpecification[] getMcpAsyncResourceSpecifications();

    /**
     * Get a specific resource specification by ID
     * 
     * @param resourceId The resource ID
     * @return The resource specification or null if not found
     */
    @Nullable
    ResourceSpecification getResourceSpecification(String resourceId);

    /**
     * Get a specific resource by URI
     * 
     * @param uri The resource URI
     * @return The resource or null if not found
     */
    @Nullable
    Resource getResource(String uri);

    /**
     * Get all registered resource specifications
     * 
     * @return List of all resource specifications
     */
    List<ResourceSpecification> getAllResourceSpecifications();

    /**
     * Get all registered resources
     * 
     * @return Map of all registered resources by URI
     */
    Map<String, Resource> getAllResources();

    /**
     * Register a new resource specification
     * 
     * @param resource The resource specification to register
     */
    void registerResource(ResourceSpecification resource);

    /**
     * Register a new resource
     * 
     * @param resource The resource to register
     */
    void registerResource(Resource resource);

    /**
     * Unregister a resource specification by ID
     * 
     * @param resourceId The resource ID to unregister
     */
    void unregisterResource(String resourceId);

    /**
     * Check if a resource specification exists
     * 
     * @param resourceId The resource ID to check
     * @return true if the resource exists, false otherwise
     */
    boolean hasResource(String resourceId);

    /**
     * Check if a resource is registered
     * 
     * @param uri The resource URI
     * @return true if the resource is registered
     */
    boolean isResourceRegistered(String uri);

    /**
     * Get the total number of registered resources
     * 
     * @return The number of resources
     */
    int getResourceCount();
}
