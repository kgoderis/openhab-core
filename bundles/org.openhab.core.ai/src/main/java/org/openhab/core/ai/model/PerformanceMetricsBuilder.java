package org.openhab.core.ai.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class PerformanceMetricsBuilder {
    private long totalParsingAttempts;
    private long successfulJsonParses;
    private long successfulRegexParses;
    private long failedParses;
    private long totalActionCalls;

    public PerformanceMetricsBuilder totalParsingAttempts(long v) {
        this.totalParsingAttempts = v;
        return this;
    }

    public PerformanceMetricsBuilder successfulJsonParses(long v) {
        this.successfulJsonParses = v;
        return this;
    }

    public PerformanceMetricsBuilder successfulRegexParses(long v) {
        this.successfulRegexParses = v;
        return this;
    }

    public PerformanceMetricsBuilder failedParses(long v) {
        this.failedParses = v;
        return this;
    }

    public PerformanceMetricsBuilder totalActionCalls(long v) {
        this.totalActionCalls = v;
        return this;
    }

    public PerformanceMetrics build() {
        return PerformanceMetrics.builder().totalParsingAttempts(totalParsingAttempts)
                .successfulJsonParses(successfulJsonParses).successfulRegexParses(successfulRegexParses)
                .failedParses(failedParses).totalActionCalls(totalActionCalls).build();
    }
}
