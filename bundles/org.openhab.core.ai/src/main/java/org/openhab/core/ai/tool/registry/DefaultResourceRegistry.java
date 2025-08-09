package org.openhab.core.ai.tool.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.adapter.ResourceAdapter;
import org.openhab.core.ai.tool.api.ResourceRegistry;
import org.openhab.core.ai.tool.resources.ResourceRegistrationService;
import org.openhab.core.ai.tool.resources.dto.Resource;
import org.openhab.core.ai.tool.resources.specification.ConfigurationResourceSpecification;
import org.openhab.core.ai.tool.resources.specification.ItemResourceSpecification;
import org.openhab.core.ai.tool.resources.specification.ResourceSpecification;
import org.openhab.core.ai.tool.resources.specification.RuleResourceSpecification;
import org.openhab.core.ai.tool.resources.specification.ThingResourceSpecification;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.modelcontextprotocol.server.McpServerFeatures;

/**
 * Enhanced Resource Registry Implementation for MCP Resources
 * 
 * This class provides comprehensive MCP resource specifications for openHAB entities
 * including Items, Things, Rules, and Configuration with proper URI patterns and MIME types.
 * 
 * Features:
 * - ResourceSpecification management for MCP protocol compliance
 * - Resource class management for direct resource access
 * - Security filtering and access control
 * - Performance monitoring and metrics
 * - OSGi service integration
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
@Component(service = ResourceRegistry.class, immediate = true)
public class DefaultResourceRegistry implements ResourceRegistry {

    private static final Logger logger = LoggerFactory.getLogger(DefaultResourceRegistry.class);

    // ResourceSpecification management
    private final Map<String, ResourceSpecification> resourceSpecifications = new ConcurrentHashMap<>();
    private final List<Object> syncSpecifications = new ArrayList<>();
    private final List<Object> asyncSpecifications = new ArrayList<>();

    // Resource class management
    private final Map<String, Resource> resources = new ConcurrentHashMap<>();

    // Security filtering - resources that should be excluded
    private final Map<String, Boolean> securityFilters = new ConcurrentHashMap<>();

    // Performance monitoring
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong successfulRequests = new AtomicLong(0);
    private final AtomicLong failedRequests = new AtomicLong(0);
    private final AtomicLong totalResponseTimeMs = new AtomicLong(0);

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    private volatile @Nullable ResourceRegistrationService registrationService;

    @Activate
    protected void activate() {
        logger.debug("Activating ResourceRegistryImpl");
        initializeResourceSpecifications();
    }

    @Deactivate
    protected void deactivate() {
        logger.debug("Deactivating ResourceRegistryImpl");
        resourceSpecifications.clear();
        resources.clear();
        syncSpecifications.clear();
        asyncSpecifications.clear();
        securityFilters.clear();
    }

    /**
     * Initialize all resource specifications for openHAB entities
     */
    private void initializeResourceSpecifications() {
        try {
            // Create Item Resource Specification
            ItemResourceSpecification itemSpec = new ItemResourceSpecification();
            resourceSpecifications.put("items", itemSpec);
            syncSpecifications.add(ResourceAdapter.createSyncResourceSpecification(itemSpec));
            asyncSpecifications.add(ResourceAdapter.createAsyncResourceSpecification(itemSpec));

            // Create Thing Resource Specification
            ThingResourceSpecification thingSpec = new ThingResourceSpecification();
            resourceSpecifications.put("things", thingSpec);
            syncSpecifications.add(ResourceAdapter.createSyncResourceSpecification(thingSpec));
            asyncSpecifications.add(ResourceAdapter.createAsyncResourceSpecification(thingSpec));

            // Create Rule Resource Specification
            RuleResourceSpecification ruleSpec = new RuleResourceSpecification();
            resourceSpecifications.put("rules", ruleSpec);
            syncSpecifications.add(ResourceAdapter.createSyncResourceSpecification(ruleSpec));
            asyncSpecifications.add(ResourceAdapter.createAsyncResourceSpecification(ruleSpec));

            // Create Configuration Resource Specification
            ConfigurationResourceSpecification configSpec = new ConfigurationResourceSpecification();
            resourceSpecifications.put("configuration", configSpec);
            syncSpecifications.add(ResourceAdapter.createSyncResourceSpecification(configSpec));
            asyncSpecifications.add(ResourceAdapter.createAsyncResourceSpecification(configSpec));

            logger.info("Initialized {} resource specifications", resourceSpecifications.size());
        } catch (Exception e) {
            logger.error("Failed to initialize resource specifications", e);
        }
    }

    // ResourceSpecification methods
    @Override
    public Object[] getSyncResourceSpecifications() {
        return syncSpecifications.toArray(new Object[0]);
    }

    @Override
    public Object[] getAsyncResourceSpecifications() {
        return asyncSpecifications.toArray(new Object[0]);
    }

    @Override
    public McpServerFeatures.SyncResourceSpecification[] getMcpSyncResourceSpecifications() {
        try {
            logger.debug("Creating MCP sync resource specifications for {} resources", resources.size());
            // TODO: Implement actual MCP resource specification creation
            // For now, return empty array until MCP SDK integration is properly implemented
            logger.debug("Returning empty MCP sync resource specifications (MCP SDK integration pending)");
            return new McpServerFeatures.SyncResourceSpecification[0];
        } catch (Exception e) {
            logger.error("Error creating MCP sync resource specifications", e);
            return new McpServerFeatures.SyncResourceSpecification[0];
        }
    }

    @Override
    public McpServerFeatures.AsyncResourceSpecification[] getMcpAsyncResourceSpecifications() {
        try {
            logger.debug("Creating MCP async resource specifications for {} resources", resources.size());
            // TODO: Implement actual MCP resource specification creation
            // For now, return empty array until MCP SDK integration is properly implemented
            logger.debug("Returning empty MCP async resource specifications (MCP SDK integration pending)");
            return new McpServerFeatures.AsyncResourceSpecification[0];
        } catch (Exception e) {
            logger.error("Error creating MCP async resource specifications", e);
            return new McpServerFeatures.AsyncResourceSpecification[0];
        }
    }

    @Override
    public @Nullable ResourceSpecification getResourceSpecification(String resourceId) {
        return resourceSpecifications.get(resourceId);
    }

    @Override
    public List<ResourceSpecification> getAllResourceSpecifications() {
        return new ArrayList<>(resourceSpecifications.values());
    }

    @Override
    public void registerResource(ResourceSpecification resource) {
        if (resource != null && resource.getId() != null) {
            // Apply security filtering
            if (isResourceBlocked(resource.getId())) {
                logger.warn("Resource specification registration blocked by security filter: {}", resource.getId());
                return;
            }

            resourceSpecifications.put(resource.getId(), resource);
            syncSpecifications.add(ResourceAdapter.createSyncResourceSpecification(resource));
            logger.debug("Registered resource specification: {}", resource.getId());
        }
    }

    @Override
    public void unregisterResource(String resourceId) {
        ResourceSpecification removed = resourceSpecifications.remove(resourceId);
        if (removed != null) {
            // Remove any spec entries matching the removed id from both sync/async lists
            syncSpecifications.removeIf(spec -> (spec instanceof Map<?, ?> m) && resourceId.equals(m.get("id")));
            asyncSpecifications.removeIf(spec -> (spec instanceof Map<?, ?> m) && resourceId.equals(m.get("id")));
            logger.debug("Unregistered resource specification: {}", resourceId);
        }
    }

    @Override
    public boolean hasResource(String resourceId) {
        return resourceSpecifications.containsKey(resourceId) && !isResourceBlocked(resourceId);
    }

    // Resource class methods
    @Override
    public @Nullable Resource getResource(String uri) {
        totalRequests.incrementAndGet();
        long startTime = System.currentTimeMillis();

        try {
            // Apply security filtering
            if (isResourceBlocked(uri)) {
                logger.warn("Resource access blocked by security filter: {}", uri);
                failedRequests.incrementAndGet();
                return null;
            }

            Resource resource = resources.get(uri);
            if (resource != null) {
                successfulRequests.incrementAndGet();
            } else {
                failedRequests.incrementAndGet();
            }

            return resource;
        } finally {
            long responseTime = System.currentTimeMillis() - startTime;
            totalResponseTimeMs.addAndGet(responseTime);
        }
    }

    @Override
    public Map<String, Resource> getAllResources() {
        return resources.entrySet().stream().filter(entry -> !isResourceBlocked(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public void registerResource(Resource resource) {
        String uri = resource.getUri();

        // Apply security filtering
        if (isResourceBlocked(uri)) {
            logger.warn("Resource registration blocked by security filter: {}", uri);
            return;
        }

        resources.put(uri, resource);
        logger.debug("Registered resource: {}", uri);
    }

    @Override
    public boolean isResourceRegistered(String uri) {
        return resources.containsKey(uri) && !isResourceBlocked(uri);
    }

    @Override
    public int getResourceCount() {
        return (int) resources.entrySet().stream().filter(entry -> !isResourceBlocked(entry.getKey())).count();
    }

    /**
     * Check if a resource is blocked by security filters.
     *
     * @param uri the resource URI
     * @return true if the resource is blocked
     */
    private boolean isResourceBlocked(String uri) {
        return securityFilters.containsKey(uri) && securityFilters.get(uri);
    }

    /**
     * Add a security filter to block a resource.
     *
     * @param uri the resource URI to block
     */
    public void blockResource(String uri) {
        securityFilters.put(uri, true);
        logger.info("Resource blocked by security filter: {}", uri);
    }

    /**
     * Remove a security filter for a resource.
     *
     * @param uri the resource URI to unblock
     */
    public void unblockResource(String uri) {
        securityFilters.remove(uri);
        logger.info("Resource unblocked: {}", uri);
    }

    /**
     * Get performance metrics.
     *
     * @return performance metrics as a map
     */
    public Map<String, Object> getPerformanceMetrics() {
        Map<String, Object> metrics = new ConcurrentHashMap<>();
        metrics.put("totalRequests", totalRequests.get());
        metrics.put("successfulRequests", successfulRequests.get());
        metrics.put("failedRequests", failedRequests.get());
        metrics.put("totalResponseTimeMs", totalResponseTimeMs.get());
        metrics.put("averageResponseTimeMs",
                totalRequests.get() > 0 ? totalResponseTimeMs.get() / totalRequests.get() : 0);
        metrics.put("successRate",
                totalRequests.get() > 0 ? (double) successfulRequests.get() / totalRequests.get() : 0.0);
        metrics.put("resourceSpecificationCount", resourceSpecifications.size());
        metrics.put("resourceCount", resources.size());
        return metrics;
    }
}
