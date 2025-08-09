package org.openhab.core.ai.transport;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
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
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong totalErrors = new AtomicLong(0);
    private final AtomicLong startTime = new AtomicLong(0);

    /**
     * Servlet information container.
     */
    public static class ServletInfo {
        private final String servletId;
        private final String servletName;
        private final String servletPattern;
        private final String protocol;
        private final long registrationTime;
        private final AtomicLong requestCount = new AtomicLong(0);
        private final AtomicLong errorCount = new AtomicLong(0);
        private final AtomicBoolean isHealthy = new AtomicBoolean(true);
        private volatile long lastRequestTime = 0;
        private volatile long lastErrorTime = 0;

        public ServletInfo(String servletId, String servletName, String servletPattern, String protocol) {
            this.servletId = servletId;
            this.servletName = servletName;
            this.servletPattern = servletPattern;
            this.protocol = protocol;
            this.registrationTime = System.currentTimeMillis();
        }

        public String getServletId() {
            return servletId;
        }

        public String getServletName() {
            return servletName;
        }

        public String getServletPattern() {
            return servletPattern;
        }

        public String getProtocol() {
            return protocol;
        }

        public long getRegistrationTime() {
            return registrationTime;
        }

        public long getRequestCount() {
            return requestCount.get();
        }

        public long getErrorCount() {
            return errorCount.get();
        }

        public boolean isHealthy() {
            return isHealthy.get();
        }

        public long getLastRequestTime() {
            return lastRequestTime;
        }

        public long getLastErrorTime() {
            return lastErrorTime;
        }

        public void incrementRequestCount() {
            requestCount.incrementAndGet();
            lastRequestTime = System.currentTimeMillis();
        }

        public void incrementErrorCount() {
            errorCount.incrementAndGet();
            lastErrorTime = System.currentTimeMillis();
        }

        public void setHealthy(boolean healthy) {
            isHealthy.set(healthy);
        }

        @Override
        public String toString() {
            return String.format(
                    "ServletInfo{servletId='%s', servletName='%s', protocol='%s', healthy=%s, requests=%d, errors=%d}",
                    servletId, servletName, protocol, isHealthy.get(), requestCount.get(), errorCount.get());
        }
    }

    /**
     * Activate the servlet lifecycle manager.
     */
    @Activate
    public void activate() {
        isActive.set(true);
        startTime.set(System.currentTimeMillis());
        logger.info("Servlet Lifecycle Manager activated");
    }

    /**
     * Deactivate the servlet lifecycle manager.
     */
    @Deactivate
    public void deactivate() {
        isActive.set(false);
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
        ServletInfo servletInfo = new ServletInfo(servletId, servletName, servletPattern, protocol);
        registeredServlets.put(servletId, servletInfo);
        logger.info("Registered servlet: {} ({}) with pattern: {}", servletName, protocol, servletPattern);
    }

    /**
     * Unregister a servlet from the lifecycle manager.
     * 
     * @param servletId the servlet ID
     */
    public void unregisterServlet(String servletId) {
        ServletInfo servletInfo = registeredServlets.remove(servletId);
        if (servletInfo != null) {
            logger.info("Unregistered servlet: {} ({})", servletInfo.getServletName(), servletInfo.getProtocol());
        }
    }

    /**
     * Record a request for a servlet.
     * 
     * @param servletId the servlet ID
     */
    public void recordRequest(String servletId) {
        totalRequests.incrementAndGet();
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
        totalErrors.incrementAndGet();
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
     * @return total request count
     */
    public long getTotalRequests() {
        return totalRequests.get();
    }

    /**
     * Get the total number of errors across all servlets.
     * 
     * @return total error count
     */
    public long getTotalErrors() {
        return totalErrors.get();
    }

    /**
     * Get the uptime of the lifecycle manager in milliseconds.
     * 
     * @return uptime in milliseconds
     */
    public long getUptimeMs() {
        return System.currentTimeMillis() - startTime.get();
    }

    /**
     * Get the error rate across all servlets.
     * 
     * @return error rate as a percentage
     */
    public double getErrorRate() {
        long total = totalRequests.get();
        return total > 0 ? (double) totalErrors.get() / total : 0.0;
    }

    /**
     * Get the requests per minute across all servlets.
     * 
     * @return requests per minute
     */
    public double getRequestsPerMinute() {
        long uptimeMs = getUptimeMs();
        return uptimeMs > 0 ? (totalRequests.get() * 60000.0) / uptimeMs : 0.0;
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
        totalRequests.set(0);
        totalErrors.set(0);
        startTime.set(System.currentTimeMillis());
        registeredServlets.values().forEach(servletInfo -> {
            servletInfo.requestCount.set(0);
            servletInfo.errorCount.set(0);
        });
        logger.info("Servlet Lifecycle Manager statistics reset");
    }

    /**
     * Lifecycle statistics container.
     */
    public static class LifecycleStatistics {
        private final boolean active;
        private final int servletCount;
        private final long totalRequests;
        private final long totalErrors;
        private final long uptimeMs;
        private final double errorRate;
        private final double requestsPerMinute;
        private final boolean allServletsHealthy;
        private final Map<String, ServletInfo> servlets;

        public LifecycleStatistics(boolean active, int servletCount, long totalRequests, long totalErrors,
                long uptimeMs, double errorRate, double requestsPerMinute, boolean allServletsHealthy,
                Map<String, ServletInfo> servlets) {
            this.active = active;
            this.servletCount = servletCount;
            this.totalRequests = totalRequests;
            this.totalErrors = totalErrors;
            this.uptimeMs = uptimeMs;
            this.errorRate = errorRate;
            this.requestsPerMinute = requestsPerMinute;
            this.allServletsHealthy = allServletsHealthy;
            this.servlets = servlets;
        }

        public boolean isActive() {
            return active;
        }

        public int getServletCount() {
            return servletCount;
        }

        public long getTotalRequests() {
            return totalRequests;
        }

        public long getTotalErrors() {
            return totalErrors;
        }

        public long getUptimeMs() {
            return uptimeMs;
        }

        public double getErrorRate() {
            return errorRate;
        }

        public double getRequestsPerMinute() {
            return requestsPerMinute;
        }

        public boolean isAllServletsHealthy() {
            return allServletsHealthy;
        }

        public Map<String, ServletInfo> getServlets() {
            return new ConcurrentHashMap<>(servlets);
        }

        @Override
        public String toString() {
            return String.format(
                    "LifecycleStatistics{active=%s, servletCount=%d, totalRequests=%d, totalErrors=%d, uptimeMs=%d, errorRate=%.2f%%, requestsPerMinute=%.2f, allServletsHealthy=%s}",
                    active, servletCount, totalRequests, totalErrors, uptimeMs, errorRate * 100, requestsPerMinute,
                    allServletsHealthy);
        }
    }
}
