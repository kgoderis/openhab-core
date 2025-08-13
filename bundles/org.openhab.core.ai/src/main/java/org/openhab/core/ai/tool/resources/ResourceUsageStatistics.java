package org.openhab.core.ai.tool.resources;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.model.api.ModelProviderType;

/**
 * Resource usage statistics snapshot.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ResourceUsageStatistics {
    private final long currentConcurrentRequests;
    private final long totalRequestsProcessed;
    private final long totalRequestsRejected;
    private final long totalRequestsTimedOut;
    private final long currentMemoryUsage;
    private final long totalMemory;
    private final int queueSize;
    private final int activeThreads;
    private final int poolSize;
    private final Map<ModelProviderType, ProviderResourceUsage> providerUsage;

    public ResourceUsageStatistics(long currentConcurrentRequests, long totalRequestsProcessed,
            long totalRequestsRejected, long totalRequestsTimedOut, long currentMemoryUsage, long totalMemory,
            int queueSize, int activeThreads, int poolSize,
            Map<ModelProviderType, ProviderResourceUsage> providerUsage) {
        this.currentConcurrentRequests = currentConcurrentRequests;
        this.totalRequestsProcessed = totalRequestsProcessed;
        this.totalRequestsRejected = totalRequestsRejected;
        this.totalRequestsTimedOut = totalRequestsTimedOut;
        this.currentMemoryUsage = currentMemoryUsage;
        this.totalMemory = totalMemory;
        this.queueSize = queueSize;
        this.activeThreads = activeThreads;
        this.poolSize = poolSize;
        this.providerUsage = providerUsage;
    }

    public long getCurrentConcurrentRequests() {
        return currentConcurrentRequests;
    }

    public long getTotalRequestsProcessed() {
        return totalRequestsProcessed;
    }

    public long getTotalRequestsRejected() {
        return totalRequestsRejected;
    }

    public long getTotalRequestsTimedOut() {
        return totalRequestsTimedOut;
    }

    public long getCurrentMemoryUsage() {
        return currentMemoryUsage;
    }

    public long getTotalMemory() {
        return totalMemory;
    }

    public int getQueueSize() {
        return queueSize;
    }

    public int getActiveThreads() {
        return activeThreads;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public Map<ModelProviderType, ProviderResourceUsage> getProviderUsage() {
        return providerUsage;
    }

    public double getMemoryUsagePercentage() {
        return totalMemory > 0 ? (double) currentMemoryUsage / totalMemory * 100.0 : 0.0;
    }

    public double getRejectionRate() {
        long totalRequests = totalRequestsProcessed + totalRequestsRejected;
        return totalRequests > 0 ? (double) totalRequestsRejected / totalRequests * 100.0 : 0.0;
    }
}
