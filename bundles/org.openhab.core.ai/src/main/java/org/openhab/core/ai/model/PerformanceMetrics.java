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

    public PerformanceMetrics(long totalParsingAttempts, long successfulJsonParses, long successfulRegexParses,
            long failedParses, long totalActionCalls) {
        this.totalParsingAttempts = totalParsingAttempts;
        this.successfulJsonParses = successfulJsonParses;
        this.successfulRegexParses = successfulRegexParses;
        this.failedParses = failedParses;
        this.totalActionCalls = totalActionCalls;
    }

    public long getTotalParsingAttempts() {
        return totalParsingAttempts;
    }

    public long getSuccessfulJsonParses() {
        return successfulJsonParses;
    }

    public long getSuccessfulRegexParses() {
        return successfulRegexParses;
    }

    public long getFailedParses() {
        return failedParses;
    }

    public long getTotalActionCalls() {
        return totalActionCalls;
    }

    public static PerformanceMetricsBuilder builder() {
        return new PerformanceMetricsBuilder();
    }
}
