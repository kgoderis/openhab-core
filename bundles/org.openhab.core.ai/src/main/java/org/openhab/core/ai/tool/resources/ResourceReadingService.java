package org.openhab.core.ai.tool.resources;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.patterns.ConfigurationOperationMetrics;
import org.openhab.core.ai.tool.resources.api.ResourceContext;
import org.openhab.core.ai.tool.resources.api.ResourceRegistry;
import org.openhab.core.ai.tool.resources.api.ResourceResult;
import org.openhab.core.ai.tool.resources.api.specification.ResourceSpecification;
import org.openhab.core.ai.tool.resources.api.validation.ResourceValidationResult;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resource Reading Service for MCP Resources
 * 
 * This service implements the resources/read method for resource content retrieval
 * with caching, validation, security, and performance monitoring.
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
@Component(immediate = true)
public class ResourceReadingService {

    private static final Logger logger = LoggerFactory.getLogger(ResourceReadingService.class);

    private final Map<String, CachedResourceContent> contentCache = new ConcurrentHashMap<>();
    // Performance monitoring - migrated to MetricsService
    // private final AtomicLong totalReads = new AtomicLong(0);
    // private final AtomicLong cacheHits = new AtomicLong(0);
    // private final AtomicLong cacheMisses = new AtomicLong(0);
    // private final AtomicLong totalReadTime = new AtomicLong(0);

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    private volatile @Nullable ResourceRegistry resourceRegistry;

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    private volatile @Nullable MetricsService metricsService;

    @Activate
    protected void activate() {
        logger.debug("Activating ResourceReadingService");
    }

    @Deactivate
    protected void deactivate() {
        logger.debug("Deactivating ResourceReadingService");
        contentCache.clear();
    }

    /**
     * Read resource content with caching and performance monitoring
     * 
     * @param resourceId The resource ID to read
     * @param parameters The parameters for the read operation
     * @param context The execution context
     * @return The resource content result
     */
    public ResourceResult readResource(String resourceId, Map<String, Object> parameters, ResourceContext context) {
        long startTime = System.currentTimeMillis();
        // totalReads.incrementAndGet(); // Migrated to MetricsService
        MetricsService metrics = metricsService;

        try {
            logger.debug("Reading resource: {} with parameters: {}", resourceId, parameters);

            // Validate input parameters
            if (resourceId == null || resourceId.trim().isEmpty()) {
                return ResourceResult.failure("Resource ID cannot be null or empty",
                        System.currentTimeMillis() - startTime);
            }

            // Check cache first
            String cacheKey = generateCacheKey(resourceId, parameters);
            CachedResourceContent cachedContent = contentCache.get(cacheKey);

            if (cachedContent != null && !cachedContent.isExpired()) {
                // cacheHits.incrementAndGet(); // Migrated to MetricsService
                logger.debug("Cache hit for resource: {}", resourceId);

                // Record cache hit metrics
                if (metrics != null) {
                    // Estimate content size based on content type
                    long estimatedSize = estimateContentSize(cachedContent.getContent());
                    ConfigurationOperationMetrics.recordCacheOperation(metrics, "resource-cache", "hit", cacheKey,
                            estimatedSize, java.time.Duration.ofMillis(System.currentTimeMillis() - startTime));
                }

                return ResourceResult.success(cachedContent.getContent(), System.currentTimeMillis() - startTime);
            }

            // cacheMisses.incrementAndGet(); // Migrated to MetricsService

            // Record cache miss metrics
            if (metrics != null) {
                ConfigurationOperationMetrics.recordCacheOperation(metrics, "resource-cache", "miss", cacheKey,
                        0L, java.time.Duration.ofMillis(System.currentTimeMillis() - startTime));
            }

            // Get resource specification
            ResourceRegistry registry = resourceRegistry;
            if (registry == null) {
                return ResourceResult.failure("Resource registry not available",
                        System.currentTimeMillis() - startTime);
            }

            ResourceSpecification resource = registry.getResourceSpecification(resourceId);
            if (resource == null) {
                return ResourceResult.failure("Resource not found: " + resourceId,
                        System.currentTimeMillis() - startTime);
            }

            // Validate parameters
            ResourceValidationResult validation = resource.validateParameters(parameters);
            if (!validation.isValid()) {
                return ResourceResult.failure("Parameter validation failed: " + validation.getMessage(),
                        System.currentTimeMillis() - startTime);
            }

            // Check access control
            if (!hasAccess(resourceId, context)) {
                return ResourceResult.failure("Access denied for resource: " + resourceId,
                        System.currentTimeMillis() - startTime);
            }

            // Execute resource
            ResourceResult result = resource.execute(parameters, context);

            // Cache successful results
            if (result.isSuccess() && result.getContent() != null) {
                CachedResourceContent newCachedContent = new CachedResourceContent(result.getContent(),
                        System.currentTimeMillis() + getCacheExpirationTime());
                contentCache.put(cacheKey, newCachedContent);
                logger.debug("Cached content for resource: {}", resourceId);
            }

            long executionTime = System.currentTimeMillis() - startTime;
            // totalReadTime.addAndGet(executionTime); // Migrated to MetricsService

            // Record successful resource read metrics
            if (metrics != null) {
                // Estimate content size based on content type
                long estimatedSize = estimateContentSize(result.getContent());
                ConfigurationOperationMetrics.recordFileOperation(metrics, "read", resourceId,
                        estimatedSize, java.time.Duration.ofMillis(executionTime), result.isSuccess());
            }

            logger.debug("Resource read completed in {}ms", executionTime);
            return result;

        } catch (Exception e) {
            logger.error("Error reading resource: {}", resourceId, e);

            // Record failed resource read metrics
            if (metrics != null) {
                ConfigurationOperationMetrics.recordFileOperation(metrics, "read", resourceId,
                        0L, java.time.Duration.ofMillis(System.currentTimeMillis() - startTime), false);
            }

            return ResourceResult.failure("Read error: " + e.getMessage(), System.currentTimeMillis() - startTime);
        }
    }

