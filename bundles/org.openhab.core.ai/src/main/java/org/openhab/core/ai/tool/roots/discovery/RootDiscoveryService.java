package org.openhab.core.ai.tool.roots.discovery;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.tool.resources.specification.ResourceSpecification;

/**
 * Service for discovering MCP roots.
 * 
 * This interface defines the contract for root discovery services that can
 * discover and manage MCP root resources.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface RootDiscoveryService {

    /**
     * Get the service ID.
     * 
     * @return the service ID
     */
    String getServiceId();

    /**
     * Get the service name.
     * 
     * @return the service name
     */
    String getServiceName();

    /**
     * Get the service description.
     * 
     * @return the service description
     */
    String getServiceDescription();

    /**
     * Discover available roots.
     * 
     * @return list of discovered root specifications
     */
    List<ResourceSpecification> discoverRoots();

    /**
     * Discover roots with the given criteria.
     * 
     * @param criteria the discovery criteria
     * @return list of discovered root specifications
     */
    List<ResourceSpecification> discoverRoots(Map<String, Object> criteria);

    /**
     * Get a specific root by ID.
     * 
     * @param rootId the root ID
     * @return the root specification or null if not found
     */
    ResourceSpecification getRoot(String rootId);

    /**
     * Register a new root.
     * 
     * @param root the root to register
     */
    void registerRoot(ResourceSpecification root);

    /**
     * Unregister a root.
     * 
     * @param rootId the root ID to unregister
     */
    void unregisterRoot(String rootId);

    /**
     * Get the service configuration.
     * 
     * @return the service configuration
     */
    Map<String, Object> getConfiguration();

    /**
     * Update the service configuration.
     * 
     * @param configuration the new configuration
     */
    void updateConfiguration(Map<String, Object> configuration);

    // TODO: Implement root discovery logic
    // TODO: Add support for dynamic root discovery
    // TODO: Implement root discovery caching
    // TODO: Add support for root discovery filtering
}
