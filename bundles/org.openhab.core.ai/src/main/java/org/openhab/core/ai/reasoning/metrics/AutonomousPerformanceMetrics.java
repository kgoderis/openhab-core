package org.openhab.core.ai.reasoning.metrics;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class AutonomousPerformanceMetrics {
    private final long totalEventsProcessed;
    private final long totalAutonomousActions;
    private final long totalPatternDetections;
    private final long totalSafetyViolations;
    private final long totalUserOverrides;
    private final int pendingActionCount;
    private final int patternCount;
    private final int preferenceCount;
    private final int constraintCount;

    AutonomousPerformanceMetrics(AutonomousPerformanceMetricsBuilder builder) {
        this.totalEventsProcessed = builder.totalEventsProcessed;
        this.totalAutonomousActions = builder.totalAutonomousActions;
        this.totalPatternDetections = builder.totalPatternDetections;
        this.totalSafetyViolations = builder.totalSafetyViolations;
        this.totalUserOverrides = builder.totalUserOverrides;
        this.pendingActionCount = builder.pendingActionCount;
        this.patternCount = builder.patternCount;
        this.preferenceCount = builder.preferenceCount;
        this.constraintCount = builder.constraintCount;
    }

    public long getTotalEventsProcessed() {
        return totalEventsProcessed;
    }

    public long getTotalAutonomousActions() {
        return totalAutonomousActions;
    }

    public long getTotalPatternDetections() {
        return totalPatternDetections;
    }

    public long getTotalSafetyViolations() {
        return totalSafetyViolations;
    }

    public long getTotalUserOverrides() {
        return totalUserOverrides;
    }

    public int getPendingActionCount() {
        return pendingActionCount;
    }

    public int getPatternCount() {
        return patternCount;
    }

    public int getPreferenceCount() {
        return preferenceCount;
    }

    public int getConstraintCount() {
        return constraintCount;
    }

    public static AutonomousPerformanceMetricsBuilder builder() {
        return new AutonomousPerformanceMetricsBuilder();
    }

    /* Extracted: org.openhab.core.ai.reasoning.AutonomousPerformanceMetricsBuilder */
}
