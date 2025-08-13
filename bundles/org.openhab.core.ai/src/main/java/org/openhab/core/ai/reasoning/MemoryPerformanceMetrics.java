package org.openhab.core.ai.reasoning;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class MemoryPerformanceMetrics {
    private final long totalStores;
    private final long totalRetrievals;
    private final long totalConsolidations;
    private final long totalPatternRecognitions;
    private final int shortTermMemoryCount;
    private final int longTermMemoryCount;
    private final int patternCount;

    MemoryPerformanceMetrics(MemoryPerformanceMetricsBuilder builder) {
        this.totalStores = builder.totalStores;
        this.totalRetrievals = builder.totalRetrievals;
        this.totalConsolidations = builder.totalConsolidations;
        this.totalPatternRecognitions = builder.totalPatternRecognitions;
        this.shortTermMemoryCount = builder.shortTermMemoryCount;
        this.longTermMemoryCount = builder.longTermMemoryCount;
        this.patternCount = builder.patternCount;
    }

    public long getTotalStores() { return totalStores; }
    public long getTotalRetrievals() { return totalRetrievals; }
    public long getTotalConsolidations() { return totalConsolidations; }
    public long getTotalPatternRecognitions() { return totalPatternRecognitions; }
    public int getShortTermMemoryCount() { return shortTermMemoryCount; }
    public int getLongTermMemoryCount() { return longTermMemoryCount; }
    public int getPatternCount() { return patternCount; }

    public static MemoryPerformanceMetricsBuilder builder() { return new MemoryPerformanceMetricsBuilder(); }

    /* Extracted: org.openhab.core.ai.reasoning.MemoryPerformanceMetricsBuilder */
}


