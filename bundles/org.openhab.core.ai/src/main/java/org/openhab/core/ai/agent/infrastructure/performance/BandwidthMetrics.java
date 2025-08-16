package org.openhab.core.ai.agent.infrastructure.performance;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Bandwidth metrics for agent communication performance monitoring
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class BandwidthMetrics {
    private final String agentId;
    private final List<Long> bandwidthSamples = new ArrayList<>();
    private final AtomicLong totalBytes = new AtomicLong(0);

    public BandwidthMetrics(String agentId) {
        this.agentId = agentId;
    }

    public void recordBandwidth(long bytesPerSecond) {
        synchronized (bandwidthSamples) {
            bandwidthSamples.add(bytesPerSecond);
            if (bandwidthSamples.size() > 100) {
                bandwidthSamples.remove(0);
            }
        }
    }

    public long getAverageBandwidth() {
        synchronized (bandwidthSamples) {
            if (bandwidthSamples.isEmpty()) {
                return 0L;
            }
            return bandwidthSamples.stream().mapToLong(Long::longValue).sum() / bandwidthSamples.size();
        }
    }

    public String getAgentId() {
        return agentId;
    }

    public long getTotalBytes() {
        return totalBytes.get();
    }
}
