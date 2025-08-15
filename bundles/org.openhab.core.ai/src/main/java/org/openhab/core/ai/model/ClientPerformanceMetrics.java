package org.openhab.core.ai.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Per-client performance metrics summary.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ClientPerformanceMetrics {
    private final double averageResponseTime;
    private final long minResponseTime;
    private final long maxResponseTime;
    private final double errorRate;
    private final long totalErrors;
    private final long totalRequests;

    public ClientPerformanceMetrics(double averageResponseTime, long minResponseTime, long maxResponseTime,
            double errorRate, long totalErrors, long totalRequests) {
        this.averageResponseTime = averageResponseTime;
        this.minResponseTime = minResponseTime;
        this.maxResponseTime = maxResponseTime;
        this.errorRate = errorRate;
        this.totalErrors = totalErrors;
        this.totalRequests = totalRequests;
    }

    public double getAverageResponseTime() {
        return averageResponseTime;
    }

    public long getMinResponseTime() {
        return minResponseTime;
    }

    public long getMaxResponseTime() {
        return maxResponseTime;
    }

    public double getErrorRate() {
        return errorRate;
    }

    public long getTotalErrors() {
        return totalErrors;
    }

    public long getTotalRequests() {
        return totalRequests;
    }
}
