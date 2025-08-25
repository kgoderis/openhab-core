package org.openhab.core.ai.transport;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unified HTTP Server Lifecycle Manager for MCP and A2A protocols.
 * 
 * This component manages the lifecycle of MCP and A2A servlets within openHAB's
 * HTTP server using the OSGi HTTP Whiteboard pattern. It provides centralized
 * management, monitoring, and health checking for all protocol servlets.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
@Component(service = ServletLifecycleManager.class, immediate = true)
public class ServletLifecycleManager {

    private static final Logger logger = LoggerFactory.getLogger(ServletLifecycleManager.class);

    private final Map<String, ServletInfo> registeredServlets = new ConcurrentHashMap<>();
    private final AtomicBoolean isActive = new AtomicBoolean(false);
    private final long startTime = System.currentTimeMillis();

    @Reference
    private @Nullable MetricsService metricsService;

    /**
     * Activate the servlet lifecycle manager.
     */
    @Activate
    public void activate() {
        isActive.set(true);
        recordMetrics("servlet-lifecycle", "manager-activated", true, Duration.ZERO);
        logger.info("Servlet Lifecycle Manager activated");
    }

    /**
     * Deactivate the servlet lifecycle manager.
     */
    @Deactivate
    public void deactivate() {
        isActive.set(false);
        recordMetrics("servlet-lifecycle", "manager-deactivated", true, Duration.ZERO);
        logger.info("Servlet Lifecycle Manager deactivated");
    }

    /**
     * Register a servlet with the lifecycle manager.
     * 
     * @param servletId the servlet ID
     * @param servletName the servlet name
     * @param servletPattern the servlet pattern
     * @param protocol the protocol (MCP or A2A)
     */
    public void registerServlet(String servletId, String servletName, String servletPattern, String protocol) {
        Instant startTime = Instant.now();
        boolean success = false;

        try {
            ServletInfo servletInfo = new ServletInfo(servletId, servletName, servletPattern, protocol);
            registeredServlets.put(servletId, servletInfo);
            success = true;
            logger.info("Registered servlet: {} ({}) with pattern: {}", servletName, protocol, servletPattern);
        } catch (Exception e) {
            logger.error("Error registering servlet {}: {}", servletId, e.getMessage(), e);
        } finally {
            recordMetrics("servlet-lifecycle", "servlet-registered", success,
                    Duration.between(startTime, Instant.now()));
        }
    }

    /**
     * Unregister a servlet from the lifecycle manager.
     * 
     * @param servletId the servlet ID
     */
    public void unregisterServlet(String servletId) {
        Instant startTime = Instant.now();
        boolean success = false;

        try {
            ServletInfo servletInfo = registeredServlets.remove(servletId);
            if (servletInfo != null) {
                success = true;
                logger.info("Unregistered servlet: {} ({})", servletInfo.getServletName(), servletInfo.getProtocol());
            }
        } catch (Exception e) {
            logger.error("Error unregistering servlet {}: {}", servletId, e.getMessage(), e);
        } finally {
            recordMetrics("servlet-lifecycle", "servlet-unregistered", success,
                    Duration.between(startTime, Instant.now()));
        }
    }

    /**
     * Record a request for a servlet.
     * 
     * @param servletId the servlet ID
     */
    public void recordRequest(String servletId) {
        recordMetrics("servlet-lifecycle", "request-recorded", true, Duration.ZERO);
        ServletInfo servletInfo = registeredServlets.get(servletId);
        if (servletInfo != null) {
            servletInfo.incrementRequestCount();
        }
    }

    /**
     * Record an error for a servlet.
     * 
     * @param servletId the servlet ID
     */
    public void recordError(String servletId) {
        recordMetrics("servlet-lifecycle", "error-recorded", false, Duration.ZERO);
        ServletInfo servletInfo = registeredServlets.get(servletId);
        if (servletInfo != null) {
            servletInfo.incrementErrorCount();
        }
    }

    /**
     * Update the health status of a servlet.
     * 
     * @param servletId the servlet ID
     * @param healthy the health status
     */
    public void updateServletHealth(String servletId, boolean healthy) {
        ServletInfo servletInfo = registeredServlets.get(servletId);
        if (servletInfo != null) {
            servletInfo.setHealthy(healthy);
            if (!healthy) {
                logger.warn("Servlet {} marked as unhealthy", servletInfo.getServletName());
            } else {
                logger.debug("Servlet {} marked as healthy", servletInfo.getServletName());
            }
        }
    }

