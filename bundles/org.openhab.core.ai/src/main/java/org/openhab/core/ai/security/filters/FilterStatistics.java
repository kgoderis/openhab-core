package org.openhab.core.ai.security.filters;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * HTTP filter statistics DTO.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class FilterStatistics {
    private final long totalRequests;
    private final long blockedRequests;
    private final long uptimeMs;

    public FilterStatistics(long totalRequests, long blockedRequests, long uptimeMs) {
        this.totalRequests = totalRequests;
        this.blockedRequests = blockedRequests;
        this.uptimeMs = uptimeMs;
    }

    public long getTotalRequests() {
        return totalRequests;
    }

    public long getBlockedRequests() {
        return blockedRequests;
    }

    public long getUptimeMs() {
        return uptimeMs;
    }

    public double getBlockRate() {
        return totalRequests > 0 ? (double) blockedRequests / totalRequests : 0.0;
    }

    public double getRequestsPerMinute() {
        return uptimeMs > 0 ? (totalRequests * 60000.0) / uptimeMs : 0.0;
    }

    @Override
    public String toString() {
        return String.format(
                "FilterStatistics{totalRequests=%d, blockedRequests=%d, uptimeMs=%d, blockRate=%.2f%%, requestsPerMinute=%.2f}",
                totalRequests, blockedRequests, uptimeMs, getBlockRate() * 100, getRequestsPerMinute());
    }
}
