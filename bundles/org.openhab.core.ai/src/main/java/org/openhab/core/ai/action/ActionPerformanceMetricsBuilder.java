package org.openhab.core.ai.action;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Builder for {@link ActionPerformanceMetrics}.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class ActionPerformanceMetricsBuilder {
    String actionId = "";
    long totalExecutions = 0;
    long successfulExecutions = 0;
    long failedExecutions = 0;
    long totalExecutionTimeMs = 0;
    long minExecutionTimeMs = Long.MAX_VALUE;
    long maxExecutionTimeMs = 0;
    Instant firstExecution = Instant.now();
    Instant lastExecution = Instant.now();

    public ActionPerformanceMetricsBuilder actionId(String actionId) { this.actionId = actionId; return this; }
    public ActionPerformanceMetricsBuilder totalExecutions(long v) { this.totalExecutions = v; return this; }
    public ActionPerformanceMetricsBuilder successfulExecutions(long v) { this.successfulExecutions = v; return this; }
    public ActionPerformanceMetricsBuilder failedExecutions(long v) { this.failedExecutions = v; return this; }
    public ActionPerformanceMetricsBuilder totalExecutionTimeMs(long v) { this.totalExecutionTimeMs = v; return this; }
    public ActionPerformanceMetricsBuilder minExecutionTimeMs(long v) { this.minExecutionTimeMs = v; return this; }
    public ActionPerformanceMetricsBuilder maxExecutionTimeMs(long v) { this.maxExecutionTimeMs = v; return this; }
    public ActionPerformanceMetricsBuilder firstExecution(Instant v) { this.firstExecution = v; return this; }
    public ActionPerformanceMetricsBuilder lastExecution(Instant v) { this.lastExecution = v; return this; }

    public ActionPerformanceMetrics build() { return new ActionPerformanceMetrics(this); }
}


