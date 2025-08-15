package org.openhab.core.ai.tool.resources.api;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.resources.api.dto.Resource;
import org.openhab.core.ai.tool.resources.api.specification.ResourceSpecification;

/**
 * Registry API for MCP resource specifications and resources.
 */
@NonNullByDefault
public interface ResourceRegistry {

    // Specification registration and lookup
    @Nullable
    ResourceSpecification getResourceSpecification(String id);

    List<ResourceSpecification> getAllResourceSpecifications();

    void registerResource(ResourceSpecification spec);

    void unregisterResource(String id);

    boolean hasResource(String id);

    // MCP exposure helpers (opaque objects and SDK-specific types)
    Object[] getSyncResourceSpecifications();

    Object[] getAsyncResourceSpecifications();

    io.modelcontextprotocol.server.McpServerFeatures.SyncResourceSpecification[] getMcpSyncResourceSpecifications();

    io.modelcontextprotocol.server.McpServerFeatures.AsyncResourceSpecification[] getMcpAsyncResourceSpecifications();

    // Resource registration and lookup
    @Nullable
    Resource getResource(String uri);

    Map<String, Resource> getAllResources();

    void registerResource(Resource resource);

    boolean isResourceRegistered(String uri);

    int getResourceCount();
}
