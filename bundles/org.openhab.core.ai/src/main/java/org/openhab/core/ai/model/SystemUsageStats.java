package org.openhab.core.ai.model;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * System-wide model usage statistics DTO.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class SystemUsageStats {
    private final long totalRequests;
    private final long totalTokens;
    private final double totalCost;
    private final Instant lastRequestTime;
    private final double averageResponseTime;
    private final double errorRate;

    public SystemUsageStats(long totalRequests, long totalTokens, double totalCost, Instant lastRequestTime,
            double averageResponseTime, double errorRate) {
        this.totalRequests = totalRequests;
        this.totalTokens = totalTokens;
        this.totalCost = totalCost;
        this.lastRequestTime = lastRequestTime;
        this.averageResponseTime = averageResponseTime;
        this.errorRate = errorRate;
    }

    public long getTotalRequests() {
        return totalRequests;
    }

    public long getTotalTokens() {
        return totalTokens;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public Instant getLastRequestTime() {
        return lastRequestTime;
    }

    public double getAverageResponseTime() {
        return averageResponseTime;
    }

    public double getErrorRate() {
        return errorRate;
    }
}
