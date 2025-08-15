package org.openhab.core.ai.agent.infrastructure.performance;

import java.time.Duration;
import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@NonNullByDefault
public record PerformanceReport(String agentId, Duration timeRange, Instant startTime, Instant endTime,
        @Nullable Duration averageLatency, long averageThroughput, long averageBandwidth, long latencyViolations,
        long throughputViolations, long bandwidthViolations) {
}
