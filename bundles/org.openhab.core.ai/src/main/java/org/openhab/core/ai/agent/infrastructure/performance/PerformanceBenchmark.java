package org.openhab.core.ai.agent.infrastructure.performance;

import java.time.Duration;
import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@NonNullByDefault
public record PerformanceBenchmark(String agentId, Instant startTime, Instant endTime, Duration duration,
        @Nullable MessageLatencyMetrics baselineLatency, @Nullable MessageLatencyMetrics finalLatency,
        @Nullable ThroughputMetrics baselineThroughput, @Nullable ThroughputMetrics finalThroughput,
        @Nullable BandwidthMetrics baselineBandwidth, @Nullable BandwidthMetrics finalBandwidth) {
}
