package org.openhab.core.ai.reasoning;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Performance metrics for the autonomous reasoning input manager.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class InputPerformanceMetrics {
    private final long totalInputsProcessed;
    private final long totalBatchesCreated;
    private final long totalInputsRouted;
    private final long totalProcessingTime;
    private final int queueSize;
    private final int activeInputsCount;
    private final int batchCount;
    private final int routerCount;

    public InputPerformanceMetrics(long totalInputsProcessed, long totalBatchesCreated, long totalInputsRouted,
            long totalProcessingTime, int queueSize, int activeInputsCount, int batchCount, int routerCount) {
        this.totalInputsProcessed = totalInputsProcessed;
        this.totalBatchesCreated = totalBatchesCreated;
        this.totalInputsRouted = totalInputsRouted;
        this.totalProcessingTime = totalProcessingTime;
        this.queueSize = queueSize;
        this.activeInputsCount = activeInputsCount;
        this.batchCount = batchCount;
        this.routerCount = routerCount;
    }

    public long getTotalInputsProcessed() {
        return totalInputsProcessed;
    }

    public long getTotalBatchesCreated() {
        return totalBatchesCreated;
    }

    public long getTotalInputsRouted() {
        return totalInputsRouted;
    }

    public long getTotalProcessingTime() {
        return totalProcessingTime;
    }

    public int getQueueSize() {
        return queueSize;
    }

    public int getActiveInputsCount() {
        return activeInputsCount;
    }

    public int getBatchCount() {
        return batchCount;
    }

    public int getRouterCount() {
        return routerCount;
    }
}


