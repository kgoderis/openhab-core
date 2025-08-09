package org.openhab.core.ai.tool.services;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.api.ResourceContext;
import org.openhab.core.ai.tool.api.ResourceResult;
import org.openhab.core.ai.tool.resources.specification.ResourceSpecification;

/**
 * Service interface for resource operations.
 * 
 * Provides high-level resource management and access capabilities.
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public interface ResourceService {

    /**
     * Register a resource.
     * 
     * @param specification the resource specification
     * @return true if registration was successful
     */
    boolean registerResource(ResourceSpecification specification);

    /**
     * Unregister a resource.
     * 
     * @param resourceId the resource ID
     * @return true if unregistration was successful
     */
    boolean unregisterResource(String resourceId);

    /**
     * Get a resource by ID.
     * 
     * @param resourceId the resource ID
     * @return the resource specification or null if not found
     */
    @Nullable
    ResourceSpecification getResource(String resourceId);

    /**
     * Get all registered resources.
     * 
     * @return map of resource ID to resource specification
     */
    Map<String, ResourceSpecification> getAllResources();

    /**
     * Access a resource.
     * 
     * @param resourceId the resource ID
     * @param context the resource context
     * @return the access result
     */
    ResourceResult accessResource(String resourceId, ResourceContext context);

    /**
     * Validate a resource.
     * 
     * @param resourceId the resource ID
     * @return true if the resource is valid
     */
    boolean validateResource(String resourceId);

    /**
     * Get resource statistics.
     * 
     * @return map of resource statistics
     */
    Map<String, Object> getResourceStatistics();

    /**
     * Get service health status.
     * 
     * @return true if the service is healthy
     */
    boolean isHealthy();

    /**
     * Get service version.
     * 
     * @return the service version
     */
    String getVersion();
}
