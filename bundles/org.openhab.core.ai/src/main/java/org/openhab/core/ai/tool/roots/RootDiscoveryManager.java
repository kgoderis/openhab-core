package org.openhab.core.ai.tool.roots;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.roots.discovery.Root;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Root Discovery Manager implementation for MCP Client Features
 * 
 * Provides filesystem boundary management for server operations,
 * allowing clients to specify which directories servers should focus on.
 * 
 * @author Karel Goderis - Initial Contribution
 */
@Component(service = RootsService.class, immediate = true)
@NonNullByDefault
public class RootDiscoveryManager implements RootsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RootDiscoveryManager.class);

    /** Map of active roots by ID */
    private final Map<String, Root> activeRoots = new ConcurrentHashMap<>();

    /** Performance monitoring - migrated to MetricsService */
    // private final AtomicLong totalRequests = new AtomicLong(0);
    // private final AtomicLong successfulRequests = new AtomicLong(0);
    // private final AtomicLong failedRequests = new AtomicLong(0);
    // private final AtomicLong totalResponseTimeMs = new AtomicLong(0);

    private MetricsService metricsService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC)
    protected void setMetricsService(MetricsService metricsService) {
        this.metricsService = metricsService;
        LOGGER.debug("MetricsService set for RootDiscoveryManager");
    }

    @Activate
    public RootDiscoveryManager() {
        LOGGER.debug("Initializing OpenHAB Roots Service");
        initializeDefaultRoots();
    }

    @Deactivate
    public void deactivate() {
        LOGGER.debug("Deactivating OpenHAB Roots Service");
        activeRoots.clear();
    }

    @Modified
    public void modified() {
        LOGGER.debug("Modifying OpenHAB Roots Service");
        initializeDefaultRoots();
    }

    private void initializeDefaultRoots() {
        // Initialize default openHAB roots
        addRoot("conf", "/conf", "openHAB configuration directory", true);
        addRoot("userdata", "/userdata", "openHAB user data directory", true);
        addRoot("logs", "/logs", "openHAB logs directory", true);
        addRoot("addons", "/addons", "openHAB addons directory", true);

        LOGGER.info("OpenHAB Roots Service initialized with {} default roots", activeRoots.size());
    }

    @Override
    public List<Root> listRoots() {
        metricsService.recordOperation("listRoots");
        long startTime = System.currentTimeMillis();

        try {
            LOGGER.debug("Listing {} active roots", activeRoots.size());

            List<Root> roots = List.copyOf(activeRoots.values());
            metricsService.recordOperation("listRoots.success");

            return roots;

        } catch (Exception e) {
            LOGGER.error("Error listing roots", e);
            metricsService.recordOperation("listRoots.failure");
            throw new RuntimeException("Failed to list roots", e);
        } finally {
            long responseTime = System.currentTimeMillis() - startTime;
            metricsService.recordOperation("listRoots.duration", responseTime);
        }
    }

    @Override
    public @Nullable Root getRoot(String rootId) {
        metricsService.recordOperation("getRoot");
        long startTime = System.currentTimeMillis();

        try {
            LOGGER.debug("Getting root: {}", rootId);

            Root root = activeRoots.get(rootId);
            if (root != null) {
                metricsService.recordOperation("getRoot.success");
            } else {
                metricsService.recordOperation("getRoot.failure");
            }

            return root;

        } catch (Exception e) {
            LOGGER.error("Error getting root: {}", rootId, e);
            metricsService.recordOperation("getRoot.failure");
            return null;
        } finally {
            long responseTime = System.currentTimeMillis() - startTime;
            metricsService.recordOperation("getRoot.duration", responseTime);
        }
    }

    @Override
    public boolean addRoot(String rootId, String path, String description, boolean readOnly) {
        metricsService.recordOperation("addRoot");
        long startTime = System.currentTimeMillis();

        try {
            LOGGER.debug("Adding root: {} at path: {}", rootId, path);

            // Validate input parameters
            if (rootId == null || rootId.trim().isEmpty()) {
                throw new IllegalArgumentException("Root ID cannot be null or empty");
            }
            if (path == null || path.trim().isEmpty()) {
                throw new IllegalArgumentException("Root path cannot be null or empty");
            }

            // Check if root already exists
            if (activeRoots.containsKey(rootId)) {
                LOGGER.warn("Root already exists: {}", rootId);
                metricsService.recordOperation("addRoot.failure");
                return false;
            }

            // Create and add root
            Root root = new DefaultRoot(rootId, path, description, readOnly);
            activeRoots.put(rootId, root);

            metricsService.recordOperation("addRoot.success");
            LOGGER.info("Added root: {} at path: {}", rootId, path);
            return true;

        } catch (Exception e) {
            LOGGER.error("Error adding root: {} at path: {}", rootId, path, e);
            metricsService.recordOperation("addRoot.failure");
            return false;
        } finally {
            long responseTime = System.currentTimeMillis() - startTime;
            metricsService.recordOperation("addRoot.duration", responseTime);
        }
    }

    @Override
    public boolean removeRoot(String rootId) {
        metricsService.recordOperation("removeRoot");
        long startTime = System.currentTimeMillis();

        try {
            LOGGER.debug("Removing root: {}", rootId);

            Root removedRoot = activeRoots.remove(rootId);
            if (removedRoot != null) {
                metricsService.recordOperation("removeRoot.success");
                LOGGER.info("Removed root: {}", rootId);
                return true;
            } else {
                metricsService.recordOperation("removeRoot.failure");
                LOGGER.warn("Root not found for removal: {}", rootId);
                return false;
            }

        } catch (Exception e) {
            LOGGER.error("Error removing root: {}", rootId, e);
            metricsService.recordOperation("removeRoot.failure");
            return false;
        } finally {
            long responseTime = System.currentTimeMillis() - startTime;
            metricsService.recordOperation("removeRoot.duration", responseTime);
        }
    }

    @Override
    public int getRootCount() {
        return activeRoots.size();
    }

    @Override
    public Map<String, Object> getPerformanceMetrics() {
        Map<String, Object> metrics = new ConcurrentHashMap<>();
        metrics.put("totalRequests", metricsService.getOperationCount("listRoots"));
        metrics.put("successfulRequests", metricsService.getOperationCount("listRoots.success"));
        metrics.put("failedRequests", metricsService.getOperationCount("listRoots.failure"));
        metrics.put("activeRoots", activeRoots.size());
        metrics.put("totalResponseTimeMs", metricsService.getOperationDuration("listRoots.duration"));
        metrics.put("averageResponseTimeMs",
                metricsService.getOperationCount("listRoots.duration") > 0 ? metricsService.getOperationDuration("listRoots.duration") / metricsService.getOperationCount("listRoots.duration") : 0);
        metrics.put("successRate",
                metricsService.getOperationCount("listRoots.duration") > 0 ? (double) metricsService.getOperationCount("listRoots.success") / metricsService.getOperationCount("listRoots.duration") : 0.0);
        return metrics;
    }

    // DefaultRoot extracted to org.openhab.core.ai.tool.roots.DefaultRoot
}
