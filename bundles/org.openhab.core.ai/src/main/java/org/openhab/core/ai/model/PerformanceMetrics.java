package org.openhab.core.ai.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Performance metrics for model response parsing.
 * Extracted from ModelResponseActionParser.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public class PerformanceMetrics {
    private final long totalParsingAttempts;
    private final long successfulJsonParses;
    private final long successfulRegexParses;
    private final long failedParses;
    private final long totalActionCalls;

    private PerformanceMetrics(Builder builder) {
        this.totalParsingAttempts = builder.totalParsingAttempts;
        this.successfulJsonParses = builder.successfulJsonParses;
        this.successfulRegexParses = builder.successfulRegexParses;
        this.failedParses = builder.failedParses;
        this.totalActionCalls = builder.totalActionCalls;
    }

    public long getTotalParsingAttempts() { return totalParsingAttempts; }
    public long getSuccessfulJsonParses() { return successfulJsonParses; }
    public long getSuccessfulRegexParses() { return successfulRegexParses; }
    public long getFailedParses() { return failedParses; }
    public long getTotalActionCalls() { return totalActionCalls; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private long totalParsingAttempts;
        private long successfulJsonParses;
        private long successfulRegexParses;
        private long failedParses;
        private long totalActionCalls;

        public Builder totalParsingAttempts(long v) { this.totalParsingAttempts = v; return this; }
        public Builder successfulJsonParses(long v) { this.successfulJsonParses = v; return this; }
        public Builder successfulRegexParses(long v) { this.successfulRegexParses = v; return this; }
        public Builder failedParses(long v) { this.failedParses = v; return this; }
        public Builder totalActionCalls(long v) { this.totalActionCalls = v; return this; }
        public PerformanceMetrics build() { return new PerformanceMetrics(this); }
    }
}