    /**
     * Get servlet information by ID.
     * 
     * @param servletId the servlet ID
     * @return the servlet information or null if not found
     */
    public @Nullable ServletInfo getServletInfo(String servletId) {
        return registeredServlets.get(servletId);
    }

    /**
     * Get all registered servlets.
     * 
     * @return map of servlet ID to servlet information
     */
    public Map<String, ServletInfo> getAllServlets() {
        return new ConcurrentHashMap<>(registeredServlets);
    }

    /**
     * Check if the lifecycle manager is active.
     * 
     * @return true if active
     */
    public boolean isActive() {
        return isActive.get();
    }

    /**
     * Get the total number of requests across all servlets.
     * 
     * @return total requests
     */
    public long getTotalRequests() {
        MetricsService metrics = metricsService;
        if (metrics == null) {
            logger.warn("MetricsService not available, returning 0 for total requests");
            return 0;
        }

        var snapshot = metrics.getDomainAggregatedSnapshot("servlet-lifecycle");
        return snapshot.totalOperations();
    }

    /**
     * Get the total number of errors across all servlets.
     * 
     * @return total errors
     */
    public long getTotalErrors() {
        MetricsService metrics = metricsService;
        if (metrics == null) {
            logger.warn("MetricsService not available, returning 0 for total errors");
            return 0;
        }

        var snapshot = metrics.getDomainAggregatedSnapshot("servlet-lifecycle");
        return snapshot.failedOperations();
    }

    /**
     * Get the uptime of the lifecycle manager in milliseconds.
     * 
     * @return uptime in milliseconds
     */
    public long getUptimeMs() {
        return System.currentTimeMillis() - startTime;
    }

    /**
     * Get the error rate across all servlets.
     * 
     * @return error rate as a percentage
     */
    public double getErrorRate() {
        long total = getTotalRequests();
        return total > 0 ? (double) getTotalErrors() / total : 0.0;
    }

    /**
     * Get the requests per minute across all servlets.
     * 
     * @return requests per minute
     */
    public double getRequestsPerMinute() {
        long uptimeMs = getUptimeMs();
        return uptimeMs > 0 ? (getTotalRequests() * 60000.0) / uptimeMs : 0.0;
    }

    /**
     * Check if all servlets are healthy.
     * 
     * @return true if all servlets are healthy
     */
    public boolean areAllServletsHealthy() {
        return registeredServlets.values().stream().allMatch(ServletInfo::isHealthy);
    }

    /**
     * Get the number of registered servlets.
     * 
     * @return number of servlets
     */
    public int getServletCount() {
        return registeredServlets.size();
    }

    /**
     * Get servlets by protocol.
     * 
     * @param protocol the protocol (MCP or A2A)
     * @return map of servlet ID to servlet information for the protocol
     */
    public Map<String, ServletInfo> getServletsByProtocol(String protocol) {
        Map<String, ServletInfo> protocolServlets = new ConcurrentHashMap<>();
        registeredServlets.forEach((id, info) -> {
            if (protocol.equals(info.getProtocol())) {
                protocolServlets.put(id, info);
            }
        });
        return protocolServlets;
    }

    /**
     * Get overall health status.
     * 
     * @return true if the lifecycle manager and all servlets are healthy
     */
    public boolean isHealthy() {
        return isActive.get() && areAllServletsHealthy();
    }

    /**
     * Get comprehensive statistics.
     * 
     * @return statistics object
     */
    public LifecycleStatistics getStatistics() {
        return new LifecycleStatistics(isActive.get(), getServletCount(), getTotalRequests(), getTotalErrors(),
                getUptimeMs(), getErrorRate(), getRequestsPerMinute(), areAllServletsHealthy(),
                new ConcurrentHashMap<>(registeredServlets));
    }

    /**
     * Reset statistics.
     */
    public void resetStatistics() {
        recordMetrics("servlet-lifecycle", "statistics-reset", true, Duration.ZERO);
        registeredServlets.values().forEach(ServletInfo::resetStatistics);
        logger.info("Servlet Lifecycle Manager statistics reset");
    }

    private void recordMetrics(String domain, String operation, boolean success, Duration duration) {
        MetricsService metrics = metricsService;
        if (metrics != null) {
            metrics.recordOperation(domain, operation, success, duration);
        } else {
            logger.warn("MetricsService not available, cannot record metrics for operation: {} - {}", domain,
                    operation);
        }
    }
}
