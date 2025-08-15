package org.openhab.core.ai.transport;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Servlet information container.
 * 
 * This class contains information about a registered servlet including
 * metadata, request counts, error counts, health status, and timing information.
 * Originally extracted from ServletLifecycleManager as an inner class.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ServletInfo {
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

    /**
     * Reset the servlet statistics.
     */
    public void resetStatistics() {
        requestCount.set(0);
        errorCount.set(0);
        lastRequestTime = 0;
        lastErrorTime = 0;
    }

    @Override
    public String toString() {
        return String.format(
                "ServletInfo{servletId='%s', servletName='%s', protocol='%s', healthy=%s, requests=%d, errors=%d}",
                servletId, servletName, protocol, isHealthy.get(), requestCount.get(), errorCount.get());
    }
}
