package org.openhab.core.ai.agent.infrastructure.performance;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Performance history for agent communication performance monitoring
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class PerformanceHistory {
    private final String agentId;
    private final List<LatencySnapshot> latencySnapshots = new ArrayList<>();
    private final List<ThroughputSnapshot> throughputSnapshots = new ArrayList<>();
    private final List<BandwidthSnapshot> bandwidthSnapshots = new ArrayList<>();

    public PerformanceHistory(String agentId) {
        this.agentId = agentId;
    }

    public void addLatencySnapshot(Instant timestamp, @Nullable Duration latency) {
        synchronized (latencySnapshots) {
            latencySnapshots.add(new LatencySnapshot(timestamp, latency));
            if (latencySnapshots.size() > 1000) {
                latencySnapshots.remove(0);
            }
        }
    }

    public void addThroughputSnapshot(Instant timestamp, long throughput) {
        synchronized (throughputSnapshots) {
            throughputSnapshots.add(new ThroughputSnapshot(timestamp, throughput));
            if (throughputSnapshots.size() > 1000) {
                throughputSnapshots.remove(0);
            }
        }
    }

    public void addBandwidthSnapshot(Instant timestamp, long bandwidth) {
        synchronized (bandwidthSnapshots) {
            bandwidthSnapshots.add(new BandwidthSnapshot(timestamp, bandwidth));
            if (bandwidthSnapshots.size() > 1000) {
                bandwidthSnapshots.remove(0);
            }
        }
    }

    public String getAgentId() {
        return agentId;
    }

    public record LatencySnapshot(Instant timestamp, @Nullable Duration latency) {
    }

    public record ThroughputSnapshot(Instant timestamp, long throughput) {
    }

    public record BandwidthSnapshot(Instant timestamp, long bandwidth) {
    }
}
