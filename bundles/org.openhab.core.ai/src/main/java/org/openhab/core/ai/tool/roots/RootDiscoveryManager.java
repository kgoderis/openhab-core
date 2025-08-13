package org.openhab.core.ai.tool.roots;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.roots.discovery.Root;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
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

    /** Performance monitoring */
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong successfulRequests = new AtomicLong(0);
    private final AtomicLong failedRequests = new AtomicLong(0);
    private final AtomicLong totalResponseTimeMs = new AtomicLong(0);

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
        totalRequests.incrementAndGet();
        long startTime = System.currentTimeMillis();

        try {
            LOGGER.debug("Listing {} active roots", activeRoots.size());

            List<Root> roots = List.copyOf(activeRoots.values());
            successfulRequests.incrementAndGet();

            return roots;

        } catch (Exception e) {
            LOGGER.error("Error listing roots", e);
            failedRequests.incrementAndGet();
            throw new RuntimeException("Failed to list roots", e);
        } finally {
            long responseTime = System.currentTimeMillis() - startTime;
            totalResponseTimeMs.addAndGet(responseTime);
        }
    }

    @Override
    public @Nullable Root getRoot(String rootId) {
        totalRequests.incrementAndGet();
        long startTime = System.currentTimeMillis();

        try {
            LOGGER.debug("Getting root: {}", rootId);

            Root root = activeRoots.get(rootId);
            if (root != null) {
                successfulRequests.incrementAndGet();
            } else {
                failedRequests.incrementAndGet();
            }

            return root;

        } catch (Exception e) {
            LOGGER.error("Error getting root: {}", rootId, e);
            failedRequests.incrementAndGet();
            return null;
        } finally {
            long responseTime = System.currentTimeMillis() - startTime;
            totalResponseTimeMs.addAndGet(responseTime);
        }
    }

    @Override
    public boolean addRoot(String rootId, String path, String description, boolean readOnly) {
        totalRequests.incrementAndGet();
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
                failedRequests.incrementAndGet();
                return false;
            }

            // Create and add root
            Root root = new DefaultRoot(rootId, path, description, readOnly);
            activeRoots.put(rootId, root);

            successfulRequests.incrementAndGet();
            LOGGER.info("Added root: {} at path: {}", rootId, path);
            return true;

        } catch (Exception e) {
            LOGGER.error("Error adding root: {} at path: {}", rootId, path, e);
            failedRequests.incrementAndGet();
            return false;
        } finally {
            long responseTime = System.currentTimeMillis() - startTime;
            totalResponseTimeMs.addAndGet(responseTime);
        }
    }

    @Override
    public boolean removeRoot(String rootId) {
        totalRequests.incrementAndGet();
        long startTime = System.currentTimeMillis();

        try {
            LOGGER.debug("Removing root: {}", rootId);

            Root removedRoot = activeRoots.remove(rootId);
            if (removedRoot != null) {
                successfulRequests.incrementAndGet();
                LOGGER.info("Removed root: {}", rootId);
                return true;
            } else {
                failedRequests.incrementAndGet();
                LOGGER.warn("Root not found for removal: {}", rootId);
                return false;
            }

        } catch (Exception e) {
            LOGGER.error("Error removing root: {}", rootId, e);
            failedRequests.incrementAndGet();
            return false;
        } finally {
            long responseTime = System.currentTimeMillis() - startTime;
            totalResponseTimeMs.addAndGet(responseTime);
        }
    }

    @Override
    public int getRootCount() {
        return activeRoots.size();
    }

    @Override
    public Map<String, Object> getPerformanceMetrics() {
        Map<String, Object> metrics = new ConcurrentHashMap<>();
        metrics.put("totalRequests", totalRequests.get());
        metrics.put("successfulRequests", successfulRequests.get());
        metrics.put("failedRequests", failedRequests.get());
        metrics.put("activeRoots", activeRoots.size());
        metrics.put("totalResponseTimeMs", totalResponseTimeMs.get());
        metrics.put("averageResponseTimeMs",
                totalRequests.get() > 0 ? totalResponseTimeMs.get() / totalRequests.get() : 0);
        metrics.put("successRate",
                totalRequests.get() > 0 ? (double) successfulRequests.get() / totalRequests.get() : 0.0);
        return metrics;
    }

    // DefaultRoot extracted to org.openhab.core.ai.tool.roots.DefaultRoot
}
