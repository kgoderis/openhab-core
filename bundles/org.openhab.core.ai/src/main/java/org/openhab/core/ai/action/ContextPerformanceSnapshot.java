package org.openhab.core.ai.action;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.MetricsSnapshot;

/**
 * Snapshot class for context performance metrics.
 * 
 * Holds snapshot data for context building, optimization, and caching operations.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record ContextPerformanceSnapshot(long totalContextBuilds, long successfulContextBuilds,
        long failedContextBuilds, long totalContextBuildDurationNanos, long totalOptimizations,
        long successfulOptimizations, long totalOptimizationDurationNanos, long totalCacheOperations,
        double cacheHitRate, double contextBuildSuccessRate, double averageContextBuildTimeMs,
        double contextBuildTime95thPercentileMs, double contextBuildTime99thPercentileMs,
        long timestampMs) implements MetricsSnapshot {
}
