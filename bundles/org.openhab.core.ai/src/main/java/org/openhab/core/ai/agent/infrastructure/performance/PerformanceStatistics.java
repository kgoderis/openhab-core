package org.openhab.core.ai.agent.infrastructure.performance;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public record PerformanceStatistics(long totalMessagesProcessed, long totalLatencyViolations,
        long totalThroughputViolations, long totalBandwidthViolations, int activeLatencyMetrics,
        int activeThroughputMetrics, int activeBandwidthMetrics) {
}
