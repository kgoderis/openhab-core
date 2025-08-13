package org.openhab.core.ai.reasoning;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class MemoryPerformanceMetricsBuilder {
    long totalStores;
    long totalRetrievals;
    long totalConsolidations;
    long totalPatternRecognitions;
    int shortTermMemoryCount;
    int longTermMemoryCount;
    int patternCount;

    public MemoryPerformanceMetricsBuilder totalStores(long v) { this.totalStores = v; return this; }
    public MemoryPerformanceMetricsBuilder totalRetrievals(long v) { this.totalRetrievals = v; return this; }
    public MemoryPerformanceMetricsBuilder totalConsolidations(long v) { this.totalConsolidations = v; return this; }
    public MemoryPerformanceMetricsBuilder totalPatternRecognitions(long v) { this.totalPatternRecognitions = v; return this; }
    public MemoryPerformanceMetricsBuilder shortTermMemoryCount(int v) { this.shortTermMemoryCount = v; return this; }
    public MemoryPerformanceMetricsBuilder longTermMemoryCount(int v) { this.longTermMemoryCount = v; return this; }
    public MemoryPerformanceMetricsBuilder patternCount(int v) { this.patternCount = v; return this; }
    public MemoryPerformanceMetrics build() { return new MemoryPerformanceMetrics(this); }
}


