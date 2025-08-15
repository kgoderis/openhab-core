package org.openhab.core.ai.reasoning.memory;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class AgentMemoryPerformanceMetrics {
    private final long totalMemories;
    private final long shortTermMemories;
    private final long longTermMemories;
    private final double averageSearchTime;
    private final double averageStorageTime;

    public AgentMemoryPerformanceMetrics(long totalMemories, long shortTermMemories, long longTermMemories,
            double averageSearchTime, double averageStorageTime) {
        this.totalMemories = totalMemories;
        this.shortTermMemories = shortTermMemories;
        this.longTermMemories = longTermMemories;
        this.averageSearchTime = averageSearchTime;
        this.averageStorageTime = averageStorageTime;
    }

    public long getTotalMemories() {
        return totalMemories;
    }

    public long getShortTermMemories() {
        return shortTermMemories;
    }

    public long getLongTermMemories() {
        return longTermMemories;
    }

    public double getAverageSearchTime() {
        return averageSearchTime;
    }

    public double getAverageStorageTime() {
        return averageStorageTime;
    }
}
