package org.openhab.core.ai.reasoning;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class AutonomousPerformanceMetricsBuilder {
    long totalEventsProcessed;
    long totalAutonomousActions;
    long totalPatternDetections;
    long totalSafetyViolations;
    long totalUserOverrides;
    int pendingActionCount;
    int patternCount;
    int preferenceCount;
    int constraintCount;

    public AutonomousPerformanceMetricsBuilder totalEventsProcessed(long v) { this.totalEventsProcessed = v; return this; }
    public AutonomousPerformanceMetricsBuilder totalAutonomousActions(long v) { this.totalAutonomousActions = v; return this; }
    public AutonomousPerformanceMetricsBuilder totalPatternDetections(long v) { this.totalPatternDetections = v; return this; }
    public AutonomousPerformanceMetricsBuilder totalSafetyViolations(long v) { this.totalSafetyViolations = v; return this; }
    public AutonomousPerformanceMetricsBuilder totalUserOverrides(long v) { this.totalUserOverrides = v; return this; }
    public AutonomousPerformanceMetricsBuilder pendingActionCount(int v) { this.pendingActionCount = v; return this; }
    public AutonomousPerformanceMetricsBuilder patternCount(int v) { this.patternCount = v; return this; }
    public AutonomousPerformanceMetricsBuilder preferenceCount(int v) { this.preferenceCount = v; return this; }
    public AutonomousPerformanceMetricsBuilder constraintCount(int v) { this.constraintCount = v; return this; }
    public AutonomousPerformanceMetrics build() { return new AutonomousPerformanceMetrics(this); }
}