    /**
     * Subscribe to resource changes
     * 
     * @param resourceId The resource ID to subscribe to
     * @param parameters The subscription parameters
     * @param context The execution context
     * @return The subscription result
     */
    public ResourceResult subscribeToResource(String resourceId, Map<String, Object> parameters,
            ResourceContext context) {
        long startTime = System.currentTimeMillis();

        try {
            logger.debug("Subscribing to resource: {} with parameters: {}", resourceId, parameters);

            // Validate input parameters
            if (resourceId == null || resourceId.trim().isEmpty()) {
                return ResourceResult.failure("Resource ID cannot be null or empty",
                        System.currentTimeMillis() - startTime);
            }

            // Get resource specification
            ResourceRegistry registry = resourceRegistry;
            if (registry == null) {
                return ResourceResult.failure("Resource registry not available",
                        System.currentTimeMillis() - startTime);
            }

            ResourceSpecification resource = registry.getResourceSpecification(resourceId);
            if (resource == null) {
                return ResourceResult.failure("Resource not found: " + resourceId,
                        System.currentTimeMillis() - startTime);
            }

            // Check access control
            if (!hasAccess(resourceId, context)) {
                return ResourceResult.failure("Access denied for resource: " + resourceId,
                        System.currentTimeMillis() - startTime);
            }

            // TODO: Implement actual subscription mechanism
            // For now, return a mock subscription result
            Map<String, Object> subscriptionResult = new HashMap<>();
            subscriptionResult.put("subscriptionId", "sub-" + System.currentTimeMillis());
            subscriptionResult.put("resourceId", resourceId);
            subscriptionResult.put("status", "active");
            subscriptionResult.put("expiresAt", System.currentTimeMillis() + 3600000); // 1 hour

            long executionTime = System.currentTimeMillis() - startTime;
            logger.debug("Resource subscription completed in {}ms", executionTime);

            return ResourceResult.success(subscriptionResult, executionTime);

        } catch (Exception e) {
            logger.error("Error subscribing to resource: {}", resourceId, e);
            return ResourceResult.failure("Subscription error: " + e.getMessage(),
                    System.currentTimeMillis() - startTime);
        }
    }

    /**
     * Get performance metrics
     * 
     * @return Performance metrics map
     */
    public Map<String, Object> getPerformanceMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        // Performance metrics migrated to MetricsService
        metrics.put("totalReads", 0);
        metrics.put("cacheHits", 0);
        metrics.put("cacheMisses", 0);
        metrics.put("cacheHitRate", 0.0);
        metrics.put("averageReadTime", 0.0);
        metrics.put("cacheSize", contentCache.size());
        return metrics;
    }

    /**
     * Clear the content cache
     */
    public void clearCache() {
        logger.debug("Clearing resource content cache");
        contentCache.clear();
    }

    /**
     * Generate cache key for resource and parameters
     * 
     * @param resourceId The resource ID
     * @param parameters The parameters
     * @return The cache key
     */
    private String generateCacheKey(String resourceId, Map<String, Object> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return resourceId;
        }
        return resourceId + ":" + parameters.hashCode();
    }

    /**
     * Check if the current context has access to the resource
     * 
     * @param resourceId The resource ID
     * @param context The execution context
     * @return true if access is allowed, false otherwise
     */
    private boolean hasAccess(String resourceId, ResourceContext context) {
        // TODO: Implement proper access control
        // For now, allow all access
        return true;
    }

    /**
     * Get cache expiration time in milliseconds
     * 
     * @return Cache expiration time
     */
    private long getCacheExpirationTime() {
        // TODO: Make configurable
        return 300000; // 5 minutes
    }

    /**
     * Cached resource content with expiration
     */
    // CachedResourceContent extracted to org.openhab.core.ai.tool.resources.CachedResourceContent

    /**
     * Estimate content size based on content type.
     * 
     * @param content the content to estimate size for
     * @return estimated size in bytes
     */
    private long estimateContentSize(Object content) {
        if (content == null) {
            return 0L;
        }
        
        if (content instanceof String) {
            return ((String) content).length() * 2L; // UTF-16 encoding
        } else if (content instanceof byte[]) {
            return ((byte[]) content).length;
        } else {
            // For other types, estimate based on toString() length
            return content.toString().length() * 2L;
        }
    }

}
